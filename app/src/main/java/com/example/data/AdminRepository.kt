package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Direct Firebase Realtime Database repository for /battery_boss_update/ with NO Firebase Auth.
 *
 * Uses FirebaseDatabase.getInstance(savedUrl) natively when google-services.json is bundled,
 * and falls back directly to the official Firebase RTDB REST API when running without it.
 */
class AdminRepository(
    context: Context,
    val prefs: PrefsManager
) {
    private val appContext = context.applicationContext

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val backendMode: FirebaseBackendMode
        get() = if (isNativeFirebaseReady()) {
            FirebaseBackendMode.NATIVE_SDK
        } else {
            FirebaseBackendMode.REST_API
        }

    fun isNativeFirebaseReady(): Boolean {
        return try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                true
            } else {
                FirebaseApp.initializeApp(appContext) != null
            }
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Validates the entered Firebase Realtime Database URL, probes reachability,
     * and saves it locally in SharedPreferences.
     */
    suspend fun verifyAndSaveDatabaseUrl(rawUrl: String): Result<ConnectionStatus> =
        withContext(Dispatchers.IO) {
            val normalized = PrefsManager.normalizeUrl(rawUrl)
            if (!PrefsManager.isValidFirebaseRtdbUrl(normalized)) {
                return@withContext Result.failure(
                    IllegalArgumentException(
                        "Invalid URL. Use https://<project>-default-rtdb.firebaseio.com or .firebasedatabase.app"
                    )
                )
            }

            val probeUrl = "$normalized/${FirebaseConstants.UPDATE_NODE_PATH}.json?shallow=true"
            val request = Request.Builder().url(probeUrl).get().build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    when {
                        response.isSuccessful -> {
                            prefs.saveDatabaseUrl(normalized)
                            Result.success(ConnectionStatus.CONNECTED)
                        }
                        response.code == 401 || response.code == 403 -> {
                            Result.failure(
                                SecurityException(
                                    "Permission denied (HTTP ${response.code}). Set .read and .write to true on /battery_boss_update/ in Firebase Realtime Database Rules."
                                )
                            )
                        }
                        response.code == 404 -> {
                            Result.failure(
                                IOException("Firebase Realtime Database not found (HTTP 404). Check the URL in Firebase Console.")
                            )
                        }
                        else -> {
                            Result.failure(
                                IOException("Unexpected response from Firebase (HTTP ${response.code}).")
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Connection probe failed for $normalized", e)
                Result.failure(
                    IOException("Could not reach Firebase endpoint. Check internet connection and URL.")
                )
            }
        }

    fun observeUpdateConfig(): Flow<SyncSnapshot> {
        val dbUrl = prefs.getDatabaseUrl()
            ?: return flow {
                emit(SyncSnapshot(ConnectionStatus.DISCONNECTED, errorMessage = "No Firebase URL saved"))
            }

        return if (isNativeFirebaseReady()) {
            observeViaNativeSdk(dbUrl)
        } else {
            observeViaRestPolling(dbUrl)
        }
    }

    private fun observeViaNativeSdk(dbUrl: String): Flow<SyncSnapshot> = callbackFlow {
        trySend(SyncSnapshot(ConnectionStatus.CONNECTING))
        val database = try {
            FirebaseDatabase.getInstance(dbUrl)
        } catch (e: Exception) {
            trySend(SyncSnapshot(ConnectionStatus.DISCONNECTED, errorMessage = e.localizedMessage))
            close()
            return@callbackFlow
        }

        val connectedRef = database.getReference(".info/connected")
        val updateRef = database.getReference(FirebaseConstants.UPDATE_NODE_PATH)

        var latestConfig: UpdateConfig? = null
        var latestStatus = ConnectionStatus.CONNECTING

        val connectionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) == true
                latestStatus = if (connected) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
                trySend(SyncSnapshot(latestStatus, latestConfig))
            }

            override fun onCancelled(error: DatabaseError) {
                latestStatus = ConnectionStatus.DISCONNECTED
                trySend(SyncSnapshot(latestStatus, latestConfig, error.message))
            }
        }

        val dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                latestConfig = parseDataSnapshot(snapshot)
                latestStatus = ConnectionStatus.CONNECTED
                trySend(SyncSnapshot(latestStatus, latestConfig))
            }

            override fun onCancelled(error: DatabaseError) {
                latestStatus = ConnectionStatus.DISCONNECTED
                trySend(SyncSnapshot(latestStatus, latestConfig, error.message))
            }
        }

        connectedRef.addValueEventListener(connectionListener)
        updateRef.addValueEventListener(dataListener)

        awaitClose {
            connectedRef.removeEventListener(connectionListener)
            updateRef.removeEventListener(dataListener)
        }
    }

    private fun observeViaRestPolling(dbUrl: String): Flow<SyncSnapshot> = flow {
        emit(SyncSnapshot(ConnectionStatus.CONNECTING))
        while (currentCoroutineContext().isActive) {
            val snapshot = fetchUpdateConfigOnceRest(dbUrl)
            emit(snapshot)
            delay(10_000L)
        }
    }

    suspend fun fetchUpdateConfigOnce(): SyncSnapshot {
        val dbUrl = prefs.getDatabaseUrl()
            ?: return SyncSnapshot(ConnectionStatus.DISCONNECTED, errorMessage = "No Firebase URL saved")

        return if (isNativeFirebaseReady()) {
            fetchUpdateConfigOnceNative(dbUrl)
        } else {
            fetchUpdateConfigOnceRest(dbUrl)
        }
    }

    private suspend fun fetchUpdateConfigOnceNative(
        dbUrl: String
    ): SyncSnapshot = suspendCancellableCoroutine { cont ->
        try {
            FirebaseDatabase.getInstance(dbUrl)
                .getReference(FirebaseConstants.UPDATE_NODE_PATH)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (cont.isActive) {
                        cont.resume(
                            SyncSnapshot(
                                status = ConnectionStatus.CONNECTED,
                                config = parseDataSnapshot(snapshot)
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) {
                        cont.resume(
                            SyncSnapshot(
                                status = ConnectionStatus.DISCONNECTED,
                                errorMessage = e.localizedMessage
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            if (cont.isActive) {
                cont.resume(SyncSnapshot(ConnectionStatus.DISCONNECTED, errorMessage = e.localizedMessage))
            }
        }
    }

    private suspend fun fetchUpdateConfigOnceRest(dbUrl: String): SyncSnapshot =
        withContext(Dispatchers.IO) {
            val url = "$dbUrl/${FirebaseConstants.UPDATE_NODE_PATH}.json"
            val request = Request.Builder().url(url).get().build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val config = if (body.isBlank() || body == "null") {
                            UpdateConfig()
                        } else {
                            parseJsonToConfig(JSONObject(body))
                        }
                        SyncSnapshot(ConnectionStatus.CONNECTED, config)
                    } else {
                        SyncSnapshot(
                            status = ConnectionStatus.DISCONNECTED,
                            errorMessage = "Firebase read error (HTTP ${response.code})"
                        )
                    }
                }
            } catch (e: Exception) {
                SyncSnapshot(
                    status = ConnectionStatus.DISCONNECTED,
                    errorMessage = e.localizedMessage ?: "Network unavailable"
                )
            }
        }

    /**
     * Writes release configuration directly to /battery_boss_update/ without authentication
     * and without overwriting download_clicks.
     */
    suspend fun saveUpdateConfig(config: UpdateConfig): Result<Unit> {
        val dbUrl = prefs.getDatabaseUrl()
            ?: return Result.failure(IllegalStateException("Firebase Database URL is not configured."))

        return if (isNativeFirebaseReady()) {
            saveViaNativeSdk(dbUrl, config)
        } else {
            saveViaRestApi(dbUrl, config)
        }
    }

    private suspend fun saveViaNativeSdk(
        dbUrl: String,
        config: UpdateConfig
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        val updates = mapOf<String, Any>(
            FirebaseConstants.FIELD_ENABLED to config.isEnabled,
            FirebaseConstants.FIELD_IS_ENABLED to config.isEnabled,
            FirebaseConstants.FIELD_VERSION to config.version.trim(),
            FirebaseConstants.FIELD_TITLE to config.title.trim(),
            FirebaseConstants.FIELD_DESCRIPTION to config.description.trim(),
            FirebaseConstants.FIELD_APK_URL to config.apkUrl.trim(),
            FirebaseConstants.FIELD_RELEASE_DATE to config.releaseDate.trim(),
            FirebaseConstants.FIELD_RELEASE_DATE_MILLIS to config.releaseDateMillis,
            FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL to config.releaseDateMillis,
            FirebaseConstants.FIELD_MANDATORY to config.isMandatory,
            FirebaseConstants.FIELD_IS_MANDATORY to config.isMandatory,
            FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
        )

        try {
            FirebaseDatabase.getInstance(dbUrl)
                .getReference(FirebaseConstants.UPDATE_NODE_PATH)
                .updateChildren(updates)
                .addOnSuccessListener {
                    if (cont.isActive) cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { error ->
                    if (cont.isActive) cont.resume(Result.failure(error))
                }
        } catch (e: Exception) {
            if (cont.isActive) cont.resume(Result.failure(e))
        }
    }

    private suspend fun saveViaRestApi(
        dbUrl: String,
        config: UpdateConfig
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put(FirebaseConstants.FIELD_ENABLED, config.isEnabled)
            put(FirebaseConstants.FIELD_IS_ENABLED, config.isEnabled)
            put(FirebaseConstants.FIELD_VERSION, config.version.trim())
            put(FirebaseConstants.FIELD_TITLE, config.title.trim())
            put(FirebaseConstants.FIELD_DESCRIPTION, config.description.trim())
            put(FirebaseConstants.FIELD_APK_URL, config.apkUrl.trim())
            put(FirebaseConstants.FIELD_RELEASE_DATE, config.releaseDate.trim())
            put(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS, config.releaseDateMillis)
            put(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL, config.releaseDateMillis)
            put(FirebaseConstants.FIELD_MANDATORY, config.isMandatory)
            put(FirebaseConstants.FIELD_IS_MANDATORY, config.isMandatory)
            put(FirebaseConstants.FIELD_UPDATED_AT, System.currentTimeMillis())
        }.toString()

        val request = Request.Builder()
            .url("$dbUrl/${FirebaseConstants.UPDATE_NODE_PATH}.json")
            .patch(payload.toRequestBody(jsonMediaType))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                when {
                    response.isSuccessful -> Result.success(Unit)
                    response.code == 401 || response.code == 403 -> Result.failure(
                        SecurityException(
                            "Write blocked by Firebase Rules (HTTP ${response.code}). Set .write to true on /battery_boss_update/."
                        )
                    )
                    else -> Result.failure(
                        IOException("Firebase write failed (HTTP ${response.code}): ${body.take(120)}")
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(IOException("Failed to save to Firebase: ${e.localizedMessage ?: "network error"}"))
        }
    }

    fun resetDatabaseUrl() {
        prefs.clearDatabaseUrl()
    }

    private fun parseDataSnapshot(snapshot: DataSnapshot): UpdateConfig {
        if (!snapshot.exists()) return UpdateConfig()
        val enabledValue = snapshot.child(FirebaseConstants.FIELD_ENABLED).getValue(Boolean::class.java)
            ?: snapshot.child(FirebaseConstants.FIELD_IS_ENABLED).getValue(Boolean::class.java)
            ?: false
        val mandatoryValue = snapshot.child(FirebaseConstants.FIELD_MANDATORY).getValue(Boolean::class.java)
            ?: snapshot.child(FirebaseConstants.FIELD_IS_MANDATORY).getValue(Boolean::class.java)
            ?: false
        val releaseDateMillisValue =
            (snapshot.child(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS).value as? Number)?.toLong()
                ?: (snapshot.child(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL).value as? Number)?.toLong()
                ?: 0L
        return UpdateConfig(
            isEnabled = enabledValue,
            version = snapshot.child(FirebaseConstants.FIELD_VERSION).getValue(String::class.java) ?: "",
            title = snapshot.child(FirebaseConstants.FIELD_TITLE).getValue(String::class.java) ?: "",
            description = snapshot.child(FirebaseConstants.FIELD_DESCRIPTION).getValue(String::class.java) ?: "",
            apkUrl = snapshot.child(FirebaseConstants.FIELD_APK_URL).getValue(String::class.java) ?: "",
            releaseDate = snapshot.child(FirebaseConstants.FIELD_RELEASE_DATE).getValue(String::class.java) ?: "",
            releaseDateMillis = releaseDateMillisValue,
            isMandatory = mandatoryValue,
            downloadClicks = (snapshot.child(FirebaseConstants.FIELD_DOWNLOAD_CLICKS).value as? Number)?.toLong() ?: 0L,
            updatedAt = (snapshot.child(FirebaseConstants.FIELD_UPDATED_AT).value as? Number)?.toLong() ?: 0L
        )
    }

    private fun parseJsonToConfig(json: JSONObject): UpdateConfig {
        val enabledValue = if (json.has(FirebaseConstants.FIELD_ENABLED)) {
            json.optBoolean(FirebaseConstants.FIELD_ENABLED, false)
        } else {
            json.optBoolean(FirebaseConstants.FIELD_IS_ENABLED, false)
        }
        val mandatoryValue = if (json.has(FirebaseConstants.FIELD_MANDATORY)) {
            json.optBoolean(FirebaseConstants.FIELD_MANDATORY, false)
        } else {
            json.optBoolean(FirebaseConstants.FIELD_IS_MANDATORY, false)
        }
        val releaseDateMillisValue = when {
            json.has(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS) ->
                json.optLong(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS, 0L)
            json.has(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL) ->
                json.optLong(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL, 0L)
            else -> 0L
        }
        return UpdateConfig(
            isEnabled = enabledValue,
            version = json.optString(FirebaseConstants.FIELD_VERSION, ""),
            title = json.optString(FirebaseConstants.FIELD_TITLE, ""),
            description = json.optString(FirebaseConstants.FIELD_DESCRIPTION, ""),
            apkUrl = json.optString(FirebaseConstants.FIELD_APK_URL, ""),
            releaseDate = json.optString(FirebaseConstants.FIELD_RELEASE_DATE, ""),
            releaseDateMillis = releaseDateMillisValue,
            isMandatory = mandatoryValue,
            downloadClicks = json.optLong(FirebaseConstants.FIELD_DOWNLOAD_CLICKS, 0L),
            updatedAt = json.optLong(FirebaseConstants.FIELD_UPDATED_AT, 0L)
        )
    }

    companion object {
        private const val TAG = "AdminRepository"
    }
}

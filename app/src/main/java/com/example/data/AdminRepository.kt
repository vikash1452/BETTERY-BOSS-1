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
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

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
        val updates = buildWriteMap(config)

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
        val payload = buildWriteJson(config).toString()

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

    suspend fun fetchLatestGitHubRelease(repoInput: String): Result<GitHubReleaseInfo> =
        withContext(Dispatchers.IO) {
            val cleanedPath = repoInput.trim()
                .removePrefix("https://github.com/")
                .removePrefix("http://github.com/")
                .removePrefix("github.com/")
                .trim('/')
                .removeSuffix(".git")

            val parts = cleanedPath.split("/").filter { it.isNotBlank() }
            if (parts.size < 2) {
                return@withContext Result.failure(
                    IllegalArgumentException("Enter repository as owner/repo (e.g. username/battery-boss)")
                )
            }
            val owner = parts[0]
            val repo = parts[1]
            val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"

            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "BatteryBossAdmin-Android")
                .get()
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    when {
                        response.isSuccessful -> {
                            val json = JSONObject(body)
                            val tag = json.optString("tag_name", "").trim()
                            val cleanVersion = tag.removePrefix("v").removePrefix("V")
                            val releaseTitle = json.optString("name", "").trim()
                                .ifBlank { "Update $tag".trim() }
                            val notes = json.optString("body", "").trim()
                            val assets = json.optJSONArray("assets")

                            var apkUrl = ""
                            var apkName = ""
                            if (assets != null) {
                                for (i in 0 until assets.length()) {
                                    val asset = assets.optJSONObject(i) ?: continue
                                    val name = asset.optString("name", "")
                                    val downloadUrl = asset.optString("browser_download_url", "")
                                    if (name.endsWith(".apk", ignoreCase = true) && downloadUrl.isNotBlank()) {
                                        apkUrl = downloadUrl
                                        apkName = name
                                        break
                                    }
                                }
                            }

                            if (apkUrl.isBlank()) {
                                Result.failure(
                                    IOException("Latest release ($tag) has no .apk file attached in Assets.")
                                )
                            } else {
                                Result.success(
                                    GitHubReleaseInfo(
                                        version = cleanVersion.ifBlank { tag },
                                        title = releaseTitle,
                                        description = notes,
                                        apkDownloadUrl = apkUrl,
                                        assetFileName = apkName
                                    )
                                )
                            }
                        }
                        response.code == 404 -> Result.failure(
                            IOException("No public release found for $owner/$repo. Publish a Release with an .apk asset first.")
                        )
                        response.code == 403 -> Result.failure(
                            IOException("GitHub API rate limit reached or repository is private (HTTP 403).")
                        )
                        else -> Result.failure(
                            IOException("GitHub API error (HTTP ${response.code}).")
                        )
                    }
                }
            } catch (e: Exception) {
                Result.failure(IOException("Could not fetch GitHub release: ${e.localizedMessage ?: "network error"}"))
            }
        }

    private fun buildWriteMap(config: UpdateConfig): Map<String, Any> {
        val syncedTitle = config.uiConfig.title.ifBlank { config.title }.trim()
        val syncedDescription = config.uiConfig.description.ifBlank { config.description }.trim()
        val ui = config.uiConfig.copy(
            title = syncedTitle,
            description = syncedDescription
        )
        val resolvedMediaUrl = ui.resolvedMediaUrl()

        val buttonMaps = ui.buttons.map { btn ->
            mapOf<String, Any>(
                FirebaseConstants.BTN_ID to btn.id.trim(),
                FirebaseConstants.BTN_TEXT to btn.text.trim(),
                FirebaseConstants.BTN_ACTION to btn.action,
                FirebaseConstants.BTN_URL to btn.url.trim(),
                FirebaseConstants.BTN_SHARE_TEXT to btn.shareText.trim(),
                FirebaseConstants.BTN_STYLE to btn.style,
                FirebaseConstants.BTN_BG_COLOR to btn.bgColor.trim(),
                FirebaseConstants.BTN_GRADIENT_END to btn.gradientEnd.trim(),
                FirebaseConstants.BTN_TEXT_COLOR to btn.textColor.trim(),
                FirebaseConstants.BTN_ICON to btn.icon,
                FirebaseConstants.BTN_CORNER_RADIUS_DP to btn.cornerRadiusDp.coerceIn(4, 50),
                FirebaseConstants.BTN_ROW_GROUP to btn.rowGroup.trim()
            )
        }

        val uiConfigMap = mapOf<String, Any>(
            FirebaseConstants.UI_LAYOUT_STYLE to ui.layoutStyle,
            FirebaseConstants.UI_ENTRANCE_ANIMATION to ui.entranceAnimation,
            FirebaseConstants.UI_ANIMATION_TYPE to ui.animationType,
            FirebaseConstants.UI_SHOW_HEADER_WAVE to ui.showHeaderWave,
            FirebaseConstants.UI_SHOW_SPARKLES to ui.showSparkles,
            FirebaseConstants.UI_SHOW_CONFETTI to ui.showConfetti,
            FirebaseConstants.UI_TITLE_ALIGN to ui.titleAlign,
            FirebaseConstants.UI_MEDIA_TYPE to ui.mediaType,
            FirebaseConstants.UI_MEDIA_URL to resolvedMediaUrl,
            FirebaseConstants.UI_IMAGE_URL to ui.imageUrl.trim(),
            FirebaseConstants.UI_GIF_URL to ui.gifUrl.trim(),
            FirebaseConstants.UI_VIDEO_URL to ui.videoUrl.trim(),
            FirebaseConstants.UI_THUMBNAIL_URL to ui.thumbnailUrl.trim(),
            FirebaseConstants.UI_MEDIA_HEIGHT_DP to ui.mediaHeightDp.coerceIn(90, 460),
            FirebaseConstants.UI_MEDIA_CORNER_RADIUS_DP to ui.mediaCornerRadiusDp.coerceIn(0, 36),
            FirebaseConstants.UI_MEDIA_CONTENT_SCALE to ui.mediaContentScale,
            FirebaseConstants.UI_VIDEO_AUTOPLAY to ui.videoAutoplay,
            FirebaseConstants.UI_VIDEO_LOOP to ui.videoLoop,
            FirebaseConstants.UI_VIDEO_MUTED to ui.videoMuted,
            FirebaseConstants.UI_VIDEO_SHOW_CONTROLS to ui.videoShowControls,
            FirebaseConstants.UI_TITLE to ui.title,
            FirebaseConstants.UI_SUBTITLE to ui.subtitle.trim(),
            FirebaseConstants.UI_BADGE_TEXT to ui.badgeText.trim(),
            FirebaseConstants.UI_DESCRIPTION to ui.description,
            FirebaseConstants.UI_HIGHLIGHT_TITLE to ui.highlightTitle.trim(),
            FirebaseConstants.UI_HIGHLIGHT_TEXT to ui.highlightText.trim(),
            FirebaseConstants.UI_FOOTER_TEXT to ui.footerText.trim(),
            FirebaseConstants.UI_BACKDROP_COLOR to ui.backdropColor.trim(),
            FirebaseConstants.UI_BACKDROP_GRADIENT_END to ui.backdropGradientEnd.trim(),
            FirebaseConstants.UI_BACKDROP_IMAGE_URL to ui.backdropImageUrl.trim(),
            FirebaseConstants.UI_BACKDROP_DIM_ALPHA to ui.backdropDimAlpha.coerceIn(0f, 0.92f).toDouble(),
            FirebaseConstants.UI_CARD_COLOR to ui.cardColor.trim(),
            FirebaseConstants.UI_CARD_GRADIENT_END to ui.cardGradientEnd.trim(),
            FirebaseConstants.UI_CARD_BORDER_COLOR to ui.cardBorderColor.trim(),
            FirebaseConstants.UI_CARD_CORNER_RADIUS to ui.cardCornerRadius.coerceIn(0, 44),
            FirebaseConstants.UI_HEADER_COLOR to ui.headerColor.trim(),
            FirebaseConstants.UI_HEADER_GRADIENT_END to ui.headerGradientEnd.trim(),
            FirebaseConstants.UI_TITLE_COLOR to ui.titleColor.trim(),
            FirebaseConstants.UI_SUBTITLE_COLOR to ui.subtitleColor.trim(),
            FirebaseConstants.UI_DESCRIPTION_COLOR to ui.descriptionColor.trim(),
            FirebaseConstants.UI_VERSION_COLOR to ui.versionColor.trim(),
            FirebaseConstants.UI_ACCENT_COLOR to ui.accentColor.trim(),
            FirebaseConstants.UI_BADGE_COLOR to ui.badgeColor.trim(),
            FirebaseConstants.UI_BADGE_TEXT_COLOR to ui.badgeTextColor.trim(),
            FirebaseConstants.UI_HIGHLIGHT_BG_COLOR to ui.highlightBgColor.trim(),
            FirebaseConstants.UI_HIGHLIGHT_TEXT_COLOR to ui.highlightTextColor.trim(),
            FirebaseConstants.UI_FOOTER_COLOR to ui.footerColor.trim(),
            FirebaseConstants.UI_SHOW_PRIMARY_BUTTON to ui.showPrimaryButton,
            FirebaseConstants.UI_PRIMARY_BUTTON_TEXT to ui.primaryButtonText.trim(),
            FirebaseConstants.UI_PRIMARY_BUTTON_URL to ui.primaryButtonUrl.trim(),
            FirebaseConstants.UI_PRIMARY_BUTTON_ACTION to ui.primaryButtonAction,
            FirebaseConstants.UI_PRIMARY_BUTTON_ICON to ui.primaryButtonIcon,
            FirebaseConstants.UI_PRIMARY_BUTTON_COLOR to ui.primaryButtonColor.trim(),
            FirebaseConstants.UI_PRIMARY_BUTTON_GRADIENT_END to ui.primaryButtonGradientEnd.trim(),
            FirebaseConstants.UI_PRIMARY_BUTTON_TEXT_COLOR to ui.primaryButtonTextColor.trim(),
            FirebaseConstants.UI_PRIMARY_BUTTON_PULSE to ui.primaryButtonPulse,
            FirebaseConstants.UI_SHOW_NOT_NOW to ui.showNotNow,
            FirebaseConstants.UI_NOT_NOW_TEXT to ui.notNowText.trim(),
            FirebaseConstants.UI_NOT_NOW_COLOR to ui.notNowColor.trim(),
            FirebaseConstants.UI_BUTTONS to buttonMaps,
            FirebaseConstants.UI_BLOCKS to ui.uiBlocks
        )

        return mapOf(
            FirebaseConstants.FIELD_ENABLED to config.isEnabled,
            FirebaseConstants.FIELD_IS_ENABLED to config.isEnabled,
            FirebaseConstants.FIELD_DIALOG_MODE to config.dialogMode,
            FirebaseConstants.FIELD_MANDATORY to config.isMandatory,
            FirebaseConstants.FIELD_IS_MANDATORY to config.isMandatory,
            FirebaseConstants.FIELD_DIALOG_ID to config.dialogId.trim(),
            FirebaseConstants.FIELD_SHOW_ONCE to config.showOnce,
            FirebaseConstants.FIELD_VERSION to config.version.trim(),
            FirebaseConstants.FIELD_APK_URL to config.apkUrl.trim(),
            FirebaseConstants.FIELD_TITLE to syncedTitle,
            FirebaseConstants.FIELD_DESCRIPTION to syncedDescription,
            FirebaseConstants.FIELD_RELEASE_DATE to config.releaseDate.trim(),
            FirebaseConstants.FIELD_RELEASE_DATE_MILLIS to config.releaseDateMillis,
            FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL to config.releaseDateMillis,
            FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis(),
            FirebaseConstants.FIELD_UI_CONFIG to uiConfigMap
        )
    }

    private fun buildWriteJson(config: UpdateConfig): JSONObject {
        val syncedTitle = config.uiConfig.title.ifBlank { config.title }.trim()
        val syncedDescription = config.uiConfig.description.ifBlank { config.description }.trim()
        val ui = config.uiConfig.copy(
            title = syncedTitle,
            description = syncedDescription
        )
        val resolvedMediaUrl = ui.resolvedMediaUrl()

        val buttonsArray = JSONArray().apply {
            ui.buttons.forEach { btn ->
                put(
                    JSONObject().apply {
                        put(FirebaseConstants.BTN_ID, btn.id.trim())
                        put(FirebaseConstants.BTN_TEXT, btn.text.trim())
                        put(FirebaseConstants.BTN_ACTION, btn.action)
                        put(FirebaseConstants.BTN_URL, btn.url.trim())
                        put(FirebaseConstants.BTN_SHARE_TEXT, btn.shareText.trim())
                        put(FirebaseConstants.BTN_STYLE, btn.style)
                        put(FirebaseConstants.BTN_BG_COLOR, btn.bgColor.trim())
                        put(FirebaseConstants.BTN_GRADIENT_END, btn.gradientEnd.trim())
                        put(FirebaseConstants.BTN_TEXT_COLOR, btn.textColor.trim())
                        put(FirebaseConstants.BTN_ICON, btn.icon)
                        put(FirebaseConstants.BTN_CORNER_RADIUS_DP, btn.cornerRadiusDp.coerceIn(4, 50))
                        put(FirebaseConstants.BTN_ROW_GROUP, btn.rowGroup.trim())
                    }
                )
            }
        }

        val blocksArray = JSONArray().apply {
            ui.uiBlocks.forEach { put(it) }
        }

        val uiJson = JSONObject().apply {
            put(FirebaseConstants.UI_LAYOUT_STYLE, ui.layoutStyle)
            put(FirebaseConstants.UI_ENTRANCE_ANIMATION, ui.entranceAnimation)
            put(FirebaseConstants.UI_ANIMATION_TYPE, ui.animationType)
            put(FirebaseConstants.UI_SHOW_HEADER_WAVE, ui.showHeaderWave)
            put(FirebaseConstants.UI_SHOW_SPARKLES, ui.showSparkles)
            put(FirebaseConstants.UI_SHOW_CONFETTI, ui.showConfetti)
            put(FirebaseConstants.UI_TITLE_ALIGN, ui.titleAlign)
            put(FirebaseConstants.UI_MEDIA_TYPE, ui.mediaType)
            put(FirebaseConstants.UI_MEDIA_URL, resolvedMediaUrl)
            put(FirebaseConstants.UI_IMAGE_URL, ui.imageUrl.trim())
            put(FirebaseConstants.UI_GIF_URL, ui.gifUrl.trim())
            put(FirebaseConstants.UI_VIDEO_URL, ui.videoUrl.trim())
            put(FirebaseConstants.UI_THUMBNAIL_URL, ui.thumbnailUrl.trim())
            put(FirebaseConstants.UI_MEDIA_HEIGHT_DP, ui.mediaHeightDp.coerceIn(90, 460))
            put(FirebaseConstants.UI_MEDIA_CORNER_RADIUS_DP, ui.mediaCornerRadiusDp.coerceIn(0, 36))
            put(FirebaseConstants.UI_MEDIA_CONTENT_SCALE, ui.mediaContentScale)
            put(FirebaseConstants.UI_VIDEO_AUTOPLAY, ui.videoAutoplay)
            put(FirebaseConstants.UI_VIDEO_LOOP, ui.videoLoop)
            put(FirebaseConstants.UI_VIDEO_MUTED, ui.videoMuted)
            put(FirebaseConstants.UI_VIDEO_SHOW_CONTROLS, ui.videoShowControls)
            put(FirebaseConstants.UI_TITLE, ui.title)
            put(FirebaseConstants.UI_SUBTITLE, ui.subtitle.trim())
            put(FirebaseConstants.UI_BADGE_TEXT, ui.badgeText.trim())
            put(FirebaseConstants.UI_DESCRIPTION, ui.description)
            put(FirebaseConstants.UI_HIGHLIGHT_TITLE, ui.highlightTitle.trim())
            put(FirebaseConstants.UI_HIGHLIGHT_TEXT, ui.highlightText.trim())
            put(FirebaseConstants.UI_FOOTER_TEXT, ui.footerText.trim())
            put(FirebaseConstants.UI_BACKDROP_COLOR, ui.backdropColor.trim())
            put(FirebaseConstants.UI_BACKDROP_GRADIENT_END, ui.backdropGradientEnd.trim())
            put(FirebaseConstants.UI_BACKDROP_IMAGE_URL, ui.backdropImageUrl.trim())
            put(FirebaseConstants.UI_BACKDROP_DIM_ALPHA, ui.backdropDimAlpha.coerceIn(0f, 0.92f).toDouble())
            put(FirebaseConstants.UI_CARD_COLOR, ui.cardColor.trim())
            put(FirebaseConstants.UI_CARD_GRADIENT_END, ui.cardGradientEnd.trim())
            put(FirebaseConstants.UI_CARD_BORDER_COLOR, ui.cardBorderColor.trim())
            put(FirebaseConstants.UI_CARD_CORNER_RADIUS, ui.cardCornerRadius.coerceIn(0, 44))
            put(FirebaseConstants.UI_HEADER_COLOR, ui.headerColor.trim())
            put(FirebaseConstants.UI_HEADER_GRADIENT_END, ui.headerGradientEnd.trim())
            put(FirebaseConstants.UI_TITLE_COLOR, ui.titleColor.trim())
            put(FirebaseConstants.UI_SUBTITLE_COLOR, ui.subtitleColor.trim())
            put(FirebaseConstants.UI_DESCRIPTION_COLOR, ui.descriptionColor.trim())
            put(FirebaseConstants.UI_VERSION_COLOR, ui.versionColor.trim())
            put(FirebaseConstants.UI_ACCENT_COLOR, ui.accentColor.trim())
            put(FirebaseConstants.UI_BADGE_COLOR, ui.badgeColor.trim())
            put(FirebaseConstants.UI_BADGE_TEXT_COLOR, ui.badgeTextColor.trim())
            put(FirebaseConstants.UI_HIGHLIGHT_BG_COLOR, ui.highlightBgColor.trim())
            put(FirebaseConstants.UI_HIGHLIGHT_TEXT_COLOR, ui.highlightTextColor.trim())
            put(FirebaseConstants.UI_FOOTER_COLOR, ui.footerColor.trim())
            put(FirebaseConstants.UI_SHOW_PRIMARY_BUTTON, ui.showPrimaryButton)
            put(FirebaseConstants.UI_PRIMARY_BUTTON_TEXT, ui.primaryButtonText.trim())
            put(FirebaseConstants.UI_PRIMARY_BUTTON_URL, ui.primaryButtonUrl.trim())
            put(FirebaseConstants.UI_PRIMARY_BUTTON_ACTION, ui.primaryButtonAction)
            put(FirebaseConstants.UI_PRIMARY_BUTTON_ICON, ui.primaryButtonIcon)
            put(FirebaseConstants.UI_PRIMARY_BUTTON_COLOR, ui.primaryButtonColor.trim())
            put(FirebaseConstants.UI_PRIMARY_BUTTON_GRADIENT_END, ui.primaryButtonGradientEnd.trim())
            put(FirebaseConstants.UI_PRIMARY_BUTTON_TEXT_COLOR, ui.primaryButtonTextColor.trim())
            put(FirebaseConstants.UI_PRIMARY_BUTTON_PULSE, ui.primaryButtonPulse)
            put(FirebaseConstants.UI_SHOW_NOT_NOW, ui.showNotNow)
            put(FirebaseConstants.UI_NOT_NOW_TEXT, ui.notNowText.trim())
            put(FirebaseConstants.UI_NOT_NOW_COLOR, ui.notNowColor.trim())
            put(FirebaseConstants.UI_BUTTONS, buttonsArray)
            put(FirebaseConstants.UI_BLOCKS, blocksArray)
        }

        return JSONObject().apply {
            put(FirebaseConstants.FIELD_ENABLED, config.isEnabled)
            put(FirebaseConstants.FIELD_IS_ENABLED, config.isEnabled)
            put(FirebaseConstants.FIELD_DIALOG_MODE, config.dialogMode)
            put(FirebaseConstants.FIELD_MANDATORY, config.isMandatory)
            put(FirebaseConstants.FIELD_IS_MANDATORY, config.isMandatory)
            put(FirebaseConstants.FIELD_DIALOG_ID, config.dialogId.trim())
            put(FirebaseConstants.FIELD_SHOW_ONCE, config.showOnce)
            put(FirebaseConstants.FIELD_VERSION, config.version.trim())
            put(FirebaseConstants.FIELD_APK_URL, config.apkUrl.trim())
            put(FirebaseConstants.FIELD_TITLE, syncedTitle)
            put(FirebaseConstants.FIELD_DESCRIPTION, syncedDescription)
            put(FirebaseConstants.FIELD_RELEASE_DATE, config.releaseDate.trim())
            put(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS, config.releaseDateMillis)
            put(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL, config.releaseDateMillis)
            put(FirebaseConstants.FIELD_UPDATED_AT, System.currentTimeMillis())
            put(FirebaseConstants.FIELD_UI_CONFIG, uiJson)
        }
    }

    private fun parseDataSnapshot(snapshot: DataSnapshot): UpdateConfig {
        if (!snapshot.exists()) return UpdateConfig()
        val enabledValue = snapshot.child(FirebaseConstants.FIELD_IS_ENABLED).getValue(Boolean::class.java)
            ?: snapshot.child(FirebaseConstants.FIELD_ENABLED).getValue(Boolean::class.java)
            ?: false
        val mandatoryValue = snapshot.child(FirebaseConstants.FIELD_IS_MANDATORY).getValue(Boolean::class.java)
            ?: snapshot.child(FirebaseConstants.FIELD_MANDATORY).getValue(Boolean::class.java)
            ?: false
        val releaseDateMillisValue =
            (snapshot.child(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS).value as? Number)?.toLong()
                ?: (snapshot.child(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL).value as? Number)?.toLong()
                ?: 0L

        val topTitle = snapshot.child(FirebaseConstants.FIELD_TITLE).getValue(String::class.java) ?: ""
        val topDescription = snapshot.child(FirebaseConstants.FIELD_DESCRIPTION).getValue(String::class.java) ?: ""

        val buttonClicksMap = buildMap<String, Long> {
            snapshot.child(FirebaseConstants.FIELD_BUTTON_CLICKS).children.forEach { child ->
                val key = child.key ?: return@forEach
                val count = (child.value as? Number)?.toLong() ?: 0L
                put(key, count)
            }
        }

        val uiSnap = snapshot.child(FirebaseConstants.FIELD_UI_CONFIG)
        val uiConfig = parseUiConfigSnapshot(uiSnap, topTitle, topDescription)

        return UpdateConfig(
            isEnabled = enabledValue,
            dialogMode = snapshot.child(FirebaseConstants.FIELD_DIALOG_MODE).getValue(String::class.java)
                ?.takeIf { it.isNotBlank() } ?: DialogOptions.MODE_UPDATE,
            isMandatory = mandatoryValue,
            dialogId = snapshot.child(FirebaseConstants.FIELD_DIALOG_ID).getValue(String::class.java)
                ?.takeIf { it.isNotBlank() } ?: "update_01",
            showOnce = snapshot.child(FirebaseConstants.FIELD_SHOW_ONCE).getValue(Boolean::class.java) ?: false,
            version = snapshot.child(FirebaseConstants.FIELD_VERSION).getValue(String::class.java) ?: "",
            apkUrl = snapshot.child(FirebaseConstants.FIELD_APK_URL).getValue(String::class.java) ?: "",
            title = topTitle,
            description = topDescription,
            releaseDate = snapshot.child(FirebaseConstants.FIELD_RELEASE_DATE).getValue(String::class.java) ?: "",
            releaseDateMillis = releaseDateMillisValue,
            downloadClicks = (snapshot.child(FirebaseConstants.FIELD_DOWNLOAD_CLICKS).value as? Number)?.toLong() ?: 0L,
            buttonClicks = buttonClicksMap,
            updatedAt = (snapshot.child(FirebaseConstants.FIELD_UPDATED_AT).value as? Number)?.toLong() ?: 0L,
            uiConfig = uiConfig
        )
    }

    private fun parseUiConfigSnapshot(
        snap: DataSnapshot,
        fallbackTitle: String,
        fallbackDescription: String
    ): DialogUiConfig {
        val defaults = DialogUiConfig(title = fallbackTitle, description = fallbackDescription)
        if (!snap.exists()) return defaults

        fun str(key: String, def: String) = snap.child(key).getValue(String::class.java) ?: def
        fun bool(key: String, def: Boolean) = snap.child(key).getValue(Boolean::class.java) ?: def
        fun int(key: String, def: Int) = (snap.child(key).value as? Number)?.toInt() ?: def
        fun flt(key: String, def: Float) = (snap.child(key).value as? Number)?.toFloat() ?: def

        val buttons = snap.child(FirebaseConstants.UI_BUTTONS).children.mapNotNull { b ->
            if (!b.exists()) return@mapNotNull null
            DialogButtonConfig(
                id = b.child(FirebaseConstants.BTN_ID).getValue(String::class.java) ?: "btn",
                text = b.child(FirebaseConstants.BTN_TEXT).getValue(String::class.java) ?: "",
                action = b.child(FirebaseConstants.BTN_ACTION).getValue(String::class.java)
                    ?: DialogOptions.ACTION_OPEN_URL,
                url = b.child(FirebaseConstants.BTN_URL).getValue(String::class.java) ?: "",
                shareText = b.child(FirebaseConstants.BTN_SHARE_TEXT).getValue(String::class.java) ?: "",
                style = b.child(FirebaseConstants.BTN_STYLE).getValue(String::class.java)
                    ?: DialogOptions.STYLE_FILLED,
                bgColor = b.child(FirebaseConstants.BTN_BG_COLOR).getValue(String::class.java) ?: "#00E676",
                gradientEnd = b.child(FirebaseConstants.BTN_GRADIENT_END).getValue(String::class.java) ?: "",
                textColor = b.child(FirebaseConstants.BTN_TEXT_COLOR).getValue(String::class.java) ?: "#06150B",
                icon = b.child(FirebaseConstants.BTN_ICON).getValue(String::class.java) ?: "open",
                cornerRadiusDp = (b.child(FirebaseConstants.BTN_CORNER_RADIUS_DP).value as? Number)?.toInt() ?: 14,
                rowGroup = b.child(FirebaseConstants.BTN_ROW_GROUP).getValue(String::class.java) ?: ""
            )
        }

        val blocks = snap.child(FirebaseConstants.UI_BLOCKS).children.mapNotNull {
            it.getValue(String::class.java)?.takeIf { item -> item.isNotBlank() }
        }

        val mediaType = str(FirebaseConstants.UI_MEDIA_TYPE, defaults.mediaType)
        val rawMediaUrl = str(FirebaseConstants.UI_MEDIA_URL, "")
        val imageUrl = str(FirebaseConstants.UI_IMAGE_URL, if (mediaType == "image") rawMediaUrl else "")
        val gifUrl = str(FirebaseConstants.UI_GIF_URL, if (mediaType == "gif") rawMediaUrl else "")
        val videoUrl = str(FirebaseConstants.UI_VIDEO_URL, if (mediaType == "video") rawMediaUrl else "")

        return DialogUiConfig(
            layoutStyle = str(FirebaseConstants.UI_LAYOUT_STYLE, defaults.layoutStyle),
            entranceAnimation = str(FirebaseConstants.UI_ENTRANCE_ANIMATION, defaults.entranceAnimation),
            animationType = str(FirebaseConstants.UI_ANIMATION_TYPE, defaults.animationType),
            showHeaderWave = bool(FirebaseConstants.UI_SHOW_HEADER_WAVE, defaults.showHeaderWave),
            showSparkles = bool(FirebaseConstants.UI_SHOW_SPARKLES, defaults.showSparkles),
            showConfetti = bool(FirebaseConstants.UI_SHOW_CONFETTI, defaults.showConfetti),
            titleAlign = str(FirebaseConstants.UI_TITLE_ALIGN, defaults.titleAlign),
            mediaType = mediaType,
            mediaUrl = rawMediaUrl,
            imageUrl = imageUrl,
            gifUrl = gifUrl,
            videoUrl = videoUrl,
            thumbnailUrl = str(FirebaseConstants.UI_THUMBNAIL_URL, defaults.thumbnailUrl),
            mediaHeightDp = int(FirebaseConstants.UI_MEDIA_HEIGHT_DP, defaults.mediaHeightDp),
            mediaCornerRadiusDp = int(FirebaseConstants.UI_MEDIA_CORNER_RADIUS_DP, defaults.mediaCornerRadiusDp),
            mediaContentScale = str(FirebaseConstants.UI_MEDIA_CONTENT_SCALE, defaults.mediaContentScale),
            videoAutoplay = bool(FirebaseConstants.UI_VIDEO_AUTOPLAY, defaults.videoAutoplay),
            videoLoop = bool(FirebaseConstants.UI_VIDEO_LOOP, defaults.videoLoop),
            videoMuted = bool(FirebaseConstants.UI_VIDEO_MUTED, defaults.videoMuted),
            videoShowControls = bool(FirebaseConstants.UI_VIDEO_SHOW_CONTROLS, defaults.videoShowControls),
            title = str(FirebaseConstants.UI_TITLE, fallbackTitle).ifBlank { fallbackTitle },
            subtitle = str(FirebaseConstants.UI_SUBTITLE, defaults.subtitle),
            badgeText = str(FirebaseConstants.UI_BADGE_TEXT, defaults.badgeText),
            description = str(FirebaseConstants.UI_DESCRIPTION, fallbackDescription).ifBlank { fallbackDescription },
            highlightTitle = str(FirebaseConstants.UI_HIGHLIGHT_TITLE, defaults.highlightTitle),
            highlightText = str(FirebaseConstants.UI_HIGHLIGHT_TEXT, defaults.highlightText),
            footerText = str(FirebaseConstants.UI_FOOTER_TEXT, defaults.footerText),
            backdropColor = str(FirebaseConstants.UI_BACKDROP_COLOR, defaults.backdropColor),
            backdropGradientEnd = str(FirebaseConstants.UI_BACKDROP_GRADIENT_END, defaults.backdropGradientEnd),
            backdropImageUrl = str(FirebaseConstants.UI_BACKDROP_IMAGE_URL, defaults.backdropImageUrl),
            backdropDimAlpha = flt(FirebaseConstants.UI_BACKDROP_DIM_ALPHA, defaults.backdropDimAlpha),
            cardColor = str(FirebaseConstants.UI_CARD_COLOR, defaults.cardColor),
            cardGradientEnd = str(FirebaseConstants.UI_CARD_GRADIENT_END, defaults.cardGradientEnd),
            cardBorderColor = str(FirebaseConstants.UI_CARD_BORDER_COLOR, defaults.cardBorderColor),
            cardCornerRadius = int(FirebaseConstants.UI_CARD_CORNER_RADIUS, defaults.cardCornerRadius),
            headerColor = str(FirebaseConstants.UI_HEADER_COLOR, defaults.headerColor),
            headerGradientEnd = str(FirebaseConstants.UI_HEADER_GRADIENT_END, defaults.headerGradientEnd),
            titleColor = str(FirebaseConstants.UI_TITLE_COLOR, defaults.titleColor),
            subtitleColor = str(FirebaseConstants.UI_SUBTITLE_COLOR, defaults.subtitleColor),
            descriptionColor = str(FirebaseConstants.UI_DESCRIPTION_COLOR, defaults.descriptionColor),
            versionColor = str(FirebaseConstants.UI_VERSION_COLOR, defaults.versionColor),
            accentColor = str(FirebaseConstants.UI_ACCENT_COLOR, defaults.accentColor),
            badgeColor = str(FirebaseConstants.UI_BADGE_COLOR, defaults.badgeColor),
            badgeTextColor = str(FirebaseConstants.UI_BADGE_TEXT_COLOR, defaults.badgeTextColor),
            highlightBgColor = str(FirebaseConstants.UI_HIGHLIGHT_BG_COLOR, defaults.highlightBgColor),
            highlightTextColor = str(FirebaseConstants.UI_HIGHLIGHT_TEXT_COLOR, defaults.highlightTextColor),
            footerColor = str(FirebaseConstants.UI_FOOTER_COLOR, defaults.footerColor),
            showPrimaryButton = bool(FirebaseConstants.UI_SHOW_PRIMARY_BUTTON, defaults.showPrimaryButton),
            primaryButtonText = str(FirebaseConstants.UI_PRIMARY_BUTTON_TEXT, defaults.primaryButtonText),
            primaryButtonUrl = str(FirebaseConstants.UI_PRIMARY_BUTTON_URL, defaults.primaryButtonUrl),
            primaryButtonAction = str(FirebaseConstants.UI_PRIMARY_BUTTON_ACTION, defaults.primaryButtonAction),
            primaryButtonIcon = str(FirebaseConstants.UI_PRIMARY_BUTTON_ICON, defaults.primaryButtonIcon),
            primaryButtonColor = str(FirebaseConstants.UI_PRIMARY_BUTTON_COLOR, defaults.primaryButtonColor),
            primaryButtonGradientEnd = str(FirebaseConstants.UI_PRIMARY_BUTTON_GRADIENT_END, defaults.primaryButtonGradientEnd),
            primaryButtonTextColor = str(FirebaseConstants.UI_PRIMARY_BUTTON_TEXT_COLOR, defaults.primaryButtonTextColor),
            primaryButtonPulse = bool(FirebaseConstants.UI_PRIMARY_BUTTON_PULSE, defaults.primaryButtonPulse),
            showNotNow = bool(FirebaseConstants.UI_SHOW_NOT_NOW, defaults.showNotNow),
            notNowText = str(FirebaseConstants.UI_NOT_NOW_TEXT, defaults.notNowText),
            notNowColor = str(FirebaseConstants.UI_NOT_NOW_COLOR, defaults.notNowColor),
            buttons = buttons,
            uiBlocks = blocks
        )
    }

    private fun parseJsonToConfig(json: JSONObject): UpdateConfig {
        val enabledValue = if (json.has(FirebaseConstants.FIELD_IS_ENABLED)) {
            json.optBoolean(FirebaseConstants.FIELD_IS_ENABLED, false)
        } else {
            json.optBoolean(FirebaseConstants.FIELD_ENABLED, false)
        }
        val mandatoryValue = if (json.has(FirebaseConstants.FIELD_IS_MANDATORY)) {
            json.optBoolean(FirebaseConstants.FIELD_IS_MANDATORY, false)
        } else {
            json.optBoolean(FirebaseConstants.FIELD_MANDATORY, false)
        }
        val releaseDateMillisValue = when {
            json.has(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS) ->
                json.optLong(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS, 0L)
            json.has(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL) ->
                json.optLong(FirebaseConstants.FIELD_RELEASE_DATE_MILLIS_CAMEL, 0L)
            else -> 0L
        }
        val topTitle = json.optString(FirebaseConstants.FIELD_TITLE, "")
        val topDescription = json.optString(FirebaseConstants.FIELD_DESCRIPTION, "")

        val clicksJson = json.optJSONObject(FirebaseConstants.FIELD_BUTTON_CLICKS)
        val buttonClicksMap = buildMap<String, Long> {
            if (clicksJson != null) {
                val keys = clicksJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, clicksJson.optLong(key, 0L))
                }
            }
        }

        val uiJson = json.optJSONObject(FirebaseConstants.FIELD_UI_CONFIG)
        val uiConfig = parseUiConfigJson(uiJson, topTitle, topDescription)

        return UpdateConfig(
            isEnabled = enabledValue,
            dialogMode = json.optString(FirebaseConstants.FIELD_DIALOG_MODE, DialogOptions.MODE_UPDATE)
                .ifBlank { DialogOptions.MODE_UPDATE },
            isMandatory = mandatoryValue,
            dialogId = json.optString(FirebaseConstants.FIELD_DIALOG_ID, "update_01")
                .ifBlank { "update_01" },
            showOnce = json.optBoolean(FirebaseConstants.FIELD_SHOW_ONCE, false),
            version = json.optString(FirebaseConstants.FIELD_VERSION, ""),
            apkUrl = json.optString(FirebaseConstants.FIELD_APK_URL, ""),
            title = topTitle,
            description = topDescription,
            releaseDate = json.optString(FirebaseConstants.FIELD_RELEASE_DATE, ""),
            releaseDateMillis = releaseDateMillisValue,
            downloadClicks = json.optLong(FirebaseConstants.FIELD_DOWNLOAD_CLICKS, 0L),
            buttonClicks = buttonClicksMap,
            updatedAt = json.optLong(FirebaseConstants.FIELD_UPDATED_AT, 0L),
            uiConfig = uiConfig
        )
    }

    private fun parseUiConfigJson(
        json: JSONObject?,
        fallbackTitle: String,
        fallbackDescription: String
    ): DialogUiConfig {
        val defaults = DialogUiConfig(title = fallbackTitle, description = fallbackDescription)
        if (json == null) return defaults

        fun str(key: String, def: String) = if (json.has(key) && !json.isNull(key)) json.optString(key, def) else def
        fun bool(key: String, def: Boolean) = if (json.has(key) && !json.isNull(key)) json.optBoolean(key, def) else def
        fun int(key: String, def: Int) = if (json.has(key) && !json.isNull(key)) json.optInt(key, def) else def
        fun flt(key: String, def: Float) = if (json.has(key) && !json.isNull(key)) json.optDouble(key, def.toDouble()).toFloat() else def

        val buttonsList = buildList {
            val arr = json.optJSONArray(FirebaseConstants.UI_BUTTONS)
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val b = arr.optJSONObject(i) ?: continue
                    add(parseButtonJson(b))
                }
            } else {
                val obj = json.optJSONObject(FirebaseConstants.UI_BUTTONS)
                if (obj != null) {
                    val keys = obj.keys().asSequence().toList().sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
                    for (k in keys) {
                        val b = obj.optJSONObject(k) ?: continue
                        add(parseButtonJson(b))
                    }
                }
            }
        }

        val blocksList = buildList {
            val arr = json.optJSONArray(FirebaseConstants.UI_BLOCKS)
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i, "")
                    if (item.isNotBlank()) add(item)
                }
            } else {
                val obj = json.optJSONObject(FirebaseConstants.UI_BLOCKS)
                if (obj != null) {
                    val keys = obj.keys().asSequence().toList().sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
                    for (k in keys) {
                        val item = obj.optString(k, "")
                        if (item.isNotBlank()) add(item)
                    }
                }
            }
        }

        val mediaType = str(FirebaseConstants.UI_MEDIA_TYPE, defaults.mediaType)
        val rawMediaUrl = str(FirebaseConstants.UI_MEDIA_URL, "")
        val imageUrl = str(FirebaseConstants.UI_IMAGE_URL, if (mediaType == "image") rawMediaUrl else "")
        val gifUrl = str(FirebaseConstants.UI_GIF_URL, if (mediaType == "gif") rawMediaUrl else "")
        val videoUrl = str(FirebaseConstants.UI_VIDEO_URL, if (mediaType == "video") rawMediaUrl else "")

        return DialogUiConfig(
            layoutStyle = str(FirebaseConstants.UI_LAYOUT_STYLE, defaults.layoutStyle),
            entranceAnimation = str(FirebaseConstants.UI_ENTRANCE_ANIMATION, defaults.entranceAnimation),
            animationType = str(FirebaseConstants.UI_ANIMATION_TYPE, defaults.animationType),
            showHeaderWave = bool(FirebaseConstants.UI_SHOW_HEADER_WAVE, defaults.showHeaderWave),
            showSparkles = bool(FirebaseConstants.UI_SHOW_SPARKLES, defaults.showSparkles),
            showConfetti = bool(FirebaseConstants.UI_SHOW_CONFETTI, defaults.showConfetti),
            titleAlign = str(FirebaseConstants.UI_TITLE_ALIGN, defaults.titleAlign),
            mediaType = mediaType,
            mediaUrl = rawMediaUrl,
            imageUrl = imageUrl,
            gifUrl = gifUrl,
            videoUrl = videoUrl,
            thumbnailUrl = str(FirebaseConstants.UI_THUMBNAIL_URL, defaults.thumbnailUrl),
            mediaHeightDp = int(FirebaseConstants.UI_MEDIA_HEIGHT_DP, defaults.mediaHeightDp),
            mediaCornerRadiusDp = int(FirebaseConstants.UI_MEDIA_CORNER_RADIUS_DP, defaults.mediaCornerRadiusDp),
            mediaContentScale = str(FirebaseConstants.UI_MEDIA_CONTENT_SCALE, defaults.mediaContentScale),
            videoAutoplay = bool(FirebaseConstants.UI_VIDEO_AUTOPLAY, defaults.videoAutoplay),
            videoLoop = bool(FirebaseConstants.UI_VIDEO_LOOP, defaults.videoLoop),
            videoMuted = bool(FirebaseConstants.UI_VIDEO_MUTED, defaults.videoMuted),
            videoShowControls = bool(FirebaseConstants.UI_VIDEO_SHOW_CONTROLS, defaults.videoShowControls),
            title = str(FirebaseConstants.UI_TITLE, fallbackTitle).ifBlank { fallbackTitle },
            subtitle = str(FirebaseConstants.UI_SUBTITLE, defaults.subtitle),
            badgeText = str(FirebaseConstants.UI_BADGE_TEXT, defaults.badgeText),
            description = str(FirebaseConstants.UI_DESCRIPTION, fallbackDescription).ifBlank { fallbackDescription },
            highlightTitle = str(FirebaseConstants.UI_HIGHLIGHT_TITLE, defaults.highlightTitle),
            highlightText = str(FirebaseConstants.UI_HIGHLIGHT_TEXT, defaults.highlightText),
            footerText = str(FirebaseConstants.UI_FOOTER_TEXT, defaults.footerText),
            backdropColor = str(FirebaseConstants.UI_BACKDROP_COLOR, defaults.backdropColor),
            backdropGradientEnd = str(FirebaseConstants.UI_BACKDROP_GRADIENT_END, defaults.backdropGradientEnd),
            backdropImageUrl = str(FirebaseConstants.UI_BACKDROP_IMAGE_URL, defaults.backdropImageUrl),
            backdropDimAlpha = flt(FirebaseConstants.UI_BACKDROP_DIM_ALPHA, defaults.backdropDimAlpha),
            cardColor = str(FirebaseConstants.UI_CARD_COLOR, defaults.cardColor),
            cardGradientEnd = str(FirebaseConstants.UI_CARD_GRADIENT_END, defaults.cardGradientEnd),
            cardBorderColor = str(FirebaseConstants.UI_CARD_BORDER_COLOR, defaults.cardBorderColor),
            cardCornerRadius = int(FirebaseConstants.UI_CARD_CORNER_RADIUS, defaults.cardCornerRadius),
            headerColor = str(FirebaseConstants.UI_HEADER_COLOR, defaults.headerColor),
            headerGradientEnd = str(FirebaseConstants.UI_HEADER_GRADIENT_END, defaults.headerGradientEnd),
            titleColor = str(FirebaseConstants.UI_TITLE_COLOR, defaults.titleColor),
            subtitleColor = str(FirebaseConstants.UI_SUBTITLE_COLOR, defaults.subtitleColor),
            descriptionColor = str(FirebaseConstants.UI_DESCRIPTION_COLOR, defaults.descriptionColor),
            versionColor = str(FirebaseConstants.UI_VERSION_COLOR, defaults.versionColor),
            accentColor = str(FirebaseConstants.UI_ACCENT_COLOR, defaults.accentColor),
            badgeColor = str(FirebaseConstants.UI_BADGE_COLOR, defaults.badgeColor),
            badgeTextColor = str(FirebaseConstants.UI_BADGE_TEXT_COLOR, defaults.badgeTextColor),
            highlightBgColor = str(FirebaseConstants.UI_HIGHLIGHT_BG_COLOR, defaults.highlightBgColor),
            highlightTextColor = str(FirebaseConstants.UI_HIGHLIGHT_TEXT_COLOR, defaults.highlightTextColor),
            footerColor = str(FirebaseConstants.UI_FOOTER_COLOR, defaults.footerColor),
            showPrimaryButton = bool(FirebaseConstants.UI_SHOW_PRIMARY_BUTTON, defaults.showPrimaryButton),
            primaryButtonText = str(FirebaseConstants.UI_PRIMARY_BUTTON_TEXT, defaults.primaryButtonText),
            primaryButtonUrl = str(FirebaseConstants.UI_PRIMARY_BUTTON_URL, defaults.primaryButtonUrl),
            primaryButtonAction = str(FirebaseConstants.UI_PRIMARY_BUTTON_ACTION, defaults.primaryButtonAction),
            primaryButtonIcon = str(FirebaseConstants.UI_PRIMARY_BUTTON_ICON, defaults.primaryButtonIcon),
            primaryButtonColor = str(FirebaseConstants.UI_PRIMARY_BUTTON_COLOR, defaults.primaryButtonColor),
            primaryButtonGradientEnd = str(FirebaseConstants.UI_PRIMARY_BUTTON_GRADIENT_END, defaults.primaryButtonGradientEnd),
            primaryButtonTextColor = str(FirebaseConstants.UI_PRIMARY_BUTTON_TEXT_COLOR, defaults.primaryButtonTextColor),
            primaryButtonPulse = bool(FirebaseConstants.UI_PRIMARY_BUTTON_PULSE, defaults.primaryButtonPulse),
            showNotNow = bool(FirebaseConstants.UI_SHOW_NOT_NOW, defaults.showNotNow),
            notNowText = str(FirebaseConstants.UI_NOT_NOW_TEXT, defaults.notNowText),
            notNowColor = str(FirebaseConstants.UI_NOT_NOW_COLOR, defaults.notNowColor),
            buttons = buttonsList,
            uiBlocks = blocksList
        )
    }

    private fun parseButtonJson(b: JSONObject): DialogButtonConfig {
        return DialogButtonConfig(
            id = b.optString(FirebaseConstants.BTN_ID, "btn"),
            text = b.optString(FirebaseConstants.BTN_TEXT, ""),
            action = b.optString(FirebaseConstants.BTN_ACTION, DialogOptions.ACTION_OPEN_URL),
            url = b.optString(FirebaseConstants.BTN_URL, ""),
            shareText = b.optString(FirebaseConstants.BTN_SHARE_TEXT, ""),
            style = b.optString(FirebaseConstants.BTN_STYLE, DialogOptions.STYLE_FILLED),
            bgColor = b.optString(FirebaseConstants.BTN_BG_COLOR, "#00E676"),
            gradientEnd = b.optString(FirebaseConstants.BTN_GRADIENT_END, ""),
            textColor = b.optString(FirebaseConstants.BTN_TEXT_COLOR, "#06150B"),
            icon = b.optString(FirebaseConstants.BTN_ICON, "open"),
            cornerRadiusDp = b.optInt(FirebaseConstants.BTN_CORNER_RADIUS_DP, 14),
            rowGroup = b.optString(FirebaseConstants.BTN_ROW_GROUP, "")
        )
    }

    companion object {
        private const val TAG = "AdminRepository"
    }
}

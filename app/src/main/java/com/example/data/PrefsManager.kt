package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Local SharedPreferences storage for the dynamically configured Firebase Realtime Database URL.
 * Once saved, future app launches skip the Setup Screen and open the Admin Dashboard directly.
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveDatabaseUrl(url: String) {
        prefs.edit().putString(KEY_FIREBASE_DB_URL, normalizeUrl(url)).apply()
    }

    fun getDatabaseUrl(): String? {
        val saved = prefs.getString(KEY_FIREBASE_DB_URL, null)?.trim()
        return saved?.takeIf { it.isNotEmpty() }
    }

    fun hasDatabaseUrl(): Boolean = getDatabaseUrl() != null

    fun clearDatabaseUrl() {
        prefs.edit().remove(KEY_FIREBASE_DB_URL).apply()
    }

    companion object {
        private const val PREFS_NAME = "battery_boss_admin_prefs"
        private const val KEY_FIREBASE_DB_URL = "firebase_db_url"

        fun normalizeUrl(raw: String): String {
            var cleaned = raw.trim()
            if (cleaned.isEmpty()) return ""
            if (!cleaned.startsWith("https://", ignoreCase = true) &&
                !cleaned.startsWith("http://", ignoreCase = true)
            ) {
                cleaned = "https://$cleaned"
            }
            return cleaned.trimEnd('/')
        }

        fun isValidFirebaseRtdbUrl(raw: String): Boolean {
            val normalized = normalizeUrl(raw)
            if (!normalized.startsWith("https://", ignoreCase = true)) return false
            val host = normalized.removePrefix("https://").substringBefore("/")
            return host.endsWith(".firebaseio.com", ignoreCase = true) ||
                host.endsWith(".firebasedatabase.app", ignoreCase = true)
        }
    }
}

package com.example.data

enum class ConnectionStatus {
    CONNECTING,
    CONNECTED,
    DISCONNECTED
}

enum class FirebaseBackendMode {
    NATIVE_SDK,
    REST_API
}

data class UpdateConfig(
    val isEnabled: Boolean = false,
    val version: String = "",
    val title: String = "",
    val description: String = "",
    val apkUrl: String = "",
    val releaseDate: String = "",
    val releaseDateMillis: Long = 0L,
    val isMandatory: Boolean = false,
    val downloadClicks: Long = 0L,
    val updatedAt: Long = 0L
)

data class SyncSnapshot(
    val status: ConnectionStatus,
    val config: UpdateConfig? = null,
    val errorMessage: String? = null
)

data class GitHubReleaseInfo(
    val version: String,
    val title: String,
    val description: String,
    val apkDownloadUrl: String,
    val assetFileName: String
)

object FirebaseConstants {
    const val UPDATE_NODE_PATH = "battery_boss_update"

    const val FIELD_ENABLED = "enabled"
    const val FIELD_IS_ENABLED = "is_enabled"
    const val FIELD_VERSION = "version"
    const val FIELD_TITLE = "title"
    const val FIELD_DESCRIPTION = "description"
    const val FIELD_APK_URL = "apk_url"
    const val FIELD_RELEASE_DATE = "release_date"
    const val FIELD_RELEASE_DATE_MILLIS = "release_date_millis"
    const val FIELD_RELEASE_DATE_MILLIS_CAMEL = "releaseDateMillis"
    const val FIELD_MANDATORY = "mandatory"
    const val FIELD_IS_MANDATORY = "is_mandatory"
    const val FIELD_DOWNLOAD_CLICKS = "download_clicks"
    const val FIELD_UPDATED_AT = "updated_at"

    val SECURITY_RULES_JSON: String = """
        {
          "rules": {
            ".read": false,
            ".write": false,
            "battery_boss_update": {
              ".read": true,
              ".write": true
            }
          }
        }
    """.trimIndent()
}

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

data class DialogButtonConfig(
    val id: String = "btn_1",
    val text: String = "Open Link",
    val action: String = DialogOptions.ACTION_OPEN_URL,
    val url: String = "",
    val shareText: String = "",
    val style: String = DialogOptions.STYLE_FILLED,
    val bgColor: String = "#00E676",
    val gradientEnd: String = "",
    val textColor: String = "#06150B",
    val icon: String = "open",
    val cornerRadiusDp: Int = 14,
    val rowGroup: String = ""
)

data class DialogUiConfig(
    // Layout, Entrance & Built-in Animations
    val layoutStyle: String = "auto",
    val entranceAnimation: String = "spring_pop",
    val animationType: String = "rocket",
    val showHeaderWave: Boolean = true,
    val showSparkles: Boolean = false,
    val showConfetti: Boolean = false,
    val titleAlign: String = "start",

    // Photo, GIF & Video Media Controls
    val mediaType: String = "auto",
    val mediaUrl: String = "",
    val imageUrl: String = "",
    val gifUrl: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val mediaHeightDp: Int = 200,
    val mediaCornerRadiusDp: Int = 16,
    val mediaContentScale: String = "crop",
    val videoAutoplay: Boolean = true,
    val videoLoop: Boolean = true,
    val videoMuted: Boolean = true,
    val videoShowControls: Boolean = true,

    // Texts, Badge, Highlight Box & Footer
    val title: String = "",
    val subtitle: String = "",
    val badgeText: String = "UPDATE",
    val description: String = "",
    val highlightTitle: String = "",
    val highlightText: String = "",
    val footerText: String = "",

    // Theme Colors & Backdrop
    val backdropColor: String = "#05070A",
    val backdropGradientEnd: String = "#0D1520",
    val backdropImageUrl: String = "",
    val backdropDimAlpha: Float = 0.72f,
    val cardColor: String = "#131922",
    val cardGradientEnd: String = "#18212E",
    val cardBorderColor: String = "#263244",
    val cardCornerRadius: Int = 24,
    val headerColor: String = "#0D261C",
    val headerGradientEnd: String = "#0E2235",
    val titleColor: String = "#F2F5F8",
    val subtitleColor: String = "#9AA8BA",
    val descriptionColor: String = "#D6E0EB",
    val versionColor: String = "#22D3EE",
    val accentColor: String = "#00E676",
    val badgeColor: String = "#00E676",
    val badgeTextColor: String = "#05190D",
    val highlightBgColor: String = "#192638",
    val highlightTextColor: String = "#E5F3FF",
    val footerColor: String = "#7D8B9E",

    // Primary Button
    val showPrimaryButton: Boolean = true,
    val primaryButtonText: String = "Update Now",
    val primaryButtonUrl: String = "",
    val primaryButtonAction: String = DialogOptions.ACTION_OPEN_URL,
    val primaryButtonIcon: String = "rocket",
    val primaryButtonColor: String = "#00E676",
    val primaryButtonGradientEnd: String = "#22D3EE",
    val primaryButtonTextColor: String = "#05190D",
    val primaryButtonPulse: Boolean = true,

    // Dismiss Button
    val showNotNow: Boolean = true,
    val notNowText: String = "Not Now",
    val notNowColor: String = "#8E9CAF",

    // Dynamic Extra Buttons & Optional Block Ordering
    val buttons: List<DialogButtonConfig> = emptyList(),
    val uiBlocks: List<String> = emptyList()
) {
    /**
     * Resolves the effective media URL for the currently selected media type.
     */
    fun resolvedMediaUrl(): String {
        return when (mediaType) {
            "image" -> imageUrl.ifBlank { mediaUrl }
            "gif" -> gifUrl.ifBlank { mediaUrl }
            "video" -> videoUrl.ifBlank { mediaUrl }
            else -> mediaUrl
                .ifBlank { videoUrl }
                .ifBlank { gifUrl }
                .ifBlank { imageUrl }
        }.trim()
    }
}

data class UpdateConfig(
    val isEnabled: Boolean = false,
    val dialogMode: String = DialogOptions.MODE_UPDATE,
    val isMandatory: Boolean = false,
    val dialogId: String = "update_01",
    val showOnce: Boolean = false,
    val version: String = "",
    val apkUrl: String = "",
    val title: String = "",
    val description: String = "",
    val releaseDate: String = "",
    val releaseDateMillis: Long = 0L,
    val downloadClicks: Long = 0L,
    val buttonClicks: Map<String, Long> = emptyMap(),
    val updatedAt: Long = 0L,
    val uiConfig: DialogUiConfig = DialogUiConfig()
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

object DialogOptions {
    const val MODE_UPDATE = "update"
    const val MODE_ANNOUNCEMENT = "announcement"

    const val ACTION_OPEN_URL = "open_url"
    const val ACTION_DISMISS = "dismiss"
    const val ACTION_SHARE = "share"
    const val ACTION_COPY = "copy"
    const val ACTION_OPEN_TAB = "open_tab"

    const val STYLE_FILLED = "filled"
    const val STYLE_OUTLINED = "outlined"
    const val STYLE_TONAL = "tonal"
    const val STYLE_TEXT = "text"
    const val STYLE_GRADIENT = "gradient"

    val DIALOG_MODES = listOf(MODE_UPDATE, MODE_ANNOUNCEMENT)

    val LAYOUT_STYLES = listOf(
        "auto",
        "wave_rocket",
        "hero_media",
        "full_card_media",
        "minimal",
        "fullscreen_poster"
    )

    val ENTRANCE_ANIMATIONS = listOf(
        "spring_pop",
        "slide_up",
        "fade_zoom",
        "flip_in",
        "none"
    )

    val ANIMATION_TYPES = listOf(
        "rocket",
        "pulse_badge",
        "confetti",
        "sparkle_wave",
        "glow_ring",
        "bounce_icon",
        "none"
    )

    val TITLE_ALIGNS = listOf("start", "center")

    val MEDIA_TYPES = listOf("auto", "image", "gif", "video", "animation", "none")

    val MEDIA_CONTENT_SCALES = listOf("crop", "fit")

    val BUTTON_ACTIONS = listOf(
        ACTION_OPEN_URL,
        ACTION_DISMISS,
        ACTION_SHARE,
        ACTION_COPY,
        ACTION_OPEN_TAB
    )

    val BUTTON_STYLES = listOf(
        STYLE_FILLED,
        STYLE_GRADIENT,
        STYLE_OUTLINED,
        STYLE_TONAL,
        STYLE_TEXT
    )

    val TAB_KEYS = listOf(
        "dashboard",
        "smart_tracker",
        "voice_alerts",
        "battery_health",
        "hardware_oem",
        "history_about"
    )

    val BUTTON_ICONS = listOf(
        "download",
        "rocket",
        "play",
        "video",
        "image",
        "telegram",
        "whatsapp",
        "link",
        "open",
        "copy",
        "share",
        "star",
        "gift",
        "info",
        "bolt",
        "settings",
        "close",
        "check",
        "sparkles",
        "warning",
        "bell",
        "campaign",
        "phone"
    )

    val UI_BLOCK_TYPES = listOf(
        "badge",
        "title",
        "subtitle",
        "text",
        "bullets",
        "media",
        "animation",
        "highlight_box",
        "divider",
        "spacer",
        "button",
        "button_row"
    )

    val PRESET_COLOR_SWATCHES = listOf(
        "#00E676",
        "#22D3EE",
        "#FFB300",
        "#FF5252",
        "#A855F7",
        "#EC4899",
        "#3B82F6",
        "#F97316",
        "#131922",
        "#1B2432",
        "#0B0E11",
        "#FFFFFF",
        "#9AA8BA",
        "#05190D",
        "#1E1B4B",
        "#3B0764"
    )
}

enum class DialogPreset(
    val label: String,
    val subtitle: String
) {
    CLASSIC_UPDATE_ROCKET(
        label = "Classic Update Rocket",
        subtitle = "Wave header + animated rocket + green CTA"
    ),
    VIDEO_ANNOUNCEMENT(
        label = "Video Announcement",
        subtitle = "Hero video player + dismissible promo"
    ),
    FESTIVE_GIFT_CONFETTI(
        label = "Festive Gift & Confetti",
        subtitle = "Confetti + sparkles + dual action row"
    ),
    MINIMAL_NOTICE(
        label = "Minimal Notice",
        subtitle = "Clean compact dark card without media"
    );

    fun buildConfig(existing: UpdateConfig): UpdateConfig {
        return when (this) {
            CLASSIC_UPDATE_ROCKET -> existing.copy(
                isEnabled = true,
                dialogMode = DialogOptions.MODE_UPDATE,
                isMandatory = false,
                dialogId = existing.dialogId.ifBlank { "update_v2" },
                showOnce = false,
                version = existing.version.ifBlank { "2.0" },
                title = existing.title.ifBlank { "Battery Boss v2.0 is Here!" },
                description = existing.description.ifBlank {
                    "• Real-time charging wattage telemetry\n• Faster thermal protection alerts\n• Smoother OLED dark theme"
                },
                uiConfig = DialogUiConfig(
                    layoutStyle = "wave_rocket",
                    entranceAnimation = "spring_pop",
                    animationType = "rocket",
                    showHeaderWave = true,
                    showSparkles = true,
                    showConfetti = false,
                    titleAlign = "start",
                    mediaType = "animation",
                    title = existing.title.ifBlank { "Battery Boss v2.0 is Here!" },
                    subtitle = "Major Performance & Battery Health Upgrade",
                    badgeText = "NEW UPDATE",
                    description = existing.description.ifBlank {
                        "• Real-time charging wattage telemetry\n• Faster thermal protection alerts\n• Smoother OLED dark theme"
                    },
                    highlightTitle = "What's Improved",
                    highlightText = "35% faster diagnostics with zero background drain.",
                    footerText = "Safe & verified official Battery Boss APK",
                    backdropColor = "#06090D",
                    backdropGradientEnd = "#0D1A26",
                    backdropDimAlpha = 0.78f,
                    cardColor = "#111722",
                    cardGradientEnd = "#172130",
                    cardBorderColor = "#243548",
                    cardCornerRadius = 26,
                    headerColor = "#0A2A1E",
                    headerGradientEnd = "#0C2438",
                    titleColor = "#F4F8FB",
                    subtitleColor = "#92A4B8",
                    descriptionColor = "#D7E3EE",
                    versionColor = "#22D3EE",
                    accentColor = "#00E676",
                    badgeColor = "#00E676",
                    badgeTextColor = "#04180C",
                    highlightBgColor = "#162638",
                    highlightTextColor = "#CFE9FF",
                    footerColor = "#7A8CA2",
                    showPrimaryButton = true,
                    primaryButtonText = "Update Now",
                    primaryButtonUrl = existing.apkUrl,
                    primaryButtonAction = DialogOptions.ACTION_OPEN_URL,
                    primaryButtonIcon = "rocket",
                    primaryButtonColor = "#00E676",
                    primaryButtonGradientEnd = "#22D3EE",
                    primaryButtonTextColor = "#04180C",
                    primaryButtonPulse = true,
                    showNotNow = true,
                    notNowText = "Not Now",
                    notNowColor = "#8F9EB2",
                    buttons = emptyList(),
                    uiBlocks = emptyList()
                )
            )

            VIDEO_ANNOUNCEMENT -> existing.copy(
                isEnabled = true,
                dialogMode = DialogOptions.MODE_ANNOUNCEMENT,
                isMandatory = false,
                dialogId = "promo_video_01",
                showOnce = true,
                title = "Watch What's New in Battery Boss",
                description = "• Live charging curve & health monitor demo\n• Custom voice alert triggers explained\n• Tap below to explore Voice Alerts tab",
                uiConfig = DialogUiConfig(
                    layoutStyle = "hero_media",
                    entranceAnimation = "slide_up",
                    animationType = "glow_ring",
                    showHeaderWave = false,
                    showSparkles = false,
                    showConfetti = false,
                    titleAlign = "start",
                    mediaType = "video",
                    mediaUrl = existing.uiConfig.videoUrl.ifBlank {
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                    },
                    videoUrl = existing.uiConfig.videoUrl.ifBlank {
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                    },
                    thumbnailUrl = existing.uiConfig.thumbnailUrl,
                    mediaHeightDp = 210,
                    mediaCornerRadiusDp = 18,
                    mediaContentScale = "crop",
                    videoAutoplay = true,
                    videoLoop = true,
                    videoMuted = true,
                    videoShowControls = true,
                    title = "Watch What's New in Battery Boss",
                    subtitle = "Feature Walkthrough • 45 sec",
                    badgeText = "FEATURED VIDEO",
                    description = "• Live charging curve & health monitor demo\n• Custom voice alert triggers explained\n• Tap below to explore Voice Alerts tab",
                    highlightTitle = "Pro Tip",
                    highlightText = "Enable 80% charge chime to extend battery lifespan.",
                    footerText = "Announcement shown once per device",
                    backdropColor = "#05070B",
                    backdropGradientEnd = "#0E1629",
                    backdropDimAlpha = 0.82f,
                    cardColor = "#121722",
                    cardGradientEnd = "#1B2233",
                    cardBorderColor = "#2E3B56",
                    cardCornerRadius = 24,
                    headerColor = "#141E33",
                    headerGradientEnd = "#0F293B",
                    titleColor = "#F8FAFC",
                    subtitleColor = "#94A3B8",
                    descriptionColor = "#CBD5E1",
                    versionColor = "#38BDF8",
                    accentColor = "#38BDF8",
                    badgeColor = "#38BDF8",
                    badgeTextColor = "#082F49",
                    highlightBgColor = "#1E293B",
                    highlightTextColor = "#E2E8F0",
                    footerColor = "#64748B",
                    showPrimaryButton = true,
                    primaryButtonText = "Try Voice Alerts",
                    primaryButtonUrl = "voice_alerts",
                    primaryButtonAction = DialogOptions.ACTION_OPEN_TAB,
                    primaryButtonIcon = "play",
                    primaryButtonColor = "#38BDF8",
                    primaryButtonGradientEnd = "#6366F1",
                    primaryButtonTextColor = "#FFFFFF",
                    primaryButtonPulse = false,
                    showNotNow = true,
                    notNowText = "Close",
                    notNowColor = "#94A3B8",
                    buttons = listOf(
                        DialogButtonConfig(
                            id = "share_promo",
                            text = "Share App",
                            action = DialogOptions.ACTION_SHARE,
                            url = "",
                            shareText = "Check out Battery Boss for smart charging alerts!",
                            style = DialogOptions.STYLE_OUTLINED,
                            bgColor = "#38BDF8",
                            gradientEnd = "",
                            textColor = "#38BDF8",
                            icon = "share",
                            cornerRadiusDp = 14,
                            rowGroup = ""
                        )
                    ),
                    uiBlocks = emptyList()
                )
            )

            FESTIVE_GIFT_CONFETTI -> existing.copy(
                isEnabled = true,
                dialogMode = DialogOptions.MODE_ANNOUNCEMENT,
                isMandatory = false,
                dialogId = "festive_gift_01",
                showOnce = true,
                title = "Festive Celebration Bonus!",
                description = "• Join our Telegram community for early beta drops\n• Share Battery Boss with friends for tips & presets\n• Exclusive custom voice packs unlocked",
                uiConfig = DialogUiConfig(
                    layoutStyle = "full_card_media",
                    entranceAnimation = "fade_zoom",
                    animationType = "confetti",
                    showHeaderWave = true,
                    showSparkles = true,
                    showConfetti = true,
                    titleAlign = "center",
                    mediaType = "animation",
                    title = "Festive Celebration Bonus!",
                    subtitle = "Limited-Time Community Event",
                    badgeText = "SPECIAL GIFT",
                    description = "• Join our Telegram community for early beta drops\n• Share Battery Boss with friends for tips & presets\n• Exclusive custom voice packs unlocked",
                    highlightTitle = "Gift Code: BOSS2026",
                    highlightText = "Tap Copy Code or join our Telegram channel below.",
                    footerText = "Thank you for powering Battery Boss!",
                    backdropColor = "#12071D",
                    backdropGradientEnd = "#230B2E",
                    backdropDimAlpha = 0.80f,
                    cardColor = "#1C102B",
                    cardGradientEnd = "#2A143D",
                    cardBorderColor = "#6D28D9",
                    cardCornerRadius = 28,
                    headerColor = "#4C1D95",
                    headerGradientEnd = "#9D174D",
                    titleColor = "#FFFBEB",
                    subtitleColor = "#FDE68A",
                    descriptionColor = "#F3E8FF",
                    versionColor = "#FBBF24",
                    accentColor = "#F59E0B",
                    badgeColor = "#F59E0B",
                    badgeTextColor = "#291400",
                    highlightBgColor = "#3B1754",
                    highlightTextColor = "#FDE68A",
                    footerColor = "#C4B5FD",
                    showPrimaryButton = true,
                    primaryButtonText = "Claim Gift",
                    primaryButtonUrl = "BOSS2026",
                    primaryButtonAction = DialogOptions.ACTION_COPY,
                    primaryButtonIcon = "gift",
                    primaryButtonColor = "#F59E0B",
                    primaryButtonGradientEnd = "#EC4899",
                    primaryButtonTextColor = "#1A0A00",
                    primaryButtonPulse = true,
                    showNotNow = true,
                    notNowText = "Maybe Later",
                    notNowColor = "#DDD6FE",
                    buttons = listOf(
                        DialogButtonConfig(
                            id = "tg_btn",
                            text = "Telegram",
                            action = DialogOptions.ACTION_OPEN_URL,
                            url = "https://t.me/batteryboss",
                            style = DialogOptions.STYLE_FILLED,
                            bgColor = "#229ED9",
                            gradientEnd = "",
                            textColor = "#FFFFFF",
                            icon = "telegram",
                            cornerRadiusDp = 14,
                            rowGroup = "social"
                        ),
                        DialogButtonConfig(
                            id = "wa_btn",
                            text = "WhatsApp",
                            action = DialogOptions.ACTION_OPEN_URL,
                            url = "https://whatsapp.com/channel/batteryboss",
                            style = DialogOptions.STYLE_FILLED,
                            bgColor = "#25D366",
                            gradientEnd = "",
                            textColor = "#041A0B",
                            icon = "whatsapp",
                            cornerRadiusDp = 14,
                            rowGroup = "social"
                        )
                    ),
                    uiBlocks = emptyList()
                )
            )

            MINIMAL_NOTICE -> existing.copy(
                isEnabled = true,
                dialogMode = DialogOptions.MODE_ANNOUNCEMENT,
                isMandatory = false,
                dialogId = "notice_01",
                showOnce = true,
                title = "System Maintenance Notice",
                description = "• Cloud backup sync will be upgraded tonight\n• Local battery tracking continues normally",
                uiConfig = DialogUiConfig(
                    layoutStyle = "minimal",
                    entranceAnimation = "fade_zoom",
                    animationType = "pulse_badge",
                    showHeaderWave = false,
                    showSparkles = false,
                    showConfetti = false,
                    titleAlign = "start",
                    mediaType = "none",
                    title = "System Maintenance Notice",
                    subtitle = "Quick heads-up for Battery Boss users",
                    badgeText = "NOTICE",
                    description = "• Cloud backup sync will be upgraded tonight\n• Local battery tracking continues normally",
                    highlightTitle = "",
                    highlightText = "",
                    footerText = "No action needed",
                    backdropColor = "#080A0D",
                    backdropGradientEnd = "#080A0D",
                    backdropDimAlpha = 0.65f,
                    cardColor = "#14181F",
                    cardGradientEnd = "#14181F",
                    cardBorderColor = "#2A323F",
                    cardCornerRadius = 20,
                    headerColor = "#14181F",
                    headerGradientEnd = "#14181F",
                    titleColor = "#F3F4F6",
                    subtitleColor = "#9CA3AF",
                    descriptionColor = "#D1D5DB",
                    versionColor = "#00E676",
                    accentColor = "#00E676",
                    badgeColor = "#1E293B",
                    badgeTextColor = "#00E676",
                    highlightBgColor = "#1B222D",
                    highlightTextColor = "#E5E7EB",
                    footerColor = "#6B7280",
                    showPrimaryButton = true,
                    primaryButtonText = "Got It",
                    primaryButtonUrl = "",
                    primaryButtonAction = DialogOptions.ACTION_DISMISS,
                    primaryButtonIcon = "check",
                    primaryButtonColor = "#00E676",
                    primaryButtonGradientEnd = "",
                    primaryButtonTextColor = "#05190D",
                    primaryButtonPulse = false,
                    showNotNow = false,
                    notNowText = "Dismiss",
                    notNowColor = "#9CA3AF",
                    buttons = emptyList(),
                    uiBlocks = emptyList()
                )
            )
        }
    }
}

object FirebaseConstants {
    const val UPDATE_NODE_PATH = "battery_boss_update"

    // Top-level fields
    const val FIELD_ENABLED = "enabled"
    const val FIELD_IS_ENABLED = "is_enabled"
    const val FIELD_DIALOG_MODE = "dialog_mode"
    const val FIELD_MANDATORY = "mandatory"
    const val FIELD_IS_MANDATORY = "is_mandatory"
    const val FIELD_DIALOG_ID = "dialog_id"
    const val FIELD_SHOW_ONCE = "show_once"
    const val FIELD_VERSION = "version"
    const val FIELD_APK_URL = "apk_url"
    const val FIELD_TITLE = "title"
    const val FIELD_DESCRIPTION = "description"
    const val FIELD_RELEASE_DATE = "release_date"
    const val FIELD_RELEASE_DATE_MILLIS = "release_date_millis"
    const val FIELD_RELEASE_DATE_MILLIS_CAMEL = "releaseDateMillis"
    const val FIELD_DOWNLOAD_CLICKS = "download_clicks"
    const val FIELD_BUTTON_CLICKS = "button_clicks"
    const val FIELD_UPDATED_AT = "updated_at"
    const val FIELD_UI_CONFIG = "ui_config"

    // ui_config fields
    const val UI_LAYOUT_STYLE = "layout_style"
    const val UI_ENTRANCE_ANIMATION = "entrance_animation"
    const val UI_ANIMATION_TYPE = "animation_type"
    const val UI_SHOW_HEADER_WAVE = "show_header_wave"
    const val UI_SHOW_SPARKLES = "show_sparkles"
    const val UI_SHOW_CONFETTI = "show_confetti"
    const val UI_TITLE_ALIGN = "title_align"

    const val UI_MEDIA_TYPE = "media_type"
    const val UI_MEDIA_URL = "media_url"
    const val UI_IMAGE_URL = "image_url"
    const val UI_GIF_URL = "gif_url"
    const val UI_VIDEO_URL = "video_url"
    const val UI_THUMBNAIL_URL = "thumbnail_url"
    const val UI_MEDIA_HEIGHT_DP = "media_height_dp"
    const val UI_MEDIA_CORNER_RADIUS_DP = "media_corner_radius_dp"
    const val UI_MEDIA_CONTENT_SCALE = "media_content_scale"
    const val UI_VIDEO_AUTOPLAY = "video_autoplay"
    const val UI_VIDEO_LOOP = "video_loop"
    const val UI_VIDEO_MUTED = "video_muted"
    const val UI_VIDEO_SHOW_CONTROLS = "video_show_controls"

    const val UI_TITLE = "title"
    const val UI_SUBTITLE = "subtitle"
    const val UI_BADGE_TEXT = "badge_text"
    const val UI_DESCRIPTION = "description"
    const val UI_HIGHLIGHT_TITLE = "highlight_title"
    const val UI_HIGHLIGHT_TEXT = "highlight_text"
    const val UI_FOOTER_TEXT = "footer_text"

    const val UI_BACKDROP_COLOR = "backdrop_color"
    const val UI_BACKDROP_GRADIENT_END = "backdrop_gradient_end"
    const val UI_BACKDROP_IMAGE_URL = "backdrop_image_url"
    const val UI_BACKDROP_DIM_ALPHA = "backdrop_dim_alpha"
    const val UI_CARD_COLOR = "card_color"
    const val UI_CARD_GRADIENT_END = "card_gradient_end"
    const val UI_CARD_BORDER_COLOR = "card_border_color"
    const val UI_CARD_CORNER_RADIUS = "card_corner_radius"
    const val UI_HEADER_COLOR = "header_color"
    const val UI_HEADER_GRADIENT_END = "header_gradient_end"
    const val UI_TITLE_COLOR = "title_color"
    const val UI_SUBTITLE_COLOR = "subtitle_color"
    const val UI_DESCRIPTION_COLOR = "description_color"
    const val UI_VERSION_COLOR = "version_color"
    const val UI_ACCENT_COLOR = "accent_color"
    const val UI_BADGE_COLOR = "badge_color"
    const val UI_BADGE_TEXT_COLOR = "badge_text_color"
    const val UI_HIGHLIGHT_BG_COLOR = "highlight_bg_color"
    const val UI_HIGHLIGHT_TEXT_COLOR = "highlight_text_color"
    const val UI_FOOTER_COLOR = "footer_color"

    const val UI_SHOW_PRIMARY_BUTTON = "show_primary_button"
    const val UI_PRIMARY_BUTTON_TEXT = "primary_button_text"
    const val UI_PRIMARY_BUTTON_URL = "primary_button_url"
    const val UI_PRIMARY_BUTTON_ACTION = "primary_button_action"
    const val UI_PRIMARY_BUTTON_ICON = "primary_button_icon"
    const val UI_PRIMARY_BUTTON_COLOR = "primary_button_color"
    const val UI_PRIMARY_BUTTON_GRADIENT_END = "primary_button_gradient_end"
    const val UI_PRIMARY_BUTTON_TEXT_COLOR = "primary_button_text_color"
    const val UI_PRIMARY_BUTTON_PULSE = "primary_button_pulse"

    const val UI_SHOW_NOT_NOW = "show_not_now"
    const val UI_NOT_NOW_TEXT = "not_now_text"
    const val UI_NOT_NOW_COLOR = "not_now_color"

    const val UI_BUTTONS = "buttons"
    const val UI_BLOCKS = "ui_blocks"

    // Button object fields
    const val BTN_ID = "id"
    const val BTN_TEXT = "text"
    const val BTN_ACTION = "action"
    const val BTN_URL = "url"
    const val BTN_SHARE_TEXT = "share_text"
    const val BTN_STYLE = "style"
    const val BTN_BG_COLOR = "bg_color"
    const val BTN_GRADIENT_END = "gradient_end"
    const val BTN_TEXT_COLOR = "text_color"
    const val BTN_ICON = "icon"
    const val BTN_CORNER_RADIUS_DP = "corner_radius_dp"
    const val BTN_ROW_GROUP = "row_group"

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

package com.example.ui

import com.example.ui.theme.AppThemeMode

enum class AppRoute {
    SPLASH,
    PROFILE_SETUP,
    FIREBASE_SETUP,
    MAIN_SHELL
}

enum class DrawerSection(val title: String) {
    HOME("Home"),
    DASHBOARD("Dashboard"),
    MANAGE_LINKS("Manage Links"),
    SETTINGS("Settings")
}

data class DashboardAppItem(
    val id: String,
    val appName: String,
    val packageName: String,
    val accentIndex: Int = 0
)

data class SavedLinkItem(
    val id: String,
    val url: String,
    val isActive: Boolean = false
)

data class HomeFormState(
    val packageName: String = "com.zenx.batteryboss",
    val enableUiAnimations: Boolean = true,
    val enableThemes: Boolean = true,
    val enableRgbBorder: Boolean = false,
    val selectedTheme: String = "Violet Glass",
    val scheduledDate: String = "01/12/2026",
    val releaseDate: String = "28/11/2026",
    val appVersion: String = "2.0.0",
    val isFetchingVersion: Boolean = false,
    val dialogTitle: String = "Update Available!",
    val updateButtonLink: String = "https://github.com/zenx/releases/latest",
    val updateButtonText: String = "Update Now",
    val logoUrl: String = "https://cdn.zenxmodz.com/logo.png",
    val socialLink1: String = "https://t.me/ridh007",
    val socialLink2: String = "https://t.me/zenxmodz",
    val socialLink3: String = "https://youtube.com/@zenxmodz",
    val mainMessage: String = "• Major performance upgrade\n• Material 3 theme support\n• Improved dialog animations",
    val detailMessage: String = "Please install the latest release for optimal stability, improved dark mode styling, and updated server controls.",
    val showNotification: Boolean = false,
    val notificationTitle: String = "New Update Ready",
    val notificationMessage: String = "Tap to view the latest release notes and improvements.",
    val activeDialog: Boolean = true,
    val isPushingChange: Boolean = false
)

data class AdminPanelUiState(
    val currentRoute: AppRoute = AppRoute.SPLASH,
    val currentSection: DrawerSection = DrawerSection.HOME,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,

    // Profile Setup
    val profileName: String = "Guest",
    val profileInput: String = "",
    val avatarColorIndex: Int = 0,

    // Firebase URL Setup
    val firebaseSetupUrlInput: String = "",

    // Home Form UI State
    val homeForm: HomeFormState = HomeFormState(),

    // Dashboard Sample UI State
    val searchQuery: String = "",
    val apps: List<DashboardAppItem> = listOf(
        DashboardAppItem("1", "Battery Boss", "com.zenx.batteryboss", 0),
        DashboardAppItem("2", "Zenx Hub Pro", "com.zenx.modzhub", 1),
        DashboardAppItem("3", "Volt Telemetry", "com.example.telemetry", 2)
    ),

    // Manage Links Sample UI State
    val newLinkInput: String = "",
    val savedLinks: List<SavedLinkItem> = listOf(
        SavedLinkItem("l1", "https://admin-50dca-default-rtdb.firebaseio.com", isActive = true),
        SavedLinkItem("l2", "https://zenx-update-panel-default-rtdb.firebaseio.com", isActive = false),
        SavedLinkItem("l3", "https://staging-battery-boss.firebasedatabase.app", isActive = false)
    ),

    val snackbarMessage: String? = null
)

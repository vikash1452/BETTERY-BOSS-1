package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdminRepository
import com.example.data.ConnectionStatus
import com.example.data.DialogButtonConfig
import com.example.data.DialogOptions
import com.example.data.DialogPreset
import com.example.data.DialogUiConfig
import com.example.data.FirebaseBackendMode
import com.example.data.PrefsManager
import com.example.data.UpdateConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AdminScreen {
    Setup,
    Dashboard
}

enum class DashboardTab(val label: String) {
    MODE_CONTENT("Mode & Content"),
    LAYOUT_MEDIA("Layout & Media"),
    COLORS_THEME("Theme & Colors"),
    BUTTONS_BLOCKS("Buttons & Blocks"),
    LIVE_PREVIEW("Live Preview")
}

data class AdminUiState(
    val currentScreen: AdminScreen = AdminScreen.Setup,
    val selectedTab: DashboardTab = DashboardTab.MODE_CONTENT,
    val savedDatabaseUrl: String = "",
    val backendMode: FirebaseBackendMode = FirebaseBackendMode.REST_API,

    // Setup Screen state
    val setupUrlInput: String = "",
    val setupError: String? = null,
    val isConnectingUrl: Boolean = false,

    // Dashboard state
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING,
    val config: UpdateConfig = UpdateConfig(),
    val isCustomBlockOrderEnabled: Boolean = false,
    val hasHydratedFromRemote: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val isFetchingGitHubRelease: Boolean = false,
    val githubFetchError: String? = null,
    val snackbarMessage: String? = null
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsManager(application)
    private val repository = AdminRepository(application, prefs)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var syncJob: Job? = null

    init {
        if (_uiState.value.currentScreen == AdminScreen.Dashboard) {
            startObservingDatabase()
        }
    }

    private fun createInitialState(): AdminUiState {
        val savedUrl = prefs.getDatabaseUrl().orEmpty()
        val initialScreen = if (savedUrl.isNotEmpty()) {
            AdminScreen.Dashboard
        } else {
            AdminScreen.Setup
        }
        return AdminUiState(
            currentScreen = initialScreen,
            savedDatabaseUrl = savedUrl,
            backendMode = repository.backendMode,
            setupUrlInput = savedUrl
        )
    }

    // --- Setup Screen Actions ---

    fun onSetupUrlChanged(value: String) {
        _uiState.update { it.copy(setupUrlInput = value, setupError = null) }
    }

    fun connectDatabaseUrl() {
        val current = _uiState.value
        if (current.isConnectingUrl) return

        viewModelScope.launch {
            _uiState.update { it.copy(isConnectingUrl = true, setupError = null) }
            val result = repository.verifyAndSaveDatabaseUrl(current.setupUrlInput)
            result.fold(
                onSuccess = { status ->
                    val normalizedUrl = prefs.getDatabaseUrl().orEmpty()
                    _uiState.update {
                        it.copy(
                            isConnectingUrl = false,
                            savedDatabaseUrl = normalizedUrl,
                            connectionStatus = status,
                            currentScreen = AdminScreen.Dashboard,
                            hasHydratedFromRemote = false,
                            snackbarMessage = "Connected & saved Firebase URL"
                        )
                    }
                    startObservingDatabase()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isConnectingUrl = false,
                            setupError = error.message ?: "Failed to connect to Firebase URL."
                        )
                    }
                }
            )
        }
    }

    fun onTabSelected(tab: DashboardTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // --- Live Firebase Sync ---

    private fun startObservingDatabase() {
        stopObservingDatabase()
        syncJob = viewModelScope.launch {
            repository.observeUpdateConfig().collect { snapshot ->
                _uiState.update { state ->
                    val remote = snapshot.config
                    if (remote != null && (!state.hasHydratedFromRemote || !state.hasUnsavedChanges)) {
                        val hydrated = hydrateRemoteConfig(remote)
                        state.copy(
                            connectionStatus = snapshot.status,
                            config = hydrated,
                            isCustomBlockOrderEnabled = hydrated.uiConfig.uiBlocks.isNotEmpty(),
                            hasHydratedFromRemote = true,
                            isRefreshing = false
                        )
                    } else if (remote != null) {
                        state.copy(
                            connectionStatus = snapshot.status,
                            config = state.config.copy(
                                downloadClicks = remote.downloadClicks,
                                buttonClicks = remote.buttonClicks
                            ),
                            isRefreshing = false
                        )
                    } else {
                        state.copy(
                            connectionStatus = snapshot.status,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    private fun stopObservingDatabase() {
        syncJob?.cancel()
        syncJob = null
    }

    fun refreshFromFirebase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val snapshot = repository.fetchUpdateConfigOnce()
            _uiState.update { state ->
                val remote = snapshot.config
                if (remote != null) {
                    val hydrated = hydrateRemoteConfig(remote)
                    state.copy(
                        connectionStatus = snapshot.status,
                        config = hydrated,
                        isCustomBlockOrderEnabled = hydrated.uiConfig.uiBlocks.isNotEmpty(),
                        hasHydratedFromRemote = true,
                        hasUnsavedChanges = false,
                        isRefreshing = false,
                        snackbarMessage = "Synced live data from /battery_boss_update/"
                    )
                } else {
                    state.copy(
                        connectionStatus = snapshot.status,
                        isRefreshing = false,
                        snackbarMessage = snapshot.errorMessage ?: "Unable to sync from Firebase"
                    )
                }
            }
        }
    }

    private fun hydrateRemoteConfig(remote: UpdateConfig): UpdateConfig {
        val resolvedDate = remote.releaseDate.ifBlank {
            if (remote.releaseDateMillis > 0L) formatDateMillis(remote.releaseDateMillis) else ""
        }
        val syncedTitle = remote.uiConfig.title.ifBlank { remote.title }
        val syncedDesc = remote.uiConfig.description.ifBlank { remote.description }
        return remote.copy(
            title = syncedTitle,
            description = syncedDesc,
            releaseDate = resolvedDate,
            uiConfig = remote.uiConfig.copy(
                title = syncedTitle,
                description = syncedDesc
            )
        )
    }

    // --- Config Mutators ---

    fun updateConfig(transform: (UpdateConfig) -> UpdateConfig) {
        _uiState.update { state ->
            state.copy(
                config = transform(state.config),
                hasUnsavedChanges = true
            )
        }
    }

    fun updateUiConfig(transform: (DialogUiConfig) -> DialogUiConfig) {
        _uiState.update { state ->
            val updatedUi = transform(state.config.uiConfig)
            state.copy(
                config = state.config.copy(
                    title = updatedUi.title,
                    description = updatedUi.description,
                    uiConfig = updatedUi
                ),
                hasUnsavedChanges = true
            )
        }
    }

    fun onTitleChanged(value: String) {
        updateConfig { cfg ->
            cfg.copy(
                title = value,
                uiConfig = cfg.uiConfig.copy(title = value)
            )
        }
    }

    fun onDescriptionChanged(value: String) {
        updateConfig { cfg ->
            cfg.copy(
                description = value,
                uiConfig = cfg.uiConfig.copy(description = value)
            )
        }
    }

    fun onReleaseImmediatelySelected() {
        updateConfig { it.copy(releaseDate = "", releaseDateMillis = 0L) }
    }

    fun onReleaseDateSelected(startOfDayMillis: Long) {
        updateConfig {
            it.copy(
                releaseDate = formatDateMillis(startOfDayMillis),
                releaseDateMillis = startOfDayMillis
            )
        }
    }

    // --- Dynamic Extra Buttons CRUD ---

    fun upsertExtraButton(button: DialogButtonConfig, editIndex: Int?) {
        updateUiConfig { ui ->
            val mutable = ui.buttons.toMutableList()
            if (editIndex != null && editIndex in mutable.indices) {
                mutable[editIndex] = button
            } else {
                mutable.add(button)
            }
            ui.copy(buttons = mutable)
        }
    }

    fun deleteExtraButton(index: Int) {
        updateUiConfig { ui ->
            val mutable = ui.buttons.toMutableList()
            if (index in mutable.indices) mutable.removeAt(index)
            ui.copy(buttons = mutable)
        }
    }

    fun moveExtraButton(index: Int, delta: Int) {
        updateUiConfig { ui ->
            val target = index + delta
            if (index !in ui.buttons.indices || target !in ui.buttons.indices) return@updateUiConfig ui
            val mutable = ui.buttons.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(target, item)
            ui.copy(buttons = mutable)
        }
    }

    // --- Optional Custom Block Order ---

    fun toggleCustomBlockOrder(enabled: Boolean) {
        _uiState.update { state ->
            val nextBlocks = if (enabled) {
                state.config.uiConfig.uiBlocks.ifEmpty {
                    listOf(
                        "badge",
                        "media",
                        "title",
                        "subtitle",
                        "bullets",
                        "highlight_box",
                        "button",
                        "button_row"
                    )
                }
            } else {
                emptyList()
            }
            state.copy(
                isCustomBlockOrderEnabled = enabled,
                config = state.config.copy(
                    uiConfig = state.config.uiConfig.copy(uiBlocks = nextBlocks)
                ),
                hasUnsavedChanges = true
            )
        }
    }

    fun addUiBlock(blockType: String) {
        updateUiConfig { ui ->
            ui.copy(uiBlocks = ui.uiBlocks + blockType)
        }
    }

    fun removeUiBlock(index: Int) {
        updateUiConfig { ui ->
            val mutable = ui.uiBlocks.toMutableList()
            if (index in mutable.indices) mutable.removeAt(index)
            ui.copy(uiBlocks = mutable)
        }
    }

    fun moveUiBlock(index: Int, delta: Int) {
        updateUiConfig { ui ->
            val target = index + delta
            if (index !in ui.uiBlocks.indices || target !in ui.uiBlocks.indices) return@updateUiConfig ui
            val mutable = ui.uiBlocks.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(target, item)
            ui.copy(uiBlocks = mutable)
        }
    }

    // --- Quick Presets & Actions ---

    fun applyPreset(preset: DialogPreset) {
        _uiState.update { state ->
            val presetConfig = preset.buildConfig(state.config)
            state.copy(
                config = presetConfig,
                isCustomBlockOrderEnabled = presetConfig.uiConfig.uiBlocks.isNotEmpty(),
                hasUnsavedChanges = true,
                snackbarMessage = "Applied preset: ${preset.label}"
            )
        }
    }

    fun resetUiToDefault() {
        _uiState.update { state ->
            val preservedClicks = state.config.downloadClicks
            val preservedBtnClicks = state.config.buttonClicks
            val defaultConfig = UpdateConfig(
                isEnabled = state.config.isEnabled,
                dialogMode = DialogOptions.MODE_UPDATE,
                version = state.config.version,
                apkUrl = state.config.apkUrl,
                title = state.config.title,
                description = state.config.description,
                releaseDate = state.config.releaseDate,
                releaseDateMillis = state.config.releaseDateMillis,
                downloadClicks = preservedClicks,
                buttonClicks = preservedBtnClicks,
                uiConfig = DialogUiConfig(
                    title = state.config.title,
                    description = state.config.description,
                    primaryButtonUrl = state.config.apkUrl
                )
            )
            state.copy(
                config = defaultConfig,
                isCustomBlockOrderEnabled = false,
                hasUnsavedChanges = true,
                snackbarMessage = "Reset UI styling & blocks to default"
            )
        }
    }

    fun disableDialogQuickly() {
        val current = _uiState.value
        if (current.isSaving) return
        val disabledConfig = current.config.copy(isEnabled = false)
        _uiState.update {
            it.copy(
                config = disabledConfig,
                isSaving = true
            )
        }
        viewModelScope.launch {
            val result = repository.saveUpdateConfig(disabledConfig)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasUnsavedChanges = false,
                            connectionStatus = ConnectionStatus.CONNECTED,
                            snackbarMessage = "Dialog disabled live on Firebase"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasUnsavedChanges = true,
                            snackbarMessage = error.message ?: "Failed to disable dialog"
                        )
                    }
                }
            )
        }
    }

    // --- Save to Firebase ---

    fun saveToFirebase() {
        val state = _uiState.value
        if (state.isSaving) return
        val cfg = state.config

        if (cfg.dialogMode == DialogOptions.MODE_UPDATE && cfg.isEnabled && cfg.version.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Enter a version number for Update Mode (e.g. 2.0)") }
            return
        }
        if (cfg.title.isBlank() && cfg.uiConfig.title.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Please enter a dialog title") }
            return
        }
        if (cfg.apkUrl.isNotBlank() &&
            !cfg.apkUrl.trim().startsWith("https://", ignoreCase = true) &&
            !cfg.apkUrl.trim().startsWith("http://", ignoreCase = true)
        ) {
            _uiState.update { it.copy(snackbarMessage = "APK URL must start with https:// or http://") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = repository.saveUpdateConfig(cfg)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasUnsavedChanges = false,
                            connectionStatus = ConnectionStatus.CONNECTED,
                            config = it.config.copy(updatedAt = System.currentTimeMillis()),
                            snackbarMessage = "Saved full UI & update config to Firebase"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbarMessage = error.message ?: "Failed to save to Firebase"
                        )
                    }
                }
            )
        }
    }

    fun resetFirebaseUrl() {
        stopObservingDatabase()
        repository.resetDatabaseUrl()
        _uiState.update {
            AdminUiState(
                currentScreen = AdminScreen.Setup,
                backendMode = repository.backendMode,
                snackbarMessage = "Firebase URL cleared. Enter a new endpoint."
            )
        }
    }

    fun fetchGitHubReleaseApk(repoInput: String, onSuccess: () -> Unit) {
        if (_uiState.value.isFetchingGitHubRelease) return
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingGitHubRelease = true, githubFetchError = null) }
            val result = repository.fetchLatestGitHubRelease(repoInput)
            result.fold(
                onSuccess = { release ->
                    _uiState.update { state ->
                        val nextTitle = release.title.ifBlank { state.config.title }
                        val nextDesc = release.description.ifBlank { state.config.description }
                        state.copy(
                            isFetchingGitHubRelease = false,
                            githubFetchError = null,
                            config = state.config.copy(
                                apkUrl = release.apkDownloadUrl,
                                version = release.version.ifBlank { state.config.version },
                                title = nextTitle,
                                description = nextDesc,
                                uiConfig = state.config.uiConfig.copy(
                                    title = nextTitle,
                                    description = nextDesc,
                                    primaryButtonUrl = state.config.uiConfig.primaryButtonUrl.ifBlank {
                                        release.apkDownloadUrl
                                    }
                                )
                            ),
                            hasUnsavedChanges = true,
                            snackbarMessage = "Imported ${release.assetFileName} from GitHub"
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isFetchingGitHubRelease = false,
                            githubFetchError = error.message ?: "Failed to fetch GitHub release"
                        )
                    }
                }
            )
        }
    }

    fun clearGitHubFetchError() {
        _uiState.update { it.copy(githubFetchError = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun notifyMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private fun formatDateMillis(localStartOfDayMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return formatter.format(Date(localStartOfDayMillis))
    }
}

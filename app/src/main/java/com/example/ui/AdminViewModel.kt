package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdminRepository
import com.example.data.ConnectionStatus
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

data class AdminUiState(
    val currentScreen: AdminScreen = AdminScreen.Setup,
    val savedDatabaseUrl: String = "",
    val backendMode: FirebaseBackendMode = FirebaseBackendMode.REST_API,

    // Setup Screen state
    val setupUrlInput: String = "",
    val setupError: String? = null,
    val isConnectingUrl: Boolean = false,

    // Dashboard state
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING,
    val isEnabled: Boolean = false,
    val version: String = "",
    val title: String = "",
    val description: String = "",
    val apkUrl: String = "",
    val releaseDate: String = "",
    val releaseDateMillis: Long = 0L,
    val isMandatory: Boolean = false,
    val downloadClicks: Long = 0L,
    val lastUpdatedAt: Long = 0L,
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

    // --- Dashboard Actions ---

    private fun startObservingDatabase() {
        stopObservingDatabase()
        syncJob = viewModelScope.launch {
            repository.observeUpdateConfig().collect { snapshot ->
                _uiState.update { state ->
                    val remote = snapshot.config
                    if (remote != null && (!state.hasHydratedFromRemote || !state.hasUnsavedChanges)) {
                        val resolvedReleaseDate = remote.releaseDate.ifBlank {
                            if (remote.releaseDateMillis > 0L) formatDateMillis(remote.releaseDateMillis) else ""
                        }
                        state.copy(
                            connectionStatus = snapshot.status,
                            isEnabled = remote.isEnabled,
                            version = remote.version,
                            title = remote.title,
                            description = remote.description,
                            apkUrl = remote.apkUrl,
                            releaseDate = resolvedReleaseDate,
                            releaseDateMillis = remote.releaseDateMillis,
                            isMandatory = remote.isMandatory,
                            downloadClicks = remote.downloadClicks,
                            lastUpdatedAt = remote.updatedAt,
                            hasHydratedFromRemote = true,
                            isRefreshing = false
                        )
                    } else if (remote != null) {
                        state.copy(
                            connectionStatus = snapshot.status,
                            downloadClicks = remote.downloadClicks,
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
                    val resolvedReleaseDate = remote.releaseDate.ifBlank {
                        if (remote.releaseDateMillis > 0L) formatDateMillis(remote.releaseDateMillis) else ""
                    }
                    state.copy(
                        connectionStatus = snapshot.status,
                        isEnabled = remote.isEnabled,
                        version = remote.version,
                        title = remote.title,
                        description = remote.description,
                        apkUrl = remote.apkUrl,
                        releaseDate = resolvedReleaseDate,
                        releaseDateMillis = remote.releaseDateMillis,
                        isMandatory = remote.isMandatory,
                        downloadClicks = remote.downloadClicks,
                        lastUpdatedAt = remote.updatedAt,
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

    fun onUpdateEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(isEnabled = enabled, hasUnsavedChanges = true) }
    }

    fun onVersionChanged(value: String) {
        _uiState.update { it.copy(version = value, hasUnsavedChanges = true) }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value, hasUnsavedChanges = true) }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value, hasUnsavedChanges = true) }
    }

    fun onApkUrlChanged(value: String) {
        _uiState.update { it.copy(apkUrl = value, hasUnsavedChanges = true) }
    }

    fun onReleaseImmediatelySelected() {
        _uiState.update {
            it.copy(
                releaseDate = "",
                releaseDateMillis = 0L,
                hasUnsavedChanges = true
            )
        }
    }

    fun onReleaseDateSelected(startOfDayMillis: Long) {
        val formatted = formatDateMillis(startOfDayMillis)
        _uiState.update {
            it.copy(
                releaseDate = formatted,
                releaseDateMillis = startOfDayMillis,
                hasUnsavedChanges = true
            )
        }
    }

    fun onMandatoryChanged(mandatory: Boolean) {
        _uiState.update { it.copy(isMandatory = mandatory, hasUnsavedChanges = true) }
    }

    fun saveToFirebase() {
        val state = _uiState.value
        if (state.isSaving) return

        if (state.version.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Please enter a version number (e.g., 2.0)") }
            return
        }
        if (state.title.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Please enter an update title") }
            return
        }
        if (state.apkUrl.isNotBlank() &&
            !state.apkUrl.trim().startsWith("https://", ignoreCase = true) &&
            !state.apkUrl.trim().startsWith("http://", ignoreCase = true)
        ) {
            _uiState.update { it.copy(snackbarMessage = "APK Download URL must start with https:// or http://") }
            return
        }

        val config = UpdateConfig(
            isEnabled = state.isEnabled,
            version = state.version.trim(),
            title = state.title.trim(),
            description = state.description.trim(),
            apkUrl = state.apkUrl.trim(),
            releaseDate = state.releaseDate.trim(),
            releaseDateMillis = state.releaseDateMillis,
            isMandatory = state.isMandatory,
            downloadClicks = state.downloadClicks
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = repository.saveUpdateConfig(config)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasUnsavedChanges = false,
                            connectionStatus = ConnectionStatus.CONNECTED,
                            lastUpdatedAt = System.currentTimeMillis(),
                            snackbarMessage = "Saved to /battery_boss_update/ successfully"
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
                        state.copy(
                            isFetchingGitHubRelease = false,
                            githubFetchError = null,
                            apkUrl = release.apkDownloadUrl,
                            version = release.version.ifBlank { state.version },
                            title = release.title.ifBlank { state.title },
                            description = release.description.ifBlank { state.description },
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

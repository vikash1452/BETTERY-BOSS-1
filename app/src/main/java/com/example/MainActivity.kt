package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AdminDashboard
import com.example.ui.AdminScreen
import com.example.ui.AdminViewModel
import com.example.ui.SetupScreen
import com.example.ui.theme.BatteryBossTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryBossTheme {
                BatteryBossAdminApp()
            }
        }
    }
}

@Composable
fun BatteryBossAdminApp(
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
        viewModel.clearSnackbar()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(240)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "admin_screen_transition"
        ) { screen ->
            when (screen) {
                AdminScreen.Setup -> SetupScreen(
                    uiState = uiState,
                    onUrlChange = viewModel::onSetupUrlChanged,
                    onConnectClick = viewModel::connectDatabaseUrl
                )

                AdminScreen.Dashboard -> AdminDashboard(
                    uiState = uiState,
                    onUpdateEnabledChange = viewModel::onUpdateEnabledChanged,
                    onVersionChange = viewModel::onVersionChanged,
                    onTitleChange = viewModel::onTitleChanged,
                    onDescriptionChange = viewModel::onDescriptionChanged,
                    onApkUrlChange = viewModel::onApkUrlChanged,
                    onReleaseImmediatelySelected = viewModel::onReleaseImmediatelySelected,
                    onReleaseDateSelected = viewModel::onReleaseDateSelected,
                    onMandatoryChange = viewModel::onMandatoryChanged,
                    onSaveToFirebaseClick = viewModel::saveToFirebase,
                    onRefreshClick = viewModel::refreshFromFirebase,
                    onResetUrlClick = viewModel::resetFirebaseUrl,
                    onFetchGitHubRelease = viewModel::fetchGitHubReleaseApk,
                    onClearGitHubFetchError = viewModel::clearGitHubFetchError,
                    onRulesCopied = viewModel::notifyMessage
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                actionColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

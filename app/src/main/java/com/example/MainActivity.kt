package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Crossfade(
        targetState = uiState.currentScreen,
        animationSpec = tween(durationMillis = 280),
        modifier = Modifier.fillMaxSize(),
        label = "screen_crossfade"
    ) { screen ->
        when (screen) {
            AdminScreen.Setup -> SetupScreen(
                uiState = uiState,
                onUrlChange = viewModel::onSetupUrlChanged,
                onConnectClick = viewModel::connectDatabaseUrl
            )
            AdminScreen.Dashboard -> AdminDashboard(
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}

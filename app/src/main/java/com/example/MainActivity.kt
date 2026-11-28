package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.AdminPanelUiState
import com.example.ui.AppRoute
import com.example.ui.DrawerSection
import com.example.ui.FirebaseUrlSetupScreen
import com.example.ui.MainShellScreen
import com.example.ui.ProfileSetupScreen
import com.example.ui.SavedLinkItem
import com.example.ui.SplashScreen
import com.example.ui.theme.AdminPanelTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminPanelRootApp()
        }
    }
}

@Composable
fun AdminPanelRootApp() {
    var uiState by remember { mutableStateOf(AdminPanelUiState()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = msg,
            duration = SnackbarDuration.Short
        )
        uiState = uiState.copy(snackbarMessage = null)
    }

    AdminPanelTheme(themeMode = uiState.themeMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState.currentRoute,
                transitionSpec = {
                    fadeIn(animationSpec = tween(240)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "admin_route_transition"
            ) { route ->
                when (route) {
                    AppRoute.SPLASH -> SplashScreen(
                        themeMode = uiState.themeMode,
                        onSplashFinished = {
                            uiState = uiState.copy(currentRoute = AppRoute.PROFILE_SETUP)
                        }
                    )

                    AppRoute.PROFILE_SETUP -> ProfileSetupScreen(
                        nameInput = uiState.profileInput,
                        avatarColorIndex = uiState.avatarColorIndex,
                        onNameChange = { uiState = uiState.copy(profileInput = it) },
                        onCycleAvatarColor = {
                            uiState = uiState.copy(avatarColorIndex = (uiState.avatarColorIndex + 1) % 4)
                        },
                        onSkipClick = {
                            uiState = uiState.copy(
                                profileName = "Guest",
                                currentRoute = AppRoute.FIREBASE_SETUP
                            )
                        },
                        onGetStartedClick = {
                            val resolvedName = uiState.profileInput.trim().ifBlank { "Guest" }
                            uiState = uiState.copy(
                                profileName = resolvedName,
                                currentRoute = AppRoute.FIREBASE_SETUP
                            )
                        }
                    )

                    AppRoute.FIREBASE_SETUP -> {
                        BackHandler {
                            uiState = uiState.copy(currentRoute = AppRoute.PROFILE_SETUP)
                        }
                        FirebaseUrlSetupScreen(
                            urlInput = uiState.firebaseSetupUrlInput,
                            onUrlChange = { uiState = uiState.copy(firebaseSetupUrlInput = it) },
                            onSaveAndContinueClick = {
                                val entered = uiState.firebaseSetupUrlInput.trim()
                                val updatedLinks = if (entered.isNotEmpty()) {
                                    listOf(
                                        SavedLinkItem(
                                            id = "link_${System.currentTimeMillis()}",
                                            url = entered,
                                            isActive = true
                                        )
                                    ) + uiState.savedLinks.map { it.copy(isActive = false) }
                                } else {
                                    uiState.savedLinks
                                }
                                uiState = uiState.copy(
                                    savedLinks = updatedLinks,
                                    currentRoute = AppRoute.MAIN_SHELL,
                                    currentSection = DrawerSection.HOME
                                )
                            }
                        )
                    }

                    AppRoute.MAIN_SHELL -> MainShellScreen(
                        uiState = uiState,
                        onSelectSection = { section ->
                            uiState = uiState.copy(currentSection = section)
                        },
                        onRefreshAppClick = {
                            uiState = uiState.copy(snackbarMessage = "App UI refreshed")
                        },
                        onExitAppClick = {
                            activity?.finishAffinity()
                        },
                        onUpdateHomeForm = { transform ->
                            uiState = uiState.copy(homeForm = transform(uiState.homeForm))
                        },
                        onFetchVersionClick = {
                            if (!uiState.homeForm.isFetchingVersion) {
                                uiState = uiState.copy(
                                    homeForm = uiState.homeForm.copy(isFetchingVersion = true)
                                )
                                scope.launch {
                                    delay(500L)
                                    uiState = uiState.copy(
                                        homeForm = uiState.homeForm.copy(isFetchingVersion = false),
                                        snackbarMessage = "UI Preview: Fetched version ${uiState.homeForm.appVersion}"
                                    )
                                }
                            }
                        },
                        onPushChangeClick = {
                            if (!uiState.homeForm.isPushingChange) {
                                uiState = uiState.copy(
                                    homeForm = uiState.homeForm.copy(isPushingChange = true)
                                )
                                scope.launch {
                                    delay(500L)
                                    uiState = uiState.copy(
                                        homeForm = uiState.homeForm.copy(isPushingChange = false),
                                        snackbarMessage = "UI Preview: Change pushed"
                                    )
                                }
                            }
                        },
                        onSearchQueryChange = { q ->
                            uiState = uiState.copy(searchQuery = q)
                        },
                        onEditDashboardApp = { updated ->
                            uiState = uiState.copy(
                                apps = uiState.apps.map { if (it.id == updated.id) updated else it },
                                snackbarMessage = "Updated ${updated.appName}"
                            )
                        },
                        onDeleteDashboardApp = { appId ->
                            uiState = uiState.copy(
                                apps = uiState.apps.filterNot { it.id == appId },
                                snackbarMessage = "Removed app from list"
                            )
                        },
                        onManageLinkInputChange = { input ->
                            uiState = uiState.copy(newLinkInput = input)
                        },
                        onSaveLinkClick = {
                            val trimmed = uiState.newLinkInput.trim()
                            if (trimmed.isEmpty()) {
                                uiState = uiState.copy(snackbarMessage = "Enter a Firebase URL first")
                            } else {
                                val next = SavedLinkItem(
                                    id = "link_${System.currentTimeMillis()}",
                                    url = trimmed,
                                    isActive = true
                                )
                                uiState = uiState.copy(
                                    newLinkInput = "",
                                    savedLinks = listOf(next) + uiState.savedLinks.map { it.copy(isActive = false) },
                                    snackbarMessage = "Link saved & activated"
                                )
                            }
                        },
                        onSelectActiveLink = { id ->
                            uiState = uiState.copy(
                                savedLinks = uiState.savedLinks.map { it.copy(isActive = it.id == id) },
                                snackbarMessage = "Active link switched"
                            )
                        },
                        onCopyLink = {
                            uiState = uiState.copy(snackbarMessage = "URL copied to clipboard")
                        },
                        onDeleteLink = { id ->
                            uiState = uiState.copy(
                                savedLinks = uiState.savedLinks.filterNot { it.id == id },
                                snackbarMessage = "Link deleted"
                            )
                        },
                        onEditProfileName = { newName ->
                            uiState = uiState.copy(
                                profileName = newName,
                                profileInput = newName,
                                snackbarMessage = "Profile name updated"
                            )
                        },
                        onThemeModeSelected = { mode ->
                            uiState = uiState.copy(themeMode = mode)
                        },
                        onLogoutClick = {
                            uiState = uiState.copy(
                                currentRoute = AppRoute.PROFILE_SETUP,
                                currentSection = DrawerSection.HOME,
                                snackbarMessage = "Logged out"
                            )
                        },
                        onUiNotice = { msg ->
                            uiState = uiState.copy(snackbarMessage = msg)
                        }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ConnectionStatus
import com.example.data.DialogPreset
import com.example.data.FirebaseConstants
import com.example.ui.components.ButtonsAndBlocksTab
import com.example.ui.components.ColorsAndThemeTab
import com.example.ui.components.DialogPreviewPane
import com.example.ui.components.LayoutAndMediaTab
import com.example.ui.components.ModeAndContentTab
import com.example.ui.components.adminTextFieldColors
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showGitHubDialog by remember { mutableStateOf(false) }
    var showResetUrlDialog by remember { mutableStateOf(false) }
    var showRulesSheet by remember { mutableStateOf(false) }
    var showPreviewSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            SaveBottomBar(
                isSaving = uiState.isSaving,
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                onSaveClick = viewModel::saveToFirebase
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            val isWideScreen = maxWidth >= 840.dp

            Column(modifier = Modifier.fillMaxSize()) {
                DashboardTopHeader(
                    uiState = uiState,
                    onRefreshClick = viewModel::refreshFromFirebase,
                    onOpenPreviewSheet = { showPreviewSheet = true },
                    onOpenRulesSheet = { showRulesSheet = true },
                    onOpenResetDialog = { showResetUrlDialog = true }
                )

                PresetQuickActionStrip(
                    isDialogEnabled = uiState.config.isEnabled,
                    isSaving = uiState.isSaving,
                    onApplyPreset = viewModel::applyPreset,
                    onResetToDefault = viewModel::resetUiToDefault,
                    onDisableDialog = viewModel::disableDialogQuickly
                )

                val visibleTabs = remember(isWideScreen) {
                    if (isWideScreen) {
                        DashboardTab.entries.filter { it != DashboardTab.LIVE_PREVIEW }
                    } else {
                        DashboardTab.entries.toList()
                    }
                }

                val activeTab = if (isWideScreen && uiState.selectedTab == DashboardTab.LIVE_PREVIEW) {
                    DashboardTab.MODE_CONTENT
                } else {
                    uiState.selectedTab
                }

                val selectedIndex = visibleTabs.indexOf(activeTab).coerceAtLeast(0)

                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        if (selectedIndex in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    visibleTabs.forEach { tab ->
                        Tab(
                            selected = tab == activeTab,
                            onClick = { viewModel.onTabSelected(tab) },
                            text = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (tab == activeTab) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1.25f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp)
                        ) {
                            EditorTabContent(
                                tab = activeTab,
                                uiState = uiState,
                                viewModel = viewModel,
                                onOpenDatePicker = { showDatePicker = true },
                                onOpenGitHubDialog = { showGitHubDialog = true },
                                onTestApkUrl = { url -> openApkUrl(context, url, viewModel::notifyMessage) }
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(0.95f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp)
                        ) {
                            DialogPreviewPane(config = uiState.config)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        EditorTabContent(
                            tab = activeTab,
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenDatePicker = { showDatePicker = true },
                            onOpenGitHubDialog = { showGitHubDialog = true },
                            onTestApkUrl = { url -> openApkUrl(context, url, viewModel::notifyMessage) }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        ReleaseDatePickerModal(
            initialMillis = uiState.config.releaseDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { localStartMillis ->
                viewModel.onReleaseDateSelected(localStartMillis)
                showDatePicker = false
            }
        )
    }

    if (showGitHubDialog) {
        GitHubReleaseImportDialog(
            isFetching = uiState.isFetchingGitHubRelease,
            errorMessage = uiState.githubFetchError,
            onDismiss = {
                viewModel.clearGitHubFetchError()
                showGitHubDialog = false
            },
            onFetch = { repoInput ->
                viewModel.fetchGitHubReleaseApk(repoInput) {
                    showGitHubDialog = false
                }
            }
        )
    }

    if (showResetUrlDialog) {
        AlertDialog(
            onDismissRequest = { showResetUrlDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = { Text(stringResource(R.string.dialog_reset_title)) },
            text = { Text(stringResource(R.string.dialog_reset_message)) },
            dismissButton = {
                TextButton(onClick = { showResetUrlDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetUrlDialog = false
                        viewModel.resetFirebaseUrl()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.dialog_confirm_reset))
                }
            }
        )
    }

    if (showPreviewSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPreviewSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                DialogPreviewPane(config = uiState.config)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showRulesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRulesSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.rules_sheet_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(R.string.rules_sheet_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = FirebaseConstants.SECURITY_RULES_JSON,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("Firebase Rules", FirebaseConstants.SECURITY_RULES_JSON)
                        )
                        viewModel.notifyMessage(context.getString(R.string.rules_copied))
                        showRulesSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.rules_copy_button))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DashboardTopHeader(
    uiState: AdminUiState,
    onRefreshClick: () -> Unit,
    onOpenPreviewSheet: () -> Unit,
    onOpenRulesSheet: () -> Unit,
    onOpenResetDialog: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        ConnectionStatusPill(status = uiState.connectionStatus)
                    }
                    Text(
                        text = uiState.savedDatabaseUrl.removePrefix("https://"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenPreviewSheet) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = "Open mobile preview",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onRefreshClick, enabled = !uiState.isRefreshing) {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh_data),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                IconButton(onClick = onOpenRulesSheet) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = stringResource(R.string.cd_view_rules),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onOpenResetDialog) {
                    Icon(
                        imageVector = Icons.Rounded.LinkOff,
                        contentDescription = stringResource(R.string.cd_reset_url),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusPill(status: ConnectionStatus) {
    val (label, color) = when (status) {
        ConnectionStatus.CONNECTED -> stringResource(R.string.dashboard_connected) to MaterialTheme.colorScheme.primary
        ConnectionStatus.CONNECTING -> stringResource(R.string.dashboard_connecting) to MaterialTheme.colorScheme.secondary
        ConnectionStatus.DISCONNECTED -> stringResource(R.string.dashboard_disconnected) to MaterialTheme.colorScheme.error
    }

    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun PresetQuickActionStrip(
    isDialogEnabled: Boolean,
    isSaving: Boolean,
    onApplyPreset: (DialogPreset) -> Unit,
    onResetToDefault: () -> Unit,
    onDisableDialog: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DialogPreset.entries.forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable { onApplyPreset(preset) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onResetToDefault,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Reset to Default", style = MaterialTheme.typography.labelSmall)
            }

            if (isDialogEnabled) {
                OutlinedButton(
                    onClick = onDisableDialog,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Disable Dialog",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorTabContent(
    tab: DashboardTab,
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    onOpenDatePicker: () -> Unit,
    onOpenGitHubDialog: () -> Unit,
    onTestApkUrl: (String) -> Unit
) {
    when (tab) {
        DashboardTab.MODE_CONTENT -> ModeAndContentTab(
            uiState = uiState,
            viewModel = viewModel,
            onOpenDatePicker = onOpenDatePicker,
            onOpenGitHubDialog = onOpenGitHubDialog,
            onTestApkUrl = onTestApkUrl
        )
        DashboardTab.LAYOUT_MEDIA -> LayoutAndMediaTab(
            uiState = uiState,
            viewModel = viewModel
        )
        DashboardTab.COLORS_THEME -> ColorsAndThemeTab(
            uiState = uiState,
            viewModel = viewModel
        )
        DashboardTab.BUTTONS_BLOCKS -> ButtonsAndBlocksTab(
            uiState = uiState,
            viewModel = viewModel
        )
        DashboardTab.LIVE_PREVIEW -> DialogPreviewPane(
            config = uiState.config
        )
    }
}

@Composable
private fun SaveBottomBar(
    isSaving: Boolean,
    hasUnsavedChanges: Boolean,
    onSaveClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasUnsavedChanges) "Unsaved changes ready" else "Synced with /battery_boss_update",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (hasUnsavedChanges) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Preserves click counters automatically",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onSaveClick,
                enabled = !isSaving,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("save_firebase_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.dashboard_saving))
                } else {
                    Icon(
                        imageVector = if (hasUnsavedChanges) Icons.Rounded.Save else Icons.Rounded.CloudDone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.dashboard_save_button))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReleaseDatePickerModal(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = if (initialMillis > 0L) initialMillis else System.currentTimeMillis()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val utcSelected = state.selectedDateMillis ?: System.currentTimeMillis()
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = utcSelected
                    }
                    val localCal = Calendar.getInstance().apply {
                        set(
                            utcCal.get(Calendar.YEAR),
                            utcCal.get(Calendar.MONTH),
                            utcCal.get(Calendar.DAY_OF_MONTH),
                            0,
                            0,
                            0
                        )
                        set(Calendar.MILLISECOND, 0)
                    }
                    onConfirm(localCal.timeInMillis)
                }
            ) {
                Text(stringResource(R.string.dialog_date_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun GitHubReleaseImportDialog(
    isFetching: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onFetch: (String) -> Unit
) {
    var repoInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Text(stringResource(R.string.dialog_github_title))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.dialog_github_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = repoInput,
                    onValueChange = { repoInput = it },
                    label = { Text(stringResource(R.string.dialog_github_repo_label)) },
                    placeholder = { Text(stringResource(R.string.dialog_github_repo_placeholder)) },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = { onFetch(repoInput) },
                enabled = repoInput.isNotBlank() && !isFetching
            ) {
                if (isFetching) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.dialog_github_fetching))
                } else {
                    Text(stringResource(R.string.dialog_github_fetch_button))
                }
            }
        }
    )
}

private fun openApkUrl(
    context: Context,
    url: String,
    onError: (String) -> Unit
) {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmed))
        context.startActivity(intent)
    } catch (_: Exception) {
        onError("Unable to open URL on this device")
    }
}

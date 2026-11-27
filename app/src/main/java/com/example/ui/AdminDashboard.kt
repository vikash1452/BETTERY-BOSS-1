package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ConnectionStatus
import com.example.data.DialogButtonConfig
import com.example.data.DialogPreset
import com.example.data.DialogUiConfig
import com.example.data.FirebaseConstants
import com.example.data.UpdateConfig
import com.example.ui.components.ColorsAndThemeTab
import com.example.ui.components.CustomBlockOrderingCard
import com.example.ui.components.DialogPreviewPane
import com.example.ui.components.ExtraButtonsBuilderCard
import com.example.ui.components.LayoutAndMediaTab
import com.example.ui.components.ModeAndContentTab
import com.example.ui.components.PrimaryAndDismissButtonsCard
import com.example.ui.components.adminTextFieldColors
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    uiState: AdminUiState,
    onTabSelected: (DashboardTab) -> Unit,
    onUpdateConfig: ((UpdateConfig) -> UpdateConfig) -> Unit,
    onUpdateUiConfig: ((DialogUiConfig) -> DialogUiConfig) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReleaseImmediatelySelected: () -> Unit,
    onReleaseDateSelected: (Long) -> Unit,
    onUpsertExtraButton: (DialogButtonConfig, Int?) -> Unit,
    onDeleteExtraButton: (Int) -> Unit,
    onMoveExtraButton: (Int, Int) -> Unit,
    onToggleCustomBlockOrder: (Boolean) -> Unit,
    onAddUiBlock: (String) -> Unit,
    onRemoveUiBlock: (Int) -> Unit,
    onMoveUiBlock: (Int, Int) -> Unit,
    onApplyPreset: (DialogPreset) -> Unit,
    onResetUiToDefault: () -> Unit,
    onDisableDialogQuickly: () -> Unit,
    onSaveToFirebaseClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onResetUrlClick: () -> Unit,
    onFetchGitHubRelease: (String, () -> Unit) -> Unit,
    onClearGitHubFetchError: () -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showResetConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showRulesSheet by rememberSaveable { mutableStateOf(false) }
    var showPreviewSheet by rememberSaveable { mutableStateOf(false) }
    var showGitHubDialog by rememberSaveable { mutableStateOf(false) }
    var githubRepoInput by rememberSaveable { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    val rulesCopiedMessage = stringResource(R.string.rules_copied)

    val displayHost = uiState.savedDatabaseUrl
        .removePrefix("https://")
        .removePrefix("http://")
        .substringBefore("/")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            StickySaveBottomBar(
                isSaving = uiState.isSaving,
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                onSaveClick = onSaveToFirebaseClick,
                onPreviewClick = { showPreviewSheet = true }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            val isWideScreen = maxWidth >= 840.dp
            val editorTabs = if (isWideScreen) {
                DashboardTab.entries.filter { it != DashboardTab.LIVE_PREVIEW }
            } else {
                DashboardTab.entries
            }
            val activeTab = if (isWideScreen && uiState.selectedTab == DashboardTab.LIVE_PREVIEW) {
                DashboardTab.MODE_CONTENT
            } else {
                uiState.selectedTab
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                DashboardTopHeader(
                    status = uiState.connectionStatus,
                    endpointLabel = "$displayHost/${FirebaseConstants.UPDATE_NODE_PATH}",
                    isRefreshing = uiState.isRefreshing,
                    onRefreshClick = onRefreshClick,
                    onRulesClick = { showRulesSheet = true },
                    onQuickPreviewClick = { showPreviewSheet = true },
                    onResetUrlClick = { showResetConfirmDialog = true }
                )

                // Main Content Area
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight()
                        ) {
                            PresetsAndQuickActionsStrip(
                                onApplyPreset = onApplyPreset,
                                onResetUiToDefault = onResetUiToDefault,
                                onDisableDialogQuickly = onDisableDialogQuickly
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            DashboardTabsRow(
                                tabs = editorTabs,
                                selectedTab = activeTab,
                                onTabSelected = onTabSelected
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .imePadding(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                EditorTabBody(
                                    tab = activeTab,
                                    uiState = uiState,
                                    onUpdateConfig = onUpdateConfig,
                                    onUpdateUiConfig = onUpdateUiConfig,
                                    onTitleChange = onTitleChange,
                                    onDescriptionChange = onDescriptionChange,
                                    onReleaseImmediatelySelected = onReleaseImmediatelySelected,
                                    onPickScheduledDateClick = { showDatePicker = true },
                                    onOpenGitHubDialog = {
                                        onClearGitHubFetchError()
                                        showGitHubDialog = true
                                    },
                                    onUpsertExtraButton = onUpsertExtraButton,
                                    onDeleteExtraButton = onDeleteExtraButton,
                                    onMoveExtraButton = onMoveExtraButton,
                                    onToggleCustomBlockOrder = onToggleCustomBlockOrder,
                                    onAddUiBlock = onAddUiBlock,
                                    onRemoveUiBlock = onRemoveUiBlock,
                                    onMoveUiBlock = onMoveUiBlock,
                                    onNotify = onNotify
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            DialogPreviewPane(config = uiState.config)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    ) {
                        PresetsAndQuickActionsStrip(
                            onApplyPreset = onApplyPreset,
                            onResetUiToDefault = onResetUiToDefault,
                            onDisableDialogQuickly = onDisableDialogQuickly,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DashboardTabsRow(
                            tabs = editorTabs,
                            selectedTab = activeTab,
                            onTabSelected = onTabSelected,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .imePadding()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            EditorTabBody(
                                tab = activeTab,
                                uiState = uiState,
                                onUpdateConfig = onUpdateConfig,
                                onUpdateUiConfig = onUpdateUiConfig,
                                onTitleChange = onTitleChange,
                                onDescriptionChange = onDescriptionChange,
                                onReleaseImmediatelySelected = onReleaseImmediatelySelected,
                                onPickScheduledDateClick = { showDatePicker = true },
                                onOpenGitHubDialog = {
                                    onClearGitHubFetchError()
                                    showGitHubDialog = true
                                },
                                onUpsertExtraButton = onUpsertExtraButton,
                                onDeleteExtraButton = onDeleteExtraButton,
                                onMoveExtraButton = onMoveExtraButton,
                                onToggleCustomBlockOrder = onToggleCustomBlockOrder,
                                onAddUiBlock = onAddUiBlock,
                                onRemoveUiBlock = onRemoveUiBlock,
                                onMoveUiBlock = onMoveUiBlock,
                                onNotify = onNotify
                            )

                            OutlinedButton(
                                onClick = { showResetConfirmDialog = true },
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("reset_url_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LinkOff,
                                    contentDescription = stringResource(R.string.cd_reset_url),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.dashboard_reset_url_button),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Material 3 Release Date Picker Dialog
    if (showDatePicker) {
        val initialUtcDateMillis = if (uiState.config.releaseDateMillis > 0L) {
            localStartOfDayToUtcDatePickerMillis(uiState.config.releaseDateMillis)
        } else {
            null
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedUtcMillis ->
                            val startOfDayMillis = utcDatePickerMillisToLocalStartOfDay(selectedUtcMillis)
                            onReleaseDateSelected(startOfDayMillis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_date_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Reset URL Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text(stringResource(R.string.dialog_reset_title), style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = stringResource(R.string.dialog_reset_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetUrlClick()
                    },
                    modifier = Modifier.testTag("confirm_reset_url_button")
                ) {
                    Text(stringResource(R.string.dialog_confirm_reset), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Import Latest APK from GitHub Releases Dialog
    if (showGitHubDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isFetchingGitHubRelease) showGitHubDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text(stringResource(R.string.dialog_github_title), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.dialog_github_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = githubRepoInput,
                        onValueChange = {
                            githubRepoInput = it
                            onClearGitHubFetchError()
                        },
                        label = { Text(stringResource(R.string.dialog_github_repo_label)) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.dialog_github_repo_placeholder),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily)
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        singleLine = true,
                        isError = uiState.githubFetchError != null,
                        shape = MaterialTheme.shapes.small,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_repo_input")
                    )
                    if (uiState.githubFetchError != null) {
                        Text(
                            text = uiState.githubFetchError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = githubRepoInput.isNotBlank() && !uiState.isFetchingGitHubRelease,
                    onClick = { onFetchGitHubRelease(githubRepoInput) { showGitHubDialog = false } },
                    modifier = Modifier.testTag("confirm_github_fetch_button")
                ) {
                    if (uiState.isFetchingGitHubRelease) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.dialog_github_fetching))
                    } else {
                        Text(stringResource(R.string.dialog_github_fetch_button), color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !uiState.isFetchingGitHubRelease, onClick = { showGitHubDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Live Preview Modal Sheet (accessible anytime from any tab)
    if (showPreviewSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPreviewSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogPreviewPane(config = uiState.config)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Firebase Security Rules Sheet
    if (showRulesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRulesSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.rules_sheet_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.rules_sheet_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = FirebaseConstants.SECURITY_RULES_JSON,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            lineHeight = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                }

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(FirebaseConstants.SECURITY_RULES_JSON))
                        onNotify(rulesCopiedMessage)
                        showRulesSheet = false
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.rules_copy_button), style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun EditorTabBody(
    tab: DashboardTab,
    uiState: AdminUiState,
    onUpdateConfig: ((UpdateConfig) -> UpdateConfig) -> Unit,
    onUpdateUiConfig: ((DialogUiConfig) -> DialogUiConfig) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReleaseImmediatelySelected: () -> Unit,
    onPickScheduledDateClick: () -> Unit,
    onOpenGitHubDialog: () -> Unit,
    onUpsertExtraButton: (DialogButtonConfig, Int?) -> Unit,
    onDeleteExtraButton: (Int) -> Unit,
    onMoveExtraButton: (Int, Int) -> Unit,
    onToggleCustomBlockOrder: (Boolean) -> Unit,
    onAddUiBlock: (String) -> Unit,
    onRemoveUiBlock: (Int) -> Unit,
    onMoveUiBlock: (Int, Int) -> Unit,
    onNotify: (String) -> Unit
) {
    when (tab) {
        DashboardTab.MODE_CONTENT -> ModeAndContentTab(
            uiState = uiState,
            onUpdateConfig = onUpdateConfig,
            onUpdateUi = onUpdateUiConfig,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onReleaseImmediatelyClick = onReleaseImmediatelySelected,
            onPickScheduledDateClick = onPickScheduledDateClick,
            onOpenGitHubDialog = onOpenGitHubDialog,
            onNotify = onNotify
        )

        DashboardTab.LAYOUT_MEDIA -> LayoutAndMediaTab(
            uiConfig = uiState.config.uiConfig,
            onUpdateUi = onUpdateUiConfig
        )

        DashboardTab.COLORS_THEME -> ColorsAndThemeTab(
            uiConfig = uiState.config.uiConfig,
            onUpdateUi = onUpdateUiConfig
        )

        DashboardTab.BUTTONS_BLOCKS -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PrimaryAndDismissButtonsCard(
                uiConfig = uiState.config.uiConfig,
                onUpdateUi = onUpdateUiConfig
            )
            ExtraButtonsBuilderCard(
                buttons = uiState.config.uiConfig.buttons,
                buttonClicks = uiState.config.buttonClicks,
                onUpsertButton = onUpsertExtraButton,
                onDeleteButton = onDeleteExtraButton,
                onMoveButton = onMoveExtraButton
            )
            CustomBlockOrderingCard(
                isCustomOrderEnabled = uiState.isCustomBlockOrderEnabled,
                blocks = uiState.config.uiConfig.uiBlocks,
                onToggleCustomOrder = onToggleCustomBlockOrder,
                onAddBlock = onAddUiBlock,
                onRemoveBlock = onRemoveUiBlock,
                onMoveBlock = onMoveUiBlock
            )
        }

        DashboardTab.LIVE_PREVIEW -> DialogPreviewPane(config = uiState.config)
    }
}

@Composable
private fun DashboardTopHeader(
    status: ConnectionStatus,
    endpointLabel: String,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onRulesClick: () -> Unit,
    onQuickPreviewClick: () -> Unit,
    onResetUrlClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_command_header),
            contentDescription = stringResource(R.string.cd_header_banner),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.38f)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConnectionStatusPill(status = status)

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onQuickPreviewClick) {
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = "Quick Dialog Preview",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !isRefreshing,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.cd_refresh_data),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(
                        onClick = onRulesClick,
                        modifier = Modifier.testTag("view_rules_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = stringResource(R.string.cd_view_rules),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(onClick = onResetUrlClick) {
                        Icon(
                            imageVector = Icons.Rounded.LinkOff,
                            contentDescription = stringResource(R.string.cd_reset_url),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.app_full_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = endpointLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = MaterialTheme.colorScheme.tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PresetsAndQuickActionsStrip(
    onApplyPreset: (DialogPreset) -> Unit,
    onResetUiToDefault: () -> Unit,
    onDisableDialogQuickly: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DialogPreset.entries.forEach { preset ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onApplyPreset(preset) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoFixHigh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onResetUiToDefault)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Reset to Default",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onDisableDialogQuickly)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Disable Dialog",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DashboardTabsRow(
    tabs: List<DashboardTab>,
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .height(38.dp)
                    .clip(CircleShape)
                    .clickable { onTabSelected(tab) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StickySaveBottomBar(
    isSaving: Boolean,
    hasUnsavedChanges: Boolean,
    onSaveClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPreviewClick,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 14.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = "Preview",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Preview", color = MaterialTheme.colorScheme.tertiary)
            }

            Button(
                onClick = onSaveClick,
                enabled = !isSaving,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("save_to_firebase_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.dashboard_saving), style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(Icons.Rounded.CloudUpload, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasUnsavedChanges) {
                            "${stringResource(R.string.dashboard_save_button)} •"
                        } else {
                            stringResource(R.string.dashboard_save_button)
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusPill(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val (dotColor, labelText, containerColor) = when (status) {
        ConnectionStatus.CONNECTED -> Triple(
            MaterialTheme.colorScheme.primary,
            stringResource(R.string.dashboard_connected),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
        )
        ConnectionStatus.CONNECTING -> Triple(
            MaterialTheme.colorScheme.tertiary,
            stringResource(R.string.dashboard_connecting),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
        )
        ConnectionStatus.DISCONNECTED -> Triple(
            MaterialTheme.colorScheme.error,
            stringResource(R.string.dashboard_disconnected),
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
        )
    }

    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.5f)),
        modifier = modifier.testTag("connection_status_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .alpha(if (status == ConnectionStatus.CONNECTED || status == ConnectionStatus.CONNECTING) pulseAlpha else 1f)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = "FIREBASE $labelText",
                style = MaterialTheme.typography.labelSmall,
                color = dotColor
            )
        }
    }
}

private fun utcDatePickerMillisToLocalStartOfDay(selectedUtcMillis: Long): Long {
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = selectedUtcMillis
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun localStartOfDayToUtcDatePickerMillis(localStartOfDayMillis: Long): Long {
    val localCalendar = Calendar.getInstance().apply {
        timeInMillis = localStartOfDayMillis
    }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

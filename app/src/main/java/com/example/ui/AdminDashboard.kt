package com.example.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.WarningAmber
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ConnectionStatus
import com.example.data.FirebaseConstants
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    uiState: AdminUiState,
    onUpdateEnabledChange: (Boolean) -> Unit,
    onVersionChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onApkUrlChange: (String) -> Unit,
    onReleaseImmediatelySelected: () -> Unit,
    onReleaseDateSelected: (Long) -> Unit,
    onMandatoryChange: (Boolean) -> Unit,
    onSaveToFirebaseClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onResetUrlClick: () -> Unit,
    onFetchGitHubRelease: (String, () -> Unit) -> Unit,
    onClearGitHubFetchError: () -> Unit,
    onRulesCopied: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showResetConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showRulesSheet by rememberSaveable { mutableStateOf(false) }
    var showGitHubDialog by rememberSaveable { mutableStateOf(false) }
    var githubRepoInput by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val rulesCopiedMessage = stringResource(R.string.rules_copied)

    val displayHost = uiState.savedDatabaseUrl
        .removePrefix("https://")
        .removePrefix("http://")
        .substringBefore("/")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Telemetry Header
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
                        .alpha(0.45f)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .align(Alignment.Center)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ConnectionStatusPill(status = uiState.connectionStatus)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = onRefreshClick,
                                enabled = !uiState.isRefreshing,
                                modifier = Modifier.testTag("refresh_button")
                            ) {
                                if (uiState.isRefreshing) {
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
                                onClick = { showRulesSheet = true },
                                modifier = Modifier.testTag("view_rules_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = stringResource(R.string.cd_view_rules),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.app_full_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$displayHost/${FirebaseConstants.UPDATE_NODE_PATH}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Main Dashboard Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Telemetry KPI Card: Total Download Clicks
                DownloadClicksTelemetryCard(
                    downloadClicks = uiState.downloadClicks,
                    modifier = Modifier.fillMaxWidth()
                )

                // Master Update ON/OFF Switch Card
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = if (uiState.isEnabled) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (uiState.isEnabled) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateEnabledChange(!uiState.isEnabled) }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.isEnabled) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PowerSettingsNew,
                                        contentDescription = null,
                                        tint = if (uiState.isEnabled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.dashboard_update_switch_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uiState.isEnabled) {
                                        stringResource(R.string.dashboard_update_active)
                                    } else {
                                        stringResource(R.string.dashboard_update_paused)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.isEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isEnabled,
                            onCheckedChange = onUpdateEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("update_enabled_switch")
                        )
                    }
                }

                // Release Configuration Form Card
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NewReleases,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.dashboard_release_config_header),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Version
                        OutlinedTextField(
                            value = uiState.version,
                            onValueChange = onVersionChange,
                            label = { Text(stringResource(R.string.dashboard_version_label)) },
                            placeholder = { Text(stringResource(R.string.dashboard_version_placeholder)) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Tag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            colors = dashboardTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("version_input")
                        )

                        // Title
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            label = { Text(stringResource(R.string.dashboard_title_label)) },
                            placeholder = { Text(stringResource(R.string.dashboard_title_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Title,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            colors = dashboardTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("title_input")
                        )

                        // Description (Multi-line)
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = onDescriptionChange,
                            label = { Text(stringResource(R.string.dashboard_description_label)) },
                            placeholder = { Text(stringResource(R.string.dashboard_description_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            minLines = 4,
                            maxLines = 7,
                            shape = MaterialTheme.shapes.small,
                            colors = dashboardTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("description_input")
                        )

                        // APK Download URL
                        OutlinedTextField(
                            value = uiState.apkUrl,
                            onValueChange = onApkUrlChange,
                            label = { Text(stringResource(R.string.dashboard_apk_url_label)) },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.dashboard_apk_url_placeholder),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            },
                            trailingIcon = {
                                val trimmedUrl = uiState.apkUrl.trim()
                                if (trimmedUrl.startsWith("https://", ignoreCase = true) ||
                                    trimmedUrl.startsWith("http://", ignoreCase = true)
                                ) {
                                    IconButton(
                                        onClick = {
                                            runCatching {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(trimmedUrl))
                                                )
                                            }.onFailure {
                                                onRulesCopied("Could not open APK URL")
                                            }
                                        },
                                        modifier = Modifier.testTag("test_download_apk_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.OpenInNew,
                                            contentDescription = stringResource(R.string.cd_test_download_apk),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            shape = MaterialTheme.shapes.small,
                            colors = dashboardTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apk_url_input")
                        )

                        // Quick Import from GitHub Releases
                        OutlinedButton(
                            onClick = {
                                onClearGitHubFetchError()
                                showGitHubDialog = true
                            },
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("fetch_github_release_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.dashboard_github_fetch_cta),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Release Schedule Card: Release Immediately vs Schedule for Date
                ReleaseScheduleCard(
                    releaseDate = uiState.releaseDate,
                    releaseDateMillis = uiState.releaseDateMillis,
                    onReleaseImmediatelyClick = onReleaseImmediatelySelected,
                    onPickScheduledDateClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )

                // Mandatory (Force Update) Toggle Card
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = if (uiState.isMandatory) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (uiState.isMandatory) {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMandatoryChange(!uiState.isMandatory) }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.isMandatory) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = if (uiState.isMandatory) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.dashboard_mandatory_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.dashboard_mandatory_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.isMandatory) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isMandatory,
                            onCheckedChange = onMandatoryChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("mandatory_update_switch")
                        )
                    }
                }

                // Primary Save Button
                Button(
                    onClick = onSaveToFirebaseClick,
                    enabled = !uiState.isSaving,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_to_firebase_button")
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.dashboard_saving),
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uiState.hasUnsavedChanges) {
                                "${stringResource(R.string.dashboard_save_button)} •"
                            } else {
                                stringResource(R.string.dashboard_save_button)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Reset Firebase URL Button
                OutlinedButton(
                    onClick = { showResetConfirmDialog = true },
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Material 3 Release Date Picker Dialog (converts selected date to local 00:00:00 start of day)
    if (showDatePicker) {
        val initialUtcDateMillis = if (uiState.releaseDateMillis > 0L) {
            localStartOfDayToUtcDatePickerMillis(uiState.releaseDateMillis)
        } else {
            null
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcDateMillis
        )
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
            title = {
                Text(
                    text = stringResource(R.string.dialog_reset_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
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
                    Text(
                        text = stringResource(R.string.dialog_confirm_reset),
                        color = MaterialTheme.colorScheme.error
                    )
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
            onDismissRequest = {
                if (!uiState.isFetchingGitHubRelease) {
                    showGitHubDialog = false
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = stringResource(R.string.dialog_github_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
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
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        singleLine = true,
                        isError = uiState.githubFetchError != null,
                        shape = MaterialTheme.shapes.small,
                        colors = dashboardTextFieldColors(),
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
                    onClick = {
                        onFetchGitHubRelease(githubRepoInput) {
                            showGitHubDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_github_fetch_button")
                ) {
                    if (uiState.isFetchingGitHubRelease) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.dialog_github_fetching))
                    } else {
                        Text(
                            text = stringResource(R.string.dialog_github_fetch_button),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isFetchingGitHubRelease,
                    onClick = { showGitHubDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Firebase Security Rules Reference Sheet
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
                Text(
                    text = stringResource(R.string.rules_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                        onRulesCopied(rulesCopiedMessage)
                        showRulesSheet = false
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.rules_copy_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun DownloadClicksTelemetryCard(
    downloadClicks: Long,
    modifier: Modifier = Modifier
) {
    val formattedClicks = NumberFormat.getIntegerInstance(Locale.US).format(downloadClicks)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
        modifier = modifier.testTag("total_download_clicks_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_total_clicks_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = formattedClicks,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/${FirebaseConstants.UPDATE_NODE_PATH}/${FirebaseConstants.FIELD_DOWNLOAD_CLICKS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.DownloadForOffline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReleaseScheduleCard(
    releaseDate: String,
    releaseDateMillis: Long,
    onReleaseImmediatelyClick: () -> Unit,
    onPickScheduledDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScheduled = releaseDateMillis > 0L
    val isFutureRelease = isScheduled && releaseDateMillis > System.currentTimeMillis()

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = 1.dp,
            color = if (isScheduled) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.dashboard_release_schedule_header),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isScheduled) {
                    val badgeColor = if (isFutureRelease) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                    Surface(
                        shape = CircleShape,
                        color = badgeColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = if (isFutureRelease) {
                                stringResource(R.string.dashboard_scheduled_future_badge)
                            } else {
                                stringResource(R.string.dashboard_scheduled_active_badge)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Segmented mode toggle: Release Immediately vs Schedule for Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScheduleModeOption(
                    label = stringResource(R.string.dashboard_release_immediately),
                    icon = Icons.Rounded.FlashOn,
                    selected = !isScheduled,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = onReleaseImmediatelyClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("release_immediately_option")
                )

                ScheduleModeOption(
                    label = stringResource(R.string.dashboard_schedule_for_date),
                    icon = Icons.Rounded.CalendarMonth,
                    selected = isScheduled,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onPickScheduledDateClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("schedule_for_date_option")
                )
            }

            if (isScheduled) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onPickScheduledDateClick)
                        .testTag("release_date_picker_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_release_date_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "${releaseDate.ifBlank { "Scheduled" }}  •  00:00:00",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "releaseDateMillis: $releaseDateMillis",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = stringResource(R.string.cd_pick_date),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FlashOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.dashboard_immediate_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleModeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            accentColor.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .height(48.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Converts a Material 3 DatePicker UTC midnight timestamp into the exact local start-of-day
 * (00:00:00.000) timestamp in milliseconds for that selected calendar date.
 */
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

/**
 * Converts a stored local start-of-day timestamp back to UTC midnight so Material 3 DatePicker
 * highlights the exact matching calendar date when reopening the picker.
 */
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

@Composable
private fun dashboardTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

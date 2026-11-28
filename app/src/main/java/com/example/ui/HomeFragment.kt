package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusOnlineGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class ActiveDateTarget {
    NONE,
    SCHEDULED_DATE,
    RELEASE_DATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFragment(
    profileName: String,
    avatarColorIndex: Int,
    form: HomeFormState,
    onUpdateForm: ((HomeFormState) -> HomeFormState) -> Unit,
    onFetchVersionClick: () -> Unit,
    onPushChangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeDateTarget by remember { mutableStateOf(ActiveDateTarget.NONE) }
    var showThemeSelectorDialog by remember { mutableStateOf(false) }

    val themePresets = listOf("Violet Glass", "Neon Cyber", "Dark Obsidian", "Emerald Pulse")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Profile & Status Card
        AdminElevatedCard(
            cornerRadius = 18.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RingedProfileAvatar(
                    avatarSize = 60.dp,
                    ringSize = 64.dp,
                    ringWidth = 3.dp,
                    avatarColorIndex = avatarColorIndex
                )
                Text(
                    text = profileName.ifBlank { "Guest" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(StatusOnlineGreen)
                )
                Text(
                    text = "Status :",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Online",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = StatusOnlineGreen
                )
                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .height(16.dp)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = "ADMIN PANEL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 2. Package & Animations Card
        AdminElevatedCard(
            cornerRadius = 18.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Package & Animations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = form.packageName,
                        onValueChange = { v -> onUpdateForm { it.copy(packageName = v) } },
                        label = { Text("Package Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = adminOutlinedFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onUpdateForm { it.copy(packageName = "com.example.app") } },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset package name",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HomeSwitchRow(
                    label = "Enable UI Animations",
                    checked = form.enableUiAnimations,
                    onCheckedChange = { c -> onUpdateForm { it.copy(enableUiAnimations = c) } }
                )
                HomeSwitchRow(
                    label = "Enable Themes",
                    checked = form.enableThemes,
                    onCheckedChange = { c -> onUpdateForm { it.copy(enableThemes = c) } }
                )
                HomeSwitchRow(
                    label = "Enable RGB Border",
                    checked = form.enableRgbBorder,
                    onCheckedChange = { c -> onUpdateForm { it.copy(enableRgbBorder = c) } }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeSelectorDialog = true }
                ) {
                    OutlinedTextField(
                        value = form.selectedTheme,
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        label = { Text("Select Theme") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Select Theme"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = adminOutlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Scheduled & Version Card
        AdminElevatedCard(
            cornerRadius = 18.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Scheduled & Version",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeDateTarget = ActiveDateTarget.SCHEDULED_DATE }
                ) {
                    OutlinedTextField(
                        value = form.scheduledDate,
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        label = { Text("Scheduled Date") },
                        trailingIcon = {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick Scheduled Date")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = adminOutlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeDateTarget = ActiveDateTarget.RELEASE_DATE }
                ) {
                    OutlinedTextField(
                        value = form.releaseDate,
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        label = { Text("Release Date") },
                        trailingIcon = {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick Release Date")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = adminOutlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = form.appVersion,
                    onValueChange = { v -> onUpdateForm { it.copy(appVersion = v) } },
                    label = { Text("App Version") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (form.isFetchingVersion) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Button(
                        onClick = onFetchVersionClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = "Fetch Current Version",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 4. Dialog Info Card
        AdminElevatedCard(
            cornerRadius = 18.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Dialog Info",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = form.dialogTitle,
                    onValueChange = { v -> onUpdateForm { it.copy(dialogTitle = v) } },
                    label = { Text("Dialog Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.updateButtonLink,
                    onValueChange = { v -> onUpdateForm { it.copy(updateButtonLink = v) } },
                    label = { Text("Update Button Link") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.updateButtonText,
                    onValueChange = { v -> onUpdateForm { it.copy(updateButtonText = v) } },
                    label = { Text("Update Button Text") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.logoUrl,
                    onValueChange = { v -> onUpdateForm { it.copy(logoUrl = v) } },
                    label = { Text("Logo URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 5. Social Links & Message Card
        AdminElevatedCard(
            cornerRadius = 18.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Social Links & Message",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = form.socialLink1,
                    onValueChange = { v -> onUpdateForm { it.copy(socialLink1 = v) } },
                    label = { Text("Social Link 1") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.socialLink2,
                    onValueChange = { v -> onUpdateForm { it.copy(socialLink2 = v) } },
                    label = { Text("Social Link 2") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.socialLink3,
                    onValueChange = { v -> onUpdateForm { it.copy(socialLink3 = v) } },
                    label = { Text("Social Link 3") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.mainMessage,
                    onValueChange = { v -> onUpdateForm { it.copy(mainMessage = v) } },
                    label = { Text("Main Message") },
                    minLines = 4,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.detailMessage,
                    onValueChange = { v -> onUpdateForm { it.copy(detailMessage = v) } },
                    label = { Text("Detail Message") },
                    minLines = 5,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 6. Add Notifications Card
        AdminElevatedCard(
            cornerRadius = 18.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HomeSwitchRow(
                    label = "Show Notification",
                    checked = form.showNotification,
                    onCheckedChange = { c -> onUpdateForm { it.copy(showNotification = c) } }
                )

                OutlinedTextField(
                    value = form.notificationTitle,
                    onValueChange = { v -> onUpdateForm { it.copy(notificationTitle = v) } },
                    label = { Text("Notification Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.notificationMessage,
                    onValueChange = { v -> onUpdateForm { it.copy(notificationMessage = v) } },
                    label = { Text("Notification Message") },
                    minLines = 3,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 7. Active Dialog & Update Card
        AdminElevatedCard(
            cornerRadius = 18.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Active Dialog & Update",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HomeSwitchRow(
                    label = "Active Dialog",
                    checked = form.activeDialog,
                    onCheckedChange = { c -> onUpdateForm { it.copy(activeDialog = c) } }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (form.isPushingChange) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Button(
                        onClick = onPushChangeClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("push_change_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Push Change", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showThemeSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showThemeSelectorDialog = false },
            title = { Text("Select Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themePresets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateForm { it.copy(selectedTheme = preset) }
                                    showThemeSelectorDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = form.selectedTheme == preset,
                                onClick = {
                                    onUpdateForm { it.copy(selectedTheme = preset) }
                                    showThemeSelectorDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(preset, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeSelectorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (activeDateTarget != ActiveDateTarget.NONE) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { activeDateTarget = ActiveDateTarget.NONE },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val formatted = fmt.format(Date(millis))
                            when (activeDateTarget) {
                                ActiveDateTarget.SCHEDULED_DATE ->
                                    onUpdateForm { it.copy(scheduledDate = formatted) }
                                ActiveDateTarget.RELEASE_DATE ->
                                    onUpdateForm { it.copy(releaseDate = formatted) }
                                ActiveDateTarget.NONE -> Unit
                            }
                        }
                        activeDateTarget = ActiveDateTarget.NONE
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDateTarget = ActiveDateTarget.NONE }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun HomeSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

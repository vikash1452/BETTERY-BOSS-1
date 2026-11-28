package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloRedLight

@Composable
fun SettingsFragment(
    profileName: String,
    avatarColorIndex: Int,
    themeMode: AppThemeMode,
    onEditProfileName: (String) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onLogoutClick: () -> Unit,
    onUiNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Account Card
        AdminElevatedCard(
            cornerRadius = 16.dp,
            elevation = 4.dp,
            contentPadding = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
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

                IconButton(
                    onClick = { showEditNameDialog = true },
                    modifier = Modifier.testTag("settings_edit_name_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit username",
                        tint = HoloBlueLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogoutClick)
                    .padding(vertical = 8.dp)
                    .testTag("settings_logout_row"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Logout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = HoloRedLight
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Logout",
                    tint = HoloRedLight,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 2. Appearance Card
        AdminElevatedCard(
            cornerRadius = 16.dp,
            elevation = 4.dp,
            contentPadding = 16.dp
        ) {
            Text(
                text = "Appearance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDropdown = true }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = themeMode.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showThemeDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("theme_dropdown_trigger")
                    ) {
                        Text(
                            text = themeMode.label,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Choose Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showThemeDropdown,
                        onDismissRequest = { showThemeDropdown = false }
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                onClick = {
                                    onThemeModeSelected(mode)
                                    showThemeDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Credits & Contact Card
        AdminElevatedCard(
            cornerRadius = 16.dp,
            elevation = 4.dp,
            contentPadding = 16.dp
        ) {
            Text(
                text = "Credits & Contact",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            SettingsDetailRow(
                title = "Developer :",
                value = "RIDH",
                onClick = { onUiNotice("Developer: RIDH") }
            )
            SettingsExternalRow(
                title = "Telegram Contact",
                subtitle = "@ridh007",
                onClick = { onUiNotice("Telegram Contact: @ridh007") }
            )
            SettingsExternalRow(
                title = "Telegram Channel",
                subtitle = "Join our community",
                onClick = { onUiNotice("Opening Telegram Channel") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsExternalRow(
                title = "Dialog Design By : ZENX",
                subtitle = "@ZeNx_10",
                onClick = { onUiNotice("Dialog Design By: ZENX (@ZeNx_10)") }
            )
        }

        // 4. About Card
        AdminElevatedCard(
            cornerRadius = 16.dp,
            elevation = 4.dp,
            contentPadding = 16.dp
        ) {
            Text(
                text = "About",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            SettingsDetailRow(
                title = "Version",
                value = "1.0",
                onClick = {}
            )
            SettingsExternalRow(
                title = "Report Bugs",
                subtitle = "Found an issue? Let us know!",
                onClick = { onUiNotice("Report Bugs tapped") }
            )
            SettingsExternalRow(
                title = "Share App",
                subtitle = "Send app to your friends",
                onClick = { onUiNotice("Share App tapped") }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUiNotice("You are using Version 1.0") }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Check for Updates",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showEditNameDialog) {
        var nameDraft by remember(profileName) { mutableStateOf(profileName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Profile Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { if (it.length <= 26) nameDraft = it },
                    label = { Text("Enter your name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEditProfileName(nameDraft.trim().ifBlank { "Guest" })
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsDetailRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsExternalRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

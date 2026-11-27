package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DialogButtonConfig
import com.example.data.DialogOptions
import com.example.data.DialogUiConfig
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrimaryAndDismissButtonsCard(
    uiConfig: DialogUiConfig,
    onUpdateUi: ((DialogUiConfig) -> DialogUiConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard(
            title = "Primary Action Button",
            icon = Icons.Rounded.SmartButton,
            accentColor = MaterialTheme.colorScheme.primary
        ) {
            LabeledSwitchRow(
                title = "Show Primary Button",
                subtitle = "Main call-to-action button inside dialog",
                checked = uiConfig.showPrimaryButton,
                onCheckedChange = { checked -> onUpdateUi { it.copy(showPrimaryButton = checked) } }
            )

            AnimatedVisibility(visible = uiConfig.showPrimaryButton) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiConfig.primaryButtonText,
                        onValueChange = { v -> onUpdateUi { it.copy(primaryButtonText = v) } },
                        label = { Text("primary_button_text") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OptionChipsSelector(
                        label = "primary_button_action",
                        options = DialogOptions.BUTTON_ACTIONS,
                        selected = uiConfig.primaryButtonAction,
                        onSelect = { act -> onUpdateUi { it.copy(primaryButtonAction = act) } }
                    )

                    if (uiConfig.primaryButtonAction == DialogOptions.ACTION_OPEN_TAB) {
                        OptionChipsSelector(
                            label = "Quick Select Tab Key",
                            options = DialogOptions.TAB_KEYS,
                            selected = uiConfig.primaryButtonUrl,
                            onSelect = { key -> onUpdateUi { it.copy(primaryButtonUrl = key) } },
                            accentColor = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    if (uiConfig.primaryButtonAction != DialogOptions.ACTION_DISMISS) {
                        OutlinedTextField(
                            value = uiConfig.primaryButtonUrl,
                            onValueChange = { v -> onUpdateUi { it.copy(primaryButtonUrl = v) } },
                            label = {
                                Text(
                                    when (uiConfig.primaryButtonAction) {
                                        DialogOptions.ACTION_COPY -> "Text to Copy (primary_button_url)"
                                        DialogOptions.ACTION_OPEN_TAB -> "Tab Key (primary_button_url)"
                                        DialogOptions.ACTION_SHARE -> "Share URL / Fallback (primary_button_url)"
                                        else -> "Target URL (primary_button_url)"
                                    }
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            singleLine = true,
                            shape = MaterialTheme.shapes.small,
                            colors = adminTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    IconPickerField(
                        label = "primary_button_icon",
                        selectedIcon = uiConfig.primaryButtonIcon,
                        onIconSelected = { icon -> onUpdateUi { it.copy(primaryButtonIcon = icon) } }
                    )

                    ColorInputField(
                        label = "primary_button_color",
                        hexValue = uiConfig.primaryButtonColor,
                        onHexChange = { hex -> onUpdateUi { it.copy(primaryButtonColor = hex) } }
                    )

                    ColorInputField(
                        label = "primary_button_gradient_end",
                        hexValue = uiConfig.primaryButtonGradientEnd,
                        allowEmpty = true,
                        onHexChange = { hex -> onUpdateUi { it.copy(primaryButtonGradientEnd = hex) } }
                    )

                    ColorInputField(
                        label = "primary_button_text_color",
                        hexValue = uiConfig.primaryButtonTextColor,
                        onHexChange = { hex -> onUpdateUi { it.copy(primaryButtonTextColor = hex) } }
                    )

                    LabeledSwitchRow(
                        title = "Primary Button Pulse (primary_button_pulse)",
                        subtitle = "Subtle glowing scale pulse to draw user attention",
                        checked = uiConfig.primaryButtonPulse,
                        onCheckedChange = { p -> onUpdateUi { it.copy(primaryButtonPulse = p) } },
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        SectionCard(
            title = "Dismiss / Not Now Button",
            icon = Icons.Rounded.TouchApp,
            accentColor = MaterialTheme.colorScheme.secondary
        ) {
            LabeledSwitchRow(
                title = "Show Dismiss Button (show_not_now)",
                subtitle = "Hidden automatically when is_mandatory is ON",
                checked = uiConfig.showNotNow,
                onCheckedChange = { s -> onUpdateUi { it.copy(showNotNow = s) } },
                accentColor = MaterialTheme.colorScheme.secondary
            )

            AnimatedVisibility(visible = uiConfig.showNotNow) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiConfig.notNowText,
                        onValueChange = { v -> onUpdateUi { it.copy(notNowText = v) } },
                        label = { Text("not_now_text") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    ColorInputField(
                        label = "not_now_color",
                        hexValue = uiConfig.notNowColor,
                        onHexChange = { hex -> onUpdateUi { it.copy(notNowColor = hex) } }
                    )
                }
            }
        }
    }
}

@Composable
fun ExtraButtonsBuilderCard(
    buttons: List<DialogButtonConfig>,
    buttonClicks: Map<String, Long>,
    onUpsertButton: (DialogButtonConfig, Int?) -> Unit,
    onDeleteButton: (Int) -> Unit,
    onMoveButton: (Int, Int) -> Unit
) {
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }

    SectionCard(
        title = "Dynamic Extra Buttons (${buttons.size})",
        subtitle = "Add Telegram, WhatsApp, Share, Copy or Tab navigation buttons (same row_group renders side-by-side)",
        icon = Icons.Rounded.ViewAgenda,
        accentColor = MaterialTheme.colorScheme.tertiary
    ) {
        if (buttons.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No extra buttons configured yet. Tap below to add buttons to ui_config.buttons.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                buttons.forEachIndexed { index, btn ->
                    val clicks = buttonClicks[btn.id] ?: 0L
                    val btnColor = parseHexColor(btn.bgColor, MaterialTheme.colorScheme.primary)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = btnColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, btnColor.copy(alpha = 0.5f)),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = resolveDialogIcon(btn.icon),
                                        contentDescription = null,
                                        tint = btnColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = btn.text.ifBlank { "(Untitled)" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Text(
                                            text = btn.style,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = buildString {
                                        append("id:${btn.id} • ${btn.action}")
                                        if (btn.rowGroup.isNotBlank()) append(" • row:${btn.rowGroup}")
                                        append(" • $clicks clicks")
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onMoveButton(index, -1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowUpward,
                                        contentDescription = "Move Up",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onMoveButton(index, 1) },
                                    enabled = index < buttons.lastIndex,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDownward,
                                        contentDescription = "Move Down",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        editingIndex = index
                                        showEditorDialog = true
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit Button",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteButton(index) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Delete Button",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                editingIndex = null
                showEditorDialog = true
            },
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Extra Button", style = MaterialTheme.typography.labelLarge)
        }
    }

    if (showEditorDialog) {
        val initialBtn = editingIndex?.let { buttons.getOrNull(it) } ?: DialogButtonConfig(
            id = "btn_${buttons.size + 1}",
            text = "Join Telegram",
            action = DialogOptions.ACTION_OPEN_URL,
            url = "https://t.me/",
            style = DialogOptions.STYLE_FILLED,
            bgColor = "#229ED9",
            textColor = "#FFFFFF",
            icon = "telegram",
            cornerRadiusDp = 14
        )
        ExtraButtonEditorDialog(
            initialButton = initialBtn,
            isEditing = editingIndex != null,
            onSave = { updated ->
                onUpsertButton(updated, editingIndex)
                showEditorDialog = false
            },
            onDismiss = { showEditorDialog = false }
        )
    }
}

@Composable
private fun ExtraButtonEditorDialog(
    initialButton: DialogButtonConfig,
    isEditing: Boolean,
    onSave: (DialogButtonConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var id by remember { mutableStateOf(initialButton.id) }
    var text by remember { mutableStateOf(initialButton.text) }
    var action by remember { mutableStateOf(initialButton.action) }
    var url by remember { mutableStateOf(initialButton.url) }
    var shareText by remember { mutableStateOf(initialButton.shareText) }
    var style by remember { mutableStateOf(initialButton.style) }
    var bgColor by remember { mutableStateOf(initialButton.bgColor) }
    var gradientEnd by remember { mutableStateOf(initialButton.gradientEnd) }
    var textColor by remember { mutableStateOf(initialButton.textColor) }
    var icon by remember { mutableStateOf(initialButton.icon) }
    var cornerRadius by remember { mutableIntStateOf(initialButton.cornerRadiusDp) }
    var rowGroup by remember { mutableStateOf(initialButton.rowGroup) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = if (isEditing) "Edit Extra Button" else "Add Extra Button",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("id (analytics click key, e.g. tg_btn)") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("text (Button Label)") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OptionChipsSelector(
                    label = "action",
                    options = DialogOptions.BUTTON_ACTIONS,
                    selected = action,
                    onSelect = { action = it }
                )

                if (action == DialogOptions.ACTION_OPEN_TAB) {
                    OptionChipsSelector(
                        label = "Select Tab Key",
                        options = DialogOptions.TAB_KEYS,
                        selected = url,
                        onSelect = { url = it },
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }

                if (action != DialogOptions.ACTION_DISMISS) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = {
                            Text(
                                when (action) {
                                    DialogOptions.ACTION_COPY -> "url (text to copy)"
                                    DialogOptions.ACTION_OPEN_TAB -> "url (tab key)"
                                    else -> "url"
                                }
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (action == DialogOptions.ACTION_SHARE) {
                    OutlinedTextField(
                        value = shareText,
                        onValueChange = { shareText = it },
                        label = { Text("share_text") },
                        minLines = 2,
                        maxLines = 4,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OptionChipsSelector(
                    label = "style",
                    options = DialogOptions.BUTTON_STYLES,
                    selected = style,
                    onSelect = { style = it },
                    accentColor = MaterialTheme.colorScheme.secondary
                )

                IconPickerField(
                    label = "icon",
                    selectedIcon = icon,
                    onIconSelected = { icon = it }
                )

                ColorInputField(
                    label = "bg_color",
                    hexValue = bgColor,
                    onHexChange = { bgColor = it }
                )

                ColorInputField(
                    label = "gradient_end (for gradient style)",
                    hexValue = gradientEnd,
                    allowEmpty = true,
                    onHexChange = { gradientEnd = it }
                )

                ColorInputField(
                    label = "text_color",
                    hexValue = textColor,
                    onHexChange = { textColor = it }
                )

                LabeledIntSlider(
                    label = "corner_radius_dp",
                    value = cornerRadius,
                    valueRange = 4..50,
                    onValueChange = { cornerRadius = it }
                )

                OutlinedTextField(
                    value = rowGroup,
                    onValueChange = { rowGroup = it },
                    label = { Text("row_group (optional, e.g. social)") },
                    placeholder = { Text("Same row_group sits side-by-side") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = id.isNotBlank() && text.isNotBlank(),
                onClick = {
                    onSave(
                        DialogButtonConfig(
                            id = id.trim(),
                            text = text.trim(),
                            action = action,
                            url = url.trim(),
                            shareText = shareText.trim(),
                            style = style,
                            bgColor = bgColor.trim().ifBlank { "#00E676" },
                            gradientEnd = gradientEnd.trim(),
                            textColor = textColor.trim().ifBlank { "#FFFFFF" },
                            icon = icon,
                            cornerRadiusDp = cornerRadius,
                            rowGroup = rowGroup.trim()
                        )
                    )
                }
            ) {
                Text("Save Button", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomBlockOrderingCard(
    isCustomOrderEnabled: Boolean,
    blocks: List<String>,
    onToggleCustomOrder: (Boolean) -> Unit,
    onAddBlock: (String) -> Unit,
    onRemoveBlock: (Int) -> Unit,
    onMoveBlock: (Int, Int) -> Unit
) {
    SectionCard(
        title = "Custom Block Order (ui_blocks)",
        subtitle = "Optional: When OFF, ui_blocks saves as [] for standard layout",
        icon = Icons.Rounded.Layers,
        accentColor = MaterialTheme.colorScheme.secondary
    ) {
        LabeledSwitchRow(
            title = "Enable Custom Block Order",
            subtitle = if (isCustomOrderEnabled) "${blocks.size} ordered blocks active" else "Standard layout active (ui_blocks = [])",
            checked = isCustomOrderEnabled,
            onCheckedChange = onToggleCustomOrder,
            accentColor = MaterialTheme.colorScheme.secondary
        )

        AnimatedVisibility(visible = isCustomOrderEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (blocks.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        blocks.forEachIndexed { index, block ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            text = block,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily)
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { onMoveBlock(index, -1) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Rounded.ArrowUpward, "Move up", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onMoveBlock(index, 1) },
                                            enabled = index < blocks.lastIndex,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Rounded.ArrowDownward, "Move down", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onRemoveBlock(index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                "Remove",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tap to append block:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DialogOptions.UI_BLOCK_TYPES.forEach { blockType ->
                            OutlinedButton(
                                onClick = { onAddBlock(blockType) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.height(34.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = blockType,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

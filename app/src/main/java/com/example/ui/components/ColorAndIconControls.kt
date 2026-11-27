package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DialogOptions
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.roundToInt

fun parseHexColor(hex: String?, fallback: Color): Color {
    val clean = hex?.trim().orEmpty()
    if (clean.isEmpty()) return fallback
    return try {
        val withHash = if (clean.startsWith("#")) clean else "#$clean"
        Color(android.graphics.Color.parseColor(withHash))
    } catch (_: IllegalArgumentException) {
        fallback
    }
}

fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

fun resolveDialogIcon(iconKey: String): ImageVector {
    return when (iconKey.lowercase().trim()) {
        "download" -> Icons.Rounded.Download
        "rocket" -> Icons.Rounded.RocketLaunch
        "play" -> Icons.Rounded.PlayArrow
        "video" -> Icons.Rounded.Videocam
        "image" -> Icons.Rounded.Image
        "telegram" -> Icons.AutoMirrored.Rounded.Send
        "whatsapp" -> Icons.Rounded.Chat
        "link" -> Icons.Rounded.Link
        "open" -> Icons.AutoMirrored.Rounded.OpenInNew
        "copy" -> Icons.Rounded.ContentCopy
        "share" -> Icons.Rounded.Share
        "star" -> Icons.Rounded.Star
        "gift" -> Icons.Rounded.CardGiftcard
        "info" -> Icons.Rounded.Info
        "bolt" -> Icons.Rounded.Bolt
        "settings" -> Icons.Rounded.Settings
        "close" -> Icons.Rounded.Close
        "check" -> Icons.Rounded.CheckCircle
        "sparkles" -> Icons.Rounded.AutoAwesome
        "warning" -> Icons.Rounded.WarningAmber
        "bell" -> Icons.Rounded.Notifications
        "campaign" -> Icons.Rounded.Campaign
        "phone" -> Icons.Rounded.PhoneAndroid
        else -> Icons.AutoMirrored.Rounded.OpenInNew
    }
}

@Composable
fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                        )
                    }
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionChipsSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    labelMapper: (String) -> String = { it }
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(option) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = labelMapper(option),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LabeledSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = if (checked) accentColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    }
}

@Composable
fun LabeledIntSlider(
    label: String,
    value: Int,
    valueRange: IntRange,
    unitSuffix: String = "dp",
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$value$unitSuffix",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(valueRange.first, valueRange.last)) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
fun LabeledFloatSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Slider(
            value = value.coerceIn(min, max),
            onValueChange = { onValueChange((it * 100f).roundToInt() / 100f) },
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
fun ColorInputField(
    label: String,
    hexValue: String,
    onHexChange: (String) -> Unit,
    allowEmpty: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showPickerDialog by remember { mutableStateOf(false) }
    val previewColor = parseHexColor(hexValue, Color.Transparent)
    val isEmptyOrTransparent = hexValue.isBlank()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isEmptyOrTransparent) MaterialTheme.colorScheme.surfaceContainerHigh else previewColor,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { showPickerDialog = true }
        ) {
            if (isEmptyOrTransparent) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = "Pick color for $label",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = hexValue,
            onValueChange = onHexChange,
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            placeholder = {
                Text(
                    text = if (allowEmpty) "Optional #RRGGBB" else "#RRGGBB",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily)
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = adminTextFieldColors(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "Open swatch picker",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showPickerDialog = true }
                )
            },
            modifier = Modifier.weight(1f)
        )
    }

    if (showPickerDialog) {
        ColorPickerDialog(
            title = label,
            initialHex = hexValue,
            allowEmpty = allowEmpty,
            onConfirm = { newHex ->
                onHexChange(newHex)
                showPickerDialog = false
            },
            onDismiss = { showPickerDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    title: String,
    initialHex: String,
    allowEmpty: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val startColor = parseHexColor(initialHex, Color(0xFF00E676))
    var red by remember { mutableFloatStateOf(startColor.red) }
    var green by remember { mutableFloatStateOf(startColor.green) }
    var blue by remember { mutableFloatStateOf(startColor.blue) }

    val liveColor = Color(red, green, blue, 1f)
    val liveHex = colorToHex(liveColor)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(liveColor)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    )
                    Column {
                        Text(
                            text = liveHex,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Tap a preset swatch or tune RGB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DialogOptions.PRESET_COLOR_SWATCHES.forEach { swatchHex ->
                        val c = parseHexColor(swatchHex, Color.Gray)
                        val selected = swatchHex.equals(liveHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable {
                                    red = c.red
                                    green = c.green
                                    blue = c.blue
                                }
                        )
                    }
                }

                Column {
                    Text(
                        text = "Red (${(red * 255).roundToInt()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B6B)
                    )
                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF6B6B), activeTrackColor = Color(0xFFFF6B6B))
                    )
                    Text(
                        text = "Green (${(green * 255).roundToInt()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E676)
                    )
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E676), activeTrackColor = Color(0xFF00E676))
                    )
                    Text(
                        text = "Blue (${(blue * 255).roundToInt()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF38BDF8)
                    )
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8))
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(liveHex) }) {
                Text("Apply", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            Row {
                if (allowEmpty) {
                    TextButton(onClick = { onConfirm("") }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun IconPickerField(
    label: String,
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showIconDialog by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable { showIconDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = resolveDialogIcon(selectedIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedIcon.ifBlank { "none" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Text(
                text = "Change",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }

    if (showIconDialog) {
        AlertDialog(
            onDismissRequest = { showIconDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text(label, style = MaterialTheme.typography.titleMedium) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(DialogOptions.BUTTON_ICONS) { iconKey ->
                        val isSelected = iconKey == selectedIcon
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onIconSelected(iconKey)
                                    showIconDialog = false
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = resolveDialogIcon(iconKey),
                                    contentDescription = iconKey,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = iconKey,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

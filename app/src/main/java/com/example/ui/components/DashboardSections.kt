package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.GifBox
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DialogOptions
import com.example.data.DialogUiConfig
import com.example.data.FirebaseConstants
import com.example.data.UpdateConfig
import com.example.ui.AdminUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeAndContentTab(
    uiState: AdminUiState,
    onUpdateConfig: ((UpdateConfig) -> UpdateConfig) -> Unit,
    onUpdateUi: ((DialogUiConfig) -> DialogUiConfig) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReleaseImmediatelyClick: () -> Unit,
    onPickScheduledDateClick: () -> Unit,
    onOpenGitHubDialog: () -> Unit,
    onNotify: (String) -> Unit
) {
    val config = uiState.config
    val ui = config.uiConfig
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Read-Only Analytics Card (download_clicks & button_clicks)
        AnalyticsTelemetryCard(
            downloadClicks = config.downloadClicks,
            buttonClicks = config.buttonClicks
        )

        // Master Broadcast Switch
        Surface(
            shape = MaterialTheme.shapes.large,
            color = if (config.isEnabled) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (config.isEnabled) {
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
                    .clickable { onUpdateConfig { it.copy(isEnabled = !it.isEnabled) } }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
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
                        color = if (config.isEnabled) {
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
                                tint = if (config.isEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Dialog Broadcast (is_enabled)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (config.isEnabled) {
                                "ON — Active on Battery Boss devices"
                            } else {
                                "OFF — Dialog hidden for all users"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (config.isEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = { enabled -> onUpdateConfig { it.copy(isEnabled = enabled) } },
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

        // Dialog Mode & Behavior Controls
        SectionCard(
            title = "Dialog Mode & Behavior",
            icon = Icons.Rounded.NewReleases,
            accentColor = MaterialTheme.colorScheme.primary
        ) {
            OptionChipsSelector(
                label = "dialog_mode",
                options = DialogOptions.DIALOG_MODES,
                selected = config.dialogMode,
                onSelect = { mode -> onUpdateConfig { it.copy(dialogMode = mode) } },
                labelMapper = { mode ->
                    if (mode == DialogOptions.MODE_UPDATE) {
                        "update (App Update Lock)"
                    } else {
                        "announcement (Promo / Notice)"
                    }
                }
            )

            OutlinedTextField(
                value = config.dialogId,
                onValueChange = { v -> onUpdateConfig { it.copy(dialogId = v) } },
                label = { Text("dialog_id (e.g. promo_01)") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            LabeledSwitchRow(
                title = "Show Once (show_once)",
                subtitle = "Don't show again after user dismisses until dialog_id changes",
                checked = config.showOnce,
                onCheckedChange = { v -> onUpdateConfig { it.copy(showOnce = v) } },
                accentColor = MaterialTheme.colorScheme.tertiary
            )

            LabeledSwitchRow(
                title = "Mandatory / Unclosable (is_mandatory)",
                subtitle = "User cannot dismiss or close the dialog when ON",
                checked = config.isMandatory,
                onCheckedChange = { v -> onUpdateConfig { it.copy(isMandatory = v) } },
                accentColor = MaterialTheme.colorScheme.secondary
            )
        }

        // Release Version & APK URL Card
        SectionCard(
            title = "Release Version & APK Link",
            icon = Icons.Rounded.Tag,
            accentColor = MaterialTheme.colorScheme.tertiary
        ) {
            OutlinedTextField(
                value = config.version,
                onValueChange = { v -> onUpdateConfig { it.copy(version = v) } },
                label = { Text(stringResource(R.string.dashboard_version_label)) },
                placeholder = { Text(stringResource(R.string.dashboard_version_placeholder)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                leadingIcon = {
                    Icon(Icons.Rounded.Tag, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("version_input")
            )

            OutlinedTextField(
                value = config.apkUrl,
                onValueChange = { v -> onUpdateConfig { it.copy(apkUrl = v) } },
                label = { Text(stringResource(R.string.dashboard_apk_url_label)) },
                placeholder = { Text(stringResource(R.string.dashboard_apk_url_placeholder)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                leadingIcon = {
                    Icon(Icons.Rounded.Link, null, tint = MaterialTheme.colorScheme.tertiary)
                },
                trailingIcon = {
                    val trimmedUrl = config.apkUrl.trim()
                    if (trimmedUrl.startsWith("https://", true) || trimmedUrl.startsWith("http://", true)) {
                        IconButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trimmedUrl)))
                                }.onFailure { onNotify("Could not open APK URL") }
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
                colors = adminTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apk_url_input")
            )

            OutlinedButton(
                onClick = onOpenGitHubDialog,
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

        // Release Schedule Card
        ReleaseScheduleCard(
            releaseDate = config.releaseDate,
            releaseDateMillis = config.releaseDateMillis,
            onReleaseImmediatelyClick = onReleaseImmediatelyClick,
            onPickScheduledDateClick = onPickScheduledDateClick
        )

        // Dialog Text, Badge & Highlight Box
        SectionCard(
            title = "Texts, Badge & Highlight Box",
            icon = Icons.Rounded.TextFields,
            accentColor = MaterialTheme.colorScheme.primary
        ) {
            OutlinedTextField(
                value = ui.badgeText,
                onValueChange = { v -> onUpdateUi { it.copy(badgeText = v) } },
                label = { Text("badge_text (e.g. NEW, UPDATE)") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.title,
                onValueChange = onTitleChange,
                label = { Text("title") },
                placeholder = { Text(stringResource(R.string.dashboard_title_placeholder)) },
                leadingIcon = { Icon(Icons.Rounded.Title, null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input")
            )

            OutlinedTextField(
                value = ui.subtitle,
                onValueChange = { v -> onUpdateUi { it.copy(subtitle = v) } },
                label = { Text("subtitle") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.description,
                onValueChange = onDescriptionChange,
                label = { Text("description (multi-line bullets)") },
                placeholder = { Text(stringResource(R.string.dashboard_description_placeholder)) },
                leadingIcon = { Icon(Icons.Rounded.Notes, null, tint = MaterialTheme.colorScheme.primary) },
                minLines = 4,
                maxLines = 8,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input")
            )

            OutlinedTextField(
                value = ui.highlightTitle,
                onValueChange = { v -> onUpdateUi { it.copy(highlightTitle = v) } },
                label = { Text("highlight_title") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.highlightText,
                onValueChange = { v -> onUpdateUi { it.copy(highlightText = v) } },
                label = { Text("highlight_text") },
                minLines = 2,
                maxLines = 4,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.footerText,
                onValueChange = { v -> onUpdateUi { it.copy(footerText = v) } },
                label = { Text("footer_text") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LayoutAndMediaTab(
    uiConfig: DialogUiConfig,
    onUpdateUi: ((DialogUiConfig) -> DialogUiConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard(
            title = "Layout & Entrance Animation",
            icon = Icons.Rounded.ViewQuilt,
            accentColor = MaterialTheme.colorScheme.primary
        ) {
            OptionChipsSelector(
                label = "layout_style",
                options = DialogOptions.LAYOUT_STYLES,
                selected = uiConfig.layoutStyle,
                onSelect = { style -> onUpdateUi { it.copy(layoutStyle = style) } }
            )

            OptionChipsSelector(
                label = "entrance_animation",
                options = DialogOptions.ENTRANCE_ANIMATIONS,
                selected = uiConfig.entranceAnimation,
                onSelect = { anim -> onUpdateUi { it.copy(entranceAnimation = anim) } },
                accentColor = MaterialTheme.colorScheme.tertiary
            )

            OptionChipsSelector(
                label = "title_align",
                options = DialogOptions.TITLE_ALIGNS,
                selected = uiConfig.titleAlign,
                onSelect = { align -> onUpdateUi { it.copy(titleAlign = align) } }
            )
        }

        SectionCard(
            title = "Built-In Animations & FX",
            icon = Icons.Rounded.Animation,
            accentColor = MaterialTheme.colorScheme.tertiary
        ) {
            OptionChipsSelector(
                label = "animation_type",
                options = DialogOptions.ANIMATION_TYPES,
                selected = uiConfig.animationType,
                onSelect = { anim -> onUpdateUi { it.copy(animationType = anim) } },
                accentColor = MaterialTheme.colorScheme.tertiary
            )

            LabeledSwitchRow(
                title = "Show Header Wave (show_header_wave)",
                checked = uiConfig.showHeaderWave,
                onCheckedChange = { v -> onUpdateUi { it.copy(showHeaderWave = v) } }
            )

            LabeledSwitchRow(
                title = "Show Sparkles (show_sparkles)",
                checked = uiConfig.showSparkles,
                onCheckedChange = { v -> onUpdateUi { it.copy(showSparkles = v) } },
                accentColor = MaterialTheme.colorScheme.tertiary
            )

            LabeledSwitchRow(
                title = "Show Confetti (show_confetti)",
                checked = uiConfig.showConfetti,
                onCheckedChange = { v -> onUpdateUi { it.copy(showConfetti = v) } },
                accentColor = MaterialTheme.colorScheme.secondary
            )
        }

        SectionCard(
            title = "Photo, GIF & Video Media Controls",
            icon = Icons.Rounded.PermMedia,
            accentColor = MaterialTheme.colorScheme.secondary
        ) {
            OptionChipsSelector(
                label = "media_type",
                options = DialogOptions.MEDIA_TYPES,
                selected = uiConfig.mediaType,
                onSelect = { type -> onUpdateUi { it.copy(mediaType = type) } },
                accentColor = MaterialTheme.colorScheme.secondary
            )

            OutlinedTextField(
                value = uiConfig.mediaUrl,
                onValueChange = { v -> onUpdateUi { it.copy(mediaUrl = v) } },
                label = { Text("media_url (Primary Media URL)") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                leadingIcon = { Icon(Icons.Rounded.Link, null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiConfig.imageUrl,
                onValueChange = { v ->
                    onUpdateUi {
                        it.copy(
                            imageUrl = v,
                            mediaUrl = if (it.mediaType == "image" || it.mediaUrl.isBlank()) v else it.mediaUrl
                        )
                    }
                },
                label = { Text("image_url (Photo Link)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                leadingIcon = { Icon(Icons.Rounded.Image, null, tint = MaterialTheme.colorScheme.tertiary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiConfig.gifUrl,
                onValueChange = { v ->
                    onUpdateUi {
                        it.copy(
                            gifUrl = v,
                            mediaUrl = if (it.mediaType == "gif") v else it.mediaUrl
                        )
                    }
                },
                label = { Text("gif_url (Animated GIF Link)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                leadingIcon = { Icon(Icons.Rounded.GifBox, null, tint = MaterialTheme.colorScheme.secondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiConfig.videoUrl,
                onValueChange = { v ->
                    onUpdateUi {
                        it.copy(
                            videoUrl = v,
                            mediaUrl = if (it.mediaType == "video") v else it.mediaUrl
                        )
                    }
                },
                label = { Text("video_url (MP4 / M3U8 Video Link)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                leadingIcon = { Icon(Icons.Rounded.VideoLibrary, null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiConfig.thumbnailUrl,
                onValueChange = { v -> onUpdateUi { it.copy(thumbnailUrl = v) } },
                label = { Text("thumbnail_url (Video Poster Image)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                leadingIcon = { Icon(Icons.Rounded.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            LabeledIntSlider(
                label = "media_height_dp",
                value = uiConfig.mediaHeightDp,
                valueRange = 90..460,
                onValueChange = { h -> onUpdateUi { it.copy(mediaHeightDp = h) } }
            )

            LabeledIntSlider(
                label = "media_corner_radius_dp",
                value = uiConfig.mediaCornerRadiusDp,
                valueRange = 0..36,
                onValueChange = { r -> onUpdateUi { it.copy(mediaCornerRadiusDp = r) } }
            )

            OptionChipsSelector(
                label = "media_content_scale",
                options = DialogOptions.MEDIA_CONTENT_SCALES,
                selected = uiConfig.mediaContentScale,
                onSelect = { scale -> onUpdateUi { it.copy(mediaContentScale = scale) } }
            )

            LabeledSwitchRow(
                title = "video_autoplay",
                checked = uiConfig.videoAutoplay,
                onCheckedChange = { v -> onUpdateUi { it.copy(videoAutoplay = v) } }
            )
            LabeledSwitchRow(
                title = "video_loop",
                checked = uiConfig.videoLoop,
                onCheckedChange = { v -> onUpdateUi { it.copy(videoLoop = v) } }
            )
            LabeledSwitchRow(
                title = "video_muted",
                checked = uiConfig.videoMuted,
                onCheckedChange = { v -> onUpdateUi { it.copy(videoMuted = v) } }
            )
            LabeledSwitchRow(
                title = "video_show_controls",
                checked = uiConfig.videoShowControls,
                onCheckedChange = { v -> onUpdateUi { it.copy(videoShowControls = v) } }
            )
        }
    }
}

@Composable
fun ColorsAndThemeTab(
    uiConfig: DialogUiConfig,
    onUpdateUi: ((DialogUiConfig) -> DialogUiConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard(
            title = "Backdrop & Card Geometry",
            icon = Icons.Rounded.Wallpaper,
            accentColor = MaterialTheme.colorScheme.tertiary
        ) {
            ColorInputField(
                label = "backdrop_color",
                hexValue = uiConfig.backdropColor,
                onHexChange = { hex -> onUpdateUi { it.copy(backdropColor = hex) } }
            )
            ColorInputField(
                label = "backdrop_gradient_end",
                hexValue = uiConfig.backdropGradientEnd,
                allowEmpty = true,
                onHexChange = { hex -> onUpdateUi { it.copy(backdropGradientEnd = hex) } }
            )
            OutlinedTextField(
                value = uiConfig.backdropImageUrl,
                onValueChange = { url -> onUpdateUi { it.copy(backdropImageUrl = url) } },
                label = { Text("backdrop_image_url") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            LabeledFloatSlider(
                label = "backdrop_dim_alpha",
                value = uiConfig.backdropDimAlpha,
                min = 0.0f,
                max = 0.92f,
                onValueChange = { a -> onUpdateUi { it.copy(backdropDimAlpha = a) } }
            )
            ColorInputField(
                label = "card_color",
                hexValue = uiConfig.cardColor,
                onHexChange = { hex -> onUpdateUi { it.copy(cardColor = hex) } }
            )
            ColorInputField(
                label = "card_gradient_end",
                hexValue = uiConfig.cardGradientEnd,
                allowEmpty = true,
                onHexChange = { hex -> onUpdateUi { it.copy(cardGradientEnd = hex) } }
            )
            ColorInputField(
                label = "card_border_color",
                hexValue = uiConfig.cardBorderColor,
                onHexChange = { hex -> onUpdateUi { it.copy(cardBorderColor = hex) } }
            )
            LabeledIntSlider(
                label = "card_corner_radius",
                value = uiConfig.cardCornerRadius,
                valueRange = 0..44,
                onValueChange = { r -> onUpdateUi { it.copy(cardCornerRadius = r) } }
            )
        }

        SectionCard(
            title = "Header, Typography & Accent Palette",
            icon = Icons.Rounded.ColorLens,
            accentColor = MaterialTheme.colorScheme.primary
        ) {
            ColorInputField(
                label = "header_color",
                hexValue = uiConfig.headerColor,
                onHexChange = { hex -> onUpdateUi { it.copy(headerColor = hex) } }
            )
            ColorInputField(
                label = "header_gradient_end",
                hexValue = uiConfig.headerGradientEnd,
                allowEmpty = true,
                onHexChange = { hex -> onUpdateUi { it.copy(headerGradientEnd = hex) } }
            )
            ColorInputField(
                label = "accent_color",
                hexValue = uiConfig.accentColor,
                onHexChange = { hex -> onUpdateUi { it.copy(accentColor = hex) } }
            )
            ColorInputField(
                label = "title_color",
                hexValue = uiConfig.titleColor,
                onHexChange = { hex -> onUpdateUi { it.copy(titleColor = hex) } }
            )
            ColorInputField(
                label = "subtitle_color",
                hexValue = uiConfig.subtitleColor,
                onHexChange = { hex -> onUpdateUi { it.copy(subtitleColor = hex) } }
            )
            ColorInputField(
                label = "description_color",
                hexValue = uiConfig.descriptionColor,
                onHexChange = { hex -> onUpdateUi { it.copy(descriptionColor = hex) } }
            )
            ColorInputField(
                label = "version_color",
                hexValue = uiConfig.versionColor,
                onHexChange = { hex -> onUpdateUi { it.copy(versionColor = hex) } }
            )
            ColorInputField(
                label = "badge_color",
                hexValue = uiConfig.badgeColor,
                onHexChange = { hex -> onUpdateUi { it.copy(badgeColor = hex) } }
            )
            ColorInputField(
                label = "badge_text_color",
                hexValue = uiConfig.badgeTextColor,
                onHexChange = { hex -> onUpdateUi { it.copy(badgeTextColor = hex) } }
            )
            ColorInputField(
                label = "highlight_bg_color",
                hexValue = uiConfig.highlightBgColor,
                onHexChange = { hex -> onUpdateUi { it.copy(highlightBgColor = hex) } }
            )
            ColorInputField(
                label = "highlight_text_color",
                hexValue = uiConfig.highlightTextColor,
                onHexChange = { hex -> onUpdateUi { it.copy(highlightTextColor = hex) } }
            )
            ColorInputField(
                label = "footer_color",
                hexValue = uiConfig.footerColor,
                onHexChange = { hex -> onUpdateUi { it.copy(footerColor = hex) } }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsTelemetryCard(
    downloadClicks: Long,
    buttonClicks: Map<String, Long>,
    modifier: Modifier = Modifier
) {
    val formattedDownloads = NumberFormat.getIntegerInstance(Locale.US).format(downloadClicks)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("total_download_clicks_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_total_clicks_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = formattedDownloads,
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
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadForOffline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // button_clicks read-only map breakdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "BUTTON CLICKS (${FirebaseConstants.FIELD_BUTTON_CLICKS})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (buttonClicks.isEmpty()) {
                Text(
                    text = "No button_clicks recorded yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttonClicks.entries.sortedByDescending { it.value }.forEach { (btnId, count) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = btnId,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = NumberFormat.getIntegerInstance(Locale.US).format(count),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReleaseScheduleCard(
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
            color = if (isScheduled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.fillMaxWidth()
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
                    val badgeColor = if (isFutureRelease) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
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
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "release_date_millis: $releaseDateMillis",
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
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) accentColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerLow,
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

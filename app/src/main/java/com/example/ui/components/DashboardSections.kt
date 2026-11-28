package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DialogOptions
import com.example.data.UpdateConfig
import com.example.ui.AdminUiState
import com.example.ui.AdminViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun ModeAndContentTab(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    onOpenDatePicker: () -> Unit,
    onOpenGitHubDialog: () -> Unit,
    onTestApkUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cfg = uiState.config
    val ui = cfg.uiConfig

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnalyticsTelemetryCard(config = cfg)

        SectionCard(
            title = "Dialog Mode & Behavior",
            subtitle = "Control live status, update lock vs announcement mode, and frequency",
            icon = Icons.Rounded.NotificationsActive,
            badge = if (cfg.isEnabled) "LIVE ON" else "PAUSED"
        ) {
            LabeledSwitchRow(
                title = "Dialog Enabled (is_enabled)",
                subtitle = if (cfg.isEnabled) {
                    "Users receive this dialog in Battery Boss app"
                } else {
                    "Hidden for all users"
                },
                checked = cfg.isEnabled,
                onCheckedChange = { enabled -> viewModel.updateConfig { it.copy(isEnabled = enabled) } }
            )

            OptionChipsSelector(
                label = "Dialog Mode (dialog_mode)",
                options = DialogOptions.DIALOG_MODES,
                selected = cfg.dialogMode,
                onSelect = { mode -> viewModel.updateConfig { it.copy(dialogMode = mode) } },
                labelFormatter = { mode ->
                    if (mode == DialogOptions.MODE_UPDATE) "Update (App Version)" else "Announcement (Promo/Notice)"
                }
            )

            LabeledSwitchRow(
                title = "Mandatory Lock (is_mandatory)",
                subtitle = "When ON, users cannot close or skip the dialog",
                checked = cfg.isMandatory,
                onCheckedChange = { mandatory -> viewModel.updateConfig { it.copy(isMandatory = mandatory) } }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = cfg.dialogId,
                    onValueChange = { v -> viewModel.updateConfig { it.copy(dialogId = v) } },
                    label = { Text("Dialog ID (dialog_id)") },
                    placeholder = { Text("promo_01") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cfg.version,
                    onValueChange = { v -> viewModel.updateConfig { it.copy(version = v) } },
                    label = { Text("Version") },
                    placeholder = { Text("2.0") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(0.75f)
                )
            }

            LabeledSwitchRow(
                title = "Show Once Per Device (show_once)",
                subtitle = "Won't show again after dismiss until dialog_id changes",
                checked = cfg.showOnce,
                onCheckedChange = { once -> viewModel.updateConfig { it.copy(showOnce = once) } }
            )

            OutlinedTextField(
                value = cfg.apkUrl,
                onValueChange = { v -> viewModel.updateConfig { it.copy(apkUrl = v) } },
                label = { Text(stringResource(R.string.dashboard_apk_url_label)) },
                placeholder = { Text(stringResource(R.string.dashboard_apk_url_placeholder)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                trailingIcon = {
                    if (cfg.apkUrl.isNotBlank()) {
                        IconButton(onClick = { onTestApkUrl(cfg.apkUrl) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = stringResource(R.string.cd_test_download_apk),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                },
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = onOpenGitHubDialog,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
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
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        ReleaseScheduleCard(
            config = cfg,
            onImmediateClick = viewModel::onReleaseImmediatelySelected,
            onOpenDatePicker = onOpenDatePicker
        )

        SectionCard(
            title = "Text, Badge, Highlight & Footer",
            subtitle = "Content synced across root fields and ui_config",
            icon = Icons.Rounded.EditNote
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ui.badgeText,
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(badgeText = v) } },
                    label = { Text("Badge Text") },
                    placeholder = { Text("NEW / UPDATE") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ui.subtitle,
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(subtitle = v) } },
                    label = { Text("Subtitle") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1.3f)
                )
            }

            OutlinedTextField(
                value = ui.title.ifEmpty { cfg.title },
                onValueChange = viewModel::onTitleChanged,
                label = { Text("Dialog Title") },
                placeholder = { Text(stringResource(R.string.dashboard_title_placeholder)) },
                singleLine = true,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.description.ifEmpty { cfg.description },
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description / Multi-line Bullets") },
                placeholder = { Text("• Feature 1\n• Feature 2") },
                minLines = 3,
                maxLines = 6,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.highlightTitle,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(highlightTitle = v) } },
                label = { Text("Highlight Box Title") },
                singleLine = true,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.highlightText,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(highlightText = v) } },
                label = { Text("Highlight Box Text") },
                minLines = 2,
                maxLines = 4,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.footerText,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(footerText = v) } },
                label = { Text("Footer Text") },
                singleLine = true,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsTelemetryCard(
    config: UpdateConfig,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Live Analytics Telemetry",
        subtitle = "Read-only counters preserved automatically on every save",
        icon = Icons.Rounded.Analytics,
        badge = "READ-ONLY",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_total_clicks_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = config.downloadClicks.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "BUTTON CLICKS MAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val totalButtonClicks = config.buttonClicks.values.sum()
                    Text(
                        text = totalButtonClicks.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (config.buttonClicks.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                config.buttonClicks.forEach { (btnKey, count) ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "$btnKey: $count",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReleaseScheduleCard(
    config: UpdateConfig,
    onImmediateClick: () -> Unit,
    onOpenDatePicker: () -> Unit
) {
    val isScheduled = config.releaseDateMillis > 0L
    val isFuture = isScheduled && config.releaseDateMillis > System.currentTimeMillis()

    SectionCard(
        title = "Release Schedule",
        subtitle = "Control immediate rollout or scheduled future activation",
        icon = Icons.Rounded.Schedule,
        badge = if (!isScheduled) "IMMEDIATE" else if (isFuture) "SCHEDULED" else "DATE REACHED"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isScheduled,
                onClick = onImmediateClick,
                label = { Text(stringResource(R.string.dashboard_release_immediately)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isScheduled,
                onClick = onOpenDatePicker,
                label = { Text(stringResource(R.string.dashboard_schedule_for_date)) },
                leadingIcon = {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isScheduled) {
                            "Scheduled: ${config.releaseDate.ifBlank { "Selected Date" }}"
                        } else {
                            stringResource(R.string.dashboard_immediate_hint)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isScheduled) {
                        Text(
                            text = "release_date_millis = ${config.releaseDateMillis}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                if (isScheduled) {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = stringResource(R.string.cd_pick_date),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutAndMediaTab(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val ui = uiState.config.uiConfig

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(
            title = "Layout, Entrance & Built-in Animations",
            subtitle = "Configure structural template, card reveal, and decorative effects",
            icon = Icons.Rounded.Animation
        ) {
            OptionChipsSelector(
                label = "Layout Style (layout_style)",
                options = DialogOptions.LAYOUT_STYLES,
                selected = ui.layoutStyle,
                onSelect = { style -> viewModel.updateUiConfig { it.copy(layoutStyle = style) } }
            )

            OptionChipsSelector(
                label = "Entrance Animation (entrance_animation)",
                options = DialogOptions.ENTRANCE_ANIMATIONS,
                selected = ui.entranceAnimation,
                onSelect = { anim -> viewModel.updateUiConfig { it.copy(entranceAnimation = anim) } }
            )

            OptionChipsSelector(
                label = "Animation Type (animation_type)",
                options = DialogOptions.ANIMATION_TYPES,
                selected = ui.animationType,
                onSelect = { type -> viewModel.updateUiConfig { it.copy(animationType = type) } }
            )

            OptionChipsSelector(
                label = "Title Alignment (title_align)",
                options = DialogOptions.TITLE_ALIGNS,
                selected = ui.titleAlign,
                onSelect = { align -> viewModel.updateUiConfig { it.copy(titleAlign = align) } }
            )

            LabeledSwitchRow(
                title = "Header Wave Banner (show_header_wave)",
                checked = ui.showHeaderWave,
                onCheckedChange = { v -> viewModel.updateUiConfig { it.copy(showHeaderWave = v) } }
            )
            LabeledSwitchRow(
                title = "Sparkle Overlay (show_sparkles)",
                checked = ui.showSparkles,
                onCheckedChange = { v -> viewModel.updateUiConfig { it.copy(showSparkles = v) } }
            )
            LabeledSwitchRow(
                title = "Confetti Overlay (show_confetti)",
                checked = ui.showConfetti,
                onCheckedChange = { v -> viewModel.updateUiConfig { it.copy(showConfetti = v) } }
            )
        }

        SectionCard(
            title = "Photo, GIF & Video Media Controls",
            subtitle = "Supports direct image, animated GIF, or MP4/M3U8 streaming video",
            icon = Icons.Rounded.Movie
        ) {
            OptionChipsSelector(
                label = "Media Type (media_type)",
                options = DialogOptions.MEDIA_TYPES,
                selected = ui.mediaType,
                onSelect = { type -> viewModel.updateUiConfig { it.copy(mediaType = type) } }
            )

            OutlinedTextField(
                value = ui.mediaUrl,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(mediaUrl = v) } },
                label = { Text("Primary Media URL (media_url)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.imageUrl,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(imageUrl = v) } },
                label = { Text("Photo URL (image_url)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.gifUrl,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(gifUrl = v) } },
                label = { Text("GIF URL (gif_url)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.videoUrl,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(videoUrl = v) } },
                label = { Text("Video MP4 / M3U8 URL (video_url)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.thumbnailUrl,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(thumbnailUrl = v) } },
                label = { Text("Video Poster Thumbnail URL (thumbnail_url)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            LabeledIntSlider(
                label = "Media Height (media_height_dp)",
                value = ui.mediaHeightDp,
                range = 90..460,
                onValueChange = { h -> viewModel.updateUiConfig { it.copy(mediaHeightDp = h) } }
            )

            LabeledIntSlider(
                label = "Media Corner Radius (media_corner_radius_dp)",
                value = ui.mediaCornerRadiusDp,
                range = 0..36,
                onValueChange = { r -> viewModel.updateUiConfig { it.copy(mediaCornerRadiusDp = r) } }
            )

            OptionChipsSelector(
                label = "Media Content Scale (media_content_scale)",
                options = DialogOptions.MEDIA_CONTENT_SCALES,
                selected = ui.mediaContentScale,
                onSelect = { s -> viewModel.updateUiConfig { it.copy(mediaContentScale = s) } }
            )

            LabeledSwitchRow(
                title = "Video Autoplay (video_autoplay)",
                checked = ui.videoAutoplay,
                onCheckedChange = { v -> viewModel.updateUiConfig { it.copy(videoAutoplay = v) } }
            )
            LabeledSwitchRow(
                title = "Video Loop (video_loop)",
                checked = ui.videoLoop,
                onCheckedChange = { v -> viewModel.updateUiConfig { it.copy(videoLoop = v) } }
            )
            LabeledSwitchRow(
                title = "Video Muted (video_muted)",
                checked = ui.videoMuted,
                onCheckedChange = { v -> viewModel.updateUiConfig { it.copy(videoMuted = v) } }
            )
            LabeledSwitchRow(
                title = "Show Video Controls (video_show_controls)",
                checked = ui.videoShowControls,
                onCheckedChange = { v -> viewModel.updateUiConfig { it.copy(videoShowControls = v) } }
            )
        }
    }
}

@Composable
fun ColorsAndThemeTab(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val ui = uiState.config.uiConfig

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(
            title = "Backdrop & Overlay",
            subtitle = "Behind-dialog scrim gradient, image, and dim alpha",
            icon = Icons.Rounded.Layers
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Backdrop Color",
                    value = ui.backdropColor,
                    fallbackHex = "#05070A",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(backdropColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Backdrop Gradient End",
                    value = ui.backdropGradientEnd,
                    fallbackHex = "#0D1520",
                    allowEmpty = true,
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(backdropGradientEnd = v) } },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = ui.backdropImageUrl,
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(backdropImageUrl = v) } },
                label = { Text("Backdrop Image URL (backdrop_image_url)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            LabeledFloatSlider(
                label = "Backdrop Dim Alpha (backdrop_dim_alpha)",
                value = ui.backdropDimAlpha,
                range = 0.0f..0.92f,
                onValueChange = { a -> viewModel.updateUiConfig { it.copy(backdropDimAlpha = a) } }
            )
        }

        SectionCard(
            title = "Dialog Card & Header Surface",
            subtitle = "Card background gradient, border stroke, corners, and header wave",
            icon = Icons.Rounded.Gradient
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Card Color",
                    value = ui.cardColor,
                    fallbackHex = "#131922",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(cardColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Card Gradient End",
                    value = ui.cardGradientEnd,
                    fallbackHex = "#18212E",
                    allowEmpty = true,
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(cardGradientEnd = v) } },
                    modifier = Modifier.weight(1f)
                )
            }

            ColorInputField(
                label = "Card Border Color",
                value = ui.cardBorderColor,
                fallbackHex = "#263244",
                onValueChange = { v -> viewModel.updateUiConfig { it.copy(cardBorderColor = v) } }
            )

            LabeledIntSlider(
                label = "Card Corner Radius (card_corner_radius)",
                value = ui.cardCornerRadius,
                range = 0..44,
                onValueChange = { r -> viewModel.updateUiConfig { it.copy(cardCornerRadius = r) } }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Header Color",
                    value = ui.headerColor,
                    fallbackHex = "#0D261C",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(headerColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Header Gradient End",
                    value = ui.headerGradientEnd,
                    fallbackHex = "#0E2235",
                    allowEmpty = true,
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(headerGradientEnd = v) } },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SectionCard(
            title = "Typography, Badge & Highlight Palette",
            subtitle = "Fine-tune every text, accent, badge, and callout color",
            icon = Icons.Rounded.ColorLens
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Title Color",
                    value = ui.titleColor,
                    fallbackHex = "#F2F5F8",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(titleColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Subtitle Color",
                    value = ui.subtitleColor,
                    fallbackHex = "#9AA8BA",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(subtitleColor = v) } },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Description Color",
                    value = ui.descriptionColor,
                    fallbackHex = "#D6E0EB",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(descriptionColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Version Color",
                    value = ui.versionColor,
                    fallbackHex = "#22D3EE",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(versionColor = v) } },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Accent Color",
                    value = ui.accentColor,
                    fallbackHex = "#00E676",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(accentColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Footer Color",
                    value = ui.footerColor,
                    fallbackHex = "#7D8B9E",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(footerColor = v) } },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Badge Bg Color",
                    value = ui.badgeColor,
                    fallbackHex = "#00E676",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(badgeColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Badge Text Color",
                    value = ui.badgeTextColor,
                    fallbackHex = "#05190D",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(badgeTextColor = v) } },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorInputField(
                    label = "Highlight Bg Color",
                    value = ui.highlightBgColor,
                    fallbackHex = "#192638",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(highlightBgColor = v) } },
                    modifier = Modifier.weight(1f)
                )
                ColorInputField(
                    label = "Highlight Text Color",
                    value = ui.highlightTextColor,
                    fallbackHex = "#E5F3FF",
                    onValueChange = { v -> viewModel.updateUiConfig { it.copy(highlightTextColor = v) } },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

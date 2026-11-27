package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DialogButtonConfig
import com.example.data.DialogOptions
import com.example.data.DialogUiConfig
import com.example.data.UpdateConfig
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DialogPreviewPane(
    config: UpdateConfig,
    modifier: Modifier = Modifier
) {
    val ui = config.uiConfig
    var replayKey by remember { mutableIntStateOf(0) }

    val entranceProgress = remember { Animatable(1f) }
    LaunchedEffect(replayKey, ui.entranceAnimation) {
        if (ui.entranceAnimation == "none") {
            entranceProgress.snapTo(1f)
        } else {
            entranceProgress.snapTo(0f)
            when (ui.entranceAnimation) {
                "spring_pop" -> entranceProgress.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                "flip_in" -> entranceProgress.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
                else -> entranceProgress.animateTo(1f, animationSpec = tween(340, easing = FastOutSlowInEasing))
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "preview_fx")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "anim_phase"
    )
    val bounceFloat by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val effectiveLayout = if (ui.layoutStyle == "auto") {
        if (ui.resolvedMediaUrl().isNotBlank() && ui.mediaType != "none" && ui.mediaType != "animation") {
            "hero_media"
        } else {
            "wave_rocket"
        }
    } else {
        ui.layoutStyle
    }

    val backdropStart = parseHexColor(ui.backdropColor, Color(0xFF05070A))
    val backdropEnd = parseHexColor(ui.backdropGradientEnd, backdropStart)
    val cardStart = parseHexColor(ui.cardColor, Color(0xFF131922))
    val cardEnd = parseHexColor(ui.cardGradientEnd, cardStart)
    val cardBorder = parseHexColor(ui.cardBorderColor, Color(0xFF263244))
    val headerStart = parseHexColor(ui.headerColor, Color(0xFF0D261C))
    val headerEnd = parseHexColor(ui.headerGradientEnd, headerStart)
    val accent = parseHexColor(ui.accentColor, Color(0xFF00E676))

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Preview Header Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "LIVE MOBILE PREVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${config.dialogMode.uppercase()} • $effectiveLayout • ${ui.entranceAnimation}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = { replayKey++ },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = "Replay entrance animation",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Replay",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // Phone Frame Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(3.dp, Color(0xFF2A3441), RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(backdropStart, backdropEnd)))
        ) {
            if (ui.backdropImageUrl.isNotBlank()) {
                AsyncImage(
                    model = ui.backdropImageUrl.trim(),
                    contentDescription = "Backdrop preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            // Dim layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = ui.backdropDimAlpha.coerceIn(0f, 0.92f)))
            )

            // Confetti / Sparkle Backdrop FX
            if (ui.showConfetti || ui.animationType == "confetti" || ui.showSparkles || ui.animationType == "sparkle_wave") {
                PreviewParticlesOverlay(
                    phase = animPhase,
                    showConfetti = ui.showConfetti || ui.animationType == "confetti",
                    showSparkles = ui.showSparkles || ui.animationType == "sparkle_wave",
                    accentColor = accent,
                    modifier = Modifier.matchParentSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (effectiveLayout == "fullscreen_poster") 8.dp else 16.dp,
                        vertical = if (effectiveLayout == "fullscreen_poster") 12.dp else 20.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status bar indicator strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (config.isEnabled) "LIVE ON DEVICE" else "DISABLED (PREVIEW)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = if (config.isEnabled) Color(0xFF00E676) else Color(0xFFFFB300)
                    )
                    if (config.isMandatory) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "LOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color(0xFFFFB300)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dialog Card with Entrance Transform
                val p = entranceProgress.value
                val cardShape = RoundedCornerShape(ui.cardCornerRadius.coerceIn(0, 44).dp)

                Surface(
                    shape = cardShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.2.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            when (ui.entranceAnimation) {
                                "spring_pop", "fade_zoom" -> {
                                    val s = 0.78f + 0.22f * p
                                    scaleX = s
                                    scaleY = s
                                    alpha = p.coerceIn(0f, 1f)
                                }
                                "slide_up" -> {
                                    translationY = (1f - p) * 110f
                                    alpha = p.coerceIn(0f, 1f)
                                }
                                "flip_in" -> {
                                    rotationX = (1f - p) * 65f
                                    alpha = p.coerceIn(0f, 1f)
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(cardStart, cardEnd)))
                    ) {
                        // Optional Wave Header (for wave_rocket or when showHeaderWave is true and not minimal)
                        if (effectiveLayout == "wave_rocket" ||
                            (ui.showHeaderWave && effectiveLayout != "minimal" && effectiveLayout != "full_card_media")
                        ) {
                            WaveHeaderPreview(
                                ui = ui,
                                headerStart = headerStart,
                                headerEnd = headerEnd,
                                accent = accent,
                                bounceOffset = bounceFloat,
                                pulseScale = pulseScale
                            )
                        } else if (effectiveLayout == "full_card_media") {
                            PreviewMediaBlock(
                                ui = ui,
                                fullBleed = true
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (ui.uiBlocks.isNotEmpty()) {
                                // Custom ordered blocks
                                ui.uiBlocks.forEach { blockType ->
                                    PreviewSingleBlock(
                                        blockType = blockType,
                                        config = config,
                                        ui = ui,
                                        accent = accent,
                                        bounceOffset = bounceFloat,
                                        pulseScale = pulseScale
                                    )
                                }
                            } else {
                                // Standard layout sequence
                                PreviewBadgeAndVersionRow(config = config, ui = ui, pulseScale = pulseScale)

                                if (effectiveLayout == "hero_media" || effectiveLayout == "fullscreen_poster") {
                                    PreviewMediaBlock(ui = ui, fullBleed = false)
                                } else if (effectiveLayout == "wave_rocket" && ui.resolvedMediaUrl().isNotBlank() &&
                                    ui.mediaType != "none" && ui.mediaType != "animation"
                                ) {
                                    PreviewMediaBlock(ui = ui, fullBleed = false)
                                }

                                PreviewTitleAndSubtitle(ui = ui)
                                PreviewDescriptionBullets(config = config, ui = ui)
                                PreviewHighlightBox(ui = ui)
                                PreviewPrimaryAndExtraButtons(
                                    config = config,
                                    ui = ui,
                                    pulseScale = pulseScale
                                )
                            }

                            // Dismiss Button + Footer
                            if (ui.showNotNow && !config.isMandatory) {
                                TextButton(
                                    onClick = {},
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(
                                        text = ui.notNowText.ifBlank { "Not Now" },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = parseHexColor(ui.notNowColor, Color(0xFF8E9CAF))
                                    )
                                }
                            }

                            if (ui.footerText.isNotBlank()) {
                                Text(
                                    text = ui.footerText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = parseHexColor(ui.footerColor, Color(0xFF7D8B9E)),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
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
private fun WaveHeaderPreview(
    ui: DialogUiConfig,
    headerStart: Color,
    headerEnd: Color,
    accent: Color,
    bounceOffset: Float,
    pulseScale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(Brush.horizontalGradient(listOf(headerStart, headerEnd))),
        contentAlignment = Alignment.Center
    ) {
        if (ui.showHeaderWave) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.72f)
                    for (x in 0..w.toInt() step 8) {
                        val y = h * 0.72f + sin((x / w) * 2.5 * PI).toFloat() * 14f
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path = path, color = Color.White.copy(alpha = 0.08f))
            }
        }

        PreviewAnimationOrb(
            animationType = ui.animationType,
            accent = accent,
            bounceOffset = bounceOffset,
            pulseScale = pulseScale
        )
    }
}

@Composable
private fun PreviewAnimationOrb(
    animationType: String,
    accent: Color,
    bounceOffset: Float,
    pulseScale: Float
) {
    if (animationType == "none") return

    val orbScale = when (animationType) {
        "pulse_badge", "glow_ring" -> pulseScale
        else -> 1f
    }
    val offsetY = when (animationType) {
        "rocket", "bounce_icon" -> bounceOffset
        else -> 0f
    }
    val icon = when (animationType) {
        "confetti" -> Icons.Rounded.Celebration
        "sparkle_wave" -> Icons.Rounded.AutoAwesome
        else -> Icons.Rounded.RocketLaunch
    }

    Surface(
        shape = CircleShape,
        color = accent.copy(alpha = 0.18f),
        border = BorderStroke(
            width = if (animationType == "glow_ring") 2.5.dp else 1.5.dp,
            color = accent.copy(alpha = 0.7f)
        ),
        modifier = Modifier
            .size(54.dp)
            .graphicsLayer {
                scaleX = orbScale
                scaleY = orbScale
                translationY = offsetY
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun PreviewMediaBlock(
    ui: DialogUiConfig,
    fullBleed: Boolean
) {
    val resolvedUrl = ui.resolvedMediaUrl()
    val effectiveMediaType = if (ui.mediaType == "auto") {
        when {
            ui.videoUrl.isNotBlank() || resolvedUrl.endsWith(".mp4", true) || resolvedUrl.endsWith(".m3u8", true) -> "video"
            ui.gifUrl.isNotBlank() || resolvedUrl.endsWith(".gif", true) -> "gif"
            resolvedUrl.isNotBlank() -> "image"
            else -> "none"
        }
    } else {
        ui.mediaType
    }

    if (effectiveMediaType == "none" || effectiveMediaType == "animation") return

    val radius = if (fullBleed) 0.dp else ui.mediaCornerRadiusDp.coerceIn(0, 36).dp
    val previewHeight = ui.mediaHeightDp.coerceIn(90, 460).coerceAtMost(240).dp
    val scaleType = if (ui.mediaContentScale == "fit") ContentScale.Fit else ContentScale.Crop
    val imageModel = if (effectiveMediaType == "video") {
        ui.thumbnailUrl.ifBlank { resolvedUrl }
    } else {
        resolvedUrl
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(previewHeight)
            .clip(RoundedCornerShape(radius))
            .background(Color(0xFF0B111A)),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel.isNotBlank()) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Dialog media preview",
                contentScale = scaleType,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (effectiveMediaType == "video") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircleFilled,
                    contentDescription = "Video preview",
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.size(46.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Text(
                            text = buildString {
                                append("VIDEO")
                                if (ui.videoAutoplay) append(" • AUTO")
                                if (ui.videoLoop) append(" • LOOP")
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (ui.videoMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        } else if (resolvedUrl.isBlank()) {
            Text(
                text = "Enter ${effectiveMediaType.uppercase()} URL to preview",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7D8B9E)
            )
        }
    }
}

@Composable
private fun PreviewBadgeAndVersionRow(
    config: UpdateConfig,
    ui: DialogUiConfig,
    pulseScale: Float
) {
    val badgeBg = parseHexColor(ui.badgeColor, Color(0xFF00E676))
    val badgeTxt = parseHexColor(ui.badgeTextColor, Color(0xFF05190D))
    val verColor = parseHexColor(ui.versionColor, Color(0xFF22D3EE))
    val align = if (ui.titleAlign == "center") Arrangement.Center else Arrangement.Start

    if (ui.badgeText.isNotBlank() || config.version.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = align,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (ui.badgeText.isNotBlank()) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.scale(if (ui.animationType == "pulse_badge") pulseScale else 1f)
                ) {
                    Text(
                        text = ui.badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeTxt,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            if (config.version.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v${config.version.removePrefix("v")}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = verColor
                )
            }
        }
    }
}

@Composable
private fun PreviewTitleAndSubtitle(ui: DialogUiConfig) {
    val align = if (ui.titleAlign == "center") TextAlign.Center else TextAlign.Start
    val titleColor = parseHexColor(ui.titleColor, Color(0xFFF2F5F8))
    val subtitleColor = parseHexColor(ui.subtitleColor, Color(0xFF9AA8BA))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = ui.title.ifBlank { "Battery Boss Update" },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = titleColor,
            textAlign = align,
            modifier = Modifier.fillMaxWidth()
        )
        if (ui.subtitle.isNotBlank()) {
            Text(
                text = ui.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor,
                textAlign = align,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PreviewDescriptionBullets(config: UpdateConfig, ui: DialogUiConfig) {
    val desc = ui.description.ifBlank { config.description }
    if (desc.isBlank()) return
    val descColor = parseHexColor(ui.descriptionColor, Color(0xFFD6E0EB))
    Text(
        text = desc,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
        color = descColor,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PreviewHighlightBox(ui: DialogUiConfig) {
    if (ui.highlightTitle.isBlank() && ui.highlightText.isBlank()) return
    val bg = parseHexColor(ui.highlightBgColor, Color(0xFF192638))
    val txt = parseHexColor(ui.highlightTextColor, Color(0xFFE5F3FF))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, txt.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (ui.highlightTitle.isNotBlank()) {
                Text(
                    text = ui.highlightTitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = txt
                )
            }
            if (ui.highlightText.isNotBlank()) {
                Text(
                    text = ui.highlightText,
                    style = MaterialTheme.typography.bodySmall,
                    color = txt.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun PreviewPrimaryAndExtraButtons(
    config: UpdateConfig,
    ui: DialogUiConfig,
    pulseScale: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (ui.showPrimaryButton) {
            val btnStart = parseHexColor(ui.primaryButtonColor, Color(0xFF00E676))
            val btnEnd = parseHexColor(ui.primaryButtonGradientEnd, btnStart)
            val txtColor = parseHexColor(ui.primaryButtonTextColor, Color(0xFF05190D))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .scale(if (ui.primaryButtonPulse) pulseScale else 1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(btnStart, btnEnd))),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = resolveDialogIcon(ui.primaryButtonIcon),
                        contentDescription = null,
                        tint = txtColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = ui.primaryButtonText.ifBlank { "Update Now" },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = txtColor
                    )
                }
            }
        }

        if (ui.buttons.isNotEmpty()) {
            PreviewGroupedExtraButtons(buttons = ui.buttons)
        }
    }
}

@Composable
private fun PreviewGroupedExtraButtons(buttons: List<DialogButtonConfig>) {
    // Group consecutive or matching non-blank rowGroup buttons together
    val renderedGroups = mutableSetOf<String>()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { btn ->
            val group = btn.rowGroup.trim()
            if (group.isBlank()) {
                PreviewSingleExtraButton(btn = btn, modifier = Modifier.fillMaxWidth())
            } else if (renderedGroups.add(group)) {
                val rowButtons = buttons.filter { it.rowGroup.trim() == group }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowButtons.forEach { item ->
                        PreviewSingleExtraButton(
                            btn = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewSingleExtraButton(
    btn: DialogButtonConfig,
    modifier: Modifier = Modifier
) {
    val bg = parseHexColor(btn.bgColor, Color(0xFF00E676))
    val gradEnd = parseHexColor(btn.gradientEnd, bg)
    val txt = parseHexColor(btn.textColor, Color.White)
    val shape = RoundedCornerShape(btn.cornerRadiusDp.coerceIn(4, 50).dp)

    when (btn.style) {
        DialogOptions.STYLE_OUTLINED -> OutlinedButton(
            onClick = {},
            shape = shape,
            border = BorderStroke(1.2.dp, bg),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = modifier.height(42.dp)
        ) {
            Icon(resolveDialogIcon(btn.icon), null, tint = txt, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(btn.text, color = txt, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
        }

        DialogOptions.STYLE_TONAL -> Button(
            onClick = {},
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = bg.copy(alpha = 0.22f), contentColor = txt),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = modifier.height(42.dp)
        ) {
            Icon(resolveDialogIcon(btn.icon), null, tint = txt, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(btn.text, color = txt, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
        }

        DialogOptions.STYLE_TEXT -> TextButton(
            onClick = {},
            shape = shape,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = modifier.height(40.dp)
        ) {
            Icon(resolveDialogIcon(btn.icon), null, tint = txt, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(btn.text, color = txt, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
        }

        DialogOptions.STYLE_GRADIENT -> Box(
            modifier = modifier
                .height(42.dp)
                .clip(shape)
                .background(Brush.horizontalGradient(listOf(bg, gradEnd))),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(resolveDialogIcon(btn.icon), null, tint = txt, modifier = Modifier.size(16.dp))
                Text(btn.text, color = txt, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
            }
        }

        else -> Button(
            onClick = {},
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = txt),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = modifier.height(42.dp)
        ) {
            Icon(resolveDialogIcon(btn.icon), null, tint = txt, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(btn.text, color = txt, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PreviewSingleBlock(
    blockType: String,
    config: UpdateConfig,
    ui: DialogUiConfig,
    accent: Color,
    bounceOffset: Float,
    pulseScale: Float
) {
    when (blockType) {
        "badge" -> PreviewBadgeAndVersionRow(config, ui, pulseScale)
        "title" -> Text(
            text = ui.title.ifBlank { "Battery Boss Update" },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = parseHexColor(ui.titleColor, Color(0xFFF2F5F8)),
            textAlign = if (ui.titleAlign == "center") TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        "subtitle" -> if (ui.subtitle.isNotBlank()) {
            Text(
                text = ui.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = parseHexColor(ui.subtitleColor, Color(0xFF9AA8BA)),
                textAlign = if (ui.titleAlign == "center") TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        "text", "bullets" -> PreviewDescriptionBullets(config, ui)
        "media" -> PreviewMediaBlock(ui, fullBleed = false)
        "animation" -> Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PreviewAnimationOrb(ui.animationType, accent, bounceOffset, pulseScale)
        }
        "highlight_box" -> PreviewHighlightBox(ui)
        "divider" -> HorizontalDivider(color = parseHexColor(ui.cardBorderColor, Color(0xFF263244)))
        "spacer" -> Spacer(modifier = Modifier.height(6.dp))
        "button" -> {
            if (ui.showPrimaryButton) {
                val btnStart = parseHexColor(ui.primaryButtonColor, Color(0xFF00E676))
                val btnEnd = parseHexColor(ui.primaryButtonGradientEnd, btnStart)
                val txtColor = parseHexColor(ui.primaryButtonTextColor, Color(0xFF05190D))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .scale(if (ui.primaryButtonPulse) pulseScale else 1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(btnStart, btnEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(resolveDialogIcon(ui.primaryButtonIcon), null, tint = txtColor, modifier = Modifier.size(18.dp))
                        Text(ui.primaryButtonText.ifBlank { "Update Now" }, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }
            }
        }
        "button_row" -> PreviewGroupedExtraButtons(ui.buttons)
    }
}

@Composable
private fun PreviewParticlesOverlay(
    phase: Float,
    showConfetti: Boolean,
    showSparkles: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val palette = listOf(accentColor, Color(0xFF22D3EE), Color(0xFFF59E0B), Color(0xFFEC4899))

        if (showConfetti) {
            for (i in 0 until 18) {
                val x = ((i * 73) % 100) / 100f * w
                val yProgress = (phase + i * 0.13f) % 1f
                val y = yProgress * h
                drawCircle(
                    color = palette[i % palette.size].copy(alpha = 0.55f),
                    radius = if (i % 2 == 0) 4.5f else 3f,
                    center = Offset(x, y)
                )
            }
        }
        if (showSparkles) {
            for (i in 0 until 12) {
                val x = ((i * 59 + 17) % 100) / 100f * w
                val y = ((i * 41 + 11) % 100) / 100f * h
                val twinkle = 0.25f + 0.65f * kotlin.math.abs(sin((phase * 2 * PI + i).toFloat()))
                drawCircle(
                    color = Color.White.copy(alpha = twinkle * 0.55f),
                    radius = 2.8f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

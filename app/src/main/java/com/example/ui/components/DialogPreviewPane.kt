package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.GifBox
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialogPreviewPane(
    config: UpdateConfig,
    modifier: Modifier = Modifier
) {
    val ui = config.uiConfig
    var replayKey by remember { mutableIntStateOf(0) }
    var entranceVisible by remember(replayKey, ui.entranceAnimation) { mutableStateOf(false) }

    LaunchedEffect(replayKey, ui.entranceAnimation) {
        entranceVisible = false
        delay(40)
        entranceVisible = true
    }

    val backdropStart = parseHexColor(ui.backdropColor, Color(0xFF05070A))
    val backdropEnd = parseHexColor(ui.backdropGradientEnd.ifBlank { ui.backdropColor }, backdropStart)
    val cardStart = parseHexColor(ui.cardColor, Color(0xFF131922))
    val cardEnd = parseHexColor(ui.cardGradientEnd.ifBlank { ui.cardColor }, cardStart)
    val cardBorder = parseHexColor(ui.cardBorderColor, Color(0xFF263244))
    val headerStart = parseHexColor(ui.headerColor, Color(0xFF0D261C))
    val headerEnd = parseHexColor(ui.headerGradientEnd.ifBlank { ui.headerColor }, headerStart)
    val titleColor = parseHexColor(ui.titleColor, Color(0xFFF2F5F8))
    val subtitleColor = parseHexColor(ui.subtitleColor, Color(0xFF9AA8BA))
    val descColor = parseHexColor(ui.descriptionColor, Color(0xFFD6E0EB))
    val versionColor = parseHexColor(ui.versionColor, Color(0xFF22D3EE))
    val accentColor = parseHexColor(ui.accentColor, Color(0xFF00E676))
    val badgeColor = parseHexColor(ui.badgeColor, accentColor)
    val badgeTextColor = parseHexColor(ui.badgeTextColor, Color(0xFF05190D))
    val highlightBg = parseHexColor(ui.highlightBgColor, Color(0xFF192638))
    val highlightText = parseHexColor(ui.highlightTextColor, Color(0xFFE5F3FF))
    val footerColor = parseHexColor(ui.footerColor, Color(0xFF7D8B9E))

    val infiniteTransition = rememberInfiniteTransition(label = "preview_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cta_pulse"
    )
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_bob"
    )
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LIVE MOBILE PREVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${config.dialogMode.uppercase()} • ${ui.layoutStyle} • ${if (config.isEnabled) "ENABLED" else "DISABLED"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = { replayKey++ },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Replay entrance", modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Replay", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Simulated Phone Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(backdropStart, backdropEnd)))
        ) {
            if (ui.backdropImageUrl.isNotBlank()) {
                AsyncImage(
                    model = ui.backdropImageUrl,
                    contentDescription = "Backdrop preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = ui.backdropDimAlpha.coerceIn(0f, 0.92f)))
            )

            if (ui.showConfetti || ui.animationType == "confetti" || ui.showSparkles || ui.animationType == "sparkle_wave") {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val confettiColors = listOf(accentColor, versionColor, Color(0xFFF59E0B), Color(0xFFEC4899), Color.White)
                    if (ui.showConfetti || ui.animationType == "confetti") {
                        for (i in 0 until 24) {
                            val xFrac = ((i * 37) % 100) / 100f
                            val yFrac = (((i * 23) % 100) / 100f + particlePhase) % 1f
                            drawCircle(
                                color = confettiColors[i % confettiColors.size].copy(alpha = 0.72f),
                                radius = (3 + (i % 4)).dp.toPx(),
                                center = Offset(size.width * xFrac, size.height * yFrac)
                            )
                        }
                    }
                    if (ui.showSparkles || ui.animationType == "sparkle_wave") {
                        for (i in 0 until 14) {
                            val angle = (particlePhase * 2 * PI + i).toFloat()
                            val x = size.width * (((i * 53) % 90 + 5) / 100f)
                            val y = size.height * (((i * 41) % 85 + 8) / 100f)
                            val r = (2f + 2f * sin(angle)).dp.toPx()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.55f),
                                radius = r,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!config.isEnabled) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "DIALOG CURRENTLY OFF (PREVIEWING DRAFT)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                val isFullscreenPoster = ui.layoutStyle == "fullscreen_poster"
                val cardCorner = if (isFullscreenPoster) 18.dp else ui.cardCornerRadius.coerceIn(0, 44).dp

                val cardContent: @Composable () -> Unit = {
                    Surface(
                        shape = RoundedCornerShape(cardCorner),
                        color = Color.Transparent,
                        border = BorderStroke(1.2.dp, cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(cardCorner))
                            .background(Brush.verticalGradient(listOf(cardStart, cardEnd)))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Header Wave Banner
                            if (ui.showHeaderWave && ui.layoutStyle != "minimal") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(82.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val path = Path().apply {
                                            moveTo(0f, 0f)
                                            lineTo(size.width, 0f)
                                            lineTo(size.width, size.height * 0.72f)
                                            quadraticTo(
                                                size.width * 0.5f,
                                                size.height * 1.05f,
                                                0f,
                                                size.height * 0.75f
                                            )
                                            close()
                                        }
                                        drawPath(
                                            path = path,
                                            brush = Brush.horizontalGradient(listOf(headerStart, headerEnd))
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        PreviewBadgeAndVersion(
                                            badgeText = ui.badgeText,
                                            version = config.version,
                                            mode = config.dialogMode,
                                            badgeColor = badgeColor,
                                            badgeTextColor = badgeTextColor,
                                            versionColor = versionColor
                                        )
                                        if (ui.animationType != "none") {
                                            PreviewAnimationGlyph(
                                                animationType = ui.animationType,
                                                accentColor = accentColor,
                                                bobOffset = bobOffset,
                                                pulseScale = pulseScale
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (ui.uiBlocks.isNotEmpty()) {
                                    RenderCustomBlocks(
                                        ui = ui,
                                        config = config,
                                        titleColor = titleColor,
                                        subtitleColor = subtitleColor,
                                        descColor = descColor,
                                        versionColor = versionColor,
                                        accentColor = accentColor,
                                        badgeColor = badgeColor,
                                        badgeTextColor = badgeTextColor,
                                        highlightBg = highlightBg,
                                        highlightText = highlightText,
                                        bobOffset = bobOffset,
                                        pulseScale = pulseScale
                                    )
                                } else {
                                    if (!ui.showHeaderWave || ui.layoutStyle == "minimal") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PreviewBadgeAndVersion(
                                                badgeText = ui.badgeText,
                                                version = config.version,
                                                mode = config.dialogMode,
                                                badgeColor = badgeColor,
                                                badgeTextColor = badgeTextColor,
                                                versionColor = versionColor
                                            )
                                            if (ui.animationType != "none" && ui.layoutStyle != "minimal") {
                                                PreviewAnimationGlyph(
                                                    animationType = ui.animationType,
                                                    accentColor = accentColor,
                                                    bobOffset = bobOffset,
                                                    pulseScale = pulseScale
                                                )
                                            }
                                        }
                                    }

                                    val showMedia = ui.mediaType != "none" &&
                                        (ui.resolvedMediaUrl().isNotBlank() || ui.mediaType in listOf("image", "gif", "video"))
                                    if (showMedia && ui.layoutStyle != "minimal") {
                                        PreviewMediaBlock(ui = ui, accentColor = accentColor)
                                    }

                                    PreviewTextContent(
                                        ui = ui,
                                        fallbackTitle = config.title,
                                        fallbackDesc = config.description,
                                        titleColor = titleColor,
                                        subtitleColor = subtitleColor,
                                        descColor = descColor
                                    )

                                    if (ui.highlightTitle.isNotBlank() || ui.highlightText.isNotBlank()) {
                                        PreviewHighlightBox(
                                            title = ui.highlightTitle,
                                            text = ui.highlightText,
                                            bgColor = highlightBg,
                                            textColor = highlightText,
                                            accentColor = accentColor
                                        )
                                    }

                                    PreviewButtonsCluster(
                                        ui = ui,
                                        isMandatory = config.isMandatory,
                                        pulseScale = pulseScale
                                    )
                                }

                                if (ui.footerText.isNotBlank()) {
                                    Text(
                                        text = ui.footerText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = footerColor,
                                        textAlign = if (ui.titleAlign == "center") TextAlign.Center else TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                when (ui.entranceAnimation) {
                    "none" -> cardContent()
                    "slide_up" -> AnimatedVisibility(
                        visible = entranceVisible,
                        enter = fadeIn(tween(260)) + slideInVertically(tween(340)) { it / 3 }
                    ) { cardContent() }
                    "fade_zoom" -> AnimatedVisibility(
                        visible = entranceVisible,
                        enter = fadeIn(tween(280)) + scaleIn(tween(280), initialScale = 0.88f)
                    ) { cardContent() }
                    "flip_in" -> AnimatedVisibility(
                        visible = entranceVisible,
                        enter = fadeIn(tween(300)) + scaleIn(tween(320), initialScale = 0.78f)
                    ) {
                        Box(modifier = Modifier.graphicsLayer { rotationX = if (entranceVisible) 0f else 18f }) {
                            cardContent()
                        }
                    }
                    else -> AnimatedVisibility(
                        visible = entranceVisible,
                        enter = fadeIn(tween(220)) + scaleIn(
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                            initialScale = 0.82f
                        )
                    ) { cardContent() }
                }
            }
        }
    }
}

@Composable
private fun PreviewBadgeAndVersion(
    badgeText: String,
    version: String,
    mode: String,
    badgeColor: Color,
    badgeTextColor: Color,
    versionColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (badgeText.isNotBlank()) {
            Surface(
                shape = CircleShape,
                color = badgeColor
            ) {
                Text(
                    text = badgeText.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeTextColor,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }
        if (version.isNotBlank() && mode == DialogOptions.MODE_UPDATE) {
            Text(
                text = "v$version",
                style = MaterialTheme.typography.labelMedium,
                color = versionColor
            )
        }
    }
}

@Composable
private fun PreviewAnimationGlyph(
    animationType: String,
    accentColor: Color,
    bobOffset: Float,
    pulseScale: Float
) {
    val icon = when (animationType) {
        "confetti" -> Icons.Rounded.Celebration
        "sparkle_wave" -> Icons.Rounded.AutoAwesome
        else -> Icons.Rounded.RocketLaunch
    }
    val modifier = when (animationType) {
        "pulse_badge", "glow_ring" -> Modifier.scale(pulseScale)
        "bounce_icon", "rocket" -> Modifier.offset(y = bobOffset.dp)
        else -> Modifier
    }
    Surface(
        shape = CircleShape,
        color = accentColor.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = animationType,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PreviewMediaBlock(
    ui: DialogUiConfig,
    accentColor: Color
) {
    val url = ui.resolvedMediaUrl()
    val displayUrl = if (ui.mediaType == "video" && ui.thumbnailUrl.isNotBlank()) ui.thumbnailUrl else url
    val heightDp = ui.mediaHeightDp.coerceIn(90, 280).dp
    val cornerDp = ui.mediaCornerRadiusDp.coerceIn(0, 36).dp
    val scale = if (ui.mediaContentScale == "fit") ContentScale.Fit else ContentScale.Crop

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .clip(RoundedCornerShape(cornerDp))
            .background(Color(0xFF0A0F16))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(cornerDp)),
        contentAlignment = Alignment.Center
    ) {
        if (displayUrl.isNotBlank()) {
            AsyncImage(
                model = displayUrl,
                contentDescription = "Dialog media preview",
                contentScale = scale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (ui.mediaType) {
                        "video" -> Icons.Rounded.PlayCircleFilled
                        "gif" -> Icons.Rounded.GifBox
                        else -> Icons.Rounded.Image
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = "${ui.mediaType.uppercase()} MEDIA SLOT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        if (ui.mediaType == "video" || url.endsWith(".mp4", true) || url.endsWith(".m3u8", true)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircleFilled,
                    contentDescription = "Video preview",
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.size(44.dp)
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.65f)) {
                    Icon(
                        imageVector = if (ui.videoMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(5.dp).size(14.dp)
                    )
                }
                if (ui.videoAutoplay) {
                    Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.65f)) {
                        Text(
                            text = if (ui.videoLoop) "AUTO • LOOP" else "AUTO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewTextContent(
    ui: DialogUiConfig,
    fallbackTitle: String,
    fallbackDesc: String,
    titleColor: Color,
    subtitleColor: Color,
    descColor: Color
) {
    val align = if (ui.titleAlign == "center") TextAlign.Center else TextAlign.Start
    val titleText = ui.title.ifBlank { fallbackTitle }.ifBlank { "Battery Boss Update" }
    val descText = ui.description.ifBlank { fallbackDesc }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
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
        if (descText.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = descText,
                style = MaterialTheme.typography.bodyMedium,
                color = descColor,
                textAlign = align,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PreviewHighlightBox(
    title: String,
    text: String,
    bgColor: Color,
    textColor: Color,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor
                )
            }
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun PreviewButtonsCluster(
    ui: DialogUiConfig,
    isMandatory: Boolean,
    pulseScale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (ui.showPrimaryButton) {
            val btnStart = parseHexColor(ui.primaryButtonColor, Color(0xFF00E676))
            val btnEnd = parseHexColor(ui.primaryButtonGradientEnd.ifBlank { ui.primaryButtonColor }, btnStart)
            val btnText = parseHexColor(ui.primaryButtonTextColor, Color(0xFF05190D))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (ui.primaryButtonPulse) Modifier.scale(pulseScale) else Modifier)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(btnStart, btnEnd)))
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = btnText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = resolveDialogIcon(ui.primaryButtonIcon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ui.primaryButtonText.ifBlank { "Update Now" },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        PreviewExtraButtonsRows(ui.buttons)

        if (ui.showNotNow && !isMandatory) {
            TextButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = ui.notNowText.ifBlank { "Not Now" },
                    color = parseHexColor(ui.notNowColor, Color(0xFF8E9CAF))
                )
            }
        } else if (isMandatory) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Color(0xFFFFB020),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Mandatory — cannot be dismissed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB020)
                )
            }
        }
    }
}

@Composable
private fun PreviewExtraButtonsRows(buttons: List<DialogButtonConfig>) {
    if (buttons.isEmpty()) return
    val grouped = linkedMapOf<String, MutableList<DialogButtonConfig>>()
    buttons.forEachIndexed { idx, btn ->
        val key = if (btn.rowGroup.isBlank()) "__single_$idx" else "group_${btn.rowGroup}"
        grouped.getOrPut(key) { mutableListOf() }.add(btn)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        grouped.values.forEach { rowBtns ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowBtns.forEach { btn ->
                    PreviewSingleExtraButton(btn = btn, modifier = Modifier.weight(1f))
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
    val end = parseHexColor(btn.gradientEnd.ifBlank { btn.bgColor }, bg)
    val fg = parseHexColor(btn.textColor, Color.White)
    val shape = RoundedCornerShape(btn.cornerRadiusDp.coerceIn(0, 32).dp)

    when (btn.style) {
        DialogOptions.STYLE_OUTLINED -> {
            OutlinedButton(
                onClick = {},
                shape = shape,
                border = BorderStroke(1.dp, bg),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = modifier
            ) {
                Icon(resolveDialogIcon(btn.icon), contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(btn.text, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DialogOptions.STYLE_TEXT -> {
            TextButton(onClick = {}, shape = shape, modifier = modifier) {
                Icon(resolveDialogIcon(btn.icon), contentDescription = null, tint = bg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(btn.text, color = bg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DialogOptions.STYLE_TONAL -> {
            Button(
                onClick = {},
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = bg.copy(alpha = 0.22f),
                    contentColor = bg
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = modifier
            ) {
                Icon(resolveDialogIcon(btn.icon), contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(btn.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DialogOptions.STYLE_GRADIENT -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(Brush.horizontalGradient(listOf(bg, end)))
            ) {
                Button(
                    onClick = {},
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = fg),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(resolveDialogIcon(btn.icon), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(btn.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        else -> {
            Button(
                onClick = {},
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = modifier
            ) {
                Icon(resolveDialogIcon(btn.icon), contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(btn.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun RenderCustomBlocks(
    ui: DialogUiConfig,
    config: UpdateConfig,
    titleColor: Color,
    subtitleColor: Color,
    descColor: Color,
    versionColor: Color,
    accentColor: Color,
    badgeColor: Color,
    badgeTextColor: Color,
    highlightBg: Color,
    highlightText: Color,
    bobOffset: Float,
    pulseScale: Float
) {
    val align = if (ui.titleAlign == "center") TextAlign.Center else TextAlign.Start
    ui.uiBlocks.forEach { block ->
        when (block) {
            "badge" -> PreviewBadgeAndVersion(
                badgeText = ui.badgeText,
                version = config.version,
                mode = config.dialogMode,
                badgeColor = badgeColor,
                badgeTextColor = badgeTextColor,
                versionColor = versionColor
            )
            "title" -> Text(
                text = ui.title.ifBlank { config.title }.ifBlank { "Battery Boss Update" },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = titleColor,
                textAlign = align,
                modifier = Modifier.fillMaxWidth()
            )
            "subtitle" -> if (ui.subtitle.isNotBlank()) {
                Text(
                    text = ui.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    textAlign = align,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "text", "bullets" -> {
                val txt = ui.description.ifBlank { config.description }
                if (txt.isNotBlank()) {
                    Text(
                        text = txt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = descColor,
                        textAlign = align,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            "media" -> PreviewMediaBlock(ui = ui, accentColor = accentColor)
            "animation" -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PreviewAnimationGlyph(
                    animationType = ui.animationType,
                    accentColor = accentColor,
                    bobOffset = bobOffset,
                    pulseScale = pulseScale
                )
            }
            "highlight_box" -> if (ui.highlightTitle.isNotBlank() || ui.highlightText.isNotBlank()) {
                PreviewHighlightBox(
                    title = ui.highlightTitle,
                    text = ui.highlightText,
                    bgColor = highlightBg,
                    textColor = highlightText,
                    accentColor = accentColor
                )
            }
            "divider" -> HorizontalDivider(color = descColor.copy(alpha = 0.2f))
            "spacer" -> Spacer(modifier = Modifier.height(6.dp))
            "button" -> PreviewButtonsCluster(
                ui = ui.copy(buttons = emptyList()),
                isMandatory = config.isMandatory,
                pulseScale = pulseScale
            )
            "button_row" -> PreviewExtraButtonsRows(ui.buttons)
        }
    }
}

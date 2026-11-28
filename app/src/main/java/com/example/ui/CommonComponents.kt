package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevated MaterialCardView styling: 0dp stroke, 16dp-20dp corner radius, 4dp-8dp elevation.
 */
@Composable
fun AdminElevatedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 6.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier.fillMaxWidth()

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = elevation,
            tonalElevation = 0.dp,
            border = null,
            modifier = cardModifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content
            )
        }
    } else {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = elevation,
            tonalElevation = 0.dp,
            border = null,
            modifier = cardModifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun adminOutlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    disabledBorderColor = MaterialTheme.colorScheme.outline,
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

/**
 * Custom squircle or circular logo for Splash, Drawer Header, and Dashboard app cards.
 */
@Composable
fun AdminBrandLogo(
    size: Dp,
    shape: Shape = RoundedCornerShape(26.dp),
    accentIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val palette = when (accentIndex % 3) {
        1 -> listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF6366F1))
        2 -> listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF6366F1))
        else -> listOf(Color(0xFF73E8E0), Color(0xFF8B5CF6), Color(0xFF5B21B6))
    }
    val icon = when (accentIndex % 3) {
        1 -> Icons.Rounded.Apps
        2 -> Icons.Rounded.Bolt
        else -> Icons.Rounded.AdminPanelSettings
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = palette,
                    start = Offset(0f, 0f),
                    end = Offset(300f, 300f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = this.size.minDimension * 0.42f,
                center = Offset(this.size.width * 0.25f, this.size.height * 0.22f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = "ADMIN Logo",
            tint = Color.White,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

/**
 * Ringed circular avatar with optional bottom-right gray circular camera edit badge.
 */
@Composable
fun RingedProfileAvatar(
    avatarSize: Dp,
    ringSize: Dp,
    ringWidth: Dp = 3.dp,
    avatarColorIndex: Int = 0,
    showCameraBadge: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val avatarColors = listOf(
        Color(0xFF8B5CF6) to Color(0xFFF3E5F5),
        Color(0xFF0EA5E9) to Color(0xFFE0F2FE),
        Color(0xFFEC4899) to Color(0xFFFCE7F3),
        Color(0xFF10B981) to Color(0xFFD1FAE5)
    )
    val (bg, fg) = avatarColors[avatarColorIndex % avatarColors.size]

    Box(
        modifier = modifier
            .size(ringSize)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .clip(CircleShape)
                .border(BorderStroke(ringWidth, MaterialTheme.colorScheme.primary), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(bg.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile Avatar",
                    tint = bg,
                    modifier = Modifier.size(avatarSize * 0.58f)
                )
            }
        }

        if (showCameraBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF757575))
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Edit avatar",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

fun middleEllipsizeUrl(url: String, maxChars: Int = 34): String {
    if (url.length <= maxChars) return url
    val head = (maxChars * 0.52f).toInt().coerceAtLeast(10)
    val tail = (maxChars - head - 3).coerceAtLeast(6)
    return "${url.take(head)}...${url.takeLast(tail)}"
}

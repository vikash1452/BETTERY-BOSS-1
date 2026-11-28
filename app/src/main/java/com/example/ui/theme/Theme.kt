package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ColorElevatedHighest = androidx.compose.ui.graphics.Color(0xFF283444)
private val ColorOutlineSubtle = androidx.compose.ui.graphics.Color(0xFF1E2733)

private val BatteryBossDarkColorScheme = darkColorScheme(
    primary = VoltGreen,
    onPrimary = OnVoltGreen,
    primaryContainer = VoltGreenContainer,
    onPrimaryContainer = OnVoltGreenContainer,
    secondary = AmberVoltage,
    onSecondary = OnAmberVoltage,
    secondaryContainer = AmberVoltageContainer,
    onSecondaryContainer = OnAmberVoltageContainer,
    tertiary = CyanTelemetry,
    onTertiary = OnCyanTelemetry,
    tertiaryContainer = CyanTelemetryContainer,
    onTertiaryContainer = OnCyanTelemetryContainer,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = GraphiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = SlateSurfaceLow,
    surfaceContainer = SlateSurfaceCard,
    surfaceContainerHigh = SlateSurfaceElevated,
    surfaceContainerHighest = ColorElevatedHighest,
    outline = HairlineBorder,
    outlineVariant = ColorOutlineSubtle,
    error = CrimsonFault,
    onError = OnCrimsonFault,
    errorContainer = CrimsonFaultContainer,
    onErrorContainer = OnCrimsonFaultContainer
)

val BatteryBossShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BatteryBossTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = BatteryBossDarkColorScheme,
        typography = AppTypography,
        shapes = BatteryBossShapes,
        content = content
    )
}

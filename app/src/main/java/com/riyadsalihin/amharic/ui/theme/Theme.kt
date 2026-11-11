package com.riyadsalihin.amharic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.riyadsalihin.amharic.data.ThemeMode
import com.riyadsalihin.amharic.util.findActivity

private val LightColors = lightColorScheme(
    primary = GreenDeep,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = GreenDarker,
    secondary = Gold,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF2C2000),
    tertiary = androidx.compose.ui.graphics.Color(0xFF4A6353),
    background = GreenPale,
    onBackground = Ink,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Ink,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7EEE7),
    onSurfaceVariant = InkMuted,
    outline = androidx.compose.ui.graphics.Color(0xFF717A72),
    outlineVariant = OutlineLight,
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF04301C),
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFC5EBD4),
    secondary = GoldLight,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF3E2E00),
    secondaryContainer = GoldContainerDark,
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFF7E4B6),
    tertiary = androidx.compose.ui.graphics.Color(0xFFB0CCB8),
    background = NightBackground,
    onBackground = NightOnSurface,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightOnSurfaceVariant,
    outline = NightOutline,
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF4A554C),
)

private val RiyadShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun RiyadTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = RiyadTypography,
        shapes = RiyadShapes,
        content = content,
    )
}

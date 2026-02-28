package com.mindgambit.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================
// MindGambit — Theme
// Light is default. Full Material 3 color scheme mapping.
// ============================================================

private val LightColorScheme = lightColorScheme(
    primary            = Gold,
    onPrimary          = Cream,
    primaryContainer   = GoldBg,
    onPrimaryContainer = Obsidian,

    secondary          = ObsidianSoft,
    onSecondary        = Cream,
    secondaryContainer = Paper,
    onSecondaryContainer = TextPrimary,

    tertiary           = GoldLight,
    onTertiary         = Obsidian,

    background         = Cream,
    onBackground       = TextPrimary,

    surface            = Ivory,
    onSurface          = TextPrimary,
    surfaceVariant     = Paper,
    onSurfaceVariant   = TextSecondary,

    outline            = PaperDark,
    outlineVariant     = PaperDark,

    error              = Error,
    onError            = Cream,
)

private val DarkColorScheme = darkColorScheme(
    primary            = Gold,
    onPrimary          = Obsidian,
    primaryContainer   = GoldBg,
    onPrimaryContainer = GoldLight,

    secondary          = Paper,
    onSecondary        = Obsidian,
    secondaryContainer = DarkSurface2,
    onSecondaryContainer = TextOnDark,

    tertiary           = GoldLight,
    onTertiary         = Obsidian,

    background         = DarkBackground,
    onBackground       = TextOnDark,

    surface            = DarkSurface,
    onSurface          = TextOnDark,
    surfaceVariant     = DarkSurface2,
    onSurfaceVariant   = TextSecondary,

    outline            = DarkBorder,
    outlineVariant     = DarkBorder,

    error              = Error,
    onError            = Cream,
)

@Composable
fun MindGambitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Update status bar color to match theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = MindGambitTypography,
        shapes      = MindGambitShapes,
        content     = content
    )
}

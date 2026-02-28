package com.mindgambit.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mindgambit.app.R

// ============================================================
// MindGambit — Typography
//
// Display/headings: Cormorant Garamond (serif, editorial)
// Body/UI:          DM Sans (humanist sans, clean)
// Mono/labels:      DM Mono (technical, precise)
// ============================================================

val CormorantGaramond = FontFamily(
    Font(R.font.cormorant_garamond_regular,         FontWeight.Normal),
    Font(R.font.cormorant_garamond_italic,           FontWeight.Normal,  FontStyle.Italic),
    Font(R.font.cormorant_garamond_semibold,         FontWeight.SemiBold),
    Font(R.font.cormorant_garamond_semibold_italic,  FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.cormorant_garamond_bold,             FontWeight.Bold),
    Font(R.font.cormorant_garamond_bold_italic,      FontWeight.Bold,    FontStyle.Italic),
)

val DmSans = FontFamily(
    Font(R.font.dm_sans_light,    FontWeight.Light),
    Font(R.font.dm_sans_regular,  FontWeight.Normal),
    Font(R.font.dm_sans_medium,   FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(R.font.dm_sans_bold,     FontWeight.Bold),
)

val DmMono = FontFamily(
    Font(R.font.dm_mono_regular, FontWeight.Normal),
    Font(R.font.dm_mono_medium,  FontWeight.Medium),
)

// Convenience text styles used directly in composables
object MindGambitType {
    // Cormorant — display sizes
    val displayLarge  = TextStyle(fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold,    fontSize = 52.sp, letterSpacing = (-1).sp)
    val displayMedium = TextStyle(fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold,    fontSize = 36.sp, letterSpacing = (-0.5).sp)
    val displaySmall  = TextStyle(fontFamily = CormorantGaramond, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, letterSpacing = (-0.3).sp)

    // DM Sans — body
    val bodyLarge  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val bodyMedium = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val bodySmall  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)

    // DM Mono — labels/badges
    val labelLarge  = TextStyle(fontFamily = DmMono, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.5.sp)
    val labelMedium = TextStyle(fontFamily = DmMono, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 1.5.sp)
    val labelSmall  = TextStyle(fontFamily = DmMono, fontWeight = FontWeight.Normal, fontSize = 9.sp,  letterSpacing = 2.sp)

    // DM Sans — titles
    val titleLarge  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
    val titleMedium = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    val titleSmall  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium,   fontSize = 14.sp)

    // ELO number (big display)
    val eloNumber = TextStyle(fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 56.sp, letterSpacing = (-2).sp)
}

val MindGambitTypography = Typography(
    displayLarge  = MindGambitType.displayLarge,
    displayMedium = MindGambitType.displayMedium,
    displaySmall  = MindGambitType.displaySmall,
    headlineLarge = TextStyle(fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold,    fontSize = 32.sp),
    headlineMedium= TextStyle(fontFamily = CormorantGaramond, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    headlineSmall = TextStyle(fontFamily = CormorantGaramond, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge    = MindGambitType.titleLarge,
    titleMedium   = MindGambitType.titleMedium,
    titleSmall    = MindGambitType.titleSmall,
    bodyLarge     = MindGambitType.bodyLarge,
    bodyMedium    = MindGambitType.bodyMedium,
    bodySmall     = MindGambitType.bodySmall,
    labelLarge    = MindGambitType.labelLarge,
    labelMedium   = MindGambitType.labelMedium,
    labelSmall    = MindGambitType.labelSmall,
)

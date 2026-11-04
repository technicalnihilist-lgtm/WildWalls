package com.wildwalls.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Editorial pairing: a serif display face for the wordmark and section titles, a clean sans for
 * everything else. Both ship with Android, so no font files are downloaded at runtime.
 */
val WildTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextTertiary
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.4.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        letterSpacing = 1.4.sp,
        color = TextTertiary
    )
)

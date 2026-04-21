package com.recipeindex.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Sage & Linen Design System v2.0 typography
// Display font (Lora serif): used for screen titles, recipe names, section headers, step numbers
// Body/UI font (DM Sans): used for all other text, labels, metadata, navigation
// Currently using system font fallbacks; replace with bundled fonts via res/font/ for exact spec
private val DisplayFont = FontFamily.Serif    // → Lora
private val BodyFont = FontFamily.SansSerif  // → DM Sans

val RecipeIndexTypography = Typography(
    // 18sp 600 Lora — screen titles in headers
    displayLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.5.sp
    ),
    // 16sp 600 Lora — recipe card names
    titleLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.8.sp
    ),
    // 14sp 600 Lora — meal plan dates, modal headers
    titleMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.2.sp
    ),
    // 13sp 600 Lora — section labels (Quick actions, This week)
    titleSmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.2.sp
    ),
    // 16sp 400 DM Sans — cooking mode instructions
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.6.sp
    ),
    // 13sp 400 DM Sans — ingredients, grocery items
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.5.sp
    ),
    // 11sp 400 DM Sans — servings, cook time, dates
    bodySmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.4.sp
    ),
    // 10sp 500 DM Sans — nav labels, tag chips, category headers
    labelLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp
    ),
    // 9sp 500 DM Sans — footnotes, step counter x/y
    labelMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 11.7.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 11.7.sp
    )
)

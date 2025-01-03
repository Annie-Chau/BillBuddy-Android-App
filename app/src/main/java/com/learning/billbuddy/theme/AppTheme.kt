package com.example.blooddonation.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val primaryColor = Color(0xFFd70133)
val secondaryColor = Color(0xFFFAE2E6)
val backgroundColor = Color(0xF5F4F3F3)


private val lightColorScheme = AppColorScheme(
    primary = Color(0xFF1c8980),
    secondary = Color(0xFFfef3f5),
    third = Color(0xFFFAE2E6),
    onThird = Color(0xFFFAE2E6),
    background = Color(0xF5F4F3F3),
    onPrimary = Color.White,
    onSecondary = Color(0xFFd70133),
    border = Color.Gray,
    onBackground = Color.Black,
    tealBlue = Color(0xFF007AFF),
    success = Color(0xFF00C48C),
)

private val darkColorScheme = AppColorScheme(
    primary = Color(0xFF1c8980),
    secondary = Color(0xFFfef3f5),
    third = Color(0xFFFAE2E6),
    onThird = Color(0xFFFAE2E6),
    background = Color(0xF5F4F3F3),
    onPrimary = Color.White,
    onSecondary = Color(0xFFd70133),
    border = Color.Gray,
    onBackground = Color.Black,
    tealBlue = Color(0xFF007AFF),
    success = Color(0xFF00C48C),
)

private val typography = AppTypography(
    heading = TextStyle(
        fontFamily = Kavoon,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = 0.15.sp
    ),

    titleLarge = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = 0.15.sp
    ),

    titleMedium = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        letterSpacing = 0.15.sp
    ),

    titleSmall = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        letterSpacing = 0.15.sp
    ),

    titleExtraSmall = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 0.15.sp
    ),

    bodyExtraLarge = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.15.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.15.sp
    ),

    bodySmall = TextStyle(
        fontFamily = Archivo,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.15.sp
    ),


    )


private val shape = AppShape(
    container = RoundedCornerShape(12.dp),
    button = RoundedCornerShape(50.dp)
)

private val size = AppSize(
    large = 24.dp,
    medium = 16.dp,
    normal = 12.dp,
    small = 8.dp,
    extraSmall = 8.dp
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) darkColorScheme else lightColorScheme
//    val rippleIndication = rememberRipple()

    CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalAppTypography provides typography,
        LocalAppShape provides shape,
        LocalAppSize provides size,
//        LocalIndication provides rippleIndication,
        content = content
    )
}

object AppTheme {
    val colorScheme: AppColorScheme
        @Composable get() = LocalAppColorScheme.current

    val typography: AppTypography
        @Composable get() = LocalAppTypography.current

    val shape: AppShape
        @Composable get() = LocalAppShape.current

    val size: AppSize
        @Composable get() = LocalAppSize.current
}




















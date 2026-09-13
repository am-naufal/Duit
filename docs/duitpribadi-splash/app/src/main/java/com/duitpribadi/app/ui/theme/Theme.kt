package com.duitpribadi.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = DuitColor.Blue,
    onPrimary = Color.White,
    secondary = DuitColor.Pink,
    onSecondary = Color.White,
    background = DuitColor.Background,
    onBackground = Color(0xFF11161F),
    surface = DuitColor.Surface,
    onSurface = Color(0xFF11161F),
    outlineVariant = DuitColor.Divider
)

private val DarkColors = darkColorScheme(
    primary = DuitColor.BlueLight,
    onPrimary = Color.White,
    secondary = DuitColor.PinkLight,
    onSecondary = Color.White,
    background = DuitColor.BackgroundDark,
    onBackground = Color(0xFFECEFF5),
    surface = DuitColor.SurfaceDark,
    onSurface = Color(0xFFECEFF5)
)

private val DuitTypography = Typography(
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
)

@Composable
fun DuitPribadiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colors, typography = DuitTypography, content = content)
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ArrowUpSky,
    secondary = ArrowRightAmber,
    tertiary = ArrowDownPink,
    background = ArrowCubeLightBg,
    surface = ArrowCubeLightSurface,
    onPrimary = Color.White,
    onSecondary = Color(0xFF0F172A),
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun ArrowCubeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

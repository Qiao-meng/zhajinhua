package com.example.zhajinhua.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CardBrown = Color(0xFF5D4037)
val Cream = Color(0xFFFFF8E1)
val TableGreen = Color(0xFF2E7D32)
val AccentGold = Color(0xFFFFC107)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD84315),
    secondary = AccentGold,
    background = Color(0xFFFFF3E0),
    surface = Cream
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8A65),
    secondary = AccentGold,
    background = Color(0xFF263238),
    surface = Color(0xFF37474F)
)

@Composable
fun ZhajinhuaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

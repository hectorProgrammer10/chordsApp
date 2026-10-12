package com.magodeescalas.chordsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme =
        lightColorScheme(
                primary = WebPrimary,
                secondary = WebSecondary,
                tertiary = WebAccent,
                background = WebBackground,
                surface = WebBackground,
                onPrimary = WebPrimaryForeground,
                onSecondary = WebSecondaryForeground,
                onTertiary = WebAccentForeground,
                onBackground = WebForeground,
                onSurface = WebForeground
        )

@Composable
fun ChordsappTheme(content: @Composable () -> Unit) {
    // FORCE LIGHT THEME: Always use LightColorScheme
    val colorScheme = LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

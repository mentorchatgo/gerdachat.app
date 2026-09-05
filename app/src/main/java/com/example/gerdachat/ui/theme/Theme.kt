package com.example.gerdachat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WaTeal,
    secondary = WaTealDark,
    background = WaBackgroundDark,
    surface = WaPanelDark,
    onPrimary = WaBackgroundDark,
    onSecondary = WaTextPrimary,
    onBackground = WaTextPrimary,
    onSurface = WaTextPrimary,
    error = WaDanger
)

@Composable
fun GerdaChatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

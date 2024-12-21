package com.example.wordleclone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFFFFFFFF),
    onBackground = Color.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = Color.Black,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
)

object WordleColors {
    val matchInPositionLight = LightGreen
    val matchInWordLight = LightYellow
    val noMatchLight = LightGray

    val matchInPositionDark = DarkGreen
    val matchInWordDark = DarkYellow
    val noMatchDark = DarkGray

    val keyboardButton = MidGray
    val keyboardButtonNoMatch = DarkerGray
}

// Add custom colors to MaterialTheme
val MaterialTheme.matchInPositionColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) WordleColors.matchInPositionDark else WordleColors.matchInPositionLight

val MaterialTheme.matchInWordColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) WordleColors.matchInWordDark else WordleColors.matchInWordLight

val MaterialTheme.noMatchColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) WordleColors.noMatchDark else WordleColors.noMatchLight

val MaterialTheme.keyboardButtonColor: Color
    @Composable
    get() = WordleColors.keyboardButton

val MaterialTheme.keyboardButtonNoMatchColor: Color
    @Composable
    get() = WordleColors.keyboardButtonNoMatch

@Composable
fun WordleCloneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

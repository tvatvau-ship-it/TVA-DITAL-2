package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE50914), // Netflix Crimson Red
    secondary = Color(0xFF00E5FF), // Cyan Accent
    tertiary = Color(0xFF22242D),
    background = Color(0xFF090A0E), // Ultra dark background
    surface = Color(0xFF14151C), // Dark surface
    surfaceVariant = Color(0xFF1E202A),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFF5F5F7),
    onSurface = Color(0xFFF5F5F7),
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme
  // Disable dynamic color for branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

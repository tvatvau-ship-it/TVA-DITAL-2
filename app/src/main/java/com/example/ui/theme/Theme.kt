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
    primary = Color(0xFFE040FB), // Vibrant Neon Purple (Morado Brillante)
    secondary = Color(0xFFFFEA00), // Vibrant Yellow / Neon Gold
    tertiary = Color(0xFF651FFF), // Deep Indigo Violet
    background = Color(0xFF070010), // Very dark black-purple
    surface = Color(0xFF160A26), // Dark Purple Surface
    surfaceVariant = Color(0xFF2C134A), // Elevated Purple Surface
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
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

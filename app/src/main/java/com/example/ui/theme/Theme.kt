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

private val DarkColorScheme =
  darkColorScheme(
    primary = PastelPrimary,
    secondary = PastelSecondary,
    tertiary = PastelTertiary,
    background = Color(0xFF141F1A), // A soft dark sage green background
    surface = Color(0xFF1C2B24),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = PastelAccent,
    onBackground = Color(0xFFE3EDE8),
    onSurface = Color(0xFFE3EDE8),
    surfaceVariant = Color(0xFF263B31),
    onSurfaceVariant = PastelTextSecondary,
    error = PastelError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PastelPrimary,
    secondary = PastelSecondary,
    tertiary = PastelTertiary,
    background = PastelBg,
    surface = PastelSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = PastelAccent,
    onBackground = PastelTextPrimary,
    onSurface = PastelTextPrimary,
    surfaceVariant = PastelSurfaceVariant,
    onSurfaceVariant = PastelTextSecondary,
    error = PastelError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Disable dynamic colors to preserve requested custom branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

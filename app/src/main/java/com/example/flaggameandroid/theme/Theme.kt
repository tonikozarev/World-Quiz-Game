package com.example.flaggameandroid.theme

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
    primary = Canvas100,
    secondary = AccentGold,
    tertiary = AccentGreen,
    background = Ink900,
    surface = Ink800,
    onPrimary = Ink900,
    onSecondary = Ink900,
    onTertiary = Canvas100,
    onBackground = Canvas100,
    onSurface = Canvas100,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Ink800,
    secondary = AccentGold,
    tertiary = AccentGreen,
    background = Color(0xFFF4EEDD),
    surface = Color(0xFFF8F1E4),
    surfaceVariant = Color(0xFFE9DCC6),
    primaryContainer = Color(0xFFE3D3B7),
    secondaryContainer = Color(0xFFF0DEB6),
    tertiaryContainer = Color(0xFFDCE9D8),
    onPrimary = Color.White,
    onSecondary = Ink900,
    onTertiary = Ink900,
    onBackground = Ink900,
    onSurface = Ink900,
    onSurfaceVariant = Ink900,
    outline = Color(0xFFB8A98B),
  )

@Composable
fun FlagGameAndroidTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

package com.example.flaggameandroid.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
    secondary = AccentRed,
    tertiary = AccentGreen,
    background = Canvas100,
    surface = Canvas100,
    onPrimary = Canvas100,
    onSecondary = Canvas100,
    onTertiary = Canvas100,
    onBackground = Ink900,
    onSurface = Ink900,
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

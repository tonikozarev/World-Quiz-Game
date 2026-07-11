package com.example.flaggameandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.flaggameandroid.theme.FlagGameAndroidTheme

open class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(
        scrim = android.graphics.Color.BLACK,
      ),
      navigationBarStyle = SystemBarStyle.dark(
        scrim = android.graphics.Color.BLACK,
      ),
    )
    setContent {
      FlagGameAndroidTheme {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
          color = Color.Black,
        ) {
          FlagGameApp()
        }
      }
    }
  }
}

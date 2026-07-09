package com.example.flaggameandroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.flaggameandroid.engagement.EngagementDebugLogger
import com.example.flaggameandroid.persistence.AppGraph
import com.example.flaggameandroid.theme.FlagGameAndroidTheme
import kotlinx.coroutines.launch

open class MainActivity : ComponentActivity() {
  private val appContainer by lazy { AppGraph.from(applicationContext) }

  private val notificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

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

    lifecycleScope.launch {
      runCatching {
        EngagementDebugLogger.info("MainActivity engagement startup begin.")
        appContainer.engagementCoordinator.onAppOpened()
        val reminderEnabled = appContainer.settingsStore.loadReminderEnabled()
        if (reminderEnabled &&
          android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
          ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        EngagementDebugLogger.info("MainActivity engagement startup complete. reminderEnabled=$reminderEnabled")
      }.getOrElse {
        EngagementDebugLogger.error("MainActivity engagement startup failed.", it)
      }
    }
  }

  override fun onStop() {
    super.onStop()
    runCatching {
      EngagementDebugLogger.info("MainActivity onStop syncing launcher icon.")
      appContainer.engagementCoordinator.syncLauncherIconToPersistedState()
    }.getOrElse {
      EngagementDebugLogger.error("MainActivity onStop launcher icon sync failed.", it)
    }
  }
}

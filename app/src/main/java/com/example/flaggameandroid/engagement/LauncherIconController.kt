package com.example.flaggameandroid.engagement

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.flaggameandroid.persistence.ProgressStore
import kotlinx.coroutines.runBlocking

internal class LauncherIconController(
  private val context: Context,
  private val progressStore: ProgressStore,
) {
  fun syncLauncherIconToPersistedState() {
    runBlocking {
      val progress = progressStore.loadProgress()
      EngagementDebugLogger.debug("Syncing launcher icon to persisted state: inactive=${progress.inactiveIconActive}")
      setInactiveLauncherIcon(active = progress.inactiveIconActive)
    }
  }

  fun setInactiveLauncherIcon(active: Boolean) {
    val packageManager = context.packageManager
    val defaultAlias = ComponentName(context, "com.example.flaggameandroid.DefaultLauncherAlias")
    val inactiveAlias = ComponentName(context, "com.example.flaggameandroid.InactiveLauncherAlias")

    packageManager.setComponentEnabledSetting(
      defaultAlias,
      if (active) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
      PackageManager.DONT_KILL_APP,
    )
    packageManager.setComponentEnabledSetting(
      inactiveAlias,
      if (active) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
      PackageManager.DONT_KILL_APP,
    )
    EngagementDebugLogger.info(
      "Launcher icon switched. inactiveActive=$active, " +
        "defaultState=${packageManager.getComponentEnabledSetting(defaultAlias)}, " +
        "inactiveState=${packageManager.getComponentEnabledSetting(inactiveAlias)}",
    )
  }
}

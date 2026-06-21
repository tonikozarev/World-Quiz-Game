package com.example.flaggameandroid.engagement

import android.content.Context
import com.example.flaggameandroid.persistence.ProgressStore
import com.example.flaggameandroid.persistence.SettingsStore
import kotlinx.coroutines.runBlocking

class AppEngagementCoordinator(
  private val context: Context,
  private val settingsStore: SettingsStore,
  private val progressStore: ProgressStore,
  private val nowProvider: () -> Long = System::currentTimeMillis,
) {
  private val reminderScheduler = DailyReminderScheduler(context, nowProvider)
  private val reminderNotifier = ReminderNotificationPoster(context)
  private val launcherIconController = LauncherIconController(context, progressStore)

  fun onAppOpened() {
    recordAppOpened(progressStore, nowProvider)
    reminderNotifier.ensureNotificationChannel()
    scheduleDailyCheck()
  }

  fun syncLauncherIconToPersistedState() {
    launcherIconController.syncLauncherIconToPersistedState()
  }

  fun onDailyCheckTriggered() {
    triggerDailyEngagementNudge(
      settingsStore = settingsStore,
      progressStore = progressStore,
      reminderNotifier = reminderNotifier,
      launcherIconController = launcherIconController,
      nowProvider = nowProvider,
    )
    scheduleDailyCheck()
  }

  fun scheduleDailyCheck() {
    val zoneId = runBlocking { settingsStore.loadTimeZone().zoneId }
    reminderScheduler.scheduleDailyCheck(zoneId)
  }

  fun triggerTestingReminderNotification() {
    reminderNotifier.ensureNotificationChannel()
    reminderNotifier.postReminderNotification()
  }

  fun setInactiveLauncherIcon(active: Boolean) {
    launcherIconController.setInactiveLauncherIcon(active)
  }

  companion object {
    const val NotificationChannelId: String = "daily_flag_reminder"
  }
}

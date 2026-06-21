package com.example.flaggameandroid.engagement

import com.example.flaggameandroid.persistence.ProgressStore
import com.example.flaggameandroid.persistence.SettingsStore
import kotlinx.coroutines.runBlocking

internal fun recordAppOpened(
  progressStore: ProgressStore,
  nowProvider: () -> Long,
) {
  runBlocking {
    val progress = progressStore.loadProgress()
    progressStore.saveProgress(progress.copy(lastOpenedAtEpochMillis = nowProvider()))
  }
}

internal fun triggerDailyEngagementNudge(
  settingsStore: SettingsStore,
  progressStore: ProgressStore,
  reminderNotifier: ReminderNotificationPoster,
  launcherIconController: LauncherIconController,
  nowProvider: () -> Long,
) {
  reminderNotifier.ensureNotificationChannel()

  runBlocking {
    val now = nowProvider()
    val settingsReminderEnabled = settingsStore.loadReminderEnabled()
    val timeZone = settingsStore.loadTimeZone()
    val progress = progressStore.loadProgress()
    val shouldNudge = DailyEngagementRules.shouldTriggerDailyNudge(progress.lastOpenedAtEpochMillis, now, timeZone.zoneId)
    if (!shouldNudge) return@runBlocking

    val updatedProgress = progress.copy(inactiveIconActive = true)
    progressStore.saveProgress(updatedProgress)
    launcherIconController.setInactiveLauncherIcon(active = true)

    if (settingsReminderEnabled) {
      reminderNotifier.postReminderNotification()
    }
  }
}

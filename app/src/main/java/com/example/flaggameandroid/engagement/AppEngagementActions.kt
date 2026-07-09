package com.example.flaggameandroid.engagement

import com.example.flaggameandroid.persistence.ProgressStore
import com.example.flaggameandroid.persistence.SettingsStore
import kotlinx.coroutines.runBlocking
import java.time.ZoneOffset

internal fun recordAppOpened(
  progressStore: ProgressStore,
  nowProvider: () -> Long,
) {
  runBlocking {
    val progress = progressStore.loadProgress()
    val now = nowProvider()
    progressStore.saveProgress(progress.copy(lastOpenedAtEpochMillis = now))
    EngagementDebugLogger.info("App opened at ${EngagementDebugLogger.formatEpoch(now)}.")
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
    val progress = progressStore.loadProgress()
    val shouldNudge = DailyEngagementRules.shouldTriggerDailyNudge(progress.lastOpenedAtEpochMillis, now, ZoneOffset.UTC)
    EngagementDebugLogger.info(
      "Daily nudge check at ${EngagementDebugLogger.formatEpoch(now)}. " +
        "lastOpened=${EngagementDebugLogger.formatEpoch(progress.lastOpenedAtEpochMillis.coerceAtLeast(0L))}, " +
        "reminderEnabled=$settingsReminderEnabled, shouldNudge=$shouldNudge",
    )
    if (!shouldNudge) {
      EngagementDebugLogger.debug("Daily nudge skipped because the app was already opened in the current UTC day.")
      return@runBlocking
    }

    if (progress.inactiveIconActive) {
      EngagementDebugLogger.debug("Daily nudge skipped because inactive state is already active for the current cycle.")
      return@runBlocking
    }

    val updatedProgress = progress.copy(inactiveIconActive = true)
    progressStore.saveProgress(updatedProgress)
    launcherIconController.setInactiveLauncherIcon(active = true)
    EngagementDebugLogger.info("Inactive launcher icon activated for daily engagement nudge.")

    if (settingsReminderEnabled) {
      reminderNotifier.postReminderNotification()
    } else {
      EngagementDebugLogger.debug("Reminder notification not posted because reminders are disabled in settings.")
    }
  }
}

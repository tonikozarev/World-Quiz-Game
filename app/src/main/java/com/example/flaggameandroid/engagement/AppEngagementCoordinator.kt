package com.example.flaggameandroid.engagement

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.flaggameandroid.MainActivity
import com.example.flaggameandroid.R
import com.example.flaggameandroid.persistence.PersistedAppState
import com.example.flaggameandroid.persistence.ProgressStore
import com.example.flaggameandroid.persistence.SettingsStore
import kotlinx.coroutines.runBlocking

class AppEngagementCoordinator(
  private val context: Context,
  private val settingsStore: SettingsStore,
  private val progressStore: ProgressStore,
  private val nowProvider: () -> Long = System::currentTimeMillis,
) {
  fun onAppOpened() {
    runBlocking {
      val progress = progressStore.loadProgress()
      progressStore.saveProgress(progress.copy(lastOpenedAtEpochMillis = nowProvider()))
    }
    ensureNotificationChannel()
    scheduleDailyCheck()
  }

  fun onQuizCompleted() {
    // Defer launcher icon changes until the app leaves foreground so quiz results stay visible.
  }

  fun syncLauncherIconToPersistedState() {
    runBlocking {
      val progress = progressStore.loadProgress()
      setInactiveLauncherIcon(active = progress.inactiveIconActive)
    }
  }

  fun onDailyCheckTriggered() {
    ensureNotificationChannel()

    runBlocking {
      val now = nowProvider()
      val settingsReminderEnabled = settingsStore.loadReminderEnabled()
      val progress = progressStore.loadProgress()
      val shouldNudge = DailyEngagementRules.shouldTriggerDailyNudge(progress.lastOpenedAtEpochMillis, now)
      if (!shouldNudge) return@runBlocking

      val updatedProgress = progress.copy(inactiveIconActive = true)
      progressStore.saveProgress(updatedProgress)
      setInactiveLauncherIcon(active = true)

      if (settingsReminderEnabled) {
        postReminderNotification()
      }
    }

    scheduleDailyCheck()
  }

  fun scheduleDailyCheck() {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val pendingIntent = dailyCheckPendingIntent()
    val triggerAtMillis = DailyEngagementRules.nextReminderEpochMillis(nowProvider())

    alarmManager.setInexactRepeating(
      AlarmManager.RTC_WAKEUP,
      triggerAtMillis,
      AlarmManager.INTERVAL_DAY,
      pendingIntent,
    )
  }

  fun triggerTestingReminderNotification() {
    ensureNotificationChannel()
    postReminderNotification()
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
  }

  private fun postReminderNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
      return
    }

    val launchIntent =
      Intent(context, MainActivity::class.java).let { intent ->
        PendingIntent.getActivity(
          context,
          RequestCodes.OpenApp,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
      }

    val notification =
      NotificationCompat.Builder(context, NotificationChannelId)
        .setSmallIcon(R.drawable.ic_notification_small)
        .setContentTitle("Flag check-in")
        .setContentText("Your flags are waiting. Jump back in and keep the streak moving.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(launchIntent)
        .build()

    NotificationManagerCompat.from(context).notify(NotificationIds.DailyReminder, notification)
  }

  private fun ensureNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

      val channel =
      NotificationChannel(
        NotificationChannelId,
        "Daily reminders",
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = "12:00 reminder to return to the flag quiz."
      }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    notificationManager.createNotificationChannel(channel)
  }

  private fun dailyCheckPendingIntent(): PendingIntent {
    val intent = Intent(context, DailyReminderReceiver::class.java)
    return PendingIntent.getBroadcast(
      context,
      RequestCodes.DailyReminder,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  companion object {
    const val NotificationChannelId: String = "daily_flag_reminder"
  }
}

private object NotificationIds {
  const val DailyReminder: Int = 2001
}

private object RequestCodes {
  const val DailyReminder: Int = 2002
  const val OpenApp: Int = 2003
}

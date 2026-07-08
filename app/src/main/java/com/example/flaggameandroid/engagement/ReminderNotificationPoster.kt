package com.example.flaggameandroid.engagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.flaggameandroid.MainActivity
import com.example.flaggameandroid.R

internal class ReminderNotificationPoster(
  private val context: Context,
) {
  fun ensureNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channel =
      NotificationChannel(
        AppEngagementCoordinator.NotificationChannelId,
        "Daily reminders",
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = "12:00 reminder to return to the flag quiz."
      }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
    notificationManager.createNotificationChannel(channel)
    EngagementDebugLogger.debug("Reminder notification channel ensured.")
  }

  fun postReminderNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
      EngagementDebugLogger.warn("Reminder notification skipped: POST_NOTIFICATIONS permission not granted.")
      return
    }

    val launchIntent =
      Intent(context, MainActivity::class.java).let { intent ->
        PendingIntent.getActivity(
          context,
          NotificationRequestCodes.OpenApp,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
      }

    val notification =
      NotificationCompat.Builder(context, AppEngagementCoordinator.NotificationChannelId)
        .setSmallIcon(R.drawable.ic_notification_small)
        .setContentTitle("Flag check-in")
        .setContentText("Your flags are waiting. Jump back in and keep the streak moving.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(launchIntent)
        .build()

    NotificationManagerCompat.from(context).notify(NotificationIds.DailyReminder, notification)
    EngagementDebugLogger.info(
      "Reminder notification posted at ${EngagementDebugLogger.formatEpoch(System.currentTimeMillis())}.",
    )
  }
}

private object NotificationIds {
  const val DailyReminder: Int = 2001
}

private object NotificationRequestCodes {
  const val OpenApp: Int = 2003
}

package com.example.flaggameandroid.engagement

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.ZoneId

internal class DailyReminderScheduler(
  private val context: Context,
  private val nowProvider: () -> Long = System::currentTimeMillis,
) {
  fun scheduleDailyCheck(zoneId: ZoneId) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val pendingIntent = dailyCheckPendingIntent()
    val triggerAtMillis = DailyEngagementRules.nextReminderEpochMillis(nowProvider(), zoneId)

    alarmManager.cancel(pendingIntent)

    when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms() -> {
        EngagementDebugLogger.info(
          "Scheduling exact daily reminder at ${EngagementDebugLogger.formatEpoch(triggerAtMillis)} " +
            "(zone=$zoneId, exact=true)",
        )
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent,
        )
      }
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
        EngagementDebugLogger.info(
          "Scheduling while-idle daily reminder at ${EngagementDebugLogger.formatEpoch(triggerAtMillis)} " +
            "(zone=$zoneId, exact=false)",
        )
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent,
        )
      }
      else -> {
        EngagementDebugLogger.info(
          "Scheduling standard daily reminder at ${EngagementDebugLogger.formatEpoch(triggerAtMillis)} " +
            "(zone=$zoneId, exact=false)",
        )
        alarmManager.set(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent,
        )
      }
    }
  }

  private fun dailyCheckPendingIntent(): PendingIntent {
    val intent = Intent(context, DailyReminderReceiver::class.java)
    return PendingIntent.getBroadcast(
      context,
      SchedulerRequestCodes.DailyReminder,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }
}

private object SchedulerRequestCodes {
  const val DailyReminder: Int = 2002
}

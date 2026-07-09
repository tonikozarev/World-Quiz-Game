package com.example.flaggameandroid.engagement

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Instant
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
    scheduleAlarm(alarmManager, pendingIntent, triggerAtMillis, zoneId, "daily reminder")
  }

  fun scheduleTestingReminderInOneMinute() {
    scheduleDeterministicTestingAlarm(
      label = "test reminder",
      pendingIntent = testingReminderPendingIntent(),
    )
  }

  fun scheduleTestingInactiveIconInOneMinute() {
    scheduleDeterministicTestingAlarm(
      label = "test inactive-icon action",
      pendingIntent = testingInactiveIconPendingIntent(),
    )
  }

  private fun scheduleDeterministicTestingAlarm(
    label: String,
    pendingIntent: PendingIntent,
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val triggerAtMillis = EngagementTestSchedule.triggerAtMillis(nowProvider())
    alarmManager.cancel(pendingIntent)
    EngagementDebugLogger.info(
      "Scheduling deterministic $label at ${EngagementDebugLogger.formatEpoch(triggerAtMillis)} " +
        "(delay=${EngagementTestSchedule.OneMinuteDelayMillis / 1000}s, alarm-only)",
    )
    scheduleAlarm(
      alarmManager = alarmManager,
      pendingIntent = pendingIntent,
      triggerAtMillis = triggerAtMillis,
      zoneId = ZoneId.systemDefault(),
      label = label,
    )
  }

  private fun scheduleAlarm(
    alarmManager: AlarmManager,
    pendingIntent: PendingIntent,
    triggerAtMillis: Long,
    zoneId: ZoneId,
    label: String,
  ) {
    val now = Instant.ofEpochMilli(nowProvider()).atZone(zoneId)
    val target = Instant.ofEpochMilli(triggerAtMillis).atZone(zoneId)

    when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms() -> {
        EngagementDebugLogger.info(
          "Scheduling exact $label at ${EngagementDebugLogger.formatEpoch(triggerAtMillis)} " +
            "(zone=$zoneId, exact=true, in=${target.toEpochSecond() - now.toEpochSecond()}s)",
        )
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent,
        )
      }
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
        EngagementDebugLogger.info(
          "Scheduling while-idle fallback $label at ${EngagementDebugLogger.formatEpoch(triggerAtMillis)} " +
            "(zone=$zoneId, exact=false, in=${target.toEpochSecond() - now.toEpochSecond()}s)",
        )
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent,
        )
      }
      else -> {
        EngagementDebugLogger.info(
          "Scheduling standard $label at ${EngagementDebugLogger.formatEpoch(triggerAtMillis)} " +
            "(zone=$zoneId, exact=false, in=${target.toEpochSecond() - now.toEpochSecond()}s)",
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

  private fun testingReminderPendingIntent(): PendingIntent {
    val intent = Intent(context, TestingReminderReceiver::class.java)
    return PendingIntent.getBroadcast(
      context,
      SchedulerRequestCodes.TestingReminder,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun testingInactiveIconPendingIntent(): PendingIntent {
    val intent = Intent(context, TestingInactiveIconReceiver::class.java)
    return PendingIntent.getBroadcast(
      context,
      SchedulerRequestCodes.TestingInactiveIcon,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

}

private object SchedulerRequestCodes {
  const val DailyReminder: Int = 2002
  const val TestingReminder: Int = 2003
  const val TestingInactiveIcon: Int = 2004
}

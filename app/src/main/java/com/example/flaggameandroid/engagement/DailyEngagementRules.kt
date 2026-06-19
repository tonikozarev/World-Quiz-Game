package com.example.flaggameandroid.engagement

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object DailyEngagementRules {
  fun shouldTriggerDailyNudge(
    lastOpenedAtEpochMillis: Long,
    nowEpochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
  ): Boolean {
    if (lastOpenedAtEpochMillis <= 0L) return true
    return toLocalDate(lastOpenedAtEpochMillis, zoneId).isBefore(toLocalDate(nowEpochMillis, zoneId))
  }

  fun nextReminderEpochMillis(
    nowEpochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
  ): Long {
    val now = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId)
    val todayReminder =
      now.toLocalDate()
        .atTime(12, 0)
        .atZone(zoneId)

    val target = if (now.isBefore(todayReminder)) todayReminder else todayReminder.plusDays(1)
    return target.toInstant().toEpochMilli()
  }

  private fun toLocalDate(
    epochMillis: Long,
    zoneId: ZoneId,
  ): LocalDate = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
}

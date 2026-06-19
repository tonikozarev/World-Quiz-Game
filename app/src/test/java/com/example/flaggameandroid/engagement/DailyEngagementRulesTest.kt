package com.example.flaggameandroid.engagement

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyEngagementRulesTest {
  private val zoneId: ZoneId = ZoneId.of("Europe/Bucharest")

  @Test
  fun shouldTriggerDailyNudge_whenNeverOpened_returnsTrue() {
    val now = dateTime(2026, 6, 17, 13, 0)

    assertTrue(DailyEngagementRules.shouldTriggerDailyNudge(0L, now, zoneId))
  }

  @Test
  fun shouldTriggerDailyNudge_whenOpenedToday_returnsFalse() {
    val lastOpened = dateTime(2026, 6, 17, 8, 30)
    val now = dateTime(2026, 6, 17, 13, 0)

    assertFalse(DailyEngagementRules.shouldTriggerDailyNudge(lastOpened, now, zoneId))
  }

  @Test
  fun shouldTriggerDailyNudge_whenOpenedYesterday_returnsTrue() {
    val lastOpened = dateTime(2026, 6, 16, 21, 0)
    val now = dateTime(2026, 6, 17, 13, 0)

    assertTrue(DailyEngagementRules.shouldTriggerDailyNudge(lastOpened, now, zoneId))
  }

  @Test
  fun nextReminderEpochMillis_beforeReminder_returnsTodayAtTwelve() {
    val now = dateTime(2026, 6, 17, 9, 45)

    val result = DailyEngagementRules.nextReminderEpochMillis(now, zoneId)

    assertEquals(dateTime(2026, 6, 17, 12, 0), result)
  }

  @Test
  fun nextReminderEpochMillis_afterReminder_returnsTomorrowAtTwelve() {
    val now = dateTime(2026, 6, 17, 16, 10)

    val result = DailyEngagementRules.nextReminderEpochMillis(now, zoneId)

    assertEquals(dateTime(2026, 6, 18, 12, 0), result)
  }

  private fun dateTime(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
  ): Long =
    ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zoneId)
      .toInstant()
      .toEpochMilli()
}

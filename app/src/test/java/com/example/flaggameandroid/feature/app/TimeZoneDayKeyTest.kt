package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.ActivityDayRecord
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.DayOfWeek
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

class TimeZoneDayKeyTest {
  @Test
  fun localDayKey_usesSelectedTimeZone() {
    val epochMillis = Instant.parse("2026-06-21T22:30:00Z").toEpochMilli()

    val utcDay = localDayKey(epochMillis, AppTimeZone.Utc)
    val utcPlusThreeDay = localDayKey(epochMillis, AppTimeZone.UtcPlus3)

    assertEquals(20625L, utcDay)
    assertEquals(20626L, utcPlusThreeDay)
    assertTrue(utcDay != utcPlusThreeDay)
  }

  @Test
  fun weekActivityDays_returnsFixedMondayToSundayStrip() {
    val nowEpochMillis = Instant.parse("2026-06-17T10:00:00Z").toEpochMilli()
    val mondayKey =
      Instant.ofEpochMilli(nowEpochMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .toEpochDay()
    val wednesdayKey = mondayKey + 2
    val thursdayKey = mondayKey + 3
    val activityCalendar =
      mapOf(
        wednesdayKey to ActivityDayRecord(dayKey = wednesdayKey, quizzesCompleted = 1),
        thursdayKey to ActivityDayRecord(dayKey = thursdayKey, quizzesCompleted = 2),
      )

    val weekDays = weekActivityDays(activityCalendar, nowEpochMillis = nowEpochMillis, timeZone = AppTimeZone.Utc)

    assertEquals(7, weekDays.size)
    assertEquals(mondayKey, weekDays.first().dayKey)
    assertEquals(mondayKey + 6, weekDays.last().dayKey)
    assertEquals(1, weekDays[2].quizzesCompleted)
    assertEquals(2, weekDays[3].quizzesCompleted)
  }
}

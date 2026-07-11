package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.RatingsProgress
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FlagGameStreakMedalsTest {
  @Test
  fun awardStreakMedalsIfEligible_awardsSevenDayMedalAndResetsProgress() {
    val completedAt = 7L * DAY
    val previousActivityCalendar =
      mapOf(
        0L to ActivityDayRecord(dayKey = 0L, quizzesCompleted = 1),
        1L to ActivityDayRecord(dayKey = 1L, quizzesCompleted = 1),
        2L to ActivityDayRecord(dayKey = 2L, quizzesCompleted = 1),
        3L to ActivityDayRecord(dayKey = 3L, quizzesCompleted = 1),
        4L to ActivityDayRecord(dayKey = 4L, quizzesCompleted = 1),
        5L to ActivityDayRecord(dayKey = 5L, quizzesCompleted = 1),
        6L to ActivityDayRecord(dayKey = 6L, quizzesCompleted = 1),
      )
    val ratings = RatingsProgress(streak7ProgressDays = 6)

    val updated =
      awardStreakMedalsIfEligible(
        ratings = ratings,
        previousActivityCalendar = previousActivityCalendar,
        updatedActivityCalendar = emptyMap(),
        completedAtEpochMillis = completedAt,
      )

    assertEquals(1, updated.streak7Count)
    assertEquals(0, updated.streak7ProgressDays)
  }

  @Test
  fun awardStreakMedalsIfEligible_awardsThirtyDayMedalAndResetsProgress() {
    val completedAt = 30L * DAY
    val previousActivityCalendar =
      (0L until 30L).associateWith { day ->
        ActivityDayRecord(dayKey = day, quizzesCompleted = 1)
      }
    val ratings = RatingsProgress(streak30ProgressDays = 29)

    val updated =
      awardStreakMedalsIfEligible(
        ratings = ratings,
        previousActivityCalendar = previousActivityCalendar,
        updatedActivityCalendar = emptyMap(),
        completedAtEpochMillis = completedAt,
      )

    assertEquals(1, updated.streak30Count)
    assertEquals(0, updated.streak30ProgressDays)
  }

  private companion object {
    const val DAY = 86_400_000L
  }
}

package com.example.flaggameandroid.engagement

import junit.framework.TestCase.assertEquals
import org.junit.Test

class EngagementTestScheduleTest {
  @Test
  fun triggerAtMillis_addsExactlyOneMinute() {
    val now = 1_234_567L

    val result = EngagementTestSchedule.triggerAtMillis(now)

    assertEquals(now + 60_000L, result)
  }

  @Test
  fun oneMinuteDelayMillis_isExactlySixtySeconds() {
    assertEquals(60_000L, EngagementTestSchedule.OneMinuteDelayMillis)
  }
}

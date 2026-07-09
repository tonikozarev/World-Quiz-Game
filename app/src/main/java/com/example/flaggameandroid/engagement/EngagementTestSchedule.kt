package com.example.flaggameandroid.engagement

internal object EngagementTestSchedule {
  const val OneMinuteDelayMillis: Long = 60_000L

  fun triggerAtMillis(nowMillis: Long): Long = nowMillis + OneMinuteDelayMillis
}

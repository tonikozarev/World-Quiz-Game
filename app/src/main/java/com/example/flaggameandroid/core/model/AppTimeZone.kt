package com.example.flaggameandroid.core.model

import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

enum class AppTimeZone(
  val offsetHours: Int,
) {
  UtcMinus12(-12),
  UtcMinus11(-11),
  UtcMinus10(-10),
  UtcMinus9(-9),
  UtcMinus8(-8),
  UtcMinus7(-7),
  UtcMinus6(-6),
  UtcMinus5(-5),
  UtcMinus4(-4),
  UtcMinus3(-3),
  UtcMinus2(-2),
  UtcMinus1(-1),
  Utc(0),
  UtcPlus1(1),
  UtcPlus2(2),
  UtcPlus3(3),
  UtcPlus4(4),
  UtcPlus5(5),
  UtcPlus6(6),
  UtcPlus7(7),
  UtcPlus8(8),
  UtcPlus9(9),
  UtcPlus10(10),
  UtcPlus11(11),
  UtcPlus12(12),
  UtcPlus13(13),
  UtcPlus14(14);

  val label: String
    get() =
      when {
        offsetHours == 0 -> "UTC"
        offsetHours > 0 -> "UTC+$offsetHours"
        else -> "UTC$offsetHours"
      }

  val zoneId: ZoneId
    get() = ZoneOffset.ofHours(offsetHours)

  companion object {
    fun default(): AppTimeZone {
      val currentOffsetHours = ZonedDateTime.now().offset.totalSeconds / 3600
      return entries.firstOrNull { it.offsetHours == currentOffsetHours } ?: Utc
    }

    fun fromNameOrDefault(name: String?): AppTimeZone =
      entries.firstOrNull { it.name == name } ?: default()
  }
}

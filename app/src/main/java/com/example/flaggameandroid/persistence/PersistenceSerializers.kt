package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.MedalTier
import com.example.flaggameandroid.core.model.RatingsProgress

internal fun RatingsProgress.serialize(): String =
  buildList {
    add("bronze=$bronzeCount")
    add("silver=$silverCount")
    add("gold=$goldCount")
    add("titanium=$titaniumCount")
    add("diamond=$diamondCount")
    add("streak7Count=$streak7Count")
    add("streak30Count=$streak30Count")
    add("streak7ProgressDays=$streak7ProgressDays")
    add("streak30ProgressDays=$streak30ProgressDays")
  }.joinToString(separator = "|")

internal fun String.toRatingsProgress(): RatingsProgress {
  if (isBlank()) return RatingsProgress()
  val values =
    split("|")
      .mapNotNull { token ->
        val parts = token.split("=")
        if (parts.size == 2) {
          parts[0] to parts[1].toIntOrNull()
        } else {
          null
        }
      }.toMap()
  return RatingsProgress(
    bronzeCount = values["bronze"] ?: 0,
    silverCount = values["silver"] ?: 0,
    goldCount = values["gold"] ?: 0,
    titaniumCount = values["titanium"] ?: 0,
    diamondCount = values["diamond"] ?: 0,
    streak7Count = values["streak7Count"] ?: 0,
    streak30Count = values["streak30Count"] ?: 0,
    streak7ProgressDays = values["streak7ProgressDays"] ?: 0,
    streak30ProgressDays = values["streak30ProgressDays"] ?: 0,
  )
}

internal fun AchievementsProgress.serialize(): String =
  unlockedAtEpochMillisById.entries
    .sortedBy { it.key.name }
    .joinToString(separator = "|") { "${it.key.name}=${it.value}" }

internal fun String.toAchievementsProgress(): AchievementsProgress {
  if (isBlank()) return AchievementsProgress()
  val unlocked =
    split("|")
      .mapNotNull { token ->
        val parts = token.split("=")
        if (parts.size != 2) return@mapNotNull null
        val achievementId = AchievementId.entries.firstOrNull { it.name == parts[0] } ?: return@mapNotNull null
        val unlockedAt = parts[1].toLongOrNull() ?: return@mapNotNull null
        achievementId to unlockedAt
      }.toMap()
  return AchievementsProgress(unlockedAtEpochMillisById = unlocked)
}

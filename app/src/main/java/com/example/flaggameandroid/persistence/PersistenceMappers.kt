package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.DailyChallengeTheme
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.feature.app.AppLanguage

internal fun ProgressEntity.toPersistedAppState(hintDifficultyName: String): PersistedAppState =
  PersistedAppState(
    hintDifficulty = enumValueOf<HintDifficulty>(hintDifficultyName),
    hintCount = hintCount,
    level = level,
    hintsTowardNextLevel = hintsTowardNextLevel,
    correctAnswersTowardNextLevel = correctAnswersTowardNextLevel,
    eligibleQuizzesTowardNextLevel = eligibleQuizzesTowardNextLevel,
    lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
    lastPlayedAtEpochMillis = lastPlayedAtEpochMillis,
    inactiveIconActive = inactiveIconActive,
    ratings = ratingsSerialized.toRatingsProgress(),
    achievements = achievementUnlocksSerialized.toAchievementsProgress(),
    countryPracticeStats = countryPracticeSerialized.toCountryPracticeStats(),
    activityCalendar = activityCalendarSerialized.toActivityCalendar(),
    dailyChallengeCache = dailyChallengeSerialized.toDailyChallengeCache(),
    accountName = accountName,
    avatarIndex = avatarIndex,
    language = AppLanguage.entries.firstOrNull { it.name == languageName } ?: AppLanguage.English,
    mistakeReviewUnlocked = mistakeReviewUnlocked,
  )

internal fun PersistedAppState.toProgressEntity(): ProgressEntity =
  ProgressEntity(
    hintCount = hintCount,
    level = level,
    hintsTowardNextLevel = hintsTowardNextLevel,
    correctAnswersTowardNextLevel = correctAnswersTowardNextLevel,
    eligibleQuizzesTowardNextLevel = eligibleQuizzesTowardNextLevel,
    lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
    lastPlayedAtEpochMillis = lastPlayedAtEpochMillis,
    inactiveIconActive = inactiveIconActive,
    ratingsSerialized = ratings.serialize(),
    achievementUnlocksSerialized = achievements.serialize(),
    countryPracticeSerialized = countryPracticeStats.serializeCountryPracticeStats(),
    activityCalendarSerialized = activityCalendar.serializeActivityCalendar(),
    dailyChallengeSerialized = dailyChallengeCache.serializeDailyChallengeCache(),
    accountName = accountName,
    avatarIndex = avatarIndex,
    languageName = language.name,
    mistakeReviewUnlocked = mistakeReviewUnlocked,
  )

internal fun PersistedQuizHistory.toEntity(): QuizHistoryEntity =
  QuizHistoryEntity(
    mode = mode.name,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    skippedAnswers = skippedAnswers,
    netScore = netScore,
    completedAtEpochMillis = completedAtEpochMillis,
  )

private fun Map<String, CountryPracticeStats>.serializeCountryPracticeStats(): String =
  entries.joinToString(separator = ";") { (code, stats) ->
    listOf(
      code,
      stats.correctCount.toString(),
      stats.wrongCount.toString(),
      stats.lastMissedAtEpochMillis.toString(),
      if (stats.favorite) "1" else "0",
    ).joinToString(separator = ",")
  }

private fun String.toCountryPracticeStats(): Map<String, CountryPracticeStats> =
  if (isBlank()) {
    emptyMap()
  } else {
    split(";")
      .mapNotNull { row ->
        val parts = row.split(",")
        if (parts.size < 5) return@mapNotNull null
        val code = parts[0]
        code to
          CountryPracticeStats(
            correctCount = parts[1].toIntOrNull() ?: 0,
            wrongCount = parts[2].toIntOrNull() ?: 0,
            lastMissedAtEpochMillis = parts[3].toLongOrNull() ?: 0L,
            favorite = parts[4] == "1",
          )
      }
      .toMap()
  }

private fun Map<Long, ActivityDayRecord>.serializeActivityCalendar(): String =
  entries.joinToString(separator = ";") { (dayKey, record) ->
    listOf(
      dayKey.toString(),
      record.quizzesCompleted.toString(),
      if (record.dailyChallengeCompleted) "1" else "0",
      record.lastUpdatedAtEpochMillis.toString(),
      record.streakStartDayKey?.toString().orEmpty(),
      record.lastActiveDayKey?.toString().orEmpty(),
    ).joinToString(separator = ",")
  }

private fun String.toActivityCalendar(): Map<Long, ActivityDayRecord> =
  if (isBlank()) {
    emptyMap()
  } else {
    split(";")
      .mapNotNull { row ->
        val parts = row.split(",")
        if (parts.size < 4) return@mapNotNull null
        val dayKey = parts[0].toLongOrNull() ?: return@mapNotNull null
        dayKey to
          ActivityDayRecord(
            dayKey = dayKey,
            quizzesCompleted = parts[1].toIntOrNull() ?: 0,
            dailyChallengeCompleted = parts[2] == "1",
            lastUpdatedAtEpochMillis = parts[3].toLongOrNull() ?: 0L,
            streakStartDayKey = parts.getOrNull(4)?.takeIf { it.isNotBlank() }?.toLongOrNull(),
            lastActiveDayKey = parts.getOrNull(5)?.takeIf { it.isNotBlank() }?.toLongOrNull(),
          )
      }
      .toMap()
  }

private fun DailyChallengeCache?.serializeDailyChallengeCache(): String =
  this?.let { cache ->
    listOf(
      cache.dayKey.toString(),
      cache.theme.name,
      cache.questionCount.toString(),
      cache.seed.toString(),
      if (cache.completed) "1" else "0",
      cache.completedAtEpochMillis.toString(),
    ).joinToString(separator = ",")
  }.orEmpty()

private fun String.toDailyChallengeCache(): DailyChallengeCache? {
  if (isBlank()) return null
  val parts = split(",")
  if (parts.size < 4) return null
  val dayKey = parts[0].toLongOrNull() ?: return null
  val theme = DailyChallengeTheme.entries.firstOrNull { it.name == parts[1] } ?: DailyChallengeTheme.World
  return DailyChallengeCache(
    dayKey = dayKey,
    theme = theme,
    questionCount = parts[2].toIntOrNull() ?: 10,
    seed = parts[3].toLongOrNull() ?: 0L,
    completed = parts.getOrNull(4) == "1",
    completedAtEpochMillis = parts.getOrNull(5)?.toLongOrNull() ?: 0L,
  )
}

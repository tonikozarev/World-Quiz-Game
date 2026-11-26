package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.feature.app.AppLanguage
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PersistenceMappersTest {
  @Test
  fun progressEntity_roundTripsThroughPersistedState() {
    val original =
      PersistedAppState(
        hintDifficulty = HintDifficulty.Hard,
        hintCount = 7.0,
        level = 4,
        hintsTowardNextLevel = 3,
        correctAnswersTowardNextLevel = 19,
        eligibleQuizzesTowardNextLevel = 2,
        lastOpenedAtEpochMillis = 1234L,
        lastPlayedAtEpochMillis = 5678L,
        ratings = RatingsProgress(
          bronzeCount = 2,
          goldCount = 1,
          streak7Count = 3,
          streak30Count = 1,
          streak7ProgressDays = 4,
          streak30ProgressDays = 12,
        ),
        achievements =
          AchievementsProgress(
            unlockedAtEpochMillisById =
              mapOf(
                AchievementId.FirstPerfect to 111L,
                AchievementId.WorldPurist to 222L,
              ),
          ),
        countryPracticeStats =
          mapOf(
            "DE" to CountryPracticeStats(favoriteCountry = true),
            "FR" to CountryPracticeStats(favoriteCapital = true),
            "BG" to CountryPracticeStats(favoriteCountry = true, favoriteCapital = true),
          ),
        accountName = "Tony",
        avatarIndex = 12,
        language = AppLanguage.German,
      )

    val entity = original.toProgressEntity()
    val restored = entity.toPersistedAppState(hintDifficultyName = original.hintDifficulty.name)

    assertEquals(original.hintDifficulty, restored.hintDifficulty)
    assertEquals(original.hintCount, restored.hintCount)
    assertEquals(original.level, restored.level)
    assertEquals(original.hintsTowardNextLevel, restored.hintsTowardNextLevel)
    assertEquals(original.correctAnswersTowardNextLevel, restored.correctAnswersTowardNextLevel)
    assertEquals(original.eligibleQuizzesTowardNextLevel, restored.eligibleQuizzesTowardNextLevel)
    assertEquals(original.lastOpenedAtEpochMillis, restored.lastOpenedAtEpochMillis)
    assertEquals(original.lastPlayedAtEpochMillis, restored.lastPlayedAtEpochMillis)
    assertEquals(original.ratings, restored.ratings)
    assertEquals(original.achievements, restored.achievements)
    assertEquals(original.countryPracticeStats, restored.countryPracticeStats)
    assertEquals(original.accountName, restored.accountName)
    assertEquals(original.avatarIndex, restored.avatarIndex)
    assertEquals(original.language, restored.language)
  }

  @Test
  fun legacyCountryPracticeData_preservesStatsAndClearsFavorites() {
    val legacyEntity =
      PersistedAppState()
        .toProgressEntity()
        .copy(countryPracticeSerialized = "DE,4,2,1,3,99,1;BG,7,5,123,1")

    val restored = legacyEntity.toPersistedAppState(HintDifficulty.Medium.name)
    val stats = restored.countryPracticeStats.getValue("DE")

    assertEquals(4, stats.correctCount)
    assertEquals(2, stats.wrongCount)
    assertEquals(1, stats.capitalCorrectCount)
    assertEquals(3, stats.capitalWrongCount)
    assertEquals(99L, stats.lastMissedAtEpochMillis)
    assertEquals(false, stats.favoriteCountry)
    assertEquals(false, stats.favoriteCapital)

    val oldestStats = restored.countryPracticeStats.getValue("BG")
    assertEquals(7, oldestStats.correctCount)
    assertEquals(5, oldestStats.wrongCount)
    assertEquals(0, oldestStats.capitalCorrectCount)
    assertEquals(0, oldestStats.capitalWrongCount)
    assertEquals(123L, oldestStats.lastMissedAtEpochMillis)
    assertEquals(false, oldestStats.favoriteCountry)
    assertEquals(false, oldestStats.favoriteCapital)
  }

  @Test
  fun quizHistoryEntity_usesTheExpectedFieldMapping() {
    val history =
      PersistedQuizHistory(
        mode = GameMode.CreateQuiz,
        totalQuestions = 44,
        correctAnswers = 44,
        skippedAnswers = 0,
        netScore = 44,
        completedAtEpochMillis = 999L,
      )

    val entity = history.toEntity()

    assertEquals("CreateQuiz", entity.mode)
    assertEquals(44, entity.totalQuestions)
    assertEquals(44, entity.correctAnswers)
    assertEquals(0, entity.skippedAnswers)
    assertEquals(44, entity.netScore)
    assertEquals(999L, entity.completedAtEpochMillis)
  }
}

package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.RatingsProgress
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FlagGameProgressStateReducersTest {
  @Test
  fun withTestingLevelUp_incrementsLevelAndAddsFiveHints() {
    val state =
      FlagGameUiState(
        hintCount = 7.0,
        levelProgress = LevelProgressState(level = 2, hintsTowardNextLevel = 3),
      )

    val updated = state.withTestingLevelUp()

    assertEquals(3, updated.levelProgress.level)
    assertTrue(updated.levelProgress.levelUpVisible)
    assertEquals(12.0, updated.hintCount)
    assertEquals(12.0, updated.quiz.currentPlayer.hintPoints)
  }

  @Test
  fun withMedalsReset_clearsOnlyMedals() {
    val state =
      FlagGameUiState(
        ratings = RatingsProgress(bronzeCount = 4, silverCount = 2, streak7Count = 1, streak30ProgressDays = 8),
        achievements =
          AchievementsProgress(
            unlockedAtEpochMillisById =
              mapOf(
                AchievementId.FirstPerfect to 100L,
              ),
          ),
      )

    val updated = state.withMedalsReset()

    assertEquals(0, updated.ratings.bronzeCount)
    assertEquals(0, updated.ratings.silverCount)
    assertEquals(0, updated.ratings.streak7Count)
    assertEquals(0, updated.ratings.streak30ProgressDays)
    assertTrue(updated.achievements.isUnlocked(AchievementId.FirstPerfect))
  }

  @Test
  fun progressFraction_uses33_34_33Weighting() {
    val hintsOnly =
      LevelProgressState(
        level = 1,
        hintsTowardNextLevel = 10,
        correctAnswersTowardNextLevel = 0,
        eligibleQuizzesTowardNextLevel = 0,
      )
    val correctOnly =
      LevelProgressState(
        level = 1,
        hintsTowardNextLevel = 0,
        correctAnswersTowardNextLevel = 100,
        eligibleQuizzesTowardNextLevel = 0,
      )
    val quizzesOnly =
      LevelProgressState(
        level = 1,
        hintsTowardNextLevel = 0,
        correctAnswersTowardNextLevel = 0,
        eligibleQuizzesTowardNextLevel = 10,
      )
    val full =
      LevelProgressState(
        level = 1,
        hintsTowardNextLevel = 10,
        correctAnswersTowardNextLevel = 100,
        eligibleQuizzesTowardNextLevel = 10,
      )

    assertEquals(0.33f, hintsOnly.progressFraction, 0.01f)
    assertEquals(0.34f, correctOnly.progressFraction, 0.01f)
    assertEquals(0.33f, quizzesOnly.progressFraction, 0.01f)
    assertEquals(1f, full.progressFraction, 0.0001f)
  }

  @Test
  fun levelProgress_displayValues_areClampedToCurrentRequirement() {
    val progress =
      LevelProgressState(
        level = 2,
        hintsTowardNextLevel = 26,
        correctAnswersTowardNextLevel = 260,
        eligibleQuizzesTowardNextLevel = 26,
      )

    assertEquals(15, progress.hintsTowardNextLevelDisplay)
    assertEquals(150, progress.correctAnswersTowardNextLevelDisplay)
    assertEquals(15, progress.eligibleQuizzesTowardNextLevelDisplay)
  }

  @Test
  fun advanceLevelProgress_capsOverflowWhenNotEnoughForLevelUp() {
    val result =
      advanceLevelProgress(
        progress =
          LevelProgressState(
            level = 2,
            hintsTowardNextLevel = 14,
            correctAnswersTowardNextLevel = 260,
            eligibleQuizzesTowardNextLevel = 26,
          ),
        earnedHints = 0,
        correctAnswers = 0,
        eligibleQuizCompletions = 0,
      )

    assertEquals(2, result.progress.level)
    assertEquals(14, result.progress.hintsTowardNextLevel)
    assertEquals(150, result.progress.correctAnswersTowardNextLevel)
    assertEquals(15, result.progress.eligibleQuizzesTowardNextLevel)
  }
}

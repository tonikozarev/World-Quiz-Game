package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.RatingsProgress
import kotlin.random.Random

internal fun FlagGameUiState.withLevelUpSeen(): FlagGameUiState =
  copy(levelProgress = levelProgress.copy(levelUpVisible = false))

internal fun FlagGameUiState.withTestingToolsVisibleChanged(visible: Boolean): FlagGameUiState =
  copy(settings = settings.copy(testingToolsVisible = visible))

internal fun FlagGameUiState.withResetHints(): FlagGameUiState =
  copy(
    hintCount = 0.0,
    quiz = quiz.copy(players = quiz.players.map { player -> player.copy(hintPoints = 0.0) }),
  )

internal fun FlagGameUiState.withAddedTestingHints(): FlagGameUiState {
  val newHintCount = hintCount + 10.0
  return copy(
    hintCount = newHintCount,
    quiz = quiz.copy(players = quiz.players.map { player -> player.copy(hintPoints = newHintCount) }),
  )
}

internal fun FlagGameUiState.withTestingLevelUp(): FlagGameUiState {
  val nextLevel = (levelProgress.level + 1).coerceAtMost(ProgressionRules.MaxLevel)
  val newHintCount = hintCount + 5.0
  return copy(
    levelProgress =
      levelProgress.copy(
        level = nextLevel,
        levelUpVisible = nextLevel > levelProgress.level,
      ),
    hintCount = newHintCount,
    quiz = quiz.copy(players = quiz.players.map { player -> player.copy(hintPoints = newHintCount) }),
  )
}

internal fun FlagGameUiState.withTestingLevelReset(): FlagGameUiState =
  copy(
    levelProgress =
      levelProgress.copy(
        level = 1,
        levelUpVisible = false,
      ),
  )

internal fun FlagGameUiState.withRandomAchievementUnlocked(random: Random): FlagGameUiState {
  val lockedAchievement = AchievementId.entries.filterNot(achievements::isUnlocked).randomOrNull(random)
  return if (lockedAchievement == null) {
    this
  } else {
    copy(achievements = achievements.unlock(lockedAchievement, System.currentTimeMillis()))
  }
}

internal fun FlagGameUiState.withAllAchievementsLocked(): FlagGameUiState =
  copy(achievements = AchievementsProgress())

internal fun FlagGameUiState.withMedalsReset(): FlagGameUiState =
  copy(ratings = RatingsProgress())

internal fun FlagGameUiState.withInactiveIconActive(active: Boolean): FlagGameUiState =
  copy(inactiveIconActive = active)

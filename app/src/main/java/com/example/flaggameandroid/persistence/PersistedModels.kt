package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty

data class PersistedAppState(
  val hintDifficulty: HintDifficulty = HintDifficulty.Medium,
  val hintCount: Int = 0,
  val level: Int = 1,
  val hintsTowardNextLevel: Int = 0,
  val correctAnswersTowardNextLevel: Int = 0,
  val eligibleQuizzesTowardNextLevel: Int = 0,
)

data class PersistedQuizHistory(
  val mode: GameMode,
  val totalQuestions: Int,
  val correctAnswers: Int,
  val skippedAnswers: Int,
  val netScore: Int,
  val completedAtEpochMillis: Long,
)

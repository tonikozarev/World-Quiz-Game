package com.example.flaggameandroid.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
  @PrimaryKey val id: Int = SingletonId,
  val hintCount: Int,
  val level: Int,
  val hintsTowardNextLevel: Int,
  val correctAnswersTowardNextLevel: Int,
  val eligibleQuizzesTowardNextLevel: Int,
) {
  fun toPersistedAppState(hintDifficultyName: String): PersistedAppState =
    PersistedAppState(
      hintDifficulty = enumValueOf(hintDifficultyName),
      hintCount = hintCount,
      level = level,
      hintsTowardNextLevel = hintsTowardNextLevel,
      correctAnswersTowardNextLevel = correctAnswersTowardNextLevel,
      eligibleQuizzesTowardNextLevel = eligibleQuizzesTowardNextLevel,
    )

  companion object {
    const val SingletonId: Int = 1
  }
}

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val mode: String,
  val totalQuestions: Int,
  val correctAnswers: Int,
  val skippedAnswers: Int,
  val netScore: Int,
  val completedAtEpochMillis: Long,
)

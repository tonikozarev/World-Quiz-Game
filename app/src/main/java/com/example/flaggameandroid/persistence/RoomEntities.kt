package com.example.flaggameandroid.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flaggameandroid.feature.app.AppLanguage

@Entity(tableName = "progress")
data class ProgressEntity(
  @PrimaryKey val id: Int = SingletonId,
  val hintCount: Double,
  val level: Int,
  val hintsTowardNextLevel: Int,
  val correctAnswersTowardNextLevel: Int,
  val eligibleQuizzesTowardNextLevel: Int,
  val lastOpenedAtEpochMillis: Long = 0L,
  val lastPlayedAtEpochMillis: Long = 0L,
  val inactiveIconActive: Boolean = false,
  val ratingsSerialized: String = "",
  val achievementUnlocksSerialized: String = "",
  val countryPracticeSerialized: String = "",
  val activityCalendarSerialized: String = "",
  val dailyChallengeSerialized: String = "",
  val savedQuizTemplatesSerialized: String = "",
  val accountName: String = "",
  val avatarIndex: Int = 0,
  val languageName: String = AppLanguage.English.name,
  val mistakeReviewUnlocked: Boolean = false,
) {
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

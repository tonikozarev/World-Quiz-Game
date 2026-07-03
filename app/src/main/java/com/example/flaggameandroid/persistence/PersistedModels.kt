package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.SavedQuizTemplate
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.feature.app.AppLanguage

data class PersistedAppState(
  val hintDifficulty: HintDifficulty = HintDifficulty.Medium,
  val reminderEnabled: Boolean = true,
  val hintCount: Double = 0.0,
  val level: Int = 1,
  val hintsTowardNextLevel: Int = 0,
  val correctAnswersTowardNextLevel: Int = 0,
  val eligibleQuizzesTowardNextLevel: Int = 0,
  val lastOpenedAtEpochMillis: Long = 0L,
  val lastPlayedAtEpochMillis: Long = 0L,
  val inactiveIconActive: Boolean = false,
  val ratings: RatingsProgress = RatingsProgress(),
  val achievements: AchievementsProgress = AchievementsProgress(),
  val countryPracticeStats: Map<String, CountryPracticeStats> = emptyMap(),
  val activityCalendar: Map<Long, ActivityDayRecord> = emptyMap(),
  val dailyChallengeCaches: Map<QuizTopic, DailyChallengeCache> = emptyMap(),
  val savedQuizTemplates: List<SavedQuizTemplate> = emptyList(),
  val accountName: String = "",
  val avatarIndex: Int = 0,
  val language: AppLanguage = AppLanguage.English,
  val mistakeReviewUnlocked: Boolean = false,
) {
  val dailyChallengeCache: DailyChallengeCache?
    get() = dailyChallengeCaches[QuizTopic.Countries]
}

data class PersistedQuizHistory(
  val mode: GameMode,
  val totalQuestions: Int,
  val correctAnswers: Int,
  val skippedAnswers: Int,
  val netScore: Int,
  val completedAtEpochMillis: Long,
)

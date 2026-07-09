package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.core.model.MistakeReviewUnlockCountryCount
import com.example.flaggameandroid.core.model.SavedQuizTemplate

internal data class QuizCompletionSummary(
  val completionTime: Long,
  val completedResults: List<QuestionResult>,
  val correctAnswers: Int,
  val distinctCountries: Int,
  val scoredPlayers: List<PlayerProgress>,
  val releasedHints: Int,
  val quizWithResults: QuizState,
  val updatedRatings: RatingsProgress,
  val updatedAchievements: com.example.flaggameandroid.core.model.AchievementsProgress,
  val finalProgress: LevelProgressState,
  val totalBonusHints: Int,
  val finalPlayers: List<PlayerProgress>,
  val updatedCountryPracticeStats: Map<String, com.example.flaggameandroid.core.model.CountryPracticeStats>,
  val updatedMistakeReviewUnlocked: Boolean,
  val updatedActivityCalendar: Map<Long, ActivityDayRecord>,
  val updatedDailyChallengeCaches: Map<QuizTopic, com.example.flaggameandroid.core.model.DailyChallengeCache>,
  val updatedSavedQuizTemplates: List<SavedQuizTemplate>,
)

internal data class QuizCompletionResult(
  val uiState: FlagGameUiState,
  val summary: QuizCompletionSummary,
)

internal fun buildQuizCompletionSummary(
  state: FlagGameUiState,
  quiz: QuizState,
  countries: List<FlagCountry>,
  completionTime: Long,
): QuizCompletionSummary {
  val completedResults = buildQuizResults(quiz, state.settings.language)
  val correctAnswers = completedResults.count { it.countsAsCorrect }
  val distinctCountries = completedResults.map { it.question.correctCountry.code }.distinct().size
  val scoredPlayers = scorePlayersFromResults(quiz.players, completedResults, state.settings.hintDifficulty, quiz.mode != GameMode.LocalMultiplayer)
  val releasedHints =
    if (quiz.mode == GameMode.LocalMultiplayer) {
      0
    } else {
      scoredPlayers.sumOf { it.earnedHintPoints }
    }
  val quizWithResults = quiz.copy(results = completedResults)
  val shouldProgressLevel = quiz.mode == GameMode.WorldFlags
  val eligibleQuizCompletions = if (shouldProgressLevel && completedResults.size >= 10) 1 else 0
  val progressResult =
    if (shouldProgressLevel) {
      advanceLevelProgress(
        progress = state.levelProgress,
        earnedHints = releasedHints,
        correctAnswers = correctAnswers,
        eligibleQuizCompletions = eligibleQuizCompletions,
      )
    } else {
      LevelProgressResult(
        progress = state.levelProgress,
        bonusHints = 0,
      )
    }
  val finalProgress = progressResult.progress
  val updatedRatings =
    awardMedalIfEligible(
      ratings = state.ratings,
      quiz = quizWithResults,
      completedResults = completedResults,
      distinctCountries = distinctCountries,
      totalCatalogCountries = countries.size,
    )
  val updatedAchievements =
    awardAchievementsIfEligible(
      achievements = state.achievements,
      ratings = updatedRatings,
      quiz = quizWithResults,
      completedResults = completedResults,
      distinctCountries = distinctCountries,
      completedAtEpochMillis = completionTime,
      totalCatalogCountries = countries.size,
      availableCountriesForSelectedContinent =
        quiz.selectedContinents.singleOrNull()?.let { selectedContinent ->
          countries.count { it.continent == selectedContinent }
        } ?: 0,
    )
  val totalBonusHints = progressResult.bonusHints
  val newHintCount = state.hintCount + releasedHints + totalBonusHints
  val finalPlayers =
    scoredPlayers.map { player -> player.copy(hintPoints = newHintCount) }
  val updatedCountryPracticeStats =
    updateCountryPracticeStats(
      previous = state.countryPracticeStats,
      results = completedResults,
      completedAtEpochMillis = completionTime,
      mode = quiz.mode ?: GameMode.Training,
    )
  val updatedMistakeReviewUnlocked =
    state.mistakeReviewUnlocked ||
      mistakeReviewEligibleCountryCount(updatedCountryPracticeStats, QuizTopic.Mixed) >= MistakeReviewUnlockCountryCount
  val updatedActivityCalendar =
    updateActivityCalendar(
      previous = state.activityCalendar,
      completedAtEpochMillis = completionTime,
      mode = quiz.mode ?: GameMode.Training,
    )
  val updatedRatingsWithStreaks =
    awardStreakMedalsIfEligible(
      ratings = updatedRatings,
      previousActivityCalendar = state.activityCalendar,
      updatedActivityCalendar = updatedActivityCalendar,
      completedAtEpochMillis = completionTime,
    )
  val updatedDailyChallengeCaches =
    if (quiz.mode == GameMode.DailyChallenge) {
      state.dailyChallengeCaches.withDailyChallengeCache(
        state.dailyChallengeCache?.copy(
          completed = true,
          completedAtEpochMillis = completionTime,
        )
      )
    } else {
      state.dailyChallengeCaches
    }
  val updatedSavedQuizTemplates =
    if (quiz.savedQuizTemplateId == null) {
      state.savedQuizTemplates
    } else {
      state.savedQuizTemplates.map { template ->
        if (template.id == quiz.savedQuizTemplateId) {
          template.copy(completionCount = template.completionCount + 1)
        } else {
          template
        }
      }
    }

  return QuizCompletionSummary(
    completionTime = completionTime,
    completedResults = completedResults,
    correctAnswers = correctAnswers,
    distinctCountries = distinctCountries,
    scoredPlayers = scoredPlayers,
    releasedHints = releasedHints,
    quizWithResults = quizWithResults,
    updatedRatings = updatedRatingsWithStreaks,
    updatedAchievements = updatedAchievements,
    finalProgress = finalProgress,
    totalBonusHints = totalBonusHints,
    finalPlayers = finalPlayers,
    updatedCountryPracticeStats = updatedCountryPracticeStats,
    updatedMistakeReviewUnlocked = updatedMistakeReviewUnlocked,
    updatedActivityCalendar = updatedActivityCalendar,
    updatedDailyChallengeCaches = updatedDailyChallengeCaches,
    updatedSavedQuizTemplates = updatedSavedQuizTemplates,
  )
}

internal fun buildQuizCompletionResult(
  state: FlagGameUiState,
  quiz: QuizState,
  countries: List<FlagCountry>,
  completionTime: Long,
): QuizCompletionResult {
  val summary = buildQuizCompletionSummary(state, quiz, countries, completionTime)
  return QuizCompletionResult(
    uiState =
      state.copy(
        screen = AppScreen.Results,
        quiz =
          summary.quizWithResults.copy(
            players = summary.finalPlayers,
            results = summary.completedResults,
          ),
        hintCount = state.hintCount + summary.releasedHints + summary.totalBonusHints,
        ratings = summary.updatedRatings,
        achievements = summary.updatedAchievements,
        levelProgress = summary.finalProgress,
        lastPlayedAtEpochMillis = summary.completionTime,
        inactiveIconActive = false,
        countryPracticeStats = summary.updatedCountryPracticeStats,
        mistakeReviewUnlocked = summary.updatedMistakeReviewUnlocked,
        activityCalendar = summary.updatedActivityCalendar,
        dailyChallengeCaches = summary.updatedDailyChallengeCaches,
        savedQuizTemplates = summary.updatedSavedQuizTemplates,
      ),
    summary = summary,
  )
}

internal fun awardStreakMedalsIfEligible(
  ratings: RatingsProgress,
  previousActivityCalendar: Map<Long, ActivityDayRecord>,
  updatedActivityCalendar: Map<Long, ActivityDayRecord>,
  completedAtEpochMillis: Long,
  timeZone: com.example.flaggameandroid.core.model.AppTimeZone = com.example.flaggameandroid.core.model.AppTimeZone.Utc,
): RatingsProgress {
  val dayKey = localDayKey(completedAtEpochMillis, timeZone)
  if ((previousActivityCalendar[dayKey]?.quizzesCompleted ?: 0) > 0) {
    return ratings
  }

  val consecutiveStreak =
    (previousActivityCalendar[dayKey - 1]?.quizzesCompleted ?: 0) > 0

  val next7Progress = if (consecutiveStreak) ratings.streak7ProgressDays + 1 else 1
  val next30Progress = if (consecutiveStreak) ratings.streak30ProgressDays + 1 else 1

  return ratings.withStreakProgress(
    streak7ProgressDays = if (next7Progress >= 7) 0 else next7Progress,
    streak30ProgressDays = if (next30Progress >= 30) 0 else next30Progress,
    streak7Count = ratings.streak7Count + if (next7Progress >= 7) 1 else 0,
    streak30Count = ratings.streak30Count + if (next30Progress >= 30) 1 else 0,
  )
}

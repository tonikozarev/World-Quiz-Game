package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.RatingsProgress

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
  val correctAnswers = completedResults.count { it.isCorrect }
  val distinctCountries = completedResults.map { it.question.correctCountry.code }.distinct().size
  val scoredPlayers = scorePlayersFromResults(quiz.players, completedResults, state.settings.hintDifficulty, quiz.mode != GameMode.LocalMultiplayer)
  val releasedHints =
    if (quiz.mode == GameMode.LocalMultiplayer) {
      0
    } else {
      scoredPlayers.sumOf { it.earnedHintPoints }
    }
  val quizWithResults = quiz.copy(results = completedResults)
  val qualifiesForPerfectNoBluffLevel =
    quiz.mode == GameMode.AllIn &&
      quiz.allInType == AllInType.NoBluffAllTough &&
      quiz.variants.size == QuizVariant.entries.size &&
      completedResults.isNotEmpty() &&
      completedResults.all { it.isCorrect }
  val perfectNoBluffLevelGain =
    if (qualifiesForPerfectNoBluffLevel) {
      if (state.settings.hintDifficulty == HintDifficulty.Impossible) 2 else 1
    } else {
      0
    }
  val shouldProgressLevel =
    (quiz.mode == GameMode.Continents || quiz.mode == GameMode.SpeedRun || quiz.mode == GameMode.AllIn) && !qualifiesForPerfectNoBluffLevel
  val eligibleQuizCompletions = if (shouldProgressLevel && completedResults.size >= 10) 1 else 0
  val progressResult =
    advanceLevelProgress(
      progress = state.levelProgress,
      earnedHints = if (shouldProgressLevel) releasedHints else 0,
      correctAnswers = if (shouldProgressLevel) correctAnswers else 0,
      eligibleQuizCompletions = eligibleQuizCompletions,
    )
  val finalProgress =
    if (qualifiesForPerfectNoBluffLevel) {
      val targetLevel = (state.levelProgress.level + perfectNoBluffLevelGain).coerceAtMost(ProgressionRules.MaxLevel)
      state.levelProgress.copy(
        level = targetLevel,
        hintsTowardNextLevel = 0,
        levelUpVisible = targetLevel > state.levelProgress.level,
      )
    } else {
      progressResult.progress
    }
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
  val noBluffBonusHints =
    if (qualifiesForPerfectNoBluffLevel) {
      val gainedLevels = (finalProgress.level - state.levelProgress.level).coerceAtLeast(0)
      gainedLevels * 5
    } else {
      0
    }
  val totalBonusHints = progressResult.bonusHints + noBluffBonusHints
  val newHintCount = state.hintCount + releasedHints + totalBonusHints
  val finalPlayers =
    scoredPlayers.map { player -> player.copy(hintPoints = newHintCount, earnedHintPoints = 0) }

  return QuizCompletionSummary(
    completionTime = completionTime,
    completedResults = completedResults,
    correctAnswers = correctAnswers,
    distinctCountries = distinctCountries,
    scoredPlayers = scoredPlayers,
    releasedHints = releasedHints,
    quizWithResults = quizWithResults,
    updatedRatings = updatedRatings,
    updatedAchievements = updatedAchievements,
    finalProgress = finalProgress,
    totalBonusHints = totalBonusHints,
    finalPlayers = finalPlayers,
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
      ),
    summary = summary,
  )
}

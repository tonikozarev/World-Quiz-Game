package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.DailyChallengeCache
import kotlin.random.Random
import com.example.flaggameandroid.core.model.MistakeReviewUnlockCountryCount

internal data class QuizStartResult(
  val quiz: QuizState? = null,
  val validationError: String? = null,
  val dailyChallengeCache: DailyChallengeCache? = null,
)

internal fun buildStartedQuizState(
  setup: SetupState,
  countries: List<FlagCountry>,
  questionGenerator: QuizQuestionGenerator,
  hintDifficulty: HintDifficulty,
  random: Random,
  hintCount: Int,
  displayName: String,
  practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
  dailyChallengeCache: DailyChallengeCache? = null,
  nowEpochMillis: Long = System.currentTimeMillis(),
  timeZone: AppTimeZone = AppTimeZone.default(),
): QuizState {
  val poolResolution =
    resolveQuizPool(
      setup = setup,
      countries = countries,
      practiceStats = practiceStats,
      dailyChallengeCache = dailyChallengeCache,
      nowEpochMillis = nowEpochMillis,
      timeZone = timeZone,
    )
  val pool = poolResolution.pool
  val config = configFor(setup, pool.size, hintDifficulty, random)
  val generator =
    if (setup.mode == GameMode.DailyChallenge) {
      QuizQuestionGenerator(Random(poolResolution.dailyChallengeCache?.seed ?: nowEpochMillis))
    } else {
      questionGenerator
    }
  val questions = generator.buildQuestions(pool, config, practiceStats)
  val players = config.players.map { PlayerProgress(name = it, hintPoints = hintCount) }
  val questionStates = List(questions.size) { QuestionDraftState() }

  return QuizState(
    mode = setup.mode,
    allInType = setup.allInType,
    variants = config.variants,
    selectedContinents = setup.selectedContinents,
    questions = questions,
    questionStates = questionStates,
    players = players,
    startedAtEpochMillis = System.currentTimeMillis(),
    poolSource = config.poolSource,
    dailyChallengeTheme = config.dailyChallengeTheme ?: poolResolution.dailyChallengeCache?.theme,
  ).loadQuestionDraft(0)
}

internal fun buildQuizStartResult(
  setup: SetupState,
  countries: List<FlagCountry>,
  questionGenerator: QuizQuestionGenerator,
  hintDifficulty: HintDifficulty,
  random: Random,
  hintCount: Int,
  displayName: String,
  practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
  dailyChallengeCache: DailyChallengeCache? = null,
  nowEpochMillis: Long = System.currentTimeMillis(),
  timeZone: AppTimeZone = AppTimeZone.default(),
  mistakeReviewUnlocked: Boolean = false,
): QuizStartResult {
  val poolResolution =
    resolveQuizPool(
      setup = setup,
      countries = countries,
      practiceStats = practiceStats,
      dailyChallengeCache = dailyChallengeCache,
      nowEpochMillis = nowEpochMillis,
      timeZone = timeZone,
    )
  if (setup.mode == GameMode.DailyChallenge && poolResolution.pool.isEmpty()) {
    return QuizStartResult(validationError = "Daily challenge already completed for today.")
  }
  if (setup.mode == GameMode.MistakeReview) {
    val eligibleCount = mistakeReviewEligibleCountryCount(practiceStats)
    if (!mistakeReviewUnlocked && eligibleCount < MistakeReviewUnlockCountryCount) {
      return QuizStartResult(validationError = "No missed countries to review yet.")
    }
  }
  if (setup.mode == GameMode.MistakeReview && poolResolution.pool.isEmpty()) {
    return QuizStartResult(validationError = "No missed countries to review yet.")
  }
  val validationError = validateSetup(setup) { poolResolution.pool }
  if (validationError != null) {
    return QuizStartResult(validationError = validationError)
  }

  return QuizStartResult(
    quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = questionGenerator,
        hintDifficulty = hintDifficulty,
        random = random,
        hintCount = hintCount,
        displayName = displayName,
        practiceStats = practiceStats,
        dailyChallengeCache = poolResolution.dailyChallengeCache,
        nowEpochMillis = nowEpochMillis,
        timeZone = timeZone,
      ),
    dailyChallengeCache = poolResolution.dailyChallengeCache,
  )
}

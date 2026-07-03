package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.DailyChallengeCache
import kotlin.random.Random
import com.example.flaggameandroid.core.model.MistakeReviewUnlockCountryCount
import com.example.flaggameandroid.core.data.QuizQuestionGenerator

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
  hintCount: Double,
  displayName: String,
  practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
  dailyChallengeCache: DailyChallengeCache? = null,
  nowEpochMillis: Long = System.currentTimeMillis(),
  timeZone: AppTimeZone = AppTimeZone.Utc,
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
  val config = configFor(setup, pool.size, hintDifficulty, random, practiceStats)
  val quizSeed =
    when (setup.mode) {
      GameMode.DailyChallenge -> poolResolution.dailyChallengeCache?.seed ?: nowEpochMillis
      GameMode.CreateQuiz -> setup.createQuizSeed.takeIf { it != 0L } ?: random.nextLong()
      else -> random.nextLong()
    }
  val generator = QuizQuestionGenerator(Random(quizSeed))
  val questions = generator.buildQuestions(pool, config, practiceStats, countries)
  val players = config.players.map { PlayerProgress(name = it, hintPoints = hintCount) }
  val questionStates = List(questions.size) { QuestionDraftState() }
  val countdownEnabled = config.countdownEnabled

  return QuizState(
    mode = config.mode,
    allInType = setup.allInType,
    variants = config.variants,
    topic = config.topic,
    selectedContinents = setup.selectedContinents,
    instantCorrectionEnabled = setup.instantCorrectionEnabled,
    hintsAllowed = !setup.usesCreateQuizManualHardcore,
    questions = questions,
    questionStates = questionStates,
    players = players,
    startedAtEpochMillis = System.currentTimeMillis(),
    speedRunSecondsPerAnswer = if (countdownEnabled) config.speedRunSecondsPerAnswer else 0,
    countdownEnabled = countdownEnabled,
    poolSource = config.poolSource,
    dailyChallengeTheme = config.dailyChallengeTheme ?: poolResolution.dailyChallengeCache?.theme,
    quizSeed = quizSeed,
    savedQuizTemplateId = setup.savedQuizTemplateId,
  ).loadQuestionDraft(0)
}

internal fun buildQuizStartResult(
  setup: SetupState,
  countries: List<FlagCountry>,
  questionGenerator: QuizQuestionGenerator,
  hintDifficulty: HintDifficulty,
  random: Random,
  hintCount: Double,
  displayName: String,
  practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
  dailyChallengeCache: DailyChallengeCache? = null,
  nowEpochMillis: Long = System.currentTimeMillis(),
  timeZone: AppTimeZone = AppTimeZone.Utc,
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
    val eligibleCount = mistakeReviewEligibleCountryCount(practiceStats, setup.topic)
    if (eligibleCount < MistakeReviewUnlockCountryCount) {
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

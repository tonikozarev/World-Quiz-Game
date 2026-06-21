package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizPoolSource
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.RatingsProgress
import kotlin.random.Random

internal fun QuizState.loadQuestionDraft(index: Int): QuizState {
  if (questions.isEmpty()) return this
  val clampedIndex = index.coerceIn(0, questions.lastIndex)
  val draft = questionStates.getOrElse(clampedIndex) { QuestionDraftState() }
  return copy(
    currentQuestionIndex = clampedIndex,
    currentPlayerIndex = if (players.isEmpty()) 0 else clampedIndex % players.size,
    selectedCountry = draft.selectedCountry,
    typedAnswer = draft.typedAnswer,
    hiddenOptionCodes = draft.hiddenOptionCodes,
    typedHintPrefix = draft.typedHintPrefix,
    hintUsedOnCurrentQuestion = draft.hintUses > 0,
    questionStates = questionStates,
  )
}

internal fun QuizState.previousAnsweredQuestionIndex(): Int? =
  (currentQuestionIndex - 1 downTo 0).firstOrNull {
    questionStates.getOrElse(it) { QuestionDraftState() }.status != QuestionStatus.Unanswered
  }

internal fun QuizState.nextAnsweredQuestionIndex(): Int? =
  ((currentQuestionIndex + 1)..questions.lastIndex).firstOrNull {
    questionStates.getOrElse(it) { QuestionDraftState() }.status != QuestionStatus.Unanswered
  }

internal fun QuizState.previewAdvanceQuestionStates(): List<QuestionDraftState> {
  val currentDraft = currentQuestionState
  val currentQuestion = currentQuestion
  return when {
    currentQuestionHasPendingAnswer ->
      questionStates.replaceAt(
        currentQuestionIndex,
        currentDraft.copy(
          status = QuestionStatus.Answered,
          locked = currentDraft.locked || mode == GameMode.Training,
        ),
      )

    currentQuestion?.variant == QuizVariant.TypeCountryName &&
      currentDraft.typedAnswer.isBlank() &&
      currentDraft.status != QuestionStatus.Answered ->
      questionStates.replaceAt(currentQuestionIndex, currentDraft.copy(status = QuestionStatus.Skipped))

    currentQuestion?.variant != QuizVariant.TypeCountryName && currentDraft.status == QuestionStatus.Unanswered ->
      questionStates.replaceAt(currentQuestionIndex, currentDraft.copy(status = QuestionStatus.Skipped))

    else -> questionStates
  }
}

internal fun QuizState.previewAdvanceTargetIndex(): Int? =
  (currentQuestionIndex + 1).takeIf { it <= questions.lastIndex }

internal fun QuizState.nextSkippedQuestionIndex(): Int? {
  val skippedIndices =
    questionStates.mapIndexedNotNull { index, questionState ->
      if (questionState.status == QuestionStatus.Skipped) index else null
    }
  if (skippedIndices.isEmpty()) return null
  return skippedIndices.firstOrNull { it > currentQuestionIndex } ?: skippedIndices.first()
}

internal fun buildQuizResults(
  quiz: QuizState,
  language: AppLanguage,
): List<QuestionResult> {
  val streakByPlayer = mutableMapOf<String, Int>()
  return quiz.questions.mapIndexedNotNull { index, question ->
    val draft = quiz.questionStates.getOrElse(index) { QuestionDraftState() }
    if (draft.status != QuestionStatus.Answered) return@mapIndexedNotNull null
    val playerName =
      if (quiz.players.isEmpty()) {
        "Solo"
      } else {
        quiz.players[index % quiz.players.size].name
      }
    val previousStreak = streakByPlayer[playerName] ?: 0
    val isCorrect =
      when (question.variant) {
        QuizVariant.TypeCountryName ->
          QuizAnswerChecker.isTypedAnswerCorrect(
            typedAnswer = draft.typedAnswer,
            acceptedAnswers = question.correctCountry.acceptedTypedAnswers(language),
          )

        QuizVariant.FlagToCountry,
        QuizVariant.CountryToFlag ->
          QuizAnswerChecker.isCountrySelectionCorrect(draft.selectedCountry, question.correctCountry)
      }
    val updatedStreak =
      when {
        !isCorrect || draft.hintUses >= 2 -> 0
        draft.hintUses == 1 -> previousStreak
        else -> previousStreak + 1
      }
    streakByPlayer[playerName] = updatedStreak

    QuestionResult(
      question = question,
      playerName = playerName,
      selectedCountry = draft.selectedCountry,
      typedAnswer = draft.typedAnswer,
      isCorrect = isCorrect,
      hintUsed = draft.hintUsed || draft.hintUses > 0,
      hintUses = draft.hintUses,
      hintStreak = updatedStreak,
    )
  }
}

internal fun scorePlayersFromResults(
  players: List<PlayerProgress>,
  results: List<QuestionResult>,
  hintDifficulty: HintDifficulty,
  canEarnHints: Boolean,
): List<PlayerProgress> {
  if (players.isEmpty()) return emptyList()
  val scored = players.map { it.copy(score = 0, correctStreak = 0, earnedHintPoints = 0) }.toMutableList()
  results.forEach { result ->
    val index = scored.indexOfFirst { it.name == result.playerName }
    if (index == -1) return@forEach
    scored[index] =
      scored[index].afterAnswer(
        isCorrect = result.isCorrect,
        hintUses = result.hintUses,
        hintDifficulty = hintDifficulty,
        canEarnHints = canEarnHints,
      )
  }
  return scored
}

internal fun <T> List<T>.replaceAt(
  index: Int,
  value: T,
): List<T> = mapIndexed { currentIndex, currentValue -> if (currentIndex == index) value else currentValue }

internal fun validateSetup(
  setup: SetupState,
  countryPoolFor: (SetupState) -> List<FlagCountry>,
): String? {
  if (setup.variants.isEmpty()) return "Choose at least one question variant."
  if ((setup.mode == GameMode.Continents || setup.mode == GameMode.SpeedRun || setup.usesContinentsBase()) && setup.selectedContinents.isEmpty()) {
    return "Choose at least one continent."
  }
  if ((setup.mode == GameMode.Continents || setup.mode == GameMode.SpeedRun || setup.usesContinentsBase()) && countryPoolFor(setup).size < 4) {
    return "Choose continents with at least 4 countries."
  }
  if (!setup.surpriseMe) {
    val questionCount = setup.questionCount ?: return "Write how many questions you want."
    if (questionCount <= 0) return "Question count must be at least 1."
    val maxQuestions = countryPoolFor(setup).size
    val limit = if (setup.mode == GameMode.Training || setup.mode == GameMode.MistakeReview) 999 else maxQuestions
    if (questionCount > limit) return "Question count must be between 1 and $limit."
  }

  if (setup.mode == GameMode.LocalMultiplayer) {
    val names = setup.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
    if (names.size !in 2..5) return "Local multiplayer needs 2 to 5 named players."
    if (names.distinctBy { it.lowercase() }.size != names.size) return "Player names must be unique."
  }
  return null
}

internal fun configFor(
  setup: SetupState,
  poolSize: Int,
  hintDifficulty: HintDifficulty,
  random: Random,
): QuizConfig {
  val variants = setup.variants

  val questionCount =
    if (setup.mode == GameMode.AllIn || setup.usesAllInBase()) {
      poolSize
    } else if (setup.mode == GameMode.DailyChallenge) {
      (setup.questionCount?.coerceIn(1, minOf(poolSize, 10)) ?: minOf(poolSize, 10)).coerceAtLeast(1)
    } else if (setup.mode == GameMode.MistakeReview) {
      setup.questionCount?.coerceIn(1, 999) ?: 10
    } else if (setup.mode == GameMode.Training) {
      if (setup.surpriseMe) {
        random.nextInt(from = 1, until = 1000)
      } else {
        setup.questionCount?.coerceIn(1, 999) ?: 1
      }
    } else if (setup.surpriseMe) {
      random.nextInt(from = 1, until = poolSize + 1)
    } else {
      setup.questionCount?.coerceIn(1, poolSize) ?: 1
    }

  val players =
    if (setup.mode == GameMode.LocalMultiplayer) {
      setup.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
    } else {
      listOf("Solo")
    }

  return QuizConfig(
    mode = setup.mode,
    variants = variants,
    selectedContinents = setup.selectedContinents,
    questionCount = questionCount,
    surpriseMe = setup.surpriseMe,
    allInType = setup.allInType,
    hintDifficulty = hintDifficulty,
    players = players,
    poolSource =
      when (setup.mode) {
        GameMode.DailyChallenge -> QuizPoolSource.DailyChallenge
        GameMode.MistakeReview -> QuizPoolSource.MistakeReview
        else -> QuizPoolSource.Standard
      },
    dailyChallengeTheme = setup.dailyChallengeTheme,
  )
}

internal fun countryPoolFor(
  setup: SetupState,
  countries: List<FlagCountry>,
): List<FlagCountry> =
  if (setup.mode == GameMode.Continents || setup.mode == GameMode.SpeedRun || setup.usesContinentsBase()) {
    countries.filter { it.continent in setup.selectedContinents }
  } else if (setup.mode == GameMode.DailyChallenge || setup.mode == GameMode.MistakeReview) {
    countries
  } else {
    countries
  }

internal fun questionLimitFor(
  setup: SetupState,
  countries: List<FlagCountry>,
): Int =
  if (setup.mode == GameMode.Training) {
    999
  } else if (setup.mode == GameMode.MistakeReview) {
    999
  } else if (setup.mode == GameMode.DailyChallenge) {
    10
  } else {
    countryPoolFor(setup, countries).size
  }

internal fun advanceLevelProgress(
  progress: LevelProgressState,
  earnedHints: Int,
  correctAnswers: Int,
  eligibleQuizCompletions: Int,
): LevelProgressResult {
  var level = progress.level
  var hints = progress.hintsTowardNextLevel + earnedHints
  var correct = progress.correctAnswersTowardNextLevel + correctAnswers
  var eligibleQuizzes = progress.eligibleQuizzesTowardNextLevel + eligibleQuizCompletions
  var levelUps = 0

  while (level < ProgressionRules.MaxLevel) {
    val requirements = ProgressionRules.requirementsForLevel(level)
    if (
      hints < requirements.hintsNeeded ||
      correct < requirements.correctAnswersNeeded ||
      eligibleQuizzes < requirements.eligibleQuizzesNeeded
    ) {
      break
    }
    level++
    levelUps++
    hints -= requirements.hintsNeeded
    correct -= requirements.correctAnswersNeeded
    eligibleQuizzes -= requirements.eligibleQuizzesNeeded
  }

  val finalRequirements = ProgressionRules.requirementsForLevel(level)
  val clampedHints = hints.coerceAtMost(finalRequirements.hintsNeeded)
  val clampedCorrect = correct.coerceAtMost(finalRequirements.correctAnswersNeeded)
  val clampedEligibleQuizzes = eligibleQuizzes.coerceAtMost(finalRequirements.eligibleQuizzesNeeded)

  return LevelProgressResult(
    progress =
      progress.copy(
        level = level,
        hintsTowardNextLevel = if (levelUps > 0) 0 else clampedHints,
        correctAnswersTowardNextLevel = clampedCorrect,
        eligibleQuizzesTowardNextLevel = clampedEligibleQuizzes,
        levelUpVisible = levelUps > 0,
      ),
    bonusHints = levelUps * 5,
  )
}

internal data class LevelProgressResult(
  val progress: LevelProgressState,
  val bonusHints: Int,
)

internal fun awardMedalIfEligible(
  ratings: RatingsProgress,
  quiz: QuizState,
  completedResults: List<QuestionResult>,
  distinctCountries: Int,
  totalCatalogCountries: Int,
): RatingsProgress {
  if (quiz.isMultiplayer) return ratings
  if (completedResults.isEmpty() || completedResults.any { !it.isCorrect }) return ratings

  val awardedMedal =
    ProgressionRules.medalForPerfectQuiz(
      totalQuestions = completedResults.size,
      distinctCountries = distinctCountries,
      totalCatalogCountries = totalCatalogCountries,
    ) ?: return ratings

  return ratings.increment(awardedMedal)
}

internal fun awardAchievementsIfEligible(
  achievements: AchievementsProgress,
  ratings: RatingsProgress,
  quiz: QuizState,
  completedResults: List<QuestionResult>,
  distinctCountries: Int,
  completedAtEpochMillis: Long,
  totalCatalogCountries: Int,
  availableCountriesForSelectedContinent: Int,
): AchievementsProgress {
  if (quiz.isMultiplayer || completedResults.isEmpty()) return achievements

  var updatedAchievements = achievements
  val perfectQuiz = completedResults.all { it.isCorrect }
  val medalEligiblePerfectQuiz = perfectQuiz && completedResults.size >= 10

  if (ratings.bronzeCount >= 50) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.BronzeCollector, completedAtEpochMillis)
  }
  if (ratings.silverCount >= 25) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.SilverCollector, completedAtEpochMillis)
  }
  if (ratings.goldCount >= 10) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.GoldCollector, completedAtEpochMillis)
  }
  if (ratings.titaniumCount >= 5) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.PlatinumCollector, completedAtEpochMillis)
  }
  if (ratings.diamondCount >= 1) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.DiamondCollector, completedAtEpochMillis)
  }

  if (medalEligiblePerfectQuiz) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.FirstPerfect, completedAtEpochMillis)
  }

  if (medalEligiblePerfectQuiz && completedResults.none { it.hintUsed }) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.HintlessHero, completedAtEpochMillis)
  }

  if (medalEligiblePerfectQuiz && QuizVariant.entries.all { variant -> completedResults.any { it.question.variant == variant } }) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.VariantMaster, completedAtEpochMillis)
  }

  if (quiz.mode == GameMode.SpeedRun && completedResults.isNotEmpty()) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.SpeedRunStarter, completedAtEpochMillis)
    if (completedResults.all { it.isCorrect } && completedResults.none { it.hintUsed }) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.SpeedRunPurist, completedAtEpochMillis)
    }
  }

  if (perfectQuiz && completedResults.size == totalCatalogCountries && distinctCountries == totalCatalogCountries) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.DiamondWorld, completedAtEpochMillis)
    if (completedResults.none { it.hintUsed }) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.WorldPurist, completedAtEpochMillis)
    }
  }

  if (
    perfectQuiz &&
    quiz.mode == GameMode.AllIn &&
    quiz.variants.containsAll(QuizVariant.entries) &&
    completedResults.size == totalCatalogCountries &&
    distinctCountries == totalCatalogCountries
  ) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.NoBluffLegend, completedAtEpochMillis)
  }

  val selectedContinent = quiz.selectedContinents.singleOrNull()
  val continentAchievementId = selectedContinent?.let(AchievementId::forContinent)
  if (continentAchievementId != null) {
    val qualifiesForContinentAchievement =
      ProgressionRules.qualifiesForContinentAchievement(
        mode = quiz.mode ?: GameMode.Training,
        selectedContinents = quiz.selectedContinents,
        usedHint = completedResults.any { it.hintUsed },
        totalQuestions = completedResults.size,
        correctAnswers = completedResults.count { it.isCorrect },
        distinctCountries = distinctCountries,
        availableCountriesForSelectedContinent = availableCountriesForSelectedContinent,
      )
    if (qualifiesForContinentAchievement) {
      updatedAchievements = updatedAchievements.unlock(continentAchievementId, completedAtEpochMillis)
    }
  }

  return updatedAchievements
}

internal fun SetupState.usesContinentsBase(): Boolean =
  mode == GameMode.LocalMultiplayer && multiplayerBase == MultiplayerQuizBase.Continents

internal fun SetupState.usesAllInBase(): Boolean =
  mode == GameMode.LocalMultiplayer && multiplayerBase == MultiplayerQuizBase.AllIn

internal data class QuestionAdvanceOutcome(
  val quiz: QuizState,
  val shouldComplete: Boolean,
)

internal fun buildQuestionAdvanceOutcome(
  state: FlagGameUiState,
): QuestionAdvanceOutcome? {
  val quiz = state.quiz
  val question = quiz.currentQuestion ?: return null
  val currentDraft = quiz.currentQuestionState
  val updatedDraft =
    currentDraft.copy(
      status = QuestionStatus.Answered,
      typedAnswer = currentDraft.typedAnswer,
      locked = quiz.mode == GameMode.Training && currentDraft.status == QuestionStatus.Answered,
    )
  val updatedQuestionStates = quiz.questionStates.replaceAt(quiz.currentQuestionIndex, updatedDraft)
  val isCorrect =
    when (question.variant) {
      QuizVariant.TypeCountryName ->
        QuizAnswerChecker.isTypedAnswerCorrect(
          typedAnswer = currentDraft.typedAnswer,
          acceptedAnswers = question.correctCountry.acceptedTypedAnswers(state.settings.language),
        )

      QuizVariant.FlagToCountry,
      QuizVariant.CountryToFlag ->
        QuizAnswerChecker.isCountrySelectionCorrect(currentDraft.selectedCountry, question.correctCountry)
    }

  val answeredPlayers = quiz.players.toMutableList()
  answeredPlayers[quiz.currentPlayerIndex] =
    quiz.currentPlayer.afterAnswer(
      isCorrect = isCorrect,
      hintUses = currentDraft.hintUses,
      hintDifficulty = state.settings.hintDifficulty,
      canEarnHints = quiz.mode != GameMode.LocalMultiplayer,
    )
  val updatedResults = buildQuizResults(quiz.copy(questionStates = updatedQuestionStates), state.settings.language)
  val updatedPlayers = scorePlayersFromResults(
    answeredPlayers,
    updatedResults,
    state.settings.hintDifficulty,
    quiz.mode != GameMode.LocalMultiplayer,
  )
  val updatedQuiz =
    quiz.copy(
      questionStates = updatedQuestionStates,
      results = updatedResults,
      players = updatedPlayers,
    )

  if (quiz.currentQuestionIndex >= quiz.questions.lastIndex) {
    return if (updatedQuiz.canFinish) {
      QuestionAdvanceOutcome(quiz = updatedQuiz, shouldComplete = true)
    } else {
      QuestionAdvanceOutcome(
        quiz = updatedQuiz.loadQuestionDraft(quiz.currentQuestionIndex),
        shouldComplete = false,
      )
    }
  }

  val nextQuestionIndex = quiz.currentQuestionIndex + 1
  return QuestionAdvanceOutcome(
    quiz =
      updatedQuiz.copy(
        currentQuestionIndex = nextQuestionIndex,
        currentPlayerIndex = nextQuestionIndex % quiz.players.size,
      ).loadQuestionDraft(nextQuestionIndex),
    shouldComplete = false,
  )
}

package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.data.QuizQuestionSpec
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizPoolSource
import com.example.flaggameandroid.core.model.QuizSessionMode
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.RatingsProgress
import kotlin.random.Random

private fun tr(language: AppLanguage, english: String, bulgarian: String, german: String): String =
  when (language) {
    AppLanguage.English -> english
    AppLanguage.Bulgarian -> bulgarian
    AppLanguage.German -> german
  }

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
          locked = currentDraft.locked || instantCorrectionEnabled,
        ),
      )

    currentQuestion?.variant == QuizVariant.TypeText &&
      currentDraft.typedAnswer.isBlank() &&
      currentDraft.status != QuestionStatus.Answered ->
      questionStates.replaceAt(currentQuestionIndex, currentDraft.copy(status = QuestionStatus.Skipped))

    currentQuestion?.variant != QuizVariant.TypeText && currentDraft.status == QuestionStatus.Unanswered ->
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

internal fun QuizState.nextUnansweredQuestionIndex(): Int? {
  val unansweredIndices =
    questionStates.mapIndexedNotNull { index, questionState ->
      if (questionState.status == QuestionStatus.Unanswered) index else null
    }
  if (unansweredIndices.isEmpty()) return null
  return unansweredIndices.firstOrNull { it > currentQuestionIndex } ?: unansweredIndices.first()
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
        QuizVariant.TypeText ->
          QuizAnswerChecker.isTypedAnswerCorrect(
            typedAnswer = draft.typedAnswer,
            acceptedAnswers = question.correctCountry.acceptedTypedAnswers(language, question.topic),
          )

        QuizVariant.FlagToText,
        QuizVariant.TextToFlag ->
          QuizAnswerChecker.isCountrySelectionCorrect(draft.selectedCountry, question.correctCountry)
      }
    val updatedStreak =
      when {
        !isCorrect || draft.revealed -> 0
        draft.hintUses > 0 -> previousStreak
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
      revealed = draft.revealed,
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
        revealed = result.revealed,
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
  language: AppLanguage,
): String? {
  if (setup.variants.isEmpty()) {
    return tr(
      language,
      "Choose at least one question variant.",
      "Избери поне един вид въпрос.",
      "Wähle mindestens einen Fragetyp.",
    )
  }
  if (
    setup.mode == GameMode.CreateQuiz &&
    !setup.usesCreateQuizTraining &&
    !setup.usesCreateQuizManualHardcore &&
    setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals &&
    setup.selectedCountryCodes.isEmpty() &&
    setup.selectedCapitalCountryCodes.isEmpty()
  ) {
    return if (setup.topic == QuizTopic.Mixed) {
      tr(
        language,
        "Choose at least one country or capital.",
        "Избери поне една държава или столица.",
        "Wähle mindestens ein Land oder eine Hauptstadt.",
      )
    } else {
      tr(
        language,
        "Choose at least one country.",
        "Избери поне една държава.",
        "Wähle mindestens ein Land.",
      )
    }
  }
  val needsTimer =
    setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizManualTimer
  if (needsTimer) {
    val secondsPerAnswer =
      setup.speedRunSecondsPerAnswer
        ?: return tr(
          language,
          "Write how many seconds each answer should get.",
          "Напиши колко секунди да има всеки отговор.",
          "Gib an, wie viele Sekunden jede Antwort haben soll.",
        )
    if (secondsPerAnswer !in 1..60) {
      return tr(
        language,
        "Seconds per answer must be between 1 and 60.",
        "Секундите за отговор трябва да са между 1 и 60.",
        "Sekunden pro Antwort müssen zwischen 1 und 60 liegen.",
      )
    }
  }
  if (!setup.surpriseMe) {
    val maxQuestions = countryPoolFor(setup).size
    if (setup.mode == GameMode.MistakeReview) return null
    if (setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.usesCreateQuizManualHardcore) return null
    if (
      setup.mode == GameMode.CreateQuiz &&
      !setup.usesCreateQuizTraining &&
      setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals &&
      setup.topic == QuizTopic.Mixed
    ) return null
    if (setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals) return null
    val questionCount =
      setup.questionCount
        ?: return tr(
          language,
          "Write how many questions you want.",
          "Напиши колко въпроса искаш.",
          "Gib an, wie viele Fragen du möchtest.",
        )
    if (questionCount <= 0) {
      return tr(
        language,
        "Question count must be at least 1.",
        "Броят въпроси трябва да е поне 1.",
        "Die Anzahl der Fragen muss mindestens 1 sein.",
      )
    }
    val limit =
      when {
        setup.usesCreateQuizTraining -> 999
        setup.mode == GameMode.CreateQuiz &&
          !setup.usesCreateQuizTraining &&
          setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals &&
          setup.topic == QuizTopic.Mixed ->
          setup.derivedCreateQuizQuestionCount()
        else -> maxQuestions
      }
    if (questionCount > limit) {
      return tr(
        language,
        "Question count must be between 1 and $limit.",
        "Броят въпроси трябва да е между 1 и $limit.",
        "Die Anzahl der Fragen muss zwischen 1 und $limit liegen.",
      )
    }
  }

  if (setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizLocalMultiplayer) {
    val names = setup.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
    if (names.size !in 2..5) {
      return tr(
        language,
        "Local multiplayer needs 2 to 5 named players.",
        "Локалният мултиплейър изисква 2 до 5 именувани играча.",
        "Lokaler Mehrspieler braucht 2 bis 5 benannte Spieler.",
      )
    }
    if (names.distinctBy { it.lowercase() }.size != names.size) {
      return tr(
        language,
        "Player names must be unique.",
        "Имената на играчите трябва да са уникални.",
        "Spielernamen müssen eindeutig sein.",
      )
    }
  }
  return null
}

internal fun configFor(
  setup: SetupState,
  poolSize: Int,
  hintDifficulty: HintDifficulty,
  random: Random,
  practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
): QuizConfig {
  val variants = setup.variants

  val questionCount =
    when {
      setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.usesCreateQuizManualHardcore ->
        if (setup.topic == QuizTopic.Mixed) poolSize * 2 else poolSize
      setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals ->
        if (setup.topic == QuizTopic.Mixed) setup.derivedCreateQuizQuestionCount() else poolSize
      setup.mode == GameMode.DailyChallenge -> (setup.questionCount?.coerceIn(1, minOf(poolSize, 10)) ?: minOf(poolSize, 10)).coerceAtLeast(1)
      setup.mode == GameMode.MistakeReview -> mistakeReviewEligibleCountryCount(practiceStats, setup.topic)
      setup.usesCreateQuizTraining -> setup.questionCount?.coerceIn(1, 999) ?: 1
      setup.surpriseMe -> random.nextInt(from = 1, until = poolSize + 1)
      else -> setup.questionCount?.coerceIn(1, poolSize) ?: 1
    }

  val players =
    if (setup.usesCreateQuizLocalMultiplayer) {
      setup.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
    } else {
      listOf("Solo")
    }

  return QuizConfig(
    mode = setup.mode,
    sessionMode =
      when {
        setup.usesCreateQuizTraining -> QuizSessionMode.Training
        setup.usesCreateQuizLocalMultiplayer -> QuizSessionMode.LocalMultiplayer
        else -> QuizSessionMode.Standard
      },
    variants = variants,
    topic = setup.topic,
    selectedContinents = setup.selectedContinents,
    questionSpecs =
      if (
        setup.mode == GameMode.CreateQuiz &&
        !setup.usesCreateQuizTraining &&
        setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals &&
        setup.topic == QuizTopic.Mixed
      ) {
        setup.selectedCountryCodes.map { code -> QuizQuestionSpec(code, QuizTopic.Countries) } +
          setup.selectedCapitalCountryCodes.map { code -> QuizQuestionSpec(code, QuizTopic.Capitals) }
      } else {
        emptyList()
      },
    questionCount = questionCount,
    speedRunSecondsPerAnswer = setup.speedRunSecondsPerAnswer ?: 5,
    countdownEnabled =
      setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizManualTimer,
    surpriseMe = setup.surpriseMe,
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
  if (setup.mode == GameMode.CreateQuiz) {
    if (setup.usesCreateQuizTraining) {
      countries
    } else when (setup.createQuizSource) {
      CreateQuizSource.PresetFilter -> {
        val selectedPresets = setup.createQuizPresets.ifEmpty { setOf(setup.createQuizPreset) }
        countries.filter { country -> selectedPresets.any { preset -> matchesCreateQuizPreset(country, preset, setup.topic) } }
      }
      CreateQuizSource.ManualCountriesCapitals ->
        if (setup.usesCreateQuizManualHardcore) {
          countries
        } else {
          countries.filter { country ->
            country.code in setup.selectedCountryCodes || country.code in setup.selectedCapitalCountryCodes
          }
        }
    }
  } else if (setup.mode == GameMode.DailyChallenge || setup.mode == GameMode.MistakeReview) {
    countries
  } else {
    countries
  }

internal fun questionLimitFor(
  setup: SetupState,
  countries: List<FlagCountry>,
  practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
): Int =
  if (setup.usesCreateQuizTraining) {
    999
  } else if (setup.mode == GameMode.CreateQuiz) {
    when {
      setup.mode == GameMode.CreateQuiz &&
        !setup.usesCreateQuizTraining &&
        setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals &&
        setup.topic == QuizTopic.Mixed ->
        setup.derivedCreateQuizQuestionCount()
      setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals ->
        setup.selectedCountryCodes.derivedCreateQuizQuestionCount(setup.topic)
      setup.usesCreateQuizManualHardcore ->
        if (setup.topic == QuizTopic.Mixed) countries.size * 2 else countries.size
      else -> countryPoolFor(setup, countries).size
    }
  } else if (setup.mode == GameMode.MistakeReview) {
    mistakeReviewEligibleCountryCount(practiceStats, setup.topic)
  } else if (setup.mode == GameMode.DailyChallenge) {
    10
  } else {
    countryPoolFor(setup, countries).size
  }

private fun Set<String>.derivedCreateQuizQuestionCount(topic: QuizTopic): Int =
  if (topic == QuizTopic.Mixed) size * 2 else size

private fun SetupState.derivedCreateQuizQuestionCount(): Int =
  if (topic == QuizTopic.Mixed) {
    selectedCountryCodes.size + selectedCapitalCountryCodes.size
  } else {
    selectedCountryCodes.size
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
  if (completedResults.isEmpty() || completedResults.any { !it.countsAsCorrect }) return ratings

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
  val perfectQuiz = completedResults.all { it.countsAsCorrect }
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

  if (quiz.mode == GameMode.CreateQuiz && quiz.countdownEnabled && completedResults.isNotEmpty()) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.SpeedRunStarter, completedAtEpochMillis)
    if (completedResults.all { it.countsAsCorrect } && completedResults.none { it.hintUsed }) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.SpeedRunPurist, completedAtEpochMillis)
    }
    if (quiz.speedRunSecondsPerAnswer == 1 && completedResults.all { it.countsAsCorrect }) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.SpeedRunOneSecond, completedAtEpochMillis)
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
    quiz.mode == GameMode.CreateQuiz &&
    quiz.variants.containsAll(QuizVariant.entries) &&
    completedResults.size == totalCatalogCountries &&
    distinctCountries == totalCatalogCountries
  ) {
    updatedAchievements =
      updatedAchievements.unlock(
        when (quiz.topic) {
          QuizTopic.Countries -> AchievementId.HardcoreCountriesLegend
          QuizTopic.Capitals -> AchievementId.HardcoreCapitalsLegend
          QuizTopic.Mixed -> AchievementId.HardcoreLegend
        },
        completedAtEpochMillis,
      )
  }

  val selectedContinent = quiz.selectedContinents.singleOrNull()
  val continentAchievementId = selectedContinent?.let(AchievementId::forContinent)
  if (continentAchievementId != null) {
    val qualifiesForContinentAchievement =
      ProgressionRules.qualifiesForContinentAchievement(
        mode = quiz.mode ?: GameMode.CreateQuiz,
        selectedContinents = quiz.selectedContinents,
        usedHint = completedResults.any { it.hintUsed },
        totalQuestions = completedResults.size,
        correctAnswers = completedResults.count { it.countsAsCorrect },
        distinctCountries = distinctCountries,
        availableCountriesForSelectedContinent = availableCountriesForSelectedContinent,
      )
    if (qualifiesForContinentAchievement) {
      updatedAchievements = updatedAchievements.unlock(continentAchievementId, completedAtEpochMillis)
    }
  }

  return updatedAchievements
}

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
      locked = currentDraft.locked || quiz.instantCorrectionEnabled,
    )
  val updatedQuestionStates = quiz.questionStates.replaceAt(quiz.currentQuestionIndex, updatedDraft)
  val isCorrect =
    when (question.variant) {
      QuizVariant.TypeText ->
        QuizAnswerChecker.isTypedAnswerCorrect(
          typedAnswer = currentDraft.typedAnswer,
          acceptedAnswers = question.correctCountry.acceptedTypedAnswers(state.settings.language, question.topic),
        )

      QuizVariant.FlagToText,
      QuizVariant.TextToFlag ->
        QuizAnswerChecker.isCountrySelectionCorrect(currentDraft.selectedCountry, question.correctCountry)
    }

  val answeredPlayers = quiz.players.toMutableList()
  answeredPlayers[quiz.currentPlayerIndex] =
    quiz.currentPlayer.afterAnswer(
      isCorrect = isCorrect,
      hintUses = currentDraft.hintUses,
      revealed = currentDraft.revealed,
      hintDifficulty = state.settings.hintDifficulty,
      canEarnHints = quiz.sessionMode != QuizSessionMode.LocalMultiplayer,
    )
  val updatedResults = buildQuizResults(quiz.copy(questionStates = updatedQuestionStates), state.settings.language)
  val updatedPlayers =
    scorePlayersFromResults(
      answeredPlayers,
      updatedResults,
      state.settings.hintDifficulty,
      quiz.sessionMode != QuizSessionMode.LocalMultiplayer,
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


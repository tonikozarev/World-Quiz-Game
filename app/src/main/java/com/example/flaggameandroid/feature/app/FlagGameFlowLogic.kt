package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
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
          locked = currentDraft.locked || instantCorrectionEnabled,
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
  if (
    setup.mode == GameMode.CreateQuiz &&
    !setup.usesCreateQuizTraining &&
    !setup.usesCreateQuizManualHardcore &&
    setup.createQuizSource == CreateQuizSource.ManualCountries &&
    setup.selectedCountryCodes.isEmpty()
  ) {
    return "Choose at least one country."
  }
  val needsContinents =
    setup.mode == GameMode.WorldFlags ||
      (setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.Continents)
  if (needsContinents && setup.selectedContinents.isEmpty()) {
    return "Choose at least one continent."
  }
  if (needsContinents && countryPoolFor(setup).size < 4) {
    return "Choose continents with at least 4 countries."
  }
  val needsTimer =
    (setup.mode == GameMode.WorldFlags && setup.usesWorldFlagsTimer) ||
      (setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizManualTimer)
  if (needsTimer) {
    val secondsPerAnswer = setup.speedRunSecondsPerAnswer ?: return "Write how many seconds each answer should get."
    if (secondsPerAnswer !in 1..60) return "Seconds per answer must be between 1 and 60."
  }
  if (!setup.surpriseMe) {
    val maxQuestions = countryPoolFor(setup).size
    if (setup.mode == GameMode.MistakeReview) return null
    if (setup.mode == GameMode.WorldFlags && setup.usesWorldFlagsHardcore) return null
    if (setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.usesCreateQuizManualHardcore) return null
    if (setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.createQuizSource == CreateQuizSource.ManualCountries) return null
    val questionCount = setup.questionCount ?: return "Write how many questions you want."
    if (questionCount <= 0) return "Question count must be at least 1."
    val limit =
      when {
        setup.mode == GameMode.Training -> 999
        setup.usesCreateQuizTraining -> 999
        setup.mode == GameMode.WorldFlags && setup.usesWorldFlagsHardcore -> 195
        else -> maxQuestions
      }
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
  practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
): QuizConfig {
  val variants = setup.variants

  val questionCount =
    when {
      setup.mode == GameMode.WorldFlags && setup.usesWorldFlagsHardcore -> poolSize
      setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.usesCreateQuizManualHardcore -> poolSize
      setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && setup.createQuizSource == CreateQuizSource.ManualCountries -> poolSize
      setup.mode == GameMode.DailyChallenge -> (setup.questionCount?.coerceIn(1, minOf(poolSize, 10)) ?: minOf(poolSize, 10)).coerceAtLeast(1)
      setup.mode == GameMode.MistakeReview -> mistakeReviewEligibleCountryCount(practiceStats)
      setup.mode == GameMode.Training ->
        if (setup.surpriseMe) {
          random.nextInt(from = 1, until = 1000)
        } else {
          setup.questionCount?.coerceIn(1, 999) ?: 1
        }
      setup.usesCreateQuizTraining -> setup.questionCount?.coerceIn(1, 999) ?: 1
      setup.mode == GameMode.WorldFlags ->
        setup.questionCount?.coerceIn(1, poolSize) ?: 1
      setup.mode == GameMode.LocalMultiplayer ->
        setup.questionCount?.coerceIn(1, poolSize) ?: 1
      setup.surpriseMe -> random.nextInt(from = 1, until = poolSize + 1)
      else -> setup.questionCount?.coerceIn(1, poolSize) ?: 1
    }

  val players =
    if (setup.mode == GameMode.LocalMultiplayer) {
      setup.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
    } else {
      listOf("Solo")
    }

  return QuizConfig(
    mode = if (setup.usesCreateQuizTraining) GameMode.Training else setup.mode,
    variants = variants,
    selectedContinents = setup.selectedContinents,
    questionCount = questionCount,
    speedRunSecondsPerAnswer = setup.speedRunSecondsPerAnswer ?: 5,
    countdownEnabled =
      (setup.mode == GameMode.WorldFlags && setup.usesWorldFlagsTimer) ||
        (setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizManualTimer),
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
  if (setup.mode == GameMode.CreateQuiz) {
    if (setup.usesCreateQuizTraining) {
      countries
    } else when (setup.createQuizSource) {
      CreateQuizSource.PresetFilter -> {
        val selectedPresets = setup.createQuizPresets.ifEmpty { setOf(setup.createQuizPreset) }
        countries.filter { country -> selectedPresets.any { preset -> matchesCreateQuizPreset(country, preset) } }
      }
      CreateQuizSource.ManualCountries ->
        if (setup.usesCreateQuizManualHardcore) {
          countries
        } else {
          countries.filter { country -> country.code in setup.selectedCountryCodes }
        }
    }
  } else if (setup.mode == GameMode.WorldFlags && setup.usesWorldFlagsHardcore) {
    countries
  } else if (setup.mode == GameMode.WorldFlags || (setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.Continents)) {
    countries.filter { it.continent in setup.selectedContinents }
  } else if (setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.AllIn) {
    countries
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
  if (setup.mode == GameMode.Training) {
    999
  } else if (setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizManualHardcore) {
    countries.size
  } else if (setup.usesCreateQuizTraining) {
    999
  } else if (setup.mode == GameMode.CreateQuiz) {
    countryPoolFor(setup, countries).size
  } else if (setup.mode == GameMode.MistakeReview) {
    mistakeReviewEligibleCountryCount(practiceStats)
  } else if (setup.mode == GameMode.WorldFlags && setup.usesWorldFlagsHardcore) {
    countries.size
  } else if (setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.AllIn) {
    countries.size
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

  if (quiz.mode == GameMode.WorldFlags && quiz.countdownEnabled && completedResults.isNotEmpty()) {
    updatedAchievements = updatedAchievements.unlock(AchievementId.SpeedRunStarter, completedAtEpochMillis)
    if (completedResults.all { it.isCorrect } && completedResults.none { it.hintUsed }) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.SpeedRunPurist, completedAtEpochMillis)
    }
    if (quiz.speedRunSecondsPerAnswer == 1 && completedResults.all { it.isCorrect }) {
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
    quiz.mode == GameMode.WorldFlags &&
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

private fun matchesCreateQuizPreset(
  country: FlagCountry,
  preset: CreateQuizPreset,
): Boolean {
  val code = country.code
  return when (preset) {
    CreateQuizPreset.TwoColors -> code in createQuizTwoColorCountries
    CreateQuizPreset.ThreeColors -> code in createQuizThreeColorCountries
    CreateQuizPreset.FourPlusColors -> code in createQuizFourPlusColorCountries
    CreateQuizPreset.HorizontalStripes -> code in createQuizHorizontalStripeCountries
    CreateQuizPreset.VerticalStripes -> code in createQuizVerticalStripeCountries
    CreateQuizPreset.Stars -> code in createQuizStarCountries
    CreateQuizPreset.Crosses -> code in createQuizCrossCountries
    CreateQuizPreset.Animals -> code in createQuizAnimalCountries
    CreateQuizPreset.Nato -> code in createQuizNato
    CreateQuizPreset.EuUnion -> code in createQuizEuUnion
    CreateQuizPreset.WorldTradeOrganization -> code in createQuizWorldTradeOrganization
    CreateQuizPreset.CommonwealthOfNations -> code in createQuizCommonwealthOfNations
    CreateQuizPreset.AfricanUnion -> code in createQuizAfricanUnion
    CreateQuizPreset.OrganisationOfIslamicCooperation -> code in createQuizOrganisationOfIslamicCooperation
    CreateQuizPreset.NoSymbols -> false
  }
}

private val createQuizAllCountries =
  setOf(
    "AD", "AE", "AF", "AG", "AL", "AM", "AO", "AR", "AT", "AU", "AZ", "BA", "BB", "BD",
    "BE", "BF", "BG", "BH", "BI", "BJ", "BN", "BO", "BR", "BS", "BT", "BW", "BY", "BZ",
    "CA", "CD", "CF", "CG", "CH", "CI", "CL", "CM", "CN", "CO", "CR", "CU", "CV", "CY",
    "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "ER", "ES", "ET", "FI",
    "FJ", "FM", "FR", "GA", "GB", "GD", "GE", "GH", "GM", "GN", "GQ", "GR", "GT", "GW",
    "GY", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IN", "IQ", "IR", "IS", "IT", "JM",
    "JO", "JP", "KE", "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KZ", "LA", "LB",
    "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MG",
    "MH", "MK", "ML", "MM", "MN", "MR", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA",
    "NE", "NG", "NI", "NL", "NO", "NP", "NR", "NZ", "OM", "PA", "PE", "PG", "PH", "PK",
    "PL", "PS", "PT", "PW", "PY", "QA", "RO", "RS", "RU", "RW", "SA", "SB", "SC", "SD",
    "SE", "SG", "SI", "SK", "SL", "SM", "SN", "SO", "SR", "SS", "ST", "SV", "SY", "SZ",
    "TD", "TG", "TH", "TJ", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TZ", "UA", "UG",
    "US", "UY", "UZ", "VA", "VC", "VE", "VN", "VU", "WS", "YE", "ZA", "ZM", "ZW"
  )

private val createQuizTwoColorCountries =
  setOf(
    "AL", "AT", "BD", "BH", "CA", "CH", "CN", "DK", "FI", "FM", "GE", "GR", "HN", "ID",
    "IL", "JP", "KG", "KZ", "LV", "MA", "MC", "MK", "NG", "PE", "PK", "PL", "PW", "QA",
    "SA", "SC", "SE", "SG", "SO", "TN", "TO", "TR", "UA", "VN",
  )

private val createQuizThreeColorCountries =
  setOf(
    "AM", "AO", "AR", "AU", "BA", "BB", "BE", "BF", "BI", "BG", "BJ", "BO", "BS", "BW",
    "BY", "CD", "CG", "CI", "CL", "CM", "CO", "CR", "CU", "CV", "CY", "CZ", "DZ", "EE",
    "FR", "GA", "GB", "GN", "HU", "IE", "IR", "IS", "JM", "KH", "KP", "LA", "LB", "LI",
    "LT", "LU", "MH", "MG", "ML", "MN", "MR", "MV", "MW", "NE", "NL", "NO", "NP", "NR",
    "NZ", "PA", "RO", "RU", "RW", "VC", "WS", "SI", "SK", "SL", "SN", "TH", "TT", "US",
    "UY", "YE",
  )

private val createQuizFourPlusColorCountries =
  createQuizAllCountries.filterNot { code -> code in createQuizTwoColorCountries || code in createQuizThreeColorCountries }.toSet()

private val createQuizHorizontalStripeCountries =
  setOf(
    "AR", "AM", "AT", "AZ", "BS", "BJ", "BY", "BO", "BW", "BG", "BF", "CV", "KH", "CF",
    "CO", "KM", "CR", "HR", "EC", "EG", "SV", "GQ", "EE", "SZ", "ET", "GA", "GM", "DE",
    "GH", "GR", "GW", "HT", "HN", "HU", "IN", "ID", "IR", "IQ", "JO", "KE", "KW", "LA",
    "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MG", "MW", "MY", "MU", "NR", "NL", "NI",
    "NE", "KP", "OM", "PS", "PY", "PL", "RU", "RW", "SL", "SG", "SK", "SI", "SS", "ES",
    "SD", "SR", "SY", "TJ", "TH", "TG", "AE", "US", "UY", "UZ", "VU", "VE", "YE", "ZW"
  )

private val createQuizVerticalStripeCountries =
  setOf(
    "DZ", "AD", "BH", "BB", "BE", "BJ", "CM", "CA", "TD", "CI", "FR", "GT", "GN", "IE",
    "IT", "ML", "MX", "MD", "MN", "NG", "PK", "PE", "PT", "QA", "RO", "VC", "SN", "VA"
  )

private val createQuizStarCountries =
  setOf(
    "DZ", "AO", "AU", "AZ", "BA", "BR", "BF", "BI", "CV", "CM", "CL", "CN", "KM", "HR",
    "CU", "CD", "DJ", "DM", "ET", "GH", "GD", "GW", "HN", "IL", "JO", "LR", "LY", "MY",
    "MH", "FM", "MD", "MA", "MZ", "MM", "NR", "NZ", "KP", "PK", "PA", "PG", "PY", "PH",
    "KN", "WS", "ST", "SN", "SG", "SI", "SB", "SO", "SS", "SR", "SY", "TJ", "TG", "TN",
    "TR", "TM", "TV", "US", "UZ", "VE", "VN", "ZW"
  )

private val createQuizCrossCountries =
  setOf(
    "AU", "BI", "CH", "DK", "DM", "DO", "FI", "FJ", "GB", "GE", "GR", "IS", "JM", "MT",
    "NZ", "TO", "TV", "NO", "SE",
  )

private val createQuizAnimalCountries =
  setOf(
    "AD", "AL", "BO", "BN", "BT", "DM", "EC", "EG", "ES", "FJ", "GT", "HR", "KI", "KZ",
    "LK", "ME", "MD", "MX", "PG", "RS", "UG", "ZM", "ZW",
  )

private val createQuizNato =
  setOf(
    "AL", "BE", "BG", "CA", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB", "GR", "HR", "HU", "IS",
    "IT", "LT", "LU", "LV", "ME", "MK", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SK", "TR", "US",
  )

private val createQuizEuUnion =
  setOf(
    "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GR", "HR", "HU", "IE", "IT",
    "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK",
  )

// Info for WTO: 163 from 166 are sovereign countries in World Trade Organization (WTO) - Hong Kong, Macao and one more are not counting...
private val createQuizWorldTradeOrganization =
  setOf(
    "AD", "AE", "AF", "AG", "AL", "AM", "AO", "AR", "AT", "AU", "BB", "BD", "BE", "BF", "BG", "BH",
    "BI", "BJ", "BN", "BO", "BR", "BW", "BZ", "CA", "CD", "CF", "CG", "CH", "CI", "CL", "CM", "CN",
    "CO", "CR", "CU", "CV", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "EC", "EE", "EG", "ES", "FI",
    "FJ", "FR", "GA", "GB", "GD", "GE", "GH", "GM", "GN", "GR", "GT", "GW", "GY", "HN", "HR", "HT",
    "HU", "ID", "IE", "IL", "IN", "IS", "IT", "JM", "JO", "JP", "KE", "KG", "KH", "KM", "KN", "KR",
    "KW", "KZ", "LA", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "MA", "MD", "ME", "MG", "MK",
    "ML", "MM", "MN", "MR", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NE", "NG", "NI", "NL",
    "NO", "NP", "NZ", "OM", "PA", "PE", "PG", "PH", "PK", "PL", "PT", "PY", "QA", "RO", "RU", "RW",
    "SA", "SB", "SC", "SE", "SG", "SI", "SK", "SL", "SN", "SR", "SV", "SZ", "TD", "TG", "TH", "TJ",
    "TL", "TN", "TO", "TR", "TT", "TZ", "UA", "UG", "US", "UY", "VC", "VE", "VN", "VU", "WS", "YE",
    "ZA", "ZM", "ZW",
  )

private val createQuizCommonwealthOfNations =
  setOf(
    "AG", "AU", "BB", "BD", "BN", "BS", "BW", "BZ", "CA", "CM", "CY", "DM", "FJ", "GA", "GB", "GD",
    "GH", "GM", "GY", "IN", "JM", "KE", "KI", "KN", "LC", "LK", "LS", "MT", "MU", "MV", "MW", "MY",
    "MZ", "NA", "NG", "NR", "NZ", "PG", "PK", "RW", "SB", "SC", "SG", "SL", "SZ", "TG", "TO", "TT",
    "TV", "TZ", "UG", "VC", "VU", "WS", "ZA", "ZM",
  )

private val createQuizAfricanUnion =
  setOf(
    "AO", "BF", "BI", "BJ", "BW", "CD", "CF", "CG", "CI", "CM", "CV", "DJ", "DZ", "EG", "EH", "ER",
    "ET", "GA", "GH", "GM", "GN", "GQ", "GW", "KE", "KM", "LR", "LS", "LY", "MA", "MG", "ML", "MR",
    "MU", "MW", "MZ", "NA", "NE", "NG", "RW", "SC", "SD", "SL", "SN", "SO", "SS", "ST", "SZ", "TD",
    "TG", "TN", "TZ", "UG", "ZA", "ZM", "ZW",
  )

private val createQuizOrganisationOfIslamicCooperation =
  setOf(
    "AE", "AF", "AL", "AZ", "BD", "BF", "BH", "BJ", "BN", "CI", "CM", "DJ", "DZ", "EG", "GA", "GM",
    "GN", "GW", "GY", "ID", "IQ", "IR", "JO", "KG", "KM", "KW", "KZ", "LB", "LY", "MA", "ML", "MR",
    "MV", "MY", "MZ", "NE", "NG", "OM", "PK", "PS", "QA", "SA", "SD", "SL", "SN", "SO", "SR", "SY",
    "TD", "TG", "TJ", "TM", "TN", "TR", "UG", "UZ", "YE",
  )

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

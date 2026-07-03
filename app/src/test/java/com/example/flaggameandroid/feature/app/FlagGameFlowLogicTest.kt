package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.capitalQuizMetadata
import com.example.flaggameandroid.core.model.SavedQuizTemplate
import com.example.flaggameandroid.core.model.hasSameQuizConfiguration
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class FlagGameFlowLogicTest {
  @Test
  fun buildQuestionAdvanceOutcome_advancesToNextQuestionAndKeepsResultStatePure() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.Training,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "2",
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 0.0,
        displayName = "Tony",
      )

    val answeredQuiz = quiz.withSelectedCountry(quiz.currentQuestion!!.correctCountry)
    val state = FlagGameUiState(quiz = answeredQuiz)

    val outcome = buildQuestionAdvanceOutcome(state)

    assertFalse(outcome!!.shouldComplete)
    assertEquals(1, outcome.quiz.currentQuestionIndex)
    assertEquals(1, outcome.quiz.results.size)
    assertEquals(2, outcome.quiz.players.first().score)
  }

  @Test
  fun withSelectedCountry_marksNonTrainingQuestionsAnsweredImmediately() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "1",
        selectedContinents = setOf("Europe"),
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 1.0,
        displayName = "Tony",
      )

    val updatedQuiz = quiz.withSelectedCountry(quiz.currentQuestion!!.correctCountry)

    assertEquals(QuestionStatus.Answered, updatedQuiz.currentQuestionState.status)
    assertTrue(updatedQuiz.currentQuestionState.selectedCountry != null)
  }

  @Test
  fun withSelectedCountry_togglesSelectionOffWhenTappedAgainOutsideTraining() {
    val country = FlagCountry(code = "DE", name = "Germany", emoji = "🇩🇪", continent = "Europe")
    val question =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country),
        variant = QuizVariant.FlagToCountry,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        questions = listOf(question),
        questionStates = listOf(QuestionDraftState()),
      )

    val selectedQuiz = quiz.withSelectedCountry(country)
    val deselectedQuiz = selectedQuiz.withSelectedCountry(country)

    assertEquals(QuestionStatus.Unanswered, deselectedQuiz.currentQuestionState.status)
    assertEquals(null, deselectedQuiz.currentQuestionState.selectedCountry)
  }

  @Test
  fun buildQuestions_handlesManualQuizzesSmallerThanFourCountries() {
    val allCountries = StaticFlagCatalogRepository().getCountries()
    val manualCountries = allCountries.filter { it.code in setOf("AT", "DE", "BG") }
    val quiz =
      QuizQuestionGenerator(Random(11)).buildQuestions(
        countries = manualCountries,
        config =
          com.example.flaggameandroid.core.model.QuizConfig(
            mode = GameMode.CreateQuiz,
            variants = setOf(QuizVariant.FlagToCountry),
            questionCount = manualCountries.size,
          ),
        answerPool = allCountries,
      )

    assertEquals(3, quiz.size)
    quiz.forEach { question ->
      assertEquals(4, question.options.size)
      assertTrue(question.options.any { it.code == question.correctCountry.code })
    }
  }

  @Test
  fun withNextSkippedQuestionLoaded_commitsCurrentPendingAnswerBeforeMovingOn() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "3",
        selectedContinents = setOf("Europe"),
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 0.0,
        displayName = "Tony",
      )

    val currentQuestion = quiz.currentQuestion!!
    val updatedQuiz =
      quiz.copy(
        selectedCountry = currentQuestion.correctCountry,
        questionStates =
          quiz.questionStates.replaceAt(
            0,
            QuestionDraftState(
              status = QuestionStatus.Skipped,
              selectedCountry = currentQuestion.correctCountry,
            ),
          ).replaceAt(
            1,
            QuestionDraftState(status = QuestionStatus.Skipped),
          ),
      )

    val jumpedQuiz = updatedQuiz.withNextSkippedQuestionLoaded()

    assertEquals(1, jumpedQuiz.currentQuestionIndex)
    assertEquals(QuestionStatus.Answered, jumpedQuiz.questionStates[0].status)
    assertEquals(currentQuestion.correctCountry, jumpedQuiz.questionStates[0].selectedCountry)
    assertEquals(QuestionStatus.Skipped, jumpedQuiz.questionStates[1].status)
  }

  @Test
  fun withNextSkippedQuestionLoaded_fallsBackToNextUnansweredQuestionWhenNoSkippedExists() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "3",
        selectedContinents = setOf("Europe"),
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 0.0,
        displayName = "Tony",
      )

    val currentQuestion = quiz.currentQuestion!!
    val updatedQuiz =
      quiz.copy(
        currentQuestionIndex = 0,
        selectedCountry = currentQuestion.correctCountry,
        questionStates =
          quiz.questionStates.replaceAt(
            0,
            QuestionDraftState(
              status = QuestionStatus.Answered,
              selectedCountry = currentQuestion.correctCountry,
            ),
          ),
      )

    val jumpedQuiz = updatedQuiz.withNextSkippedQuestionLoaded()

    assertEquals(1, jumpedQuiz.currentQuestionIndex)
    assertEquals(QuestionStatus.Answered, jumpedQuiz.questionStates[0].status)
  }

  @Test
  fun canFinish_doesNotRequireBeingOnLastQuestionWhenAllQuestionsAreAnswered() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "3",
        selectedContinents = setOf("Europe"),
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 0.0,
        displayName = "Tony",
      )

    val completedQuiz =
      quiz.copy(
        currentQuestionIndex = 0,
        questionStates =
          quiz.questions.map { question ->
            QuestionDraftState(
              status = QuestionStatus.Answered,
              selectedCountry = question.correctCountry,
            )
          },
      )

    assertTrue(completedQuiz.canFinish)
  }

  @Test
  fun canFinish_allowsCurrentSkippedQuestionWithPendingTypedAnswer() {
    val country = FlagCountry(code = "DE", name = "Germany", emoji = "🇩🇪", continent = "Europe")
    val question =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country),
        variant = QuizVariant.TypeCountryName,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        questions = listOf(question, question),
        currentQuestionIndex = 0,
        questionStates =
          listOf(
            QuestionDraftState(status = QuestionStatus.Skipped, typedAnswer = "Germany"),
            QuestionDraftState(status = QuestionStatus.Answered, typedAnswer = "Germany"),
          ),
        typedAnswer = "Germany",
      )

    assertTrue(quiz.canFinish)
  }

  @Test
  fun canFinish_updatesImmediatelyAfterAnswerSelection() {
    val country = FlagCountry(code = "DE", name = "Germany", emoji = "🇩🇪", continent = "Europe")
    val question =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country),
        variant = QuizVariant.FlagToCountry,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        questions = listOf(question),
        questionStates = listOf(QuestionDraftState()),
      )

    val answeredQuiz = quiz.withSelectedCountry(country)

    assertTrue(answeredQuiz.canFinish)
  }

  @Test
  fun updateCountryPracticeStats_countsWrongAnswersAcrossNonTrainingModesByCountry() {
    val country = FlagCountry(code = "DE", name = "Germany", emoji = "🇩🇪", continent = "Europe")
    val flagQuestion =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country),
        variant = QuizVariant.FlagToCountry,
      )
    val typedQuestion =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country),
        variant = QuizVariant.TypeCountryName,
      )
    val flagMiss =
      QuestionResult(
        question = flagQuestion,
        playerName = "Tony",
        selectedCountry = null,
        typedAnswer = "",
        isCorrect = false,
        hintUsed = false,
      )
    val typedMiss =
      QuestionResult(
        question = typedQuestion,
        playerName = "Tony",
        selectedCountry = null,
        typedAnswer = "wrong",
        isCorrect = false,
        hintUsed = false,
      )

    val afterContinents = updateCountryPracticeStats(emptyMap(), listOf(flagMiss), 1L, GameMode.WorldFlags)
    val afterSpeedRun = updateCountryPracticeStats(afterContinents, listOf(typedMiss), 2L, GameMode.WorldFlags)
    val afterTraining = updateCountryPracticeStats(afterSpeedRun, listOf(typedMiss), 3L, GameMode.Training)

    assertEquals(2, afterSpeedRun[country.code]?.wrongCount)
    assertEquals(2, afterTraining[country.code]?.wrongCount)
  }

  @Test
  fun validateSetup_blocksQuestionCountAboveSelectedContinentPoolLimit() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        selectedContinents = setOf("Europe"),
        questionCountInput = "999",
        variants = setOf(QuizVariant.FlagToCountry),
      )
    val limit = questionLimitFor(setup, countries)

    val validationError = validateSetup(setup) { countryPoolFor(it, countries) }

    assertEquals("Question count must be between 1 and $limit.", validationError)
  }

  @Test
  fun createQuizCountryPresetPath_keepsExistingUnionBehavior() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val baseSetup =
      buildSetupForMode(
        GameMode.CreateQuiz,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(createQuizSource = CreateQuizSource.PresetFilter)

    val twoColorsPool =
      countryPoolFor(
        baseSetup.copy(createQuizPresets = setOf(CreateQuizPreset.TwoColors)),
        countries,
      )
    val threeColorsPool =
      countryPoolFor(
        baseSetup.copy(createQuizPresets = setOf(CreateQuizPreset.ThreeColors)),
        countries,
      )
    val combinedPool =
      countryPoolFor(
        baseSetup.copy(createQuizPresets = setOf(CreateQuizPreset.TwoColors, CreateQuizPreset.ThreeColors)),
        countries,
      )

    assertTrue(combinedPool.isNotEmpty())
    assertEquals(combinedPool.map { it.code }.distinct().size, combinedPool.size)
    assertEquals(
      (twoColorsPool.map { it.code } + threeColorsPool.map { it.code }).toSet(),
      combinedPool.map { it.code }.toSet(),
    )
  }

  @Test
  fun createQuizCapitalPresetPath_filtersAndDeduplicatesOverlaps() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val baseSetup =
      buildSetupForMode(
        GameMode.CreateQuiz,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        topic = QuizTopic.Capitals,
        createQuizSource = CreateQuizSource.PresetFilter,
      )

    val pool =
      countryPoolFor(
        baseSetup.copy(
          createQuizPresets =
            setOf(
              CreateQuizPreset.CapitalPopulationUnderOneMillion,
              CreateQuizPreset.CapitalAreaUnderFiftySquareKm,
              CreateQuizPreset.CapitalNotCoastal,
            ),
        ),
        countries,
      )

    assertTrue(pool.isNotEmpty())
    assertEquals(pool.map { it.code }.distinct().size, pool.size)
    assertEquals(1, pool.count { it.code == "MC" })
    assertTrue(
      pool.all { country ->
        val metadata = capitalQuizMetadata(country.code)
        metadata != null &&
          (
            metadata.population < 1_000_000L ||
              metadata.areaKm2 < 50.0 ||
              metadata.notCoastal
          )
      },
    )
  }

  @Test
  fun awardAchievementsIfEligible_unlocksSpeedRunOneSecondForPerfectRun() {
    val country = FlagCountry(code = "SR", name = "Speedland", emoji = "SR", continent = "Europe")
    val question =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country),
        variant = QuizVariant.FlagToCountry,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        questions = listOf(question),
        questionStates = listOf(QuestionDraftState(status = QuestionStatus.Answered, selectedCountry = country)),
        speedRunSecondsPerAnswer = 1,
        countdownEnabled = true,
      )
    val results =
      listOf(
        QuestionResult(
          question = question,
          playerName = "Tony",
          selectedCountry = country,
          typedAnswer = "",
          isCorrect = true,
          hintUsed = false,
        ),
      )

    val achievements =
      awardAchievementsIfEligible(
        achievements = AchievementsProgress(),
        ratings = RatingsProgress(),
        quiz = quiz,
        completedResults = results,
        distinctCountries = 1,
        completedAtEpochMillis = 1_000L,
        totalCatalogCountries = 1,
        availableCountriesForSelectedContinent = 1,
      )

    assertTrue(achievements.isUnlocked(AchievementId.SpeedRunOneSecond))
  }

  @Test
  fun savedQuizTemplateConfigurationIgnoresVariantDifferencesForDuplicates() {
    val base =
      SavedQuizTemplate(
        id = "a",
        createdAtEpochMillis = 1L,
        title = "Quiz A",
        source = CreateQuizSource.ManualCountriesCapitals,
        selectedCountryCodes = setOf("AT", "BG", "DE"),
        variants = setOf(QuizVariant.FlagToCountry),
        questionCount = 3,
        seed = 1L,
      )
    val duplicate =
      base.copy(
        id = "b",
        title = "Quiz B",
        variants = setOf(QuizVariant.CountryToFlag, QuizVariant.TypeCountryName),
        seed = 999L,
      )

    assertTrue(base.hasSameQuizConfiguration(duplicate))
  }

  @Test
  fun savedQuizTemplateConfigurationDistinguishesMixedCountryAndCapitalSelectionSets() {
    val base =
      SavedQuizTemplate(
        id = "a",
        createdAtEpochMillis = 1L,
        title = "Quiz A",
        topic = QuizTopic.Mixed,
        source = CreateQuizSource.ManualCountriesCapitals,
        selectedCountryCodes = setOf("AT", "BG"),
        selectedCapitalCountryCodes = setOf("AT", "BG"),
        variants = setOf(QuizVariant.FlagToCountry),
        questionCount = 4,
        seed = 1L,
      )
    val differentCapitalSelection =
      base.copy(
        id = "b",
        selectedCapitalCountryCodes = setOf("AT", "DE"),
      )

    assertFalse(base.hasSameQuizConfiguration(differentCapitalSelection))
  }

  @Test
  fun applyHintToCurrentQuestion_allowsHintAfterAnswerSelectionOutsideTraining() {
    val country = FlagCountry(code = "DE", name = "Germany", emoji = "🇩🇪", continent = "Europe")
    val question =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country, FlagCountry("BG", "Bulgaria", "🇧🇬", "Europe"), FlagCountry("AT", "Austria", "🇦🇹", "Europe"), FlagCountry("FR", "France", "🇫🇷", "Europe")),
        variant = QuizVariant.FlagToCountry,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        questions = listOf(question),
        questionStates = listOf(QuestionDraftState()),
        players = listOf(com.example.flaggameandroid.core.model.PlayerProgress("Solo", hintPoints = 2.0)),
      ).withSelectedCountry(country)
    val state = FlagGameUiState(quiz = quiz, settings = com.example.flaggameandroid.feature.app.SettingsState())

    val result = applyHintToCurrentQuestion(state)

    assertTrue(result != null)
    assertTrue(result!!.quiz.currentQuestionState.hintUses > 0)
  }
}

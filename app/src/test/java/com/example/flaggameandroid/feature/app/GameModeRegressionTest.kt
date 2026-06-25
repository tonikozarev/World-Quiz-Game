package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.visibleGameModes
import com.example.flaggameandroid.persistence.PersistedAppState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameModeRegressionTest {
  private fun viewModel(initialPersistedState: PersistedAppState = PersistedAppState()): FlagGameViewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = QuizQuestionGenerator(Random(11)),
      random = Random(12),
      initialPersistedState = initialPersistedState,
    )

  @Test
  fun menuNavigation_opensGameModesAndSettings() {
    val viewModel = viewModel()

    viewModel.onStartClicked()
    assertEquals(AppScreen.GameModes, viewModel.uiState.value.screen)

    viewModel.onBackToMenu()
    viewModel.onSettingsClicked()
    assertEquals(AppScreen.Settings, viewModel.uiState.value.screen)
  }

  @Test
  fun eachGameModeCanStartAQuizFromDefaultSetup() {
    visibleGameModes().filterNot { it == GameMode.MistakeReview }.forEach { mode ->
      val viewModel = viewModel()

      viewModel.onModeSelected(mode)
      viewModel.onQuestionCountChanged(6)
      viewModel.onStartQuiz()

      assertEquals("Failed to start $mode", AppScreen.Quiz, viewModel.uiState.value.screen)
      assertEquals(mode, viewModel.uiState.value.quiz.mode)
      assertTrue(viewModel.uiState.value.quiz.totalQuestions > 0)
    }
  }

  @Test
  fun visibleGameModes_useStableExplicitOrder() {
    assertEquals(
      listOf(
        GameMode.Training,
        GameMode.DailyChallenge,
        GameMode.WorldFlags,
        GameMode.Continents,
        GameMode.SpeedRun,
        GameMode.LocalMultiplayer,
        GameMode.AllIn,
        GameMode.MistakeReview,
      ),
      visibleGameModes(),
    )
  }

  @Test
  fun mistakeReviewSession_reducesReviewedCountriesToFiveAndKeepsModeUnlocked() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val initialStats =
      countries.take(10).associate { country ->
        country.code to CountryPracticeStats(wrongCount = 10)
      }
    val viewModel =
      viewModel(
        initialPersistedState =
          PersistedAppState(
            countryPracticeStats = initialStats,
            mistakeReviewUnlocked = true,
          ),
      )

    viewModel.onModeSelected(GameMode.MistakeReview)
    viewModel.onQuestionCountChanged(10)
    viewModel.onStartQuiz()

    val reviewedCountryCode = viewModel.uiState.value.quiz.currentQuestion!!.correctCountry.code
    repeat(viewModel.uiState.value.quiz.totalQuestions) {
      val question = viewModel.uiState.value.quiz.currentQuestion!!
      viewModel.onCountryAnswerSelected(question.correctCountry)
      viewModel.onNextQuestion()
    }

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(5, viewModel.uiState.value.countryPracticeStats[reviewedCountryCode]?.wrongCount)
    assertTrue(viewModel.uiState.value.mistakeReviewUnlocked)
  }

  @Test
  fun mistakeReviewMode_locksQuestionCountToEligibleCountryCount() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val initialStats =
      countries.take(12).associate { country ->
        country.code to CountryPracticeStats(wrongCount = 10)
      }
    val viewModel =
      viewModel(
        initialPersistedState =
          PersistedAppState(
            countryPracticeStats = initialStats,
            mistakeReviewUnlocked = true,
          ),
      )

    viewModel.onModeSelected(GameMode.MistakeReview)

    assertEquals("12", viewModel.uiState.value.setup.questionCountInput)
    assertEquals(12, viewModel.uiState.value.questionCountLimit)
  }

  @Test
  fun mistakeReview_restartAfterCompletionDoesNotCrashWhenPoolDropsBelowUnlockThreshold() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val initialStats =
      countries.take(10).associate { country ->
        country.code to CountryPracticeStats(wrongCount = 10)
      }
    val viewModel =
      viewModel(
        initialPersistedState =
          PersistedAppState(
            countryPracticeStats = initialStats,
            mistakeReviewUnlocked = true,
          ),
      )

    viewModel.onModeSelected(GameMode.MistakeReview)
    viewModel.onStartQuiz()

    repeat(viewModel.uiState.value.quiz.totalQuestions) {
      val question = viewModel.uiState.value.quiz.currentQuestion!!
      viewModel.onCountryAnswerSelected(question.correctCountry)
      viewModel.onNextQuestion()
    }

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertTrue(viewModel.uiState.value.countryPracticeStats.values.count { it.isMistakeReviewEligible } < 10)

    viewModel.onPlayAgain()

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals("No missed countries to review yet.", viewModel.uiState.value.setupError)
  }

  @Test
  fun continentsModeUsesOnlySelectedContinentCountries() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Continents)
    viewModel.uiState.value.availableContinents
      .filter { it != "Europe" && it != "Antarctica" }
      .forEach(viewModel::onContinentToggled)
    viewModel.onQuestionCountChanged(10)
    viewModel.onStartQuiz()

    assertEquals(AppScreen.Quiz, viewModel.uiState.value.screen)
    assertTrue(viewModel.uiState.value.quiz.questions.all { it.correctCountry.continent == "Europe" })
  }

  @Test
  fun allInUsesFullCatalogAndDefaultVariants() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.AllIn)
    viewModel.onStartQuiz()

    val quiz = viewModel.uiState.value.quiz
    assertEquals(195, quiz.totalQuestions)
    assertEquals(QuizVariant.entries.toSet(), quiz.questions.map { it.variant }.toSet())
  }

  @Test
  fun noBluffAllToughUsesFullCatalogAndSelectedVariants() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.AllIn)
    QuizVariant.entries.filterNot { it == QuizVariant.TypeCountryName }.forEach(viewModel::onVariantToggled)
    viewModel.onStartQuiz()

    val quiz = viewModel.uiState.value.quiz
    assertEquals(195, quiz.totalQuestions)
    assertTrue(quiz.questions.all { it.variant == QuizVariant.TypeCountryName })
  }

  @Test
  fun hardDifficultyAwardsHintOnlyAfterTenCorrectInARow() {
    val viewModel = viewModel()

    viewModel.onHintDifficultySelected(HintDifficulty.Hard)
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 10)
    repeat(9) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }
    assertEquals(0, viewModel.uiState.value.quiz.currentPlayer.earnedHintPoints)

    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(1, viewModel.uiState.value.hintCount)
  }

  @Test
  fun impossibleDifficultyAwardsHintOnlyAfterFiftyCorrectInARow() {
    val viewModel = viewModel()

    viewModel.onHintDifficultySelected(HintDifficulty.Impossible)
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 50)
    repeat(50) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(1, viewModel.uiState.value.hintCount)
  }

  private fun startSingleVariantQuiz(
    viewModel: FlagGameViewModel,
    variant: QuizVariant,
    count: Int,
  ) {
    viewModel.onModeSelected(GameMode.Training)
    QuizVariant.entries.filterNot { it == variant }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(count)
    viewModel.onStartQuiz()
  }

  private fun answerCurrentCorrectly(viewModel: FlagGameViewModel) {
    val question = viewModel.uiState.value.quiz.currentQuestion!!
    when (question.variant) {
      QuizVariant.TypeCountryName -> viewModel.onTypedAnswerChanged(question.correctCountry.name)
      QuizVariant.FlagToCountry,
      QuizVariant.CountryToFlag -> viewModel.onCountryAnswerSelected(question.correctCountry)
    }
  }
}

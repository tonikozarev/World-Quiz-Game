package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameModeRegressionTest {
  private fun viewModel(): FlagGameViewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = QuizQuestionGenerator(Random(11)),
      random = Random(12),
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
    GameMode.entries.forEach { mode ->
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
  fun allInHardcoreUsesFullCatalogAndAllVariants() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.AllIn)
    viewModel.onAllInTypeSelected(AllInType.Hardcore)
    viewModel.onStartQuiz()

    val quiz = viewModel.uiState.value.quiz
    assertEquals(195, quiz.totalQuestions)
    assertEquals(QuizVariant.entries.toSet(), quiz.questions.map { it.variant }.toSet())
  }

  @Test
  fun noBluffAllToughUsesFullCatalogAndSelectedVariants() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.AllIn)
    viewModel.onAllInTypeSelected(AllInType.NoBluffAllTough)
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

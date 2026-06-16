package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class FlagGameViewModelTest {
  private fun viewModel(): FlagGameViewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = QuizQuestionGenerator(Random(3)),
      random = Random(4),
    )

  @Test
  fun trainingMode_startsConfigurableQuiz() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    viewModel.onQuestionCountChanged(12)
    viewModel.onStartQuiz()

    val state = viewModel.uiState.value
    assertEquals(AppScreen.Quiz, state.screen)
    assertEquals(GameMode.Training, state.quiz.mode)
    assertEquals(12, state.quiz.totalQuestions)
  }

  @Test
  fun setup_showsSevenContinentsIncludingSeparateAmericas() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Continents)

    assertEquals(
      listOf("Africa", "Antarctica", "Asia", "Europe", "North America", "Oceania", "South America"),
      viewModel.uiState.value.availableContinents,
    )
    assertTrue("Antarctica" !in viewModel.uiState.value.setup.selectedContinents)
  }

  @Test
  fun surpriseMeClearsCountAndCanBeDeselectedForCustomAmount() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    viewModel.onSurpriseMeClicked()
    assertEquals("", viewModel.uiState.value.setup.questionCountInput)
    assertTrue(viewModel.uiState.value.setup.surpriseMe)

    viewModel.onSurpriseMeClicked()
    viewModel.onQuestionCountChanged("14")
    viewModel.onStartQuiz()

    val state = viewModel.uiState.value
    assertEquals(AppScreen.Quiz, state.screen)
    assertEquals(14, state.quiz.totalQuestions)
  }

  @Test
  fun changedAnswerBeforeNext_usesFinalSelectionForScore() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 3)

    val question = viewModel.uiState.value.quiz.currentQuestion!!
    val correct = question.correctCountry
    val wrong = question.options.first { it.code != correct.code }

    viewModel.onCountryAnswerSelected(correct)
    assertEquals(0, viewModel.uiState.value.quiz.currentPlayer.score)

    viewModel.onCountryAnswerSelected(wrong)
    viewModel.onNextQuestion()

    assertEquals(0, viewModel.uiState.value.quiz.players.first().score)
    assertEquals(false, viewModel.uiState.value.quiz.results.first().isCorrect)
  }

  @Test
  fun fiveCorrectInARow_keepsNewHintPointLockedUntilQuizEnds() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 6)

    repeat(5) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val player = viewModel.uiState.value.quiz.players.first()
    assertEquals(5, player.score)
    assertEquals(0, player.hintPoints)
    assertEquals(1, player.earnedHintPoints)
    assertEquals(5, player.correctStreak)
  }

  @Test
  fun earnedHintPointsBecomeUsableAfterQuizEnds() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 5)

    repeat(5) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val player = viewModel.uiState.value.quiz.players.first()
    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(1, player.hintPoints)
    assertEquals(0, player.earnedHintPoints)
    assertEquals(1, viewModel.uiState.value.hintCount)
  }

  @Test
  fun wrongAnswerResetsCorrectStreakBeforeHintIsEarned() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 8)

    repeat(4) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }
    answerCurrentWrongly(viewModel)
    viewModel.onNextQuestion()
    repeat(3) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val player = viewModel.uiState.value.quiz.players.first()
    assertEquals(7, player.score)
    assertEquals(0, player.hintPoints)
    assertEquals(0, player.earnedHintPoints)
    assertEquals(3, player.correctStreak)
  }

  @Test
  fun hintCostsOneOldUsableHint() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 5)

    repeat(5) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 2)

    assertEquals(1, viewModel.uiState.value.quiz.currentPlayer.hintPoints)
    viewModel.onUseHint()

    val quiz = viewModel.uiState.value.quiz
    assertEquals(0, quiz.currentPlayer.hintPoints)
    assertTrue(quiz.hintUsedOnCurrentQuestion)
    assertEquals(2, quiz.hiddenOptionCodes.size)
  }

  @Test
  fun rookieDifficultyAwardsHintForEveryCorrectAnswerAfterQuizEnds() {
    val viewModel = viewModel()

    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 3)

    repeat(3) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(3, viewModel.uiState.value.hintCount)
  }

  @Test
  fun localMultiplayerCanSpendExistingHintsButDoesNotCollectNewHints() {
    val viewModel = viewModel()

    viewModel.onTestingToolsVisibleChanged(true)
    viewModel.onAddTestingHintsClicked()
    viewModel.onModeSelected(GameMode.LocalMultiplayer)
    viewModel.onQuestionCountChanged(2)
    viewModel.onStartQuiz()

    assertEquals(10, viewModel.uiState.value.quiz.currentPlayer.hintPoints)
    viewModel.onUseHint()
    assertEquals(9, viewModel.uiState.value.hintCount)

    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()
    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(9, viewModel.uiState.value.hintCount)
    assertEquals(0, viewModel.uiState.value.quiz.players.sumOf { it.earnedHintPoints })
  }

  @Test
  fun settingsCanResetAndAddTestingHints() {
    val viewModel = viewModel()

    viewModel.onAddTestingHintsClicked()
    assertEquals(10, viewModel.uiState.value.hintCount)

    viewModel.onResetHintsClicked()
    assertEquals(0, viewModel.uiState.value.hintCount)
  }

  @Test
  fun testingHintsDoNotAdvanceLevelProgress() {
    val viewModel = viewModel()

    viewModel.onAddTestingHintsClicked()

    val progress = viewModel.uiState.value.levelProgress
    assertEquals(1, progress.level)
    assertEquals(0, progress.hintsTowardNextLevel)
    assertEquals(0, progress.correctAnswersTowardNextLevel)
    assertEquals(0, progress.eligibleQuizzesTowardNextLevel)
  }

  @Test
  fun levelProgress_requiresHintsCorrectAnswersAndFiftyEligibleQuizzes() {
    val viewModel = viewModel()
    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)

    repeat(49) {
      completePerfectQuiz(viewModel, questionCount = 10)
    }

    assertEquals(1, viewModel.uiState.value.levelProgress.level)
    assertEquals(49, viewModel.uiState.value.levelProgress.eligibleQuizzesTowardNextLevel)

    completePerfectQuiz(viewModel, questionCount = 10)

    val state = viewModel.uiState.value
    assertEquals(2, state.levelProgress.level)
    assertTrue(state.levelProgress.levelUpVisible)
    assertEquals(505, state.hintCount)
    assertEquals(0, state.levelProgress.eligibleQuizzesTowardNextLevel)
  }

  @Test
  fun levelUpSeen_hidesLevelUpCelebration() {
    val viewModel = viewModel()
    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)

    repeat(50) {
      completePerfectQuiz(viewModel, questionCount = 10)
    }

    assertTrue(viewModel.uiState.value.levelProgress.levelUpVisible)
    viewModel.onLevelUpSeen()
    assertEquals(false, viewModel.uiState.value.levelProgress.levelUpVisible)
  }

  @Test
  fun continentSelection_updatesQuestionCountLimitAndRejectsTooManyQuestions() {
    val viewModel = viewModel()
    val europeCount = StaticFlagCatalogRepository().getCountries().count { it.continent == "Europe" }

    viewModel.onModeSelected(GameMode.Continents)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)

    assertEquals(europeCount, viewModel.uiState.value.questionCountLimit)

    viewModel.onQuestionCountChanged(europeCount + 1)
    viewModel.onStartQuiz()

    assertEquals(AppScreen.Setup, viewModel.uiState.value.screen)
    assertEquals("Question count must be between 1 and $europeCount.", viewModel.uiState.value.setupError)
  }

  @Test
  fun surpriseMe_usesSelectedCountryPoolRange() {
    val viewModel = viewModel()
    val europeCount = StaticFlagCatalogRepository().getCountries().count { it.continent == "Europe" }

    viewModel.onModeSelected(GameMode.Continents)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)
    viewModel.onSurpriseMeClicked()
    viewModel.onStartQuiz()

    val totalQuestions = viewModel.uiState.value.quiz.totalQuestions
    assertTrue(totalQuestions in 1..europeCount)
  }

  @Test
  fun skipQuestion_keepsScoreNeutralAndMovesForward() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 2)

    viewModel.onSkipQuestion()

    val state = viewModel.uiState.value
    assertEquals(0, state.quiz.players.first().score)
    assertEquals(1, state.quiz.currentQuestionIndex)
    assertTrue(state.quiz.results.first().skipped)
  }

  @Test
  fun multiplayer_rotatesTurnsAndKeepsSeparateScores() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.LocalMultiplayer)
    viewModel.onQuestionCountChanged(4)
    viewModel.onStartQuiz()

    assertEquals("Player 1", viewModel.uiState.value.quiz.currentPlayer.name)
    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()

    assertEquals("Player 2", viewModel.uiState.value.quiz.currentPlayer.name)
    answerCurrentWrongly(viewModel)
    viewModel.onNextQuestion()

    val players = viewModel.uiState.value.quiz.players
    assertEquals(1, players.first { it.name == "Player 1" }.score)
    assertEquals(0, players.first { it.name == "Player 2" }.score)
    assertEquals("Player 1", viewModel.uiState.value.quiz.currentPlayer.name)
  }

  @Test
  fun typedAnswer_acceptsAliasCaseAndWhitespace() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    QuizVariant.entries.filterNot { it == QuizVariant.TypeCountryName }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(1)
    viewModel.onStartQuiz()

    val country = viewModel.uiState.value.quiz.currentQuestion!!.correctCountry
    val answer = country.aliases.firstOrNull() ?: country.name
    viewModel.onTypedAnswerChanged("  ${answer.uppercase()}  ")
    viewModel.onNextQuestion()

    assertTrue(viewModel.uiState.value.quiz.results.first().isCorrect)
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

  private fun completePerfectQuiz(
    viewModel: FlagGameViewModel,
    questionCount: Int,
  ) {
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = questionCount)
    repeat(questionCount) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }
  }

  private fun answerCurrentCorrectly(viewModel: FlagGameViewModel) {
    val question = viewModel.uiState.value.quiz.currentQuestion!!
    when (question.variant) {
      QuizVariant.TypeCountryName -> viewModel.onTypedAnswerChanged(question.correctCountry.name)
      QuizVariant.FlagToCountry,
      QuizVariant.CountryToFlag -> viewModel.onCountryAnswerSelected(question.correctCountry)
    }
  }

  private fun answerCurrentWrongly(viewModel: FlagGameViewModel) {
    val question = viewModel.uiState.value.quiz.currentQuestion!!
    when (question.variant) {
      QuizVariant.TypeCountryName -> viewModel.onTypedAnswerChanged("wrong answer")
      QuizVariant.FlagToCountry,
      QuizVariant.CountryToFlag -> viewModel.onCountryAnswerSelected(question.options.first { it.code != question.correctCountry.code })
    }
  }
}

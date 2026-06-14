package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.DefaultFlagQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.GameMode
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FlagGameViewModelTest {
  private val viewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = DefaultFlagQuestionGenerator(),
    )

  @Test
  fun multipleChoiceMode_startsQuizWithQuestions() {
    viewModel.onMultipleChoiceModeSelected()

    val state = viewModel.uiState.value
    assertEquals(AppScreen.Quiz, state.screen)
    assertEquals(GameMode.MultipleChoice, state.quiz.mode)
    assertEquals(5, state.quiz.totalQuestions)
    assertTrue(state.quiz.currentQuestion != null)
  }

  @Test
  fun answerSelection_tracksScoreAndRevealState() {
    viewModel.onBackToMenu()
    viewModel.onMultipleChoiceModeSelected()

    val firstQuestion = viewModel.uiState.value.quiz.currentQuestion!!
    viewModel.onAnswerSelected(firstQuestion.options.first { it.code == firstQuestion.flag.code })

    val answeredState = viewModel.uiState.value
    assertTrue(answeredState.quiz.answerRevealed)
    assertEquals(1, answeredState.quiz.score)
    assertEquals(firstQuestion.flag.code, answeredState.quiz.selectedAnswer?.code)
    assertEquals(1, answeredState.quiz.results.size)
  }
}

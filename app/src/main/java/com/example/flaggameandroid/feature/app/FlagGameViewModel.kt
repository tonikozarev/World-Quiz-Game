package com.example.flaggameandroid.feature.app

import androidx.lifecycle.ViewModel
import com.example.flaggameandroid.core.data.DefaultFlagQuestionGenerator
import com.example.flaggameandroid.core.data.FlagCatalogRepository
import com.example.flaggameandroid.core.data.FlagQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuestionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FlagGameViewModel(
  private val catalogRepository: FlagCatalogRepository = StaticFlagCatalogRepository(),
  private val questionGenerator: FlagQuestionGenerator = DefaultFlagQuestionGenerator(),
) : ViewModel() {
  private val _uiState = MutableStateFlow(FlagGameUiState())
  val uiState: StateFlow<FlagGameUiState> = _uiState.asStateFlow()

  fun onGameModesClicked() {
    _uiState.update { it.copy(screen = AppScreen.GameModes) }
  }

  fun onHowToPlayClicked() {
    _uiState.update { it.copy(instructionsExpanded = !it.instructionsExpanded) }
  }

  fun onBackToMenu() {
    _uiState.value = FlagGameUiState()
  }

  fun onMultipleChoiceModeSelected() {
    val questions = questionGenerator.buildMultipleChoiceQuestions(
      countries = catalogRepository.getCountries(),
      totalQuestions = 5,
    )
    _uiState.update {
      it.copy(
        screen = AppScreen.Quiz,
        quiz =
          QuizState(
            mode = GameMode.MultipleChoice,
            questions = questions,
          ),
      )
    }
  }

  fun onAnswerSelected(answer: FlagCountry) {
    val state = _uiState.value
    val question = state.quiz.currentQuestion ?: return
    if (state.quiz.answerRevealed) return

    val isCorrect = answer.code == question.flag.code
    _uiState.update {
      it.copy(
        quiz =
          it.quiz.copy(
            score = it.quiz.score + if (isCorrect) 1 else 0,
            selectedAnswer = answer,
            answerRevealed = true,
            results =
              it.quiz.results +
                QuestionResult(
                  question = question,
                  selectedAnswer = answer,
                  isCorrect = isCorrect,
                ),
          ),
      )
    }
  }

  fun onNextQuestion() {
    val state = _uiState.value
    val quiz = state.quiz
    if (!quiz.answerRevealed) return

    if (quiz.isLastQuestion) {
      _uiState.update { it.copy(screen = AppScreen.Results) }
    } else {
      _uiState.update {
        it.copy(
          quiz =
            it.quiz.copy(
              currentQuestionIndex = it.quiz.currentQuestionIndex + 1,
              selectedAnswer = null,
              answerRevealed = false,
            ),
        )
      }
    }
  }

  fun onReplayMultipleChoice() {
    onMultipleChoiceModeSelected()
  }
}

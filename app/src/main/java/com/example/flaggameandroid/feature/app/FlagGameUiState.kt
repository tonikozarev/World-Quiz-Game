package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuestionResult

sealed interface AppScreen {
  data object Menu : AppScreen

  data object GameModes : AppScreen

  data object Quiz : AppScreen

  data object Results : AppScreen
}

data class QuizState(
  val mode: GameMode? = null,
  val questions: List<FlagQuestion> = emptyList(),
  val currentQuestionIndex: Int = 0,
  val score: Int = 0,
  val selectedAnswer: FlagCountry? = null,
  val answerRevealed: Boolean = false,
  val results: List<QuestionResult> = emptyList(),
) {
  val currentQuestion: FlagQuestion?
    get() = questions.getOrNull(currentQuestionIndex)

  val totalQuestions: Int
    get() = questions.size

  val isLastQuestion: Boolean
    get() = currentQuestionIndex >= questions.lastIndex
}

data class FlagGameUiState(
  val screen: AppScreen = AppScreen.Menu,
  val quiz: QuizState = QuizState(),
  val instructionsExpanded: Boolean = false,
)

package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizVariant

sealed interface AppScreen {
  data object Menu : AppScreen

  data object GameModes : AppScreen

  data object Settings : AppScreen

  data object Setup : AppScreen

  data object Quiz : AppScreen

  data object Results : AppScreen
}

enum class MultiplayerQuizBase(
  val title: String,
) {
  Continents("Continents setup"),
  AllIn("All-In setup"),
}

data class SettingsState(
  val hintDifficulty: HintDifficulty = HintDifficulty.Medium,
  val testingToolsVisible: Boolean = false,
)

data class SetupState(
  val mode: GameMode = GameMode.Training,
  val variants: Set<QuizVariant> = QuizVariant.entries.toSet(),
  val selectedContinents: Set<String> = emptySet(),
  val questionCountInput: String = "10",
  val surpriseMe: Boolean = false,
  val allInType: AllInType = AllInType.Hardcore,
  val multiplayerBase: MultiplayerQuizBase = MultiplayerQuizBase.Continents,
  val playerNames: List<String> = listOf("Player 1", "Player 2"),
) {
  val questionCount: Int?
    get() = questionCountInput.toIntOrNull()

  val needsContinents: Boolean
    get() = mode == GameMode.Continents

  val needsPlayers: Boolean
    get() = mode == GameMode.LocalMultiplayer
}

data class QuizState(
  val mode: GameMode? = null,
  val allInType: AllInType? = null,
  val questions: List<FlagQuestion> = emptyList(),
  val currentQuestionIndex: Int = 0,
  val players: List<PlayerProgress> = listOf(PlayerProgress("Solo")),
  val currentPlayerIndex: Int = 0,
  val selectedCountry: FlagCountry? = null,
  val typedAnswer: String = "",
  val hiddenOptionCodes: Set<String> = emptySet(),
  val typedHintPrefix: String? = null,
  val hintUsedOnCurrentQuestion: Boolean = false,
  val results: List<QuestionResult> = emptyList(),
) {
  val currentQuestion: FlagQuestion?
    get() = questions.getOrNull(currentQuestionIndex)

  val currentPlayer: PlayerProgress
    get() = players.getOrElse(currentPlayerIndex) { PlayerProgress("Solo") }

  val totalQuestions: Int
    get() = questions.size

  val isLastQuestion: Boolean
    get() = currentQuestionIndex >= questions.lastIndex

  val isMultiplayer: Boolean
    get() = players.size > 1
}

data class LevelProgressState(
  val level: Int = 1,
  val hintsTowardNextLevel: Int = 0,
  val correctAnswersTowardNextLevel: Int = 0,
  val eligibleQuizzesTowardNextLevel: Int = 0,
  val levelUpVisible: Boolean = false,
) {
  val hintsNeeded: Int = 20
  val correctAnswersNeeded: Int = 200
  val eligibleQuizzesNeeded: Int = 50

  val progressFraction: Float
    get() =
      minOf(
        hintsTowardNextLevel.toFloat() / hintsNeeded,
        correctAnswersTowardNextLevel.toFloat() / correctAnswersNeeded,
        eligibleQuizzesTowardNextLevel.toFloat() / eligibleQuizzesNeeded,
      ).coerceIn(0f, 1f)
}

data class FlagGameUiState(
  val screen: AppScreen = AppScreen.Menu,
  val settings: SettingsState = SettingsState(),
  val setup: SetupState = SetupState(),
  val quiz: QuizState = QuizState(),
  val availableContinents: List<String> = emptyList(),
  val questionCountLimit: Int = 195,
  val levelProgress: LevelProgressState = LevelProgressState(),
  val hintCount: Int = 0,
  val setupError: String? = null,
)

package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.SavedQuizTemplate
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.DailyChallengeTheme
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.RatingsProgress

sealed interface AppScreen {
  data object Menu : AppScreen

  data object GameModes : AppScreen

  data object GameModesHub : AppScreen

  data object Medals : AppScreen

  data object Achievements : AppScreen

  data object Favorites : AppScreen

  data object Settings : AppScreen

  data object Setup : AppScreen

  data object Quiz : AppScreen

  data object Results : AppScreen
}

enum class AppLanguage(
  val title: String,
) {
  English("English"),
  Bulgarian("Bulgarian"),
  German("German"),
}

data class ProfileState(
  val accountName: String = "",
  val avatarIndex: Int = 0,
) {
  val displayName: String
    get() = accountName.ifBlank { "Player 1" }
}

enum class MultiplayerQuizBase(
  val title: String,
) {
  Continents("Continents setup"),
  AllIn("All-In setup"),
}

data class SettingsState(
  val hintDifficulty: HintDifficulty = HintDifficulty.Medium,
  val reminderEnabled: Boolean = true,
  val testingToolsVisible: Boolean = false,
  val language: AppLanguage = AppLanguage.English,
)

data class SetupState(
  val mode: GameMode = GameMode.Training,
  val variants: Set<QuizVariant> = QuizVariant.entries.toSet(),
  val topic: QuizTopic = QuizTopic.Countries,
  val selectedContinents: Set<String> = emptySet(),
  val instantCorrectionEnabled: Boolean = true,
  val worldFlagsHardcoreEnabled: Boolean = false,
  val worldFlagsTimerEnabled: Boolean = false,
  val createQuizTrainingEnabled: Boolean = false,
  val createQuizManualHardcoreEnabled: Boolean = false,
  val createQuizLocalMultiplayerEnabled: Boolean = false,
  val createQuizManualTimerEnabled: Boolean = false,
  val createQuizSource: CreateQuizSource = CreateQuizSource.PresetFilter,
  val createQuizPreset: CreateQuizPreset = CreateQuizPreset.TwoColors,
  val createQuizPresets: Set<CreateQuizPreset> =
    setOf(
      CreateQuizPreset.TwoColors,
      CreateQuizPreset.ThreeColors,
      CreateQuizPreset.FourPlusColors,
    ),
  val selectedCountryCodes: Set<String> = emptySet(),
  val selectedCapitalCountryCodes: Set<String> = emptySet(),
  val createQuizSeed: Long = 0L,
  val savedQuizTemplateId: String? = null,
  val questionCountInput: String = "10",
  val speedRunSecondsPerAnswerInput: String = "5",
  val surpriseMe: Boolean = false,
  val allInType: AllInType = AllInType.NoBluffAllTough,
  val multiplayerBase: MultiplayerQuizBase = MultiplayerQuizBase.Continents,
  val playerNames: List<String> = listOf("Player 1", "Player 2"),
  val dailyChallengeTheme: DailyChallengeTheme? = null,
) {
  val questionCount: Int?
    get() = questionCountInput.toIntOrNull()

  val speedRunSecondsPerAnswer: Int?
    get() = speedRunSecondsPerAnswerInput.toIntOrNull()

  val needsContinents: Boolean
    get() =
      mode == GameMode.WorldFlags ||
        (mode == GameMode.LocalMultiplayer && multiplayerBase == MultiplayerQuizBase.Continents)

  val needsPlayers: Boolean
    get() = mode == GameMode.LocalMultiplayer || (mode == GameMode.CreateQuiz && createQuizLocalMultiplayerEnabled)

  val needsManualCountriesCapitals: Boolean
    get() = mode == GameMode.CreateQuiz && createQuizSource == CreateQuizSource.ManualCountriesCapitals

  val usesWorldFlagsHardcore: Boolean
    get() = mode == GameMode.WorldFlags && worldFlagsHardcoreEnabled

  val usesWorldFlagsTimer: Boolean
    get() = mode == GameMode.WorldFlags && worldFlagsTimerEnabled

  val usesCreateQuizManualHardcore: Boolean
    get() = mode == GameMode.CreateQuiz && createQuizManualHardcoreEnabled

  val usesCreateQuizTraining: Boolean
    get() = mode == GameMode.CreateQuiz && createQuizTrainingEnabled

  val usesCreateQuizLocalMultiplayer: Boolean
    get() = mode == GameMode.CreateQuiz && createQuizLocalMultiplayerEnabled

  val usesCreateQuizManualTimer: Boolean
    get() = mode == GameMode.CreateQuiz && createQuizManualTimerEnabled

  val createQuizMixedSelectionCount: Int
    get() = selectedCountryCodes.size + selectedCapitalCountryCodes.size
}

enum class QuestionStatus {
  Unanswered,
  Answered,
  Skipped,
}

data class QuestionDraftState(
  val status: QuestionStatus = QuestionStatus.Unanswered,
  val selectedCountry: FlagCountry? = null,
  val typedAnswer: String = "",
  val hiddenOptionCodes: Set<String> = emptySet(),
  val typedHintPrefix: String? = null,
  val hintUses: Int = 0,
  val hintUsed: Boolean = false,
  val revealed: Boolean = false,
  val locked: Boolean = false,
)

data class QuizState(
  val mode: GameMode? = null,
  val allInType: AllInType? = null,
  val variants: Set<QuizVariant> = emptySet(),
  val topic: QuizTopic = QuizTopic.Countries,
  val selectedContinents: Set<String> = emptySet(),
  val instantCorrectionEnabled: Boolean = false,
  val questions: List<FlagQuestion> = emptyList(),
  val currentQuestionIndex: Int = 0,
  val questionStates: List<QuestionDraftState> = emptyList(),
  val players: List<PlayerProgress> = listOf(PlayerProgress("Solo")),
  val currentPlayerIndex: Int = 0,
  val selectedCountry: FlagCountry? = null,
  val typedAnswer: String = "",
  val hiddenOptionCodes: Set<String> = emptySet(),
  val typedHintPrefix: String? = null,
  val hintUsedOnCurrentQuestion: Boolean = false,
  val hintsAllowed: Boolean = true,
  val startedAtEpochMillis: Long = 0L,
  val speedRunSecondsPerAnswer: Int = 5,
  val speedRunPenaltySeconds: Int = 0,
  val countdownEnabled: Boolean = false,
  val timedOut: Boolean = false,
  val poolSource: com.example.flaggameandroid.core.model.QuizPoolSource = com.example.flaggameandroid.core.model.QuizPoolSource.Standard,
  val dailyChallengeTheme: com.example.flaggameandroid.core.model.DailyChallengeTheme? = null,
  val quizSeed: Long = 0L,
  val savedQuizTemplateId: String? = null,
  val results: List<QuestionResult> = emptyList(),
) {
  val currentQuestion: FlagQuestion?
    get() = questions.getOrNull(currentQuestionIndex)

  val currentQuestionState: QuestionDraftState
    get() = questionStates.getOrElse(currentQuestionIndex) { QuestionDraftState() }

  val currentPlayer: PlayerProgress
    get() = players.getOrElse(currentPlayerIndex) { PlayerProgress("Solo") }

  val totalQuestions: Int
    get() = questions.size

  val isLastQuestion: Boolean
    get() = currentQuestionIndex >= questions.lastIndex

  val isMultiplayer: Boolean
    get() = players.size > 1

  val canFinish: Boolean
    get() {
      if (questions.isEmpty() || questionStates.size != questions.size) return false
      return questionStates.withIndex().all { (index, questionState) ->
        questionState.status == QuestionStatus.Answered ||
          (index == currentQuestionIndex && currentQuestionHasPendingAnswer)
      }
    }

  val currentQuestionHasPendingAnswer: Boolean
    get() =
      currentQuestion?.let { question ->
        when (question.variant) {
          QuizVariant.TypeText -> typedAnswer.isNotBlank()
          QuizVariant.FlagToText,
          QuizVariant.TextToFlag -> selectedCountry != null
        }
      } == true
}

data class LevelProgressState(
  val level: Int = 1,
  val hintsTowardNextLevel: Int = 0,
  val correctAnswersTowardNextLevel: Int = 0,
  val eligibleQuizzesTowardNextLevel: Int = 0,
  val levelUpVisible: Boolean = false,
) {
  val hintsNeeded: Int
    get() = ProgressionRules.requirementsForLevel(level).hintsNeeded

  val hintsTowardNextLevelDisplay: Int
    get() = hintsTowardNextLevel.coerceAtMost(hintsNeeded)

  val correctAnswersNeeded: Int
    get() = ProgressionRules.requirementsForLevel(level).correctAnswersNeeded

  val correctAnswersTowardNextLevelDisplay: Int
    get() = correctAnswersTowardNextLevel.coerceAtMost(correctAnswersNeeded)

  val eligibleQuizzesNeeded: Int
    get() = ProgressionRules.requirementsForLevel(level).eligibleQuizzesNeeded

  val eligibleQuizzesTowardNextLevelDisplay: Int
    get() = eligibleQuizzesTowardNextLevel.coerceAtMost(eligibleQuizzesNeeded)

  val isMaxLevel: Boolean
    get() = ProgressionRules.isMaxLevel(level)

  val progressFraction: Float
    get() =
      if (isMaxLevel) {
        1f
      } else {
        val hintProgress = (hintsTowardNextLevelDisplay.toFloat() / hintsNeeded).coerceIn(0f, 1f)
        val correctProgress = (correctAnswersTowardNextLevelDisplay.toFloat() / correctAnswersNeeded).coerceIn(0f, 1f)
        val quizProgress = (eligibleQuizzesTowardNextLevelDisplay.toFloat() / eligibleQuizzesNeeded).coerceIn(0f, 1f)
        (
          (hintProgress * 0.33f) +
            (correctProgress * 0.34f) +
            (quizProgress * 0.33f)
          ).coerceIn(0f, 1f)
      }
}

data class FlagGameUiState(
  val screen: AppScreen = AppScreen.Menu,
  val quizReturnTarget: AppScreen = AppScreen.GameModes,
  val selectedQuizTopic: QuizTopic = QuizTopic.Countries,
  val settings: SettingsState = SettingsState(),
  val setup: SetupState = SetupState(),
  val quiz: QuizState = QuizState(),
  val availableContinents: List<String> = emptyList(),
  val questionCountLimit: Int = 195,
  val levelProgress: LevelProgressState = LevelProgressState(),
  val profile: ProfileState = ProfileState(),
  val countries: List<FlagCountry> = emptyList(),
  val hintCount: Double = 0.0,
  val ratings: RatingsProgress = RatingsProgress(),
  val achievements: AchievementsProgress = AchievementsProgress(),
  val setupError: String? = null,
  val lastOpenedAtEpochMillis: Long = 0L,
  val lastPlayedAtEpochMillis: Long = 0L,
  val inactiveIconActive: Boolean = false,
  val countryPracticeStats: Map<String, CountryPracticeStats> = emptyMap(),
  val activityCalendar: Map<Long, ActivityDayRecord> = emptyMap(),
  val dailyChallengeCaches: Map<QuizTopic, DailyChallengeCache> = emptyMap(),
  val mistakeReviewUnlocked: Boolean = false,
  val savedQuizTemplates: List<SavedQuizTemplate> = emptyList(),
) {
  val dailyChallengeCache: DailyChallengeCache?
    get() = dailyChallengeCaches[QuizTopic.Mixed] ?: dailyChallengeCaches[QuizTopic.Countries]

  fun dailyChallengeCacheFor(topic: QuizTopic): DailyChallengeCache? = dailyChallengeCaches[topic]
}

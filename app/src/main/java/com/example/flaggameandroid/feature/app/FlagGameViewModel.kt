package com.example.flaggameandroid.feature.app

import androidx.lifecycle.ViewModel
import com.example.flaggameandroid.core.data.FlagCatalogRepository
import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class FlagGameViewModel(
  private val catalogRepository: FlagCatalogRepository = StaticFlagCatalogRepository(),
  private val questionGenerator: QuizQuestionGenerator = QuizQuestionGenerator(),
  private val random: Random = Random.Default,
) : ViewModel() {
  private val countries = catalogRepository.getCountries()
  private val allContinents =
    listOf(
      "Africa",
      "Antarctica",
      "Asia",
      "Europe",
      "North America",
      "Oceania",
      "South America",
    )
  private val selectableContinents = allContinents - "Antarctica"

  private val _uiState =
    MutableStateFlow(
      FlagGameUiState(
        availableContinents = allContinents,
        setup = SetupState(selectedContinents = selectableContinents.toSet()),
        questionCountLimit = countries.size,
      ),
    )
  val uiState: StateFlow<FlagGameUiState> = _uiState.asStateFlow()

  fun onBackToMenu() {
    val state = _uiState.value
    _uiState.value =
      FlagGameUiState(
        availableContinents = allContinents,
        setup = SetupState(selectedContinents = selectableContinents.toSet()),
        questionCountLimit = countries.size,
        settings = state.settings,
        hintCount = state.hintCount,
        levelProgress = state.levelProgress,
      )
  }

  fun onStartClicked() {
    _uiState.update { it.copy(screen = AppScreen.GameModes, setupError = null) }
  }

  fun onSettingsClicked() {
    _uiState.update { it.copy(screen = AppScreen.Settings, setupError = null) }
  }

  fun onLevelUpSeen() {
    _uiState.update { it.copy(levelProgress = it.levelProgress.copy(levelUpVisible = false)) }
  }

  fun onModeSelected(mode: GameMode) {
    _uiState.update {
      val setup =
        SetupState(
          mode = mode,
          selectedContinents = selectableContinents.toSet(),
          questionCountInput = if (mode == GameMode.AllIn) countries.size.toString() else "10",
          playerNames = listOf("Player 1", "Player 2"),
        )
      it.copy(
        screen = AppScreen.Setup,
        setup = setup,
        questionCountLimit = questionLimitFor(setup),
        setupError = null,
      )
    }
  }

  fun onHintDifficultySelected(difficulty: HintDifficulty) {
    _uiState.update { it.copy(settings = it.settings.copy(hintDifficulty = difficulty)) }
  }

  fun onResetHintsClicked() {
    _uiState.update {
      it.copy(
        hintCount = 0,
        quiz = it.quiz.copy(players = it.quiz.players.map { player -> player.copy(hintPoints = 0) }),
      )
    }
  }

  fun onTestingToolsVisibleChanged(visible: Boolean) {
    _uiState.update { it.copy(settings = it.settings.copy(testingToolsVisible = visible)) }
  }

  fun onAddTestingHintsClicked() {
    _uiState.update {
      val newHintCount = it.hintCount + 10
      it.copy(
        hintCount = newHintCount,
        quiz = it.quiz.copy(players = it.quiz.players.map { player -> player.copy(hintPoints = newHintCount) }),
      )
    }
  }

  fun onVariantToggled(variant: QuizVariant) {
    _uiState.update {
      val current = it.setup.variants
      val next = if (variant in current) current - variant else current + variant
      it.copy(setup = it.setup.copy(variants = next), setupError = null)
    }
  }

  fun onContinentToggled(continent: String) {
    _uiState.update {
      val current = it.setup.selectedContinents
      val next = if (continent in current) current - continent else current + continent
      val setup = it.setup.copy(selectedContinents = next)
      it.copy(setup = setup, questionCountLimit = questionLimitFor(setup), setupError = null)
    }
  }

  fun onQuestionCountChanged(questionCount: Int) {
    onQuestionCountChanged(questionCount.toString())
  }

  fun onQuestionCountChanged(questionCount: String) {
    _uiState.update {
      it.copy(
        setup = it.setup.copy(questionCountInput = questionCount.filter { char -> char.isDigit() }, surpriseMe = false),
        setupError = null,
      )
    }
  }

  fun onSurpriseMeClicked() {
    _uiState.update {
      val surpriseMe = !it.setup.surpriseMe
      it.copy(
        setup =
          it.setup.copy(
            surpriseMe = surpriseMe,
            questionCountInput = if (surpriseMe) "" else it.setup.questionCountInput,
          ),
        setupError = null,
      )
    }
  }

  fun onAllInTypeSelected(allInType: AllInType) {
    _uiState.update {
      val variants =
        if (allInType == AllInType.Hardcore) {
          QuizVariant.entries.toSet()
        } else {
          it.setup.variants.ifEmpty { QuizVariant.entries.toSet() }
        }
      it.copy(setup = it.setup.copy(allInType = allInType, variants = variants), setupError = null)
    }
  }

  fun onMultiplayerBaseSelected(base: MultiplayerQuizBase) {
    _uiState.update {
      val setup = it.setup.copy(multiplayerBase = base)
      it.copy(setup = setup, questionCountLimit = questionLimitFor(setup), setupError = null)
    }
  }

  fun onPlayerNameChanged(
    index: Int,
    name: String,
  ) {
    _uiState.update {
      val names = it.setup.playerNames.toMutableList()
      if (index in names.indices) names[index] = name
      it.copy(setup = it.setup.copy(playerNames = names), setupError = null)
    }
  }

  fun onAddPlayer() {
    _uiState.update {
      if (it.setup.playerNames.size >= 5) {
        it.copy(setupError = "Local multiplayer supports up to 5 players.")
      } else {
        it.copy(
          setup = it.setup.copy(playerNames = it.setup.playerNames + "Player ${it.setup.playerNames.size + 1}"),
          setupError = null,
        )
      }
    }
  }

  fun onRemovePlayer() {
    _uiState.update {
      if (it.setup.playerNames.size <= 2) {
        it.copy(setupError = "Local multiplayer needs at least 2 players.")
      } else {
        it.copy(setup = it.setup.copy(playerNames = it.setup.playerNames.dropLast(1)), setupError = null)
      }
    }
  }

  fun onStartQuiz() {
    val setup = _uiState.value.setup
    val validationError = validateSetup(setup)
    if (validationError != null) {
      _uiState.update { it.copy(setupError = validationError) }
      return
    }

    val pool = countryPoolFor(setup)
    val config = configFor(setup, pool.size)
    val questions = questionGenerator.buildQuestions(pool, config)
    val hintCount = _uiState.value.hintCount
    val players = config.players.map { PlayerProgress(name = it, hintPoints = hintCount) }

    _uiState.update {
      it.copy(
        screen = AppScreen.Quiz,
        quiz =
          QuizState(
            mode = setup.mode,
            allInType = setup.allInType,
            questions = questions,
            players = players,
          ),
        setupError = null,
      )
    }
  }

  fun onCountryAnswerSelected(country: FlagCountry) {
    _uiState.update { it.copy(quiz = it.quiz.copy(selectedCountry = country)) }
  }

  fun onTypedAnswerChanged(answer: String) {
    _uiState.update { it.copy(quiz = it.quiz.copy(typedAnswer = answer)) }
  }

  fun onUseHint() {
    val state = _uiState.value
    val quiz = state.quiz
    val question = quiz.currentQuestion ?: return
    if (quiz.hintUsedOnCurrentQuestion || quiz.currentPlayer.hintPoints < 1) return

    val players = quiz.players.toMutableList()
    val newHintCount = quiz.currentPlayer.hintPoints - 1
    players.replaceAll { it.copy(hintPoints = newHintCount) }

    val hiddenCodes =
      if (question.variant == QuizVariant.TypeCountryName) {
        quiz.hiddenOptionCodes
      } else {
        question.options
          .filterNot { it.code == question.correctCountry.code }
          .filterNot { it.code in quiz.hiddenOptionCodes }
          .take(2)
          .map { it.code }
          .toSet()
      }

    _uiState.update {
      it.copy(
        quiz =
          quiz.copy(
            players = players,
            hiddenOptionCodes = quiz.hiddenOptionCodes + hiddenCodes,
            typedHintPrefix =
              if (question.variant == QuizVariant.TypeCountryName) {
                question.correctCountry.name.take(3)
              } else {
                quiz.typedHintPrefix
              },
            hintUsedOnCurrentQuestion = true,
          ),
        hintCount = newHintCount,
      )
    }
  }

  fun onNextQuestion() {
    completeCurrentQuestion(skipped = false)
  }

  fun onSkipQuestion() {
    completeCurrentQuestion(skipped = true)
  }

  private fun completeCurrentQuestion(skipped: Boolean) {
    val state = _uiState.value
    val quiz = state.quiz
    val question = quiz.currentQuestion ?: return
    val isCorrect =
      if (skipped) {
        false
      } else {
        when (question.variant) {
          QuizVariant.TypeCountryName -> QuizAnswerChecker.isTypedAnswerCorrect(quiz.typedAnswer, question.correctCountry)
          QuizVariant.FlagToCountry,
          QuizVariant.CountryToFlag -> QuizAnswerChecker.isCountrySelectionCorrect(quiz.selectedCountry, question.correctCountry)
        }
      }

    val players = quiz.players.toMutableList()
    players[quiz.currentPlayerIndex] =
      if (skipped) {
        quiz.currentPlayer.afterSkip()
      } else {
        quiz.currentPlayer.afterAnswer(
          isCorrect = isCorrect,
          hintDifficulty = state.settings.hintDifficulty,
          canEarnHints = quiz.mode != GameMode.LocalMultiplayer,
        )
      }
    val result =
      QuestionResult(
        question = question,
        playerName = quiz.currentPlayer.name,
        selectedCountry = quiz.selectedCountry,
        typedAnswer = if (skipped) "Skipped" else quiz.typedAnswer,
        isCorrect = isCorrect,
        hintUsed = quiz.hintUsedOnCurrentQuestion,
        skipped = skipped,
      )

    if (quiz.isLastQuestion) {
      val releasedHints =
        if (quiz.mode == GameMode.LocalMultiplayer) {
          0
        } else {
          players.sumOf { it.earnedHintPoints }
        }
      val completedResults = quiz.results + result
      val correctAnswers = completedResults.count { it.isCorrect }
      val eligibleQuizCompletions = if (quiz.totalQuestions >= 10) 1 else 0
      val progressResult =
        advanceLevelProgress(
          progress = state.levelProgress,
          earnedHints = releasedHints,
          correctAnswers = correctAnswers,
          eligibleQuizCompletions = eligibleQuizCompletions,
        )
      val newHintCount = _uiState.value.hintCount + releasedHints + progressResult.bonusHints
      _uiState.update {
        it.copy(
          screen = AppScreen.Results,
          quiz =
            quiz.copy(
              players = players.map { player -> player.copy(hintPoints = newHintCount, earnedHintPoints = 0) },
              results = completedResults,
            ),
          hintCount = newHintCount,
          levelProgress = progressResult.progress,
        )
      }
    } else {
      _uiState.update {
        it.copy(
          quiz =
            quiz.copy(
              players = players,
              results = quiz.results + result,
              currentQuestionIndex = quiz.currentQuestionIndex + 1,
              currentPlayerIndex = (quiz.currentPlayerIndex + 1) % quiz.players.size,
              selectedCountry = null,
              typedAnswer = "",
              hiddenOptionCodes = emptySet(),
              typedHintPrefix = null,
              hintUsedOnCurrentQuestion = false,
            ),
        )
      }
    }
  }

  fun onPlayAgain() {
    onStartQuiz()
  }

  private fun validateSetup(setup: SetupState): String? {
    if (setup.variants.isEmpty()) return "Choose at least one question variant."
    if ((setup.mode == GameMode.Continents || setup.usesContinentsBase()) && setup.selectedContinents.isEmpty()) {
      return "Choose at least one continent."
    }
    if ((setup.mode == GameMode.Continents || setup.usesContinentsBase()) && countryPoolFor(setup).size < 4) {
      return "Choose continents with at least 4 countries."
    }
    if (!setup.surpriseMe) {
      val questionCount = setup.questionCount ?: return "Write how many questions you want."
      if (questionCount <= 0) return "Question count must be at least 1."
      val maxQuestions = countryPoolFor(setup).size
      if (questionCount > maxQuestions) return "Question count must be between 1 and $maxQuestions."
    }

    if (setup.mode == GameMode.LocalMultiplayer) {
      val names = setup.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
      if (names.size !in 2..5) return "Local multiplayer needs 2 to 5 named players."
      if (names.distinctBy { it.lowercase() }.size != names.size) return "Player names must be unique."
    }
    return null
  }

  private fun configFor(
    setup: SetupState,
    poolSize: Int,
  ): QuizConfig {
    val variants =
      when {
        setup.mode == GameMode.AllIn && setup.allInType == AllInType.Hardcore -> QuizVariant.entries.toSet()
        setup.mode == GameMode.LocalMultiplayer &&
          setup.multiplayerBase == MultiplayerQuizBase.AllIn &&
          setup.allInType == AllInType.Hardcore -> QuizVariant.entries.toSet()
        else -> setup.variants
      }

      val questionCount =
      if (setup.mode == GameMode.AllIn || setup.usesAllInBase()) {
        poolSize
      } else if (setup.surpriseMe) {
        random.nextInt(from = 1, until = poolSize + 1)
      } else {
        setup.questionCount?.coerceIn(1, poolSize) ?: 1
      }

    val players =
      if (setup.mode == GameMode.LocalMultiplayer) {
        setup.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
      } else {
        listOf("Solo")
      }

    return QuizConfig(
      mode = setup.mode,
      variants = variants,
      selectedContinents = setup.selectedContinents,
      questionCount = questionCount,
      surpriseMe = setup.surpriseMe,
      allInType = setup.allInType,
      hintDifficulty = _uiState.value.settings.hintDifficulty,
      players = players,
    )
  }

  private fun countryPoolFor(setup: SetupState): List<FlagCountry> {
    return if (setup.mode == GameMode.Continents || setup.usesContinentsBase()) {
      countries.filter { it.continent in setup.selectedContinents }
    } else {
      countries
    }
  }

  private fun questionLimitFor(setup: SetupState): Int = countryPoolFor(setup).size

  private fun advanceLevelProgress(
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

    while (
      hints >= progress.hintsNeeded &&
      correct >= progress.correctAnswersNeeded &&
      eligibleQuizzes >= progress.eligibleQuizzesNeeded
    ) {
      level++
      levelUps++
      hints -= progress.hintsNeeded
      correct -= progress.correctAnswersNeeded
      eligibleQuizzes -= progress.eligibleQuizzesNeeded
    }

    return LevelProgressResult(
      progress =
        progress.copy(
          level = level,
          hintsTowardNextLevel = hints,
          correctAnswersTowardNextLevel = correct,
          eligibleQuizzesTowardNextLevel = eligibleQuizzes,
          levelUpVisible = levelUps > 0,
        ),
      bonusHints = levelUps * 5,
    )
  }

  private data class LevelProgressResult(
    val progress: LevelProgressState,
    val bonusHints: Int,
  )

  private fun SetupState.usesContinentsBase(): Boolean =
    mode == GameMode.LocalMultiplayer && multiplayerBase == MultiplayerQuizBase.Continents

  private fun SetupState.usesAllInBase(): Boolean =
    mode == GameMode.LocalMultiplayer && multiplayerBase == MultiplayerQuizBase.AllIn
}

package com.example.flaggameandroid.feature.app

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flaggameandroid.core.data.FlagCatalogRepository
import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.MedalTier
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.engagement.AppEngagementCoordinator
import com.example.flaggameandroid.persistence.AppGraph
import com.example.flaggameandroid.persistence.InMemoryProgressStore
import com.example.flaggameandroid.persistence.InMemorySettingsStore
import com.example.flaggameandroid.persistence.PersistedAppState
import com.example.flaggameandroid.persistence.PersistedQuizHistory
import com.example.flaggameandroid.persistence.ProgressStore
import com.example.flaggameandroid.persistence.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

private const val MaxAvatarIndex = ProgressionRules.TotalAvatarCount - 1

class FlagGameViewModel(
  private val catalogRepository: FlagCatalogRepository = StaticFlagCatalogRepository(),
  private val questionGenerator: QuizQuestionGenerator = QuizQuestionGenerator(),
  private val random: Random = Random.Default,
  private val settingsStore: SettingsStore = InMemorySettingsStore(),
  private val progressStore: ProgressStore = InMemoryProgressStore(),
  private val engagementCoordinator: AppEngagementCoordinator? = null,
  initialPersistedState: PersistedAppState = PersistedAppState(),
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
        settings =
          SettingsState(
            hintDifficulty = initialPersistedState.hintDifficulty,
            reminderEnabled = initialPersistedState.reminderEnabled,
            language = initialPersistedState.language,
          ),
        availableContinents = allContinents,
        setup = SetupState(selectedContinents = selectableContinents.toSet()),
        questionCountLimit = countries.size,
        profile =
          ProfileState(
            accountName = initialPersistedState.accountName,
            avatarIndex = initialPersistedState.avatarIndex.coerceIn(0, MaxAvatarIndex),
          ),
        hintCount = initialPersistedState.hintCount,
        ratings = initialPersistedState.ratings,
        achievements = initialPersistedState.achievements,
        levelProgress =
          LevelProgressState(
            level = initialPersistedState.level,
            hintsTowardNextLevel = initialPersistedState.hintsTowardNextLevel,
            correctAnswersTowardNextLevel = initialPersistedState.correctAnswersTowardNextLevel,
            eligibleQuizzesTowardNextLevel = initialPersistedState.eligibleQuizzesTowardNextLevel,
          ),
        lastOpenedAtEpochMillis = initialPersistedState.lastOpenedAtEpochMillis,
        lastPlayedAtEpochMillis = initialPersistedState.lastPlayedAtEpochMillis,
        inactiveIconActive = initialPersistedState.inactiveIconActive,
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
        profile = state.profile,
        hintCount = state.hintCount,
        ratings = state.ratings,
        achievements = state.achievements,
        levelProgress = state.levelProgress,
        lastOpenedAtEpochMillis = state.lastOpenedAtEpochMillis,
        lastPlayedAtEpochMillis = state.lastPlayedAtEpochMillis,
        inactiveIconActive = state.inactiveIconActive,
      )
  }

  fun onStartClicked() {
    _uiState.update { it.copy(screen = AppScreen.GameModes, setupError = null) }
  }

  fun onSettingsClicked() {
    _uiState.update { it.copy(screen = AppScreen.Settings, setupError = null) }
  }

  fun onMedalsClicked() {
    _uiState.update { it.copy(screen = AppScreen.Medals, setupError = null) }
  }

  fun onAchievementsClicked() {
    _uiState.update { it.copy(screen = AppScreen.Achievements, setupError = null) }
  }

  fun onBackToGameModes() {
    _uiState.update { it.copy(screen = AppScreen.GameModes, setupError = null) }
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
          playerNames = listOf(it.profile.displayName, "Player 2"),
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
    persistSettings()
  }

  fun onReminderEnabledChanged(enabled: Boolean) {
    _uiState.update { it.copy(settings = it.settings.copy(reminderEnabled = enabled)) }
    persistSettings()
  }

  fun onLanguageSelected(language: AppLanguage) {
    _uiState.update { it.copy(settings = it.settings.copy(language = language)) }
    persistProgress()
  }

  fun onAccountNameChanged(name: String) {
    _uiState.update { it.copy(profile = it.profile.copy(accountName = name.take(24))) }
    persistProgress()
  }

  fun onAvatarSelected(index: Int) {
    _uiState.update { it.copy(profile = it.profile.copy(avatarIndex = index.coerceIn(0, MaxAvatarIndex))) }
    persistProgress()
  }

  fun onResetHintsClicked() {
    _uiState.update {
      it.copy(
        hintCount = 0,
        quiz = it.quiz.copy(players = it.quiz.players.map { player -> player.copy(hintPoints = 0) }),
      )
    }
    persistProgress()
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
    persistProgress()
  }

  fun onTestingLevelUpClicked() {
    _uiState.update {
      val nextLevel = (it.levelProgress.level + 1).coerceAtMost(ProgressionRules.MaxLevel)
      val newHintCount = it.hintCount + 5
      it.copy(
        levelProgress =
          it.levelProgress.copy(
            level = nextLevel,
            levelUpVisible = nextLevel > it.levelProgress.level,
          ),
        hintCount = newHintCount,
        quiz = it.quiz.copy(players = it.quiz.players.map { player -> player.copy(hintPoints = newHintCount) }),
      )
    }
    persistProgress()
  }

  fun onTestingResetLevelClicked() {
    _uiState.update {
      it.copy(
        levelProgress =
          it.levelProgress.copy(
            level = 1,
            levelUpVisible = false,
          ),
      )
    }
    persistProgress()
  }

  fun onUnlockRandomAchievementClicked() {
    _uiState.update {
      val lockedAchievement = AchievementId.entries.filterNot(it.achievements::isUnlocked).randomOrNull(random)
      if (lockedAchievement == null) {
        it
      } else {
        it.copy(achievements = it.achievements.unlock(lockedAchievement, System.currentTimeMillis()))
      }
    }
    persistProgress()
  }

  fun onLockAllAchievementsClicked() {
    _uiState.update { it.copy(achievements = AchievementsProgress()) }
    persistProgress()
  }

  fun onResetAchievementsAndMedalsClicked() {
    _uiState.update { it.copy(ratings = RatingsProgress()) }
    persistProgress()
  }

  fun onToggleTestingIconClicked() {
    val nextActiveState = !_uiState.value.inactiveIconActive
    engagementCoordinator?.setInactiveLauncherIcon(nextActiveState)
    _uiState.update { it.copy(inactiveIconActive = nextActiveState) }
    persistProgress()
  }

  fun onTriggerTestingReminderClicked() {
    engagementCoordinator?.triggerTestingReminderNotification()
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
      val variants = it.setup.variants.ifEmpty { QuizVariant.entries.toSet() }
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
    val questionStates = List(questions.size) { QuestionDraftState() }

    _uiState.update {
      it.copy(
        screen = AppScreen.Quiz,
        quiz =
          QuizState(
            mode = setup.mode,
            allInType = setup.allInType,
            variants = config.variants,
            selectedContinents = setup.selectedContinents,
            questions = questions,
            questionStates = questionStates,
            players = players,
          ).loadQuestionDraft(0),
        setupError = null,
      )
    }
  }

  fun onCountryAnswerSelected(country: FlagCountry) {
    updateCurrentQuestionDraft { draft ->  
      draft.copy(
        status = QuestionStatus.Answered,
        selectedCountry = country,
        locked = _uiState.value.quiz.mode == GameMode.Training,
      )
    }
  }

  fun onTypedAnswerChanged(answer: String) {
    updateCurrentQuestionDraft { draft ->
      draft.copy(
        typedAnswer = answer,
      )
    }
  }

  fun onVerifyTypedAnswer() {
    val state = _uiState.value
    val quiz = state.quiz
    val currentDraft = quiz.currentQuestionState
    if (quiz.currentQuestion?.variant != QuizVariant.TypeCountryName) return
    if (currentDraft.typedAnswer.isBlank() || currentDraft.locked) return

    _uiState.update {
      it.copy(
        quiz =
          quiz.copy(
            questionStates =
              quiz.questionStates.replaceAt(
                quiz.currentQuestionIndex,
                currentDraft.copy(
                  status = QuestionStatus.Answered,
                  locked = true,
                ),
              ),
          ),
      )
    }
  }

  fun onUseHint() {
    val state = _uiState.value
    val quiz = state.quiz
    val question = quiz.currentQuestion ?: return
    val currentDraft = quiz.currentQuestionState
    if (currentDraft.locked || currentDraft.status == QuestionStatus.Answered) return
    if (currentDraft.hintUses >= 2 || quiz.currentPlayer.hintPoints < 1) return

    val players = quiz.players.toMutableList()
    val newHintCount = quiz.currentPlayer.hintPoints - 1
    players.replaceAll { it.copy(hintPoints = newHintCount) }

    val isTypedQuestion = question.variant == QuizVariant.TypeCountryName
    val firstHint = currentDraft.hintUses == 0
    val fullCountryName = question.correctCountry.localizedName(state.settings.language)
    val hiddenCodes =
      when {
        isTypedQuestion -> currentDraft.hiddenOptionCodes
        firstHint ->
          currentDraft.hiddenOptionCodes +
            question.options
              .filterNot { it.code == question.correctCountry.code }
              .filterNot { it.code in currentDraft.hiddenOptionCodes }
              .take(2)
              .map { it.code }
        else ->
          question.options
            .filterNot { it.code == question.correctCountry.code }
            .map { it.code }
      }.toSet()

    _uiState.update {
      val updatedQuestionState =
        currentDraft.copy(
          hiddenOptionCodes = hiddenCodes,
          typedHintPrefix =
            when {
              !isTypedQuestion -> currentDraft.typedHintPrefix
              firstHint -> fullCountryName.take(3)
              else -> fullCountryName
            },
          typedAnswer =
            when {
              !isTypedQuestion -> currentDraft.typedAnswer
              firstHint -> currentDraft.typedAnswer
              else -> fullCountryName
            },
          selectedCountry =
            when {
              isTypedQuestion -> currentDraft.selectedCountry
              firstHint -> currentDraft.selectedCountry
              else -> question.correctCountry
            },
          status =
            when {
              firstHint -> currentDraft.status
              else -> QuestionStatus.Answered
            },
          hintUses = (currentDraft.hintUses + 1).coerceAtMost(2),
          hintUsed = true,
        )
      it.copy(
        quiz =
          quiz.copy(
            players = players,
            questionStates = quiz.questionStates.replaceAt(quiz.currentQuestionIndex, updatedQuestionState),
            selectedCountry = updatedQuestionState.selectedCountry,
            typedAnswer = updatedQuestionState.typedAnswer,
            hiddenOptionCodes = updatedQuestionState.hiddenOptionCodes,
            typedHintPrefix = updatedQuestionState.typedHintPrefix,
            hintUsedOnCurrentQuestion = updatedQuestionState.hintUses > 0,
          ),
        hintCount = newHintCount,
      )
    }
    persistProgress()
  }

  fun onNextQuestion() {
    val state = _uiState.value
    val quiz = state.quiz
    val question = quiz.currentQuestion ?: return
    val currentDraft = quiz.currentQuestionState
    val updatedDraft =
      currentDraft.copy(
        status = QuestionStatus.Answered,
        typedAnswer = currentDraft.typedAnswer,
        locked = quiz.mode == GameMode.Training && currentDraft.status == QuestionStatus.Answered,
      )
    val updatedQuestionStates = quiz.questionStates.replaceAt(quiz.currentQuestionIndex, updatedDraft)
    val isCorrect =
      when (question.variant) {
        QuizVariant.TypeCountryName ->
          QuizAnswerChecker.isTypedAnswerCorrect(
            typedAnswer = currentDraft.typedAnswer,
            acceptedAnswers = question.correctCountry.acceptedTypedAnswers(state.settings.language),
          )

        QuizVariant.FlagToCountry,
        QuizVariant.CountryToFlag -> QuizAnswerChecker.isCountrySelectionCorrect(currentDraft.selectedCountry, question.correctCountry)
      }

    val answeredPlayers = quiz.players.toMutableList()
    answeredPlayers[quiz.currentPlayerIndex] =
      quiz.currentPlayer.afterAnswer(
        isCorrect = isCorrect,
        hintUses = currentDraft.hintUses,
        hintDifficulty = state.settings.hintDifficulty,
        canEarnHints = quiz.mode != GameMode.LocalMultiplayer,
      )
    val updatedResults = buildQuizResults(quiz.copy(questionStates = updatedQuestionStates), state.settings.language)
    val updatedPlayers = scorePlayersFromResults(answeredPlayers, updatedResults, state.settings.hintDifficulty, quiz.mode != GameMode.LocalMultiplayer)
    val updatedQuiz =
      quiz.copy(
        questionStates = updatedQuestionStates,
        results = updatedResults,
        players = updatedPlayers,
      )

    if (quiz.currentQuestionIndex >= quiz.questions.lastIndex) {
      if (updatedQuiz.canFinish) {
        completeQuiz(state, updatedQuiz)
      } else {
        _uiState.update {
          it.copy(
            quiz = updatedQuiz.loadQuestionDraft(quiz.currentQuestionIndex),
          )
        }
      }
      return
    }

    val nextQuestionIndex = quiz.currentQuestionIndex + 1
    _uiState.update {
      it.copy(
        quiz =
          updatedQuiz.copy(
            currentQuestionIndex = nextQuestionIndex,
            currentPlayerIndex = nextQuestionIndex % quiz.players.size,
          ).loadQuestionDraft(nextQuestionIndex),
      )
    }
  }

  fun onSkipQuestion() {
    onUnskipQuestion()
  }

  fun onFinishQuiz() {
    val state = _uiState.value
    val quiz = state.quiz
    if (!quiz.canFinish) return
    completeQuiz(state, quiz)
  }

  fun onPreviousQuestion() {
    val state = _uiState.value
    val quiz = state.quiz
    val targetIndex = (quiz.currentQuestionIndex - 1).takeIf { it >= 0 } ?: return
    _uiState.update {
      it.copy(
        quiz = quiz.loadQuestionDraft(targetIndex),
      )
    }
  }

  fun onNextQuestionPreview() {
    val state = _uiState.value
    val quiz = state.quiz
    val currentDraft = quiz.currentQuestionState
    val currentQuestion = quiz.currentQuestion
    val updatedQuestionStates =
      when {
        currentQuestion?.variant == QuizVariant.TypeCountryName && currentDraft.typedAnswer.isNotBlank() ->
          quiz.questionStates.replaceAt(
            quiz.currentQuestionIndex,
            currentDraft.copy(
              status = QuestionStatus.Answered,
              locked = true,
            ),
          )
        currentQuestion?.variant == QuizVariant.TypeCountryName &&
          currentDraft.typedAnswer.isBlank() &&
          currentDraft.status != QuestionStatus.Answered ->
          quiz.questionStates.replaceAt(quiz.currentQuestionIndex, currentDraft.copy(status = QuestionStatus.Skipped))
        currentQuestion?.variant != QuizVariant.TypeCountryName && currentDraft.status == QuestionStatus.Unanswered ->
          quiz.questionStates.replaceAt(quiz.currentQuestionIndex, currentDraft.copy(status = QuestionStatus.Skipped))
        quiz.mode == GameMode.Training && currentDraft.status == QuestionStatus.Answered ->
          quiz.questionStates.replaceAt(quiz.currentQuestionIndex, currentDraft.copy(locked = true))
        else -> quiz.questionStates
      }
    val targetIndex =
      (quiz.currentQuestionIndex + 1).takeIf { it <= quiz.questions.lastIndex }
        ?: run {
          _uiState.update {
            it.copy(
              quiz = quiz.copy(questionStates = updatedQuestionStates).loadQuestionDraft(quiz.currentQuestionIndex),
            )
          }
          return
        }
    _uiState.update {
      it.copy(
        quiz = quiz.copy(questionStates = updatedQuestionStates).loadQuestionDraft(targetIndex),
      )
    }
  }

  fun onQuestionBack() {
    val state = _uiState.value
    val quiz = state.quiz
    val targetIndex = (quiz.currentQuestionIndex - 1).takeIf { it >= 0 } ?: return
    _uiState.update { it.copy(quiz = quiz.loadQuestionDraft(targetIndex)) }
  }

  fun onQuestionForward() {
    onNextQuestionPreview()
  }

  fun onUnskipQuestion() {
    val state = _uiState.value
    val quiz = state.quiz
    val skippedIndices = quiz.questionStates.mapIndexedNotNull { index, questionState ->
      if (questionState.status == QuestionStatus.Skipped) index else null
    }
    if (skippedIndices.isEmpty()) return

    val currentIndex = quiz.currentQuestionIndex
    val targetIndex =
      skippedIndices.firstOrNull { it > currentIndex }
        ?: skippedIndices.first()

    _uiState.update {
      it.copy(
        quiz = quiz.loadQuestionDraft(targetIndex),
      )
    }
  }

  fun onPlayAgain() {
    onStartQuiz()
  }

  private fun updateCurrentQuestionDraft(transform: (QuestionDraftState) -> QuestionDraftState) {
    _uiState.update { state ->
      val quiz = state.quiz
      val currentQuestion = quiz.currentQuestion ?: return@update state
      if (quiz.mode == GameMode.Training && quiz.currentQuestionState.locked) return@update state
      val updatedDraft = transform(quiz.currentQuestionState)
      state.copy(
        quiz =
          quiz.copy(
            questionStates = quiz.questionStates.replaceAt(quiz.currentQuestionIndex, updatedDraft),
            selectedCountry = updatedDraft.selectedCountry,
            typedAnswer = updatedDraft.typedAnswer,
            hiddenOptionCodes = updatedDraft.hiddenOptionCodes,
            typedHintPrefix = updatedDraft.typedHintPrefix,
            hintUsedOnCurrentQuestion = updatedDraft.hintUses > 0,
          ),
      )
    }
  }

  private fun completeQuiz(
    state: FlagGameUiState,
    quiz: QuizState,
  ) {
    val completionTime = System.currentTimeMillis()
    val completedResults = buildQuizResults(quiz, state.settings.language)
    val correctAnswers = completedResults.count { it.isCorrect }
    val distinctCountries = completedResults.map { it.question.correctCountry.code }.distinct().size
    val scoredPlayers = scorePlayersFromResults(quiz.players, completedResults, state.settings.hintDifficulty, quiz.mode != GameMode.LocalMultiplayer)
    val releasedHints =
      if (quiz.mode == GameMode.LocalMultiplayer) {
        0
      } else {
        scoredPlayers.sumOf { it.earnedHintPoints }
      }
    val quizWithResults = quiz.copy(results = completedResults)
    val qualifiesForPerfectNoBluffLevel =
      quiz.mode == GameMode.AllIn &&
        quiz.allInType == AllInType.NoBluffAllTough &&
        quiz.variants.size == QuizVariant.entries.size &&
        completedResults.isNotEmpty() &&
        completedResults.all { it.isCorrect }
    val perfectNoBluffLevelGain =
      if (qualifiesForPerfectNoBluffLevel) {
        if (state.settings.hintDifficulty == HintDifficulty.Impossible) 2 else 1
      } else {
        0
      }
    val shouldProgressLevel =
      (quiz.mode == GameMode.Continents || quiz.mode == GameMode.AllIn) && !qualifiesForPerfectNoBluffLevel
    val eligibleQuizCompletions = if (shouldProgressLevel && completedResults.size >= 10) 1 else 0
    val progressResult =
      advanceLevelProgress(
        progress = state.levelProgress,
        earnedHints = if (shouldProgressLevel) releasedHints else 0,
        correctAnswers = if (shouldProgressLevel) correctAnswers else 0,
        eligibleQuizCompletions = eligibleQuizCompletions,
      )
    val finalProgress =
      if (qualifiesForPerfectNoBluffLevel) {
        val targetLevel = (state.levelProgress.level + perfectNoBluffLevelGain).coerceAtMost(ProgressionRules.MaxLevel)
        state.levelProgress.copy(
          level = targetLevel,
          hintsTowardNextLevel = 0,
          levelUpVisible = targetLevel > state.levelProgress.level,
        )
      } else {
        progressResult.progress
      }
    val updatedRatings = awardMedalIfEligible(state.ratings, quizWithResults, completedResults, distinctCountries)
    val updatedAchievements =
      awardAchievementsIfEligible(
        achievements = state.achievements,
        ratings = updatedRatings,
        quiz = quizWithResults,
        completedResults = completedResults,
        distinctCountries = distinctCountries,
        completedAtEpochMillis = completionTime,
      )
    val noBluffBonusHints =
      if (qualifiesForPerfectNoBluffLevel) {
        val gainedLevels = (finalProgress.level - state.levelProgress.level).coerceAtLeast(0)
        gainedLevels * 5
      } else {
        0
      }
    val totalBonusHints = progressResult.bonusHints + noBluffBonusHints
    val newHintCount = _uiState.value.hintCount + releasedHints + totalBonusHints
    val finalPlayers =
      scoredPlayers.map { player -> player.copy(hintPoints = newHintCount, earnedHintPoints = 0) }

    _uiState.update {
      it.copy(
        screen = AppScreen.Results,
        quiz =
          quizWithResults.copy(
            players = finalPlayers,
            results = completedResults,
          ),
        hintCount = newHintCount,
        ratings = updatedRatings,
        achievements = updatedAchievements,
        levelProgress = finalProgress,
        lastPlayedAtEpochMillis = completionTime,
        inactiveIconActive = false,
      )
    }
    engagementCoordinator?.onQuizCompleted()
    persistProgress()
    recordCompletedQuiz(
      mode = quiz.mode ?: GameMode.Training,
      totalQuestions = completedResults.size,
      correctAnswers = correctAnswers,
      skippedAnswers = 0,
      netScore = scoredPlayers.sumOf { it.score },
    )
  }

  private fun QuizState.loadQuestionDraft(index: Int): QuizState {
    if (questions.isEmpty()) return this
    val clampedIndex = index.coerceIn(0, questions.lastIndex)
    val draft = questionStates.getOrElse(clampedIndex) { QuestionDraftState() }
    return copy(
      currentQuestionIndex = clampedIndex,
      currentPlayerIndex = if (players.isEmpty()) 0 else clampedIndex % players.size,
      selectedCountry = draft.selectedCountry,
      typedAnswer = draft.typedAnswer,
      hiddenOptionCodes = draft.hiddenOptionCodes,
      typedHintPrefix = draft.typedHintPrefix,
      hintUsedOnCurrentQuestion = draft.hintUses > 0,
      questionStates = questionStates,
    )
  }

  private fun QuizState.previousAnsweredQuestionIndex(): Int? =
    (currentQuestionIndex - 1 downTo 0).firstOrNull {
      questionStates.getOrElse(it) { QuestionDraftState() }.status != QuestionStatus.Unanswered
    }

  private fun QuizState.nextAnsweredQuestionIndex(): Int? =
    ((currentQuestionIndex + 1)..questions.lastIndex).firstOrNull {
      questionStates.getOrElse(it) { QuestionDraftState() }.status != QuestionStatus.Unanswered
    }

  private fun buildQuizResults(
    quiz: QuizState,
    language: AppLanguage,
  ): List<QuestionResult> =
    run {
      val streakByPlayer = mutableMapOf<String, Int>()
      quiz.questions.mapIndexedNotNull { index, question ->
      val draft = quiz.questionStates.getOrElse(index) { QuestionDraftState() }
      if (draft.status != QuestionStatus.Answered) return@mapIndexedNotNull null
      val playerName =
        if (quiz.players.isEmpty()) {
          "Solo"
        } else {
          quiz.players[index % quiz.players.size].name
        }
      val previousStreak = streakByPlayer[playerName] ?: 0
      val isCorrect =
        when (question.variant) {
          QuizVariant.TypeCountryName ->
            QuizAnswerChecker.isTypedAnswerCorrect(
              typedAnswer = draft.typedAnswer,
              acceptedAnswers = question.correctCountry.acceptedTypedAnswers(language),
            )

          QuizVariant.FlagToCountry,
          QuizVariant.CountryToFlag ->
            QuizAnswerChecker.isCountrySelectionCorrect(draft.selectedCountry, question.correctCountry)
        }
      val updatedStreak =
        when {
          !isCorrect || draft.hintUses >= 2 -> 0
          draft.hintUses == 1 -> previousStreak
          else -> previousStreak + 1
        }
      streakByPlayer[playerName] = updatedStreak

      QuestionResult(
        question = question,
        playerName = playerName,
        selectedCountry = draft.selectedCountry,
        typedAnswer = draft.typedAnswer,
        isCorrect = isCorrect,
        hintUsed = draft.hintUsed || draft.hintUses > 0,
        hintUses = draft.hintUses,
        hintStreak = updatedStreak,
      )
    }
    }

  private fun scorePlayersFromResults(
    players: List<PlayerProgress>,
    results: List<QuestionResult>,
    hintDifficulty: HintDifficulty,
    canEarnHints: Boolean,
  ): List<PlayerProgress> {
    if (players.isEmpty()) return emptyList()
    val scored = players.map { it.copy(score = 0, correctStreak = 0, earnedHintPoints = 0) }.toMutableList()
    results.forEach { result ->
      val index = scored.indexOfFirst { it.name == result.playerName }
      if (index == -1) return@forEach
      scored[index] =
        scored[index].afterAnswer(
          isCorrect = result.isCorrect,
          hintUses = result.hintUses,
          hintDifficulty = hintDifficulty,
          canEarnHints = canEarnHints,
        )
    }
    return scored
  }

  private fun <T> List<T>.replaceAt(
    index: Int,
    value: T,
  ): List<T> = mapIndexed { currentIndex, currentValue -> if (currentIndex == index) value else currentValue }

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
      val limit = if (setup.mode == GameMode.Training) 999 else maxQuestions
      if (questionCount > limit) return "Question count must be between 1 and $limit."
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
    val variants = setup.variants

    val questionCount =
      if (setup.mode == GameMode.AllIn || setup.usesAllInBase()) {
        poolSize
      } else if (setup.mode == GameMode.Training) {
        if (setup.surpriseMe) {
          random.nextInt(from = 1, until = 1000)
        } else {
          setup.questionCount?.coerceIn(1, 999) ?: 1
        }
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

  private fun questionLimitFor(setup: SetupState): Int =
    if (setup.mode == GameMode.Training) {
      999
    } else {
      countryPoolFor(setup).size
    }

  private fun persistSettings() {
    runBlocking {
      settingsStore.saveHintDifficulty(_uiState.value.settings.hintDifficulty)
      settingsStore.saveReminderEnabled(_uiState.value.settings.reminderEnabled)
    }
  }

  private fun persistProgress() {
    val state = _uiState.value
    runBlocking {
      progressStore.saveProgress(
        PersistedAppState(
          hintDifficulty = state.settings.hintDifficulty,
          reminderEnabled = state.settings.reminderEnabled,
          language = state.settings.language,
          accountName = state.profile.accountName,
          avatarIndex = state.profile.avatarIndex,
          hintCount = state.hintCount,
          ratings = state.ratings,
          achievements = state.achievements,
          level = state.levelProgress.level,
          hintsTowardNextLevel = state.levelProgress.hintsTowardNextLevel,
          correctAnswersTowardNextLevel = state.levelProgress.correctAnswersTowardNextLevel,
          eligibleQuizzesTowardNextLevel = state.levelProgress.eligibleQuizzesTowardNextLevel,
          lastOpenedAtEpochMillis = state.lastOpenedAtEpochMillis,
          lastPlayedAtEpochMillis = state.lastPlayedAtEpochMillis,
          inactiveIconActive = state.inactiveIconActive,
        ),
      )
    }
  }

  private fun recordCompletedQuiz(
    mode: GameMode,
    totalQuestions: Int,
    correctAnswers: Int,
    skippedAnswers: Int,
    netScore: Int,
  ) {
    runBlocking {
      progressStore.recordQuiz(
        PersistedQuizHistory(
          mode = mode,
          totalQuestions = totalQuestions,
          correctAnswers = correctAnswers,
          skippedAnswers = skippedAnswers,
          netScore = netScore,
          completedAtEpochMillis = System.currentTimeMillis(),
        ),
      )
    }
  }

  private fun awardMedalIfEligible(
    ratings: RatingsProgress,
    quiz: QuizState,
    completedResults: List<QuestionResult>,
    distinctCountries: Int,
  ): RatingsProgress {
    if (quiz.isMultiplayer) return ratings
    if (completedResults.isEmpty() || completedResults.any { !it.isCorrect }) return ratings

    val awardedMedal =
      ProgressionRules.medalForPerfectQuiz(
        totalQuestions = completedResults.size,
        distinctCountries = distinctCountries,
        totalCatalogCountries = countries.size,
      ) ?: return ratings

    return ratings.increment(awardedMedal)
  }

  private fun awardAchievementsIfEligible(
    achievements: AchievementsProgress,
    ratings: RatingsProgress,
    quiz: QuizState,
    completedResults: List<QuestionResult>,
    distinctCountries: Int,
    completedAtEpochMillis: Long,
  ): AchievementsProgress {
    if (quiz.isMultiplayer || completedResults.isEmpty()) return achievements

    var updatedAchievements = achievements
    val perfectQuiz = completedResults.all { it.isCorrect }
    val medalEligiblePerfectQuiz = perfectQuiz && completedResults.size >= 10

    if (ratings.bronzeCount >= 50) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.BronzeCollector, completedAtEpochMillis)
    }
    if (ratings.silverCount >= 25) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.SilverCollector, completedAtEpochMillis)
    }
    if (ratings.goldCount >= 10) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.GoldCollector, completedAtEpochMillis)
    }
    if (ratings.titaniumCount >= 5) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.PlatinumCollector, completedAtEpochMillis)
    }
    if (ratings.diamondCount >= 1) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.DiamondCollector, completedAtEpochMillis)
    }

    if (medalEligiblePerfectQuiz) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.FirstPerfect, completedAtEpochMillis)
    }

    if (medalEligiblePerfectQuiz && completedResults.none { it.hintUsed }) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.HintlessHero, completedAtEpochMillis)
    }

    if (medalEligiblePerfectQuiz && QuizVariant.entries.all { variant -> completedResults.any { it.question.variant == variant } }) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.VariantMaster, completedAtEpochMillis)
    }

    if (perfectQuiz && completedResults.size == countries.size && distinctCountries == countries.size) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.DiamondWorld, completedAtEpochMillis)
      if (completedResults.none { it.hintUsed }) {
        updatedAchievements = updatedAchievements.unlock(AchievementId.WorldPurist, completedAtEpochMillis)
      }
    }

    if (
      perfectQuiz &&
      quiz.mode == GameMode.AllIn &&
      quiz.variants.containsAll(QuizVariant.entries) &&
      completedResults.size == countries.size &&
      distinctCountries == countries.size
    ) {
      updatedAchievements = updatedAchievements.unlock(AchievementId.NoBluffLegend, completedAtEpochMillis)
    }

    val selectedContinent = quiz.selectedContinents.singleOrNull()
    val continentAchievementId = selectedContinent?.let(AchievementId::forContinent)
    if (continentAchievementId != null) {
      val availableCountriesForSelectedContinent = countries.count { it.continent == selectedContinent }
      val qualifiesForContinentAchievement =
        ProgressionRules.qualifiesForContinentAchievement(
          mode = quiz.mode ?: GameMode.Training,
          selectedContinents = quiz.selectedContinents,
          usedHint = completedResults.any { it.hintUsed },
          totalQuestions = completedResults.size,
          correctAnswers = completedResults.count { it.isCorrect },
          distinctCountries = distinctCountries,
          availableCountriesForSelectedContinent = availableCountriesForSelectedContinent,
        )
      if (qualifiesForContinentAchievement) {
        updatedAchievements = updatedAchievements.unlock(continentAchievementId, completedAtEpochMillis)
      }
    }

    return updatedAchievements
  }

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
      level < ProgressionRules.MaxLevel
    ) {
      val requirements = ProgressionRules.requirementsForLevel(level)
      if (
        hints < requirements.hintsNeeded ||
        correct < requirements.correctAnswersNeeded ||
        eligibleQuizzes < requirements.eligibleQuizzesNeeded
      ) {
        break
      }
      level++
      levelUps++
      hints -= requirements.hintsNeeded
      correct -= requirements.correctAnswersNeeded
      eligibleQuizzes -= requirements.eligibleQuizzesNeeded
    }

    return LevelProgressResult(
      progress =
        progress.copy(
          level = level,
          hintsTowardNextLevel = if (levelUps > 0) 0 else hints,
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

  companion object {
    fun factory(context: Context): ViewModelProvider.Factory =
      viewModelFactory {
        initializer {
          val container = AppGraph.from(context.applicationContext)
          val initialPersistedState =
            runBlocking {
              val hintDifficulty = container.settingsStore.loadHintDifficulty()
              val reminderEnabled = container.settingsStore.loadReminderEnabled()
              val progress = container.progressStore.loadProgress()
              progress.copy(hintDifficulty = hintDifficulty, reminderEnabled = reminderEnabled)
            }
          FlagGameViewModel(
            settingsStore = container.settingsStore,
            progressStore = container.progressStore,
            engagementCoordinator = container.engagementCoordinator,
            initialPersistedState = initialPersistedState,
          )
        }
      }
  }
}

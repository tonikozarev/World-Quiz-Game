package com.example.flaggameandroid.feature.app

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flaggameandroid.core.data.FlagCatalogRepository
import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.engagement.AppEngagementCoordinator
import com.example.flaggameandroid.persistence.AppGraph
import com.example.flaggameandroid.persistence.persistProgress
import com.example.flaggameandroid.persistence.persistSettings
import com.example.flaggameandroid.persistence.recordCompletedQuiz
import com.example.flaggameandroid.persistence.InMemoryProgressStore
import com.example.flaggameandroid.persistence.InMemorySettingsStore
import com.example.flaggameandroid.persistence.PersistedAppState
import com.example.flaggameandroid.persistence.ProgressStore
import com.example.flaggameandroid.persistence.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

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
  private val maxAvatarIndex = ProgressionRules.TotalAvatarCount - 1

  private val _uiState =
    MutableStateFlow(
      buildInitialUiState(
        initialPersistedState = initialPersistedState,
        allContinents = allContinents,
        selectableContinents = selectableContinents,
        countries = countries,
      ),
  )
  val uiState: StateFlow<FlagGameUiState> = _uiState.asStateFlow()

  private fun updateState(update: (FlagGameUiState) -> FlagGameUiState) {
    _uiState.update(update)
  }

  private fun updateStateAndPersistProgress(update: (FlagGameUiState) -> FlagGameUiState) {
    _uiState.update(update)
    persistProgress(progressStore, _uiState.value)
  }

  private fun updateSettings(update: (SettingsState) -> SettingsState) {
    _uiState.update { state -> state.copy(settings = update(state.settings)) }
    persistSettings(settingsStore, _uiState.value.settings)
  }

  private fun navigateTo(screen: AppScreen) {
    updateState { it.copy(screen = screen, setupError = null) }
  }

  fun onBackToMenu() {
    updateState {
      it.resetToMenu(
        allContinents = allContinents,
        selectableContinents = selectableContinents,
        questionCountLimit = countries.size,
      )
    }
  }

  fun onStartClicked() {
    navigateTo(AppScreen.GameModes)
  }

  fun onSettingsClicked() {
    navigateTo(AppScreen.Settings)
  }

  fun onMedalsClicked() {
    navigateTo(AppScreen.Medals)
  }

  fun onAchievementsClicked() {
    navigateTo(AppScreen.Achievements)
  }

  fun onFavoritesClicked() {
    navigateTo(AppScreen.Favorites)
  }

  fun onBackToGameModes() {
    navigateTo(AppScreen.GameModes)
  }

  fun onLevelUpSeen() {
    updateState { it.withLevelUpSeen() }
  }

  fun onModeSelected(mode: GameMode) {
    val setup = buildSetupForMode(mode, selectableContinents, countries, _uiState.value.profile.displayName)
    if (mode == GameMode.DailyChallenge) {
      val state = _uiState.value.copy(
        setup = setup,
        questionCountLimit = questionLimitFor(setup, countries),
      )
      val result =
        buildQuizStartResult(
          setup = setup,
          countries = countries,
          questionGenerator = questionGenerator,
          hintDifficulty = state.settings.hintDifficulty,
          random = random,
          hintCount = state.hintCount,
          displayName = state.profile.displayName,
          practiceStats = state.countryPracticeStats,
          dailyChallengeCache = state.dailyChallengeCache,
          nowEpochMillis = System.currentTimeMillis(),
          timeZone = state.settings.timeZone,
          mistakeReviewUnlocked = state.mistakeReviewUnlocked,
        )
      if (result.validationError != null) {
        updateState { it.copy(setupError = result.validationError) }
        return
      }
      updateState {
        val quiz = requireNotNull(result.quiz)
        it.copy(
          screen = AppScreen.Quiz,
          setup = setup,
          questionCountLimit = questionLimitFor(setup, countries),
          quiz = quiz,
          dailyChallengeCache = result.dailyChallengeCache ?: it.dailyChallengeCache,
          setupError = null,
        )
      }
    } else {
      updateState {
        it.copy(
          screen = AppScreen.Setup,
          setup = setup,
          questionCountLimit = questionLimitFor(setup, countries),
          setupError = null,
        )
      }
    }
  }

  fun onHintDifficultySelected(difficulty: HintDifficulty) {
    updateSettings { it.copy(hintDifficulty = difficulty) }
  }

  fun onReminderEnabledChanged(enabled: Boolean) {
    updateSettings { it.copy(reminderEnabled = enabled) }
  }

  fun onLanguageSelected(language: AppLanguage) {
    updateStateAndPersistProgress { it.copy(settings = it.settings.copy(language = language)) }
  }

  fun onTimeZoneSelected(timeZone: AppTimeZone) {
    updateSettings { it.copy(timeZone = timeZone) }
    engagementCoordinator?.scheduleDailyCheck()
  }

  fun onAccountNameChanged(name: String) {
    updateStateAndPersistProgress { it.copy(profile = it.profile.copy(accountName = name.take(24))) }
  }

  fun onAvatarSelected(index: Int) {
    updateStateAndPersistProgress {
      it.copy(profile = it.profile.copy(avatarIndex = index.coerceIn(0, maxAvatarIndex)))
    }
  }

  fun onResetHintsClicked() {
    updateStateAndPersistProgress {
      it.copy(
        hintCount = 0,
        quiz = it.quiz.copy(players = it.quiz.players.map { player -> player.copy(hintPoints = 0) }),
      )
    }
  }

  fun onTestingToolsVisibleChanged(visible: Boolean) {
    updateState { it.withTestingToolsVisibleChanged(visible) }
  }

  fun onAddTestingHintsClicked() {
    updateStateAndPersistProgress { it.withAddedTestingHints() }
  }

  fun onTestingLevelUpClicked() {
    updateStateAndPersistProgress { it.withTestingLevelUp() }
  }

  fun onTestingResetLevelClicked() {
    updateStateAndPersistProgress { it.withTestingLevelReset() }
  }

  fun onUnlockRandomAchievementClicked() {
    updateStateAndPersistProgress { it.withRandomAchievementUnlocked(random) }
  }

  fun onLockAllAchievementsClicked() {
    updateStateAndPersistProgress { it.withAllAchievementsLocked() }
  }

  fun onResetAchievementsAndMedalsClicked() {
    updateStateAndPersistProgress { it.withMedalsReset() }
  }

  fun onResetDailyChallengeClicked() {
    updateStateAndPersistProgress {
      it.copy(
        dailyChallengeCache =
          it.dailyChallengeCache?.copy(
            completed = false,
            completedAtEpochMillis = 0L,
          ),
      )
    }
  }

  fun onToggleTestingIconClicked() {
    val nextActiveState = !_uiState.value.inactiveIconActive
    engagementCoordinator?.setInactiveLauncherIcon(nextActiveState)
    updateStateAndPersistProgress { it.withInactiveIconActive(nextActiveState) }
  }

  fun onToggleFavoriteCountry(countryCode: String) {
    updateStateAndPersistProgress { state ->
      val current = state.countryPracticeStats[countryCode] ?: CountryPracticeStats()
      val updated = current.copy(favorite = !current.favorite)
      state.copy(countryPracticeStats = state.countryPracticeStats + (countryCode to updated))
    }
  }

  fun onTriggerTestingReminderClicked() {
    engagementCoordinator?.triggerTestingReminderNotification()
  }

  fun onVariantToggled(variant: QuizVariant) {
    updateState { it.withSelectedVariantsToggled(variant) }
  }

  fun onContinentToggled(continent: String) {
    updateState { it.withContinentToggled(continent, countries) }
  }

  fun onQuestionCountChanged(questionCount: Int) {
    onQuestionCountChanged(questionCount.toString())
  }

  fun onQuestionCountChanged(questionCount: String) {
    updateState { it.withQuestionCountInput(questionCount) }
  }

  fun onSurpriseMeClicked() {
    updateState { it.withSurpriseMeToggled() }
  }

  fun onAllInTypeSelected(allInType: AllInType) {
    updateState { it.withAllInTypeSelected(allInType) }
  }

  fun onMultiplayerBaseSelected(base: MultiplayerQuizBase) {
    updateState { it.withMultiplayerBaseSelected(base, countries) }
  }

  fun onPlayerNameChanged(
    index: Int,
    name: String,
  ) {
    updateState { it.withPlayerNameUpdated(index, name) }
  }

  fun onAddPlayer() {
    updateState { it.withPlayerAdded() }
  }

  fun onRemovePlayer() {
    updateState { it.withPlayerRemoved() }
  }

  fun onStartQuiz() {
    val state = _uiState.value
    val result =
      buildQuizStartResult(
        setup = state.setup,
        countries = countries,
        questionGenerator = questionGenerator,
        hintDifficulty = state.settings.hintDifficulty,
        random = random,
        hintCount = state.hintCount,
        displayName = state.profile.displayName,
        practiceStats = state.countryPracticeStats,
        dailyChallengeCache = state.dailyChallengeCache,
        nowEpochMillis = System.currentTimeMillis(),
        timeZone = state.settings.timeZone,
        mistakeReviewUnlocked = state.mistakeReviewUnlocked,
      )
    if (result.validationError != null) {
      updateState { it.copy(setupError = result.validationError) }
      return
    }

    updateState {
      val quiz = requireNotNull(result.quiz)
      it.copy(
        screen = AppScreen.Quiz,
        quiz = quiz,
        dailyChallengeCache = result.dailyChallengeCache ?: it.dailyChallengeCache,
        setupError = null,
      )
    }
  }

  fun onCountryAnswerSelected(country: FlagCountry) {
    updateState { state -> state.copy(quiz = state.quiz.withSelectedCountry(country)) }
  }

  fun onTypedAnswerChanged(answer: String) {
    updateState { state -> state.copy(quiz = state.quiz.withTypedAnswer(answer)) }
  }

  fun onVerifyTypedAnswer() {
    val state = _uiState.value
    val quiz = state.quiz
    if (quiz.mode != GameMode.Training) return
    if (quiz.currentQuestion?.variant != QuizVariant.TypeCountryName) return
    if (quiz.currentQuestionState.typedAnswer.isBlank() || quiz.currentQuestionState.locked) return

    updateState { state -> state.copy(quiz = state.quiz.withVerifiedTypedAnswer()) }
  }

  fun onUseHint() {
    val state = _uiState.value
    val result = applyHintToCurrentQuestion(state) ?: return
    updateStateAndPersistProgress { it.copy(quiz = result.quiz, hintCount = result.hintCount) }
  }

  fun onNextQuestion() {
    val state = _uiState.value
    val outcome = buildQuestionAdvanceOutcome(state) ?: return
    if (outcome.shouldComplete) {
      completeQuiz(state, outcome.quiz)
    } else {
      updateState { it.copy(quiz = outcome.quiz) }
    }
  }

  fun onSkipQuestion() {
    onUnskipQuestion()
  }

  fun onFinishQuiz() {
    val state = _uiState.value
    val quiz = state.quiz
    if (!quiz.canFinish) return
    completeQuiz(state, quiz.withCurrentQuestionSubmitted() ?: quiz)
  }

  fun onPreviousQuestion() {
    val state = _uiState.value
    val quiz = state.quiz
    updateState { it.copy(quiz = quiz.withPreviousQuestionLoaded()) }
  }

  fun onNextQuestionPreview() {
    val state = _uiState.value
    val quiz = state.quiz
    updateState { it.copy(quiz = quiz.withPreviewAdvancedQuestion()) }
  }

  fun onQuestionBack() {
    val state = _uiState.value
    val quiz = state.quiz
    updateState { it.copy(quiz = quiz.withPreviousQuestionLoaded()) }
  }

  fun onQuestionForward() {
    onNextQuestionPreview()
  }

  fun onUnskipQuestion() {
    val state = _uiState.value
    val quiz = state.quiz
    updateState { it.copy(quiz = quiz.withNextSkippedQuestionLoaded()) }
  }

  fun onPlayAgain() {
    onStartQuiz()
  }

  private fun completeQuiz(
    state: FlagGameUiState,
    quiz: QuizState,
  ) {
    val completionTime = System.currentTimeMillis()
    val completionResult = buildQuizCompletionResult(state, quiz, countries, completionTime)
    updateState { completionResult.uiState }
    persistProgress(progressStore, _uiState.value)
    recordCompletedQuiz(
      progressStore = progressStore,
      mode = quiz.mode ?: GameMode.Training,
      totalQuestions = completionResult.summary.completedResults.size,
      correctAnswers = completionResult.summary.correctAnswers,
      skippedAnswers = 0,
      netScore = completionResult.summary.scoredPlayers.sumOf { it.score },
    )
  }

  companion object {
    fun factory(context: Context): ViewModelProvider.Factory =
      viewModelFactory {
        initializer {
          val container = AppGraph.from(context.applicationContext)
          val initialPersistedState =
            runBlocking {
              val hintDifficulty = container.settingsStore.loadHintDifficulty()
              val reminderEnabled = container.settingsStore.loadReminderEnabled()
              val timeZone = container.settingsStore.loadTimeZone()
              val progress = container.progressStore.loadProgress()
              progress.copy(hintDifficulty = hintDifficulty, reminderEnabled = reminderEnabled, timeZone = timeZone)
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

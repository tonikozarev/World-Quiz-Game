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
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.SavedQuizTemplate
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.hasSameQuizConfiguration
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
        nowEpochMillis = System.currentTimeMillis(),
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
    val state = _uiState.value
    when (state.screen) {
      AppScreen.Quiz,
      AppScreen.Setup,
      AppScreen.Results ->
        updateState {
          it.copy(
            screen = state.quizReturnTarget,
            setupError = null,
          )
        }
      AppScreen.GameModesHub -> navigateTo(AppScreen.GameModes)
      AppScreen.GameModes,
      AppScreen.Medals,
      AppScreen.Achievements,
      AppScreen.Favorites,
      AppScreen.Settings,
      AppScreen.Menu -> {
        updateState {
          it.resetToMenu(
            allContinents = allContinents,
            selectableContinents = selectableContinents,
            questionCountLimit = countries.size,
          )
        }
      }
    }
  }

  fun refreshDailyChallengeAvailability() {
    val nowEpochMillis = System.currentTimeMillis()
    val currentState = _uiState.value
    val updated =
      currentState.let { state ->
        val refreshedCache =
          buildDailyChallengeCache(
            countries = countries,
            topic = QuizTopic.Mixed,
            dailyChallengeCache = state.dailyChallengeCacheFor(QuizTopic.Mixed) ?: state.dailyChallengeCacheFor(QuizTopic.Countries),
            nowEpochMillis = nowEpochMillis,
          )
        if (refreshedCache == state.dailyChallengeCache) {
          state
        } else {
          state.copy(dailyChallengeCaches = state.dailyChallengeCaches.withDailyChallengeCache(refreshedCache))
        }
      }
    if (updated != currentState) {
      updateState { updated }
      persistProgress(progressStore, _uiState.value)
    }
  }

  fun onStartClicked() {
    navigateTo(AppScreen.GameModes)
  }

  fun onGameModesClicked() {
    navigateTo(AppScreen.GameModesHub)
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
    onBackToMenu()
  }

  fun onLevelUpSeen() {
    updateState { it.withLevelUpSeen() }
  }

  fun onModeSelected(mode: GameMode) {
    val currentReturnTarget =
      when (_uiState.value.screen) {
        AppScreen.GameModesHub -> AppScreen.GameModesHub
        else -> AppScreen.GameModes
      }
    val topic =
      when (mode) {
        GameMode.DailyChallenge -> QuizTopic.Mixed
        else -> _uiState.value.selectedQuizTopic
      }
    val setup = buildSetupForMode(mode, topic, selectableContinents, countries, _uiState.value.profile.displayName)
    val practiceStats = _uiState.value.countryPracticeStats
    val questionCountLimit = questionLimitFor(setup, countries, practiceStats)
    val preparedSetup =
      if (mode == GameMode.MistakeReview) {
        setup.copy(questionCountInput = questionCountLimit.toString())
      } else {
        setup
      }
    if (mode == GameMode.DailyChallenge) {
      val state = _uiState.value.copy(
        quizReturnTarget = currentReturnTarget,
        setup = preparedSetup,
        questionCountLimit = questionCountLimit,
      )
      val result =
        buildQuizStartResult(
          setup = preparedSetup,
          countries = countries,
          questionGenerator = questionGenerator,
          hintDifficulty = state.settings.hintDifficulty,
          random = random,
          hintCount = state.hintCount,
          displayName = state.profile.displayName,
          practiceStats = state.countryPracticeStats,
          dailyChallengeCache = state.dailyChallengeCacheFor(QuizTopic.Mixed) ?: state.dailyChallengeCacheFor(QuizTopic.Countries),
          nowEpochMillis = System.currentTimeMillis(),
          mistakeReviewUnlocked = state.mistakeReviewUnlocked,
        )
      if (result.validationError != null) {
        updateState { it.copy(setupError = result.validationError) }
        return
      }
      updateStateAndPersistProgress {
        val quiz = requireNotNull(result.quiz)
        it.copy(
          screen = AppScreen.Quiz,
          quizReturnTarget = currentReturnTarget,
          setup = preparedSetup,
          questionCountLimit = questionCountLimit,
          quiz = quiz,
          dailyChallengeCaches = it.dailyChallengeCaches.withDailyChallengeCache(result.dailyChallengeCache),
          setupError = null,
        )
      }
    } else {
      updateState {
        it.copy(
          screen = AppScreen.Setup,
          quizReturnTarget = currentReturnTarget,
          setup = preparedSetup,
          questionCountLimit = questionCountLimit,
          setupError = null,
        )
      }
    }
  }

  fun onQuizTopicSelected(topic: QuizTopic) {
    updateState { it.withSelectedQuizTopic(topic, countries) }
    refreshDailyChallengeAvailability()
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
        hintCount = 0.0,
        quiz = it.quiz.copy(players = it.quiz.players.map { player -> player.copy(hintPoints = 0.0) }),
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
        dailyChallengeCaches =
          it.dailyChallengeCaches.mapValues { (_, cache) ->
            cache.copy(
              completed = false,
              completedAtEpochMillis = 0L,
            )
          },
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

  fun onInstantCorrectionToggled() {
    updateState { it.withInstantCorrectionToggled() }
  }

  fun onCreateQuizSourceSelected(source: CreateQuizSource) {
    updateState { it.withCreateQuizSourceSelected(source, countries) }
  }

  fun onCreateQuizPresetSelected(preset: CreateQuizPreset) {
    updateState { it.withCreateQuizPresetSelected(preset, countries) }
  }

  fun onCreateQuizCountryToggled(countryCode: String) {
    updateState { it.withCreateQuizCountryToggled(countryCode, countries) }
  }

  fun onCreateQuizCountryBulkToggled(countryCodes: Set<String>) {
    updateState { it.withCreateQuizCountryBulkToggled(countryCodes, countries) }
  }

  fun onCreateQuizCapitalToggled(countryCode: String) {
    updateState { it.withCreateQuizCapitalToggled(countryCode, countries) }
  }

  fun onCreateQuizCapitalBulkToggled(countryCodes: Set<String>) {
    updateState { it.withCreateQuizCapitalBulkToggled(countryCodes, countries) }
  }

  fun onCreateQuizContinentToggled(continent: String) {
    updateState { it.withCreateQuizContinentToggled(continent, countries) }
  }

  fun onCreateQuizAllCountriesToggled() {
    updateState { it.withCreateQuizAllCountriesToggled(countries) }
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

  fun onSpeedRunSecondsChanged(seconds: String) {
    updateState { it.withSpeedRunSecondsPerAnswerInput(seconds) }
  }

  fun onCreateQuizManualHardcoreToggled() {
    updateState { it.withCreateQuizManualHardcoreToggled(countries) }
  }

  fun onCreateQuizManualTimerToggled() {
    updateState { it.withCreateQuizManualTimerEnabledToggled() }
  }

  fun onCreateQuizTrainingToggled() {
    updateState { it.withCreateQuizTrainingToggled(countries) }
  }

  fun onCreateQuizLocalMultiplayerToggled() {
    updateState { it.withCreateQuizLocalMultiplayerToggled(countries) }
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

  private fun startQuizWithSetup(setup: SetupState) {
    val state = _uiState.value
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
          quizReturnTarget = state.quizReturnTarget,
        quiz = quiz,
        dailyChallengeCaches = it.dailyChallengeCaches.withDailyChallengeCache(result.dailyChallengeCache),
        setupError = null,
      )
    }
  }

  fun onStartQuiz() {
    startQuizWithSetup(_uiState.value.setup)
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
    if (!quiz.instantCorrectionEnabled) return
    if (quiz.currentQuestion?.variant != QuizVariant.TypeText) return
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

  fun onSpeedRunTimeExpired() {
    val state = _uiState.value
    val quiz = state.quiz
    if (!quiz.countdownEnabled || quiz.timedOut) return
    updateState {
      it.copy(
        screen = AppScreen.Results,
        quiz =
          quiz.copy(
            timedOut = true,
            results = buildQuizResults(quiz, state.settings.language),
          ),
      )
    }
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

  fun onSaveCreateQuizClicked(
    templateName: String,
    replaceTemplateId: String? = null,
  ): SaveQuizResult {
    val state = _uiState.value
    val setup = state.setup
    if (setup.mode != GameMode.CreateQuiz) return SaveQuizResult.NoOp
    if (setup.usesCreateQuizTraining) return SaveQuizResult.NoOp

    val seed = if (setup.createQuizSeed != 0L) setup.createQuizSeed else random.nextLong()
    val resolvedQuestionCountryCodes =
      resolveCreateQuizQuestionCountryCodes(
        setup = setup.copy(createQuizSeed = seed),
        hintDifficulty = state.settings.hintDifficulty,
      )
    val exactQuestionCount =
      when (setup.createQuizSource) {
        CreateQuizSource.ManualCountriesCapitals ->
          if (setup.topic == QuizTopic.Mixed) {
            (setup.selectedCountryCodes.size + setup.selectedCapitalCountryCodes.size).coerceAtLeast(1)
          } else {
            setup.selectedCountryCodes.size.coerceAtLeast(1)
          }
        CreateQuizSource.PresetFilter -> setup.questionCount ?: state.questionCountLimit
      }
    val templateTitle =
      when (setup.createQuizSource) {
        CreateQuizSource.PresetFilter -> {
          val presetTitle =
            localizedCreateQuizPresetTitle(setup.createQuizPreset, state.settings.language, setup.topic)
          when (state.settings.language) {
            AppLanguage.English -> "$presetTitle quiz"
            AppLanguage.Bulgarian -> "Тест с $presetTitle"
            AppLanguage.German -> "Quiz mit $presetTitle"
          }
        }
        CreateQuizSource.ManualCountriesCapitals ->
          when (state.settings.language) {
            AppLanguage.English -> "Manual quiz"
            AppLanguage.Bulgarian -> "Ръчен тест"
            AppLanguage.German -> "Manuelles Quiz"
          }
      }
    val template =
      SavedQuizTemplate(
        id = "saved-${seed}-${setup.createQuizSource.name.lowercase()}",
        createdAtEpochMillis = System.currentTimeMillis(),
        title = templateName.trim().take(30).ifBlank { templateTitle },
        topic = setup.topic,
        source = setup.createQuizSource,
        preset = if (setup.createQuizSource == CreateQuizSource.PresetFilter) setup.createQuizPreset else null,
        selectedCountryCodes = setup.selectedCountryCodes,
        selectedCapitalCountryCodes = setup.selectedCapitalCountryCodes,
        questionCountryCodes = resolvedQuestionCountryCodes,
        variants = setup.variants,
        questionCount = exactQuestionCount.coerceAtLeast(1),
        seed = seed,
        createQuizLocalMultiplayerEnabled = setup.usesCreateQuizLocalMultiplayer,
        playerNames = if (setup.usesCreateQuizLocalMultiplayer) setup.playerNames else emptyList(),
      )

    val normalizedTargetName = template.title.trim().lowercase()
    val sameNameTemplate = state.savedQuizTemplates.firstOrNull { it.title.trim().lowercase() == normalizedTargetName }
    if (sameNameTemplate != null && sameNameTemplate.id != replaceTemplateId) {
      return SaveQuizResult.NameConflict(
        existingTemplateId = sameNameTemplate.id,
        existingName = sameNameTemplate.title,
      )
    }

    val sameConfigurationTemplate =
      state.savedQuizTemplates.firstOrNull { it.hasSameQuizConfiguration(template) }
    if (sameConfigurationTemplate != null && sameConfigurationTemplate.id != replaceTemplateId) {
      return SaveQuizResult.DuplicateConfiguration(existingName = sameConfigurationTemplate.title)
    }

    val replacingExisting = replaceTemplateId != null
    if (!replacingExisting && state.savedQuizTemplates.size >= 10) {
      val oldestTemplate = state.savedQuizTemplates.minByOrNull { it.createdAtEpochMillis }
      if (oldestTemplate != null) {
        return SaveQuizResult.CapacityConflict(
          replaceTemplateId = oldestTemplate.id,
          replaceTemplateName = oldestTemplate.title,
        )
      }
    }

    updateStateAndPersistProgress { current ->
      val templates =
        (listOf(template) +
          current.savedQuizTemplates
            .filterNot { it.id == template.id }
            .filterNot { it.id == replaceTemplateId })
      current.copy(
        setup = current.setup.copy(createQuizSeed = seed),
        savedQuizTemplates = templates,
      )
    }

    return SaveQuizResult.Saved(
      when (state.settings.language) {
        AppLanguage.English -> "Saved \"${template.title}\"."
        AppLanguage.Bulgarian -> "Р—Р°РїР°Р·РµРЅ Рµ \"${template.title}\"."
        AppLanguage.German -> "\"${template.title}\" wurde gespeichert."
      },
    )
  }

  private fun resolveCreateQuizQuestionCountryCodes(
    setup: SetupState,
    hintDifficulty: HintDifficulty,
  ): Set<String> {
    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = questionGenerator,
        hintDifficulty = hintDifficulty,
        random = Random(setup.createQuizSeed),
        hintCount = 0.0,
        displayName = _uiState.value.profile.displayName,
      )
    return quiz.questions.map { it.correctCountry.code }.toSet()
  }

  fun onRemoveSavedQuizTemplate(templateId: String) {
    updateStateAndPersistProgress { current ->
      current.copy(savedQuizTemplates = current.savedQuizTemplates.filterNot { it.id == templateId })
    }
  }

  fun onOpenSavedQuizTemplate(templateId: String) {
    val state = _uiState.value
    val template = state.savedQuizTemplates.firstOrNull { it.id == templateId } ?: return
    val setup =
      SetupState(
        mode = GameMode.CreateQuiz,
        topic = template.topic,
        variants = template.variants,
        createQuizSource = template.source,
        createQuizPreset = template.preset ?: createQuizDefaultPresetsForTopic(template.topic).first(),
        createQuizPresets = template.preset?.let { setOf(it) } ?: createQuizDefaultPresetsForTopic(template.topic),
        selectedCountryCodes = template.selectedCountryCodes,
        selectedCapitalCountryCodes = template.selectedCapitalCountryCodes,
        createQuizSeed = template.seed,
        savedQuizTemplateId = template.id,
        createQuizLocalMultiplayerEnabled = template.createQuizLocalMultiplayerEnabled,
        playerNames = template.playerNames.ifEmpty { listOf("Player 1", "Player 2") },
        questionCountInput =
          if (template.topic == QuizTopic.Mixed) {
            (template.selectedCountryCodes.size + template.selectedCapitalCountryCodes.size).toString()
          } else {
            template.questionCount.toString()
          },
      )
    updateState {
      it.copy(
        screen = AppScreen.Setup,
        selectedQuizTopic = template.topic,
        quizReturnTarget = AppScreen.Favorites,
        setup = setup,
        questionCountLimit = questionLimitFor(setup, countries),
        setupError = null,
      )
    }
  }

  sealed interface SaveQuizResult {
    data object NoOp : SaveQuizResult

    data class Saved(
      val message: String,
    ) : SaveQuizResult

    data class DuplicateConfiguration(
      val existingName: String,
    ) : SaveQuizResult

    data class NameConflict(
      val existingTemplateId: String,
      val existingName: String,
    ) : SaveQuizResult

    data class CapacityConflict(
      val replaceTemplateId: String,
      val replaceTemplateName: String,
    ) : SaveQuizResult
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


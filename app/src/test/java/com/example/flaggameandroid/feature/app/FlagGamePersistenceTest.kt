package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.DailyChallengeTheme
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.SavedQuizTemplate
import com.example.flaggameandroid.persistence.PersistedAppState
import com.example.flaggameandroid.persistence.PersistedQuizHistory
import com.example.flaggameandroid.persistence.ProgressStore
import com.example.flaggameandroid.persistence.SettingsStore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class FlagGamePersistenceTest {
  @Test
  fun persistedState_isLoadedIntoInitialUiState() {
    val viewModel =
      viewModel(
        initialPersistedState =
          PersistedAppState(
            hintDifficulty = HintDifficulty.Hard,
            reminderEnabled = false,
            hintCount = 7.0,
            level = 3,
            hintsTowardNextLevel = 4,
            correctAnswersTowardNextLevel = 18,
            eligibleQuizzesTowardNextLevel = 2,
            mistakeReviewUnlocked = true,
            dailyChallengeCaches = emptyMap(),
          ),
      )

    val state = viewModel.uiState.value
    assertEquals(HintDifficulty.Hard, state.settings.hintDifficulty)
    assertEquals(false, state.settings.reminderEnabled)
    assertEquals(7.0, state.hintCount)
    assertEquals(3, state.levelProgress.level)
    assertEquals(4, state.levelProgress.hintsTowardNextLevel)
    assertEquals(18, state.levelProgress.correctAnswersTowardNextLevel)
    assertEquals(2, state.levelProgress.eligibleQuizzesTowardNextLevel)
    assertEquals(true, state.mistakeReviewUnlocked)
  }

  @Test
  fun changingHintDifficulty_savesToSettingsStore() {
    val settingsStore = RecordingSettingsStore()
    val viewModel = viewModel(settingsStore = settingsStore)

    viewModel.onHintDifficultySelected(HintDifficulty.Impossible)

    assertEquals(HintDifficulty.Impossible, settingsStore.savedHintDifficulty)
  }

  @Test
  fun changingReminderToggle_savesToSettingsStore() {
    val settingsStore = RecordingSettingsStore()
    val viewModel = viewModel(settingsStore = settingsStore)

    viewModel.onReminderEnabledChanged(false)

    assertEquals(false, settingsStore.savedReminderEnabled)
  }

  @Test
  fun togglingTestingIcon_updatesUiStateAndSavedProgress() {
    val progressStore = RecordingProgressStore()
    val viewModel = viewModel(progressStore = progressStore)

    viewModel.onToggleTestingIconClicked()

    assertEquals(true, viewModel.uiState.value.inactiveIconActive)
    assertTrue(progressStore.savedProgressSnapshots.last().inactiveIconActive)
  }

  @Test
  fun finishingQuiz_savesProgressAndRecordsHistory() {
    val progressStore = RecordingProgressStore()
    val viewModel = viewModel(progressStore = progressStore)

    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)
    QuizVariant.entries.filterNot { it == QuizVariant.FlagToCountry }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(10)
    viewModel.onStartQuiz()

    repeat(10) {
      val question = viewModel.uiState.value.quiz.currentQuestion!!
      viewModel.onCountryAnswerSelected(question.correctCountry)
      viewModel.onNextQuestion()
    }

    assertEquals(1, progressStore.savedProgressSnapshots.size)
    assertEquals(1, progressStore.recordedHistories.size)
    assertEquals(10, progressStore.savedProgressSnapshots.single().correctAnswersTowardNextLevel)
    assertEquals(1, progressStore.savedProgressSnapshots.single().eligibleQuizzesTowardNextLevel)
    assertEquals(10, progressStore.recordedHistories.single().totalQuestions)
    assertEquals(10, progressStore.recordedHistories.single().correctAnswers)
  }

  @Test
  fun resettingDailyChallenge_clearsCompletionFlagInUiStateAndPersistence() {
    val progressStore = RecordingProgressStore()
    val viewModel =
      viewModel(
        progressStore = progressStore,
        initialPersistedState =
          PersistedAppState(
            dailyChallengeCaches =
              mapOf(
                QuizTopic.Countries to
                  DailyChallengeCache(
                    dayKey = 123L,
                    theme = DailyChallengeTheme.Europe,
                    questionCount = 10,
                    seed = 42L,
                    completed = true,
                    completedAtEpochMillis = 1_234_567L,
                  ),
              ),
          ),
      )

    viewModel.onResetDailyChallengeClicked()

    val resetCache = viewModel.uiState.value.dailyChallengeCache
    assertEquals(false, resetCache?.completed)
    assertEquals(0L, resetCache?.completedAtEpochMillis)
    assertEquals(false, progressStore.savedProgressSnapshots.last().dailyChallengeCache?.completed)
    assertEquals(0L, progressStore.savedProgressSnapshots.last().dailyChallengeCache?.completedAtEpochMillis)
  }

  @Test
  fun openingSavedQuizTemplate_onlyLoadsSetupInsteadOfStartingQuiz() {
    val template =
      SavedQuizTemplate(
        id = "saved-1",
        createdAtEpochMillis = 1L,
        title = "My quiz",
        topic = QuizTopic.Mixed,
        source = CreateQuizSource.ManualCountriesCapitals,
        selectedCountryCodes = setOf("DE", "BG", "AT"),
        selectedCapitalCountryCodes = setOf("DE", "BG", "AT"),
        variants = setOf(QuizVariant.FlagToCountry),
        questionCount = 6,
        seed = 99L,
      )
    val viewModel =
      viewModel(
        initialPersistedState =
          PersistedAppState(
            savedQuizTemplates = listOf(template),
          ),
      )

    viewModel.onOpenSavedQuizTemplate("saved-1")

    assertEquals(AppScreen.Setup, viewModel.uiState.value.screen)
    assertEquals(GameMode.CreateQuiz, viewModel.uiState.value.setup.mode)
    assertEquals(3, viewModel.uiState.value.setup.selectedCountryCodes.size)
    assertEquals(3, viewModel.uiState.value.setup.selectedCapitalCountryCodes.size)
    assertTrue(viewModel.uiState.value.quiz.questions.isEmpty())
  }

  private fun viewModel(
    settingsStore: SettingsStore = RecordingSettingsStore(),
    progressStore: ProgressStore = RecordingProgressStore(),
    initialPersistedState: PersistedAppState = PersistedAppState(),
  ): FlagGameViewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = QuizQuestionGenerator(Random(21)),
      random = Random(22),
      settingsStore = settingsStore,
      progressStore = progressStore,
      initialPersistedState = initialPersistedState,
    )

  private class RecordingSettingsStore : SettingsStore {
    var savedHintDifficulty: HintDifficulty? = null
    var savedReminderEnabled: Boolean? = null

    override suspend fun loadHintDifficulty(): HintDifficulty = HintDifficulty.Medium

    override suspend fun loadReminderEnabled(): Boolean = true

    override suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty) {
      savedHintDifficulty = hintDifficulty
    }

    override suspend fun saveReminderEnabled(enabled: Boolean) {
      savedReminderEnabled = enabled
    }
  }

  private class RecordingProgressStore : ProgressStore {
    val savedProgressSnapshots = mutableListOf<PersistedAppState>()
    val recordedHistories = mutableListOf<PersistedQuizHistory>()

    override suspend fun loadProgress(): PersistedAppState = PersistedAppState()

    override suspend fun saveProgress(progress: PersistedAppState) {
      savedProgressSnapshots += progress
    }

    override suspend fun recordQuiz(history: PersistedQuizHistory) {
      recordedHistories += history
    }
  }
}

package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
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
            hintCount = 7,
            level = 3,
            hintsTowardNextLevel = 4,
            correctAnswersTowardNextLevel = 18,
            eligibleQuizzesTowardNextLevel = 2,
          ),
      )

    val state = viewModel.uiState.value
    assertEquals(HintDifficulty.Hard, state.settings.hintDifficulty)
    assertEquals(7, state.hintCount)
    assertEquals(3, state.levelProgress.level)
    assertEquals(4, state.levelProgress.hintsTowardNextLevel)
    assertEquals(18, state.levelProgress.correctAnswersTowardNextLevel)
    assertEquals(2, state.levelProgress.eligibleQuizzesTowardNextLevel)
  }

  @Test
  fun changingHintDifficulty_savesToSettingsStore() {
    val settingsStore = RecordingSettingsStore()
    val viewModel = viewModel(settingsStore = settingsStore)

    viewModel.onHintDifficultySelected(HintDifficulty.Impossible)

    assertEquals(HintDifficulty.Impossible, settingsStore.savedHintDifficulty)
  }

  @Test
  fun finishingQuiz_savesProgressAndRecordsHistory() {
    val progressStore = RecordingProgressStore()
    val viewModel = viewModel(progressStore = progressStore)

    viewModel.onModeSelected(GameMode.Continents)
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

    override suspend fun loadHintDifficulty(): HintDifficulty = HintDifficulty.Medium

    override suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty) {
      savedHintDifficulty = hintDifficulty
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

package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.feature.app.FlagGameUiState
import com.example.flaggameandroid.feature.app.SettingsState
import com.example.flaggameandroid.feature.app.toPersistedAppState
import kotlinx.coroutines.runBlocking

internal fun persistSettings(
  settingsStore: SettingsStore,
  settings: SettingsState,
) {
  runBlocking {
    settingsStore.saveHintDifficulty(settings.hintDifficulty)
  }
}

internal fun persistProgress(
  progressStore: ProgressStore,
  state: FlagGameUiState,
) {
  runBlocking {
    progressStore.saveProgress(state.toPersistedAppState())
  }
}

internal fun recordCompletedQuiz(
  progressStore: ProgressStore,
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

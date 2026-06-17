package com.example.flaggameandroid.persistence

interface ProgressStore {
  suspend fun loadProgress(): PersistedAppState

  suspend fun saveProgress(progress: PersistedAppState)

  suspend fun recordQuiz(history: PersistedQuizHistory)
}

class InMemoryProgressStore(
  initialState: PersistedAppState = PersistedAppState(),
) : ProgressStore {
  private var storedState: PersistedAppState = initialState
  private val recordedQuizzes = mutableListOf<PersistedQuizHistory>()

  override suspend fun loadProgress(): PersistedAppState = storedState

  override suspend fun saveProgress(progress: PersistedAppState) {
    storedState = progress
  }

  override suspend fun recordQuiz(history: PersistedQuizHistory) {
    recordedQuizzes += history
  }
}

package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.GameMode

class RoomProgressStore(
  private val progressDao: ProgressDao,
  private val quizHistoryDao: QuizHistoryDao,
) : ProgressStore {
  override suspend fun loadProgress(): PersistedAppState {
    val entity = progressDao.load() ?: return PersistedAppState()
    return PersistedAppState(
      hintCount = entity.hintCount,
      level = entity.level,
      hintsTowardNextLevel = entity.hintsTowardNextLevel,
      correctAnswersTowardNextLevel = entity.correctAnswersTowardNextLevel,
      eligibleQuizzesTowardNextLevel = entity.eligibleQuizzesTowardNextLevel,
    )
  }

  override suspend fun saveProgress(progress: PersistedAppState) {
    progressDao.upsert(
      ProgressEntity(
        hintCount = progress.hintCount,
        level = progress.level,
        hintsTowardNextLevel = progress.hintsTowardNextLevel,
        correctAnswersTowardNextLevel = progress.correctAnswersTowardNextLevel,
        eligibleQuizzesTowardNextLevel = progress.eligibleQuizzesTowardNextLevel,
      ),
    )
  }

  override suspend fun recordQuiz(history: PersistedQuizHistory) {
    quizHistoryDao.insert(
      QuizHistoryEntity(
        mode = history.mode.name,
        totalQuestions = history.totalQuestions,
        correctAnswers = history.correctAnswers,
        skippedAnswers = history.skippedAnswers,
        netScore = history.netScore,
        completedAtEpochMillis = history.completedAtEpochMillis,
      ),
    )
  }
}

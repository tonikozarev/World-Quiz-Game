package com.example.flaggameandroid.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [ProgressEntity::class, QuizHistoryEntity::class],
  version = 8,
  exportSchema = false,
)
abstract class FlagGameDatabase : RoomDatabase() {
  abstract fun progressDao(): ProgressDao

  abstract fun quizHistoryDao(): QuizHistoryDao
}

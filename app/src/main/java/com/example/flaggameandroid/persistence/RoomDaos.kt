package com.example.flaggameandroid.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProgressDao {
  @Query("SELECT * FROM progress WHERE id = 1")
  suspend fun load(): ProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: ProgressEntity)
}

@Dao
interface QuizHistoryDao {
  @Insert
  suspend fun insert(entity: QuizHistoryEntity)
}

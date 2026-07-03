package com.example.flaggameandroid.persistence

import android.content.Context
import androidx.room.Room
import com.example.flaggameandroid.engagement.AppEngagementCoordinator

class AppContainer(
  context: Context,
) {
  private val database: FlagGameDatabase =
    Room.databaseBuilder(
      context,
      FlagGameDatabase::class.java,
      "flag_game.db",
    ).addMigrations(Migration1To2, Migration2To3, Migration3To4, Migration4To5, Migration5To6, Migration6To7, Migration7To8).build()

  val settingsStore: SettingsStore = DataStoreSettingsStore(context.flagGameSettingsDataStore)

  val progressStore: ProgressStore =
    RoomProgressStore(
      progressDao = database.progressDao(),
      quizHistoryDao = database.quizHistoryDao(),
    )

  val engagementCoordinator: AppEngagementCoordinator =
    AppEngagementCoordinator(
      context = context.applicationContext,
      settingsStore = settingsStore,
      progressStore = progressStore,
    )
}

object AppGraph {
  @Volatile
  private var container: AppContainer? = null

  fun from(context: Context): AppContainer {
    return container ?: synchronized(this) {
      container ?: AppContainer(context.applicationContext).also { container = it }
    }
  }
}

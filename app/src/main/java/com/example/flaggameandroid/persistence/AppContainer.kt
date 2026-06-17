package com.example.flaggameandroid.persistence

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room

private val Context.flagGameSettingsDataStore by preferencesDataStore(name = "flag_game_settings")

class AppContainer(
  context: Context,
) {
  private val database: FlagGameDatabase =
    Room.databaseBuilder(
      context,
      FlagGameDatabase::class.java,
      "flag_game.db",
    ).build()

  val settingsStore: SettingsStore = DataStoreSettingsStore(context.flagGameSettingsDataStore)

  val progressStore: ProgressStore =
    RoomProgressStore(
      progressDao = database.progressDao(),
      quizHistoryDao = database.quizHistoryDao(),
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

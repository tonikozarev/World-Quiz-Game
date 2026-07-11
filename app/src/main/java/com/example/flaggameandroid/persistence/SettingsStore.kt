package com.example.flaggameandroid.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.flaggameandroid.core.model.HintDifficulty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SettingsStore {
  suspend fun loadHintDifficulty(): HintDifficulty

  suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty)
}

class DataStoreSettingsStore(
  private val dataStore: DataStore<Preferences>,
) : SettingsStore {
  override suspend fun loadHintDifficulty(): HintDifficulty {
    return dataStore.data
      .map { preferences ->
        preferences[HintDifficultyKey]?.let(::parseHintDifficulty) ?: HintDifficulty.Medium
      }
      .first()
  }

  override suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty) {
    dataStore.edit { preferences ->
      preferences[HintDifficultyKey] = hintDifficulty.name
    }
  }

  private companion object {
    val HintDifficultyKey = stringPreferencesKey("hint_difficulty")
  }
}

private fun parseHintDifficulty(value: String): HintDifficulty =
  when (value) {
    "Easy" -> HintDifficulty.Easy
    else -> HintDifficulty.valueOf(value)
  }

class InMemorySettingsStore(
  initialHintDifficulty: HintDifficulty = HintDifficulty.Medium,
) : SettingsStore {
  private var storedHintDifficulty: HintDifficulty = initialHintDifficulty

  override suspend fun loadHintDifficulty(): HintDifficulty = storedHintDifficulty

  override suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty) {
    storedHintDifficulty = hintDifficulty
  }
}

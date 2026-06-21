package com.example.flaggameandroid.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.HintDifficulty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SettingsStore {
  suspend fun loadHintDifficulty(): HintDifficulty

  suspend fun loadReminderEnabled(): Boolean

  suspend fun loadTimeZone(): AppTimeZone

  suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty)

  suspend fun saveReminderEnabled(enabled: Boolean)

  suspend fun saveTimeZone(timeZone: AppTimeZone)
}

class DataStoreSettingsStore(
  private val dataStore: DataStore<Preferences>,
) : SettingsStore {
  override suspend fun loadHintDifficulty(): HintDifficulty {
    return dataStore.data
      .map { preferences ->
        preferences[HintDifficultyKey]?.let(HintDifficulty::valueOf) ?: HintDifficulty.Medium
      }
      .first()
  }

  override suspend fun loadReminderEnabled(): Boolean {
    return dataStore.data
      .map { preferences -> preferences[ReminderEnabledKey] ?: true }
      .first()
  }

  override suspend fun loadTimeZone(): AppTimeZone {
    return dataStore.data
      .map { preferences -> AppTimeZone.fromNameOrDefault(preferences[TimeZoneKey]) }
      .first()
  }

  override suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty) {
    dataStore.edit { preferences ->
      preferences[HintDifficultyKey] = hintDifficulty.name
    }
  }

  override suspend fun saveReminderEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
      preferences[ReminderEnabledKey] = enabled
    }
  }

  override suspend fun saveTimeZone(timeZone: AppTimeZone) {
    dataStore.edit { preferences ->
      preferences[TimeZoneKey] = timeZone.name
    }
  }

  private companion object {
    val HintDifficultyKey = stringPreferencesKey("hint_difficulty")
    val ReminderEnabledKey = booleanPreferencesKey("reminder_enabled")
    val TimeZoneKey = stringPreferencesKey("time_zone")
  }
}

class InMemorySettingsStore(
  initialHintDifficulty: HintDifficulty = HintDifficulty.Medium,
  initialReminderEnabled: Boolean = true,
  initialTimeZone: AppTimeZone = AppTimeZone.default(),
) : SettingsStore {
  private var storedHintDifficulty: HintDifficulty = initialHintDifficulty
  private var storedReminderEnabled: Boolean = initialReminderEnabled
  private var storedTimeZone: AppTimeZone = initialTimeZone

  override suspend fun loadHintDifficulty(): HintDifficulty = storedHintDifficulty

  override suspend fun loadReminderEnabled(): Boolean = storedReminderEnabled

  override suspend fun loadTimeZone(): AppTimeZone = storedTimeZone

  override suspend fun saveHintDifficulty(hintDifficulty: HintDifficulty) {
    storedHintDifficulty = hintDifficulty
  }

  override suspend fun saveReminderEnabled(enabled: Boolean) {
    storedReminderEnabled = enabled
  }

  override suspend fun saveTimeZone(timeZone: AppTimeZone) {
    storedTimeZone = timeZone
  }
}

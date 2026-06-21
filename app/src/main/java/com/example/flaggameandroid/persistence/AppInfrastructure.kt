package com.example.flaggameandroid.persistence

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val Context.flagGameSettingsDataStore by preferencesDataStore(name = "flag_game_settings")

internal val Migration1To2 =
  object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE progress ADD COLUMN lastOpenedAtEpochMillis INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE progress ADD COLUMN lastPlayedAtEpochMillis INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE progress ADD COLUMN inactiveIconActive INTEGER NOT NULL DEFAULT 0")
    }
  }

internal val Migration2To3 =
  object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE progress ADD COLUMN ratingsSerialized TEXT NOT NULL DEFAULT ''")
      db.execSQL("ALTER TABLE progress ADD COLUMN achievementUnlocksSerialized TEXT NOT NULL DEFAULT ''")
    }
  }

internal val Migration3To4 =
  object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE progress ADD COLUMN accountName TEXT NOT NULL DEFAULT ''")
      db.execSQL("ALTER TABLE progress ADD COLUMN avatarIndex INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE progress ADD COLUMN languageName TEXT NOT NULL DEFAULT 'English'")
    }
  }

internal val Migration4To5 =
  object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE progress ADD COLUMN countryPracticeSerialized TEXT NOT NULL DEFAULT ''")
      db.execSQL("ALTER TABLE progress ADD COLUMN activityCalendarSerialized TEXT NOT NULL DEFAULT ''")
      db.execSQL("ALTER TABLE progress ADD COLUMN dailyChallengeSerialized TEXT NOT NULL DEFAULT ''")
    }
  }

internal val Migration5To6 =
  object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE progress ADD COLUMN mistakeReviewUnlocked INTEGER NOT NULL DEFAULT 0")
    }
  }

package com.example.flaggameandroid.persistence

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration6To7 =
  object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL(
        "ALTER TABLE progress ADD COLUMN savedQuizTemplatesSerialized TEXT NOT NULL DEFAULT ''",
      )
    }
  }

val Migration7To8 =
  object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("CREATE TABLE progress_new (id INTEGER NOT NULL, hintCount REAL NOT NULL, level INTEGER NOT NULL, hintsTowardNextLevel INTEGER NOT NULL, correctAnswersTowardNextLevel INTEGER NOT NULL, eligibleQuizzesTowardNextLevel INTEGER NOT NULL, lastOpenedAtEpochMillis INTEGER NOT NULL DEFAULT 0, lastPlayedAtEpochMillis INTEGER NOT NULL DEFAULT 0, inactiveIconActive INTEGER NOT NULL DEFAULT 0, ratingsSerialized TEXT NOT NULL DEFAULT '', achievementUnlocksSerialized TEXT NOT NULL DEFAULT '', countryPracticeSerialized TEXT NOT NULL DEFAULT '', activityCalendarSerialized TEXT NOT NULL DEFAULT '', dailyChallengeSerialized TEXT NOT NULL DEFAULT '', savedQuizTemplatesSerialized TEXT NOT NULL DEFAULT '', accountName TEXT NOT NULL DEFAULT '', avatarIndex INTEGER NOT NULL DEFAULT 0, languageName TEXT NOT NULL DEFAULT 'English', mistakeReviewUnlocked INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id))")
      db.execSQL("INSERT INTO progress_new SELECT id, CAST(hintCount AS REAL), level, hintsTowardNextLevel, correctAnswersTowardNextLevel, eligibleQuizzesTowardNextLevel, lastOpenedAtEpochMillis, lastPlayedAtEpochMillis, inactiveIconActive, ratingsSerialized, achievementUnlocksSerialized, countryPracticeSerialized, activityCalendarSerialized, dailyChallengeSerialized, savedQuizTemplatesSerialized, accountName, avatarIndex, languageName, mistakeReviewUnlocked FROM progress")
      db.execSQL("DROP TABLE progress")
      db.execSQL("ALTER TABLE progress_new RENAME TO progress")
    }
  }

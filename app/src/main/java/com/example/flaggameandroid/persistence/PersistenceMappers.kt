package com.example.flaggameandroid.persistence

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.DailyChallengeTheme
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.SavedQuizTemplate
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.feature.app.AppLanguage

internal fun ProgressEntity.toPersistedAppState(hintDifficultyName: String): PersistedAppState =
  PersistedAppState(
    hintDifficulty = hintDifficultyName.toHintDifficulty(),
    hintCount = hintCount,
    level = level,
    hintsTowardNextLevel = hintsTowardNextLevel,
    correctAnswersTowardNextLevel = correctAnswersTowardNextLevel,
    eligibleQuizzesTowardNextLevel = eligibleQuizzesTowardNextLevel,
    lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
    lastPlayedAtEpochMillis = lastPlayedAtEpochMillis,
    ratings = ratingsSerialized.toRatingsProgress(),
    achievements = achievementUnlocksSerialized.toAchievementsProgress(),
    countryPracticeStats = countryPracticeSerialized.toCountryPracticeStats(),
    activityCalendar = activityCalendarSerialized.toActivityCalendar(),
    dailyChallengeCaches = dailyChallengeSerialized.toDailyChallengeCaches(),
    savedQuizTemplates = savedQuizTemplatesSerialized.toSavedQuizTemplates(),
    accountName = accountName,
    avatarIndex = avatarIndex,
    language = AppLanguage.entries.firstOrNull { it.name == languageName } ?: AppLanguage.English,
    mistakeReviewUnlocked = mistakeReviewUnlocked,
  )

private fun String.toHintDifficulty(): HintDifficulty =
  when (this) {
    "Easy" -> HintDifficulty.Easy
    else -> enumValueOf<HintDifficulty>(this)
  }

internal fun PersistedAppState.toProgressEntity(): ProgressEntity =
  ProgressEntity(
    hintCount = hintCount,
    level = level,
    hintsTowardNextLevel = hintsTowardNextLevel,
    correctAnswersTowardNextLevel = correctAnswersTowardNextLevel,
    eligibleQuizzesTowardNextLevel = eligibleQuizzesTowardNextLevel,
    lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
    lastPlayedAtEpochMillis = lastPlayedAtEpochMillis,
    ratingsSerialized = ratings.serialize(),
    achievementUnlocksSerialized = achievements.serialize(),
    countryPracticeSerialized = countryPracticeStats.serializeCountryPracticeStats(),
    activityCalendarSerialized = activityCalendar.serializeActivityCalendar(),
    dailyChallengeSerialized = dailyChallengeCaches.serializeDailyChallengeCaches(),
    savedQuizTemplatesSerialized = savedQuizTemplates.serializeSavedQuizTemplates(),
    accountName = accountName,
    avatarIndex = avatarIndex,
    languageName = language.name,
    mistakeReviewUnlocked = mistakeReviewUnlocked,
  )

internal fun PersistedQuizHistory.toEntity(): QuizHistoryEntity =
  QuizHistoryEntity(
    mode = mode.name,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    skippedAnswers = skippedAnswers,
    netScore = netScore,
    completedAtEpochMillis = completedAtEpochMillis,
  )

private fun Map<String, CountryPracticeStats>.serializeCountryPracticeStats(): String =
  entries.joinToString(separator = ";") { (code, stats) ->
    listOf(
      code,
      stats.correctCount.toString(),
      stats.wrongCount.toString(),
      stats.capitalCorrectCount.toString(),
      stats.capitalWrongCount.toString(),
      stats.lastMissedAtEpochMillis.toString(),
      if (stats.favorite) "1" else "0",
    ).joinToString(separator = ",")
  }

private fun String.toCountryPracticeStats(): Map<String, CountryPracticeStats> =
  if (isBlank()) {
    emptyMap()
  } else {
    split(";")
      .mapNotNull { row ->
        val parts = row.split(",")
        if (parts.size < 5) return@mapNotNull null
        val code = parts[0]
        code to
          CountryPracticeStats(
            correctCount = parts[1].toIntOrNull() ?: 0,
            wrongCount = parts[2].toIntOrNull() ?: 0,
            capitalCorrectCount = parts.getOrNull(3)?.toIntOrNull() ?: 0,
            capitalWrongCount = parts.getOrNull(4)?.toIntOrNull() ?: 0,
            lastMissedAtEpochMillis = parts.getOrNull(if (parts.size >= 7) 5 else 3)?.toLongOrNull() ?: 0L,
            favorite = parts.getOrNull(if (parts.size >= 7) 6 else 4) == "1",
          )
      }
      .toMap()
  }

private fun Map<Long, ActivityDayRecord>.serializeActivityCalendar(): String =
  entries.joinToString(separator = ";") { (dayKey, record) ->
    listOf(
      dayKey.toString(),
      record.quizzesCompleted.toString(),
      if (record.dailyChallengeCompleted) "1" else "0",
      record.lastUpdatedAtEpochMillis.toString(),
      record.streakStartDayKey?.toString().orEmpty(),
      record.lastActiveDayKey?.toString().orEmpty(),
    ).joinToString(separator = ",")
  }

private fun String.toActivityCalendar(): Map<Long, ActivityDayRecord> =
  if (isBlank()) {
    emptyMap()
  } else {
    split(";")
      .mapNotNull { row ->
        val parts = row.split(",")
        if (parts.size < 4) return@mapNotNull null
        val dayKey = parts[0].toLongOrNull() ?: return@mapNotNull null
        dayKey to
          ActivityDayRecord(
            dayKey = dayKey,
            quizzesCompleted = parts[1].toIntOrNull() ?: 0,
            dailyChallengeCompleted = parts[2] == "1",
            lastUpdatedAtEpochMillis = parts[3].toLongOrNull() ?: 0L,
            streakStartDayKey = parts.getOrNull(4)?.takeIf { it.isNotBlank() }?.toLongOrNull(),
            lastActiveDayKey = parts.getOrNull(5)?.takeIf { it.isNotBlank() }?.toLongOrNull(),
          )
      }
      .toMap()
  }

private fun Map<QuizTopic, DailyChallengeCache>.serializeDailyChallengeCaches(): String =
  entries.joinToString(separator = "|") { (_, cache) ->
    listOf(
      cache.dayKey.toString(),
      cache.topic.name,
      cache.theme.name,
      cache.questionCount.toString(),
      cache.seed.toString(),
      if (cache.completed) "1" else "0",
      cache.completedAtEpochMillis.toString(),
    ).joinToString(separator = ",")
  }

private fun String.toDailyChallengeCaches(): Map<QuizTopic, DailyChallengeCache> {
  if (isBlank()) return emptyMap()
  return split("|")
    .mapNotNull { entry -> entry.toDailyChallengeCache() }
    .associateBy { it.topic }
}

private fun String.toDailyChallengeCache(): DailyChallengeCache? {
  if (isBlank()) return null
  val parts = split(",")
  if (parts.size < 4) return null
  val dayKey = parts[0].toLongOrNull() ?: return null
  val hasTopic = parts.size >= 7
  val topic =
    if (hasTopic) {
      QuizTopic.entries.firstOrNull { it.name == parts[1] } ?: QuizTopic.Countries
    } else {
      QuizTopic.Countries
    }
  val themeIndex = if (hasTopic) 2 else 1
  val questionCountIndex = if (hasTopic) 3 else 2
  val seedIndex = if (hasTopic) 4 else 3
  val completedIndex = if (hasTopic) 5 else 4
  val completedAtIndex = if (hasTopic) 6 else 5
  val theme = DailyChallengeTheme.entries.firstOrNull { it.name == parts[themeIndex] } ?: DailyChallengeTheme.World
  return DailyChallengeCache(
    dayKey = dayKey,
    topic = topic,
    theme = theme,
    questionCount = parts[questionCountIndex].toIntOrNull() ?: 10,
    seed = parts[seedIndex].toLongOrNull() ?: 0L,
    completed = parts.getOrNull(completedIndex) == "1",
    completedAtEpochMillis = parts.getOrNull(completedAtIndex)?.toLongOrNull() ?: 0L,
  )
}

private fun List<SavedQuizTemplate>.serializeSavedQuizTemplates(): String =
  joinToString(separator = "|") { template ->
    listOf(
      template.id,
      template.createdAtEpochMillis.toString(),
      template.title,
      template.topic.name,
      template.source.name,
      template.preset?.name.orEmpty(),
      template.createQuizPresets.joinToString(separator = ".") { it.name },
      template.selectedCountryCodes.joinToString(separator = "."),
      template.selectedCapitalCountryCodes.joinToString(separator = "."),
      template.questionCountryCodes.joinToString(separator = "."),
      template.variants.joinToString(separator = ".") { it.name },
      template.questionCount.toString(),
      template.seed.toString(),
      if (template.instantCorrectionEnabled) "1" else "0",
      if (template.createQuizManualTimerEnabled) "1" else "0",
      template.speedRunSecondsPerAnswer.toString(),
      if (template.createQuizLocalMultiplayerEnabled) "1" else "0",
      template.playerNames.joinToString(separator = "."),
      template.completionCount.toString(),
    ).joinToString(separator = ",")
  }

private fun String.toSavedQuizTemplates(): List<SavedQuizTemplate> =
  if (isBlank()) {
    emptyList()
  } else {
    split("|")
      .mapNotNull { row ->
        val parts = row.split(",")
        if (parts.size < 11) return@mapNotNull null
        val hasTopic = parts.size >= 19
        val topic =
          if (hasTopic) {
            QuizTopic.entries.firstOrNull { it.name == parts[3] } ?: QuizTopic.Countries
          } else {
            QuizTopic.Countries
          }
        val sourceIndex = if (hasTopic) 4 else 3
        val presetIndex = if (hasTopic) 5 else 4
        val presetsIndex = if (hasTopic) 6 else -1
        val selectedCodesIndex = if (hasTopic) 7 else 5
        val selectedCapitalCodesIndex = if (hasTopic) 8 else -1
        val questionCodesIndex = if (hasTopic) 9 else 6
        val variantsIndex = if (hasTopic) 10 else 7
        val questionCountIndex = if (hasTopic) 11 else 8
        val seedIndex = if (hasTopic) 12 else 9
        val instantCorrectionIndex = if (hasTopic) 13 else -1
        val manualTimerIndex = if (hasTopic) 14 else -1
        val secondsIndex = if (hasTopic) 15 else -1
        val multiplayerIndex = if (hasTopic) 16 else 10
        val playersIndex = if (hasTopic) 17 else 11
        val completionIndex = if (hasTopic) 18 else 12
        val source = CreateQuizSource.entries.firstOrNull { it.name == parts[sourceIndex] } ?: CreateQuizSource.PresetFilter
        val preset = parts[presetIndex].takeIf { it.isNotBlank() }?.let { name -> CreateQuizPreset.entries.firstOrNull { it.name == name } }
        val createQuizPresets =
          if (hasTopic) {
            parts[presetsIndex].takeIf { it.isNotBlank() }?.split(".")?.mapNotNull { presetName ->
              CreateQuizPreset.entries.firstOrNull { it.name == presetName }
            }?.toSet().orEmpty()
          } else {
            emptySet()
          }
        val selectedCountryCodes = parts[selectedCodesIndex].takeIf { it.isNotBlank() }?.split(".")?.toSet().orEmpty()
        val selectedCapitalCountryCodes =
          if (selectedCapitalCodesIndex >= 0) {
            parts[selectedCapitalCodesIndex].takeIf { it.isNotBlank() }?.split(".")?.toSet().orEmpty()
          } else {
            emptySet()
          }
        val modernFormat = parts.size >= (if (hasTopic) 19 else 13)
        val questionCountryCodes =
          if (parts.size >= (if (hasTopic) 13 else 12)) {
            parts[questionCodesIndex].takeIf { it.isNotBlank() }?.split(".")?.toSet().orEmpty()
          } else {
            selectedCountryCodes
          }
        val variants =
          parts[variantsIndex]
            .takeIf { it.isNotBlank() }
            ?.split(".")
            ?.mapNotNull { variantName -> com.example.flaggameandroid.core.model.QuizVariant.entries.firstOrNull { it.name == variantName } }
            ?.toSet()
            ?: com.example.flaggameandroid.core.model.QuizVariant.entries.toSet()
        val createQuizLocalMultiplayerEnabled =
          if (modernFormat) {
            parts[multiplayerIndex] == "1"
          } else {
            false
          }
        val instantCorrectionEnabled =
          if (modernFormat && instantCorrectionIndex >= 0) {
            parts[instantCorrectionIndex] == "1"
          } else {
            true
          }
        val createQuizManualTimerEnabled =
          if (modernFormat && manualTimerIndex >= 0) {
            parts[manualTimerIndex] == "1"
          } else {
            false
          }
        val speedRunSecondsPerAnswer =
          if (modernFormat && secondsIndex >= 0) {
            parts[secondsIndex].toIntOrNull() ?: 5
          } else {
            5
          }
        val playerNames =
          if (modernFormat) {
            parts[playersIndex].takeIf { it.isNotBlank() }?.split(".")?.filter { it.isNotBlank() }.orEmpty()
          } else {
            emptyList()
          }
        SavedQuizTemplate(
          id = parts[0],
          createdAtEpochMillis = parts[1].toLongOrNull() ?: 0L,
          title = parts[2],
          topic = topic,
          source = source,
          preset = preset,
          createQuizPresets = createQuizPresets,
          selectedCountryCodes = selectedCountryCodes,
          selectedCapitalCountryCodes = selectedCapitalCountryCodes,
          questionCountryCodes = questionCountryCodes,
          variants = variants,
          questionCount = parts[questionCountIndex].toIntOrNull() ?: 10,
          seed = parts[seedIndex].toLongOrNull() ?: 0L,
          instantCorrectionEnabled = instantCorrectionEnabled,
          createQuizManualTimerEnabled = createQuizManualTimerEnabled,
          speedRunSecondsPerAnswer = speedRunSecondsPerAnswer,
          createQuizLocalMultiplayerEnabled = createQuizLocalMultiplayerEnabled,
          playerNames = playerNames,
          completionCount = parts[if (modernFormat) completionIndex else if (parts.size >= (if (hasTopic) 13 else 12)) seedIndex + 1 else seedIndex].toIntOrNull() ?: 0,
        )
      }
      .toList()
  }

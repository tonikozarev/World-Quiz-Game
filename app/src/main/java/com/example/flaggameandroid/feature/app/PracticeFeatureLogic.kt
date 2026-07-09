package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.CountryTag
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.DailyChallengeTheme
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.MistakeReviewRecoveryWrongCount
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

internal data class QuizPoolResolution(
  val pool: List<FlagCountry>,
  val dailyChallengeCache: DailyChallengeCache? = null,
)

internal fun Map<QuizTopic, DailyChallengeCache>.withDailyChallengeCache(
  cache: DailyChallengeCache?,
): Map<QuizTopic, DailyChallengeCache> {
  if (cache == null) return this
  return this + (cache.topic to cache)
}

internal fun Map<QuizTopic, DailyChallengeCache>.dailyChallengeCacheFor(topic: QuizTopic): DailyChallengeCache? =
  get(topic)

internal fun resolveQuizPool(
  setup: SetupState,
  countries: List<FlagCountry>,
  practiceStats: Map<String, CountryPracticeStats>,
  dailyChallengeCache: DailyChallengeCache?,
  nowEpochMillis: Long,
): QuizPoolResolution {
  val generatedDailyChallengeCache =
    if (setup.mode == GameMode.DailyChallenge) {
      buildDailyChallengeCache(
        countries = countries,
        topic = setup.topic,
        dailyChallengeCache = dailyChallengeCache,
        nowEpochMillis = nowEpochMillis,
      )
    } else {
      dailyChallengeCache
    }
  val pool =
    when (setup.mode) {
      GameMode.DailyChallenge ->
        if (generatedDailyChallengeCache?.completed == true) {
          emptyList()
        } else {
          dailyChallengePool(countries, generatedDailyChallengeCache, nowEpochMillis)
        }
      GameMode.MistakeReview -> countries.filter { country -> practiceStats[country.code]?.isMistakeReviewEligible(setup.topic) == true }
      GameMode.CreateQuiz -> createQuizPool(setup, countries)
      GameMode.WorldFlags,
      GameMode.LocalMultiplayer,
      GameMode.Training -> countryPoolFor(setup, countries)
    }

  val updatedCache =
    if (setup.mode == GameMode.DailyChallenge) {
      generatedDailyChallengeCache
    } else {
      dailyChallengeCache
    }

  return QuizPoolResolution(
    pool = pool,
    dailyChallengeCache = updatedCache,
  )
}

internal fun dailyChallengePool(
  countries: List<FlagCountry>,
  dailyChallengeCache: DailyChallengeCache?,
  nowEpochMillis: Long,
): List<FlagCountry> {
  val dayKey = localDayKey(nowEpochMillis)
  val theme =
    dailyChallengeCache?.takeIf { it.dayKey == dayKey }?.theme
      ?: determineDailyChallengeTheme(dayKey, countries)
  val themedPool = countriesForTheme(theme, countries)
  return if (themedPool.size >= 4) themedPool else countries
}

internal fun buildDailyChallengeCache(
  countries: List<FlagCountry>,
  topic: QuizTopic,
  dailyChallengeCache: DailyChallengeCache?,
  nowEpochMillis: Long,
): DailyChallengeCache {
  val dayKey = localDayKey(nowEpochMillis)
  val theme =
    dailyChallengeCache?.takeIf { it.dayKey == dayKey && it.topic == topic }?.theme
      ?: determineDailyChallengeTheme(dayKey, countries, topic)
  val questionCount = minOf(10, countriesForTheme(theme, countries).size.coerceAtLeast(4))
  val currentInstanceKey = listOf(dayKey, topic.name, theme.name, questionCount, dayKey).joinToString(separator = ":")
  return if (dailyChallengeCache?.instanceKey == currentInstanceKey) {
    dailyChallengeCache.copy(
      dayKey = dayKey,
      topic = topic,
      theme = theme,
      questionCount = questionCount,
      seed = dayKey,
    )
  } else {
    DailyChallengeCache(
      dayKey = dayKey,
      topic = topic,
      theme = theme,
      questionCount = questionCount,
      seed = dayKey,
      completed = false,
      completedAtEpochMillis = 0L,
    )
  }
}

internal fun determineDailyChallengeTheme(
  dayKey: Long,
  countries: List<FlagCountry>,
  topic: QuizTopic = QuizTopic.Countries,
): DailyChallengeTheme {
  val validThemes =
    DailyChallengeTheme.entries
      .filterNot { theme -> topic == QuizTopic.Countries && theme == DailyChallengeTheme.Capitals }
      .filter { theme -> countriesForTheme(theme, countries).size >= 4 }
  if (validThemes.isEmpty()) return DailyChallengeTheme.World
  return validThemes[(dayKey.mod(validThemes.size.toLong())).toInt()]
}

internal fun countriesForTheme(
  theme: DailyChallengeTheme,
  countries: List<FlagCountry>,
): List<FlagCountry> =
  when (theme) {
    DailyChallengeTheme.World -> countries
    DailyChallengeTheme.Africa -> countries.filter { it.continent == "Africa" }
    DailyChallengeTheme.Asia -> countries.filter { it.continent == "Asia" }
    DailyChallengeTheme.Europe -> countries.filter { it.continent == "Europe" }
    DailyChallengeTheme.NorthAmerica -> countries.filter { it.continent == "North America" }
    DailyChallengeTheme.Oceania -> countries.filter { it.continent == "Oceania" }
    DailyChallengeTheme.SouthAmerica -> countries.filter { it.continent == "South America" }
    DailyChallengeTheme.FlagsWithStripes -> countries.filter { it.tags.contains(CountryTag.StripedFlag) }
    DailyChallengeTheme.Capitals -> countries.filter { !it.capital.isNullOrBlank() }
  }

internal fun localDayKey(
  epochMillis: Long,
): Long = Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()

internal fun updateCountryPracticeStats(
  previous: Map<String, CountryPracticeStats>,
  results: List<QuestionResult>,
  completedAtEpochMillis: Long,
  mode: GameMode,
): Map<String, CountryPracticeStats> =
  if (mode == GameMode.Training) {
    previous
  } else {
    val updated =
      results.fold(previous) { statsByCode, result ->
        val code = result.question.correctCountry.code
        val current = statsByCode[code] ?: CountryPracticeStats()
        val next =
          if (result.isCorrect) {
            when (result.question.topic) {
              QuizTopic.Countries ->
                current.copy(correctCount = current.correctCount + 1)
              QuizTopic.Capitals ->
                current.copy(capitalCorrectCount = current.capitalCorrectCount + 1)
              QuizTopic.Mixed ->
                current.copy(correctCount = current.correctCount + 1)
            }
          } else {
            when (result.question.topic) {
              QuizTopic.Countries ->
                current.copy(
                  wrongCount = current.wrongCount + 1,
                  lastMissedAtEpochMillis = completedAtEpochMillis,
                )
              QuizTopic.Capitals ->
                current.copy(
                  capitalWrongCount = current.capitalWrongCount + 1,
                  lastMissedAtEpochMillis = completedAtEpochMillis,
                )
              QuizTopic.Mixed ->
                current.copy(
                  wrongCount = current.wrongCount + 1,
                  lastMissedAtEpochMillis = completedAtEpochMillis,
                )
            }
          }
        statsByCode + (code to next)
      }

    if (mode != GameMode.MistakeReview) {
      updated
    } else {
      results.fold(updated) { statsByCode, result ->
        val code = result.question.correctCountry.code
        val stats = statsByCode[code] ?: CountryPracticeStats()
        val recovered =
          when (result.question.topic) {
            QuizTopic.Countries ->
              stats.copy(wrongCount = minOf(stats.wrongCount, MistakeReviewRecoveryWrongCount))
            QuizTopic.Capitals ->
              stats.copy(capitalWrongCount = minOf(stats.capitalWrongCount, MistakeReviewRecoveryWrongCount))
            QuizTopic.Mixed ->
              stats.copy(
                wrongCount = minOf(stats.wrongCount, MistakeReviewRecoveryWrongCount),
                capitalWrongCount = minOf(stats.capitalWrongCount, MistakeReviewRecoveryWrongCount),
              )
          }
        statsByCode + (code to recovered)
      }
    }
  }

internal fun mistakeReviewEligibleCountryCount(
  practiceStats: Map<String, CountryPracticeStats>,
  topic: QuizTopic = QuizTopic.Countries,
): Int = practiceStats.values.count { it.isMistakeReviewEligible(topic) }

internal fun mistakeReviewEligibleCountries(
  countries: List<FlagCountry>,
  practiceStats: Map<String, CountryPracticeStats>,
  topic: QuizTopic = QuizTopic.Countries,
): List<FlagCountry> =
  countries.filter { country -> practiceStats[country.code]?.isMistakeReviewEligible(topic) == true }

internal fun updateActivityCalendar(
  previous: Map<Long, ActivityDayRecord>,
  completedAtEpochMillis: Long,
  mode: GameMode,
): Map<Long, ActivityDayRecord> {
  val dayKey = localDayKey(completedAtEpochMillis)
  val current = previous[dayKey] ?: ActivityDayRecord(dayKey = dayKey)
  val previousDayKey = dayKey - 1
  val previousDay = previous[previousDayKey]
  val streakStartDayKey =
    if (previousDay != null) {
      previousDay.streakStartDayKey ?: previousDayKey
    } else {
      dayKey
    }
  return previous + (
    dayKey to
      current.copy(
        quizzesCompleted = current.quizzesCompleted + 1,
        dailyChallengeCompleted = current.dailyChallengeCompleted || mode == GameMode.DailyChallenge,
        lastUpdatedAtEpochMillis = completedAtEpochMillis,
        streakStartDayKey = streakStartDayKey,
        lastActiveDayKey = dayKey,
      )
  )
}

internal fun recentActivityDays(
  activityCalendar: Map<Long, ActivityDayRecord>,
  days: Int = 30,
  nowEpochMillis: Long = System.currentTimeMillis(),
): List<ActivityDayRecord> {
  val today = localDayKey(nowEpochMillis)
  return (0 until days)
    .map { offset ->
      val dayKey = today - offset
      activityCalendar[dayKey] ?: ActivityDayRecord(dayKey = dayKey)
    }
    .reversed()
}

internal fun weekActivityDays(
  activityCalendar: Map<Long, ActivityDayRecord>,
  nowEpochMillis: Long = System.currentTimeMillis(),
): List<ActivityDayRecord> {
  val today = LocalDate.ofEpochDay(localDayKey(nowEpochMillis))
  val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
  return (0..6).map { offset ->
    val date = monday.plusDays(offset.toLong())
    val dayKey = date.toEpochDay()
    activityCalendar[dayKey] ?: ActivityDayRecord(dayKey = dayKey)
  }
}

internal fun streakLength(
  activityCalendar: Map<Long, ActivityDayRecord>,
  nowEpochMillis: Long = System.currentTimeMillis(),
): Int {
  val today = localDayKey(nowEpochMillis)
  val todayRecord = activityCalendar[today] ?: return 0
  val startDayKey = todayRecord.streakStartDayKey ?: today
  return (today - startDayKey + 1).coerceAtLeast(1).toInt()
}

private fun createQuizPool(
  setup: SetupState,
  countries: List<FlagCountry>,
): List<FlagCountry> =
  if (setup.usesCreateQuizTraining) {
    countries
  } else {
    when (setup.createQuizSource) {
      CreateQuizSource.PresetFilter -> {
        val selectedPresets = setup.createQuizPresets.ifEmpty { setOf(setup.createQuizPreset) }
        countries.filter { country -> selectedPresets.any { preset -> matchesCreateQuizPreset(country, preset, setup.topic) } }
      }
      CreateQuizSource.ManualCountriesCapitals ->
        if (setup.usesCreateQuizManualHardcore) {
          countries
        } else {
          countries.filter { country -> country.code in setup.selectedCountryCodes }
        }
    }
  }


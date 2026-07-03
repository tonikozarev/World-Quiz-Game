package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.random.Random

class FlagGameQuizBootstrapLogicTest {
  @Test
  fun buildStartedQuizState_usesSetupAndInitialHintCount() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup = buildSetupForMode(GameMode.Training, listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"), countries, "Tony")
    val quiz =
      buildStartedQuizState(
        setup = setup.copy(questionCountInput = "12", variants = setOf(QuizVariant.FlagToCountry)),
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(21)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(22),
        hintCount = 7.0,
        displayName = "Tony",
      )

    assertEquals(GameMode.Training, quiz.mode)
    assertEquals(12, quiz.totalQuestions)
    assertEquals(7.0, quiz.currentPlayer.hintPoints)
    assertEquals(12, quiz.questionStates.size)
    assertTrue(quiz.questions.all { it.variant == QuizVariant.FlagToCountry })
  }

  @Test
  fun buildStartedQuizState_usesEligibleMistakeReviewCountryCount() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.MistakeReview,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(variants = setOf(QuizVariant.FlagToCountry))
    val practiceStats =
      countries.take(12).associate { country ->
        country.code to CountryPracticeStats(wrongCount = 10)
      }

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(25)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(26),
        hintCount = 3.0,
        displayName = "Tony",
        practiceStats = practiceStats,
      )

    assertEquals(12, quiz.totalQuestions)
    assertEquals(12, quiz.questions.size)
  }

  @Test
  fun buildStartedQuizState_mistakeReviewQuestionsAreUnique() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.MistakeReview,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(variants = setOf(QuizVariant.FlagToCountry))
    val practiceStats =
      countries.take(12).associate { country ->
        country.code to CountryPracticeStats(wrongCount = 10)
      }

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(27)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(28),
        hintCount = 3.0,
        displayName = "Tony",
        practiceStats = practiceStats,
      )

    val codes = quiz.questions.map { it.correctCountry.code }
    assertEquals(codes.size, codes.distinct().size)
  }

  @Test
  fun buildQuizStartResult_returnsValidationErrorForInvalidSetup() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(selectedContinents = emptySet())

    val result =
      buildQuizStartResult(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(21)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(22),
        hintCount = 7.0,
        displayName = "Tony",
      )

    assertEquals("Choose at least one continent.", result.validationError)
    assertNull(result.quiz)
  }

  @Test
  fun dailyChallenge_forcesMixedTopicAtSetupTime() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.DailyChallenge,
        QuizTopic.Countries,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      )

    assertEquals(QuizTopic.Mixed, setup.topic)
  }

  @Test
  fun dailyChallenge_isDeterministicForTheSameLocalDaySeed() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup = buildSetupForMode(GameMode.DailyChallenge, listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"), countries, "Tony")
    val now = 1_717_900_000_000L

    val first =
      buildQuizStartResult(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(1)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(2),
        hintCount = 0.0,
        displayName = "Tony",
        nowEpochMillis = now,
      ).quiz!!
    val second =
      buildQuizStartResult(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(9)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(10),
        hintCount = 0.0,
        displayName = "Tony",
        nowEpochMillis = now,
      ).quiz!!

    assertEquals(
      first.questions.map { it.correctCountry.code to it.variant },
      second.questions.map { it.correctCountry.code to it.variant },
    )
    assertEquals(QuizTopic.Mixed, first.topic)
  }

  @Test
  fun dailyChallenge_completedInstance_staysLockedAfterTimeZoneChange() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup = buildSetupForMode(GameMode.DailyChallenge, listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"), countries, "Tony")
    val now = Instant.parse("2026-06-21T12:00:00Z").toEpochMilli()

    val generatedCache =
      buildQuizStartResult(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(1)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(2),
        hintCount = 0.0,
        displayName = "Tony",
        nowEpochMillis = now,
        timeZone = AppTimeZone.UtcPlus3,
      ).dailyChallengeCache!!

    val completedCache =
      generatedCache.copy(
        completed = true,
        completedAtEpochMillis = now,
      )

    val resultAfterTimeZoneChange =
      buildQuizStartResult(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(3)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(4),
        hintCount = 0.0,
        displayName = "Tony",
        dailyChallengeCache = completedCache,
        nowEpochMillis = now,
        timeZone = AppTimeZone.Utc,
      )

    assertEquals("Daily challenge already completed for today.", resultAfterTimeZoneChange.validationError)
  }

  @Test
  fun mistakeReview_requiresTenCountriesBeforeUnlocking() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.MistakeReview,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      )
    val lockedStats =
      countries.take(9).associate { country ->
        country.code to CountryPracticeStats(wrongCount = 10)
      }

    val lockedResult =
      buildQuizStartResult(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 0.0,
        displayName = "Tony",
        practiceStats = lockedStats,
      )

    assertEquals("No missed countries to review yet.", lockedResult.validationError)
    assertNull(lockedResult.quiz)
  }

  @Test
  fun mistakeReview_requiresTenEligibleCountriesEvenWithPersistedUnlockBit() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.MistakeReview,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      )
    val unlockedStats =
      countries.take(5).associate { country ->
        country.code to CountryPracticeStats(wrongCount = 10)
      }

    val unlockedResult =
      buildQuizStartResult(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(33)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(34),
        hintCount = 0.0,
        displayName = "Tony",
        practiceStats = unlockedStats,
        mistakeReviewUnlocked = true,
      )

    assertEquals("No missed countries to review yet.", unlockedResult.validationError)
    assertNull(unlockedResult.quiz)
  }

  @Test
  fun buildStartedQuizState_speedRunStartsWithTimerAndContinentPool() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "10",
        variants = setOf(QuizVariant.FlagToCountry),
      )

    assertEquals("5", setup.speedRunSecondsPerAnswerInput)
    assertEquals(5, setup.speedRunSecondsPerAnswer)

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(23)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(24),
        hintCount = 4.0,
        displayName = "Tony",
      )

    assertEquals(GameMode.WorldFlags, quiz.mode)
    assertTrue(quiz.startedAtEpochMillis > 0L)
    assertEquals(10, quiz.totalQuestions)
    assertEquals(4.0, quiz.currentPlayer.hintPoints)
  }

  @Test
  fun buildStartedQuizState_worldFlagsHardcoreUsesAllCountriesAndNoTimerByDefault() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        worldFlagsHardcoreEnabled = true,
        worldFlagsTimerEnabled = false,
        variants = setOf(QuizVariant.FlagToCountry, QuizVariant.CountryToFlag),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(41)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(42),
        hintCount = 2.0,
        displayName = "Tony",
      )

    assertEquals(GameMode.WorldFlags, quiz.mode)
    assertEquals(countries.size, quiz.totalQuestions)
    assertEquals(0, quiz.speedRunSecondsPerAnswer)
    assertFalse(quiz.countdownEnabled)
  }

  @Test
  fun buildStartedQuizState_worldFlagsTimerEnablesCountdownMode() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        selectedContinents = setOf("Europe"),
        worldFlagsTimerEnabled = true,
        speedRunSecondsPerAnswerInput = "7",
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(43)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(44),
        hintCount = 2.0,
        displayName = "Tony",
      )

    assertEquals(GameMode.WorldFlags, quiz.mode)
    assertTrue(quiz.countdownEnabled)
    assertEquals(7, quiz.speedRunSecondsPerAnswer)
    assertTrue(quiz.totalQuestions > 0)
  }
}

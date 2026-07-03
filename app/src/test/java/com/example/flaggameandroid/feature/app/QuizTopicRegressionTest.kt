package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.MistakeReviewRecoveryWrongCount
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizTopicRegressionTest {
  private val germany =
    FlagCountry(
      code = "DE",
      name = "Germany",
      emoji = "🇩🇪",
      continent = "Europe",
      capital = "Berlin",
    )

  @Test
  fun capitalTypedAnswers_useCapitalInsteadOfCountryName() {
    val acceptedAnswers = germany.acceptedTypedAnswers(AppLanguage.English, QuizTopic.Capitals)

    assertTrue("Berlin" in acceptedAnswers)
    assertFalse("Germany" in acceptedAnswers)
  }

  @Test
  fun dailyChallengeCache_isSeparatedByTopic() {
    val countries = listOf(germany, germany.copy(code = "FR", name = "France", emoji = "🇫🇷", capital = "Paris"))

    val countryCache =
      buildDailyChallengeCache(
        countries = countries,
        topic = QuizTopic.Countries,
        dailyChallengeCache = null,
        nowEpochMillis = 0L,
      )
    val capitalCache =
      buildDailyChallengeCache(
        countries = countries,
        topic = QuizTopic.Capitals,
        dailyChallengeCache = null,
        nowEpochMillis = 0L,
      )

    assertEquals(QuizTopic.Countries, countryCache.topic)
    assertEquals(QuizTopic.Capitals, capitalCache.topic)
    assertFalse(countryCache.instanceKey == capitalCache.instanceKey)
  }

  @Test
  fun mistakeReviewEligibility_usesCapitalWrongCountForCapitalTopic() {
    val stats =
      mapOf(
        "DE" to CountryPracticeStats(wrongCount = 0, capitalWrongCount = 10),
      )

    assertEquals(0, mistakeReviewEligibleCountryCount(stats, QuizTopic.Countries))
    assertEquals(1, mistakeReviewEligibleCountryCount(stats, QuizTopic.Capitals))
    assertEquals(1, mistakeReviewEligibleCountryCount(stats, QuizTopic.Mixed))
  }

  @Test
  fun updatingPracticeStats_routesCapitalMistakesToCapitalCounter() {
    val result =
      QuestionResult(
        question =
          com.example.flaggameandroid.core.model.FlagQuestion(
            correctCountry = germany,
            options = listOf(germany),
            variant = QuizVariant.FlagToText,
            topic = QuizTopic.Capitals,
          ),
        playerName = "Solo",
        selectedCountry = null,
        typedAnswer = "Munich",
        isCorrect = false,
        hintUsed = false,
      )

    val updated =
      updateCountryPracticeStats(
        previous = emptyMap(),
        results = listOf(result),
        completedAtEpochMillis = 1000L,
        mode = GameMode.CreateQuiz,
      )

    assertEquals(0, updated.getValue("DE").wrongCount)
    assertEquals(1, updated.getValue("DE").capitalWrongCount)
  }

  @Test
  fun mistakeReview_recovery_keepsCountryAndCapitalCountersIndependent() {
    val germanyWithBoth =
      CountryPracticeStats(
        wrongCount = 12,
        capitalWrongCount = 13,
      )
    val countryResult =
      QuestionResult(
        question =
          com.example.flaggameandroid.core.model.FlagQuestion(
            correctCountry = germany,
            options = listOf(germany),
            variant = QuizVariant.FlagToText,
            topic = QuizTopic.Countries,
          ),
        playerName = "Solo",
        selectedCountry = germany,
        typedAnswer = "",
        isCorrect = true,
        hintUsed = false,
      )
    val capitalResult =
      QuestionResult(
        question =
          com.example.flaggameandroid.core.model.FlagQuestion(
            correctCountry = germany,
            options = listOf(germany),
            variant = QuizVariant.TypeText,
            topic = QuizTopic.Capitals,
          ),
        playerName = "Solo",
        selectedCountry = null,
        typedAnswer = "Berlin",
        isCorrect = true,
        hintUsed = false,
      )

    val updated =
      updateCountryPracticeStats(
        previous = mapOf("DE" to germanyWithBoth),
        results = listOf(countryResult, capitalResult),
        completedAtEpochMillis = 1000L,
        mode = GameMode.MistakeReview,
      )

    assertEquals(MistakeReviewRecoveryWrongCount, updated.getValue("DE").wrongCount)
    assertEquals(MistakeReviewRecoveryWrongCount, updated.getValue("DE").capitalWrongCount)
  }

  @Test
  fun mixedMistakeReviewCountsCountriesAndCapitalsSeparatelyForEligibility() {
    val stats =
      mapOf(
        "DE" to CountryPracticeStats(wrongCount = 10, capitalWrongCount = 0),
        "FR" to CountryPracticeStats(wrongCount = 0, capitalWrongCount = 10),
      )

    assertEquals(2, mistakeReviewEligibleCountryCount(stats, QuizTopic.Mixed))
    assertEquals(1, mistakeReviewEligibleCountryCount(stats, QuizTopic.Countries))
    assertEquals(1, mistakeReviewEligibleCountryCount(stats, QuizTopic.Capitals))
  }
}

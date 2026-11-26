package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteTopicsRegressionTest {
  private val germany = FlagCountry("DE", "Germany", "🇩🇪", "Europe", capital = "Berlin")
  private val japan = FlagCountry("JP", "Japan", "🇯🇵", "Asia", capital = "Tokyo")
  private val brazil = FlagCountry("BR", "Brazil", "🇧🇷", "South America", capital = "Brasília")
  private val countries = listOf(germany, japan, brazil)
  private val stats =
    mapOf(
      "DE" to CountryPracticeStats(favoriteCountry = true),
      "JP" to CountryPracticeStats(favoriteCapital = true),
      "BR" to CountryPracticeStats(favoriteCountry = true, favoriteCapital = true),
    )

  @Test
  fun favoriteLists_filterAndGroupCountriesAndCapitalsIndependently() {
    val countryGroups = favoriteItemsByContinent(countries, stats, QuizTopic.Countries)
    val capitalGroups = favoriteItemsByContinent(countries, stats, QuizTopic.Capitals)

    assertEquals(listOf("DE"), countryGroups.getValue("Europe").map { it.code })
    assertEquals(listOf("BR"), countryGroups.getValue("South America").map { it.code })
    assertFalse("Asia" in countryGroups)

    assertEquals(listOf("JP"), capitalGroups.getValue("Asia").map { it.code })
    assertEquals(listOf("BR"), capitalGroups.getValue("South America").map { it.code })
    assertFalse("Europe" in capitalGroups)
  }

  @Test
  fun answerReviewStar_usesTheQuestionTopic() {
    val countryResult = resultFor(germany, QuizTopic.Countries)
    val capitalResult = resultFor(germany, QuizTopic.Capitals)
    val capitalOnlyStats = mapOf("DE" to CountryPracticeStats(favoriteCapital = true))

    assertFalse(isFavoriteResult(countryResult, capitalOnlyStats))
    assertTrue(isFavoriteResult(capitalResult, capitalOnlyStats))
  }

  @Test
  fun tabTitles_areLocalizedInEverySupportedLanguage() {
    assertEquals("Countries", localizedFavoriteTopicTitle(QuizTopic.Countries, AppLanguage.English))
    assertEquals("Държави", localizedFavoriteTopicTitle(QuizTopic.Countries, AppLanguage.Bulgarian))
    assertEquals("Länder", localizedFavoriteTopicTitle(QuizTopic.Countries, AppLanguage.German))
    assertEquals("Capitals", localizedFavoriteTopicTitle(QuizTopic.Capitals, AppLanguage.English))
    assertEquals("Столици", localizedFavoriteTopicTitle(QuizTopic.Capitals, AppLanguage.Bulgarian))
    assertEquals("Hauptstädte", localizedFavoriteTopicTitle(QuizTopic.Capitals, AppLanguage.German))
  }

  @Test
  fun emptyAndRemovalMessages_areTopicSpecificInEverySupportedLanguage() {
    AppLanguage.entries.forEach { language ->
      val countryEmpty = localizedEmptyFavoritesText(QuizTopic.Countries, language)
      val capitalEmpty = localizedEmptyFavoritesText(QuizTopic.Capitals, language)
      val countryRemoval = localizedRemoveFavoriteMessage(QuizTopic.Countries, language)
      val capitalRemoval = localizedRemoveFavoriteMessage(QuizTopic.Capitals, language)

      assertFalse(countryEmpty == capitalEmpty)
      assertFalse(countryRemoval == capitalRemoval)
      assertTrue(countryEmpty.isNotBlank())
      assertTrue(capitalEmpty.isNotBlank())
      assertTrue(countryRemoval.isNotBlank())
      assertTrue(capitalRemoval.isNotBlank())
    }
  }

  private fun resultFor(
    country: FlagCountry,
    topic: QuizTopic,
  ): QuestionResult =
    QuestionResult(
      question =
        FlagQuestion(
          correctCountry = country,
          options = listOf(country),
          variant = QuizVariant.FlagToText,
          topic = topic,
        ),
      playerName = "Solo",
      selectedCountry = country,
      typedAnswer = "",
      isCorrect = true,
      hintUsed = false,
    )
}

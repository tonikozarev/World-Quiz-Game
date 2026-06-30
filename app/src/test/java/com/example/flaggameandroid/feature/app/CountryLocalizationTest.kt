package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.model.FlagCountry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryLocalizationTest {
  private val catalog = StaticFlagCatalogRepository().getCountries()
  private val germany =
    FlagCountry(
      code = "DE",
      name = "Germany",
      emoji = "🇩🇪",
      continent = "Europe",
    )

  @Test
  fun catalog_populatesKnownCapitalCitiesForCountries() {
    val capitalsByCode = catalog.associate { it.code to it.capital }

    assertEquals("Berlin", capitalsByCode.getValue("DE"))
    assertEquals("Vienna", capitalsByCode.getValue("AT"))
    assertEquals("Washington D.C.", capitalsByCode.getValue("US"))
    assertEquals("Tokyo", capitalsByCode.getValue("JP"))
  }

  @Test
  fun typedAnswer_acceptsOnlyBulgarianNameWhenBulgarianIsSelected() {
    val acceptedAnswers = germany.acceptedTypedAnswers(AppLanguage.Bulgarian)

    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("Германия", acceptedAnswers))
    assertFalse(QuizAnswerChecker.isTypedAnswerCorrect("Germany", acceptedAnswers))
    assertFalse(QuizAnswerChecker.isTypedAnswerCorrect("Deutschland", acceptedAnswers))
  }

  @Test
  fun typedAnswer_acceptsOnlyGermanNameWhenGermanIsSelected() {
    val acceptedAnswers = germany.acceptedTypedAnswers(AppLanguage.German)

    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("Deutschland", acceptedAnswers))
    assertFalse(QuizAnswerChecker.isTypedAnswerCorrect("Germany", acceptedAnswers))
    assertFalse(QuizAnswerChecker.isTypedAnswerCorrect("Германия", acceptedAnswers))
  }

  @Test
  fun typedAnswer_acceptsOnlyEnglishNameWhenEnglishIsSelected() {
    val acceptedAnswers = germany.acceptedTypedAnswers(AppLanguage.English)

    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("Germany", acceptedAnswers))
    assertFalse(QuizAnswerChecker.isTypedAnswerCorrect("Deutschland", acceptedAnswers))
    assertFalse(QuizAnswerChecker.isTypedAnswerCorrect("Германия", acceptedAnswers))
  }
}

package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
      capital = "Berlin",
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

  @Test
  fun capitalLabels_areLocalizedInBulgarianAndGerman() {
    val austria = catalog.first { it.code == "AT" }

    assertEquals("Виена", austria.localizedCapital(AppLanguage.Bulgarian))
    assertEquals("Wien", austria.localizedCapital(AppLanguage.German))
    assertTrue(
      QuizAnswerChecker.isTypedAnswerCorrect(
        "Виена",
        austria.acceptedTypedAnswers(AppLanguage.Bulgarian, QuizTopic.Capitals),
      ),
    )
    assertTrue(
      QuizAnswerChecker.isTypedAnswerCorrect(
        "Wien",
        austria.acceptedTypedAnswers(AppLanguage.German, QuizTopic.Capitals),
      ),
    )
  }

  @Test
  fun formatHintPoints_keepsTwoDecimalPlacesForQuarterValues() {
    assertEquals("15.25", formatHintPoints(15.25))
    assertEquals("0.75", formatHintPoints(0.75))
  }

  @Test
  fun resultPointValue_usesRequestedHintAndRevealScoring() {
    val question =
      FlagQuestion(
        correctCountry = germany,
        options = listOf(germany),
        variant = QuizVariant.TextToFlag,
        topic = QuizTopic.Countries,
      )

    assertEquals(
      1.0,
      resultPointValue(
        QuestionResult(
          question = question,
          playerName = "Solo",
          selectedCountry = germany,
          typedAnswer = "",
          isCorrect = true,
          hintUsed = false,
          hintUses = 0,
          revealed = false,
        ),
      ),
      0.0,
    )
    assertEquals(
      0.75,
      resultPointValue(
        QuestionResult(
          question = question,
          playerName = "Solo",
          selectedCountry = germany,
          typedAnswer = "",
          isCorrect = true,
          hintUsed = true,
          hintUses = 1,
          revealed = false,
        ),
      ),
      0.0,
    )
    assertEquals(
      0.25,
      resultPointValue(
        QuestionResult(
          question = question,
          playerName = "Solo",
          selectedCountry = germany,
          typedAnswer = "",
          isCorrect = true,
          hintUsed = true,
          hintUses = 2,
          revealed = false,
        ),
      ),
      0.0,
    )
    assertEquals(
      0.0,
      resultPointValue(
        QuestionResult(
          question = question,
          playerName = "Solo",
          selectedCountry = germany,
          typedAnswer = "",
          isCorrect = true,
          hintUsed = true,
          hintUses = 3,
          revealed = true,
        ),
      ),
      0.0,
    )
    assertEquals(
      0.0,
      resultPointValue(
        QuestionResult(
          question = question,
          playerName = "Solo",
          selectedCountry = germany,
          typedAnswer = "",
          isCorrect = false,
          hintUsed = false,
          hintUses = 0,
          revealed = false,
        ),
      ),
      0.0,
    )
  }

  @Test
  fun hintedAnswerLabels_startWithFlagsAndAddLocalizedPrefixesAfterHint() {
    val countriesQuestion =
      FlagQuestion(
        correctCountry = germany,
        options = listOf(germany),
        variant = QuizVariant.TextToFlag,
        topic = QuizTopic.Countries,
      )
    val capitalsQuestion = countriesQuestion.copy(topic = QuizTopic.Capitals)

    assertEquals("🇩🇪", answerOptionLabel(countriesQuestion, germany, AppLanguage.English))
    assertEquals("🇩🇪 (Capital: Berlin)", answerOptionLabel(countriesQuestion, germany, AppLanguage.English, hintUses = 1))
    assertEquals("🇩🇪 (Столица: Берлин)", answerOptionLabel(countriesQuestion, germany, AppLanguage.Bulgarian, hintUses = 1))
    assertEquals("🇩🇪 (Hauptstadt: Berlin)", answerOptionLabel(countriesQuestion, germany, AppLanguage.German, hintUses = 1))
    assertEquals("🇩🇪 (Country: Germany)", answerOptionLabel(capitalsQuestion, germany, AppLanguage.English, hintUses = 1))
    assertEquals("🇩🇪 (Държава: Германия)", answerOptionLabel(capitalsQuestion, germany, AppLanguage.Bulgarian, hintUses = 1))
    assertEquals("🇩🇪 (Land: Deutschland)", answerOptionLabel(capitalsQuestion, germany, AppLanguage.German, hintUses = 1))
  }
}

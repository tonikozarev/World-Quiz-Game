package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class QuizAnswerCheckerTest {
  private val unitedStates =
    FlagCountry(
      code = "US",
      name = "United States",
      emoji = "🇺🇸",
      continent = "North America",
      aliases = listOf("USA", "United States of America", "America"),
    )

  @Test
  fun typedAnswer_acceptsAliasesIgnoringCaseAndWhitespace() {
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("  usa  ", unitedStates))
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("UNITED STATES OF AMERICA", unitedStates))
  }

  @Test
  fun typedAnswer_ignoresBasicPunctuation() {
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("United-States", unitedStates))
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("U.S.A.", unitedStates))
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("unitedstates", unitedStates))
  }

  @Test
  fun typedAnswer_acceptsMultiWordCountryWithDifferentSeparators() {
    val elSalvador =
      FlagCountry(
        code = "SV",
        name = "El Salvador",
        emoji = "flag",
        continent = "North America",
        aliases = listOf("Republic of El Salvador"),
      )

    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("el salvador", elSalvador))
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("El-Salvador", elSalvador))
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("elsalvador", elSalvador))
  }

  @Test
  fun typedAnswer_handlesAccentsAndAmpersands() {
    val saoTome =
      FlagCountry(
        code = "ST",
        name = "São Tomé and Príncipe",
        emoji = "flag",
        continent = "Africa",
      )

    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("Sao Tome and Principe", saoTome))
    assertTrue(QuizAnswerChecker.isTypedAnswerCorrect("sao-tome-principe", saoTome))
  }

  @Test
  fun typedAnswer_rejectsWrongCountry() {
    assertFalse(QuizAnswerChecker.isTypedAnswerCorrect("Canada", unitedStates))
  }

  @Test
  fun countrySelection_requiresMatchingCountryCode() {
    val canada = FlagCountry(code = "CA", name = "Canada", emoji = "🇨🇦", continent = "North America")

    assertTrue(QuizAnswerChecker.isCountrySelectionCorrect(unitedStates, unitedStates))
    assertFalse(QuizAnswerChecker.isCountrySelectionCorrect(canada, unitedStates))
    assertFalse(QuizAnswerChecker.isCountrySelectionCorrect(null, unitedStates))
  }
}

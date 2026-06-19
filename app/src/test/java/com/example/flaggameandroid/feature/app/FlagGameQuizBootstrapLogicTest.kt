package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
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
        hintCount = 7,
        displayName = "Tony",
      )

    assertEquals(GameMode.Training, quiz.mode)
    assertEquals(12, quiz.totalQuestions)
    assertEquals(7, quiz.currentPlayer.hintPoints)
    assertEquals(12, quiz.questionStates.size)
    assertTrue(quiz.questions.all { it.variant == QuizVariant.FlagToCountry })
  }

  @Test
  fun buildQuizStartResult_returnsValidationErrorForInvalidSetup() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.Continents,
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
        hintCount = 7,
        displayName = "Tony",
      )

    assertEquals("Choose at least one continent.", result.validationError)
    assertNull(result.quiz)
  }

  @Test
  fun buildStartedQuizState_speedRunStartsWithTimerAndContinentPool() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.SpeedRun,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "10",
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(23)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(24),
        hintCount = 4,
        displayName = "Tony",
      )

    assertEquals(GameMode.SpeedRun, quiz.mode)
    assertTrue(quiz.startedAtEpochMillis > 0L)
    assertEquals(10, quiz.totalQuestions)
    assertEquals(4, quiz.currentPlayer.hintPoints)
  }
}

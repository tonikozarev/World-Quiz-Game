package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class FlagGameFlowLogicTest {
  @Test
  fun buildQuestionAdvanceOutcome_advancesToNextQuestionAndKeepsResultStatePure() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.Training,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "2",
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 0,
        displayName = "Tony",
      )

    val answeredQuiz = quiz.withSelectedCountry(quiz.currentQuestion!!.correctCountry)
    val state = FlagGameUiState(quiz = answeredQuiz)

    val outcome = buildQuestionAdvanceOutcome(state)

    assertFalse(outcome!!.shouldComplete)
    assertEquals(1, outcome.quiz.currentQuestionIndex)
    assertEquals(1, outcome.quiz.results.size)
    assertEquals(2, outcome.quiz.players.first().score)
  }

  @Test
  fun withSelectedCountry_keepsNonTrainingQuestionsHintable() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.Continents,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "1",
        selectedContinents = setOf("Europe"),
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(31)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(32),
        hintCount = 1,
        displayName = "Tony",
      )

    val updatedQuiz = quiz.withSelectedCountry(quiz.currentQuestion!!.correctCountry)

    assertEquals(QuestionStatus.Unanswered, updatedQuiz.currentQuestionState.status)
    assertTrue(updatedQuiz.currentQuestionState.selectedCountry != null)
  }
}

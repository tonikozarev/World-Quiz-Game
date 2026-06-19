package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import kotlin.random.Random

class FlagGameHintLogicTest {
  @Test
  fun applyHintToCurrentQuestion_speedRunAddsTimePenalty() {
    val countries = StaticFlagCatalogRepository().getCountries()
    val setup =
      buildSetupForMode(
        GameMode.SpeedRun,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "1",
        variants = setOf(QuizVariant.FlagToCountry),
      )

    val quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = QuizQuestionGenerator(Random(41)),
        hintDifficulty = HintDifficulty.Medium,
        random = Random(42),
        hintCount = 2,
        displayName = "Tony",
      )
    val state = FlagGameUiState(quiz = quiz)

    val firstHint = applyHintToCurrentQuestion(state)
    assertNotNull(firstHint)
    assertEquals(1, firstHint!!.hintCount)
    assertEquals(5, firstHint.speedRunPenaltySeconds)
    assertEquals(5, firstHint.quiz.speedRunPenaltySeconds)

    val secondState = state.copy(
      quiz = firstHint.quiz,
      hintCount = firstHint.hintCount,
    )
    val secondHint = applyHintToCurrentQuestion(secondState)
    assertNotNull(secondHint)
    assertEquals(0, secondHint!!.hintCount)
    assertEquals(10, secondHint.speedRunPenaltySeconds)
    assertEquals(15, secondHint.quiz.speedRunPenaltySeconds)
  }
}

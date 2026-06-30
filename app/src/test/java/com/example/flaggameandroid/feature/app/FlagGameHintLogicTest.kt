package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.QuizTopic
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
        GameMode.WorldFlags,
        listOf("Africa", "Asia", "Europe", "North America", "Oceania", "South America"),
        countries,
        "Tony",
      ).copy(
        questionCountInput = "1",
        variants = setOf(QuizVariant.FlagToCountry),
        worldFlagsTimerEnabled = true,
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
    assertEquals(1, firstHint.speedRunPenaltySeconds)
    assertEquals(1, firstHint.quiz.speedRunPenaltySeconds)

    val secondState =
      state.copy(
        quiz = firstHint.quiz,
        hintCount = firstHint.hintCount,
      )
    val secondHint = applyHintToCurrentQuestion(secondState)
    assertNotNull(secondHint)
    assertEquals(0, secondHint!!.hintCount)
    assertEquals(2, secondHint.speedRunPenaltySeconds)
    assertEquals(3, secondHint.quiz.speedRunPenaltySeconds)
  }

  @Test
  fun applyHintToCurrentQuestion_usesCapitalTextForCapitalQuestions() {
    val country =
      FlagCountry(
        code = "DE",
        name = "Germany",
        emoji = "",
        continent = "Europe",
        capital = "Berlin",
      )
    val question =
      FlagQuestion(
        correctCountry = country,
        options = listOf(country),
        variant = QuizVariant.TypeText,
        topic = QuizTopic.Capitals,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        questions = listOf(question),
        questionStates = listOf(QuestionDraftState()),
        players = listOf(PlayerProgress(name = "Solo", hintPoints = 1)),
      )
    val state = FlagGameUiState(quiz = quiz)

    val firstHint = applyHintToCurrentQuestion(state)

    assertNotNull(firstHint)
    assertEquals("Ber", firstHint!!.quiz.currentQuestionState.typedHintPrefix)
    assertEquals("", firstHint.quiz.currentQuestionState.typedAnswer)
  }
}

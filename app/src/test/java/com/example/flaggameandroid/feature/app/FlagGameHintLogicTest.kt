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
import junit.framework.TestCase.assertNull
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
        hintCount = 2.0,
        displayName = "Tony",
      )
    val state = FlagGameUiState(quiz = quiz)

    val firstHint = applyHintToCurrentQuestion(state)
    assertNotNull(firstHint)
    assertEquals(1.25, firstHint!!.hintCount)
    assertEquals(1, firstHint.speedRunPenaltySeconds)
    assertEquals(1, firstHint.quiz.speedRunPenaltySeconds)

    val secondState =
      state.copy(
        quiz = firstHint.quiz,
        hintCount = firstHint.hintCount,
      )
    val secondHint = applyHintToCurrentQuestion(secondState)
    assertNotNull(secondHint)
    assertEquals(0.5, secondHint!!.hintCount)
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
        players = listOf(PlayerProgress(name = "Solo", hintPoints = 2.0)),
      )
    val state = FlagGameUiState(quiz = quiz)

    val firstHint = applyHintToCurrentQuestion(state)
    val secondHint =
      applyHintToCurrentQuestion(
        state.copy(
          quiz = firstHint!!.quiz,
          hintCount = firstHint.hintCount,
        ),
      )
    val thirdHint =
      applyHintToCurrentQuestion(
        state.copy(
          quiz = secondHint!!.quiz,
          hintCount = secondHint.hintCount,
        ),
      )

    assertNotNull(firstHint)
    assertNotNull(secondHint)
    assertNotNull(thirdHint)
    val first = requireNotNull(firstHint)
    val second = requireNotNull(secondHint)
    val third = requireNotNull(thirdHint)
    assertEquals(1, first.quiz.currentQuestionState.hintUses)
    assertEquals(2, second.quiz.currentQuestionState.hintUses)
    assertEquals(3, third.quiz.currentQuestionState.hintUses)
    assertEquals(null, first.quiz.currentQuestionState.typedHintPrefix)
    assertEquals("Ber", second.quiz.currentQuestionState.typedHintPrefix)
    assertEquals(null, third.quiz.currentQuestionState.typedHintPrefix)
    assertEquals("Berlin", third.quiz.currentQuestionState.typedAnswer)
    assertEquals(true, third.quiz.currentQuestionState.revealed)
  }

  @Test
  fun applyHintToCurrentQuestion_secondHintSelectsCorrectAnswerForMultipleChoice() {
    val correct =
      FlagCountry(
        code = "DE",
        name = "Germany",
        emoji = "",
        continent = "Europe",
      )
    val wrongOne = FlagCountry(code = "AT", name = "Austria", emoji = "", continent = "Europe")
    val wrongTwo = FlagCountry(code = "CH", name = "Switzerland", emoji = "", continent = "Europe")
    val wrongThree = FlagCountry(code = "FR", name = "France", emoji = "", continent = "Europe")
    val question =
      FlagQuestion(
        correctCountry = correct,
        options = listOf(correct, wrongOne, wrongTwo, wrongThree),
        variant = QuizVariant.FlagToText,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        questions = listOf(question),
        questionStates = listOf(QuestionDraftState()),
        players = listOf(PlayerProgress(name = "Solo", hintPoints = 2.0)),
      )
    val state = FlagGameUiState(quiz = quiz)

    val firstHint = applyHintToCurrentQuestion(state)
    val secondHint =
      applyHintToCurrentQuestion(
        state.copy(
          quiz = firstHint!!.quiz,
          hintCount = firstHint.hintCount,
        ),
      )

    assertNotNull(firstHint)
    assertNotNull(secondHint)
    val first = requireNotNull(firstHint)
    val second = requireNotNull(secondHint)
    assertEquals(1, first.quiz.currentQuestionState.hintUses)
    assertEquals(2, second.quiz.currentQuestionState.hintUses)
    assertEquals(2, second.quiz.currentQuestionState.hiddenOptionCodes.size)

    val thirdHint =
      applyHintToCurrentQuestion(
        state.copy(
          quiz = second.quiz,
          hintCount = second.hintCount,
        ),
      )
    assertNotNull(thirdHint)
    val third = requireNotNull(thirdHint)
    assertEquals(3, third.quiz.currentQuestionState.hintUses)
    assertEquals(correct, third.quiz.currentQuestionState.selectedCountry)
    assertEquals(true, third.quiz.currentQuestionState.revealed)
  }

  @Test
  fun applyHintToCurrentQuestion_doesNotSpendHintsAfterInstantCorrectionLocksMultipleChoice() {
    val correct =
      FlagCountry(
        code = "DE",
        name = "Germany",
        emoji = "",
        continent = "Europe",
      )
    val wrong = FlagCountry(code = "AT", name = "Austria", emoji = "", continent = "Europe")
    val question =
      FlagQuestion(
        correctCountry = correct,
        options = listOf(correct, wrong),
        variant = QuizVariant.FlagToText,
      )
    val quiz =
      QuizState(
        mode = GameMode.WorldFlags,
        instantCorrectionEnabled = true,
        questions = listOf(question),
        questionStates =
          listOf(
            QuestionDraftState(
              status = QuestionStatus.Answered,
              selectedCountry = correct,
              locked = true,
            ),
          ),
        players = listOf(PlayerProgress(name = "Solo", hintPoints = 2.0)),
        selectedCountry = correct,
      )
    val state = FlagGameUiState(quiz = quiz)

    val hint = applyHintToCurrentQuestion(state)

    assertNull(hint)
  }
}

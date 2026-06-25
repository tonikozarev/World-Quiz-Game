package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FlagGameSpeedRunLogicTest {
  @Test
  fun speedRunBudget_countsTypedQuestionsAsDoubleTime() {
    val typedCountry = FlagCountry(code = "TA", name = "Typedland", emoji = "TA", continent = "Europe")
    val flagCountry = FlagCountry(code = "FA", name = "Flagland", emoji = "FA", continent = "Europe")
    val quiz =
      QuizState(
        mode = GameMode.SpeedRun,
        questions =
          listOf(
            FlagQuestion(
              correctCountry = typedCountry,
              options = listOf(typedCountry, flagCountry),
              variant = QuizVariant.TypeCountryName,
            ),
            FlagQuestion(
              correctCountry = flagCountry,
              options = listOf(typedCountry, flagCountry),
              variant = QuizVariant.FlagToCountry,
            ),
          ),
        questionStates =
          listOf(
            QuestionDraftState(status = QuestionStatus.Unanswered),
            QuestionDraftState(status = QuestionStatus.Unanswered),
          ),
        startedAtEpochMillis = 1_000L,
        speedRunSecondsPerAnswer = 3,
        countdownEnabled = true,
      )

    assertEquals(9_000L, speedRunTotalBudgetMillis(quiz))
    assertEquals(9_000L, speedRunRemainingMillis(quiz, 1_000L))
    assertEquals(6_000L, speedRunRemainingMillis(quiz.copy(speedRunPenaltySeconds = 3), 1_000L))
  }

  @Test
  fun speedRunBudget_addsFiveSecondBonusForOneSecondModeOnly() {
    val country = FlagCountry(code = "FA", name = "Flagland", emoji = "FA", continent = "Europe")
    val quiz =
      QuizState(
        mode = GameMode.SpeedRun,
        questions =
          List(10) {
            FlagQuestion(
              correctCountry = country,
              options = listOf(country),
              variant = QuizVariant.FlagToCountry,
            )
          },
        questionStates = List(10) { QuestionDraftState(status = QuestionStatus.Unanswered) },
        startedAtEpochMillis = 1_000L,
        speedRunSecondsPerAnswer = 1,
        countdownEnabled = true,
      )

    assertEquals(15_000L, speedRunTotalBudgetMillis(quiz))
  }
}

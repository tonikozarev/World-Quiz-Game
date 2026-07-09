package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizSessionMode
import com.example.flaggameandroid.core.model.QuizVariant

class DefaultFlagQuestionGenerator : FlagQuestionGenerator {
  override fun buildMultipleChoiceQuestions(
    countries: List<FlagCountry>,
    totalQuestions: Int,
  ): List<FlagQuestion> {
    return QuizQuestionGenerator().buildQuestions(
      countries = countries,
      config =
        QuizConfig(
          mode = GameMode.CreateQuiz,
          sessionMode = QuizSessionMode.Training,
          variants = setOf(QuizVariant.FlagToText),
          questionCount = totalQuestions,
        ),
    )
  }
}

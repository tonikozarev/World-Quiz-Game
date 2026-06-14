package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion

interface FlagQuestionGenerator {
  fun buildMultipleChoiceQuestions(
    countries: List<FlagCountry>,
    totalQuestions: Int = 5,
  ): List<FlagQuestion>
}

package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import kotlin.random.Random

class DefaultFlagQuestionGenerator : FlagQuestionGenerator {
  override fun buildMultipleChoiceQuestions(
    countries: List<FlagCountry>,
    totalQuestions: Int,
  ): List<FlagQuestion> {
    val safeCountries = countries.distinctBy { it.code }
    require(safeCountries.size >= 4) { "Need at least 4 countries to build multiple-choice questions." }

    return safeCountries.take(totalQuestions).mapIndexed { index, correctCountry ->
      val wrongCandidates = safeCountries.filterNot { it.code == correctCountry.code }
      val rotated = wrongCandidates.drop(index % wrongCandidates.size) + wrongCandidates.take(index % wrongCandidates.size)
      val options = (rotated.take(3) + correctCountry).shuffled(Random(index.toLong()))

      FlagQuestion(
        flag = correctCountry,
        options = options,
      )
    }
  }
}

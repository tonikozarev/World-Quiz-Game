package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagQuestion

class StaticFlagQuestionRepository : FlagQuestionRepository {
  override fun getMultipleChoiceQuestions(): List<FlagQuestion> =
    listOf(
      FlagQuestion(
        flagEmoji = "🇯🇵",
        correctAnswer = "Japan",
        options = listOf("Norway", "Mexico", "Ireland", "Japan"),
      ),
      FlagQuestion(
        flagEmoji = "🇳🇴",
        correctAnswer = "Norway",
        options = listOf("Norway", "Brazil", "Mexico", "Ireland"),
      ),
      FlagQuestion(
        flagEmoji = "🇲🇽",
        correctAnswer = "Mexico",
        options = listOf("Brazil", "Mexico", "Japan", "Ireland"),
      ),
      FlagQuestion(
        flagEmoji = "🇮🇪",
        correctAnswer = "Ireland",
        options = listOf("Ireland", "Norway", "Canada", "Japan"),
      ),
      FlagQuestion(
        flagEmoji = "🇧🇷",
        correctAnswer = "Brazil",
        options = listOf("Mexico", "Ireland", "Brazil", "Japan"),
      ),
    )
}

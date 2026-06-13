package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagQuestion

interface FlagQuestionRepository {
  fun getMultipleChoiceQuestions(): List<FlagQuestion>
}

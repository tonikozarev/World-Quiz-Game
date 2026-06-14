package com.example.flaggameandroid.core.model

data class QuestionResult(
  val question: FlagQuestion,
  val selectedAnswer: FlagCountry,
  val isCorrect: Boolean,
)

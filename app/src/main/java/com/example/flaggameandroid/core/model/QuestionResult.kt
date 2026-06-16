package com.example.flaggameandroid.core.model

data class QuestionResult(
  val question: FlagQuestion,
  val playerName: String,
  val selectedCountry: FlagCountry?,
  val typedAnswer: String,
  val isCorrect: Boolean,
  val hintUsed: Boolean,
  val skipped: Boolean = false,
)

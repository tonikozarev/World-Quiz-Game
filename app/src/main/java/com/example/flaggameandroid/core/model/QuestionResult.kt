package com.example.flaggameandroid.core.model

data class QuestionResult(
  val question: FlagQuestion,
  val playerName: String,
  val selectedCountry: FlagCountry?,
  val typedAnswer: String,
  val isCorrect: Boolean,
  val hintUsed: Boolean,
  val hintUses: Int = 0,
  val revealed: Boolean = false,
  val hintStreak: Int = 0,
)

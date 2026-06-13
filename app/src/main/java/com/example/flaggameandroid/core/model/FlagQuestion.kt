package com.example.flaggameandroid.core.model

data class FlagQuestion(
  val flagEmoji: String,
  val correctAnswer: String,
  val options: List<String>,
)

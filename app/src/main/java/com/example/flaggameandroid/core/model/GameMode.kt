package com.example.flaggameandroid.core.model

enum class GameMode(
  val title: String,
  val description: String,
) {
  MultipleChoice(
    title = "Multiple choices",
    description = "Pick the correct country from four answers.",
  ),
}

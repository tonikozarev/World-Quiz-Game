package com.example.flaggameandroid.core.model

data class FlagQuestion(
  val correctCountry: FlagCountry,
  val options: List<FlagCountry>,
  val variant: QuizVariant,
  val topic: QuizTopic = QuizTopic.Countries,
)

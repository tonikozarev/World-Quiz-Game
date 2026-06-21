package com.example.flaggameandroid.core.model

data class FlagCountry(
  val code: String,
  val name: String,
  val emoji: String,
  val continent: String,
  val aliases: List<String> = emptyList(),
  val capital: String? = null,
  val tags: Set<CountryTag> = emptySet(),
)

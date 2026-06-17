package com.example.flaggameandroid.core.model

enum class GameMode(
  val title: String,
  val description: String,
) {
  Training(
    title = "Training",
    description = "Mix flags, country names, and typed answers at your pace. Training does not give level-up progress.",
  ),
  Continents(
    title = "Continents",
    description = "Build a quiz from the continents you want to practice.",
  ),
  AllIn(
    title = "No Bluff, All Tough",
    description = "All countries with only the variants you choose.",
  ),
  LocalMultiplayer(
    title = "Local Multiplayer",
    description = "Up to 5 players pass one device and play turn by turn.",
  ),
}

enum class QuizVariant(
  val title: String,
  val description: String,
) {
  FlagToCountry(
    title = "Flag -> country",
    description = "See a flag and pick the country.",
  ),
  CountryToFlag(
    title = "Country -> flag",
    description = "See a country and pick the flag.",
  ),
  TypeCountryName(
    title = "Type the country",
    description = "See a flag and write the country name.",
  ),
}

enum class AllInType(
  val title: String,
  val description: String,
) {
  NoBluffAllTough(
    title = "No Bluff, All Tough",
    description = "All countries with only the variants you choose. Perfect clear with all 3 variants gives +1 full level, or +2 if the hint setting is 'the impossible one'.",
  ),
}

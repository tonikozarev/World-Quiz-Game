package com.example.flaggameandroid.core.model

enum class GameMode(
  val title: String,
  val description: String,
) {
  Training(
    title = "Training",
    description = "Mix flags, country names, and typed answers at your pace. Training does not give level-up progress.",
  ),
  CreateQuiz(
    title = "Create a quiz",
    description = "Build a custom quiz from preset flag filters or your own country selection.",
  ),
  Continents(
    title = "Continents",
    description = "Build a quiz from the continents you want to practice.",
  ),
  WorldFlags(
    title = "World Flags",
    description = "Guess the country/flag.",
  ),
  DailyChallenge(
    title = "Daily challenge",
    description = "One short themed quiz per UTC day. The challenge stays the same until midnight UTC.",
  ),
  MistakeReview(
    title = "Mistake review",
    description = "Practice only the countries you missed before.",
  ),
  SpeedRun(
    title = "Speed run",
    description = "A continent-based quiz against the clock. Hints and reveals add time.",
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

internal fun visibleGameModes(): List<GameMode> =
  listOf(
    GameMode.Training,
    GameMode.DailyChallenge,
    GameMode.WorldFlags,
    GameMode.Continents,
    GameMode.SpeedRun,
    GameMode.LocalMultiplayer,
    GameMode.AllIn,
    GameMode.MistakeReview,
  )

internal fun startQuizModes(): List<GameMode> =
  listOf(
    GameMode.Training,
    GameMode.DailyChallenge,
    GameMode.LocalMultiplayer,
    GameMode.MistakeReview,
  )

internal fun gameModesHubModes(): List<GameMode> =
  listOf(
    GameMode.WorldFlags,
    GameMode.Continents,
    GameMode.SpeedRun,
    GameMode.AllIn,
    GameMode.CreateQuiz,
  )

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

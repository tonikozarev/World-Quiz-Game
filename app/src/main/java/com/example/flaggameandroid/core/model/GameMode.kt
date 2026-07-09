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
  WorldFlags(
    title = "Country Flags",
    description = "Guess the correct country or flag.",
  ),
  DailyChallenge(
    title = "Daily challenge",
    description = "One short themed quiz per UTC day. The challenge stays the same until midnight UTC.",
  ),
  MistakeReview(
    title = "Mistake review",
    description = "Practice only the countries you missed before.",
  ),
  LocalMultiplayer(
    title = "Local Multiplayer",
    description = "Up to 5 players pass one device and play turn by turn.",
  ),
}

internal fun startQuizModes(): List<GameMode> =
  listOf(
    GameMode.DailyChallenge,
    GameMode.CreateQuiz,
    GameMode.MistakeReview,
  )

internal fun gameModesHubModes(): List<GameMode> =
  emptyList()

enum class QuizVariant(
  val title: String,
  val description: String,
) {
  FlagToText(
    title = "Flag -> text",
    description = "See a flag and pick the matching text.",
  ),
  TextToFlag(
    title = "Text -> flag",
    description = "See text and pick the matching flag.",
  ),
  TypeText(
    title = "Type the text",
    description = "See a flag and write the matching text.",
  ),
  ;

  companion object {
    @Suppress("PropertyName")
    val FlagToCountry: QuizVariant = FlagToText

    @Suppress("PropertyName")
    val CountryToFlag: QuizVariant = TextToFlag

    @Suppress("PropertyName")
    val TypeCountryName: QuizVariant = TypeText
  }
}

enum class QuizTopic(
  val title: String,
) {
  Countries("Countries"),
  Capitals("Capitals"),
  Mixed("Mixed"),
}

enum class AllInType(
  val title: String,
  val description: String,
) {
  NoBluffAllTough(
    title = "No Bluff, All Tough",
    description = "All countries with only the variants you choose.",
  ),
}

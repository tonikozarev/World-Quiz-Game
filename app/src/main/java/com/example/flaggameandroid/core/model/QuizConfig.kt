package com.example.flaggameandroid.core.model

data class QuizConfig(
  val mode: GameMode,
  val variants: Set<QuizVariant>,
  val selectedContinents: Set<String> = emptySet(),
  val questionCount: Int,
  val speedRunSecondsPerAnswer: Int = 3,
  val countdownEnabled: Boolean = false,
  val surpriseMe: Boolean = false,
  val allInType: AllInType? = null,
  val hintDifficulty: HintDifficulty = HintDifficulty.Medium,
  val players: List<String> = listOf("Solo"),
  val poolSource: QuizPoolSource = QuizPoolSource.Standard,
  val dailyChallengeTheme: DailyChallengeTheme? = null,
)

data class PlayerProgress(
  val name: String,
  val score: Int = 0,
  val hintPoints: Int = 0,
  val earnedHintPoints: Int = 0,
  val correctStreak: Int = 0,
) {
  fun afterAnswer(
    isCorrect: Boolean,
    hintUses: Int,
    hintDifficulty: HintDifficulty,
    canEarnHints: Boolean = true,
  ): PlayerProgress {
    if (!isCorrect || hintUses >= 2) return copy(correctStreak = 0)

    if (hintUses == 1) {
      return copy(score = score + 1)
    }

    val newStreak = correctStreak + 1
    return copy(
      score = score + 2,
      correctStreak = newStreak,
      earnedHintPoints = earnedHintPoints + if (canEarnHints && newStreak % hintDifficulty.correctStreakRequired == 0) 1 else 0,
    )
  }

  fun spendHint(): PlayerProgress = copy(hintPoints = hintPoints - 1)

  fun afterSkip(): PlayerProgress = copy(correctStreak = 0)

  fun releaseEarnedHints(): PlayerProgress =
    copy(
      hintPoints = hintPoints + earnedHintPoints,
      earnedHintPoints = 0,
    )
}

enum class HintDifficulty(
  val title: String,
  val correctStreakRequired: Int,
  val variantWeights: Map<QuizVariant, Int>,
) {
  Rookie(
    title = "Rookie",
    correctStreakRequired = 3,
    variantWeights =
      mapOf(
        QuizVariant.FlagToCountry to 45,
        QuizVariant.CountryToFlag to 45,
        QuizVariant.TypeCountryName to 10,
      ),
  ),
  Medium(
    title = "Medium",
    correctStreakRequired = 5,
    variantWeights =
      mapOf(
        QuizVariant.FlagToCountry to 40,
        QuizVariant.CountryToFlag to 40,
        QuizVariant.TypeCountryName to 20,
      ),
  ),
  Hard(
    title = "Hard",
    correctStreakRequired = 10,
    variantWeights =
      mapOf(
        QuizVariant.FlagToCountry to 30,
        QuizVariant.CountryToFlag to 30,
        QuizVariant.TypeCountryName to 40,
      ),
  ),
  Impossible(
    title = "The impossible one",
    correctStreakRequired = 50,
    variantWeights =
      mapOf(
        QuizVariant.FlagToCountry to 20,
        QuizVariant.CountryToFlag to 20,
        QuizVariant.TypeCountryName to 60,
      ),
  ),
}

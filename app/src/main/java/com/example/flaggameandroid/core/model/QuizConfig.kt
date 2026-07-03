package com.example.flaggameandroid.core.model

data class QuizConfig(
  val mode: GameMode,
  val variants: Set<QuizVariant>,
  val topic: QuizTopic = QuizTopic.Countries,
  val selectedContinents: Set<String> = emptySet(),
  val questionSpecs: List<QuizQuestionSpec> = emptyList(),
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

data class QuizQuestionSpec(
  val countryCode: String,
  val topic: QuizTopic,
)

data class PlayerProgress(
  val name: String,
  val score: Int = 0,
  val hintPoints: Double = 0.0,
  val earnedHintPoints: Int = 0,
  val correctStreak: Int = 0,
) {
  fun afterAnswer(
    isCorrect: Boolean,
    hintUses: Int,
    revealed: Boolean = false,
    hintDifficulty: HintDifficulty,
    canEarnHints: Boolean = true,
  ): PlayerProgress {
    if (!isCorrect || revealed) return copy(correctStreak = 0)

    if (hintUses > 0) {
      return copy(score = score + 1)
    }

    val newStreak = correctStreak + 1
    return copy(
      score = score + 2,
      correctStreak = newStreak,
      earnedHintPoints = earnedHintPoints + if (canEarnHints && newStreak % hintDifficulty.correctStreakRequired == 0) 1 else 0,
    )
  }

  fun spendHint(cost: Double = 1.0): PlayerProgress = copy(hintPoints = hintPoints - cost)

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
        QuizVariant.FlagToText to 45,
        QuizVariant.TextToFlag to 45,
        QuizVariant.TypeText to 10,
      ),
  ),
  Medium(
    title = "Medium",
    correctStreakRequired = 5,
    variantWeights =
      mapOf(
        QuizVariant.FlagToText to 40,
        QuizVariant.TextToFlag to 40,
        QuizVariant.TypeText to 20,
      ),
  ),
  Hard(
    title = "Hard",
    correctStreakRequired = 10,
    variantWeights =
      mapOf(
        QuizVariant.FlagToText to 30,
        QuizVariant.TextToFlag to 30,
        QuizVariant.TypeText to 40,
      ),
  ),
  Impossible(
    title = "The impossible one",
    correctStreakRequired = 50,
    variantWeights =
      mapOf(
        QuizVariant.FlagToText to 20,
        QuizVariant.TextToFlag to 20,
        QuizVariant.TypeText to 60,
      ),
  ),
}

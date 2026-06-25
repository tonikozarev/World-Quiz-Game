package com.example.flaggameandroid.core.model

object ProgressionRules {
  const val MaxLevel: Int = 10
  const val TotalAvatarCount: Int = 50

  fun medalForPerfectQuiz(
    totalQuestions: Int,
    distinctCountries: Int,
    totalCatalogCountries: Int,
  ): MedalTier? =
    when {
      totalQuestions == totalCatalogCountries && distinctCountries == totalCatalogCountries -> MedalTier.Diamond
      totalQuestions >= 100 -> MedalTier.Titanium
      totalQuestions >= 50 -> MedalTier.Gold
      totalQuestions >= 25 -> MedalTier.Silver
      totalQuestions >= 10 -> MedalTier.Bronze
      else -> null
    }

  fun shouldWarnNoMedal(questionCount: Int?): Boolean = questionCount != null && questionCount in 1..9

  fun requirementsForLevel(level: Int): LevelRequirements {
    val clampedLevel = level.coerceIn(1, MaxLevel)
    return LevelRequirements(
      hintsNeeded = 10 + ((clampedLevel - 1) * 5),
      correctAnswersNeeded = 100 + ((clampedLevel - 1) * 50),
      eligibleQuizzesNeeded = 10 + ((clampedLevel - 1) * 5),
    )
  }

  fun isMaxLevel(level: Int): Boolean = level >= MaxLevel

  fun unlockedAvatarCount(level: Int): Int = (level.coerceIn(1, MaxLevel) * 5).coerceAtMost(TotalAvatarCount)

  fun qualifiesForContinentAchievement(
    mode: GameMode,
    selectedContinents: Set<String>,
    usedHint: Boolean,
    totalQuestions: Int,
    correctAnswers: Int,
    distinctCountries: Int,
    availableCountriesForSelectedContinent: Int,
  ): Boolean =
    (mode == GameMode.Continents || mode == GameMode.WorldFlags || mode == GameMode.SpeedRun) &&
      selectedContinents.size == 1 &&
      !usedHint &&
      totalQuestions == correctAnswers &&
      totalQuestions == availableCountriesForSelectedContinent &&
      distinctCountries == availableCountriesForSelectedContinent
}

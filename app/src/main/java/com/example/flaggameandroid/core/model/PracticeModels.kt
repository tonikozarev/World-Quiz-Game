package com.example.flaggameandroid.core.model

internal const val MistakeReviewUnlockCountryCount = 10
internal const val MistakeReviewMissThreshold = 10
internal const val MistakeReviewRecoveryWrongCount = 5

data class CountryPracticeStats(
  val correctCount: Int = 0,
  val wrongCount: Int = 0,
  val lastMissedAtEpochMillis: Long = 0L,
  val favorite: Boolean = false,
) {
  val isWeak: Boolean
    get() = wrongCount >= 2 && wrongCount > correctCount

  val isMistakeReviewEligible: Boolean
    get() = wrongCount >= MistakeReviewMissThreshold
}

data class ActivityDayRecord(
  val dayKey: Long,
  val quizzesCompleted: Int = 0,
  val dailyChallengeCompleted: Boolean = false,
  val lastUpdatedAtEpochMillis: Long = 0L,
  val streakStartDayKey: Long? = null,
  val lastActiveDayKey: Long? = null,
)

data class DailyChallengeCache(
  val dayKey: Long = 0L,
  val theme: DailyChallengeTheme = DailyChallengeTheme.World,
  val questionCount: Int = 10,
  val seed: Long = 0L,
  val completed: Boolean = false,
  val completedAtEpochMillis: Long = 0L,
) {
  val instanceKey: String
    get() = listOf(dayKey, theme.name, questionCount, seed).joinToString(separator = ":")
}

enum class DailyChallengeTheme(
  val title: String,
) {
  World("World"),
  Africa("Africa"),
  Asia("Asia"),
  Europe("Europe"),
  NorthAmerica("North America"),
  Oceania("Oceania"),
  SouthAmerica("South America"),
  FlagsWithStripes("Flags with stripes"),
  Capitals("Capitals"),
}

enum class QuizPoolSource {
  Standard,
  DailyChallenge,
  MistakeReview,
}

enum class CountryTag {
  StripedFlag,
  Landlocked,
  Island,
}

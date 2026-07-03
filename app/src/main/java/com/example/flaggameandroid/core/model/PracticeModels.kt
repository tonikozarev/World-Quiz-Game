package com.example.flaggameandroid.core.model

internal const val MistakeReviewUnlockCountryCount = 10
internal const val MistakeReviewMissThreshold = 10
internal const val MistakeReviewRecoveryWrongCount = 5

data class CountryPracticeStats(
  val correctCount: Int = 0,
  val wrongCount: Int = 0,
  val capitalCorrectCount: Int = 0,
  val capitalWrongCount: Int = 0,
  val lastMissedAtEpochMillis: Long = 0L,
  val favorite: Boolean = false,
) {
  val totalCorrectCount: Int
    get() = correctCount + capitalCorrectCount

  val totalWrongCount: Int
    get() = wrongCount + capitalWrongCount

  val isWeak: Boolean
    get() = totalWrongCount >= 2 && totalWrongCount > totalCorrectCount

  val isMistakeReviewEligible: Boolean
    get() = wrongCount >= MistakeReviewMissThreshold

  fun wrongCountFor(topic: QuizTopic): Int =
    when (topic) {
      QuizTopic.Countries -> wrongCount
      QuizTopic.Capitals -> capitalWrongCount
      QuizTopic.Mixed -> maxOf(wrongCount, capitalWrongCount)
    }

  fun correctCountFor(topic: QuizTopic): Int =
    when (topic) {
      QuizTopic.Countries -> correctCount
      QuizTopic.Capitals -> capitalCorrectCount
      QuizTopic.Mixed -> maxOf(correctCount, capitalCorrectCount)
    }

  fun isMistakeReviewEligible(topic: QuizTopic): Boolean =
    when (topic) {
      QuizTopic.Countries -> wrongCount >= MistakeReviewMissThreshold
      QuizTopic.Capitals -> capitalWrongCount >= MistakeReviewMissThreshold
      QuizTopic.Mixed ->
        wrongCount >= MistakeReviewMissThreshold || capitalWrongCount >= MistakeReviewMissThreshold
    }
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
  val topic: QuizTopic = QuizTopic.Countries,
  val theme: DailyChallengeTheme = DailyChallengeTheme.World,
  val questionCount: Int = 10,
  val seed: Long = 0L,
  val completed: Boolean = false,
  val completedAtEpochMillis: Long = 0L,
) {
  val instanceKey: String
    get() = listOf(dayKey, topic.name, theme.name, questionCount, seed).joinToString(separator = ":")
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

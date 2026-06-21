package com.example.flaggameandroid.core.model

enum class MedalTier(
  val title: String,
  val badge: String,
) {
  Bronze("Bronze", "\uD83E\uDD49"),
  Silver("Silver", "\uD83E\uDD48"),
  Gold("Gold", "\uD83E\uDD47"),
  Titanium("Platinum", "\uD83C\uDFC5"),
  Diamond("Diamond", "\uD83D\uDC8E"),
}

enum class AchievementSector(
  val title: String,
) {
  Continents("Continent masters"),
  World("World runs"),
  SpeedRuns("Speed runs"),
  Collectors("Medal collectors"),
  Skill("Skill feats"),
}

enum class AchievementId(
  val sector: AchievementSector,
  val title: String,
  val badge: String,
  val description: String,
) {
  AfricaPerfect(AchievementSector.Continents, "Africa Perfect", "\uD83C\uDF0D", "Complete every African country perfectly without using hints."),
  AsiaPerfect(AchievementSector.Continents, "Asia Perfect", "\uD83C\uDF0F", "Complete every Asian country perfectly without using hints."),
  EuropePerfect(AchievementSector.Continents, "Europe Perfect", "\uD83C\uDFF0", "Complete every European country perfectly without using hints."),
  NorthAmericaPerfect(AchievementSector.Continents, "North America Perfect", "\uD83E\uDDAC", "Complete every North American country perfectly without using hints."),
  OceaniaPerfect(AchievementSector.Continents, "Oceania Perfect", "\uD83C\uDF0A", "Complete every Oceanian country perfectly without using hints."),
  SouthAmericaPerfect(AchievementSector.Continents, "South America Perfect", "\uD83E\uDD9C", "Complete every South American country perfectly without using hints."),
  DiamondWorld(AchievementSector.World, "Diamond World", "\uD83D\uDC8E", "Complete all countries perfectly in one quiz."),
  NoBluffLegend(AchievementSector.World, "No Bluff Legend", "\uD83E\uDDE0", "Perfectly clear No Bluff, All Tough with all three variants selected."),
  WorldPurist(AchievementSector.World, "World Purist", "\uD83C\uDF10", "Complete all countries perfectly without using any hints."),
  SpeedRunStarter(AchievementSector.SpeedRuns, "Speed Run Starter", "\u23F1\uFE0F", "Finish any Speed Run quiz."),
  SpeedRunPurist(AchievementSector.SpeedRuns, "Speed Run Purist", "\uD83D\uDD25", "Finish a perfect Speed Run quiz without using hints."),
  BronzeCollector(AchievementSector.Collectors, "Bronze Collector", "\uD83E\uDD49", "Earn bronze medals 50 times."),
  SilverCollector(AchievementSector.Collectors, "Silver Collector", "\uD83E\uDD48", "Earn silver medals 25 times."),
  GoldCollector(AchievementSector.Collectors, "Gold Collector", "\uD83E\uDD47", "Earn gold medals 10 times."),
  PlatinumCollector(AchievementSector.Collectors, "Platinum Collector", "\uD83C\uDFC5", "Earn platinum medals 5 times."),
  DiamondCollector(AchievementSector.Collectors, "Diamond Collector", "\uD83D\uDC8E", "Earn a diamond medal once."),
  FirstPerfect(AchievementSector.Skill, "First Perfect", "\u2728", "Finish any medal-eligible quiz with 100% correct answers."),
  HintlessHero(AchievementSector.Skill, "Hintless Hero", "\uD83D\uDEE1\uFE0F", "Finish a perfect medal-eligible quiz without using hints."),
  VariantMaster(AchievementSector.Skill, "Variant Master", "\uD83C\uDFAF", "Finish a perfect quiz that includes all three question variants."),
  ;

  companion object {
    fun forContinent(continent: String): AchievementId? =
      when (continent) {
        "Africa" -> AfricaPerfect
        "Asia" -> AsiaPerfect
        "Europe" -> EuropePerfect
        "North America" -> NorthAmericaPerfect
        "Oceania" -> OceaniaPerfect
        "South America" -> SouthAmericaPerfect
        else -> null
      }
  }
}

data class RatingsProgress(
  val bronzeCount: Int = 0,
  val silverCount: Int = 0,
  val goldCount: Int = 0,
  val titaniumCount: Int = 0,
  val diamondCount: Int = 0,
  val streak7Count: Int = 0,
  val streak30Count: Int = 0,
  val streak7ProgressDays: Int = 0,
  val streak30ProgressDays: Int = 0,
) {
  fun countFor(tier: MedalTier): Int =
    when (tier) {
      MedalTier.Bronze -> bronzeCount
      MedalTier.Silver -> silverCount
      MedalTier.Gold -> goldCount
      MedalTier.Titanium -> titaniumCount
      MedalTier.Diamond -> diamondCount
    }

  fun countForStreak(days: Int): Int =
    when (days) {
      7 -> streak7Count
      30 -> streak30Count
      else -> 0
    }

  fun increment(tier: MedalTier): RatingsProgress =
    when (tier) {
      MedalTier.Bronze -> copy(bronzeCount = bronzeCount + 1)
      MedalTier.Silver -> copy(silverCount = silverCount + 1)
      MedalTier.Gold -> copy(goldCount = goldCount + 1)
      MedalTier.Titanium -> copy(titaniumCount = titaniumCount + 1)
      MedalTier.Diamond -> copy(diamondCount = diamondCount + 1)
    }

  fun withStreakProgress(
    streak7ProgressDays: Int,
    streak30ProgressDays: Int,
    streak7Count: Int = this.streak7Count,
    streak30Count: Int = this.streak30Count,
  ): RatingsProgress =
    copy(
      streak7ProgressDays = streak7ProgressDays,
      streak30ProgressDays = streak30ProgressDays,
      streak7Count = streak7Count,
      streak30Count = streak30Count,
    )
}

data class AchievementsProgress(
  val unlockedAtEpochMillisById: Map<AchievementId, Long> = emptyMap(),
) {
  fun unlockedAt(achievementId: AchievementId): Long? = unlockedAtEpochMillisById[achievementId]

  fun isUnlocked(achievementId: AchievementId): Boolean = unlockedAt(achievementId) != null

  fun unlock(
    achievementId: AchievementId,
    unlockedAtEpochMillis: Long,
  ): AchievementsProgress =
    if (isUnlocked(achievementId)) {
      this
    } else {
      copy(unlockedAtEpochMillisById = unlockedAtEpochMillisById + (achievementId to unlockedAtEpochMillis))
    }
}

data class LevelRequirements(
  val hintsNeeded: Int,
  val correctAnswersNeeded: Int,
  val eligibleQuizzesNeeded: Int,
)

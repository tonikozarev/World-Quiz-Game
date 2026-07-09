package com.example.flaggameandroid.core.model

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ProgressionRulesTest {
  @Test
  fun medalRules_pickBronzeForPerfectTenQuestionQuiz() {
    assertEquals(
      MedalTier.Bronze,
      ProgressionRules.medalForPerfectQuiz(
        totalQuestions = 10,
        distinctCountries = 10,
        totalCatalogCountries = 195,
      ),
    )
  }

  @Test
  fun medalRules_pickDiamondWhenWholeCatalogIsPerfect() {
    assertEquals(
      MedalTier.Diamond,
      ProgressionRules.medalForPerfectQuiz(
        totalQuestions = 195,
        distinctCountries = 195,
        totalCatalogCountries = 195,
      ),
    )
  }

  @Test
  fun underTenQuestions_showNoMedalWarning() {
    assertTrue(ProgressionRules.shouldWarnNoMedal(9))
    assertFalse(ProgressionRules.shouldWarnNoMedal(10))
    assertFalse(ProgressionRules.shouldWarnNoMedal(null))
  }

  @Test
  fun levelRequirements_followTheNewTenLevelProgressionCurve() {
    assertEquals(LevelRequirements(10, 100, 10), ProgressionRules.requirementsForLevel(1))
    assertEquals(LevelRequirements(15, 150, 15), ProgressionRules.requirementsForLevel(2))
    assertEquals(LevelRequirements(20, 200, 20), ProgressionRules.requirementsForLevel(3))
    assertEquals(LevelRequirements(55, 550, 55), ProgressionRules.requirementsForLevel(10))
  }

  @Test
  fun unlockedAvatarCount_revealsFiveMorePerLevelUpToFifty() {
    assertEquals(5, ProgressionRules.unlockedAvatarCount(1))
    assertEquals(10, ProgressionRules.unlockedAvatarCount(2))
    assertEquals(25, ProgressionRules.unlockedAvatarCount(5))
    assertEquals(50, ProgressionRules.unlockedAvatarCount(10))
    assertEquals(50, ProgressionRules.unlockedAvatarCount(99))
  }

  @Test
  fun continentAchievement_requiresSingleContinentPerfectNoHintRun() {
    assertTrue(
      ProgressionRules.qualifiesForContinentAchievement(
        mode = GameMode.CreateQuiz,
        selectedContinents = setOf("Europe"),
        usedHint = false,
        totalQuestions = 44,
        correctAnswers = 44,
        distinctCountries = 44,
        availableCountriesForSelectedContinent = 44,
      ),
    )
  }

  @Test
  fun continentAchievement_rejectsHintedOrPartialRun() {
    assertFalse(
      ProgressionRules.qualifiesForContinentAchievement(
        mode = GameMode.CreateQuiz,
        selectedContinents = setOf("Europe"),
        usedHint = true,
        totalQuestions = 44,
        correctAnswers = 44,
        distinctCountries = 44,
        availableCountriesForSelectedContinent = 44,
      ),
    )
    assertFalse(
      ProgressionRules.qualifiesForContinentAchievement(
        mode = GameMode.CreateQuiz,
        selectedContinents = setOf("Europe"),
        usedHint = false,
        totalQuestions = 20,
        correctAnswers = 20,
        distinctCountries = 20,
        availableCountriesForSelectedContinent = 44,
      ),
    )
  }
}

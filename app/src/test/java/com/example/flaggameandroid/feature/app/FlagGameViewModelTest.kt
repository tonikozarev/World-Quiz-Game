package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.data.StaticFlagCatalogRepository
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.persistence.PersistedAppState
import com.example.flaggameandroid.persistence.PersistedQuizHistory
import com.example.flaggameandroid.persistence.ProgressStore
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class FlagGameViewModelTest {
  private fun viewModel(): FlagGameViewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = QuizQuestionGenerator(Random(3)),
      random = Random(4),
    )

  private fun viewModel(initialPersistedState: PersistedAppState): FlagGameViewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = QuizQuestionGenerator(Random(3)),
      random = Random(4),
      initialPersistedState = initialPersistedState,
    )

  private fun viewModel(
    initialPersistedState: PersistedAppState,
    progressStore: ProgressStore,
  ): FlagGameViewModel =
    FlagGameViewModel(
      catalogRepository = StaticFlagCatalogRepository(),
      questionGenerator = QuizQuestionGenerator(Random(3)),
      random = Random(4),
      progressStore = progressStore,
      initialPersistedState = initialPersistedState,
    )

  @Test
  fun trainingMode_startsConfigurableQuiz() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    viewModel.onQuestionCountChanged(12)
    viewModel.onStartQuiz()

    val state = viewModel.uiState.value
    assertEquals(AppScreen.Quiz, state.screen)
    assertEquals(GameMode.Training, state.quiz.mode)
    assertEquals(12, state.quiz.totalQuestions)
  }

  @Test
  fun instantCorrectionToggle_enablesImmediatePreviewOutsideTraining() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.onInstantCorrectionToggled()
    QuizVariant.entries.filterNot { it == QuizVariant.FlagToCountry }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(1)
    viewModel.onStartQuiz()

    val question = viewModel.uiState.value.quiz.currentQuestion!!
    viewModel.onCountryAnswerSelected(question.correctCountry)

    val quiz = viewModel.uiState.value.quiz
    assertTrue(quiz.instantCorrectionEnabled)
    assertEquals(QuestionStatus.Answered, quiz.currentQuestionState.status)
    assertTrue(quiz.currentQuestionState.locked)
  }

  @Test
  fun instantCorrectionToggle_allowsTypedVerifyOutsideTraining() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.onInstantCorrectionToggled()
    QuizVariant.entries.filterNot { it == QuizVariant.TypeCountryName }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(1)
    viewModel.onStartQuiz()

    val question = viewModel.uiState.value.quiz.currentQuestion!!
    viewModel.onTypedAnswerChanged(question.correctCountry.name)
    viewModel.onVerifyTypedAnswer()

    val quiz = viewModel.uiState.value.quiz
    assertEquals(QuestionStatus.Answered, quiz.currentQuestionState.status)
    assertTrue(quiz.currentQuestionState.locked)
  }

  @Test
  fun setup_showsSevenContinentsIncludingSeparateAmericas() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.WorldFlags)

    assertEquals(
      listOf("Africa", "Antarctica", "Asia", "Europe", "North America", "Oceania", "South America"),
      viewModel.uiState.value.availableContinents,
    )
    assertTrue("Antarctica" !in viewModel.uiState.value.setup.selectedContinents)
  }

  @Test
  fun createQuizMixedKeepsCountryAndCapitalSelectionsSeparate() {
    val viewModel = viewModel()

    viewModel.onQuizTopicSelected(QuizTopic.Mixed)
    viewModel.onModeSelected(GameMode.CreateQuiz)
    viewModel.onCreateQuizCountryToggled("AT")
    viewModel.onCreateQuizCountryToggled("BG")
    viewModel.onCreateQuizCountryToggled("DE")
    viewModel.onCreateQuizCapitalToggled("AT")
    viewModel.onCreateQuizCapitalToggled("BG")
    viewModel.onCreateQuizCapitalToggled("DE")

    val setup = viewModel.uiState.value.setup
    assertEquals(CreateQuizSource.ManualCountriesCapitals, setup.createQuizSource)
    assertEquals(setOf("AT", "BG", "DE"), setup.selectedCountryCodes)
    assertEquals(setOf("AT", "BG", "DE"), setup.selectedCapitalCountryCodes)
    assertEquals("6", setup.questionCountInput)
    assertEquals(6, viewModel.uiState.value.questionCountLimit)

    viewModel.onStartQuiz()

    val quiz = viewModel.uiState.value.quiz
    assertEquals(GameMode.CreateQuiz, quiz.mode)
    assertEquals(6, quiz.totalQuestions)
    assertEquals(3, quiz.questions.map { it.correctCountry.code }.distinct().size)
    assertTrue(quiz.questions.any { it.topic == QuizTopic.Countries })
    assertTrue(quiz.questions.any { it.topic == QuizTopic.Capitals })
  }

  @Test
  fun surpriseMeClearsCountAndCanBeDeselectedForCustomAmount() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    viewModel.onSurpriseMeClicked()
    assertEquals("", viewModel.uiState.value.setup.questionCountInput)
    assertTrue(viewModel.uiState.value.setup.surpriseMe)

    viewModel.onSurpriseMeClicked()
    viewModel.onQuestionCountChanged("14")
    viewModel.onStartQuiz()

    val state = viewModel.uiState.value
    assertEquals(AppScreen.Quiz, state.screen)
    assertEquals(14, state.quiz.totalQuestions)
  }

  @Test
  fun setupBack_returnsToGameModes() {
    val viewModel = viewModel()

    viewModel.onStartClicked()
    viewModel.onModeSelected(GameMode.Training)
    viewModel.onBackToGameModes()

    assertEquals(AppScreen.GameModes, viewModel.uiState.value.screen)
  }

  @Test
  fun trainingMode_allowsNineHundredNinetyNineQuestionsAndRepeatsCountries() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    viewModel.onQuestionCountChanged(999)
    viewModel.onStartQuiz()

    val questions = viewModel.uiState.value.quiz.questions
    assertEquals(999, questions.size)
    assertTrue(questions.map { it.correctCountry.code }.distinct().size < questions.size)
  }

  @Test
  fun changedAnswerBeforeNext_usesFinalSelectionForScore() {
    val viewModel = viewModel()
    viewModel.onModeSelected(GameMode.WorldFlags)
    QuizVariant.entries.filterNot { it == QuizVariant.FlagToCountry }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(3)
    viewModel.onStartQuiz()

    val question = viewModel.uiState.value.quiz.currentQuestion!!
    val correct = question.correctCountry
    val wrong = question.options.first { it.code != correct.code }

    viewModel.onCountryAnswerSelected(correct)
    assertEquals(0, viewModel.uiState.value.quiz.currentPlayer.score)

    viewModel.onCountryAnswerSelected(wrong)
    viewModel.onNextQuestion()

    assertEquals(0, viewModel.uiState.value.quiz.players.first().score)
    assertEquals(false, viewModel.uiState.value.quiz.results.first().isCorrect)
  }

  @Test
  fun trainingAnsweredQuestionLocksAfterAdvancingAndCannotBeChangedOnReturn() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 2)

    val question = viewModel.uiState.value.quiz.currentQuestion!!
    val correct = question.correctCountry
    val wrong = question.options.first { it.code != correct.code }

    viewModel.onCountryAnswerSelected(correct)
    viewModel.onNextQuestionPreview()
    viewModel.onPreviousQuestion()

    assertTrue(viewModel.uiState.value.quiz.questionStates.first().locked)
    assertEquals(correct, viewModel.uiState.value.quiz.questionStates.first().selectedCountry)

    viewModel.onCountryAnswerSelected(wrong)
    assertEquals(correct, viewModel.uiState.value.quiz.questionStates.first().selectedCountry)
  }

  @Test
  fun trainingMode_toggleOffDisablesImmediateCorrectionAndAllowsChangingAnswer() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    viewModel.onInstantCorrectionToggled()
    QuizVariant.entries.filterNot { it == QuizVariant.FlagToCountry }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(1)
    viewModel.onStartQuiz()

    val question = viewModel.uiState.value.quiz.currentQuestion!!
    val correct = question.correctCountry
    val wrong = question.options.first { it.code != correct.code }

    viewModel.onCountryAnswerSelected(correct)
    assertFalse(viewModel.uiState.value.quiz.currentQuestionState.locked)

    viewModel.onCountryAnswerSelected(wrong)
    assertEquals(wrong, viewModel.uiState.value.quiz.currentQuestionState.selectedCountry)
    assertFalse(viewModel.uiState.value.quiz.currentQuestionState.locked)
  }

  @Test
  fun fiveCorrectInARow_keepsNewHintPointLockedUntilQuizEnds() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 6)

    repeat(5) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val player = viewModel.uiState.value.quiz.players.first()
    assertEquals(10, player.score)
    assertEquals(0.0, player.hintPoints)
    assertEquals(1, player.earnedHintPoints)
    assertEquals(5, player.correctStreak)
  }

  @Test
  fun earnedHintPointsBecomeUsableAfterQuizEnds() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 5)

    repeat(5) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val player = viewModel.uiState.value.quiz.players.first()
    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(1.0, player.hintPoints)
    assertEquals(1, player.earnedHintPoints)
    assertEquals(1.0, viewModel.uiState.value.hintCount)
  }

  @Test
  fun wrongAnswerResetsCorrectStreakBeforeHintIsEarned() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 8)

    repeat(4) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }
    answerCurrentWrongly(viewModel)
    viewModel.onNextQuestion()
    repeat(3) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val player = viewModel.uiState.value.quiz.players.first()
    assertEquals(14, player.score)
    assertEquals(0.0, player.hintPoints)
    assertEquals(0, player.earnedHintPoints)
    assertEquals(3, player.correctStreak)
  }

  @Test
  fun hintUsesTwoSteps_firstHintRemovesHalfAndSecondHintSolvesQuestion() {
    val viewModel =
      viewModel(
        PersistedAppState(
          hintCount = 10.0,
        ),
      )
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 2)

    assertEquals(10.0, viewModel.uiState.value.quiz.currentPlayer.hintPoints)
    viewModel.onUseHint()

    var quiz = viewModel.uiState.value.quiz
    assertEquals(9.25, quiz.currentPlayer.hintPoints)
    assertTrue(quiz.hintUsedOnCurrentQuestion)
    assertEquals(0, quiz.hiddenOptionCodes.size)

    viewModel.onUseHint()
    quiz = viewModel.uiState.value.quiz
    assertEquals(8.5, quiz.currentPlayer.hintPoints)
    assertTrue(quiz.hintUsedOnCurrentQuestion)
    assertEquals(2, quiz.currentQuestion?.options?.count { it.code !in quiz.hiddenOptionCodes })
    assertEquals(null, quiz.selectedCountry)
  }

  @Test
  fun hintThenCorrectAnswer_awardsHalfPointWithoutAdvancingHintStreak() {
    val viewModel =
      viewModel(
        PersistedAppState(
          hintCount = 5.0,
        ),
      )
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 6)

    repeat(4) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    viewModel.onUseHint()
    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()
    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()

    val state = viewModel.uiState.value
    val player = state.quiz.players.first()
    assertEquals(AppScreen.Results, state.screen)
    assertEquals(11, player.score)
    assertEquals(1, player.earnedHintPoints)
    assertEquals(5.25, state.hintCount)
    assertEquals(5, player.correctStreak)
    assertEquals(4, state.quiz.results[4].hintStreak)
    assertEquals(1, state.quiz.results[4].hintUses)
  }

  @Test
  fun revealGivesZeroPointsAndResetsHintStreak() {
    val viewModel =
      viewModel(
        PersistedAppState(
          hintCount = 5.0,
        ),
      )
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 6)

    repeat(4) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    viewModel.onUseHint()
    viewModel.onUseHint()
    viewModel.onUseHint()
    viewModel.onNextQuestion()
    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()

    val state = viewModel.uiState.value
    val player = state.quiz.players.first()
    assertEquals(AppScreen.Results, state.screen)
    assertEquals(10, player.score)
    assertEquals(0, player.earnedHintPoints)
    assertEquals(3.0, state.hintCount)
    assertEquals(1, player.correctStreak)
    assertEquals(0, state.quiz.results[4].hintStreak)
    assertEquals(3, state.quiz.results[4].hintUses)
    assertEquals(true, state.quiz.results[4].revealed)
  }

  @Test
  fun rookieDifficultyAwardsHintForEveryThreeCorrectAnswersAfterQuizEnds() {
    val viewModel = viewModel()

    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 3)

    repeat(3) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(1.0, viewModel.uiState.value.hintCount)
  }

  @Test
  fun trainingMode_doesNotProgressLevelCounters() {
    val viewModel = viewModel()
    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 10)

    repeat(10) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val progress = viewModel.uiState.value.levelProgress
    assertEquals(0, progress.hintsTowardNextLevel)
    assertEquals(0, progress.correctAnswersTowardNextLevel)
    assertEquals(0, progress.eligibleQuizzesTowardNextLevel)
  }

  @Test
  fun perfectQuizWithTenQuestions_awardsBronzeMedal() {
    val viewModel = viewModel()

    completePerfectQuiz(viewModel, questionCount = 10)

    val ratings = viewModel.uiState.value.ratings
    assertEquals(1, ratings.bronzeCount)
    assertEquals(0, ratings.silverCount)
    assertEquals(0, ratings.goldCount)
    assertEquals(0, ratings.titaniumCount)
    assertEquals(0, ratings.diamondCount)
  }

  @Test
  fun bronzeCollectorUnlocksAtFiftyBronzeMedals() {
    val viewModel =
      viewModel(
        PersistedAppState(
          ratings = RatingsProgress(bronzeCount = 49),
        ),
      )

    completePerfectQuiz(viewModel, questionCount = 10)

    assertEquals(50, viewModel.uiState.value.ratings.bronzeCount)
    assertTrue(viewModel.uiState.value.achievements.isUnlocked(AchievementId.BronzeCollector))
  }

  @Test
  fun resetAchievementsAndMedalsClearsOnlyThoseTestingCounters() {
    val viewModel =
      viewModel(
        PersistedAppState(
          hintCount = 7.0,
          ratings = RatingsProgress(bronzeCount = 49),
          achievements =
            com.example.flaggameandroid.core.model.AchievementsProgress().unlock(
              AchievementId.BronzeCollector,
              123L,
            ),
        ),
      )
    completePerfectQuiz(viewModel, questionCount = 10)

    viewModel.onResetAchievementsAndMedalsClicked()

    val state = viewModel.uiState.value
    assertEquals(0, state.ratings.bronzeCount)
    assertTrue(state.achievements.isUnlocked(AchievementId.BronzeCollector))
    assertTrue(state.hintCount >= 7)
  }

  @Test
  fun perfectQuizWithTwentyFiveQuestions_awardsSilverMedal() {
    val viewModel = viewModel()

    completePerfectQuiz(viewModel, questionCount = 25)

    val ratings = viewModel.uiState.value.ratings
    assertEquals(0, ratings.bronzeCount)
    assertEquals(1, ratings.silverCount)
  }

  @Test
  fun perfectQuizWithFiftyQuestions_awardsGoldMedal() {
    val viewModel = viewModel()

    completePerfectQuiz(viewModel, questionCount = 50)

    val ratings = viewModel.uiState.value.ratings
    assertEquals(1, ratings.goldCount)
    assertEquals(0, ratings.titaniumCount)
  }

  @Test
  fun perfectQuizWithOneHundredQuestions_awardsTitaniumMedal() {
    val viewModel = viewModel()

    completePerfectQuiz(viewModel, questionCount = 100)

    val ratings = viewModel.uiState.value.ratings
    assertEquals(1, ratings.titaniumCount)
    assertEquals(0, ratings.diamondCount)
  }

  @Test
  fun perfectQuizWithLessThanTenQuestions_awardsNoMedal() {
    val viewModel = viewModel()

    completePerfectQuiz(viewModel, questionCount = 9)

    val ratings = viewModel.uiState.value.ratings
    assertEquals(0, ratings.bronzeCount)
    assertEquals(0, ratings.silverCount)
    assertEquals(0, ratings.goldCount)
    assertEquals(0, ratings.titaniumCount)
    assertEquals(0, ratings.diamondCount)
  }

  @Test
  fun perfectAllCountriesQuiz_awardsDiamondMedalAndAchievement() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.CreateQuiz)
    viewModel.onCreateQuizSourceSelected(CreateQuizSource.ManualCountriesCapitals)
    viewModel.onCreateQuizManualHardcoreToggled()
    viewModel.onStartQuiz()

    repeat(viewModel.uiState.value.quiz.totalQuestions) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    val state = viewModel.uiState.value
    assertEquals(195, state.quiz.totalQuestions)
    assertEquals(1, state.ratings.diamondCount)
    assertTrue(state.achievements.isUnlocked(AchievementId.DiamondWorld))
    assertTrue(state.achievements.isUnlocked(AchievementId.DiamondCollector))
  }

  @Test
  fun perfectSingleContinentWithoutHints_unlocksContinentAchievement() {
    val viewModel = viewModel()
    val europeCount = StaticFlagCatalogRepository().getCountries().count { it.continent == "Europe" }

    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)
    viewModel.onQuestionCountChanged(europeCount)
    viewModel.onStartQuiz()

    repeat(europeCount) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    assertTrue(viewModel.uiState.value.achievements.isUnlocked(AchievementId.EuropePerfect))
  }

  @Test
  fun perfectSingleContinentWithHint_doesNotUnlockContinentAchievement() {
    val viewModel = viewModel()
    val europeCount = StaticFlagCatalogRepository().getCountries().count { it.continent == "Europe" }

    viewModel.onAddTestingHintsClicked()
    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)
    viewModel.onQuestionCountChanged(europeCount)
    viewModel.onStartQuiz()

    viewModel.onUseHint()
    repeat(europeCount) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }

    assertFalse(viewModel.uiState.value.achievements.isUnlocked(AchievementId.EuropePerfect))
  }

  @Test
  fun localMultiplayerCanSpendExistingHintsButDoesNotCollectNewHints() {
    val viewModel = viewModel()

    viewModel.onTestingToolsVisibleChanged(true)
    viewModel.onAddTestingHintsClicked()
    viewModel.onModeSelected(GameMode.LocalMultiplayer)
    viewModel.onQuestionCountChanged(2)
    viewModel.onStartQuiz()

    assertEquals(10.0, viewModel.uiState.value.quiz.currentPlayer.hintPoints)
    viewModel.onUseHint()
    assertEquals(9.25, viewModel.uiState.value.hintCount)

    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()
    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(9.25, viewModel.uiState.value.hintCount)
    assertEquals(0, viewModel.uiState.value.quiz.players.sumOf { it.earnedHintPoints })
  }

  @Test
  fun settingsCanResetAndAddTestingHints() {
    val viewModel = viewModel()

    viewModel.onAddTestingHintsClicked()
    assertEquals(10.0, viewModel.uiState.value.hintCount)

    viewModel.onResetHintsClicked()
    assertEquals(0.0, viewModel.uiState.value.hintCount)
  }

  @Test
  fun testingHintsDoNotAdvanceLevelProgress() {
    val viewModel = viewModel()

    viewModel.onAddTestingHintsClicked()

    val progress = viewModel.uiState.value.levelProgress
    assertEquals(1, progress.level)
    assertEquals(0, progress.hintsTowardNextLevel)
    assertEquals(0, progress.correctAnswersTowardNextLevel)
    assertEquals(0, progress.eligibleQuizzesTowardNextLevel)
  }

  @Test
  fun testingLevelButtonsChangeOnlyLevelAndKeepProgressCounters() {
    val viewModel =
      viewModel(
        PersistedAppState(
          hintCount = 22.0,
          level = 4,
          hintsTowardNextLevel = 7,
          correctAnswersTowardNextLevel = 88,
          eligibleQuizzesTowardNextLevel = 6,
        ),
      )

    viewModel.onTestingLevelUpClicked()

    var progress = viewModel.uiState.value.levelProgress
    assertEquals(5, progress.level)
    assertEquals(7, progress.hintsTowardNextLevel)
    assertEquals(88, progress.correctAnswersTowardNextLevel)
    assertEquals(6, progress.eligibleQuizzesTowardNextLevel)
    assertEquals(27.0, viewModel.uiState.value.hintCount)

    viewModel.onTestingResetLevelClicked()

    progress = viewModel.uiState.value.levelProgress
    assertEquals(1, progress.level)
    assertEquals(7, progress.hintsTowardNextLevel)
    assertEquals(88, progress.correctAnswersTowardNextLevel)
    assertEquals(6, progress.eligibleQuizzesTowardNextLevel)
    assertEquals(27.0, viewModel.uiState.value.hintCount)
  }

  @Test
  fun testingAchievementButtonsUnlockOneAndThenLockAllAchievements() {
    val viewModel = viewModel()

    viewModel.onUnlockRandomAchievementClicked()
    assertEquals(1, viewModel.uiState.value.achievements.unlockedAtEpochMillisById.size)

    viewModel.onLockAllAchievementsClicked()
    assertTrue(viewModel.uiState.value.achievements.unlockedAtEpochMillisById.isEmpty())
  }

  @Test
  fun levelProgress_requiresTenHintsHundredCorrectAnswersAndTenEligibleQuizzesForLevelTwo() {
    val viewModel = viewModel()
    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)

    repeat(9) {
      completePerfectContinentsQuiz(viewModel, questionCount = 10)
    }

    assertEquals(1, viewModel.uiState.value.levelProgress.level)
    assertEquals(9, viewModel.uiState.value.levelProgress.eligibleQuizzesTowardNextLevel)

    completePerfectContinentsQuiz(viewModel, questionCount = 10)

    val state = viewModel.uiState.value
    assertEquals(2, state.levelProgress.level)
    assertTrue(state.levelProgress.levelUpVisible)
    assertEquals(35.0, state.hintCount)
    assertEquals(0, state.levelProgress.eligibleQuizzesTowardNextLevel)
    assertEquals(0, state.levelProgress.hintsTowardNextLevel)
    assertEquals(0, state.levelProgress.correctAnswersTowardNextLevel)
  }

  @Test
  fun levelUpAddsFiveFreeHintsToTheCollectedCount() {
    val viewModel = viewModel()
    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)

    repeat(9) {
      completePerfectContinentsQuiz(viewModel, questionCount = 10)
    }

    assertEquals(27.0, viewModel.uiState.value.hintCount)

    completePerfectContinentsQuiz(viewModel, questionCount = 10)

    assertEquals(35.0, viewModel.uiState.value.hintCount)
    assertEquals(2, viewModel.uiState.value.levelProgress.level)
  }

  @Test
  fun levelProgress_usesHigherRequirementsAfterLevelTwo() {
    val viewModel = viewModel()
    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)

    repeat(10) {
      completePerfectContinentsQuiz(viewModel, questionCount = 10)
    }

    assertEquals(2, viewModel.uiState.value.levelProgress.level)

    repeat(14) {
      completePerfectContinentsQuiz(viewModel, questionCount = 10)
    }

    val beforeFinalRun = viewModel.uiState.value.levelProgress
    assertEquals(2, beforeFinalRun.level)
    assertEquals(14, beforeFinalRun.eligibleQuizzesTowardNextLevel)
    assertEquals(15, beforeFinalRun.hintsTowardNextLevel)
    assertEquals(140, beforeFinalRun.correctAnswersTowardNextLevel)

    completePerfectContinentsQuiz(viewModel, questionCount = 10)

    val finalProgress = viewModel.uiState.value.levelProgress
    assertEquals(3, finalProgress.level)
    assertEquals(85.0, viewModel.uiState.value.hintCount)
    assertEquals(0, finalProgress.hintsTowardNextLevel)
    assertEquals(0, finalProgress.correctAnswersTowardNextLevel)
    assertEquals(0, finalProgress.eligibleQuizzesTowardNextLevel)
  }

  @Test
  fun levelUpSeen_hidesLevelUpCelebration() {
    val viewModel = viewModel()
    viewModel.onHintDifficultySelected(HintDifficulty.Rookie)

    repeat(10) {
      completePerfectContinentsQuiz(viewModel, questionCount = 10)
    }

    assertTrue(viewModel.uiState.value.levelProgress.levelUpVisible)
    viewModel.onLevelUpSeen()
    assertEquals(false, viewModel.uiState.value.levelProgress.levelUpVisible)
  }

  @Test
  fun continentSelection_updatesQuestionCountLimitAndRejectsTooManyQuestions() {
    val viewModel = viewModel()
    val europeCount = StaticFlagCatalogRepository().getCountries().count { it.continent == "Europe" }

    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)

    assertEquals(europeCount, viewModel.uiState.value.questionCountLimit)

    viewModel.onQuestionCountChanged(europeCount + 1)
    viewModel.onStartQuiz()

    assertEquals(AppScreen.Setup, viewModel.uiState.value.screen)
    assertEquals("Question count must be between 1 and $europeCount.", viewModel.uiState.value.setupError)
  }

  @Test
  fun surpriseMe_usesSelectedCountryPoolRange() {
    val viewModel = viewModel()
    val europeCount = StaticFlagCatalogRepository().getCountries().count { it.continent == "Europe" }

    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)
    viewModel.onSurpriseMeClicked()
    viewModel.onStartQuiz()

    val totalQuestions = viewModel.uiState.value.quiz.totalQuestions
    assertTrue(totalQuestions in 1..europeCount)
  }

  @Test
  fun continentsQuizWithTenOrMoreQuestions_countsAsOneLevelTest() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)
    viewModel.onQuestionCountChanged(10)
    viewModel.onStartQuiz()

    repeat(10) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }
    if (viewModel.uiState.value.quiz.canFinish) {
      viewModel.onFinishQuiz()
    }

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(0, viewModel.uiState.value.levelProgress.eligibleQuizzesTowardNextLevel)
  }

  @Test
  fun rightArrowSkippingQuestion_marksSkippedAndMovesForward() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 2)

    viewModel.onNextQuestionPreview()

    val state = viewModel.uiState.value
    assertEquals(0, state.quiz.players.first().score)
    assertEquals(1, state.quiz.currentQuestionIndex)
    assertTrue(state.quiz.questionStates.first().status == QuestionStatus.Skipped)
    assertTrue(state.quiz.results.isEmpty())
  }

  @Test
  fun rightArrowSkippingTypedQuestion_marksSkippedAndCanUnskip() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.TypeCountryName, count = 2)

    viewModel.onNextQuestionPreview()

    var state = viewModel.uiState.value
    assertEquals(1, state.quiz.currentQuestionIndex)
    assertTrue(state.quiz.questionStates.first().status == QuestionStatus.Skipped)

    viewModel.onUnskipQuestion()

    state = viewModel.uiState.value
    assertEquals(0, state.quiz.currentQuestionIndex)
    assertTrue(state.quiz.questionStates.first().status == QuestionStatus.Skipped)
  }

  @Test
  fun rightArrowSkippingLastTypedQuestion_marksSkipped() {
    val viewModel = viewModel()
    startSingleVariantQuiz(viewModel, QuizVariant.TypeCountryName, count = 2)

    viewModel.onTypedAnswerChanged(viewModel.uiState.value.quiz.currentQuestion!!.correctCountry.name)
    viewModel.onNextQuestionPreview()
    viewModel.onNextQuestionPreview()

    val state = viewModel.uiState.value
    assertEquals(1, state.quiz.currentQuestionIndex)
    assertTrue(state.quiz.questionStates[1].status == QuestionStatus.Skipped)
  }

  @Test
  fun multiplayer_rotatesTurnsAndKeepsSeparateScores() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.LocalMultiplayer)
    viewModel.onQuestionCountChanged(4)
    viewModel.onStartQuiz()

    assertEquals("Player 1", viewModel.uiState.value.quiz.currentPlayer.name)
    answerCurrentCorrectly(viewModel)
    viewModel.onNextQuestion()

    assertEquals("Player 2", viewModel.uiState.value.quiz.currentPlayer.name)
    answerCurrentWrongly(viewModel)
    viewModel.onNextQuestion()

    val players = viewModel.uiState.value.quiz.players
    assertEquals(2, players.first { it.name == "Player 1" }.score)
    assertEquals(0, players.first { it.name == "Player 2" }.score)
    assertEquals("Player 1", viewModel.uiState.value.quiz.currentPlayer.name)
  }

  @Test
  fun typedAnswer_acceptsAliasCaseAndWhitespace() {
    val viewModel = viewModel()

    viewModel.onModeSelected(GameMode.Training)
    QuizVariant.entries.filterNot { it == QuizVariant.TypeCountryName }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(1)
    viewModel.onStartQuiz()

    val country = viewModel.uiState.value.quiz.currentQuestion!!.correctCountry
    val answer = country.aliases.firstOrNull() ?: country.name
    viewModel.onTypedAnswerChanged("  ${answer.uppercase()}  ")
    viewModel.onNextQuestion()

    assertTrue(viewModel.uiState.value.quiz.results.first().isCorrect)
  }

  @Test
  fun togglingFavoriteCountry_updatesPracticeStatsAndPersists() {
    val progressStore = RecordingProgressStore()
    val viewModel = viewModel(PersistedAppState(), progressStore)

    viewModel.onToggleFavoriteCountry("DE")

    val state = viewModel.uiState.value
    assertTrue(state.countryPracticeStats["DE"]?.favorite == true)
    assertTrue(progressStore.savedProgressSnapshots.last().countryPracticeStats["DE"]?.favorite == true)
  }

  @Test
  fun finishQuiz_completesLastQuestionEvenBeforeNextIsPressed() {
    val viewModel = viewModel()

    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = 1)
    val question = viewModel.uiState.value.quiz.currentQuestion!!
    viewModel.onCountryAnswerSelected(question.correctCountry)

    assertTrue(viewModel.uiState.value.quiz.canFinish)

    viewModel.onFinishQuiz()

    assertEquals(AppScreen.Results, viewModel.uiState.value.screen)
    assertEquals(1, viewModel.uiState.value.quiz.results.size)
    assertTrue(viewModel.uiState.value.quiz.results.first().isCorrect)
  }

  private fun startSingleVariantQuiz(
    viewModel: FlagGameViewModel,
    variant: QuizVariant,
    count: Int,
  ) {
    viewModel.onModeSelected(GameMode.Training)
    QuizVariant.entries.filterNot { it == variant }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(count)
    viewModel.onStartQuiz()
  }

  private fun completePerfectQuiz(
    viewModel: FlagGameViewModel,
    questionCount: Int,
  ) {
    startSingleVariantQuiz(viewModel, QuizVariant.FlagToCountry, count = questionCount)
    repeat(questionCount) {
      answerCurrentCorrectly(viewModel)
      viewModel.onNextQuestion()
    }
  }

  private fun completePerfectContinentsQuiz(
    viewModel: FlagGameViewModel,
    questionCount: Int,
  ) {
    viewModel.onModeSelected(GameMode.WorldFlags)
    viewModel.uiState.value.setup.selectedContinents
      .filterNot { it == "Europe" }
      .forEach(viewModel::onContinentToggled)
    QuizVariant.entries.filterNot { it == QuizVariant.FlagToCountry }.forEach(viewModel::onVariantToggled)
    viewModel.onQuestionCountChanged(questionCount)
    viewModel.onStartQuiz()
    repeat(questionCount) { index ->
      answerCurrentCorrectly(viewModel)
      if (index == questionCount - 1) {
        viewModel.onFinishQuiz()
        if (viewModel.uiState.value.screen != AppScreen.Results) {
          viewModel.onNextQuestion()
          viewModel.onFinishQuiz()
        }
      } else {
        viewModel.onNextQuestion()
      }
    }
  }

  private fun answerCurrentCorrectly(viewModel: FlagGameViewModel) {
    val question = viewModel.uiState.value.quiz.currentQuestion!!
    when (question.variant) {
      QuizVariant.TypeText -> viewModel.onTypedAnswerChanged(question.correctCountry.name)
      QuizVariant.FlagToText,
      QuizVariant.TextToFlag -> viewModel.onCountryAnswerSelected(question.correctCountry)
    }
  }

  private fun answerCurrentWrongly(viewModel: FlagGameViewModel) {
    val question = viewModel.uiState.value.quiz.currentQuestion!!
    when (question.variant) {
      QuizVariant.TypeText -> viewModel.onTypedAnswerChanged("wrong answer")
      QuizVariant.FlagToText,
      QuizVariant.TextToFlag -> viewModel.onCountryAnswerSelected(question.options.first { it.code != question.correctCountry.code })
    }
  }

  private class RecordingProgressStore : ProgressStore {
    val savedProgressSnapshots = mutableListOf<PersistedAppState>()
    val recordedHistories = mutableListOf<PersistedQuizHistory>()

    override suspend fun loadProgress(): PersistedAppState = PersistedAppState()

    override suspend fun saveProgress(progress: PersistedAppState) {
      savedProgressSnapshots += progress
    }

    override suspend fun recordQuiz(history: PersistedQuizHistory) {
      recordedHistories += history
    }
  }
}

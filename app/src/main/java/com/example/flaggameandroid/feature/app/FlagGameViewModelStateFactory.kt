package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.persistence.PersistedAppState

internal fun buildInitialUiState(
  initialPersistedState: PersistedAppState,
  allContinents: List<String>,
  selectableContinents: List<String>,
  countries: List<FlagCountry>,
  nowEpochMillis: Long = System.currentTimeMillis(),
): FlagGameUiState =
  FlagGameUiState(
    quizReturnTarget = AppScreen.GameModes,
    settings =
      SettingsState(
        hintDifficulty = initialPersistedState.hintDifficulty,
        reminderEnabled = initialPersistedState.reminderEnabled,
        language = initialPersistedState.language,
      ),
    availableContinents = allContinents,
    setup = SetupState(selectedContinents = selectableContinents.toSet()),
    questionCountLimit = countries.size,
    profile =
      ProfileState(
        accountName = initialPersistedState.accountName,
        avatarIndex = initialPersistedState.avatarIndex.coerceIn(0, ProgressionRules.TotalAvatarCount - 1),
      ),
    countries = countries,
    hintCount = initialPersistedState.hintCount,
    ratings = initialPersistedState.ratings,
    achievements = initialPersistedState.achievements,
    mistakeReviewUnlocked =
      initialPersistedState.mistakeReviewUnlocked ||
        mistakeReviewEligibleCountryCount(initialPersistedState.countryPracticeStats) >= com.example.flaggameandroid.core.model.MistakeReviewUnlockCountryCount,
    levelProgress =
      LevelProgressState(
        level = initialPersistedState.level,
        hintsTowardNextLevel = initialPersistedState.hintsTowardNextLevel,
        correctAnswersTowardNextLevel = initialPersistedState.correctAnswersTowardNextLevel,
        eligibleQuizzesTowardNextLevel = initialPersistedState.eligibleQuizzesTowardNextLevel,
      ),
    lastOpenedAtEpochMillis = initialPersistedState.lastOpenedAtEpochMillis,
    lastPlayedAtEpochMillis = initialPersistedState.lastPlayedAtEpochMillis,
    inactiveIconActive = initialPersistedState.inactiveIconActive,
    countryPracticeStats = initialPersistedState.countryPracticeStats,
    activityCalendar = initialPersistedState.activityCalendar,
    dailyChallengeCache =
      buildDailyChallengeCache(
        countries = countries,
        dailyChallengeCache = initialPersistedState.dailyChallengeCache,
        nowEpochMillis = nowEpochMillis,
      ),
    savedQuizTemplates = initialPersistedState.savedQuizTemplates,
  )

internal fun FlagGameUiState.resetToMenu(
  allContinents: List<String>,
  selectableContinents: List<String>,
  questionCountLimit: Int,
): FlagGameUiState =
  copy(
    screen = AppScreen.Menu,
    quizReturnTarget = AppScreen.GameModes,
    availableContinents = allContinents,
    setup = SetupState(selectedContinents = selectableContinents.toSet()),
    questionCountLimit = questionCountLimit,
    setupError = null,
  )

internal fun FlagGameUiState.toPersistedAppState(): PersistedAppState =
  PersistedAppState(
    hintDifficulty = settings.hintDifficulty,
    reminderEnabled = settings.reminderEnabled,
    language = settings.language,
    accountName = profile.accountName,
    avatarIndex = profile.avatarIndex,
    hintCount = hintCount,
    ratings = ratings,
    achievements = achievements,
    level = levelProgress.level,
    hintsTowardNextLevel = levelProgress.hintsTowardNextLevel,
    correctAnswersTowardNextLevel = levelProgress.correctAnswersTowardNextLevel,
    eligibleQuizzesTowardNextLevel = levelProgress.eligibleQuizzesTowardNextLevel,
    lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
    lastPlayedAtEpochMillis = lastPlayedAtEpochMillis,
    inactiveIconActive = inactiveIconActive,
    countryPracticeStats = countryPracticeStats,
    activityCalendar = activityCalendar,
    dailyChallengeCache = dailyChallengeCache,
    savedQuizTemplates = savedQuizTemplates,
    mistakeReviewUnlocked = mistakeReviewUnlocked,
  )

internal fun buildSetupForMode(
  mode: GameMode,
  selectableContinents: List<String>,
  countries: List<FlagCountry>,
  displayName: String,
): SetupState =
  SetupState(
    mode = mode,
    instantCorrectionEnabled = mode == GameMode.Training,
    selectedContinents =
      when (mode) {
        GameMode.WorldFlags,
        GameMode.LocalMultiplayer ->
          selectableContinents.toSet()
        GameMode.CreateQuiz ->
          emptySet()
        GameMode.DailyChallenge,
        GameMode.MistakeReview,
        GameMode.Training -> emptySet()
      },
    questionCountInput =
      when (mode) {
        GameMode.DailyChallenge -> "10"
        GameMode.MistakeReview -> "10"
        GameMode.CreateQuiz -> "10"
        GameMode.WorldFlags -> "10"
        GameMode.LocalMultiplayer -> "10"
        else -> "10"
      },
    speedRunSecondsPerAnswerInput = "5",
    worldFlagsHardcoreEnabled = false,
    worldFlagsTimerEnabled = false,
    playerNames = listOf(displayName, "Player 2"),
  )

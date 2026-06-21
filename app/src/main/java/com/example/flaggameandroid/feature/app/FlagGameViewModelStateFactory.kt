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
): FlagGameUiState =
  FlagGameUiState(
    settings =
      SettingsState(
        hintDifficulty = initialPersistedState.hintDifficulty,
        reminderEnabled = initialPersistedState.reminderEnabled,
        language = initialPersistedState.language,
        timeZone = initialPersistedState.timeZone,
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
    dailyChallengeCache = initialPersistedState.dailyChallengeCache,
  )

internal fun FlagGameUiState.resetToMenu(
  allContinents: List<String>,
  selectableContinents: List<String>,
  questionCountLimit: Int,
): FlagGameUiState =
  copy(
    screen = AppScreen.Menu,
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
    timeZone = settings.timeZone,
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
    selectedContinents =
      when (mode) {
        GameMode.Continents,
        GameMode.SpeedRun,
        GameMode.LocalMultiplayer ->
          selectableContinents.toSet()
        GameMode.AllIn ->
          selectableContinents.toSet()
        GameMode.DailyChallenge,
        GameMode.MistakeReview,
        GameMode.Training -> emptySet()
      },
    questionCountInput =
      when (mode) {
        GameMode.AllIn -> countries.size.toString()
        GameMode.DailyChallenge -> "10"
        GameMode.MistakeReview -> "10"
        else -> "10"
      },
    playerNames = listOf(displayName, "Player 2"),
  )

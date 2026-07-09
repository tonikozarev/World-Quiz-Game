package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuizTopic
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
        mistakeReviewEligibleCountryCount(initialPersistedState.countryPracticeStats, QuizTopic.Mixed) >= com.example.flaggameandroid.core.model.MistakeReviewUnlockCountryCount,
    levelProgress =
      LevelProgressState(
        level = initialPersistedState.level,
        hintsTowardNextLevel = initialPersistedState.hintsTowardNextLevel,
        correctAnswersTowardNextLevel = initialPersistedState.correctAnswersTowardNextLevel,
        eligibleQuizzesTowardNextLevel = initialPersistedState.eligibleQuizzesTowardNextLevel,
      ),
    lastOpenedAtEpochMillis = initialPersistedState.lastOpenedAtEpochMillis,
    lastPlayedAtEpochMillis = initialPersistedState.lastPlayedAtEpochMillis,
    countryPracticeStats = initialPersistedState.countryPracticeStats,
    activityCalendar = initialPersistedState.activityCalendar,
    dailyChallengeCaches =
      initialPersistedState.dailyChallengeCaches +
        (QuizTopic.Countries to
          buildDailyChallengeCache(
            countries = countries,
            topic = QuizTopic.Countries,
            dailyChallengeCache = initialPersistedState.dailyChallengeCaches[QuizTopic.Countries],
            nowEpochMillis = nowEpochMillis,
          )) +
        (QuizTopic.Mixed to
          buildDailyChallengeCache(
            countries = countries,
            topic = QuizTopic.Mixed,
            dailyChallengeCache = initialPersistedState.dailyChallengeCaches[QuizTopic.Mixed]
              ?: initialPersistedState.dailyChallengeCaches[QuizTopic.Countries],
            nowEpochMillis = nowEpochMillis,
          )),
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
    selectedQuizTopic = QuizTopic.Countries,
    availableContinents = allContinents,
    setup = SetupState(selectedContinents = selectableContinents.toSet()),
    questionCountLimit = questionCountLimit,
    setupError = null,
  )

internal fun FlagGameUiState.toPersistedAppState(): PersistedAppState =
  PersistedAppState(
    hintDifficulty = settings.hintDifficulty,
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
    countryPracticeStats = countryPracticeStats,
    activityCalendar = activityCalendar,
    dailyChallengeCaches = dailyChallengeCaches,
    savedQuizTemplates = savedQuizTemplates,
    mistakeReviewUnlocked = mistakeReviewUnlocked,
  )

internal fun buildSetupForMode(
  mode: GameMode,
  topic: QuizTopic,
  selectableContinents: List<String>,
  countries: List<FlagCountry>,
  displayName: String,
): SetupState =
  SetupState(
    mode = mode,
    topic =
      when (mode) {
        GameMode.DailyChallenge -> QuizTopic.Mixed
        else -> topic
      },
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
        GameMode.CreateQuiz -> if (topic == QuizTopic.Mixed) "0" else "10"
        GameMode.WorldFlags -> "10"
        GameMode.LocalMultiplayer -> "10"
        else -> "10"
      },
    speedRunSecondsPerAnswerInput = "5",
    worldFlagsHardcoreEnabled = false,
    worldFlagsTimerEnabled = false,
    createQuizPreset =
      when (topic) {
        QuizTopic.Capitals -> CreateQuizPreset.CapitalPopulationUnderQuarterMillion
        QuizTopic.Countries,
        QuizTopic.Mixed -> CreateQuizPreset.TwoColors
      },
    createQuizSource =
      when (topic) {
        QuizTopic.Mixed -> CreateQuizSource.ManualCountriesCapitals
        else -> CreateQuizSource.PresetFilter
      },
    createQuizPresets = createQuizDefaultPresetsForTopic(topic),
    playerNames = listOf(displayName, "Player 2"),
  )

internal fun buildSetupForMode(
  mode: GameMode,
  selectableContinents: List<String>,
  countries: List<FlagCountry>,
  displayName: String,
): SetupState =
  buildSetupForMode(
    mode = mode,
    topic = QuizTopic.Countries,
    selectableContinents = selectableContinents,
    countries = countries,
    displayName = displayName,
  )

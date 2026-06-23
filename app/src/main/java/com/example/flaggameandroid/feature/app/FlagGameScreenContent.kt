package com.example.flaggameandroid.feature.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizVariant

@Composable
internal fun FlagGameScreenContent(
  uiState: FlagGameUiState,
  onStartClicked: () -> Unit,
  onMedalsClicked: () -> Unit,
  onAchievementsClicked: () -> Unit,
  onFavoritesClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onGameModesClicked: () -> Unit,
  onQuitClicked: () -> Unit,
  onLevelUpSeen: () -> Unit,
  onAccountNameChanged: (String) -> Unit,
  onAvatarSelected: (Int) -> Unit,
  onModeSelected: (GameMode) -> Unit,
  onBackToMenu: () -> Unit,
  onBackToGameModes: () -> Unit,
  onRefreshDailyChallengeAvailability: () -> Unit,
  onHintDifficultySelected: (HintDifficulty) -> Unit,
  onLanguageSelected: (AppLanguage) -> Unit,
  onReminderEnabledChanged: (Boolean) -> Unit,
  onResetHintsClick: () -> Unit,
  onAddTestingHintsClick: () -> Unit,
  onTestingLevelUpClick: () -> Unit,
  onTestingResetLevelClick: () -> Unit,
  onUnlockRandomAchievementClick: () -> Unit,
  onLockAllAchievementsClick: () -> Unit,
  onResetAchievementsAndMedalsClick: () -> Unit,
  onResetDailyChallengeClick: () -> Unit,
  onToggleTestingIconClick: () -> Unit,
  onTriggerTestingReminderClick: () -> Unit,
  onVariantToggled: (QuizVariant) -> Unit,
  onContinentToggled: (String) -> Unit,
  onQuestionCountChanged: (String) -> Unit,
  onSpeedRunSecondsChanged: (String) -> Unit,
  onSurpriseMeClicked: () -> Unit,
  onAllInTypeSelected: (AllInType) -> Unit,
  onMultiplayerBaseSelected: (MultiplayerQuizBase) -> Unit,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
  onStartQuiz: () -> Unit,
  onCreateQuizSourceSelected: (com.example.flaggameandroid.core.model.CreateQuizSource) -> Unit,
  onCreateQuizPresetSelected: (com.example.flaggameandroid.core.model.CreateQuizPreset) -> Unit,
  onCreateQuizCountryToggled: (String) -> Unit,
  onSaveCreateQuizClicked: (String, String?) -> FlagGameViewModel.SaveQuizResult,
  onCountryAnswerSelected: (FlagCountry) -> Unit,
  onTypedAnswerChanged: (String) -> Unit,
  onVerifyTypedAnswer: () -> Unit,
  onUseHint: () -> Unit,
  onPreviousQuestion: () -> Unit,
  onNextQuestionPreview: () -> Unit,
  onUnskipQuestion: () -> Unit,
  onFinishQuiz: () -> Unit,
  onSpeedRunTimeExpired: () -> Unit,
  onPlayAgain: () -> Unit,
  onBackToMenuClick: () -> Unit,
  onBackToGameModesClick: () -> Unit,
  onQuestionBack: () -> Unit,
  onQuestionForward: () -> Unit,
  onToggleFavoriteCountry: (String) -> Unit,
  onOpenSavedQuizTemplate: (String) -> Unit,
  onRemoveSavedQuizTemplate: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState.screen) {
    AppScreen.Menu ->
      MenuScreen(
        levelProgress = uiState.levelProgress,
        profile = uiState.profile,
        language = uiState.settings.language,
        activityCalendar = uiState.activityCalendar,
        countryPracticeStats = uiState.countryPracticeStats,
        onStartClick = onStartClicked,
        onMedalsClick = onMedalsClicked,
        onAchievementsClick = onAchievementsClicked,
        onFavoritesClick = onFavoritesClicked,
        onSettingsClick = onSettingsClicked,
        onQuitClick = onQuitClicked,
        onLevelUpSeen = onLevelUpSeen,
        onAccountNameChanged = onAccountNameChanged,
        onAvatarSelected = onAvatarSelected,
        modifier = modifier,
      )
    AppScreen.Medals ->
      MedalsScreen(
        ratings = uiState.ratings,
        language = uiState.settings.language,
        onBack = onBackToMenuClick,
        modifier = modifier,
      )
    AppScreen.Achievements ->
      AchievementsScreen(
        achievements = uiState.achievements,
        language = uiState.settings.language,
        onBack = onBackToMenuClick,
        modifier = modifier,
      )
    AppScreen.Favorites ->
      FavoritesScreen(
        countries = uiState.countries,
        countryPracticeStats = uiState.countryPracticeStats,
        savedQuizTemplates = uiState.savedQuizTemplates,
        language = uiState.settings.language,
        onBack = onBackToMenuClick,
        onToggleFavoriteCountry = onToggleFavoriteCountry,
        onOpenSavedQuizTemplate = onOpenSavedQuizTemplate,
        onRemoveSavedQuizTemplate = onRemoveSavedQuizTemplate,
        modifier = modifier,
      )
    AppScreen.GameModes ->
      GameModesScreen(
        language = uiState.settings.language,
        dailyChallengeCache = uiState.dailyChallengeCache,
        mistakeReviewEligibleCount = mistakeReviewEligibleCountryCount(uiState.countryPracticeStats),
        onGameModesClick = onGameModesClicked,
        onModeSelected = onModeSelected,
        onRefreshDailyChallengeAvailability = onRefreshDailyChallengeAvailability,
        modifier = modifier,
      )
    AppScreen.GameModesHub ->
      GameModesHubScreen(
        language = uiState.settings.language,
        onModeSelected = onModeSelected,
        modifier = modifier,
      )
    AppScreen.Settings -> {
      val context = LocalContext.current
      val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
      SettingsScreen(
        settings = uiState.settings,
        hintCount = uiState.hintCount,
        inactiveIconActive = uiState.inactiveIconActive,
        onBack = onBackToMenuClick,
        onHintDifficultySelected = onHintDifficultySelected,
        onLanguageSelected = onLanguageSelected,
        onReminderEnabledChanged = { enabled ->
          onReminderEnabledChanged(enabled)
          if (enabled &&
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
          ) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
          }
        },
        onResetHintsClick = onResetHintsClick,
        onAddTestingHintsClick = onAddTestingHintsClick,
        onTestingLevelUpClick = onTestingLevelUpClick,
        onTestingResetLevelClick = onTestingResetLevelClick,
        onUnlockRandomAchievementClick = onUnlockRandomAchievementClick,
        onLockAllAchievementsClick = onLockAllAchievementsClick,
        onResetAchievementsAndMedalsClick = onResetAchievementsAndMedalsClick,
        onResetDailyChallengeClick = onResetDailyChallengeClick,
        onToggleTestingIconClick = onToggleTestingIconClick,
        onTriggerTestingReminderClick = onTriggerTestingReminderClick,
        modifier = modifier,
      )
    }
    AppScreen.Setup ->
      SetupScreen(
        setup = uiState.setup,
        hintDifficulty = uiState.settings.hintDifficulty,
        language = uiState.settings.language,
        availableContinents = uiState.availableContinents,
        countries = uiState.countries,
        questionCountLimit = uiState.questionCountLimit,
        setupError = uiState.setupError,
        onBack = onBackToGameModesClick,
        onVariantToggle = onVariantToggled,
        onContinentToggle = onContinentToggled,
        onQuestionCountChange = onQuestionCountChanged,
        onSpeedRunSecondsChange = onSpeedRunSecondsChanged,
        onSurpriseMe = onSurpriseMeClicked,
        onAllInTypeSelected = onAllInTypeSelected,
        onMultiplayerBaseSelected = onMultiplayerBaseSelected,
        onPlayerNameChanged = onPlayerNameChanged,
        onAddPlayer = onAddPlayer,
        onRemovePlayer = onRemovePlayer,
        onStartQuiz = onStartQuiz,
        onCreateQuizSourceSelected = onCreateQuizSourceSelected,
        onCreateQuizPresetSelected = onCreateQuizPresetSelected,
        onCreateQuizCountryToggled = onCreateQuizCountryToggled,
        onSaveCreateQuizClicked = onSaveCreateQuizClicked,
        modifier = modifier,
      )
    AppScreen.Quiz ->
      QuizScreen(
        quiz = uiState.quiz,
        language = uiState.settings.language,
        onLeaveQuiz = onBackToGameModesClick,
        onCountryAnswerSelected = onCountryAnswerSelected,
        onTypedAnswerChanged = onTypedAnswerChanged,
        onVerifyTypedAnswer = onVerifyTypedAnswer,
        onUseHint = onUseHint,
        onPreviousQuestion = onPreviousQuestion,
        onNextQuestionPreview = onNextQuestionPreview,
        onUnskipQuestion = onUnskipQuestion,
        onFinishQuiz = onFinishQuiz,
        onSpeedRunTimeExpired = onSpeedRunTimeExpired,
        modifier = modifier,
      )
    AppScreen.Results ->
      ResultsScreen(
        quiz = uiState.quiz,
        language = uiState.settings.language,
        levelProgress = uiState.levelProgress,
        countryPracticeStats = uiState.countryPracticeStats,
        onToggleFavoriteCountry = onToggleFavoriteCountry,
        completedAtEpochMillis = uiState.lastPlayedAtEpochMillis,
        onPlayAgain = onPlayAgain,
        onBackToMenu = onBackToGameModesClick,
        onLevelUpSeen = onLevelUpSeen,
        modifier = modifier,
      )
  }
}


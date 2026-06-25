package com.example.flaggameandroid.feature.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FlagGameRoute(
  screenViewModel: FlagGameViewModel? = null,
) {
  val activity = LocalContext.current as? Activity
  val context = LocalContext.current
  val notificationPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
  val resolvedViewModel =
    screenViewModel ?: viewModel(factory = FlagGameViewModel.factory(LocalContext.current.applicationContext))
  val uiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()
  var showExitDialog by remember { mutableStateOf(false) }

  if (showExitDialog) {
    AlertDialog(
      onDismissRequest = { showExitDialog = false },
      title = { Text(t(uiState.settings.language, UiText.ExitAppTitle)) },
      text = { Text(t(uiState.settings.language, UiText.ExitAppBody)) },
      confirmButton = {
        TextButton(onClick = { activity?.finishAndRemoveTask() }) {
          Text(t(uiState.settings.language, UiText.Exit))
        }
      },
      dismissButton = {
        TextButton(onClick = { showExitDialog = false }) {
          Text(t(uiState.settings.language, UiText.Stay))
        }
      },
    )
  }

  BackHandler(enabled = uiState.screen != AppScreen.Quiz) {
    when (uiState.screen) {
      AppScreen.Menu -> showExitDialog = true
      AppScreen.GameModes,
      AppScreen.Medals,
      AppScreen.Achievements,
      AppScreen.Favorites,
      AppScreen.Settings,
      AppScreen.Results -> resolvedViewModel.onBackToGameModes()
      AppScreen.GameModesHub -> resolvedViewModel.onBackToGameModes()
      AppScreen.Setup -> resolvedViewModel.onBackToGameModes()
      AppScreen.Quiz -> Unit
    }
  }

  FlagGameScreenContent(
    uiState = uiState,
    onStartClicked = resolvedViewModel::onStartClicked,
    onMedalsClicked = resolvedViewModel::onMedalsClicked,
    onAchievementsClicked = resolvedViewModel::onAchievementsClicked,
    onFavoritesClicked = resolvedViewModel::onFavoritesClicked,
    onSettingsClicked = resolvedViewModel::onSettingsClicked,
    onGameModesClicked = resolvedViewModel::onGameModesClicked,
    onQuitClicked = { showExitDialog = true },
    onLevelUpSeen = resolvedViewModel::onLevelUpSeen,
    onAccountNameChanged = resolvedViewModel::onAccountNameChanged,
    onAvatarSelected = resolvedViewModel::onAvatarSelected,
    onModeSelected = resolvedViewModel::onModeSelected,
    onBackToMenu = resolvedViewModel::onBackToMenu,
    onBackToGameModes = resolvedViewModel::onBackToGameModes,
    onRefreshDailyChallengeAvailability = resolvedViewModel::refreshDailyChallengeAvailability,
    onHintDifficultySelected = resolvedViewModel::onHintDifficultySelected,
    onLanguageSelected = resolvedViewModel::onLanguageSelected,
    onReminderEnabledChanged = { enabled ->
      resolvedViewModel.onReminderEnabledChanged(enabled)
      if (enabled &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
      ) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    },
    onResetHintsClick = resolvedViewModel::onResetHintsClicked,
    onAddTestingHintsClick = resolvedViewModel::onAddTestingHintsClicked,
    onTestingLevelUpClick = resolvedViewModel::onTestingLevelUpClicked,
    onTestingResetLevelClick = resolvedViewModel::onTestingResetLevelClicked,
    onUnlockRandomAchievementClick = resolvedViewModel::onUnlockRandomAchievementClicked,
    onLockAllAchievementsClick = resolvedViewModel::onLockAllAchievementsClicked,
    onResetAchievementsAndMedalsClick = resolvedViewModel::onResetAchievementsAndMedalsClicked,
    onResetDailyChallengeClick = resolvedViewModel::onResetDailyChallengeClicked,
    onToggleTestingIconClick = resolvedViewModel::onToggleTestingIconClicked,
    onTriggerTestingReminderClick = resolvedViewModel::onTriggerTestingReminderClicked,
    onVariantToggled = resolvedViewModel::onVariantToggled,
    onContinentToggled = resolvedViewModel::onContinentToggled,
    onWorldFlagsHardcoreToggled = resolvedViewModel::onWorldFlagsHardcoreToggled,
    onWorldFlagsTimerToggled = resolvedViewModel::onWorldFlagsTimerToggled,
    onQuestionCountChanged = resolvedViewModel::onQuestionCountChanged,
    onSpeedRunSecondsChanged = resolvedViewModel::onSpeedRunSecondsChanged,
    onSurpriseMeClicked = resolvedViewModel::onSurpriseMeClicked,
    onAllInTypeSelected = resolvedViewModel::onAllInTypeSelected,
    onMultiplayerBaseSelected = resolvedViewModel::onMultiplayerBaseSelected,
    onPlayerNameChanged = resolvedViewModel::onPlayerNameChanged,
    onAddPlayer = resolvedViewModel::onAddPlayer,
    onRemovePlayer = resolvedViewModel::onRemovePlayer,
    onStartQuiz = resolvedViewModel::onStartQuiz,
    onCreateQuizSourceSelected = resolvedViewModel::onCreateQuizSourceSelected,
    onCreateQuizPresetSelected = resolvedViewModel::onCreateQuizPresetSelected,
    onCreateQuizCountryToggled = resolvedViewModel::onCreateQuizCountryToggled,
    onSaveCreateQuizClicked = resolvedViewModel::onSaveCreateQuizClicked,
    onCountryAnswerSelected = resolvedViewModel::onCountryAnswerSelected,
    onTypedAnswerChanged = resolvedViewModel::onTypedAnswerChanged,
    onVerifyTypedAnswer = resolvedViewModel::onVerifyTypedAnswer,
    onUseHint = resolvedViewModel::onUseHint,
    onPreviousQuestion = resolvedViewModel::onQuestionBack,
    onNextQuestionPreview = resolvedViewModel::onQuestionForward,
    onUnskipQuestion = resolvedViewModel::onUnskipQuestion,
    onFinishQuiz = resolvedViewModel::onFinishQuiz,
    onSpeedRunTimeExpired = resolvedViewModel::onSpeedRunTimeExpired,
    onPlayAgain = resolvedViewModel::onPlayAgain,
    onBackToMenuClick = resolvedViewModel::onBackToMenu,
    onBackToGameModesClick = resolvedViewModel::onBackToGameModes,
    onQuestionBack = resolvedViewModel::onQuestionBack,
    onQuestionForward = resolvedViewModel::onQuestionForward,
    onToggleFavoriteCountry = resolvedViewModel::onToggleFavoriteCountry,
    onOpenSavedQuizTemplate = resolvedViewModel::onOpenSavedQuizTemplate,
    onRemoveSavedQuizTemplate = resolvedViewModel::onRemoveSavedQuizTemplate,
  )
}

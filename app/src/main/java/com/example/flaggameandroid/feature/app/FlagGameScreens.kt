package com.example.flaggameandroid.feature.app

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementSector
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.MedalTier
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.theme.AccentGold
import com.example.flaggameandroid.theme.AccentGreen
import com.example.flaggameandroid.theme.AccentRed
import com.example.flaggameandroid.theme.FlagGameAndroidTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val InfoSymbolText = "\uD835\uDC8A"
private const val PreviousArrowText = "\u2190"
private const val NextArrowText = "\u2192"
private const val UnskipArrowText = "\u21B7"

@Composable
fun FlagGameRoute(
  screenViewModel: FlagGameViewModel? = null,
) {
  val activity = LocalContext.current as? Activity
  val context = LocalContext.current
  val notificationPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
  val resolvedViewModel = screenViewModel ?: viewModel(factory = FlagGameViewModel.factory(LocalContext.current.applicationContext))
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
      AppScreen.Settings,
      AppScreen.Results -> resolvedViewModel.onBackToMenu()
      AppScreen.Setup -> resolvedViewModel.onBackToGameModes()
      AppScreen.Quiz -> Unit
    }
  }

  when (uiState.screen) {
    AppScreen.Menu ->
      MenuScreen(
        levelProgress = uiState.levelProgress,
        profile = uiState.profile,
        language = uiState.settings.language,
        onStartClick = resolvedViewModel::onStartClicked,
        onMedalsClick = resolvedViewModel::onMedalsClicked,
        onAchievementsClick = resolvedViewModel::onAchievementsClicked,
        onSettingsClick = resolvedViewModel::onSettingsClicked,
        onQuitClick = { showExitDialog = true },
        onLevelUpSeen = resolvedViewModel::onLevelUpSeen,
        onAccountNameChanged = resolvedViewModel::onAccountNameChanged,
        onAvatarSelected = resolvedViewModel::onAvatarSelected,
      )
    AppScreen.Medals ->
      MedalsScreen(
        ratings = uiState.ratings,
        language = uiState.settings.language,
        onBack = resolvedViewModel::onBackToMenu,
      )
    AppScreen.Achievements ->
      AchievementsScreen(
        achievements = uiState.achievements,
        language = uiState.settings.language,
        onBack = resolvedViewModel::onBackToMenu,
      )
    AppScreen.GameModes ->
      GameModesScreen(
        language = uiState.settings.language,
        onBack = resolvedViewModel::onBackToMenu,
        onModeSelected = resolvedViewModel::onModeSelected,
      )
    AppScreen.Settings ->
      SettingsScreen(
        settings = uiState.settings,
        hintCount = uiState.hintCount,
        inactiveIconActive = uiState.inactiveIconActive,
        onBack = resolvedViewModel::onBackToMenu,
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
        onToggleTestingIconClick = resolvedViewModel::onToggleTestingIconClicked,
        onTriggerTestingReminderClick = resolvedViewModel::onTriggerTestingReminderClicked,
      )
    AppScreen.Setup ->
      SetupScreen(
        setup = uiState.setup,
        hintDifficulty = uiState.settings.hintDifficulty,
        language = uiState.settings.language,
        availableContinents = uiState.availableContinents,
        questionCountLimit = uiState.questionCountLimit,
        setupError = uiState.setupError,
        onBack = resolvedViewModel::onBackToGameModes,
        onVariantToggle = resolvedViewModel::onVariantToggled,
        onContinentToggle = resolvedViewModel::onContinentToggled,
        onQuestionCountChange = resolvedViewModel::onQuestionCountChanged,
        onSurpriseMe = resolvedViewModel::onSurpriseMeClicked,
        onAllInTypeSelected = resolvedViewModel::onAllInTypeSelected,
        onMultiplayerBaseSelected = resolvedViewModel::onMultiplayerBaseSelected,
        onPlayerNameChanged = resolvedViewModel::onPlayerNameChanged,
        onAddPlayer = resolvedViewModel::onAddPlayer,
        onRemovePlayer = resolvedViewModel::onRemovePlayer,
        onStartQuiz = resolvedViewModel::onStartQuiz,
      )
    AppScreen.Quiz ->
      QuizScreen(
        quiz = uiState.quiz,
        language = uiState.settings.language,
        onLeaveQuiz = resolvedViewModel::onBackToGameModes,
        onCountryAnswerSelected = resolvedViewModel::onCountryAnswerSelected,
        onTypedAnswerChanged = resolvedViewModel::onTypedAnswerChanged,
        onVerifyTypedAnswer = resolvedViewModel::onVerifyTypedAnswer,
        onUseHint = resolvedViewModel::onUseHint,
        onPreviousQuestion = resolvedViewModel::onQuestionBack,
        onNextQuestionPreview = resolvedViewModel::onQuestionForward,
        onUnskipQuestion = resolvedViewModel::onUnskipQuestion,
        onFinishQuiz = resolvedViewModel::onFinishQuiz,
      )
    AppScreen.Results ->
      ResultsScreen(
        quiz = uiState.quiz,
        language = uiState.settings.language,
        levelProgress = uiState.levelProgress,
        onPlayAgain = resolvedViewModel::onPlayAgain,
        onBackToMenu = resolvedViewModel::onBackToMenu,
        onLevelUpSeen = resolvedViewModel::onLevelUpSeen,
      )
  }
}

@Composable
fun MenuScreen(
  levelProgress: LevelProgressState,
  profile: ProfileState,
  language: AppLanguage,
  onStartClick: () -> Unit,
  onMedalsClick: () -> Unit,
  onAchievementsClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onQuitClick: () -> Unit,
  onLevelUpSeen: () -> Unit,
  onAccountNameChanged: (String) -> Unit,
  onAvatarSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  var profileDialogVisible by remember { mutableStateOf(false) }

  if (profileDialogVisible) {
    ProfileEditorDialog(
      profile = profile,
      levelProgress = levelProgress,
      language = language,
      onDismiss = { profileDialogVisible = false },
      onSave = { name, avatarIndex ->
        onAccountNameChanged(name)
        onAvatarSelected(avatarIndex)
        profileDialogVisible = false
      },
    )
  }

  ScreenShell(modifier = modifier) {
    LevelProgressPanel(
      levelProgress = levelProgress,
      profile = profile,
      onLevelUpSeen = onLevelUpSeen,
      language = language,
      onClick = { profileDialogVisible = true },
    )

    HeroPanel(
      title = cleanText(language, UiText.WorldFlagGame),
      subtitle = cleanText(language, UiText.HeroSubtitle),
      language = language,
      onStartClick = onStartClick,
      onMedalsClick = onMedalsClick,
      onAchievementsClick = onAchievementsClick,
      onSettingsClick = onSettingsClick,
      onQuitClick = onQuitClick,
    )
  }
}

@Composable
fun GameModesScreen(
  language: AppLanguage,
  onBack: () -> Unit,
  onModeSelected: (GameMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expandedInfoMode by remember { mutableStateOf<GameMode?>(null) }

  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanModeSelectionTitle(language))

    GameMode.entries.forEach { mode ->
      ModeCard(
        mode = mode,
        language = language,
        infoExpanded = expandedInfoMode == mode,
        onInfoClick = {
          expandedInfoMode = if (expandedInfoMode == mode) null else mode
        },
        onClick = { onModeSelected(mode) },
      )
    }
  }
}

@Composable
fun MedalsScreen(
  ratings: RatingsProgress,
  language: AppLanguage,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanText(language, UiText.Medals))
    RatingsSection(ratings = ratings, language = language)
  }
}

@Composable
fun AchievementsScreen(
  achievements: AchievementsProgress,
  language: AppLanguage,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanText(language, UiText.Achievements))
    AchievementsSection(achievements = achievements, language = language)
  }
}

@Composable
fun SettingsScreen(
  settings: SettingsState,
  hintCount: Int,
  inactiveIconActive: Boolean,
  onBack: () -> Unit,
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
  onToggleTestingIconClick: () -> Unit,
  onTriggerTestingReminderClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var expandedDifficulty by remember { mutableStateOf<HintDifficulty?>(null) }
  var testingButtonEnabled by remember { mutableStateOf(true) }
  var languageMenuExpanded by remember { mutableStateOf(false) }

  LaunchedEffect(testingButtonEnabled) {
    if (!testingButtonEnabled) {
      delay(3_000)
      testingButtonEnabled = true
    }
  }

  ScreenShell(modifier = modifier) {
    HeaderRow(title = t(settings.language, UiText.Settings))

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = t(settings.language, UiText.Language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          LanguageSelector(
            selectedLanguage = settings.language,
            expanded = languageMenuExpanded,
            onExpandedChange = { languageMenuExpanded = it },
            onLanguageSelected = onLanguageSelected,
          )
        }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = t(settings.language, UiText.Hints),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
          )
          Text("$hintCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HintDifficulty.entries.forEach { difficulty ->
          CompactInfoRow(
            title = localizedHintDifficultyTitle(difficulty, settings.language),
            shortText = localizedHintDifficultyShortRule(difficulty, settings.language),
            infoText = localizedHintDifficultyDescription(difficulty, settings.language),
            selected = settings.hintDifficulty == difficulty,
            infoExpanded = expandedDifficulty == difficulty,
            onClick = { onHintDifficultySelected(difficulty) },
            onInfoClick = {
              expandedDifficulty = if (expandedDifficulty == difficulty) null else difficulty
            },
          )
        }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = when (settings.language) {
              AppLanguage.English -> "Played today?"
              AppLanguage.Bulgarian -> "Игра ли днес?"
              AppLanguage.German -> "Heute gespielt?"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
          )
          Switch(
            checked = settings.reminderEnabled,
            onCheckedChange = onReminderEnabledChanged,
            modifier = Modifier.graphicsLayer(scaleX = 0.88f, scaleY = 0.88f),
          )
        }
        Text(
          text = when (settings.language) {
            AppLanguage.English -> "The app sends a notification at 12:00 if you have not opened it yet today."
            AppLanguage.Bulgarian -> "Приложението изпраща известие в 12:00, ако още не си го отворил днес."
            AppLanguage.German -> "Um 12:00 kommt eine Benachrichtigung, falls du die App heute nicht geöffnet hast."
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    SectionCard(title = when (settings.language) {
      AppLanguage.English -> "Testing"
      AppLanguage.Bulgarian -> "Тестване"
      AppLanguage.German -> "Testen"
    }) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
          onClick = {
            onAddTestingHintsClick()
            testingButtonEnabled = false
          },
          enabled = testingButtonEnabled,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Text(
            when (settings.language) {
              AppLanguage.English -> "Add 10 hints"
              AppLanguage.Bulgarian -> "Добави 10 жокера"
              AppLanguage.German -> "10 Hinweise hinzufügen"
            },
          )
        }
        OutlinedButton(
          onClick = onResetHintsClick,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Text(
            when (settings.language) {
              AppLanguage.English -> "Reset hints"
              AppLanguage.Bulgarian -> "Нулирай жокерите"
              AppLanguage.German -> "Hinweise zurücksetzen"
            },
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
          onClick = onTestingLevelUpClick,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Text(
            when (settings.language) {
              AppLanguage.English -> "Level +1"
              AppLanguage.Bulgarian -> "Ниво +1"
              AppLanguage.German -> "Level +1"
            },
          )
        }
        OutlinedButton(
          onClick = onTestingResetLevelClick,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Text(
            when (settings.language) {
              AppLanguage.English -> "Reset level"
              AppLanguage.Bulgarian -> "Нулирай нивото"
              AppLanguage.German -> "Level zurücksetzen"
            },
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
          onClick = onUnlockRandomAchievementClick,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Text(
            when (settings.language) {
              AppLanguage.English -> "Open random achievement"
              AppLanguage.Bulgarian -> "Случайно постижение"
              AppLanguage.German -> "Zufälliges Achievement öffnen"
            },
            textAlign = TextAlign.Center,
          )
        }
        OutlinedButton(
          onClick = onLockAllAchievementsClick,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Text(
            when (settings.language) {
              AppLanguage.English -> "Lock achievements"
              AppLanguage.Bulgarian -> "Заключи постиженията"
              AppLanguage.German -> "Erfolge sperren"
            },
            textAlign = TextAlign.Center,
          )
        }
      }
      Text(
        text =
          if (inactiveIconActive) {
            when (settings.language) {
              AppLanguage.English -> "Icon status: inactive test icon enabled"
              AppLanguage.Bulgarian -> "Състояние на иконата: тестовата неактивна икона е включена"
              AppLanguage.German -> "Symbolstatus: inaktives Testsymbol aktiviert"
            }
          } else {
            when (settings.language) {
              AppLanguage.English -> "Icon status: normal icon enabled"
              AppLanguage.Bulgarian -> "Състояние на иконата: нормалната икона е включена"
              AppLanguage.German -> "Symbolstatus: normales Symbol aktiviert"
            }
          },
        style = MaterialTheme.typography.bodySmall,
      )
      OutlinedButton(
        onClick = onToggleTestingIconClick,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          if (inactiveIconActive) {
            when (settings.language) {
              AppLanguage.English -> "Switch to normal icon"
              AppLanguage.Bulgarian -> "Към нормална икона"
              AppLanguage.German -> "Zum normalen Symbol wechseln"
            }
          } else {
            when (settings.language) {
              AppLanguage.English -> "Switch to inactive icon"
              AppLanguage.Bulgarian -> "Към неактивна икона"
              AppLanguage.German -> "Zum inaktiven Symbol wechseln"
            }
          },
        )
      }
      OutlinedButton(
        onClick = onTriggerTestingReminderClick,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          when (settings.language) {
            AppLanguage.English -> "Send test reminder"
            AppLanguage.Bulgarian -> "Изпрати тестово напомняне"
            AppLanguage.German -> "Test-Erinnerung senden"
          },
        )
      }
      OutlinedButton(
        onClick = onResetAchievementsAndMedalsClick,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          when (settings.language) {
            AppLanguage.English -> "Reset medals"
            AppLanguage.Bulgarian -> "Нулирай медалите"
            AppLanguage.German -> "Medaillen zurücksetzen"
          },
        )
      }
    }
  }
}

@Composable
fun SetupScreen(
  setup: SetupState,
  hintDifficulty: HintDifficulty,
  language: AppLanguage,
  availableContinents: List<String>,
  questionCountLimit: Int,
  setupError: String?,
  onBack: () -> Unit,
  onVariantToggle: (QuizVariant) -> Unit,
  onContinentToggle: (String) -> Unit,
  onQuestionCountChange: (String) -> Unit,
  onSurpriseMe: () -> Unit,
  onAllInTypeSelected: (AllInType) -> Unit,
  onMultiplayerBaseSelected: (MultiplayerQuizBase) -> Unit,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
  onStartQuiz: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanModeTitle(setup.mode, language))

    if (setup.mode == GameMode.LocalMultiplayer) {
      SectionCard(title = when (language) {
        AppLanguage.English -> "Players"
        AppLanguage.Bulgarian -> "Играчите"
        AppLanguage.German -> "Spieler"
      }) {
        setup.playerNames.forEachIndexed { index, name ->
          OutlinedTextField(
            value = name,
            onValueChange = { onPlayerNameChanged(index, it) },
            label = {
              Text(
                when (language) {
                  AppLanguage.English -> "Player ${index + 1}"
                  AppLanguage.Bulgarian -> "Играч ${index + 1}"
                  AppLanguage.German -> "Spieler ${index + 1}"
                },
              )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onRemovePlayer, modifier = Modifier.weight(1f)) {
            Text(
              when (language) {
                AppLanguage.English -> "Remove"
                AppLanguage.Bulgarian -> "Премахни"
                AppLanguage.German -> "Entfernen"
              },
            )
          }
          Button(onClick = onAddPlayer, modifier = Modifier.weight(1f)) {
            Text(
              when (language) {
                AppLanguage.English -> "Add player"
                AppLanguage.Bulgarian -> "Добави играч"
                AppLanguage.German -> "Spieler hinzufügen"
              },
            )
          }
        }
      }

      SectionCard(title = when (language) {
        AppLanguage.English -> "Quiz base"
        AppLanguage.Bulgarian -> "База за теста"
        AppLanguage.German -> "Quiz-Basis"
      }) {
        MultiplayerQuizBase.entries.forEach { base ->
          SelectableRow(
            title = modeBaseTitle(base, language),
            selected = setup.multiplayerBase == base,
            onClick = { onMultiplayerBaseSelected(base) },
            description = modeBaseDescription(base, language),
          )
        }
      }
    }

    if (setup.mode == GameMode.Continents || setup.multiplayerBase == MultiplayerQuizBase.Continents && setup.mode == GameMode.LocalMultiplayer) {
    SectionCard(title = cleanText(language, UiText.Continents)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          availableContinents.forEach { continent ->
            val isSelectable = continent != "Antarctica"
            FilterChip(
              selected = continent in setup.selectedContinents,
              onClick = { if (isSelectable) onContinentToggle(continent) },
              enabled = isSelectable,
              label = {
                Text(
                  text = localizedContinentName(continent, language),
                  textDecoration = if (isSelectable) TextDecoration.None else TextDecoration.LineThrough,
                )
              },
            )
          }
        }
      }
    }

    if (setup.mode != GameMode.AllIn && !(setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.AllIn)) {
      SectionCard(title = when (language) {
        AppLanguage.English -> "Question count"
        AppLanguage.Bulgarian -> "Брой въпроси"
        AppLanguage.German -> "Fragenanzahl"
      }) {
        OutlinedTextField(
          value = setup.questionCountInput,
          onValueChange = onQuestionCountChange,
          label = {
            Text(
              when (language) {
                AppLanguage.English -> "Amount of questions"
                AppLanguage.Bulgarian -> "Брой въпроси"
                AppLanguage.German -> "Fragenanzahl"
              },
            )
          },
          placeholder = {
            Text(
              if (setup.surpriseMe) {
                when (language) {
                  AppLanguage.English -> "Surprise me selected"
                  AppLanguage.Bulgarian -> "Избрано е \"Изненадай ме\""
                  AppLanguage.German -> "\"Überrasche mich\" ausgewählt"
                }
              } else {
                when (language) {
                  AppLanguage.English -> "Example: 10"
                  AppLanguage.Bulgarian -> "Пример: 10"
                  AppLanguage.German -> "Beispiel: 10"
                }
              },
            )
          },
          supportingText = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                when (language) {
                  AppLanguage.English -> "Allowed range: 1-$questionCountLimit"
                  AppLanguage.Bulgarian -> "Допустим диапазон: 1-$questionCountLimit"
                  AppLanguage.German -> "Erlaubter Bereich: 1-$questionCountLimit"
                },
              )
              Text(
                when (language) {
                  AppLanguage.English -> "Questions repeat only if you ask for more than 195; otherwise each country appears once."
                  AppLanguage.Bulgarian -> "Въпросите се повтарят само ако поискаш повече от 195. Иначе всяка държава се показва само веднъж."
                  AppLanguage.German -> "Fragen wiederholen sich nur, wenn du mehr als 195 willst; sonst erscheint jedes Land nur einmal."
                },
              )
              if (ProgressionRules.shouldWarnNoMedal(setup.questionCount)) {
                Text(
                  when (language) {
                    AppLanguage.English -> "Perfect runs under 10 questions do not earn a medal."
                    AppLanguage.Bulgarian -> "Перфектен тест под 10 въпроса не носи медал."
                    AppLanguage.German -> "Perfekte Läufe unter 10 Fragen geben keine Medaille."
                  },
                )
              }
            }
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          enabled = !setup.surpriseMe,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = onSurpriseMe, modifier = Modifier.fillMaxWidth()) {
          Text(
            if (setup.surpriseMe) {
              when (language) {
                AppLanguage.English -> "Use custom amount"
                AppLanguage.Bulgarian -> "Използвай собствен брой"
                AppLanguage.German -> "Eigene Anzahl verwenden"
              }
            } else {
              when (language) {
                AppLanguage.English -> "Surprise me! (1-$questionCountLimit)"
                AppLanguage.Bulgarian -> "Изненадай ме! (1-$questionCountLimit)"
                AppLanguage.German -> "Überrasche mich! (1-$questionCountLimit)"
              }
            },
          )
        }
      }
    }

    SectionCard(title = when (language) {
      AppLanguage.English -> "Question variants"
      AppLanguage.Bulgarian -> "Видове въпроси"
      AppLanguage.German -> "Fragetypen"
    }) {
      QuizVariant.entries.forEach { variant ->
        CheckRow(
          title = localizedVariantTitle(variant, language),
          description = localizedVariantDescription(variant, language),
          checked = variant in setup.variants,
          onClick = { onVariantToggle(variant) },
        )
      }
    }

    if (setupError != null) {
      Text(text = setupError, color = AccentRed, style = MaterialTheme.typography.bodyMedium)
    }

    Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
      Text(
        when (language) {
          AppLanguage.English -> "Start quiz"
          AppLanguage.Bulgarian -> "Започни теста"
          AppLanguage.German -> "Quiz starten"
        },
      )
    }

    if (setup.mode == GameMode.AllIn) {
      val hasAllVariants = setup.variants.size == QuizVariant.entries.size
      val hintSettingLabel = localizedHintDifficultyTitle(hintDifficulty, language)
      val levelReward = if (hintDifficulty == HintDifficulty.Impossible) "+2" else "+1"
      InfoPanel(
        text = allInRewardInfo(
          language = language,
          hintSettingLabel = hintSettingLabel,
          hasAllVariants = hasAllVariants,
          rewardLevels = levelReward,
          isImpossible = hintDifficulty == HintDifficulty.Impossible,
        ),
      )
    }
  }
}

@Composable
fun QuizScreen(
  quiz: QuizState,
  language: AppLanguage,
  onLeaveQuiz: () -> Unit,
  onCountryAnswerSelected: (FlagCountry) -> Unit,
  onTypedAnswerChanged: (String) -> Unit,
  onVerifyTypedAnswer: () -> Unit,
  onUseHint: () -> Unit,
  onPreviousQuestion: () -> Unit,
  onNextQuestionPreview: () -> Unit,
  onUnskipQuestion: () -> Unit,
  onFinishQuiz: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val question = quiz.currentQuestion ?: return
  val draft = quiz.currentQuestionState
  val isTrainingLocked = quiz.mode == GameMode.Training && draft.locked
  val isTrainingPreview = quiz.mode == GameMode.Training && draft.status == QuestionStatus.Answered
  val isTrainingCorrect =
    when (question.variant) {
      QuizVariant.TypeCountryName ->
        QuizAnswerChecker.isTypedAnswerCorrect(
          typedAnswer = draft.typedAnswer,
          acceptedAnswers = question.correctCountry.acceptedTypedAnswers(language),
        )

      QuizVariant.FlagToCountry,
      QuizVariant.CountryToFlag -> QuizAnswerChecker.isCountrySelectionCorrect(draft.selectedCountry, question.correctCountry)
    }
  var showQuitDialog by remember { mutableStateOf(false) }
  var showQuizInfo by remember { mutableStateOf(false) }
  BackHandler { showQuitDialog = true }
  val canGoBack = quiz.currentQuestionIndex > 0
  val canGoForward = quiz.currentQuestionIndex < quiz.questions.lastIndex
  val unansweredQuestions = quiz.questionStates.mapIndexedNotNull { index, state -> if (state.status == QuestionStatus.Unanswered) index + 1 else null }
  val skippedQuestions = quiz.questionStates.mapIndexedNotNull { index, state -> if (state.status == QuestionStatus.Skipped) index + 1 else null }

  if (showQuitDialog) {
    AlertDialog(
      onDismissRequest = { showQuitDialog = false },
      title = { Text(cleanText(language, UiText.LeaveQuizTitle)) },
      text = { Text(cleanText(language, UiText.LeaveQuizBody)) },
      confirmButton = {
        TextButton(onClick = onLeaveQuiz) {
          Text(cleanText(language, UiText.Leave))
        }
      },
      dismissButton = {
        TextButton(onClick = { showQuitDialog = false }) {
          Text(cleanText(language, UiText.Stay))
        }
      },
    )
  }

  ScreenShell(modifier = modifier, padding = 12.dp, spacing = 8.dp) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = displayModeTitle(quiz.mode, language),
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        modifier = Modifier.weight(1f),
      )
      Button(onClick = onFinishQuiz, enabled = quiz.canFinish) {
        Text(cleanText(language, UiText.Finish))
      }
    }

    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(cleanText(language, UiText.GuessTheFlag), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("${quiz.currentQuestionIndex + 1}/${quiz.totalQuestions}", style = MaterialTheme.typography.bodySmall)
        Text("${cleanText(language, UiText.Hints)}: ${quiz.currentPlayer.hintPoints}", style = MaterialTheme.typography.bodySmall)
      }
    }

    if (showQuizInfo) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoPanel(text = cleanText(language, UiText.QuizInfo))
        QuestionTrackingPanel(
          title = cleanText(language, UiText.Unanswered),
          questionNumbers = unansweredQuestions,
        )
        QuestionTrackingPanel(
          title = cleanText(language, UiText.Skipped),
          questionNumbers = skippedQuestions,
        )
      }
    }

    if (quiz.isMultiplayer) {
      Surface(color = AccentGold.copy(alpha = 0.18f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "${cleanText(language, UiText.NextUp)}: ${quiz.currentPlayer.name}",
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedButton(
        onClick = onPreviousQuestion,
        enabled = canGoBack,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
      ) {
        Text(PreviousArrowText)
      }
      QuestionPrompt(
        question = question,
        language = language,
        modifier = Modifier.weight(1f),
      )
      OutlinedButton(
        onClick = onNextQuestionPreview,
        enabled = canGoForward,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
      ) {
        Text(NextArrowText)
      }
    }

    if (question.variant == QuizVariant.TypeCountryName) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = quiz.typedAnswer,
          onValueChange = onTypedAnswerChanged,
          enabled = !isTrainingLocked,
          label = {
            Text(
              when (language) {
                AppLanguage.English -> "Country name"
                AppLanguage.Bulgarian -> "Име на държава"
                AppLanguage.German -> "Ländername"
              },
            )
          },
          singleLine = true,
          supportingText = {
            quiz.typedHintPrefix?.let {
              Text(
                when (language) {
                  AppLanguage.English -> "Hint: starts with $it"
                  AppLanguage.Bulgarian -> "Подсказка: започва с $it"
                  AppLanguage.German -> "Hinweis: beginnt mit $it"
                },
              )
            }
          },
          colors =
            if (isTrainingPreview) {
              OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isTrainingCorrect) AccentGreen.copy(alpha = 0.18f) else AccentRed.copy(alpha = 0.18f),
                unfocusedContainerColor = if (isTrainingCorrect) AccentGreen.copy(alpha = 0.18f) else AccentRed.copy(alpha = 0.18f),
                disabledContainerColor = if (isTrainingCorrect) AccentGreen.copy(alpha = 0.18f) else AccentRed.copy(alpha = 0.18f),
                focusedBorderColor = if (isTrainingCorrect) AccentGreen else AccentRed,
                unfocusedBorderColor = if (isTrainingCorrect) AccentGreen else AccentRed,
                disabledBorderColor = if (isTrainingCorrect) AccentGreen else AccentRed,
                focusedTextColor = if (isTrainingCorrect) AccentGreen else AccentRed,
                unfocusedTextColor = if (isTrainingCorrect) AccentGreen else AccentRed,
                disabledTextColor = if (isTrainingCorrect) AccentGreen else AccentRed,
              )
            } else {
              OutlinedTextFieldDefaults.colors()
            },
          modifier = Modifier.fillMaxWidth(),
        )
        if (isTrainingPreview && !isTrainingCorrect) {
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(2.dp, AccentGreen),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
              Text(
                text =
                  when (language) {
                    AppLanguage.English -> "Correct answer"
                    AppLanguage.Bulgarian -> "Верен отговор"
                    AppLanguage.German -> "Richtige Antwort"
                  },
                style = MaterialTheme.typography.bodySmall,
              )
              Text(
                text = question.correctCountry.localizedName(language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AccentGreen,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
              )
            }
          }
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        question.options
          .filterNot { it.code in quiz.hiddenOptionCodes }
          .forEach { option ->
            AnswerButton(
              question = question,
              option = option,
              selectedCountry = quiz.selectedCountry,
              language = language,
              onCountryAnswerSelected = onCountryAnswerSelected,
              enabled = !isTrainingLocked,
              trainingPreview = isTrainingPreview,
              correctCountry = question.correctCountry,
            )
          }
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      InfoButton(
        label = localizedQuizInfoButtonLabel(language),
        modifier = Modifier.weight(1f),
        onClick = { showQuizInfo = !showQuizInfo },
      )
      OutlinedButton(
        onClick = onUseHint,
        enabled = quiz.currentPlayer.hintPoints >= 1 && quiz.currentQuestionState.hintUses < 2 && !quiz.currentQuestionState.locked && quiz.currentQuestionState.status != QuestionStatus.Answered,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
      ) {
        Text(
          text =
            if (quiz.currentQuestionState.hintUses == 0) {
              localizedHintButtonLabel(language)
            } else {
              localizedRevealButtonLabel(language)
            },
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          softWrap = false,
        )
      }
      OutlinedButton(
        onClick = onUnskipQuestion,
        enabled = skippedQuestions.isNotEmpty(),
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
      ) {
        Text(
          text = localizedUnskipButtonLabel(language),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          softWrap = false,
        )
      }
      if (question.variant == QuizVariant.TypeCountryName) {
        OutlinedButton(
          onClick = onVerifyTypedAnswer,
          enabled = quiz.typedAnswer.isNotBlank() && !isTrainingLocked,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
        ) {
          Text(
            text = localizedVerifyButtonLabel(language),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }
  }
}

@Composable
fun ResultsScreen(
  quiz: QuizState,
  language: AppLanguage,
  levelProgress: LevelProgressState,
  onPlayAgain: () -> Unit,
  onBackToMenu: () -> Unit,
  onLevelUpSeen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = quizCompleteTitle(language))

    if (levelProgress.levelUpVisible) {
      LevelUpBanner(level = levelProgress.level, language = language, onLevelUpSeen = onLevelUpSeen)
    }

    SectionCard(title = when (language) {
      AppLanguage.English -> "Final results"
      AppLanguage.Bulgarian -> "Краен резултат"
      AppLanguage.German -> "Endergebnisse"
    }) {
      quiz.players.sortedByDescending { it.score }.forEach { player ->
        val playerResults = quiz.results.filter { it.playerName == player.name }
        PlayerResultRow(
          player = player,
          language = language,
          totalQuestions = playerResults.size,
          correctAnswers = playerResults.count { it.isCorrect },
          showHints = quiz.mode != GameMode.LocalMultiplayer,
        )
      }
    }

    SectionCard(title = when (language) {
      AppLanguage.English -> "Answer review"
      AppLanguage.Bulgarian -> "Преглед на отговорите"
              AppLanguage.German -> "Antwortübersicht"
    }) {
      quiz.results.forEachIndexed { index, result ->
        ResultRow(index = index + 1, result = result, language = language)
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      Button(onClick = onPlayAgain, modifier = Modifier.weight(1f)) {
        Text(
          when (language) {
            AppLanguage.English -> "Play again"
            AppLanguage.Bulgarian -> "Играй отново"
            AppLanguage.German -> "Nochmal spielen"
          },
        )
      }
      OutlinedButton(onClick = onBackToMenu, modifier = Modifier.weight(1f)) {
        Text(
          when (language) {
            AppLanguage.English -> "Menu"
            AppLanguage.Bulgarian -> "Меню"
            AppLanguage.German -> "Menü"
          },
        )
      }
    }
  }
}

@Composable
private fun ScreenShell(
  modifier: Modifier = Modifier,
  padding: androidx.compose.ui.unit.Dp = 20.dp,
  spacing: androidx.compose.ui.unit.Dp = 16.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  val backgroundGradient =
    Brush.verticalGradient(
      colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface),
    )

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(backgroundGradient)
        .padding(padding),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(spacing),
      content = content,
    )
  }
}

@Composable
private fun LevelProgressPanel(
  levelProgress: LevelProgressState,
  profile: ProfileState,
  onLevelUpSeen: () -> Unit,
  language: AppLanguage,
  onClick: () -> Unit,
) {
  if (levelProgress.levelUpVisible) {
    LevelUpBanner(level = levelProgress.level, language = language, onLevelUpSeen = onLevelUpSeen)
  }

  val animatedProgress by animateFloatAsState(
    targetValue = levelProgress.progressFraction,
    label = "level-progress",
  )
  val progressPercent = (animatedProgress.coerceIn(0f, 1f) * 100).roundToInt()

  ElevatedCard(
    onClick = onClick,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
      ) {
        Text(
          text = avatarFor(profile.avatarIndex),
          modifier = Modifier.padding(14.dp),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            "${profile.displayName} - ${cleanText(language, UiText.Level)} ${levelProgress.level}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
          )
          Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            contentColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
          ) {
            Text(
              text = "\u270E",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
            )
          }
        }
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(999.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(18.dp),
            contentAlignment = Alignment.Center,
          ) {
            Surface(
              color = AccentGreen,
              shape = RoundedCornerShape(999.dp),
              modifier =
                Modifier
                  .fillMaxWidth(animatedProgress.coerceAtLeast(0.03f))
                  .height(18.dp)
                  .align(Alignment.CenterStart),
            ) {}
            Text(
              text = "$progressPercent%",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              textAlign = TextAlign.Center,
            )
          }
        }
        Text(
          text =
            "${levelProgress.hintsTowardNextLevel}/${levelProgress.hintsNeeded} hints  ↺  " +
              "${levelProgress.correctAnswersTowardNextLevel}/${levelProgress.correctAnswersNeeded} correct  ↺  " +
              "${levelProgress.eligibleQuizzesTowardNextLevel}/${levelProgress.eligibleQuizzesNeeded} tests",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.Transparent, fontSize = 0.sp),
        )
      }
    }
  }
}

@Composable
private fun ProfileEditorDialog(
  profile: ProfileState,
  levelProgress: LevelProgressState,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (String, Int) -> Unit,
) {
  var nameDraft by remember(profile.accountName) { mutableStateOf(profile.accountName) }
  var avatarDraft by remember(profile.avatarIndex) { mutableStateOf(profile.avatarIndex) }
  var avatarPickerVisible by remember { mutableStateOf(false) }

  if (avatarPickerVisible) {
    AvatarPickerSheet(
      selectedAvatarIndex = avatarDraft,
      language = language,
      level = levelProgress.level,
      onDismiss = { avatarPickerVisible = false },
      onAvatarSelected = {
        avatarDraft = it
        avatarPickerVisible = false
      },
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(cleanText(language, UiText.Profile)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            shape = CircleShape,
          ) {
            Text(
              text = avatarFor(avatarDraft),
              modifier = Modifier.padding(16.dp),
              fontSize = 34.sp,
            )
          }
          Button(onClick = { avatarPickerVisible = true }) {
            Text(
              when (language) {
                AppLanguage.English -> "\u270E Change icon"
                AppLanguage.Bulgarian -> "✎ Промени иконата"
                AppLanguage.German -> "✎ Symbol ändern"
              },
            )
          }
        }
        OutlinedTextField(
          value = nameDraft,
          onValueChange = { nameDraft = it.take(24) },
          label = { Text(cleanText(language, UiText.AccountName)) },
          placeholder = { Text("Player 1") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Surface(
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              when (language) {
                AppLanguage.English -> "Next level requirements"
                AppLanguage.Bulgarian -> "Изисквания за следващо ниво"
                AppLanguage.German -> "Anforderungen für das nächste Level"
              },
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Text("${levelProgress.hintsTowardNextLevel}/${levelProgress.hintsNeeded} ${cleanText(language, UiText.Hints)}")
            Text("${levelProgress.correctAnswersTowardNextLevel}/${levelProgress.correctAnswersNeeded} ${cleanText(language, UiText.CorrectAnswers)}")
            Text("${levelProgress.eligibleQuizzesTowardNextLevel}/${levelProgress.eligibleQuizzesNeeded} ${cleanText(language, UiText.CompletedTests)}")
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = { onSave(nameDraft, avatarDraft) }) {
        Text(
          when (language) {
            AppLanguage.English -> "Save"
            AppLanguage.Bulgarian -> "Запази"
            AppLanguage.German -> "Speichern"
          },
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
          when (language) {
            AppLanguage.English -> "Cancel"
            AppLanguage.Bulgarian -> "Отказ"
            AppLanguage.German -> "Abbrechen"
          },
        )
      }
    },
  )
}

@Composable
private fun AvatarPickerDialog(
  selectedAvatarIndex: Int,
  language: AppLanguage,
  level: Int,
  onDismiss: () -> Unit,
  onAvatarSelected: (Int) -> Unit,
) {
  var showUnlockInfo by remember { mutableStateOf(false) }
  val unlockedAvatarCount = ProgressionRules.unlockedAvatarCount(level)
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        when (language) {
          AppLanguage.English -> "Choose profile icon"
          AppLanguage.Bulgarian -> "Избери икона на профила"
          AppLanguage.German -> "Profilbild wählen"
          else -> cleanText(language, UiText.ChooseProfileIcon)
        },
      )
    },
    text = {
      Box(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AvatarOptions.forEachIndexed { index, avatar ->
            OutlinedButton(
              onClick = { onAvatarSelected(index) },
              colors =
                ButtonDefaults.outlinedButtonColors(
                  containerColor =
                    if (selectedAvatarIndex == index) {
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    } else {
                      Color.Transparent
                    },
                ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            ) {
              Text(avatar, fontSize = 22.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(
          when (language) {
            AppLanguage.English -> "Close"
            AppLanguage.Bulgarian -> "Затвори"
            AppLanguage.German -> "Schließen"
            else -> cleanText(language, UiText.Close)
          },
        )
      }
    },
  )
}

@Composable
private fun AvatarPickerSheet(
  selectedAvatarIndex: Int,
  language: AppLanguage,
  level: Int,
  onDismiss: () -> Unit,
  onAvatarSelected: (Int) -> Unit,
) {
  var showUnlockInfo by remember { mutableStateOf(false) }
  val unlockedAvatarCount = ProgressionRules.unlockedAvatarCount(level)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(cleanText(language, UiText.ChooseProfileIcon), modifier = Modifier.weight(1f))
        InfoButton(onClick = { showUnlockInfo = !showUnlockInfo })
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (showUnlockInfo) {
          InfoPanel(text = localizedAvatarUnlockInfo(language))
        }
        Box(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AvatarOptions.chunked(5).forEachIndexed { rowIndex, avatars ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                avatars.forEachIndexed { columnIndex, avatar ->
                  val index = rowIndex * 5 + columnIndex
                  val unlocked = index < unlockedAvatarCount
                  Surface(
                    onClick = { if (unlocked) onAvatarSelected(index) },
                    color =
                      if (selectedAvatarIndex == index && unlocked) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                      } else {
                        MaterialTheme.colorScheme.surfaceVariant
                      },
                    contentColor =
                      if (unlocked) {
                        MaterialTheme.colorScheme.onSurface
                      } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                      },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                  ) {
                    Box(
                      modifier = Modifier.height(46.dp).fillMaxWidth(),
                      contentAlignment = Alignment.Center,
                    ) {
                      if (unlocked) {
                        Text(avatar, fontSize = 21.sp)
                      } else {
                        Text("\uD83D\uDD12", fontSize = 18.sp)
                      }
                    }
                  }
                }
                repeat(5 - avatars.size) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(cleanText(language, UiText.Close))
      }
    },
  )
}

@Composable
private fun LanguageSelector(
  selectedLanguage: AppLanguage,
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit,
  onLanguageSelected: (AppLanguage) -> Unit,
) {
  Box(modifier = Modifier.widthIn(min = 128.dp, max = 162.dp)) {
    OutlinedButton(
      onClick = { onExpandedChange(!expanded) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = CircleShape,
        ) {
          Text(
            text = languageFlag(selectedLanguage),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
          )
        }
        Text(
          languageName(selectedLanguage),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "\u25BE",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.graphicsLayer {
            rotationZ = if (expanded) 180f else 0f
          },
        )
      }
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { onExpandedChange(false) },
      modifier = Modifier.widthIn(min = 160.dp, max = 220.dp),
    ) {
      AppLanguage.entries.forEach { language ->
        DropdownMenuItem(
          text = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
              ) {
                Text(
                  text = languageFlag(language),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  fontWeight = FontWeight.Bold,
                )
              }
              Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(languageName(language), fontWeight = FontWeight.Bold)
                Text(
                  languageDescription(language),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          },
          onClick = {
            onLanguageSelected(language)
            onExpandedChange(false)
          },
        )
      }
    }
  }
}

@Composable
private fun LevelUpBanner(
  level: Int,
  language: AppLanguage,
  onLevelUpSeen: () -> Unit,
) {
  var visible by remember(level) { mutableStateOf(false) }
  val appear by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    label = "level-up-appear",
  )
  val fireworkPulse =
    rememberInfiniteTransition(label = "level-up-fireworks").animateFloat(
      initialValue = 0.15f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(1400, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "firework-pulse",
    )

  LaunchedEffect(level) {
    visible = true
    delay(4_000)
    onLevelUpSeen()
  }

  ElevatedCard(
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .graphicsLayer(
            alpha = appear,
            scaleX = 0.92f + (0.08f * appear),
            scaleY = 0.92f + (0.08f * appear),
            translationY = (1f - appear) * 16f,
          )
          .background(
            Brush.linearGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
              ),
            ),
          ),
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = size.minDimension / 2.15f
        val pulseRadius = maxRadius * fireworkPulse.value
        val pulseAlpha = (0.20f + fireworkPulse.value * 0.25f).coerceAtMost(0.45f)
        drawCircle(
          color = AccentGold.copy(alpha = pulseAlpha),
          radius = pulseRadius,
          style = Stroke(width = 4.dp.toPx()),
        )
        drawCircle(
          color = AccentGreen.copy(alpha = pulseAlpha),
          radius = pulseRadius * 0.72f,
          style = Stroke(width = 3.dp.toPx()),
        )
        drawCircle(
          color = AccentRed.copy(alpha = pulseAlpha * 0.8f),
          radius = pulseRadius * 0.48f,
          style = Stroke(width = 2.dp.toPx()),
        )
      }
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(cleanText(language, UiText.LevelUpTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(levelUpBody(language, level), style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Preview(showBackground = true, name = "Level Up Banner - English")
@Composable
private fun PreviewLevelUpBannerEnglish() {
  FlagGameAndroidTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      LevelUpBanner(level = 4, language = AppLanguage.English, onLevelUpSeen = {})
    }
  }
}

@Preview(showBackground = true, name = "Level Up Banner - German")
@Composable
private fun PreviewLevelUpBannerGerman() {
  FlagGameAndroidTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      LevelUpBanner(level = 7, language = AppLanguage.German, onLevelUpSeen = {})
    }
  }
}

@Composable
private fun HeroPanel(
  title: String,
  subtitle: String,
  language: AppLanguage,
  onStartClick: () -> Unit,
  onMedalsClick: () -> Unit,
  onAchievementsClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onQuitClick: () -> Unit,
) {
  var showInfo by remember { mutableStateOf(false) }

  ElevatedCard(
    colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
              ),
            ),
            shape = RoundedCornerShape(24.dp),
          )
          .padding(18.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          HeroGoldPill(cleanHeroPill(0, language), modifier = Modifier.weight(1f))
          HeroGoldPill(cleanHeroPill(1, language), modifier = Modifier.weight(1f))
          HeroGoldPill(cleanHeroPill(2, language), modifier = Modifier.weight(1f))
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 25.sp),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
          )
          HeroInfoButton(onClick = { showInfo = !showInfo })
        }
        if (showInfo) {
          Surface(
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = subtitle,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
        Surface(
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.13f),
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = RoundedCornerShape(28.dp),
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(top = 4.dp, start = 18.dp, end = 18.dp),
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp),
          ) {
            Button(
              onClick = onStartClick,
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(vertical = 17.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = AccentGold,
                  contentColor = Color(0xFF172033),
                ),
            ) {
              Text(cleanText(language, UiText.Start), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            HeroNavButton(cleanText(language, UiText.Medals), onMedalsClick, widthFraction = 0.78f)
            HeroNavButton(cleanText(language, UiText.Achievements), onAchievementsClick, widthFraction = 0.78f)
            HeroNavButton(cleanText(language, UiText.Settings), onSettingsClick, widthFraction = 0.78f)
            HeroNavButton(cleanText(language, UiText.Quit), onQuitClick, widthFraction = 0.78f)
          }
        }
      }
    }
  }
}

@Composable
private fun HeroGoldPill(
  text: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = AccentGold.copy(alpha = 0.16f),
    contentColor = AccentGold,
    shape = RoundedCornerShape(999.dp),
    modifier = modifier.height(44.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
        text = text,
        modifier = Modifier.padding(horizontal = 6.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun HeroNavButton(
  label: String,
  onClick: () -> Unit,
  widthFraction: Float,
) {
  OutlinedButton(
    onClick = onClick,
    colors =
      ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
    ),
    shape = RoundedCornerShape(16.dp),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    modifier = Modifier.fillMaxWidth(widthFraction),
  ) {
    Text(label, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun HeroInfoButton(onClick: () -> Unit) {
  OutlinedButton(
    onClick = onClick,
    colors =
      ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
      ),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
  ) {
    Text(InfoSymbolText, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun HeroStatPill(text: String) {
  Surface(
    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
    contentColor = MaterialTheme.colorScheme.onPrimary,
    shape = RoundedCornerShape(999.dp),
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun RatingsSection(
  ratings: RatingsProgress,
  language: AppLanguage,
) {
  var showMedalInfo by remember { mutableStateOf(false) }
  SectionCard(title = cleanText(language, UiText.Medals)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = cleanMedalIntro(language),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f),
      )
      InfoButton(onClick = { showMedalInfo = !showMedalInfo })
    }
    if (showMedalInfo) {
      InfoPanel(text = cleanMedalInfo(language))
    }
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MedalTier.entries.forEach { medalTier ->
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(text = medalTier.badge, fontSize = 24.sp)
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = cleanMedalTitle(medalTier, language), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              }
            }
            Text(
              text = ratings.countFor(medalTier).toString(),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AchievementsSection(
  achievements: AchievementsProgress,
  language: AppLanguage,
) {
  var expandedAchievement by remember { mutableStateOf<AchievementId?>(null) }
  SectionCard(title = cleanText(language, UiText.Achievements)) {
    Text(
      text = localizedAchievementHint(language),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    AchievementSector.entries.forEach { sector ->
      Text(
        text = localizedAchievementSectorTitle(sector, language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 6.dp),
      )
      AchievementId.entries.filter { it.sector == sector }.forEach { achievementId ->
        val unlockedAt = achievements.unlockedAt(achievementId)
        val unlocked = unlockedAt != null
        Card(
          onClick = { expandedAchievement = if (expandedAchievement == achievementId) null else achievementId },
          colors =
            CardDefaults.cardColors(
              containerColor =
                if (unlocked) {
                  MaterialTheme.colorScheme.surfaceVariant
                } else {
                  MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                },
            ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                color =
                  if (unlocked) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                  } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                  },
                shape = CircleShape,
              ) {
                Box(
                  modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = if (unlocked) achievementId.badge else "\uD83D\uDD12",
                    fontSize = 20.sp,
                    color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                  )
                }
              }
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = localizedAchievementTitle(achievementId, language),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                )
                Text(
                  text = localizedAchievementStatus(language, unlockedAt),
                  style = MaterialTheme.typography.bodySmall,
                  color = if (unlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
              }
            }
            if (expandedAchievement == achievementId) {
              InfoPanel(text = localizedAchievementDescription(achievementId, language))
            }
          }
        }
      }
    }
  }
}

private fun formatAchievementDate(epochMillis: Long?): String {
  if (epochMillis == null) return "-"
  return SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).format(Date(epochMillis))
}

@Composable
private fun HeaderRow(
  title: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium,
      color = Color.White,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun ModeCard(
  mode: GameMode,
  language: AppLanguage,
  infoExpanded: Boolean,
  onInfoClick: () -> Unit,
  onClick: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(text = cleanModeTitle(mode, language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(text = cleanModeShortLabel(mode, language), style = MaterialTheme.typography.bodySmall)
        }
        InfoButton(onClick = onInfoClick)
        Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
          Text(
            cleanText(language, UiText.Open),
          )
        }
      }
      if (infoExpanded) {
        InfoPanel(text = cleanModeDescription(mode, language))
      }
    }
  }
}

@Composable
private fun InfoButton(
  label: String = InfoSymbolText,
  modifier: Modifier = Modifier,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.78f),
  containerColor: Color = Color.Transparent,
  onClick: () -> Unit,
) {
  val iconOnly = label == InfoSymbolText
  if (!iconOnly) {
    OutlinedButton(
      onClick = onClick,
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
    ) {
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        softWrap = false,
      )
    }
    return
  }

  Surface(
    color = containerColor,
    contentColor = contentColor,
    shape = RoundedCornerShape(999.dp),
    border = BorderStroke(1.dp, borderColor),
    modifier =
      modifier
        .then(
          Modifier
            .width(42.dp)
            .height(40.dp),
        )
        .clickable(onClick = onClick),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 0.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = contentColor,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun CompactInfoRow(
  title: String,
  shortText: String,
  infoText: String,
  selected: Boolean,
  infoExpanded: Boolean,
  onClick: () -> Unit,
  onInfoClick: () -> Unit,
) {
  val colors =
    if (selected) {
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    } else {
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }
  val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

  Card(onClick = onClick, colors = colors, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
          fontWeight = FontWeight.Bold,
          color = contentColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        InfoButton(
          onClick = onInfoClick,
          contentColor = contentColor,
          borderColor = contentColor.copy(alpha = 0.92f),
          containerColor = contentColor.copy(alpha = 0.12f),
        )
      }
      if (infoExpanded) {
        InfoPanel(text = infoText)
      }
    }
  }
}

@Composable
private fun InfoPanel(text: String) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    shape = RoundedCornerShape(10.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun QuestionTrackingPanel(
  title: String,
  questionNumbers: List<Int>,
) {
  InfoPanel(
    text =
      if (questionNumbers.isEmpty()) {
        "$title: -"
      } else {
        "$title: ${formatQuestionRanges(questionNumbers)}"
      },
  )
}

private fun formatQuestionRanges(numbers: List<Int>): String {
  if (numbers.isEmpty()) return "-"
  val sorted = numbers.distinct().sorted()
  val ranges = mutableListOf<String>()
  var start = sorted.first()
  var previous = start
  for (index in 1 until sorted.size) {
    val current = sorted[index]
    if (current == previous + 1) {
      previous = current
      continue
    }
    ranges += if (start == previous) "$start" else "$start-$previous"
    start = current
    previous = current
  }
  ranges += if (start == previous) "$start" else "$start-$previous"
  return ranges.joinToString(", ")
}

private fun HintDifficulty.shortRule(): String =
  when (this) {
    HintDifficulty.Rookie -> "Every 3-streak"
    HintDifficulty.Medium -> "Every 5-streak"
    HintDifficulty.Hard -> "Every 10-streak"
    HintDifficulty.Impossible -> "Every 50-streak"
  }

@Composable
private fun SettingSwitchRow(
  title: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Text(text = description, style = MaterialTheme.typography.bodySmall)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
private fun SectionCard(
  title: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(text = title, style = MaterialTheme.typography.titleLarge)
      content()
    }
  }
}

@Composable
private fun SelectableRow(
  title: String,
  selected: Boolean,
  onClick: () -> Unit,
  description: String? = null,
) {
  val colors =
    if (selected) {
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    } else {
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }
  Card(onClick = onClick, colors = colors, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(title, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
      if (description != null) {
        Text(description, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}

@Composable
private fun QuestionPrompt(
  question: FlagQuestion,
  language: AppLanguage,
  modifier: Modifier = Modifier,
) {
  ElevatedCard(
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      when (question.variant) {
        QuizVariant.FlagToCountry,
        QuizVariant.TypeCountryName -> {
          Text(
            text = question.correctCountry.emoji,
            fontSize = 62.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
          )
        }

        QuizVariant.CountryToFlag -> {
          Text(
            text = question.correctCountry.localizedName(language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}

@Composable
private fun CheckRow(
  title: String,
  description: String,
  checked: Boolean,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(text = title, style = MaterialTheme.typography.titleSmall)
      Text(text = description, style = MaterialTheme.typography.bodySmall)
    }
    Checkbox(checked = checked, onCheckedChange = { onClick() })
  }
}

@Composable
private fun AnswerButton(
  question: FlagQuestion,
  option: FlagCountry,
  selectedCountry: FlagCountry?,
  language: AppLanguage,
  onCountryAnswerSelected: (FlagCountry) -> Unit,
  enabled: Boolean = true,
  trainingPreview: Boolean = false,
  correctCountry: FlagCountry? = null,
) {
  val selected = selectedCountry?.code == option.code
  val isCorrectOption = correctCountry?.code == option.code
  val isTrainingCorrect = trainingPreview && selected && isCorrectOption
  val isTrainingWrong = trainingPreview && selected && !isCorrectOption
  val color =
    when {
      isTrainingCorrect -> AccentGreen.copy(alpha = 0.88f)
      isTrainingWrong -> AccentRed.copy(alpha = 0.88f)
      selected -> MaterialTheme.colorScheme.primary
      else -> MaterialTheme.colorScheme.surfaceVariant
    }
  val border =
    when {
      trainingPreview && isCorrectOption && !selected -> BorderStroke(2.dp, AccentGreen)
      else -> null
    }
  Button(
    onClick = { onCountryAnswerSelected(option) },
    enabled = enabled,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = color,
        contentColor = buttonContentColor(color),
        disabledContainerColor = color.copy(alpha = if (trainingPreview) 0.72f else 0.42f),
        disabledContentColor = buttonContentColor(color),
      ),
    border = border,
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
    shape = RoundedCornerShape(10.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = if (question.variant == QuizVariant.CountryToFlag) option.emoji else option.localizedName(language),
      fontSize = if (question.variant == QuizVariant.CountryToFlag) 32.sp else 16.sp,
    )
  }
}

@Composable
private fun PlayerResultRow(
  player: PlayerProgress,
  language: AppLanguage,
  totalQuestions: Int,
  correctAnswers: Int,
  showHints: Boolean,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = player.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Correct answers: $correctAnswers / $totalQuestions"
            AppLanguage.Bulgarian -> "Верни отговори: $correctAnswers / $totalQuestions"
            AppLanguage.German -> "Richtige Antworten: $correctAnswers / $totalQuestions"
          },
      )
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Net score: ${formatScore(player.score)}"
            AppLanguage.Bulgarian -> "Краен резултат: ${formatScore(player.score)}"
            AppLanguage.German -> "Punktestand: ${formatScore(player.score)}"
          },
      )
      if (showHints) {
        Text(
          text =
            when (language) {
              AppLanguage.English -> "Hint points available: ${player.hintPoints}"
              AppLanguage.Bulgarian -> "Налични жокери: ${player.hintPoints}"
              AppLanguage.German -> "Verfügbare Hinweise: ${player.hintPoints}"
            },
        )
      }
    }
  }
}

private fun hintDifficultyDescription(difficulty: HintDifficulty): String =
  when (difficulty) {
    HintDifficulty.Rookie -> "Collect 1 hint for every correct answer."
    HintDifficulty.Medium -> "Collect 1 hint for every 5 correct answers in a row."
    HintDifficulty.Hard -> "Collect 1 hint for every 10 correct answers in a row."
    HintDifficulty.Impossible -> "Collect 1 hint for every 50 correct answers in a row."
  }

@Composable
private fun ResultRow(
  index: Int,
  result: QuestionResult,
  language: AppLanguage,
) {
  val background =
    when {
      result.isCorrect -> AccentGreen.copy(alpha = 0.15f)
      else -> AccentRed.copy(alpha = 0.15f)
    }
  val hintSuffix =
    when (result.hintUses) {
      1 ->
        when (language) {
          AppLanguage.English -> " (Hinted)"
          AppLanguage.Bulgarian -> " (Подсказано)"
          AppLanguage.German -> " (Tipp genutzt)"
        }
      2 ->
        when (language) {
          AppLanguage.English -> " (Revealed)"
          AppLanguage.Bulgarian -> " (Разкрито)"
          AppLanguage.German -> " (Aufgedeckt)"
        }
      else -> ""
    }
  val wrongOptions =
    if (result.question.variant == QuizVariant.TypeCountryName) {
      emptyList()
    } else {
      result.question.options.filterNot { it.code == result.question.correctCountry.code }
    }
  Surface(
    color = background,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Question $index$hintSuffix"
            AppLanguage.Bulgarian -> "Въпрос $index$hintSuffix"
            AppLanguage.German -> "Frage $index$hintSuffix"
          },
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Question type: ${localizedVariantTitle(result.question.variant, language)}"
            AppLanguage.Bulgarian -> "Тип въпрос: ${localizedVariantTitle(result.question.variant, language)}"
            AppLanguage.German -> "Fragetyp: ${localizedVariantTitle(result.question.variant, language)}"
          },
      )
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Correct: ${result.question.correctCountry.emoji} ${result.question.correctCountry.localizedName(language)}"
            AppLanguage.Bulgarian -> "Верен: ${result.question.correctCountry.emoji} ${result.question.correctCountry.localizedName(language)}"
            AppLanguage.German -> "Richtig: ${result.question.correctCountry.emoji} ${result.question.correctCountry.localizedName(language)}"
          },
      )
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Your answer: ${result.selectedCountry?.localizedName(language) ?: result.typedAnswer.ifBlank { cleanText(language, UiText.NoAnswer) }}"
            AppLanguage.Bulgarian -> "Твой отговор: ${result.selectedCountry?.localizedName(language) ?: result.typedAnswer.ifBlank { cleanText(language, UiText.NoAnswer) }}"
            AppLanguage.German -> "Deine Antwort: ${result.selectedCountry?.localizedName(language) ?: result.typedAnswer.ifBlank { cleanText(language, UiText.NoAnswer) }}"
          },
      )
      if (wrongOptions.isNotEmpty()) {
        Text(
          text =
            when (language) {
              AppLanguage.English -> "Wrong options: ${wrongOptions.joinToString(", ") { wrongOptionLabel(it, result.question.variant, language) }}"
              AppLanguage.Bulgarian -> "Грешни опции: ${wrongOptions.joinToString(", ") { wrongOptionLabel(it, result.question.variant, language) }}"
              AppLanguage.German -> "Falsche Optionen: ${wrongOptions.joinToString(", ") { wrongOptionLabel(it, result.question.variant, language) }}"
            },
        )
      }
      Text(
        text =
          when (result.hintUses) {
            1 ->
              when (language) {
                AppLanguage.English -> "Hint streak is paused and stays ${result.hintStreak}."
                AppLanguage.Bulgarian -> "Серията за нов жокер е паузирана и остава ${result.hintStreak}."
                AppLanguage.German -> "Hinweis-Serie ist pausiert und bleibt bei ${result.hintStreak}."
              }
            2 ->
              when (language) {
                AppLanguage.English -> "Hint streak is reset."
                AppLanguage.Bulgarian -> "Серията за нов жокер се нулира."
                AppLanguage.German -> "Hinweis-Serie ist zurückgesetzt."
              }
            else ->
              when (language) {
                AppLanguage.English -> "No hint used - Streak is now ${result.hintStreak}."
                AppLanguage.Bulgarian -> "Без жокер - Серията за нов жокер: ${result.hintStreak}."
                AppLanguage.German -> "Kein Hinweis verwendet - Hinweis-Serie ist jetzt ${result.hintStreak}."
              }
          },
      )
    }
  }
}

private fun wrongOptionLabel(
  country: FlagCountry,
  variant: QuizVariant,
  language: AppLanguage,
): String =
  if (variant == QuizVariant.CountryToFlag) {
    country.emoji
  } else {
    country.localizedName(language)
  }

internal val AvatarOptions =
  listOf(
    "\uD83C\uDFAF",
    "\uD83C\uDFC6",
    "\uD83C\uDF0D",
    "\uD83D\uDD25",
    "\u2B50",
    "\uD83D\uDCA1",
    "\uD83E\uDDED",
    "\uD83D\uDC8E",
    "\uD83D\uDCAA",
    "\uD83C\uDF89",
    "\uD83C\uDFAE",
    "\uD83D\uDEA9",
    "\uD83C\uDF10",
    "\uD83E\uDD47",
    "\uD83D\uDE80",
    "\uD83E\uDD85",
    "\uD83E\uDD81",
    "\uD83D\uDC3A",
    "\uD83E\uDD8A",
    "\uD83D\uDC31",
    "\uD83D\uDC36",
    "\uD83D\uDC38",
    "\uD83D\uDC22",
    "\uD83D\uDC27",
    "\uD83D\uDC19",
    "\uD83E\uDD16",
    "\uD83E\uDDD9",
    "\uD83E\uDD77",
    "\uD83E\uDD20",
    "\uD83D\uDC51",
    "\uD83C\uDF1F",
    "\uD83C\uDF08",
    "\uD83E\uDE90",
    "\uD83C\uDFA8",
    "\uD83C\uDFB8",
    "\uD83C\uDFB5",
    "\uD83C\uDFB2",
    "\uD83C\uDFC0",
    "\u26BD",
    "\uD83C\uDFBE",
    "\uD83E\uDD4A",
    "\uD83C\uDF53",
    "\uD83C\uDF4E",
    "\uD83C\uDF4B",
    "\uD83C\uDF44",
    "\uD83C\uDF3B",
    "\uD83C\uDF35",
    "\uD83C\uDF32",
    "\uD83C\uDF1A",
    "\u2600\uFE0F",
  )

private fun avatarFor(index: Int): String = AvatarOptions.getOrElse(index) { AvatarOptions.first() }

private fun languageFlag(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "\uD83C\uDDEC\uD83C\uDDE7"
    AppLanguage.Bulgarian -> "\uD83C\uDDE7\uD83C\uDDEC"
    AppLanguage.German -> "\uD83C\uDDE9\uD83C\uDDEA"
  }

private fun languageName(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "English"
    AppLanguage.Bulgarian -> "Български"
    AppLanguage.German -> "Deutsch"
  }

private fun languageDescription(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "English (UK)"
    AppLanguage.Bulgarian -> "Български (BG)"
    AppLanguage.German -> "Deutsch (DE)"
  }

private fun localizedHintButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Hint"
    AppLanguage.Bulgarian -> "Жокер"
    AppLanguage.German -> "Hinweis"
  }

private fun localizedRevealButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Reveal"
    AppLanguage.Bulgarian -> "Разкрий"
    AppLanguage.German -> "Aufdecken"
  }

private fun localizedVerifyButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Verify"
    AppLanguage.Bulgarian -> "Провери"
    AppLanguage.German -> "Prüfen"
  }

private fun localizedQuizInfoButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Info $InfoSymbolText"
    AppLanguage.Bulgarian -> "Инфо $InfoSymbolText"
    AppLanguage.German -> "Info $InfoSymbolText"
  }

private fun localizedUnskipButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Unskip $UnskipArrowText"
    AppLanguage.Bulgarian -> "Върни $UnskipArrowText"
    AppLanguage.German -> "Zurück $UnskipArrowText"
  }
private fun formatScore(score: Int): String =
  if (score % 2 == 0) {
    (score / 2).toString()
  } else {
    "${score / 2}.5"
  }

private fun modeSelectionTitle(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Choose mode"
    AppLanguage.Bulgarian -> "Избери режим"
    AppLanguage.German -> "Modus wählen"
  }

private fun quizCompleteTitle(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Quiz complete"
    AppLanguage.Bulgarian -> "Тестът е завършен"
    AppLanguage.German -> "Quiz beendet"
  }

private fun localizedHeroPill(
  index: Int,
  language: AppLanguage,
): String =
  when (index) {
    0 ->
      when (language) {
        AppLanguage.English -> "Global"
        AppLanguage.Bulgarian -> "Глобално"
        AppLanguage.German -> "Global"
      }
    1 ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    else ->
      when (language) {
        AppLanguage.English -> "Flags"
        AppLanguage.Bulgarian -> "Флагове"
        AppLanguage.German -> "Flaggen"
      }
  }

private fun allInRewardInfo(
  language: AppLanguage,
  hintSettingLabel: String,
  hasAllVariants: Boolean,
  rewardLevels: String,
  isImpossible: Boolean,
): String =
  when (language) {
    AppLanguage.English ->
      if (hasAllVariants) {
        "Hint setting: $hintSettingLabel. Perfect clear reward is active. Finish with no mistakes using all 3 variants to earn $rewardLevels full level(s)." +
          if (isImpossible) "" else " Switch to 'The impossible one' to earn +1 more level, for +2 full levels total."
      } else {
        "Hint setting: $hintSettingLabel. Perfect clear reward is inactive because not all 3 variants are selected. Re-enable every variant to earn $rewardLevels full level(s)." +
          if (isImpossible) "" else " With 'The impossible one' enabled, that reward would become +2 full levels."
      }
    AppLanguage.Bulgarian ->
      if (hasAllVariants) {
        "Настройка за жокери: $hintSettingLabel. Наградата за перфектен тест е активна. Завърши без грешка с всичките 3 варианта, за да вземеш $rewardLevels пълно ниво." +
          if (isImpossible) "" else " Ако включиш 'The impossible one', ще получиш още +1 ниво, общо +2."
      } else {
        "Настройка за жокери: $hintSettingLabel. Наградата за перфектен тест е неактивна, защото не са избрани и 3-те варианта. Включи всички варианти, за да вземеш $rewardLevels пълно ниво." +
          if (isImpossible) "" else " С 'The impossible one' тази награда ще стане +2 пълни нива."
      }
    AppLanguage.German ->
      if (hasAllVariants) {
        "Hinweis-Einstellung: $hintSettingLabel. Die Belohnung für einen fehlerfreien Durchlauf ist aktiv. Beende das Quiz ohne Fehler mit allen 3 Varianten, um $rewardLevels volle Level zu erhalten." +
          if (isImpossible) "" else " Mit 'The impossible one' bekommst du +1 Level mehr, also insgesamt +2 volle Level."
      } else {
        "Hinweis-Einstellung: $hintSettingLabel. Die Belohnung für einen fehlerfreien Durchlauf ist inaktiv, weil nicht alle 3 Varianten ausgewählt sind. Aktiviere alle Varianten, um $rewardLevels volle Level zu erhalten." +
          if (isImpossible) "" else " Mit 'The impossible one' würde diese Belohnung +2 volle Level geben."
      }
  }

private fun modeBaseTitle(
  base: MultiplayerQuizBase,
  language: AppLanguage,
): String =
  when (base) {
    MultiplayerQuizBase.Continents ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    MultiplayerQuizBase.AllIn ->
      when (language) {
        AppLanguage.English -> "No Bluff, All Tough"
        AppLanguage.Bulgarian -> "Без блъф, само трудно"
        AppLanguage.German -> "Kein Bluff, alles hart"
      }
  }

private fun modeBaseDescription(
  base: MultiplayerQuizBase,
  language: AppLanguage,
): String =
  when (base) {
    MultiplayerQuizBase.Continents ->
      when (language) {
        AppLanguage.English -> "Quiz by selected continents."
        AppLanguage.Bulgarian -> "Тест по избрани континенти."
        AppLanguage.German -> "Quiz nach ausgewählten Kontinenten."
      }
    MultiplayerQuizBase.AllIn ->
      when (language) {
        AppLanguage.English -> "Quiz from all countries."
        AppLanguage.Bulgarian -> "Тест от всички държави."
        AppLanguage.German -> "Quiz mit allen Ländern."
      }
  }

private fun localizedModeTitle(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      when (language) {
        AppLanguage.English -> "Training"
        AppLanguage.Bulgarian -> "Тренировка"
        AppLanguage.German -> "Training"
      }
    GameMode.Continents ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    GameMode.AllIn ->
      when (language) {
        AppLanguage.English -> "No Bluff, All Tough"
        AppLanguage.Bulgarian -> "Без блъф, само трудно"
        AppLanguage.German -> "Kein Bluff, alles hart"
      }
    GameMode.LocalMultiplayer ->
      when (language) {
        AppLanguage.English -> "Local multiplayer"
        AppLanguage.Bulgarian -> "Локална игра"
        AppLanguage.German -> "Lokaler Mehrspieler"
      }
  }

private fun localizedModeDescription(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      when (language) {
        AppLanguage.English -> "Mix flags, country names, and typed answers at your pace. Training does not give level-up progress."
        AppLanguage.Bulgarian -> "Смесвай флагове, имена на държави и писмени отговори със свое темпо. Тренировката не дава прогрес към ниво."
        AppLanguage.German -> "Mische Flaggen, Ländernamen und Texteingaben in deinem Tempo. Training bringt keinen Level-Fortschritt."
      }
    GameMode.Continents ->
      when (language) {
        AppLanguage.English -> "Build a quiz from the continents you want to practice."
        AppLanguage.Bulgarian -> "Създай тест от континентите, които искаш да упражняваш."
        AppLanguage.German -> "Erstelle ein Quiz aus den Kontinenten, die du üben möchtest."
      }
    GameMode.AllIn ->
      when (language) {
        AppLanguage.English -> "All countries with only the variants you choose."
        AppLanguage.Bulgarian -> "Всички държави само с вариантите, които избереш."
        AppLanguage.German -> "Alle Länder mit nur den Varianten, die du auswählst."
      }
    GameMode.LocalMultiplayer ->
      when (language) {
        AppLanguage.English -> "Up to 5 players pass one device and play turn by turn."
        AppLanguage.Bulgarian -> "До 5 играчи използват едно устройство и играят поред."
        AppLanguage.German -> "Bis zu 5 Spieler teilen sich ein Gerät und spielen reihum."
      }
  }

private fun localizedModeShortLabel(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      when (language) {
        AppLanguage.English -> "Practice freely."
        AppLanguage.Bulgarian -> "Упражнявай свободно."
        AppLanguage.German -> "Frei üben."
      }
    GameMode.Continents ->
      when (language) {
        AppLanguage.English -> "Pick continents."
        AppLanguage.Bulgarian -> "Избери континенти."
        AppLanguage.German -> "Kontinente wählen."
      }
    GameMode.AllIn ->
      when (language) {
        AppLanguage.English -> "All countries."
        AppLanguage.Bulgarian -> "Всички държави."
        AppLanguage.German -> "Alle Länder."
      }
    GameMode.LocalMultiplayer ->
      when (language) {
        AppLanguage.English -> "Play together."
        AppLanguage.Bulgarian -> "Играй заедно."
        AppLanguage.German -> "Zusammen spielen."
      }
  }

private fun localizedVariantTitle(
  variant: QuizVariant,
  language: AppLanguage,
): String =
  when (variant) {
    QuizVariant.FlagToCountry ->
      when (language) {
        AppLanguage.English -> "Flag -> country"
        AppLanguage.Bulgarian -> "Флаг -> държава"
        AppLanguage.German -> "Flagge -> Land"
      }
    QuizVariant.CountryToFlag ->
      when (language) {
        AppLanguage.English -> "Country -> flag"
        AppLanguage.Bulgarian -> "Държава -> флаг"
        AppLanguage.German -> "Land -> Flagge"
      }
    QuizVariant.TypeCountryName ->
      when (language) {
        AppLanguage.English -> "Type the country"
        AppLanguage.Bulgarian -> "Напиши държавата"
        AppLanguage.German -> "Land eintippen"
      }
  }

private fun localizedVariantDescription(
  variant: QuizVariant,
  language: AppLanguage,
): String =
  when (variant) {
    QuizVariant.FlagToCountry ->
      when (language) {
        AppLanguage.English -> "See a flag and pick the country."
        AppLanguage.Bulgarian -> "Виж флаг и избери държавата."
        AppLanguage.German -> "Sieh eine Flagge und wähle das Land."
      }
    QuizVariant.CountryToFlag ->
      when (language) {
        AppLanguage.English -> "See a country and pick the flag."
        AppLanguage.Bulgarian -> "Виж държава и избери флага."
        AppLanguage.German -> "Sieh ein Land und wähle die Flagge."
      }
    QuizVariant.TypeCountryName ->
      when (language) {
        AppLanguage.English -> "See a flag and write the country name."
        AppLanguage.Bulgarian -> "Виж флаг и напиши името на държавата."
        AppLanguage.German -> "Sieh eine Flagge und tippe den Ländernamen."
      }
  }

private fun localizedHintDifficultyTitle(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie ->
      when (language) {
        AppLanguage.English -> "Easy (Every 3-streak)"
        AppLanguage.Bulgarian -> "Лесно (Всеки 3 поредни ✔)"
        AppLanguage.German -> "Einsteiger (Alle 3 in Folge)"
      }
    HintDifficulty.Medium ->
      when (language) {
        AppLanguage.English -> "Medium (Every 5-streak)"
        AppLanguage.Bulgarian -> "Средно (5 поредни ✔)"
        AppLanguage.German -> "Mittel (Alle 5 in Folge)"
      }
    HintDifficulty.Hard ->
      when (language) {
        AppLanguage.English -> "Hard (Every 10-streak)"
        AppLanguage.Bulgarian -> "Трудно (10 поредни ✔)"
        AppLanguage.German -> "Schwer (Alle 10 in Folge)"
      }
    HintDifficulty.Impossible ->
      when (language) {
        AppLanguage.English -> "Impossible (Every 50-streak)"
        AppLanguage.Bulgarian -> "Невъзможно (50 поредни ✔)"
        AppLanguage.German -> "Unmöglich (Alle 50 in Folge)"
      }
  }

private fun localizedHintDifficultyShortRule(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie ->
      when (language) {
        AppLanguage.English -> "Every 3-streak"
        AppLanguage.Bulgarian -> "Всеки 3 поредни верни"
        AppLanguage.German -> "Alle 3 in Folge"
      }
    HintDifficulty.Medium ->
      when (language) {
        AppLanguage.English -> "Every 5-streak"
        AppLanguage.Bulgarian -> "Всеки 5 верни поред"
        AppLanguage.German -> "Alle 5 in Folge"
      }
    HintDifficulty.Hard ->
      when (language) {
        AppLanguage.English -> "Every 10-streak"
        AppLanguage.Bulgarian -> "Всеки 10 верни поред"
        AppLanguage.German -> "Alle 10 in Folge"
      }
    HintDifficulty.Impossible ->
      when (language) {
        AppLanguage.English -> "Every 50-streak"
        AppLanguage.Bulgarian -> "Всеки 50 верни поред"
        AppLanguage.German -> "Alle 50 in Folge"
      }
  }

private fun localizedHintDifficultyDescription(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie ->
      when (language) {
        AppLanguage.English -> "Collect 1 hint for every 3 correct answers in a row."
        AppLanguage.Bulgarian -> "Събирай 1 жокер за всеки 3 верни отговора подред."
        AppLanguage.German -> "Sammle 1 Hinweis für je 3 richtige Antworten in Folge."
      }
    HintDifficulty.Medium ->
      when (language) {
        AppLanguage.English -> "Collect 1 hint for every 5 correct answers in a row."
        AppLanguage.Bulgarian -> "Събирай 1 жокер на всеки 5 верни отговора поред."
        AppLanguage.German -> "Sammle 1 Hinweis für jeweils 5 richtige Antworten in Folge."
      }
    HintDifficulty.Hard ->
      when (language) {
        AppLanguage.English -> "Collect 1 hint for every 10 correct answers in a row."
        AppLanguage.Bulgarian -> "Събирай 1 жокер на всеки 10 верни отговора поред."
        AppLanguage.German -> "Sammle 1 Hinweis für jeweils 10 richtige Antworten in Folge."
      }
    HintDifficulty.Impossible ->
      when (language) {
        AppLanguage.English -> "Collect 1 hint for every 50 correct answers in a row."
        AppLanguage.Bulgarian -> "Събирай 1 жокер на всеки 50 верни отговора поред."
        AppLanguage.German -> "Sammle 1 Hinweis für jeweils 50 richtige Antworten in Folge."
      }
  }

private fun localizedMedalTitle(
  medalTier: MedalTier,
  language: AppLanguage,
): String =
  when (medalTier) {
    MedalTier.Bronze ->
      when (language) {
        AppLanguage.English -> "Bronze"
        AppLanguage.Bulgarian -> "Бронз"
        AppLanguage.German -> "Bronze"
      }
    MedalTier.Silver ->
      when (language) {
        AppLanguage.English -> "Silver"
        AppLanguage.Bulgarian -> "Сребро"
        AppLanguage.German -> "Silber"
      }
    MedalTier.Gold ->
      when (language) {
        AppLanguage.English -> "Gold"
        AppLanguage.Bulgarian -> "Злато"
        AppLanguage.German -> "Gold"
      }
    MedalTier.Titanium ->
      when (language) {
        AppLanguage.English -> "Platinum"
        AppLanguage.Bulgarian -> "Платина"
        AppLanguage.German -> "Platin"
      }
    MedalTier.Diamond ->
      when (language) {
        AppLanguage.English -> "Diamond"
        AppLanguage.Bulgarian -> "Диамант"
        AppLanguage.German -> "Diamant"
      }
  }

private fun localizedMedalLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Perfect quiz count"
    AppLanguage.Bulgarian -> "Брояч за перфектни тестове"
    AppLanguage.German -> "Zähler für fehlerfreie Quizze"
  }

private fun localizedMedalIntro(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Perfect quiz counters"
    AppLanguage.Bulgarian -> "Броячи за перфектни тестове"
    AppLanguage.German -> "Zähler für fehlerfreie Quizze"
  }

private fun localizedMedalInfo(language: AppLanguage): String =
  when (language) {
    AppLanguage.English ->
      "Bronze: finish a 10-24 question quiz with 0 mistakes.\n" +
        "Silver: finish a 25-49 question quiz with 0 mistakes.\n" +
        "Gold: finish a 50-99 question quiz with 0 mistakes.\n" +
        "Platinum: finish a 100-194 question quiz with 0 mistakes.\n" +
        "Diamond: finish all 195 countries with 0 mistakes.\n" +
        "Hints are allowed for medals."
    AppLanguage.Bulgarian ->
      "Бронз: завърши тест с 10-24 въпроса без грешка.\n" +
        "Сребро: завърши тест с 25-49 въпроса без грешка.\n" +
        "Злато: завърши тест с 50-99 въпроса без грешка.\n" +
        "Платина: завърши тест с 100-194 въпроса без грешка.\n" +
        "Диамант: завърши всички 195 държави без грешка.\n" +
        "Жокерите са позволени за медалите."
    AppLanguage.German ->
      "Bronze: beende ein Quiz mit 10-24 Fragen und 0 Fehlern.\n" +
        "Silber: beende ein Quiz mit 25-49 Fragen und 0 Fehlern.\n" +
        "Gold: beende ein Quiz mit 50-99 Fragen und 0 Fehlern.\n" +
        "Platin: beende ein Quiz mit 100-194 Fragen und 0 Fehlern.\n" +
        "Diamant: beende alle 195 Länder mit 0 Fehlern.\n" +
        "Hinweise sind für Medaillen erlaubt."
  }

private fun localizedAvatarUnlockInfo(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "You unlock 5 more profile icons every time you level up. At level 1 you start with 5 unlocked icons, at level 2 you have 10, and so on."
    AppLanguage.Bulgarian -> "При всяко качване на ниво отключваш по 5 нови профилни икони. На ниво 1 започваш с 5 отключени икони, на ниво 2 имаш 10 и така нататък."
    AppLanguage.German -> "Mit jedem Levelaufstieg schaltest du 5 weitere Profilsymbole frei. Auf Level 1 startest du mit 5 freigeschalteten Symbolen, auf Level 2 hast du 10 und so weiter."
  }

private fun localizedAchievementSectorTitle(
  sector: AchievementSector,
  language: AppLanguage,
): String =
  when (sector) {
    AchievementSector.Continents ->
      when (language) {
        AppLanguage.English -> "Continent masters"
        AppLanguage.Bulgarian -> "Майстори на континенти"
        AppLanguage.German -> "Kontinent-Meister"
      }
    AchievementSector.World ->
      when (language) {
        AppLanguage.English -> "World runs"
        AppLanguage.Bulgarian -> "Световни серии"
        AppLanguage.German -> "Weltläufe"
      }
    AchievementSector.Collectors ->
      when (language) {
        AppLanguage.English -> "Collectors"
        AppLanguage.Bulgarian -> "Събирачи"
        AppLanguage.German -> "Sammler"
      }
    AchievementSector.Skill ->
      when (language) {
        AppLanguage.English -> "Skill feats"
        AppLanguage.Bulgarian -> "Умения"
        AppLanguage.German -> "Skill-Leistungen"
      }
  }

private fun localizedTitle(
  language: AppLanguage,
  en: String,
  bg: String,
  de: String,
): String =
  when (language) {
    AppLanguage.English -> en
    AppLanguage.Bulgarian -> bg
    AppLanguage.German -> de
  }

private fun localizedAchievementTitle(
  achievementId: AchievementId,
  language: AppLanguage,
): String =
  when (achievementId) {
    AchievementId.AfricaPerfect -> localizedTitle(language, "Africa Perfect", "Африка без грешка", "Afrika fehlerfrei")
    AchievementId.AsiaPerfect -> localizedTitle(language, "Asia Perfect", "Азия без грешка", "Asien fehlerfrei")
    AchievementId.EuropePerfect -> localizedTitle(language, "Europe Perfect", "Европа без грешка", "Europa fehlerfrei")
    AchievementId.NorthAmericaPerfect -> localizedTitle(language, "North America Perfect", "Северна Америка без грешка", "Nordamerika fehlerfrei")
    AchievementId.OceaniaPerfect -> localizedTitle(language, "Oceania Perfect", "Океания без грешка", "Ozeanien fehlerfrei")
    AchievementId.SouthAmericaPerfect -> localizedTitle(language, "South America Perfect", "Южна Америка без грешка", "Südamerika fehlerfrei")
    AchievementId.DiamondWorld -> localizedTitle(language, "Diamond World", "Диамантен свят", "Diamantene Welt")
    AchievementId.NoBluffLegend -> localizedTitle(language, "No Bluff Legend", "Легенда без блъф", "Kein Bluff Legende")
    AchievementId.WorldPurist -> localizedTitle(language, "World Purist", "Световен пурист", "Weltpurist")
    AchievementId.BronzeCollector -> localizedTitle(language, "Bronze Collector", "Събирач на бронз", "Bronze-Sammler")
    AchievementId.SilverCollector -> localizedTitle(language, "Silver Collector", "Събирач на сребро", "Silber-Sammler")
    AchievementId.GoldCollector -> localizedTitle(language, "Gold Collector", "Събирач на злато", "Gold-Sammler")
    AchievementId.PlatinumCollector -> localizedTitle(language, "Platinum Collector", "Събирач на платина", "Platin-Sammler")
    AchievementId.DiamondCollector -> localizedTitle(language, "Diamond Collector", "Събирач на диаманти", "Diamant-Sammler")
    AchievementId.FirstPerfect -> localizedTitle(language, "First Perfect", "Първи перфектен", "Erstes Perfekt")
    AchievementId.HintlessHero -> localizedTitle(language, "Hintless Hero", "Герой без жокери", "Hinweisloser Held")
    AchievementId.VariantMaster -> localizedTitle(language, "Variant Master", "Майстор на варианти", "Variantenmeister")
  }

private fun localizedAchievementDescription(
  achievementId: AchievementId,
  language: AppLanguage,
): String =
  when (achievementId) {
    AchievementId.AfricaPerfect -> localizedTitle(language, "Finish an Africa-only quiz with 0 mistakes.", "Завърши тест само за Африка без грешка.", "Beende ein Quiz nur für Afrika ohne Fehler.")
    AchievementId.AsiaPerfect -> localizedTitle(language, "Finish an Asia-only quiz with 0 mistakes.", "Завърши тест само за Азия без грешка.", "Beende ein Quiz nur für Asien ohne Fehler.")
    AchievementId.EuropePerfect -> localizedTitle(language, "Finish an Europe-only quiz with 0 mistakes.", "Завърши тест само за Европа без грешка.", "Beende ein Quiz nur für Europa ohne Fehler.")
    AchievementId.NorthAmericaPerfect -> localizedTitle(language, "Finish a North America-only quiz with 0 mistakes.", "Завърши тест само за Северна Америка без грешка.", "Beende ein Quiz nur für Nordamerika ohne Fehler.")
    AchievementId.OceaniaPerfect -> localizedTitle(language, "Finish an Oceania-only quiz with 0 mistakes.", "Завърши тест само за Океания без грешка.", "Beende ein Quiz nur für Ozeanien ohne Fehler.")
    AchievementId.SouthAmericaPerfect -> localizedTitle(language, "Finish a South America-only quiz with 0 mistakes.", "Завърши тест само за Южна Америка без грешка.", "Beende ein Quiz nur für Südamerika ohne Fehler.")
    AchievementId.DiamondWorld -> localizedTitle(language, "Finish all countries with 0 mistakes.", "Завърши всички държави без грешка.", "Beende alle Länder ohne Fehler.")
    AchievementId.NoBluffLegend -> localizedTitle(language, "Finish No Bluff, All Tough with all three variants selected and 0 mistakes.", "Завърши Без блъф, само трудно с трите варианта и без грешка.", "Beende Kein Bluff, alles hart mit allen drei Varianten und ohne Fehler.")
    AchievementId.WorldPurist -> localizedTitle(language, "Finish all countries with 0 mistakes and no hints.", "Завърши всички държави без грешка и без жокери.", "Beende alle Länder ohne Fehler und ohne Hinweise.")
    AchievementId.BronzeCollector -> localizedTitle(language, "Earn bronze 50 times.", "Спечели бронз 50 пъти.", "Bronze 50-mal verdienen.")
    AchievementId.SilverCollector -> localizedTitle(language, "Earn silver 25 times.", "Спечели сребро 25 пъти.", "Silber 25-mal verdienen.")
    AchievementId.GoldCollector -> localizedTitle(language, "Earn gold 10 times.", "Спечели злато 10 пъти.", "Gold 10-mal verdienen.")
    AchievementId.PlatinumCollector -> localizedTitle(language, "Earn platinum 5 times.", "Спечели платина 5 пъти.", "Platin 5-mal verdienen.")
    AchievementId.DiamondCollector -> localizedTitle(language, "Earn diamond 1 time.", "Спечели диамант 1 път.", "Diamant 1-mal verdienen.")
    AchievementId.FirstPerfect -> localizedTitle(language, "Finish any medal-eligible quiz with 100% correct answers.", "Завърши всеки тест за медал с 100% верни отговори.", "Beende ein medaillenfähiges Quiz mit 100% richtigen Antworten.")
    AchievementId.HintlessHero -> localizedTitle(language, "Finish a perfect medal-eligible quiz without using hints.", "Завърши перфектен тест за медал без жокери.", "Beende ein perfektes medaillenfähiges Quiz ohne Hinweise.")
    AchievementId.VariantMaster -> localizedTitle(language, "Finish a perfect quiz that includes all three question variants.", "Завърши перфектен тест с трите варианта въпроси.", "Beende ein perfektes Quiz mit allen drei Fragetypen.")
  }

private fun localizedAchievementHint(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Tap an achievement to see its unlock clue."
    AppLanguage.Bulgarian -> "Докосни постижение, за да видиш подсказка за отключване."
    AppLanguage.German -> "Tippe auf einen Erfolg, um den Freischalt-Hinweis zu sehen."
  }

private fun localizedAchievementStatus(
  language: AppLanguage,
  unlockedAt: Long?,
): String =
  if (unlockedAt == null) {
    when (language) {
      AppLanguage.English -> "Locked"
      AppLanguage.Bulgarian -> "Заключено"
      AppLanguage.German -> "Gesperrt"
    }
  } else {
    when (language) {
      AppLanguage.English -> "Unlocked on ${formatAchievementDate(unlockedAt)}"
      AppLanguage.Bulgarian -> "Отключено на ${formatAchievementDate(unlockedAt)}"
      AppLanguage.German -> "Freigeschaltet am ${formatAchievementDate(unlockedAt)}"
    }
  }
private fun cleanText(
  language: AppLanguage,
  text: UiText,
): String =
  when (text) {
    UiText.WorldFlagGame ->
      when (language) {
        AppLanguage.English -> "World flag game"
        AppLanguage.Bulgarian -> "Познай флага"
        AppLanguage.German -> "Flaggen-Spiel"
      }
    UiText.HeroSubtitle ->
      when (language) {
        AppLanguage.English -> "Practice flags, earn achievements, collect medals, and track your progress across every country."
        AppLanguage.Bulgarian -> "Упражнявай флагове, отключвай постижения, събирай медали и следи напредъка си."
        AppLanguage.German -> "Übe Flaggen, sammle Erfolge und Medaillen und verfolge deinen Fortschritt."
      }
    UiText.Start ->
      when (language) {
        AppLanguage.English -> "Start"
        AppLanguage.Bulgarian -> "Старт"
        AppLanguage.German -> "Start"
      }
    UiText.Medals ->
      when (language) {
        AppLanguage.English -> "Medals"
        AppLanguage.Bulgarian -> "Медали"
        AppLanguage.German -> "Medaillen"
      }
    UiText.Achievements ->
      when (language) {
        AppLanguage.English -> "Achievements"
        AppLanguage.Bulgarian -> "Постижения"
        AppLanguage.German -> "Erfolge"
      }
    UiText.Settings ->
      when (language) {
        AppLanguage.English -> "Settings"
        AppLanguage.Bulgarian -> "Настройки"
        AppLanguage.German -> "Einstellungen"
      }
    UiText.Quit ->
      when (language) {
        AppLanguage.English -> "Quit"
        AppLanguage.Bulgarian -> "Изход"
        AppLanguage.German -> "Beenden"
      }
    UiText.Open ->
      when (language) {
        AppLanguage.English -> "Open"
        AppLanguage.Bulgarian -> "Отвори"
        AppLanguage.German -> "Öffnen"
      }
    UiText.Level ->
      when (language) {
        AppLanguage.English -> "Level"
        AppLanguage.Bulgarian -> "Ниво"
        AppLanguage.German -> "Level"
      }
    UiText.Profile ->
      when (language) {
        AppLanguage.English -> "Profile"
        AppLanguage.Bulgarian -> "Профил"
        AppLanguage.German -> "Profil"
      }
    UiText.AccountName ->
      when (language) {
        AppLanguage.English -> "Account name"
        AppLanguage.Bulgarian -> "Име на профила"
        AppLanguage.German -> "Profilname"
      }
    UiText.ChooseProfileIcon ->
      when (language) {
        AppLanguage.English -> "Choose profile icon"
        AppLanguage.Bulgarian -> "Избери икона на профила"
        AppLanguage.German -> "Profilsymbol wählen"
      }
    UiText.Close ->
      when (language) {
        AppLanguage.English -> "Close"
        AppLanguage.Bulgarian -> "Затвори"
        AppLanguage.German -> "Schließen"
      }
    UiText.LeaveQuizTitle ->
      when (language) {
        AppLanguage.English -> "Leave quiz?"
        AppLanguage.Bulgarian -> "Да напуснеш теста?"
        AppLanguage.German -> "Quiz verlassen?"
      }
    UiText.LeaveQuizBody ->
      when (language) {
        AppLanguage.English -> "This quiz will not count toward results, newly earned hints, medals, achievements, or level progression."
        AppLanguage.Bulgarian -> "Този тест няма да се брои за резултатите, новите жокери, медали, постижения или прогреса към ниво."
        AppLanguage.German -> "Dieses Quiz zählt nicht für Ergebnisse, neu verdiente Hinweise, Medaillen, Erfolge oder den Level-Fortschritt."
      }
    UiText.Leave ->
      when (language) {
        AppLanguage.English -> "Leave"
        AppLanguage.Bulgarian -> "Напусни"
        AppLanguage.German -> "Verlassen"
      }
    UiText.Stay ->
      when (language) {
        AppLanguage.English -> "Stay"
        AppLanguage.Bulgarian -> "Остани"
        AppLanguage.German -> "Bleiben"
      }
    UiText.Finish ->
      when (language) {
        AppLanguage.English -> "Finish"
        AppLanguage.Bulgarian -> "Завърши"
        AppLanguage.German -> "Fertig"
      }
    UiText.GuessTheFlag ->
      when (language) {
        AppLanguage.English -> "Guess the flag"
        AppLanguage.Bulgarian -> "Познай флага"
        AppLanguage.German -> "Errate die Flagge"
      }
    UiText.QuizInfo ->
      when (language) {
        AppLanguage.English -> "Score is revealed at the end. Newly earned hints and level progress count only after finishing the full quiz."
        AppLanguage.Bulgarian -> "Резултатът се показва накрая. Новите жокери и прогресът към ниво се броят само след завършване на целия тест."
        AppLanguage.German -> "Die Punktzahl wird am Ende gezeigt. Neu verdiente Hinweise und Level-Fortschritt zählen erst nach dem vollständigen Quiz."
      }
    UiText.NextUp ->
      when (language) {
        AppLanguage.English -> "Next up"
        AppLanguage.Bulgarian -> "Следва"
        AppLanguage.German -> "Als Nächstes"
      }
    UiText.Unanswered ->
      when (language) {
        AppLanguage.English -> "Unanswered"
        AppLanguage.Bulgarian -> "Неотговорени"
        AppLanguage.German -> "Unbeantwortet"
      }
    UiText.Skipped ->
      when (language) {
        AppLanguage.English -> "Skipped"
        AppLanguage.Bulgarian -> "Пропуснати"
        AppLanguage.German -> "Übersprungen"
      }
    UiText.CorrectAnswers ->
      when (language) {
        AppLanguage.English -> "correct answers"
        AppLanguage.Bulgarian -> "верни отговори"
        AppLanguage.German -> "richtige Antworten"
      }
    UiText.CompletedTests ->
      when (language) {
        AppLanguage.English -> "completed tests"
        AppLanguage.Bulgarian -> "завършени тестове"
        AppLanguage.German -> "abgeschlossene Tests"
      }
    UiText.LevelUpTitle ->
      when (language) {
        AppLanguage.English -> "Level up!"
        AppLanguage.Bulgarian -> "Ново ниво!"
        AppLanguage.German -> "Levelaufstieg!"
      }
    UiText.LevelUpBody ->
      when (language) {
        AppLanguage.English -> "You reached level %1\$d and earned 5 free hints."
        AppLanguage.Bulgarian -> "Достигна ниво %1\$d и получи 5 безплатни жокера."
        AppLanguage.German -> "Du hast Level %1\$d erreicht und 5 kostenlose Hinweise erhalten."
      }
    else -> t(language, text)
  }

private fun cleanModeSelectionTitle(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Choose mode"
    AppLanguage.Bulgarian -> "Избери режим"
    AppLanguage.German -> "Modus wählen"
  }

private fun cleanHeroPill(
  index: Int,
  language: AppLanguage,
): String =
  when (index) {
    0 ->
      when (language) {
        AppLanguage.English -> "Global"
        AppLanguage.Bulgarian -> "Глобално"
        AppLanguage.German -> "Global"
      }
    1 ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    else ->
      when (language) {
        AppLanguage.English -> "Flags"
        AppLanguage.Bulgarian -> "Флагове"
        AppLanguage.German -> "Flaggen"
      }
  }

private fun cleanModeTitle(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      when (language) {
        AppLanguage.English -> "Training"
        AppLanguage.Bulgarian -> "Тренировка"
        AppLanguage.German -> "Training"
      }
    GameMode.Continents ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    GameMode.AllIn ->
      when (language) {
        AppLanguage.English -> "No Bluff, All Tough"
        AppLanguage.Bulgarian -> "Без блъф, само трудно"
        AppLanguage.German -> "Kein Bluff, alles hart"
      }
    GameMode.LocalMultiplayer ->
      when (language) {
        AppLanguage.English -> "Local multiplayer"
        AppLanguage.Bulgarian -> "Локална игра"
        AppLanguage.German -> "Lokaler Mehrspieler"
      }
  }

private fun cleanModeShortLabel(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      when (language) {
        AppLanguage.English -> "Practice freely."
        AppLanguage.Bulgarian -> "Упражнявай свободно."
        AppLanguage.German -> "Frei üben."
      }
    GameMode.Continents ->
      when (language) {
        AppLanguage.English -> "Pick continents."
        AppLanguage.Bulgarian -> "Избери континенти."
        AppLanguage.German -> "Kontinente wählen."
      }
    GameMode.AllIn ->
      when (language) {
        AppLanguage.English -> "All countries."
        AppLanguage.Bulgarian -> "Всички държави."
        AppLanguage.German -> "Alle Länder."
      }
    GameMode.LocalMultiplayer ->
      when (language) {
        AppLanguage.English -> "Play together."
        AppLanguage.Bulgarian -> "Играй заедно."
        AppLanguage.German -> "Zusammen spielen."
      }
  }

private fun cleanModeDescription(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      when (language) {
        AppLanguage.English -> "Mix flags, country names, and typed answers at your pace. Training does not give level-up progress."
        AppLanguage.Bulgarian -> "Смесвай флагове, имена на държави и писмени отговори със свое темпо. Тренировката не дава прогрес към ниво."
        AppLanguage.German -> "Mische Flaggen, Ländernamen und Texteingaben in deinem Tempo. Training bringt keinen Level-Fortschritt."
      }
    GameMode.Continents ->
      when (language) {
        AppLanguage.English -> "Build a quiz from the continents you want to practice."
        AppLanguage.Bulgarian -> "Създай тест от континентите, които искаш да упражняваш."
        AppLanguage.German -> "Erstelle ein Quiz aus den Kontinenten, die du üben möchtest."
      }
    GameMode.AllIn ->
      when (language) {
        AppLanguage.English -> "All countries with only the variants you choose."
        AppLanguage.Bulgarian -> "Всички държави само с вариантите, които избереш."
        AppLanguage.German -> "Alle Länder mit nur den Varianten, die du auswählst."
      }
    GameMode.LocalMultiplayer ->
      when (language) {
        AppLanguage.English -> "Up to 5 players pass one device and play turn by turn."
        AppLanguage.Bulgarian -> "До 5 играчи използват едно устройство и играят поред."
        AppLanguage.German -> "Bis zu 5 Spieler teilen sich ein Gerät und spielen reihum."
      }
  }

private fun cleanMedalIntro(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Perfect quiz counters"
    AppLanguage.Bulgarian -> "Броячи за перфектни тестове"
    AppLanguage.German -> "Zähler für fehlerfreie Quizze"
  }

private fun cleanMedalInfo(language: AppLanguage): String =
  when (language) {
    AppLanguage.English ->
      "Bronze: finish a 10-24 question quiz with 0 mistakes.\n" +
        "Silver: finish a 25-49 question quiz with 0 mistakes.\n" +
        "Gold: finish a 50-99 question quiz with 0 mistakes.\n" +
        "Platinum: finish a 100-194 question quiz with 0 mistakes.\n" +
        "Diamond: finish all 195 countries with 0 mistakes.\n" +
        "Hints are allowed for medals."
    AppLanguage.Bulgarian ->
      "Бронз: завърши тест с 10-24 въпроса без грешка.\n" +
        "Сребро: завърши тест с 25-49 въпроса без грешка.\n" +
        "Злато: завърши тест с 50-99 въпроса без грешка.\n" +
        "Платина: завърши тест със 100-194 въпроса без грешка.\n" +
        "Диамант: завърши всички 195 държави без грешка.\n" +
        "Жокерите са позволени за медали."
    AppLanguage.German ->
      "Bronze: Beende ein Quiz mit 10-24 Fragen ohne Fehler.\n" +
        "Silber: Beende ein Quiz mit 25-49 Fragen ohne Fehler.\n" +
        "Gold: Beende ein Quiz mit 50-99 Fragen ohne Fehler.\n" +
        "Platin: Beende ein Quiz mit 100-194 Fragen ohne Fehler.\n" +
        "Diamant: Beende alle 195 Länder ohne Fehler.\n" +
        "Hinweise sind für Medaillen erlaubt."
  }

private fun cleanMedalTitle(
  medalTier: MedalTier,
  language: AppLanguage,
): String =
  when (medalTier) {
    MedalTier.Bronze ->
      when (language) {
        AppLanguage.English -> "Bronze"
        AppLanguage.Bulgarian -> "Бронз"
        AppLanguage.German -> "Bronze"
      }
    MedalTier.Silver ->
      when (language) {
        AppLanguage.English -> "Silver"
        AppLanguage.Bulgarian -> "Сребро"
        AppLanguage.German -> "Silber"
      }
    MedalTier.Gold ->
      when (language) {
        AppLanguage.English -> "Gold"
        AppLanguage.Bulgarian -> "Злато"
        AppLanguage.German -> "Gold"
      }
    MedalTier.Titanium ->
      when (language) {
        AppLanguage.English -> "Platinum"
        AppLanguage.Bulgarian -> "Платина"
        AppLanguage.German -> "Platin"
      }
    MedalTier.Diamond ->
      when (language) {
        AppLanguage.English -> "Diamond"
        AppLanguage.Bulgarian -> "Диамант"
        AppLanguage.German -> "Diamant"
      }
  }

private enum class UiText {
  ChooseMode,
  WorldFlagGame,
  HeroSubtitle,
  Menu,
  Start,
  Medals,
  Achievements,
  Settings,
  Quit,
  Profile,
  AccountName,
  ProfileIcon,
  ChangeIcon,
  ChooseProfileIcon,
  Save,
  Cancel,
  Close,
  Language,
  AppLanguage,
  Hints,
  CollectedHints,
  AddTenHints,
  ResetHints,
  SwitchToNormalIcon,
  SwitchToInactiveIcon,
  SendTestReminder,
  ResetAchievementsAndMedals,
  IconStatusInactive,
  IconStatusNormal,
  Player,
  Remove,
  AddPlayer,
  QuizBase,
  Continents,
  QuestionCount,
  AmountOfQuestions,
  ExampleQuestionCount,
  AllowedRange,
  PerfectRunNoMedal,
  UseCustomAmount,
  SurpriseMe,
  StartQuiz,
  LeaveQuizTitle,
  LeaveQuizBody,
  ExitAppTitle,
  ExitAppBody,
  Exit,
  Leave,
  Stay,
  QuizInfo,
  GuessTheFlag,
  CountryName,
  HintStartsWith,
  Hint,
  Skip,
  Unskip,
  Finish,
  Next,
  PlayAgain,
  QuizComplete,
  FinalResults,
  AnswerReview,
  CorrectAnswers,
  Skipped,
  Unanswered,
  Unsure,
  NetScore,
  HintPointsAvailable,
  QuestionReview,
  Correct,
  YourAnswer,
  NoAnswer,
  HintUsed,
  NoHintUsed,
  CompletedTests,
  Level,
  NextUp,
  NextLevelRequirements,
  LanguageLabel,
  Open,
  MedalsCountLabel,
  PerfectQuizCount,
  LevelUpTitle,
  LevelUpBody,
  QuestionPromptFlag,
  QuestionPromptCountry,
}

private fun t(
  language: AppLanguage,
  text: UiText,
): String =
  when (text) {
    UiText.ChooseMode ->
      when (language) {
        AppLanguage.English -> "Choose mode"
        AppLanguage.Bulgarian -> "Избери режим"
        AppLanguage.German -> "Modus wählen"
      }
    UiText.WorldFlagGame -> cleanText(language, UiText.WorldFlagGame)
    UiText.HeroSubtitle -> cleanText(language, UiText.HeroSubtitle)
    UiText.Menu ->
      when (language) {
        AppLanguage.English -> "Menu"
        AppLanguage.Bulgarian -> "Меню"
        AppLanguage.German -> "Menü"
      }
    UiText.Start -> cleanText(language, UiText.Start)
    UiText.Medals -> cleanText(language, UiText.Medals)
    UiText.Achievements -> cleanText(language, UiText.Achievements)
    UiText.Settings -> cleanText(language, UiText.Settings)
    UiText.Quit -> cleanText(language, UiText.Quit)
    UiText.Profile ->
      when (language) {
        AppLanguage.English -> "Profile"
        AppLanguage.Bulgarian -> "Профил"
        AppLanguage.German -> "Profil"
      }
    UiText.AccountName ->
      when (language) {
        AppLanguage.English -> "Account name"
        AppLanguage.Bulgarian -> "Име на профила"
        AppLanguage.German -> "Profilname"
      }
    UiText.ProfileIcon ->
      when (language) {
        AppLanguage.English -> "Profile icon"
        AppLanguage.Bulgarian -> "Икона на профила"
        AppLanguage.German -> "Profilsymbol"
      }
    UiText.ChangeIcon ->
      when (language) {
        AppLanguage.English -> "Change icon"
        AppLanguage.Bulgarian -> "Промени иконата"
        AppLanguage.German -> "Symbol ändern"
      }
    UiText.ChooseProfileIcon ->
      when (language) {
        AppLanguage.English -> "Choose profile icon"
        AppLanguage.Bulgarian -> "Избери икона на профила"
        AppLanguage.German -> "Profilsymbol wählen"
      }
    UiText.Save ->
      when (language) {
        AppLanguage.English -> "Save"
        AppLanguage.Bulgarian -> "Запази"
        AppLanguage.German -> "Speichern"
      }
    UiText.Cancel ->
      when (language) {
        AppLanguage.English -> "Cancel"
        AppLanguage.Bulgarian -> "Отказ"
        AppLanguage.German -> "Abbrechen"
      }
    UiText.Close ->
      when (language) {
        AppLanguage.English -> "Close"
        AppLanguage.Bulgarian -> "Затвори"
        AppLanguage.German -> "Schließen"
      }
    UiText.Language,
    UiText.LanguageLabel ->
      when (language) {
        AppLanguage.English -> "Language"
        AppLanguage.Bulgarian -> "Език"
        AppLanguage.German -> "Sprache"
      }
    UiText.AppLanguage ->
      when (language) {
        AppLanguage.English -> "App language"
        AppLanguage.Bulgarian -> "Език на приложението"
        AppLanguage.German -> "App-Sprache"
      }
    UiText.Hints ->
      when (language) {
        AppLanguage.English -> "Hints"
        AppLanguage.Bulgarian -> "Жокери"
        AppLanguage.German -> "Hinweise"
      }
    UiText.CollectedHints ->
      when (language) {
        AppLanguage.English -> "Collected hints"
        AppLanguage.Bulgarian -> "Събрани жокери"
        AppLanguage.German -> "Gesammelte Hinweise"
      }
    UiText.AddTenHints ->
      when (language) {
        AppLanguage.English -> "Add 10 hints"
        AppLanguage.Bulgarian -> "Добави 10 жокера"
        AppLanguage.German -> "10 Hinweise hinzufügen"
      }
    UiText.ResetHints ->
      when (language) {
        AppLanguage.English -> "Reset hints"
        AppLanguage.Bulgarian -> "Нулирай жокерите"
        AppLanguage.German -> "Hinweise zurücksetzen"
      }
    UiText.SwitchToNormalIcon ->
      when (language) {
        AppLanguage.English -> "Switch to normal icon"
        AppLanguage.Bulgarian -> "Премини към нормалната икона"
        AppLanguage.German -> "Zum normalen Symbol wechseln"
      }
    UiText.SwitchToInactiveIcon ->
      when (language) {
        AppLanguage.English -> "Switch to inactive icon"
        AppLanguage.Bulgarian -> "Премини към неактивната икона"
        AppLanguage.German -> "Zum inaktiven Symbol wechseln"
      }
    UiText.SendTestReminder ->
      when (language) {
        AppLanguage.English -> "Send test reminder"
        AppLanguage.Bulgarian -> "Изпрати тестово напомняне"
        AppLanguage.German -> "Test-Erinnerung senden"
      }
    UiText.ResetAchievementsAndMedals ->
      when (language) {
        AppLanguage.English -> "Reset medals"
        AppLanguage.Bulgarian -> "Нулирай медалите"
        AppLanguage.German -> "Medaillen zurücksetzen"
      }
    UiText.IconStatusInactive ->
      when (language) {
        AppLanguage.English -> "Icon status: inactive test icon enabled"
        AppLanguage.Bulgarian -> "Статус на иконата: неактивна"
        AppLanguage.German -> "Symbolstatus: inaktives Testsymbol aktiviert"
      }
    UiText.IconStatusNormal ->
      when (language) {
        AppLanguage.English -> "Icon status: normal icon enabled"
        AppLanguage.Bulgarian -> "Статус на иконата: активна"
        AppLanguage.German -> "Symbolstatus: normales Symbol aktiviert"
      }
    UiText.Player ->
      when (language) {
        AppLanguage.English -> "Player"
        AppLanguage.Bulgarian -> "Играч"
        AppLanguage.German -> "Spieler"
      }
    UiText.Remove ->
      when (language) {
        AppLanguage.English -> "Remove"
        AppLanguage.Bulgarian -> "Премахни"
        AppLanguage.German -> "Entfernen"
      }
    UiText.AddPlayer ->
      when (language) {
        AppLanguage.English -> "Add player"
        AppLanguage.Bulgarian -> "Добави играч"
        AppLanguage.German -> "Spieler hinzufügen"
      }
    UiText.QuizBase ->
      when (language) {
        AppLanguage.English -> "Quiz base"
        AppLanguage.Bulgarian -> "Основа на теста"
        AppLanguage.German -> "Quiz-Basis"
      }
    UiText.Continents ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    UiText.QuestionCount,
    UiText.AmountOfQuestions ->
      when (language) {
        AppLanguage.English -> "Amount of questions"
        AppLanguage.Bulgarian -> "Брой въпроси"
        AppLanguage.German -> "Anzahl der Fragen"
      }
    UiText.ExampleQuestionCount ->
      when (language) {
        AppLanguage.English -> "Example: 10"
        AppLanguage.Bulgarian -> "Пример: 10"
        AppLanguage.German -> "Beispiel: 10"
      }
    UiText.AllowedRange ->
      when (language) {
        AppLanguage.English -> "Allowed range"
        AppLanguage.Bulgarian -> "Позволен диапазон"
        AppLanguage.German -> "Erlaubter Bereich"
      }
    UiText.PerfectRunNoMedal ->
      when (language) {
        AppLanguage.English -> "Quizzes under 10 questions do not earn medals."
        AppLanguage.Bulgarian -> "Тестове под 10 въпроса не дават медали."
        AppLanguage.German -> "Quizze unter 10 Fragen bringen keine Medaillen."
      }
    UiText.UseCustomAmount ->
      when (language) {
        AppLanguage.English -> "Use custom amount"
        AppLanguage.Bulgarian -> "Използвай свой брой"
        AppLanguage.German -> "Eigene Anzahl verwenden"
      }
    UiText.SurpriseMe ->
      when (language) {
        AppLanguage.English -> "Surprise me!"
        AppLanguage.Bulgarian -> "Изненадай ме!"
        AppLanguage.German -> "Überrasch mich!"
      }
    UiText.StartQuiz ->
      when (language) {
        AppLanguage.English -> "Start quiz"
        AppLanguage.Bulgarian -> "Започни теста"
        AppLanguage.German -> "Quiz starten"
      }
    UiText.LeaveQuizTitle ->
      when (language) {
        AppLanguage.English -> "Leave quiz?"
        AppLanguage.Bulgarian -> "Да напуснеш теста?"
        AppLanguage.German -> "Quiz verlassen?"
      }
    UiText.LeaveQuizBody ->
      when (language) {
        AppLanguage.English -> "This quiz will not count toward results, newly earned hints, medals, achievements, or level progression."
        AppLanguage.Bulgarian -> "Този тест няма да се брои към резултати, нови жокери, медали, постижения или прогрес към ниво."
        AppLanguage.German -> "Dieses Quiz zählt nicht für Ergebnisse, neue Hinweise, Medaillen, Erfolge oder Level-Fortschritt."
      }
    UiText.ExitAppTitle ->
      when (language) {
        AppLanguage.English -> "Exit app?"
        AppLanguage.Bulgarian -> "Изход от приложението?"
        AppLanguage.German -> "App schließen?"
      }
    UiText.ExitAppBody ->
      when (language) {
        AppLanguage.English -> "Do you really want to close World Flag Game?"
        AppLanguage.Bulgarian -> "Наистина ли искаш да затвориш играта?"
        AppLanguage.German -> "Möchtest du World Flag Game wirklich schließen?"
      }
    UiText.Exit ->
      when (language) {
        AppLanguage.English -> "Exit"
        AppLanguage.Bulgarian -> "Изход"
        AppLanguage.German -> "Schließen"
      }
    UiText.Leave ->
      when (language) {
        AppLanguage.English -> "Leave"
        AppLanguage.Bulgarian -> "Напусни"
        AppLanguage.German -> "Verlassen"
      }
    UiText.Stay ->
      when (language) {
        AppLanguage.English -> "Stay"
        AppLanguage.Bulgarian -> "Остани"
        AppLanguage.German -> "Bleiben"
      }
    UiText.QuizInfo ->
      when (language) {
        AppLanguage.English -> "Score is revealed at the end. Newly earned hints and level progress count only after finishing the full quiz."
        AppLanguage.Bulgarian -> "Резултатът се показва накрая. Новите жокери и прогресът се броят само след завършен тест."
        AppLanguage.German -> "Der Punktestand wird am Ende gezeigt. Neue Hinweise und Fortschritt zählen erst nach dem vollständigen Quiz."
      }
    UiText.GuessTheFlag,
    UiText.QuestionPromptFlag ->
      when (language) {
        AppLanguage.English -> "Guess the flag"
        AppLanguage.Bulgarian -> "Познай флага"
        AppLanguage.German -> "Errate die Flagge"
      }
    UiText.CountryName,
    UiText.QuestionPromptCountry ->
      when (language) {
        AppLanguage.English -> "Country name"
        AppLanguage.Bulgarian -> "Име на държава"
        AppLanguage.German -> "Ländername"
      }
    UiText.HintStartsWith ->
      when (language) {
        AppLanguage.English -> "Starts with"
        AppLanguage.Bulgarian -> "Започва с"
        AppLanguage.German -> "Beginnt mit"
      }
    UiText.Hint ->
      when (language) {
        AppLanguage.English -> "Hint"
        AppLanguage.Bulgarian -> "Жокер"
        AppLanguage.German -> "Hinweis"
      }
    UiText.Skip ->
      when (language) {
        AppLanguage.English -> UnskipArrowText
        AppLanguage.Bulgarian -> UnskipArrowText
        AppLanguage.German -> UnskipArrowText
      }
    UiText.Unskip ->
      when (language) {
        AppLanguage.English -> UnskipArrowText
        AppLanguage.Bulgarian -> UnskipArrowText
        AppLanguage.German -> UnskipArrowText
      }
    UiText.Finish ->
      when (language) {
        AppLanguage.English -> "Finish"
        AppLanguage.Bulgarian -> "Финал"
        AppLanguage.German -> "Fertig"
      }
    UiText.Next ->
      when (language) {
        AppLanguage.English -> "Next"
        AppLanguage.Bulgarian -> "Напред"
        AppLanguage.German -> "Weiter"
      }
    UiText.PlayAgain ->
      when (language) {
        AppLanguage.English -> "Play again"
        AppLanguage.Bulgarian -> "Играй отново"
        AppLanguage.German -> "Nochmal spielen"
      }
    UiText.QuizComplete ->
      when (language) {
        AppLanguage.English -> "Quiz complete"
        AppLanguage.Bulgarian -> "Тестът е завършен"
        AppLanguage.German -> "Quiz beendet"
      }
    UiText.FinalResults ->
      when (language) {
        AppLanguage.English -> "Final results"
        AppLanguage.Bulgarian -> "Крайни резултати"
        AppLanguage.German -> "Endergebnisse"
      }
    UiText.AnswerReview ->
      when (language) {
        AppLanguage.English -> "Answer review"
        AppLanguage.Bulgarian -> "Преглед на отговорите"
        AppLanguage.German -> "Antwortübersicht"
      }
    UiText.CorrectAnswers -> cleanText(language, UiText.CorrectAnswers)
    UiText.Skipped ->
      when (language) {
        AppLanguage.English -> "Skipped"
        AppLanguage.Bulgarian -> "Прескочени"
        AppLanguage.German -> "Übersprungen"
      }
    UiText.Unanswered ->
      when (language) {
        AppLanguage.English -> "Unanswered"
        AppLanguage.Bulgarian -> "Неотговорени"
        AppLanguage.German -> "Unbeantwortet"
      }
    UiText.Unsure ->
      when (language) {
        AppLanguage.English -> "Unsure"
        AppLanguage.Bulgarian -> "Несигурни"
        AppLanguage.German -> "Unsicher"
      }
    UiText.NetScore ->
      when (language) {
        AppLanguage.English -> "Score"
        AppLanguage.Bulgarian -> "Резултат"
        AppLanguage.German -> "Punktestand"
      }
    UiText.HintPointsAvailable ->
      when (language) {
        AppLanguage.English -> "Won hint points"
        AppLanguage.Bulgarian -> "Спечелени жокери"
        AppLanguage.German -> "Gewonnene Hinweise"
      }
    UiText.QuestionReview ->
      when (language) {
        AppLanguage.English -> "Question"
        AppLanguage.Bulgarian -> "Въпрос"
        AppLanguage.German -> "Frage"
      }
    UiText.Correct ->
      when (language) {
        AppLanguage.English -> "Correct"
        AppLanguage.Bulgarian -> "Правилен отговор"
        AppLanguage.German -> "Richtig"
      }
    UiText.YourAnswer ->
      when (language) {
        AppLanguage.English -> "Your answer"
        AppLanguage.Bulgarian -> "Твоят отговор"
        AppLanguage.German -> "Deine Antwort"
      }
    UiText.NoAnswer ->
      when (language) {
        AppLanguage.English -> "No answer"
        AppLanguage.Bulgarian -> "Няма отговор"
        AppLanguage.German -> "Keine Antwort"
      }
    UiText.HintUsed ->
      when (language) {
        AppLanguage.English -> "Hint streak is paused and stays X."
        AppLanguage.Bulgarian -> "Серията за нов жокер е на пауза и остава X."
        AppLanguage.German -> "Die Hinweis-Serie pausiert und bleibt X."
      }
    UiText.NoHintUsed ->
      when (language) {
        AppLanguage.English -> "No hint used - Streak is now X."
        AppLanguage.Bulgarian -> "Без използван жокер - серията за нов жокер вече е X."
        AppLanguage.German -> "Kein Hinweis verwendet - die Hinweis-Serie ist jetzt X."
      }
    UiText.CompletedTests -> cleanText(language, UiText.CompletedTests)
    UiText.Level -> cleanText(language, UiText.Level)
    UiText.NextUp ->
      when (language) {
        AppLanguage.English -> "Next up"
        AppLanguage.Bulgarian -> "Следва"
        AppLanguage.German -> "Als Nächstes"
      }
    UiText.NextLevelRequirements ->
      when (language) {
        AppLanguage.English -> "Next level requirements"
        AppLanguage.Bulgarian -> "Изисквания за следващо ниво"
        AppLanguage.German -> "Anforderungen für das nächste Level"
      }
    UiText.Open -> cleanText(language, UiText.Open)
    UiText.MedalsCountLabel ->
      when (language) {
        AppLanguage.English -> "Medals"
        AppLanguage.Bulgarian -> "Медали"
        AppLanguage.German -> "Medaillen"
      }
    UiText.PerfectQuizCount ->
      when (language) {
        AppLanguage.English -> "Perfect quiz count"
        AppLanguage.Bulgarian -> "Брой перфектни тестове"
        AppLanguage.German -> "Anzahl fehlerfreier Quizze"
      }
    UiText.LevelUpTitle ->
      when (language) {
        AppLanguage.English -> "Level up!"
        AppLanguage.Bulgarian -> "Ново ниво!"
        AppLanguage.German -> "Levelaufstieg!"
      }
    UiText.LevelUpBody ->
      when (language) {
        AppLanguage.English -> "You reached level %1\$d and earned 5 free hints."
        AppLanguage.Bulgarian -> "Достигна ниво %1\$d и получи 5 безплатни жокера."
        AppLanguage.German -> "Du hast Level %1\$d erreicht und 5 kostenlose Hinweise erhalten."
      }
  }

private fun legacyT(
  language: AppLanguage,
  text: UiText,
): String =
  when (language) {
    AppLanguage.English ->
      when (text) {
        UiText.WorldFlagGame -> "World flag game"
        UiText.HeroSubtitle -> "Practice flags, earn achievements, collect medals, and track your progress across every country."
        UiText.Menu -> "Menu"
        UiText.Start -> "Start"
        UiText.Medals -> "Medals"
        UiText.Achievements -> "Achievements"
        UiText.Settings -> "Settings"
        UiText.Quit -> "Quit"
        UiText.Profile -> "Profile"
        UiText.AccountName -> "Account name"
        UiText.Language -> "Language"
        UiText.Hints -> "Hints"
        UiText.CollectedHints -> "Collected hints"
        UiText.AddTenHints -> "Add 10 hints"
        UiText.ResetHints -> "Reset hints"
        UiText.SwitchToNormalIcon -> "Switch to normal icon"
        UiText.SwitchToInactiveIcon -> "Switch to inactive icon"
        UiText.SendTestReminder -> "Send test reminder"
        UiText.ResetAchievementsAndMedals -> "Reset medals"
        UiText.CorrectAnswers -> "correct answers"
        UiText.CompletedTests -> "completed tests"
        UiText.Level -> "Level"
        UiText.LeaveQuizTitle -> "Leave quiz?"
        UiText.LeaveQuizBody -> "This quiz will not count toward results, newly earned hints, medals, achievements, or level progression."
        UiText.ExitAppTitle -> "Exit app?"
        UiText.ExitAppBody -> "Do you really want to close World Flag Game?"
        UiText.Exit -> "Exit"
        UiText.Leave -> "Leave"
        UiText.Stay -> "Stay"
        UiText.QuizInfo -> "Score is revealed at the end. Newly earned hints and level progress count only after finishing the full quiz."
        UiText.NextUp -> "Next up"
        UiText.ChooseProfileIcon -> "Choose profile icon"
        UiText.ChangeIcon -> "Change icon"
        UiText.Save -> "Save"
        UiText.Cancel -> "Cancel"
        UiText.Close -> "Close"
        UiText.Continents -> "Continents"
        UiText.GuessTheFlag -> "Guess the flag"
        UiText.CountryName -> "Country name"
        UiText.Hint -> "Hint"
        UiText.Skip -> UnskipArrowText
        UiText.Unskip -> UnskipArrowText
        UiText.Finish -> "Finish"
        UiText.Next -> "Next"
        UiText.PlayAgain -> "Play again"
        UiText.FinalResults -> "Final results"
        UiText.AnswerReview -> "Answer review"
        UiText.Skipped -> "Skipped"
        UiText.NetScore -> "Net score"
        UiText.HintPointsAvailable -> "Won hint points"
        UiText.Correct -> "Correct"
        UiText.YourAnswer -> "Your answer"
        UiText.NoAnswer -> "No answer"
        UiText.HintUsed -> "Hint streak is paused and stays X."
        UiText.NoHintUsed -> "No hint used"
        UiText.NextLevelRequirements -> "Next level requirements"
        UiText.Open -> "Open"
        UiText.LevelUpTitle -> "Level up!"
        UiText.LevelUpBody -> "You reached level %1\$d and earned 5 free hints."
        else ->
          when (text) {
            UiText.Unanswered -> "Unanswered"
            UiText.Unsure -> "Unsure"
            else -> ""
          }
      }
    AppLanguage.Bulgarian ->
      when (text) {
        UiText.WorldFlagGame -> "Р В Р’В Р РЋРЎСџР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В·Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂњР В Р’В Р вЂ™Р’В°"
        UiText.HeroSubtitle -> "Р В Р’В Р РЋРЎвЂєР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’Вµ, Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В»Р В Р Р‹Р В РІР‚в„–Р В Р Р‹Р Р†Р вЂљР Р‹Р В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СћР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В¶Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В Р РЏ, Р В Р Р‹Р В РЎвЂњР В Р Р‹Р В РІР‚В°Р В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚В Р В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р РЋРІР‚В Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р Р‹Р В РІР‚В°Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В° Р В Р Р‹Р В РЎвЂњР В Р’В Р РЋРІР‚В."
        UiText.Menu -> "Р В Р’В Р РЋРЎв„ўР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р В РІР‚в„–"
        UiText.Start -> "Р В Р’В Р В Р вЂ№Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°Р В Р Р‹Р В РІР‚С™Р В Р Р‹Р Р†Р вЂљРЎв„ў"
        UiText.Medals -> "Р В Р’В Р РЋРЎв„ўР В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚В"
        UiText.Achievements -> "Р В Р’В Р РЋРЎСџР В Р’В Р РЋРІР‚СћР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В¶Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В Р РЏ"
        UiText.Settings -> "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р Р†РІР‚С›РІР‚вЂњР В Р’В Р РЋРІР‚СњР В Р’В Р РЋРІР‚В"
        UiText.Quit -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р вЂ™Р’В·Р В Р Р‹Р Р†Р вЂљР’В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р СћРІР‚В"
        UiText.Profile -> "Р В Р’В Р РЋРЎСџР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В»"
        UiText.AccountName -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’Вµ Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В° Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В°"
        UiText.Language -> "Р В Р’В Р Р†Р вЂљРЎС›Р В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚ВР В Р’В Р РЋРІР‚Сњ"
        UiText.Hints -> "Р В Р’В Р Р†Р вЂљРІР‚СљР В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В"
        UiText.CollectedHints -> "Р В Р’В Р В Р вЂ№Р В Р Р‹Р В РІР‚В°Р В Р’В Р вЂ™Р’В±Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В"
        UiText.AddTenHints -> "Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚В 10 Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°"
        UiText.ResetHints -> "Р В Р’В Р РЋРЎС™Р В Р Р‹Р РЋРІР‚СљР В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’Вµ"
        UiText.SwitchToNormalIcon -> "Р В Р’В Р В Р вЂ№Р В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚СњР В Р Р‹Р В РІР‚В°Р В Р’В Р РЋР’В Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В° Р В Р’В Р РЋРІР‚ВР В Р’В Р РЋРІР‚СњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°"
        UiText.SwitchToInactiveIcon -> "Р В Р’В Р В Р вЂ№Р В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚СњР В Р Р‹Р В РІР‚В°Р В Р’В Р РЋР’В Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚СњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В° Р В Р’В Р РЋРІР‚ВР В Р’В Р РЋРІР‚СњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°"
        UiText.SendTestReminder -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚В Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚Сћ Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СћР В Р’В Р РЋР’ВР В Р’В Р В РІР‚В¦Р В Р Р‹Р В Р РЏР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’Вµ"
        UiText.ResetAchievementsAndMedals -> "Р В Р’В Р РЋРЎС™Р В Р Р‹Р РЋРІР‚СљР В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’Вµ"
        UiText.CorrectAnswers -> "Р В Р’В Р Р†Р вЂљРІвЂћСћР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В"
        UiText.CompletedTests -> "Р В Р’В Р Р†Р вЂљРІР‚СњР В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р Р‹Р В РІР‚В°Р В Р Р‹Р В РІР‚С™Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’Вµ"
        UiText.Level -> "Р В Р’В Р РЋРЎС™Р В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚Сћ"
        UiText.LeaveQuizTitle -> "Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р вЂ™Р’В° Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†РІР‚С™Р’В¬ Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°?"
        UiText.LeaveQuizBody -> "Р В Р’В Р РЋРЎвЂєР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚В Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р’В Р В РІР‚В¦Р В Р Р‹Р В Р РЏР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’В° Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В° Р В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’Вµ Р В Р’В Р вЂ™Р’В±Р В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚В Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В° Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В·Р В Р Р‹Р РЋРІР‚СљР В Р’В Р вЂ™Р’В»Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚В, Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚В Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В, Р В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚В, Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СћР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В¶Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В Р РЏ Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚В Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚Сћ."
        UiText.ExitAppTitle -> "Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р вЂ™Р’В° Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†РІР‚С™Р’В¬ Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В¶Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚Сћ?"
        UiText.ExitAppBody -> "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В° Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РЎвЂњР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†РІР‚С™Р’В¬ Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В° Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†РІР‚С™Р’В¬ World Flag Game?"
        UiText.Exit -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р вЂ™Р’В·Р В Р Р‹Р Р†Р вЂљР’В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р СћРІР‚В"
        UiText.Leave -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В·"
        UiText.Stay -> "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В"
        UiText.QuizInfo -> "Р В Р’В Р вЂ™Р’В Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В·Р В Р Р‹Р РЋРІР‚СљР В Р’В Р вЂ™Р’В»Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚В°Р В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’Вµ Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В·Р В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В° Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚СњР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р В Р РЏ. Р В Р’В Р РЋРЎС™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’Вµ Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚вЂњР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р В РІР‚В°Р В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’Вµ Р В Р’В Р вЂ™Р’В±Р В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В Р РЏР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋР’ВР В Р’В Р РЋРІР‚Сћ Р В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚В Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р Р‹Р В РІР‚В°Р В Р Р‹Р В РІР‚С™Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦ Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ў."
        UiText.NextUp -> "Р В Р’В Р В Р вЂ№Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°"
        UiText.ChooseProfileIcon -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В»Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В° Р В Р’В Р РЋРІР‚ВР В Р’В Р РЋРІР‚СњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°"
        UiText.ChangeIcon -> "Р В Р’В Р РЋРЎСџР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚ВР В Р’В Р РЋРІР‚СњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°"
        UiText.Save -> "Р В Р’В Р Р†Р вЂљРІР‚СњР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂќР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚В"
        UiText.Cancel -> "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В·"
        UiText.Close -> "Р В Р’В Р Р†Р вЂљРІР‚СњР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В"
        UiText.Continents -> "Р В Р’В Р РЋРІвЂћСћР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚В"
        UiText.GuessTheFlag -> "Р В Р’В Р РЋРЎСџР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В·Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂњР В Р’В Р вЂ™Р’В°"
        UiText.CountryName -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’Вµ Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В° Р В Р’В Р СћРІР‚ВР В Р Р‹Р В РІР‚В°Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В¶Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°"
        UiText.Hint -> "Р В Р’В Р Р†Р вЂљРІР‚СљР В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™"
        UiText.Skip -> UnskipArrowText
        UiText.Unskip -> UnskipArrowText
        UiText.Finish -> "Р В Р’В Р вЂ™Р’В¤Р В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»"
        UiText.Next -> "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚В"
        UiText.PlayAgain -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р РЋРІР‚вЂњР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњ Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚Сћ"
        UiText.FinalResults -> "Р В Р’В Р РЋРІвЂћСћР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р Р†РІР‚С›РІР‚вЂњР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В·Р В Р Р‹Р РЋРІР‚СљР В Р’В Р вЂ™Р’В»Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚В"
        UiText.AnswerReview -> "Р В Р’В Р РЋРЎСџР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋРІР‚вЂњР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚В Р В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В° Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’Вµ"
        UiText.Skipped -> "Р В Р’В Р РЋРЎСџР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚вЂќР В Р Р‹Р РЋРІР‚СљР В Р Р‹Р В РЎвЂњР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚В"
        UiText.NetScore -> "Р В Р’В Р РЋРІвЂћСћР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦ Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В·Р В Р Р‹Р РЋРІР‚СљР В Р’В Р вЂ™Р’В»Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ў"
        UiText.HintPointsAvailable -> "Р В Р’В Р В Р вЂ№Р В Р’В Р РЋРІР‚вЂќР В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљР Р‹Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В"
        UiText.Correct -> "Р В Р’В Р Р†Р вЂљРІвЂћСћР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦ Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™"
        UiText.YourAnswer -> "Р В Р’В Р РЋРЎвЂєР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В Р РЏР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™"
        UiText.NoAnswer -> "Р В Р’В Р Р†Р вЂљР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В· Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™"
        UiText.HintUsed -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В·Р В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦ Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™"
        UiText.NoHintUsed -> "Р В Р’В Р Р†Р вЂљР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В· Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В·Р В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦ Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™"
        UiText.NextLevelRequirements -> "Р В Р’В Р вЂ™Р’ВР В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РЎвЂњР В Р’В Р РЋРІР‚СњР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В Р РЏ Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В° Р В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљР’В°Р В Р’В Р РЋРІР‚Сћ Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚Сћ"
        UiText.Open -> "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚В"
        UiText.LevelUpTitle -> "Р В Р’В Р РЋРІвЂћСћР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљР Р‹Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚Сћ Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚Сћ!"
        UiText.LevelUpBody -> "Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р РЋРІР‚СћР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р РЋРІР‚вЂњР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В° Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚Сћ %1\$d Р В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В»Р В Р Р‹Р РЋРІР‚СљР В Р Р‹Р Р†Р вЂљР Р‹Р В Р’В Р РЋРІР‚В 5 Р В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В·Р В Р’В Р РЋРІР‚вЂќР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В Р В Р’В Р вЂ™Р’В¶Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°."
        else ->
          when (text) {
            UiText.Unanswered -> "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В"
            UiText.Unsure -> "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р’В Р РЋРІР‚ВР В Р’В Р РЋРІР‚вЂњР В Р Р‹Р РЋРІР‚СљР В Р Р‹Р В РІР‚С™Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚В"
            else -> ""
          }
      }
    AppLanguage.German ->
      when (text) {
        UiText.WorldFlagGame -> "Flagge erraten"
        UiText.HeroSubtitle -> "Trainiere Flagge, sammle Erfolge und Medaillen und verfolge deinen Fortschritt."
        UiText.Menu -> "MenР В РІР‚СљР РЋР’В"
        UiText.Start -> "Start"
        UiText.Medals -> "Medaillen"
        UiText.Achievements -> "Erfolge"
        UiText.Settings -> "Einstellungen"
        UiText.Quit -> "Beenden"
        UiText.Profile -> "Profil"
        UiText.AccountName -> "Profilname"
        UiText.Language -> "Sprache"
        UiText.Hints -> "Hinweise"
        UiText.CollectedHints -> "Gesammelte Hinweise"
        UiText.AddTenHints -> "10 Hinweise hinzufР В РІР‚СљР РЋР’Вgen"
        UiText.ResetHints -> "Hinweise zurР В РІР‚СљР РЋР’Вcksetzen"
        UiText.SwitchToNormalIcon -> "Zum normalen Symbol wechseln"
        UiText.SwitchToInactiveIcon -> "Zum inaktiven Symbol wechseln"
        UiText.SendTestReminder -> "Test-Erinnerung senden"
        UiText.ResetAchievementsAndMedals -> "Medaillen zurР В РІР‚СљР РЋР’Вcksetzen"
        UiText.CorrectAnswers -> "richtige Antworten"
        UiText.CompletedTests -> "abgeschlossene Tests"
        UiText.Level -> "Level"
        UiText.LeaveQuizTitle -> "Quiz verlassen?"
        UiText.LeaveQuizBody -> "Dieses Quiz zР В РІР‚СљР вЂ™Р’В¤hlt nicht fР В РІР‚СљР РЋР’Вr Ergebnisse, neue Hinweise, Medaillen, Erfolge oder Level-Fortschritt."
        UiText.ExitAppTitle -> "App schlieР В РІР‚СљР РЋРЎСџen?"
        UiText.ExitAppBody -> "MР В РІР‚СљР вЂ™Р’В¶chtest du World Flag Game wirklich schlieР В РІР‚СљР РЋРЎСџen?"
        UiText.Exit -> "SchlieР В РІР‚СљР РЋРЎСџen"
        UiText.Leave -> "Verlassen"
        UiText.Stay -> "Bleiben"
        UiText.QuizInfo -> "Der Score wird am Ende gezeigt. Neue Hinweise und Fortschritt zР В РІР‚СљР вЂ™Р’В¤hlen erst nach dem vollstР В РІР‚СљР вЂ™Р’В¤ndigen Quiz."
        UiText.NextUp -> "Als NР В РІР‚СљР вЂ™Р’В¤chstes"
        UiText.ChooseProfileIcon -> "Profilsymbol wР В РІР‚СљР вЂ™Р’В¤hlen"
        UiText.ChangeIcon -> "Symbol Р В РІР‚СљР вЂ™Р’В¤ndern"
        UiText.Save -> "Speichern"
        UiText.Cancel -> "Abbrechen"
        UiText.Close -> "SchlieР В РІР‚СљР РЋРЎСџen"
        UiText.Continents -> "Kontinente"
        UiText.GuessTheFlag -> "Errate die Flagge"
        UiText.CountryName -> "LР В РІР‚СљР вЂ™Р’В¤ndername"
        UiText.Hint -> "Hinweis"
        UiText.Skip -> UnskipArrowText
        UiText.Unskip -> UnskipArrowText
        UiText.Finish -> "Fertig"
        UiText.Next -> "Weiter"
        UiText.PlayAgain -> "Nochmal spielen"
        UiText.FinalResults -> "Endergebnisse"
        UiText.AnswerReview -> "AntwortР В РІР‚СљР РЋР’Вbersicht"
        UiText.Skipped -> "Р В РІР‚СљР РЋРЎв„ўbersprungen"
        UiText.NetScore -> "Punktestand"
        UiText.HintPointsAvailable -> "Hinweise gewonnen"
        UiText.Correct -> "Richtig"
        UiText.YourAnswer -> "Deine Antwort"
        UiText.NoAnswer -> "Keine Antwort"
        UiText.HintUsed -> "Hinweis verwendet"
        UiText.NoHintUsed -> "Kein Hinweis verwendet"
        UiText.NextLevelRequirements -> "Anforderungen fР В РІР‚СљР РЋР’Вr das nР В РІР‚СљР вЂ™Р’В¤chste Level"
        UiText.Open -> "Р В РІР‚СљР Р†Р вЂљРІР‚Сљffnen"
        UiText.LevelUpTitle -> "Levelaufstieg!"
        UiText.LevelUpBody -> "Du hast Level %1\$d erreicht und 5 kostenlose Hinweise erhalten."
        else ->
          when (text) {
            UiText.Unanswered -> "Nicht beantwortet"
            UiText.Unsure -> "Unsicher"
            else -> ""
          }
      }
  }

private fun levelUpBody(language: AppLanguage, level: Int): String =
  when (language) {
    AppLanguage.English -> "You reached level $level and earned 5 free hints."
    AppLanguage.Bulgarian -> "Достигна ниво $level и получи 5 безплатни жокера."
    AppLanguage.German -> "Du hast Level $level erreicht und 5 kostenlose Hinweise erhalten."
  }

private fun displayModeTitle(
  mode: GameMode?,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> cleanModeTitle(GameMode.Training, language)
    GameMode.Continents -> cleanModeTitle(GameMode.Continents, language)
    GameMode.AllIn -> cleanModeTitle(GameMode.AllIn, language)
    GameMode.LocalMultiplayer -> cleanModeTitle(GameMode.LocalMultiplayer, language)
    null ->
      when (language) {
        AppLanguage.English -> "Quiz"
        AppLanguage.Bulgarian -> "Тест"
        AppLanguage.German -> "Quiz"
      }
  }

@Composable
private fun buttonContentColor(background: Color): Color {
  return when (background) {
    MaterialTheme.colorScheme.primary -> MaterialTheme.colorScheme.onPrimary
    MaterialTheme.colorScheme.surfaceVariant -> MaterialTheme.colorScheme.onSurface
    else -> if (background.luminance() > 0.5f) Color.Black else Color.White
  }
}








package com.example.flaggameandroid.feature.app

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.theme.AccentGreen
import com.example.flaggameandroid.theme.AccentRed
import kotlinx.coroutines.delay

@Composable
fun FlagGameRoute(
  screenViewModel: FlagGameViewModel = viewModel { FlagGameViewModel() },
) {
  val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
  val activity = LocalContext.current as? Activity

  when (uiState.screen) {
    AppScreen.Menu ->
      MenuScreen(
        levelProgress = uiState.levelProgress,
        onStartClick = screenViewModel::onStartClicked,
        onSettingsClick = screenViewModel::onSettingsClicked,
        onQuitClick = { activity?.finish() },
        onLevelUpSeen = screenViewModel::onLevelUpSeen,
      )
    AppScreen.GameModes ->
      GameModesScreen(
        onBack = screenViewModel::onBackToMenu,
        onModeSelected = screenViewModel::onModeSelected,
      )
    AppScreen.Settings ->
      SettingsScreen(
        settings = uiState.settings,
        hintCount = uiState.hintCount,
        onBack = screenViewModel::onBackToMenu,
        onHintDifficultySelected = screenViewModel::onHintDifficultySelected,
        onResetHintsClick = screenViewModel::onResetHintsClicked,
        onAddTestingHintsClick = screenViewModel::onAddTestingHintsClicked,
      )
    AppScreen.Setup ->
      SetupScreen(
        setup = uiState.setup,
        availableContinents = uiState.availableContinents,
        questionCountLimit = uiState.questionCountLimit,
        setupError = uiState.setupError,
        onBack = screenViewModel::onBackToMenu,
        onVariantToggle = screenViewModel::onVariantToggled,
        onContinentToggle = screenViewModel::onContinentToggled,
        onQuestionCountChange = screenViewModel::onQuestionCountChanged,
        onSurpriseMe = screenViewModel::onSurpriseMeClicked,
        onAllInTypeSelected = screenViewModel::onAllInTypeSelected,
        onMultiplayerBaseSelected = screenViewModel::onMultiplayerBaseSelected,
        onPlayerNameChanged = screenViewModel::onPlayerNameChanged,
        onAddPlayer = screenViewModel::onAddPlayer,
        onRemovePlayer = screenViewModel::onRemovePlayer,
        onStartQuiz = screenViewModel::onStartQuiz,
      )
    AppScreen.Quiz ->
      QuizScreen(
        quiz = uiState.quiz,
        onBackToMenu = screenViewModel::onBackToMenu,
        onCountryAnswerSelected = screenViewModel::onCountryAnswerSelected,
        onTypedAnswerChanged = screenViewModel::onTypedAnswerChanged,
        onUseHint = screenViewModel::onUseHint,
        onNextQuestion = screenViewModel::onNextQuestion,
        onSkipQuestion = screenViewModel::onSkipQuestion,
      )
    AppScreen.Results ->
      ResultsScreen(
        quiz = uiState.quiz,
        levelProgress = uiState.levelProgress,
        onPlayAgain = screenViewModel::onPlayAgain,
        onBackToMenu = screenViewModel::onBackToMenu,
        onLevelUpSeen = screenViewModel::onLevelUpSeen,
      )
  }
}

@Composable
fun MenuScreen(
  levelProgress: LevelProgressState,
  onStartClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onQuitClick: () -> Unit,
  onLevelUpSeen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    LevelProgressPanel(levelProgress = levelProgress, onLevelUpSeen = onLevelUpSeen)

    HeroPanel(
      title = "Flag Game Android",
      subtitle = "A sharper world quiz with training, continents, all-in challenges, and local multiplayer.",
    )

    SectionCard(title = "Menu") {
      Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Text("Start")
      }
      OutlinedButton(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Text("Settings")
      }
      OutlinedButton(onClick = onQuitClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Text("Quit")
      }
    }
  }
}

@Composable
fun GameModesScreen(
  onBack: () -> Unit,
  onModeSelected: (GameMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expandedInfoMode by remember { mutableStateOf<GameMode?>(null) }

  ScreenShell(modifier = modifier) {
    HeaderRow(title = "Choose mode", onBack = onBack)

    GameMode.entries.forEach { mode ->
      ModeCard(
        mode = mode,
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
fun SettingsScreen(
  settings: SettingsState,
  hintCount: Int,
  onBack: () -> Unit,
  onHintDifficultySelected: (HintDifficulty) -> Unit,
  onResetHintsClick: () -> Unit,
  onAddTestingHintsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var expandedDifficulty by remember { mutableStateOf<HintDifficulty?>(null) }
  var testingButtonEnabled by remember { mutableStateOf(true) }

  LaunchedEffect(testingButtonEnabled) {
    if (!testingButtonEnabled) {
      delay(3_000)
      testingButtonEnabled = true
    }
  }

  ScreenShell(modifier = modifier) {
    HeaderRow(title = "Settings", onBack = onBack)

    SectionCard(title = "Hints") {
      Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Collected hints", style = MaterialTheme.typography.titleMedium)
          Text("$hintCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
      }
      HintDifficulty.entries.forEach { difficulty ->
        CompactInfoRow(
          title = difficulty.title,
          shortText = difficulty.shortRule(),
          infoText = hintDifficultyDescription(difficulty),
          selected = settings.hintDifficulty == difficulty,
          infoExpanded = expandedDifficulty == difficulty,
          onClick = { onHintDifficultySelected(difficulty) },
          onInfoClick = {
            expandedDifficulty = if (expandedDifficulty == difficulty) null else difficulty
          },
        )
      }
    }

    SectionCard(title = "Testing") {
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
          Text("Add 10 hints")
        }
        OutlinedButton(
          onClick = onResetHintsClick,
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Text("Reset hints")
        }
      }
    }
  }
}

@Composable
fun SetupScreen(
  setup: SetupState,
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
    HeaderRow(title = setup.mode.title, onBack = onBack)

    if (setup.mode == GameMode.LocalMultiplayer) {
      SectionCard(title = "Players") {
        setup.playerNames.forEachIndexed { index, name ->
          OutlinedTextField(
            value = name,
            onValueChange = { onPlayerNameChanged(index, it) },
            label = { Text("Player ${index + 1}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onRemovePlayer, modifier = Modifier.weight(1f)) {
            Text("Remove")
          }
          Button(onClick = onAddPlayer, modifier = Modifier.weight(1f)) {
            Text("Add player")
          }
        }
      }

      SectionCard(title = "Quiz base") {
        MultiplayerQuizBase.entries.forEach { base ->
          SelectableRow(
            title = base.title,
            selected = setup.multiplayerBase == base,
            onClick = { onMultiplayerBaseSelected(base) },
          )
        }
      }
    }

    if (setup.mode == GameMode.AllIn || setup.multiplayerBase == MultiplayerQuizBase.AllIn && setup.mode == GameMode.LocalMultiplayer) {
      SectionCard(title = "All-In type") {
        AllInType.entries.forEach { type ->
          SelectableRow(
            title = type.title,
            description = type.description,
            selected = setup.allInType == type,
            onClick = { onAllInTypeSelected(type) },
          )
        }
      }
    }

    if (setup.mode == GameMode.Continents || setup.multiplayerBase == MultiplayerQuizBase.Continents && setup.mode == GameMode.LocalMultiplayer) {
      SectionCard(title = "Continents") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          availableContinents.forEach { continent ->
            val isSelectable = continent != "Antarctica"
            FilterChip(
              selected = continent in setup.selectedContinents,
              onClick = { if (isSelectable) onContinentToggle(continent) },
              enabled = isSelectable,
              label = {
                Text(
                  text = continent,
                  textDecoration = if (isSelectable) TextDecoration.None else TextDecoration.LineThrough,
                )
              },
            )
          }
        }
      }
    }

    if (setup.mode != GameMode.AllIn && !(setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.AllIn)) {
      SectionCard(title = "Question count") {
        val exampleCount = 5.coerceAtMost(questionCountLimit.coerceAtLeast(1))
        OutlinedTextField(
          value = setup.questionCountInput,
          onValueChange = onQuestionCountChange,
          label = { Text("Amount of questions") },
          placeholder = {
            Text(
              if (setup.surpriseMe) {
                "Surprise me selected"
              } else {
                "Example: $exampleCount"
              },
            )
          },
          supportingText = { Text("Allowed range: 1-$questionCountLimit") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          enabled = !setup.surpriseMe,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = onSurpriseMe, modifier = Modifier.fillMaxWidth()) {
          Text(if (setup.surpriseMe) "Use custom amount" else "Surprise me! (1-$questionCountLimit)")
        }
      }
    }

    if (!(setup.mode == GameMode.AllIn && setup.allInType == AllInType.Hardcore) &&
      !(setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.AllIn && setup.allInType == AllInType.Hardcore)
    ) {
      SectionCard(title = "Question variants") {
        QuizVariant.entries.forEach { variant ->
          CheckRow(
            title = variant.title,
            description = variant.description,
            checked = variant in setup.variants,
            onClick = { onVariantToggle(variant) },
          )
        }
      }
    } else {
      SectionCard(title = "Question variants") {
        Text("Hardcore uses all three variants automatically.", style = MaterialTheme.typography.bodyMedium)
      }
    }

    if (setupError != null) {
      Text(text = setupError, color = AccentRed, style = MaterialTheme.typography.bodyMedium)
    }

    Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
      Text("Start quiz")
    }
  }
}

@Composable
fun QuizScreen(
  quiz: QuizState,
  onBackToMenu: () -> Unit,
  onCountryAnswerSelected: (FlagCountry) -> Unit,
  onTypedAnswerChanged: (String) -> Unit,
  onUseHint: () -> Unit,
  onNextQuestion: () -> Unit,
  onSkipQuestion: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val question = quiz.currentQuestion ?: return
  val canSubmit =
    when (question.variant) {
      QuizVariant.TypeCountryName -> quiz.typedAnswer.isNotBlank()
      QuizVariant.FlagToCountry,
      QuizVariant.CountryToFlag -> quiz.selectedCountry != null
    }

  ScreenShell(modifier = modifier) {
    HeaderRow(title = quiz.mode?.title ?: "Quiz", onBack = onBackToMenu)

    SectionCard(title = "Question ${quiz.currentQuestionIndex + 1} of ${quiz.totalQuestions}") {
      if (quiz.isMultiplayer) {
        Text("Next up: ${quiz.currentPlayer.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
      Text("Usable hints: ${quiz.currentPlayer.hintPoints}", style = MaterialTheme.typography.bodyMedium)
      Text("Score is revealed at the end.", style = MaterialTheme.typography.bodyMedium)
    }

    QuestionPrompt(question)

    if (question.variant == QuizVariant.TypeCountryName) {
      OutlinedTextField(
        value = quiz.typedAnswer,
        onValueChange = onTypedAnswerChanged,
        label = { Text("Country name") },
        singleLine = true,
        supportingText = {
          quiz.typedHintPrefix?.let { Text("Hint: starts with $it") }
        },
        modifier = Modifier.fillMaxWidth(),
      )
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        question.options
          .filterNot { it.code in quiz.hiddenOptionCodes }
          .forEach { option ->
            AnswerButton(
              question = question,
              option = option,
              selectedCountry = quiz.selectedCountry,
              onCountryAnswerSelected = onCountryAnswerSelected,
            )
          }
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(
        onClick = onUseHint,
        enabled = quiz.currentPlayer.hintPoints >= 1 && !quiz.hintUsedOnCurrentQuestion,
        modifier = Modifier.weight(1f),
      ) {
        Text("Hint")
      }
      if (question.variant == QuizVariant.TypeCountryName) {
        OutlinedButton(onClick = onSkipQuestion, modifier = Modifier.weight(1f)) {
          Text("Skip")
        }
      }
      Button(onClick = onNextQuestion, enabled = canSubmit, modifier = Modifier.weight(1f)) {
        Text(if (quiz.isLastQuestion) "Finish" else "Next")
      }
    }
  }
}

@Composable
fun ResultsScreen(
  quiz: QuizState,
  levelProgress: LevelProgressState,
  onPlayAgain: () -> Unit,
  onBackToMenu: () -> Unit,
  onLevelUpSeen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = "Quiz complete", onBack = onBackToMenu)

    if (levelProgress.levelUpVisible) {
      LevelUpBanner(level = levelProgress.level, onLevelUpSeen = onLevelUpSeen)
    }

    SectionCard(title = "Final results") {
      quiz.players.sortedByDescending { it.score }.forEach { player ->
        val playerResults = quiz.results.filter { it.playerName == player.name }
        PlayerResultRow(
          player = player,
          totalQuestions = playerResults.size,
          correctAnswers = playerResults.count { it.isCorrect },
          skippedAnswers = playerResults.count { it.skipped },
          showHints = quiz.mode != GameMode.LocalMultiplayer,
        )
      }
    }

    SectionCard(title = "Answer review") {
      quiz.results.forEachIndexed { index, result ->
        ResultRow(index = index + 1, result = result)
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      Button(onClick = onPlayAgain, modifier = Modifier.weight(1f)) {
        Text("Play again")
      }
      OutlinedButton(onClick = onBackToMenu, modifier = Modifier.weight(1f)) {
        Text("Menu")
      }
    }
  }
}

@Composable
private fun ScreenShell(
  modifier: Modifier = Modifier,
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
        .padding(20.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      content = content,
    )
  }
}

@Composable
private fun LevelProgressPanel(
  levelProgress: LevelProgressState,
  onLevelUpSeen: () -> Unit,
) {
  if (levelProgress.levelUpVisible) {
    LevelUpBanner(level = levelProgress.level, onLevelUpSeen = onLevelUpSeen)
  }

  val animatedProgress by animateFloatAsState(
    targetValue = levelProgress.progressFraction,
    label = "level-progress",
  )

  ElevatedCard(
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
          text = "L${levelProgress.level}",
          modifier = Modifier.padding(14.dp),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Next level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(999.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
              color = AccentGreen,
              shape = RoundedCornerShape(999.dp),
              modifier =
                Modifier
                  .fillMaxWidth(animatedProgress.coerceAtLeast(0.03f))
                  .height(10.dp),
            ) {}
          }
        }
        Text(
          text =
            "${levelProgress.hintsTowardNextLevel}/${levelProgress.hintsNeeded} hints  •  " +
              "${levelProgress.correctAnswersTowardNextLevel}/${levelProgress.correctAnswersNeeded} correct  •  " +
              "${levelProgress.eligibleQuizzesTowardNextLevel}/${levelProgress.eligibleQuizzesNeeded} tests",
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }
  }
}

@Composable
private fun LevelUpBanner(
  level: Int,
  onLevelUpSeen: () -> Unit,
) {
  LaunchedEffect(level) {
    delay(4_000)
    onLevelUpSeen()
  }

  ElevatedCard(
    colors = CardDefaults.elevatedCardColors(containerColor = AccentGreen.copy(alpha = 0.22f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text("Level up!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      Text("You reached level $level and earned 5 free hints.", style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@Composable
private fun HeroPanel(
  title: String,
  subtitle: String,
) {
  ElevatedCard(
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(text = title, style = MaterialTheme.typography.headlineLarge)
      Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
    }
  }
}

@Composable
private fun HeaderRow(
  title: String,
  onBack: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium,
      color = Color.White,
      modifier = Modifier.weight(1f),
    )
    OutlinedButton(
      onClick = onBack,
      colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
    ) {
      Text("Back")
    }
  }
}

@Composable
private fun ModeCard(
  mode: GameMode,
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
          Text(text = mode.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(text = mode.shortLabel(), style = MaterialTheme.typography.bodySmall)
        }
        InfoButton(onClick = onInfoClick)
        Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
          Text("Open")
        }
      }
      if (infoExpanded) {
        InfoPanel(text = mode.description)
      }
    }
  }
}

@Composable
private fun InfoButton(onClick: () -> Unit) {
  OutlinedButton(
    onClick = onClick,
    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
  ) {
    Text("i", fontWeight = FontWeight.Bold)
  }
}

private fun GameMode.shortLabel(): String =
  when (this) {
    GameMode.Training -> "Practice freely."
    GameMode.Continents -> "Pick continents."
    GameMode.AllIn -> "All countries."
    GameMode.LocalMultiplayer -> "Pass-and-play."
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
          Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
          Text(shortText, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.86f))
        }
        InfoButton(onClick = onInfoClick)
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

private fun HintDifficulty.shortRule(): String =
  when (this) {
    HintDifficulty.Rookie -> "Every correct answer"
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
private fun CheckRow(
  title: String,
  description: String,
  checked: Boolean,
  onClick: () -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    Checkbox(checked = checked, onCheckedChange = { onClick() })
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleMedium)
      Text(description, style = MaterialTheme.typography.bodySmall)
    }
  }
}

@Composable
private fun QuestionPrompt(question: FlagQuestion) {
  ElevatedCard(
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      when (question.variant) {
        QuizVariant.FlagToCountry,
        QuizVariant.TypeCountryName -> {
          Text(text = question.correctCountry.emoji, fontSize = 76.sp)
          Text(
            text = if (question.variant == QuizVariant.FlagToCountry) "Which country owns this flag?" else "Type this country name.",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
          )
        }
        QuizVariant.CountryToFlag -> {
          Text(text = question.correctCountry.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
          Text(text = "Choose the matching flag.", style = MaterialTheme.typography.titleMedium)
        }
      }
    }
  }
}

@Composable
private fun AnswerButton(
  question: FlagQuestion,
  option: FlagCountry,
  selectedCountry: FlagCountry?,
  onCountryAnswerSelected: (FlagCountry) -> Unit,
) {
  val selected = selectedCountry?.code == option.code
  val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
  Button(
    onClick = { onCountryAnswerSelected(option) },
    colors =
      ButtonDefaults.buttonColors(
        containerColor = color,
        contentColor = buttonContentColor(color),
      ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = if (question.variant == QuizVariant.CountryToFlag) option.emoji else option.name,
      fontSize = if (question.variant == QuizVariant.CountryToFlag) 32.sp else 16.sp,
    )
  }
}

@Composable
private fun PlayerResultRow(
  player: PlayerProgress,
  totalQuestions: Int,
  correctAnswers: Int,
  skippedAnswers: Int,
  showHints: Boolean,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = player.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text(text = "Correct answers: $correctAnswers / $totalQuestions")
      if (skippedAnswers > 0) {
        Text(text = "Skipped: $skippedAnswers")
      }
      Text(text = "Net score: ${player.score}")
      if (showHints) {
        Text(text = "Hint points available: ${player.hintPoints}")
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
) {
  val background = if (result.isCorrect) AccentGreen.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f)
  Surface(
    color = background,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(text = "Question $index - ${result.playerName}", style = MaterialTheme.typography.titleMedium)
      Text(text = "Correct: ${result.question.correctCountry.emoji} ${result.question.correctCountry.name}")
      Text(
        text =
          if (result.skipped) {
            "Your answer: Skipped"
          } else {
            "Your answer: ${result.selectedCountry?.name ?: result.typedAnswer.ifBlank { "No answer" }}"
          },
      )
      Text(text = if (result.hintUsed) "Hint used" else "No hint used")
    }
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

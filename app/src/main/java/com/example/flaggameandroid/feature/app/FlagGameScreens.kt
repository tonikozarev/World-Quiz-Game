package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.theme.AccentGreen
import com.example.flaggameandroid.theme.AccentRed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FlagGameRoute(
  screenViewModel: FlagGameViewModel = viewModel { FlagGameViewModel() },
) {
  val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()

  when (uiState.screen) {
    AppScreen.Menu -> MenuScreen(
      instructionsExpanded = uiState.instructionsExpanded,
      onGameModesClick = screenViewModel::onGameModesClicked,
      onHowToPlayClick = screenViewModel::onHowToPlayClicked,
    )
    AppScreen.GameModes -> GameModesScreen(
      onBack = screenViewModel::onBackToMenu,
      onMultipleChoiceClick = screenViewModel::onMultipleChoiceModeSelected,
    )
    AppScreen.Quiz -> {
      val quiz = uiState.quiz
      val question = quiz.currentQuestion
      if (question != null) {
        MultipleChoiceQuizScreen(
          questionNumber = quiz.currentQuestionIndex + 1,
          totalQuestions = quiz.totalQuestions,
          mode = quiz.mode ?: GameMode.MultipleChoice,
          question = question,
          selectedAnswer = quiz.selectedAnswer,
          answerRevealed = quiz.answerRevealed,
          onBackToMenu = screenViewModel::onBackToMenu,
          onAnswerSelected = screenViewModel::onAnswerSelected,
          onNextQuestion = screenViewModel::onNextQuestion,
        )
      }
    }
    AppScreen.Results -> ResultsScreen(
      score = uiState.quiz.score,
      totalQuestions = uiState.quiz.totalQuestions,
      results = uiState.quiz.results,
      onPlayAgain = screenViewModel::onReplayMultipleChoice,
      onBackToMenu = screenViewModel::onBackToMenu,
    )
  }
}

@Composable
fun MenuScreen(
  instructionsExpanded: Boolean,
  onGameModesClick: () -> Unit,
  onHowToPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
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
    ) {
      HeroPanel(
        title = "Flag Game Android",
        subtitle = "A fresh menu, a test mode, and a 5-question quiz you can play right now.",
      )

      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(text = "Menu", style = MaterialTheme.typography.titleLarge)
          Button(onClick = onGameModesClick, modifier = Modifier.fillMaxWidth()) {
            Text("Game modes")
          }
          OutlinedButton(onClick = onHowToPlayClick, modifier = Modifier.fillMaxWidth()) {
            Text("How to play")
          }
          OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
            Text("Settings coming later")
          }
        }
      }

      if (instructionsExpanded) {
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "How the test mode works", style = MaterialTheme.typography.titleLarge)
            Text(
              text = "Pick Game modes, then choose Multiple choices. Each question shows a flag and four answers. Your selected answer turns green or red, and the final score appears only at the end.",
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }
  }
}

@Composable
fun GameModesScreen(
  onBack: () -> Unit,
  onMultipleChoiceClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .padding(20.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
      HeaderRow(title = "Game modes", onBack = onBack)

      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          ModeCard(
            mode = GameMode.MultipleChoice,
            onClick = onMultipleChoiceClick,
          )

          Card(
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
              ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(text = "More modes later", style = MaterialTheme.typography.titleMedium)
              Text(
                text = "This section is reserved for future game modes so the app can scale without changing the menu structure.",
                style = MaterialTheme.typography.bodyMedium,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MultipleChoiceQuizScreen(
  questionNumber: Int,
  totalQuestions: Int,
  mode: GameMode,
  question: com.example.flaggameandroid.core.model.FlagQuestion,
  selectedAnswer: String?,
  answerRevealed: Boolean,
  onBackToMenu: () -> Unit,
  onAnswerSelected: (String) -> Unit,
  onNextQuestion: () -> Unit,
  modifier: Modifier = Modifier,
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
    ) {
      HeaderRow(title = mode.title, onBack = onBackToMenu)

      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(text = "Question $questionNumber of $totalQuestions", style = MaterialTheme.typography.titleMedium)
          Text(text = "Score stays hidden until the end.", style = MaterialTheme.typography.bodyMedium)
        }
      }

      ElevatedCard(
        colors =
          CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(text = question.flagEmoji, fontSize = 72.sp)
          Text(text = "Which country is this flag from?", style = MaterialTheme.typography.titleLarge)
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        question.options.forEach { option ->
          val optionColor =
            when {
              !answerRevealed -> MaterialTheme.colorScheme.primary
              option == question.correctAnswer -> AccentGreen
              option == selectedAnswer && option != question.correctAnswer -> AccentRed
              else -> MaterialTheme.colorScheme.surfaceVariant
            }

          Button(
            onClick = { onAnswerSelected(option) },
            enabled = !answerRevealed,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = optionColor,
                contentColor = buttonContentColor(optionColor),
              ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(text = option)
          }
        }
      }

      if (answerRevealed) {
        Button(
          onClick = onNextQuestion,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(if (questionNumber == totalQuestions) "Finish quiz" else "Next question")
        }
      }
    }
  }
}

@Composable
fun ResultsScreen(
  score: Int,
  totalQuestions: Int,
  results: List<QuestionResult>,
  onPlayAgain: () -> Unit,
  onBackToMenu: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .padding(20.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      HeaderRow(title = "Quiz complete", onBack = onBackToMenu)

      ElevatedCard(
        colors =
          CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(text = "Final score", style = MaterialTheme.typography.titleLarge)
          Text(
            text = "$score / $totalQuestions",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "The result only appears here, at the end of the quiz.",
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }

      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(text = "Answer review", style = MaterialTheme.typography.titleLarge)
          results.forEachIndexed { index, result ->
            ResultRow(index = index + 1, result = result)
          }
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
}

@Composable
private fun HeroPanel(
  title: String,
  subtitle: String,
) {
  ElevatedCard(
    colors =
      CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    Column {
      Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
    OutlinedButton(onClick = onBack) {
      Text("Back")
    }
  }
}

@Composable
private fun ModeCard(
  mode: GameMode,
  onClick: () -> Unit,
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(text = mode.title, style = MaterialTheme.typography.titleLarge)
      Text(text = mode.description, style = MaterialTheme.typography.bodyMedium)
      Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text("Start")
      }
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

@Composable
private fun ResultRow(
  index: Int,
  result: QuestionResult,
) {
  val background =
    if (result.isCorrect) {
      AccentGreen.copy(alpha = 0.15f)
    } else {
      AccentRed.copy(alpha = 0.15f)
    }

  Surface(
    color = background,
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(text = "Question $index", style = MaterialTheme.typography.titleMedium)
      Text(text = "Flag: ${result.question.flagEmoji}", style = MaterialTheme.typography.bodyMedium)
      Text(text = "Your answer: ${result.selectedAnswer}", style = MaterialTheme.typography.bodyMedium)
      Text(text = "Correct answer: ${result.question.correctAnswer}", style = MaterialTheme.typography.bodyMedium)
    }
  }
}

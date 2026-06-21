package com.example.flaggameandroid.feature.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.data.QuizAnswerChecker
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.theme.AccentGold
import com.example.flaggameandroid.theme.AccentGreen
import com.example.flaggameandroid.theme.AccentRed

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
  var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
  BackHandler { showQuitDialog = true }
  LaunchedEffect(quiz.mode, quiz.startedAtEpochMillis) {
    if (quiz.mode == GameMode.SpeedRun && quiz.startedAtEpochMillis > 0L) {
      while (true) {
        nowMillis = System.currentTimeMillis()
        kotlinx.coroutines.delay(1_000)
      }
    }
  }
  val canGoBack = quiz.currentQuestionIndex > 0
  val canGoForward = quiz.currentQuestionIndex < quiz.questions.lastIndex
  val unansweredQuestions = quiz.questionStates.mapIndexedNotNull { index, state -> if (state.status == QuestionStatus.Unanswered) index + 1 else null }
  val skippedQuestions = quiz.questionStates.mapIndexedNotNull { index, state -> if (state.status == QuestionStatus.Skipped) index + 1 else null }
  val speedRunElapsedLabel =
    if (quiz.mode == GameMode.SpeedRun) {
      formatElapsedTime(speedRunElapsedMillis(quiz, nowMillis))
    } else {
      null
    }

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

    if (speedRunElapsedLabel != null) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text =
              when (language) {
                AppLanguage.English -> "Speed run"
                AppLanguage.Bulgarian -> "Скоростна игра"
                AppLanguage.German -> "Schnelllauf"
              },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = speedRunElapsedLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
        }
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

    QuizNavigationRow(
      onPreviousQuestion = onPreviousQuestion,
      canGoBack = canGoBack,
      questionContent = {
        QuestionPrompt(
          question = question,
          language = language,
          modifier = Modifier.weight(1f),
        )
      },
      onNextQuestionPreview = onNextQuestionPreview,
      canGoForward = canGoForward,
    )

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
    QuizActionButtonsRow(
      infoLabel = localizedQuizInfoButtonLabel(language),
      showInfo = showQuizInfo,
      onInfoClick = { showQuizInfo = !showQuizInfo },
      hintLabel =
        if (quiz.currentQuestionState.hintUses == 0) {
          localizedHintButtonLabel(language)
        } else {
          localizedRevealButtonLabel(language)
        },
      canUseHint = quiz.currentPlayer.hintPoints >= 1 && quiz.currentQuestionState.hintUses < 2 && !quiz.currentQuestionState.locked && quiz.currentQuestionState.status != QuestionStatus.Answered,
      onUseHint = onUseHint,
      unskipLabel = localizedUnskipButtonLabel(language),
      canUnskip = skippedQuestions.isNotEmpty(),
      onUnskipQuestion = onUnskipQuestion,
      verifyLabel = if (quiz.mode == GameMode.Training && question.variant == QuizVariant.TypeCountryName) localizedVerifyButtonLabel(language) else null,
      canVerify = quiz.typedAnswer.isNotBlank() && !isTrainingLocked,
      onVerifyTypedAnswer = onVerifyTypedAnswer,
    )

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
  }
}

@Composable
fun ResultsScreen(
  quiz: QuizState,
  language: AppLanguage,
  levelProgress: LevelProgressState,
  countryPracticeStats: Map<String, com.example.flaggameandroid.core.model.CountryPracticeStats>,
  completedAtEpochMillis: Long,
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

    if (quiz.mode == GameMode.SpeedRun) {
      SectionCard(title = when (language) {
        AppLanguage.English -> "Speed run time"
        AppLanguage.Bulgarian -> "Време за скоростна игра"
        AppLanguage.German -> "Zeit im Schnelllauf"
      }) {
        Text(
          text =
            when (language) {
              AppLanguage.English -> "Final time: ${formatElapsedTime(speedRunElapsedMillis(quiz, completedAtEpochMillis))}"
              AppLanguage.Bulgarian -> "Краен резултат: ${formatElapsedTime(speedRunElapsedMillis(quiz, completedAtEpochMillis))}"
              AppLanguage.German -> "Endzeit: ${formatElapsedTime(speedRunElapsedMillis(quiz, completedAtEpochMillis))}"
            },
        )
      }
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
        ResultRow(
          index = index + 1,
          result = result,
          language = language,
        )
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


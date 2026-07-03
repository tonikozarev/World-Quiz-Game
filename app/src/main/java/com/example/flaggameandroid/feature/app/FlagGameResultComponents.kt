package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.QuestionResult
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.theme.AccentGreen
import com.example.flaggameandroid.theme.AccentRed

@Composable
internal fun PlayerResultRow(
  player: PlayerProgress,
  language: AppLanguage,
  totalQuestions: Int,
  correctAnswers: Int,
  showHints: Boolean,
  netScoreText: String = formatScore(player.score),
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = player.name, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
            AppLanguage.English -> "Net score: $netScoreText"
            AppLanguage.Bulgarian -> "Краен резултат: $netScoreText"
            AppLanguage.German -> "Punktestand: $netScoreText"
          },
      )
      if (showHints) {
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Gained hint points: ${player.earnedHintPoints}"
            AppLanguage.Bulgarian -> "Спечелени жокер точки: ${player.earnedHintPoints}"
            AppLanguage.German -> "Erhaltene Hinweis-Punkte: ${player.earnedHintPoints}"
          },
      )
      }
    }
  }
}

@Composable
internal fun ResultRow(
  index: Int,
  result: QuestionResult,
  language: AppLanguage,
  isFavorite: Boolean,
  onToggleFavoriteCountry: (String) -> Unit,
  isWeak: Boolean = false,
) {
  val background =
    when {
      result.isCorrect && !result.revealed -> AccentGreen.copy(alpha = 0.15f)
      else -> AccentRed.copy(alpha = 0.15f)
    }
  val hintSuffix =
    when {
      result.revealed ->
        when (language) {
          AppLanguage.English -> " (Revealed)"
          AppLanguage.Bulgarian -> " (Разкрито)"
          AppLanguage.German -> " (Aufgedeckt)"
        }
      result.hintUses == 1 ->
        when (language) {
          AppLanguage.English -> " (Hinted x1)"
          AppLanguage.Bulgarian -> " (Подсказано x1)"
          AppLanguage.German -> " (Hinweis x1)"
        }
      result.hintUses == 2 ->
        when (language) {
          AppLanguage.English -> " (Hinted x2)"
          AppLanguage.Bulgarian -> " (Подсказано x2)"
          AppLanguage.German -> " (Hinweis x2)"
        }
      else -> ""
    }
  val allOptions =
    if (result.question.variant == QuizVariant.TypeText) {
      emptyList()
    } else {
      result.question.options
    }
  val netPoints = resultPointValue(result)
  val showPlayerName = result.playerName.isNotBlank() && result.playerName != "Solo"
  Surface(
    color = background,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            when (language) {
              AppLanguage.English -> "$index. Question$hintSuffix"
              AppLanguage.Bulgarian -> "$index. Въпрос$hintSuffix"
              AppLanguage.German -> "$index. Frage$hintSuffix"
            },
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.weight(1f),
        )
        TextButton(
          onClick = { onToggleFavoriteCountry(result.question.correctCountry.code) },
          contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
          modifier = Modifier.size(28.dp),
        ) {
          Text(
            text = if (isFavorite) "★" else "☆",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
            color = if (isFavorite) AccentGreen else MaterialTheme.colorScheme.onSurface,
          )
        }
      }
      if (isWeak) {
        Text(
          text =
            when (language) {
              AppLanguage.English -> "Often missed"
              AppLanguage.Bulgarian -> "Често пропускана"
              AppLanguage.German -> "Oft verfehlt"
            },
          style = MaterialTheme.typography.labelMedium,
        )
      }
      if (showPlayerName) {
        Text(
          text =
            when (language) {
              AppLanguage.English -> "Player: ${result.playerName}"
              AppLanguage.Bulgarian -> "Играч: ${result.playerName}"
              AppLanguage.German -> "Spieler: ${result.playerName}"
            },
        )
      }
      Text(text = localizedReviewedQuestionText(result, language))
      Text(text = localizedMyAnswerText(result, language))
      Text(
        text =
          if (result.question.variant == QuizVariant.TypeText) {
            localizedCorrectAnswerText(result, language)
          } else {
            localizedAllAnswersText(result, language, allOptions)
          },
      )
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Points: ${formatPointValue(netPoints)}"
            AppLanguage.Bulgarian -> "Точки: ${formatPointValue(netPoints)}"
            AppLanguage.German -> "Punkte: ${formatPointValue(netPoints)}"
          },
      )
    }
  }
}

internal fun resultPointValue(result: QuestionResult): Double =
  when {
    result.revealed -> 0.0
    !result.isCorrect -> 0.0
    result.hintUses == 1 -> 0.75
    result.hintUses == 2 -> 0.25
    else -> 1.0
  }

internal fun formatPointValue(points: Double): String =
  run {
    val rounded = kotlin.math.round(points * 100.0) / 100.0
    if (rounded % 1.0 == 0.0) rounded.toInt().toString() else "%.2f".format(java.util.Locale.US, rounded)
  }

private fun localizedReviewedQuestionText(
  result: QuestionResult,
  language: AppLanguage,
): String {
  val countryLabel = result.question.correctCountry.localizedName(language)
  val countryWithFlag = "$countryLabel ${result.question.correctCountry.emoji}"
  val capitalLabel = result.question.correctCountry.localizedQuizText(language, QuizTopic.Capitals)
  val prompt =
    when (result.question.variant) {
      QuizVariant.FlagToText -> {
        when (result.question.topic) {
          QuizTopic.Capitals ->
            when (language) {
              AppLanguage.English -> "What is the capital of $countryWithFlag?"
              AppLanguage.Bulgarian -> "Коя е столицата на $countryWithFlag?"
              AppLanguage.German -> "Was ist die Hauptstadt von $countryWithFlag?"
            }
          else ->
            when (language) {
              AppLanguage.English -> "Whose flag is this: ${result.question.correctCountry.emoji}?"
              AppLanguage.Bulgarian -> "Чий е този флаг: ${result.question.correctCountry.emoji}?"
              AppLanguage.German -> "Wessen Flagge ist das: ${result.question.correctCountry.emoji}?"
            }
        }
      }
      QuizVariant.TextToFlag -> {
        if (result.question.topic == QuizTopic.Capitals) {
          when (language) {
            AppLanguage.English -> "What is the flag of the country with the capital $capitalLabel?"
            AppLanguage.Bulgarian -> "Кой е флагът на държавата със столица $capitalLabel?"
            AppLanguage.German -> "Welche Flagge gehört zu dem Land mit der Hauptstadt $capitalLabel?"
          }
        } else {
          when (language) {
            AppLanguage.English -> "Which country does this flag ${result.question.correctCountry.emoji} belong to?"
            AppLanguage.Bulgarian -> "На коя държава принадлежи този флаг ${result.question.correctCountry.emoji}?"
            AppLanguage.German -> "Zu welchem Land gehört diese Flagge ${result.question.correctCountry.emoji}?"
          }
        }
      }
      QuizVariant.TypeText -> {
        if (result.question.topic == QuizTopic.Capitals) {
          when (language) {
            AppLanguage.English -> "Type the capital of $countryWithFlag:"
            AppLanguage.Bulgarian -> "Напишете столицата на $countryWithFlag:"
            AppLanguage.German -> "Geben Sie die Hauptstadt von $countryWithFlag ein:"
          }
        } else {
          when (language) {
            AppLanguage.English -> "Type the country with the following flag: ${result.question.correctCountry.emoji}"
            AppLanguage.Bulgarian -> "Напиши държавата със следния флаг: ${result.question.correctCountry.emoji}"
            AppLanguage.German -> "Geben Sie das Land mit der folgenden Flagge ein: ${result.question.correctCountry.emoji}"
          }
        }
      }
    }
  return when (language) {
    AppLanguage.English -> "Question: $prompt"
    AppLanguage.Bulgarian -> "Въпрос: $prompt"
    AppLanguage.German -> "Frage: $prompt"
  }
}

private fun localizedMyAnswerText(
  result: QuestionResult,
  language: AppLanguage,
) = buildAnnotatedString {
  append(
    when (language) {
      AppLanguage.English -> "My answer: "
      AppLanguage.Bulgarian -> "Моят отговор: "
      AppLanguage.German -> "Meine Antwort: "
    },
  )
  val answer =
    if (result.revealed) {
      "-"
    } else if (result.question.variant == QuizVariant.TypeText) {
      result.typedAnswer.ifBlank { "-" }
    } else {
      result.selectedCountry?.let {
        reviewAnswerLabel(it, result.question.variant, language, result.question.topic)
      } ?: "-"
    }
  if (result.isCorrect && !result.revealed) {
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))
    append(answer)
    pop()
  } else {
    append(answer)
  }
}

private fun localizedCorrectAnswerText(
  result: QuestionResult,
  language: AppLanguage,
) = buildAnnotatedString {
  append(
    when (language) {
      AppLanguage.English -> "Correct answer: "
      AppLanguage.Bulgarian -> "Верен отговор: "
      AppLanguage.German -> "Richtige Antwort: "
    },
  )
  pushStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))
  append(reviewAnswerLabel(result.question.correctCountry, result.question.variant, language, result.question.topic))
  pop()
}

private fun localizedAllAnswersText(
  result: QuestionResult,
  language: AppLanguage,
  allOptions: List<FlagCountry>,
) = buildAnnotatedString {
  append(
    when (language) {
      AppLanguage.English -> "All answers: "
      AppLanguage.Bulgarian -> "Всички отговори: "
      AppLanguage.German -> "Alle Antworten: "
    },
  )
  allOptions.forEachIndexed { optionIndex, option ->
    if (optionIndex > 0) {
      append(", ")
    }
    val label = reviewAnswerLabel(option, result.question.variant, language, result.question.topic)
    if (option.code == result.question.correctCountry.code) {
      pushStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))
      append(label)
      pop()
    } else {
      append(label)
    }
  }
}

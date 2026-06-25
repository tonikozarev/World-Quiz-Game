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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.QuestionResult
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
            AppLanguage.English -> "Net score: ${formatScore(player.score)}"
            AppLanguage.Bulgarian -> "Краен резултат: ${formatScore(player.score)}"
            AppLanguage.German -> "Punktestand: ${formatScore(player.score)}"
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
  val netPointsInternal =
    when {
      !result.isCorrect || result.hintUses >= 2 -> 0
      result.hintUses == 1 -> 1
      else -> 2
    }
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
              AppLanguage.English -> "Question $index$hintSuffix"
              AppLanguage.Bulgarian -> "Въпрос $index$hintSuffix"
              AppLanguage.German -> "Frage $index$hintSuffix"
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
          when (language) {
            AppLanguage.English -> "Used a hint? ${if (result.hintUsed) "Yes" else "No"}"
            AppLanguage.Bulgarian -> "Ползван жокер? ${if (result.hintUsed) "Да" else "Не"}"
            AppLanguage.German -> "Hinweis verwendet? ${if (result.hintUsed) "Ja" else "Nein"}"
          },
      )
      Text(
        text =
          when (language) {
            AppLanguage.English -> "NET points: ${formatScore(netPointsInternal)}"
            AppLanguage.Bulgarian -> "NET точки: ${formatScore(netPointsInternal)}"
            AppLanguage.German -> "NET-Punkte: ${formatScore(netPointsInternal)}"
          },
      )
    }
  }
}



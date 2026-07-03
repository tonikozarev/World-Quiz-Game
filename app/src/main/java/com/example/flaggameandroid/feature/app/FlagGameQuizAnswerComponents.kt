package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.QuizVariant
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.theme.FlagGameAndroidTheme

@Composable
internal fun QuestionPrompt(
  question: FlagQuestion,
  language: AppLanguage,
  showContextHint: Boolean,
  modifier: Modifier = Modifier,
) {
  ElevatedCard(
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
      when (question.variant) {
        QuizVariant.FlagToText,
        QuizVariant.TypeText -> {
          Text(
            text = question.correctCountry.emoji,
            fontSize = 62.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
          )
          if (showContextHint) {
            capitalQuestionCountryLabel(question, language)?.let { countryLabel ->
              Text(
                text = countryLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
              )
            }
          }
        }

        QuizVariant.TextToFlag -> {
          Text(
            text = question.correctCountry.localizedQuizText(language, question.topic),
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

@Preview(showBackground = true, name = "Question Prompt")
@Composable
private fun PreviewQuestionPrompt() {
  FlagGameAndroidTheme {
    Surface {
      QuestionPrompt(
        question =
          FlagQuestion(
            correctCountry = FlagCountry(
              code = "de",
              name = "Germany",
              emoji = "🇩🇪",
              continent = "Europe",
            ),
            variant = QuizVariant.FlagToText,
            options = emptyList(),
          ),
        language = AppLanguage.English,
        showContextHint = true,
      )
    }
  }
}

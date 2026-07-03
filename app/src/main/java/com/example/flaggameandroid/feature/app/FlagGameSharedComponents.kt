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
import androidx.compose.ui.draw.alpha
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

@Composable
internal fun HeaderRow(
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
internal fun ModeCard(
  mode: GameMode,
  language: AppLanguage,
  infoExpanded: Boolean,
  openEnabled: Boolean,
  openLabel: String,
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
        Button(
          onClick = onClick,
          enabled = openEnabled,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = if (openEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              contentColor = if (openEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
              disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
              disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            ),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
          Text(
            openLabel,
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
internal fun CompactInfoRow(
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
internal fun SectionCard(
  title: String,
  headerAction: (@Composable () -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        headerAction?.invoke()
      }
      content()
    }
  }
}

@Composable
internal fun SelectableRow(
  title: String,
  selected: Boolean,
  onClick: () -> Unit,
  enabled: Boolean = true,
  description: String? = null,
) {
  val colors =
    if (selected) {
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    } else {
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }
  Card(
    onClick = onClick,
    enabled = enabled,
    colors = colors,
    modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.55f),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(title, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
      if (description != null) {
        Text(description, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}

@Composable
internal fun CheckRow(
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
internal fun AnswerButton(
  question: FlagQuestion,
  option: FlagCountry,
  selectedCountry: FlagCountry?,
  language: AppLanguage,
  hintUses: Int = 0,
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
    val isCapitalFlagAnswer = question.variant == QuizVariant.TextToFlag && question.topic == com.example.flaggameandroid.core.model.QuizTopic.Capitals
    val hasHintAnswerText = question.variant == QuizVariant.TextToFlag && hintUses > 0
    Text(
      text = answerOptionLabel(question, option, language, hintUses),
      fontSize =
        when {
          isCapitalFlagAnswer || hasHintAnswerText -> 16.sp
          question.variant == QuizVariant.TextToFlag -> 32.sp
          else -> 16.sp
        },
      maxLines = if (isCapitalFlagAnswer || hasHintAnswerText) 2 else 1,
      textAlign = TextAlign.Center,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Preview(showBackground = true, name = "Info Button")
@Composable
private fun PreviewInfoButton() {
  FlagGameAndroidTheme {
    InfoButton(onClick = {})
  }
}

@Preview(showBackground = true, name = "Mode Card")
@Composable
private fun PreviewModeCard() {
  FlagGameAndroidTheme {
    Surface {
      ModeCard(
        mode = GameMode.Training,
        language = AppLanguage.English,
        infoExpanded = true,
        openEnabled = true,
        openLabel = "Start",
        onInfoClick = {},
        onClick = {},
      )
    }
  }
}

@Preview(showBackground = true, name = "Result Row")
@Composable
private fun PreviewResultRow() {
  FlagGameAndroidTheme {
    Surface {
      ResultRow(
        index = 1,
        result =
          QuestionResult(
            question =
              FlagQuestion(
                correctCountry = FlagCountry("DE", "Germany", "🇩🇪", "Europe"),
                options =
                  listOf(
                    FlagCountry("DE", "Germany", "🇩🇪", "Europe"),
                    FlagCountry("AT", "Austria", "🇦🇹", "Europe"),
                  ),
                variant = QuizVariant.FlagToText,
              ),
            playerName = "Solo",
            selectedCountry = FlagCountry("DE", "Germany", "🇩🇪", "Europe"),
            typedAnswer = "",
            isCorrect = true,
            hintUsed = false,
            hintUses = 0,
            hintStreak = 1,
          ),
        language = AppLanguage.English,
        isFavorite = false,
        onToggleFavoriteCountry = {},
        isWeak = true,
      )
    }
  }
}

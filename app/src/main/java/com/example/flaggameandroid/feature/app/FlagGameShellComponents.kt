package com.example.flaggameandroid.feature.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.theme.AccentGold
import com.example.flaggameandroid.theme.AccentGreen
import com.example.flaggameandroid.theme.AccentRed
import com.example.flaggameandroid.theme.FlagGameAndroidTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
internal fun ScreenShell(
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
internal fun LevelProgressPanel(
  levelProgress: LevelProgressState,
  profile: ProfileState,
  activityCalendar: Map<Long, ActivityDayRecord>,
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
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        androidx.compose.material3.Surface(
          color = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = androidx.compose.foundation.shape.CircleShape,
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
            androidx.compose.material3.Surface(
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
              contentColor = MaterialTheme.colorScheme.primary,
              shape = androidx.compose.foundation.shape.CircleShape,
            ) {
              Text(
                text = "\u270E",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
              )
            }
          }
          androidx.compose.material3.Surface(
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
              androidx.compose.material3.Surface(
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
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
      ) {
        WeekdayStreakStrip(
          activityCalendar = activityCalendar,
          modifier = Modifier.weight(1f),
        )
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(0.dp),
          modifier = Modifier.padding(start = 10.dp, bottom = 2.dp),
        )
        {
          Text(
            text = "${streakProgressCount(activityCalendar)}/30",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
          )
          Text(
            text = "days",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
          )
        }
      }
    }
  }
}

@Composable
private fun WeekdayStreakStrip(
  activityCalendar: Map<Long, ActivityDayRecord>,
  modifier: Modifier = Modifier,
) {
  val weekDays = weekActivityDays(activityCalendar = activityCalendar)
  val todayDayKey = localDayKey(System.currentTimeMillis())
  Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    weekDays.forEach { record ->
      val fillColor =
        when {
          record.dayKey > todayDayKey -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          record.quizzesCompleted > 0 -> AccentGreen.copy(alpha = 0.92f)
          else -> Color(0xFFB84A4A)
        }
      Surface(
        color = fillColor,
        shape = androidx.compose.foundation.shape.CircleShape,
        modifier = Modifier.weight(1f),
      ) {
        Column(
          modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Text(
            text = weekdayShortLabel(record.dayKey),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
          )
        }
      }
    }
  }
}

private fun weekdayShortLabel(dayKey: Long): String {
  val date = java.time.LocalDate.ofEpochDay(dayKey)
  return when (date.dayOfWeek) {
    java.time.DayOfWeek.MONDAY -> "Mon"
    java.time.DayOfWeek.TUESDAY -> "Tue"
    java.time.DayOfWeek.WEDNESDAY -> "Wed"
    java.time.DayOfWeek.THURSDAY -> "Thu"
    java.time.DayOfWeek.FRIDAY -> "Fri"
    java.time.DayOfWeek.SATURDAY -> "Sat"
    java.time.DayOfWeek.SUNDAY -> "Sun"
  }
}

private fun streakProgressCount(
  activityCalendar: Map<Long, ActivityDayRecord>,
): Int = streakLength(activityCalendar = activityCalendar).coerceAtMost(30)

@Composable
internal fun LevelUpBanner(
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

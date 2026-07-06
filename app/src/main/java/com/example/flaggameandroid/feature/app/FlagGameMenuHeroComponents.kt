package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.theme.AccentGold

@Composable
internal fun HeroPanel(
  title: String,
  subtitle: String,
  language: AppLanguage,
  onStartClick: () -> Unit,
  onMedalsClick: () -> Unit,
  onAchievementsClick: () -> Unit,
  onFavoritesClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onQuitClick: () -> Unit,
) {
  ElevatedCard(
    colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
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
        }
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
              contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 17.dp),
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
            HeroNavButton(cleanText(language, UiText.Favorites), onFavoritesClick, widthFraction = 0.78f)
            HeroNavButton(cleanText(language, UiText.Settings), onSettingsClick, widthFraction = 0.78f)
            HeroNavButton(cleanText(language, UiText.Quit), onQuitClick, widthFraction = 0.78f)
          }
        }
      }
    }
  }
}

@Composable
internal fun HeroGoldPill(
  text: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = AccentGold.copy(alpha = 0.92f),
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.72f)),
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = text,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 7.dp),
      style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
      fontWeight = FontWeight.SemiBold,
      color = Color(0xFF172033),
      textAlign = TextAlign.Center,
      maxLines = 1,
      softWrap = false,
    )
  }
}

@Composable
internal fun HeroNavButton(
  label: String,
  onClick: () -> Unit,
  widthFraction: Float,
) {
  Button(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(widthFraction),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
  ) {
    Text(label)
  }
}

@Composable
internal fun HeroInfoButton(onClick: () -> Unit) {
  InfoButton(onClick = onClick)
}

@Composable
internal fun HeroStatPill(text: String) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)),
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Medium,
    )
  }
}

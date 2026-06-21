package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.MedalTier

@Composable
internal fun MedalTierRow(
  medalTier: MedalTier,
  count: Int,
  title: String,
) {
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
          Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
      }
      Text(
        text = count.toString(),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
      )
    }
  }
}

@Composable
internal fun StreakMedalRow(
  title: String,
  count: Int,
  progressText: String,
  badge: String,
) {
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
        Text(text = badge, fontSize = 24.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
          Text(text = progressText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Text(
        text = count.toString(),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
      )
    }
  }
}

@Composable
internal fun AchievementCardItem(
  achievementId: AchievementId,
  title: String,
  status: String,
  unlocked: Boolean,
  expanded: Boolean,
  description: String,
  onClick: () -> Unit,
) {
  Card(
    onClick = onClick,
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
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
          )
          Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = if (unlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
          )
        }
      }
      if (expanded) {
        InfoPanel(text = description)
      }
    }
  }
}

package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementSector
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.RatingsProgress

@Composable
internal fun RatingsSection(
  ratings: RatingsProgress,
  language: AppLanguage,
) {
  var showMedalInfo by remember { mutableStateOf(false) }
  SectionCard(title = cleanText(language, UiText.Medals)) {
    androidx.compose.foundation.layout.Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
      Text(
        text = cleanMedalIntro(language),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f),
      )
      InfoButton(onClick = { showMedalInfo = !showMedalInfo })
    }
    if (showMedalInfo) {
      InfoPanel(text = cleanMedalInfo(language))
    }
    androidx.compose.foundation.layout.FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      com.example.flaggameandroid.core.model.MedalTier.entries.forEach { medalTier ->
        MedalTierRow(
          medalTier = medalTier,
          count = ratings.countFor(medalTier),
          title = cleanMedalTitle(medalTier, language),
        )
      }
    }
  }

  SectionCard(
    title =
      when (language) {
        AppLanguage.English -> "Streak medals"
        AppLanguage.Bulgarian -> "Медали за поредица"
        AppLanguage.German -> "Streak-Medaillen"
      },
  ) {
    Text(
      text =
        when (language) {
          AppLanguage.English -> "Only completed-day streaks count here."
          AppLanguage.Bulgarian -> "Тук се броят само поредиците от напълно завършени дни."
          AppLanguage.German -> "Hier zählen nur vollständig abgeschlossene Tagesserien."
        },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    StreakMedalRow(
      title =
        when (language) {
          AppLanguage.English -> "7 consecutive days"
          AppLanguage.Bulgarian -> "7 последователни дни"
          AppLanguage.German -> "7 Tage in Folge"
        },
      count = ratings.streak7Count,
      progressText =
        when (language) {
          AppLanguage.English -> "Progress: ${ratings.streak7ProgressDays}/7"
          AppLanguage.Bulgarian -> "Напредък: ${ratings.streak7ProgressDays}/7"
          AppLanguage.German -> "Fortschritt: ${ratings.streak7ProgressDays}/7"
        },
      badge = "\uD83D\uDD25",
    )
    StreakMedalRow(
      title =
        when (language) {
          AppLanguage.English -> "30 consecutive days"
          AppLanguage.Bulgarian -> "30 последователни дни"
          AppLanguage.German -> "30 Tage in Folge"
        },
      count = ratings.streak30Count,
      progressText =
        when (language) {
          AppLanguage.English -> "Progress: ${ratings.streak30ProgressDays}/30"
          AppLanguage.Bulgarian -> "Напредък: ${ratings.streak30ProgressDays}/30"
          AppLanguage.German -> "Fortschritt: ${ratings.streak30ProgressDays}/30"
        },
      badge = "\uD83C\uDF1F",
    )
  }
}

@Composable
internal fun AchievementsSection(
  achievements: AchievementsProgress,
  language: AppLanguage,
) {
  var expandedAchievement by remember { mutableStateOf<AchievementId?>(null) }
  SectionCard(title = cleanText(language, UiText.Achievements)) {
    Text(
      text = localizedAchievementHint(language),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    AchievementSector.entries.forEach { sector ->
      Text(
        text = localizedAchievementSectorTitle(sector, language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = Modifier.padding(top = 6.dp),
      )
      AchievementId.entries.filter { it.sector == sector }.forEach { achievementId ->
        val unlockedAt = achievements.unlockedAt(achievementId)
        val unlocked = unlockedAt != null
        AchievementCardItem(
          achievementId = achievementId,
          title = localizedAchievementTitle(achievementId, language),
          status = localizedAchievementStatus(language, unlockedAt),
          unlocked = unlocked,
          expanded = expandedAchievement == achievementId,
          description = localizedAchievementDescription(achievementId, language),
          onClick = { expandedAchievement = if (expandedAchievement == achievementId) null else achievementId },
        )
      }
    }
  }
}

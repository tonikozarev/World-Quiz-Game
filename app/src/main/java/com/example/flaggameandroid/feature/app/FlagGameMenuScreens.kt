package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.AchievementsProgress
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.DailyChallengeCache
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import com.example.flaggameandroid.core.model.RatingsProgress
import com.example.flaggameandroid.core.model.visibleGameModes
import kotlinx.coroutines.delay

@Composable
fun MenuScreen(
  levelProgress: LevelProgressState,
  profile: ProfileState,
  language: AppLanguage,
  timeZone: AppTimeZone,
  activityCalendar: Map<Long, ActivityDayRecord>,
  countryPracticeStats: Map<String, CountryPracticeStats>,
  onStartClick: () -> Unit,
  onMedalsClick: () -> Unit,
  onAchievementsClick: () -> Unit,
  onFavoritesClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onQuitClick: () -> Unit,
  onLevelUpSeen: () -> Unit,
  onAccountNameChanged: (String) -> Unit,
  onAvatarSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  var profileDialogVisible by remember { mutableStateOf(false) }

  if (profileDialogVisible) {
    ProfileEditorDialog(
      profile = profile,
      levelProgress = levelProgress,
      language = language,
      onDismiss = { profileDialogVisible = false },
      onSave = { name, avatarIndex ->
        onAccountNameChanged(name)
        onAvatarSelected(avatarIndex)
        profileDialogVisible = false
      },
    )
  }

  ScreenShell(modifier = modifier) {
    LevelProgressPanel(
      levelProgress = levelProgress,
      profile = profile,
      activityCalendar = activityCalendar,
      timeZone = timeZone,
      onLevelUpSeen = onLevelUpSeen,
      language = language,
      onClick = { profileDialogVisible = true },
    )

    HeroPanel(
      title = cleanText(language, UiText.WorldFlagGame),
      subtitle = cleanText(language, UiText.HeroSubtitle),
      language = language,
      onStartClick = onStartClick,
      onMedalsClick = onMedalsClick,
      onAchievementsClick = onAchievementsClick,
      onFavoritesClick = onFavoritesClick,
      onSettingsClick = onSettingsClick,
      onQuitClick = onQuitClick,
    )

    PracticeSummaryCard(
      language = language,
      countryPracticeStats = countryPracticeStats,
    )
  }
}

@Composable
fun GameModesScreen(
  language: AppLanguage,
  dailyChallengeCache: DailyChallengeCache?,
  mistakeReviewUnlocked: Boolean,
  onBack: () -> Unit,
  onModeSelected: (GameMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expandedInfoMode by remember { mutableStateOf<GameMode?>(null) }

  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanModeSelectionTitle(language))

    visibleGameModes().forEach { mode ->
      ModeCard(
        mode = mode,
        language = language,
        infoExpanded = expandedInfoMode == mode,
        openEnabled =
          when (mode) {
            GameMode.DailyChallenge -> dailyChallengeCache?.completed != true
            GameMode.MistakeReview -> mistakeReviewUnlocked
            else -> true
          },
        openLabel =
          when (mode) {
            GameMode.DailyChallenge -> cleanText(language, UiText.Start)
            else -> cleanText(language, UiText.Open)
          },
        onInfoClick = {
          expandedInfoMode = if (expandedInfoMode == mode) null else mode
        },
        onClick = { onModeSelected(mode) },
      )
    }
  }
}

@Composable
internal fun PracticeSummaryCard(
  language: AppLanguage,
  countryPracticeStats: Map<String, CountryPracticeStats>,
) {
  val favorites = countryPracticeStats.values.count { it.favorite }
  val weakCountries = countryPracticeStats.values.count { it.isWeak }
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Practice focus"
            AppLanguage.Bulgarian -> "Фокус за упражнения"
            AppLanguage.German -> "Übungsfokus"
          },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text =
          when (language) {
            AppLanguage.English -> "Favorites: $favorites | Weak countries: $weakCountries"
            AppLanguage.Bulgarian -> "Любими: $favorites | Трудни страни: $weakCountries"
            AppLanguage.German -> "Favoriten: $favorites | Schwache Länder: $weakCountries"
          },
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@Composable
internal fun StreakCalendarCard(
  language: AppLanguage,
  activityCalendar: Map<Long, ActivityDayRecord>,
  timeZone: AppTimeZone,
) {
  var expanded by remember { mutableStateOf(false) }
  val days = recentActivityDays(activityCalendar, days = if (expanded) 30 else 7, timeZone = timeZone)
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            when (language) {
              AppLanguage.English -> "Streak calendar"
              AppLanguage.Bulgarian -> "Календар на редицата"
              AppLanguage.German -> "Streak-Kalender"
            },
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = { expanded = !expanded }) {
          Text(
            text =
              when (language) {
                AppLanguage.English -> if (expanded) "7 days" else "30 days"
                AppLanguage.Bulgarian -> if (expanded) "7 дни" else "30 дни"
                AppLanguage.German -> if (expanded) "7 Tage" else "30 Tage"
              },
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        days.takeLast(if (expanded) 30 else 7).forEach { record ->
          val active = record.quizzesCompleted > 0
          Surface(
            color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Text(text = dayLabel(record.dayKey, language), style = MaterialTheme.typography.labelSmall)
              Text(
                text = record.quizzesCompleted.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}

internal fun dayLabel(
  dayKey: Long,
  language: AppLanguage,
): String {
  val locale = language.toLocale()
  val date = java.time.LocalDate.ofEpochDay(dayKey)
  return date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale)
}

@Composable
fun MedalsScreen(
  ratings: RatingsProgress,
  language: AppLanguage,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanText(language, UiText.Medals))
    RatingsSection(ratings = ratings, language = language)
  }
}

@Composable
fun AchievementsScreen(
  achievements: AchievementsProgress,
  language: AppLanguage,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanText(language, UiText.Achievements))
    AchievementsSection(achievements = achievements, language = language)
  }
}

@Composable
fun FavoritesScreen(
  countries: List<com.example.flaggameandroid.core.model.FlagCountry>,
  countryPracticeStats: Map<String, CountryPracticeStats>,
  language: AppLanguage,
  onBack: () -> Unit,
  onToggleFavoriteCountry: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val favoriteCountries = countries.filter { countryPracticeStats[it.code]?.favorite == true }
  val grouped = favoriteCountries.groupBy { it.continent }
  var pendingRemovalCountryCode by remember { mutableStateOf<String?>(null) }

  ScreenShell(modifier = modifier) {
    pendingRemovalCountryCode?.let { countryCode ->
      AlertDialog(
        onDismissRequest = { pendingRemovalCountryCode = null },
        title = {
          Text(
            text =
              when (language) {
                AppLanguage.English -> "Remove favorite?"
                AppLanguage.Bulgarian -> "Премахване от любими?"
                AppLanguage.German -> "Favorit entfernen?"
              },
          )
        },
        text = {
          Text(
            text =
              when (language) {
                AppLanguage.English -> "Do you want to remove this country from your favorites?"
                AppLanguage.Bulgarian -> "Искаш ли да премахнеш тази държава от любимите?"
                AppLanguage.German -> "Möchtest du dieses Land aus den Favoriten entfernen?"
              },
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              onToggleFavoriteCountry(countryCode)
              pendingRemovalCountryCode = null
            },
          ) {
            Text(
              text =
                when (language) {
                  AppLanguage.English -> "Remove"
                  AppLanguage.Bulgarian -> "Премахни"
                  AppLanguage.German -> "Entfernen"
                },
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { pendingRemovalCountryCode = null }) {
            Text(
              text =
                when (language) {
                  AppLanguage.English -> "Cancel"
                  AppLanguage.Bulgarian -> "Отказ"
                  AppLanguage.German -> "Abbrechen"
                },
            )
          }
        },
      )
    }

    HeaderRow(title = cleanText(language, UiText.Favorites))

    if (favoriteCountries.isEmpty()) {
      SectionCard(
        title = when (language) {
          AppLanguage.English -> "No favorites yet"
          AppLanguage.Bulgarian -> "Все още няма любими"
          AppLanguage.German -> "Noch keine Favoriten"
        },
      ) {
        Text(
          text = when (language) {
            AppLanguage.English -> "Marked favorites will appear here, grouped by continent."
            AppLanguage.Bulgarian -> "Маркираните любими ще се показват тук, групирани по континенти."
            AppLanguage.German -> "Markierte Favoriten erscheinen hier, nach Kontinenten gruppiert."
          },
        )
      }
    } else {
      grouped.keys.sorted().forEach { continent ->
        val list = grouped[continent].orEmpty()
        if (list.isNotEmpty()) {
          SectionCard(title = localizedContinentName(continent, language)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              list.sortedBy { it.localizedName(language) }.forEach { country ->
                FavoriteCountryRow(
                  country = country,
                  language = language,
                  onRemoveFavoriteCountry = { pendingRemovalCountryCode = it },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FavoriteCountryRow(
  country: com.example.flaggameandroid.core.model.FlagCountry,
  language: AppLanguage,
  onRemoveFavoriteCountry: (String) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = country.emoji, style = MaterialTheme.typography.titleLarge)
      Text(
        text = country.localizedName(language),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(1f),
      )
      Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = CircleShape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier =
          Modifier
            .size(32.dp)
            .clickable { onRemoveFavoriteCountry(country.code) },
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = "×",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}
@Composable
fun SettingsScreen(
  settings: SettingsState,
  hintCount: Int,
  inactiveIconActive: Boolean,
  onBack: () -> Unit,
  onHintDifficultySelected: (HintDifficulty) -> Unit,
  onLanguageSelected: (AppLanguage) -> Unit,
  onTimeZoneSelected: (AppTimeZone) -> Unit,
  onReminderEnabledChanged: (Boolean) -> Unit,
  onResetHintsClick: () -> Unit,
  onAddTestingHintsClick: () -> Unit,
  onTestingLevelUpClick: () -> Unit,
  onTestingResetLevelClick: () -> Unit,
  onUnlockRandomAchievementClick: () -> Unit,
  onLockAllAchievementsClick: () -> Unit,
  onResetAchievementsAndMedalsClick: () -> Unit,
  onResetDailyChallengeClick: () -> Unit,
  onToggleTestingIconClick: () -> Unit,
  onTriggerTestingReminderClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var expandedDifficulty by remember { mutableStateOf<HintDifficulty?>(null) }
  var testingButtonEnabled by remember { mutableStateOf(true) }
  var languageMenuExpanded by remember { mutableStateOf(false) }
  var timeZoneMenuExpanded by remember { mutableStateOf(false) }
  var timeZoneInfoExpanded by remember { mutableStateOf(false) }

  LaunchedEffect(testingButtonEnabled) {
    if (!testingButtonEnabled) {
      delay(3_000)
      testingButtonEnabled = true
    }
  }

  ScreenShell(modifier = modifier) {
    HeaderRow(title = t(settings.language, UiText.Settings))

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = t(settings.language, UiText.Language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          LanguageSelector(
            selectedLanguage = settings.language,
            expanded = languageMenuExpanded,
            onExpandedChange = { languageMenuExpanded = it },
            onLanguageSelected = onLanguageSelected,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = localizedTimeZoneTitle(settings.language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          TimeZoneSelector(
            selectedTimeZone = settings.timeZone,
            expanded = timeZoneMenuExpanded,
            onExpandedChange = { timeZoneMenuExpanded = it },
            onTimeZoneSelected = onTimeZoneSelected,
          )
          InfoButton(onClick = { timeZoneInfoExpanded = !timeZoneInfoExpanded })
        }
        if (timeZoneInfoExpanded) {
          InfoPanel(text = localizedTimeZoneInfo(settings.language))
        }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = t(settings.language, UiText.Hints),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
          )
          Text("$hintCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HintDifficulty.entries.forEach { difficulty ->
          CompactInfoRow(
            title = localizedHintDifficultyTitle(difficulty, settings.language),
            shortText = localizedHintDifficultyShortRule(difficulty, settings.language),
            infoText = localizedHintDifficultyDescription(difficulty, settings.language),
            selected = settings.hintDifficulty == difficulty,
            infoExpanded = expandedDifficulty == difficulty,
            onClick = { onHintDifficultySelected(difficulty) },
            onInfoClick = {
              expandedDifficulty = if (expandedDifficulty == difficulty) null else difficulty
            },
          )
        }
      }
    }

    ReminderSettingsCard(
      language = settings.language,
      reminderEnabled = settings.reminderEnabled,
      onReminderEnabledChanged = onReminderEnabledChanged,
    )

    TestingToolsCard(
      language = settings.language,
      inactiveIconActive = inactiveIconActive,
      testingButtonEnabled = testingButtonEnabled,
      onAddTestingHintsClick = {
        onAddTestingHintsClick()
        testingButtonEnabled = false
      },
      onResetHintsClick = onResetHintsClick,
      onTestingLevelUpClick = onTestingLevelUpClick,
      onTestingResetLevelClick = onTestingResetLevelClick,
      onUnlockRandomAchievementClick = onUnlockRandomAchievementClick,
      onLockAllAchievementsClick = onLockAllAchievementsClick,
      onResetAchievementsAndMedalsClick = onResetAchievementsAndMedalsClick,
      onResetDailyChallengeClick = onResetDailyChallengeClick,
      onToggleTestingIconClick = onToggleTestingIconClick,
      onTriggerTestingReminderClick = onTriggerTestingReminderClick,
    )
  }
}





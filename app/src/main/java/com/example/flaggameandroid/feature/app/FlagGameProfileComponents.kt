package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flaggameandroid.core.model.ActivityDayRecord
import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.theme.FlagGameAndroidTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun ProfileEditorDialog(
  profile: ProfileState,
  levelProgress: LevelProgressState,
  activityCalendar: Map<Long, ActivityDayRecord>,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (String, Int) -> Unit,
) {
  var nameDraft by remember(profile.accountName) { mutableStateOf(profile.accountName) }
  var avatarDraft by remember(profile.avatarIndex) { mutableStateOf(profile.avatarIndex) }
  var avatarPickerVisible by remember { mutableStateOf(false) }
  var nameEditorVisible by remember { mutableStateOf(true) }

  if (avatarPickerVisible) {
    AvatarPickerSheet(
      selectedAvatarIndex = avatarDraft,
      language = language,
      level = levelProgress.level,
      onDismiss = { avatarPickerVisible = false },
      onAvatarSelected = {
        avatarDraft = it
        avatarPickerVisible = false
      },
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "${profile.displayName} (lvl ${levelProgress.level})",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Box(
        modifier =
          Modifier
            .heightIn(max = 560.dp)
            .verticalScroll(rememberScrollState()),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
              shape = CircleShape,
            ) {
              Text(
                text = avatarFor(avatarDraft),
                modifier = Modifier.padding(10.dp),
                fontSize = 28.sp,
              )
            }
            OutlinedButton(
              onClick = { nameEditorVisible = !nameEditorVisible },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Text(
                when (language) {
                  AppLanguage.English -> "Name ✏️"
                  AppLanguage.Bulgarian -> "Име ✏️"
                  AppLanguage.German -> "Name ✏️"
                },
                maxLines = 1,
              )
            }
            OutlinedButton(
              onClick = { avatarPickerVisible = true },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Text(
                when (language) {
                  AppLanguage.English -> "Avatar ✏️"
                  AppLanguage.Bulgarian -> "Аватар ✏️"
                  AppLanguage.German -> "Avatar ✏️"
                },
                maxLines = 1,
              )
            }
          }
          if (nameEditorVisible) {
            OutlinedTextField(
              value = nameDraft,
              onValueChange = { nameDraft = it.take(24) },
              label = { Text(cleanText(language, UiText.AccountName)) },
              placeholder = { Text("Player 1") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
          }
          NextLevelRequirementsCard(
            levelProgress = levelProgress,
            language = language,
          )
          ActivityCalendarCard(
            activityCalendar = activityCalendar,
            language = language,
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = { onSave(nameDraft, avatarDraft) }) {
        Text(
          when (language) {
            AppLanguage.English -> "Save"
            AppLanguage.Bulgarian -> "Запази"
            AppLanguage.German -> "Speichern"
          },
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
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

@Composable
private fun NextLevelRequirementsCard(
  levelProgress: LevelProgressState,
  language: AppLanguage,
) {
  Surface(
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        when (language) {
          AppLanguage.English -> "Next level requirements (for lvl ${levelProgress.level + 1})"
          AppLanguage.Bulgarian -> "Изисквания за следващо ниво (за ниво ${levelProgress.level + 1})"
          AppLanguage.German -> "Anforderungen für das nächste Level (für Level ${levelProgress.level + 1})"
        },
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
      )
      if (ProgressionRules.isMaxLevel(levelProgress.level)) {
        Text(
          when (language) {
            AppLanguage.English -> "You achieved max level."
            AppLanguage.Bulgarian -> "Достигна максималното ниво."
            AppLanguage.German -> "Du hast das Maximallevel erreicht."
          },
        )
      } else {
        Text(
          "${levelProgress.hintsTowardNextLevelDisplay}/${levelProgress.hintsNeeded} ${cleanText(language, UiText.Hints)}" +
            if (levelProgress.hintsTowardNextLevelDisplay >= levelProgress.hintsNeeded) " ✔" else "",
        )
        Text(
          "${levelProgress.correctAnswersTowardNextLevelDisplay}/${levelProgress.correctAnswersNeeded} ${cleanText(language, UiText.CorrectAnswers)}" +
            if (levelProgress.correctAnswersTowardNextLevelDisplay >= levelProgress.correctAnswersNeeded) " ✔" else "",
        )
        Text(
          "${levelProgress.eligibleQuizzesTowardNextLevelDisplay}/${levelProgress.eligibleQuizzesNeeded} ${cleanText(language, UiText.CompletedTests)}" +
            if (levelProgress.eligibleQuizzesTowardNextLevelDisplay >= levelProgress.eligibleQuizzesNeeded) " ✔" else "",
        )
      }
    }
  }
}

@Composable
private fun ActivityCalendarCard(
  activityCalendar: Map<Long, ActivityDayRecord>,
  language: AppLanguage,
) {
  val today = LocalDate.now()
  val currentMonth = YearMonth.from(today)
  var visibleMonthOffset by remember { mutableStateOf(0) }
  val visibleMonth = remember(visibleMonthOffset, today) { currentMonth.minusMonths(visibleMonthOffset.toLong()) }
  val monthGrid =
    remember(visibleMonth, activityCalendar, today) {
      buildActivityCalendarGrid(
        month = visibleMonth,
        activityCalendar = activityCalendar,
        today = today,
      )
    }
  val completedDays = monthGrid.sumOf { row -> row.count { it.state == MonthActivityDayState.Completed } }
  val daysInMonth = visibleMonth.lengthOfMonth()
  val canGoBackFurther = visibleMonthOffset < 12
  val canGoForward = visibleMonthOffset > 0

  Surface(
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        when (language) {
          AppLanguage.English -> "Activity calendar ($completedDays/$daysInMonth)"
          AppLanguage.Bulgarian -> "Календар на активността ($completedDays/$daysInMonth)"
          AppLanguage.German -> "Aktivitätskalender ($completedDays/$daysInMonth)"
        },
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(
          onClick = { if (canGoBackFurther) visibleMonthOffset += 1 },
          enabled = canGoBackFurther,
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        ) {
          Text("<-")
        }
        Text(
          text = visibleMonth.labelForCalendar(language),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Normal,
        )
        TextButton(
          onClick = { if (canGoForward) visibleMonthOffset -= 1 },
          enabled = canGoForward,
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        ) {
          Text("->")
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf("M", "T", "W", "T", "F", "S", "S").forEach { weekday ->
            Text(
              text = weekday,
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              maxLines = 1,
            )
          }
        }
        monthGrid.forEach { week ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            week.forEach { day ->
              Surface(
                color =
                  when (day.state) {
                    MonthActivityDayState.Completed -> Color(0xFF2F9E68)
                    MonthActivityDayState.Missed -> Color(0xFFB84A4A)
                    MonthActivityDayState.Pending -> MaterialTheme.colorScheme.surfaceVariant
                    MonthActivityDayState.Disabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                  },
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.weight(1f),
              ) {
                Box(
                  modifier = Modifier.height(18.dp).fillMaxWidth(),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = day.day?.toString().orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color =
                      when (day.state) {
                        MonthActivityDayState.Completed -> Color.White
                        MonthActivityDayState.Missed -> Color.White
                        MonthActivityDayState.Pending -> MaterialTheme.colorScheme.onSurface
                        MonthActivityDayState.Disabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                      },
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun buildActivityCalendarGrid(
  month: YearMonth,
  activityCalendar: Map<Long, ActivityDayRecord>,
  today: LocalDate,
): List<List<MonthActivityDayUiState>> {
  val firstDayOfMonth = month.atDay(1)
  val leadingBlankDays = firstDayOfMonth.dayOfWeek.value - 1
  val cells = mutableListOf<MonthActivityDayUiState>()

  repeat(leadingBlankDays) {
    cells += MonthActivityDayUiState(day = null, state = MonthActivityDayState.Disabled)
  }

  repeat(month.lengthOfMonth()) { index ->
    val dayOfMonth = index + 1
    val date = month.atDay(dayOfMonth)
    val record = activityCalendar[date.toEpochDay()]
    val completed = (record?.quizzesCompleted ?: 0) > 0
    val state =
      when {
        completed -> MonthActivityDayState.Completed
        date.isBefore(today) -> MonthActivityDayState.Missed
        else -> MonthActivityDayState.Pending
      }
    cells += MonthActivityDayUiState(day = dayOfMonth, state = state)
  }

  while (cells.size % 7 != 0) {
    cells += MonthActivityDayUiState(day = null, state = MonthActivityDayState.Disabled)
  }

  return cells.chunked(7)
}

private fun YearMonth.labelForCalendar(language: AppLanguage): String {
  val locale =
    when (language) {
      AppLanguage.Bulgarian -> Locale.forLanguageTag("bg")
      AppLanguage.German -> Locale.GERMAN
      else -> Locale.ENGLISH
    }
  return "${month.getDisplayName(TextStyle.FULL, locale)} ${year}"
}

private data class MonthActivityDayUiState(
  val day: Int?,
  val state: MonthActivityDayState,
)

private enum class MonthActivityDayState {
  Completed,
  Missed,
  Pending,
  Disabled,
}

@Composable
internal fun AvatarPickerDialog(
  selectedAvatarIndex: Int,
  language: AppLanguage,
  level: Int,
  onDismiss: () -> Unit,
  onAvatarSelected: (Int) -> Unit,
) {
  var showUnlockInfo by remember { mutableStateOf(false) }
  val unlockedAvatarCount = ProgressionRules.unlockedAvatarCount(level)
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        when (language) {
          AppLanguage.English -> "Choose profile icon"
          AppLanguage.Bulgarian -> "Избери икона за профила"
          AppLanguage.German -> "Profilbild wählen"
          else -> cleanText(language, UiText.ChooseProfileIcon)
        },
      )
    },
    text = {
      Box(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AvatarOptions.forEachIndexed { index, avatar ->
            OutlinedButton(
              onClick = { onAvatarSelected(index) },
              colors =
                ButtonDefaults.outlinedButtonColors(
                  containerColor =
                    if (selectedAvatarIndex == index) {
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    } else {
                      Color.Transparent
                    },
                ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            ) {
              Text(avatar, fontSize = 22.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(
          when (language) {
            AppLanguage.English -> "Close"
            AppLanguage.Bulgarian -> "Затвори"
            AppLanguage.German -> "Schließen"
            else -> cleanText(language, UiText.Close)
          },
        )
      }
    },
  )
}

@Composable
internal fun AvatarPickerSheet(
  selectedAvatarIndex: Int,
  language: AppLanguage,
  level: Int,
  onDismiss: () -> Unit,
  onAvatarSelected: (Int) -> Unit,
) {
  var showUnlockInfo by remember { mutableStateOf(false) }
  val unlockedAvatarCount = ProgressionRules.unlockedAvatarCount(level)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(cleanText(language, UiText.ChooseProfileIcon), modifier = Modifier.weight(1f))
        TextButton(onClick = { showUnlockInfo = !showUnlockInfo }) {
          Text("i")
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (showUnlockInfo) {
          InfoPanel(text = localizedAvatarUnlockInfo(language))
        }
        Box(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AvatarOptions.chunked(5).forEachIndexed { rowIndex, avatars ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                avatars.forEachIndexed { columnIndex, avatar ->
                  val index = rowIndex * 5 + columnIndex
                  val unlocked = index < unlockedAvatarCount
                  Surface(
                    onClick = { if (unlocked) onAvatarSelected(index) },
                    color =
                      if (selectedAvatarIndex == index && unlocked) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                      } else {
                        MaterialTheme.colorScheme.surfaceVariant
                      },
                    contentColor =
                      if (unlocked) {
                        MaterialTheme.colorScheme.onSurface
                      } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                      },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                  ) {
                    Box(
                      modifier = Modifier.height(46.dp).fillMaxWidth(),
                      contentAlignment = Alignment.Center,
                    ) {
                      if (unlocked) {
                        Text(avatar, fontSize = 21.sp)
                      } else {
                        Text("🔒", fontSize = 18.sp)
                      }
                    }
                  }
                }
                repeat(5 - avatars.size) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(cleanText(language, UiText.Close))
      }
    },
  )
}

@Composable
internal fun LanguageSelector(
  selectedLanguage: AppLanguage,
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit,
  onLanguageSelected: (AppLanguage) -> Unit,
) {
  Box(modifier = Modifier.widthIn(min = 128.dp, max = 162.dp)) {
    OutlinedButton(
      onClick = { onExpandedChange(!expanded) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = CircleShape,
        ) {
          Text(
            text = languageFlag(selectedLanguage),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
          )
        }
        Text(
          languageName(selectedLanguage),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(start = 6.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
          text = "\u25BE",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(start = 8.dp, end = 2.dp),
        )
      }
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { onExpandedChange(false) },
      modifier = Modifier.widthIn(min = 160.dp, max = 220.dp),
    ) {
      AppLanguage.entries.forEach { language ->
        DropdownMenuItem(
          text = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
              ) {
                Text(
                  text = languageFlag(language),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  fontWeight = FontWeight.Bold,
                )
              }
              Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(languageName(language), fontWeight = FontWeight.Bold)
                Text(
                  languageDescription(language),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          },
          onClick = {
            onLanguageSelected(language)
            onExpandedChange(false)
          },
        )
      }
    }
  }
}

@Composable
internal fun TimeZoneSelector(
  selectedTimeZone: AppTimeZone,
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit,
  onTimeZoneSelected: (AppTimeZone) -> Unit,
) {
  Box(modifier = Modifier.widthIn(min = 104.dp, max = 118.dp)) {
    OutlinedButton(
      onClick = { onExpandedChange(!expanded) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          selectedTimeZone.label,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "\u25BE",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { onExpandedChange(false) },
      modifier = Modifier.widthIn(min = 120.dp, max = 150.dp),
    ) {
      AppTimeZone.entries.forEach { timeZone ->
        DropdownMenuItem(
          text = {
            Text(
              text = timeZone.label,
              fontWeight = if (timeZone == selectedTimeZone) FontWeight.Bold else FontWeight.Normal,
            )
          },
          onClick = {
            onTimeZoneSelected(timeZone)
            onExpandedChange(false)
          },
        )
      }
    }
  }
}

@Preview(showBackground = true, name = "Profile Editor")
@Composable
private fun PreviewProfileEditorDialog() {
  FlagGameAndroidTheme {
    Surface {
      ProfileEditorDialog(
        profile = ProfileState(accountName = "Tony", avatarIndex = 3),
        levelProgress = LevelProgressState(
          level = 4,
          hintsTowardNextLevel = 2,
          correctAnswersTowardNextLevel = 30,
          eligibleQuizzesTowardNextLevel = 1,
        ),
        activityCalendar = emptyMap(),
        language = AppLanguage.English,
        onDismiss = {},
        onSave = { _, _ -> },
      )
    }
  }
}

@Preview(showBackground = true, name = "Language Selector")
@Composable
private fun PreviewLanguageSelector() {
  FlagGameAndroidTheme {
    LanguageSelector(
      selectedLanguage = AppLanguage.German,
      expanded = true,
      onExpandedChange = {},
      onLanguageSelected = {},
    )
  }
}

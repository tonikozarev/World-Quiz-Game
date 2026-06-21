package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.flaggameandroid.core.model.AppTimeZone
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.theme.FlagGameAndroidTheme

@Composable
internal fun ProfileEditorDialog(
  profile: ProfileState,
  levelProgress: LevelProgressState,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (String, Int) -> Unit,
) {
  var nameDraft by remember(profile.accountName) { mutableStateOf(profile.accountName) }
  var avatarDraft by remember(profile.avatarIndex) { mutableStateOf(profile.avatarIndex) }
  var avatarPickerVisible by remember { mutableStateOf(false) }

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
    title = { Text(cleanText(language, UiText.Profile)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            shape = CircleShape,
          ) {
            Text(
              text = avatarFor(avatarDraft),
              modifier = Modifier.padding(16.dp),
              fontSize = 34.sp,
            )
          }
          Button(onClick = { avatarPickerVisible = true }) {
            Text(
              when (language) {
                AppLanguage.English -> "\u270E Change icon"
                AppLanguage.Bulgarian -> "✎ Промени иконата"
                AppLanguage.German -> "✎ Symbol ändern"
              },
            )
          }
        }
        OutlinedTextField(
          value = nameDraft,
          onValueChange = { nameDraft = it.take(24) },
          label = { Text(cleanText(language, UiText.AccountName)) },
          placeholder = { Text("Player 1") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Surface(
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              when (language) {
                AppLanguage.English -> "Next level requirements"
                AppLanguage.Bulgarian -> "Изисквания за следващо ниво"
                AppLanguage.German -> "Anforderungen für das nächste Level"
              },
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Text("${levelProgress.hintsTowardNextLevel}/${levelProgress.hintsNeeded} ${cleanText(language, UiText.Hints)}")
            Text("${levelProgress.correctAnswersTowardNextLevel}/${levelProgress.correctAnswersNeeded} ${cleanText(language, UiText.CorrectAnswers)}")
            Text("${levelProgress.eligibleQuizzesTowardNextLevel}/${levelProgress.eligibleQuizzesNeeded} ${cleanText(language, UiText.CompletedTests)}")
          }
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
          AppLanguage.Bulgarian -> "Избери икона на профила"
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
        InfoButton(onClick = { showUnlockInfo = !showUnlockInfo })
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
                        Text("\uD83D\uDD12", fontSize = 18.sp)
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
        )
        Text(
          text = "\u25BE",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.graphicsLayer {
            rotationZ = if (expanded) 180f else 0f
          },
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
          modifier = Modifier.graphicsLayer {
            rotationZ = if (expanded) 180f else 0f
          },
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
        levelProgress = LevelProgressState(level = 4, hintsTowardNextLevel = 2, correctAnswersTowardNextLevel = 30, eligibleQuizzesTowardNextLevel = 1),
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

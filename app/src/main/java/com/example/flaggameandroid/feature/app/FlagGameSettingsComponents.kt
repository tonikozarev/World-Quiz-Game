package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun ReminderSettingsCard(
  language: AppLanguage,
  reminderEnabled: Boolean,
  onReminderEnabledChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
      ) {
        Text(
          text = when (language) {
            AppLanguage.English -> "Played today?"
            AppLanguage.Bulgarian -> "Игра ли днес?"
            AppLanguage.German -> "Heute gespielt?"
          },
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f),
        )
        Switch(
          checked = reminderEnabled,
          onCheckedChange = onReminderEnabledChanged,
          modifier = Modifier.graphicsLayer(scaleX = 0.88f, scaleY = 0.88f),
        )
      }
      Text(
        text = when (language) {
          AppLanguage.English -> "The app sends a notification at 12:00 if you have not opened it yet today."
          AppLanguage.Bulgarian -> "Приложението изпраща известие в 12:00, ако още не си го отворил днес."
          AppLanguage.German -> "Um 12:00 kommt eine Benachrichtigung, falls du die App heute nicht geöffnet hast."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
internal fun TestingToolsCard(
  language: AppLanguage,
  inactiveIconActive: Boolean,
  testingButtonEnabled: Boolean,
  onAddTestingHintsClick: () -> Unit,
  onResetHintsClick: () -> Unit,
  onTestingLevelUpClick: () -> Unit,
  onTestingResetLevelClick: () -> Unit,
  onUnlockRandomAchievementClick: () -> Unit,
  onLockAllAchievementsClick: () -> Unit,
  onResetAchievementsAndMedalsClick: () -> Unit,
  onResetDailyChallengeClick: () -> Unit,
  onToggleTestingIconClick: () -> Unit,
  onTriggerTestingReminderClick: () -> Unit,
  onScheduleTestingReminderInOneMinuteClick: () -> Unit,
  onScheduleTestingInactiveIconInOneMinuteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  SectionCard(
    title = when (language) {
      AppLanguage.English -> "Testing"
      AppLanguage.Bulgarian -> "Тестване"
      AppLanguage.German -> "Testen"
    },
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      Button(
        onClick = {
          onAddTestingHintsClick()
        },
        enabled = testingButtonEnabled,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Add 10 hints"
            AppLanguage.Bulgarian -> "Добави 10 жокера"
            AppLanguage.German -> "10 Hinweise hinzufügen"
          },
        )
      }
      OutlinedButton(
        onClick = onResetHintsClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Reset hints"
            AppLanguage.Bulgarian -> "Нулирай жокерите"
            AppLanguage.German -> "Hinweise zurücksetzen"
          },
        )
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(
        onClick = onTestingLevelUpClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Level +1"
            AppLanguage.Bulgarian -> "Ниво +1"
            AppLanguage.German -> "Level +1"
          },
        )
      }
      OutlinedButton(
        onClick = onTestingResetLevelClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Reset level"
            AppLanguage.Bulgarian -> "Нулирай нивото"
            AppLanguage.German -> "Level zurücksetzen"
          },
        )
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(
        onClick = onUnlockRandomAchievementClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Open random achievement"
            AppLanguage.Bulgarian -> "Случайно постижение"
            AppLanguage.German -> "Zufälliges Achievement öffnen"
          },
          textAlign = TextAlign.Center,
        )
      }
      OutlinedButton(
        onClick = onLockAllAchievementsClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Lock achievements"
            AppLanguage.Bulgarian -> "Заключи постиженията"
            AppLanguage.German -> "Erfolge sperren"
          },
          textAlign = TextAlign.Center,
        )
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(
        onClick = onResetAchievementsAndMedalsClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Reset medals"
            AppLanguage.Bulgarian -> "Нулирай медалите"
            AppLanguage.German -> "Medaillen zurücksetzen"
          },
          textAlign = TextAlign.Center,
        )
      }
      OutlinedButton(
        onClick = onToggleTestingIconClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          if (inactiveIconActive) {
            when (language) {
              AppLanguage.English -> "Switch to normal icon"
              AppLanguage.Bulgarian -> "Към нормална икона"
              AppLanguage.German -> "Zum normalen Symbol wechseln"
            }
          } else {
            when (language) {
              AppLanguage.English -> "Switch to inactive icon"
              AppLanguage.Bulgarian -> "Към неактивна икона"
              AppLanguage.German -> "Zum inaktiven Symbol wechseln"
            }
          },
        )
      }
    }
    OutlinedButton(
      onClick = onResetDailyChallengeClick,
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
    ) {
      Text(
        when (language) {
          AppLanguage.English -> "Reset daily challenge"
          AppLanguage.Bulgarian -> "Нулирай дневното предизвикателство"
          AppLanguage.German -> "Tägliche Herausforderung zurücksetzen"
        },
        textAlign = TextAlign.Center,
      )
    }
    OutlinedButton(
      onClick = onTriggerTestingReminderClick,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        when (language) {
          AppLanguage.English -> "Send test reminder"
          AppLanguage.Bulgarian -> "Изпрати тестово напомняне"
          AppLanguage.German -> "Test-Erinnerung senden"
        },
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(
        onClick = onScheduleTestingReminderInOneMinuteClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Reminder in 1 min"
            AppLanguage.Bulgarian -> "Напомняне след 1 мин"
            AppLanguage.German -> "Erinnerung in 1 Min"
          },
          textAlign = TextAlign.Center,
        )
      }
      OutlinedButton(
        onClick = onScheduleTestingInactiveIconInOneMinuteClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Inactive icon in 1 min"
            AppLanguage.Bulgarian -> "Неактивна икона след 1 мин"
            AppLanguage.German -> "Inaktives Symbol in 1 Min"
          },
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

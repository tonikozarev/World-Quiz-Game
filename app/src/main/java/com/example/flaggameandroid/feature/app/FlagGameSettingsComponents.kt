package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun TestingToolsCard(
  language: AppLanguage,
  testingButtonEnabled: Boolean,
  onAddTestingHintsClick: () -> Unit,
  onResetHintsClick: () -> Unit,
  onTestingLevelUpClick: () -> Unit,
  onTestingResetLevelClick: () -> Unit,
  onUnlockRandomAchievementClick: () -> Unit,
  onLockAllAchievementsClick: () -> Unit,
  onResetAchievementsAndMedalsClick: () -> Unit,
  onResetDailyChallengeClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  SectionCard(
    title =
      when (language) {
        AppLanguage.English -> "Testing"
        AppLanguage.Bulgarian -> "Тестване"
        AppLanguage.German -> "Testen"
      },
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      Button(
        onClick = onAddTestingHintsClick,
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
            AppLanguage.Bulgarian -> "Отключи произволно постижение"
            AppLanguage.German -> "Zufälligen Erfolg öffnen"
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
    OutlinedButton(
      onClick = onResetAchievementsAndMedalsClick,
      modifier = Modifier.fillMaxWidth(),
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
  }
}

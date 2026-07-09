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
    title = when (language) {
      AppLanguage.English -> "Testing"
      AppLanguage.Bulgarian -> "РўРµСЃС‚РІР°РЅРµ"
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
            AppLanguage.Bulgarian -> "Р”РѕР±Р°РІРё 10 Р¶РѕРєРµСЂР°"
            AppLanguage.German -> "10 Hinweise hinzufГјgen"
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
            AppLanguage.Bulgarian -> "РќСѓР»РёСЂР°Р№ Р¶РѕРєРµСЂРёС‚Рµ"
            AppLanguage.German -> "Hinweise zurГјcksetzen"
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
            AppLanguage.Bulgarian -> "РќРёРІРѕ +1"
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
            AppLanguage.Bulgarian -> "РќСѓР»РёСЂР°Р№ РЅРёРІРѕС‚Рѕ"
            AppLanguage.German -> "Level zurГјcksetzen"
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
            AppLanguage.Bulgarian -> "РЎР»СѓС‡Р°Р№РЅРѕ РїРѕСЃС‚РёР¶РµРЅРёРµ"
            AppLanguage.German -> "ZufГ¤lliges Achievement Г¶ffnen"
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
            AppLanguage.Bulgarian -> "Р—Р°РєР»СЋС‡Рё РїРѕСЃС‚РёР¶РµРЅРёСЏС‚Р°"
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
          AppLanguage.Bulgarian -> "РќСѓР»РёСЂР°Р№ РјРµРґР°Р»РёС‚Рµ"
          AppLanguage.German -> "Medaillen zurГјcksetzen"
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
          AppLanguage.Bulgarian -> "РќСѓР»РёСЂР°Р№ РґРЅРµРІРЅРѕС‚Рѕ РїСЂРµРґРёР·РІРёРєР°С‚РµР»СЃС‚РІРѕ"
          AppLanguage.German -> "TГ¤gliche Herausforderung zurГјcksetzen"
        },
        textAlign = TextAlign.Center,
      )
    }
  }
}

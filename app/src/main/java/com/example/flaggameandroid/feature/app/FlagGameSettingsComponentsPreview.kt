package com.example.flaggameandroid.feature.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.flaggameandroid.theme.FlagGameAndroidTheme

@Preview(showBackground = true, name = "Reminder Settings")
@Composable
private fun PreviewReminderSettingsCard() {
  FlagGameAndroidTheme {
    Surface {
      ReminderSettingsCard(
        language = AppLanguage.English,
        reminderEnabled = true,
        onReminderEnabledChanged = {},
      )
    }
  }
}

@Preview(showBackground = true, name = "Testing Tools")
@Composable
private fun PreviewTestingToolsCard() {
  FlagGameAndroidTheme {
    Surface {
      TestingToolsCard(
        language = AppLanguage.German,
        inactiveIconActive = false,
        testingButtonEnabled = true,
        onAddTestingHintsClick = {},
        onResetHintsClick = {},
        onTestingLevelUpClick = {},
        onTestingResetLevelClick = {},
        onUnlockRandomAchievementClick = {},
        onLockAllAchievementsClick = {},
        onResetAchievementsAndMedalsClick = {},
        onResetDailyChallengeClick = {},
        onToggleTestingIconClick = {},
        onTriggerTestingReminderClick = {},
      )
    }
  }
}

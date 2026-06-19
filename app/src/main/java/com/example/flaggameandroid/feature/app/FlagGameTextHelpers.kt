package com.example.flaggameandroid.feature.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizVariant

internal fun wrongOptionLabel(
  country: FlagCountry,
  variant: QuizVariant,
  language: AppLanguage,
): String =
  if (variant == QuizVariant.CountryToFlag) {
    country.emoji
  } else {
    country.localizedName(language)
  }

internal val AvatarOptions =
  listOf(
    "\uD83C\uDFAF",
    "\uD83C\uDFC6",
    "\uD83C\uDF0D",
    "\uD83D\uDD25",
    "\u2B50",
    "\uD83D\uDCA1",
    "\uD83E\uDDED",
    "\uD83D\uDC8E",
    "\uD83D\uDCAA",
    "\uD83C\uDF89",
    "\uD83C\uDFAE",
    "\uD83D\uDEA9",
    "\uD83C\uDF10",
    "\uD83E\uDD47",
    "\uD83D\uDE80",
    "\uD83E\uDD85",
    "\uD83E\uDD81",
    "\uD83D\uDC3A",
    "\uD83E\uDD8A",
    "\uD83D\uDC31",
    "\uD83D\uDC36",
    "\uD83D\uDC38",
    "\uD83D\uDC22",
    "\uD83D\uDC27",
    "\uD83D\uDC19",
    "\uD83E\uDD16",
    "\uD83E\uDDD9",
    "\uD83E\uDD77",
    "\uD83E\uDD20",
    "\uD83D\uDC51",
    "\uD83C\uDF1F",
    "\uD83C\uDF08",
    "\uD83E\uDE90",
    "\uD83C\uDFA8",
    "\uD83C\uDFB8",
    "\uD83C\uDFB5",
    "\uD83C\uDFB2",
    "\uD83C\uDFC0",
    "\u26BD",
    "\uD83C\uDFBE",
    "\uD83E\uDD4A",
    "\uD83C\uDF53",
    "\uD83C\uDF4E",
    "\uD83C\uDF4B",
    "\uD83C\uDF44",
    "\uD83C\uDF3B",
    "\uD83C\uDF35",
    "\uD83C\uDF32",
    "\uD83C\uDF1A",
    "\u2600\uFE0F",
  )

internal fun avatarFor(index: Int): String = AvatarOptions.getOrElse(index) { AvatarOptions.first() }

internal fun languageFlag(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "\uD83C\uDDEC\uD83C\uDDE7"
    AppLanguage.Bulgarian -> "\uD83C\uDDE7\uD83C\uDDEC"
    AppLanguage.German -> "\uD83C\uDDE9\uD83C\uDDEA"
  }

internal fun languageName(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "English"
    AppLanguage.Bulgarian -> "Български"
    AppLanguage.German -> "Deutsch"
  }

internal fun languageDescription(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "English (UK)"
    AppLanguage.Bulgarian -> "Български (BG)"
    AppLanguage.German -> "Deutsch (DE)"
  }

internal fun localizedHintButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Hint"
    AppLanguage.Bulgarian -> "Жокер"
    AppLanguage.German -> "Hinweis"
  }

internal fun localizedRevealButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Reveal"
    AppLanguage.Bulgarian -> "Разкрий"
    AppLanguage.German -> "Aufdecken"
  }

internal fun localizedVerifyButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Verify"
    AppLanguage.Bulgarian -> "Провери"
    AppLanguage.German -> "Prüfen"
  }

internal fun localizedQuizInfoButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Info \uD835\uDC8A"
    AppLanguage.Bulgarian -> "Инфо \uD835\uDC8A"
    AppLanguage.German -> "Info \uD835\uDC8A"
  }

internal fun localizedUnskipButtonLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Unskip \u21B7"
    AppLanguage.Bulgarian -> "Върни \u21B7"
    AppLanguage.German -> "Zurück \u21B7"
  }

internal fun formatScore(score: Int): String =
  if (score % 2 == 0) {
    (score / 2).toString()
  } else {
    "${score / 2}.5"
  }

internal fun modeSelectionTitle(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Choose mode"
    AppLanguage.Bulgarian -> "Избери режим"
    AppLanguage.German -> "Modus wählen"
  }

internal fun quizCompleteTitle(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Quiz complete"
    AppLanguage.Bulgarian -> "Тестът е завършен"
    AppLanguage.German -> "Quiz beendet"
  }

internal fun localizedHeroPill(
  index: Int,
  language: AppLanguage,
): String =
  when (index) {
    0 ->
      when (language) {
        AppLanguage.English -> "Global"
        AppLanguage.Bulgarian -> "Глобално"
        AppLanguage.German -> "Global"
      }
    1 ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    else ->
      when (language) {
        AppLanguage.English -> "Flags"
        AppLanguage.Bulgarian -> "Флагове"
        AppLanguage.German -> "Flaggen"
      }
  }

internal fun allInRewardInfo(
  language: AppLanguage,
  hintSettingLabel: String,
  hasAllVariants: Boolean,
  rewardLevels: String,
  isImpossible: Boolean,
): String =
  when (language) {
    AppLanguage.English ->
      if (hasAllVariants) {
        "Hint setting: $hintSettingLabel. Perfect clear reward is active. Finish with no mistakes using all 3 variants to earn $rewardLevels full level(s)." +
          if (isImpossible) "" else " Switch to 'The impossible one' to earn +1 more level, for +2 full levels total."
      } else {
        "Hint setting: $hintSettingLabel. Perfect clear reward is inactive because not all 3 variants are selected. Re-enable every variant to earn $rewardLevels full level(s)." +
          if (isImpossible) "" else " With 'The impossible one' enabled, that reward would become +2 full levels."
      }
    AppLanguage.Bulgarian ->
      if (hasAllVariants) {
        "Настройка за жокери: $hintSettingLabel. Наградата за перфектен тест е активна. Завърши без грешка с всичките 3 варианта, за да вземеш $rewardLevels пълно ниво." +
          if (isImpossible) "" else " Ако включиш 'The impossible one', ще получиш още +1 ниво, общо +2."
      } else {
        "Настройка за жокери: $hintSettingLabel. Наградата за перфектен тест е неактивна, защото не са избрани и 3-те варианта. Включи всички варианти, за да вземеш $rewardLevels пълно ниво." +
          if (isImpossible) "" else " С 'The impossible one' тази награда ще стане +2 пълни нива."
      }
    AppLanguage.German ->
      if (hasAllVariants) {
        "Hinweis-Einstellung: $hintSettingLabel. Die Belohnung für einen fehlerfreien Durchlauf ist aktiv. Beende das Quiz ohne Fehler mit allen 3 Varianten, um $rewardLevels volle Level zu erhalten." +
          if (isImpossible) "" else " Mit 'The impossible one' bekommst du +1 Level mehr, also insgesamt +2 volle Level."
      } else {
        "Hinweis-Einstellung: $hintSettingLabel. Die Belohnung für einen fehlerfreien Durchlauf ist inaktiv, weil nicht alle 3 Varianten ausgewählt sind. Aktiviere alle Varianten, um $rewardLevels volle Level zu erhalten." +
          if (isImpossible) "" else " Mit 'The impossible one' würde diese Belohnung +2 volle Level geben."
      }
  }

internal fun displayModeTitle(
  mode: GameMode?,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> cleanModeTitle(GameMode.Training, language)
    GameMode.Continents -> cleanModeTitle(GameMode.Continents, language)
    GameMode.SpeedRun -> cleanModeTitle(GameMode.SpeedRun, language)
    GameMode.AllIn -> cleanModeTitle(GameMode.AllIn, language)
    GameMode.LocalMultiplayer -> cleanModeTitle(GameMode.LocalMultiplayer, language)
    null ->
      when (language) {
        AppLanguage.English -> "Quiz"
        AppLanguage.Bulgarian -> "Тест"
        AppLanguage.German -> "Quiz"
      }
  }

@Composable
internal fun buttonContentColor(background: Color): Color {
  return when (background) {
    MaterialTheme.colorScheme.primary -> MaterialTheme.colorScheme.onPrimary
    MaterialTheme.colorScheme.surfaceVariant -> MaterialTheme.colorScheme.onSurface
    else -> if (background.luminance() > 0.5f) Color.Black else Color.White
  }
}

internal fun levelUpBody(language: AppLanguage, level: Int): String =
  when (language) {
    AppLanguage.English -> "You reached level $level and earned 5 free hints."
    AppLanguage.Bulgarian -> "Достигна ниво $level и получи 5 безплатни жокера."
    AppLanguage.German -> "Du hast Level $level erreicht und 5 kostenlose Hinweise erhalten."
  }

internal fun speedRunElapsedMillis(
  quiz: QuizState,
  nowMillis: Long,
) : Long {
  if (quiz.mode != GameMode.SpeedRun || quiz.startedAtEpochMillis <= 0L) return 0L
  return (nowMillis - quiz.startedAtEpochMillis + (quiz.speedRunPenaltySeconds * 1000L)).coerceAtLeast(0L)
}

internal fun formatElapsedTime(totalMillis: Long): String {
  val totalSeconds = (totalMillis / 1000L).coerceAtLeast(0L)
  val minutes = totalSeconds / 60L
  val seconds = totalSeconds % 60L
  return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

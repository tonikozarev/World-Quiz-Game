package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.MedalTier
import com.example.flaggameandroid.core.model.QuizVariant

private fun tr(language: AppLanguage, english: String, bulgarian: String, german: String): String =
  when (language) {
    AppLanguage.English -> english
    AppLanguage.Bulgarian -> bulgarian
    AppLanguage.German -> german
  }

internal fun modeBaseTitle(
  base: MultiplayerQuizBase,
  language: AppLanguage,
): String =
  when (base) {
    MultiplayerQuizBase.Continents -> tr(language, "Continents", "Континенти", "Kontinente")
    MultiplayerQuizBase.AllIn -> tr(language, "No Bluff, All Tough", "Без блъф, само трудно", "Kein Bluff, alles schwer")
  }

internal fun modeBaseDescription(
  base: MultiplayerQuizBase,
  language: AppLanguage,
): String =
  when (base) {
    MultiplayerQuizBase.Continents -> tr(language, "Quiz by selected continents.", "Тест по избрани континенти.", "Quiz nach ausgewählten Kontinenten.")
    MultiplayerQuizBase.AllIn -> tr(language, "Quiz from all countries.", "Тест от всички държави.", "Quiz mit allen Ländern.")
  }

internal fun localizedModeTitle(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> tr(language, "Training", "Тренировка", "Training")
    GameMode.Continents -> tr(language, "Continents", "Континенти", "Kontinente")
    GameMode.DailyChallenge -> tr(language, "Daily challenge", "Дневно предизвикателство", "Tägliche Herausforderung")
    GameMode.MistakeReview -> tr(language, "Mistake review", "Преглед на грешките", "Fehlerprüfung")
    GameMode.SpeedRun -> tr(language, "Speed run", "Скоростна игра", "Schnelllauf")
    GameMode.AllIn -> tr(language, "No Bluff, All Tough", "Без блъф, само трудно", "Kein Bluff, alles schwer")
    GameMode.LocalMultiplayer -> tr(language, "Local multiplayer", "Локална игра", "Lokaler Mehrspieler")
    else -> mode.title
  }

internal fun localizedModeDescription(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      tr(
        language,
        "Mix flags, country names, and typed answers at your pace. Training does not give level-up progress.",
        "Смесвай флагове, имена на държави и писмени отговори със свое темпо. Тренировката не дава прогрес към ниво.",
        "Mische Flaggen, Ländernamen und Texteingaben in deinem Tempo. Training bringt keinen Level-Fortschritt.",
      )
    GameMode.Continents ->
      tr(language, "Build a quiz from the continents you want to practice.", "Създай тест от континентите, които искаш да упражняваш.", "Erstelle ein Quiz aus den Kontinenten, die du üben möchtest.")
    GameMode.DailyChallenge ->
      tr(language, "One fixed quiz each local day.", "Един фиксиран тест за всеки местен ден.", "Ein festes Quiz pro lokalem Tag.")
    GameMode.MistakeReview ->
      tr(language, "Practice the countries you often miss.", "Упражнявай държавите, които често пропускаш.", "Übe die Länder, die du oft verpasst.")
    GameMode.SpeedRun ->
      tr(language, "Same quiz setup as continents, but time is always ticking. Hints and reveals cost seconds.", "Същата настройка като при континентите, но времето тече. Подсказките и разкритията струват секунди.", "Dasselbe Setup wie bei Kontinente, aber die Zeit läuft. Hinweise und Aufdeckungen kosten Sekunden.")
    GameMode.AllIn ->
      tr(language, "All countries with only the variants you choose.", "Всички държави само с вариантите, които избереш.", "Alle Länder mit nur den Varianten, die du auswählst.")
    GameMode.LocalMultiplayer ->
      tr(language, "Up to 5 players pass one device and play turn by turn.", "До 5 играчи използват едно устройство и играят поред.", "Bis zu 5 Spieler teilen sich ein Gerät und spielen reihum.")
    else -> mode.description
  }

internal fun localizedModeShortLabel(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> tr(language, "Practice freely.", "Упражнявай се свободно.", "Frei üben.")
    GameMode.Continents -> tr(language, "Pick continents.", "Избери континенти.", "Kontinente wählen.")
    GameMode.DailyChallenge -> tr(language, "Today’s challenge.", "Днешното предизвикателство.", "Heutige Herausforderung.")
    GameMode.MistakeReview -> tr(language, "Fix mistakes.", "Поправи грешките.", "Fehler verbessern.")
    GameMode.SpeedRun -> tr(language, "Beat the clock.", "Победи времето.", "Schlage die Zeit.")
    GameMode.AllIn -> tr(language, "All countries.", "Всички държави.", "Alle Länder.")
    GameMode.LocalMultiplayer -> tr(language, "Play together.", "Играй заедно.", "Zusammen spielen.")
    else -> mode.title
  }

internal fun localizedVariantTitle(
  variant: QuizVariant,
  language: AppLanguage,
): String =
  when (variant) {
    QuizVariant.FlagToCountry -> tr(language, "Flag -> country", "Флаг -> държава", "Flagge -> Land")
    QuizVariant.CountryToFlag -> tr(language, "Country -> flag", "Държава -> флаг", "Land -> Flagge")
    QuizVariant.TypeCountryName -> tr(language, "Type the country", "Напиши държавата", "Land eintippen")
  }

internal fun localizedVariantDescription(
  variant: QuizVariant,
  language: AppLanguage,
): String =
  when (variant) {
    QuizVariant.FlagToCountry -> tr(language, "See a flag and pick the country.", "Виж флаг и избери държавата.", "Sieh eine Flagge und wähle das Land.")
    QuizVariant.CountryToFlag -> tr(language, "See a country and pick the flag.", "Виж държава и избери флага.", "Sieh ein Land und wähle die Flagge.")
    QuizVariant.TypeCountryName -> tr(language, "See a flag and write the country name.", "Виж флаг и напиши името на държавата.", "Sieh eine Flagge und tippe den Ländernamen.")
  }

internal fun localizedHintDifficultyTitle(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie -> tr(language, "Easy (Every 3-streak)", "Лесно (Всеки 3 поредни ✔)", "Einsteiger (Alle 3 in Folge)")
    HintDifficulty.Medium -> tr(language, "Medium (Every 5-streak)", "Средно (5 поредни ✔)", "Mittel (Alle 5 in Folge)")
    HintDifficulty.Hard -> tr(language, "Hard (Every 10-streak)", "Трудно (10 поредни ✔)", "Schwer (Alle 10 in Folge)")
    HintDifficulty.Impossible -> tr(language, "Impossible (Every 50-streak)", "Невъзможно (50 поредни ✔)", "Unmöglich (Alle 50 in Folge)")
  }

internal fun localizedHintDifficultyShortRule(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie -> tr(language, "Every 3-streak", "Всеки 3 верни поред", "Alle 3 in Folge")
    HintDifficulty.Medium -> tr(language, "Every 5-streak", "Всеки 5 верни поред", "Alle 5 in Folge")
    HintDifficulty.Hard -> tr(language, "Every 10-streak", "Всеки 10 верни поред", "Alle 10 in Folge")
    HintDifficulty.Impossible -> tr(language, "Every 50-streak", "Всеки 50 верни поред", "Alle 50 in Folge")
  }

internal fun localizedHintDifficultyDescription(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie -> tr(language, "Collect 1 hint for every 3 correct answers in a row.", "Събирай 1 жокер за всеки 3 верни отговора подред.", "Sammle 1 Hinweis für je 3 richtige Antworten in Folge.")
    HintDifficulty.Medium -> tr(language, "Collect 1 hint for every 5 correct answers in a row.", "Събирай 1 жокер на всеки 5 верни отговора поред.", "Sammle 1 Hinweis für jeweils 5 richtige Antworten in Folge.")
    HintDifficulty.Hard -> tr(language, "Collect 1 hint for every 10 correct answers in a row.", "Събирай 1 жокер на всеки 10 верни отговора поред.", "Sammle 1 Hinweis für jeweils 10 richtige Antworten in Folge.")
    HintDifficulty.Impossible -> tr(language, "Collect 1 hint for every 50 correct answers in a row.", "Събирай 1 жокер на всеки 50 верни отговора поред.", "Sammle 1 Hinweis für jeweils 50 richtige Antworten in Folge.")
  }

internal fun localizedMedalTitle(
  medalTier: MedalTier,
  language: AppLanguage,
): String =
  when (medalTier) {
    MedalTier.Bronze -> tr(language, "Bronze", "Бронз", "Bronze")
    MedalTier.Silver -> tr(language, "Silver", "Сребро", "Silber")
    MedalTier.Gold -> tr(language, "Gold", "Злато", "Gold")
    MedalTier.Titanium -> tr(language, "Platinum", "Платина", "Platin")
    MedalTier.Diamond -> tr(language, "Diamond", "Диамант", "Diamant")
  }

internal fun localizedMedalLabel(language: AppLanguage): String =
  tr(language, "Perfect quiz count", "Брояч за перфектни тестове", "Zähler für fehlerfreie Quizze")

internal fun localizedMedalIntro(language: AppLanguage): String =
  tr(language, "Perfect quiz counters", "Броячи за перфектни тестове", "Zähler für fehlerfreie Quizze")

internal fun cleanModeSelectionTitle(language: AppLanguage): String =
  tr(language, "Choose mode", "Избери режим", "Modus wählen")

internal fun cleanHeroPill(
  index: Int,
  language: AppLanguage,
): String =
  when (index) {
    0 -> tr(language, "Global", "Глобално", "Global")
    1 -> tr(language, "Continents", "Континенти", "Kontinente")
    else -> tr(language, "Flags", "Флагове", "Flaggen")
  }

internal fun cleanModeTitle(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> tr(language, "Training", "Тренировка", "Training")
    GameMode.Continents -> tr(language, "Continents", "Континенти", "Kontinente")
    GameMode.DailyChallenge -> tr(language, "Daily challenge", "Дневно предизвикателство", "Tägliche Herausforderung")
    GameMode.MistakeReview -> tr(language, "Mistake review", "Преглед на грешките", "Fehlerprüfung")
    GameMode.SpeedRun -> tr(language, "Speed run", "Скоростна игра", "Schnelllauf")
    GameMode.AllIn -> tr(language, "No Bluff, All Tough", "Без блъф, само трудно", "Kein Bluff, alles schwer")
    GameMode.LocalMultiplayer -> tr(language, "Local multiplayer", "Локална игра", "Lokaler Mehrspieler")
    else -> mode.title
  }

internal fun cleanModeShortLabel(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> tr(language, "Practice freely.", "Упражнявай се свободно.", "Frei üben.")
    GameMode.Continents -> tr(language, "Pick continents.", "Избери континенти.", "Kontinente wählen.")
    GameMode.DailyChallenge -> tr(language, "Today’s challenge.", "Днешното предизвикателство.", "Heutige Herausforderung.")
    GameMode.MistakeReview -> tr(language, "Fix mistakes.", "Поправи грешките.", "Fehler verbessern.")
    GameMode.SpeedRun -> tr(language, "Race the timer.", "Надпреварвай времето.", "Renne gegen die Zeit.")
    GameMode.AllIn -> tr(language, "All countries.", "Всички държави.", "Alle Länder.")
    GameMode.LocalMultiplayer -> tr(language, "Play together.", "Играй заедно.", "Zusammen spielen.")
    else -> mode.title
  }

internal fun cleanModeDescription(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training ->
      tr(
        language,
        "Mix flags, country names, and typed answers at your pace. Training does not give level-up progress.",
        "Смесвай флагове, имена на държави и писмени отговори със свое темпо. Тренировката не дава прогрес към ниво.",
        "Mische Flaggen, Ländernamen und Texteingaben in deinem Tempo. Training bringt keinen Level-Fortschritt.",
      )
    GameMode.Continents -> tr(language, "Build a quiz from the continents you want to practice.", "Създай тест от континентите, които искаш да упражняваш.", "Erstelle ein Quiz aus den Kontinenten, die du üben möchtest.")
    GameMode.DailyChallenge -> tr(language, "One fixed quiz each local day.", "Един фиксиран тест за всеки местен ден.", "Ein festes Quiz pro lokalem Tag.")
    GameMode.MistakeReview -> tr(language, "Practice the countries you often miss.", "Упражнявай държавите, които често пропускаш.", "Übe die Länder, die du oft verpasst.")
    GameMode.SpeedRun -> tr(language, "Same quiz setup as continents, but time is always ticking. Hints and reveals cost seconds.", "Същата настройка като при континентите, но времето тече. Подсказките и разкритията струват секунди.", "Dasselbe Setup wie bei Kontinente, aber die Zeit läuft. Hinweise und Aufdeckungen kosten Sekunden.")
    GameMode.AllIn -> tr(language, "All countries with only the variants you choose.", "Всички държави само с вариантите, които избереш.", "Alle Länder mit nur den Varianten, die du auswählst.")
    GameMode.LocalMultiplayer -> tr(language, "Up to 5 players pass one device and play turn by turn.", "До 5 играчи използват едно устройство и играят поред.", "Bis zu 5 Spieler teilen sich ein Gerät und spielen reihum.")
    else -> mode.description
  }

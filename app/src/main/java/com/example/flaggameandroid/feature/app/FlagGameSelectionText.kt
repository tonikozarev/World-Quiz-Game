package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.MedalTier
import com.example.flaggameandroid.core.model.QuizTopic
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
    MultiplayerQuizBase.AllIn -> tr(language, "No Bluff, All Tough", "Без блъф, всичко тежко", "Kein Bluff, alles schwer")
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
    GameMode.CreateQuiz -> tr(language, "Custom Quiz", "Персонален тест", "Benutzerdefiniertes Quiz")
    GameMode.WorldFlags ->
      tr(language, "Country Flags", "Държавни флагове", "Länderflaggen")
    GameMode.DailyChallenge -> tr(language, "Daily challenge", "Дневно предизвикателство", "Tägliche Herausforderung")
    GameMode.MistakeReview -> tr(language, "Mistake review", "Преглед на грешките", "Fehlerprüfung")
    GameMode.LocalMultiplayer -> tr(language, "Local multiplayer", "Локална игра", "Lokaler Mehrspieler")
    else -> mode.title
  }

internal fun localUtcMidnightResetLabel(): String {
  val offsetHours =
    java.time.ZoneId.systemDefault()
      .rules
      .getOffset(java.time.Instant.now())
      .totalSeconds / 3600
  val resetHour = (24 + offsetHours) % 24
  return "${resetHour.toString().padStart(2, '0')}:00"
}

internal fun localizedVariantTitle(
  variant: QuizVariant,
  language: AppLanguage,
  topic: QuizTopic = QuizTopic.Countries,
): String =
  when (variant) {
    QuizVariant.FlagToText ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "Flag -> Country", "Флаг -> Държава", "Flagge -> Land")
        QuizTopic.Capitals -> tr(language, "Flag -> Capital", "Флаг -> Столица", "Flagge -> Hauptstadt")
        QuizTopic.Mixed -> tr(language, "Flag -> Text", "Флаг -> Текст", "Flagge -> Text")
      }
    QuizVariant.TextToFlag ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "Country -> Flag", "Държава -> Флаг", "Land -> Flagge")
        QuizTopic.Capitals -> tr(language, "Capital -> Flag", "Столица -> Флаг", "Hauptstadt -> Flagge")
        QuizTopic.Mixed -> tr(language, "Text -> Flag", "Текст -> Флаг", "Text -> Flagge")
      }
    QuizVariant.TypeText ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "Type the country", "Напиши държавата", "Land eingeben")
        QuizTopic.Capitals -> tr(language, "Type the capital", "Напиши столицата", "Hauptstadt eingeben")
        QuizTopic.Mixed -> tr(language, "Type the text", "Напиши текста", "Text eingeben")
      }
  }

internal fun localizedVariantDescription(
  variant: QuizVariant,
  language: AppLanguage,
  topic: QuizTopic = QuizTopic.Countries,
): String =
  when (variant) {
    QuizVariant.FlagToText ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "See a flag and pick the country.", "Виж флаг и избери държавата.", "Sieh eine Flagge und wähle das Land.")
        QuizTopic.Capitals -> tr(language, "See a flag and pick the capital.", "Виж флаг и избери столицата.", "Sieh eine Flagge und wähle die Hauptstadt.")
        QuizTopic.Mixed -> tr(language, "See a flag and pick the matching text.", "Виж флаг и избери съответния текст.", "Sieh eine Flagge und wähle den passenden Text.")
      }
    QuizVariant.TextToFlag ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "See a country and pick the flag.", "Виж държава и избери флага.", "Sieh ein Land und wähle die Flagge.")
        QuizTopic.Capitals -> tr(language, "See a capital and pick the flag.", "Виж столица и избери флага.", "Sieh eine Hauptstadt und wähle die Flagge.")
        QuizTopic.Mixed -> tr(language, "See text and pick the flag.", "Виж текст и избери флага.", "Sieh Text und wähle die Flagge.")
      }
    QuizVariant.TypeText ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "See a flag and write the country name.", "Виж флаг и напиши името на държавата.", "Sieh eine Flagge und tippe den Ländernamen.")
        QuizTopic.Capitals -> tr(language, "See a flag and write the capital.", "Виж флаг и напиши столицата.", "Sieh eine Flagge und tippe die Hauptstadt.")
        QuizTopic.Mixed -> tr(language, "See a flag and write the matching text.", "Виж флаг и напиши съответния текст.", "Sieh eine Flagge und tippe den passenden Text.")
      }
  }

internal fun localizedHintDifficultyTitle(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie -> tr(language, "Easy (Every 3-streak)", "Лесно (Всеки 3 поредни ✔)", "Einsteiger (Alle 3 in Folge)")
    HintDifficulty.Medium -> tr(language, "Medium (Every 5-streak)", "Средно (5 поредни ✔)", "Mittel (Alle 5 in Folge)")
    HintDifficulty.Hard -> tr(language, "Hard (Every 10-streak)", "Трудно (10 поредни ✔)", "Schwer (Alle 10 in Folge)")
    HintDifficulty.Impossible -> tr(language, "Impossible (Every 25-streak)", "Невъзможно (25 поредни ✔)", "Unmöglich (Alle 25 in Folge)")
  }

internal fun localizedHintDifficultyShortRule(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie -> tr(language, "Every 3-streak", "Всеки 3 верни поред", "Alle 3 in Folge")
    HintDifficulty.Medium -> tr(language, "Every 5-streak", "Всеки 5 верни поред", "Alle 5 in Folge")
    HintDifficulty.Hard -> tr(language, "Every 10-streak", "Всеки 10 верни поред", "Alle 10 in Folge")
    HintDifficulty.Impossible -> tr(language, "Every 25-streak", "Всеки 25 верни поред", "Alle 25 in Folge")
  }

internal fun localizedHintDifficultyDescription(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Rookie -> tr(language, "Collect 1 hint for every 3 correct answers in a row.", "Събирай 1 жокер за всеки 3 верни отговора поред.", "Sammle 1 Hinweis für je 3 richtige Antworten in Folge.")
    HintDifficulty.Medium -> tr(language, "Collect 1 hint for every 5 correct answers in a row.", "Събирай 1 жокер за всеки 5 верни отговора поред.", "Sammle 1 Hinweis für jeweils 5 richtige Antworten in Folge.")
    HintDifficulty.Hard -> tr(language, "Collect 1 hint for every 10 correct answers in a row.", "Събирай 1 жокер за всеки 10 верни отговора поред.", "Sammle 1 Hinweis für jeweils 10 richtige Antworten in Folge.")
    HintDifficulty.Impossible -> tr(language, "Collect 1 hint for every 25 correct answers in a row.", "Събирай 1 жокер за всеки 25 верни отговора поред.", "Sammle 1 Hinweis für jeweils 25 richtige Antworten in Folge.")
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
  tr(language, "Start a quiz", "Стартирай тест", "Starte ein Quiz")

internal fun localizedGameModesHubTitle(language: AppLanguage): String =
  tr(language, "Game modes", "Режими на игра", "Spielmodi")

internal fun localizedQuizTopicTitle(language: AppLanguage): String =
  tr(language, "Choose type of quiz", "Избери тип тест", "Quiz-Typ auswählen")

internal fun localizedQuizTopicLabel(
  topic: QuizTopic,
  language: AppLanguage,
): String =
  when (topic) {
    QuizTopic.Countries -> tr(language, "Countries", "Държави", "Länder")
    QuizTopic.Capitals -> tr(language, "Capitals", "Столици", "Hauptstädte")
    QuizTopic.Mixed -> tr(language, "Both", "И двете", "Beide")
  }

internal fun localizedSavedTestsTitle(
  language: AppLanguage,
  savedCount: Int,
  maxCount: Int = 10,
): String =
  tr(
    language,
    "Saved quizzes ($savedCount/$maxCount)",
    "Запазени тестове ($savedCount/$maxCount)",
    "Gespeicherte Vorlagen ($savedCount/$maxCount)",
  )

internal fun cleanHeroPill(
  index: Int,
  language: AppLanguage,
): String =
  when (index) {
    0 -> tr(language, "Countries", "Държави", "Länder")
    1 -> tr(language, "Flags", "Флагове", "Flaggen")
    else -> tr(language, "Capitals", "Столици", "Hauptstädte")
  }

internal fun cleanModeTitle(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> tr(language, "Training", "Тренировка", "Training")
    GameMode.CreateQuiz -> tr(language, "Create own quiz", "Създай собствен тест", "Quiz selber erstellen")
    GameMode.WorldFlags ->
      tr(language, "Country Flags", "Държавни флагове", "Länderflaggen")
    GameMode.DailyChallenge -> tr(language, "Daily challenge", "Дневно предизвикателство", "Tägliche Herausforderung")
    GameMode.MistakeReview -> tr(language, "Mistake review", "Преглед на грешките", "Fehlerprüfung")
    GameMode.LocalMultiplayer -> tr(language, "Local multiplayer", "Локална игра", "Lokaler Mehrspieler")
    else -> mode.title
  }

internal fun cleanModeShortLabel(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.Training -> tr(language, "Practice freely.", "Упражнявай се свободно.", "Frei üben.")
    GameMode.WorldFlags ->
      tr(language, "Guess the correct country or flag.", "Познай правилната държава или флаг.", "Errate das richtige Land oder die richtige Flagge.")
    GameMode.DailyChallenge ->
      tr(
        language,
        "Resets at ${localUtcMidnightResetLabel()}.",
        "Нулира се в ${localUtcMidnightResetLabel()}.",
        "Wird um ${localUtcMidnightResetLabel()} zurückgesetzt.",
      )
    GameMode.MistakeReview -> tr(language, "Fix mistakes.", "Поправи грешките.", "Fehler verbessern.")
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
    GameMode.CreateQuiz ->
      tr(language, "Build and save exact quizzes from preset flag filters or chosen countries.", "Създавай и запазвай точни тестове от филтри по флагове или избрани държави.", "Erstelle und speichere exakte Quizze mit Flaggenfiltern oder ausgewählten Ländern.")
    GameMode.WorldFlags ->
      tr(
        language,
        "Build a country or flag quiz with optional continents and timer.",
        "Създай тест по държави или флагове с избор на континенти и таймер по желание.",
        "Erstelle ein Länder- oder Flaggenquiz mit optionalen Kontinenten und Timer.",
      )
    GameMode.DailyChallenge ->
      tr(language, "One fixed quiz each local day.", "Един фиксиран тест за всеки местен ден.", "Ein festes Quiz pro lokalem Tag.")
    GameMode.MistakeReview ->
      tr(language, "Practice the countries you often miss.", "Упражнявай държавите, които често пропускаш.", "Übe die Länder, die du oft verpasst.")
    GameMode.LocalMultiplayer ->
      tr(language, "Up to 5 players pass one device and play turn by turn.", "До 5 играчи ползват едно устройство и играят поред.", "Bis zu 5 Spieler teilen sich ein Gerät und spielen reihum.")
    else -> mode.description
  }

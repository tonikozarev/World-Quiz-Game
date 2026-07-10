package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.MedalTier
import com.example.flaggameandroid.core.model.QuizSessionMode
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant

private fun tr(language: AppLanguage, english: String, bulgarian: String, german: String): String =
  when (language) {
    AppLanguage.English -> english
    AppLanguage.Bulgarian -> bulgarian
    AppLanguage.German -> german
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
        QuizTopic.Countries -> tr(language, "Pick the correct country.", "Избери вярната държава.", "Wähle das richtige Land.")
        QuizTopic.Capitals -> tr(language, "Pick the correct capital.", "Избери вярната столица.", "Wähle die richtige Hauptstadt.")
        QuizTopic.Mixed -> tr(language, "Pick the correct answer.", "Избери верния отговор.", "Wähle die richtige Antwort.")
      }
    QuizVariant.TextToFlag ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "Pick the correct flag.", "Избери верния флаг.", "Wähle die richtige Flagge.")
        QuizTopic.Capitals -> tr(language, "Pick the correct flag.", "Избери верния флаг.", "Wähle die richtige Flagge.")
        QuizTopic.Mixed -> tr(language, "Pick the correct flag.", "Избери верния флаг.", "Wähle die richtige Flagge.")
      }
    QuizVariant.TypeText ->
      when (topic) {
        QuizTopic.Countries -> tr(language, "Type the country.", "Напиши вярната държавата.", "Richtiges Land eingeben.")
        QuizTopic.Capitals -> tr(language, "Type the capital.", "Напиши вярната столицата.", "Richtige Hauptstadt eingeben.")
        QuizTopic.Mixed -> tr(language, "Type the correct answer.", "Напиши верния отговор.", "Richtige Antwort eingeben.")
      }
  }

internal fun localizedHintDifficultyTitle(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Easy -> tr(language, "Easy (Every 3-streak)", "Лесно (Всеки 3 поредни ✔)", "Einsteiger (Alle 3 in Folge)")
    HintDifficulty.Medium -> tr(language, "Medium (Every 5-streak)", "Средно (5 поредни ✔)", "Mittel (Alle 5 in Folge)")
    HintDifficulty.Hard -> tr(language, "Hard (Every 10-streak)", "Трудно (10 поредни ✔)", "Schwer (Alle 10 in Folge)")
    HintDifficulty.Impossible -> tr(language, "Impossible (Every 25-streak)", "Невъзможно (25 поредни ✔)", "Unmöglich (Alle 25 in Folge)")
  }

internal fun localizedHintDifficultyShortRule(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Easy -> tr(language, "Every 3-streak", "Всеки 3 верни поред", "Alle 3 in Folge")
    HintDifficulty.Medium -> tr(language, "Every 5-streak", "Всеки 5 верни поред", "Alle 5 in Folge")
    HintDifficulty.Hard -> tr(language, "Every 10-streak", "Всеки 10 верни поред", "Alle 10 in Folge")
    HintDifficulty.Impossible -> tr(language, "Every 25-streak", "Всеки 25 верни поред", "Alle 25 in Folge")
  }

internal fun localizedHintDifficultyDescription(
  difficulty: HintDifficulty,
  language: AppLanguage,
): String =
  when (difficulty) {
    HintDifficulty.Easy ->
      tr(
        language,
        "Collect 1 hint for every 3 correct answers in a row. On this difficulty, generated quizzes aim for about 45% flag-to-text questions, 45% text-to-flag questions, and 10% typed-answer questions.",
        "Събирай 1 жокер за всеки 3 верни отговора поред. При тази трудност генерираните тестове се стремят към около 45% въпроси флаг към текст, 45% въпроси текст към флаг и 10% въпроси с писане на отговор.",
        "Sammle 1 Hinweis für je 3 richtige Antworten in Folge. Bei diesem Schwierigkeitsgrad zielen die generierten Quizze auf ungefähr 45% Flagge-zu-Text-Fragen, 45% Text-zu-Flagge-Fragen und 10% Fragen mit Texteingabe ab.",
      )
    HintDifficulty.Medium ->
      tr(
        language,
        "Collect 1 hint for every 5 correct answers in a row. On this difficulty, generated quizzes aim for about 40% flag-to-text questions, 40% text-to-flag questions, and 20% typed-answer questions.",
        "Събирай 1 жокер за всеки 5 верни отговора поред. При тази трудност генерираните тестове се стремят към около 40% въпроси флаг към текст, 40% въпроси текст към флаг и 20% въпроси с писане на отговор.",
        "Sammle 1 Hinweis für jeweils 5 richtige Antworten in Folge. Bei diesem Schwierigkeitsgrad zielen die generierten Quizze auf ungefähr 40% Flagge-zu-Text-Fragen, 40% Text-zu-Flagge-Fragen und 20% Fragen mit Texteingabe ab.",
      )
    HintDifficulty.Hard ->
      tr(
        language,
        "Collect 1 hint for every 10 correct answers in a row. On this difficulty, generated quizzes aim for about 30% flag-to-text questions, 30% text-to-flag questions, and 40% typed-answer questions.",
        "Събирай 1 жокер за всеки 10 верни отговора поред. При тази трудност генерираните тестове се стремят към около 30% въпроси флаг към текст, 30% въпроси текст към флаг и 40% въпроси с писане на отговор.",
        "Sammle 1 Hinweis für jeweils 10 richtige Antworten in Folge. Bei diesem Schwierigkeitsgrad zielen die generierten Quizze auf ungefähr 30% Flagge-zu-Text-Fragen, 30% Text-zu-Flagge-Fragen und 40% Fragen mit Texteingabe ab.",
      )
    HintDifficulty.Impossible ->
      tr(
        language,
        "Collect 1 hint for every 25 correct answers in a row. On this difficulty, generated quizzes aim for about 20% flag-to-text questions, 20% text-to-flag questions, and 60% typed-answer questions.",
        "Събирай 1 жокер за всеки 25 верни отговора поред. При тази трудност генерираните тестове се стремят към около 20% въпроси флаг към текст, 20% въпроси текст към флаг и 60% въпроси с писане на отговор.",
        "Sammle 1 Hinweis für jeweils 25 richtige Antworten in Folge. Bei diesem Schwierigkeitsgrad zielen die generierten Quizze auf ungefähr 20% Flagge-zu-Text-Fragen, 20% Text-zu-Flagge-Fragen und 60% Fragen mit Texteingabe ab.",
      )
  }

internal fun localizedMedalIntro(language: AppLanguage): String =
  tr(language, "Perfect quiz counters", "Броячи за перфектни тестове", "Zähler für fehlerfreie Quizze")

internal fun cleanModeSelectionTitle(language: AppLanguage): String =
  tr(language, "Start a quiz", "Стартирай тест", "Starte ein Quiz")

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
    GameMode.CreateQuiz -> tr(language, "Create own quiz", "Създай собствен тест", "Quiz selber erstellen")
    GameMode.DailyChallenge -> tr(language, "Daily challenge", "Дневно предизвикателство", "Tägliche Herausforderung")
    GameMode.MistakeReview -> tr(language, "Mistake review", "Преглед на грешките", "Fehlerprüfung")
  }

internal fun cleanModeShortLabel(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.CreateQuiz ->
      tr(language, "Build your own quiz.", "Създай свой собствен тест.", "Erstelle dein eigenes Quiz.")
    GameMode.DailyChallenge ->
      tr(
        language,
        "Resets at ${localUtcMidnightResetLabel()}.",
        "Нулира се в ${localUtcMidnightResetLabel()}.",
        "Wird um ${localUtcMidnightResetLabel()} zurückgesetzt.",
      )
    GameMode.MistakeReview -> tr(language, "Fix mistakes.", "Поправи грешките.", "Fehler verbessern.")
  }

internal fun cleanModeDescription(
  mode: GameMode,
  language: AppLanguage,
): String =
  when (mode) {
    GameMode.CreateQuiz ->
      tr(language, "Build and save exact quizzes from preset flag filters or chosen countries.", "Създавай и запазвай точни тестове от филтри по флагове или избрани държави.", "Erstelle und speichere exakte Quizze mit Flaggenfiltern oder ausgewählten Ländern.")
    GameMode.DailyChallenge ->
      tr(language, "Only one fixed quiz each day.", "Точно един фиксиран тест за всеки ден.", "Genau ein festes Quiz pro Tag.")
    GameMode.MistakeReview ->
      tr(language, "Practice the countries you often get wrong.", "Упражнявай държавите, които често грешиш.", "Übe die Länder, die du oft falsch hast.")
  }
internal fun localizedSessionModeTitle(
  sessionMode: QuizSessionMode,
  language: AppLanguage,
): String =
  when (sessionMode) {
    QuizSessionMode.Standard -> tr(language, "Quiz", "Тест", "Quiz")
    QuizSessionMode.Training -> tr(language, "Training", "Тренировка", "Training")
    QuizSessionMode.LocalMultiplayer -> tr(language, "Local multiplayer", "Локална игра", "Lokaler Mehrspieler")
  }

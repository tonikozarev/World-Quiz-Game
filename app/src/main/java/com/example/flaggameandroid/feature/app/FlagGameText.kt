package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementSector
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.MedalTier
import com.example.flaggameandroid.core.model.QuizVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun cleanText(
  language: AppLanguage,
  text: UiText,
): String =
  when (text) {
    UiText.WorldFlagGame ->
      when (language) {
        AppLanguage.English -> "Global quiz"
        AppLanguage.Bulgarian -> "Глобален тест"
        AppLanguage.German -> "Weltquiz"
      }
    UiText.HeroSubtitle ->
      when (language) {
        AppLanguage.English -> "Practice flags, earn achievements, collect medals, and track your progress across every country."
        AppLanguage.Bulgarian -> "Практикувай флагове, печели постижения, събирай медали и следи напредъка си във всяка държава."
        AppLanguage.German -> "Übe Flaggen, sammle Erfolge und Medaillen und verfolge deinen Fortschritt."
      }
    UiText.Start ->
      when (language) {
        AppLanguage.English -> "Start"
        AppLanguage.Bulgarian -> "Старт"
        AppLanguage.German -> "Start"
      }
    UiText.Medals ->
      when (language) {
        AppLanguage.English -> "Medals"
        AppLanguage.Bulgarian -> "Медали"
        AppLanguage.German -> "Medaillen"
      }
    UiText.Achievements ->
      when (language) {
        AppLanguage.English -> "Achievements"
        AppLanguage.Bulgarian -> "Постижения"
        AppLanguage.German -> "Erfolge"
      }
    UiText.Favorites ->
      when (language) {
        AppLanguage.English -> "Saved"
        AppLanguage.Bulgarian -> "Запазени"
        AppLanguage.German -> "Gespeichert"
      }
    UiText.FavoriteCountriesFlags ->
      when (language) {
        AppLanguage.English -> "Favorite countries/flags"
        AppLanguage.Bulgarian -> "Любими държави/флагове"
        AppLanguage.German -> "Lieblingsländer/Flaggen"
      }
    UiText.Settings ->
      when (language) {
        AppLanguage.English -> "Settings"
        AppLanguage.Bulgarian -> "Настройки"
        AppLanguage.German -> "Einstellungen"
      }
    UiText.Quit ->
      when (language) {
        AppLanguage.English -> "Quit"
        AppLanguage.Bulgarian -> "Изход"
        AppLanguage.German -> "Beenden"
      }
    UiText.Open ->
      when (language) {
        AppLanguage.English -> "Open"
        AppLanguage.Bulgarian -> "Отвори"
        AppLanguage.German -> "Öffnen"
      }
    UiText.Level ->
      when (language) {
        AppLanguage.English -> "Level"
        AppLanguage.Bulgarian -> "Ниво"
        AppLanguage.German -> "Level"
      }
    UiText.Profile ->
      when (language) {
        AppLanguage.English -> "Profile"
        AppLanguage.Bulgarian -> "Профил"
        AppLanguage.German -> "Profil"
      }
    UiText.AccountName ->
      when (language) {
        AppLanguage.English -> "Account name"
        AppLanguage.Bulgarian -> "Име на акаунта"
        AppLanguage.German -> "Profilname"
      }
    UiText.ChooseProfileIcon ->
      when (language) {
        AppLanguage.English -> "Choose profile icon"
        AppLanguage.Bulgarian -> "Избери икона за профила"
        AppLanguage.German -> "Profilsymbol wählen"
      }
    UiText.Close ->
      when (language) {
        AppLanguage.English -> "Close"
        AppLanguage.Bulgarian -> "Затвори"
        AppLanguage.German -> "Schließen"
      }
    UiText.LeaveQuizTitle ->
      when (language) {
        AppLanguage.English -> "Leave quiz?"
        AppLanguage.Bulgarian -> "Да напуснеш ли теста?"
        AppLanguage.German -> "Quiz verlassen?"
      }
    UiText.LeaveQuizBody ->
      when (language) {
        AppLanguage.English -> "This quiz will not count toward results, newly earned hints, medals, achievements, or level progression."
        AppLanguage.Bulgarian -> "Този тест няма да се брои за резултатите, новите жокери, медали, постиженията или напредъка към ниво."
        AppLanguage.German -> "Dieses Quiz zählt nicht für Ergebnisse, neu verdiente Hinweise, Medaillen, Erfolge oder den Level-Fortschritt."
      }
    UiText.Leave ->
      when (language) {
        AppLanguage.English -> "Leave"
        AppLanguage.Bulgarian -> "Напусни"
        AppLanguage.German -> "Verlassen"
      }
    UiText.Stay ->
      when (language) {
        AppLanguage.English -> "Stay"
        AppLanguage.Bulgarian -> "Остани"
        AppLanguage.German -> "Bleiben"
      }
    UiText.Finish ->
      when (language) {
        AppLanguage.English -> "Finish"
        AppLanguage.Bulgarian -> "Приключи"
        AppLanguage.German -> "Fertig"
      }
    UiText.GuessTheFlag ->
      when (language) {
        AppLanguage.English -> "Guess the flag"
        AppLanguage.Bulgarian -> "Познай флага"
        AppLanguage.German -> "Errate die Flagge"
      }
    UiText.QuizInfo ->
      when (language) {
        AppLanguage.English -> "Score is revealed at the end. Newly earned hints and level progress count only after finishing the full quiz."
        AppLanguage.Bulgarian -> "Резултатът се показва накрая. Ново спечелените жокери и напредъкът към ниво се броят само след завършване на целия тест."
        AppLanguage.German -> "Die Punktzahl wird am Ende gezeigt. Neu verdiente Hinweise und Level-Fortschritt zählen erst nach dem vollständigen Quiz."
      }
    UiText.NextUp ->
      when (language) {
        AppLanguage.English -> "Next up"
        AppLanguage.Bulgarian -> "Следващ"
        AppLanguage.German -> "Als Nächstes"
      }
    UiText.Unanswered ->
      when (language) {
        AppLanguage.English -> "Unanswered"
        AppLanguage.Bulgarian -> "Неотговорени"
        AppLanguage.German -> "Unbeantwortet"
      }
    UiText.Skipped ->
      when (language) {
        AppLanguage.English -> "Skipped"
        AppLanguage.Bulgarian -> "Прескочени"
        AppLanguage.German -> "Übersprungen"
      }
    UiText.CorrectAnswers ->
      when (language) {
        AppLanguage.English -> "correct answers"
        AppLanguage.Bulgarian -> "верни отговори"
        AppLanguage.German -> "richtige Antworten"
      }
    UiText.CompletedTests ->
      when (language) {
        AppLanguage.English -> "completed tests"
        AppLanguage.Bulgarian -> "завършени тестове"
        AppLanguage.German -> "abgeschlossene Tests"
      }
    UiText.LevelUpTitle ->
      when (language) {
        AppLanguage.English -> "Level up!"
        AppLanguage.Bulgarian -> "Ниво нагоре!"
        AppLanguage.German -> "Levelaufstieg!"
      }
    UiText.LevelUpBody ->
      when (language) {
        AppLanguage.English -> "You reached level %1\$d and earned 5 free hints."
        AppLanguage.Bulgarian -> "Достигна ниво %1\$d и получи 5 безплатни жокера."
        AppLanguage.German -> "Du hast Level %1\$d erreicht und 5 kostenlose Hinweise erhalten."
      }
    else -> fallbackText(language, text)
  }

internal enum class UiText {
  ChooseMode,
  WorldFlagGame,
  HeroSubtitle,
  Menu,
  Start,
  Medals,
  Achievements,
  Favorites,
  FavoriteCountriesFlags,
  Settings,
  Quit,
  Profile,
  AccountName,
  ProfileIcon,
  ChangeIcon,
  ChooseProfileIcon,
  Save,
  Cancel,
  Close,
  Language,
  AppLanguage,
  Hints,
  CollectedHints,
  AddTenHints,
  ResetHints,
  SwitchToNormalIcon,
  SwitchToInactiveIcon,
  SendTestReminder,
  ResetAchievementsAndMedals,
  IconStatusInactive,
  IconStatusNormal,
  Player,
  Remove,
  AddPlayer,
  QuizBase,
  Continents,
  QuestionCount,
  AmountOfQuestions,
  ExampleQuestionCount,
  AllowedRange,
  PerfectRunNoMedal,
  UseCustomAmount,
  SurpriseMe,
  StartQuiz,
  LeaveQuizTitle,
  LeaveQuizBody,
  ExitAppTitle,
  ExitAppBody,
  Exit,
  Leave,
  Stay,
  QuizInfo,
  GuessTheFlag,
  CountryName,
  HintStartsWith,
  Hint,
  Skip,
  Unskip,
  Finish,
  Next,
  PlayAgain,
  QuizComplete,
  FinalResults,
  AnswerReview,
  CorrectAnswers,
  Skipped,
  Unanswered,
  Unsure,
  NetScore,
  HintPointsAvailable,
  QuestionReview,
  Correct,
  YourAnswer,
  NoAnswer,
  HintUsed,
  NoHintUsed,
  CompletedTests,
  Level,
  NextUp,
  NextLevelRequirements,
  LanguageLabel,
  Open,
  MedalsCountLabel,
  PerfectQuizCount,
  LevelUpTitle,
  LevelUpBody,
  QuestionPromptFlag,
  QuestionPromptCountry,
}

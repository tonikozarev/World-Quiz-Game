package com.example.flaggameandroid.feature.app

internal fun t(
  language: AppLanguage,
  text: UiText,
): String = cleanText(language, text)

internal fun fallbackText(
  language: AppLanguage,
  text: UiText,
): String =
  when (text) {
    UiText.Language ->
      when (language) {
        AppLanguage.English -> "Language"
        AppLanguage.Bulgarian -> "Език"
        AppLanguage.German -> "Sprache"
      }
    UiText.Hints ->
      when (language) {
        AppLanguage.English -> "Hints"
        AppLanguage.Bulgarian -> "Жокери"
        AppLanguage.German -> "Hinweise"
      }
    UiText.NoAnswer ->
      when (language) {
        AppLanguage.English -> "No answer"
        AppLanguage.Bulgarian -> "Няма отговор"
        AppLanguage.German -> "Keine Antwort"
      }
    UiText.Continents ->
      when (language) {
        AppLanguage.English -> "Continents"
        AppLanguage.Bulgarian -> "Континенти"
        AppLanguage.German -> "Kontinente"
      }
    UiText.ExitAppTitle ->
      when (language) {
        AppLanguage.English -> "Exit app?"
        AppLanguage.Bulgarian -> "Изход от приложението?"
        AppLanguage.German -> "App beenden?"
      }
    UiText.ExitAppBody ->
      when (language) {
        AppLanguage.English -> "Do you really want to leave the game?"
        AppLanguage.Bulgarian -> "Наистина ли искаш да излезеш от играта?"
        AppLanguage.German -> "Möchtest du das Spiel wirklich verlassen?"
      }
    UiText.Exit ->
      when (language) {
        AppLanguage.English -> "Exit"
        AppLanguage.Bulgarian -> "Изход"
        AppLanguage.German -> "Beenden"
      }
    UiText.NextLevelRequirements ->
      when (language) {
        AppLanguage.English -> "Next level requirements"
        AppLanguage.Bulgarian -> "Изисквания за следващо ниво"
        AppLanguage.German -> "Anforderungen für das nächste Level"
      }
    UiText.Save ->
      when (language) {
        AppLanguage.English -> "Save"
        AppLanguage.Bulgarian -> "Запази"
        AppLanguage.German -> "Speichern"
      }
    UiText.Cancel ->
      when (language) {
        AppLanguage.English -> "Cancel"
        AppLanguage.Bulgarian -> "Отказ"
        AppLanguage.German -> "Abbrechen"
      }
    UiText.ChangeIcon ->
      when (language) {
        AppLanguage.English -> "Change icon"
        AppLanguage.Bulgarian -> "Промени иконата"
        AppLanguage.German -> "Symbol ändern"
      }
    else -> text.name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
  }

package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.QuizTopic
import java.util.Locale

internal fun AppLanguage.toLocale(): Locale =
  when (this) {
    AppLanguage.English -> Locale.ENGLISH
    AppLanguage.Bulgarian -> Locale.forLanguageTag("bg")
    AppLanguage.German -> Locale.GERMAN
  }

internal fun FlagCountry.localizedName(language: AppLanguage): String {
  val displayName = Locale("", code).getDisplayCountry(language.toLocale()).trim()
  return displayName.ifBlank { name }
}

internal fun FlagCountry.acceptedTypedAnswers(
  language: AppLanguage,
  topic: QuizTopic = QuizTopic.Countries,
): List<String> {
  if (topic == QuizTopic.Capitals) {
    return listOfNotNull(capital?.trim()).distinct()
  }
  val localizedAliases = localizedCountryAliases[language].orEmpty()[code].orEmpty()
  return when (language) {
    AppLanguage.English -> listOf(name) + aliases + localizedAliases
    AppLanguage.Bulgarian,
    AppLanguage.German -> listOf(localizedName(language)) + localizedAliases
  }.distinct()
}

internal fun FlagCountry.localizedQuizText(
  language: AppLanguage,
  topic: QuizTopic,
): String =
  when (topic) {
    QuizTopic.Countries -> localizedName(language)
    QuizTopic.Capitals -> capital?.takeIf { it.isNotBlank() } ?: localizedName(language)
    QuizTopic.Mixed -> localizedName(language)
  }

internal fun localizedContinentName(
  continent: String,
  language: AppLanguage,
): String = localizedContinentNames[language].orEmpty()[continent] ?: continent

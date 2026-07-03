package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.QuizTopic
import java.text.Normalizer
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
    return listOfNotNull(capital?.trim(), localizedCapital(language)).distinct()
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
    QuizTopic.Capitals -> localizedCapital(language)
    QuizTopic.Mixed -> localizedName(language)
  }

internal fun localizedContinentName(
  continent: String,
  language: AppLanguage,
): String = localizedContinentNames[language].orEmpty()[continent] ?: continent

internal fun FlagCountry.localizedCapital(language: AppLanguage): String {
  val rawCapital = capital?.trim().orEmpty()
  if (rawCapital.isBlank()) return localizedName(language)

  localizedCapitalAliases[language].orEmpty()[rawCapital]?.let { return it }

  return when (language) {
    AppLanguage.English -> rawCapital
    AppLanguage.Bulgarian -> rawCapital.toBulgarianDisplayName()
    AppLanguage.German -> rawCapital
  }
}

private fun String.toBulgarianDisplayName(): String {
  val normalized = Normalizer.normalize(this, Normalizer.Form.NFD).replace("\\p{Mn}+".toRegex(), "")
  val lower = normalized.lowercase(Locale.ROOT)
  val result = buildString(lower.length * 2) {
    var index = 0
    while (index < lower.length) {
      val remaining = lower.substring(index)
      when {
        remaining.startsWith("shch") -> {
          append("щ")
          index += 4
        }
        remaining.startsWith("sch") -> {
          append("щ")
          index += 3
        }
        remaining.startsWith("zh") -> {
          append("ж")
          index += 2
        }
        remaining.startsWith("ch") -> {
          append("ч")
          index += 2
        }
        remaining.startsWith("sh") -> {
          append("ш")
          index += 2
        }
        remaining.startsWith("ts") -> {
          append("ц")
          index += 2
        }
        remaining.startsWith("yo") -> {
          append("йо")
          index += 2
        }
        remaining.startsWith("yu") -> {
          append("ю")
          index += 2
        }
        remaining.startsWith("ya") -> {
          append("я")
          index += 2
        }
        remaining.startsWith("kh") -> {
          append("х")
          index += 2
        }
        remaining.startsWith("ph") -> {
          append("ф")
          index += 2
        }
        remaining.startsWith("th") -> {
          append("т")
          index += 2
        }
        remaining.startsWith("qu") -> {
          append("ку")
          index += 2
        }
        remaining.startsWith("ck") -> {
          append("к")
          index += 2
        }
        else -> {
          val ch = lower[index]
          append(
            when (ch) {
              'a' -> "а"
              'b' -> "б"
              'c' -> "к"
              'd' -> "д"
              'e' -> "е"
              'f' -> "ф"
              'g' -> "г"
              'h' -> "х"
              'i' -> "и"
              'j' -> "й"
              'k' -> "к"
              'l' -> "л"
              'm' -> "м"
              'n' -> "н"
              'o' -> "о"
              'p' -> "п"
              'q' -> "к"
              'r' -> "р"
              's' -> "с"
              't' -> "т"
              'u' -> "у"
              'v' -> "в"
              'w' -> "в"
              'x' -> "кс"
              'y' -> "й"
              'z' -> "з"
              else -> ch.toString()
            },
          )
          index += 1
        }
      }
    }
  }
  return result
    .replace("\\b([a-zа-я])".toRegex()) { match ->
      match.value.replaceFirstChar { character ->
        character.titlecase(Locale.ROOT)
      }
    }
    .replace(" d. ", " Д. ")
    .replace(" d. ", " Д. ")
    .replace(" d.c.", " Д.К.")
    .replace(" d.c", " Д.К")
}

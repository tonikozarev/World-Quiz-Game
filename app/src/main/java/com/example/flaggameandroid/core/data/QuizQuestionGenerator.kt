package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizVariant
import java.text.Normalizer
import kotlin.random.Random

class QuizQuestionGenerator(
  private val random: Random = Random.Default,
) {
  fun buildQuestions(
    countries: List<FlagCountry>,
    config: QuizConfig,
  ): List<FlagQuestion> {
    val pool = countries.distinctBy { it.code }
    require(pool.size >= 4) { "Need at least 4 countries to build a quiz." }

    val targetCount =
      if (config.mode == GameMode.Training) {
        config.questionCount.coerceIn(1, 999)
      } else {
        config.questionCount.coerceIn(1, pool.size)
      }
    val variants = buildWeightedVariants(config, targetCount)
    val correctCountries =
      if (config.mode == GameMode.Training) {
        List(config.questionCount.coerceIn(1, 999)) { pool.random(random) }
      } else {
        pool.shuffled(random).take(targetCount)
      }

    return correctCountries.mapIndexed { index, correctCountry ->
      val variant = variants[index % variants.size]
      val wrongOptions =
        pool
          .filterNot { it.code == correctCountry.code }
          .shuffled(random)
          .take(3)

      FlagQuestion(
        correctCountry = correctCountry,
        options = (wrongOptions + correctCountry).shuffled(random),
        variant = variant,
      )
    }.shuffled(random)
  }

  private fun buildWeightedVariants(
    config: QuizConfig,
    targetCount: Int,
  ): List<QuizVariant> {
    val selectedVariants = config.variants.ifEmpty { QuizVariant.entries.toSet() }
    if (
      config.mode != GameMode.AllIn ||
      config.allInType != AllInType.NoBluffAllTough ||
      selectedVariants.size != QuizVariant.entries.size
    ) {
      return buildEvenVariants(selectedVariants, targetCount)
    }

    val weights = config.hintDifficulty.variantWeights.filterKeys { it in selectedVariants }
    val totalWeight = weights.values.sum().coerceAtLeast(1)
    val exactCounts = weights.mapValues { (_, weight) -> targetCount * weight.toDouble() / totalWeight }
    val baseCounts = exactCounts.mapValues { (_, exact) -> exact.toInt() }.toMutableMap()
    var remaining = targetCount - baseCounts.values.sum()

    exactCounts
      .entries
      .sortedWith(compareByDescending<Map.Entry<QuizVariant, Double>> { it.value - it.value.toInt() }.thenBy { it.key.ordinal })
      .forEach { entry ->
        if (remaining > 0) {
          baseCounts[entry.key] = baseCounts.getValue(entry.key) + 1
          remaining--
        }
      }

    return baseCounts
      .flatMap { (variant, count) -> List(count) { variant } }
      .ifEmpty { selectedVariants.toList() }
      .shuffled(random)
  }

  private fun buildEvenVariants(
    selectedVariants: Set<QuizVariant>,
    targetCount: Int,
  ): List<QuizVariant> {
    val orderedVariants = selectedVariants.toList().sortedBy { it.ordinal }
    val baseCount = targetCount / orderedVariants.size
    val remainder = targetCount % orderedVariants.size

    return orderedVariants
      .flatMapIndexed { index, variant ->
        List(baseCount + if (index < remainder) 1 else 0) { variant }
      }
      .ifEmpty { orderedVariants }
      .shuffled(random)
  }
}

object QuizAnswerChecker {
  fun isCountrySelectionCorrect(
    selectedCountry: FlagCountry?,
    correctCountry: FlagCountry,
  ): Boolean = selectedCountry?.code == correctCountry.code

  fun isTypedAnswerCorrect(
    typedAnswer: String,
    correctCountry: FlagCountry,
  ): Boolean {
    val normalizedAnswer = typedAnswer.normalizeAnswer()
    val acceptedAnswers = (listOf(correctCountry.name) + correctCountry.aliases).map { it.normalizeAnswer() }
    return normalizedAnswer in acceptedAnswers ||
      normalizedAnswer.compactAnswer() in acceptedAnswers.map { it.compactAnswer() } ||
      normalizedAnswer.looseCompactAnswer() in acceptedAnswers.map { it.looseCompactAnswer() }
  }

  fun String.normalizeAnswer(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
      .replace(Regex("\\p{Mn}+"), "")
      .lowercase()
      .replace("&", " and ")
      .replace(Regex("[^a-z0-9 ]"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()

  private fun String.compactAnswer(): String = replace(" ", "")

  private fun String.looseCompactAnswer(): String = compactAnswer().replace("and", "")
}

package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.FlagQuestion
import com.example.flaggameandroid.core.model.CountryPracticeStats
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizPoolSource
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import java.text.Normalizer
import kotlin.random.Random

class QuizQuestionGenerator(
  private val random: Random = Random.Default,
) {
  fun buildQuestions(
    countries: List<FlagCountry>,
    config: QuizConfig,
    practiceStats: Map<String, CountryPracticeStats> = emptyMap(),
    answerPool: List<FlagCountry> = countries,
  ): List<FlagQuestion> {
    val pool = countries.distinctBy { it.code }
    val optionPool = answerPool.distinctBy { it.code }
    require(optionPool.size >= 4) { "Need at least 4 countries to build a quiz." }
    val explicitSpecs = config.questionSpecs
    val configuredCount = if (explicitSpecs.isNotEmpty()) explicitSpecs.size else config.questionCount

    val maxQuestions =
      when {
        explicitSpecs.isNotEmpty() -> explicitSpecs.size
        config.mode == GameMode.CreateQuiz &&
          config.topic == QuizTopic.Mixed &&
          config.poolSource == com.example.flaggameandroid.core.model.QuizPoolSource.Standard ->
          pool.size * 2
        config.mode == GameMode.Training -> 999
        else -> pool.size
      }
    val targetCount =
      if (config.mode == GameMode.Training) {
        configuredCount.coerceIn(1, 999)
      } else {
        configuredCount.coerceIn(1, maxQuestions)
      }
    val variants = buildWeightedVariants(config, targetCount)
    val correctCountries =
      if (explicitSpecs.isNotEmpty()) {
        explicitSpecs.mapNotNull { spec -> pool.firstOrNull { it.code == spec.countryCode } ?: answerPool.firstOrNull { it.code == spec.countryCode } }
      } else if (config.mode == GameMode.Training) {
        buildTrainingCountries(pool, targetCount)
      } else if (config.poolSource == QuizPoolSource.MistakeReview) {
        buildUniqueReviewCountries(pool, targetCount)
      } else if (config.mode == GameMode.CreateQuiz && config.topic == QuizTopic.Mixed && targetCount > pool.size) {
        buildMixedCreateQuizCountries(pool, targetCount)
      } else {
        pickWeightedCountries(pool, targetCount, practiceStats, config.topic)
      }

    return correctCountries.mapIndexed { index, correctCountry ->
      val variant = variants[index % variants.size]
      val questionTopic =
        explicitSpecs.getOrNull(index)?.topic ?: resolveQuestionTopic(config.topic)
      val wrongOptions =
        optionPool
          .filterNot { it.code == correctCountry.code }
          .shuffled(random)
          .take(3)

      FlagQuestion(
        correctCountry = correctCountry,
        options = (wrongOptions + correctCountry).shuffled(random),
        variant = variant,
        topic = questionTopic,
      )
      }.shuffled(random)
  }

  private fun resolveQuestionTopic(topic: QuizTopic): QuizTopic =
    when (topic) {
      QuizTopic.Countries,
      QuizTopic.Capitals -> topic
      QuizTopic.Mixed -> if (random.nextBoolean()) QuizTopic.Countries else QuizTopic.Capitals
    }

  private fun buildTrainingCountries(
    pool: List<FlagCountry>,
    targetCount: Int,
  ): List<FlagCountry> =
    buildList {
      repeat(5) {
        addAll(pool.shuffled(random))
      }
      if (targetCount > size) {
        addAll(pool.shuffled(random).take(targetCount - size))
      }
    }.shuffled(random).take(targetCount)

  private fun pickWeightedCountries(
    pool: List<FlagCountry>,
    targetCount: Int,
    practiceStats: Map<String, CountryPracticeStats>,
    topic: QuizTopic,
  ): List<FlagCountry> {
    if (targetCount >= pool.size) {
      return pool.shuffled(random).take(targetCount)
    }

    val available = pool.toMutableList()
    val picked = mutableListOf<FlagCountry>()
    while (picked.size < targetCount && available.isNotEmpty()) {
      val weightedIndices =
        available.mapIndexed { index, country ->
          index to countrySelectionWeight(country, practiceStats, topic)
        }
      val totalWeight = weightedIndices.sumOf { it.second }.coerceAtLeast(1)
      var draw = random.nextInt(totalWeight)
      var selectedIndex = 0
      for ((index, weight) in weightedIndices) {
        draw -= weight
        if (draw < 0) {
          selectedIndex = index
          break
        }
      }
      picked += available.removeAt(selectedIndex)
    }
    return picked
  }

  private fun buildUniqueReviewCountries(
    pool: List<FlagCountry>,
    targetCount: Int,
  ): List<FlagCountry> {
    if (pool.isEmpty()) return emptyList()
    return pool.shuffled(random).take(minOf(targetCount, pool.size))
  }

  private fun buildMixedCreateQuizCountries(
    pool: List<FlagCountry>,
    targetCount: Int,
  ): List<FlagCountry> {
    if (pool.isEmpty()) return emptyList()
    val repeated = buildList {
      while (size < targetCount) {
        addAll(pool.shuffled(random))
      }
    }
    return repeated.take(targetCount)
  }

  private fun countrySelectionWeight(
    country: FlagCountry,
    practiceStats: Map<String, CountryPracticeStats>,
    topic: QuizTopic = QuizTopic.Countries,
  ): Int {
    val stats = practiceStats[country.code]
    var weight = 1
    if (stats?.favorite == true) weight += 3
    val weakForTopic = (stats?.wrongCountFor(topic) ?: 0) >= 2 && (stats?.wrongCountFor(topic) ?: 0) > (stats?.correctCountFor(topic) ?: 0)
    if (weakForTopic) weight += 4
    val topicWrongCount = stats?.wrongCountFor(topic) ?: 0
    if (topicWrongCount > 0) weight += minOf(3, topicWrongCount)
    return weight
  }

  private fun buildWeightedVariants(
    config: QuizConfig,
    targetCount: Int,
  ): List<QuizVariant> {
    val selectedVariants = config.variants.ifEmpty { QuizVariant.entries.toSet() }
    if (selectedVariants.size != QuizVariant.entries.size) {
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
    return isTypedAnswerCorrect(typedAnswer, listOf(correctCountry.name) + correctCountry.aliases)
  }

  fun isTypedAnswerCorrect(
    typedAnswer: String,
    acceptedAnswers: List<String>,
  ): Boolean {
    val normalizedAnswer = typedAnswer.normalizeAnswer()
    val normalizedAcceptedAnswers = acceptedAnswers.map { it.normalizeAnswer() }
    return normalizedAnswer in normalizedAcceptedAnswers ||
      normalizedAnswer.compactAnswer() in normalizedAcceptedAnswers.map { it.compactAnswer() } ||
      normalizedAnswer.looseCompactAnswer() in normalizedAcceptedAnswers.map { it.looseCompactAnswer() }
  }

  fun String.normalizeAnswer(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
      .replace(Regex("\\p{Mn}+"), "")
      .lowercase()
      .replace("&", " and ")
      .replace(Regex("[^\\p{L}\\p{Nd} ]"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()

  private fun String.compactAnswer(): String = replace(" ", "")

  private fun String.looseCompactAnswer(): String = compactAnswer().replace("and", "")
}

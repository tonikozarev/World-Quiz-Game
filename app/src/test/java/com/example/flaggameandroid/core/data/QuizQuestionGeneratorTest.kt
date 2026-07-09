package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.QuizConfig
import com.example.flaggameandroid.core.model.QuizSessionMode
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizQuestionGeneratorTest {
  private val repository = StaticFlagCatalogRepository()

  @Test
  fun catalog_hasFullCountrySetWithValidData() {
    val countries = repository.getCountries()

    assertTrue(countries.size >= 195)
    assertEquals(countries.size, countries.distinctBy { it.code }.size)
    assertTrue(countries.all { it.name.isNotBlank() })
    assertTrue(countries.all { it.emoji.isNotBlank() })
    assertTrue(countries.all { it.continent.isNotBlank() })
  }

  @Test
  fun catalog_splitsAmericasIntoNorthAndSouthAmerica() {
    val continents = repository.getCountries().map { it.continent }.toSet()

    assertTrue("North America" in continents)
    assertTrue("South America" in continents)
    assertTrue("Americas" !in continents)
  }

  @Test
  fun buildQuestions_respectsSelectedVariantsAndDoesNotRepeatCountries() {
    val generator = QuizQuestionGenerator(Random(7))
    val variants = setOf(QuizVariant.FlagToCountry, QuizVariant.TypeCountryName)

    val questions =
      generator.buildQuestions(
        countries = repository.getCountries(),
        config =
          QuizConfig(
            mode = GameMode.CreateQuiz,
            variants = variants,
            questionCount = 25,
          ),
      )

    assertEquals(25, questions.size)
    assertTrue(questions.all { it.variant in variants })
    assertEquals(25, questions.map { it.correctCountry.code }.distinct().size)
    questions.forEach { question ->
      assertEquals(4, question.options.distinctBy { it.code }.size)
      assertTrue(question.options.any { it.code == question.correctCountry.code })
    }
  }

  @Test
  fun buildQuestions_supportsMixedCreateQuizManualPoolsThatNeedDoubleQuestionCount() {
    val generator = QuizQuestionGenerator(Random(9))
    val manualCountries = repository.getCountries().filter { it.code in setOf("AT", "BG", "DE") }

    val questions =
      generator.buildQuestions(
        countries = manualCountries,
        config =
          QuizConfig(
            mode = GameMode.CreateQuiz,
            topic = QuizTopic.Mixed,
            variants = setOf(QuizVariant.FlagToCountry),
            questionCount = 6,
            questionSpecs =
              listOf(
                QuizQuestionSpec("AT", QuizTopic.Countries),
                QuizQuestionSpec("BG", QuizTopic.Countries),
                QuizQuestionSpec("DE", QuizTopic.Countries),
                QuizQuestionSpec("AT", QuizTopic.Capitals),
                QuizQuestionSpec("BG", QuizTopic.Capitals),
                QuizQuestionSpec("DE", QuizTopic.Capitals),
              ),
          ),
        answerPool = repository.getCountries(),
      )

    assertEquals(6, questions.size)
    assertEquals(3, questions.map { it.correctCountry.code }.distinct().size)
    assertTrue(questions.any { it.topic == QuizTopic.Countries })
    assertTrue(questions.any { it.topic == QuizTopic.Capitals })
  }

  @Test
  fun buildQuestions_usesEasyVariantWeights() {
    val counts = weightedVariantCountsFor(HintDifficulty.Easy)

    assertEquals(45, counts.getValue(QuizVariant.FlagToCountry))
    assertEquals(45, counts.getValue(QuizVariant.CountryToFlag))
    assertEquals(10, counts.getValue(QuizVariant.TypeCountryName))
  }

  @Test
  fun buildQuestions_usesMediumVariantWeights() {
    val counts = weightedVariantCountsFor(HintDifficulty.Medium)

    assertEquals(40, counts.getValue(QuizVariant.FlagToCountry))
    assertEquals(40, counts.getValue(QuizVariant.CountryToFlag))
    assertEquals(20, counts.getValue(QuizVariant.TypeCountryName))
  }

  @Test
  fun buildQuestions_usesHardVariantWeights() {
    val counts = weightedVariantCountsFor(HintDifficulty.Hard)

    assertEquals(30, counts.getValue(QuizVariant.FlagToCountry))
    assertEquals(30, counts.getValue(QuizVariant.CountryToFlag))
    assertEquals(40, counts.getValue(QuizVariant.TypeCountryName))
  }

  @Test
  fun buildQuestions_usesImpossibleVariantWeights() {
    val counts = weightedVariantCountsFor(HintDifficulty.Impossible)

    assertEquals(20, counts.getValue(QuizVariant.FlagToCountry))
    assertEquals(20, counts.getValue(QuizVariant.CountryToFlag))
    assertEquals(60, counts.getValue(QuizVariant.TypeCountryName))
  }

  @Test
  fun buildQuestions_rebalancesWeightsAcrossSelectedVariantsOnly() {
    val generator = QuizQuestionGenerator(Random(8))
    val questions =
      generator.buildQuestions(
        countries = repository.getCountries(),
        config =
          QuizConfig(
            mode = GameMode.CreateQuiz,
            sessionMode = QuizSessionMode.Training,
            variants = setOf(QuizVariant.FlagToCountry, QuizVariant.CountryToFlag),
            questionCount = 100,
            hintDifficulty = HintDifficulty.Easy,
          ),
      )
    val counts = questions.groupingBy { it.variant }.eachCount()

    assertEquals(50, counts.getValue(QuizVariant.FlagToCountry))
    assertEquals(50, counts.getValue(QuizVariant.CountryToFlag))
    assertTrue(QuizVariant.TypeCountryName !in counts)
  }

  @Test
  fun training_canRepeatCountriesUpToNineHundredNinetyNineQuestions() {
    val generator = QuizQuestionGenerator(Random(8))

    val questions =
      generator.buildQuestions(
        countries = repository.getCountries(),
        config =
          QuizConfig(
            mode = GameMode.CreateQuiz,
            sessionMode = QuizSessionMode.Training,
            variants = QuizVariant.entries.toSet(),
            questionCount = 300,
          ),
      )

    assertEquals(300, questions.size)
    assertTrue(questions.map { it.correctCountry.code }.distinct().size < questions.size)
  }

  @Test
  fun training_canRepeatCountriesEvenWhenQuestionCountIsWithinSinglePassPoolSize() {
    val generator = QuizQuestionGenerator(Random(8))

    val questions =
      generator.buildQuestions(
        countries = repository.getCountries(),
        config =
          QuizConfig(
            mode = GameMode.CreateQuiz,
            sessionMode = QuizSessionMode.Training,
            variants = QuizVariant.entries.toSet(),
            questionCount = 195,
          ),
      )

    assertEquals(195, questions.size)
    assertTrue(questions.map { it.correctCountry.code }.distinct().size < questions.size)
  }

  @Test
  fun worldFlags_usesHintDifficultyWeightsWhenAllThreeVariantsAreSelected() {
    val generator = QuizQuestionGenerator(Random(8))

    val counts =
      generator
        .buildQuestions(
          countries = repository.getCountries(),
          config =
            QuizConfig(
              mode = GameMode.CreateQuiz,
              variants = QuizVariant.entries.toSet(),
              questionCount = 100,
              hintDifficulty = HintDifficulty.Impossible,
            ),
        )
        .groupingBy { it.variant }
        .eachCount()

    assertEquals(20, counts.getValue(QuizVariant.FlagToCountry))
    assertEquals(20, counts.getValue(QuizVariant.CountryToFlag))
    assertEquals(60, counts.getValue(QuizVariant.TypeCountryName))
  }

  private fun weightedVariantCountsFor(difficulty: HintDifficulty): Map<QuizVariant, Int> {
    val generator = QuizQuestionGenerator(Random(8))
    return generator
      .buildQuestions(
        countries = repository.getCountries(),
        config =
          QuizConfig(
            mode = GameMode.CreateQuiz,
            variants = QuizVariant.entries.toSet(),
            questionCount = 100,
            hintDifficulty = difficulty,
          ),
      )
      .groupingBy { it.variant }
      .eachCount()
  }
}

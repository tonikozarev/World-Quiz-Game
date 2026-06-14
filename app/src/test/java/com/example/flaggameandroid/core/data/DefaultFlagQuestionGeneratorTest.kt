package com.example.flaggameandroid.core.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DefaultFlagQuestionGeneratorTest {
  private val generator = DefaultFlagQuestionGenerator()
  private val repository = StaticFlagCatalogRepository()

  @Test
  fun buildMultipleChoiceQuestions_returnsFiveQuestionsWithFourOptions() {
    val questions = generator.buildMultipleChoiceQuestions(repository.getCountries(), totalQuestions = 5)

    assertEquals(5, questions.size)
    questions.forEach { question ->
      assertEquals(4, question.options.size)
      assertTrue(question.options.any { it.code == question.flag.code })
      assertEquals(4, question.options.distinctBy { it.code }.size)
    }
  }
}

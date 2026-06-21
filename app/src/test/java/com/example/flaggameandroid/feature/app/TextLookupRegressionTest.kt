package com.example.flaggameandroid.feature.app

import org.junit.Assert.assertTrue
import org.junit.Test

class TextLookupRegressionTest {
  @Test
  fun allUiTextKeysResolveWithoutRecursion() {
    AppLanguage.entries.forEach { language ->
      UiText.entries.forEach { text ->
        val label = cleanText(language, text)

        assertTrue("${language.name}.${text.name} should not be blank", label.isNotBlank())
      }
    }
  }
}

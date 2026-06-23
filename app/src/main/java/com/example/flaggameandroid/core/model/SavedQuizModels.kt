package com.example.flaggameandroid.core.model

enum class CreateQuizSource(
  val title: String,
) {
  PresetFilter("Preset filter"),
  ManualCountries("Manual countries"),
}

enum class CreateQuizPreset(
  val title: String,
) {
  TwoColors("2 colors"),
  ThreeColors("3 colors"),
  FourPlusColors("4+ colors"),
  HorizontalStripes("Horizontal stripes"),
  VerticalStripes("Vertical stripes"),
  Stars("Stars"),
  Crosses("Crosses"),
  Animals("Animals"),
  Nato("NATO flags"),
  EuUnion("EU union flags"),
  WorldTradeOrganization("WTO flags"),
  CommonwealthOfNations("Commonwealth flags"),
  AfricanUnion("African Union flags"),
  OrganisationOfIslamicCooperation("OIC flags"),
  NoSymbols("No symbols"),
}

enum class SavedQuizDifficulty(
  val title: String,
) {
  Easy("Easy"),
  ItIsOk("It's OK"),
  Tough("Tough"),
}

data class SavedQuizTemplate(
  val id: String,
  val createdAtEpochMillis: Long,
  val title: String,
  val source: CreateQuizSource,
  val preset: CreateQuizPreset? = null,
  val selectedCountryCodes: Set<String> = emptySet(),
  val questionCountryCodes: Set<String> = emptySet(),
  val variants: Set<QuizVariant> = QuizVariant.entries.toSet(),
  val questionCount: Int = 10,
  val seed: Long = 0L,
  val completionCount: Int = 0,
  val difficulty: SavedQuizDifficulty = SavedQuizDifficulty.ItIsOk,
)

internal fun SavedQuizTemplate.hasSameQuizConfiguration(other: SavedQuizTemplate): Boolean =
  normalizedQuestionCountryCodes() == other.normalizedQuestionCountryCodes()

private fun SavedQuizTemplate.normalizedQuestionCountryCodes(): Set<String> =
  if (questionCountryCodes.isNotEmpty()) {
    questionCountryCodes
  } else {
    selectedCountryCodes
  }

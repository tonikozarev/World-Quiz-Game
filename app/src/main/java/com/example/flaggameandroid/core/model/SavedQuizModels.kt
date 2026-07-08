package com.example.flaggameandroid.core.model

enum class CreateQuizSource(
  val title: String,
) {
  PresetFilter("Preset filter"),
  ManualCountriesCapitals("Manual countries/capitals"),
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
  CapitalPopulationUnderQuarterMillion("Population < 250k"),
  CapitalPopulationQuarterToOneMillion("Population 250k-1M"),
  CapitalPopulationOneToTwoPointFiveMillion("Population 1M-2.5M"),
  CapitalPopulationOverTwoPointFiveMillion("Population > 2.5M"),
  CapitalAreaUnderFiftySquareKm("Area < 50 km²"),
  CapitalAreaFiftyToThreeHundredSquareKm("Area 50-300 km²"),
  CapitalAreaThreeHundredToEightHundredSquareKm("Area 300-800 km²"),
  CapitalAreaOverEightHundredSquareKm("Area > 800 km²"),
  CapitalNotCoastal("Not coastal"),
  Nato("NATO flags"),
  EuUnion("EU union flags"),
  WorldTradeOrganization("WTO flags"),
  CommonwealthOfNations("Commonwealth flags"),
  AfricanUnion("African Union flags"),
  OrganisationOfIslamicCooperation("OIC flags"),
}

data class SavedQuizTemplate(
  val id: String,
  val createdAtEpochMillis: Long,
  val title: String,
  val topic: QuizTopic = QuizTopic.Countries,
  val source: CreateQuizSource,
  val preset: CreateQuizPreset? = null,
  val selectedCountryCodes: Set<String> = emptySet(),
  val selectedCapitalCountryCodes: Set<String> = emptySet(),
  val questionCountryCodes: Set<String> = emptySet(),
  val variants: Set<QuizVariant> = QuizVariant.entries.toSet(),
  val questionCount: Int = 10,
  val seed: Long = 0L,
  val createQuizLocalMultiplayerEnabled: Boolean = false,
  val playerNames: List<String> = emptyList(),
  val completionCount: Int = 0,
)

internal fun SavedQuizTemplate.hasSameQuizConfiguration(other: SavedQuizTemplate): Boolean =
  if (topic == QuizTopic.Mixed || other.topic == QuizTopic.Mixed) {
    topic == other.topic &&
      selectedCountryCodes == other.selectedCountryCodes &&
      selectedCapitalCountryCodes == other.selectedCapitalCountryCodes
  } else {
    topic == other.topic &&
      normalizedQuestionCountryCodes() == other.normalizedQuestionCountryCodes()
  }

private fun SavedQuizTemplate.normalizedQuestionCountryCodes(): Set<String> =
  if (questionCountryCodes.isNotEmpty()) {
    questionCountryCodes
  } else {
    selectedCountryCodes
  }

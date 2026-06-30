package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant

internal fun FlagGameUiState.withUpdatedSetup(update: (SetupState) -> SetupState): FlagGameUiState =
  copy(setup = update(setup), setupError = null)

internal fun FlagGameUiState.withSelectedQuizTopic(topic: QuizTopic): FlagGameUiState =
  copy(selectedQuizTopic = topic, setupError = null)

internal fun FlagGameUiState.withSelectedVariantsToggled(variant: QuizVariant): FlagGameUiState {
  val current = setup.variants
  val next = if (variant in current) current - variant else current + variant
  return withUpdatedSetup { it.copy(variants = next) }
}

internal fun FlagGameUiState.withInstantCorrectionToggled(): FlagGameUiState =
  withUpdatedSetup {
    it.copy(instantCorrectionEnabled = !it.instantCorrectionEnabled)
  }

internal fun FlagGameUiState.withContinentToggled(
  continent: String,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val current = setup.selectedContinents
  val next = if (continent in current) current - continent else current + continent
  val nextSetup = setup.copy(selectedContinents = next)
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withQuestionCountInput(questionCount: String): FlagGameUiState =
  withUpdatedSetup {
    val digitsOnly = questionCount.filter { char -> char.isDigit() }
    val capped = digitsOnly.toIntOrNull()?.coerceAtMost(999)?.toString().orEmpty()
    it.copy(questionCountInput = capped, surpriseMe = false)
  }

internal fun FlagGameUiState.withCreateQuizSourceSelected(
  source: CreateQuizSource,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val resolvedSource =
    if (setup.topic == QuizTopic.Mixed && source == CreateQuizSource.PresetFilter) {
      CreateQuizSource.ManualCountriesCapitals
    } else {
      source
    }
  val nextSetup =
    setup.copy(
      createQuizSource = resolvedSource,
      selectedCountryCodes = if (resolvedSource == CreateQuizSource.ManualCountriesCapitals) setup.selectedCountryCodes else emptySet(),
      createQuizManualHardcoreEnabled = if (resolvedSource == CreateQuizSource.ManualCountriesCapitals) setup.createQuizManualHardcoreEnabled else false,
      createQuizPresets = setup.createQuizPresets.ifEmpty { createQuizDefaultPresetsForTopic(setup.topic) },
      createQuizSeed = 0L,
      questionCountInput =
        when (resolvedSource) {
          CreateQuizSource.PresetFilter -> setup.questionCountInput.ifBlank { "10" }
          CreateQuizSource.ManualCountriesCapitals -> setup.selectedCountryCodes.derivedCreateQuizQuestionCount(setup.topic).toString()
        },
      surpriseMe = if (resolvedSource == CreateQuizSource.ManualCountriesCapitals) false else setup.surpriseMe,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizPresetSelected(
  preset: CreateQuizPreset,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val currentPresets = setup.createQuizPresets.ifEmpty { createQuizDefaultPresetsForTopic(setup.topic) }
  val toggledPresets =
    if (preset in currentPresets) {
      currentPresets - preset
    } else {
      currentPresets + preset
    }
  val nextPresets = toggledPresets.ifEmpty { currentPresets }
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.PresetFilter,
      createQuizPreset = nextPresets.first(),
      createQuizPresets = nextPresets,
      createQuizSeed = 0L,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizCountryToggled(
  countryCode: String,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val current = setup.selectedCountryCodes
  val next = if (countryCode in current) current - countryCode else current + countryCode
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = next,
      createQuizSeed = 0L,
      questionCountInput = next.derivedCreateQuizQuestionCount(setup.topic).toString(),
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizContinentToggled(
  continent: String,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val continentCodes = countries.filter { it.continent == continent }.map { it.code }.toSet()
  if (continentCodes.isEmpty()) return this
  val continentSelected = continentCodes.all { it in setup.selectedCountryCodes }
  val nextSelection =
    if (continentSelected) {
      setup.selectedCountryCodes - continentCodes
    } else {
      setup.selectedCountryCodes + continentCodes
    }
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = nextSelection,
      createQuizSeed = 0L,
      questionCountInput = nextSelection.derivedCreateQuizQuestionCount(setup.topic).toString(),
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withSpeedRunSecondsPerAnswerInput(speedRunSeconds: String): FlagGameUiState =
  withUpdatedSetup {
    it.copy(speedRunSecondsPerAnswerInput = speedRunSeconds.filter { char -> char.isDigit() })
  }

internal fun FlagGameUiState.withCreateQuizManualHardcoreToggled(countries: List<FlagCountry>): FlagGameUiState {
  val enabled = !setup.createQuizManualHardcoreEnabled
  val nextSetup =
    setup.copy(
      createQuizManualHardcoreEnabled = enabled,
      createQuizTrainingEnabled = false,
      createQuizLocalMultiplayerEnabled = false,
      createQuizSeed = 0L,
      questionCountInput = if (enabled) countries.derivedCreateQuizQuestionCount(setup.topic).toString() else "1",
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizManualTimerEnabledToggled(): FlagGameUiState =
  withUpdatedSetup {
    it.copy(createQuizManualTimerEnabled = !it.createQuizManualTimerEnabled)
  }

internal fun FlagGameUiState.withCreateQuizTrainingToggled(countries: List<FlagCountry>): FlagGameUiState {
  val enabled = !setup.createQuizTrainingEnabled
  val nextSetup =
    setup.copy(
      createQuizTrainingEnabled = enabled,
      createQuizManualHardcoreEnabled = false,
      createQuizLocalMultiplayerEnabled = false,
      createQuizSeed = 0L,
      surpriseMe = false,
      questionCountInput = "1",
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizLocalMultiplayerToggled(countries: List<FlagCountry>): FlagGameUiState {
  val enabled = !setup.createQuizLocalMultiplayerEnabled
  val nextSetup =
    setup.copy(
      createQuizLocalMultiplayerEnabled = enabled,
      createQuizTrainingEnabled = false,
      createQuizManualHardcoreEnabled = false,
      createQuizSeed = 0L,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizAllCountriesToggled(countries: List<FlagCountry>): FlagGameUiState {
  val allCodes = countries.map { it.code }.toSet()
  val nextSelection = if (setup.selectedCountryCodes.size == allCodes.size) emptySet() else allCodes
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = nextSelection,
      createQuizSeed = 0L,
      questionCountInput = nextSelection.derivedCreateQuizQuestionCount(setup.topic).toString(),
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

private fun Set<String>.derivedCreateQuizQuestionCount(topic: QuizTopic): Int =
  if (topic == QuizTopic.Mixed) size * 2 else size

private fun List<FlagCountry>.derivedCreateQuizQuestionCount(topic: QuizTopic): Int =
  if (topic == QuizTopic.Mixed) size * 2 else size

internal fun FlagGameUiState.withSurpriseMeToggled(): FlagGameUiState {
  val surpriseMe = !setup.surpriseMe
  return withUpdatedSetup {
    it.copy(
      surpriseMe = surpriseMe,
      questionCountInput = if (surpriseMe) "" else it.questionCountInput,
    )
  }
}

internal fun FlagGameUiState.withAllInTypeSelected(allInType: AllInType): FlagGameUiState =
  withUpdatedSetup {
    val variants = it.variants.ifEmpty { QuizVariant.entries.toSet() }
    it.copy(allInType = allInType, variants = variants)
  }

internal fun FlagGameUiState.withMultiplayerBaseSelected(
  base: MultiplayerQuizBase,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val nextSetup = setup.copy(multiplayerBase = base)
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withPlayerNameUpdated(
  index: Int,
  name: String,
): FlagGameUiState {
  val names = setup.playerNames.toMutableList()
  if (index in names.indices) names[index] = name
  return withUpdatedSetup { it.copy(playerNames = names) }
}

internal fun FlagGameUiState.withPlayerAdded(): FlagGameUiState =
  if (setup.playerNames.size >= 5) {
    copy(setupError = "Local multiplayer supports up to 5 players.")
  } else {
    withUpdatedSetup {
      it.copy(playerNames = it.playerNames + "Player ${it.playerNames.size + 1}")
    }
  }

internal fun FlagGameUiState.withPlayerRemoved(): FlagGameUiState =
  if (setup.playerNames.size <= 2) {
    copy(setupError = "Local multiplayer needs at least 2 players.")
  } else {
    withUpdatedSetup { it.copy(playerNames = it.playerNames.dropLast(1)) }
  }

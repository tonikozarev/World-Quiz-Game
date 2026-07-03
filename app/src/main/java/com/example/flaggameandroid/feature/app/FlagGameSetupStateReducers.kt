package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.QuizVariant

internal fun FlagGameUiState.withUpdatedSetup(update: (SetupState) -> SetupState): FlagGameUiState =
  copy(setup = update(setup), setupError = null)

internal fun FlagGameUiState.withSelectedQuizTopic(
  topic: QuizTopic,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val nextSetup =
    when (setup.mode) {
      com.example.flaggameandroid.core.model.GameMode.CreateQuiz ->
        setup.copy(
          topic = topic,
          createQuizSource =
            if (topic == QuizTopic.Mixed) {
              CreateQuizSource.ManualCountriesCapitals
            } else {
              CreateQuizSource.PresetFilter
            },
          createQuizPreset =
            when (topic) {
              QuizTopic.Capitals -> CreateQuizPreset.CapitalPopulationUnderOneMillion
              QuizTopic.Countries,
              QuizTopic.Mixed -> CreateQuizPreset.TwoColors
            },
          createQuizPresets = createQuizDefaultPresetsForTopic(topic),
          selectedCountryCodes = emptySet(),
          selectedCapitalCountryCodes = emptySet(),
          createQuizManualHardcoreEnabled = false,
          createQuizSeed = 0L,
          questionCountInput = if (topic == QuizTopic.Mixed) "0" else "10",
          surpriseMe = false,
        )
      com.example.flaggameandroid.core.model.GameMode.MistakeReview ->
        setup.copy(topic = topic, questionCountInput = "10", surpriseMe = false)
      else -> setup
    }
  return copy(
    selectedQuizTopic = topic,
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

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
      selectedCapitalCountryCodes = if (resolvedSource == CreateQuizSource.ManualCountriesCapitals) setup.selectedCapitalCountryCodes else emptySet(),
      createQuizManualHardcoreEnabled = if (resolvedSource == CreateQuizSource.ManualCountriesCapitals) setup.createQuizManualHardcoreEnabled else false,
      createQuizPresets = setup.createQuizPresets.ifEmpty { createQuizDefaultPresetsForTopic(setup.topic) },
      createQuizSeed = 0L,
      questionCountInput =
        when (resolvedSource) {
          CreateQuizSource.PresetFilter -> setup.questionCountInput.ifBlank { "10" }
          CreateQuizSource.ManualCountriesCapitals -> setup.derivedCreateQuizQuestionCount().toString()
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
  val nextCountrySelection =
    if (countryCode in setup.selectedCountryCodes) {
      setup.selectedCountryCodes - countryCode
    } else {
      setup.selectedCountryCodes + countryCode
    }
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = nextCountrySelection,
      selectedCapitalCountryCodes = setup.selectedCapitalCountryCodes,
      createQuizSeed = 0L,
      questionCountInput =
        if (setup.topic == QuizTopic.Mixed) {
          setup.derivedCreateQuizQuestionCount(nextCountrySelection, setup.selectedCapitalCountryCodes).toString()
        } else {
          nextCountrySelection.derivedCreateQuizQuestionCount(setup.topic).toString()
        },
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizCountryBulkToggled(
  countryCodes: Set<String>,
  countries: List<FlagCountry>,
): FlagGameUiState {
  if (countryCodes.isEmpty()) return this
  val allSelected = countryCodes.all { it in setup.selectedCountryCodes }
  val nextCountrySelection =
    if (allSelected) {
      setup.selectedCountryCodes - countryCodes
    } else {
      setup.selectedCountryCodes + countryCodes
    }
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = nextCountrySelection,
      selectedCapitalCountryCodes = setup.selectedCapitalCountryCodes,
      createQuizSeed = 0L,
      questionCountInput =
        if (setup.topic == QuizTopic.Mixed) {
          setup.derivedCreateQuizQuestionCount(nextCountrySelection, setup.selectedCapitalCountryCodes).toString()
        } else {
          nextCountrySelection.derivedCreateQuizQuestionCount(setup.topic).toString()
        },
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizCapitalToggled(
  countryCode: String,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val nextCapitalSelection =
    if (countryCode in setup.selectedCapitalCountryCodes) {
      setup.selectedCapitalCountryCodes - countryCode
    } else {
      setup.selectedCapitalCountryCodes + countryCode
    }
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = setup.selectedCountryCodes,
      selectedCapitalCountryCodes = nextCapitalSelection,
      createQuizSeed = 0L,
      questionCountInput = setup.derivedCreateQuizQuestionCount(setup.selectedCountryCodes, nextCapitalSelection).toString(),
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withCreateQuizCapitalBulkToggled(
  countryCodes: Set<String>,
  countries: List<FlagCountry>,
): FlagGameUiState {
  if (countryCodes.isEmpty()) return this
  val allSelected = countryCodes.all { it in setup.selectedCapitalCountryCodes }
  val nextCapitalSelection =
    if (allSelected) {
      setup.selectedCapitalCountryCodes - countryCodes
    } else {
      setup.selectedCapitalCountryCodes + countryCodes
    }
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = setup.selectedCountryCodes,
      selectedCapitalCountryCodes = nextCapitalSelection,
      createQuizSeed = 0L,
      questionCountInput = setup.derivedCreateQuizQuestionCount(setup.selectedCountryCodes, nextCapitalSelection).toString(),
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
  val continentSelectedCountries = continentCodes.all { it in setup.selectedCountryCodes }
  val continentSelectedCapitals = continentCodes.all { it in setup.selectedCapitalCountryCodes }
  val nextCountrySelection =
    if (continentSelectedCountries) {
      setup.selectedCountryCodes - continentCodes
    } else {
      setup.selectedCountryCodes + continentCodes
    }
  val nextCapitalSelection =
    if (setup.topic == QuizTopic.Mixed) {
      if (continentSelectedCapitals) {
        setup.selectedCapitalCountryCodes - continentCodes
      } else {
        setup.selectedCapitalCountryCodes + continentCodes
      }
    } else {
      setup.selectedCapitalCountryCodes
    }
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = nextCountrySelection,
      selectedCapitalCountryCodes = nextCapitalSelection,
      createQuizSeed = 0L,
      questionCountInput = if (setup.topic == QuizTopic.Mixed) setup.derivedCreateQuizQuestionCount(nextCountrySelection, nextCapitalSelection).toString() else nextCountrySelection.derivedCreateQuizQuestionCount(setup.topic).toString(),
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
  val nextCapitalSelection = if (setup.topic == QuizTopic.Mixed && setup.selectedCapitalCountryCodes.size == allCodes.size) emptySet() else if (setup.topic == QuizTopic.Mixed) allCodes else emptySet()
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountriesCapitals,
      selectedCountryCodes = nextSelection,
      selectedCapitalCountryCodes = nextCapitalSelection,
      createQuizSeed = 0L,
      questionCountInput = if (setup.topic == QuizTopic.Mixed) setup.derivedCreateQuizQuestionCount(nextSelection, nextCapitalSelection).toString() else nextSelection.derivedCreateQuizQuestionCount(setup.topic).toString(),
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

private fun SetupState.derivedCreateQuizQuestionCount(): Int =
  derivedCreateQuizQuestionCount(selectedCountryCodes, selectedCapitalCountryCodes)

private fun SetupState.derivedCreateQuizQuestionCount(
  selectedCountries: Set<String>,
  selectedCapitals: Set<String>,
): Int =
  if (topic == QuizTopic.Mixed) {
    selectedCountries.size + selectedCapitals.size
  } else {
    selectedCountries.size
  }

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

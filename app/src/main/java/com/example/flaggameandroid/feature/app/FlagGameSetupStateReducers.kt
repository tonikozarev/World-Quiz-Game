package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.QuizVariant

internal fun FlagGameUiState.withUpdatedSetup(update: (SetupState) -> SetupState): FlagGameUiState =
  copy(setup = update(setup), setupError = null)

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
    it.copy(questionCountInput = questionCount.filter { char -> char.isDigit() }, surpriseMe = false)
  }

internal fun FlagGameUiState.withCreateQuizSourceSelected(
  source: CreateQuizSource,
  countries: List<FlagCountry>,
): FlagGameUiState {
  val nextSetup =
    setup.copy(
      createQuizSource = source,
      selectedCountryCodes = if (source == CreateQuizSource.ManualCountries) setup.selectedCountryCodes else emptySet(),
      createQuizManualHardcoreEnabled = if (source == CreateQuizSource.ManualCountries) setup.createQuizManualHardcoreEnabled else false,
      createQuizSeed = 0L,
      questionCountInput =
        when (source) {
          CreateQuizSource.PresetFilter -> setup.questionCountInput.ifBlank { "10" }
          CreateQuizSource.ManualCountries -> setup.selectedCountryCodes.size.toString()
        },
      surpriseMe = if (source == CreateQuizSource.ManualCountries) false else setup.surpriseMe,
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
  val nextSetup = setup.copy(createQuizSource = CreateQuizSource.PresetFilter, createQuizPreset = preset)
  return copy(
    setup = nextSetup.copy(createQuizSeed = 0L),
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
      createQuizSource = CreateQuizSource.ManualCountries,
      selectedCountryCodes = next,
      createQuizSeed = 0L,
      questionCountInput = next.size.toString(),
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
      selectedCountryCodes = if (enabled) countries.map { it.code }.toSet() else setup.selectedCountryCodes,
      createQuizSeed = 0L,
      questionCountInput = if (enabled) countries.size.toString() else setup.selectedCountryCodes.size.toString(),
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

internal fun FlagGameUiState.withCreateQuizAllCountriesToggled(countries: List<FlagCountry>): FlagGameUiState {
  val allCodes = countries.map { it.code }.toSet()
  val nextSelection = if (setup.selectedCountryCodes.size == allCodes.size) emptySet() else allCodes
  val nextSetup =
    setup.copy(
      createQuizSource = CreateQuizSource.ManualCountries,
      selectedCountryCodes = nextSelection,
      createQuizSeed = 0L,
      questionCountInput = nextSelection.size.toString(),
      surpriseMe = false,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withWorldFlagsHardcoreToggled(countries: List<FlagCountry>): FlagGameUiState {
  val enabled = !setup.worldFlagsHardcoreEnabled
  val nextSetup =
    setup.copy(
      worldFlagsHardcoreEnabled = enabled,
      variants = if (enabled) QuizVariant.entries.toSet() else setup.variants,
    )
  return copy(
    setup = nextSetup,
    questionCountLimit = questionLimitFor(nextSetup, countries),
    setupError = null,
  )
}

internal fun FlagGameUiState.withWorldFlagsTimerEnabledToggled(): FlagGameUiState =
  withUpdatedSetup {
    it.copy(worldFlagsTimerEnabled = !it.worldFlagsTimerEnabled)
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

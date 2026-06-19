package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.data.QuizQuestionGenerator
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.PlayerProgress
import kotlin.random.Random

internal data class QuizStartResult(
  val quiz: QuizState? = null,
  val validationError: String? = null,
)

internal fun buildStartedQuizState(
  setup: SetupState,
  countries: List<FlagCountry>,
  questionGenerator: QuizQuestionGenerator,
  hintDifficulty: HintDifficulty,
  random: Random,
  hintCount: Int,
  displayName: String,
): QuizState {
  val pool = countryPoolFor(setup, countries)
  val config = configFor(setup, pool.size, hintDifficulty, random)
  val questions = questionGenerator.buildQuestions(pool, config)
  val players = config.players.map { PlayerProgress(name = it, hintPoints = hintCount) }
  val questionStates = List(questions.size) { QuestionDraftState() }

  return QuizState(
    mode = setup.mode,
    allInType = setup.allInType,
    variants = config.variants,
    selectedContinents = setup.selectedContinents,
    questions = questions,
    questionStates = questionStates,
    players = players,
    startedAtEpochMillis = System.currentTimeMillis(),
  ).loadQuestionDraft(0)
}

internal fun buildQuizStartResult(
  setup: SetupState,
  countries: List<FlagCountry>,
  questionGenerator: QuizQuestionGenerator,
  hintDifficulty: HintDifficulty,
  random: Random,
  hintCount: Int,
  displayName: String,
): QuizStartResult {
  val validationError = validateSetup(setup) { countryPoolFor(it, countries) }
  if (validationError != null) {
    return QuizStartResult(validationError = validationError)
  }

  return QuizStartResult(
    quiz =
      buildStartedQuizState(
        setup = setup,
        countries = countries,
        questionGenerator = questionGenerator,
        hintDifficulty = hintDifficulty,
        random = random,
        hintCount = hintCount,
        displayName = displayName,
      ),
  )
}

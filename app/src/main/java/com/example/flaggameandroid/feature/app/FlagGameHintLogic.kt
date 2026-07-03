package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizVariant

internal data class HintApplicationResult(
  val quiz: QuizState,
  val hintCount: Double,
  val speedRunPenaltySeconds: Int = 0,
)

internal fun applyHintToCurrentQuestion(
  state: FlagGameUiState,
): HintApplicationResult? {
  val quiz = state.quiz
  val question = quiz.currentQuestion ?: return null
  val currentDraft = quiz.currentQuestionState
  if (question.variant != QuizVariant.TypeText && currentDraft.locked) return null
  val hintCost =
    when (currentDraft.hintUses) {
      0,
      1 -> 0.75
      2 -> 0.5
      else -> 0.5
    }
  if (currentDraft.hintUses >= 3 || quiz.currentPlayer.hintPoints < hintCost) return null

  val players = quiz.players.toMutableList()
  val newHintCount = quiz.currentPlayer.hintPoints - hintCost
  players.replaceAll { it.copy(hintPoints = newHintCount) }

  val isTypedQuestion = question.variant == QuizVariant.TypeText
  val nextHintUses = currentDraft.hintUses + 1
  val speedRunPenaltySeconds =
    if (quiz.countdownEnabled) {
      nextHintUses
    } else {
      0
    }
  val fullAnswerText = question.correctCountry.localizedQuizText(state.settings.language, question.topic).trim()
  val hiddenWrongCodes = question.options.filterNot { it.code == question.correctCountry.code }.map { it.code }
  val hiddenCodes =
    when (nextHintUses) {
      1 -> currentDraft.hiddenOptionCodes
      2 -> {
        if (isTypedQuestion) {
          currentDraft.hiddenOptionCodes
        } else {
          currentDraft.hiddenOptionCodes + hiddenWrongCodes.take(2)
        }
      }
      else ->
        if (isTypedQuestion) {
          currentDraft.hiddenOptionCodes
        } else {
          hiddenWrongCodes.toSet()
        }
    }.toSet()

  val updatedQuestionState =
    currentDraft.copy(
      hiddenOptionCodes = hiddenCodes,
      typedHintPrefix =
        when {
          !isTypedQuestion -> currentDraft.typedHintPrefix
          nextHintUses == 1 -> currentDraft.typedHintPrefix
          nextHintUses == 2 -> fullAnswerText.take(3)
          else -> null
        },
      typedAnswer =
        when {
          !isTypedQuestion -> currentDraft.typedAnswer
          nextHintUses == 3 -> fullAnswerText
          else -> currentDraft.typedAnswer
        },
      selectedCountry =
        when {
          isTypedQuestion -> currentDraft.selectedCountry
          nextHintUses >= 3 -> question.correctCountry
          else -> currentDraft.selectedCountry
        },
      status = currentDraft.status,
      hintUses = nextHintUses.coerceAtMost(3),
      hintUsed = true,
      revealed = nextHintUses >= 3,
    )

  return HintApplicationResult(
    quiz =
      quiz.copy(
        players = players,
        questionStates = quiz.questionStates.replaceAt(quiz.currentQuestionIndex, updatedQuestionState),
        selectedCountry = updatedQuestionState.selectedCountry,
        typedAnswer = updatedQuestionState.typedAnswer,
      hiddenOptionCodes = updatedQuestionState.hiddenOptionCodes,
      typedHintPrefix = updatedQuestionState.typedHintPrefix,
      hintUsedOnCurrentQuestion = updatedQuestionState.hintUses > 0,
      speedRunPenaltySeconds = quiz.speedRunPenaltySeconds + speedRunPenaltySeconds,
    ),
    hintCount = newHintCount,
    speedRunPenaltySeconds = speedRunPenaltySeconds,
  )
}

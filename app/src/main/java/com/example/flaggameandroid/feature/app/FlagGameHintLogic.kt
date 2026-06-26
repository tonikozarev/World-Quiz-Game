package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizVariant

internal data class HintApplicationResult(
  val quiz: QuizState,
  val hintCount: Int,
  val speedRunPenaltySeconds: Int = 0,
)

internal fun applyHintToCurrentQuestion(
  state: FlagGameUiState,
): HintApplicationResult? {
  val quiz = state.quiz
  val question = quiz.currentQuestion ?: return null
  val currentDraft = quiz.currentQuestionState
  if (currentDraft.hintUses >= 2 || quiz.currentPlayer.hintPoints < 1) return null

  val players = quiz.players.toMutableList()
  val newHintCount = quiz.currentPlayer.hintPoints - 1
  players.replaceAll { it.copy(hintPoints = newHintCount) }

  val isTypedQuestion = question.variant == QuizVariant.TypeCountryName
  val firstHint = currentDraft.hintUses == 0
  val speedRunPenaltySeconds =
    if (quiz.countdownEnabled) {
      if (firstHint) 1 else 2
    } else {
      0
    }
  val fullCountryName = question.correctCountry.localizedName(state.settings.language)
  val hiddenCodes =
    when {
      isTypedQuestion -> currentDraft.hiddenOptionCodes
      firstHint ->
        currentDraft.hiddenOptionCodes +
          question.options
            .filterNot { it.code == question.correctCountry.code }
            .filterNot { it.code in currentDraft.hiddenOptionCodes }
            .take(2)
            .map { it.code }
      else ->
        question.options
          .filterNot { it.code == question.correctCountry.code }
          .map { it.code }
    }.toSet()

  val updatedQuestionState =
    currentDraft.copy(
      hiddenOptionCodes = hiddenCodes,
      typedHintPrefix =
        when {
          !isTypedQuestion -> currentDraft.typedHintPrefix
          firstHint -> fullCountryName.take(3)
          else -> fullCountryName
        },
      typedAnswer =
        when {
          !isTypedQuestion -> currentDraft.typedAnswer
          firstHint -> currentDraft.typedAnswer
          else -> fullCountryName
        },
      selectedCountry =
        when {
          isTypedQuestion -> currentDraft.selectedCountry
          firstHint -> currentDraft.selectedCountry
          else -> question.correctCountry
        },
      status =
        when {
          firstHint -> currentDraft.status
          else -> QuestionStatus.Answered
        },
      hintUses = (currentDraft.hintUses + 1).coerceAtMost(2),
      hintUsed = true,
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

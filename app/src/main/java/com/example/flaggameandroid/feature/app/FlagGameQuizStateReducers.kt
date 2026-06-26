package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.QuizVariant

internal fun QuizState.withSelectedCountry(country: FlagCountry): QuizState {
  if (currentQuestion == null) return this
  if (instantCorrectionEnabled && currentQuestionState.locked) return this

  if (currentQuestionState.selectedCountry?.code == country.code) {
    if (instantCorrectionEnabled) return this
    val clearedDraft =
      currentQuestionState.copy(
        status = QuestionStatus.Unanswered,
        selectedCountry = null,
      )
    return copy(
      questionStates = questionStates.replaceAt(currentQuestionIndex, clearedDraft),
      selectedCountry = null,
    )
  }

  val updatedDraft =
    currentQuestionState.copy(
      status = QuestionStatus.Answered,
      selectedCountry = country,
      locked = instantCorrectionEnabled,
    )

  return copy(
    questionStates = questionStates.replaceAt(currentQuestionIndex, updatedDraft),
    selectedCountry = updatedDraft.selectedCountry,
  )
}

internal fun QuizState.withTypedAnswer(answer: String): QuizState {
  if (currentQuestion == null) return this
  if (instantCorrectionEnabled && currentQuestionState.locked) return this

  val updatedDraft =
    currentQuestionState.copy(
      typedAnswer = answer,
    )

  return copy(
    questionStates = questionStates.replaceAt(currentQuestionIndex, updatedDraft),
    typedAnswer = updatedDraft.typedAnswer,
  )
}

internal fun QuizState.withVerifiedTypedAnswer(): QuizState {
  val question = currentQuestion ?: return this
  val draft = currentQuestionState
  if (question.variant != QuizVariant.TypeCountryName) return this
  if (draft.typedAnswer.isBlank() || draft.locked) return this

  val updatedDraft =
    draft.copy(
      status = QuestionStatus.Answered,
      locked = instantCorrectionEnabled,
    )

  return copy(
    questionStates = questionStates.replaceAt(currentQuestionIndex, updatedDraft),
  )
}

internal fun QuizState.withPreviousQuestionLoaded(): QuizState {
  val targetIndex = (currentQuestionIndex - 1).takeIf { it >= 0 } ?: return this
  return loadQuestionDraft(targetIndex)
}

internal fun QuizState.withPreviewAdvancedQuestion(): QuizState {
  val updatedQuestionStates = previewAdvanceQuestionStates()
  val targetIndex = previewAdvanceTargetIndex()
  return copy(questionStates = updatedQuestionStates).loadQuestionDraft(targetIndex ?: currentQuestionIndex)
}

internal fun QuizState.withNextSkippedQuestionLoaded(): QuizState {
  val committedCurrent = withCurrentQuestionSubmitted() ?: this
  val targetIndex = committedCurrent.nextSkippedQuestionIndex() ?: committedCurrent.nextUnansweredQuestionIndex() ?: return committedCurrent
  return committedCurrent.loadQuestionDraft(targetIndex)
}

internal fun QuizState.withCurrentQuestionSubmitted(): QuizState? {
  val draft = currentQuestionState
  if (!currentQuestionHasPendingAnswer) return null

  val updatedDraft =
    draft.copy(
      status = QuestionStatus.Answered,
      locked = draft.locked || instantCorrectionEnabled,
    )

  return copy(
    questionStates = questionStates.replaceAt(currentQuestionIndex, updatedDraft),
    selectedCountry = updatedDraft.selectedCountry,
    typedAnswer = updatedDraft.typedAnswer,
  )
}

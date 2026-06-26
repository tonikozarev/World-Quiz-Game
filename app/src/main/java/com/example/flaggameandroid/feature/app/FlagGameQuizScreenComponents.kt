package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun QuizNavigationRow(
  onPreviousQuestion: () -> Unit,
  canGoBack: Boolean,
  questionContent: @Composable () -> Unit,
  onNextQuestionPreview: () -> Unit,
  canGoForward: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    OutlinedButton(
      onClick = onPreviousQuestion,
      enabled = canGoBack,
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
    ) {
      Text("←")
    }
    questionContent()
    OutlinedButton(
      onClick = onNextQuestionPreview,
      enabled = canGoForward,
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
    ) {
      Text("→")
    }
  }
}

@Composable
internal fun QuizActionButtonsRow(
  infoLabel: String,
  showInfo: Boolean,
  onInfoClick: () -> Unit,
  showHintButton: Boolean,
  hintLabel: String,
  canUseHint: Boolean,
  onUseHint: () -> Unit,
  unskipLabel: String,
  canUnskip: Boolean,
  onUnskipQuestion: () -> Unit,
  verifyLabel: String?,
  canVerify: Boolean,
  onVerifyTypedAnswer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
    InfoButton(
      label = infoLabel,
      modifier = Modifier.weight(1f),
      onClick = onInfoClick,
    )
    if (showHintButton) {
      OutlinedButton(
        onClick = onUseHint,
        enabled = canUseHint,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
      ) {
        Text(
          text = hintLabel,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          softWrap = false,
        )
      }
    }
    OutlinedButton(
      onClick = onUnskipQuestion,
      enabled = canUnskip,
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
    ) {
      Text(
        text = unskipLabel,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        softWrap = false,
      )
    }
    if (verifyLabel != null) {
      OutlinedButton(
        onClick = onVerifyTypedAnswer,
        enabled = canVerify,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
      ) {
        Text(
          text = verifyLabel,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          softWrap = false,
        )
      }
    }
  }
}

package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.theme.AccentRed

@OptIn(ExperimentalLayoutApi::class)
@androidx.compose.runtime.Composable
fun SetupScreen(
  setup: SetupState,
  hintDifficulty: HintDifficulty,
  language: AppLanguage,
  availableContinents: List<String>,
  questionCountLimit: Int,
  setupError: String?,
  onBack: () -> Unit,
  onVariantToggle: (QuizVariant) -> Unit,
  onContinentToggle: (String) -> Unit,
  onQuestionCountChange: (String) -> Unit,
  onSurpriseMe: () -> Unit,
  onAllInTypeSelected: (AllInType) -> Unit,
  onMultiplayerBaseSelected: (MultiplayerQuizBase) -> Unit,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
  onStartQuiz: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ScreenShell(modifier = modifier) {
    HeaderRow(title = cleanModeTitle(setup.mode, language))

    if (setup.mode == GameMode.LocalMultiplayer) {
      SectionCard(title = when (language) {
        AppLanguage.English -> "Players"
        AppLanguage.Bulgarian -> "Играчите"
        AppLanguage.German -> "Spieler"
      }) {
        setup.playerNames.forEachIndexed { index, name ->
          OutlinedTextField(
            value = name,
            onValueChange = { onPlayerNameChanged(index, it) },
            label = {
              Text(
                when (language) {
                  AppLanguage.English -> "Player ${index + 1}"
                  AppLanguage.Bulgarian -> "Играч ${index + 1}"
                  AppLanguage.German -> "Spieler ${index + 1}"
                },
              )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onRemovePlayer, modifier = Modifier.weight(1f)) {
            Text(
              when (language) {
                AppLanguage.English -> "Remove"
                AppLanguage.Bulgarian -> "Премахни"
                AppLanguage.German -> "Entfernen"
              },
            )
          }
          Button(onClick = onAddPlayer, modifier = Modifier.weight(1f)) {
            Text(
              when (language) {
                AppLanguage.English -> "Add player"
                AppLanguage.Bulgarian -> "Добави играч"
                AppLanguage.German -> "Spieler hinzufügen"
              },
            )
          }
        }
      }

      SectionCard(title = when (language) {
        AppLanguage.English -> "Quiz base"
        AppLanguage.Bulgarian -> "База за теста"
        AppLanguage.German -> "Quiz-Basis"
      }) {
        MultiplayerQuizBase.entries.forEach { base ->
          SelectableRow(
            title = modeBaseTitle(base, language),
            selected = setup.multiplayerBase == base,
            onClick = { onMultiplayerBaseSelected(base) },
            description = modeBaseDescription(base, language),
          )
        }
      }
    }

    if (
      setup.mode == GameMode.Continents ||
      setup.mode == GameMode.SpeedRun ||
      setup.multiplayerBase == MultiplayerQuizBase.Continents && setup.mode == GameMode.LocalMultiplayer
    ) {
      SectionCard(title = cleanText(language, UiText.Continents)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          availableContinents.forEach { continent ->
            val isSelectable = continent != "Antarctica"
            FilterChip(
              selected = continent in setup.selectedContinents,
              onClick = { if (isSelectable) onContinentToggle(continent) },
              enabled = isSelectable,
              label = {
                Text(
                  text = localizedContinentName(continent, language),
                  textDecoration = if (isSelectable) TextDecoration.None else TextDecoration.LineThrough,
                )
              },
            )
          }
        }
      }
    }

    if (setup.mode != GameMode.AllIn && !(setup.mode == GameMode.LocalMultiplayer && setup.multiplayerBase == MultiplayerQuizBase.AllIn)) {
      SectionCard(title = when (language) {
        AppLanguage.English -> "Question count"
        AppLanguage.Bulgarian -> "Брой въпроси"
        AppLanguage.German -> "Fragenanzahl"
      }) {
        OutlinedTextField(
          value = setup.questionCountInput,
          onValueChange = onQuestionCountChange,
          label = {
            Text(
              when (language) {
                AppLanguage.English -> "Amount of questions"
                AppLanguage.Bulgarian -> "Брой въпроси"
                AppLanguage.German -> "Fragenanzahl"
              },
            )
          },
          placeholder = {
            Text(
              if (setup.surpriseMe) {
                when (language) {
                  AppLanguage.English -> "Surprise me selected"
                  AppLanguage.Bulgarian -> "Избрано е \"Изненадай ме\""
                  AppLanguage.German -> "\"Überrasche mich\" ausgewählt"
                }
              } else {
                when (language) {
                  AppLanguage.English -> "Example: 10"
                  AppLanguage.Bulgarian -> "Пример: 10"
                  AppLanguage.German -> "Beispiel: 10"
                }
              },
            )
          },
          supportingText = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                when (language) {
                  AppLanguage.English -> "Allowed range: 1-$questionCountLimit"
                  AppLanguage.Bulgarian -> "Допустим диапазон: 1-$questionCountLimit"
                  AppLanguage.German -> "Erlaubter Bereich: 1-$questionCountLimit"
                },
              )
              Text(
                when (language) {
                  AppLanguage.English -> "Questions repeat only if you ask for more than 195; otherwise each country appears once."
                  AppLanguage.Bulgarian -> "Въпросите се повтарят само ако поискаш повече от 195. Иначе всяка държава се показва само веднъж."
                  AppLanguage.German -> "Fragen wiederholen sich nur, wenn du mehr als 195 willst; sonst erscheint jedes Land nur einmal."
                },
              )
              if (ProgressionRules.shouldWarnNoMedal(setup.questionCount)) {
                Text(
                  when (language) {
                    AppLanguage.English -> "Perfect runs under 10 questions do not earn a medal."
                    AppLanguage.Bulgarian -> "Перфектен тест под 10 въпроса не носи медал."
                    AppLanguage.German -> "Perfekte Läufe unter 10 Fragen geben keine Medaille."
                  },
                )
              }
            }
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          enabled = !setup.surpriseMe,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = onSurpriseMe, modifier = Modifier.fillMaxWidth()) {
          Text(
            if (setup.surpriseMe) {
              when (language) {
                AppLanguage.English -> "Use custom amount"
                AppLanguage.Bulgarian -> "Използвай собствен брой"
                AppLanguage.German -> "Eigene Anzahl verwenden"
              }
            } else {
              when (language) {
                AppLanguage.English -> "Surprise me! (1-$questionCountLimit)"
                AppLanguage.Bulgarian -> "Изненадай ме! (1-$questionCountLimit)"
                AppLanguage.German -> "Überrasche mich! (1-$questionCountLimit)"
              }
            },
          )
        }
      }
    }

    SectionCard(title = when (language) {
      AppLanguage.English -> "Question variants"
      AppLanguage.Bulgarian -> "Видове въпроси"
      AppLanguage.German -> "Fragetypen"
    }) {
      QuizVariant.entries.forEach { variant ->
        CheckRow(
          title = localizedVariantTitle(variant, language),
          description = localizedVariantDescription(variant, language),
          checked = variant in setup.variants,
          onClick = { onVariantToggle(variant) },
        )
      }
    }

    if (setupError != null) {
      Text(text = setupError, color = AccentRed, style = MaterialTheme.typography.bodyMedium)
    }

    Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
      Text(
        when (language) {
          AppLanguage.English -> "Start quiz"
          AppLanguage.Bulgarian -> "Започни теста"
          AppLanguage.German -> "Quiz starten"
        },
      )
    }

    if (setup.mode == GameMode.AllIn) {
      val hasAllVariants = setup.variants.size == QuizVariant.entries.size
      val hintSettingLabel = localizedHintDifficultyTitle(hintDifficulty, language)
      val levelReward = if (hintDifficulty == HintDifficulty.Impossible) "+2" else "+1"
      InfoPanel(
        text = allInRewardInfo(
          language = language,
          hintSettingLabel = hintSettingLabel,
          hasAllVariants = hasAllVariants,
          rewardLevels = levelReward,
          isImpossible = hintDifficulty == HintDifficulty.Impossible,
        ),
      )
    }
  }
}

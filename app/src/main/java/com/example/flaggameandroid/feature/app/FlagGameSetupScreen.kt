package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.AllInType
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuizVariant
import com.example.flaggameandroid.theme.AccentRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@androidx.compose.runtime.Composable
fun SetupScreen(
  setup: SetupState,
  hintDifficulty: HintDifficulty,
  language: AppLanguage,
  availableContinents: List<String>,
  countries: List<FlagCountry>,
  questionCountLimit: Int,
  setupError: String?,
  onBack: () -> Unit,
  onVariantToggle: (QuizVariant) -> Unit,
  onInstantCorrectionToggled: () -> Unit,
  onContinentToggle: (String) -> Unit,
  onWorldFlagsHardcoreToggled: () -> Unit,
  onWorldFlagsTimerToggled: () -> Unit,
  onCreateQuizTrainingToggled: () -> Unit,
  onCreateQuizManualHardcoreToggled: () -> Unit,
  onCreateQuizManualTimerToggled: () -> Unit,
  onQuestionCountChange: (String) -> Unit,
  onSpeedRunSecondsChange: (String) -> Unit,
  onSurpriseMe: () -> Unit,
  onAllInTypeSelected: (AllInType) -> Unit,
  onMultiplayerBaseSelected: (MultiplayerQuizBase) -> Unit,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
  onCreateQuizSourceSelected: (CreateQuizSource) -> Unit,
  onCreateQuizPresetSelected: (CreateQuizPreset) -> Unit,
  onCreateQuizContinentToggled: (String) -> Unit,
  onCreateQuizCountryToggled: (String) -> Unit,
  onCreateQuizAllCountriesToggled: () -> Unit,
  onSaveCreateQuizClicked: (String, String?) -> FlagGameViewModel.SaveQuizResult,
  onStartQuiz: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showSaveDialog by remember { mutableStateOf(false) }
  var saveQuizName by remember { mutableStateOf("") }
  var saveFeedbackMessage by remember { mutableStateOf<String?>(null) }
  var replaceConflict by remember { mutableStateOf<FlagGameViewModel.SaveQuizResult.NameConflict?>(null) }
  var showInstantCorrectionInfo by remember { mutableStateOf(false) }
  var showWorldFlagsHardcoreInfo by remember { mutableStateOf(false) }
  var showCreateQuizTrainingInfo by remember { mutableStateOf(false) }
  var showCreateQuizHardcoreInfo by remember { mutableStateOf(false) }
  var showStickyQuestionCount by remember { mutableStateOf(false) }
  var questionVariantsExpanded by remember { mutableStateOf(false) }
  val manualCountryContinentExpanded = remember { mutableStateMapOf<String, Boolean>() }
  fun showSaveFeedback(message: String) {
    if (saveFeedbackMessage == null) {
      saveFeedbackMessage = message
    }
  }
  val context = LocalContext.current
  val createQuizPresetOrder =
    remember {
      listOf(
        CreateQuizPreset.TwoColors,
        CreateQuizPreset.ThreeColors,
        CreateQuizPreset.FourPlusColors,
        CreateQuizPreset.HorizontalStripes,
        CreateQuizPreset.VerticalStripes,
        CreateQuizPreset.Stars,
        CreateQuizPreset.Crosses,
        CreateQuizPreset.Animals,
        CreateQuizPreset.Nato,
        CreateQuizPreset.EuUnion,
        CreateQuizPreset.WorldTradeOrganization,
        CreateQuizPreset.CommonwealthOfNations,
        CreateQuizPreset.AfricanUnion,
        CreateQuizPreset.OrganisationOfIslamicCooperation,
      )
    }
  val countriesByContinent = remember(countries) { countries.groupBy { it.continent } }

  LaunchedEffect(saveFeedbackMessage) {
    if (saveFeedbackMessage != null) {
      Toast.makeText(context, saveFeedbackMessage!!, Toast.LENGTH_LONG).show()
      delay(5_000)
      saveFeedbackMessage = null
    }
  }

  LaunchedEffect(setup.mode, setup.createQuizSource) {
    showStickyQuestionCount = false
  }

  fun closeSetupInfoPanels() {
    showInstantCorrectionInfo = false
    showWorldFlagsHardcoreInfo = false
    showCreateQuizTrainingInfo = false
    showCreateQuizHardcoreInfo = false
  }

  ScreenShell(
    modifier = modifier,
    overlay = {
      if (setup.mode == GameMode.CreateQuiz && showStickyQuestionCount && !setup.usesCreateQuizManualHardcore) {
        val stickyQuestionCount = setup.questionCount ?: 0
        val stickyQuestionCountOverLimit =
          (setup.usesCreateQuizTraining || setup.createQuizSource == CreateQuizSource.PresetFilter) &&
            !setup.surpriseMe &&
            stickyQuestionCount > questionCountLimit
        val stickyRangeText =
          if (setup.usesCreateQuizTraining || setup.createQuizSource == CreateQuizSource.PresetFilter) {
            when (language) {
              AppLanguage.English -> if (setup.usesCreateQuizTraining) "Range: 1-999" else "Range: 1-$questionCountLimit"
              AppLanguage.Bulgarian -> if (setup.usesCreateQuizTraining) "Диапазон: 1-999" else "Диапазон: 1-$questionCountLimit"
              AppLanguage.German -> if (setup.usesCreateQuizTraining) "Bereich: 1-999" else "Bereich: 1-$questionCountLimit"
            }
          } else {
            null
          }
        val stickyWarningText =
          if (setup.usesCreateQuizTraining || setup.createQuizSource == CreateQuizSource.PresetFilter) {
            when {
              stickyQuestionCountOverLimit ->
                when (language) {
                  AppLanguage.English -> "Selected question count is over the allowed limit."
                  AppLanguage.Bulgarian -> "Избраният брой въпроси е над позволения лимит."
                  AppLanguage.German -> "Die gewählte Fragenzahl liegt über dem erlaubten Limit."
                }

              ProgressionRules.shouldWarnNoMedal(stickyQuestionCount) ->
                when (language) {
                  AppLanguage.English -> "Perfect runs under 10 questions do not earn a medal."
                  AppLanguage.Bulgarian -> "Перфектен тест под 10 въпроса не носи медал."
                  AppLanguage.German -> "Perfekte Läufe unter 10 Fragen geben keine Medaille."
                }

              else -> null
            }
          } else {
            null
          }
        Box(
          modifier =
            Modifier
              .align(Alignment.TopCenter)
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.background)
              .padding(bottom = 8.dp),
        ) {
          QuestionCountStickyCard(
            title =
              when (language) {
                AppLanguage.English -> "Question count"
                AppLanguage.Bulgarian -> "Брой въпроси"
                AppLanguage.German -> "Fragenanzahl"
              },
            questionCountValue =
              if (!setup.usesCreateQuizTraining && !setup.usesCreateQuizManualHardcore && setup.createQuizSource == CreateQuizSource.ManualCountries) {
                setup.selectedCountryCodes.size.toString()
              } else {
                setup.questionCountInput
              },
            editable = (setup.usesCreateQuizTraining || setup.createQuizSource == CreateQuizSource.PresetFilter) && !setup.usesCreateQuizManualHardcore && !setup.surpriseMe,
            showRandomButton = !setup.usesCreateQuizTraining && !setup.usesCreateQuizManualHardcore && setup.createQuizSource == CreateQuizSource.PresetFilter,
            randomButtonText =
              when (language) {
                AppLanguage.English -> if (setup.surpriseMe) "Custom count" else "Randomizer"
                AppLanguage.Bulgarian -> if (setup.surpriseMe) "Custom count" else "Randomizer"
                AppLanguage.German -> if (setup.surpriseMe) "Custom count" else "Randomizer"
              },
            onRandomButtonClick = onSurpriseMe,
            onValueChange = onQuestionCountChange,
            rangeText = stickyRangeText,
            warningText = stickyWarningText,
            warningIsError = stickyQuestionCountOverLimit,
            placeholderText = surpriseMePlaceholderText(language, setup.surpriseMe),
          )
        }
      }
    },
  ) {
    HeaderRow(title = cleanModeTitle(setup.mode, language))

    CompactToggleInfoCard(
      title =
        when (language) {
          AppLanguage.English -> "Instant correction?"
          AppLanguage.Bulgarian -> "Моментална проверка?"
          AppLanguage.German -> "Sofortige Auswertung?"
        },
      checked = setup.instantCorrectionEnabled,
      onCheckedChange = onInstantCorrectionToggled,
      infoExpanded = showInstantCorrectionInfo,
      onInfoClick = {
        val next = !showInstantCorrectionInfo
        closeSetupInfoPanels()
        showInstantCorrectionInfo = next
      },
      infoText =
        when (language) {
          AppLanguage.English ->
            "Show right or wrong immediately after each answer is accepted. Turn it off to wait until Results page at the end of the quiz."
          AppLanguage.Bulgarian ->
            "Показва правилно или грешно веднага след като отговорът е приет. Изключи го, за да виждаш резултатите чак накрая."
          AppLanguage.German ->
            "Zeigt direkt nach der Annahme der Antwort richtig oder falsch an. Deaktiviere es, um erst in den Ergebnissen zu sehen."
        },
    )

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
        val playerCount = setup.playerNames.size
        val showRemovePlayer = playerCount > 2
        val showAddPlayer = playerCount < 5
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = onRemovePlayer,
            enabled = showRemovePlayer,
            modifier = Modifier.weight(1f),
          ) {
            Text(
              when (language) {
                AppLanguage.English -> "Remove"
                AppLanguage.Bulgarian -> "Премахни"
                AppLanguage.German -> "Entfernen"
              },
            )
          }
          Button(
            onClick = onAddPlayer,
            enabled = showAddPlayer,
            modifier = Modifier.weight(1f),
          ) {
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

    val isMistakeReview = setup.mode == GameMode.MistakeReview
    val isCreateQuizTraining = setup.usesCreateQuizTraining
    val isCreateQuizManual = setup.mode == GameMode.CreateQuiz && setup.createQuizSource == CreateQuizSource.ManualCountries
    val isCreateQuizManualHardcore = setup.usesCreateQuizManualHardcore
    val questionCount = setup.questionCount
    val questionCountChangeHandler: (String) -> Unit =
      if (isMistakeReview || (isCreateQuizManual && !isCreateQuizTraining)) {
        { _: String -> }
      } else {
        onQuestionCountChange
      }
    val questionCountOverLimit =
      !setup.surpriseMe &&
        !isMistakeReview &&
        !(isCreateQuizManual && !isCreateQuizTraining) &&
        questionCount != null &&
        questionCount > questionCountLimit
    val surpriseMePlaceholderText = surpriseMePlaceholderText(language, setup.surpriseMe)
    val renderTimerInput: @Composable ColumnScope.() -> Unit = {
      val secondsPerAnswer = setup.speedRunSecondsPerAnswer
      val secondsOutOfRange = secondsPerAnswer != null && secondsPerAnswer !in 1..60
      OutlinedTextField(
        value = setup.speedRunSecondsPerAnswerInput,
        onValueChange = onSpeedRunSecondsChange,
        label = {
          Text(
            when (language) {
              AppLanguage.English -> "Example: 5"
              AppLanguage.Bulgarian -> "Пример: 5"
              AppLanguage.German -> "Beispiel: 5"
            },
          )
        },
        supportingText = {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              when (language) {
                AppLanguage.English -> "Allowed range: 1-60"
                AppLanguage.Bulgarian -> "Допустим диапазон: 1-60"
                AppLanguage.German -> "Erlaubter Bereich: 1-60"
              },
            )
            Text(
              when (language) {
                AppLanguage.English -> "1-second bonus: +5 seconds only for quizzes with 10 or more questions."
                AppLanguage.Bulgarian -> "Бонус за 1 секунда: +5 секунди само при тестове с 10 или повече въпроса."
                AppLanguage.German -> "1-Sekunden-Bonus: +5 Sekunden nur bei Quiz mit 10 oder mehr Fragen."
              },
            )
            if (secondsOutOfRange) {
              Text(
                when (language) {
                  AppLanguage.English -> "Enter a value between 1 and 60 to start."
                  AppLanguage.Bulgarian -> "Въведи стойност между 1 и 60, за да стартираш."
                  AppLanguage.German -> "Gib einen Wert zwischen 1 und 60 ein, um zu starten."
                },
                color = AccentRed,
              )
            }
          }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    val renderChooseCountriesSection: @Composable () -> Unit = {
      SectionCard(title = when (language) {
        AppLanguage.English -> "Pick manually"
        AppLanguage.Bulgarian -> "Избери ръчно"
        AppLanguage.German -> "Selber auswählen"
      }, headerAction = {
        OutlinedButton(
          onClick = onCreateQuizAllCountriesToggled,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
          Text(
            when (language) {
              AppLanguage.English -> if (setup.selectedCountryCodes.size == countries.size) "Deselect all" else "Select all"
              AppLanguage.Bulgarian -> if (setup.selectedCountryCodes.size == countries.size) "Махни всички" else "Избери всички"
              AppLanguage.German -> if (setup.selectedCountryCodes.size == countries.size) "Alle abwählen" else "Alle wählen"
            },
          )
        }
      }) {
        countriesByContinent.forEach { (continent, list) ->
          val continentCodes = list.map { it.code }.toSet()
          val continentSelected = continentCodes.isNotEmpty() && continentCodes.all { it in setup.selectedCountryCodes }
          val continentExpanded = manualCountryContinentExpanded[continent] ?: true
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
          ) {
            Text(
              text = localizedContinentName(continent, language),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
              modifier = Modifier.clickable { manualCountryContinentExpanded[continent] = !continentExpanded },
            )
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
              OutlinedButton(
                onClick = { onCreateQuizContinentToggled(continent) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              ) {
                Text(
                  when (language) {
                    AppLanguage.English -> if (continentSelected) "Deselect all" else "Select all"
                    AppLanguage.Bulgarian -> if (continentSelected) "Махни всички" else "Избери всички"
                    AppLanguage.German -> if (continentSelected) "Alle abwählen" else "Alle wählen"
                  },
                )
              }
              TextButton(
                onClick = { manualCountryContinentExpanded[continent] = !continentExpanded },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              ) {
                Text(if (continentExpanded) "▾" else "▸")
              }
            }
          }
          if (continentExpanded) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              list.sortedBy { it.localizedName(language) }.forEach { country ->
                FilterChip(
                  selected = country.code in setup.selectedCountryCodes,
                  onClick = { onCreateQuizCountryToggled(country.code) },
                  label = { Text("${country.emoji} ${country.localizedName(language)}") },
                )
              }
            }
          }
        }
      }
    }

    val renderQuestionCountCard: @Composable (
      questionCountValue: String,
      editable: Boolean,
      showRandomButton: Boolean,
      randomButtonText: String,
      onRandomButtonClick: () -> Unit,
      onValueChange: (String) -> Unit,
      rangeText: String?,
      warningText: String?,
      warningIsError: Boolean,
    ) -> Unit = { questionCountValue, editable, showRandomButton, randomButtonText, onRandomButtonClick, onValueChange, rangeText, warningText, warningIsError ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val rangeTextAsLabel = setup.mode == GameMode.CreateQuiz && setup.createQuizSource == CreateQuizSource.PresetFilter
          val useCreateQuizQuestionCountLayout = setup.mode == GameMode.CreateQuiz && setup.createQuizSource == CreateQuizSource.PresetFilter
          Text(
            text = when (language) {
              AppLanguage.English -> "Question count"
              AppLanguage.Bulgarian -> "Брой въпроси"
              AppLanguage.German -> "Fragenanzahl"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
          ) {
            val fieldModifier =
              if (showRandomButton && useCreateQuizQuestionCountLayout) {
                Modifier.weight(1f)
              } else if (showRandomButton) {
                Modifier.fillMaxWidth(0.70f)
              } else {
                Modifier.fillMaxWidth()
              }
            OutlinedTextField(
              value = questionCountValue,
              onValueChange = onValueChange,
              placeholder = {
                Text(
                  surpriseMePlaceholderText,
                )
              },
              label =
                if (rangeTextAsLabel && rangeText != null) {
                  { Text(rangeText) }
                } else {
                  null
                },
              supportingText =
                if ((!rangeTextAsLabel && rangeText != null) || warningText != null) {
                  {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      if (!rangeTextAsLabel && rangeText != null) {
                        Text(text = rangeText, style = MaterialTheme.typography.bodySmall)
                      }
                      if (warningText != null) {
                        Text(
                          text = warningText,
                          style = MaterialTheme.typography.bodySmall,
                          color = if (warningIsError) AccentRed else MaterialTheme.colorScheme.onSurface,
                        )
                      }
                    }
                  }
                } else {
                  null
                },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              enabled = editable,
              isError = warningIsError,
              modifier = fieldModifier,
            )
            if (showRandomButton) {
              val buttonModifier =
                if (useCreateQuizQuestionCountLayout) {
                  Modifier.weight(1f)
                } else {
                  Modifier
                }
              OutlinedButton(
                onClick = onRandomButtonClick,
                modifier = buttonModifier,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
              ) {
                Text(randomButtonText)
              }
            }
          }
        }
      }
    }

    if (setup.mode == GameMode.CreateQuiz) {
      if (!setup.usesCreateQuizManualHardcore) {
        CompactToggleInfoCard(
        title =
          when (language) {
            AppLanguage.English -> "Training"
            AppLanguage.Bulgarian -> "Тренировка"
            AppLanguage.German -> "Training"
          },
        checked = setup.createQuizTrainingEnabled,
        onCheckedChange = { onCreateQuizTrainingToggled() },
        infoExpanded = showCreateQuizTrainingInfo,
        onInfoClick = {
          val next = !showCreateQuizTrainingInfo
          closeSetupInfoPanels()
          showCreateQuizTrainingInfo = next
        },
        infoText =
          when (language) {
            AppLanguage.English ->
              "Build a random training quiz with range 1-999. It uses a repeated world pool: 5 full passes over all 195 countries, then a 6th pass for any extra questions up to 999."
            AppLanguage.Bulgarian ->
              "Създава произволен тренировъчен тест с диапазон 1-999. Ползва повтарящ се световен набор: 5 пълни минавания през всичките 195 държави, после 6-то минаване за оставащите въпроси до 999."
            AppLanguage.German ->
              "Erstellt ein zufälliges Trainingsquiz mit Bereich 1-999. Es nutzt einen wiederholten Welt-Pool: 5 volle Durchgänge über alle 195 Länder und einen 6. Durchgang für zusätzliche Fragen bis 999."
          },
        )
      }

      if (!setup.usesCreateQuizTraining) {
        CompactToggleInfoCard(
          title =
            when (language) {
              AppLanguage.English -> "Hardcore"
              AppLanguage.Bulgarian -> "Хардкор"
              AppLanguage.German -> "Hardcore"
            },
          checked = setup.createQuizManualHardcoreEnabled,
          onCheckedChange = { onCreateQuizManualHardcoreToggled() },
          infoExpanded = showCreateQuizHardcoreInfo,
          onInfoClick = {
            val next = !showCreateQuizHardcoreInfo
            closeSetupInfoPanels()
            showCreateQuizHardcoreInfo = next
          },
          infoText =
            when (language) {
              AppLanguage.English ->
                "Use all 195 countries exactly once. Timer, question variants, and instant correction stay available."
              AppLanguage.Bulgarian ->
                "Използва всички 195 държави точно по веднъж. Таймерът, видовете въпроси и моменталната проверка остават налични."
              AppLanguage.German ->
                "Verwendet alle 195 Länder genau einmal. Timer, Fragetypen und Sofortauswertung bleiben verfügbar."
            },
        )
      }

      CompactToggleCard(
        title =
          when (language) {
            AppLanguage.English -> "Timer?"
            AppLanguage.Bulgarian -> "Таймер?"
            AppLanguage.German -> "Timer?"
        },
        checked = setup.createQuizManualTimerEnabled,
        onCheckedChange = { onCreateQuizManualTimerToggled() },
      ) { if (setup.createQuizManualTimerEnabled) renderTimerInput() }

      if (setup.usesCreateQuizTraining || setup.createQuizSource == CreateQuizSource.PresetFilter) {
        Box(
          modifier =
            Modifier.onGloballyPositioned {
              showStickyQuestionCount = it.boundsInRoot().bottom <= 0f
            },
        ) {
          renderQuestionCountCard(
            setup.questionCountInput,
            !setup.surpriseMe,
            !setup.usesCreateQuizTraining,
            when (language) {
              AppLanguage.English -> if (setup.surpriseMe) "Custom count" else "Randomizer"
              AppLanguage.Bulgarian -> if (setup.surpriseMe) "Custom count" else "Randomizer"
              AppLanguage.German -> if (setup.surpriseMe) "Custom count" else "Randomizer"
            },
            onSurpriseMe,
            questionCountChangeHandler,
            when (language) {
              AppLanguage.English -> if (setup.usesCreateQuizTraining) "Range: 1-999" else "Range: 1-$questionCountLimit"
              AppLanguage.Bulgarian -> if (setup.usesCreateQuizTraining) "Диапазон: 1-999" else "Диапазон: 1-$questionCountLimit"
              AppLanguage.German -> if (setup.usesCreateQuizTraining) "Bereich: 1-999" else "Bereich: 1-$questionCountLimit"
            },
            when {
              questionCountOverLimit ->
                when (language) {
                  AppLanguage.English -> "Selected question count is over the allowed limit."
                  AppLanguage.Bulgarian -> "Избраният брой въпроси е над позволения лимит."
                  AppLanguage.German -> "Die gewählte Fragenzahl liegt über dem erlaubten Limit."
                }
              ProgressionRules.shouldWarnNoMedal(setup.questionCount) ->
                when (language) {
                  AppLanguage.English -> "Perfect runs under 10 questions do not earn a medal."
                  AppLanguage.Bulgarian -> "Перфектен тест под 10 въпроса не носи медал."
                  AppLanguage.German -> "Perfekte Läufe unter 10 Fragen geben keine Medaille."
                }
              else -> null
            },
            questionCountOverLimit,
          )
        }
      } else {
        if (!setup.usesCreateQuizManualHardcore) {
          Box(
          modifier =
            Modifier.onGloballyPositioned {
              showStickyQuestionCount = it.boundsInRoot().bottom <= 0f
            },
        ) {
          renderQuestionCountCard(
            setup.selectedCountryCodes.size.toString(),
            false,
            false,
            "",
            onSurpriseMe,
            questionCountChangeHandler,
            null,
            null,
            false,
          )
        }
        }
      }

      QuestionVariantsSection(
        language = language,
        selectedVariants = setup.variants,
        expanded = questionVariantsExpanded,
        onExpandedChange = { questionVariantsExpanded = !questionVariantsExpanded },
        onVariantToggle = onVariantToggle,
      )

      if (!setup.usesCreateQuizTraining && !setup.usesCreateQuizManualHardcore) {
        SectionCard(title = when (language) {
          AppLanguage.English -> "Create a quiz"
          AppLanguage.Bulgarian -> "Създай тест"
          AppLanguage.German -> "Quiz erstellen"
        }) {
          SelectableRow(
            title = when (language) {
              AppLanguage.English -> "Preset filter"
              AppLanguage.Bulgarian -> "Готов филтър"
              AppLanguage.German -> "Vorlagenfilter"
            },
            selected = setup.createQuizSource == CreateQuizSource.PresetFilter,
            onClick = { onCreateQuizSourceSelected(CreateQuizSource.PresetFilter) },
            description = when (language) {
              AppLanguage.English -> "Use predefined flag rules."
              AppLanguage.Bulgarian -> "Ползвай готови правила за флагове."
              AppLanguage.German -> "Nutze vordefinierte Flaggenregeln."
            },
          )
          SelectableRow(
            title = when (language) {
              AppLanguage.English -> "Manual countries"
              AppLanguage.Bulgarian -> "Ръчно избрани държави"
              AppLanguage.German -> "Manuelle Länder"
            },
            selected = setup.createQuizSource == CreateQuizSource.ManualCountries,
            onClick = { onCreateQuizSourceSelected(CreateQuizSource.ManualCountries) },
            description = when (language) {
              AppLanguage.English -> "Pick the exact countries yourself."
              AppLanguage.Bulgarian -> "Избери точните държави сам."
              AppLanguage.German -> "Wähle die Länder selbst aus."
            },
          )
        }

        if (setup.createQuizSource == CreateQuizSource.PresetFilter) {
          SectionCard(title = when (language) {
            AppLanguage.English -> "Preset filters"
            AppLanguage.Bulgarian -> "Готови филтри"
            AppLanguage.German -> "Vorlagenfilter"
          }) {
            val selectedPresets = setup.createQuizPresets.ifEmpty { setOf(setup.createQuizPreset) }
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              createQuizPresetOrder.forEach { preset ->
                FilterChip(
                  selected = preset in selectedPresets,
                  onClick = { onCreateQuizPresetSelected(preset) },
                  label = {
                    Text(
                      text = localizedCreateQuizPresetTitle(preset, language),
                      maxLines = 1,
                      style = MaterialTheme.typography.bodyMedium,
                    )
                  },
                )
              }
            }
          }
        } else {
          renderChooseCountriesSection()
        }
      }
    }

    if (setup.mode == GameMode.WorldFlags) {
      CompactToggleInfoCard(
        title =
          when (language) {
            AppLanguage.English -> "Hardcore game?"
            AppLanguage.Bulgarian -> "Хардкор игра?"
            AppLanguage.German -> "Hardcore-Spiel?"
          },
        checked = setup.worldFlagsHardcoreEnabled,
        onCheckedChange = { onWorldFlagsHardcoreToggled() },
        infoExpanded = showWorldFlagsHardcoreInfo,
        onInfoClick = {
          val next = !showWorldFlagsHardcoreInfo
          closeSetupInfoPanels()
          showWorldFlagsHardcoreInfo = next
        },
        infoText =
          when (language) {
            AppLanguage.English ->
              "Use all 195 countries, keep only the timer option, and hide continent and question-count choices."
            AppLanguage.Bulgarian ->
              "Използва всички 195 държави, оставя само таймера и скрива избора на континенти и брой въпроси."
            AppLanguage.German ->
              "Verwendet alle 195 Länder, lässt nur den Timer aktiv und blendet Kontinente sowie Fragenanzahl aus."
          },
      )
      CompactToggleCard(
        title =
          when (language) {
            AppLanguage.English -> "Timer?"
            AppLanguage.Bulgarian -> "Таймер?"
            AppLanguage.German -> "Timer?"
          },
        checked = setup.worldFlagsTimerEnabled,
        onCheckedChange = { onWorldFlagsTimerToggled() },
      ) { if (setup.worldFlagsTimerEnabled) renderTimerInput() }
      if (!setup.worldFlagsHardcoreEnabled) {
        QuestionVariantsSection(
          language = language,
          selectedVariants = setup.variants,
          expanded = questionVariantsExpanded,
          onExpandedChange = { questionVariantsExpanded = !questionVariantsExpanded },
          onVariantToggle = onVariantToggle,
        )
      }
      if (!setup.worldFlagsHardcoreEnabled) {
        SectionCard(title = when (language) {
          AppLanguage.English -> "Question count"
          AppLanguage.Bulgarian -> "Брой въпроси"
          AppLanguage.German -> "Fragenanzahl"
        }) {
          OutlinedTextField(
            value = setup.questionCountInput,
            onValueChange = questionCountChangeHandler,
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
                surpriseMePlaceholderText,
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
                if (questionCountOverLimit) {
                  Text(
                    when (language) {
                      AppLanguage.English -> "Selected question count is over the allowed limit."
                      AppLanguage.Bulgarian -> "Избраният брой въпроси е над позволения лимит."
                      AppLanguage.German -> "Die gewählte Fragenzahl liegt über dem erlaubten Limit."
                    },
                    color = AccentRed,
                  )
                } else if (ProgressionRules.shouldWarnNoMedal(setup.questionCount)) {
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
            isError = questionCountOverLimit,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedButton(onClick = onSurpriseMe, modifier = Modifier.fillMaxWidth()) {
            Text(
              if (setup.surpriseMe) {
          when (language) {
                  AppLanguage.English -> "Custom count"
                  AppLanguage.Bulgarian -> "Custom count"
                  AppLanguage.German -> "Custom count"
                }
              } else {
                when (language) {
                  AppLanguage.English -> "Randomizer"
                  AppLanguage.Bulgarian -> "Randomizer"
                  AppLanguage.German -> "Randomizer"
                }
              },
            )
          }
        }
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
    } else if (setup.mode != GameMode.CreateQuiz) {
      if (setup.multiplayerBase == MultiplayerQuizBase.Continents && setup.mode == GameMode.LocalMultiplayer) {
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

      else {
        renderQuestionCountCard(
          when {
            isMistakeReview -> questionCountLimit.toString()
            else -> setup.questionCountInput
          },
          !setup.surpriseMe && !isMistakeReview,
          !isMistakeReview,
          when (language) {
            AppLanguage.English -> if (setup.surpriseMe) "Custom count" else "Randomizer"
            AppLanguage.Bulgarian -> if (setup.surpriseMe) "Custom count" else "Randomizer"
            AppLanguage.German -> if (setup.surpriseMe) "Custom count" else "Randomizer"
          },
          onSurpriseMe,
          questionCountChangeHandler,
          if (isMistakeReview) null else when (language) {
            AppLanguage.English -> "Allowed range: 1-$questionCountLimit"
            AppLanguage.Bulgarian -> "Допустим диапазон: 1-$questionCountLimit"
            AppLanguage.German -> "Erlaubter Bereich: 1-$questionCountLimit"
          },
          if (isMistakeReview) null else when {
            questionCountOverLimit ->
              when (language) {
                AppLanguage.English -> "Selected question count is over the allowed limit."
                AppLanguage.Bulgarian -> "Избраният брой въпроси е над позволения лимит."
                AppLanguage.German -> "Die gewählte Fragenzahl liegt über dem erlaubten Limit."
              }
            ProgressionRules.shouldWarnNoMedal(setup.questionCount) ->
              when (language) {
                AppLanguage.English -> "Perfect runs under 10 questions do not earn a medal."
                AppLanguage.Bulgarian -> "Перфектен тест под 10 въпроса не носи медал."
                AppLanguage.German -> "Perfekte Läufe unter 10 Fragen geben keine Medaille."
              }
            else -> null
          },
          questionCountOverLimit,
        )

        QuestionVariantsSection(
          language = language,
          selectedVariants = setup.variants,
          expanded = questionVariantsExpanded,
          onExpandedChange = { questionVariantsExpanded = !questionVariantsExpanded },
          onVariantToggle = onVariantToggle,
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

    if (setup.mode == GameMode.CreateQuiz && !setup.usesCreateQuizTraining && !setup.usesCreateQuizManualHardcore) {
      Button(
        onClick = {
          saveQuizName =
            if (setup.createQuizSource == CreateQuizSource.ManualCountries) {
              when (language) {
                AppLanguage.English -> "My quiz"
                AppLanguage.Bulgarian -> "Моят тест"
                AppLanguage.German -> "Mein Quiz"
              }
            } else {
              when (language) {
                AppLanguage.English -> "Saved quiz"
                AppLanguage.Bulgarian -> "Запазен тест"
                AppLanguage.German -> "Gespeichertes Quiz"
              }
            }
          showSaveDialog = true
        },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
      ) {
        Text(
          when (language) {
            AppLanguage.English -> "Save to favorites"
            AppLanguage.Bulgarian -> "Запази в любими"
            AppLanguage.German -> "In Favoriten speichern"
          },
        )
      }
    }

    if (showSaveDialog) {
      AlertDialog(
        onDismissRequest = { showSaveDialog = false },
        title = {
          Text(
            when (language) {
              AppLanguage.English -> "Name this quiz"
              AppLanguage.Bulgarian -> "Назови този тест"
              AppLanguage.German -> "Quiz benennen"
            },
          )
        },
        text = {
          OutlinedTextField(
            value = saveQuizName,
            onValueChange = { saveQuizName = it.take(30) },
            singleLine = true,
            label = {
              Text(
                when (language) {
                  AppLanguage.English -> "Quiz name"
                  AppLanguage.Bulgarian -> "Име на теста"
                  AppLanguage.German -> "Quizname"
                },
              )
            },
            supportingText = {
              Text(
                when (language) {
                  AppLanguage.English -> "Up to 30 characters."
                  AppLanguage.Bulgarian -> "До 30 знака."
                  AppLanguage.German -> "Bis zu 30 Zeichen."
                },
              )
            },
            modifier = Modifier.fillMaxWidth(),
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              when (val result = onSaveCreateQuizClicked(saveQuizName, null)) {
                is FlagGameViewModel.SaveQuizResult.Saved -> {
                  showSaveFeedback(result.message)
                  showSaveDialog = false
                }

                is FlagGameViewModel.SaveQuizResult.DuplicateConfiguration -> {
                  showSaveFeedback(
                    when (language) {
                      AppLanguage.English -> "That exact quiz is already saved as \"${result.existingName}\". The quiz was not saved."
                      AppLanguage.Bulgarian -> "Същият тест вече е записан като \"${result.existingName}\". Тестът не беше записан."
                      AppLanguage.German -> "Dasselbe Quiz ist bereits als \"${result.existingName}\" gespeichert. Der Test war nicht gespeichert."
                    }
                  )
                  showSaveDialog = false
                }
                is FlagGameViewModel.SaveQuizResult.NameConflict -> {
                  replaceConflict = result
                  showSaveDialog = false
                }

                FlagGameViewModel.SaveQuizResult.NoOp -> showSaveDialog = false
              }
            },
          ) {
            Text(
              when (language) {
                AppLanguage.English -> "Save"
                AppLanguage.Bulgarian -> "Запази"
                AppLanguage.German -> "Speichern"
              },
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { showSaveDialog = false }) {
            Text(
              when (language) {
                AppLanguage.English -> "Cancel"
                AppLanguage.Bulgarian -> "Отказ"
                AppLanguage.German -> "Abbrechen"
              },
            )
          }
        },
      )
    }

    replaceConflict?.let { conflict ->
      AlertDialog(
        onDismissRequest = { replaceConflict = null },
        title = {
          Text(
            when (language) {
              AppLanguage.English -> "Replace existing quiz?"
              AppLanguage.Bulgarian -> "Да заменя ли запазения тест?"
              AppLanguage.German -> "Vorhandenes Quiz ersetzen?"
            },
          )
        },
        text = {
          Text(
            when (language) {
              AppLanguage.English -> "A quiz named \"${conflict.existingName}\" already exists. Replace it with the new one?"
              AppLanguage.Bulgarian -> "Тест с име \"${conflict.existingName}\" вече съществува. Да бъде ли заменен с новия?"
              AppLanguage.German -> "Ein Quiz mit dem Namen \"${conflict.existingName}\" existiert bereits. Soll es durch das neue ersetzt werden?"
            },
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              when (val result = onSaveCreateQuizClicked(saveQuizName, conflict.existingTemplateId)) {
                is FlagGameViewModel.SaveQuizResult.Saved -> showSaveFeedback(result.message)
                is FlagGameViewModel.SaveQuizResult.DuplicateConfiguration ->
                  showSaveFeedback(
                    when (language) {
                      AppLanguage.English -> "That exact quiz is already saved as \"${result.existingName}\"."
                      AppLanguage.Bulgarian -> "Същият тест вече е записан като \"${result.existingName}\"."
                      AppLanguage.German -> "Dasselbe Quiz ist bereits als \"${result.existingName}\" gespeichert."
                    }
                  )
                is FlagGameViewModel.SaveQuizResult.NameConflict,
                FlagGameViewModel.SaveQuizResult.NoOp -> Unit
              }
              replaceConflict = null
            },
          ) {
            Text(
              when (language) {
                AppLanguage.English -> "Replace"
                AppLanguage.Bulgarian -> "Замени"
                AppLanguage.German -> "Ersetzen"
              },
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { replaceConflict = null }) {
            Text(
              when (language) {
                AppLanguage.English -> "Cancel"
                AppLanguage.Bulgarian -> "Отказ"
                AppLanguage.German -> "Abbrechen"
              },
            )
          }
        },
      )
    }

    if (setup.mode == GameMode.WorldFlags) {
      val hintSettingLabel = localizedHintDifficultyTitle(hintDifficulty, language)
      val levelReward = if (hintDifficulty == HintDifficulty.Impossible) "+2" else "+1"
      InfoPanel(
        text = worldFlagsRewardInfo(
          language = language,
          hardcoreEnabled = setup.worldFlagsHardcoreEnabled,
          hintSettingLabel = hintSettingLabel,
          rewardLevels = levelReward,
          isImpossible = hintDifficulty == HintDifficulty.Impossible,
        ),
      )
    }

  }
}

@Composable
private fun CompactToggleCard(
  title: String,
  checked: Boolean,
  onCheckedChange: () -> Unit,
  content: @Composable ColumnScope.() -> Unit = {},
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        Switch(
          checked = checked,
          onCheckedChange = { onCheckedChange() },
        )
      }
      content()
    }
  }
}

@Composable
private fun CompactToggleInfoCard(
  title: String,
  checked: Boolean,
  onCheckedChange: () -> Unit,
  infoExpanded: Boolean,
  onInfoClick: () -> Unit,
  infoText: String,
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
          modifier = Modifier.weight(1f),
        )
        InfoButton(onClick = onInfoClick)
        Switch(
          checked = checked,
          onCheckedChange = { onCheckedChange() },
        )
      }
      if (infoExpanded) {
        InfoPanel(text = infoText)
      }
    }
  }
}

@Composable
private fun QuestionVariantsSection(
  language: AppLanguage,
  selectedVariants: Set<QuizVariant>,
  expanded: Boolean,
  onExpandedChange: () -> Unit,
  onVariantToggle: (QuizVariant) -> Unit,
) {
  val title =
    when (language) {
      AppLanguage.English -> "Question variants"
      AppLanguage.Bulgarian -> "Видове въпроси"
      AppLanguage.German -> "Fragetypen"
    }

  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          modifier =
            Modifier
              .weight(1f)
              .clickable(onClick = onExpandedChange),
        )
        TextButton(
          onClick = onExpandedChange,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        ) {
          Text(if (expanded) "▼" else "▶")
        }
      }

    if (expanded) {
      QuizVariant.entries.forEach { variant ->
        CheckRow(
          title = localizedVariantTitle(variant, language),
          description = localizedVariantDescription(variant, language),
          checked = variant in selectedVariants,
          onClick = { onVariantToggle(variant) },
        )
      }
    }
    }
  }
}

@Composable
private fun QuestionCountStickyCard(
  title: String,
  questionCountValue: String,
  editable: Boolean,
  showRandomButton: Boolean,
  randomButtonText: String,
  onRandomButtonClick: () -> Unit,
  onValueChange: (String) -> Unit,
  rangeText: String?,
  warningText: String?,
  warningIsError: Boolean,
  placeholderText: String,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
      ) {
        val fieldModifier =
          if (showRandomButton) {
            Modifier.weight(3f)
          } else {
            Modifier.fillMaxWidth()
          }
        OutlinedTextField(
          value = questionCountValue,
          onValueChange = onValueChange,
          placeholder = {
            Text(placeholderText)
          },
          label =
            if (rangeText != null) {
              { Text(rangeText) }
            } else {
              null
            },
          supportingText =
            if (warningText != null) {
              {
                Text(
                  text = warningText,
                  style = MaterialTheme.typography.bodySmall,
                  color = if (warningIsError) AccentRed else MaterialTheme.colorScheme.onSurface,
                )
              }
            } else {
              null
            },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          enabled = editable,
          isError = warningIsError,
          modifier = fieldModifier,
        )
        if (showRandomButton) {
          OutlinedButton(
            onClick = onRandomButtonClick,
            modifier = Modifier.weight(2f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          ) {
            Text(randomButtonText, maxLines = 1)
          }
        }
      }
    }
  }
}

private fun surpriseMePlaceholderText(
  language: AppLanguage,
  surpriseMeEnabled: Boolean,
): String =
  if (surpriseMeEnabled) {
    when (language) {
      AppLanguage.English -> "Random count selected"
      AppLanguage.Bulgarian -> "Числото ще е случайно"
      AppLanguage.German -> "Zufallsauswahl"
    }
  } else {
    when (language) {
      AppLanguage.English -> "Example: 10"
      AppLanguage.Bulgarian -> "Пример: 10"
      AppLanguage.German -> "Beispiel: 10"
    }
  }

private fun localizedCreateQuizPresetTitle(
  preset: CreateQuizPreset,
  language: AppLanguage,
): String =
  when (preset) {
    CreateQuizPreset.TwoColors -> when (language) {
      AppLanguage.English -> "2 colors"
      AppLanguage.Bulgarian -> "2 цвята"
      AppLanguage.German -> "2 Farben"
    }
    CreateQuizPreset.ThreeColors -> when (language) {
      AppLanguage.English -> "3 colors"
      AppLanguage.Bulgarian -> "3 цвята"
      AppLanguage.German -> "3 Farben"
    }
    CreateQuizPreset.FourPlusColors -> when (language) {
      AppLanguage.English -> "4+ colors"
      AppLanguage.Bulgarian -> "4+ цвята"
      AppLanguage.German -> "4+ Farben"
    }
    CreateQuizPreset.HorizontalStripes -> when (language) {
      AppLanguage.English -> "Horizontal stripes"
      AppLanguage.Bulgarian -> "Хоризонтални ивици"
      AppLanguage.German -> "Horizontale Streifen"
    }
    CreateQuizPreset.VerticalStripes -> when (language) {
      AppLanguage.English -> "Vertical stripes"
      AppLanguage.Bulgarian -> "Вертикални ивици"
      AppLanguage.German -> "Vertikale Streifen"
    }
    CreateQuizPreset.Stars -> when (language) {
      AppLanguage.English -> "Stars"
      AppLanguage.Bulgarian -> "Звезди"
      AppLanguage.German -> "Sterne"
    }
    CreateQuizPreset.Crosses -> when (language) {
      AppLanguage.English -> "Crosses"
      AppLanguage.Bulgarian -> "Кръстове"
      AppLanguage.German -> "Kreuze"
    }
    CreateQuizPreset.NoSymbols -> when (language) {
      AppLanguage.English -> "No symbols"
      AppLanguage.Bulgarian -> "Без символи"
      AppLanguage.German -> "Ohne Symbole"
    }
    CreateQuizPreset.Animals -> when (language) {
      AppLanguage.English -> "Animals"
      AppLanguage.Bulgarian -> "Животни"
      AppLanguage.German -> "Tiere"
    }
    CreateQuizPreset.Nato -> when (language) {
      AppLanguage.English -> "NATO flags"
      AppLanguage.Bulgarian -> "Флагове на НАТО"
      AppLanguage.German -> "NATO-Flaggen"
    }
    CreateQuizPreset.EuUnion -> when (language) {
      AppLanguage.English -> "EU union flags"
      AppLanguage.Bulgarian -> "Флагове на ЕС"
      AppLanguage.German -> "EU-Flaggen"
    }
    CreateQuizPreset.WorldTradeOrganization -> when (language) {
      AppLanguage.English -> "WTO flags"
      AppLanguage.Bulgarian -> "Флагове на СТО"
      AppLanguage.German -> "WTO-Flaggen"
    }
    CreateQuizPreset.CommonwealthOfNations -> when (language) {
      AppLanguage.English -> "Commonwealth flags"
      AppLanguage.Bulgarian -> "Флагове на Британската общност"
      AppLanguage.German -> "Commonwealth-Flaggen"
    }
    CreateQuizPreset.AfricanUnion -> when (language) {
      AppLanguage.English -> "African Union flags"
      AppLanguage.Bulgarian -> "Флагове на Африканския съюз"
      AppLanguage.German -> "Flaggen der Afrikanischen Union"
    }
    CreateQuizPreset.OrganisationOfIslamicCooperation -> when (language) {
      AppLanguage.English -> "OIC flags"
      AppLanguage.Bulgarian -> "Флагове на ОИС"
      AppLanguage.German -> "OIC-Flaggen"
    }
  }

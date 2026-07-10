package com.example.flaggameandroid.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.widget.Toast
import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.CreateQuizSource
import com.example.flaggameandroid.core.model.GameMode
import com.example.flaggameandroid.core.model.HintDifficulty
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.ProgressionRules
import com.example.flaggameandroid.core.model.QuizTopic
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
  onQuizTopicSelected: (QuizTopic) -> Unit,
  onContinentToggle: (String) -> Unit,
  onCreateQuizTrainingToggled: () -> Unit,
  onCreateQuizLocalMultiplayerToggled: () -> Unit,
  onCreateQuizManualHardcoreToggled: () -> Unit,
  onCreateQuizManualTimerToggled: () -> Unit,
  onQuestionCountChange: (String) -> Unit,
  onSpeedRunSecondsChange: (String) -> Unit,
  onSurpriseMe: () -> Unit,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
  onCreateQuizSourceSelected: (CreateQuizSource) -> Unit,
  onCreateQuizPresetSelected: (CreateQuizPreset) -> Unit,
  onCreateQuizContinentToggled: (String) -> Unit,
  onCreateQuizCountryToggled: (String) -> Unit,
  onCreateQuizCapitalToggled: (String) -> Unit,
  onCreateQuizCountryBulkToggled: (Set<String>) -> Unit,
  onCreateQuizCapitalBulkToggled: (Set<String>) -> Unit,
  onCreateQuizAllCountriesToggled: () -> Unit,
  onSaveCreateQuizClicked: (String, String?) -> FlagGameViewModel.SaveQuizResult,
  onRemoveSavedQuizTemplate: (String) -> Unit,
  onStartQuiz: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val darkTheme = isSystemInDarkTheme()
  var showSaveDialog by remember { mutableStateOf(false) }
  var saveQuizName by remember { mutableStateOf("") }
  var saveFeedbackMessage by remember { mutableStateOf<String?>(null) }
  var replaceConflict by remember { mutableStateOf<FlagGameViewModel.SaveQuizResult.NameConflict?>(null) }
  var capacityConflict by remember { mutableStateOf<FlagGameViewModel.SaveQuizResult.CapacityConflict?>(null) }
  var savedCreateQuizTemplateId by remember { mutableStateOf<String?>(null) }
  var savedCreateQuizSignature by remember { mutableStateOf<String?>(null) }
  var removeSavedCreateQuizDialogVisible by remember { mutableStateOf(false) }
  var showInstantCorrectionInfo by remember { mutableStateOf(false) }
  var showCreateQuizTrainingInfo by remember { mutableStateOf(false) }
  var showCreateQuizLocalMultiplayerInfo by remember { mutableStateOf(false) }
  var showCreateQuizHardcoreInfo by remember { mutableStateOf(false) }
  var questionVariantsExpanded by remember { mutableStateOf(false) }
  var createQuizPlayersExpanded by remember { mutableStateOf(false) }
  var displayedCreateQuizSource by remember(setup.mode) { mutableStateOf(setup.createQuizSource) }
  val manualCountryContinentExpanded = remember { mutableStateMapOf<String, Boolean>() }
  fun showSaveFeedback(message: String) {
    if (saveFeedbackMessage == null) {
      saveFeedbackMessage = message
    }
  }
  val context = LocalContext.current
  val createQuizPresetOrder =
    remember(setup.topic) {
      createQuizPresetOrderFor(setup.topic)
    }
  val countriesByContinent =
    remember(countries, language) {
      countries
        .groupBy { it.continent }
        .mapValues { (_, continentCountries) -> continentCountries.sortedBy { it.localizedName(language) } }
    }
  val activeCreateQuizSource =
    if (setup.mode == GameMode.CreateQuiz) displayedCreateQuizSource else setup.createQuizSource
  val scrollState = rememberScrollState()
  val density = LocalDensity.current
  var createQuizHeaderHeightPx by remember { mutableStateOf(0f) }
  val headerQuestionCountLabel =
    when {
      setup.surpriseMe -> "***"
      setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizManualHardcore && setup.topic == QuizTopic.Mixed -> "390"
      setup.mode == GameMode.CreateQuiz && setup.usesCreateQuizManualHardcore -> "195"
      setup.mode == GameMode.CreateQuiz &&
        activeCreateQuizSource == CreateQuizSource.ManualCountriesCapitals &&
        setup.topic == QuizTopic.Mixed -> setup.createQuizMixedSelectionCount.toString()
      setup.mode == GameMode.CreateQuiz &&
        activeCreateQuizSource == CreateQuizSource.ManualCountriesCapitals -> setup.selectedCountryCodes.size.toString()
      else -> setup.questionCountInput.ifBlank { "10" }
    }
  val headerTimerLabel =
    if (setup.createQuizManualTimerEnabled) {
      setup.speedRunSecondsPerAnswer?.coerceIn(1, 60)?.toString() ?: "5"
    } else {
      null
    }

  fun currentCreateQuizSignature(): String =
    listOf(
      setup.topic.name,
      displayedCreateQuizSource.name,
      setup.createQuizPresets.map { it.name }.sorted().joinToString(","),
      setup.selectedCountryCodes.sorted().joinToString(","),
      setup.selectedCapitalCountryCodes.sorted().joinToString(","),
      setup.variants.map { it.name }.sorted().joinToString(","),
      setup.questionCountInput,
      setup.surpriseMe.toString(),
      setup.createQuizTrainingEnabled.toString(),
      setup.createQuizManualHardcoreEnabled.toString(),
      setup.createQuizLocalMultiplayerEnabled.toString(),
      setup.createQuizManualTimerEnabled.toString(),
      setup.speedRunSecondsPerAnswerInput,
      setup.instantCorrectionEnabled.toString(),
      setup.playerNames.joinToString("|"),
    ).joinToString("::")

  LaunchedEffect(saveFeedbackMessage) {
    if (saveFeedbackMessage != null) {
      Toast.makeText(context, saveFeedbackMessage!!, Toast.LENGTH_LONG).show()
      delay(5_000)
      saveFeedbackMessage = null
    }
  }

  LaunchedEffect(setup.createQuizSource, setup.mode) {
    displayedCreateQuizSource = setup.createQuizSource
  }

  LaunchedEffect(setup.savedQuizTemplateId) {
    if (setup.savedQuizTemplateId != null) {
      savedCreateQuizTemplateId = setup.savedQuizTemplateId
      savedCreateQuizSignature = currentCreateQuizSignature()
    }
  }

  LaunchedEffect(
    setup.topic,
    setup.createQuizSource,
    setup.createQuizPresets,
    setup.selectedCountryCodes,
    setup.selectedCapitalCountryCodes,
    setup.variants,
    setup.questionCountInput,
    setup.surpriseMe,
    setup.createQuizTrainingEnabled,
    setup.createQuizManualHardcoreEnabled,
    setup.createQuizLocalMultiplayerEnabled,
    setup.createQuizManualTimerEnabled,
    setup.speedRunSecondsPerAnswerInput,
    setup.instantCorrectionEnabled,
    setup.playerNames,
  ) {
    val currentSignature = currentCreateQuizSignature()
    if (savedCreateQuizSignature != null && currentSignature != savedCreateQuizSignature) {
      savedCreateQuizTemplateId = null
    }
  }

  fun closeSetupInfoPanels() {
    showInstantCorrectionInfo = false
    showCreateQuizTrainingInfo = false
    showCreateQuizLocalMultiplayerInfo = false
    showCreateQuizHardcoreInfo = false
  }

  fun openSaveCreateQuizDialog() {
    saveQuizName =
      when (language) {
        AppLanguage.English -> "Saved quiz"
        AppLanguage.Bulgarian -> "Запазен тест"
        AppLanguage.German -> "Gespeichertes Quiz"
      }
    showSaveDialog = true
  }

  ScreenShell(
    modifier = modifier,
    scrollState = scrollState,
    overlay = {
      if (setup.mode == GameMode.CreateQuiz) {
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .align(Alignment.TopStart)
              .zIndex(2f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CreateQuizPinnedHeader(
            language = language,
            darkTheme = darkTheme,
            questionCountLabel = headerQuestionCountLabel,
            timerLabel = headerTimerLabel,
            saveEnabled = !setup.usesCreateQuizTraining && !setup.usesCreateQuizManualHardcore,
            saved = savedCreateQuizTemplateId != null,
            onSaveClick = ::openSaveCreateQuizDialog,
            onRemoveSavedClick = { removeSavedCreateQuizDialogVisible = true },
            onStartQuiz = onStartQuiz,
            onHeightMeasured = { createQuizHeaderHeightPx = it },
          )
        }
      }
    },
  ) {
    if (setup.mode != GameMode.CreateQuiz) {
      HeaderRow(title = cleanModeTitle(setup.mode, language))
    }

    if (setup.mode == GameMode.CreateQuiz) {
      Spacer(
        modifier =
          Modifier.height(
            with(density) {
              createQuizHeaderHeightPx.toDp() + 8.dp
            },
          ),
      )
    }

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

    if (setup.mode == GameMode.CreateQuiz || setup.mode == GameMode.MistakeReview) {
      QuizTopicToggleSection(
        language = language,
        selectedTopic = setup.topic,
        onTopicSelected = onQuizTopicSelected,
      )
    }

    val isMistakeReview = setup.mode == GameMode.MistakeReview
    val isCreateQuizTraining = setup.usesCreateQuizTraining
    val isCreateQuizManual = setup.mode == GameMode.CreateQuiz && setup.createQuizSource == CreateQuizSource.ManualCountriesCapitals
    val isCreateQuizManualHardcore = setup.usesCreateQuizManualHardcore
    val isCreateQuizMixed = setup.topic == QuizTopic.Mixed
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
      val timerNeedsMinimumHint =
        setup.createQuizManualTimerEnabled &&
          (setup.speedRunSecondsPerAnswerInput.isBlank() ||
            setup.speedRunSecondsPerAnswerInput.toIntOrNull()?.let { it < 1 } == true)
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
            if (timerNeedsMinimumHint) {
              Text(
                when (language) {
                  AppLanguage.English -> "Timer must be at least 1 second."
                  AppLanguage.Bulgarian -> "Таймерът трябва да е поне 1 секунда."
                  AppLanguage.German -> "Der Timer muss mindestens 1 Sekunde betragen."
                },
                color = AccentRed,
              )
            }
            Text(
              when (language) {
                AppLanguage.English -> "Bonus for 1-second game: +5 seconds only for quizzes with 10 or more questions."
                AppLanguage.Bulgarian -> "Бонус за игра с 1 секунда: +5 секунди само при тестове с 10 или повече въпроса."
                AppLanguage.German -> "Bonus fürs 1-Sekunden-Spiel: +5 Sekunden nur bei Quiz mit 10 oder mehr Fragen."
              },
            )
          }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    val renderChooseCountriesSection: @Composable () -> Unit = {
      val manualSelectAllLabel =
        when (language) {
          AppLanguage.English -> if (setup.createQuizMixedSelectionCount == countries.size * 2) "Deselect all" else "Select all"
          AppLanguage.Bulgarian -> if (setup.createQuizMixedSelectionCount == countries.size * 2) "Махни всички" else "Избери всички"
          AppLanguage.German -> if (setup.createQuizMixedSelectionCount == countries.size * 2) "Alle abwählen" else "Alle wählen"
        }
      SectionCard(
        title =
          when (language) {
            AppLanguage.English -> "Pick manually"
            AppLanguage.Bulgarian -> "Избери ръчно"
            AppLanguage.German -> "Selber auswählen"
          },
        headerAction = {
          OutlinedButton(
            onClick = onCreateQuizAllCountriesToggled,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          ) {
            Text(manualSelectAllLabel)
          }
        },
      ) {
        countriesByContinent.forEach { (continent, list) ->
          val continentCodes = list.map { it.code }.toSet()
          val continentSelectedCountries =
            continentCodes.isNotEmpty() && continentCodes.all { it in setup.selectedCountryCodes }
          val continentSelectedCapitals =
            continentCodes.isNotEmpty() && continentCodes.all { it in setup.selectedCapitalCountryCodes }
          val continentExpanded = manualCountryContinentExpanded[continent] ?: false
          val continentFullySelected =
            if (isCreateQuizMixed) {
              continentSelectedCountries && continentSelectedCapitals
            } else {
              continentSelectedCountries
            }
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
                    AppLanguage.English -> if (continentFullySelected) "Deselect all" else "Select all"
                    AppLanguage.Bulgarian -> if (continentFullySelected) "Махни всички" else "Избери всички"
                    AppLanguage.German -> if (continentFullySelected) "Alle abwählen" else "Alle wählen"
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
            if (isCreateQuizMixed) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                  ) {
                    val countryColumnFullySelected = continentCodes.all { it in setup.selectedCountryCodes }
                    OutlinedButton(
                      modifier = Modifier.fillMaxWidth(),
                      onClick = {
                        onCreateQuizCountryBulkToggled(continentCodes)
                      },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                      Text(
                        when (language) {
                          AppLanguage.English -> if (countryColumnFullySelected) "Deselect all" else "Select all"
                          AppLanguage.Bulgarian -> if (countryColumnFullySelected) "Махни всички" else "Избери всички"
                          AppLanguage.German -> if (countryColumnFullySelected) "Alle abwählen" else "Alle wählen"
                        },
                      )
                    }
                  }
                  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    list.forEach { country ->
                      FilterChip(
                        selected = country.code in setup.selectedCountryCodes,
                        onClick = { onCreateQuizCountryToggled(country.code) },
                        label = { Text("${country.emoji} ${country.localizedName(language)}") },
                      )
                    }
                  }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                  ) {
                    val capitalColumnFullySelected = continentCodes.all { it in setup.selectedCapitalCountryCodes }
                    OutlinedButton(
                      modifier = Modifier.fillMaxWidth(),
                      onClick = {
                        onCreateQuizCapitalBulkToggled(continentCodes)
                      },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                      Text(
                        when (language) {
                          AppLanguage.English -> if (capitalColumnFullySelected) "Deselect all" else "Select all"
                          AppLanguage.Bulgarian -> if (capitalColumnFullySelected) "Махни всички" else "Избери всички"
                          AppLanguage.German -> if (capitalColumnFullySelected) "Alle abwählen" else "Alle wählen"
                        },
                      )
                    }
                  }
                  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    list.forEach { country ->
                      FilterChip(
                        selected = country.code in setup.selectedCapitalCountryCodes,
                        onClick = { onCreateQuizCapitalToggled(country.code) },
                        label = { Text("${country.emoji} ${country.localizedCapital(language)}") },
                      )
                    }
                  }
                }
              }
            } else {
              FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                list.forEach { country ->
                  val labelText =
                    when (setup.topic) {
                      QuizTopic.Capitals ->
                        "${country.emoji} ${country.localizedCapital(language)}"
                      else ->
                        "${country.emoji} ${country.localizedName(language)}"
                    }
                  FilterChip(
                    selected = country.code in setup.selectedCountryCodes,
                    onClick = { onCreateQuizCountryToggled(country.code) },
                    label = { Text(labelText) },
                  )
                }
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
          val rangeTextAsLabel = setup.mode == GameMode.CreateQuiz && activeCreateQuizSource == CreateQuizSource.PresetFilter
          val useCreateQuizQuestionCountLayout = setup.mode == GameMode.CreateQuiz && activeCreateQuizSource == CreateQuizSource.PresetFilter
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
      CompactToggleInfoCard(
        title =
          when (language) {
            AppLanguage.English -> "Training"
            AppLanguage.Bulgarian -> "Тренировка"
            AppLanguage.German -> "Training"
          },
        checked = setup.createQuizTrainingEnabled,
        enabled = !setup.usesCreateQuizManualHardcore && !setup.usesCreateQuizLocalMultiplayer,
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

      CompactToggleInfoCard(
          title =
            when (language) {
              AppLanguage.English -> "Hardcore"
              AppLanguage.Bulgarian -> "Хардкор"
              AppLanguage.German -> "Hardcore"
            },
          checked = setup.createQuizManualHardcoreEnabled,
          enabled = !setup.usesCreateQuizTraining && !setup.usesCreateQuizLocalMultiplayer,
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

      CompactToggleInfoCard(
        title =
          when (language) {
            AppLanguage.English -> "Local Multiplayer"
            AppLanguage.Bulgarian -> "Локален мултиплейър"
            AppLanguage.German -> "Lokaler Mehrspieler"
          },
        checked = setup.createQuizLocalMultiplayerEnabled,
        enabled = !setup.usesCreateQuizTraining && !setup.usesCreateQuizManualHardcore,
        onCheckedChange = { onCreateQuizLocalMultiplayerToggled() },
        infoExpanded = showCreateQuizLocalMultiplayerInfo,
        onInfoClick = {
          val next = !showCreateQuizLocalMultiplayerInfo
          closeSetupInfoPanels()
          showCreateQuizLocalMultiplayerInfo = next
        },
        infoText =
          when (language) {
            AppLanguage.English ->
              "Play the same custom quiz on one device with 2 to 5 players. Players answer in turns and use the exact preset or manually selected countries from this setup."
            AppLanguage.Bulgarian ->
              "Играй същия персонален тест на едно устройство с 2 до 5 играчи. Играчите отговарят на ходове и ползват същите филтри или ръчно избрани държави от тази настройка."
            AppLanguage.German ->
              "Spiele dasselbe benutzerdefinierte Quiz auf einem Gerät mit 2 bis 5 Spielern. Die Spieler antworten nacheinander und verwenden dieselben Filter oder manuell gewählten Länder aus dieser Einrichtung."
          },
      )

      if (setup.usesCreateQuizLocalMultiplayer) {
        CollapsiblePlayersSection(
          language = language,
          playerNames = setup.playerNames,
          expanded = createQuizPlayersExpanded,
          onExpandedChange = { createQuizPlayersExpanded = !createQuizPlayersExpanded },
          onPlayerNameChanged = onPlayerNameChanged,
          onAddPlayer = onAddPlayer,
          onRemovePlayer = onRemovePlayer,
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

      val displayedQuestionCount =
        when {
          isCreateQuizManual && isCreateQuizMixed ->
            setup.createQuizMixedSelectionCount.toString()
          isCreateQuizManual ->
            setup.selectedCountryCodes.size.toString()
          else -> setup.questionCountInput
        }
      renderQuestionCountCard(
        displayedQuestionCount,
        !setup.surpriseMe && !isCreateQuizManual,
        !setup.usesCreateQuizTraining && activeCreateQuizSource == CreateQuizSource.PresetFilter,
        when (language) {
          AppLanguage.English -> if (setup.surpriseMe) "Manual count" else "Randomizer"
          AppLanguage.Bulgarian -> if (setup.surpriseMe) "Manual count" else "Randomizer"
          AppLanguage.German -> if (setup.surpriseMe) "Manual count" else "Randomizer"
        },
        onSurpriseMe,
        questionCountChangeHandler,
        if (activeCreateQuizSource == CreateQuizSource.PresetFilter) {
          when (language) {
            AppLanguage.English -> if (setup.usesCreateQuizTraining) "Range: 1-999" else "Range: 1-$questionCountLimit"
            AppLanguage.Bulgarian -> if (setup.usesCreateQuizTraining) "Диапазон: 1-999" else "Диапазон: 1-$questionCountLimit"
            AppLanguage.German -> if (setup.usesCreateQuizTraining) "Bereich: 1-999" else "Bereich: 1-$questionCountLimit"
          }
        } else {
          null
        },
        if (activeCreateQuizSource == CreateQuizSource.PresetFilter) {
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
          }
        } else {
          null
        },
        questionCountOverLimit,
      )

      QuestionVariantsSection(
        language = language,
        topic = setup.topic,
        selectedVariants = setup.variants,
        expanded = questionVariantsExpanded,
        onExpandedChange = { questionVariantsExpanded = !questionVariantsExpanded },
        onVariantToggle = onVariantToggle,
      )

      if (!setup.usesCreateQuizTraining && !setup.usesCreateQuizManualHardcore) {
        if (setup.topic != QuizTopic.Mixed) {
          SectionCard(title = when (language) {
            AppLanguage.English -> "Custom Quiz"
            AppLanguage.Bulgarian -> "Персонален тест"
            AppLanguage.German -> "Benutzerdefiniertes Quiz"
          }) {
            SelectableRow(
              title = when (language) {
                AppLanguage.English -> "Preset filter"
                AppLanguage.Bulgarian -> "Готов филтър"
                AppLanguage.German -> "Vorlagenfilter"
              },
              selected = activeCreateQuizSource == CreateQuizSource.PresetFilter,
              enabled = setup.topic != QuizTopic.Mixed,
              onClick = {
                displayedCreateQuizSource = CreateQuizSource.PresetFilter
                onCreateQuizSourceSelected(CreateQuizSource.PresetFilter)
              },
              description = when (language) {
                AppLanguage.English -> "Use predefined flag rules."
                AppLanguage.Bulgarian -> "Ползвай готови правила за флагове."
                AppLanguage.German -> "Nutze vordefinierte Flaggenregeln."
              },
            )
            SelectableRow(
              title = when (language) {
                AppLanguage.English -> "Manual countries/capitals"
                AppLanguage.Bulgarian -> "Ръчно избрани държави/градове"
                AppLanguage.German -> "Manuelle Länder/Städte"
              },
              selected = activeCreateQuizSource == CreateQuizSource.ManualCountriesCapitals,
              onClick = {
                displayedCreateQuizSource = CreateQuizSource.ManualCountriesCapitals
                onCreateQuizSourceSelected(CreateQuizSource.ManualCountriesCapitals)
              },
              description = when (language) {
                AppLanguage.English -> "Pick the exact ones yourself."
                AppLanguage.Bulgarian -> "Избери точните сам."
                AppLanguage.German -> "Wähle selbst aus."
              },
            )
          }
        }

        if (setup.topic == QuizTopic.Mixed || activeCreateQuizSource == CreateQuizSource.ManualCountriesCapitals) {
          renderChooseCountriesSection()
        } else {
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
                      text = localizedCreateQuizPresetTitle(preset, language, setup.topic),
                      maxLines = 1,
                      style = MaterialTheme.typography.bodyMedium,
                    )
                  },
                )
              }
            }
          }
        }
      }
    }

    if (setup.mode != GameMode.CreateQuiz) {
      renderQuestionCountCard(
        when {
          isMistakeReview -> questionCountLimit.toString()
          else -> setup.questionCountInput
        },
        !setup.surpriseMe && !isMistakeReview,
        !isMistakeReview,
        when (language) {
          AppLanguage.English -> if (setup.surpriseMe) "Manual count" else "Randomizer"
          AppLanguage.Bulgarian -> if (setup.surpriseMe) "Manual count" else "Randomizer"
          AppLanguage.German -> if (setup.surpriseMe) "Manual count" else "Randomizer"
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
          topic = setup.topic,
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

    if (setup.mode != GameMode.CreateQuiz) {
      Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Text(
          when (language) {
            AppLanguage.English -> "Start quiz"
            AppLanguage.Bulgarian -> "Започни теста"
            AppLanguage.German -> "Quiz starten"
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
            onValueChange = { saveQuizName = it.take(15) },
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
                  AppLanguage.English -> "Up to 15 characters."
                  AppLanguage.Bulgarian -> "До 15 знака."
                  AppLanguage.German -> "Bis zu 15 Zeichen."
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
                  savedCreateQuizTemplateId = result.templateId
                  savedCreateQuizSignature = currentCreateQuizSignature()
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
                  savedCreateQuizTemplateId = result.existingTemplateId
                  savedCreateQuizSignature = currentCreateQuizSignature()
                  showSaveDialog = false
                }
                is FlagGameViewModel.SaveQuizResult.NameConflict -> {
                  replaceConflict = result
                  showSaveDialog = false
                }

                is FlagGameViewModel.SaveQuizResult.CapacityConflict -> {
                  capacityConflict = result
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

    if (removeSavedCreateQuizDialogVisible) {
      AlertDialog(
        onDismissRequest = { removeSavedCreateQuizDialogVisible = false },
        title = {
          Text(
            when (language) {
              AppLanguage.English -> "Delete saved quiz?"
              AppLanguage.Bulgarian -> "Да изтрия ли запазения тест?"
              AppLanguage.German -> "Gespeichertes Quiz löschen?"
            },
          )
        },
        text = {
          Text(
            when (language) {
              AppLanguage.English -> "Do you really want to delete this saved quiz?"
              AppLanguage.Bulgarian -> "Наистина ли искаш да изтриеш този запазен тест?"
              AppLanguage.German -> "Möchtest du dieses gespeicherte Quiz wirklich löschen?"
            },
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              savedCreateQuizTemplateId?.let { templateId ->
                onRemoveSavedQuizTemplate(templateId)
                savedCreateQuizTemplateId = null
                savedCreateQuizSignature = null
                Toast
                  .makeText(
                    context,
                    when (language) {
                      AppLanguage.English -> "Saved quiz deleted."
                      AppLanguage.Bulgarian -> "Запазеният тест е изтрит."
                      AppLanguage.German -> "Gespeichertes Quiz gelöscht."
                    },
                    Toast.LENGTH_LONG,
                  )
                  .show()
              }
              removeSavedCreateQuizDialogVisible = false
            },
          ) {
            Text(
              when (language) {
                AppLanguage.English -> "Yes"
                AppLanguage.Bulgarian -> "Да"
                AppLanguage.German -> "Ja"
              },
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { removeSavedCreateQuizDialogVisible = false }) {
            Text(
              when (language) {
                AppLanguage.English -> "No"
                AppLanguage.Bulgarian -> "Не"
                AppLanguage.German -> "Nein"
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
                is FlagGameViewModel.SaveQuizResult.Saved -> {
                  showSaveFeedback(result.message)
                  savedCreateQuizTemplateId = result.templateId
                  savedCreateQuizSignature = currentCreateQuizSignature()
                }
                is FlagGameViewModel.SaveQuizResult.DuplicateConfiguration ->
                  showSaveFeedback(
                    when (language) {
                      AppLanguage.English -> "That exact quiz is already saved as \"${result.existingName}\"."
                      AppLanguage.Bulgarian -> "Същият тест вече е записан като \"${result.existingName}\"."
                      AppLanguage.German -> "Dasselbe Quiz ist bereits als \"${result.existingName}\" gespeichert."
                    }
                  ).also {
                    savedCreateQuizTemplateId = result.existingTemplateId
                    savedCreateQuizSignature = currentCreateQuizSignature()
                  }
                is FlagGameViewModel.SaveQuizResult.NameConflict,
                is FlagGameViewModel.SaveQuizResult.CapacityConflict,
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

    capacityConflict?.let { conflict ->
      AlertDialog(
        onDismissRequest = { capacityConflict = null },
        title = {
          Text(
            when (language) {
              AppLanguage.English -> "Saved quiz limit reached"
              AppLanguage.Bulgarian -> "Лимитът за запазени тестове е достигнат"
              AppLanguage.German -> "Limit für gespeicherte Quiz erreicht"
            },
          )
        },
        text = {
          Text(
            when (language) {
              AppLanguage.English -> "You already have 10 saved quizzes. Replace \"${conflict.replaceTemplateName}\" with this new quiz?"
              AppLanguage.Bulgarian -> "Вече имаш 10 запазени теста. Да заменя ли \"${conflict.replaceTemplateName}\" с този нов тест?"
              AppLanguage.German -> "Du hast bereits 10 gespeicherte Quiz. Soll \"${conflict.replaceTemplateName}\" durch dieses neue Quiz ersetzt werden?"
            },
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              when (val result = onSaveCreateQuizClicked(saveQuizName, conflict.replaceTemplateId)) {
                is FlagGameViewModel.SaveQuizResult.Saved -> {
                  showSaveFeedback(result.message)
                  savedCreateQuizTemplateId = result.templateId
                  savedCreateQuizSignature = currentCreateQuizSignature()
                }
                is FlagGameViewModel.SaveQuizResult.DuplicateConfiguration ->
                  showSaveFeedback(
                    when (language) {
                      AppLanguage.English -> "That exact quiz is already saved as \"${result.existingName}\"."
                      AppLanguage.Bulgarian -> "Същият тест вече е записан като \"${result.existingName}\"."
                      AppLanguage.German -> "Dasselbe Quiz ist bereits als \"${result.existingName}\" gespeichert."
                    }
                  ).also {
                    savedCreateQuizTemplateId = result.existingTemplateId
                    savedCreateQuizSignature = currentCreateQuizSignature()
                  }
                is FlagGameViewModel.SaveQuizResult.NameConflict -> replaceConflict = result
                is FlagGameViewModel.SaveQuizResult.CapacityConflict,
                FlagGameViewModel.SaveQuizResult.NoOp -> Unit
              }
              capacityConflict = null
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
          TextButton(onClick = { capacityConflict = null }) {
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

  }

@Composable
private fun PlayersSection(
  language: AppLanguage,
  playerNames: List<String>,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
) {
  SectionCard(
    title =
      when (language) {
        AppLanguage.English -> "Players"
        AppLanguage.Bulgarian -> "Играчите"
        AppLanguage.German -> "Spieler"
      },
  ) {
    PlayerFieldsContent(
      language = language,
      playerNames = playerNames,
      onPlayerNameChanged = onPlayerNameChanged,
      onAddPlayer = onAddPlayer,
      onRemovePlayer = onRemovePlayer,
    )
  }
}

@Composable
private fun CollapsiblePlayersSection(
  language: AppLanguage,
  playerNames: List<String>,
  expanded: Boolean,
  onExpandedChange: () -> Unit,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
) {
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
          text =
            when (language) {
              AppLanguage.English -> "Players"
              AppLanguage.Bulgarian -> "Играчите"
              AppLanguage.German -> "Spieler"
            },
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
        PlayerFieldsContent(
          language = language,
          playerNames = playerNames,
          onPlayerNameChanged = onPlayerNameChanged,
          onAddPlayer = onAddPlayer,
          onRemovePlayer = onRemovePlayer,
        )
      }
    }
  }
}

@Composable
private fun PlayerFieldsContent(
  language: AppLanguage,
  playerNames: List<String>,
  onPlayerNameChanged: (Int, String) -> Unit,
  onAddPlayer: () -> Unit,
  onRemovePlayer: () -> Unit,
) {
  playerNames.forEachIndexed { index, name ->
    OutlinedTextField(
      value = name,
      onValueChange = { onPlayerNameChanged(index, it.take(15)) },
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
  val playerCount = playerNames.size
  Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
    OutlinedButton(
      onClick = onRemovePlayer,
      enabled = playerCount > 2,
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
      enabled = playerCount < 5,
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
  enabled: Boolean = true,
  onCheckedChange: () -> Unit,
  infoExpanded: Boolean,
  onInfoClick: () -> Unit,
  infoText: String,
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier =
        Modifier
          .alpha(if (enabled) 1f else 0.55f)
          .padding(horizontal = 16.dp, vertical = 10.dp),
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
          enabled = enabled,
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
private fun QuizTopicToggleSection(
  language: AppLanguage,
  selectedTopic: QuizTopic,
  onTopicSelected: (QuizTopic) -> Unit,
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = localizedQuizTopicTitle(language),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QuizTopic.entries.forEach { topic ->
          val selected = selectedTopic == topic
          val label = localizedQuizTopicLabel(topic, language)
          if (selected) {
            Button(
              onClick = { onTopicSelected(topic) },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Text(text = label, maxLines = 1, fontSize = 13.sp)
            }
          } else {
            OutlinedButton(
              onClick = { onTopicSelected(topic) },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Text(text = label, maxLines = 1, fontSize = 13.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun QuestionVariantsSection(
  language: AppLanguage,
  topic: com.example.flaggameandroid.core.model.QuizTopic,
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
          title = localizedVariantTitle(variant, language, topic),
          description = localizedVariantDescription(variant, language, topic),
          checked = variant in selectedVariants,
          onClick = { onVariantToggle(variant) },
        )
      }
    }
    }
  }
}

private fun localizedCreateQuizHeaderTitle(
  language: AppLanguage,
): String =
  when (language) {
    AppLanguage.English -> "Custom quiz"
    AppLanguage.Bulgarian -> "Персонален тест"
    AppLanguage.German -> "Benutzerdefiniertes Quiz"
  }

private fun localizedCreateQuizQuestionCountLine(
  language: AppLanguage,
  questionCountLabel: String,
  timerLabel: String? = null,
): String =
  when (language) {
    AppLanguage.English ->
      if (timerLabel == null) {
        "($questionCountLabel questions)"
      } else {
        "($questionCountLabel questions, $timerLabel seconds)"
      }
    AppLanguage.Bulgarian ->
      if (timerLabel == null) {
        "($questionCountLabel въпроса)"
      } else {
        "($questionCountLabel въпроса, $timerLabel секунди)"
      }
    AppLanguage.German ->
      if (timerLabel == null) {
        "($questionCountLabel Fragen)"
      } else {
        "($questionCountLabel Fragen, $timerLabel Sekunden)"
      }
  }

@Composable
private fun CreateQuizPinnedHeader(
  language: AppLanguage,
  darkTheme: Boolean,
  questionCountLabel: String,
  timerLabel: String?,
  saveEnabled: Boolean,
  saved: Boolean,
  onSaveClick: () -> Unit,
  onRemoveSavedClick: () -> Unit,
  onStartQuiz: () -> Unit,
  onHeightMeasured: (Float) -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    shape = androidx.compose.ui.graphics.RectangleShape,
    modifier =
      Modifier
        .fillMaxWidth()
        .onGloballyPositioned { coordinates -> onHeightMeasured(coordinates.size.height.toFloat()) },
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.weight(0.18f),
        contentAlignment = Alignment.CenterEnd,
      ) {
        if (saveEnabled) {
          TextButton(
            onClick = if (saved) onRemoveSavedClick else onSaveClick,
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
          ) {
            Text(
            text = if (saved) "★" else "☆",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
          )
          }
        }
      }
      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
      ) {
        Text(
          text = localizedCreateQuizHeaderTitle(language),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
        )
        Text(
          text = localizedCreateQuizQuestionCountLine(language, questionCountLabel, timerLabel),
          style = MaterialTheme.typography.labelMedium,
          color = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
        )
      }
      Box(
        modifier = Modifier.weight(0.24f),
        contentAlignment = Alignment.CenterStart,
      ) {
        Button(
          onClick = onStartQuiz,
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp),
        ) {
          Text(
            when (language) {
              AppLanguage.English -> "Start"
              AppLanguage.Bulgarian -> "Старт"
              AppLanguage.German -> "Start"
            },
            maxLines = 1,
          )
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

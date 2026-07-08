package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.CreateQuizPreset
import com.example.flaggameandroid.core.model.FlagCountry
import com.example.flaggameandroid.core.model.QuizTopic
import com.example.flaggameandroid.core.model.capitalQuizMetadata

internal fun createQuizPresetOrderFor(topic: QuizTopic): List<CreateQuizPreset> =
  when (topic) {
    QuizTopic.Capitals -> capitalCreateQuizPresetOrder
    QuizTopic.Countries,
    QuizTopic.Mixed -> countryCreateQuizPresetOrder
  }

internal fun createQuizDefaultPresetsForTopic(topic: QuizTopic): Set<CreateQuizPreset> =
  when (topic) {
    QuizTopic.Capitals ->
      setOf(
        CreateQuizPreset.CapitalPopulationUnderQuarterMillion,
        CreateQuizPreset.CapitalPopulationQuarterToOneMillion,
        CreateQuizPreset.CapitalPopulationOneToTwoPointFiveMillion,
        CreateQuizPreset.CapitalPopulationOverTwoPointFiveMillion,
      )
    QuizTopic.Countries,
    QuizTopic.Mixed ->
      setOf(
        CreateQuizPreset.TwoColors,
        CreateQuizPreset.ThreeColors,
        CreateQuizPreset.FourPlusColors,
      )
  }

internal fun localizedCreateQuizPresetTitle(
  preset: CreateQuizPreset,
  language: AppLanguage,
  topic: QuizTopic,
): String =
  when (topic) {
    QuizTopic.Capitals -> localizedCapitalCreateQuizPresetTitle(preset, language)
    QuizTopic.Countries,
    QuizTopic.Mixed -> localizedCountryCreateQuizPresetTitle(preset, language)
  }

private fun localizedCapitalCreateQuizPresetTitle(
  preset: CreateQuizPreset,
  language: AppLanguage,
): String = localizedCountryCreateQuizPresetTitle(preset, language)

internal fun matchesCreateQuizPreset(
  country: FlagCountry,
  preset: CreateQuizPreset,
  topic: QuizTopic,
): Boolean =
  when (topic) {
    QuizTopic.Capitals -> matchesCapitalCreateQuizPreset(country, preset)
    QuizTopic.Countries,
    QuizTopic.Mixed -> matchesCountryCreateQuizPreset(country, preset)
  }

private val countryCreateQuizPresetOrder =
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

private val capitalCreateQuizPresetOrder =
  listOf(
    CreateQuizPreset.CapitalPopulationUnderQuarterMillion,
    CreateQuizPreset.CapitalPopulationQuarterToOneMillion,
    CreateQuizPreset.CapitalPopulationOneToTwoPointFiveMillion,
    CreateQuizPreset.CapitalPopulationOverTwoPointFiveMillion,
    CreateQuizPreset.CapitalAreaUnderFiftySquareKm,
    CreateQuizPreset.CapitalAreaFiftyToThreeHundredSquareKm,
    CreateQuizPreset.CapitalAreaThreeHundredToEightHundredSquareKm,
    CreateQuizPreset.CapitalAreaOverEightHundredSquareKm,
    CreateQuizPreset.CapitalNotCoastal,
  )

private fun localizedCountryCreateQuizPresetTitle(
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
    CreateQuizPreset.CapitalPopulationUnderQuarterMillion -> when (language) {
      AppLanguage.English -> "Population < 250k"
      AppLanguage.Bulgarian -> "Население < 250k"
      AppLanguage.German -> "Bevölkerung < 250k"
    }
    CreateQuizPreset.CapitalPopulationQuarterToOneMillion -> when (language) {
      AppLanguage.English -> "Population 250k-1M"
      AppLanguage.Bulgarian -> "Население 250k-1M"
      AppLanguage.German -> "Bevölkerung 250k-1 Mio."
    }
    CreateQuizPreset.CapitalPopulationOneToTwoPointFiveMillion -> when (language) {
      AppLanguage.English -> "Population 1M-2.5M"
      AppLanguage.Bulgarian -> "Население 1M-2.5M"
      AppLanguage.German -> "Bevölkerung 1-2,5 Mio."
    }
    CreateQuizPreset.CapitalPopulationOverTwoPointFiveMillion -> when (language) {
      AppLanguage.English -> "Population > 2.5M"
      AppLanguage.Bulgarian -> "Население > 2.5M"
      AppLanguage.German -> "Bevölkerung > 2,5 Mio."
    }
    CreateQuizPreset.CapitalAreaUnderFiftySquareKm -> when (language) {
      AppLanguage.English -> "Area < 50 km²"
      AppLanguage.Bulgarian -> "Площ < 50 km²"
      AppLanguage.German -> "Fläche < 50 km²"
    }
    CreateQuizPreset.CapitalAreaFiftyToThreeHundredSquareKm -> when (language) {
      AppLanguage.English -> "Area 50-300 km²"
      AppLanguage.Bulgarian -> "Площ 50-300 km²"
      AppLanguage.German -> "Fläche 50-300 km²"
    }
    CreateQuizPreset.CapitalAreaThreeHundredToEightHundredSquareKm -> when (language) {
      AppLanguage.English -> "Area 300-800 km²"
      AppLanguage.Bulgarian -> "Площ 300-800 km²"
      AppLanguage.German -> "Fläche 300-800 km²"
    }
    CreateQuizPreset.CapitalAreaOverEightHundredSquareKm -> when (language) {
      AppLanguage.English -> "Area > 800 km²"
      AppLanguage.Bulgarian -> "Площ > 800 km²"
      AppLanguage.German -> "Fläche > 800 km²"
    }
    CreateQuizPreset.CapitalNotCoastal -> when (language) {
      AppLanguage.English -> "Not coastal"
      AppLanguage.Bulgarian -> "Без излаз на море"
      AppLanguage.German -> "Binnenstaat"
    }
  }

private fun matchesCountryCreateQuizPreset(
  country: FlagCountry,
  preset: CreateQuizPreset,
): Boolean {
  val code = country.code
  return when (preset) {
    CreateQuizPreset.TwoColors -> code in createQuizTwoColorCountries
    CreateQuizPreset.ThreeColors -> code in createQuizThreeColorCountries
    CreateQuizPreset.FourPlusColors -> code in createQuizFourPlusColorCountries
    CreateQuizPreset.HorizontalStripes -> code in createQuizHorizontalStripeCountries
    CreateQuizPreset.VerticalStripes -> code in createQuizVerticalStripeCountries
    CreateQuizPreset.Stars -> code in createQuizStarCountries
    CreateQuizPreset.Crosses -> code in createQuizCrossCountries
    CreateQuizPreset.Animals -> code in createQuizAnimalCountries
    CreateQuizPreset.Nato -> code in createQuizNatoCountryCodes
    CreateQuizPreset.EuUnion -> code in createQuizEuUnionCountryCodes
    CreateQuizPreset.WorldTradeOrganization -> code in createQuizWorldTradeOrganizationCountryCodes
    CreateQuizPreset.CommonwealthOfNations -> code in createQuizCommonwealthOfNationsCountryCodes
    CreateQuizPreset.AfricanUnion -> code in createQuizAfricanUnionCountryCodes
    CreateQuizPreset.OrganisationOfIslamicCooperation -> code in createQuizOrganisationOfIslamicCooperationCountryCodes
    CreateQuizPreset.CapitalPopulationUnderQuarterMillion,
    CreateQuizPreset.CapitalPopulationQuarterToOneMillion,
    CreateQuizPreset.CapitalPopulationOneToTwoPointFiveMillion,
    CreateQuizPreset.CapitalPopulationOverTwoPointFiveMillion,
    CreateQuizPreset.CapitalAreaUnderFiftySquareKm,
    CreateQuizPreset.CapitalAreaFiftyToThreeHundredSquareKm,
    CreateQuizPreset.CapitalAreaThreeHundredToEightHundredSquareKm,
    CreateQuizPreset.CapitalAreaOverEightHundredSquareKm,
    CreateQuizPreset.CapitalNotCoastal -> false
  }
}

private fun matchesCapitalCreateQuizPreset(
  country: FlagCountry,
  preset: CreateQuizPreset,
): Boolean {
  val code = country.code
  val metadata = capitalQuizMetadata(country.code) ?: return false
  return when (preset) {
    CreateQuizPreset.CapitalPopulationUnderQuarterMillion -> metadata.population < 250_000L
    CreateQuizPreset.CapitalPopulationQuarterToOneMillion -> metadata.population in 250_000L until 1_000_000L
    CreateQuizPreset.CapitalPopulationOneToTwoPointFiveMillion -> metadata.population in 1_000_000L until 2_500_000L
    CreateQuizPreset.CapitalPopulationOverTwoPointFiveMillion -> metadata.population > 2_500_000L
    CreateQuizPreset.CapitalAreaUnderFiftySquareKm -> metadata.areaKm2 < 50.0
    CreateQuizPreset.CapitalAreaFiftyToThreeHundredSquareKm -> metadata.areaKm2 in 50.0..300.0
    CreateQuizPreset.CapitalAreaThreeHundredToEightHundredSquareKm -> metadata.areaKm2 in 300.0..800.0
    CreateQuizPreset.CapitalAreaOverEightHundredSquareKm -> metadata.areaKm2 > 800.0
    CreateQuizPreset.CapitalNotCoastal -> metadata.notCoastal
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
    CreateQuizPreset.OrganisationOfIslamicCooperation -> false
  }
}

private val createQuizTwoColorCountries =
  setOf(
    "AL", "AT", "BD", "BH", "CA", "CH", "CN", "DK", "FI", "FM", "GE", "GR", "HN", "ID", "IL",
    "JP", "KG", "KZ", "LV", "MA", "MC", "MK", "NG", "PE", "PK", "PL", "PW", "QA", "SA", "SC",
    "SE", "SG", "SO", "TN", "TO", "TR", "UA", "VN",
  )

private val createQuizThreeColorCountries =
  setOf(
    "AM", "AO", "AR", "AU", "BA", "BB", "BE", "BF", "BI", "BG", "BJ", "BO", "BS", "BW", "BY",
    "CD", "CG", "CI", "CL", "CM", "CO", "CR", "CU", "CV", "CY", "CZ", "DE", "DZ", "EE", "FR",
    "GA", "GB", "GN", "HU", "IE", "IR", "IS", "JM", "KH", "KP", "LA", "LB", "LI", "LR", "LT",
    "LU", "MH", "MG", "ML", "MN", "MR", "MT", "MV", "MW", "NE", "NL", "NO", "NP", "NR", "NZ",
    "PA", "RO", "RU", "RW", "VC", "WS", "SI", "SK", "SL", "SN", "TD", "TH", "TT", "US", "UY",
    "YE",
  )

private val createQuizFourPlusColorCountries =
  setOf(
    "AD", "AE", "AF", "AG", "AZ", "BN", "BR", "BT", "BZ", "CF", "DJ", "DM", "DO", "EC", "EG",
    "ER", "ES", "ET", "FJ", "GD", "GH", "GM", "GQ", "GT", "GW", "GY", "HR", "HT", "IN", "IQ",
    "IT", "JO", "KE", "KI", "KM", "KN", "KR", "KW", "LC", "LK", "LS", "LY", "MD", "ME", "MM",
    "MU", "MX", "MY", "MZ", "NA", "NI", "OM", "PG", "PH", "PS", "PT", "PY", "RS", "SB", "SD",
    "SM", "SR", "SS", "ST", "SV", "SY", "SZ", "TG", "TJ", "TL", "TM", "TV", "TZ", "UG", "UZ",
    "VA", "VE", "VU", "ZA", "ZM", "ZW",
  )

private val createQuizCrossCountries =
  setOf("AU", "BI", "CH", "DK", "DM", "DO", "FI", "FJ", "GB", "GE", "GR", "IS", "JM", "MT", "NZ", "TO", "TV")

private val createQuizHorizontalStripeCountries =
  setOf(
    "AR", "AZ", "BS", "BO", "BW", "BG", "BF", "BI", "CV", "KH", "CF", "CO", "KM", "CR",
    "HR", "EC", "EG", "SV", "GQ", "EE", "SZ", "ET", "GA", "GM", "GH", "GR", "GW", "HT",
    "HN", "IN", "IR", "IQ", "JO", "KE", "KW", "LA", "LB", "LS", "LR", "LY", "LI", "LT",
    "MG", "MW", "MU", "NR", "NI", "NE", "KP", "OM", "PS", "PY", "RW", "SL", "SG", "SK",
    "SI", "SS", "SD", "SR", "SY", "TJ", "TG", "AE", "UZ", "VU", "YE", "ZW",
  )

private val createQuizStarCountries =
  setOf(
    "AO", "AR", "AU", "AZ", "BA", "BF", "BI", "BR", "BS", "BZ", "CA", "CD", "CF", "CL",
    "CM", "CN", "CO", "CR", "CU", "CV", "DJ", "DM", "DO", "ET", "FJ", "FM", "GA", "GD",
    "GH", "GN", "GW", "HN", "IE", "IL", "JM", "JO", "KE", "KN", "KP", "LB", "LR", "LY",
    "MH", "MD", "MG", "MK", "ML", "MN", "MR", "MW", "MY", "MZ", "NA", "NR", "NZ", "PA",
    "PE", "PG", "PH", "PK", "PS", "RW", "SA", "SB", "SC", "SG", "SI", "SN", "SO", "SR",
    "SS", "ST", "SY", "SZ", "TH", "TJ", "TM", "TN", "TO", "TT", "TV", "US", "VE", "VN",
    "WS", "YE", "ZA", "ZM", "ZW",
  )

private val createQuizVerticalStripeCountries =
  setOf("AD", "BE", "BJ", "CF", "DZ", "GA", "GN", "GW", "MG", "ML", "MD", "MN", "OM", "PK", "PE", "PT", "QA", "SN", "VC", "AE", "VA")

private val createQuizAnimalCountries =
  setOf("AD", "AL", "AO", "AR", "BT", "DM", "EC", "EG", "FJ", "GT", "KE", "KI", "KZ", "MD", "ME", "PG", "RS", "UG", "VU", "ZM", "ZW")

private val createQuizNatoCountryCodes =
  setOf(
    "AL", "BE", "BG", "CA", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB", "GR", "HR", "HU", "IS",
    "IT", "LT", "LU", "LV", "ME", "MK", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SK", "TR", "US",
  )

private val createQuizEuUnionCountryCodes =
  setOf(
    "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GR", "HR", "HU", "IE", "IT",
    "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK",
  )

private val createQuizWorldTradeOrganizationCountryCodes =
  setOf(
    "AE", "AF", "AG", "AL", "AM", "AO", "AR", "AT", "AU", "BB", "BD", "BE", "BF", "BG", "BH", "BI",
    "BJ", "BN", "BO", "BR", "BW", "BZ", "CA", "CD", "CF", "CG", "CH", "CI", "CL", "CM", "CN", "CO",
    "CR", "CU", "CV", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "EC", "EE", "EG", "ES", "FI", "FJ",
    "FR", "GA", "GB", "GD", "GE", "GH", "GM", "GN", "GR", "GT", "GW", "GY", "HN", "HR", "HT", "HU",
    "ID", "IE", "IL", "IN", "IS", "IT", "JM", "JO", "JP", "KE", "KG", "KH", "KM", "KN", "KR", "KW",
    "KZ", "LA", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "MA", "MD", "ME", "MG", "MK", "ML",
    "MM", "MN", "MR", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NE", "NG", "NI", "NL", "NO",
    "NP", "NZ", "OM", "PA", "PE", "PG", "PH", "PK", "PL", "PT", "PY", "QA", "RO", "RU", "RW", "SA",
    "SB", "SC", "SE", "SG", "SI", "SK", "SL", "SN", "SR", "SV", "SZ", "TD", "TG", "TH", "TJ", "TL",
    "TN", "TO", "TR", "TT", "TZ", "UA", "UG", "US", "UY", "VC", "VE", "VN", "VU", "WS", "YE", "ZA",
    "ZM", "ZW",
  )

private val createQuizCommonwealthOfNationsCountryCodes =
  setOf(
    "AG", "AU", "BB", "BD", "BN", "BS", "BW", "BZ", "CA", "CM", "CY", "DM", "FJ", "GA", "GB", "GD",
    "GH", "GM", "GY", "IN", "JM", "KE", "KI", "KN", "LC", "LK", "LS", "MT", "MU", "MV", "MW", "MY",
    "MZ", "NA", "NG", "NR", "NZ", "PG", "PK", "RW", "SB", "SC", "SG", "SL", "SZ", "TG", "TO", "TT",
    "TV", "TZ", "UG", "VC", "VU", "WS", "ZA", "ZM",
  )

private val createQuizAfricanUnionCountryCodes =
  setOf(
    "AO", "BF", "BI", "BJ", "BW", "CD", "CF", "CG", "CI", "CM", "CV", "DJ", "DZ", "EG", "ER", "ET",
    "GA", "GH", "GM", "GN", "GQ", "GW", "KE", "KM", "LR", "LS", "LY", "MA", "MG", "ML", "MR", "MU",
    "MW", "MZ", "NA", "NE", "NG", "RW", "SC", "SD", "SL", "SN", "SO", "SS", "ST", "SZ", "TD", "TG",
    "TN", "TZ", "UG", "ZA", "ZM", "ZW",
  )

private val createQuizOrganisationOfIslamicCooperationCountryCodes =
  setOf(
    "AE", "AF", "AL", "AZ", "BD", "BF", "BH", "BJ", "BN", "CI", "CM", "DJ", "DZ", "EG", "GA", "GM",
    "GN", "GW", "GY", "ID", "IQ", "IR", "JO", "KG", "KM", "KW", "KZ", "LB", "LY", "MA", "ML", "MR",
    "MV", "MY", "MZ", "NE", "NG", "OM", "PK", "PS", "QA", "SA", "SD", "SL", "SN", "SO", "SR", "SY",
    "TD", "TG", "TJ", "TM", "TN", "TR", "UG", "UZ", "YE",
  )

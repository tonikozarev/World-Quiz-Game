package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry

interface FlagCatalogRepository {
  fun getCountries(): List<FlagCountry>
}

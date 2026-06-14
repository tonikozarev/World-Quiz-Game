package com.example.flaggameandroid.core.data

import com.example.flaggameandroid.core.model.FlagCountry

class StaticFlagCatalogRepository : FlagCatalogRepository {
  override fun getCountries(): List<FlagCountry> =
    listOf(
      FlagCountry(code = "JP", name = "Japan", emoji = "\uD83C\uDDEF\uD83C\uDDF5", region = "Asia"),
      FlagCountry(code = "NO", name = "Norway", emoji = "\uD83C\uDDF3\uD83C\uDDF4", region = "Europe"),
      FlagCountry(code = "MX", name = "Mexico", emoji = "\uD83C\uDDF2\uD83C\uDDFD", region = "North America"),
      FlagCountry(code = "IE", name = "Ireland", emoji = "\uD83C\uDDEE\uD83C\uDDEA", region = "Europe"),
      FlagCountry(code = "BR", name = "Brazil", emoji = "\uD83C\uDDE7\uD83C\uDDF7", region = "South America"),
      FlagCountry(code = "KE", name = "Kenya", emoji = "\uD83C\uDDF0\uD83C\uDDEA", region = "Africa"),
      FlagCountry(code = "CA", name = "Canada", emoji = "\uD83C\uDDE8\uD83C\uDDE6", region = "North America"),
      FlagCountry(code = "KR", name = "South Korea", emoji = "\uD83C\uDDF0\uD83C\uDDF7", region = "Asia"),
    )
}

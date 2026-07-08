package com.example.flaggameandroid.feature.app

import com.example.flaggameandroid.core.model.AchievementId
import com.example.flaggameandroid.core.model.AchievementSector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun localizedAchievementSectorTitle(
  sector: AchievementSector,
  language: AppLanguage,
): String =
  when (sector) {
    AchievementSector.Continents ->
      when (language) {
        AppLanguage.English -> "Continent masters"
        AppLanguage.Bulgarian -> "Майстори на континенти"
        AppLanguage.German -> "Kontinent-Meister"
      }
    AchievementSector.World ->
      when (language) {
        AppLanguage.English -> "World runs"
        AppLanguage.Bulgarian -> "Световни серии"
        AppLanguage.German -> "Weltläufe"
      }
    AchievementSector.SpeedRuns ->
      when (language) {
        AppLanguage.English -> "Speed runs"
        AppLanguage.Bulgarian -> "Скоростни игри"
        AppLanguage.German -> "Schnellläufe"
      }
    AchievementSector.Collectors ->
      when (language) {
        AppLanguage.English -> "Collectors"
        AppLanguage.Bulgarian -> "Събирачи"
        AppLanguage.German -> "Sammler"
      }
    AchievementSector.Skill ->
      when (language) {
        AppLanguage.English -> "Skill feats"
        AppLanguage.Bulgarian -> "Умения"
        AppLanguage.German -> "Skill-Leistungen"
      }
  }

internal fun localizedTitle(
  language: AppLanguage,
  en: String,
  bg: String,
  de: String,
): String =
  when (language) {
    AppLanguage.English -> en
    AppLanguage.Bulgarian -> bg
    AppLanguage.German -> de
  }

internal fun localizedAchievementTitle(
  achievementId: AchievementId,
  language: AppLanguage,
): String =
  when (achievementId) {
    AchievementId.AfricaPerfect -> localizedTitle(language, "Africa Perfect", "Африка без грешка", "Afrika fehlerfrei")
    AchievementId.AsiaPerfect -> localizedTitle(language, "Asia Perfect", "Азия без грешка", "Asien fehlerfrei")
    AchievementId.EuropePerfect -> localizedTitle(language, "Europe Perfect", "Европа без грешка", "Europa fehlerfrei")
    AchievementId.NorthAmericaPerfect -> localizedTitle(language, "North America Perfect", "Северна Америка без грешка", "Nordamerika fehlerfrei")
    AchievementId.OceaniaPerfect -> localizedTitle(language, "Oceania Perfect", "Океания без грешка", "Ozeanien fehlerfrei")
    AchievementId.SouthAmericaPerfect -> localizedTitle(language, "South America Perfect", "Южна Америка без грешка", "Südamerika fehlerfrei")
    AchievementId.DiamondWorld -> localizedTitle(language, "Diamond World", "Диамантен свят", "Diamantene Welt")
    AchievementId.HardcoreCountriesLegend -> localizedTitle(language, "Hardcore Countries Legend", "Хардкор легенда за държави", "Hardcore-Legende für Länder")
    AchievementId.HardcoreCapitalsLegend -> localizedTitle(language, "Hardcore Capitals Legend", "Хардкор легенда за столици", "Hardcore-Legende für Hauptstädte")
    AchievementId.HardcoreLegend -> localizedTitle(language, "Hardcore Legend", "Хардкор легенда", "Hardcore-Legende")
    AchievementId.WorldPurist -> localizedTitle(language, "World Purist", "Световен пурист", "Weltpurist")
    AchievementId.SpeedRunStarter -> localizedTitle(language, "Speed Run Starter", "Старт на скоростна игра", "Schnelllauf-Starter")
    AchievementId.SpeedRunPurist -> localizedTitle(language, "Speed Run Purist", "Скоростен пурист", "Schnelllauf-Purist")
    AchievementId.SpeedRunOneSecond -> localizedTitle(language, "Speed Run One Second", "Скоростна игра за 1 секунда", "Ein-Sekunden-Schnelllauf")
    AchievementId.BronzeCollector -> localizedTitle(language, "Bronze Collector", "Събирач на бронз", "Bronze-Sammler")
    AchievementId.SilverCollector -> localizedTitle(language, "Silver Collector", "Събирач на сребро", "Silber-Sammler")
    AchievementId.GoldCollector -> localizedTitle(language, "Gold Collector", "Събирач на злато", "Gold-Sammler")
    AchievementId.PlatinumCollector -> localizedTitle(language, "Platinum Collector", "Събирач на платина", "Platin-Sammler")
    AchievementId.DiamondCollector -> localizedTitle(language, "Diamond Collector", "Събирач на диаманти", "Diamant-Sammler")
    AchievementId.FirstPerfect -> localizedTitle(language, "First Perfect", "Първи перфектен", "Erstes Perfekt")
    AchievementId.HintlessHero -> localizedTitle(language, "Hintless Hero", "Герой без жокери", "Hinweisloser Held")
    AchievementId.VariantMaster -> localizedTitle(language, "Variant Master", "Майстор на варианти", "Variantenmeister")
  }

internal fun localizedAchievementDescription(
  achievementId: AchievementId,
  language: AppLanguage,
): String =
  when (achievementId) {
    AchievementId.AfricaPerfect -> localizedTitle(language, "Finish an Africa-only quiz with 0 mistakes.", "Завърши тест само за Африка без грешка.", "Beende ein Quiz nur für Afrika ohne Fehler.")
    AchievementId.AsiaPerfect -> localizedTitle(language, "Finish an Asia-only quiz with 0 mistakes.", "Завърши тест само за Азия без грешка.", "Beende ein Quiz nur für Asien ohne Fehler.")
    AchievementId.EuropePerfect -> localizedTitle(language, "Finish an Europe-only quiz with 0 mistakes.", "Завърши тест само за Европа без грешка.", "Beende ein Quiz nur für Europa ohne Fehler.")
    AchievementId.NorthAmericaPerfect -> localizedTitle(language, "Finish a North America-only quiz with 0 mistakes.", "Завърши тест само за Северна Америка без грешка.", "Beende ein Quiz nur für Nordamerika ohne Fehler.")
    AchievementId.OceaniaPerfect -> localizedTitle(language, "Finish an Oceania-only quiz with 0 mistakes.", "Завърши тест само за Океания без грешка.", "Beende ein Quiz nur für Ozeanien ohne Fehler.")
    AchievementId.SouthAmericaPerfect -> localizedTitle(language, "Finish a South America-only quiz with 0 mistakes.", "Завърши тест само за Южна Америка без грешка.", "Beende ein Quiz nur für Südamerika ohne Fehler.")
    AchievementId.DiamondWorld -> localizedTitle(language, "Finish all countries and capitals with 0 mistakes.", "Завърши всички държави и столици без грешка.", "Beende alle Länder und Hauptstädte ohne Fehler.")
    AchievementId.HardcoreCountriesLegend -> localizedTitle(language, "Finish a perfect Hardcore quiz with countries only and all three question variants selected.", "Завърши перфектен Хардкор тест само за държави с избрани и трите варианта за въпроси.", "Beende ein perfektes Hardcore-Quiz nur mit Ländern und allen drei Fragetypen.")
    AchievementId.HardcoreCapitalsLegend -> localizedTitle(language, "Finish a perfect Hardcore quiz with capitals only and all three question variants selected.", "Завърши перфектен Хардкор тест само за столици с избрани и трите варианта за въпроси.", "Beende ein perfektes Hardcore-Quiz nur mit Hauptstädten und allen drei Fragetypen.")
    AchievementId.HardcoreLegend -> localizedTitle(language, "Finish a perfect Hardcore quiz with countries and capitals and all three question variants selected.", "Завърши перфектен Хардкор тест с държави и столици и с избрани и трите варианта за въпроси.", "Beende ein perfektes Hardcore-Quiz mit Ländern, Hauptstädten und allen drei Fragetypen.")
    AchievementId.WorldPurist -> localizedTitle(language, "Finish all countries and capitals with 0 mistakes and no hints.", "Завърши всички държави и столици без грешка и без жокери.", "Beende alle Länder und Hauptstädte ohne Fehler und ohne Hinweise.")
    AchievementId.SpeedRunStarter -> localizedTitle(language, "Finish any Speed Run quiz.", "Завърши който и да е тест за скоростна игра.", "Beende irgendein Schnelllauf-Quiz.")
    AchievementId.SpeedRunPurist -> localizedTitle(language, "Finish a perfect Speed Run quiz without using hints.", "Завърши перфектна скоростна игра без жокери.", "Beende ein perfektes Schnelllauf-Quiz ohne Hinweise.")
    AchievementId.SpeedRunOneSecond -> localizedTitle(language, "Finish a perfect Speed Run quiz with 1 second per question.", "Завърши перфектна скоростна игра с 1 секунда за въпрос.", "Beende ein perfektes Schnelllauf-Quiz mit 1 Sekunde pro Frage.")
    AchievementId.BronzeCollector -> localizedTitle(language, "Earn bronze 50 times.", "Спечели бронз 50 пъти.", "Bronze 50-mal verdienen.")
    AchievementId.SilverCollector -> localizedTitle(language, "Earn silver 25 times.", "Спечели сребро 25 пъти.", "Silber 25-mal verdienen.")
    AchievementId.GoldCollector -> localizedTitle(language, "Earn gold 10 times.", "Спечели злато 10 пъти.", "Gold 10-mal verdienen.")
    AchievementId.PlatinumCollector -> localizedTitle(language, "Earn platinum 5 times.", "Спечели платина 5 пъти.", "Platin 5-mal verdienen.")
    AchievementId.DiamondCollector -> localizedTitle(language, "Earn diamond 1 time.", "Спечели диамант 1 път.", "Diamant 1-mal verdienen.")
    AchievementId.FirstPerfect -> localizedTitle(language, "Finish any medal-eligible quiz with 100% correct answers.", "Завърши всеки тест за медал с 100% верни отговори.", "Beende ein medaillenfähiges Quiz mit 100% richtigen Antworten.")
    AchievementId.HintlessHero -> localizedTitle(language, "Finish a perfect medal-eligible quiz without using hints.", "Завърши перфектен тест за медал без жокери.", "Beende ein perfektes medaillenfähiges Quiz ohne Hinweise.")
    AchievementId.VariantMaster -> localizedTitle(language, "Finish a perfect quiz that includes all three question variants.", "Завърши перфектен тест с трите варианта въпроси.", "Beende ein perfektes Quiz mit allen drei Fragetypen.")
  }

internal fun localizedAchievementHint(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Tap an achievement to see its unlock clue."
    AppLanguage.Bulgarian -> "Докосни постижение, за да видиш подсказка за отключване."
    AppLanguage.German -> "Tippe auf einen Erfolg, um den Freischalt-Hinweis zu sehen."
  }

internal fun localizedAchievementStatus(
  language: AppLanguage,
  unlockedAt: Long?,
): String =
  if (unlockedAt == null) {
    when (language) {
      AppLanguage.English -> "Locked"
      AppLanguage.Bulgarian -> "Заключено"
      AppLanguage.German -> "Gesperrt"
    }
  } else {
    when (language) {
      AppLanguage.English -> "Unlocked on ${formatAchievementDate(unlockedAt)}"
      AppLanguage.Bulgarian -> "Отключено на ${formatAchievementDate(unlockedAt)}"
      AppLanguage.German -> "Freigeschaltet am ${formatAchievementDate(unlockedAt)}"
    }
  }

private fun formatAchievementDate(unlockedAt: Long): String =
  SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(unlockedAt))

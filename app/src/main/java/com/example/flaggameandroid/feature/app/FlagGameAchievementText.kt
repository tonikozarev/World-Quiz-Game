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
        AppLanguage.Bulgarian -> "РњР°Р№СЃС‚РѕСЂРё РЅР° РєРѕРЅС‚РёРЅРµРЅС‚Рё"
        AppLanguage.German -> "Kontinent-Meister"
      }
    AchievementSector.World ->
      when (language) {
        AppLanguage.English -> "World runs"
        AppLanguage.Bulgarian -> "РЎРІРµС‚РѕРІРЅРё СЃРµСЂРёРё"
        AppLanguage.German -> "WeltlГ¤ufe"
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
        AppLanguage.Bulgarian -> "РЎСЉР±РёСЂР°С‡Рё"
        AppLanguage.German -> "Sammler"
      }
    AchievementSector.Skill ->
      when (language) {
        AppLanguage.English -> "Skill feats"
        AppLanguage.Bulgarian -> "РЈРјРµРЅРёСЏ"
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
    AchievementId.AfricaPerfect -> localizedTitle(language, "Africa Perfect", "РђС„СЂРёРєР° Р±РµР· РіСЂРµС€РєР°", "Afrika fehlerfrei")
    AchievementId.AsiaPerfect -> localizedTitle(language, "Asia Perfect", "РђР·РёСЏ Р±РµР· РіСЂРµС€РєР°", "Asien fehlerfrei")
    AchievementId.EuropePerfect -> localizedTitle(language, "Europe Perfect", "Р•РІСЂРѕРїР° Р±РµР· РіСЂРµС€РєР°", "Europa fehlerfrei")
    AchievementId.NorthAmericaPerfect -> localizedTitle(language, "North America Perfect", "РЎРµРІРµСЂРЅР° РђРјРµСЂРёРєР° Р±РµР· РіСЂРµС€РєР°", "Nordamerika fehlerfrei")
    AchievementId.OceaniaPerfect -> localizedTitle(language, "Oceania Perfect", "РћРєРµР°РЅРёСЏ Р±РµР· РіСЂРµС€РєР°", "Ozeanien fehlerfrei")
    AchievementId.SouthAmericaPerfect -> localizedTitle(language, "South America Perfect", "Р®Р¶РЅР° РђРјРµСЂРёРєР° Р±РµР· РіСЂРµС€РєР°", "SГјdamerika fehlerfrei")
    AchievementId.DiamondWorld -> localizedTitle(language, "Diamond World", "Р”РёР°РјР°РЅС‚РµРЅ СЃРІСЏС‚", "Diamantene Welt")
    AchievementId.NoBluffLegend -> localizedTitle(language, "No Bluff Legend", "Р›РµРіРµРЅРґР° Р±РµР· Р±Р»СЉС„", "Kein Bluff Legende")
    AchievementId.WorldPurist -> localizedTitle(language, "World Purist", "РЎРІРµС‚РѕРІРµРЅ РїСѓСЂРёСЃС‚", "Weltpurist")
    AchievementId.SpeedRunStarter -> localizedTitle(language, "Finish any Speed Run quiz.", "Завърши който и да е тест за скоростна игра.", "Beende irgendein Schnelllauf-Quiz.")
    AchievementId.SpeedRunPurist -> localizedTitle(language, "Finish a perfect Speed Run quiz without using hints.", "Завърши перфектна скоростна игра без жокери.", "Beende ein perfektes Schnelllauf-Quiz ohne Hinweise.")
    AchievementId.BronzeCollector -> localizedTitle(language, "Bronze Collector", "РЎСЉР±РёСЂР°С‡ РЅР° Р±СЂРѕРЅР·", "Bronze-Sammler")
    AchievementId.SilverCollector -> localizedTitle(language, "Silver Collector", "РЎСЉР±РёСЂР°С‡ РЅР° СЃСЂРµР±СЂРѕ", "Silber-Sammler")
    AchievementId.GoldCollector -> localizedTitle(language, "Gold Collector", "РЎСЉР±РёСЂР°С‡ РЅР° Р·Р»Р°С‚Рѕ", "Gold-Sammler")
    AchievementId.PlatinumCollector -> localizedTitle(language, "Platinum Collector", "РЎСЉР±РёСЂР°С‡ РЅР° РїР»Р°С‚РёРЅР°", "Platin-Sammler")
    AchievementId.DiamondCollector -> localizedTitle(language, "Diamond Collector", "РЎСЉР±РёСЂР°С‡ РЅР° РґРёР°РјР°РЅС‚Рё", "Diamant-Sammler")
    AchievementId.FirstPerfect -> localizedTitle(language, "First Perfect", "РџСЉСЂРІРё РїРµСЂС„РµРєС‚РµРЅ", "Erstes Perfekt")
    AchievementId.HintlessHero -> localizedTitle(language, "Hintless Hero", "Р“РµСЂРѕР№ Р±РµР· Р¶РѕРєРµСЂРё", "Hinweisloser Held")
    AchievementId.VariantMaster -> localizedTitle(language, "Variant Master", "РњР°Р№СЃС‚РѕСЂ РЅР° РІР°СЂРёР°РЅС‚Рё", "Variantenmeister")
  }

internal fun localizedAchievementDescription(
  achievementId: AchievementId,
  language: AppLanguage,
): String =
  when (achievementId) {
    AchievementId.AfricaPerfect -> localizedTitle(language, "Finish an Africa-only quiz with 0 mistakes.", "Р—Р°РІСЉСЂС€Рё С‚РµСЃС‚ СЃР°РјРѕ Р·Р° РђС„СЂРёРєР° Р±РµР· РіСЂРµС€РєР°.", "Beende ein Quiz nur fГјr Afrika ohne Fehler.")
    AchievementId.AsiaPerfect -> localizedTitle(language, "Finish an Asia-only quiz with 0 mistakes.", "Р—Р°РІСЉСЂС€Рё С‚РµСЃС‚ СЃР°РјРѕ Р·Р° РђР·РёСЏ Р±РµР· РіСЂРµС€РєР°.", "Beende ein Quiz nur fГјr Asien ohne Fehler.")
    AchievementId.EuropePerfect -> localizedTitle(language, "Finish an Europe-only quiz with 0 mistakes.", "Р—Р°РІСЉСЂС€Рё С‚РµСЃС‚ СЃР°РјРѕ Р·Р° Р•РІСЂРѕРїР° Р±РµР· РіСЂРµС€РєР°.", "Beende ein Quiz nur fГјr Europa ohne Fehler.")
    AchievementId.NorthAmericaPerfect -> localizedTitle(language, "Finish a North America-only quiz with 0 mistakes.", "Р—Р°РІСЉСЂС€Рё С‚РµСЃС‚ СЃР°РјРѕ Р·Р° РЎРµРІРµСЂРЅР° РђРјРµСЂРёРєР° Р±РµР· РіСЂРµС€РєР°.", "Beende ein Quiz nur fГјr Nordamerika ohne Fehler.")
    AchievementId.OceaniaPerfect -> localizedTitle(language, "Finish an Oceania-only quiz with 0 mistakes.", "Р—Р°РІСЉСЂС€Рё С‚РµСЃС‚ СЃР°РјРѕ Р·Р° РћРєРµР°РЅРёСЏ Р±РµР· РіСЂРµС€РєР°.", "Beende ein Quiz nur fГјr Ozeanien ohne Fehler.")
    AchievementId.SouthAmericaPerfect -> localizedTitle(language, "Finish a South America-only quiz with 0 mistakes.", "Р—Р°РІСЉСЂС€Рё С‚РµСЃС‚ СЃР°РјРѕ Р·Р° Р®Р¶РЅР° РђРјРµСЂРёРєР° Р±РµР· РіСЂРµС€РєР°.", "Beende ein Quiz nur fГјr SГјdamerika ohne Fehler.")
    AchievementId.DiamondWorld -> localizedTitle(language, "Finish all countries with 0 mistakes.", "Р—Р°РІСЉСЂС€Рё РІСЃРёС‡РєРё РґСЉСЂР¶Р°РІРё Р±РµР· РіСЂРµС€РєР°.", "Beende alle LГ¤nder ohne Fehler.")
    AchievementId.NoBluffLegend -> localizedTitle(language, "Finish No Bluff, All Tough with all three variants selected and 0 mistakes.", "Р—Р°РІСЉСЂС€Рё Р‘РµР· Р±Р»СЉС„, СЃР°РјРѕ С‚СЂСѓРґРЅРѕ СЃ С‚СЂРёС‚Рµ РІР°СЂРёР°РЅС‚Р° Рё Р±РµР· РіСЂРµС€РєР°.", "Beende Kein Bluff, alles hart mit allen drei Varianten und ohne Fehler.")
    AchievementId.WorldPurist -> localizedTitle(language, "Finish all countries with 0 mistakes and no hints.", "Р—Р°РІСЉСЂС€Рё РІСЃРёС‡РєРё РґСЉСЂР¶Р°РІРё Р±РµР· РіСЂРµС€РєР° Рё Р±РµР· Р¶РѕРєРµСЂРё.", "Beende alle LГ¤nder ohne Fehler und ohne Hinweise.")
    AchievementId.SpeedRunStarter -> localizedTitle(language, "Finish any Speed Run quiz.", "Завърши който и да е тест за скоростна игра.", "Beende irgendein Schnelllauf-Quiz.")
    AchievementId.SpeedRunPurist -> localizedTitle(language, "Finish a perfect Speed Run quiz without using hints.", "Завърши перфектна скоростна игра без жокери.", "Beende ein perfektes Schnelllauf-Quiz ohne Hinweise.")
    AchievementId.BronzeCollector -> localizedTitle(language, "Earn bronze 50 times.", "РЎРїРµС‡РµР»Рё Р±СЂРѕРЅР· 50 РїСЉС‚Рё.", "Bronze 50-mal verdienen.")
    AchievementId.SilverCollector -> localizedTitle(language, "Earn silver 25 times.", "РЎРїРµС‡РµР»Рё СЃСЂРµР±СЂРѕ 25 РїСЉС‚Рё.", "Silber 25-mal verdienen.")
    AchievementId.GoldCollector -> localizedTitle(language, "Earn gold 10 times.", "РЎРїРµС‡РµР»Рё Р·Р»Р°С‚Рѕ 10 РїСЉС‚Рё.", "Gold 10-mal verdienen.")
    AchievementId.PlatinumCollector -> localizedTitle(language, "Earn platinum 5 times.", "РЎРїРµС‡РµР»Рё РїР»Р°С‚РёРЅР° 5 РїСЉС‚Рё.", "Platin 5-mal verdienen.")
    AchievementId.DiamondCollector -> localizedTitle(language, "Earn diamond 1 time.", "РЎРїРµС‡РµР»Рё РґРёР°РјР°РЅС‚ 1 РїСЉС‚.", "Diamant 1-mal verdienen.")
    AchievementId.FirstPerfect -> localizedTitle(language, "Finish any medal-eligible quiz with 100% correct answers.", "Р—Р°РІСЉСЂС€Рё РІСЃРµРєРё С‚РµСЃС‚ Р·Р° РјРµРґР°Р» СЃ 100% РІРµСЂРЅРё РѕС‚РіРѕРІРѕСЂРё.", "Beende ein medaillenfГ¤higes Quiz mit 100% richtigen Antworten.")
    AchievementId.HintlessHero -> localizedTitle(language, "Finish a perfect medal-eligible quiz without using hints.", "Р—Р°РІСЉСЂС€Рё РїРµСЂС„РµРєС‚РµРЅ С‚РµСЃС‚ Р·Р° РјРµРґР°Р» Р±РµР· Р¶РѕРєРµСЂРё.", "Beende ein perfektes medaillenfГ¤higes Quiz ohne Hinweise.")
    AchievementId.VariantMaster -> localizedTitle(language, "Finish a perfect quiz that includes all three question variants.", "Р—Р°РІСЉСЂС€Рё РїРµСЂС„РµРєС‚РµРЅ С‚РµСЃС‚ СЃ С‚СЂРёС‚Рµ РІР°СЂРёР°РЅС‚Р° РІСЉРїСЂРѕСЃРё.", "Beende ein perfektes Quiz mit allen drei Fragetypen.")
  }

internal fun localizedAchievementHint(language: AppLanguage): String =
  when (language) {
    AppLanguage.English -> "Tap an achievement to see its unlock clue."
    AppLanguage.Bulgarian -> "Р”РѕРєРѕСЃРЅРё РїРѕСЃС‚РёР¶РµРЅРёРµ, Р·Р° РґР° РІРёРґРёС€ РїРѕРґСЃРєР°Р·РєР° Р·Р° РѕС‚РєР»СЋС‡РІР°РЅРµ."
    AppLanguage.German -> "Tippe auf einen Erfolg, um den Freischalt-Hinweis zu sehen."
  }

internal fun localizedAchievementStatus(
  language: AppLanguage,
  unlockedAt: Long?,
): String =
  if (unlockedAt == null) {
    when (language) {
      AppLanguage.English -> "Locked"
      AppLanguage.Bulgarian -> "Р—Р°РєР»СЋС‡РµРЅРѕ"
      AppLanguage.German -> "Gesperrt"
    }
  } else {
    when (language) {
      AppLanguage.English -> "Unlocked on ${formatAchievementDate(unlockedAt)}"
      AppLanguage.Bulgarian -> "РћС‚РєР»СЋС‡РµРЅРѕ РЅР° ${formatAchievementDate(unlockedAt)}"
      AppLanguage.German -> "Freigeschaltet am ${formatAchievementDate(unlockedAt)}"
    }
  }

private fun formatAchievementDate(unlockedAt: Long): String =
  SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(unlockedAt))



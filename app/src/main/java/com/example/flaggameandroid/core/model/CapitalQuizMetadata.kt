package com.example.flaggameandroid.core.model

internal data class CapitalQuizMetadata(
  val population: Long,
  val areaKm2: Double,
  val notCoastal: Boolean,
)

internal val capitalQuizMetadataByCountryCode: Map<String, CapitalQuizMetadata> = mapOf(
  // AF — Afghanistan — Kabul
  "AF" to CapitalQuizMetadata(population = 4601789L, areaKm2 = 381.3, notCoastal = true),
  // AL — Albania — Tirana
  "AL" to CapitalQuizMetadata(population = 557422L, areaKm2 = 1110.0, notCoastal = true),
  // DZ — Algeria — Algiers
  "DZ" to CapitalQuizMetadata(population = 3915811L, areaKm2 = 363.0, notCoastal = false),
  // AD — Andorra — Andorra la Vella
  "AD" to CapitalQuizMetadata(population = 22873L, areaKm2 = 12.0, notCoastal = true),
  // AO — Angola — Luanda
  "AO" to CapitalQuizMetadata(population = 2571861L, areaKm2 = 116.0, notCoastal = false),
  // AG — Antigua and Barbuda — St. John's
  "AG" to CapitalQuizMetadata(population = 22219L, areaKm2 = 10.0, notCoastal = false),
  // AR — Argentina — Buenos Aires
  "AR" to CapitalQuizMetadata(population = 3120612L, areaKm2 = 205.9, notCoastal = false),
  // AM — Armenia — Yerevan
  "AM" to CapitalQuizMetadata(population = 1096100L, areaKm2 = 223.0, notCoastal = true),
  // AU — Australia — Canberra
  "AU" to CapitalQuizMetadata(population = 431380L, areaKm2 = 814.2, notCoastal = true),
  // AT — Austria — Vienna
  "AT" to CapitalQuizMetadata(population = 1962779L, areaKm2 = 414.78, notCoastal = true),
  // AZ — Azerbaijan — Baku
  "AZ" to CapitalQuizMetadata(population = 2303100L, areaKm2 = 2140.0, notCoastal = false),
  // BS — Bahamas — Nassau
  "BS" to CapitalQuizMetadata(population = 274400L, areaKm2 = 200.0, notCoastal = false),
  // BH — Bahrain — Manama
  "BH" to CapitalQuizMetadata(population = 200000L, areaKm2 = 30.0, notCoastal = false),
  // BD — Bangladesh — Dhaka
  "BD" to CapitalQuizMetadata(population = 8906039L, areaKm2 = 306.0, notCoastal = true),
  // BB — Barbados — Bridgetown
  "BB" to CapitalQuizMetadata(population = 110000L, areaKm2 = 40.0, notCoastal = false),
  // BY — Belarus — Minsk
  "BY" to CapitalQuizMetadata(population = 1996553L, areaKm2 = 409.53, notCoastal = true),
  // BE — Belgium — Brussels
  "BE" to CapitalQuizMetadata(population = 187686L, areaKm2 = 162.42, notCoastal = true),
  // BZ — Belize — Belmopan
  "BZ" to CapitalQuizMetadata(population = 20621L, areaKm2 = 32.78, notCoastal = true),
  // BJ — Benin — Porto-Novo
  "BJ" to CapitalQuizMetadata(population = 264320L, areaKm2 = 110.0, notCoastal = false),
  // BT — Bhutan — Thimphu
  "BT" to CapitalQuizMetadata(population = 114551L, areaKm2 = 26.1, notCoastal = true),
  // BO — Bolivia — Sucre
  "BO" to CapitalQuizMetadata(population = 360544L, areaKm2 = 1768.0, notCoastal = true),
  // BA — Bosnia and Herzegovina — Sarajevo
  "BA" to CapitalQuizMetadata(population = 275524L, areaKm2 = 141.5, notCoastal = true),
  // BW — Botswana — Gaborone
  "BW" to CapitalQuizMetadata(population = 273602L, areaKm2 = 169.0, notCoastal = true),
  // BR — Brazil — Brasília
  "BR" to CapitalQuizMetadata(population = 2982818L, areaKm2 = 5802.0, notCoastal = true),
  // BN — Brunei — Bandar Seri Begawan
  "BN" to CapitalQuizMetadata(population = 100700L, areaKm2 = 100.36, notCoastal = false),
  // BG — Bulgaria — Sofia
  "BG" to CapitalQuizMetadata(population = 1307439L, areaKm2 = 500.0, notCoastal = true),
  // BF — Burkina Faso — Ouagadougou
  "BF" to CapitalQuizMetadata(population = 2453496L, areaKm2 = 518.0, notCoastal = true),
  // BI — Burundi — Gitega
  "BI" to CapitalQuizMetadata(population = 135467L, areaKm2 = 22.0, notCoastal = true),
  // KH — Cambodia — Phnom Penh
  "KH" to CapitalQuizMetadata(population = 2281951L, areaKm2 = 679.0, notCoastal = true),
  // CM — Cameroon — Yaoundé
  "CM" to CapitalQuizMetadata(population = 2765568L, areaKm2 = 180.0, notCoastal = true),
  // CA — Canada — Ottawa
  "CA" to CapitalQuizMetadata(population = 1017449L, areaKm2 = 2970.3, notCoastal = true),
  // CV — Cape Verde — Praia
  "CV" to CapitalQuizMetadata(population = 159050L, areaKm2 = 102.6, notCoastal = false),
  // CF — Central African Republic — Bangui
  "CF" to CapitalQuizMetadata(population = 889231L, areaKm2 = 67.0, notCoastal = true),
  // TD — Chad — N'Djamena
  "TD" to CapitalQuizMetadata(population = 916000L, areaKm2 = 104.0, notCoastal = true),
  // CL — Chile — Santiago
  "CL" to CapitalQuizMetadata(population = 6310000L, areaKm2 = 641.0, notCoastal = true),
  // CN — China — Beijing
  "CN" to CapitalQuizMetadata(population = 21858000L, areaKm2 = 16410.5, notCoastal = true),
  // CO — Colombia — Bogotá
  "CO" to CapitalQuizMetadata(population = 7181469L, areaKm2 = 1587.0, notCoastal = true),
  // KM — Comoros — Moroni
  "KM" to CapitalQuizMetadata(population = 111326L, areaKm2 = 30.0, notCoastal = false),
  // CG — Republic of the Congo — Brazzaville
  "CG" to CapitalQuizMetadata(population = 1696392L, areaKm2 = 588.0, notCoastal = true),
  // CR — Costa Rica — San José
  "CR" to CapitalQuizMetadata(population = 342188L, areaKm2 = 44.62, notCoastal = true),
  // HR — Croatia — Zagreb
  "HR" to CapitalQuizMetadata(population = 769944L, areaKm2 = 641.2, notCoastal = true),
  // CU — Cuba — Havana
  "CU" to CapitalQuizMetadata(population = 2132183L, areaKm2 = 728.26, notCoastal = false),
  // CY — Cyprus — Nicosia
  "CY" to CapitalQuizMetadata(population = 326739L, areaKm2 = 20.08, notCoastal = true),
  // CZ — Czech Republic — Prague
  "CZ" to CapitalQuizMetadata(population = 1357326L, areaKm2 = 496.21, notCoastal = true),
  // DK — Denmark — Copenhagen
  "DK" to CapitalQuizMetadata(population = 638117L, areaKm2 = 90.01, notCoastal = false),
  // DJ — Djibouti — Djibouti City
  "DJ" to CapitalQuizMetadata(population = 604013L, areaKm2 = 200.0, notCoastal = false),
  // DM — Dominica — Roseau
  "DM" to CapitalQuizMetadata(population = 14725L, areaKm2 = 5.4, notCoastal = false),
  // DO — Dominican Republic — Santo Domingo
  "DO" to CapitalQuizMetadata(population = 1111838L, areaKm2 = 1502.0, notCoastal = false),
  // CD — DR Congo — Kinshasa
  "CD" to CapitalQuizMetadata(population = 12691000L, areaKm2 = 9965.0, notCoastal = true),
  // EC — Ecuador — Quito
  "EC" to CapitalQuizMetadata(population = 2800388L, areaKm2 = 197.5, notCoastal = true),
  // EG — Egypt — Cairo
  "EG" to CapitalQuizMetadata(population = 10107125L, areaKm2 = 3085.12, notCoastal = true),
  // SV — El Salvador — San Salvador
  "SV" to CapitalQuizMetadata(population = 570459L, areaKm2 = 72.25, notCoastal = true),
  // GQ — Equatorial Guinea — Ciudad de la Paz
  "GQ" to CapitalQuizMetadata(population = 2000L, areaKm2 = 81.5, notCoastal = true),
  // ER — Eritrea — Asmara
  "ER" to CapitalQuizMetadata(population = 963000L, areaKm2 = 45.0, notCoastal = true),
  // EE — Estonia — Tallinn
  "EE" to CapitalQuizMetadata(population = 438341L, areaKm2 = 159.2, notCoastal = false),
  // SZ — Eswatini — Mbabane
  "SZ" to CapitalQuizMetadata(population = 94874L, areaKm2 = 81.76, notCoastal = true),
  // ET — Ethiopia — Addis Ababa
  "ET" to CapitalQuizMetadata(population = 3040740L, areaKm2 = 527.0, notCoastal = true),
  // FJ — Fiji — Suva
  "FJ" to CapitalQuizMetadata(population = 93970L, areaKm2 = 26.24, notCoastal = false),
  // FI — Finland — Helsinki
  "FI" to CapitalQuizMetadata(population = 658864L, areaKm2 = 715.48, notCoastal = false),
  // FR — France — Paris
  "FR" to CapitalQuizMetadata(population = 2139907L, areaKm2 = 105.4, notCoastal = true),
  // GA — Gabon — Libreville
  "GA" to CapitalQuizMetadata(population = 703904L, areaKm2 = 65.42, notCoastal = false),
  // GM — Gambia — Banjul
  "GM" to CapitalQuizMetadata(population = 31301L, areaKm2 = 12.0, notCoastal = false),
  // GE — Georgia — Tbilisi
  "GE" to CapitalQuizMetadata(population = 1201769L, areaKm2 = 504.3, notCoastal = true),
  // DE — Germany — Berlin
  "DE" to CapitalQuizMetadata(population = 3677472L, areaKm2 = 891.3, notCoastal = true),
  // GH — Ghana — Accra
  "GH" to CapitalQuizMetadata(population = 2388000L, areaKm2 = 20.4, notCoastal = false),
  // GR — Greece — Athens
  "GR" to CapitalQuizMetadata(population = 637798L, areaKm2 = 38.96, notCoastal = false),
  // GD — Grenada — St. George's
  "GD" to CapitalQuizMetadata(population = 33734L, areaKm2 = 67.0, notCoastal = false),
  // GT — Guatemala — Guatemala City
  "GT" to CapitalQuizMetadata(population = 2934841L, areaKm2 = 997.0, notCoastal = true),
  // GN — Guinea — Conakry
  "GN" to CapitalQuizMetadata(population = 1660973L, areaKm2 = 450.0, notCoastal = false),
  // GW — Guinea-Bissau — Bissau
  "GW" to CapitalQuizMetadata(population = 492004L, areaKm2 = 77.5, notCoastal = false),
  // GY — Guyana — Georgetown
  "GY" to CapitalQuizMetadata(population = 118363L, areaKm2 = 70.0, notCoastal = false),
  // HT — Haiti — Port-au-Prince
  "HT" to CapitalQuizMetadata(population = 987310L, areaKm2 = 36.04, notCoastal = false),
  // HN — Honduras — Tegucigalpa
  "HN" to CapitalQuizMetadata(population = 1444085L, areaKm2 = 1502.0, notCoastal = true),
  // HU — Hungary — Budapest
  "HU" to CapitalQuizMetadata(population = 1706851L, areaKm2 = 525.2, notCoastal = true),
  // IS — Iceland — Reykjavík
  "IS" to CapitalQuizMetadata(population = 133262L, areaKm2 = 244.0, notCoastal = false),
  // IN — India — New Delhi
  "IN" to CapitalQuizMetadata(population = 249998L, areaKm2 = 42.7, notCoastal = true),
  // ID — Indonesia — Jakarta
  "ID" to CapitalQuizMetadata(population = 10562088L, areaKm2 = 660.98, notCoastal = false),
  // IR — Iran — Tehran
  "IR" to CapitalQuizMetadata(population = 8693706L, areaKm2 = 615.0, notCoastal = true),
  // IQ — Iraq — Baghdad
  "IQ" to CapitalQuizMetadata(population = 7682136L, areaKm2 = 673.0, notCoastal = true),
  // IE — Ireland — Dublin
  "IE" to CapitalQuizMetadata(population = 592713L, areaKm2 = 117.8, notCoastal = false),
  // IL — Israel — Jerusalem
  "IL" to CapitalQuizMetadata(population = 936425L, areaKm2 = 125.13, notCoastal = true),
  // IT — Italy — Rome
  "IT" to CapitalQuizMetadata(population = 2761632L, areaKm2 = 1287.36, notCoastal = false),
  // CI — Ivory Coast — Yamoussoukro
  "CI" to CapitalQuizMetadata(population = 361893L, areaKm2 = 2075.0, notCoastal = true),
  // JM — Jamaica — Kingston
  "JM" to CapitalQuizMetadata(population = 662491L, areaKm2 = 25.0, notCoastal = false),
  // JP — Japan — Tokyo
  "JP" to CapitalQuizMetadata(population = 14094034L, areaKm2 = 2194.07, notCoastal = false),
  // JO — Jordan — Amman
  "JO" to CapitalQuizMetadata(population = 4061150L, areaKm2 = 1680.0, notCoastal = true),
  // KZ — Kazakhstan — Astana
  "KZ" to CapitalQuizMetadata(population = 1511807L, areaKm2 = 810.2, notCoastal = true),
  // KE — Kenya — Nairobi
  "KE" to CapitalQuizMetadata(population = 4397073L, areaKm2 = 696.1, notCoastal = true),
  // KI — Kiribati — Tarawa
  "KI" to CapitalQuizMetadata(population = 70480L, areaKm2 = 31.02, notCoastal = false),
  // KW — Kuwait — Kuwait City
  "KW" to CapitalQuizMetadata(population = 55159L, areaKm2 = 860.0, notCoastal = false),
  // KG — Kyrgyzstan — Bishkek
  "KG" to CapitalQuizMetadata(population = 1074075L, areaKm2 = 386.0, notCoastal = true),
  // LA — Laos — Vientiane
  "LA" to CapitalQuizMetadata(population = 927724L, areaKm2 = 3920.0, notCoastal = true),
  // LV — Latvia — Riga
  "LV" to CapitalQuizMetadata(population = 605802L, areaKm2 = 304.0, notCoastal = false),
  // LB — Lebanon — Beirut
  "LB" to CapitalQuizMetadata(population = 361366L, areaKm2 = 19.0, notCoastal = false),
  // LS — Lesotho — Maseru
  "LS" to CapitalQuizMetadata(population = 330760L, areaKm2 = 137.5, notCoastal = true),
  // LR — Liberia — Monrovia
  "LR" to CapitalQuizMetadata(population = 1010970L, areaKm2 = 194.25, notCoastal = false),
  // LY — Libya — Tripoli
  "LY" to CapitalQuizMetadata(population = 1170000L, areaKm2 = 1507.0, notCoastal = false),
  // LI — Liechtenstein — Vaduz
  "LI" to CapitalQuizMetadata(population = 5774L, areaKm2 = 17.28, notCoastal = true),
  // LT — Lithuania — Vilnius
  "LT" to CapitalQuizMetadata(population = 576195L, areaKm2 = 401.0, notCoastal = true),
  // LU — Luxembourg — Luxembourg City
  "LU" to CapitalQuizMetadata(population = 136208L, areaKm2 = 51.46, notCoastal = true),
  // MG — Madagascar — Antananarivo
  "MG" to CapitalQuizMetadata(population = 1275207L, areaKm2 = 85.0, notCoastal = true),
  // MW — Malawi — Lilongwe
  "MW" to CapitalQuizMetadata(population = 989318L, areaKm2 = 727.79, notCoastal = true),
  // MY — Malaysia — Kuala Lumpur
  "MY" to CapitalQuizMetadata(population = 1782500L, areaKm2 = 243.0, notCoastal = true),
  // MV — Maldives — Malé
  "MV" to CapitalQuizMetadata(population = 211908L, areaKm2 = 11.22, notCoastal = false),
  // ML — Mali — Bamako
  "ML" to CapitalQuizMetadata(population = 1809106L, areaKm2 = 245.0, notCoastal = true),
  // MT — Malta — Valletta
  "MT" to CapitalQuizMetadata(population = 5827L, areaKm2 = 0.61, notCoastal = false),
  // MH — Marshall Islands — Majuro
  "MH" to CapitalQuizMetadata(population = 27797L, areaKm2 = 9.7, notCoastal = false),
  // MR — Mauritania — Nouakchott
  "MR" to CapitalQuizMetadata(population = 1195600L, areaKm2 = 1000.0, notCoastal = false),
  // MU — Mauritius — Port Louis
  "MU" to CapitalQuizMetadata(population = 147066L, areaKm2 = 46.7, notCoastal = false),
  // MX — Mexico — Mexico City
  "MX" to CapitalQuizMetadata(population = 9209944L, areaKm2 = 1485.0, notCoastal = true),
  // FM — Federated States of Micronesia — Palikir
  "FM" to CapitalQuizMetadata(population = 6964L, areaKm2 = 7.7, notCoastal = true),
  // MD — Moldova — Chișinău
  "MD" to CapitalQuizMetadata(population = 779300L, areaKm2 = 123.0, notCoastal = true),
  // MC — Monaco — Monaco
  "MC" to CapitalQuizMetadata(population = 38350L, areaKm2 = 2.08, notCoastal = false),
  // MN — Mongolia — Ulaanbaatar
  "MN" to CapitalQuizMetadata(population = 1466125L, areaKm2 = 4704.4, notCoastal = true),
  // ME — Montenegro — Podgorica
  "ME" to CapitalQuizMetadata(population = 190488L, areaKm2 = 108.0, notCoastal = true),
  // MA — Morocco — Rabat
  "MA" to CapitalQuizMetadata(population = 577827L, areaKm2 = 117.0, notCoastal = false),
  // MZ — Mozambique — Maputo
  "MZ" to CapitalQuizMetadata(population = 1124988L, areaKm2 = 347.69, notCoastal = false),
  // MM — Myanmar — Naypyidaw
  "MM" to CapitalQuizMetadata(population = 1160242L, areaKm2 = 7054.0, notCoastal = true),
  // NA — Namibia — Windhoek
  "NA" to CapitalQuizMetadata(population = 431000L, areaKm2 = 5133.0, notCoastal = true),
  // NR — Nauru — Yaren
  "NR" to CapitalQuizMetadata(population = 747L, areaKm2 = 1.5, notCoastal = false),
  // NP — Nepal — Kathmandu
  "NP" to CapitalQuizMetadata(population = 845767L, areaKm2 = 49.45, notCoastal = true),
  // NL — Netherlands — Amsterdam
  "NL" to CapitalQuizMetadata(population = 905234L, areaKm2 = 219.32, notCoastal = true),
  // NZ — New Zealand — Wellington
  "NZ" to CapitalQuizMetadata(population = 217000L, areaKm2 = 289.91, notCoastal = false),
  // NI — Nicaragua — Managua
  "NI" to CapitalQuizMetadata(population = 1055247L, areaKm2 = 267.0, notCoastal = true),
  // NE — Niger — Niamey
  "NE" to CapitalQuizMetadata(population = 1334984L, areaKm2 = 552.27, notCoastal = true),
  // NG — Nigeria — Abuja
  "NG" to CapitalQuizMetadata(population = 1235880L, areaKm2 = 1476.0, notCoastal = true),
  // KP — North Korea — Pyongyang
  "KP" to CapitalQuizMetadata(population = 2870000L, areaKm2 = 829.1, notCoastal = true),
  // MK — North Macedonia — Skopje
  "MK" to CapitalQuizMetadata(population = 544086L, areaKm2 = 571.46, notCoastal = true),
  // NO — Norway — Oslo
  "NO" to CapitalQuizMetadata(population = 697010L, areaKm2 = 480.0, notCoastal = false),
  // OM — Oman — Muscat
  "OM" to CapitalQuizMetadata(population = 1294101L, areaKm2 = 3500.0, notCoastal = false),
  // PK — Pakistan — Islamabad
  "PK" to CapitalQuizMetadata(population = 1014825L, areaKm2 = 220.15, notCoastal = true),
  // PW — Palau — Ngerulmud
  "PW" to CapitalQuizMetadata(population = 0L, areaKm2 = 0.45, notCoastal = true),
  // PS — Palestine — Ramallah
  "PS" to CapitalQuizMetadata(population = 38998L, areaKm2 = 16.3, notCoastal = true),
  // PA — Panama — Panama City
  "PA" to CapitalQuizMetadata(population = 1086990L, areaKm2 = 2082.0, notCoastal = false),
  // PG — Papua New Guinea — Port Moresby
  "PG" to CapitalQuizMetadata(population = 364145L, areaKm2 = 240.0, notCoastal = false),
  // PY — Paraguay — Asunción
  "PY" to CapitalQuizMetadata(population = 521559L, areaKm2 = 117.0, notCoastal = true),
  // PE — Peru — Lima
  "PE" to CapitalQuizMetadata(population = 10151000L, areaKm2 = 2672.3, notCoastal = false),
  // PH — Philippines — Manila
  "PH" to CapitalQuizMetadata(population = 1846513L, areaKm2 = 42.34, notCoastal = false),
  // PL — Poland — Warsaw
  "PL" to CapitalQuizMetadata(population = 1863056L, areaKm2 = 517.24, notCoastal = true),
  // PT — Portugal — Lisbon
  "PT" to CapitalQuizMetadata(population = 509614L, areaKm2 = 100.05, notCoastal = false),
  // QA — Qatar — Doha
  "QA" to CapitalQuizMetadata(population = 1186023L, areaKm2 = 132.0, notCoastal = false),
  // RO — Romania — Bucharest
  "RO" to CapitalQuizMetadata(population = 1716983L, areaKm2 = 240.0, notCoastal = true),
  // RU — Russia — Moscow
  "RU" to CapitalQuizMetadata(population = 13274285L, areaKm2 = 2561.4, notCoastal = true),
  // RW — Rwanda — Kigali
  "RW" to CapitalQuizMetadata(population = 1132686L, areaKm2 = 730.0, notCoastal = true),
  // KN — Saint Kitts and Nevis — Basseterre
  "KN" to CapitalQuizMetadata(population = 14000L, areaKm2 = 6.1, notCoastal = false),
  // LC — Saint Lucia — Castries
  "LC" to CapitalQuizMetadata(population = 20000L, areaKm2 = 79.0, notCoastal = false),
  // VC — Saint Vincent and the Grenadines — Kingstown
  "VC" to CapitalQuizMetadata(population = 12909L, areaKm2 = 14.8, notCoastal = false),
  // WS — Samoa — Apia
  "WS" to CapitalQuizMetadata(population = 41611L, areaKm2 = 123.81, notCoastal = false),
  // SM — San Marino — City of San Marino
  "SM" to CapitalQuizMetadata(population = 4061L, areaKm2 = 7.09, notCoastal = true),
  // ST — São Tomé and Príncipe — São Tomé
  "ST" to CapitalQuizMetadata(population = 71868L, areaKm2 = 17.0, notCoastal = false),
  // SA — Saudi Arabia — Riyadh
  "SA" to CapitalQuizMetadata(population = 7676654L, areaKm2 = 1973.0, notCoastal = true),
  // SN — Senegal — Dakar
  "SN" to CapitalQuizMetadata(population = 1438725L, areaKm2 = 79.83, notCoastal = false),
  // RS — Serbia — Belgrade
  "RS" to CapitalQuizMetadata(population = 1688667L, areaKm2 = 389.12, notCoastal = true),
  // SC — Seychelles — Victoria
  "SC" to CapitalQuizMetadata(population = 26450L, areaKm2 = 20.1, notCoastal = false),
  // SL — Sierra Leone — Freetown
  "SL" to CapitalQuizMetadata(population = 1055964L, areaKm2 = 82.48, notCoastal = false),
  // SG — Singapore — Singapore
  "SG" to CapitalQuizMetadata(population = 5917600L, areaKm2 = 735.7, notCoastal = false),
  // SK — Slovakia — Bratislava
  "SK" to CapitalQuizMetadata(population = 440948L, areaKm2 = 367.58, notCoastal = true),
  // SI — Slovenia — Ljubljana
  "SI" to CapitalQuizMetadata(population = 285604L, areaKm2 = 274.99, notCoastal = true),
  // SB — Solomon Islands — Honiara
  "SB" to CapitalQuizMetadata(population = 92344L, areaKm2 = 22.0, notCoastal = false),
  // SO — Somalia — Mogadishu
  "SO" to CapitalQuizMetadata(population = 2388000L, areaKm2 = 370.0, notCoastal = false),
  // ZA — South Africa — Pretoria
  "ZA" to CapitalQuizMetadata(population = 2921488L, areaKm2 = 687.54, notCoastal = true),
  // KR — South Korea — Seoul
  "KR" to CapitalQuizMetadata(population = 9508451L, areaKm2 = 605.21, notCoastal = true),
  // SS — South Sudan — Juba
  "SS" to CapitalQuizMetadata(population = 525953L, areaKm2 = 52.0, notCoastal = true),
  // ES — Spain — Madrid
  "ES" to CapitalQuizMetadata(population = 3305408L, areaKm2 = 604.31, notCoastal = true),
  // LK — Sri Lanka — Sri Jayawardenepura Kotte
  "LK" to CapitalQuizMetadata(population = 107925L, areaKm2 = 17.0, notCoastal = true),
  // SD — Sudan — Khartoum
  "SD" to CapitalQuizMetadata(population = 2682431L, areaKm2 = 322.7, notCoastal = true),
  // SR — Suriname — Paramaribo
  "SR" to CapitalQuizMetadata(population = 240924L, areaKm2 = 182.0, notCoastal = false),
  // SE — Sweden — Stockholm
  "SE" to CapitalQuizMetadata(population = 978770L, areaKm2 = 188.0, notCoastal = false),
  // CH — Switzerland — Bern
  "CH" to CapitalQuizMetadata(population = 134591L, areaKm2 = 51.62, notCoastal = true),
  // SY — Syria — Damascus
  "SY" to CapitalQuizMetadata(population = 2079000L, areaKm2 = 105.0, notCoastal = true),
  // TJ — Tajikistan — Dushanbe
  "TJ" to CapitalQuizMetadata(population = 863400L, areaKm2 = 203.0, notCoastal = true),
  // TZ — Tanzania — Dodoma
  "TZ" to CapitalQuizMetadata(population = 410956L, areaKm2 = 2576.0, notCoastal = true),
  // TH — Thailand — Bangkok
  "TH" to CapitalQuizMetadata(population = 8305218L, areaKm2 = 1568.74, notCoastal = false),
  // TL — Timor-Leste — Dili
  "TL" to CapitalQuizMetadata(population = 277279L, areaKm2 = 178.62, notCoastal = false),
  // TG — Togo — Lomé
  "TG" to CapitalQuizMetadata(population = 837437L, areaKm2 = 99.14, notCoastal = false),
  // TO — Tonga — Nukuʻalofa
  "TO" to CapitalQuizMetadata(population = 27600L, areaKm2 = 19.0, notCoastal = false),
  // TT — Trinidad and Tobago — Port of Spain
  "TT" to CapitalQuizMetadata(population = 37074L, areaKm2 = 12.0, notCoastal = false),
  // TN — Tunisia — Tunis
  "TN" to CapitalQuizMetadata(population = 638845L, areaKm2 = 212.0, notCoastal = false),
  // TR — Turkey — Ankara
  "TR" to CapitalQuizMetadata(population = 5747325L, areaKm2 = 25632.0, notCoastal = true),
  // TM — Turkmenistan — Ashgabat
  "TM" to CapitalQuizMetadata(population = 791000L, areaKm2 = 470.0, notCoastal = true),
  // TV — Tuvalu — Funafuti
  "TV" to CapitalQuizMetadata(population = 6320L, areaKm2 = 2.79, notCoastal = false),
  // UG — Uganda — Kampala
  "UG" to CapitalQuizMetadata(population = 1680600L, areaKm2 = 189.0, notCoastal = true),
  // UA — Ukraine — Kyiv
  "UA" to CapitalQuizMetadata(population = 2920873L, areaKm2 = 839.0, notCoastal = true),
  // AE — United Arab Emirates — Abu Dhabi
  "AE" to CapitalQuizMetadata(population = 1010092L, areaKm2 = 972.0, notCoastal = false),
  // GB — United Kingdom — London
  "GB" to CapitalQuizMetadata(population = 9002488L, areaKm2 = 1572.0, notCoastal = true),
  // US — United States — Washington, D.C.
  "US" to CapitalQuizMetadata(population = 670050L, areaKm2 = 177.0, notCoastal = true),
  // UY — Uruguay — Montevideo
  "UY" to CapitalQuizMetadata(population = 1319108L, areaKm2 = 201.0, notCoastal = false),
  // UZ — Uzbekistan — Tashkent
  "UZ" to CapitalQuizMetadata(population = 2860600L, areaKm2 = 631.29, notCoastal = true),
  // VU — Vanuatu — Port Vila
  "VU" to CapitalQuizMetadata(population = 51437L, areaKm2 = 23.6, notCoastal = false),
  // VA — Vatican City — Vatican City
  "VA" to CapitalQuizMetadata(population = 764L, areaKm2 = 0.49, notCoastal = true),
  // VE — Venezuela — Caracas
  "VE" to CapitalQuizMetadata(population = 2245744L, areaKm2 = 433.0, notCoastal = true),
  // VN — Vietnam — Hanoi
  "VN" to CapitalQuizMetadata(population = 8053663L, areaKm2 = 3358.6, notCoastal = true),
  // YE — Yemen — Sana'a
  "YE" to CapitalQuizMetadata(population = 2575347L, areaKm2 = 126.0, notCoastal = true),
  // ZM — Zambia — Lusaka
  "ZM" to CapitalQuizMetadata(population = 2731696L, areaKm2 = 360.0, notCoastal = true),
  // ZW — Zimbabwe — Harare
  "ZW" to CapitalQuizMetadata(population = 2123132L, areaKm2 = 982.3, notCoastal = true),
)

internal fun capitalQuizMetadata(code: String): CapitalQuizMetadata? = capitalQuizMetadataByCountryCode[code]

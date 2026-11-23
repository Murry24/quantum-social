# Stav práce – Quantum Social (21. 11. 2026 večer)

Tento súbor je „odovzdávka“ medzi sedeniami. Prečítaj ho ako prvý.

## Dohody s Marošom
- **Server až úplne na záver** (je platený). Dovtedy je všetko lokálne a „finálne bez servera“;
  ukážkové hviezdy (`DemoStars`) neskôr nahradí server.
- Maroš **netestuje každú zmenu** – body sa robia v balíkoch a **testujú sa spolu** v Android Studiu
  (emulátor Pixel 7 Pro); Claude z chatu ho vie spustiť sám cez počítač Maroša.
- Do `main` sa zlučuje **len po Marošovom OK** („OK, zlúč“).
- Prácu robí hlavne **Claude v cloude** (CI na GitHube overí ktlint, testy, build); notebook sa prehrieva.
- Tabuľka s úlohami a PR: artefakt „Quantum Social – stav pull requestov“ (hárky Úlohy, Test, Pull requesty).

## Čaká na spoločný test (stack PR, každý nad predchádzajúcim)
| PR | Body | Vetva |
|---|---|---|
| #16 | 32 farby galaxie | feat/32-farby-galaxie |
| #17 | 33 živá tapeta | feat/33-ziva-tapeta |
| #18 | 13, 14, 16, 21, 26 | feat/balik-13-14-16-21-26 |
| #19 | 12, 17 | feat/balik-12-17 |
| #20 | 28 (+ vlastný zvuk), 29, 30 | feat/balik-28-29-30 |
| #21 | 11, 15 | feat/balik-11-15 |
| #22 | 24, 27, 56 jazykov sveta | feat/balik-24-27-jazyky |
| #23 | 10, 22, 23 | feat/balik-10-22-23 |
| #24 | 19 (lokálne), 25 | feat/balik-19-25 |
| #25 | WishNet+ (témy, palce, splnené) | feat/wishnet-plus |
| #26 | Galaxie+ (motto, úprava, obľúbené, táborák) | feat/galaxie-plus |
| #27 | Profil+ (aura, odznaky, séria vďačnosti) | feat/profil-plus |
| #28 | Preklad 1: systém slovníkov + angličtina | feat/i18n-1 |
| #29 | Preklad 2: DE, ES, FR, CS, PL | feat/i18n-2 |
| #30 | Preklad 3: IT, PT, UK, RU, TR, ZH, JA, KO, HI, AR | feat/i18n-3 |
| #31 | Preklad 4: texty z core | feat/i18n-4 |
| #32 | Pamäť: galaxie priateľov po reštarte | feat/pamat-galaxii |
| #33 | Pamäť: priania z WishNetu po reštarte | feat/pamat-prani |
| #34 | Pamäť: priania s obrázkom | feat/pamat-obrazkov |
| #35 | ZenNet+ (dĺžka, dych, séria) | feat/zen-plus |
| #36 | Opravy po teste (ZenNet, séria) | fix/po-teste-2 |

**Na test stačí vetva `fix/po-teste-2`** (obsahuje všetko). Po OK zlúčiť v poradí #16 → #36
(každý PR pred zlúčením prepnúť na base `main`, merge commit, vetvy nemazať).
Pri teste v Android Studiu: po prepnutí vetvy **Sync**, pri čudných chybách **Build → Clean Project**.
Netestované zatiaľ: KVANT7 (vstup do galaxie priateľov), melódia/zvuk, tapeta, notifikácie.

## Ďalšie kroky (v tomto poradí)
1. Spoločný test + zlúčenie #16–#36.
2. Balík C: **19** lokálne (nahlásiť / zablokovať hviezdu), **25** preklady správ (prekladač v telefóne).
3. **Detailné vylepšenie každej časti** (Marošovo želanie: „nabité všetkým, na čo si používateľ spomenie“):
   WishNet → galaxie priateľov → profil → ZenNet → … každú časť do hĺbky.
4. **Preklad celej aplikácie** do jazykov sveta (texty do `strings.xml`, hlavné svetové jazyky).
5. **34 Luxusný vzhľad** – ladiť podľa Marošovho oka (screenshoty).
6. Rozhranie dátovej vrstvy pre server (aby sa pri prechode menila len jedna vrstva).
7. **Server** (body 9 zvyšok, 19 SMS, skutočné hviezdy) – až na úplný záver.

## Kde čo je (nové veci od #16)
- core/model: GalaxyTheme, WallpaperScene, StarLevel, Gratitude, DailyChallenge, LightSignature, MoodWaves,
  GalacticMoments, DreamMirror, StarChord (+ CustomSound), FutureSeed, Echo, GentleNotes, WorldLanguages,
  MemoryNebula, Connections (DemoStars, Serendipity, Constellation, SoulLinks); StarDust má `spent`, `alertDay`.
- core/data: DailyStores (GratitudeStore, ChallengeStore, SeedStore, EchoStore, ConnectionStore), StarDust.
- app: wallpaper/, audio/ (ToneSynth, CustomSoundStore), notify/GentleReminders, ui/screens/*Screens.kt,
  ui/nav/Actions.kt (✦ Vesmír), ui/onboarding/LanguagePicker.kt.
- Testy: core/src/test/.../Balik*Test.kt, StarDustTest, HappyGalaxyTest, WallpaperSceneTest.

## Mimo Quantum Social
- Eurojackpot feed funguje sám (robot zapisuje ťahy). Skontrolovať okolo 2. 1. 2027, či vznikne history_2027.csv.

## Preklad aplikácie (i18n)
- Texty v kóde: `tr("slovenský text")` alebo `tr("text %s", x)` (import `app.quantumsocial.core.i18n.tr`).
- Slovníky: `app/src/main/assets/i18n/<iso>.tsv` (slovensky TAB preklad), nový jazyk pridať aj do `I18n.available`.
- `I18nTest` stráži počet `%s`. Preložené aj texty z `core` (17 jazykov, 423 textov). Nový text = pridať riadok do všetkých .tsv (test to vyžaduje).
- Pri kontrole dĺžky riadku počítať emoji ako 2 znaky (ktlint počíta UTF-16).

## Test v emulátore 23. 11. (Claude cez Android Studio)
- OK: angličtina podľa telefónu, Profil (level, StarDust, aura), ✦ menu, ZenNet+ (dĺžka, dych 4–7–8, odpočet, séria),
  WishNet (emoji prianie s témou, „Splnilo sa“), priania po reštarte ostali.
- Neotestované (treba písať alebo rolovať – Claude v Android Studiu nevie): odznaky a výber jazyka dole v Profile,
  preklad textu, nahlásenie/blokovanie, galaxie (meno, KVANT7), arabčina sprava doľava.
- Build na notebooku ~20 min (Kotlin daemon sa nepripojil → pomalá náhradná kompilácia).

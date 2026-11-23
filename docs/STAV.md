# Stav práce – Quantum Social (23. 11. 2026 večer)

Tento súbor je „odovzdávka“ medzi sedeniami. Prečítaj ho ako prvý.

## Dohody s Marošom
- **Server až úplne na záver** (je platený). Dovtedy je všetko lokálne a „finálne bez servera“;
  ukážkové hviezdy (`DemoStars`) neskôr nahradí server.
- Maroš **netestuje každú zmenu** – body sa robia v balíkoch a **testujú sa spolu** v Android Studiu
  (emulátor Pixel 7 Pro); Claude z chatu ho vie spustiť sám cez počítač Maroša.
- Do `main` sa zlučuje **len po Marošovom OK** („OK, zlúč“).
- Prácu robí hlavne **Claude v cloude** (CI na GitHube overí ktlint, testy, build); notebook sa prehrieva.
- Tabuľka s úlohami a PR: artefakt „Quantum Social – stav pull requestov“ (hárky Úlohy, Test, Pull requesty).

## Zlúčené v `main` (23. 11. 2026)
PR #16 – #41 sú zlúčené (Maroš dal OK). `main` = všetko hotové; CI na `main` zelené.
Obsah: body 32, 33, 10–17, 19 (lokálne), 21–30, WishNet+, Galaxie+, Profil+, preklad 17 jazykov,
pamäť po reštarte, ZenNet+, Vďačnosť+, Semienka ako ciele, DreamMirror+ a Snové kruhy.

**Na test: vetva `main`.** Nové práce = nová vetva z `main` → PR do `main`.

## Plán ďalej (dohodnuté poradie 23. 11.)
1. ✅ Poriadok – zlúčenie #16–#41.
2. Prvé kroky – sprievodca po zrodení hviezdy.
3. Moje dáta – zmazať všetko / stiahnuť moje dáta + zásady súkromia.
4. Prehľadnejšie menu ✦ (skupiny Ja / Spojenia / Pokoj).
5. Bod 34 – luxusný vzhľad (2–3 návrhy pre Maroša).
Potom: widget, preklad v snových kruhoch, EchoNet+ a Súhvezdia+, ročný prehľad, príprava na server a Google Play.
Pri teste v Android Studiu: po prepnutí vetvy **Sync**, pri čudných chybách **Build → Clean Project**.
Netestované zatiaľ: KVANT7 (vstup do galaxie priateľov), melódia/zvuk, tapeta, notifikácie.

## Ďalšie kroky (v tomto poradí)
1. Spoločný test + zlúčenie #16–#41.
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

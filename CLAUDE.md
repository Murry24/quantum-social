# Quantum Social – pokyny pre Claude

Komunikuj s Marošom (vlastník projektu) **po slovensky**, jednoducho a bez zbytočného žargónu.
Nie je profesionálny programátor – vysvetľuj, čo si spravil a ako to vyskúša.

## Čo je Quantum Social
Nový druh sociálnej siete, nie klasický feed. Každý používateľ je **anonymná hviezda** na
farebnej (veselej, nie temnej) galaxii. Hviezdy si posielajú **signály a pocity do vesmíru**,
ktoré sú **efemérne** – po prečítaní alebo po čase zhasnú a zmiznú. Žiadne mená, fotky
profilov ani história navždy. Základ je pocit a svetlo, slová sú druhoradé.

Celý plán má 30 bodov → `docs/PLAN.md` (tam je aj stav každého bodu).

## Technológie
- Android natívne: **Kotlin + Jetpack Compose + Material 3**, minSdk 26
- Moduly: `app` (obrazovky, UI) a `core` (modely, dáta, téma)
- Balík: `app.quantumsocial`
- Dáta zatiaľ **len lokálne** (`core/data/InMemorySignalRepository`) – žiadny server
- Verzie knižníc: `gradle/libs.versions.toml`

## Štruktúra
```
app/src/main/java/app/quantumsocial/
  nav/          navigácia (MainNavHost, Screen)
  ui/screens/   obrazovky (StarMapScreen, StarProfileScreen, WishScreen, ...)
  ui/canvas/    kreslenie galaxie (NebulaBackground, StarCanvas)
  ui/components/ malé UI prvky (LegendEmotions, MiniKnobs, ZoomThinBar)
  ui/signals/   UI typov signálov (SignalText, SignalEmoji, SignalFlash, WishNetCard, ...)
  ui/model/     StarPoint (hviezda na mape)
core/src/main/java/app/quantumsocial/core/
  model/        Signal (+ typy), Emotion, SignalLifecycle
  data/         InMemorySignalRepository
  ui/theme/     farby, téma
```

## Pravidlá práce
1. **Nikdy necommituj priamo do `main`.** Každý bod plánu = nová vetva
   `feat/<číslo>-<názov>` (napr. `feat/8-send-signal`) → pull request do `main`.
2. Pred pushom musí prejsť (to isté kontroluje GitHub CI v `.github/workflows/android-ci.yml`):
   ```
   ./gradlew ktlintCheck
   ./gradlew :core:testDebugUnitTest
   ./gradlew assembleDebug
   ```
   ktlint: max dĺžka riadku 120, komentáre nie za parametrami na tom istom riadku, trailing commas.
3. Malé kroky, po každom funkčnom kroku commit. Commit správy po slovensky, napr.
   `feat(8): dialóg poslať hviezde / do chaosu`.
4. Logiku (výpočty, pravidlá, časovanie) dávaj do `core` a pokry ju **unit testami**
   v `core/src/test`. UI v `app`.
5. Neprepisuj veľké časti existujúceho kódu bez dôvodu – Maroš ho písal ručne.
6. Žiadne tajné kľúče/heslá do repozitára.
7. Na konci úlohy napíš Marošovi: čo pribudlo, ako to vyskúšať v aplikácii (kde kliknúť),
   a link na pull request.
8. Aplikáciu Maroš spúšťa v **Android Studiu** na emulátore Pixel 7 Pro.

## Lokálne prostredie (Windows)
- **Java pre Gradle = JDK 17** (Temurin, rovnaké ako GitHub CI):
  `C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot`.
  - Nastavené v `JAVA_HOME` (používateľská premenná) aj v `.gradle\config.properties` (`java.home=...`).
  - **Nie Java 25** (`jbr` v novom Android Studiu `C:\Program Files\Android\Android Studio1\jbr`):
    Gradle 8.13 na nej nebeží. Starý `JAVA_HOME` ukazoval na neexistujúci
    `...\Android Studio\jbr` → „Gradle project sync failed“ bez textu (chyba je v
    `%LOCALAPPDATA%\Google\AndroidStudio*\log\idea.log`: „supplied javaHome seems to be invalid“).
  - V termináli, ktorý bol otvorený pred zmenou `JAVA_HOME`, nastav pre reláciu:
    `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"`.
- **Windows Defender** spomaľuje/blokuje Gradle cache – odporúčané výnimky v Defenderi:
  `%USERPROFILE%\.gradle`, priečinok projektu, `%LOCALAPPDATA%\Android\Sdk`.
- Počítač je slabší: nespúšťaj viac Gradle buildov naraz, na konci práce `.\gradlew.bat --stop`.
- **Emulátor spúšťa Maroš v Android Studiu** (panel Running Devices), nie Claude cez `emulator.exe`.
  Ak už beží (`adb devices`), nový nespúšťaj. Aplikácia sa spúšťa ▶ Run v Android Studiu.

## Aktuálny stav (19. 11. 2026)
- Hotové v `main`: body 1, 2, väčšina 4, rozpracovaný 5 (profil hviezdy).
- **PR #4 (`feat/3-signal-lifecycle`)**: bod 3 – životnosť signálov (stav „končí“,
  animácia zhasnutia, `SignalLifecycle` + testy). CI zelené, čaká na Marošovo vyskúšanie
  a zlúčenie. Ďalšie body staviaj na `main` až po jeho zlúčení (alebo na tejto vetve).
- **PR #6 (`feat/8-send-signal`, nadväzuje na PR #4)**: bod 8 – ťuk na hviezdu → panel s emóciami
  → pulz letí k hviezde; tlačidlo „🌌 Do chaosu“ (náhodná hviezda); „○ ZenNet“ = minúta ticha
  (`ZenScreen`). Logika v `core`: `PulseSignal`, `SignalTarget`, `ChaosPicker`, `PulseOutbox`,
  `ZenSession` + testy. Dlhé podržanie hviezdy = profil.
- Ďalej: dokončiť 4 a 5, potom 6 (úvodná obrazovka) a 18 (onboarding).

## Dohodnuté smerovanie
- Prvá verzia bez servera – všetko lokálne, server (bod 9, 19) až keď bude jadro príjemné.
- Pri bode 2 sú text/audio/obrázok pripravené, ale dôraz je na **pocitoch, svetle a emóciách**.
- Jadro MVP podľa plánu: 1 → 4 → 5 → 8 → 9 → 18 → 19 → 20.

# Quantum Social – plán vývoja (30 bodov)

Stav: ✅ hotové · 🟡 rozpracované · ⬜ nezačaté

**Poradie jadra (MVP):** 1 → 4 → 5 → 8 → 9 → 18 → 19 → 20
Potom siete (11, 14, 13, 12), interakcie (10, 23, 22), estetika (21, 28),
kolektívne (17, 26, 27, 30), archív (24), preklady (25).
Dáta najprv lokálne (mock), server až od bodu 9.

| # | Bod | Cieľ | Stav |
|---|-----|------|------|
| 1 | Core definícia | Projekt Kotlin + Compose, moduly, CI, ktlint, galaxia s pulzujúcou hviezdou | ✅ |
| 2 | Formy správ | Typy signálov: text (200 zn.), emoji, audio 15 s, svetelný záblesk, obrázok, mix, WishNet; jednotné rozhranie `Signal` | ✅ |
| 3 | Životnosť správ | Bežné signály miznú po prečítaní/čase, WishNet dlhšie; stav „končí“, animácia zhasnutia | 🟡 PR #4 |
| 4 | Zobrazenie a filtre | Mapa hviezd (náhodné rozmiestnenie, zoom/pan) + filtre: jazyk, emócia, téma, intenzita, vzdialenosť, WishNet | 🟡 takmer hotové |
| 5 | Profil hviezdy | Anonymný detail: kvanti-meno, jazyk, emócie, témy, posledné signály, archív prianí; akcie odpovedať / sledovať žiarenie | 🟡 PR #7 |
| 6 | Úvodná obrazovka | Finálny „veselý“ vizuál galaxie (gradienty, svetlušky), šetrič výkonu | 🟡 PR #8 |
| 7 | Odpoveď a sledovanie žiarenia | Krátke vlákno odpovedí, ktoré zhasne; „stopy“ hviezdy (nie follow) | ⬜ |
| 8 | Posielanie správ + ZenNet | Klik na hviezdu → poslať hviezde / do chaosu; ZenNet = 1 min ticha s bublinami | 🟡 PR #6 |
| 9 | Bezpečnosť a anonymita | API kontrakty, šifrovanie E2E (koncept), kľúče v Keystore, minimum dát | ⬜ |
| 10 | Serendipity | Denné náhodné párovanie 1:1 na 24 h („svetelný most“) | ⬜ |
| 11 | EchoNet | Zvuky dňa: nahrávka 5–15 s, prehrávanie, tag nálady | ⬜ |
| 12 | DreamMirror | Zápis snov, podobnosti snov („snový most“) | ⬜ |
| 13 | ChallengeNet | Globálna denná výzva (24 h), galéria splnení | ⬜ |
| 14 | GratNet | 1 zápis vďačnosti denne, zlaté iskry, podobnosti | ⬜ |
| 15 | Notifikácie | Jemné, poetické (pulz/iskra namiesto červených odznakov), voliteľný push | ⬜ |
| 16 | Svetelné levely | Iskra → Hviezda → Pulzár → Hmlovina → Supernova podľa aktivity | ⬜ |
| 17 | Galactic Moments | Kolektívne globálne udalosti (zmena farieb, téma dňa) | ⬜ |
| 18 | Onboarding | „Zrodenie hviezdy“ za 60 s: jazyk, emócia, kvanti-meno, obnovovací kľúč, tutoriál | 🟡 PR #9 |
| 19 | Overenie a Aurora | 1 zariadenie = 1 hviezda, SMS overenie, captcha, moderácia, report/blok | ⬜ |
| 20 | StarDust | Jediná (nepredajná) mena za pozitívne interakcie; spúšťač Galactic Moments | ⬜ |
| 21 | LightSignature | Jedinečný svetelno-zvukový podpis hviezdy | ⬜ |
| 22 | Constellations | Dočasné skupiny do 10 hviezd na tému/emóciu (3 dni) | ⬜ |
| 23 | SoulLinks | Dlhodobé 1:1 kanály po obojstrannom súhlase (max 5) | ⬜ |
| 24 | Memory Nebula | Súkromný šifrovaný archív výnimočných momentov | ⬜ |
| 25 | Lingua Bridges | Voliteľné AI preklady (originál ↔ preklad) | ⬜ |
| 26 | Mood Waves | Živé globálne vlny nálad „tu a teraz“ | ⬜ |
| 27 | Pulse Alerts | Zvýraznený naliehavý signál, stojí StarDust, max 1/deň | ⬜ |
| 28 | StarChords | Osobná melódia hviezdy, zmena raz za kvartál | ⬜ |
| 29 | Empatický StarDust | Bonus StarDust za pomoc iným + aurora efekt | ⬜ |
| 30 | FutureSeeds → FutureBlooms | Semienka budúcnosti, ktoré rastú podporou komunity | ⬜ |

## Poznámky
- Pri každom bode: úlohy rozdelené na Android / Backend / Design, výstup = funkčné demo
  v aplikácii, akceptačné kritériá v popise pull requestu.
- Testy: unit testy logiky v `core`, neskôr UI testy (Compose).
- Citlivé veci (šifrovanie, SMS overenie, sny, archív) až po hotovom jadre – GDPR.

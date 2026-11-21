# Bod 9 – Bezpečnosť a anonymita (koncept)

Stav: **lokálna časť hotová** (kľúč v Android Keystore). Server príde až s bodmi 19 a ďalej.

## Čo platí už teraz
- **Obnovovací kľúč** (8 slov) je na disku len zašifrovaný kľúčom z Android Keystore
  (`KeyVault`, AES-256-GCM). Samotný Keystore kľúč zariadenie nikdy nepustí von.
- Zašifrovaný kľúč je v samostatnom súbore `quantum_vault`, ktorý sa **nezálohuje do cloudu
  ani neprenáša** na nový telefón (`backup_rules.xml`, `data_extraction_rules.xml`).
- Starší nešifrovaný kľúč (pred bodom 9) sa pri prvom spustení presunie do trezoru a zmaže.
- Kľúč sa nikdy neloguje ani neposiela.

## Minimum dát (pravidlo pre server)
Server smie poznať len to, bez čoho signál nedoletí:

| Údaj | Na serveri? | Prečo |
| --- | --- | --- |
| id hviezdy (náhodné UUID) | áno | adresa, kam signál letí |
| verejný kľúč hviezdy | áno | aby sa dal signál zašifrovať pre príjemcu |
| obsah signálu | **len zašifrovaný** | server ho nevie prečítať (E2E) |
| emócia, jazyk, téma | áno, hrubo | mapa a filtre; bez väzby na človeka |
| čas vytvorenia, životnosť | áno | aby signál na serveri včas zmizol |
| meno, e-mail, telefón, poloha | **nie** | aplikácia je anonymná |
| obnovovací kľúč | **nie** | zostáva len u používateľa |
| história, „kto komu“ | **nie** | po zhasnutí signálu sa maže aj na serveri |

## Návrh API (náčrt)
```
POST /stars                     { publicKey }                 → { starId }
GET  /galaxy?emotion=&lang=     → [ { starId, emotion, intensity, ttl } ]
POST /signals                   { to, sealedPayload, emotion, ttlMs }
GET  /signals/inbox             → [ { id, sealedPayload, emotion, expiresAt } ]
POST /signals/{id}/read         → signál sa na serveri hneď maže
POST /galaxies/{id}/invites     → { code, expiresAt }          (čierna diera)
```
Všetko cez HTTPS, prihlásenie podpisom kľúčom hviezdy (bez hesla a e-mailu).

## E2E šifrovanie (koncept)
- Každá hviezda má pár kľúčov (X25519) v Keystore; verejná časť ide na server.
- Signál sa šifruje pre verejný kľúč príjemcu, server prenáša len zašifrovaný balík.
- Do chaosu: server vyberie hviezdu a vráti jej verejný kľúč, až potom sa šifruje.

## Ešte otvorené (neskôr)
- väčší zoznam slov pre kľúč (teraz 64 slov ≈ 47 bitov, pre server treba ~2048 slov ako BIP39)
- obnova hviezdy na novom telefóne cez 8 slov (bod 19)
- overenie 1 zariadenie = 1 hviezda, moderácia (bod 19)

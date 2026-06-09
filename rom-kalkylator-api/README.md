# ROM kalkylator

Ett litet Spring Boot-API som läser in en deltagar-Excel (export från Rusta och
matcha) och räknar ut **dagsersättningen** verksamheten får per deltagare under en
månad, samt en totalsumma för hela filen.

Filen läses in en gång i månaden. För varje deltagare räknas antalet **helgfria
vardagar** (måndag–fredag, exklusive svenska röda dagar) inom deltagarens
start–slutperiod som infaller under den valda månaden. Beloppet bestäms av
deltagarens nivå.

## Ersättningsnivåer

| Nivå | Belopp per helgfri vardag |
|------|---------------------------|
| A    | 60 kr                     |
| B    | 80 kr                     |
| C    | 90 kr                     |

## Röda dagar

Endast officiella svenska helgdagar räknas bort (utöver lördag/söndag):
nyårsdagen, trettondedag jul, långfredagen, påskdagen, annandag påsk, första maj,
Kristi himmelsfärds dag, pingstdagen, nationaldagen, midsommardagen, alla helgons
dag, juldagen och annandag jul. Helgaftnar (julafton, midsommarafton, nyårsafton)
räknas **inte** som röda dagar. Rörliga helgdagar beräknas utifrån påskdagen
(Gauss/Meeus-algoritmen), så valfritt årtal stöds.

## Excel-format

Första raden är rubriker; data läses från rad 2. Relevanta kolumner:

| Kolumn | Innehåll      |
|--------|---------------|
| D      | Förnamn       |
| E      | Efternamn     |
| F      | Personnummer  |
| M      | Nivå (A/B/C)  |
| O      | Startdatum (YYYY-MM-DD) |
| P      | Slutdatum (YYYY-MM-DD)  |

Rader med ogiltig eller saknad nivå/datum hoppas över och listas under `varningar`
i svaret.

## API

### `POST /api/rom/berakna`

Multipart-uppladdning.

| Parameter | Typ  | Beskrivning |
|-----------|------|-------------|
| `file`    | fil  | `.xlsx`-filen med deltagare (obligatorisk) |
| `manad`   | text | Målmånad `YYYY-MM`. Valfri – standard är innevarande månad. |

Exempel:

```bash
curl -F "file=@deltagareexport.xlsx" \
     -F "manad=2026-06" \
     http://localhost:8080/api/rom/berakna
```

Svar (förkortat):

```json
{
  "manad": "2026-06",
  "antalDeltagare": 12,
  "totalErsattaDagar": 88,
  "totalBelopp": 7040,
  "deltagare": [
    {
      "fornamn": "Maria",
      "efternamn": "Asplund",
      "personnummer": "197403199321",
      "niva": "A",
      "dagsbelopp": 60,
      "ersattaDagar": 4,
      "belopp": 240,
      "kommentar": null
    }
  ],
  "varningar": []
}
```

## Köra lokalt

```bash
mvn spring-boot:run
```

## Tester

```bash
mvn test
```

# API Testing Practice

Ett övningsprojekt för dig som vill lära dig grunderna i API-testning med **Postman**. Projektet är en Spring Boot-applikation som simulerar en bokhandel. Den exponerar ett REST-API med endpoints för CRUD, autentisering, validering, query-parametrar, olika statuskoder och olika content-types.

## Innehåll

- [Förutsättningar](#förutsättningar)
- [Komma igång](#komma-igång)
- [Importera Postman-collection](#importera-postman-collection)
- [API-översikt](#api-översikt)
- [Övningar](#övningar)
- [Postman-tester (test-script)](#postman-tester-test-script)
- [Felsökning](#felsökning)

## Förutsättningar

- **Java 17** eller senare ([ladda ner Adoptium](https://adoptium.net/))
- **Maven 3.8+** (eller använd Maven Wrapper om du föredrar det)
- **Postman** ([ladda ner](https://www.postman.com/downloads/))

Kontrollera att Java fungerar:
```bash
java -version
```

## Komma igång

1. Klona/öppna projektet och gå in i mappen:
   ```bash
   cd API-testing
   ```

2. Bygg och starta applikationen:
   ```bash
   mvn spring-boot:run
   ```

3. När du ser `Started ApiTestingApplication` i loggen är API:et igång på `http://localhost:8080`.

4. Testa snabbt med curl:
   ```bash
   curl http://localhost:8080/api/status/health
   curl http://localhost:8080/api/books
   ```

## Importera Postman-collection

I mappen `postman/` finns två filer du kan importera direkt i Postman:

1. Öppna Postman
2. Klicka på **Import** (uppe till vänster)
3. Dra in eller välj båda filerna:
   - `API-Testing-Practice.postman_collection.json` (alla requests + testskript)
   - `API-Testing-Practice.postman_environment.json` (variabler för bas-URL, login etc.)
4. Välj environment **"API Testing Practice - Local"** uppe till höger i Postman

Nu kan du köra requesterna direkt. Varje request har förinställda test-script som verifierar svaret.

## API-översikt

Bas-URL: `http://localhost:8080`

### Böcker (öppen, ingen auth)

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/books` | Lista alla böcker (stödjer `?genre=`, `?author=`, `?minPrice=`, `?maxPrice=`, `?sortBy=price\|title\|author`) |
| GET | `/api/books/{id}` | Hämta en bok via id |
| GET | `/api/books/count` | Räkna antalet böcker |
| GET | `/api/books/search?title=` | Sök på exakt titel |
| POST | `/api/books` | Skapa en ny bok |
| PUT | `/api/books/{id}` | Ersätt en bok komplett |
| PATCH | `/api/books/{id}` | Uppdatera vissa fält |
| DELETE | `/api/books/{id}` | Ta bort en bok |

### Autentisering

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| POST | `/api/auth/login` | Logga in, returnerar en token |
| POST | `/api/auth/logout` | Logga ut (kräver Bearer token) |
| GET | `/api/auth/me` | Hämta info om inloggad användare (Bearer token) |
| GET | `/api/auth/basic` | Endpoint som kräver **Basic Auth** |
| GET | `/api/auth/api-key` | Endpoint som kräver header `X-API-Key` |

**Inloggningsuppgifter:**
- Användarnamn: `admin`
- Lösenord: `hemligt123`
- API-nyckel: `test-api-key-12345`

### Användare (kräver Bearer token, förutom POST)

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/users` | Lista alla användare |
| GET | `/api/users/{id}` | Hämta en användare |
| POST | `/api/users` | Registrera en ny användare |
| PUT | `/api/users/{id}` | Uppdatera en användare |
| DELETE | `/api/users/{id}` | Ta bort en användare |

### Status & headers (för övning på olika svar)

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/status/health` | Healthcheck (200) |
| GET | `/api/status/{code}` | Returnerar valfri statuskod, t.ex. `/api/status/418` |
| GET | `/api/status/delay/{sec}` | Fördröjt svar 0-10 sek (testa timeouts) |
| GET | `/api/status/headers` | Returnerar dina request-headers |
| GET | `/api/status/custom-headers` | Innehåller custom response-headers |
| GET | `/api/status/xml` | Returnerar XML |
| GET | `/api/status/text` | Returnerar plain text |
| POST | `/api/status/echo` | Returnerar din request body |

## Övningar

Övningarna är ordnade från grundläggande till mer avancerade. Försök göra dem **i Postman** och skriv testscript där det är lämpligt.

### Övning 1 - HTTP-metoderna (grunderna)
- Skicka en `GET` till `/api/books` och inspektera svaret
- Skicka `GET /api/books/1` - vilka fält finns på boken?
- Skicka `POST /api/books` med en ny bok i body (Content-Type: application/json)
- Skicka `PUT /api/books/1` och ersätt boken helt
- Skicka `PATCH /api/books/1` och uppdatera bara priset
- Skicka `DELETE /api/books/1`

### Övning 2 - Path- och query-parametrar
- Hämta endast böcker i genren "Programming": `GET /api/books?genre=Programming`
- Hämta böcker av en författare och sortera på pris: `GET /api/books?author=Tolkien&sortBy=price`
- Kombinera flera filter: `GET /api/books?minPrice=100&maxPrice=300&sortBy=title`

### Övning 3 - Statuskoder
- Hämta en bok som inte finns: `GET /api/books/9999` → ska ge **404**
- Skapa en bok som lyckas: `POST /api/books` → ska ge **201**
- Ta bort en bok: `DELETE /api/books/2` → ska ge **204**
- Pröva olika statuskoder: `GET /api/status/418`, `GET /api/status/500`

### Övning 4 - Validering (400 Bad Request)
- Skicka `POST /api/books` med tom titel, tom författare och pris = -10
  - Inspektera fältet `details` i felsvaret - det visar exakt vilka regler som bröts
- Skicka `POST /api/users` med en ogiltig email-adress (t.ex. "blabla")
- Skicka felaktig JSON (t.ex. ofullständig `{"title":`) - du ska få **400**

### Övning 5 - Autentisering med Bearer Token
1. `POST /api/auth/login` med rätt användarnamn/lösenord. Spara token från svaret.
2. Anropa `GET /api/users` med headern `Authorization: Bearer <din-token>` - ska lyckas
3. Anropa samma endpoint utan token - ska ge **401**
4. Använd `POST /api/auth/logout`, försök sedan använda samma token igen → **401**

### Övning 6 - Basic Auth
- I Postman, gå till fliken **Authorization** för requesten
- Välj typ **Basic Auth** och fyll i `admin` / `hemligt123`
- Skicka `GET /api/auth/basic`

### Övning 7 - API-nyckel
- Skicka `GET /api/auth/api-key` med headern `X-API-Key: test-api-key-12345`
- Prova med en felaktig nyckel - ska ge 401

### Övning 8 - Headers
- Skicka `GET /api/status/headers` och se vilka headers Postman skickar
- Skicka `GET /api/status/custom-headers` och inspektera **Response Headers** i Postman

### Övning 9 - Olika content-types
- `GET /api/status/xml` - ska returnera XML
- `GET /api/status/text` - ska returnera plain text
- `POST /api/status/echo` med en JSON-body - svaret ekar tillbaka din body

### Övning 10 - Postman Tests (testskript)
Skriv test-script under fliken **Tests** för en request. Exempel:

```javascript
pm.test("Status är 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Svaret är ett JSON-objekt", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('object');
});

pm.test("Boken har en titel", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.title).to.be.a('string');
});

pm.test("Response time är under 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

### Övning 11 - Kedja requests med variabler
1. Skapa en bok med `POST /api/books`. I **Tests**-fliken, spara id:
   ```javascript
   var jsonData = pm.response.json();
   pm.collectionVariables.set("bookId", jsonData.id);
   ```
2. Använd `{{bookId}}` i nästa request: `GET /api/books/{{bookId}}`
3. Avsluta med att ta bort den: `DELETE /api/books/{{bookId}}`

### Övning 12 - Collection Runner
- Klicka på din collection → **Run**
- Kör hela serien av requests i ordning - se alla tester gröna/röda
- Detta är grunden för **regression testing**.

### Övning 13 - Negativa tester (saker som ska gå fel)
- Ta bort en redan borttagen bok → 404
- Skapa en användare med email som redan finns → 409
- Skapa en bok utan Content-Type-header (försök skicka rå text) → 400/415
- Skicka en token efter logout → 401

## Postman-tester (test-script)

I `postman/API-Testing-Practice.postman_collection.json` finns redan färdiga tester för de flesta requests. När du importerat collectionen i Postman:

1. Öppna en request
2. Gå till fliken **Tests** (eller **Scripts → Post-response** i nyare Postman)
3. Där ser du JavaScript-koden som körs efter svaret

Vanliga assertions att lära sig:

```javascript
// Statuskod
pm.test("Status är 200", () => pm.response.to.have.status(200));

// Response time
pm.test("Snabbt svar", () => pm.expect(pm.response.responseTime).to.be.below(1000));

// JSON-struktur
const json = pm.response.json();
pm.test("Har fältet 'id'", () => pm.expect(json).to.have.property('id'));
pm.test("count är ett nummer", () => pm.expect(json.count).to.be.a('number'));

// Header
pm.test("Returnerar JSON", () => {
    pm.expect(pm.response.headers.get('Content-Type')).to.include('application/json');
});

// Spara värde till variabel
pm.collectionVariables.set("token", json.token);
```

## Felsökning

**Port 8080 är upptagen?**
Ändra port i `src/main/resources/application.properties`:
```
server.port=8081
```
och uppdatera `baseUrl` i Postman till `http://localhost:8081`.

**Maven hittas inte?**
Installera Maven från [maven.apache.org](https://maven.apache.org/install.html) eller använd Maven Wrapper:
```bash
./mvnw spring-boot:run
```

**Data försvinner när jag startar om?**
Det är meningen - allt lagras i minnet. Vid omstart laddas testdatan om från scratch så du alltid kan börja på en känd grund.

## Projektstruktur

```
API-testing/
├── pom.xml
├── README.md
├── postman/
│   ├── API-Testing-Practice.postman_collection.json
│   └── API-Testing-Practice.postman_environment.json
└── src/
    ├── main/
    │   ├── java/com/apitesting/
    │   │   ├── ApiTestingApplication.java
    │   │   ├── controller/      (BookController, UserController, AuthController, StatusController)
    │   │   ├── model/           (Book, User, LoginRequest, LoginResponse, ErrorResponse)
    │   │   ├── service/         (BookService, UserService, AuthService)
    │   │   └── exception/       (ResourceNotFoundException, GlobalExceptionHandler m.fl.)
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/apitesting/
            └── ApiTestingApplicationTests.java
```

Lycka till med övningarna!

# API Testing Practice – Fullstack-träningsprojekt

En träningssandlåda för dig som vill öva på **API-testning**, **Playwright-tester** och **fullstack-utveckling** (Java Spring backend + TypeScript/React frontend). Applikationen är ett enkelt boksystem med inloggning, roller och granulär behörighetshantering.

Tanken är att du ska kunna:
- öva på **API-tester** (Postman + JWT, roller, 401/403)
- öva på **Playwright** end-to-end-tester mot ett riktigt UI
- **bygga vidare** på funktionalitet och **rätta buggar** i både backend och frontend

## Innehåll

- [Arkitektur](#arkitektur)
- [Förutsättningar](#förutsättningar)
- [Komma igång](#komma-igång)
- [Inloggning, roller & behörigheter](#inloggning-roller--behörigheter)
- [API-översikt](#api-översikt)
- [Frontend](#frontend)
- [Testning](#testning)
- [Postman](#postman)
- [Övningar](#övningar)
- [Projektstruktur](#projektstruktur)
- [Felsökning](#felsökning)

## Arkitektur

| Lager | Teknik | Plats | Port |
|-------|--------|-------|------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, JWT (jjwt) | `src/` | 8080 |
| Frontend | TypeScript, React 18, Vite, React Router | `frontend/` | 5173 |
| API-tester | Postman-collection | `postman/` | – |
| E2E-tester | Playwright | `frontend/e2e/` | – |

All data ligger **i minnet** och återställs vid varje omstart av backend – du börjar alltid från ett känt utgångsläge.

## Förutsättningar

- **Java 17+** ([Adoptium](https://adoptium.net/))
- **Maven 3.8+**
- **Node.js 18+** (för frontend och Playwright)
- **Postman** (valfritt, för API-övningarna)

## Komma igång

### 1. Starta backend

```bash
mvn spring-boot:run
```

När du ser `Started ApiTestingApplication` är API:et igång på `http://localhost:8080`.

Snabbtest:
```bash
curl http://localhost:8080/api/status/health
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"hemligt123"}'
```

### 2. Starta frontend

```bash
cd frontend
npm install
npm run dev
```

Öppna `http://localhost:5173` och logga in. Vite proxar `/api` vidare till backend på `:8080`.

## Inloggning, roller & behörigheter

Inloggning sker numera mot **Spring Security + JWT**. `POST /api/auth/login` returnerar en JWT i fältet `token` som ska skickas med som `Authorization: Bearer <token>`.

### Testkonton (seedas vid uppstart)

| Användarnamn | Lösenord | Roll | Behörigheter |
|--------------|----------|------|--------------|
| `admin` | `hemligt123` | `ADMIN` | alla |
| `handlaggare` | `handlaggare123` | `HANDLAGGARE` | `BOOK_READ`, `BOOK_CREATE`, `BOOK_UPDATE`, `USER_READ` |

### Behörigheter (authorities)

| Behörighet | Ger rätt att |
|------------|--------------|
| `BOOK_READ` | läsa böcker |
| `BOOK_CREATE` | skapa böcker |
| `BOOK_UPDATE` | uppdatera böcker (PUT/PATCH) |
| `BOOK_DELETE` | ta bort böcker |
| `USER_READ` | läsa användarlistan |
| `USER_MANAGE` | uppdatera/ta bort användare |
| `PERMISSION_MANAGE` | administrera behörigheter (admin-vyn) |

En **admin** kan i admin-vyn (eller via `/api/admin`-endpoints) **lägga till och ta bort** enskilda behörigheter per konto, och byta roll. Ändringar slår igenom **direkt** – behörigheten kontrolleras live vid varje anrop, inte bara mot token-innehållet.

> **401 vs 403:** saknad/ogiltig token ger **401 Unauthorized**. Giltig token men fel behörighet ger **403 Forbidden**.

## API-översikt

Bas-URL: `http://localhost:8080`

### Autentisering

| Metod | Endpoint | Behörighet | Beskrivning |
|-------|----------|------------|-------------|
| POST | `/api/auth/login` | öppen | Logga in, returnerar JWT + roll + behörigheter |
| POST | `/api/auth/logout` | öppen | Stateless – klienten släpper sin token |
| GET | `/api/auth/me` | inloggad | Info om inloggad användare |
| GET | `/api/auth/basic` | öppen | Övningsendpoint för **Basic Auth** |
| GET | `/api/auth/api-key` | öppen | Övningsendpoint för header `X-API-Key` |

### Böcker

| Metod | Endpoint | Behörighet |
|-------|----------|------------|
| GET | `/api/books` (stödjer `?genre=`, `?author=`, `?minPrice=`, `?maxPrice=`, `?sortBy=`) | `BOOK_READ` |
| GET | `/api/books/{id}`, `/count`, `/search?title=` | `BOOK_READ` |
| POST | `/api/books` | `BOOK_CREATE` |
| PUT / PATCH | `/api/books/{id}` | `BOOK_UPDATE` |
| DELETE | `/api/books/{id}` | `BOOK_DELETE` |

### Användare

| Metod | Endpoint | Behörighet |
|-------|----------|------------|
| GET | `/api/users`, `/api/users/{id}` | `USER_READ` |
| POST | `/api/users` | öppen (självregistrering) |
| PUT / DELETE | `/api/users/{id}` | `USER_MANAGE` |

### Admin – behörighetshantering

| Metod | Endpoint | Behörighet |
|-------|----------|------------|
| GET | `/api/admin/accounts` | `PERMISSION_MANAGE` |
| POST | `/api/admin/accounts/{username}/permissions/{permission}` | `PERMISSION_MANAGE` |
| DELETE | `/api/admin/accounts/{username}/permissions/{permission}` | `PERMISSION_MANAGE` |
| PUT | `/api/admin/accounts/{username}/role` | `PERMISSION_MANAGE` |
| GET | `/api/admin/permissions`, `/api/admin/roles` | `PERMISSION_MANAGE` |

### Status & headers (öppna)

`GET /api/status/health`, `/api/status/{code}`, `/api/status/delay/{sec}`, `/api/status/headers`, `/api/status/custom-headers`, `/api/status/xml`, `/api/status/text`, `POST /api/status/echo`.

## Frontend

React-appen i `frontend/` har:
- **Login-sida** med de två testkontona som hint.
- **Böcker** – lista samt skapa/redigera/ta bort. Knapparna visas **villkorligt** utifrån dina behörigheter (en handläggare ser t.ex. ingen *Ta bort*-knapp).
- **Användare** – lista, samt ta bort om du har `USER_MANAGE`.
- **Admin** – tabell med konton × behörigheter (kryssrutor) och roll-väljare. Endast synlig med `PERMISSION_MANAGE`.

Behörighetsstyrningen i UI:t är en bekvämlighet – backend gör alltid den verkliga kontrollen (försök gärna anropa en skyddad endpoint direkt och se 403:an).

## Testning

### Backend (JUnit + MockMvc)
```bash
mvn test
```
Se `src/test/java/com/apitesting/AuthIntegrationTest.java` för exempel på login (200), fel lösen (401), saknad behörighet (403) och live-behörighetsändring.

### Frontend – komponenttester (Vitest + Testing Library)
```bash
cd frontend
npm test
```

### Frontend – e2e (Playwright)
```bash
cd frontend
npm run e2e
```
Playwright startar automatiskt både backend och frontend (se `frontend/playwright.config.ts`). Specarna ligger i `frontend/e2e/` och täcker inloggning, behörighetsstyrt UI och bok-CRUD.

## Postman

I `postman/` finns en collection och en environment. Importera båda i Postman och välj miljön **"API Testing Practice - Local"**.

> **Viktigt (ändring):** bok-endpoints kräver nu inloggning. Collectionen har ett **pre-request-script på collection-nivå** som automatiskt loggar in som admin och fyller `{{token}}`, så att alla requests fungerar oavsett ordning i Collection Runner. Mappen **"5. Behörigheter & roller"** visar 403-fallen och admin-endpointsen.

## Övningar

### API & Postman
1. Logga in som `admin` respektive `handlaggare` och jämför `permissions` i svaret.
2. Försök `DELETE /api/books/1` som handläggare → förvänta **403**. Som admin → **204**.
3. Anropa en skyddad endpoint utan token → **401**.
4. Ge handläggaren `BOOK_DELETE` via `POST /api/admin/.../permissions/BOOK_DELETE` och se att samma token nu får ta bort böcker.

### Fullstack-utveckling (bygg vidare)
- **Backend:** lägg till en ny behörighet (t.ex. `BOOK_EXPORT`) och en endpoint som kräver den.
- **Frontend:** lägg till sök/filter-fält på Böcker-sidan som använder query-parametrarna.
- **Frontend:** visa ett tydligt felmeddelande (toast) när ett 403 inträffar.

### Övningskrokar / kända begränsningar (`// TODO (övning)` i koden)
- **Stateless logout:** `POST /api/auth/logout` invaliderar inte JWT:n på serversidan. *Övning:* implementera en denylist över utloggade tokens.
- **Rollbyte nollställer behörigheter:** `AccountService.changeRole` återställer behörigheterna till rollens standard. *Övning:* bestäm önskat beteende och ändra det.
- **JWT-hemlighet i klartext:** `app.jwt.secret` ligger i `application.properties`. *Övning:* flytta till en miljövariabel.

## Projektstruktur

```
API-testing/
├── pom.xml
├── README.md
├── postman/
│   ├── API-Testing-Practice.postman_collection.json
│   └── API-Testing-Practice.postman_environment.json
├── src/
│   ├── main/java/com/apitesting/
│   │   ├── ApiTestingApplication.java
│   │   ├── config/        (SecurityConfig)
│   │   ├── controller/    (Book, User, Auth, Admin, Status)
│   │   ├── model/         (Book, User, Account, AccountResponse, Login*, ErrorResponse)
│   │   ├── security/      (Role, Permission, JwtService, JwtAuthenticationFilter,
│   │   │                   CustomUserDetailsService, Rest*EntryPoint/Handler)
│   │   ├── service/       (BookService, UserService, AccountService, AuthService)
│   │   └── exception/     (GlobalExceptionHandler m.fl.)
│   ├── main/resources/application.properties
│   └── test/java/com/apitesting/ (ApiTestingApplicationTests, AuthIntegrationTest)
└── frontend/
    ├── package.json, vite.config.ts, tsconfig*.json, playwright.config.ts
    ├── src/
    │   ├── api/client.ts          (fetch-wrapper, Bearer, 401/403)
    │   ├── auth/                  (AuthContext, ProtectedRoute)
    │   ├── components/            (ProtectedLayout)
    │   ├── pages/                 (Login, Books, Users, Admin)
    │   └── types.ts
    └── e2e/                       (auth, permissions, books-crud specs)
```

## Felsökning

**Port 8080 upptagen?** Ändra `server.port` i `application.properties` och uppdatera `baseUrl` i Postman / Vite-proxyn.

**Frontend når inte API:et?** Kontrollera att backend kör på 8080 och att du startade frontend med `npm run dev` (proxyn gäller bara devservern).

**Playwright säger "Executable doesn't exist"?** Kör inte `playwright install` i den här miljön – webbläsarna är förinstallerade. `@playwright/test`-versionen i `package.json` är pinnad för att matcha dem.

**Data försvann vid omstart?** Det är meningen – allt lagras i minnet och seedas om vid start.

Lycka till med övningarna!

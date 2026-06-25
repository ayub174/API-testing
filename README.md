# API Testing Practice – Fullstack-träningsprojekt

En träningssandlåda för dig som vill öva på **API-testning**, **Playwright-tester** och **fullstack-utveckling** (Java Spring backend + TypeScript/React frontend). Applikationen är ett enkelt **bibliotekssystem**: låntagare söker och lånar böcker, bibliotekarier (handläggare) sköter katalog och lånedisk, och admin styr konton och behörigheter – med inloggning, tre roller och granulär behörighetshantering.

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

### De tre rollerna

- **ANVANDARE** (låntagare) – söker böcker, lånar och återlämnar, ser sina egna lån.
- **HANDLAGGARE** (bibliotekarie) – sköter katalog och lager, ser alla lån, hanterar lånedisken (lånar/återlämnar åt låntagare) och kan skapa/ta bort **låntagarkonton** (men inte personalkonton, roller eller behörigheter).
- **ADMIN** – full kontroll: personalkonton, roller/behörigheter och permanent borttagning av böcker.

### Testkonton (seedas vid uppstart)

| Användarnamn | Lösenord | Roll |
|--------------|----------|------|
| `admin` | `hemligt123` | `ADMIN` |
| `handlaggare` | `handlaggare123` | `HANDLAGGARE` |
| `anvandare` | `anvandare123` | `ANVANDARE` |

### Behörigheter (authorities)

| Behörighet | Ger rätt att | ANVANDARE | HANDLAGGARE | ADMIN |
|------------|--------------|:---:|:---:|:---:|
| `BOOK_READ` | söka/läsa böcker | ✅ | ✅ | ✅ |
| `BOOK_CREATE` | skapa böcker | | ✅ | ✅ |
| `BOOK_UPDATE` | uppdatera böcker (PUT/PATCH) | | ✅ | ✅ |
| `BOOK_DELETE` | ta bort böcker | | | ✅ |
| `LOAN_BORROW` | låna en bok åt sig själv | ✅ | ✅ | ✅ |
| `LOAN_RETURN` | återlämna sitt eget lån | ✅ | ✅ | ✅ |
| `LOAN_VIEW_OWN` | se sina egna lån | ✅ | ✅ | ✅ |
| `LOAN_VIEW_ALL` | se alla lån | | ✅ | ✅ |
| `LOAN_MANAGE` | låna/återlämna åt en låntagare | | ✅ | ✅ |
| `USER_READ` | läsa användarlistan | | ✅ | ✅ |
| `USER_MANAGE` | uppdatera/ta bort användare | | ✅ | ✅ |
| `ACCOUNT_MANAGE` | skapa/ta bort inloggningskonton¹ | | ✅ | ✅ |
| `PERMISSION_MANAGE` | hantera roller och behörigheter | | | ✅ |

¹ Handläggare med `ACCOUNT_MANAGE` får bara skapa/ta bort **låntagarkonton** (`ANVANDARE`); endast admin (`PERMISSION_MANAGE`) får hantera personalkonton, roller och behörigheter.

En **admin** kan i admin-vyn (eller via `/api/admin`-endpoints) **lägga till och ta bort** enskilda behörigheter per konto, och byta roll. Ändringar slår igenom **direkt** – behörigheten kontrolleras live vid varje anrop, inte bara mot token-innehållet.

> **401 vs 403:** saknad/ogiltig token ger **401 Unauthorized**. Giltig token men fel behörighet ger **403 Forbidden**.

## API-översikt

Bas-URL: `http://localhost:8080`

### Autentisering

| Metod | Endpoint | Behörighet | Beskrivning |
|-------|----------|------------|-------------|
| POST | `/api/auth/register` | öppen | Självregistrering som låntagare (skapar ett `ANVANDARE`-konto) |
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

### Lån

Ett lån kopplas till låntagarens inloggningskonto. Vid utlåning minskar bokens lagersaldo och ett **förfallodatum** sätts (utlåningsdag + 14 dagar); vid återlämning ökar saldot igen. Ett aktivt lån vars förfallodatum passerat markeras som **försenat**.

| Metod | Endpoint | Behörighet | Beskrivning |
|-------|----------|------------|-------------|
| POST | `/api/loans` (body `{bookId}`) | `LOAN_BORROW` | Låna en tillgänglig bok åt sig själv |
| POST | `/api/loans/borrow-for` (body `{bookId, username}`) | `LOAN_MANAGE` | Personal lånar åt en låntagare |
| GET | `/api/loans/me` | `LOAN_VIEW_OWN` | Mina lån |
| GET | `/api/loans` (`?active=true`, `?overdue=true`) | `LOAN_VIEW_ALL` | Alla lån (personal) |
| POST | `/api/loans/{id}/return` | `LOAN_RETURN` eller `LOAN_MANAGE` | Återlämna (eget lån, eller åt någon som personal) |

> En otillgänglig bok (lagersaldo 0) ger **409 Conflict** vid lån.

### Användare (demo-CRUD-resurs)

| Metod | Endpoint | Behörighet |
|-------|----------|------------|
| GET | `/api/users`, `/api/users/{id}` | `USER_READ` |
| POST | `/api/users` | öppen |
| PUT / DELETE | `/api/users/{id}` | `USER_MANAGE` |

### Admin – konto- & behörighetshantering

| Metod | Endpoint | Behörighet |
|-------|----------|------------|
| GET | `/api/admin/accounts` | `ACCOUNT_MANAGE` eller `PERMISSION_MANAGE` |
| POST | `/api/admin/accounts` (body `{username, password, role}`) | `ACCOUNT_MANAGE` |
| DELETE | `/api/admin/accounts/{username}` | `ACCOUNT_MANAGE` |
| POST | `/api/admin/accounts/{username}/permissions/{permission}` | `PERMISSION_MANAGE` |
| DELETE | `/api/admin/accounts/{username}/permissions/{permission}` | `PERMISSION_MANAGE` |
| PUT | `/api/admin/accounts/{username}/role` | `PERMISSION_MANAGE` |
| GET | `/api/admin/permissions`, `/api/admin/roles` | `PERMISSION_MANAGE` |

### Status & headers (öppna)

`GET /api/status/health`, `/api/status/{code}`, `/api/status/delay/{sec}`, `/api/status/headers`, `/api/status/custom-headers`, `/api/status/xml`, `/api/status/text`, `POST /api/status/echo`.

## Frontend

React-appen i `frontend/` har:
- **Login** med testkontona som hint, och **Registrering** för nya låntagare.
- **Böcker** – sök/lista. En låntagare ser en **Låna**-knapp på tillgängliga böcker; personal ser dessutom skapa/redigera/ta bort. Knapparna visas **villkorligt** utifrån dina behörigheter.
- **Mina lån** – egna lån med status (aktiv/försenad/återlämnad) och **Återlämna**-knapp.
- **Alla lån** (personal) – alla lån med filter (aktiva/försenade) och möjlighet att registrera återlämning.
- **Användare** – lista, samt ta bort om du har `USER_MANAGE`.
- **Admin** – skapa/ta bort konton samt tabell med konton × behörigheter (kryssrutor) och roll-väljare.

Behörighetsstyrningen i UI:t är en bekvämlighet – backend gör alltid den verkliga kontrollen (försök gärna anropa en skyddad endpoint direkt och se 403:an).

## Testning

### Backend (JUnit + MockMvc)
```bash
mvn test
```
Se `AuthIntegrationTest.java` (login/401/403/live-behörighet) och `LoanIntegrationTest.java` (lån, lagersaldo, förfallodatum, 409 vid otillgänglig bok, självregistrering, kontohantering) under `src/test/java/com/apitesting/`.

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
Playwright startar automatiskt både backend och frontend (se `frontend/playwright.config.ts`). Specarna ligger i `frontend/e2e/` och täcker inloggning, behörighetsstyrt UI, bok-CRUD samt låneflödet (låna/återlämna och registrering).

## Postman

I `postman/` finns en collection och en environment. Importera båda i Postman och välj miljön **"API Testing Practice - Local"**.

> **Viktigt (ändring):** bok-endpoints kräver nu inloggning. Collectionen har ett **pre-request-script på collection-nivå** som automatiskt loggar in som admin och fyller `{{token}}`, så att alla requests fungerar oavsett ordning i Collection Runner. Mappen **"5. Behörigheter & roller"** visar 403-fallen och admin-endpointsen, och **"6. Lån"** går igenom hela låneflödet (registrera → logga in → låna → mina lån → återlämna, plus 403/409-fall).

## Övningar

### API & Postman
1. Registrera ett nytt låntagarkonto med `POST /api/auth/register`, logga in och jämför `permissions` med `admin`/`handlaggare`.
2. Låna en bok (`POST /api/loans`), kontrollera att bokens `stock` minskar, och återlämna (`POST /api/loans/{id}/return`).
3. Försök låna en slutsåld bok (id 4) → förvänta **409**. Försök se alla lån som låntagare (`GET /api/loans`) → **403**.
4. Försök `DELETE /api/books/1` som handläggare → **403**. Som admin → **204**.
5. Ge handläggaren `BOOK_DELETE` via `POST /api/admin/.../permissions/BOOK_DELETE` och se att samma token nu får ta bort böcker.

### Fullstack-utveckling (bygg vidare)
- **Backend:** lägg till en lånegräns (max antal samtidiga lån per användare) och returnera **409** när gränsen nås.
- **Backend:** lägg till ett "förseningsavgift"-fält som räknas ut från `dueDate` vid återlämning.
- **Frontend:** lägg till sök/filter-fält på Böcker-sidan som använder query-parametrarna.
- **Frontend:** visa ett tydligt felmeddelande (toast) när ett 403/409 inträffar.

### Övningskrokar / kända begränsningar (`// TODO (övning)` i koden)
- **Stateless logout:** `POST /api/auth/logout` invaliderar inte JWT:n på serversidan. *Övning:* implementera en denylist över utloggade tokens.
- **Rollbyte nollställer behörigheter:** `AccountService.changeRole` återställer behörigheterna till rollens standard. *Övning:* bestäm önskat beteende och ändra det.
- **Fast lånetid:** `LoanService.LOAN_PERIOD_DAYS` är hårdkodad till 14 dagar. *Övning:* gör den konfigurerbar via `application.properties`.
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
│   │   ├── controller/    (Book, User, Auth, Admin, Loan, Status)
│   │   ├── model/         (Book, User, Account, Loan, *Request, *Response, ErrorResponse)
│   │   ├── security/      (Role, Permission, JwtService, JwtAuthenticationFilter,
│   │   │                   CustomUserDetailsService, Rest*EntryPoint/Handler)
│   │   ├── service/       (BookService, UserService, AccountService, LoanService, AuthService)
│   │   └── exception/     (GlobalExceptionHandler m.fl.)
│   ├── main/resources/application.properties
│   └── test/java/com/apitesting/ (ApiTestingApplicationTests, AuthIntegrationTest, LoanIntegrationTest)
└── frontend/
    ├── package.json, vite.config.ts, tsconfig*.json, playwright.config.ts
    ├── src/
    │   ├── api/client.ts          (fetch-wrapper, Bearer, 401/403)
    │   ├── auth/                  (AuthContext, ProtectedRoute)
    │   ├── components/            (ProtectedLayout)
    │   ├── pages/                 (Login, Register, Books, MyLoans, AllLoans, Users, Admin)
    │   └── types.ts
    └── e2e/                       (auth, permissions, books-crud, loans specs)
```

## Felsökning

**Port 8080 upptagen?** Ändra `server.port` i `application.properties` och uppdatera `baseUrl` i Postman / Vite-proxyn.

**Frontend når inte API:et?** Kontrollera att backend kör på 8080 och att du startade frontend med `npm run dev` (proxyn gäller bara devservern).

**Playwright säger "Executable doesn't exist"?** Kör inte `playwright install` i den här miljön – webbläsarna är förinstallerade. `@playwright/test`-versionen i `package.json` är pinnad för att matcha dem.

**Data försvann vid omstart?** Det är meningen – allt lagras i minnet och seedas om vid start.

Lycka till med övningarna!

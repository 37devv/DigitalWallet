# Oepfelbaum Digital Wallet

A client-facing web banking dashboard that aggregates account and transaction data from the NatWest Sandbox Open Banking API.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | included via `mvnw` wrapper — no separate install needed |
| Node.js | 18+ (LTS recommended) |
| npm | 9+ |

---

## Project Structure

```
OepfelBaumTechAssessment/
├── digital-wallet-backend/    Spring Boot 4, Java 21
└── digital-wallet-frontend/   React 19, Vite 8
```

---

## Backend Setup

### Configuration

Fill in `digital-wallet-backend/src/main/resources/application.properties` with your NatWest Sandbox credentials. Obtain them at [developer.sandbox.natwest.com](https://developer.sandbox.natwest.com).

```properties
spring.application.name=

natwest.client-id=
natwest.client-secret=
natwest.redirect-uri=
natwest.psu-username=

natwest.token-url=
natwest.authorize-url=
natwest.resource-url=
```

> **Important:** Never commit `application.properties` with real credentials. Keep the populated file in your local working copy only.

### Running

```bash
cd digital-wallet-backend
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**.

---

## Frontend Setup

### Configuration

Create `digital-wallet-frontend/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Running

```bash
cd digital-wallet-frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173**.

### All Frontend Commands

```bash
npm run dev      # Start Vite dev server (http://localhost:5173)
npm run build    # Production build → /dist
npm run preview  # Preview production build locally
npm run lint     # Run ESLint
```

---

## API Reference

Base URL: `http://localhost:8080/api`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/accounts` | List all accounts |
| GET | `/api/accounts/{id}` | Get a single account by ID |
| GET | `/api/accounts/{id}/transactions` | Get transactions for an account |

### Account

```json
{
  "id": "string",
  "bank": "string",
  "type": "string",
  "balance": 0.00,
  "logo": "string",
  "ownerName": "string"
}
```

### Transaction

```json
{
  "id": "string",
  "date": "dd.MM.yyyy",
  "description": "string",
  "amount": 0.00,
  "type": "debit | credit"
}
```


### Key Backend Components

| Class | Responsibility |
|-------|---------------|
| `AccountController` | REST endpoints |
| `AccountService` | Business logic |
| `NatWestService` | API calls to NatWest Sandbox |
| `NatWestTokenManager` | Full OAuth2 flow, in-memory token caching |
| `NatWestMapper` | Maps NatWest DTOs to domain models |
| `GlobalExceptionHandler` | Centralized HTTP error responses |

### Key Frontend Components

| File | Responsibility |
|------|---------------|
| `src/api.js` | Fetch layer, base URL from env |
| `src/theme.js` | MUI theme (brand colors) |
| `src/components/PageLayout.jsx` | Shared header, loading, and error UI |
| `src/pages/Overview.jsx` | Account list with total balance |
| `src/pages/AccountDetail.jsx` | Account detail with sortable transaction list |

---

## NatWest Sandbox OAuth2 Flow

The backend performs the full OAuth2 authorization code flow automatically on the first request:

1. **Client Credentials** — obtain an app-level token
2. **Create Consent** — POST to `/account-access-consents` with required permissions
3. **Authorize Consent** — GET `/authorize` with `AUTO_POSTMAN` mode (no browser step needed in sandbox)
4. **Exchange Code** — exchange the authorization code for a user access token
5. **Verify Consent** — GET `/account-access-consents/{id}` to confirm status is `Authorised` (`AUTH` in sandbox)

The resulting token is cached in-memory and automatically refreshed before expiry.

---

## Development Notes

### Frontend mock data

To test the frontend without a running backend, set `USE_MOCK = true` in `digital-wallet-frontend/src/api.js`. This returns hardcoded accounts and transactions including a negative balance for UI testing.

### CORS

The backend allows requests from `http://localhost:5173` by default (configured in `CorsConfig.java`).

---

## AI Usage

[Claude Code](https://claude.ai/code) was used throughout development for implementation, refactoring, and documentation, in line with the assessment guidelines.

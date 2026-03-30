# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Claude Code Guidelines
- **Always work directly on `master`** — do not create new branches for new conversations.

## Project Overview

A client-facing web banking application (Oepfelbaum Tech Assessment) that aggregates account and transaction data into a dashboard UI. Targets the NatWest Sandbox Open Banking API. Auth/authentication is explicitly out of scope.

---

## Architecture

```
digital-wallet-frontend/   (React + Vite)
digital-wallet-backend/    (Spring Boot)
```

### Frontend — `digital-wallet-frontend/`
- **Framework:** React 19 + Vite 8
- **UI Library:** MUI (Material UI) v7
- **Routing:** React Router DOM v7
- **Entry:** `src/main.jsx` → `src/App.jsx`
- **Pages:**
  - `src/pages/Overview.jsx` — lists all accounts with total balance
  - `src/pages/AccountDetail.jsx` — account details and transaction list
- **API layer:** `src/api.js` — fetch helpers pointing to `http://localhost:8080/api`
- **Utils:** `src/utils.js` — CHF formatting, bank logo/color styles

**Routes:**
| Path | Component |
|---|---|
| `/` | Overview |
| `/account/:id` | AccountDetail |

**Frontend commands** (run from `digital-wallet-frontend/`):
```bash
npm run dev       # Start Vite dev server
npm run build     # Production build → /dist
npm run lint      # ESLint
npm run preview   # Preview production build
```

### Backend — `digital-wallet-backend/`
- **Framework:** Spring Boot 4.0.4
- **Java:** 21
- **Build:** Maven (`mvnw`)
- **CORS:** Configured in `CorsConfig.java` to allow frontend origin

**REST API (`/api/accounts`):**
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/accounts` | List all accounts |
| GET | `/api/accounts/{id}` | Get account by ID |
| GET | `/api/accounts/{id}/transactions` | Get transactions for account |

**Models:** `Account` (`id, bank, type, balance, logo, transactions`), `Transaction` (`id, date, description, amount, category`)

**Backend commands** (run from `digital-wallet-backend/`):
```bash
./mvnw spring-boot:run   # Runs on http://localhost:8080
```

---

## Current Status

- [x] Requirement #1 — Overview page with account list and total balance
- [x] Requirement #2 — Account detail page with transaction list (incl. date sorting asc/desc)
- [x] Frontend ↔ Backend communication working end-to-end with mock data (`AccountService`)
- [x] NatWest Sandbox API integration
- [x] Refactor, Cleanup — 

---

## Assessment Context

- Business requirements delivered at sprint kick-off via `OE TechAssessment_Digital-Banking-1.pdf`.
- Deliverables: runnable source code, developer docs, git history, list of future work, and a demo presentation.
- AI usage is expected — document how it was used in the README or commit messages.

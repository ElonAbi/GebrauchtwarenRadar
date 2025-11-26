<<<<<<< HEAD
# GebrauchtwarenRadar
=======
﻿# Kleinanzeigen Radar

Tool zum Monitoring von Wunsch-Artikeln auf kleinanzeigen.de mit Java Backend und React Frontend.

## Projektaufbau
- `backend/`: Spring Boot Anwendung fuer Suchprofile, Scraping-Services und REST-API.
- `frontend/`: Vite + React + TypeScript UI zur Verwaltung von Profilen und Anzeige von Ergebnissen.
- `docs/`: Architektur-Notizen und technische Leitplanken.

Ausfuehrliche Uebersicht: [docs/architecture.md](docs/architecture.md)

## Voraussetzungen
- Java 21 und Gradle oder eine lokale Gradle-Wrapper-Installation
- Node.js >= 20.19 sowie npm oder pnpm
- Optional: PostgreSQL (fuer das `postgres`-Profil)

## Backend starten
```bash
cd backend
# Abhaengigkeiten laden und Anwendung starten (dev-Profil mit In-Memory-H2)
./gradlew bootRun   # oder: gradle bootRun
```

Standard-Port: `http://localhost:8080`

Weitere Profile:
- `postgres`: `./gradlew bootRun --args='--spring.profiles.active=postgres'`

Wichtige Endpoints:
- `GET /api/search-profiles` - Alle Profile anzeigen
- `POST /api/search-profiles` - Neues Profil anlegen
- `POST /api/search/profiles/{id}/execute` - Suche fuer ein Profil sofort ausfuehren

## Frontend entwickeln
```bash
cd frontend
npm install
npm run dev
```

Vite-Dev-Server: `http://localhost:5173` (Proxy auf Backend `/api`).

## Weitere Schritte
1. Persistente Speicherung der Suchergebnisse (z.B. via dedizierter Tabelle + History-View).
2. Automatisierte Tests fuer Service-Layer und API-Verhalten ergaenzen.
3. Stabilisierung des Scraping-Stacks (Captcha-Handling, Proxy-Rotation, Fehler-Telemetrie).
4. Deployment-Setup (Docker Compose inkl. PostgreSQL, Reverse Proxy fuer Frontend/Backend).
5. Erweiterbare Marketplace-Clients (z.B. ebay, Amazon Warehouse) ueber gemeinsames Interface einklinken.

## Lizenz
Eigene Nutzung, Lizenz noch festlegen.
>>>>>>> 9d5027b (Initial commit)

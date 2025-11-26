# Architektur Ueberblick

## Zielsetzung
Das Tool soll personalisierte Suchlaeufe auf kleinanzeigen.de ausfuehren, Treffer nach Preis filtern und die Ergebnisse ueber eine grafische Oberflaeche anzeigen. Weitere Marktplatz-Connectoren sollen spaeter leicht eingebunden werden koennen.

## Systemaufbau
- **Client**: Web UI mit React + TypeScript. Stellt Formulare fuer Suchprofile bereit, zeigt Trefferlisten an und verwaltet Nutzerzustand lokal.
- **API Backend**: Java (Spring Boot) Anwendung, die Suchprofile verwaltet, Scraping-Auftraege anstoesst, Ergebnisse normalized und an den Client ausliefert.
- **Worker Layer**: Eingebettete Scheduler-Komponente (Spring `@Scheduled`) koordiniert regelmaessige Abrufe. Suchlaeufe werden ueber Service-Interfaces je Marktplatz ausgefuehrt.
- **Persistence**: PostgreSQL (spaeter konfigurierbar). Ueber Spring Data werden Suchprofile, Ausfuehrungshistorie und gecachte Suchtreffer abgelegt.
- **Integration**: REST-API (JSON) fuer Client, interne Events fuer UI-Updates (Server-Sent Events/WebSocket als spaetere Erweiterung).

## Kernmodule Backend
1. **search-profile**: REST-Endpoints, Validierung und Repository fuer Nutzerprofile (Suchbegriffe, Kategorien, Preisfilter, Frequenz, Marktplatz).
2. **crawler**: `KleinanzeigenClient` orchestriert HTTP-Abrufe mit Rate-Limit, `KleinanzeigenHtmlParser` extrahiert Identifier, Titel, Preise, Orte und Zeitstempel via JSoup.
3. **result-filter**: `ResultFilterService` wendet Preis- und Attributfilter an und bereitet Ergebnisse fuer den Client auf.
4. **search-execution**: `SearchExecutionService` verbindet Profil, Marketplace-Client und Filter. `SearchJobScheduler` legt periodische Jobs pro Profil an.
5. **notification**: Platzhalter fuer kuenftige Benachrichtigungen (E-Mail, Push).

## Erweiterbarkeit
- Neue Marktplatz-Adapter implementieren `MarketplaceClient` und werden im `MarketplaceClientRegistry` registriert.
- Feature Flags via Spring Profiles erlauben das isolierte Aktivieren experimenteller Funktionen.

## Frontend Struktur
- **Pages**: `SearchProfilesPage`, `ResultsPage` (spaeter).
- **Components**: `SearchProfileForm`, `SearchProfilesList`, `SearchExecutionPanel`, `SearchResults`.
- **State Management**: React Query fuer Server-Data, Context fuer UI-State.

## Sicherheitsaspekte
- Rate Limiting und Captcha-Handling beim Scraping beruecksichtigen.
- Konfigurierbare Proxy-Unterstuetzung fuer IP-Rotation.
- Zentralisiertes Error-Handling (Problem+JSON) fuer API.

## Build & Deployment
- Backend: Gradle-Projekt, spaeter Dockerfile fuer Containerisierung.
- Frontend: Vite + React, Deployment als statische Assets oder Container mit Reverse Proxy.
- Lokale Entwicklung: H2 fuer Dev-Profil, PostgreSQL ueber separates Profil, `npm run dev` fuer Frontend.

## Naechste Schritte
1. Trefferpersistenz inkl. Historie und Caching implementieren.
2. Integrations-/Service-Tests fuer Scraping, Scheduler und API ergaenzen.
3. Fehler-/Retry-Strategie fuer Scraping (Captcha, Proxy-Rotation) ausbauen.
4. Deployment-Setup (Docker Compose inkl. PostgreSQL, Reverse Proxy fuer Frontend/Backend).
5. Weitere Marketplace-Clients (z.B. ebay, Amazon Warehouse) ueber das bestehende Interface anbinden.

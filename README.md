# Media Ratings Platform (MRP)

Repository: https://github.com/if24b257/MRP

## Überblick
MRP ist eine kleine REST-API zum Verwalten von Medien und Bewertungen. Nutzer registrieren sich, melden sich an, legen Medienobjekte an und geben Sternebewertungen samt Kommentaren und Likes ab. Die Anwendung läuft als reiner Java-Server ohne zusätzliche Frameworks.

## Technologie-Stack
- Java 24, Maven
- Eingebauter `com.sun.net.httpserver`
- PostgreSQL (Schema in `src/main/resources/db/init.sql`)
- JDBC-Repositories mit einfacher Service-Schicht

## Voraussetzungen
- JDK 24 und Maven 3.9+
- PostgreSQL mit den Standardzugängen `postgres:postgres` auf Port 5433
- Optional Docker und Docker Compose

## Anwendung starten
1. Datenbank hochfahren  
   ```bash
   docker compose up -d postgres
   ```
2. Projekt bauen  
   ```bash
   mvn clean compile
   ```
3. Server starten  
   ```bash
   mvn exec:java -Dexec.mainClass=org.SalimMRP.application.Main -Dexec.classpathScope=runtime
   ```
4. API steht unter http://localhost:8080 bereit. Beenden mit `CTRL+C`.

## API in Kurzform
- Benutzer: `POST /api/users/register`, `POST /api/users/login` → Login liefert ein Token.
- Medien (Token nötig): `GET/POST/PUT/DELETE /api/media` und `GET /api/media/{id}`.
- Bewertungen (Token nötig):  
  `GET /api/ratings/media/{mediaId}`,  
  `POST /api/ratings/media/{mediaId}`,  
  `PUT/DELETE /api/ratings/{ratingId}`,  
  `POST /api/ratings/{ratingId}/confirm`,  
  `POST/DELETE /api/ratings/{ratingId}/likes`.

## Datenbank und Authentifizierung
- Tabellen: `users`, `media`, `ratings`, `rating_likes`.
- Passwörter werden via SHA-256 gehasht, Tokens liegen im Arbeitsspeicher.
- Für produktive Szenarien sind konfigurierbare Datenbankzugänge und stärkere Hashverfahren einzuplanen.

## Offene Punkte
- Maven-Wrapper oder Anpassung des Dockerfiles ergänzen.
- Automatisierte Tests fehlen bisher.
- Token laufen nie ab und überleben Serverneustarts nicht.

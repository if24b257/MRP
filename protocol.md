# Entwicklungsprotokoll – Media Ratings Platform (MRP)

Repository: https://github.com/if24b257/MRP

## 1. Ziel
Eine kompakte Java-Anwendung liefern, die Registrierung, Login, Medienverwaltung und Bewertungen inklusive Likes ohne externe Webframeworks bereitstellt.

## 2. Entscheidungen
- Architektur in Schichten: Application (Start), Presentation (HTTP-Handler), Business (Services), Persistence (JDBC).
- Eingebauter `HttpServer` für REST-Endpunkte, Jackson für JSON.
- Token-Verwaltung im Speicher (`InMemoryTokenService`), Passwörter via SHA-256 gehasht.
- PostgreSQL als Datenbank; Schema liegt in `src/main/resources/db/init.sql`.

## 3. Datenmodell
- `users` speichert Benutzername, Hash und Zeitstempel.
- `media` enthält Titel, Beschreibung, Typ und Besitzer.
- `ratings` verwaltet Sterne 1–5, Kommentar, Freigabe-Flag und erzwingt eine Bewertung pro Benutzer/Medium.
- `rating_likes` bildet Likes als Zuordnungstabelle ab.

## 4. Umsetzung
1. Verbindungsschicht über `Database` und Repository-Interfaces erstellt.
2. Services implementiert, Validierungen und Besitzprüfungen ergänzt.
3. HTTP-Handler verdrahtet und JSON-Parsing hinzugefügt.
4. Docker Compose für PostgreSQL sowie ein Dockerfile zum Bauen eines fat JAR vorbereitet.

## 5. Offene Punkte
- Maven-Wrapper fehlt → Dockerfile anpassen oder Wrapper hinzufügen.
- Keine automatisierten Tests vorhanden.
- Tokens haben kein Ablaufdatum und gehen bei Neustart verloren.
- Datenbankzugänge sind derzeit hart kodiert; Konfiguration über Umgebungsvariablen wäre sinnvoll.

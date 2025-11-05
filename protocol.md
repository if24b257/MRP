# Entwicklungsprotokoll – Media Ratings Platform (MRP)

Repository: https://github.com/if24b257/MRP

## 1. Ziel
Eine kompakte Java-Anwendung liefern, die Registrierung, Login, Medienverwaltung, Bewertungen (inklusive Moderation und Likes), Favoritenlisten, Benutzerstatistiken, Leaderboard und Empfehlungslogik ohne große Webframeworks bereitstellt.

## 2. Entscheidungen
- Architektur in Schichten: Application (Start), Presentation (HTTP-Handler), Business (Services), Persistence (JDBC).
- Eingebauter `HttpServer` für REST-Endpunkte, Jackson für JSON.
- Token-Verwaltung im Speicher (`InMemoryTokenService`), Passwörter via SHA-256 gehasht.
- PostgreSQL als Datenbank; Schema liegt in `src/main/resources/db/init.sql`.

## 3. Datenmodell
- `users` speichert Benutzername, Passwort-Hash und Zeitstempel.
- `media` enthält Titel, Beschreibung, Typ, Release-Jahr, Altersfreigabe, Genres und Besitzer.
- `ratings` verwaltet Sterne 1–5, Kommentar, Moderationsflag, Zeitstempel sowie eine eindeutige Zuordnung `media_id`/`user_id`.
- `rating_likes` bildet Likes als Zuordnungstabelle ab.
- `favorites` verknüpft Benutzer mit bevorzugten Medien.

## 4. Umsetzung
1. Verbindungsschicht über `Database` und Repository-Interfaces erstellt (inkl. Favoriten-Repository).
2. Business-Layer erweitert: `DefaultMediaService` deckt Suche, Favoriten, Empfehlungen ab; `DefaultProfileService` liefert Profilstatistiken und Leaderboard; `DefaultRatingService` verwaltet Historie und Moderation.
3. HTTP-Handler überarbeitet (geschützte `/api/users`-Routen, erweiterte `/api/media`-Routen, bestehende Rating-Handler).
4. Docker Compose für PostgreSQL sowie ein Dockerfile zum Bauen eines fat JAR vorbereitet. Postman-Collection dokumentiert die wichtigsten Endpunkte.

## 5. Offene Punkte
- Maven-Wrapper fehlt → Dockerfile anpassen oder Wrapper hinzufügen.
- Tokens haben kein Ablaufdatum und gehen bei Neustart verloren.
- Datenbankzugänge sind derzeit hart kodiert; Konfiguration über Umgebungsvariablen wäre sinnvoll.
- Sicherheitsaspekte wie HTTPS, Rate Limits oder Refresh-Tokens sind noch offen.

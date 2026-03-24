# Phase E: Client-Side Event Sources + Frontend

> **Priorität**: Mittel
> **Geschätzter Aufwand**: Mittel
> **Abhängigkeiten**: Phase C1 (FullCalendar Source Registry)

## Zielsetzung

Client-Side Event Sources laden Kalender-Daten direkt im Browser von externen Quellen (Google Calendar, iCalendar, JSON Feed), ohne den Java-Server als Proxy.

## Scope

### Neue Klassen (Jackson → elemental.json)

| Klasse | v7 Zeilen | Beschreibung |
|---|---|---|
| `ClientSideEventSource.java` | 390 | Abstrakte Basis-Klasse |
| `GoogleCalendarEventSource.java` | 85 | Google Calendar Feed |
| `ICalendarEventSource.java` | 65 | iCalendar (.ics) Feed |
| `JsonFeedEventSource.java` | 162 | Generischer JSON Feed |

### FullCalendar-Integration (in C1 vorbereitet)

Neues Feld + Methoden in FullCalendar.java:
```java
private final Map<String, ClientSideEventSource<?>> clientSideEventSourceRegistry = new LinkedHashMap<>();
```
- `addEventSource()`, `removeEventSource()`, `getEventSources()`
- Restore in `onAttach()` für Detach/Reattach

### @NpmPackage Dependencies

```java
@NpmPackage(value = "@fullcalendar/google-calendar", version = FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/icalendar", version = FC_CLIENT_VERSION)
@NpmPackage(value = "ical.js", version = "2.0.1")
```

**Hinweis Bundle-Size**: Diese Packages werden auf der FullCalendar-Klasse deklariert — **alle User zahlen den Bundle-Size-Preis**, auch wenn sie die Features nicht nutzen. Dies ist derselbe Trade-off wie in v7 und wird dokumentiert.

### Frontend: full-calendar.ts

- Plugin-Registrierung (google-calendar, icalendar)
- `addEventSource()` / `removeEventSource()` JS-Funktionen
- `restoreEventSources()` für Reattach

## Umsetzungsschritte

1. `ClientSideEventSource.java` kopieren und Jackson→elemental.json adaptieren
2. Subklassen kopieren und adaptieren
3. FullCalendar.java um Source-Registry erweitern
4. @NpmPackage hinzufügen
5. Frontend TS: Plugin-Registrierung + JS-Funktionen
6. Unit Tests (EventSourcesTest.java)

## Verifikation

- [ ] Event Source JSON-Serialisierung korrekt
- [ ] Source Registry Add/Remove
- [ ] Restore nach Detach/Reattach
- [ ] Frontend lädt Plugins

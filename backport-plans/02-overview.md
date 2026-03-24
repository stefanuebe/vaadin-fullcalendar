# Plan 2: Feature-Backport v7 → v6 — Phasenübersicht

> **Zielversion**: 6.4.0
> **Basis**: Branch `v6_v7_features_backport` (Vaadin 24.10, Java 17, elemental.json)

## Zentrale Erkenntnis

Die v6-Codebasis hat bereits die Grundlagen der v7-Architektur:
- **BeanProperties** Reflection-Caching existiert bereits in v6
- **JsonItemPropertyConverter** Interface + Annotations (`@JsonConverter`, `@JsonName`, `@JsonIgnore`) existieren bereits
- **5 Converter** existieren bereits (DayOfWeek, LocalDate, LocalDateTime, LocalTime, RecurringTime)
- Entry.java nutzt bereits BeanProperties-basierte Serialisierung mit elemental.json

Der Backport ist daher **inkrementelles Erweitern einer bestehenden Architektur**, kein Neuaufbau.

## Abhängigkeitsgraph

```
Phase A (Neue Converter + Utilities)
  ├── Phase B (Entry Model Erweiterungen)
  ├── Phase C1 (Performance, JsCallback, Option-System)
  │     ├── Phase E (Event Sources) — inkl. Frontend
  │     └── Phase F (Scheduler) — inkl. Frontend
  ├── Phase D (Event-Klassen + DOM-Events) — parallel zu C1
  └── Phase C2 (Listener-Registrierung + FC Version Bump) — braucht C1 + D
Phase H (Infrastruktur, Tests, Doku, Demo) — teilweise parallel
```

**Rollback-Strategie**: Nach jeder grünen Phase ein Git-Tag setzen (`v6.4-phaseA`, `v6.4-phaseB`, etc.). Bei Regressionen kann auf den letzten stabilen Tag zurückgesetzt werden.

## Phasen

| Phase | Titel | Aufwand | Abhängigkeiten | Datei |
|---|---|---|---|---|
| **A** | Neue Converter + JSON-Utilities | Klein | Keine | [02-phase-A-json-foundation.md](02-phase-A-json-foundation.md) |
| **B** | Entry Model Erweiterungen | Mittel | A | [02-phase-B-entry-model.md](02-phase-B-entry-model.md) |
| **C1** | Performance, JsCallback, Option-System | Mittel | A | [02-phase-C-fullcalendar-core.md](02-phase-C-fullcalendar-core.md) |
| **C2** | Listener-Registrierung + FC Version Bump | Klein | C1, D | [02-phase-C-fullcalendar-core.md](02-phase-C-fullcalendar-core.md) |
| **D** | Event-Klassen + DOM-Event-Registrierung (TS) | Klein-Mittel | A | [02-phase-D-events.md](02-phase-D-events.md) |
| **E** | Client-Side Event Sources + Frontend | Mittel | C1 | [02-phase-E-event-sources.md](02-phase-E-event-sources.md) |
| **F** | Scheduler Erweiterungen + Frontend | Groß | C1 | [02-phase-F-scheduler.md](02-phase-F-scheduler.md) |
| **H** | Infrastruktur, Tests, Doku, Demo | Mittel | Teilweise parallel | [02-phase-H-infrastructure.md](02-phase-H-infrastructure.md) |

**Phase G (Frontend) wurde aufgelöst** — Frontend-Arbeit ist in die jeweilige Backend-Phase integriert, da sie nur zusammen testbar ist.

## API-Kompatibilitätsstrategie

Version 6.4.0 ist ein **Minor Release mit bewusstem Plattformbruch** (V14→V24.10), aber die Java-API soll möglichst **binärkompatibel** bleiben:

| Änderung | Strategie |
|---|---|
| `overlap: boolean → Boolean` | Beides anbieten: `isOverlap()` (boolean, null→true) + `getOverlap()` (Boolean, nullable) |
| `constraint: String → Object` | Getter bleibt `String getConstraint()`, neuer `Object getConstraintValue()` |
| `setOption(Option, Serializable)` → `setOption(Option, Object)` | Overload: alte Signatur bleibt, neue kommt dazu |
| Deprecated Methoden | `@Deprecated` mit Verweis, nicht entfernen |

## Nicht backportiert

- **Vaadin/Aura Theme** (`full-calendar-theme-vaadin.css`) — erst ab V25
- **Jackson JSON** — elemental.json bleibt
- **Java 21 Sprachfeatures** — Java 17 Äquivalente
- **JsonFactory.java** — nicht nötig, v6 nutzt `Json.createObject()` direkt

## Risiken

| Risiko | Mitigation |
|---|---|
| FC JS 6.1.9→6.1.20 Breaking Changes | Changelog prüfen vor Bump, E2E-Tests |
| @NpmPackage Bundle-Size (rrule, google-cal, ical) | Alle User zahlen den Preis; dokumentieren als Trade-off |
| JsonItemPropertyConverter hat 2 Typparameter in v6 | Neue Converter mit `<SERVER_TYPE, T>` implementieren |

# Phase F: Scheduler Erweiterungen + Frontend

> **Priorität**: Mittel
> **Geschätzter Aufwand**: Groß
> **Abhängigkeiten**: Phase C1

## Zielsetzung

ComponentResourceAreaColumn (UC-024) und ResourceAreaColumn aus v7 backportieren. Erlaubt Vaadin-Components in Scheduler Resource-Spalten.

## Scope

### Neue Klassen (addon-scheduler) — Jackson → elemental.json

| Klasse | v7 Zeilen | Jackson-Refs | Beschreibung |
|---|---|---|---|
| `ComponentResourceAreaColumn.java` | 440 | 3 | Vaadin Components in Resource-Spalten |
| `ResourceAreaColumn.java` | 344 | 4 | **Neue** Konfigurationsklasse (existiert NICHT in v6) |

### FullCalendarScheduler.java Änderungen

- Neue Felder: `activeComponentColumns: List`, `hiddenContainer: Element`
- `onAttach()` / `onDetach()` Lifecycle für Component Columns
- `addResources()` / `removeResources()` — Component-Column Handling
- `unregisterResourcesInternally()` — Cleanup bei Resource-Entfernung
- Neue `SchedulerOption` Enum-Konstanten
- Deprecated Convenience-Methoden

### Scheduler.java Interface-Änderungen

- Neue Methoden für ResourceAreaColumn-Management
- Deprecations analog FullCalendarScheduler

### Frontend: full-calendar-scheduler.ts

- Component Column Rendering-Logik
- Lifecycle-Management (create/destroy Components bei Resource Add/Remove)

## Umsetzungsschritte

1. `ResourceAreaColumn.java` von v7 kopieren und adaptieren (neue Klasse)
2. `ComponentResourceAreaColumn.java` kopieren und adaptieren
3. `FullCalendarScheduler.java` Lifecycle erweitern
4. `Scheduler.java` Interface erweitern
5. `SchedulerOption` Enum erweitern
6. Frontend TS: Component Column Support
7. Unit Tests (ComponentResourceAreaColumnTest, SchedulerFeaturesTest)

## Verifikation

- [ ] ResourceAreaColumn JSON korrekt
- [ ] ComponentResourceAreaColumn erstellt/zerstört Components
- [ ] Detach/Reattach Lifecycle korrekt
- [ ] Resource Add/Remove mit Component Columns
- [ ] Hierarchische Resources mit Component Columns
- [ ] Scheduler-spezifische deprecated Methoden mit Verweis

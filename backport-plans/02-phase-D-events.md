# Phase D: Neue Event-Klassen + DOM-Event-Registrierung

> **Priorität**: Mittel-Hoch
> **Geschätzter Aufwand**: Klein-Mittel
> **Abhängigkeiten**: Phase A (JSON-Utilities)
> **Geblockt von**: Nichts — Event-Klassen referenzieren FullCalendar nur als Typparameter

## Zielsetzung

Alle neuen Event-Klassen aus v7 backportieren. Events sind reine Datenklassen mit `@DomEvent`-Annotation — sie können **vor** Phase C2 implementiert werden, da sie FullCalendar nur als generischen Typparameter referenzieren.

## Scope

### Neue Event-Klassen (KEIN Jackson-Dependency — direkt kopierbar)

| Klasse | v7 Zeilen | Beschreibung |
|---|---|---|
| `DropEvent.java` | 85 | Low-level Drop Event |
| `EventSourceFailureEvent.java` | 65 | Event Source Ladefehler |
| `TimeslotsUnselectEvent.java` | 49 | Timeslot-Deselektion |

### Neue Event-Klassen (Jackson → elemental.json Adaptation)

| Klasse | v7 Zeilen | Beschreibung |
|---|---|---|
| `EntryDragStartEvent.java` | 52 | Drag beginnt |
| `EntryDragStopEvent.java` | 51 | Drag endet |
| `EntryResizeStartEvent.java` | 51 | Resize beginnt |
| `EntryResizeStopEvent.java` | 51 | Resize endet |
| `EntryReceiveEvent.java` | 65 | Drop auf Entry empfangen |
| `ExternalEntryEvent.java` | 60 | Basis für externe Events |
| `ExternalEntryDroppedEvent.java` | 95 | Externer Drop auf Calendar |
| `ExternalEntryResizedEvent.java` | 77 | Externes Resize |

**Adaptation-Pattern** (mechanisch, alle gleich):
```java
// v7: @EventData("event.detail.data") JsonNode data
// v6: @EventData("event.detail.data") JsonObject data
// Zugriffe: node.get(key).asText() → json.getString(key) etc.
```

### Bestehende Event-Klassen — Namensabgleich

| v6 Klasse | v7 Klasse | Aktion |
|---|---|---|
| `EntryMouseLeaveEvent` | `EntryLeaveEvent` (?) | Prüfen ob Rename oder separate Klasse. Falls Rename: v6-Namen beibehalten für Kompatibilität. |
| `EntryMouseEnterEvent` | Existiert in beiden | Diff prüfen — ggf. neue Felder aus v7 |
| `EntryDroppedEvent` | Existiert in beiden | Diff prüfen — ggf. neue Felder |
| `EntryResizedEvent` | Existiert in beiden | Diff prüfen — ggf. neue Felder |
| `DatesRenderedEvent` | Existiert in beiden | `currentIntervalEnd` Tracking (Phase C1) |

### Frontend: DOM-Event-Registrierung in full-calendar.ts

Neue Events müssen im TypeScript als `CustomEvent` dispatched werden:
- DragStart/Stop, ResizeStart/Stop Events
- External Drop/Resize Events
- TimeslotsUnselect Event

## Umsetzungsschritte

1. Direkt kopierbare Events übernehmen (3 Stück)
2. Jackson-abhängige Events kopieren und adaptieren (8 Stück)
3. Bestehende Events um v7-Änderungen erweitern (Diff prüfen)
4. Namensabgleich EntryMouseLeaveEvent vs EntryLeaveEvent klären
5. Frontend: DOM-Event-Dispatching in full-calendar.ts hinzufügen
6. Unit Tests (falls vorhanden in v7)

## Verifikation

- [ ] Alle Event-Klassen kompilieren
- [ ] `@DomEvent` Annotationen korrekt
- [ ] Event-Konstruktoren parsen elemental.json korrekt
- [ ] Bestehende EntryMouseLeave/Enter Tests grün (Regression)
- [ ] Frontend dispatched neue Events

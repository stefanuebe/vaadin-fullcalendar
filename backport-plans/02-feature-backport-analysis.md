# Backport Plan 2: Feature-Analyse v7 → v6

## Methodik

Analyse aller Änderungen zwischen `v6_master` und `master` (v7).
Bewertung jedes Features nach Backport-Fähigkeit unter Berücksichtigung:
- Vaadin 24.10 API-Kompatibilität (kein Aura, kein V25-API)
- elemental.json bleibt (kein Jackson-Umbau)
- Java 17 (nicht 21) — keine Records, Sealed Classes, Pattern Matching for switch

---

## Gesamtübersicht

| Kategorie | Backportierbar | Nicht backportierbar | Anpassung nötig |
|---|---|---|---|
| Entry Model Erweiterungen | 5 | 0 | 5 (JSON-Adapter) |
| Neue Events | 8 | 0 | 8 (JSON-Adapter) |
| Client-Side Event Sources | 3 | 0 | 3 (JSON-Adapter) |
| JS Callback System | 1 | 0 | 1 (JSON-Adapter) |
| Performance/Caching | 2 | 0 | 0 |
| Converter Framework | 0 | 12 | — |
| Scheduler Features | 2 | 0 | 2 (JSON-Adapter) |
| Theming | 0 | 1 | — |
| Frontend (TS) | 1 | 0 | 0 |
| Infrastruktur | 3 | 0 | 0 |

---

## Detailanalyse

### BACKPORTIERBAR ✅

#### 1. Entry Model Erweiterungen

Alle neuen Entry-Properties können backportiert werden. Sie erfordern Anpassung der JSON-Serialisierung von Jackson (`ObjectNode`) auf elemental (`JsonObject`).

| Property | Typ | Beschreibung | Aufwand |
|---|---|---|---|
| `url` | `String` | Entry als klickbarer Link | Klein |
| `interactive` | `Boolean` | Keyboard-Fokussierbarkeit pro Entry | Klein |
| `recurringDuration` | `String` | ISO 8601 Duration für mehrtägige Recurring Events | Klein |
| `overlap` | `Boolean` (nullable) | War vorher `boolean`, jetzt nullable für Vererbung | Klein |
| `constraint` | `Object` | War vorher `String`, jetzt komplexe Constraint-Objekte | Mittel |

**RRule-Support** (RFC 5545 Recurrence):
| Klasse | Beschreibung | Aufwand |
|---|---|---|
| `RRule.java` | Fluent Builder für RFC 5545 | Mittel — Java 17 kompatibel, JSON-Adapter nötig |
| `RRuleConverter` | Serialisierung | Mittel — auf elemental umschreiben |
| `ExdateConverter` | Excluded Dates | Klein |
| `ExruleConverter` | Excluded Rules | Klein |

**Npm-Dependency**: `@fullcalendar/rrule` muss als `@NpmPackage` hinzugefügt werden.

#### 2. Neue Event-Klassen

Alle neuen Events basieren auf Vaadin Flow's `ComponentEvent` + `@DomEvent` — API ist identisch in V14 und V24.

| Event | Beschreibung | Aufwand |
|---|---|---|
| `EntryDragStartEvent` | Drag beginnt | Klein |
| `EntryDragStopEvent` | Drag endet | Klein |
| `EntryResizeStartEvent` | Resize beginnt | Klein |
| `EntryResizeStopEvent` | Resize endet | Klein |
| `EntryLeaveEvent` | Maus verlässt Entry | Klein |
| `EntryReceiveEvent` | Externes Drop auf Entry | Klein |
| `ExternalEntryDroppedEvent` | Externer Drop auf Calendar | Mittel |
| `ExternalEntryResizedEvent` | Externes Resize | Mittel |
| `EventSourceFailureEvent` | Event Source Ladefehler | Klein |
| `TimeslotsUnselectEvent` | Timeslot-Deselektion | Klein |

**Anpassung**: `@EventData` Annotationen verwenden `JsonValue`/`JsonObject` — in v7 Jackson-Typen, müssen auf elemental zurückgeschrieben werden.

#### 3. Client-Side Event Sources

Komplett neue Feature-Klasse. Erfordert JSON-Adapter und Frontend-Erweiterungen.

| Klasse | Beschreibung | Aufwand |
|---|---|---|
| `ClientSideEventSource` | Basis-Klasse für externe Quellen | Mittel |
| `GoogleCalendarEventSource` | Google Calendar Feed | Klein (nutzt ClientSideEventSource) |
| `ICalendarEventSource` | iCalendar Feed | Klein |
| `JsonFeedEventSource` | Generischer JSON Feed | Klein |

**Npm-Dependencies**: `@fullcalendar/google-calendar`, `@fullcalendar/icalendar`, `ical.js` müssen als `@NpmPackage` hinzugefügt werden.

#### 4. JsCallback System

| Klasse | Beschreibung | Aufwand |
|---|---|---|
| `JsCallback.java` | Erweitertes JS Callback System (ersetzt String-basierte Callbacks) | Mittel |

Ersetzt `eventDidMountCallback: String` durch typsicheres `JsCallback`-Objekt. Rein Java-seitige Logik, keine V25-Abhängigkeit.

#### 5. Performance & Caching

| Feature | Beschreibung | Aufwand |
|---|---|---|
| ~~`BeanProperties.java`~~ | ~~Reflections-Cache~~ | **Existiert bereits in v6** — nur Converter-Caching ergänzen |
| Bounded `LinkedHashMap` | lastFetchedEntries max 10.000 Entries | Klein — 1 Zeile |
| `volatile` + `refreshLock` | Thread-Safety Verbesserungen | Klein |

#### 6. Scheduler Erweiterungen

| Feature | Beschreibung | Aufwand |
|---|---|---|
| `ComponentResourceAreaColumn` | Vaadin-Components in Resource-Area Spalten (UC-024) | Groß — JSON-Adapter + Frontend |
| `ResourceAreaColumn` | **Neue** Konfigurationsklasse (existiert nicht in v6) | Mittel |

#### 7. Frontend TypeScript

| Datei | Änderungen | Aufwand |
|---|---|---|
| `full-calendar.ts` | +93 Zeilen: Event Source Management, JS Callbacks, Entry Cache, View-specific Options | Mittel — direkt übernehmbar |
| `full-calendar-scheduler.ts` | ComponentResourceAreaColumn Support | Mittel |

#### 8. Infrastruktur (direkt kopierbar)

| Element | Beschreibung | Aufwand |
|---|---|---|
| E2E Test Suite (`e2e-tests/`) | Playwright Tests — standalone, kein V25-Bezug | Klein (kopieren) |
| E2E Test App (`e2e-test-app/`) | Spring Boot Test-Harness — V24.10 anpassen | Mittel |
| Specs (`specs/`) | Use Cases, Architecture Docs | Klein (kopieren) |

---

### NICHT BACKPORTIERBAR ❌

#### 1. Vaadin Theme / Aura CSS

| Element | Grund |
|---|---|
| `full-calendar-theme-vaadin.css` (117 Zeilen) | "Vaadin Theme" = Aura-basiert, existiert erst ab V25. V24 hat nur Lumo. |
| Lumo-Theme Löschung | Lumo bleibt in v6 als Standard-Theme erhalten |

**Konsequenz**: v6 behält `full-calendar-theme-lumo.css`. Das neue `full-calendar-theme-vaadin.css` wird NICHT übernommen.

#### 2. Jackson JSON Migration

| Element | Grund |
|---|---|
| `tools.jackson.databind` Imports | V24 hat elemental.json als Teil von Flow. Jackson-Migration wäre ein massiver Umbau ohne Mehrwert für v6. |
| `JsonFactory.java` | Jackson-spezifische Utility-Klasse |
| `JsonConverter.java` + Annotation-System | Komplett auf Jackson aufgebaut |
| 12 Converter-Klassen | Alle Jackson-basiert — müssen stattdessen als elemental.json Adapter geschrieben werden |

**Konsequenz**: Alle Features die in v7 Jackson nutzen, müssen für v6 auf elemental.json Basis reimplementiert werden. Das betrifft die Serialisierung, NICHT die Features selbst.

#### 3. Java 21 Sprachfeatures

| Element | Grund |
|---|---|
| Pattern Matching for switch | Java 21 Feature, nicht in 17 |
| Record Patterns | Java 21 Feature |
| Sequenced Collections | Java 21 Feature |

**Prüfung nötig**: Ob v7-Code tatsächlich Java 21 Sprachfeatures nutzt (wahrscheinlich nicht, da kein zwingender Grund). Falls doch, umschreiben auf Java 17 Äquivalente.

---

## Empfohlene Backport-Reihenfolge

### Priorität 1: Quick Wins (hohes Nutzen/Aufwand-Verhältnis)

1. **Performance/Caching** — BeanProperties, bounded cache, thread safety
2. **Neue Events** — EntryDragStart/Stop, ResizeStart/Stop, etc.
3. **Entry Properties** — url, interactive, overlap (nullable)

### Priorität 2: Mittlerer Aufwand

4. **RRule Support** — Entry.rrule, Converter, @fullcalendar/rrule
5. **JsCallback System** — Ersetzt String-Callbacks
6. **Client-Side Event Sources** — Google Calendar, iCal, JSON Feed

### Priorität 3: Großer Aufwand

7. **ComponentResourceAreaColumn** — Scheduler UC-024
8. **Frontend TS Erweiterungen** — full-calendar.ts Aktualisierung

### Priorität 4: Infrastruktur (parallel möglich)

9. **E2E Tests** — Test Suite + Test App kopieren und anpassen
10. **Specs** — Dokumentation übernehmen

---

## Zentrale Herausforderung: JSON-Adapter

Die größte technische Herausforderung ist die **JSON-Schicht**. V7 nutzt Jackson überall:

```java
// V7 (Jackson)
ObjectNode json = JsonFactory.createObjectNode();
json.put("title", title);
ArrayNode arr = json.putArray("daysOfWeek");

// V6 (elemental)
JsonObject json = Json.createObject();
json.put("title", title);
JsonArray arr = Json.createArray();
```

**Strategie**: Für jedes backportierte Feature die Jackson-Aufrufe durch elemental.json Äquivalente ersetzen. Die Konvertierung ist mechanisch und gut definiert:

| Jackson (v7) | elemental.json (v6) |
|---|---|
| `ObjectNode` | `JsonObject` |
| `ArrayNode` | `JsonArray` |
| `JsonNode` | `JsonValue` |
| `JsonFactory.createObjectNode()` | `Json.createObject()` |
| `node.put(key, value)` | `json.put(key, value)` |
| `node.putArray(key)` | `Json.createArray()` + `json.put(key, arr)` |
| `node.get(key).asText()` | `json.getString(key)` |
| `node.get(key).asBoolean()` | `json.getBoolean(key)` |
| `node.has(key)` | `json.hasKey(key)` |
| `node.isNull()` | `json.getType() == JsonType.NULL` |

---

## Zusammenfassung

- **~80% der v7-Features** sind backportierbar
- **Hauptblocker**: Vaadin/Aura Theme (nicht verfügbar in V24)
- **Hauptaufwand**: JSON-Serialisierung von Jackson auf elemental.json adaptieren
- **Kein Blocker**: Java 17 vs 21 (v7 nutzt voraussichtlich keine Java 21 Sprachfeatures in der Addon-Logik)
- Die v7 Converter-Klassen werden NICHT 1:1 übernommen, sondern die Serialisierungslogik wird inline in den jeweiligen Klassen belassen (wie in v6 üblich)

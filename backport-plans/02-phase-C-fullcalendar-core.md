# Phase C: FullCalendar Core Refactoring

> **Aufgeteilt in C1 und C2**

---

## Phase C1: Performance, JsCallback, Option-System

> **Priorität**: Hoch
> **Geschätzter Aufwand**: Mittel
> **Abhängigkeiten**: Phase A

### Scope

#### 1. Performance & Memory

| Feature | Beschreibung | Aufwand |
|---|---|---|
| Bounded `lastFetchedEntries` | `LinkedHashMap` mit max 10.000 Entries | Klein |
| `volatile refreshAllEntriesRequested` | Thread-safe Refresh-Flag | Klein |
| `refreshLock` Synchronisation | `synchronized(refreshLock)` in `requestRefreshAllEntries()` | Klein |
| `currentIntervalEnd` Tracking | Neues Feld + Accessor | Klein |
| Provider-Rollback bei Fehler | `setEntryProvider()` stellt alten Provider bei Exception wieder her | Klein |

#### 2. JsCallback System (Neue Klasse)

| Klasse | v7 Zeilen | Beschreibung |
|---|---|---|
| `JsCallback.java` | 127 | Typsicheres Callback-Objekt, `toMarkerJson()` für Client |

**Auswirkung auf FullCalendar.java:**
- `eventDidMountCallback: String` → `userEntryDidMountCallback: volatile JsCallback`
- Neue `applyEntryDidMountMerge()` — merged User-Callback + native Event Listeners
- `buildEntryDidMountMerged()` (package-private, testbar)

#### 3. Option-Enum Erweiterung

~20+ neue Render-Hook-Konstanten:
- SLOT_LABEL_*, SLOT_LANE_*, VIEW_*, NOW_INDICATOR_*
- WEEK_NUMBER_*, MORE_LINK_*, ALL_DAY_*, DAY_CELL_*, DAY_HEADER_*
- NO_ENTRIES_*, LOADING, ENTRY_DATA_TRANSFORM, NAV_LINK_*

**Converter-Cache im Option-Enum:**
- `@JsonConverter` Annotation auf Option-Konstanten (nutzt bestehende Annotation)
- Statischer Converter-Cache bei Initialisierung
- `Option.getConverters()` und `Option.convertValue(Object)` Methoden

#### 4. setOption() Erweiterung (binärkompatibel)

**Bestehende Signaturen bleiben erhalten:**
```java
setOption(Option option, Serializable value)           // bleibt
setOption(String option, Serializable value)            // bleibt
setOption(String option, JsonValue value)               // bleibt
```

**Neue Overloads hinzufügen:**
```java
setOption(Option option, Object value)                  // neu, breiter
setOption(String option, Object value, JsonItemPropertyConverter<?,?>... converters)  // neu
```

**callOptionUpdate() Erweiterung:**
- JsCallback-Erkennung → `toMarkerJson()` Konvertierung
- ClientSideValue Auto-Konvertierung
- eventDidMount Spezialbehandlung

#### 5. Frontend: JsCallback Marker in full-calendar.ts

setOption() in TS muss JsCallback-Marker erkennen und JS-Funktionen evaluieren.

#### 6. Deprecated Convenience-Methoden

~30 Methoden werden `@Deprecated(since = "6.4.0")`:
- `setFirstDay()`, `setLocale()`, `setBusinessHours()`, `setSnapDuration()`
- `setSlotMinTime()`, `setSlotMaxTime()`, `setFixedWeekCount()`
- `setEntryDidMountCallback(String)` → `setOption(ENTRY_DID_MOUNT, JsCallback.of(...))`
- etc.

#### 7. @NotNull Entfernung

Alle `@NotNull` Annotationen aus public API entfernen.

### Verifikation C1

- [ ] Alle bestehenden Tests grün
- [ ] JsCallback Tests
- [ ] Option Converter Tests
- [ ] Bounded cache Test
- [ ] setOption() mit alten UND neuen Signaturen

---

## Phase C2: Listener-Registrierung + FC Version Bump

> **Priorität**: Mittel
> **Geschätzter Aufwand**: Klein
> **Abhängigkeiten**: C1, Phase D (Event-Klassen müssen kompilieren)

### Scope

#### 1. Neue Listener-Methoden in FullCalendar.java

| Listener | Event-Klasse (Phase D) |
|---|---|
| `addEntryDragStartListener()` | `EntryDragStartEvent` |
| `addEntryDragStopListener()` | `EntryDragStopEvent` |
| `addEntryResizeStartListener()` | `EntryResizeStartEvent` |
| `addEntryResizeStopListener()` | `EntryResizeStopEvent` |
| `addEntryReceiveListener()` | `EntryReceiveEvent` |
| `addTimeslotsUnselectListener()` | `TimeslotsUnselectEvent` |
| `addExternalEntryDroppedListener()` | `ExternalEntryDroppedEvent` |
| `addExternalEntryResizedListener()` | `ExternalEntryResizedEvent` |
| `addEventSourceFailureListener()` | `EventSourceFailureEvent` |

Hinweis: `addEntryMouseLeaveListener()` existiert bereits in v6 (→ `EntryMouseLeaveEvent`). v7 hat ggf. `EntryLeaveEvent` — Namensabgleich prüfen ob Rename oder separate Klasse.

#### 2. FullCalendar JS Version Bump

```java
// addon/FullCalendar.java
FC_CLIENT_VERSION: "6.1.9" → "6.1.20"

// addon-scheduler/FullCalendarScheduler.java — EBENFALLS bumpen!
FC_SCHEDULER_CLIENT_VERSION: "6.1.9" → "6.1.20"
```

**Vor dem Bump**: FC 6.1.10-6.1.20 Changelog prüfen auf Breaking Changes.

Aktualisierte @NpmPackage-Versionen für moment/moment-timezone.

### Verifikation C2

- [ ] Alle Listener kompilieren mit Event-Klassen aus Phase D
- [ ] FC 6.1.20 Changelog geprüft — keine Breaking Changes
- [ ] Demo startet mit neuer FC-Version

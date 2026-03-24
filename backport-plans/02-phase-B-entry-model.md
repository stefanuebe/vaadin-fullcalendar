# Phase B: Entry Model Erweiterungen

> **Priorität**: Hoch
> **Geschätzter Aufwand**: Mittel
> **Abhängigkeiten**: Phase A (Neue Converter)

## Zielsetzung

Neue Entry-Properties und RRule-Support aus v7 **inkrementell** zur bestehenden Entry.java hinzufügen. Kein Wholesale-Replace — die bestehende Entry.java hat akkumulierte Bugfixes (#186, #207) die in v7 ggf. nicht vorhanden sind.

## Scope

### Neue Entry-Properties

| Property | Typ | Beschreibung | Converter | Aufwand |
|---|---|---|---|---|
| `url` | `String` | Entry als klickbarer `<a>` Link | Keiner | Klein |
| `interactive` | `Boolean` | Keyboard-Fokussierbarkeit (nullable) | Keiner | Klein |
| `recurringDuration` | `String` | ISO 8601 Duration für Recurring Events | DurationConverter | Klein |

### Typänderungen (binärkompatibel)

#### `overlap`: `boolean` → `Boolean`

**Lombok-Interaktion**: Entry nutzt class-level `@Getter`/`@Setter`. Bei `boolean` generiert Lombok `isOverlap()`, bei `Boolean` generiert Lombok `getOverlap()`. Um beides anzubieten:

1. Feldtyp ändern: `private Boolean overlap;` (default `null` statt `true`)
2. **Lombok unterdrücken**: `@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)` auf dem Feld
3. Manuelle Methoden:
   - `public boolean isOverlap()` — returns `overlap != null ? overlap : true` (binärkompatibel, `@Deprecated(since="6.4.0")`)
   - `public Boolean getOverlap()` — returns nullable `Boolean` (neue API)
   - `public void setOverlap(Boolean overlap)` — neue API
   - `public void setOverlap(boolean overlap)` — binärkompatibel, delegiert an `setOverlap(Boolean.valueOf(overlap))`
4. **`isOverlapAllowed()` / `setOverlapAllowed(boolean)`**: Bestehende Convenience-Methoden ebenfalls `@Deprecated`, delegieren an neue API mit Null-Coalescing.

#### `constraint`: Bleibt `String` in v6

v7 hat `constraint` auf `Object` geändert um `setConstraint(BusinessHours)` zu ermöglichen (speichert `BusinessHours.toJson()` als JsonNode).

**Bewusste Entscheidung für v6**: Typ bleibt `String` um Binärkompatibilität zu wahren. Das bedeutet:
- `setConstraint(String)` funktioniert weiterhin (groupId oder `"businessHours"`)
- `setConstraint(BusinessHours)` Overload aus v7 wird **nicht** backportiert
- `setConstraintToBusinessHours()` Convenience-Methode kann backportiert werden (setzt `"businessHours"` String)
- **Dokumentiert als bewusste Feature-Lücke** gegenüber v7

### RRule-Support (RFC 5545 — komplett neue Klasse)

| Klasse | v7 Zeilen | Beschreibung |
|---|---|---|
| `RRule.java` | 581 | Fluent Builder mit allen RFC 5545 Feldern |
| `RRuleConverter.java` | 26 | RRule → JsonObject (elemental.json) |
| `ExdateConverter.java` | 33 | Excluded Dates → JsonArray |
| `ExruleConverter.java` | 37 | Excluded Rules → JsonArray |

**Neue Entry-Felder für RRule:**
- `rrule: RRule` — RFC 5545 Recurrence Rule
- `exdate: List<LocalDate>` — Excluded Dates
- `exrule: List<RRule>` — Excluded Rules

**Neue @NpmPackage**: `@fullcalendar/rrule` (auf FullCalendar.java — **alle User zahlen Bundle-Size**, auch ohne RRule-Nutzung; dokumentieren als Trade-off, analog Phase E)

### Java 21 → 17 Anpassung

RRule.java in v7 nutzt `instanceof` Pattern Matching — auf Java 17 Cast-Syntax umschreiben.

## Umsetzungsschritte

1. Einfache Properties hinzufügen: `url`, `interactive`, `recurringDuration` (mit Lombok @Getter/@Setter)
2. `overlap`: Feldtyp auf `Boolean` ändern, manuellen `isOverlap()` beibehalten, `getOverlap()` hinzufügen
3. `RRule.java` von v7 kopieren, Java 17 anpassen, Jackson→elemental.json in Converter
4. RRule-Converter implementieren
5. Entry-Serialisierung um neue Felder erweitern (@JsonConverter Annotations)
6. `@NpmPackage("@fullcalendar/rrule")` in FullCalendar.java
7. Unit Tests

## Verifikation

- [ ] Bestehende Entry-Tests grün (Regression)
- [ ] `overlap=null` serialisiert nicht als `false`
- [ ] `isOverlap()` gibt `true` zurück wenn `overlap == null`
- [ ] `isOverlapAllowed()` / `setOverlapAllowed()` deprecated mit Delegation
- [ ] Lombok `@Getter(AccessLevel.NONE)` auf overlap-Feld verifiziert
- [ ] RRule Builder-Tests
- [ ] RRule JSON-Serialisierung korrekt
- [ ] url, interactive, recurringDuration in toJson() enthalten

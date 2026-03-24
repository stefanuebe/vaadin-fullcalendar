# Phase A: Neue Converter + JSON-Utilities

> **Priorität**: Höchste — blockt alle anderen Phasen
> **Geschätzter Aufwand**: Klein (nicht Mittel — Infrastruktur existiert bereits)
> **Abhängigkeiten**: Keine (Startphase)

## Ausgangslage (v6 hat bereits)

Die v6-Codebasis hat die wesentliche Infrastruktur schon:

| Klasse | Status | Pfad |
|---|---|---|
| `BeanProperties.java` | **Existiert** | `addon/src/.../BeanProperties.java` |
| `JsonItemPropertyConverter<S,T>` | **Existiert** | `addon/src/.../converters/JsonItemPropertyConverter.java` |
| `@JsonConverter` Annotation | **Existiert** | `addon/src/.../json/JsonConverter.java` |
| `@JsonName` Annotation | **Existiert** | `addon/src/.../json/JsonName.java` |
| `@JsonIgnore` Annotation | **Existiert** | `addon/src/.../json/JsonIgnore.java` |
| `@JsonUpdateAllowed` Annotation | **Existiert** | `addon/src/.../json/JsonUpdateAllowed.java` |
| `ClientSideValue` Interface | **Existiert** | `addon/src/.../ClientSideValue.java` |
| `JsonUtils.java` | **Existiert** | `addon/src/.../JsonUtils.java` |
| `DayOfWeekItemConverter` | **Existiert** | `addon/src/.../converters/` |
| `LocalDateConverter` | **Existiert** | `addon/src/.../converters/` |
| `LocalDateTimeConverter` | **Existiert** | `addon/src/.../converters/` |
| `LocalTimeConverter` | **Existiert** | `addon/src/.../converters/` |
| `RecurringTimeConverter` | **Existiert** | `addon/src/.../converters/` |
| Entry.toJson() mit BeanProperties | **Existiert** | Entry.java Zeile 154ff |

## Tatsächlicher Scope (nur Neues)

### Neue Annotations

| Klasse | v7 Zeilen | Beschreibung |
|---|---|---|
| `@JsonConverters` | 15 | Repeatable-Container für `@JsonConverter` |
| `@JsonReadField` | 19 | Markiert Felder als read-only vom Client |

### Neue Converter

Alle neuen Converter implementieren `JsonItemPropertyConverter<SERVER_TYPE, T>` mit **beiden Typparametern** (v6-Konvention). Die JSON-Operationen nutzen `elemental.json` direkt (`Json.createObject()`, `Json.createArray()`, etc.) — **kein JsonFactory nötig**.

| Converter | Beschreibung | Aufwand |
|---|---|---|
| `DayOfWeekConverter` | Einzelner DayOfWeek → int (vs. existierender DayOfWeekItemConverter für Sets) | Klein |
| `DayOfWeekArrayConverter` | Set<DayOfWeek> → JsonArray | Klein |
| `DurationConverter` | ISO 8601 Duration String → JsonValue | Klein |
| `LocaleConverter` | Locale → String (language-tag Format) | Klein |
| `StringArrayConverter` | List<String> → JsonArray | Klein |
| `BusinessHoursConverter` | BusinessHours → JsonArray | Klein |
| `ToolbarConverter` | Header/Footer → JsonObject | Klein |
| `ClientSideValueConverter` | ClientSideValue Interface → getClientSideValue() | Klein |

### JsonUtils-Erweiterungen

Neue Hilfsmethoden aus v7 übernehmen (auf elemental.json Basis):
- `hasNonNull(JsonObject, String)` — prüft `hasKey()` + `getType() != NULL`
- Ggf. weitere Convenience-Methoden

### Converter-Caching Optimierung

v6 Entry.toJson() erstellt Converter-Instanzen per Reflection bei **jedem Aufruf** (Entry.java ~Zeile 166, markiert als TODO). v7 cacht diese in BeanProperties. Dieses Caching übernehmen.

## Umsetzungsschritte

1. Neue Annotations hinzufügen (JsonConverters, JsonReadField)
2. Neue Converter-Klassen schreiben (8 Stück, alle mit elemental.json)
3. JsonUtils erweitern
4. BeanProperties Converter-Caching einbauen (TODO aus Entry.java umsetzen)
5. Unit Tests für neue Converter (ConverterTest.java)

## Verifikation

- [ ] `mvn test -pl addon` — alle bestehenden Tests grün (Regression)
- [ ] Neue Converter-Tests grün
- [ ] Entry.toJson() produziert identisches JSON wie vorher

## Nicht in Scope

- ~~JsonFactory.java~~ — nicht nötig, v6 nutzt `Json.*` direkt
- ~~BeanProperties.java kopieren~~ — existiert bereits
- ~~Annotation-Framework aufbauen~~ — existiert bereits
- Neue Entry-Properties (Phase B)

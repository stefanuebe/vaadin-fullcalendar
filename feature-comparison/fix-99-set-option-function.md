# Fix 99: Generisches setOptionFunction() API

**Voraussetzung:** Alle anderen Fixes (01–12) müssen abgeschlossen sein.

## Context

Aktuell braucht jede FC-Option, die eine JS-Funktion akzeptiert, eine eigene dedizierte Callback-Methode auf `FullCalendar` + eine korrespondierende TS-Methode in `full-calendar.ts`. Beispiel:

```java
// Java — pro Option eine eigene Methode
public void setSelectAllowCallback(String jsFunction) {
    getElement().callJsFunction("setSelectAllowCallback", jsFunction);
}
public void setEventOverlapCallback(String jsFunction) {
    getElement().callJsFunction("setEventOverlapCallback", jsFunction);
}
public void setDropAcceptCallback(String jsFunction) {
    getElement().callJsFunction("setDropAcceptCallback", jsFunction);
}
// ... für jede weitere Option mit Function-Support
```

```typescript
// TS — pro Option eine eigene Methode
setSelectAllowCallback(s: string) {
    this.setOption('selectAllow', new Function("return " + s)());
}
setEventOverlapCallback(s: string) {
    this.setOption('eventOverlap', new Function("return " + s)());
}
// ... etc.
```

Das ist repetitiv und skaliert nicht. Jede neue FC-Option mit Function-Support erfordert 2 neue Methoden (Java + TS).

## Lösung

Ein **generisches** `setOptionFunction(Option, String)` das den JS-String client-seitig in eine echte Funktion wrapped — genau wie die bestehenden Callback-Methoden, aber für beliebige Options.

### Java-API

```java
/**
 * Sets a FullCalendar option to a JavaScript function value. The function string is
 * evaluated client-side via {@code new Function()} and the result is passed to FC's
 * {@code setOption()}.
 * <p>
 * Use this for FC options that accept either a plain value (set via {@link #setOption})
 * or a JavaScript function. Pass {@code null} to clear the function and revert to the
 * plain-value behavior.
 * <p>
 * <b>Note:</b> No escaping or validation is applied to the string. The caller is
 * responsible for ensuring the function string is safe and syntactically correct.
 * <p>
 * Example:
 * <pre>{@code
 * // Dynamic drop acceptance based on element data
 * calendar.setOptionFunction(Option.DROP_ACCEPT,
 *     "function(draggable) { return draggable.dataset.type === 'task'; }");
 *
 * // Custom event ordering
 * calendar.setOptionFunction(Option.ENTRY_ORDER,
 *     "function(a, b) { return a.extendedProps.priority - b.extendedProps.priority; }");
 *
 * // Per-event overlap control
 * calendar.setOptionFunction(Option.EVENT_OVERLAP,
 *     "function(stillEvent, movingEvent) { return stillEvent.display === 'background'; }");
 *
 * // Clear — revert to plain option value
 * calendar.setOptionFunction(Option.DROP_ACCEPT, null);
 * }</pre>
 *
 * @param option the FC option to set as a function
 * @param jsFunction JavaScript function string, or {@code null} to clear
 */
public void setOptionFunction(Option option, String jsFunction) {
    setOptionFunction(option.getOptionKey(), jsFunction);
}

/**
 * String-key variant of {@link #setOptionFunction(Option, String)}.
 */
public void setOptionFunction(String optionKey, String jsFunction) {
    if (jsFunction == null) {
        setOption(optionKey, null);
    } else {
        getElement().callJsFunction("setOptionFunction", optionKey, jsFunction);
    }
}
```

### TypeScript — eine einzige generische Methode

```typescript
/**
 * Evaluates a JS function string and sets it as an FC option.
 * Replaces all individual set*Callback TS methods.
 */
setOptionFunction(optionKey: string, jsFunction: string) {
    this.setOption(optionKey, new Function("return " + jsFunction)());
}
```

## Migration — bestehende Callback-Methoden

Nach Einführung von `setOptionFunction` können die dedizierten Callback-Methoden **entfernt** oder zu Einzeilern vereinfacht werden:

### Entfernen (Java + TS)

| Java-Methode | Ersetzt durch |
|---|---|
| `setSelectAllowCallback(String)` | `setOptionFunction(Option.SELECT_ALLOW, ...)` |
| `setEventAllowCallback(String)` | `setOptionFunction(Option.EVENT_ALLOW, ...)` |
| `setEventOverlapCallback(String)` | `setOptionFunction(Option.EVENT_OVERLAP, ...)` |
| `setSelectOverlapCallback(String)` | `setOptionFunction(Option.SELECT_OVERLAP, ...)` |
| `setValidRangeCallback(String)` | `setOptionFunction(Option.VALID_RANGE, ...)` |
| `setDropAcceptCallback(String)` | `setOptionFunction(Option.DROP_ACCEPT, ...)` |
| `setEntryOrderCallback(String)` | `setOptionFunction(Option.ENTRY_ORDER, ...)` |
| `setLoadingCallback(String)` | `setOptionFunction("loading", ...)` |
| `setEventDataTransformCallback(String)` | `setOptionFunction("eventDataTransform", ...)` |
| `setEventSourceSuccessCallback(String)` | `setOptionFunction("eventSourceSuccess", ...)` |
| `setFixedMirrorParent(String)` | Sonderfall — prüfen ob gleiches Pattern |

Korrespondierende TS-Methoden (`setSelectAllowCallback`, `setEventOverlapCallback`, etc.) werden ebenfalls entfernt — alles läuft über das generische `setOptionFunction`.

### Behalten (haben Zusatzlogik)

| Java-Methode | Grund |
|---|---|
| `setNavLinkDayClickCallback(String)` | Prüfen — evtl. auch auf `setOptionFunction` umstellbar |
| `setNavLinkWeekClickCallback(String)` | Prüfen — dito |
| Render-Hook-Callbacks (40 Methoden) | Nutzen `setOption(String, ...)` direkt — prüfen ob `setOptionFunction` hier auch passt |

## Auswirkung auf die Render-Hook-Callbacks

Die 40 Render-Hook-Methoden (`setDayCellClassNamesCallback`, etc.) setzen aktuell auch JS-Strings via `setOption("dayCellClassNames", jsFunction)`. Das funktioniert nur, wenn FC den String selbst als Funktion evaluiert. Prüfen:
- Falls FC die Render-Hooks als String akzeptiert → bleiben wie sie sind
- Falls FC eine echte Funktion erwartet → auf `setOptionFunction` umstellen

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java` — neue Methode + Migration
- `addon/src/main/resources/META-INF/resources/frontend/vaadin-full-calendar/full-calendar.ts` — neue TS-Methode, alte entfernen
- Tests + Docs aktualisieren

## Verification

1. `mvn test -pl addon`
2. `mvn clean install -DskipTests`
3. Grep für entfernte Callback-Methodennamen — zero hits
4. E2E-Tests für mindestens 2 verschiedene Options mit `setOptionFunction` verifizieren

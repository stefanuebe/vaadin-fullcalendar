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

### CallbackOption Enum

Eigene Enum-Klasse für alle FC-Optionen, die JS-Funktionen akzeptieren. Implementiert `ClientSideValue` für den FC-Key.

```java
/**
 * FC options that accept a JavaScript function value. Use with
 * {@link FullCalendar#setOptionFunction(CallbackOption, String)}.
 * <p>
 * For plain (non-function) option values, use {@link FullCalendar.Option} with
 * {@link FullCalendar#setOption(FullCalendar.Option, Object)} instead.
 */
public enum CallbackOption implements ClientSideValue {

    // ---- Render hooks: Entry ----
    ENTRY_CLASS_NAMES("eventClassNames"),
    ENTRY_CONTENT("eventContent"),
    ENTRY_DID_MOUNT("eventDidMount"),
    ENTRY_WILL_UNMOUNT("eventWillUnmount"),

    // ---- Render hooks: Day Cell ----
    DAY_CELL_CLASS_NAMES("dayCellClassNames"),
    DAY_CELL_CONTENT("dayCellContent"),
    DAY_CELL_DID_MOUNT("dayCellDidMount"),
    DAY_CELL_WILL_UNMOUNT("dayCellWillUnmount"),

    // ---- Render hooks: Day Header ----
    DAY_HEADER_CLASS_NAMES("dayHeaderClassNames"),
    DAY_HEADER_CONTENT("dayHeaderContent"),
    DAY_HEADER_DID_MOUNT("dayHeaderDidMount"),
    DAY_HEADER_WILL_UNMOUNT("dayHeaderWillUnmount"),

    // ---- Render hooks: Slot Label ----
    SLOT_LABEL_CLASS_NAMES("slotLabelClassNames"),
    SLOT_LABEL_CONTENT("slotLabelContent"),
    SLOT_LABEL_DID_MOUNT("slotLabelDidMount"),
    SLOT_LABEL_WILL_UNMOUNT("slotLabelWillUnmount"),

    // ---- Render hooks: Slot Lane ----
    SLOT_LANE_CLASS_NAMES("slotLaneClassNames"),
    SLOT_LANE_CONTENT("slotLaneContent"),
    SLOT_LANE_DID_MOUNT("slotLaneDidMount"),
    SLOT_LANE_WILL_UNMOUNT("slotLaneWillUnmount"),

    // ---- Render hooks: View ----
    VIEW_CLASS_NAMES("viewClassNames"),
    VIEW_DID_MOUNT("viewDidMount"),
    VIEW_WILL_UNMOUNT("viewWillUnmount"),

    // ---- Render hooks: Now Indicator ----
    NOW_INDICATOR_CLASS_NAMES("nowIndicatorClassNames"),
    NOW_INDICATOR_CONTENT("nowIndicatorContent"),
    NOW_INDICATOR_DID_MOUNT("nowIndicatorDidMount"),
    NOW_INDICATOR_WILL_UNMOUNT("nowIndicatorWillUnmount"),

    // ---- Render hooks: Week Number ----
    WEEK_NUMBER_CLASS_NAMES("weekNumberClassNames"),
    WEEK_NUMBER_CONTENT("weekNumberContent"),
    WEEK_NUMBER_DID_MOUNT("weekNumberDidMount"),
    WEEK_NUMBER_WILL_UNMOUNT("weekNumberWillUnmount"),

    // ---- Render hooks: More Link ----
    MORE_LINK_CLASS_NAMES("moreLinkClassNames"),
    MORE_LINK_CONTENT("moreLinkContent"),
    MORE_LINK_DID_MOUNT("moreLinkDidMount"),
    MORE_LINK_WILL_UNMOUNT("moreLinkWillUnmount"),

    // ---- Render hooks: No Events ----
    NO_EVENTS_CLASS_NAMES("noEventsClassNames"),
    NO_EVENTS_CONTENT("noEventsContent"),
    NO_EVENTS_DID_MOUNT("noEventsDidMount"),
    NO_EVENTS_WILL_UNMOUNT("noEventsWillUnmount"),

    // ---- Render hooks: All Day ----
    ALL_DAY_CLASS_NAMES("allDayClassNames"),
    ALL_DAY_CONTENT("allDayContent"),
    ALL_DAY_DID_MOUNT("allDayDidMount"),
    ALL_DAY_WILL_UNMOUNT("allDayWillUnmount"),

    // ---- Interaction callbacks ----
    SELECT_ALLOW("selectAllow"),
    EVENT_ALLOW("eventAllow"),
    EVENT_OVERLAP("eventOverlap"),
    SELECT_OVERLAP("selectOverlap"),
    DROP_ACCEPT("dropAccept"),
    VALID_RANGE("validRange"),
    ENTRY_ORDER("eventOrder"),

    // ---- Data transform / loading callbacks ----
    LOADING("loading"),
    EVENT_DATA_TRANSFORM("eventDataTransform"),
    EVENT_SOURCE_SUCCESS("eventSourceSuccess"),

    // ---- Navigation callbacks ----
    NAV_LINK_DAY_CLICK("navLinkDayClick"),
    NAV_LINK_WEEK_CLICK("navLinkWeekClick");

    private final String clientSideValue;

    CallbackOption(String clientSideValue) {
        this.clientSideValue = clientSideValue;
    }

    @Override
    public String getClientSideValue() {
        return clientSideValue;
    }
}
```

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
 * // Render hook — custom day cell CSS classes
 * calendar.setOptionFunction(CallbackOption.DAY_CELL_CLASS_NAMES,
 *     "function(arg) { return arg.isPast ? ['past-day'] : []; }");
 *
 * // Dynamic drop acceptance based on element data
 * calendar.setOptionFunction(CallbackOption.DROP_ACCEPT,
 *     "function(draggable) { return draggable.dataset.type === 'task'; }");
 *
 * // Per-event overlap control
 * calendar.setOptionFunction(CallbackOption.EVENT_OVERLAP,
 *     "function(stillEvent, movingEvent) { return stillEvent.display === 'background'; }");
 *
 * // Clear — revert to plain option value
 * calendar.setOptionFunction(CallbackOption.DROP_ACCEPT, null);
 * }</pre>
 *
 * @param callbackOption the FC option to set as a function
 * @param jsFunction JavaScript function string, or {@code null} to clear
 */
public void setOptionFunction(CallbackOption callbackOption, String jsFunction) {
    setOptionFunction(callbackOption.getClientSideValue(), jsFunction);
}

/**
 * String-key variant of {@link #setOptionFunction(CallbackOption, String)}
 * for FC options not covered by the {@link CallbackOption} enum.
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
| `setSelectAllowCallback(String)` | `setOptionFunction(CallbackOption.SELECT_ALLOW, ...)` |
| `setEntryAllowCallback(String)` | `setOptionFunction(CallbackOption.EVENT_ALLOW, ...)` |
| `setEntryOverlapCallback(String)` | `setOptionFunction(CallbackOption.EVENT_OVERLAP, ...)` |
| `setSelectOverlapCallback(String)` | `setOptionFunction(CallbackOption.SELECT_OVERLAP, ...)` |
| `setValidRangeCallback(String)` | `setOptionFunction(CallbackOption.VALID_RANGE, ...)` |
| `setDropAcceptCallback(String)` | `setOptionFunction(CallbackOption.DROP_ACCEPT, ...)` |
| `setEntryOrderCallback(String)` | `setOptionFunction(CallbackOption.ENTRY_ORDER, ...)` |
| `setLoadingCallback(String)` | `setOptionFunction(CallbackOption.LOADING, ...)` |
| `setEntryDataTransformCallback(String)` | `setOptionFunction(CallbackOption.EVENT_DATA_TRANSFORM, ...)` |
| `setEventSourceSuccessCallback(String)` | `setOptionFunction(CallbackOption.EVENT_SOURCE_SUCCESS, ...)` |
| `setFixedMirrorParent(String)` | Sonderfall — prüfen ob gleiches Pattern |
| Alle 40+ Render-Hook-Callbacks | `setOptionFunction(CallbackOption.XXX, ...)` |
| `setNavLinkDayClickCallback(String)` | `setOptionFunction(CallbackOption.NAV_LINK_DAY_CLICK, ...)` |
| `setNavLinkWeekClickCallback(String)` | `setOptionFunction(CallbackOption.NAV_LINK_WEEK_CLICK, ...)` |

**Hinweis:** Methodennamen folgen Fix 02 (Event→Entry): `setEventAllowCallback` → `setEntryAllowCallback`,
`setEventOverlapCallback` → `setEntryOverlapCallback`, `setEventDataTransformCallback` → `setEntryDataTransformCallback`.

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

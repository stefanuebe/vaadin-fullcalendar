# Fix 1: Remove typed setters and getters from FullCalendar

## Context

PR #223 review comment by stefanuebe:
> Review these typed setters. If they did not exist before, plan to remove them for now and better check the respective option, if it might need updated javadocs — or explain me, why they are needed.
> There is a reason, that we have the Option enum, exactly to prevent dozens of setters here.
> I want a list of all methods, that gets removed

The `FullCalendar` class has grown with ~81 typed setter and ~18 typed getter methods that are thin `setOption`/`getOption` wrappers. These belong in the `Option` enum + generic `setOption()`/`getOption()` API, not as dedicated methods.

## Scope

Remove ALL typed setter **and getter** methods from `FullCalendar.java` that:
1. Were added in the `missing-features-check` branch (do NOT exist on `master`)
2. Are simple one-line `setOption()` or `getOption()` delegations
3. Have no additional logic (validation, conversion, etc.)

**DO NOT remove:**
- Methods that existed on `master` (check with `git show master:...`)
- Methods with real logic (e.g., `setHiddenDays` does enum-to-int conversion, `setValidRange` builds JSON)
- Listener registration methods (`addXxxListener`)
- JS callback setters (`setXxxCallback` using `callJsFunction`)
- Render hook callbacks (classNames/content/didMount/willUnmount)
- Methods handled by other Fix plans (event source API → Fix 3/4/5, ThemeSystem → Fix 11)

---

## A. REMOVE — Simple setOption() wrappers (81 setters)

These are one-line `setOption(Option.XXX, value)` delegations with no additional logic.

| Method | Line | Option | Getter to also remove |
|---|---|---|---|
| `setAllDayMaintainDuration(boolean)` | 3365 | ALL_DAY_MAINTAIN_DURATION | — |
| `setAllDaySlot(boolean)` | 2348 | ALL_DAY_SLOT | `isAllDaySlot()` :3889 |
| `setAspectRatio(double)` | 2288 | ASPECT_RATIO | `getAspectRatio()` :3997 |
| `setCloseHint(String)` | 3046 | CLOSE_HINT | — |
| `setContentHeight(int)` | 2298 | CONTENT_HEIGHT | `getContentHeight()` :3988 |
| `setContentHeight(String)` | 2308 | CONTENT_HEIGHT | (same getter) |
| `setContentSecurityPolicyNonce(String)` | 3210 | CONTENT_SECURITY_POLICY | — |
| `setDateAlignment(String)` | 3192 | DATE_ALIGNMENT | — |
| `setDateIncrement(String)` | 3178 | DATE_INCREMENT | — |
| `setDayHeaderFormat(String)` | 2626 | DAY_HEADER_FORMAT | — |
| `setDayHeaders(boolean)` | 2432 | DAY_HEADERS | `isDayHeaders()` :3943 |
| `setDayMaxEventRows(int)` | 2898 | DAY_MAX_EVENT_ROWS | — |
| `setDayMaxEventRowsFitToCell()` | 2909 | DAY_MAX_EVENT_ROWS | — |
| `setDayMinWidth(int)` | 2606 | DAY_MIN_WIDTH | `getDayMinWidth()` :3961 |
| `setDayPopoverFormat(String)` | 2886 | DAY_POPOVER_FORMAT | — |
| `setDefaultAllDay(boolean)` | 3410 | DEFAULT_ALL_DAY | — |
| `setDefaultRangeSeparator(String)` | 2873 | DEFAULT_RANGE_SEPARATOR | — |
| `setDirection(Direction)` | 2477 | DIRECTION | — |
| `setDisplayEntryTime(boolean)` | 2261 | ENTRY_DISPLAY_TIME | — |
| `setDisplayEventEnd(boolean)` | 2762 | (uses ENTRY prefix internally) | — |
| `setDragRevertDuration(int)` | 3354 | DRAG_REVERT_DURATION | — |
| `setDragScroll(boolean)` | 2043 | DRAG_SCROLL | — |
| `setDropAccept(String)` | 1118 | DROP_ACCEPT | Setter entfernen, dafür `setDropAcceptCallback(String)` neu (→ A5) |
| `setDroppable(boolean)` | 1104 | DROPPABLE | — |
| `setEndParam(String)` | 1904 | END_PARAM → EXTERNAL_EVENT_SOURCE_END_PARAM | — |
| `setEntryMaxStack(int)` | 2388 | ENTRY_MAX_STACK | `getEntryMaxStack()` :3979 |
| `setEntryMinHeight(int)` | 2368 | ENTRY_MIN_HEIGHT | — |
| `setEntryOrderStrict(boolean)` | 2571 | ENTRY_ORDER_STRICT | — |
| `setEntryShortHeight(int)` | 2378 | ENTRY_SHORT_HEIGHT | — |
| `setEntryTimeFormat(String)` | 2616 | ENTRY_TIME_FORMAT | — |
| `setEventConstraint(String)` | 3138 | EVENT_CONSTRAINT | — |
| `setEventConstraintToBusinessHours()` | 3164 | EVENT_CONSTRAINT | — |
| `setEventDragMinDistance(int)` | 3376 | EVENT_DRAG_MIN_DISTANCE | — |
| `setEventHint(String)` | 3067 | EVENT_HINT | — |
| `setEventInteractive(boolean)` | 2963 | EVENT_INTERACTIVE | — |
| `setEventLongPressDelay(int)` | 2931 | EVENT_LONG_PRESS_DELAY | — |
| `setEventOverlap(boolean)` | 1093 | EVENT_OVERLAP | — |
| `setExpandRows(boolean)` | 2318 | EXPAND_ROWS | `isExpandRows()` :3898 |
| `setForceEventDuration(boolean)` | 3399 | FORCE_EVENT_DURATION | — |
| `setGoogleCalendarApiKey(String)` | 1926 | GOOGLE_CALENDAR_API_KEY → EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY | — |
| `setInitialDate(LocalDate)` | 2839 | INITIAL_DATE (Option wird entfernt) | — |
| `setInitialView(CalendarView)` | 2852 | INITIAL_VIEW (Option wird entfernt) | — |
| `setLazyFetching(boolean)` | 3388 | LAZY_FETCHING | — |
| `setListDayFormat(String)` | 2636 | LIST_DAY_FORMAT | — |
| `setListDaySideFormat(String)` | 2646 | LIST_DAY_SIDE_FORMAT | — |
| `setLongPressDelay(int)` | 2920 | LONG_PRESS_DELAY | — |
| `setMonthStartFormat(String)` | 2728 | MONTH_START_FORMAT | — |
| `setMoreLinkHint(String)` | 3036 | MORE_LINK_HINT | — |
| `setMultiMonthMaxColumns(int)` | 2698 | MULTI_MONTH_MAX_COLUMNS | `getMultiMonthMaxColumns()` :3970 |
| `setMultiMonthMinWidth(int)` | 2708 | MULTI_MONTH_MIN_WIDTH | — |
| `setMultiMonthTitleFormat(String)` | 2718 | MULTI_MONTH_TITLE_FORMAT | — |
| `setNavLinkHint(String)` | 2993 | NAV_LINK_HINT | — |
| `setNavLinks(boolean)` | 864 | NAV_LINKS | `isNavLinks()` :3934 |
| `setNow(LocalDate)` | 2804 | NOW (Option wird entfernt) | — |
| `setNow(LocalDateTime)` | 2815 | NOW (Option wird entfernt) | — |
| `setNowIndicator(boolean)` | 842 | NOW_INDICATOR | `isNowIndicator()` :3916 |
| `setNowIndicatorSnap(boolean)` | 2826 | NOW_INDICATOR_SNAP | — |
| `setProgressiveEventRendering(boolean)` | 2782 | PROGRESSIVE_EVENT_RENDERING | — |
| `setRerenderDelay(int)` | 2793 | RERENDER_DELAY | — |
| `setScrollTimeReset(boolean)` | 2466 | SCROLL_TIME_RESET | — |
| `setSelectConstraint(String)` | 2518 | SELECT_CONSTRAINT | — |
| `setSelectConstraintToBusinessHours()` | 2528 | SELECT_CONSTRAINT | — |
| `setSelectLongPressDelay(int)` | 2942 | SELECT_LONG_PRESS_DELAY | — |
| `setSelectMinDistance(int)` | 2497 | SELECT_MIN_DISTANCE | — |
| `setSelectMirror(boolean)` | 2487 | SELECT_MIRROR | — |
| `setSelectOverlap(boolean)` | 2507 | SELECT_OVERLAP | — |
| `setSelectable(boolean)` | 771 | SELECTABLE | `isSelectable()` :3925 |
| `setShowNonCurrentDates(boolean)` | 2398 | SHOW_NON_CURRENT_DATES | — |
| `setSlotEntryOverlap(boolean)` | 2358 | SLOT_ENTRY_OVERLAP | `isSlotEntryOverlap()` :3952 |
| `setStartParam(String)` | 1893 | START_PARAM → EXTERNAL_EVENT_SOURCE_START_PARAM | — |
| `setStickyFooterScrollbar(boolean)` | 2338 | STICKY_FOOTER_SCROLLBAR | — |
| `setStickyHeaderDates(boolean)` | 2328 | STICKY_HEADER_DATES | `isStickyHeaderDates()` :3907 |
| `setThemeSystem(ThemeSystem)` | 2862 | THEME_SYSTEM | — (removed in Fix 11) |
| `setTimeHint(String)` | 3056 | TIME_HINT | — |
| `setTimeZoneParam(String)` | 1915 | TIME_ZONE_PARAM → EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM | — |
| `setUnselectAuto(boolean)` | 2538 | UNSELECT_AUTO | — |
| `setUnselectCancel(String)` | 2548 | UNSELECT_CANCEL | — |
| `setViewHint(String)` | 2978 | VIEW_HINT → NATIVE_TOOLBAR_VIEW_HINT | — |
| `setWeekNumberFormat(String)` | 2668 | WEEK_NUMBER_FORMAT | — |
| `setWeekText(String)` | 2678 | WEEK_TEXT | — |
| `setWeekTextLong(String)` | 2688 | WEEK_TEXT_LONG | — |

**Getters to remove** (simple `getOption()` wrappers):

| Method | Line | Option |
|---|---|---|
| `getAspectRatio()` | 3997 | ASPECT_RATIO |
| `getContentHeight()` | 3988 | CONTENT_HEIGHT |
| `getEntryMaxStack()` | 3979 | ENTRY_MAX_STACK |
| `getSnapDuration()` | 3871 | SNAP_DURATION |
| `isAllDaySlot()` | 3889 | ALL_DAY_SLOT |
| `isExpandRows()` | 3898 | EXPAND_ROWS |
| `isStickyHeaderDates()` | 3907 | STICKY_HEADER_DATES |
| `isNowIndicator()` | 3916 | NOW_INDICATOR |
| `isSelectable()` | 3925 | SELECTABLE |
| `isNavLinks()` | 3934 | NAV_LINKS |
| `isDayHeaders()` | 3943 | DAY_HEADERS |
| `isSlotEntryOverlap()` | 3952 | SLOT_ENTRY_OVERLAP |
| `getDayMinWidth()` | 3961 | DAY_MIN_WIDTH |
| `getMultiMonthMaxColumns()` | 3970 | MULTI_MONTH_MAX_COLUMNS |
| `getSlotMinTime()` | 3853 | SLOT_MIN_TIME |
| `getSlotMaxTime()` | 3862 | SLOT_MAX_TIME |
| `getFirstDay()` | 3880 | FIRST_DAY |

**Total: 81 setters + 17 getters = 98 methods to remove**

---

## A2. Option enum constants — Relevanz-Analyse

Für jede NEUE Option-Konstante: brauchen wir die im Vaadin-Addon, oder kann sie raus?

### KEEP — Sinnvoll im Vaadin-Kontext

| Option | FC-Docs | Begründung |
|---|---|---|
| `ALL_DAY_MAINTAIN_DURATION` | allDayMaintainDuration | Entry-Verhalten beim Ziehen zwischen all-day/timed |
| `BUTTON_HINTS` | buttonHints | Accessibility (aria-label für Toolbar-Buttons) |
| `BUTTON_TEXT` | buttonText | Lokalisierung der Toolbar-Button-Labels |
| `CLOSE_HINT` | closeHint | Accessibility (Popover-Schließen-Label) |
| `CONTENT_SECURITY_POLICY` | contentSecurityPolicy | CSP-Nonce für `<style>`-Tags — relevant für sichere Deployments |
| `DATE_ALIGNMENT` | dateAlignment | Navigation-Snapping für Custom Views |
| `DATE_INCREMENT` | dateIncrement | Navigation-Schrittweite für Custom Views |
| `DAY_MAX_EVENT_ROWS` | dayMaxEventRows | "+N more"-Link-Steuerung — häufig genutzt |
| `DAY_POPOVER_FORMAT` | dayPopoverFormat | Format des "+N more"-Popover-Titels |
| `DEFAULT_ALL_DAY` | defaultAllDay | Default-Wert für Entries ohne explizite Zeitangabe |
| `DEFAULT_RANGE_SEPARATOR` | defaultRangeSeparator | Trennzeichen zwischen Start-/End-Datum im Titel |
| `DISPLAY_EVENT_END` | displayEventEnd | Ob End-Zeit angezeigt wird |
| `DRAG_REVERT_DURATION` | dragRevertDuration | Animation beim Abbrechen eines Drags |
| `DRAG_SCROLL_ELS` | dragScrollEls | Auto-Scroll-Container beim Draggen |
| `DROPPABLE` | droppable | Ob externe Elemente in den Kalender gedroppt werden können |
| `DROP_ACCEPT` | dropAccept | CSS-Selektor/Funktion für akzeptierte Drop-Elemente |
| `EVENT_ALLOW` | eventAllow | JS-Callback: Erlaubt/verbietet Drag pro Ziel |
| `EVENT_CONSTRAINT` | eventConstraint | Drag/Resize auf Zeitfenster einschränken |
| `EVENT_DRAG_MIN_DISTANCE` | eventDragMinDistance | Minimale Pixel bevor Drag startet |
| `EVENT_HINT` | eventHint | Accessibility (Entry-Label für Screen-Reader) |
| `EVENT_INTERACTIVE` | eventInteractive | Keyboard-Fokussierbarkeit (WCAG 2.1 AA) |
| `EVENT_LONG_PRESS_DELAY` | eventLongPressDelay | Touch-Delay für Entry-Drag |
| `EVENT_OVERLAP` | eventOverlap | Ob Entries sich überlappen dürfen |
| `FORCE_EVENT_DURATION` | forceEventDuration | Default-Dauer erzwingen für Entries ohne End-Zeit |
| `INITIAL_DATE` | initialDate | Startdatum beim Laden |
| `INITIAL_VIEW` | initialView | Start-View beim Laden |
| `LAZY_FETCHING` | lazyFetching | Ob bei View-Wechsel neu gefetcht wird |
| `LONG_PRESS_DELAY` | longPressDelay | Globaler Touch-Delay |
| `MORE_LINK_HINT` | moreLinkHint | Accessibility ("+N more"-Label) |
| `NAV_LINK_HINT` | navLinkHint | Accessibility (Day-/Week-Number-Link-Label) |
| `NOW_INDICATOR_SNAP` | nowIndicatorSnap | Ob Now-Indicator an Slot-Grenzen snappt |
| `PROGRESSIVE_EVENT_RENDERING` | progressiveEventRendering | Rendering-Strategie: sofort vs gesammelt |
| `RERENDER_DELAY` | rerenderDelay | Verzögerung vor Re-Render (Performance) |
| `SELECT_ALLOW` | selectAllow | JS-Callback: Erlaubt/verbietet Selection |
| `SELECT_LONG_PRESS_DELAY` | selectLongPressDelay | Touch-Delay für Selection |
| `TIME_HINT` | timeHint | Accessibility (Zeit-Label) |
| `TITLE_RANGE_SEPARATOR` | titleRangeSeparator | Trennzeichen im Titel bei Datums-Bereichen |
| `VIEW_HINT` | viewHint | Accessibility (View-Switcher-Label) |

### RENAME → `NATIVE_TOOLBAR_` Prefix — Beziehen sich auf die eingebaute FC-Toolbar

Diese Optionen konfigurieren die native FC Header/Footer-Toolbar. Prefix macht klar, dass sie sich nicht auf eine benutzerdefinierte Vaadin-Toolbar beziehen. Brauchen den `Option(String)` Konstruktor, da der FC-Optionkey unverändert bleibt.

| Alter Name | Neuer Name | FC-Key (bleibt) |
|---|---|---|
| `BUTTON_HINTS` | `NATIVE_TOOLBAR_BUTTON_HINTS` | `"buttonHints"` |
| `BUTTON_TEXT` | `NATIVE_TOOLBAR_BUTTON_TEXT` | `"buttonText"` |
| `TITLE_RANGE_SEPARATOR` | `NATIVE_TOOLBAR_TITLE_RANGE_SEPARATOR` | `"titleRangeSeparator"` |
| `DEFAULT_RANGE_SEPARATOR` | `NATIVE_TOOLBAR_DEFAULT_RANGE_SEPARATOR` | `"defaultRangeSeparator"` |
| `VIEW_HINT` | `NATIVE_TOOLBAR_VIEW_HINT` | `"viewHint"` |

Beispiel:
```java
NATIVE_TOOLBAR_BUTTON_HINTS("buttonHints"),
NATIVE_TOOLBAR_BUTTON_TEXT("buttonText"),
```

**Hinweis:** Die bestehenden Master-Options `HEADER_TOOLBAR` und `FOOTER_TOOLBAR` bleiben unverändert (Breaking Change vermeiden).

### RENAME → `EXTERNAL_EVENT_SOURCE_` Prefix — Nur für Client-Side Event Sources

Diese sind FC-Globals für JSON-Feed/Google/iCal-Sources. Per-Source-Override existiert bereits auf den Source-Klassen. Brauchen ebenfalls `Option(String)` Konstruktor.

| Alter Name | Neuer Name | FC-Key (bleibt) |
|---|---|---|
| `START_PARAM` | `EXTERNAL_EVENT_SOURCE_START_PARAM` | `"startParam"` |
| `END_PARAM` | `EXTERNAL_EVENT_SOURCE_END_PARAM` | `"endParam"` |
| `TIME_ZONE_PARAM` | `EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM` | `"timeZoneParam"` |
| `GOOGLE_CALENDAR_API_KEY` | `EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY` | `"googleCalendarApiKey"` |

### REMOVE — Nicht relevant im Vaadin-Kontext

| Option | FC-Docs | Begründung für Entfernung |
|---|---|---|
| `THEME_SYSTEM` | themeSystem | Vaadin nutzt Lumo, Bootstrap-Theming irrelevant. Wird komplett in Fix 11 entfernt. |
| `NOW` | now | Überschreibt die aktuelle Uhrzeit des Kalenders (Debugging/Testing-Zweck in FC). In Produktion unsinnig — wer braucht eine gefälschte "jetzt"-Zeit? Nutzer können `setOption("now", ...)` wenn sie es wirklich brauchen. |
| `INITIAL_DATE` | initialDate | Bereits über `FullCalendarBuilder` und Konstruktor (`initialOptions`) abgedeckt. Zur Laufzeit irrelevant (wirkt nur beim initialen Rendern). |
| `INITIAL_VIEW` | initialView | Dito — Builder/Konstruktor. Zur Laufzeit View-Wechsel über `changeView()`. |

## A3. Undeprecate — Bestehende Methoden, die zugunsten entfernter Setter deprecated wurden

Diese Methoden existieren auf `master` und wurden in diesem Branch `@Deprecated` markiert mit Verweis auf neue Setter, die jetzt entfernt werden. Sie müssen wieder **undeprecated** werden und direkt `setOption()` aufrufen.

| Methode | Line | War deprecated zugunsten von | Aktion |
|---|---|---|---|
| `setTimeslotsSelectable(boolean)` | 761 | `setSelectable(boolean)` | Undeprecate, Body → `setOption(Option.SELECTABLE, selectable)` |
| `setNowIndicatorShown(boolean)` | 832 | `setNowIndicator(boolean)` | Undeprecate, Body → `setOption(Option.NOW_INDICATOR, shown)` |
| `setNumberClickable(boolean)` | 853 | `setNavLinks(boolean)` | Undeprecate, Body → `setOption(Option.NAV_LINKS, clickable)` |
| `setColumnHeader(boolean)` | 1396 | `setDayHeaders(boolean)` | Undeprecate, Body → `setOption(Option.DAY_HEADERS, columnHeader)` |
| `setDragScrollActive(boolean)` | 2032 | `setDragScroll(boolean)` | Undeprecate, Body → `setOption(Option.DRAG_SCROLL, dragScrollActive)` |
| `lookupViewName(String)` | 2090 | `lookupViewByClientSideValue(String)` | Handled in Fix 6 |

---

## A5. Neue Methode: setDropAcceptCallback

`setDropAccept(String)` war ein einfacher `setOption`-Wrapper — funktioniert nur für CSS-Selektoren, nicht für JS-Funktionen. Analog zu `setSelectAllowCallback` eine neue Callback-Methode erstellen.

```java
/**
 * Sets a JavaScript function as the {@code dropAccept} callback. The function receives
 * a draggable DOM element and must return {@code true} if the calendar should accept it.
 * <br><br>
 * This must be a client-side only callback because it runs synchronously during drag feedback.
 * <br><br>
 * <b>Note:</b> No security mechanism is applied to the string. Validate it before passing to the client.
 * <br><br>
 * For simple CSS selector matching, use {@code setOption(Option.DROP_ACCEPT, ".my-class")} instead.
 * <br><br>
 * Example:
 * <pre>
 * calendar.setDropAcceptCallback(
 *     "function(draggable) { return draggable.dataset.type === 'task'; }");
 * </pre>
 *
 * @param jsFunction JavaScript function string
 * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
 */
public void setDropAcceptCallback(String jsFunction) {
    getElement().callJsFunction("setDropAcceptCallback", jsFunction);
}
```

Benötigt entsprechende TS-Methode in `full-calendar.ts`:
```typescript
setDropAcceptCallback(s: string) {
    this.setOption('dropAccept', new Function("return " + s)());
}
```

Analog: `setEntryOrderCallback(String)` für die Function-Variante von `eventOrder`. `setEntryOrder(String...)` bleibt erhalten (Varargs-Logik), aber FC akzeptiert bei `eventOrder` auch eine JS-Funktion für Custom-Sortierung.

```java
/**
 * Sets a JavaScript function as the {@code eventOrder} callback for custom entry sorting.
 * The function receives two event objects and must return a negative, zero, or positive number
 * (like a comparator).
 * <br><br>
 * For simple field-based ordering, use {@code setEntryOrder("start", "-duration")} instead.
 * <br><br>
 * <b>Note:</b> No security mechanism is applied to the string.
 *
 * @param jsFunction JavaScript comparator function string
 * @see <a href="https://fullcalendar.io/docs/eventOrder">eventOrder</a>
 */
public void setEntryOrderCallback(String jsFunction) {
    getElement().callJsFunction("setEntryOrderCallback", jsFunction);
}
```

Benötigt TS-Methode:
```typescript
setEntryOrderCallback(s: string) {
    this.setOption('eventOrder', new Function("return " + s)());
}
```

---

### Zusammenfassung: Options, die sowohl Wert als auch Funktion akzeptieren

| Option | Einfacher Wert (via `setOption`) | Funktion (via Callback-Methode) |
|---|---|---|
| `DROP_ACCEPT` | CSS-Selektor | `setDropAcceptCallback(String)` — **NEU** |
| `ENTRY_ORDER` | Feld-Liste | `setEntryOrderCallback(String)` — **NEU** |
| `EVENT_OVERLAP` | boolean | `setEventOverlapCallback(String)` — existiert |
| `SELECT_OVERLAP` | boolean | `setSelectOverlapCallback(String)` — existiert |
| `VALID_RANGE` | JSON-Objekt | `setValidRangeCallback(String)` — existiert |
| `EVENT_ALLOW` | — (nur Funktion) | `setEventAllowCallback(String)` — existiert |
| `SELECT_ALLOW` | — (nur Funktion) | `setSelectAllowCallback(String)` — existiert |

Alle anderen neuen Options akzeptieren **keine** Funktionen → kein Callback nötig.

---

Option `DROP_ACCEPT` Javadoc erweitern:
```java
/**
 * Controls which external draggable elements the calendar accepts.
 * <p>
 * Accepts a CSS selector string (e.g., {@code ".my-draggable"}) to filter draggable elements.
 * For dynamic per-element control use {@link FullCalendar#setDropAcceptCallback(String)} instead.
 * <p>
 * Default: {@code "*"} (accept all).
 *
 * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
 */
DROP_ACCEPT,
```

---

## A4. Option-Javadocs erweitern

Beim Entfernen der Setter geht deren Javadoc verloren. Diese Dokumentation muss in die **Option-Enum-Konstanten** übertragen werden, damit Nutzer nicht für jede Option die FC-Website aufrufen müssen.

**Format pro Option-Konstante:**
```java
/**
 * Kurzbeschreibung was die Option tut.
 * <p>
 * Erlaubte Werte: {@code true}/{@code false}, oder z.B. {@code "00:30:00"} (HH:MM:SS).
 * Default: {@code false}.
 *
 * @see <a href="https://fullcalendar.io/docs/optionName">optionName</a>
 */
OPTION_NAME,
```

Die Javadocs der entfernten Setter (Parameter-Beschreibung, erlaubte Werte, Beispiele, `@see`-Links) werden 1:1 in die Option-Konstante übertragen.

---

## B. KEEP — Methods with real logic (12 setters)

These do more than just delegate to `setOption()` — they have validation, conversion, or multi-step logic.

| Method | Line | Reason to keep |
|---|---|---|
| `setDragScrollEls(String...)` | 3339 | Varargs → joins with comma, null/empty handling |
| `setEntryOrder(String...)` | 2560 | Varargs → joins to list/string |
| `setEventConstraint(BusinessHours)` | 3151 | Converts `BusinessHours` object to JSON |
| `setNextDayThreshold(String)` | 2583 | Validation + formatting logic |
| `setNextDayThreshold(LocalTime)` | 2595 | `LocalTime` → string conversion |
| `setScrollTime(String)` | 2443 | Validation/formatting |
| `setScrollTime(LocalTime)` | 2455 | `LocalTime` → string conversion |
| `setSelectOverlapCallback(String)` | 3121 | Null-safe conditional: `setOption` vs `callJsFunction` |
| `setSlotDuration(String)` | 2409 | Validation/formatting |
| `setSlotLabelInterval(String)` | 2421 | Validation/formatting |
| `setValidRangeCallback(String)` | 3095 | Null-safe conditional: `setOption` vs `callJsFunction` |
| `setWeekNumberCalculation(WeekNumberCalculation)` | 2657 | Enum → client-side value conversion |

---

## C. KEEP — JS callback setters (11 methods)

These call `getElement().callJsFunction()` directly — they are not `setOption` wrappers.

| Method | Line | Purpose |
|---|---|---|
| `setSelectAllowCallback(String)` | 1035 | Sync drag-select feedback |
| `setEventAllowCallback(String)` | 1057 | Sync drag-allow feedback |
| `setEventOverlapCallback(String)` | 1079 | Sync overlap feedback |
| `setLoadingCallback(String)` | 1940 | Loading state indicator |
| `setEventDataTransformCallback(String)` | 1954 | Global event transform |
| `setEventSourceSuccessCallback(String)` | 1968 | JSON response transform |
| `setFixedMirrorParent(String)` | 3318 | Drag mirror parent |
| `setNavLinkDayClickCallback(String)` | 3008 | Day-number click JS handler |
| `setNavLinkWeekClickCallback(String)` | 3023 | Week-number click JS handler |
| `addEventSource(...)` | 1809 | Handled in Fix 3 |
| `removeEventSource(...)` | 1822 | Handled in Fix 3 |

---

## D. KEEP — Render hook callbacks (40 methods)

All `set*ClassNamesCallback`, `set*ContentCallback`, `set*DidMountCallback`, `set*WillUnmountCallback` methods. These call `callJsFunction` and are the only way to set render hooks. 10 hook groups × 4 methods each:

- `setEntryClassNamesCallback` / `Content` / `DidMount` / `WillUnmount` (lines 891-1009)
- `setDayCellClassNamesCallback` / ... (lines 3429-3463)
- `setDayHeaderClassNamesCallback` / ... (lines 3475-3508)
- `setSlotLabelClassNamesCallback` / ... (lines 3519-3550)
- `setSlotLaneClassNamesCallback` / ... (lines 3562-3593)
- `setViewClassNamesCallback` / ... (lines 3604-3626)
- `setNowIndicatorClassNamesCallback` / ... (lines 3638-3669)
- `setWeekNumberClassNamesCallback` / ... (lines 3680-3712)
- `setMoreLinkClassNamesCallback` / ... (lines 3723-3754)
- `setNoEventsClassNamesCallback` / ... (lines 3766-3797)
- `setAllDayClassNamesCallback` / ... (lines 3808-3840)

---

## E. KEEP — Other non-trivial methods

| Method | Line | Reason |
|---|---|---|
| `getCurrentIntervalStart/End()` | 542/555 | Property-based getter, not Option |
| `getCurrentViewName()` / `getCurrentView()` | 507/515 | Property-based getter |
| `setHiddenDays(DayOfWeek...)` | 2273 | DayOfWeek → int[] conversion with Sunday special case |
| `setDisplayEventTime(boolean)` | 2772 | Alias that delegates to `setDisplayEntryTime` |
| `setDefaultTimedEventDuration(String)` | 2738 | Has formatting/conversion logic |
| `setDefaultAllDayEventDuration(String)` | 2748 | Has formatting/conversion logic |
| `clearSelection()` | 2173 | `executeJs` call, not setOption |
| `setViewSpecificOption(...)` (4 overloads) | 3234-3284 | Complex map management + JSON |
| `setContentSecurityPolicyNonce(String)` | 3210 | Builds `Map.of("nonce", ...)` — actually keep for usability |
| All listener methods | various | `addListener()` pattern |
| Event source methods | various | Handled in Fix 3/4/5 |

---

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java` — remove 98 methods
- `addon/src/test/java/org/vaadin/stefan/fullcalendar/*.java` — update test calls to use `setOption()`/`getOption()`
- `e2e-test-app/src/main/java/org/vaadin/stefan/ui/view/testviews/*.java` — update test view calls
- `docs/Samples.md`, `docs/Features.md`, `docs/Release-notes.md` — update references

## Verification

1. `mvn test -pl addon` — all tests pass
2. `mvn clean install -DskipTests` — full build compiles
3. Grep for removed method names — zero hits in addon code

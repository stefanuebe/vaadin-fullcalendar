# Phase 7: Advanced and Niche Options

## Goal

Cover remaining FC options that are rarely used in typical applications but are part of the complete API surface. These are lower priority but represent the long tail of missing coverage.

---

## Features Covered

### 7.1 `customButtons` typed API with server-side click handling

FC's `customButtons` lets you add arbitrary buttons to the header/footer toolbar with click handlers. Currently these must be set via raw `setOption` with a JS-only click function — there is no way to register a server-side click listener.

**All FC-supported fields for a custom button:**
```java
public class CustomButton {
    private final String name;          // unique identifier; used in headerToolbar/footerToolbar config
    private String text;                // button label text (displayed on the button)
    private String hint;                // aria-label / tooltip text (accessibility)
    private String icon;                // CSS class name of an icon (e.g., from Font Awesome)
    private String bootstrapFontAwesome;// Bootstrap/Font Awesome icon name (alternative to icon)
    private String themeIcon;           // Vaadin Lumo icon name (e.g. "chevron-right")
    // click handler — see server-side routing below
}
```

**FC serialization shape** (what gets sent to the client in the `customButtons` option):
```json
{
  "myButton": {
    "text": "Schedule",
    "hint": "Open scheduling wizard",
    "icon": "fa-calendar-plus",
    "click": "function() { this.$server.customButtonClicked('myButton'); }"
  }
}
```
The `click` function is generated automatically by the addon — the developer does not write it.

**Server-side routing: how it works**

The key design insight is that all custom button clicks can be routed through a **single** `@ClientCallable` method that dispatches by button name. The TS companion generates a click function for each button that calls `this.$server.customButtonClicked(name)`.

```typescript
// Auto-generated click function for each button:
click: function() { this.$server.customButtonClicked('myButton'); }
```

On the Java side, a map of `String → ComponentEventListener<CustomButtonClickedEvent>` dispatches to the right listener:
```java
private final Map<String, List<ComponentEventListener<CustomButtonClickedEvent>>> customButtonListeners = new HashMap<>();

@ClientCallable
private void customButtonClicked(String buttonName) {
    List<ComponentEventListener<CustomButtonClickedEvent>> listeners = customButtonListeners.get(buttonName);
    if (listeners != null) {
        CustomButtonClickedEvent event = new CustomButtonClickedEvent(this, true, buttonName);
        listeners.forEach(l -> l.onComponentEvent(event));
    }
}
```

**Complete listener API:**
```java
// Add a button and register a click listener simultaneously:
public Registration addCustomButton(CustomButton button,
                                    ComponentEventListener<CustomButtonClickedEvent> listener) {
    customButtons.put(button.getName(), button);
    customButtonListeners.computeIfAbsent(button.getName(), k -> new ArrayList<>()).add(listener);
    syncCustomButtonsToClient();
    return () -> { /* remove listener */ };
}

// Or add the button to the calendar and register the listener separately:
public void addCustomButton(CustomButton button) { ... }
public Registration addCustomButtonClickedListener(String buttonName,
                                                   ComponentEventListener<CustomButtonClickedEvent> listener) { ... }

// Builder integration:
FullCalendarBuilder.withCustomButton(CustomButton button)
// Note: server-side listeners cannot be registered via the builder (no calendar instance yet)
```

**`CustomButtonClickedEvent`:**
```java
public class CustomButtonClickedEvent extends ComponentEvent<FullCalendar> {
    private final String buttonName;  // the name of the button that was clicked
}
```

**Frontend TypeScript impact:** Required — the TS companion must:
1. Receive the `customButtons` option as a map
2. For each button, add a `click` function that calls `this.$server.customButtonClicked(name)`
3. Add the `@ClientCallable` method `customButtonClicked(String name)` to the server interface

---

### 7.2 `buttonIcons` typed API

Allows customizing the prev/next/prevYear/nextYear button icons.

```java
public void setButtonIcons(Map<String, String> icons) {
    setOption("buttonIcons", /* serialize to JSON object */);
}
// e.g.: Map.of("prev", "chevron-left", "next", "chevron-right", "prevYear", "chevrons-left")
```

Or a typed `ButtonIcons` builder class:
```java
public class ButtonIcons {
    private String prev;
    private String next;
    private String prevYear;
    private String nextYear;
    // fluent setters + toJson()
}
public void setButtonIcons(ButtonIcons icons) { ... }
```

**JSON mapping:** Serializes to `{"prev": "chevron-left", "next": "chevron-right", ...}` — field names match FC's expected keys exactly.

---

### 7.3 `themeSystem` typed setter

```java
public enum ThemeSystem {
    STANDARD,    // FC's default built-in theme
    BOOTSTRAP5   // Bootstrap 5 integration
}

public void setThemeSystem(ThemeSystem themeSystem) {
    setOption("themeSystem", themeSystem.name().toLowerCase()); // "standard" or "bootstrap5"
}
```

**Important context for Vaadin developers:** In practice, `bootstrap5` is almost never used in Vaadin applications. Vaadin has its own complete design system (Lumo), and mixing Bootstrap CSS with Lumo creates style conflicts. The `STANDARD` theme is correct for virtually all Vaadin use cases.

The `bootstrap5` option requires:
1. The `@fullcalendar/bootstrap5` npm package installed and registered as a plugin in the TS companion
2. Bootstrap CSS loaded separately in the application (which conflicts with Vaadin's Lumo theme)

**Recommendation:** Document that `STANDARD` is the correct value for Vaadin apps. Only `bootstrap5` requires frontend changes — do not add the npm package or TS changes unless `BOOTSTRAP5` is explicitly requested.

**Cross-reference:** Also listed in Phase 2.1 as a missing typed setter. Implement once; the enum and setter live in `FullCalendar.java` and a separate `ThemeSystem.java` enum file.

**Frontend TypeScript impact:** Only needed for `BOOTSTRAP5` — add `@fullcalendar/bootstrap5` npm package and register in plugins array.

---

### 7.4 `validRange` as a function (dynamic)

Currently `setValidRange(LocalDate start, LocalDate end)` is implemented. FC also supports `validRange` as a JavaScript function that receives the current "now" date and dynamically returns a range.

```java
public void setValidRangeCallback(String jsFunction) {
    setOption("validRange", jsFunction);
}
// e.g.: "function(nowDate) { return { start: new Date(nowDate.getFullYear(), nowDate.getMonth(), 1), end: new Date(nowDate.getFullYear(), nowDate.getMonth() + 1, 1) }; }"
```

**Use case:** Always restrict the calendar to the current month. The function is called whenever FC recalculates (on view change, on "today" click). This cannot be done with a static date range since "today's month" changes over time.

---

### 7.5 `selectOverlap` as a function

Currently `Option.SELECT_OVERLAP` only supports a boolean. FC also accepts a JS function `function(stillEvent)` that returns true/false per event encountered during selection.

```java
public void setSelectOverlapCallback(String jsFunction) {
    setOption("selectOverlap", jsFunction);
}
// e.g.: "function(stillEvent) { return stillEvent.display !== 'background'; }"
```

**Use case:** Allow selection only over background events (to enforce "availability blocks"), but prevent selecting over regular events. The boolean `true` would allow everything; the boolean `false` would prevent selection when any event is present. The function gives per-event control.

---

### 7.6 `eventConstraint` as a BusinessHours object on `FullCalendar`

Currently `Option.SELECT_CONSTRAINT` is set as a string. The typed `setEventConstraint(BusinessHours)` overload would be useful. See also Phase 0.4 for the per-entry constraint overloads.

```java
// String groupId overload:
public void setEventConstraint(String groupId) {
    setOption("eventConstraint", groupId);
}

// BusinessHours overload:
public void setEventConstraint(BusinessHours hours) {
    setOption("eventConstraint", hours.toJson());
}

// Shortcut for calendar-wide business hours constraint:
public void setEventConstraintToBusinessHours() {
    setOption("eventConstraint", "businessHours");
}
```

---

### 7.7 `dateIncrement` and `dateAlignment` typed setters for custom views

For custom views, `dateIncrement` (how much time to advance on next/prev) and `dateAlignment` (which date boundary to align to) are important:

```java
public void setDateIncrement(String duration) {
    setOption("dateIncrement", duration);
}
// e.g.: "P1W" for weekly, "P2W" for bi-weekly, "P3D" for 3-day view

public void setDateAlignment(String alignment) {
    setOption("dateAlignment", alignment);
}
// e.g.: "week" (align to week start), "month" (align to month start), "day"
```

These apply globally but are primarily used with custom views. For built-in views, FC handles increment/alignment automatically.

---

### 7.8 `duration` and `visibleRange` for custom views typed setters

`CustomCalendarView` is an interface (`addon/src/main/java/org/vaadin/stefan/fullcalendar/CustomCalendarView.java`) with a single `getViewSettings()` method returning an `ObjectNode`. It does NOT currently have typed helper methods for `duration`, `visibleRange`, or `type` — these must be set manually in the returned `ObjectNode`.

**Current state:** Developers implementing `CustomCalendarView` must manually build the JSON settings object:
```java
@Override
public ObjectNode getViewSettings() {
    ObjectNode settings = JsonFactory.createObject();
    settings.put("type", "timeGrid");
    settings.put("duration", "{days: 3}");
    return settings;
}
```

**What to add** (if `CustomCalendarView` gets a companion builder or abstract base class):
```java
// In a helper class or builder for CustomCalendarView:
CustomCalendarView.withDuration(String duration)     // e.g. "P3D"
CustomCalendarView.withVisibleRange(LocalDate start, LocalDate end)  // static range
CustomCalendarView.withVisibleRangeCallback(String jsFunction)       // dynamic range
CustomCalendarView.withType(String baseViewType)     // "timeGrid", "dayGrid", "list"
CustomCalendarView.withDayCount(int count)           // shorthand for duration in days
```

**Check before implementing:** The current `CustomCalendarView.AnonymousCustomCalendarView` inner class is used for views from `withInitialOptions`. The main `CustomCalendarView` interface is intentionally minimal (just `getViewSettings()`) so that developers can implement it however they want. Adding typed helpers should be done as a separate builder/factory or an optional abstract base class, not by extending the interface.

---

### 7.9 `getDate()` API method — current date tracking

FC's `Calendar.getDate()` returns the current "anchor" date of the calendar (the date that determines what the calendar is showing). In the addon, the closest equivalent is the `intervalStart` exposed via `DatesRenderedEvent`.

**Current state of server-side tracking:**
The addon fires `DatesRenderedEvent` (a Vaadin component event extending `ViewRenderEvent`) after every view render. `ViewRenderEvent` exposes:
- `getIntervalStart()` — the start of the current interval (e.g., first day of the current month in month view)
- `getIntervalEnd()` — the end of the current interval
- `getStart()` — the first visible date (may precede `intervalStart` — e.g., in month view, grayed-out days from the previous month)
- `getEnd()` — the last visible date
- `getCalendarView()` — the current view type

**What's missing:** A direct `FullCalendar.getCurrentIntervalStart()` getter that does not require waiting for a render event. Currently there is no such method — the only way to get the current date is by listening to `DatesRenderedEvent` and storing the value.

**Proposed solution:**
```java
// FullCalendar.java — track the last-rendered interval:
private LocalDate currentIntervalStart;
private LocalDate currentIntervalEnd;

// Updated in the DatesRenderedEvent handler:
// (add to the existing datesSet event processing code)

// New public getters:
public Optional<LocalDate> getCurrentIntervalStart() {
    return Optional.ofNullable(currentIntervalStart);
}
public Optional<LocalDate> getCurrentIntervalEnd() {
    return Optional.ofNullable(currentIntervalEnd);
}
```

**Why `Optional`:** Before the first render, `currentIntervalStart` is null. Using `Optional` avoids NPE and makes the "not yet rendered" case explicit.

**Caveat:** The server-side value lags by one server round-trip — it is updated when the server processes the `DatesRenderedEvent`. If you call `getCurrentIntervalStart()` immediately after `gotoDate(date)` (before the client fires `datesSet`), you will get the old value. Document this.

---

### 7.10 `incrementDate()`, `prevYear()`, `nextYear()` navigation methods

```java
public void incrementDate(String duration) {
    getElement().callJsFunction("incrementDate", duration);
    // e.g.: "P1W" to move forward one week, "-P1D" to move back one day
}

public void previousYear() {
    getElement().callJsFunction("prevYear");
}

public void nextYear() {
    getElement().callJsFunction("nextYear");
}
```

These are trivially small one-line JS delegation methods, analogous to the existing `next()`, `previous()`, `today()` (already implemented), and `gotoDate(LocalDate)` methods.

**Frontend TypeScript impact:** None — direct `callJsFunction` calls.

---

### 7.11 `updateSize()` method

Forces the calendar to recalculate its dimensions. This is essential in specific UI scenarios.

```java
public void updateSize() {
    getElement().callJsFunction("updateSize");
}
```

**Primary use case: Vaadin Dialog with Calendar**

When a Vaadin dialog (or any initially hidden container) contains a calendar:
1. The calendar renders while the dialog is hidden (size = 0)
2. FC calculates its layout based on zero dimensions — events and slots are rendered incorrectly or not at all
3. When the dialog opens, the calendar appears broken (columns too narrow, overlapping events, missing slots)
4. Calling `calendar.updateSize()` in a dialog-open listener forces FC to recalculate using the actual dimensions

Example:
```java
Dialog dialog = new Dialog();
FullCalendar calendar = FullCalendarBuilder.create().build();
dialog.add(calendar);
dialog.addOpenedChangeListener(event -> {
    if (event.isOpened()) {
        calendar.updateSize();
    }
});
```

**Other use cases:**
- Calendar inside a `Details` component that is initially closed
- Calendar inside a tab that becomes visible for the first time
- Calendar inside a `SplitLayout` panel that is resized programmatically

**Relationship to `handleWindowResize`:** `updateSize()` is a one-time manual trigger. `handleWindowResize` (Phase 2.11) is FC's automatic resize observer. If `handleWindowResize = false`, use `updateSize()` manually when the container changes size.

---

### 7.12 `contentSecurityPolicy` / `nonce` support

FC supports a `contentSecurityPolicy` option for setting nonces on dynamically generated `<style>` tags.

**When this is needed:**
Vaadin applications with strict Content Security Policy (CSP) headers that include `style-src` without `'unsafe-inline'`. FC generates `<style>` elements dynamically at runtime (for event colors, theme styles, etc.). Without a nonce, the browser blocks these styles and the calendar may appear unstyled or broken.

The nonce approach: generate a random value per-session on the server, set it in the CSP header `style-src 'nonce-<value>'`, and tell FC the nonce so it can add `nonce="<value>"` to its generated `<style>` tags.

```java
public void setContentSecurityPolicyNonce(String nonce) {
    // FC expects: { contentSecurityPolicy: { nonce: "abc123" } }
    setOption("contentSecurityPolicy", Map.of("nonce", nonce));
}
```

**Caveat:** This must be set **before** the calendar is attached (before FC initializes and generates its first `<style>` tags). Use `FullCalendarBuilder.withInitialOptions(...)` or set it in the constructor / `onAttach` override before calling `super.onAttach()`.

**Note for Vaadin apps:** Vaadin's own CSP handling (via `VaadinServlet` configuration or Spring Security) may already manage nonces for Vaadin's internal styles. The calendar's nonce must be the same nonce as the application's CSP nonce — coordinate with the Vaadin app's security configuration.

---

### 7.13 View-specific options

FC supports per-view option overrides via a nested `views` object:

```js
{
  views: {
    dayGrid: { dayMaxEvents: 3 },
    timeGrid: { slotDuration: '00:30:00', slotLabelInterval: '01:00' },
    timeGridWeek: { nowIndicator: true }  // specific view variant
  }
}
```

View types in FC: `dayGrid`, `timeGrid`, `list`, `multiMonth` (and their variants: `dayGridMonth`, `timeGridWeek`, `timeGridDay`, etc.). In the addon, the corresponding `CalendarViewImpl` enum values map to these FC view names.

Currently these must be passed via `withInitialOptions(ObjectNode)` which requires manual JSON construction.

**Complete example of the JSON structure:**
```json
{
  "views": {
    "dayGrid": {
      "dayMaxEventRows": 3
    },
    "timeGrid": {
      "slotDuration": "00:30:00",
      "slotLabelInterval": "01:00:00"
    },
    "listWeek": {
      "noEventsText": "No events this week"
    }
  }
}
```

**Proposed Java API:**
```java
// Set a single option for a specific view type:
public void setViewSpecificOption(String viewType, Option option, Object value) {
    setViewSpecificOption(viewType, option.getOptionKey(), value);
}
public void setViewSpecificOption(String viewType, String optionKey, Object value) {
    // Build/update the "views" object in the options map
}

// Set multiple options at once:
public void setViewSpecificOptions(String viewType, Map<String, Object> options) { ... }

// Typed overload with CalendarView enum:
public void setViewSpecificOption(CalendarView view, Option option, Object value) {
    setViewSpecificOption(view.getClientSideValue(), option, value);
}
```

**Internal implementation:** The `views` object is a nested JSON structure. `setViewSpecificOption` must read-modify-write the current `views` option value:
```java
ObjectNode viewsNode = (ObjectNode) getOption("views").orElse(JsonFactory.createObject());
ObjectNode viewNode = viewsNode.has(viewType) ? (ObjectNode) viewsNode.get(viewType) : JsonFactory.createObject();
viewNode.set(optionKey, JsonUtils.toJsonNode(value));
viewsNode.set(viewType, viewNode);
setOption("views", viewsNode);
```

---

### 7.14 `drag-n-drop` with `fixedMirrorParent`

FC's `fixedMirrorParent` sets the DOM element used as the parent of the drag mirror (the ghost element shown during drag). Useful when the calendar is inside a CSS-transformed container (e.g., `transform: scale()` or `transform: translate()`).

Without `fixedMirrorParent`, the drag mirror may appear at the wrong screen position if the calendar's ancestor has a CSS transform applied.

```java
public void setFixedMirrorParent(String jsExpression) {
    // Note: FC expects a DOM element reference, not a CSS selector string.
    // The Java API must accept a JS expression that evaluates to an element:
    setOption("fixedMirrorParent", jsExpression);
}
// e.g.: "document.body" or "document.querySelector('.my-container')"
```

**Important caveat:** FC's `fixedMirrorParent` accepts a DOM element, not a CSS selector string. The Java API accepts a JS expression string that the client evaluates. This is different from most other options — the value is treated as executable JS rather than a literal string. The TS companion must evaluate the expression using `new Function('return ' + expression)()`.

---

### 7.15 `dragScrollEls` typed setter

FC's `dragScrollEls` specifies CSS selectors for elements that should auto-scroll when the drag mirror approaches their edge.

```java
public void setDragScrollEls(String... cssSelectors) {
    setOption("dragScrollEls", String.join(",", cssSelectors));
}
```

---

### 7.16 `progressiveEventRendering` (cross-reference)

`progressiveEventRendering` causes FC to render batches of events as they become available, rather than waiting for all events to load before rendering any. This reduces perceived load time for calendars with many events.

This option is also listed in **Phase 2.1** as a missing typed setter (`setProgressiveEventRendering(boolean)`). It is an advanced performance option and belongs semantically in both Phase 2 (display configuration) and Phase 7 (advanced/performance). **Implement once in Phase 2.1.** Cross-reference here to avoid confusion.

```java
// In FullCalendar.java (also Phase 2.1):
public void setProgressiveEventRendering(boolean progressive) {
    setOption("progressiveEventRendering", progressive);
}
```

**Default:** `false` — FC waits for all events before rendering. Setting to `true` is most useful with `CallbackEntryProvider` on slow backends.

---

### 7.17 `handleWindowResize` (cross-reference)

`handleWindowResize` enables or disables FC's automatic resize observer. This is covered in full in **Phase 2.11**. Implement there.

Cross-reference here for completeness: when `handleWindowResize = false`, use `calendar.updateSize()` (Phase 7.11) manually when the container changes size.

---

## Implementation Notes

- Items 7.9–7.11 are trivially small (one-line JS function call delegations)
- Items 7.1 (customButtons) and 7.3 (themeSystem) have the most value for typical users
- Item 7.12 (CSP nonce) is important for enterprise/security-focused applications
- Item 7.13 (view-specific options) is a power-user feature that improves ergonomics significantly
- Items 7.16 and 7.17 are cross-references only — implement in Phase 2, not here
- None of these require new frontend TypeScript changes **except:**
  - 7.1 (customButtons): requires TS companion changes for server round-trip
  - 7.3 (bootstrap5): requires npm package if `BOOTSTRAP5` theme is supported
  - 7.14 (fixedMirrorParent): requires the option value to be treated as JS expression, not a literal

---

## Testing

### JUnit tests
Add a test class `Phase7AdvancedTest.java` in `addon/src/test/java/org/vaadin/stefan/fullcalendar/`.

Cover:
- Getter methods: verify `getCurrentIntervalStart()`, `getCurrentIntervalEnd()` return correct dates
- Navigation methods: verify `incrementDate()`, `previousYear()`, `nextYear()` call the correct JS functions
- JS callback string storage: verify `setValidRangeCallback()`, `setSelectOverlapCallback()` store JS functions correctly
- Option setters: verify `setContentSecurityPolicyNonce()`, `setFixedMirrorParent()`, `setDragScrollEls()` call `setOption()` with correct keys
- `CustomButton`: verify `toJson()` output includes name, text, hint, icon, click function
- `setViewSpecificOption()`: verify nested "views" JSON object is constructed correctly
- `ThemeSystem` enum: verify each enum value maps to correct FC string

### Playwright tests (client-side effects)
Add demo view at `demo/src/main/java/org/vaadin/stefan/ui/view/testviews/Phase7AdvancedTestView.java` to verify:
- Custom buttons render and can be clicked, firing server-side listener
- Theme system setting (if `BOOTSTRAP5` is supported) applies correct styling
- View-specific options apply (e.g., `dayMaxEventRows` set for month view only)
- `validRange` callback prevents navigation outside allowed date range
- `selectOverlap` callback prevents overlapping date selections

Add Playwright spec at `e2e-tests/tests/phase7-advanced.spec.js` to:
- Verify custom button text and icons render
- Verify custom button click fires server event
- Verify view-specific options affect only their target view
- Verify JS callbacks enforce constraints correctly

### Code and test review
After implementing all features and writing tests, review each artifact before committing:
- Run a `code-reviewer` agent on the implementation code (new classes, FullCalendar.java changes, frontend TypeScript). Fix all issues found.
- Run a `code-reviewer` agent on the JUnit tests. Fix all issues found (missing null-clearing tests, weak assertions, missing edge cases, etc.).
- Run a `code-reviewer` agent on the Playwright spec. Fix all issues (weak selectors, missing value assertions, flaky timing patterns, duplicate helpers vs. fixtures.js, etc.).
- Run an `end-user-reviewer` agent on all new Javadoc, demo view descriptions, and any user-facing error messages. Fix any unclear or misleading documentation found.
- Commit only after all review passes are clean.



---

## Files to Modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
  - Add `getCurrentIntervalStart()`, `getCurrentIntervalEnd()` getters (7.9)
  - Add `incrementDate(String)`, `previousYear()`, `nextYear()` (7.10)
  - Add `updateSize()` (7.11)
  - Add `setContentSecurityPolicyNonce(String)` (7.12)
  - Add `setViewSpecificOption(...)` / `setViewSpecificOptions(...)` (7.13)
  - Add `setFixedMirrorParent(String)`, `setDragScrollEls(String...)` (7.14, 7.15)
  - Add `setValidRangeCallback(String)`, `setSelectOverlapCallback(String)` (7.4, 7.5)
  - Add `setEventConstraint(String)`, `setEventConstraint(BusinessHours)`, `setEventConstraintToBusinessHours()` (7.6)
  - Add `setButtonIcons(Map<String, String>)`, `setThemeSystem(ThemeSystem)`, `setDateIncrement(String)`, `setDateAlignment(String)` (7.2, 7.3, 7.7)
- New classes:
  - `CustomButton.java` (for 7.1)
  - `CustomButtonClickedEvent.java` (for 7.1)
  - `ThemeSystem.java` enum (for 7.3 — also needed in Phase 2.1; create once)
- Frontend TypeScript companion file:
  - Route custom button clicks to server (for 7.1) — **required**
  - Register bootstrap5 plugin optionally (for 7.3) — only if BOOTSTRAP5 is supported

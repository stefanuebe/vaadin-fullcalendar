# Phase 6: Accessibility, Touch, and Print Options

## Goal

Add typed Java API for FC's accessibility hints, touch delay settings, and the `eventInteractive` option. These are mostly small additions that improve usability for keyboard/screen reader users and touch devices.

---

## Features Covered

### 6.1 `eventInteractive` per-entry and global option

FC's `eventInteractive` makes events focusable and tabbable via keyboard, even when they have no URL.

**Why this matters specifically in Vaadin:**
Vaadin applications are frequently deployed in enterprise contexts — finance, healthcare, logistics — where keyboard navigation is required for accessibility compliance. Many regulated industries require **WCAG 2.1 AA** compliance, which mandates that all interactive UI components be reachable and operable by keyboard alone (Success Criterion 2.1.1 Keyboard).

In the default FC configuration, calendar events are **not keyboard-reachable** unless they have a `url`. This means a user who cannot use a mouse (motor impairment, keyboard power user) cannot click on events to trigger `EntryClickedEvent` handlers, detail dialogs, or any other interaction. Making calendar events focusable is therefore a non-trivial accessibility gap for enterprise Vaadin applications.

Setting `eventInteractive: true` (globally or per-event via `Entry.interactive`) makes events receive Tab focus and respond to Enter/Space key presses. The `entryClick` FC callback fires on keyboard activation, which in turn fires the server-side `EntryClickedEvent`. No additional event handlers are needed — existing `addEntryClickedListener` registrations automatically work for keyboard users once `eventInteractive` is enabled.

**Global option on `FullCalendar`:**
```java
public void setEventInteractive(boolean interactive) {
    setOption("eventInteractive", interactive);
}
```

**Per-entry property on `Entry`:**
Already listed in Phase 1.2 (`Entry.setInteractive(Boolean)`). Cross-reference Phase 1.2 for full details. Do not duplicate the implementation.

**JSON mapping:** `"eventInteractive"` (calendar option key) vs `"interactive"` (event object property). These are different keys at different levels. The typed setter uses the string `"eventInteractive"` for the calendar option; the `Entry.interactive` field serializes as `"interactive"`.

**Recommendation:** Enable `eventInteractive: true` by default in any Vaadin application that registers `addEntryClickedListener`. Document this recommendation in the Javadoc.

---

### 6.2 Accessibility hint options

FC provides several string options for annotating interactive elements with `aria-label` and `title` attributes. These improve screen reader announcements and hover tooltips.

**The `$0` placeholder:** In hint strings, `$0` is replaced by FC with a context-specific value at render time. The substitution happens entirely in the browser — `$0` is not a Java placeholder.

| Method | FC option | `$0` resolves to | Example |
|---|---|---|---|
| `setButtonHints(Map<String, String>)` | `buttonHints` | N/A (map of button-name → hint) | `Map.of("today", "Go to today", "prev", "Previous period")` |
| `setViewHint(String)` | `viewHint` | The view name/title (e.g., "Month", "Week") | `"Switch to $0 view"` |
| `setNavLinkHint(String)` | `navLinkHint` | The date text of the nav link | `"Go to $0"` |
| `setMoreLinkHint(String)` | `moreLinkHint` | The count of hidden events | `"Show $0 more events"` |
| `setCloseHint(String)` | `closeHint` | N/A (no placeholder) | `"Close"` |
| `setTimeHint(String)` | `timeHint` | N/A | `"Time"` |
| `setEventHint(String)` | `eventHint` | N/A | `"Event"` |

**Example usage:**
```java
// Localize hints for a Spanish-language calendar:
calendar.setMoreLinkHint("Mostrar $0 eventos más");
calendar.setViewHint("Vista de $0");
calendar.setNavLinkHint("Ir al $0");
```

**Java API:**
```java
public void setButtonHints(Map<String, String> hints) {
    setOption("buttonHints", /* serialize map to JSON object */);
}
public void setViewHint(String hint) { setOption("viewHint", hint); }
public void setNavLinkHint(String hint) { setOption("navLinkHint", hint); }
public void setMoreLinkHint(String hint) { setOption("moreLinkHint", hint); }
public void setCloseHint(String hint) { setOption("closeHint", hint); }
public void setTimeHint(String hint) { setOption("timeHint", hint); }
public void setEventHint(String hint) { setOption("eventHint", hint); }
```

For function forms (dynamic hints), use raw `setOption(String, Object)`.

**JSON mapping:** All option keys are the FC camelCase names as shown. The `buttonHints` map serializes to a JSON object.

---

### 6.3 Touch delay options

FC provides three touch-related delay options for controlling how long a user must press before a touch interaction begins:

- `longPressDelay` — global delay before any long-press interaction (drag or select). **Default: 1000ms (1 second)**
- `eventLongPressDelay` — overrides `longPressDelay` for event dragging specifically
- `selectLongPressDelay` — overrides `longPressDelay` for date selection specifically

**Why the 1-second default is often too long:** Calendar apps are action-oriented — users expect quick interactions. A 1-second hold before being able to drag an event feels sluggish compared to typical mobile apps (200-500ms). For most calendar applications, setting `longPressDelay` to 500ms or 300ms provides a significantly better user experience.

```java
public void setLongPressDelay(int milliseconds) {
    setOption("longPressDelay", milliseconds);
}
public void setEventLongPressDelay(int milliseconds) {
    setOption("eventLongPressDelay", milliseconds);
}
public void setSelectLongPressDelay(int milliseconds) {
    setOption("selectLongPressDelay", milliseconds);
}
```

**Recommended value:** `500` ms for most calendar applications. Test on actual touch devices.

**Overlap with Phase 2.1:** These same three methods are also listed in Phase 2.1 as missing typed setters. Implement once in whichever phase is processed first; add a cross-reference in the other.

**Frontend TypeScript impact:** None — all flow through the standard `setOption` channel.

---

### 6.4 Print support

FC automatically handles print CSS (events reflow to fit the printed page using `@media print` styles). No Java API is needed. The main thing to document is:

1. Do **not** set `height` to a fixed pixel value if you want print to work well — use `height: 'auto'` or `contentHeight: 'auto'`
2. FC's print support only applies when the user prints the page; it does not generate a PDF file programmatically
3. For PDF generation, use a headless browser solution (e.g., Playwright in headless mode) or a Vaadin server-side PDF library

**No code changes needed.** Documentation update only.

---

### 6.5 `navLinkHint` and keyboard navigation for screen readers

The `navLinkHint` option (covered in 6.2 above) deserves special attention in the context of screen reader accessibility. When `navLinks` is enabled (via `setNavLinks(boolean)`, Phase 0.2), the day/week numbers in the grid become clickable links. Screen readers need meaningful `aria-label` text for these links.

Without `navLinkHint`, a screen reader might announce "link: 15" (just the day number), which is ambiguous. With `navLinkHint = "Go to $0"`, the screen reader announces "Go to Monday, March 15, 2024" — providing full context.

**Recommended configuration for accessible calendars:**
```java
calendar.setNavLinks(true);
calendar.setNavLinkHint("Go to $0");
calendar.setMoreLinkHint("$0 more events. Click to expand");
calendar.setEventInteractive(true);  // make events keyboard-reachable
```

This configuration ensures: (a) day numbers are navigable links with descriptive labels, (b) overflow "+N more" links have meaningful labels, and (c) all events are reachable by keyboard.

---

## Implementation Notes

- All items in this phase are small typed setter additions to `FullCalendar.java`
- `buttonHints` accepts a `Map<String, String>` that serializes to a JSON object — use the same JSON object serialization pattern as other Map-typed options
- No new event classes are needed
- No frontend changes are needed — all options pass through the existing `setOption` mechanism
- The touch delay options (6.3) also appear in Phase 2.1 — implement once, cross-reference

---

## Testing

### JUnit tests
Add a test class `Phase6AccessibilityTouchTest.java` in `addon/src/test/java/org/vaadin/stefan/fullcalendar/`.

Cover:
- Typed setters for hint options: verify `setEventInteractive()`, `setViewHint()`, `setMoreLinkHint()` etc. call `setOption()` with correct FC keys
- `buttonHints` map serialization: verify the `Map<String, String>` is serialized to a JSON object with correct key-value pairs
- Touch delay setters: verify millisecond values are passed through correctly to FC options
- `Entry.interactive` (Boolean): verify null vs true/false serialization, JSON key is `"interactive"`

### Playwright tests (client-side effects)
Add demo view at `demo/src/main/java/org/vaadin/stefan/ui/view/testviews/Phase6AccessibilityTouchTestView.java` with:
- Events in the calendar to verify keyboard focus and Enter key activation work when `eventInteractive: true`
- Accessibility hints to check aria-labels render correctly (inspect DOM, use accessibility tree)
- Touch device simulation to verify long-press delays allow/prevent drag operations

Add Playwright spec at `e2e-tests/tests/phase6-accessibility-touch.spec.js` to:
- Verify events receive keyboard focus when `eventInteractive: true`
- Verify pressing Enter on a focused event fires the click listener
- Verify aria-labels are set on buttons and nav links when hints are configured

---

## Files to Modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
  - Add `setEventInteractive(boolean)`
  - Add `setButtonHints(Map<String, String>)`, `setViewHint(String)`, `setNavLinkHint(String)`, `setMoreLinkHint(String)`, `setCloseHint(String)`, `setTimeHint(String)`, `setEventHint(String)`
  - Add `setLongPressDelay(int)`, `setEventLongPressDelay(int)`, `setSelectLongPressDelay(int)` (if not already added via Phase 2.1)
- `addon/src/main/java/org/vaadin/stefan/fullcalendar/Entry.java`
  - Add `interactive` (Boolean) field and accessor (see Phase 1.2 for full details)

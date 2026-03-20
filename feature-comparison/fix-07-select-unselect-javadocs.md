# Fix 7: Improve select/unselect Javadocs

## Context

PR #223 review comments by stefanuebe:

On `setValidRangeStart`/`setValidRangeEnd` area (line ~2130):
> What exactly does that mean. The comment needs to be more specific about this feature (same for the other methods of this "feature")

On `clearSelection()` (line ~2174):
> Needs to be more specific. We are in the Java world. What selection? How do I know if no further click is required? Do I need to call this, when I open a dialog?

## Methods to improve

### clearSelection() (currently at ~line 2173)

Current Javadoc:
```java
/**
 * Clears the current selection. This is only necessary, if after a selection no further click is required
 * by the user (e.g. through a dialog button). Any click by the user will clear the selection automatically.
 */
```

Improve to explain:
- "Selection" = the highlighted time range created when `setSelectable(true)` is active and the user drags across time slots (triggers `TimeslotsSelectedEvent`)
- When to call: After handling `TimeslotsSelectedEvent` programmatically (e.g., opening a dialog, creating an entry) — the highlight remains until the user clicks elsewhere. Call this to clear it immediately.
- Not needed if the user will click a button/element next (that click clears the selection naturally)
- Maps to FC's `calendar.unselect()`

### setValidRangeStart / setValidRangeEnd / setValidRange / clearValidRange

These already exist on master but may need improved Javadoc. Clarify:
- "Valid range" = the date range the user can navigate to. Dates outside are grayed out, navigation buttons stop at the boundary.
- `setValidRangeStart(LocalDate)` = calendar cannot navigate before this date (open-ended into the future)
- `setValidRangeEnd(LocalDate)` = calendar cannot navigate past this date (open-ended into the past)
- Interplay with `setValidRangeCallback(String)` — the callback overrides static range

### unselectAuto / unselectCancel (if they survive Fix 1)

If these typed setters survive Fix 1, improve their Javadocs to explain what they do in concrete Vaadin terms.

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java` — Javadoc improvements only

## Verification

1. `mvn clean install -DskipTests` — compiles

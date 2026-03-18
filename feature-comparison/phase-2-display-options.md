# Phase 2: Missing Calendar-Level Display Options and Render Hooks

## Goal

Add typed Java setters for missing calendar-level display options and implement the JS render hook callback options that currently have no Java API.

---

## Features Covered

### 2.1 Missing typed setters for display options

These options can currently only be set via raw `setOption(String, Object)`. Add typed setters to `FullCalendar`.

**Which have an existing `Option` enum entry vs. which need a new one:**

- `displayEventEnd` — **no existing enum entry** → need both new `Option.DISPLAY_EVENT_END` entry and typed setter
- `displayEventTime` — **already `Option.DISPLAY_ENTRY_TIME`** → just add `setDisplayEventTime(boolean)` (maps to existing enum; also note the existing `setDisplayEntryTime(boolean)` in Phase 0.1)
- `progressiveEventRendering` — **no existing enum entry** → need new entry and setter
- `rerenderDelay` — **no existing enum entry** → need new entry and setter
- `now` — **no existing enum entry** → need new entry and setter
- `nowIndicatorSnap` — **no existing enum entry** → need new entry, new enum, and setter
- `windowResizeDelay` — **no existing enum entry** → need new entry and setter
- `initialDate` — **no existing enum entry** → need new entry and setter (with caveat)
- `themeSystem` — **no existing enum entry** → need new entry, new `ThemeSystem` enum, and setter (also covered in Phase 7.3 — implement once, cross-reference)
- `defaultRangeSeparator` — **no existing enum entry** → need new entry and setter
- `buttonText` — **no existing enum entry** → need new entry and setter
- `titleRangeSeparator` — **no existing enum entry** → need new entry and setter
- `dayPopoverFormat` — **no existing enum entry** → need new entry and setter
- `dayMaxEventRows` — **no existing enum entry** → need new entry and setter
- `longPressDelay` — **no existing enum entry** → need new entry and setter (also covered in Phase 6.3)
- `eventLongPressDelay` — **no existing enum entry** → same
- `selectLongPressDelay` — **no existing enum entry** → same
- `dragRevertDuration` — **no existing enum entry** → need new entry and setter
- `allDayMaintainDuration` — **no existing enum entry** → need new entry and setter
- `eventDragMinDistance` — **no existing enum entry** → need new entry and setter
- `lazyFetching` — **no existing enum entry** → need new entry and setter
- `forceEventDuration` — **no existing enum entry** → need new entry and setter
- `defaultAllDay` — **no existing enum entry** → need new entry and setter

| FC Option | Suggested Java Method | Type | Notes |
|---|---|---|---|
| `displayEventEnd` | `setDisplayEventEnd(boolean)` | boolean | |
| `displayEventTime` | `setDisplayEventTime(boolean)` | boolean | `Option.DISPLAY_ENTRY_TIME` already exists |
| `progressiveEventRendering` | `setProgressiveEventRendering(boolean)` | boolean | Also cross-referenced in Phase 7.16 |
| `rerenderDelay` | `setRerenderDelay(int ms)` | int; pass `-1` or use `Optional` to disable | |
| `now` | `setNow(LocalDate)` / `setNow(LocalDateTime)` | overloads | Overrides FC's "now" — affects today highlighting |
| `nowIndicatorSnap` | `setNowIndicatorSnap(boolean)` | boolean | FC v6: boolean only — see caveat below |
| `windowResizeDelay` | `setWindowResizeDelay(int ms)` | int | |
| `initialDate` | `setInitialDate(LocalDate)` | LocalDate | Only meaningful before calendar attaches |
| `themeSystem` | `setThemeSystem(ThemeSystem)` | new enum | See Phase 7.3; implement once |
| `defaultRangeSeparator` | `setDefaultRangeSeparator(String)` | String | |
| `buttonText` | `setButtonText(Map<String, String>)` | Map | See buttonText note below |
| `titleRangeSeparator` | `setTitleRangeSeparator(String)` | String | |
| `dayPopoverFormat` | `setDayPopoverFormat(String)` | date format string | |
| `dayMaxEventRows` | `setDayMaxEventRows(int)` / `setDayMaxEventRowsFitToCell()` | int or boolean true | |
| `longPressDelay` | `setLongPressDelay(int ms)` | int | Also in Phase 6.3 |
| `eventLongPressDelay` | `setEventLongPressDelay(int ms)` | int | Also in Phase 6.3 |
| `selectLongPressDelay` | `setSelectLongPressDelay(int ms)` | int | Also in Phase 6.3 |
| `dragRevertDuration` | `setDragRevertDuration(int ms)` | int | Duration of the revert animation when a drag is cancelled |
| `allDayMaintainDuration` | `setAllDayMaintainDuration(boolean)` | boolean | Whether to maintain event duration when moving between timed/all-day |
| `eventDragMinDistance` | `setEventDragMinDistance(int px)` | int | Pixels of movement before a drag starts |
| `lazyFetching` | `setLazyFetching(boolean)` | boolean | Whether to fetch only visible range or wider |
| `forceEventDuration` | `setForceEventDuration(boolean)` | boolean | Forces events without end to render with a duration |
| `defaultAllDay` | `setDefaultAllDay(boolean)` | boolean | Default allDay value for events without start time |

**`buttonText` note:** The `buttonText` option accepts a `Map<String, String>` mapping FC button/view names to display strings. Known keys: `"today"`, `"month"`, `"week"`, `"day"`, `"list"`, `"prev"`, `"next"`, `"prevYear"`, `"nextYear"`. Example:
```java
calendar.setButtonText(Map.of(
    "today", "Hoy",
    "month", "Mes",
    "week", "Semana"
));
```
The map serializes directly to a JSON object via `setOption("buttonText", jsonObject)`.

**`nowIndicatorSnap` caveat:** In FC v6, `nowIndicatorSnap` is a **boolean only** — `true` means the indicator snaps to slots, `false` means it renders at exact time. **Remove the `AUTO` value** from any enum proposal. The correct Java signature is simply:
```java
public void setNowIndicatorSnap(boolean snap) {
    setOption("nowIndicatorSnap", snap);
}
```
No enum needed.

**`initialDate` caveat:** Only meaningful before calendar is attached. Setting it after the calendar is rendered in the DOM has **no effect** — FC ignores `initialDate` after initialization. Use `calendar.gotoDate(date)` instead for post-render navigation. This must be clearly documented in the Javadoc:
```java
/**
 * Sets the initial date displayed when the calendar first renders.
 * <strong>Only effective before the calendar is attached to the UI.</strong>
 * Use {@link #gotoDate(LocalDate)} after attachment.
 */
public void setInitialDate(LocalDate date) { ... }
```

---

### 2.2 Day-cell render hooks

The `dayCellClassNames`, `dayCellContent`, `dayCellDidMount`, `dayCellWillUnmount` hooks customize `<td>` cells in daygrid and timegrid views.

**JS callback argument:** The callback receives an info object with:
```js
{
  date: Date,           // the date of the cell
  dayNumberText: String, // the formatted day number (e.g., "15")
  isToday: boolean,     // true if the cell is today
  isPast: boolean,      // true if the cell date is in the past
  isFuture: boolean,    // true if the cell date is in the future
  isOther: boolean,     // true if cell is outside current month (multiMonth/dayGrid)
  view: View            // the current view object
}
```

Example JS function for `dayCellClassNames`:
```js
"function(info) { return info.isToday ? ['my-today-cell'] : []; }"
```
Example for `dayCellContent`:
```js
"function(info) { return { html: '<b>' + info.dayNumberText + '</b>' }; }"
```

**FC options:** `dayCellClassNames`, `dayCellContent`, `dayCellDidMount`, `dayCellWillUnmount`

**Java changes on `FullCalendar`:**
```java
public void setDayCellClassNamesCallback(String jsFunction)
public void setDayCellContentCallback(String jsFunction)
public void setDayCellDidMountCallback(String jsFunction)
public void setDayCellWillUnmountCallback(String jsFunction)
```

Each sets the corresponding option via `setOption("dayCellClassNames", s)` etc.

**Pattern:** Same as `setEntryClassNamesCallback(String s)` and `setEntryContentCallback(String s)` — call `setOption(fcOptionName, s)`. No frontend changes needed.

**Security note:** These accept raw JavaScript strings. The addon evaluates them using `new Function()` on the client side. This is intentional (same mechanism as `setEntryClassNamesCallback`, `setEntryDidMountCallback`, `setEntryContentCallback`) and documented in the existing codebase. Never pass user-controlled strings to these methods.

---

### 2.3 Day-header render hooks

The `dayHeaderClassNames`, `dayHeaderContent`, `dayHeaderDidMount`, `dayHeaderWillUnmount` hooks customize the column header `<th>` cells.

**JS callback argument:**
```js
{
  date: Date,           // the date for this column header
  text: String,         // the formatted header text (e.g., "Mon 5/15")
  isToday: boolean,
  isPast: boolean,
  isFuture: boolean,
  view: View
}
```

Example for `dayHeaderContent`:
```js
"function(info) { return { html: '<span class=\"my-header\">' + info.text + '</span>' }; }"
```

**Java changes on `FullCalendar`:**
```java
public void setDayHeaderClassNamesCallback(String jsFunction)
public void setDayHeaderContentCallback(String jsFunction)
public void setDayHeaderDidMountCallback(String jsFunction)
public void setDayHeaderWillUnmountCallback(String jsFunction)
```

---

### 2.4 Slot render hooks (timegrid / timeline)

The `slotLabelClassNames/Content/DidMount/WillUnmount` and `slotLaneClassNames/Content/DidMount/WillUnmount` hooks customize time slot label and lane cells.

**JS callback argument for slot label hooks:**
```js
{
  date: Date,     // the datetime of the slot (e.g., 09:00:00)
  text: String,   // the formatted slot label (e.g., "9am")
  view: View
}
```

**JS callback argument for slot lane hooks:**
```js
{
  date: Date,    // the datetime of the lane
  time: Duration, // duration from start of day (as object with milliseconds)
  view: View
}
```

Example for `slotLabelContent`:
```js
"function(info) { return { html: '<b>' + info.text + '</b>' }; }"
```

**Java changes on `FullCalendar`:**
```java
public void setSlotLabelClassNamesCallback(String jsFunction)
public void setSlotLabelContentCallback(String jsFunction)
public void setSlotLabelDidMountCallback(String jsFunction)
public void setSlotLabelWillUnmountCallback(String jsFunction)
public void setSlotLaneClassNamesCallback(String jsFunction)
public void setSlotLaneContentCallback(String jsFunction)
public void setSlotLaneDidMountCallback(String jsFunction)
public void setSlotLaneWillUnmountCallback(String jsFunction)
```

---

### 2.5 View render hooks

The `viewClassNames`, `viewDidMount`, `viewWillUnmount` hooks customize the view root element.

**JS callback argument:**
```js
{
  view: View,    // the view object (type, title, activeStart, activeEnd, currentStart, currentEnd)
  el: HTMLElement // the root element of the view
}
```

Note: `viewDidMount` is already partially covered server-side by `ViewSkeletonRenderedEvent`. The JS hook version is for client-side DOM manipulation (adding classes, initializing third-party libraries, etc.).

**Java changes on `FullCalendar`:**
```java
public void setViewClassNamesCallback(String jsFunction)
public void setViewDidMountCallback(String jsFunction)   // client-side alternative to ViewSkeletonRenderedEvent
public void setViewWillUnmountCallback(String jsFunction)
```

**Caveat:** `setViewDidMountCallback` and `addViewSkeletonRenderedListener` are different mechanisms for the same FC lifecycle point. The JS callback runs synchronously in the browser; the server event involves a round-trip. They can be used together.

---

### 2.6 Now-indicator render hooks

The `nowIndicatorClassNames`, `nowIndicatorContent`, `nowIndicatorDidMount`, `nowIndicatorWillUnmount` hooks customize the now-indicator line in timegrid views.

**JS callback argument:**
```js
{
  date: Date,      // current date/time
  isAxis: boolean, // true for the label part, false for the line part
  view: View
}
```

The `isAxis` flag is important — FC calls these hooks twice per render: once for the axis label (the time label on the left) and once for the line across the grid. Use `isAxis` to style them differently.

**Java changes on `FullCalendar`:**
```java
public void setNowIndicatorClassNamesCallback(String jsFunction)
public void setNowIndicatorContentCallback(String jsFunction)
public void setNowIndicatorDidMountCallback(String jsFunction)
public void setNowIndicatorWillUnmountCallback(String jsFunction)
```

---

### 2.7 Week-number render hooks

The `weekNumberClassNames`, `weekNumberContent`, `weekNumberDidMount`, `weekNumberWillUnmount` hooks.

**JS callback argument:**
```js
{
  date: Date,    // the first day of the week
  num: number,   // the week number
  text: String,  // the formatted text (e.g., "W23")
  view: View
}
```

Example for `weekNumberContent` — show a tooltip:
```js
"function(info) { return { html: '<span title=\"Week ' + info.num + '\">' + info.text + '</span>' }; }"
```

**Java changes on `FullCalendar`:**
```java
public void setWeekNumberClassNamesCallback(String jsFunction)
public void setWeekNumberContentCallback(String jsFunction)
public void setWeekNumberDidMountCallback(String jsFunction)
public void setWeekNumberWillUnmountCallback(String jsFunction)
```

---

### 2.8 More-link render hooks

The `moreLinkClassNames`, `moreLinkContent`, `moreLinkDidMount`, `moreLinkWillUnmount` hooks customize the `+N more` link shown when events overflow a day cell.

**JS callback argument:**
```js
{
  num: number,         // number of hidden events
  text: String,        // the display text (e.g., "+3 more")
  shortText: String,   // abbreviated text (e.g., "+3")
  view: View
}
```

**Java changes on `FullCalendar`:**
```java
public void setMoreLinkClassNamesCallback(String jsFunction)
public void setMoreLinkContentCallback(String jsFunction)
public void setMoreLinkDidMountCallback(String jsFunction)
public void setMoreLinkWillUnmountCallback(String jsFunction)
```

---

### 2.9 No-events render hooks (list view)

The `noEventsClassNames`, `noEventsContent`, `noEventsDidMount`, `noEventsWillUnmount` hooks customize the "no events" message in list view.

**JS callback argument:**
```js
{
  view: View   // the current view (that's all — there's no "message" field in the info object)
}
```

The no-events message text is controlled separately by the `noEventsText` option (a plain string). These hooks customize the *element* that wraps the text.

**Java changes on `FullCalendar`:**
```java
public void setNoEventsClassNamesCallback(String jsFunction)
public void setNoEventsContentCallback(String jsFunction)
public void setNoEventsDidMountCallback(String jsFunction)
public void setNoEventsWillUnmountCallback(String jsFunction)
```

---

### 2.10 All-day section render hooks (timegrid)

The `allDayClassNames`, `allDayContent`, `allDayDidMount`, `allDayWillUnmount` hooks customize the all-day section header cell (the cell showing "all-day" text at the top-left of timegrid views).

**JS callback argument:**
```js
{
  text: String,  // the default "all-day" text
  view: View
}
```

Example for `allDayContent` — translate the label:
```js
"function(info) { return { text: 'Todo el día' }; }"
```

Note: translating "all-day" via this hook is an alternative to using `setLocale()`; prefer locale if a full locale is available.

**Java changes on `FullCalendar`:**
```java
public void setAllDayClassNamesCallback(String jsFunction)
public void setAllDayContentCallback(String jsFunction)
public void setAllDayDidMountCallback(String jsFunction)
public void setAllDayWillUnmountCallback(String jsFunction)
```

---

### 2.11 `handleWindowResize` option

FC's `handleWindowResize` boolean enables or disables FC's automatic resize observer that recalculates calendar dimensions when the browser window resizes. Default is `true`.

**Use case for disabling:** In some Vaadin applications, the calendar container is inside a resizable panel or dialog with its own resize handling. Disabling FC's window resize listener and calling `updateSize()` manually (see Phase 7.11) may give more predictable behavior.

```java
public void setHandleWindowResize(boolean handleWindowResize) {
    setOption("handleWindowResize", handleWindowResize);
}
```

**Cross-reference:** `windowResizeDelay` in section 2.1 controls the debounce delay when `handleWindowResize` is `true`. Phase 7.17 cross-references this.

**Frontend TypeScript impact:** None.

---

### 2.12 `initialView` and its relationship to `changeView()`

FC's `initialView` option sets which view is shown on first render. The addon already has `changeView(CalendarView)` for post-render view changes, and `FullCalendarBuilder.withCustomCalendarViews()` for registering custom views — but there is no `setInitialView(CalendarView)` method.

**Use case:** Most applications want to pre-select the view type before the calendar is attached (e.g., always start in `timeGridWeek`). Currently this requires `withInitialOptions(JsonUtils.createObject().put("initialView", "timeGridWeek"))` which is verbose.

```java
public void setInitialView(CalendarView view) {
    setOption("initialView", view.getClientSideValue());
}
```

**Caveat:** Like `initialDate`, this only affects the initial render. After attachment, use `changeView(CalendarView)`.

**Builder integration:** This is also a candidate for `FullCalendarBuilder.withInitialView(CalendarView)` (see Phase 0.10).

**Frontend TypeScript impact:** None.

---

## Implementation Notes

- All render hook setters follow the exact same pattern as `setEntryClassNamesCallback(String)` and `setEntryContentCallback(String)`:
  ```java
  public void setDayCellClassNamesCallback(String s) {
      setOption("dayCellClassNames", s);
  }
  ```
- No frontend TypeScript changes are needed for any item in this phase — the JS string is passed directly to FC as an option value via the existing `setOption` channel
- The JS string is evaluated using `new Function()` on the client side (this is intentional and documented in the existing codebase)
- Security note: these accept raw JS — same disclaimer as existing callbacks (`setEntryClassNamesCallback`, `setEntryDidMountCallback`). Never pass user-controlled content.
- The `setNowIndicatorSnap` typed setter is a simple boolean — **do not create an enum**. In FC v6, this is `true`/`false` only.
- `setInitialDate` should document that it only affects the initial render and has no effect after the calendar is attached. Use `gotoDate(LocalDate)` instead.
- **Overlaps with Phase 0.1:** `displayEventTime` has an existing `Option.DISPLAY_ENTRY_TIME` enum entry. The Phase 0.1 typed setter `setDisplayEntryTime(boolean)` and the Phase 2 typed setter `setDisplayEventTime(boolean)` should be the same method (or aliases). Implement once.
- **Overlaps with Phase 6.3:** Long-press delay options appear in both phases. Implement once in whichever phase is done first.

---

## Files to Modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
  - Add ~50 typed setter methods for display options and render hooks
- Optionally create new enum classes: `ThemeSystem.java` (also needed in Phase 7.3 — create once)
- New `Option` enum entries for the ~20 options that don't yet have an enum entry

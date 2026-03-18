# Phase 0: API Improvements to Existing Features

## Goal

Fix inconsistencies, add missing typed setter/getter methods for options that are already in the `Option` enum or `SchedulerOption` enum but lack dedicated typed methods, correct naming issues, and improve overall API ergonomics. These are improvements to things that already work, not new features.

---

## Issues to Address

### 0.1 Typed setters missing for existing Option enum entries

Several options are in the `Option` enum and accessible via `setOption(Option, Object)`, but have no convenience typed method on `FullCalendar`. Adding typed setters provides IDE completion, parameter validation, and avoids raw strings.

**Design rationale:** The `setOption(Option, Object)` back-door is intentional (for forward-compatibility with FC options the addon does not yet wrap), but discoverability and type safety suffer. Every option that has a clear Java type mapping should also have a named setter so developers do not need to know the internal option key string or the expected value type. This pattern is already established by methods like `setLocale(Locale)`, `setEditable(boolean)`, `setSnapDuration(String)`.

**JSON mapping note:** The `Option` enum stores the FC option key string (e.g., `Option.DISPLAY_ENTRY_TIME` → `"displayEventTime"`). The typed setter simply calls `setOption(option.getOptionKey(), value)` or the overload `setOption(Option, Object, Object)` when a server-side typed version needs to be cached separately. No `@JsonName` annotations are involved — these are calendar-level options, not Entry fields.

**Which options already have an `Option` enum entry vs. which need a new one:**

| Option Enum Entry | Already in `Option` enum? | Missing Typed Setter | Suggested Signature |
|---|---|---|---|
| `Option.DISPLAY_ENTRY_TIME` | Yes | Yes | `setDisplayEntryTime(boolean)` |
| `Option.HIDDEN_DAYS` | Yes | Yes | `setHiddenDays(DayOfWeek...)` or `setHiddenDays(Set<DayOfWeek>)` |
| `Option.ASPECT_RATIO` | Yes | Yes | `setAspectRatio(double)` |
| `Option.CONTENT_HEIGHT` | Yes | Yes | `setContentHeight(String)` / `setContentHeight(int)` |
| `Option.EXPAND_ROWS` | Yes | Yes | `setExpandRows(boolean)` |
| `Option.STICKY_HEADER_DATES` | Yes | Yes | `setStickyHeaderDates(boolean)` |
| `Option.STICKY_FOOTER_SCROLLBAR` | Yes | Yes | `setStickyFooterScrollbar(boolean)` |
| `Option.ALL_DAY_SLOT` | Yes | Yes | `setAllDaySlot(boolean)` |
| `Option.SLOT_ENTRY_OVERLAP` | Yes | Yes | `setSlotEntryOverlap(boolean)` |
| `Option.ENTRY_MIN_HEIGHT` | Yes | Yes | `setEntryMinHeight(int)` |
| `Option.ENTRY_SHORT_HEIGHT` | Yes | Yes | `setEntryShortHeight(int)` |
| `Option.ENTRY_MAX_STACK` | Yes | Yes | `setEntryMaxStack(int)` |
| `Option.SHOW_NON_CURRENT_DATES` | Yes | Yes | `setShowNonCurrentDates(boolean)` |
| `Option.SLOT_DURATION` | Yes | Yes | `setSlotDuration(String)` / `setSlotDuration(Duration)` |
| `Option.SLOT_LABEL_INTERVAL` | Yes | Yes | `setSlotLabelInterval(String)` |
| `Option.DAY_HEADERS` | Yes | Partial | `setColumnHeader(boolean)` exists but is deprecated; `setDayHeaders(boolean)` needed |
| `Option.SCROLL_TIME` | Yes | Yes | `setScrollTime(String)` / `setScrollTime(LocalTime)` |
| `Option.SCROLL_TIME_RESET` | Yes | Yes | `setScrollTimeReset(boolean)` |
| `Option.DIRECTION` | Yes | Yes | `setDirection(Direction)` — new `Direction` enum with `LTR`, `RTL` |
| `Option.SELECT_MIRROR` | Yes | Yes | `setSelectMirror(boolean)` |
| `Option.SELECT_MIN_DISTANCE` | Yes | Yes | `setSelectMinDistance(int)` |
| `Option.SELECT_OVERLAP` | Yes | Yes | `setSelectOverlap(boolean)` |
| `Option.SELECT_CONSTRAINT` | Yes | Yes | `setSelectConstraint(String groupId)` / `setSelectConstraintToBusinessHours()` |
| `Option.UNSELECT_AUTO` | Yes | Yes | `setUnselectAuto(boolean)` |
| `Option.UNSELECT_CANCEL` | Yes | Yes | `setUnselectCancel(String cssSelector)` |
| `Option.ENTRY_ORDER` | Yes | Yes | `setEntryOrder(String...)` |
| `Option.ENTRY_ORDER_STRICT` | Yes | Yes | `setEntryOrderStrict(boolean)` |
| `Option.NEXT_DAY_THRESHOLD` | Yes | Yes | `setNextDayThreshold(String)` / `setNextDayThreshold(LocalTime)` |
| `Option.DAY_MIN_WIDTH` | Yes | Yes | `setDayMinWidth(int)` |
| `Option.ENTRY_TIME_FORMAT` | Yes | Yes | `setEntryTimeFormat(String)` (accepts FC date format object as JSON string) |
| `Option.DAY_HEADER_FORMAT` | Yes | Yes | `setDayHeaderFormat(String)` |
| `Option.LIST_DAY_FORMAT` | Yes | Yes | `setListDayFormat(String)` |
| `Option.LIST_DAY_SIDE_FORMAT` | Yes | Yes | `setListDaySideFormat(String)` |
| `Option.WEEK_NUMBER_CALCULATION` | Yes | Yes | `setWeekNumberCalculation(WeekNumberCalculation)` — new enum `ISO`, `LOCAL`, `ISO8601` |
| `Option.WEEK_NUMBER_FORMAT` | Yes | Yes | `setWeekNumberFormat(String)` |
| `Option.WEEK_TEXT` | Yes | Yes | `setWeekText(String)` |
| `Option.WEEK_TEXT_LONG` | Yes | Yes | `setWeekTextLong(String)` |
| `Option.MULTI_MONTH_MAX_COLUMNS` | Yes | Yes | `setMultiMonthMaxColumns(int)` |
| `Option.MULTI_MONTH_MIN_WIDTH` | Yes | Yes | `setMultiMonthMinWidth(int)` |
| `Option.MULTI_MONTH_TITLE_FORMAT` | Yes | Yes | `setMultiMonthTitleFormat(String)` |
| `Option.MONTH_START_FORMAT` | Yes | Yes | `setMonthStartFormat(String)` |
| `Option.DRAG_SCROLL` | Yes | Partial | `setDragScrollActive(boolean)` exists but name is inconsistent (should be `setDragScroll(boolean)`) |
| `Option.SNAP_DURATION` | Yes | Present | `setSnapDuration(String)` — good, no change needed |

**Implementation pattern:** All typed setters follow the same skeleton as `setEditable(boolean)` or `setWeekends(boolean)`:
```java
public void setSlotEntryOverlap(boolean overlap) {
    setOption(Option.SLOT_ENTRY_OVERLAP, overlap);
}
```
For options that need the server-side cached value to differ from the JSON value (e.g., enum types), use the three-argument overload:
```java
public void setDirection(Direction direction) {
    setOption(Option.DIRECTION, direction.name().toLowerCase(), direction);
}
```

**Frontend TypeScript impact:** None. All options flow through the existing `setOption` mechanism which sends the value to the client via property binding. No TS changes needed for any typed setter in this section.

---

### 0.2 Naming inconsistencies

**Strategy guide:** Each naming problem is different. Before fixing, choose the correct strategy:

1. **Rename with deprecated alias** — use when the old name is actively misleading or wrong (e.g., a typo). Keep the old name as `@Deprecated(forRemoval = true)` pointing to the new name.
2. **Add new alias, keep old** — use when the old name is just slightly different from FC's name but not actively harmful. Mark old as `@Deprecated(forRemoval = false)` (not scheduled for removal). This preserves backward compatibility.
3. **Rename outright** — only use for pre-1.0 APIs or when the old name was never public. **Avoid** in this addon — breaking API changes have real user impact.

**Deprecation policy note:** Use `@Deprecated(forRemoval = false)` on old inconsistent names and add new correctly-named aliases. Do not break the API. The only exception is genuine bugs (typos in method names) which qualify as `@Deprecated(forRemoval = true)` since leaving them implies the typo is intentional.

| Current Method | Issue | Strategy | Corrected Method |
|---|---|---|---|
| `setTimeslotsSelectable(boolean)` | Inconsistent — should match FC option name `selectable` | Add new alias, keep old (`forRemoval = false`) | Add `setSelectable(boolean)` |
| `setNumberClickable(boolean)` | Obscure — maps to `navLinks` FC option | Add new alias, keep old (`forRemoval = false`) | Add `setNavLinks(boolean)` |
| `setNowIndicatorShown(boolean)` | Verbose, inconsistent | Add new alias, keep old (`forRemoval = false`) | Add `setNowIndicator(boolean)` |
| `setColumnHeader(boolean)` | Already deprecated; old FC v5 name | `setDayHeaders(boolean)` is the correct v6 name; existing `@Deprecated` on old method is correct |
| `setDragScrollActive(boolean)` | Inconsistent with `Option.DRAG_SCROLL` | Add new alias, keep old (`forRemoval = false`) | Add `setDragScroll(boolean)` |
| `Scheduler.setResourceLablelWillUnmountCallback(String)` | Typo: `Lablel` — this is a genuine bug | Rename outright; add deprecated alias (`forRemoval = true`) | `setResourceLabelWillUnmountCallback(String)` |

**Example of deprecated alias pattern:**
```java
/**
 * @deprecated Use {@link #setSelectable(boolean)} instead.
 */
@Deprecated(forRemoval = false)
public void setTimeslotsSelectable(boolean selectable) {
    setSelectable(selectable);
}

public void setSelectable(boolean selectable) {
    setOption(Option.SELECTABLE, selectable);
}
```

---

### 0.3 Missing getters for typed options

Most `setXxx(value)` methods have no corresponding `getXxx()`. The `getOption(Option)` raw method works, but typed getters are more ergonomic.

**Design rationale:** Typed getters allow Vaadin binding, conditional logic, and logging without explicit casting. The existing pattern is `getEditable()`, `getWeekends()`, `getFixedWeekCount()` — these all call `getOption(Option).orElse(defaultValue)` and cast to the return type.

**Pattern:**
```java
public boolean getSlotEntryOverlap() {
    return (boolean) getOption(Option.SLOT_ENTRY_OVERLAP).orElse(true);
}
```

**Priority list — all recommended getter methods to add:**

| Setter | Getter | Return Type | Default if not set |
|---|---|---|---|
| `setSlotMinTime(String)` | `getSlotMinTime()` | `String` | `"00:00:00"` |
| `setSlotMaxTime(String)` | `getSlotMaxTime()` | `String` | `"24:00:00"` |
| `setSnapDuration(String)` | `getSnapDuration()` | `String` | same as slotDuration |
| `setFirstDay(DayOfWeek)` | `getFirstDay()` | `DayOfWeek` | `DayOfWeek.MONDAY` |
| `setLocale(Locale)` | `getLocale()` | `Locale` | already implemented |
| `setSlotEntryOverlap(boolean)` | `getSlotEntryOverlap()` | `boolean` | `true` |
| `setAllDaySlot(boolean)` | `isAllDaySlot()` | `boolean` | `true` |
| `setExpandRows(boolean)` | `isExpandRows()` | `boolean` | `false` |
| `setStickyHeaderDates(boolean)` | `isStickyHeaderDates()` | `boolean` | `false` |
| `setNowIndicator(boolean)` | `isNowIndicator()` | `boolean` | `false` |
| `setSelectable(boolean)` | `isSelectable()` | `boolean` | `false` |
| `setNavLinks(boolean)` | `isNavLinks()` | `boolean` | `false` |
| `setDayHeaders(boolean)` | `isDayHeaders()` | `boolean` | `true` |
| `setDayMinWidth(int)` | `getDayMinWidth()` | `int` | `0` |
| `setContentHeight(int)` | `getContentHeight()` | `Optional<Integer>` | `Optional.empty()` |
| `setAspectRatio(double)` | `getAspectRatio()` | `Optional<Double>` | `Optional.empty()` |
| `setMultiMonthMaxColumns(int)` | `getMultiMonthMaxColumns()` | `int` | `3` |
| `setEntryMaxStack(int)` | `getEntryMaxStack()` | `Optional<Integer>` | `Optional.empty()` |

**Caveat:** Getters for options set through `withInitialOptions(ObjectNode)` on the builder will return empty/default since those options bypass the server-side option cache. Document this in each getter's Javadoc.

---

### 0.4 `Entry.setConstraint(String)` is too weak

The FC `constraint` property accepts:
1. A groupId string — restricts the event to only overlap with events of the same groupId
2. The string `"businessHours"` — restricts to within business hours
3. A BusinessHours-like object — an inline business hours definition

The addon currently stores `constraint` as a `String` field on `Entry`. The JSON serialization uses the field name `"constraint"` directly (no `@JsonName` needed — field name matches FC option name). The `@Getter`/`@lombok.Setter` on `Entry` expose `getConstraint()` / `setConstraint(String)`.

**Full overload set with types and when each applies:**

```java
// Overload 1: restrict to entries with the same groupId string
// Use when: you want to restrict drag/resize to specific zones defined by background events
public void setConstraint(String groupId) {
    this.constraint = groupId;
}

// Overload 2: restrict to business hours (FC keyword string)
// Use when: entry must only be placed during business hours
public void setConstraintToBusinessHours() {
    this.constraint = "businessHours";
}

// Overload 3: restrict to an inline business hours definition (object form)
// Use when: business hours for the constraint differ from the calendar's main businessHours
// Serialization: the BusinessHours object is converted to a JSON object via BusinessHours.toJson()
// The constraint field then holds a JSON snippet like: {"daysOfWeek":[1,2,3,4,5],"startTime":"09:00","endTime":"17:00"}
// This requires the constraint field type to be changed from String to Object, or a separate serialized field
public void setConstraint(BusinessHours businessHours) {
    // requires a dedicated approach since constraint can be string or object
    // recommend: store as JsonNode internally, serialize appropriately
    this.constraintObject = businessHours.toJson();
    this.constraint = null;
}
```

**Implementation caveat:** Since `constraint` is a simple `String` field backed by `@lombok.Setter`, adding a `BusinessHours` overload requires either:
- Changing the field type to `Object` and adjusting the JSON serializer
- Or adding a separate `constraintJson` field with `@JsonIgnore` on the string field and custom serialization

The cleanest approach given the existing Jackson-based serialization is to use a `JsonNode` internal field with a `@JsonConverter` that handles both string and object forms.

**Cross-reference:** Section 0.4 covers the `Entry`-level constraint. The `FullCalendar`-level `eventConstraint` and `selectConstraint` options have the same multi-form API and should also get typed overloads (see Phase 7.6).

---

### 0.5 `defaultAllDayEventDuration` / `defaultTimedEventDuration` are constants, not setters

These are declared as static final `int` constants in `FullCalendar`:
```java
public static final int DEFAULT_TIMED_EVENT_DURATION = 1;
public static final int DEFAULT_DAY_EVENT_DURATION = 1;
```

**Why this is wrong:** These constants are not wired to the FC `defaultTimedEventDuration` and `defaultAllDayEventDuration` options at all. They appear to be documentation or compile-time defaults, not actual calendar configuration. FC uses its own defaults (1 hour for timed events, 1 day for all-day events) unless explicitly configured.

**The correct fix:**
1. Keep the constants as documentation if desired, but add typed setters that actually configure FC:
   ```java
   public void setDefaultTimedEventDuration(String duration) {
       setOption("defaultTimedEventDuration", duration);
   }
   // e.g., "01:00" for 1 hour, "PT30M" for 30 minutes

   public void setDefaultAllDayEventDuration(String duration) {
       setOption("defaultAllDayEventDuration", duration);
   }
   // e.g., "P1D" for 1 day, "P2D" for 2 days
   ```
2. Optionally add `Duration`-typed overloads if a Java `Duration` can be reliably serialized to FC's duration format (ISO-8601 or HH:MM).

**Caveat:** FC duration format differs from Java `Duration.toString()`. Java uses `PT1H`, FC accepts both `"01:00:00"` and `"PT1H"`. Test which format FC v6 accepts for these options.

**Frontend TypeScript impact:** None — these flow through the standard `setOption` channel.

---

### 0.6 `FullCalendar.setHeightFull()` not documented in the Option enum

`setHeightFull()` and related size setters are present but `contentHeight` has no typed setter, creating inconsistency. See 0.1 table for `setContentHeight(String)` / `setContentHeight(int)`. The `setHeightFull()` should internally call `setContentHeight("auto")` (or the FC `height: "100%"` option) and be documented as such.

---

### 0.7 Scheduler `refetchResourcesOnNavigate` has SchedulerOption but no typed setter

`SchedulerOption.REFETCH_RESOURCES_ON_NAVIGATE` exists in the enum but there is no `Scheduler.setRefetchResourcesOnNavigate(boolean)` method. This is covered in Phase 5.6 as well — do not duplicate implementation.

```java
// In Scheduler interface:
void setRefetchResourcesOnNavigate(boolean refetch);
// Implementation in FullCalendarScheduler:
public void setRefetchResourcesOnNavigate(boolean refetch) {
    setOption(SchedulerOption.REFETCH_RESOURCES_ON_NAVIGATE, refetch);
}
```

---

### 0.8 `addViewChangedListener` is a duplicate / deprecated alias

`FullCalendar.addViewChangedListener(...)` is marked as a secondary method for `addViewSkeletonRenderedListener(...)`. The naming is confusing because "view changed" could mean the user navigated (changing the date range) or changed the view type — both are valid interpretations. The preferred method is `addViewSkeletonRenderedListener` for the skeleton render event, and `addDatesRenderedListener` (`DatesRenderedEvent`) for the actual date range update.

**Documentation clarification needed:** The Javadoc on `addViewChangedListener` should clearly state it is an alias for `addViewSkeletonRenderedListener` and explain the distinction between skeleton render (view structure ready) and dates render (data loaded and dates painted).

---

### 0.9 Missing `getOption` for scheduler options in `Scheduler` interface

`FullCalendarScheduler` has `getOption(SchedulerOption)` but it is not exposed in the `Scheduler` interface. This means code typed to `Scheduler` (interface) cannot call `getOption` without casting to `FullCalendarScheduler`.

**Fix:** Add to `Scheduler` interface:
```java
<T> Optional<T> getOption(SchedulerOption option);
```

---

### 0.10 `FullCalendarBuilder` gaps

Several options that logically belong in the initial calendar configuration (and cannot be changed after initialization, or are best set before first render) cannot be set through `FullCalendarBuilder`. The builder currently supports: `entryLimit`, `autoBrowserTimezone`, `autoBrowserLocale`, `schedulerLicenseKey`, `initialOptions` (raw JSON), `entryProvider`, `customType`, `initialEntries`, `entryContent`, `customCalendarViews`.

**Options that should be builder-settable but currently require post-construction setters or raw `withInitialOptions`:**

| Option | Why it belongs in the builder |
|---|---|
| `initialView` / initial `CalendarView` | View is typically fixed at construction time; changing it post-render causes a visual flash |
| `initialDate` | Only meaningful before first render; setting after attach has no effect |
| `locale` | Should be set before first render to avoid a locale-switch flash |
| `timezone` | Same — changing timezone after render redraws the entire calendar |
| `themeSystem` | Cannot be changed after render |
| `contentSecurityPolicyNonce` | Must be set before FC injects any `<style>` tags |
| `businessHours` | Purely initial configuration in most apps |
| `customButtons` | Toolbar buttons must be set before initial render |
| `headerToolbar` / `footerToolbar` | Same |
| `direction` (LTR/RTL) | Typically set once at construction based on locale |

**Proposed builder methods:**
```java
FullCalendarBuilder withInitialView(CalendarView view)
FullCalendarBuilder withInitialDate(LocalDate date)
FullCalendarBuilder withLocale(Locale locale)
FullCalendarBuilder withTimezone(Timezone timezone)
FullCalendarBuilder withThemeSystem(ThemeSystem themeSystem)
FullCalendarBuilder withBusinessHours(BusinessHours... businessHours)
FullCalendarBuilder withDirection(Direction direction)
```

**Design rationale:** The builder currently delegates most pre-render config to `withInitialOptions(ObjectNode)` which is raw JSON and provides no type safety. Adding builder methods for the most common "set-once" options gives developers a discoverable, type-safe path.

**Implementation note:** The builder's internal constructor is already long (11 parameters in the current `FullCalendarBuilder(boolean, int, boolean, boolean, String, ObjectNode, EntryProvider, Class, Collection, String, CustomCalendarView[])` signature). Consider switching to Lombok `@Builder` as the TODO comment in `FullCalendarBuilder.java` already suggests, which would make adding new parameters much less painful.

---

## Files to Modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java` — typed setters and renamed methods
- `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/Scheduler.java` — `setRefetchResourcesOnNavigate`, fix typo `setResourceLablelWillUnmountCallback`, expose `getOption(SchedulerOption)`
- `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendarScheduler.java` — implementations
- `addon/src/main/java/org/vaadin/stefan/fullcalendar/Entry.java` — `setConstraint(BusinessHours)`, `setConstraintToBusinessHours()`
- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendarBuilder.java` — new builder methods for 0.10
- New enums (small files): `Direction.java`, `WeekNumberCalculation.java`

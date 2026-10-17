# UC-020: Advanced Options (options() API)

**As a** Vaadin application developer, **I want to** set arbitrary FullCalendar options **so that** I can access FullCalendar features beyond what dedicated Java methods provide.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon + addon-scheduler
**Related Options:** All typed option fields of `calendar.options()`, plus `options().setRawOption(String, Object)` for raw options
**Related Events:** —

---

## User-Facing Behavior

- Every supported option is a typed field of `calendar.options()` and is set with its setter, for example `options().setEditable(true)`
- Raw FullCalendar option names can be passed as strings: `options().setRawOption("someOption", value)`
- View-specific options override global options for particular views, see `options().view(view)`
- Options can be read back with the getter of the field, for example `options().getEditable()`, or with `options().getRawOption(name)` for raw options

---

## Java API Usage

```java
// Typed option
calendar.options().setEditable(true);
calendar.options().setSlotDuration(Duration.ofMinutes(15));
calendar.options().setLocale(Locale.GERMAN);

// Initial date
calendar.options().setInitialDate(LocalDate.of(2025, 3, 1));

// Raw option (no field)
calendar.options().setRawOption("someNewFcOption", "value");

// View-specific override
calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayMaxEntryRows(3);
calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setSlotDuration(Duration.ofMinutes(30));

// Scheduler option
scheduler.options().setResourceColumnsWidth(200);

// Read back, null while not set
Boolean editable = calendar.options().getEditable();
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | The field name is the FullCalendar option name, with "event" replaced by "entry" (`eventColor` → `entryColor`) |
| BR-02 | Fields whose name differs from the library name have an explicit `@JsonName` (e.g., `dayMaxEntries` → `"dayMaxEvents"`) |
| BR-03 | `@JsonConverter` on a field handles the conversion to the client form (Duration, DayOfWeek, BusinessHours, etc.) |
| BR-04 | View-specific options are set per view type with `options().view(view)`. They override the calendar-wide value while that view is shown and reach the client as the library option `views`. A raw option `views` replaces all of them |
| BR-05 | Raw options bypass the typed fields. The value is converted to JSON as it is, incorrect values may cause client-side errors. A raw option wins over the field with the same name. The time zone cannot be a raw option, use `options().setTimeZone(Timezone)` |
| BR-06 | Options set via constructor `initialOptions` are raw options, they have no typed field. Two keys are the exception: `views` becomes custom views and `timeZone` goes into its field. `options().getRawOption(name)` returns them, the getters of the fields do not. They win over the field with the same name, as every raw option does; `setRawOption(name, value)` replaces one and `setRawOption(name, null)` removes it. |
| BR-07 | A getter returns null while the option is not set, then the library default applies. `locale`, `timeZone` and `headerToolbar` start with a value (`headerToolbar` is `ToolbarValue.NONE`) |
| BR-08 | `setInitialDate` takes a `LocalDate` and sends it as an ISO date string, or a `LocalDateTime`, which it sends as an ISO UTC date-time string |

---

## Acceptance Criteria

- [ ] All option fields can be set and take effect
- [ ] Raw string option names work
- [ ] View-specific options override global options
- [ ] Type converters handle Duration, DayOfWeek, Locale, BusinessHours correctly
- [ ] The getters return previously set values, null for an option that is not set
- [ ] `getRawOption()` returns values set via the `initialOptions` constructor, the field getters do not
- [ ] `initialDate` converts a `LocalDate` to an ISO date string

---

## Tests

### Unit Tests
- [ ] `FullCalendarOptionsTest` — option set/get, `initialDate` kept as given (its client form is tested in `TypedOptionsSyncTest`)
- [ ] `AdvancedOptionsTest` — raw options, view-specific options
- [ ] `ConverterTest` — type conversion

### E2E Tests
- [ ] `advanced-options.spec.js` — advanced option effects

---

## Related FullCalendar Docs

- [Options](https://fullcalendar.io/docs)

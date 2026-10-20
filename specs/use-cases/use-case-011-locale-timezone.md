# UC-011: Locale and Timezone

**As a** Vaadin application developer, **I want to** set the calendar's locale and timezone **so that** dates, times, and labels are displayed correctly for the user's region.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `locale`, `timeZone`, `firstDay`, `weekNumberCalculation`, `direction`
**Related Events:** `BrowserTimezoneObtainedEvent`

---

## User-Facing Behavior

- The calendar displays dates, day names, and time formats according to the configured locale
- The first day of the week adapts to the locale (Monday in DE, Sunday in US) — or can be overridden
- All entry times are converted to the configured timezone for display
- Right-to-left (RTL) layout is supported via `options().setDirection(Direction.RTL)`
- The browser's timezone is detected on first load and reported via `BrowserTimezoneObtainedEvent`

---

## Java API Usage

```java
// Set locale
calendar.options().setLocale(Locale.GERMAN);

// Set timezone (the initial value is Timezone.UTC)
calendar.options().setTimeZone(new Timezone(ZoneId.of("Europe/Berlin")));
// or UTC
calendar.options().setTimeZone(Timezone.UTC);
// read, never null; while the calendar follows the browser, Timezone.LOCAL until the browser reports its zone
Timezone zone = calendar.options().getTimeZone();

// Override first day of week
calendar.options().setFirstDay(DayOfWeek.MONDAY);

// RTL layout
calendar.options().setDirection(Direction.RTL);

// Detect browser timezone
calendar.addBrowserTimezoneObtainedListener(event -> {
    Timezone browserTz = event.getTimezone();
});

// Follow the browser time zone
calendar.options().setTimeZone(Timezone.LOCAL);

// Follow the UI locale: the calendar does not observe it, the application does
// (a LocaleChangeObserver that calls calendar.options().setLocale(event.getLocale()))
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Locale affects day names, month names, date formatting, and first day of week |
| BR-02 | `firstDay` overrides the locale's default first day |
| BR-03 | Timezone converts all entry times for display — the Java model stores `LocalDateTime` (UTC-normalized). Setters accept `LocalDateTime`, `Instant`, or `LocalDate`. |
| BR-04 | `BrowserTimezoneObtainedEvent` fires once on initial load |
| BR-05 | `weekNumberCalculation` takes a `WeekNumberCalculationValue`: `WeekNumberCalculation.LOCAL` (`"local"`), `WeekNumberCalculation.ISO` (`"ISO"`) or a `JsCallback` |
| BR-06 | A calendar created without a locale gets `CalendarLocale.getDefaultLocale()`, so `options().getLocale()` is never null. The locale cannot be removed |
| BR-07 | `options().setTimeZone(Timezone)` takes a `Timezone` and `options().getTimeZone()` always returns one. A time zone cannot be set as a raw option. A new calendar starts with `Timezone.UTC`. An initial option `"timeZone": "local"` gives `Timezone.LOCAL` |
| BR-08 | `Timezone.LOCAL` sets the time zone the browser reports, also when it is already known, and follows each later report until another zone is set. The calendar does not follow the UI locale; an application that wants that calls `options().setLocale(…)` from its own locale change observer |

---

## Acceptance Criteria

- [ ] Setting locale to DE shows German day/month names
- [ ] First day of week changes with locale (or `firstDay` override)
- [ ] Timezone conversion displays correct times
- [ ] `BrowserTimezoneObtainedEvent` fires with correct browser timezone
- [ ] RTL direction renders calendar right-to-left *(manual verification)*
- [ ] Week numbers use configured calculation method

---

## Tests

### Unit Tests
- [ ] `CalendarLocaleTest` — locale handling
- [ ] `TimezoneTests` — timezone conversion
- [x] `TypedOptionValuesTest`: the time zone is stored and sent to the client
- [x] `AutoBrowserSettingsTest`: `Timezone.LOCAL` follows the browser, a UI locale change is ignored

### E2E Tests
- [x] `auto-browser-timezone.spec.js`: the browser time zone becomes the calendar time zone on server and client
- [x] `date-format.spec.js`: day dates sent to the server are `yyyy-MM-dd` in a named time zone, also for a time that is not midnight (25-hour DST day). The end of an all-day entry without end is the next day on the 25-hour DST day (#274)
- [x] `stable-class-names.spec.js`: day numbers in the Japanese locale

---

## Related FullCalendar Docs

- [locale](https://fullcalendar.io/docs/locale)
- [timeZone](https://fullcalendar.io/docs/timeZone)
- [firstDay](https://fullcalendar.io/docs/firstDay)

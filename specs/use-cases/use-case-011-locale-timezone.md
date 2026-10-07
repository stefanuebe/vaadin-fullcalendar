# UC-011: Locale and Timezone

**As a** Vaadin application developer, **I want to** set the calendar's locale and timezone **so that** dates, times, and labels are displayed correctly for the user's region.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.LOCALE`, `Option.TIMEZONE`, `Option.FIRST_DAY`, `Option.WEEK_NUMBER_CALCULATION`, `Option.DIRECTION`
**Related Events:** `BrowserTimezoneObtainedEvent`

---

## User-Facing Behavior

- The calendar displays dates, day names, and time formats according to the configured locale
- The first day of the week adapts to the locale (Monday in DE, Sunday in US) — or can be overridden
- All entry times are converted to the configured timezone for display
- Right-to-left (RTL) layout is supported via `DIRECTION = "rtl"`
- The browser's timezone is detected on first load and reported via `BrowserTimezoneObtainedEvent`

---

## Java API Usage

```java
// Set locale
calendar.setOption(Option.LOCALE, Locale.GERMAN);

// Set timezone
calendar.setOption(Option.TIMEZONE, new Timezone(ZoneId.of("Europe/Berlin")));
// or as zone id, stored as Timezone
calendar.setOption(Option.TIMEZONE, "Europe/Berlin");
// or UTC
calendar.setOption(Option.TIMEZONE, Timezone.UTC);
// read, empty while not set
Optional<Timezone> timezone = calendar.getOption(Option.TIMEZONE);

// Override first day of week
calendar.setOption(Option.FIRST_DAY, DayOfWeek.MONDAY);

// RTL layout
calendar.setOption(Option.DIRECTION, "rtl");

// Detect browser timezone
calendar.addBrowserTimezoneObtainedListener(event -> {
    Timezone browserTz = event.getTimezone();
});

// Follow the browser time zone and the UI locale (set on attach, follows UI.setLocale)
FullCalendar calendar = new FullCalendar().withAutoBrowserTimezone().withAutoUiLocale();
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Locale affects day names, month names, date formatting, and first day of week |
| BR-02 | `FIRST_DAY` overrides the locale's default first day |
| BR-03 | Timezone converts all entry times for display — the Java model stores `LocalDateTime` (UTC-normalized). Setters accept `LocalDateTime`, `Instant`, or `LocalDate`. |
| BR-04 | `BrowserTimezoneObtainedEvent` fires once on initial load |
| BR-05 | `WEEK_NUMBER_CALCULATION` can be `"locale"` or `"ISO"` |
| BR-06 | A calendar created without a locale gets `CalendarLocale.getDefaultLocale()`, so `getOption(Option.LOCALE)` is never empty unless the locale is removed explicitly |
| BR-07 | `Option.TIMEZONE` stores a zone id as `Timezone`, so `getOption(Option.TIMEZONE)` always returns a `Timezone`. `getTimezone()` is deprecated. `"local"` is rejected with an `IllegalArgumentException` |
| BR-08 | `withAutoBrowserTimezone()` sets the time zone the browser reports, also when it is already known. `withAutoUiLocale()` sets the UI locale on attach and on every `UI.setLocale`. A later explicit `setOption` applies until the next report or locale change |

---

## Acceptance Criteria

- [ ] Setting locale to DE shows German day/month names
- [ ] First day of week changes with locale (or `FIRST_DAY` override)
- [ ] Timezone conversion displays correct times
- [ ] `BrowserTimezoneObtainedEvent` fires with correct browser timezone
- [ ] RTL direction renders calendar right-to-left *(manual verification)*
- [ ] Week numbers use configured calculation method

---

## Tests

### Unit Tests
- [ ] `CalendarLocaleTest` — locale handling
- [ ] `TimezoneTests` — timezone conversion
- [x] `TypedOptionValuesTest`: zone id stored as `Timezone`, `"local"` rejected
- [x] `AutoBrowserSettingsTest`: `withAutoBrowserTimezone()`, `withAutoUiLocale()`

### E2E Tests
- [x] `auto-browser-timezone.spec.js`: the browser time zone becomes the calendar time zone on server and client
- [x] `date-format.spec.js`: day dates sent to the server are `yyyy-MM-dd` in a named time zone, also for a time that is not midnight (25-hour DST day)
- [x] `stable-class-names.spec.js`: day numbers in the Japanese locale

---

## Related FullCalendar Docs

- [locale](https://fullcalendar.io/docs/locale)
- [timeZone](https://fullcalendar.io/docs/timeZone)
- [firstDay](https://fullcalendar.io/docs/firstDay)

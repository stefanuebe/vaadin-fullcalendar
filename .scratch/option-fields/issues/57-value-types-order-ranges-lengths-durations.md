# 57: Value types: entry order, ranges, week number calculation, lengths, durations

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, `bc41bab0`) |
| Blocked by | `24-value-types-auto-false-now`, `58-pr-363-review-round-1` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Value types for the multi-form options the review of 24 found in the batches, which none of 21–24 covers (forms from the `<dt>Type</dt>` blocks of `Option` and `SchedulerOption`):

- `eventOrder` (28): a string of property names, an array of names and functions, or a compare function (`JsCallback`).
- `validRange`, `visibleRange` (25): a `DateRange`, an object with `start` and/or `end` (`validRange` may be open-ended), or a `JsCallback` returning such an object.
- `weekNumberCalculation` (25): `"local"`, `"ISO"` (the enum `WeekNumberCalculation`), or a `JsCallback` returning the week number.
- `resourceColumnsWidth` (39): a number of pixels or a CSS width string. Check whether a shared length type with `HeightValue` (24) fits. Decided: its own `WidthValue`, see *Decisions*.
- `toolbarElements` (30): a map of element name to content, where each content is a text or a `JsCallback` (the kinds of `ContentValue`). Decide whether a map of `ContentValue` with its own converter covers it.
- `eventConstraint`, `selectConstraint` (27): a fixed period with `start` and `end`, as a further form of `ConstraintValue` from 23. The library parses constraint objects as entries (`normalizeConstraint`), so the dates follow the convention of entry dates (UTC, as `NowValue.DateTime` in 24); an array may mix periods and recurring windows.
- Text options (`moreLinkText`, `moreLinkHint`, `navLinkHint`, `viewHint` and the other hints with a callback form, batch 29): the library types them as `string | (…) => string`, never `{html}`. They get their own value type `TextValue permits TextContent, JsCallback` instead of `ContentValue` (22 had reused it), so the `{html}` form below does not reach them.
- Render-hook content (`ContentValue`, 21): the library also takes `{html: '…'}`, which it sets as the element's inner HTML (`fullcalendar/chunks`, `dangerouslySetInnerHTML`), as a further form beside `TextContent` and `JsCallback`. Its factory is named `ofHtml`, and its Javadoc says the HTML is not escaped, so it must not carry unchecked user input. The `{domNodes: […]}` form is left out for a technical reason: DOM nodes cannot come from the server; a `JsCallback` can return them. Found by the review of 21–24 together (2026-10-15).
- Durations (`dateIncrement`, `duration`, `nextDayThreshold`, `slotDuration`, `snapDuration`, `slotHeaderInterval` and the other duration options in 25 and 26): decide whether `java.time.Duration` covers all forms or whether a form with calendar units (months, years, as in `dateIncrement: {months: 1}`) is needed.

Goal: full coverage of the library (maintainer, 2026-10-15). A form is left out only for a concrete technical reason. Besides the options below, check each value type of 21–24 against the library's `.d.ts` types and refiners for forms the old `Option` Javadoc did not list, and add them here.

The list below comes from a sweep of every non-deprecated `Type` block in `Option` and `SchedulerOption` (2026-10-15): all other options with more than one form are covered by 21–24, by a single Java type the old API already used (`Direction`, `DisplayMode`, `DayOfWeek`, `Locale`, a `Set<DayOfWeek>`, an `Integer` where `null` is the library default), or by their own tickets (`height` 20, `moreLinkClick` 19, `timeZone` 13–15, the toolbars 16).

Decide per option whether a value type is needed or one Java type covers all forms, and record the decision in the type's Javadoc, as in 24.

**Decisions (2026-10-16):**

- `OrderValue` = `Order` (a list of `Order.Key`: `Order.Property` or `JsCallback`, mixed as the library allows) or one `JsCallback`. `JsCallback` implements `Order.Key`, which is part of a form, not a value type. (Named `EntryOrder*` until 39 renamed them for the shared use with `resourceOrder`.)
- `DateRangeValue` = `DateRange` or `JsCallback`; `WeekNumberCalculationValue` = `WeekNumberCalculation` or `JsCallback`. `WeekNumberCalculation.ISO8601` is removed: an alias of `ISO` cannot round-trip (maintainer, spec *Removed API*, migration guide 56).
- `WidthValue` (pixels, percentage) is its own type: the library reads `resourceColumnsWidth` with `parseDimConfig`, which takes only a number or a string ending in `px` or `%` and drops any other CSS, while `HeightValue` takes any CSS length and `"auto"`. The percentage gets no short setter (`setX(25)` and `setX(25.0)` would differ only by the literal).
- `toolbarElements`: a `Map<String, ContentValue>` with `ToolbarElementsConverter`, no value type.
- `ConstraintValue.Windows` holds `ConstraintValue.Window`s: `BusinessHours` (now with `recurringStartDate`/`recurringEndDate`, the library's `startRecur`/`endRecur`, also for `businessHours`), `DateSpan` and `DateTimeSpan` (open on one side, as the library parses constraints with `allowOpenRange`). An RRule window needs `RRule` to round-trip first: ticket 61.
- `TextValue` = `TextContent` or `JsCallback`; `ContentValue` gets `ContentValue.Html` (`ofHtml`, not escaped).
- `DurationValue` = `Time` (`java.time.Duration`, written `"hh:mm:ss"`) or `Calendar` (`Period` plus a time, written as `{years, months, days, milliseconds}`), with `ofTimeString` for the library's string format, which rejects a string the library would ignore (maintainer). Milliseconds as a number are the same duration written another way, no form. `DurationConverter` reads its strings through `ofTimeString`, so the pattern exists once.
- The setter rule changed while writing this ticket (maintainer): no setter takes a form itself. The rows of this ticket follow it; 60 moves the existing fields.

Check of the value types of 21–24 against the library types (2026-10-16): a further form only for `BusinessHours`, which cannot end after midnight (ticket 62). Not forms of their own, because they are the same value written another way: epoch milliseconds and ISO strings with an offset for `now` (`NowValue.DateTime` is already an instant in UTC), fractional pixels for `height` (`HeightValue.ofCss("650.5px")`), `duration` instead of `endTime` in business hours. `className`, `groupId` and `display` of a business hours object style the non-business background, not the hours; left to a raw option.

**Blocked by:** `24-value-types-auto-false-now`, `58-pr-363-review-round-1`

**Status:** done (2026-10-16, `bc41bab0`)

- [x] Each type round-trips through its converter (`VALUE_TYPES` in `ConverterTest`) and has a switch in `ValueTypeSwitchTest`.
- [x] Each option above has a decided Java type, recorded in the ticket of its batch.
- [x] Each new value type, and `ContentValue` with its `{html}` form, has its row in the setter table of `README.md`.
- [x] A form added to an existing value type gets its setter on every field of that type that exists by then.

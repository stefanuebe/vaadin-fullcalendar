# 24: Value types: `"auto"`, `false` and date-or-date-time variants

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**Later change (2026-10-15):** the review of 21–24 together replaced `Hidden` with `ListTextValue.NONE` and `ListDayFormatValue.NONE`, renamed `EntryLimit` to `EntryLimitValue` and the factories to the `of`/`of<Form>` rule, see the README section "Value types (21–24, 57)".

**What to build:** Value types for: number or `"auto"` (`contentHeight`), boolean or `"auto"` (`tableHeaderSticky`, `footerScrollbarSticky`, `nowIndicatorSnap`), boolean, number or string (`slotHeaderSticky`), text or `false` (`listText`), format or `false` (`listDayAltFormat`, and `listDayFormat`, see the notes), date or date-time (`now`). Decide per option whether a value type is needed or one Java type covers all forms; record the decision in the type's Javadoc.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** done (2026-10-15)

- [x] Each type round-trips through its converter.

**Notes from the implementation (2026-10-15):**

Decision per option, also in the Javadoc of each type:

- `contentHeight`: `HeightValue` with `AUTO` (`"auto"`), `ofPixels(int)` (a number) and `ofCss(String)` (a CSS length such as `"50vh"`; the library types it as `CssDimValue`).
- `tableHeaderSticky`, `footerScrollbarSticky`, `nowIndicatorSnap`: one enum `AutoBoolean` (`TRUE`, `FALSE`, `AUTO`) covers all forms, so no sealed type. It has its own converter.
- `slotHeaderSticky`: `StickyValue` with `NOT_STICKY` (`false`), `STICKY` (`true`) and `offset(String)`. The scheduler's timeline uses a number or string as the CSS offset of the sticky header from the edge (`fullcalendar-scheduler/chunks/fc014b1a.js`, `edgeCoord`). A number is a pixel length, so one string form (`"10px"`) covers both; the converter reads a number back as such a string.
- `listText` (`Hidden` later replaced by `ListTextValue.NONE`, see the note at the top): `ListTextValue permits TextContent, Hidden`. `false` shows buttons named by duration instead of a list button (`fc-docs/_docs-v7/views/list-view.md`). `Hidden` is a shared enum form for the library's `false`, implemented by the value types whose option takes `false` in that sense (`ListTextValue`, `ListDayFormatValue`), as `JsCallback` is for callbacks.
- Formats: the library builds every date format with `createFormatter` (object, command string or function). None of 21–23 had a format type, and `listDayAltFormat` needs one, so 24 adds it: `DateFormat` (a record with an immutable map of the format object's properties) and `DateFormatValue permits DateFormat, JsCallback` for nine of the eleven format options of batch 29; the other two follow. The command string is left out, and the Javadoc of `DateFormatValue` says why: it needs a formatting plugin (`context.cmdFormatter`) the add-on does not load. FullCalendar 7 dropped its Luxon and Moment packages, but format strings still work through `@fullcalendar/format-luxon3` or `@fullcalendar/format-moment`. The add-on does not ship either (maintainer, 2026-10-15): they would add Luxon or Moment to every user's bundle, and `DateFormat` and `JsCallback` give the same output.
- `listDayFormat` and `listDayAltFormat`: the library reads both with `createFalsableFormatter`, so both take `ListDayFormatValue permits DateFormat, JsCallback, Hidden` (`Hidden` later replaced by `ListDayFormatValue.NONE`).
- `now`: `NowValue permits NowValue.Day, NowValue.DateTime, JsCallback`. A date is sent as a date string, a date-time as UTC (with `Z`) like the start and end of entries, as the old `Option` API did; the library keeps the exact instant of a value with an offset, so the calendar shows it in its time zone. The Javadoc says to convert a wall time to UTC first. The callback form is kept from the old API.
- `JsCallback.isMarker` tells the marker apart from a format object, which is an object too.

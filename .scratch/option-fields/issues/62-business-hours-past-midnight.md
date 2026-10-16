# 62: Business hours past midnight

| | |
|---|---|
| GitHub | #380 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, edc70ff9) |
| Blocked by | `57-value-types-order-ranges-lengths-durations` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `BusinessHours` can end after midnight, such as 22:00 to 02:00, for `businessHours` and for the recurring windows of `eventConstraint`/`selectConstraint`.

The library reads `endTime` as a duration, not as a time of day, and takes the length of each occurrence as `endTime` minus `startTime` (recurring type in `fullcalendar/chunks`, `parse`). So `{startTime: '22:00', endTime: '26:00'}` is a window of four hours that crosses midnight. `BusinessHours` holds the end as a `LocalTime` and rejects an end before the start, so it ends at 24:00 at the latest. Found by the check of the value types of 21–24 against the library types while writing 57 (2026-10-16).

- `BusinessHours` takes an end after midnight, written as `endTime` past `24:00` (for example `"26:00"`), and reads it back.
- Decided (maintainer, 2026-10-16): ONE type for a time of day that may run past midnight, named for that concept, not for one use. `RecurringTime` is renamed to `ExtendedLocalTime` (maintainer offered `ExtendedTime` or `ExtendedLocalTime`; the second says it is a wall-clock time without zone like `LocalTime`, which it converts from and to, only allowed past 24:00) and moves to package `option`, and both the recurring times of `Entry` and the start and end of `BusinessHours` use it. Rejected: reusing `RecurringTime` under its name (a name for one use, against the concrete semantics of the other new types), and a second type of the same kind. A breaking rename is no argument in 8.0. The rename goes into the migration guide (56).
- The existing API (`end(LocalTime)`, `end(int)`, `getEnd()`) keeps working for ends up to 24:00. An end after midnight is passed and returned as `ExtendedLocalTime`; the breaking changes go into the migration guide (56).

**Blocked by:** `57-value-types-order-ranges-lengths-durations`

**Status:** done (2026-10-16, edc70ff9)

- [x] A window from 22:00 to 02:00 reaches the client as `startTime: "22:00"`, `endTime: "26:00"` and round-trips through `BusinessHoursValueConverter` and `ConstraintValueConverter`.

**Done (2026-10-16):** decided by the maintainer: `BusinessHours.getStart()`/`getEnd()` return `ExtendedLocalTime` (option A; rejected: keep `LocalTime` plus a second getter). Setters take `LocalTime` (seconds dropped, `LocalTime.MAX` = 24:00), `int` and `ExtendedLocalTime`. For a start after 24:00 set the end first. Found by the review: `Entry.isRecurring()` throws for a recurring time from 24:00 on, older than this ticket, follows in 69. 3 review rounds, gate green (669+175 unit, 446 E2E).

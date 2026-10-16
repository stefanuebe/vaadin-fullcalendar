# 62: Business hours past midnight

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `57-value-types-order-ranges-lengths-durations` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `BusinessHours` can end after midnight, such as 22:00 to 02:00, for `businessHours` and for the recurring windows of `eventConstraint`/`selectConstraint`.

The library reads `endTime` as a duration, not as a time of day, and takes the length of each occurrence as `endTime` minus `startTime` (recurring type in `fullcalendar/chunks`, `parse`). So `{startTime: '22:00', endTime: '26:00'}` is a window of four hours that crosses midnight. `BusinessHours` holds the end as a `LocalTime` and rejects an end before the start, so it ends at 24:00 at the latest. Found by the check of the value types of 21–24 against the library types while writing 57 (2026-10-16).

- `BusinessHours` takes an end after midnight, written as `endTime` past `24:00` (for example `"26:00"`), and reads it back.
- Maintainer (2026-10-16): the add-on already has `RecurringTime` (times past 24 hours, used by the recurring times of `Entry`); use it for the end, "could in theory be renamed to `ExtendedTime` or so" for such cases. Open: rename or not (my view: the name fits, business hours recur too, and a rename breaks released API). Ask the maintainer.
- The existing API (`end(LocalTime)`, `end(int)`, `getEnd()`) keeps working for ends up to 24:00. How an end after midnight is passed and returned is decided here; `BusinessHours` is released API, so a breaking change goes into the migration guide (56).

**Blocked by:** `57-value-types-order-ranges-lengths-durations`

**Status:** ready-for-agent

- [ ] A window from 22:00 to 02:00 reaches the client as `startTime: "22:00"`, `endTime: "26:00"` and round-trips through `BusinessHoursValueConverter` and `ConstraintValueConverter`.

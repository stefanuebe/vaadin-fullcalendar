# 69: `Entry.isRecurring()` with a recurring time past midnight

| | |
|---|---|
| GitHub | #381 |
| Labels | bug, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `62-business-hours-past-midnight` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `Entry.isRecurring()` checks the recurring start and end time through `getRecurringStartTimeAsLocalTime()` and `getRecurringEndTimeAsLocalTime()`, which throw a `DateTimeException` for a time of 24:00 or later (`ExtendedLocalTime.toLocalTime()`). An entry with only `setRecurringEndTime(ExtendedLocalTime.of(26, 0))` and no recurring days or dates therefore throws from `isRecurring()` instead of returning true. Check the `ExtendedLocalTime` fields for null instead. The code predates 62; found by its review (2026-10-16), read from the code, not run.

**Blocked by:** `62-business-hours-past-midnight`

**Status:** done (2026-10-16)

- [x] A unit test: an entry with only a recurring end time of 26:00 is recurring, and `isRecurring()` does not throw.

**Done (2026-10-16):** `isRecurring()` checks the two fields for null. Not changed, by decision of the review: `DemoDialog` and `PlaygroundDialog` call the throwing getters too, but their pickers cannot hold a time from 24:00 on and neither app creates such entries. 2 review rounds, gate green (670+175 unit, 446 E2E).

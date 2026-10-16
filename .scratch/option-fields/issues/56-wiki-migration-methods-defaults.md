# 56: Wiki: migration guide for removed methods and dropped defaults

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `51-remove-old-option-api` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Migration guide 7.x to 8.0: a table mapping each removed method to its replacement, every dropped default with the line that restores it, `LOCAL` as the new time zone default. From 57: the enum constant `WeekNumberCalculation.ISO8601` is gone, use `ISO` (same client value). On the local wiki branch `8.0`, written as the final state, reviewed per section with three agents, language by Opus. Spec: *Documentation*.

**Blocked by:** `51-remove-old-option-api`

**Status:** ready-for-agent

- [ ] Every method in *Removed API* and every dropped default appears with its replacement.

**Note from 66 (2026-10-16):** `Entry.getDisplayMode()` returns null while no display mode is set (was `AUTO`), and such an entry no longer sends `display`, so it follows the calendar's `entryDisplay`. `setDisplayMode(null)` removes the value (was: reset to `AUTO`). `DisplayMode.NONE` now really hides the entry (it sent null before, so the entry stayed visible).

**Note from 62 (2026-10-16):** `RecurringTime` is renamed to `ExtendedLocalTime` and moved to package `option` (used by `Entry.setRecurringStartTime`/`setRecurringEndTime` and `BusinessHours`). `BusinessHours.getStart()` and `getEnd()` return `ExtendedLocalTime` instead of `LocalTime`; the end of the day is 24:00 (was `LocalTime.MAX`), and an end may run past midnight (`end(26)`, `end(ExtendedLocalTime)`). `end(LocalTime.MAX)` still means 24:00.

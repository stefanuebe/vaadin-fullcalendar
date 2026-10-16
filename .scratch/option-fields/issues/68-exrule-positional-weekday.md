# 68: Excluded rules with a positional weekday

| | |
|---|---|
| GitHub | #382 |
| Labels | bug |
| Status | done (2026-10-16) |
| Blocked by | `61-constraint-rrule-window` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** first check, then fix if needed. `RRule.toJson()` writes a rule with a positional weekday (such as `-1fr`, the last Friday) as an iCal string. As an excluded rule (`exrule`) of an entry or of a constraint window (61), the rrule plugin runs `parseRRuleObject` on each `exrule`, which probably mis-reads a string, so the exclusion would not apply. Found by the review of 61 (2026-10-16) from the library code (`@fullcalendar/rrule/index.js`), not run in a browser.

**Blocked by:** `61-constraint-rrule-window`

**Status:** done (2026-10-16)

- [x] Playwright: a recurring entry with an excluded rule "last Friday of the month" does not show on those Fridays (shows whether the bug exists).
- [x] If it exists: the excluded rule reaches the plugin in a form it reads (for example the object form with the weekday written as the plugin expects), and the Playwright test passes.

**Done (2026-10-16):** the bug exists and was worse than expected: the string exrule crashed the calendar, no entry rendered (Playwright, before the fix). Excluded rules are now always written in the object form (`RRule.toExruleJson()`, used by `ExruleConverter` for entries and constraint windows), a positional weekday as `{weekday, n}` with Monday as 0. The main `rrule` keeps its string form, which the plugin reads with `rrulestr`. A raw rule cannot be an excluded rule (already rejected by `excludeRules`), and `RRule.fromJson` rejects a string excluded rule. The bug is older than 8.0, a backport to 7.x is the maintainer's call. 3 review rounds, gate green (677+175 unit, 448 E2E).

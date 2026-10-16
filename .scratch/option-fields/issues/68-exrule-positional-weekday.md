# 68: Excluded rules with a positional weekday

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | bug, needs-triage |
| Status | needs-triage (check first whether the bug exists) |
| Blocked by | `61-constraint-rrule-window` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** first check, then fix if needed. `RRule.toJson()` writes a rule with a positional weekday (such as `-1fr`, the last Friday) as an iCal string. As an excluded rule (`exrule`) of an entry or of a constraint window (61), the rrule plugin runs `parseRRuleObject` on each `exrule`, which probably mis-reads a string, so the exclusion would not apply. Found by the review of 61 (2026-10-16) from the library code (`@fullcalendar/rrule/index.js`), not run in a browser.

**Blocked by:** `61-constraint-rrule-window`

**Status:** needs-triage

- [ ] Playwright: a recurring entry with an excluded rule "last Friday of the month" does not show on those Fridays (shows whether the bug exists).
- [ ] If it exists: the excluded rule reaches the plugin in a form it reads (for example the object form with the weekday written as the plugin expects), and the Playwright test passes.

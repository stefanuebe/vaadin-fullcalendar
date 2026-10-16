# 59: Resource column classes in the `option` package, with internals for the scheduler

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `58-pr-363-review-round-1` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** 58 left the scheduler's resource column classes in the root package, because `FullCalendarScheduler` drives the component lifecycle of `ComponentResourceColumn` through package-private methods. The maintainer did not want the value types split over two packages (2026-10-16). `ResourceColumn`, `ResourceAreaColumn`, `ComponentResourceColumn` and `ComponentResourceAreaColumn` move to `…fullcalendar.option`. The lifecycle methods (`bind`, `unbind`, `createComponent`, `destroyComponent`, `destroyAllComponents`) become private and are reached through `ComponentResourceColumn.getInternals()`, which returns the public, clearly internal class `ComponentResourceColumn.Internals`, after the pattern of Vaadin's `UI.getInternals()`.

The options classes stay in the root package. The same pattern would need internals in both directions there (the calendar's maps and sync on one side, the options' constructor, JSON helpers and the time zone setter on the other) and a Lombok `@Delegate` exclude; the maintainer chose to keep them where they are (2026-10-16).

**Blocked by:** `58-pr-363-review-round-1`

**Status:** done (2026-10-16)

- [x] The four column classes are in `option`; scheduler, tests, Test views and demo views compile against it.
- [x] `FullCalendarScheduler` uses only `getInternals()` for the lifecycle; the methods on the column are private.
- [x] Review, gate, commit as for every ticket.

# 67: Compare every option with its field

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `42-coverage-check` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A review after all options, the scheduler's included, have their fields. Several agents compare EVERY `Option` and `SchedulerOption` constant in detail with its successor field:

- **Javadoc:** the main focus. The old Javadoc is the language frame, the facts come from the library docs (maintainer, 2026-10-16). Every factual statement of the new Javadoc is checked against the library docs, and a statement about the add-on against the add-on (`full-calendar.ts`, `FullCalendar`). Where the docs and the library code (`.d.ts`, chunks) disagree, the code wins, because it is what runs, and the case is recorded (wave 2 found two: the default header formats of `dayHeaderFormat`, and the argument of a format callback, which is an info object and not a date). Parts of the old Javadoc were written for an older library version and were wrong for the used one (wave 2: `moreLinkHint` sets `title`, not `aria-label`; a newline in `allDayText` is no line break; `now` takes effect again after a detach and attach).
- **Values:** does the field take every value and form the library takes for the option, and nothing it rejects? Does the client receive the value as the library reads it?
- **Mapping:** where the library form cannot be mapped 1:1, is the reason concrete and written down (ticket and Javadoc)?

Options the add-on handles through Vaadin itself (for example the nav link click handlers, `moreLinkClick`, `viewDidMount`, `height`) are left out, as far as they are covered by their own tickets.

Asked for by the maintainer (2026-10-16), after the review of wave 1: a human cannot review the field batches option by option, so this review does it once for all options together.

**Blocked by:** `42-coverage-check`

**Status:** ready-for-agent

- [ ] Every option constant is checked on all three points, the result is recorded per option (in this ticket or a file beside it).
- [ ] Every finding is fixed, or recorded with its reason as a deliberate gap.
- [ ] The list of options that cannot be mapped 1:1 goes to the maintainer.

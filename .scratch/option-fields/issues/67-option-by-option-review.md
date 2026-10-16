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

- **Javadoc:** is every statement of the old Javadoc that is still true for the used library version carried over? Is every statement of the new Javadoc true, checked against the library code (`.d.ts`, chunks), the library docs and the add-on (`full-calendar.ts`, `FullCalendar`)? The old Javadoc is not trusted blindly, because parts of it were written for an older library version.
- **Values:** does the field take every value and form the library takes for the option, and nothing it rejects? Does the client receive the value as the library reads it?
- **Mapping:** where the library form cannot be mapped 1:1, is the reason concrete and written down (ticket and Javadoc)?

Options the add-on handles through Vaadin itself (for example the nav link click handlers, `moreLinkClick`, `viewDidMount`, `height`) are left out, as far as they are covered by their own tickets.

Asked for by the maintainer (2026-10-16), after the review of wave 1: a human cannot review the field batches option by option, so this review does it once for all options together.

**Blocked by:** `42-coverage-check`

**Status:** ready-for-agent

- [ ] Every option constant is checked on all three points, the result is recorded per option (in this ticket or a file beside it).
- [ ] Every finding is fixed, or recorded with its reason as a deliberate gap.
- [ ] The list of options that cannot be mapped 1:1 goes to the maintainer.

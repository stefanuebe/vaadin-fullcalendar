# 71: Constraints limited to resources

| | |
|---|---|
| GitHub | – |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `61-constraint-rrule-window` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** In a resource view, `eventConstraint` and `selectConstraint` can limit dragging and selecting to resources. A constraint object is parsed as an entry (`normalizeConstraint` → `parseEvents` in `fullcalendar/chunks/642eba18.js`), so the scheduler reads its `resourceId` and `resourceIds` and refuses every other resource (`filterConstraints` in `fullcalendar-scheduler/chunks/0a9e97be.js`); docs `events/eventConstraint.md`, section *Resources*. No window form of `ConstraintValue` carries resource ids. Found by 42 (2026-10-16). Decide with the maintainer where the resource ids go (a scheduler form, or resource ids on the windows), following *Value types* in `README.md`.

**Blocked by:** `61-constraint-rrule-window`

**Status:** ready-for-agent

- [ ] A constraint limited to resources reaches the client with `resourceIds` (and `resourceId` if the form offers it), alone or with a time window.
- [ ] The setter table in `README.md` has the new form or setter; the converter tests include it.

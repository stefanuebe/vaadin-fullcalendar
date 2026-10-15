# 03: Pending options are sent before a direct client call

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `01-raw-option-sync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `flushOptions()` runs the sync at once. Every method that calls the client directly (`gotoDate`, `changeView`, `next`, `previous`, `today`, `scrollToTime`, `render`, entry sources, the reattach restore) calls it first, so the call runs under the new options. `flushOptions()` reads the `initialStateSent` flag and whether the calendar is attached. `render` flushes too. The calendar has no `select` method, only `unselect`, which no option affects. Draggables, entry reverts and entry refreshes do not flush. The first two read no calendar option, and the refreshes run in `beforeClientResponse` with the sync. Spec: *Sync* (R4), user story 33. The scheduler's resource re-add follows in 38.

**Blocked by:** `01-raw-option-sync`

**Status:** done (2026-10-15)

- [x] Browserless: an option set before `gotoDate` reaches the client before the `gotoDate` call, also in the request right after the first attach.
- [x] Playwright: a date increment set right before `next` is used by `next`. (A valid range set before `gotoDate` cannot show the order, because the library constrains the current date again when the valid range changes and renders only after all calls of a response. The browserless test covers the order for `gotoDate`.)

# 03: Pending options are sent before a direct client call

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `01-raw-option-sync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `flushOptions()` runs the sync at once. Every method that calls the client directly (`gotoDate`, `changeView`, `scrollToTime`, `select`, entry sources, the reattach restore) calls it first, so the call runs under the new options. `flushOptions()` reads only the `initialStateSent` flag. Spec: *Sync* (R4), user story 33. The scheduler's resource re-add follows in 38.

**Blocked by:** `01-raw-option-sync`

**Status:** ready-for-agent

- [ ] Browserless: an option set before `gotoDate` reaches the client before the `gotoDate` call, also in the request right after the first attach.
- [ ] Playwright: a valid range set right before `gotoDate` to a date outside it is respected.

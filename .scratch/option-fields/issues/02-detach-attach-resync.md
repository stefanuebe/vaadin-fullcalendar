# 02: A calendar keeps its options across detach, reattach and a new client element

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `01-raw-option-sync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** After a detach the next sync resends the current state, because Flow discards queued JavaScript calls on detach. An `initialStateSent` flag tells whether the client element was created from `initialOptions`; it is cleared on a detach not followed by an attach in the same round trip (checked by a `beforeClientResponse` on the UI node, see *Sync* in the spec). A non-initial attach schedules a sync. `restoreStateFromServer` restores only view and date. Covers `@PreserveOnRefresh`. Spec: *Sync*, user stories 15, 16.

**Blocked by:** `01-raw-option-sync`

**Status:** ready-for-agent

- [ ] Options changed while detached reach the client after the reattach, removals included.
- [ ] A reattach with no change still writes the current state.
- [ ] A detach and reattach within one round trip keeps the client element and still resyncs.
- [ ] Playwright: `detach-reattach.spec.js` passes; a new case reloads a `@PreserveOnRefresh` Test view and the options are still applied.

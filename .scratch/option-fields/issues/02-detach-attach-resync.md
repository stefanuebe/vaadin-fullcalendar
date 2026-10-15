# 02: A calendar keeps its options across detach, reattach and a new client element

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `01-raw-option-sync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** After a detach the next sync resends the current state, because Flow discards queued JavaScript calls on detach. An `initialStateSent` flag tells whether the client element was created from `initialOptions`; it is cleared on a detach not followed by an attach in the same round trip (checked by a `beforeClientResponse` on the UI node, see *Sync* in the spec). A non-initial attach schedules a sync. `restoreStateFromServer` restores only view and date. An attach to another UI than the last one also clears the flag, which covers `@PreserveOnRefresh`. Spec: *Sync*, user stories 15, 16.

**Blocked by:** `01-raw-option-sync`

**Status:** done (2026-10-15)

- [x] Options changed while detached reach the client after the reattach, removals included.
- [x] A reattach with no change still writes the current state.
- [x] A detach and reattach within one round trip keeps the client element and still resyncs.
- [x] Playwright: `detach-reattach.spec.js` passes; a new case reloads a `@PreserveOnRefresh` Test view and the options are still applied.

**Notes from the review of 01 (2026-10-15):**

- On a reattach, 01 sends `setOptions` as its own call before `restoreStateFromServer`, which now gets an empty options object. The options render outside `batchRendering`, and a new client element created from the property sees every non-primitive value as changed (`_optionOverrides` compares by identity), so callback markers are evaluated twice. Send the options inside the restore again, or drop its options parameter.

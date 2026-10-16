# 63: `viewDidMount` as a field through the sync hook

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `18-sync-hook-entry-did-mount`, `36-hooks-view-toolbar` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `viewDidMount` (global, `JsCallback`), contributed through the sync hook like `entryDidMount` (18), so the user's callback and the add-on's own handler both run. The client registers its own `viewDidMount` handler, which sends `ViewSkeletonRenderedEvent` to the server, and the calendar relies on it itself (the listener in the `FullCalendar` constructor). A plain field would break this: in the initial options the add-on handler overwrites the user's callback, and a later `setOptions` replaces the add-on handler, so the server stops getting the event.

Found by the review of 36 (2026-10-16), which took `viewDidMount` out of that batch. `viewWillUnmount` has no add-on handler and stays in 36.

**Blocked by:** `18-sync-hook-entry-did-mount`, `36-hooks-view-toolbar`

**Status:** ready-for-agent

- [ ] The user's callback runs, and `ViewSkeletonRenderedEvent` still reaches the server, set initially and later, and after the option is cleared.
- [ ] The field is in `SYNCED_APART` in `TypedOptionsSyncTest`, with tests of its own.
- [ ] Playwright: a Test view sets a `viewDidMount` callback and sees both the callback's effect and the server event on a view change.

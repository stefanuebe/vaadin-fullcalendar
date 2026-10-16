# 63: `viewDidMount` as a field through the sync hook

| | |
|---|---|
| GitHub | #383 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `18-sync-hook-entry-did-mount`, `36-hooks-view-toolbar` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `viewDidMount` (global, `JsCallback`), contributed through the sync hook like `entryDidMount` (18), so the user's callback and the add-on's own handler both run. The client registers its own `viewDidMount` handler, which sends `ViewSkeletonRenderedEvent` to the server, and the calendar relies on it itself (the listener in the `FullCalendar` constructor). A plain field would break this: in the initial options the add-on handler overwrites the user's callback, and a later `setOptions` replaces the add-on handler, so the server stops getting the event.

Found by the review of 36 (2026-10-16), which took `viewDidMount` out of that batch. `viewWillUnmount` has no add-on handler and stays in 36.

**Blocked by:** `18-sync-hook-entry-did-mount`, `36-hooks-view-toolbar`

**Status:** done (2026-10-16)

- [x] The user's callback runs, and `ViewSkeletonRenderedEvent` still reaches the server, set initially and later, and after the option is cleared.
- [x] The field is in `SYNCED_APART` in `TypedOptionsSyncTest`, with tests of its own.
- [x] Playwright: a Test view sets a `viewDidMount` callback and sees both the callback's effect and the server event on a view change.

**Notes from the implementation (2026-10-16):**

- Maintainer decision: a raw option replaces the add-on's own client handler completely ("absolute control"), also in the initial options. A developer who needs a server call then fires an own event, for example with the add-on's DOM events. Only a typed field runs after the handler. Rejected: chaining raw options too.
- The server hook cannot reach the client handler from a JS string, so the hook only marks the field's JsCallback marker with `__afterClientHandler: true` (`FullCalendar.CALLBACKS_AFTER_CLIENT_HANDLER`). Raw options are laid over after the hook and stay unmarked. The client (`evaluateOption` in `full-calendar.ts`) chains handler and callback for a marked value.
- `createInitOptions` now puts the add-on handlers first and the server options over them (`moreLinkClick` excluded, it keeps `setMoreLinkClickAction`). Before, a raw handler option in the initial options was silently overwritten.
- `TypedOptionsSyncTest.everyFieldForAnOptionWithAClientHandler_runsAfterIt` reads the keys of `createEventHandlers` and fails when a field for one of them is not in the set.
- `setRawOption` Javadoc says that a raw callback replaces the handler. The wiki tickets pick this up.

# 18: Protected sync hook, with `entryDidMount` as first user

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `05-options-object-first-fields` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A protected method lets the calendar contribute computed values to the sync; it runs in every sync after the fields and before the raw options and may add or replace keys. `entryDidMount` (`@JsonName("eventDidMount")`) uses it to merge the user callback with native entry listeners and the automatic entry id. Spec: *Sync* (R5), user stories 56, 57.

**Blocked by:** `05-options-object-first-fields`

**Status:** done (2026-10-15)

- [x] Browserless: the hook output for `entryDidMount` contains the user callback and the auto entry id.
- [x] A minimal subclass adds and replaces a key through the hook.
- [x] Playwright: `entryDidMount` and a native entry listener both run.

**Note from the review of 01 (2026-10-15):** `setRawOption("eventDidMount", …)` bypasses the merge with native entry listeners; the next `addEntryNativeEventListener` or reattach overwrites it with a direct client call. Decide which wins once the hook exists.

**Notes from the implementation (2026-10-15):**

- The hook is `protected void contributeOptions(Map<String, JsonNode> options)` on `FullCalendar`. `currentOptions()` calls it after the fields, the `local` rule of the time zone and the `views` object, before the raw options. A subclass can so change `views` too. An override calls `super` to keep `eventDidMount`.
- `optionsChanged()` is now `protected`, so a subclass in another package can schedule the sync when a value it contributes changes. Its Javadoc says so.
- `entryDidMount` is a `JsCallback` field in `CalendarOptions` (`eventDidMount` is global in the option-name resource). `setOption(Option.ENTRY_DID_MOUNT, …)` writes it. A value that is not a `JsCallback` clears it, as before.
- The merge no longer goes to the client as a direct `setOption` call. `addEntryNativeEventListener`, its removal and `setAutoProvideEntryIdOnClient` schedule the sync. The resend in `onAttach` is gone, because every attach syncs. With the entry id on (the default), every calendar now has `eventDidMount` in its initial options.
- The note from 01 is decided: a raw `eventDidMount` wins, also over later native listeners, because the hook runs before the raw options and nothing calls the client directly any more (`rawEventDidMount_winsOverTheMerge`).
- When nothing is left to merge, the key is missing and the sync sends a removal, so the client falls back to its own handlers (04).
- Playwright: `NativeEventListenerTestView` sets the callback with `options().setEntryDidMount`, and `native-event-listener.spec.js` checks the attribute and the click counter on the same entry. The separate click test is gone, it repeated that check (443 E2E tests now).
- Mutation check: without the hook call in the sync, 5 of the 8 tests in `OptionSyncHookTest` fail.

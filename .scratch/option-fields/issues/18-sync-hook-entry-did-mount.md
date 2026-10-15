# 18: Protected sync hook, with `entryDidMount` as first user

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `05-options-object-first-fields` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A protected method lets the calendar contribute computed values to the sync; it runs in every sync after the fields and before the raw options and may add or replace keys. `entryDidMount` (`@JsonName("eventDidMount")`) uses it to merge the user callback with native entry listeners and the automatic entry id. Spec: *Sync* (R5), user stories 56, 57.

**Blocked by:** `05-options-object-first-fields`

**Status:** ready-for-agent

- [ ] Browserless: the hook output for `entryDidMount` contains the user callback and the auto entry id.
- [ ] A minimal subclass adds and replaces a key through the hook.
- [ ] Playwright: `entryDidMount` and a native entry listener both run.

**Note from the review of 01 (2026-10-15):** `setRawOption("eventDidMount", …)` bypasses the merge with native entry listeners; the next `addEntryNativeEventListener` or reattach overwrites it with a direct client call. Decide which wins once the hook exists.

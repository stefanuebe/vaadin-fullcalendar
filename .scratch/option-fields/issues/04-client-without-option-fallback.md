# 04: The client keeps no option fallback of its own

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `01-raw-option-sync`, `02-detach-attach-resync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The client creates its calendar from `initialOptions` plus plugins and client-side handlers only. `_optionsWithoutServer` and the `initialJsonOptions` property go away; a removed option gets the library default, not a value the client remembered. Spec: *Sync* (third bullet).

**Blocked by:** `01-raw-option-sync`, `02-detach-attach-resync`

**Status:** done (2026-10-15)

- [x] `_optionsWithoutServer` and `initialJsonOptions` are gone from the client and the server.
- [x] Removing an option on the server gives the library default on the client.
- [x] Playwright: `option-reset.spec.js` is adjusted to the library defaults and passes.

**Notes from the implementation (2026-10-15):**

- The add-on's own event handlers stay as the client's only fallback, so a removed `viewDidMount` or `eventDidMount` gives the server its events back (spec *Sync* says so now).
- Until 08, the constructor JSON `views` reach the client through the `customViews` property, which holds all of `customCalendarViews`. The "only once" guard of `setCustomCalendarViews` is already the boolean flag.
- Until 14, a concrete constructor JSON `timeZone` is a raw option.
- Until 15, removing the time zone gives the browser's zone (the library default) while a new calendar still starts in UTC. `option-reset.spec.js` pins this with the browser zone `Asia/Tokyo`, see the note in 15.

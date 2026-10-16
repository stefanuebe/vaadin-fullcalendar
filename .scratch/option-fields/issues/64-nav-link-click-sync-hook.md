# 64: `navLinkDayClick` and `navLinkWeekClick` as fields through the sync hook

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `18-sync-hook-entry-did-mount`, `27-fields-interaction` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Fields `navLinkDayClick` and `navLinkWeekClick` (`JsCallback`, placement per `option-names.txt`), contributed through the sync hook like `entryDidMount` (18), so the user's callback and the add-on's own handler both run. The client registers its own handlers for both options in `createEventHandlers` (`full-calendar.ts`), which send the click to the server as `DayNumberClickedEvent` and `WeekNumberClickedEvent`. A plain field would break this, as described in 63. Because the add-on's handler is a function, the library does not change the view on a click, which the `navLinks` Javadoc (27) says.

Found by batch 27 (2026-10-16), which left both options out.

**Blocked by:** `18-sync-hook-entry-did-mount`, `27-fields-interaction`

**Status:** ready-for-agent

- [ ] The user's callback runs, and the click event still reaches the server, set initially and later, and after the option is cleared.
- [ ] Both fields are in `SYNCED_APART` in `TypedOptionsSyncTest`, with tests of their own.
- [ ] Playwright: a Test view sets a callback for a day link and sees both the callback's effect and the server event.

**Notes from 63 (2026-10-16):** 63 built the mechanism. Add both names to `FullCalendar.CALLBACKS_AFTER_CLIENT_HANDLER`; the guard test in `TypedOptionsSyncTest` fails until then. A raw option replaces the handler (maintainer decision, see 63). Both names are per view in `option-names.txt`. A per-view value goes through `views`, which the hook does not mark and the client does not chain, so it would replace the handler for that view. Decide between global placement (the spec makes calendar listeners global by the add-on's choice) and marking in `views`.

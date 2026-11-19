# 08: The server builds the complete `views` object

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `04-client-without-option-fallback`, `07-view-options` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The server merges custom views (`customCalendarViews`, including the anonymous views parsed from constructor JSON) and the per-view options into one `views` object, per view and per key. The client's `customViews` property and its merge in `createInitOptions` go away. `setCustomCalendarViews` only fills the server map; its "only once" guard becomes a boolean flag. A raw `views` replaces the generated object. `setViewSpecificOption` stays until 51 and goes into the generated `views` as its own layer, see the note below. Spec: *Sync* (second bullet), *Raw options*.

**Blocked by:** `04-client-without-option-fallback`, `07-view-options`

**Status:** done (2026-10-15)

- [x] A custom view keeps its definition after a later view value for the same view.
- [x] `clear()` on a custom view removes only the values set through `view(…)`.
- [x] Constructor JSON `views` become custom views.
- [x] A raw `views` replaces the generated `views`.
- [x] Playwright: a custom view with a per-view option renders with both.

**Note from the implementation (2026-10-15):** `setViewSpecificOption` keeps its own map until 51 and lies as a third layer over the definitions and the `view(…)` values, so for the same view and key the old API wins. `getOption("views")` still returns its values through the bridge map. Ticket 51 removes both.

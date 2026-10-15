# 08: The server builds the complete `views` object

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `04-client-without-option-fallback`, `07-view-options` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The server merges custom views (`customCalendarViews`, including the anonymous views parsed from constructor JSON) and the per-view options into one `views` object, per view and per key. The client's `customViews` property and its merge in `createInitOptions` go away. `setCustomCalendarViews` only fills the server map; its "only once" guard becomes a boolean flag. A raw `views` replaces the generated object. `setViewSpecificOption` writes into the view options until 51. Spec: *Sync* (second bullet), *Raw options*.

**Blocked by:** `04-client-without-option-fallback`, `07-view-options`

**Status:** ready-for-agent

- [ ] A custom view keeps its definition after a later view value for the same view.
- [ ] `clear()` on a custom view removes only the values set through `view(…)`.
- [ ] Constructor JSON `views` become custom views.
- [ ] A raw `views` replaces the generated `views`.
- [ ] Playwright: a custom view with a per-view option renders with both.

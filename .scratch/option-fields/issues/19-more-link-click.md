# 19: `moreLinkClick` as a field through the sync hook

| | |
|---|---|
| GitHub | #391 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 81181580) |
| Blocked by | `04-client-without-option-fallback`, `18-sync-hook-entry-did-mount`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `moreLinkClick`, value type `MoreLinkClickAction` or `JsCallback`, contributed through the hook so the server click handling keeps working. Check the client's `_moreLinkClickWithoutServer` against the dropped fallback (04) and remove what is no longer reached. Spec: *Sync* (R5).

**Note from the review of 21–24 (2026-10-15):** the value type follows the README section *Value types (21–24, 57)* and adds its row to the setter table. The library also takes a view name (`fc-docs/_docs-v7/events/moreLinkClick.md`), so the forms are the action, a view name (`ViewName`) and a `JsCallback`. The existing nested `FullCalendar.MoreLinkClickAction` and its `NOTHING` constant are checked against the rule for top-level forms; the decision is recorded here.

**Blocked by:** `04-client-without-option-fallback`, `18-sync-hook-entry-did-mount`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, 81181580)

- [x] Both value forms reach the client; the server still receives the more-link click.
- [x] Playwright: `MoreLinkClickTestView` behaviour unchanged.

**Notes from the implementation (2026-10-16):**

- Decision (maintainer): the action is the new nested enum `MoreLinkClickValue.Action` (POPUP, DAY, WEEK, NOTHING). Keeping `FullCalendar.MoreLinkClickAction` as a form does not work: the value type is sealed, and without `module-info` it can only permit types of its own package. The old enum is deprecated and goes with the old option API (51). NOTHING keeps the client value `"function"` as in 7.x; the library then calls `zoomTo` with an unknown view, which only changes the date (Javadoc says so).
- Forms: `MoreLinkClickValue.Action`, `ViewName`, `JsCallback`. Setters: the value type, `String` (view name), `CalendarView`; the README setter table has the row.
- The field is a plain field in the *Interaction* section. The client already routes the key `moreLinkClick` to `setMoreLinkClickAction` and keeps its own handler, so the server gets the click with every value; no sync-hook marking is needed. `_moreLinkClickWithoutServer` was already gone (04), nothing left to remove.
- Gate green: full build, E2E 464 passed.
- The converter reads a string that equals an action back as that action, any other as a view name. A view named like an action reads back as the action, which the library treats alike.
- `MoreLinkClickTestView` uses the field now; the new E2E test "view name set before attach" covers the view-name form.

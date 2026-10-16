# 19: `moreLinkClick` as a field through the sync hook

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `04-client-without-option-fallback`, `18-sync-hook-entry-did-mount`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `moreLinkClick`, value type `MoreLinkClickAction` or `JsCallback`, contributed through the hook so the server click handling keeps working. Check the client's `_moreLinkClickWithoutServer` against the dropped fallback (04) and remove what is no longer reached. Spec: *Sync* (R5).

**Note from the review of 21–24 (2026-10-15):** the value type follows the README section *Value types (21–24, 57)* and adds its row to the setter table. The library also takes a view name (`fc-docs/_docs-v7/events/moreLinkClick.md`), so the forms are the action, a view name (`ViewName`) and a `JsCallback`. The existing nested `FullCalendar.MoreLinkClickAction` and its `NOTHING` constant are checked against the rule for top-level forms; the decision is recorded here.

**Blocked by:** `04-client-without-option-fallback`, `18-sync-hook-entry-did-mount`, `60-setters-without-form-overloads`

**Status:** ready-for-agent

- [ ] Both value forms reach the client; the server still receives the more-link click.
- [ ] Playwright: `MoreLinkClickTestView` behaviour unchanged.

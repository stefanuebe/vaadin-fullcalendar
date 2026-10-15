# 19: `moreLinkClick` as a field through the sync hook

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `04-client-without-option-fallback`, `18-sync-hook-entry-did-mount` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `moreLinkClick`, value type `MoreLinkClickAction` or `JsCallback`, contributed through the hook so the server click handling keeps working. Check the client's `_moreLinkClickWithoutServer` against the dropped fallback (04) and remove what is no longer reached. Spec: *Sync* (R5).

**Blocked by:** `04-client-without-option-fallback`, `18-sync-hook-entry-did-mount`

**Status:** ready-for-agent

- [ ] Both value forms reach the client; the server still receives the more-link click.
- [ ] Playwright: `MoreLinkClickTestView` behaviour unchanged.

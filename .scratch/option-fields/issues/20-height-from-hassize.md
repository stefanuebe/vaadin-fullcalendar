# 20: `height` from `HasSize` through the sync hook

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `18-sync-hook-entry-did-mount` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `setHeight`/`setHeightFull` write the library option `height` through the hook. A raw `height` wins. The constructor skips `setHeightFull()` when its JSON has `height`, as on master. No `height` field (out of scope). Spec: *Height*, user story 41.

**Blocked by:** `18-sync-hook-entry-did-mount`

**Status:** ready-for-agent

- [ ] `setHeight` and the constructor's `setHeightFull()` reach the client.
- [ ] `setHeightFull()` is skipped when the constructor JSON has `height`.
- [ ] A raw `height` wins over `setHeight`.

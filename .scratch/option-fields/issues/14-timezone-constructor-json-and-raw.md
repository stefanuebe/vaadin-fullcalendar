# 14: Time zone from constructor JSON, raw time zone rejected

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `13-timezone-local-server` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Constructor JSON `timeZone` goes into the field (`"local"` becomes `LOCAL` with the flag set, a concrete id becomes that zone; new compared with master, which converted with UTC). `setRawOption("timeZone", …)` throws with a pointer to the field. Spec: *Raw options*.

**Blocked by:** `13-timezone-local-server`

**Status:** ready-for-agent

- [ ] Constructor JSON `"local"` gives `LOCAL`, `"Europe/Berlin"` gives that zone in the getter and the offset helpers.
- [ ] `setRawOption("timeZone", …)` throws and names `options().setTimeZone`.

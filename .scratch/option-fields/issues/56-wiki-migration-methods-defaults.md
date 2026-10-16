# 56: Wiki: migration guide for removed methods and dropped defaults

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `51-remove-old-option-api` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Migration guide 7.x to 8.0: a table mapping each removed method to its replacement, every dropped default with the line that restores it, `LOCAL` as the new time zone default. From 57: the enum constant `WeekNumberCalculation.ISO8601` is gone, use `ISO` (same client value). On the local wiki branch `8.0`, written as the final state, reviewed per section with three agents, language by Opus. Spec: *Documentation*.

**Blocked by:** `51-remove-old-option-api`

**Status:** ready-for-agent

- [ ] Every method in *Removed API* and every dropped default appears with its replacement.

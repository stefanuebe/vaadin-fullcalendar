# 55: Wiki: migration guide maps each `Option` constant to its field

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `51-remove-old-option-api` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A table in the migration guide 7.x to 8.0 mapping every `Option` and `SchedulerOption` constant (deprecated aliases included) to its field. Generate it from the code state before 51 or from git history. On the local wiki branch `8.0`, written as the final state, reviewed per section with three agents, language by Opus. Spec: *Documentation*.

**Blocked by:** `51-remove-old-option-api`

**Status:** ready-for-agent

- [ ] Every constant of both enums appears once.

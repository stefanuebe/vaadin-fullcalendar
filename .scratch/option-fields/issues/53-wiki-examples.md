# 53: Wiki: every other example uses the new API

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `51-remove-old-option-api` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Every wiki page outside `Calendar-configuration` with `setOption(Option.X, …)` or a removed method. This includes the pages already written on the branch `8.0` before the option fields (`Home`, `Getting-Started`, `Features`), not only the older ones. An option with several forms is shown with its short setter where the example holds a plain value, as the setter rules in `README.md` give them. On the local wiki branch `8.0`, written as the final state, reviewed per section with three agents, language by Opus. Spec: *Documentation*.

**Blocked by:** `51-remove-old-option-api`

**Status:** ready-for-agent

- [ ] `grep` over the wiki finds no removed API outside the migration guide and older release notes.

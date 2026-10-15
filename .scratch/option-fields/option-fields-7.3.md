# 7.3 and the option API

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement |
| Status | open, to discuss (maintainer, 2026-10-15) |
| Blocked by | `option-fields-spec.md` |

---

## Why

7.3 introduces the option API (`setOption(Option, …)`) and deprecates typed setters in its favour. 8.0 removes the option API again (`option-fields-spec.md`). 7.3 is not released and may still change, as long as nothing breaks against 7.2 (maintainer, 2026-10-15). It must not send its users to an API that 8.0 removes.

## To decide

- Only adjust deprecations and docs in 7.3 (no deprecation in favour of the option API), or backport the option fields to `v7_master` next to the existing API.
- What the 7.3 release notes and migration guide say about configuring a calendar.

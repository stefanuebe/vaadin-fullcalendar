# 78: Remove the deprecated resource area column classes

| | |
|---|---|
| GitHub | – |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17, 775c734f) |
| Blocked by | `51-remove-old-option-api` |
| Spec | – |

---

**What to build:** Delete `ResourceAreaColumn` and `ComponentResourceAreaColumn`, the 7.x names of `ResourceColumn` and `ComponentResourceColumn`, which 8.0 kept as deprecated subclasses (ticket 72). Decided by the maintainer (2026-10-17): the transition buys nothing, because both classes moved from the root package to `…fullcalendar.option` (tickets 58/59), so 7.x code has to change its imports anyway and can change the name in the same step; `setResourceAreaColumns`, which took them, is gone with ticket 51. Their tests and the spec mentions go with them. The migration guide (52–56) names both under the removed API, with `ResourceColumn` and `ComponentResourceColumn` as replacements. The guide on the wiki branch `8.0` still calls them deprecated and compiling (section on resource columns, with the `AbstractResourceColumn<?>` example and the width string), which 52–56 correct.

**Blocked by:** `51-remove-old-option-api`

**Status:** done

- [x] Nothing in the repository references `ResourceAreaColumn` or `ComponentResourceAreaColumn`, apart from the tickets that record their history.
- [x] Full gate green: `mvn clean install`, then the E2E run in `e2e-test-app`.

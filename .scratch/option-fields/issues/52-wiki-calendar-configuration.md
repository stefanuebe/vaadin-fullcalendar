# 52: Wiki: `Calendar-configuration` describes the options object

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `51-remove-old-option-api` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Rewrite the page around `options()`, `options().view(…)`, value-or-callback setters, `setRawOption` as an expert feature (on `options()` and per view on `options().view(…)`, 58) and the time zone default `LOCAL`. The wiki page *Glossary* gets the term *Raw option* that `CONTEXT.md` defines (58). On the local wiki branch `8.0`, written as the final state, reviewed per section with three agents, language by Opus. Spec: *Documentation*.

**Blocked by:** `51-remove-old-option-api`

**Status:** ready-for-agent

- [ ] No `setOption` left on the page.
- [ ] Every example compiles against the branch.
- [ ] The page explains why an option with several forms has two ways to set a form: the setter taking the value type (`setEntryOverlap(Overlap.FORBIDDEN)`, `setDayHeaderFormat(DateFormat.of(…))`) and the short setter per form (`setEntryOverlap(false)`, `setDayHeaderFormat(Map.of(…))`). The short one saves a conversion when the app already holds the plain value, the value type names the meaning (maintainer, 2026-10-15).

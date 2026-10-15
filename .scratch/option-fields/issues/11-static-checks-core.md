# 11: Static checks on the core option classes

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `05-options-object-first-fields`, `07-view-options`, `09-option-names-core` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Unit tests without a calendar instance, over `CalendarOptions` and `ViewOptions`: every field's JSON name is in the option-name resource; global fields are in `CalendarOptions` and per-view fields in `ViewOptions` as the resource flags say; no field name contains "event" (case-insensitive; `@JsonName` may); no field name is a Java keyword and no options method overrides an `Object` method; every field has an `@see` tag in its Javadoc (source scan). Spec: *Testing Decisions*, static checks; user stories 51, 52, 55.

**Blocked by:** `05-options-object-first-fields`, `07-view-options`, `09-option-names-core`

**Status:** ready-for-agent

- [ ] Each check fails when its rule is broken on purpose (shown once while writing the test, not committed).
- [ ] The checks pass on the fields of 05 and 06.

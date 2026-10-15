# 11: Static checks on the core option classes

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `05-options-object-first-fields`, `07-view-options`, `09-option-names-core` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Unit tests without a calendar instance, over `CalendarOptions` and `ViewOptions`: every field's JSON name is in the option-name resource; global fields are in `CalendarOptions` and per-view fields in `ViewOptions` as the resource flags say; no field name contains "event" (case-insensitive; `@JsonName` may); no field name is a Java keyword and no options method overrides an `Object` method; every field has an `@see` tag in its Javadoc (source scan). Spec: *Testing Decisions*, static checks; user stories 51, 52, 55.

**Blocked by:** `05-options-object-first-fields`, `07-view-options`, `09-option-names-core`

**Status:** done (2026-10-15)

- [x] Each check fails when its rule is broken on purpose (shown once while writing the test, not committed).
- [x] The checks pass on the fields of 05 and 06.

**Notes from the implementation (2026-10-15):**

- The checks are in `OptionFieldsStaticTest`. Each was shown to fail once with its rule broken on purpose (a field missing from the resource, a per-view field in `CalendarOptions`, a field named `eventColor`, `toString()` in `ViewOptions`, a field without `@see`, a field without Javadoc after a method, `class` added to the resource, a second field with `@JsonName("weekends")`), then reverted. A field with an array type, annotations with array values and `{@link}` in its Javadoc passes.
- Added beyond the ticket's list, from the review: no two fields share a JSON name. The sync puts the fields into one map, so one of them would silently be lost.
- The field walk includes superclasses, as the sync does. The scheduler copy of these checks in 37 checks only the fields the scheduler classes declare, because the scheduler tests do not see the core resource on their classpath.
- The keyword check runs over the library names of the resource, with "event" written as "entry", not over the field names. The compiler already rejects a field named like a keyword, so a check over field names could never fail. Over the resource it fails when a library update brings a name that needs another field name and an `@JsonName`. That is the case user story 52 is after. It is skipped for a name that a field already carries as `@JsonName`.
- The `Object` method check covers `CalendarOptions`, `ViewOptions`, `FullCalendarOptions` and `FullCalendarViewOptions` (their declared methods, the Lombok ones included).
- Not here: that every field has an accessor on the options objects (12 pins their method lists), and that every resource name has a field (42).

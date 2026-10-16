# 60: Remove the setters that take a form itself

| | |
|---|---|
| GitHub | #364 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, eb1c4692) |
| Blocked by | `57-value-types-order-ranges-lengths-durations` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The existing fields follow the changed setter rule in `README.md` (*Field batches*, setters). A setter that takes a form itself (`JsCallback`, an enum form such as `HeaderAlign`, a record form such as `DateRange`) repeats the setter taking the value type, because the form implements the value type. The maintainer decided on 2026-10-16, while 57 was written, to drop these setters and keep only those that take plain data (`String`, `int`, `LocalDate`, …), which save the user a conversion.

Found on 2026-10-16, the setters to remove:

- `ViewOptions.setDayCellClass(JsCallback)`
- `ViewOptions.setAllDayHeaderContent(JsCallback)`
- `ViewOptions.setDayHeaderAlign(HeaderAlign)` and `setDayHeaderAlign(JsCallback)`

`setEntryDidMount(JsCallback)` stays: its field has the one form `JsCallback` and keeps its Lombok setter.

Callers move to the setter taking the value type, which takes the same argument unchanged (tests, `e2e-test-app`, demo). The rows of the old value types in `PER_FORM_PARAMETERS` in `TypedOptionsSyncTest` follow the setter table in `README.md`, and the pinned method lists (`*-api.txt`) lose the removed setters.

The batch tickets 16, 19, 20, 25–36 and 39–41 wait for this ticket, so no batch writes a setter the rule no longer has.

**Blocked by:** `57-value-types-order-ranges-lengths-durations`

**Status:** done (2026-10-16, eb1c4692)

- [x] No options class has a setter that takes a `JsCallback`, an enum form or a record form of a multi-form value type.
- [x] `PER_FORM_PARAMETERS` matches the setter table in `README.md` row for row.
- [x] The pinned method lists are updated through `OptionsApiTest`.

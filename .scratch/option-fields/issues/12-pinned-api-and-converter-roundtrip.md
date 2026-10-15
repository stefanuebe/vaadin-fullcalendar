# 12: Pinned public API of the options objects and converter round trip

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `06-value-or-callback-day-cell-class`, `07-view-options` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A test pins the list of methods `FullCalendarOptions` and `FullCalendarViewOptions` expose and checks that neither has a public constructor, so no builder or helper leaks. A second test runs every value type through its converter and back. Both grow with each field batch. Spec: *Testing Decisions*, user story 53.

**Blocked by:** `06-value-or-callback-day-cell-class`, `07-view-options`

**Status:** done (2026-10-15)

- [x] Adding a public method to an options object without updating the pinned list fails the test.
- [x] The round trip covers `ClassNameValue`.

**Notes (2026-10-15):**

- `OptionsApiTest` compares the public declared methods of each options class, as `name(ParameterTypes): ReturnType` sorted, with the test resource `<class name>-api.txt`. The failure message names the file to update.
- `ConverterTest` keeps every value type in `VALUE_TYPES`, with its converter and a value of each form. `everyValueType_roundTripsThroughItsConverter` runs them there and back, also for a type that no field uses yet. `everyTypeJsCallbackImplements_isAValueType` keeps the list complete for the value-or-callback types. `everyValueTypeField_usesTheConverterOfItsType` takes every field of the data classes (superclasses included) that has a `@JsonConverter` or a sealed type: its type must be in `VALUE_TYPES` with the same converter, unless it is in `ONE_FORM_TYPES` (a converter for an option with one form, such as `DayOfWeek`). `valueTypeSamples_coverEveryForm` checks that the samples contain each permitted subtype and each enum constant.
- The `__jsCallback` marker is read by `JsCallback.fromMarkerJson`, next to `toMarkerJson`. Every value-type converter uses it.
- Scheduler: the core test classes and resources are not on the scheduler's test classpath, so 37 adds its own pinned lists and round trip over the fields the scheduler data classes declare.

# setOption rejects values of the wrong type

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | on hold (2026-10-15): the maintainer prefers options as typed fields, see `option-fields-design.md`. Decided after its prototype. Target 8.0 stays |
| Blocked by | – |

---

## Problem Statement

`setOption(Option, Object)` takes any value. Which Java types an option accepts is written only in the Javadoc of the constant. `calendar.setOption(Option.TIMEZONE, new Person())` compiles and runs. The mistake shows up later in the browser, as a wrong rendering or a JavaScript error, far away from the line that caused it.

## Solution

Every constant of `Option` and `SchedulerOption` declares the Java types it accepts, the same types its Javadoc names under *Type*. `setOption` checks the value against them and throws an `IllegalArgumentException` when it matches none. The message names the option, the type of the value and the accepted types.

Compile-time safety (a generic option key instead of an enum) was considered and left for a later major version, because it breaks every use of `Option` as an enum. Setters and getters per option were rejected: about 900 methods for about 356 options.

## Implementation Decisions

- The accepted types live on the constant, next to its converters, for example as an annotation. A value matches when it is an instance of one of them.
- The check runs in `setOption(Option, Object)` and `setViewSpecificOption(…, Option, Object)`, before the converters.
- `null` is never checked, because it removes the option.
- Options set with a string key stay unchecked. They are the way to pass anything the add-on does not know.
- Deprecated aliases declare the types of the option they stand for.

## Acceptance criteria

- [ ] Every constant of `Option` and `SchedulerOption` declares its accepted types, and they match its Javadoc.
- [ ] A test fails when a constant declares no types.
- [ ] `setOption(Option.TIMEZONE, new Object())` throws an `IllegalArgumentException` whose message names `TIMEZONE`, `Object` and the accepted types.
- [ ] Values of every accepted type still pass, including `JsCallback` where the Javadoc allows it. A test covers one option per kind of union type.
- [ ] `setOption(option, null)` and `setOption(String, …)` behave as before.
- [ ] The release notes of the target version and `Calendar-configuration` in the wiki mention the check.

## Further notes

Raised by the maintainer on 2026-10-15 while reviewing the option API documentation. Check the existing tests for values that only worked by accident, such as a raw `JsonNode` passed to a typed option. Such a value now fails, so the release notes must say so if the check goes into 8.0.

# 22: Value types for the other value-or-callback options

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**Later change (2026-10-15):** the review of 21–24 together moved the text and hint options (`moreLinkText`, the hints) from `ContentValue` to their own `TextValue` from 57, and added a setter taking the value type to every value-type field; see the README section "Value types (21–24, 57)".

**What to build:** Sealed value types for: boolean or callback (`eventOverlap`, `selectOverlap`), CSS selector or callback (`dropAccept`), view name or callback (`navLinkDayClick`, `navLinkWeekClick`), text or callback (`moreLinkText`). Reuse one type where two options take the same kinds. The fields themselves come in the batches.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** done (2026-10-15)

- [x] Each type round-trips through its converter.
- [x] A test switches over each type without `default`.

**Notes from the implementation (2026-10-15):**

- New types, each with a converter that converts both ways:
  - `OverlapValue permits Overlap, JsCallback` for `eventOverlap` and `selectOverlap`. `Overlap` is an enum `ALLOWED`/`FORBIDDEN` with `Overlap.of(boolean)`.
  - `CssSelectorValue permits CssSelector, JsCallback` for `dropAccept`. `CssSelector` is a record with `selector`.
  - `ViewNameValue permits ViewName, JsCallback` for `navLinkDayClick` and `navLinkWeekClick`. `ViewName` is a record with `name`, created from a string or a `CalendarView`. A `CalendarView` itself cannot be the form: the converter could not tell a custom view back from its name.
- `moreLinkText` reuses `ContentValue` (`TextContent` or `JsCallback`). The two kinds are the same, so a separate text type would duplicate it.
- `ValueTypeSwitchTest` switches over every value type without `default`. `ConverterTest` keeps the value types with their converters in `VALUE_TYPES` and round-trips each, also before a field uses it.

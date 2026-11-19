# 21: Value types for render hooks: content and header alignment

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Sealed value types for the render-hook kinds still missing: content (text or `JsCallback`) and header alignment (`HeaderAlign` or `JsCallback`), with converters and a pattern for the setter overloads the batches copy. No fields yet beyond one each to prove the type. Spec: *Options with more than one value form*.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** done (2026-10-15)

- [x] Each type round-trips through its converter (12).
- [x] One field per type (`allDayHeaderContent`, `dayHeaderAlign`) reaches the client in both forms.

**Notes from the implementation (2026-10-15):**

- New types: `ContentValue permits TextContent, JsCallback` (`TextContent` is a record with `text`, shown as text, not HTML) and `HeaderAlignValue permits HeaderAlign, JsCallback` (the existing enum `HeaderAlign` implements it). `JsCallback` implements all three sealed interfaces.
- Converters `ContentValueConverter` and `HeaderAlignValueConverter` switch over the sealed type and also implement `toServerModel`, so `ConverterTest` runs both forms there and back. `ClassNameValueConverter` has no `toServerModel` yet; 12 adds it with the general round-trip test.
- The pattern the batches copy, as in `ViewOptions`: the field has the sealed type and `@JsonConverter`. One hand-written setter per form, each with Javadoc that links to the getter. Lombok generates no setter for the sealed type, because a method with that name already exists.
- Fields: `allDayHeaderContent` and `dayHeaderAlign`, both per view in the option-name resource, so in `ViewOptions`.
- Playwright: the third calendar of `TypedOptionsTestView` (week view) shows both content forms in the all-day header. For the alignment the test reads the library's option value, because its alignment class names are internal to the library.
- Mutation check: a converter that sends a wrong text or alignment fails the round trip and the sync tests.
- For 12 (from the review): the marker key `__jsCallback` is now parsed in two converters and written in `JsCallback.toMarkerJson`. When 12 adds `toServerModel` to `ClassNameValueConverter`, move the parsing next to `toMarkerJson` instead of copying it a third time.

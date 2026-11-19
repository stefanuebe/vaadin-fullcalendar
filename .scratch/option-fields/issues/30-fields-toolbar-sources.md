# 30: Fields: toolbar parts and entry sources

| | |
|---|---|
| GitHub | #390 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, a57235f3) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `16-header-toolbar-and-locale-initial-values`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (13), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `buttons`, `buttonDisplay`, `footerToolbarClass`, `headerToolbarClass`, `toolbarElements`, `eventDataTransform`, `endParam`, `googleCalendarApiKey`, `startParam`, `eventSourceSuccess`, `timeZoneParam`, `lazyFetching`, `loading`.

**Value types from 57 (2026-10-16):** `toolbarElements` is a `Map<String, ContentValue>` with `ToolbarElementsConverter`; add the field type and converter to `ONE_FORM_TYPES` in `ConverterTest`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `16-header-toolbar-and-locale-initial-values`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, a57235f3)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Notes from the implementation (2026-10-16):**

- Decision (maintainer): `buttons` is a `Map<String, Map<String, Object>>` as in 7.x, no value type. The setter keeps an unmodifiable copy of the outer and the inner maps. Its values go through `JsonUtils.toJsonNode`, so `String`, `Boolean`, numbers, `NativeToolbarButtonDisplay` and `JsCallback` work; content types such as `TextContent` are not converted (Javadoc says so).
- `toolbarElements` is a `Map<String, ContentValue>` with `ToolbarElementsConverter`, its setter keeps an unmodifiable copy.
- Deviation from the note above: `toolbarElements` is not in `ONE_FORM_TYPES`, because two map fields need different handling and that table keys by raw class. `ConverterTest` has `MAP_CONVERTERS` and `MAPS_WRITTEN_AS_THEY_ARE` by field name and checks that they list exactly the map fields; `TypedOptionsSyncTest` has `MAP_SAMPLES` by field name.
- `buttonDisplay`, `headerToolbarClass` and `footerToolbarClass` are per view (option-names.txt); the rest is global. No option of this batch has a client handler in `full-calendar.ts`; the google-calendar plugin for `googleCalendarApiKey` is loaded.
- Mutation check: removing `@JsonName("eventDataTransform")` fails `OptionFieldsStaticTest`, removing the converter on `toolbarElements` fails `ConverterTest` and `TypedOptionsSyncTest`.
- Gate green: full build, E2E 463 passed.

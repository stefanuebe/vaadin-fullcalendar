# 40: Scheduler render hooks: resource cells, columns, day headers, expander

| | |
|---|---|
| GitHub | #396 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16), commit `a087d761` |
| Blocked by | `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (19), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `resourceCellClass`, `resourceCellContent`, `resourceCellDidMount`, `resourceCellInnerClass`, `resourceCellWillUnmount`, `resourceColumnDividerClass`, `resourceColumnHeaderClass`, `resourceColumnHeaderContent`, `resourceColumnHeaderDidMount`, `resourceColumnHeaderInnerClass`, `resourceColumnHeaderWillUnmount`, `resourceColumnResizerClass`, `resourceDayHeaderClass`, `resourceDayHeaderContent`, `resourceDayHeaderDidMount`, `resourceDayHeaderInnerClass`, `resourceDayHeaderWillUnmount`, `resourceExpanderClass`, `resourceExpanderContent`.

**Blocked by:** `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Notes from the implementation (2026-10-16):**

- All 19 fields per view in `SchedulerViewOptions`: the class hooks as `ClassNameValue` (setter `String`), `resourceColumnDividerClass` as plain `String` (`typeof refineClassName`), the content hooks as `ContentValue` (setter `String` for text), the DidMount/WillUnmount hooks as `JsCallback`.
- The vaadin theme and the legacy-class-names plugins set some of these class options as `optionDefaults`. The library joins class options from plugins and the user (`mergeCalendarOptions` → `joinFuncishClassNames`), so a user value adds to them.
- Calendar-level and per-column hooks: the library merges them in `processColOptions`. A column's cell or header content replaces the calendar-level one (a column callback falls back by returning `true`); class names are joined; DidMount/WillUnmount both run, calendar-level first. The Javadoc of `resourceCellContent` and `resourceColumnHeaderContent` says so. A `ComponentResourceColumn` always sets its own cell content.
- Tests: `ClassNameValue` and `ContentValue` in `SchedulerConverterTest.VALUE_TYPES` and `PER_FORM_PARAMETERS`; `everyFormOfAValueTypeField_reachesTheClient` (39) covers every form. Mutation check: a `setResourceCellContent(String)` that stores null fails `SchedulerTypedOptionsSyncTest`.
- Review: two rounds, no major.

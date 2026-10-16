# 41: Scheduler render hooks: groups, lanes, rows, timeline

| | |
|---|---|
| GitHub | #397 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16), commit `55195dd6` |
| Blocked by | `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (22), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `resourceGroupHeaderClass`, `resourceGroupHeaderContent`, `resourceGroupHeaderDidMount`, `resourceGroupHeaderInnerClass`, `resourceGroupHeaderWillUnmount`, `resourceGroupLaneClass`, `resourceGroupLaneContent`, `resourceGroupLaneDidMount`, `resourceGroupLaneInnerClass`, `resourceGroupLaneWillUnmount`, `resourceHeaderRowClass`, `resourceIndentClass`, `resourceLaneBottomClass`, `resourceLaneBottomContent`, `resourceLaneClass`, `resourceLaneDidMount`, `resourceLaneTopClass`, `resourceLaneTopContent`, `resourceLaneWillUnmount`, `resourceRowClass`, `timelineBottomClass`, `timelineTopClass`.

**Blocked by:** `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Notes from the implementation (2026-10-16):**

- All 22 fields per view in `SchedulerViewOptions`: 7 class hooks as `ClassNameValue` (setter `String`), 4 content hooks as `ContentValue` (setter `String` for text), 6 DidMount/WillUnmount hooks as `JsCallback`, and `resourceHeaderRowClass`, `resourceIndentClass`, `resourceRowClass`, `timelineBottomClass`, `timelineTopClass` as plain `String` (`typeof refineClassName`, no callback).
- The `info` properties in the Javadoc come from the `.d.ts` types (`ResourceGroupHeaderInfo`, `ResourceGroupLaneInfo`, `ResourceLaneInfo`); the docs pages under `fc-docs/_docs-v7/views/` give the descriptions and the `@see` targets.
- Tests: no new test code; the reflection-based `SchedulerTypedOptionsSyncTest` and `SchedulerConverterTest` (value types registered in 40) cover every field and form. Mutation check: a `setResourceLaneClass(String)` that stores null fails `SchedulerTypedOptionsSyncTest`.
- Review: two rounds, no major. Round 1 minor fixed: "resource timeline view" where the docs say Resource Timeline view.

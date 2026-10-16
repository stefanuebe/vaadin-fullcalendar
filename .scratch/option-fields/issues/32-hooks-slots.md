# 32: Render hooks: slots, now indicator, week numbers, tables

| | |
|---|---|
| GitHub | #368 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 007826a4) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (31), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `inlineWeekNumberClass`, `inlineWeekNumberContent`, `inlineWeekNumberDidMount`, `inlineWeekNumberWillUnmount`, `nowIndicatorDotClass`, `nowIndicatorHeaderClass`, `nowIndicatorHeaderContent`, `nowIndicatorHeaderDidMount`, `nowIndicatorHeaderWillUnmount`, `nowIndicatorLineClass`, `nowIndicatorLineContent`, `nowIndicatorLineDidMount`, `nowIndicatorLineWillUnmount`, `slotHeaderClass`, `slotHeaderContent`, `slotHeaderDidMount`, `slotHeaderDividerClass`, `slotHeaderInnerClass`, `slotHeaderRowClass`, `slotHeaderWillUnmount`, `slotLaneClass`, `slotLaneDidMount`, `slotLaneWillUnmount`, `tableBodyClass`, `tableClass`, `tableHeaderClass`, `weekNumberHeaderClass`, `weekNumberHeaderContent`, `weekNumberHeaderDidMount`, `weekNumberHeaderInnerClass`, `weekNumberHeaderWillUnmount`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, 007826a4)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

# 31: Render hooks: day cells, day headers, day lanes, all-day

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (28), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `allDayDividerClass`, `allDayHeaderClass`, `allDayHeaderContent`, `allDayHeaderDidMount`, `allDayHeaderInnerClass`, `allDayHeaderWillUnmount`, `dayCellBottomClass`, `dayCellDidMount`, `dayCellInnerClass`, `dayCellTopClass`, `dayCellTopContent`, `dayCellTopInnerClass`, `dayCellWillUnmount`, `dayHeaderClass`, `dayHeaderContent`, `dayHeaderDidMount`, `dayHeaderDividerClass`, `dayHeaderInnerClass`, `dayHeaderRowClass`, `dayHeaderWillUnmount`, `dayLaneClass`, `dayLaneDidMount`, `dayLaneInnerClass`, `dayLaneWillUnmount`, `dayRowClass`, `fillerClass`, `highlightClass`, `nonBusinessHoursClass`. `dayCellClass` exists since 06.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.

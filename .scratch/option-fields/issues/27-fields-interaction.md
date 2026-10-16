# 27: Fields: interaction

| | |
|---|---|
| GitHub | #374 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `22-value-types-other-callbacks`, `23-value-types-constraints-business-hours-day-max`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (26), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `dragRevertDuration`, `dragScroll`, `droppable`, `dropAccept`, `eventAllow`, `eventConstraint`, `eventDragMinDistance`, `eventDurationEditable`, `eventInteractive`, `eventLongPressDelay`, `eventOverlap`, `eventResizableFromStart`, `eventStartEditable`, `longPressDelay`, `navLinks`, `navLinkDayClick`, `navLinkWeekClick`, `selectable`, `selectAllow`, `selectConstraint`, `selectLongPressDelay`, `selectMinDistance`, `selectMirror`, `selectOverlap`, `unselectAuto`, `unselectCancel`.

**Value types from 57 (2026-10-16):** `eventConstraint` and `selectConstraint` take `ConstraintValue`, whose windows now mix recurring `BusinessHours` (also with a recurring start and end date) and fixed spans (`ConstraintValue.Window.span`); setters `String`, `BusinessHours...`, `ConstraintValue.Window...`. An RRule window follows in 61.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `22-value-types-other-callbacks`, `23-value-types-constraints-business-hours-day-max`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Note from the work (2026-10-16):** `navLinkDayClick` and `navLinkWeekClick` are left out, because the add-on's client registers its own handlers for them. They follow in 64.

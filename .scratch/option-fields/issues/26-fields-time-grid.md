# 26: Fields: time grid and slots

| | |
|---|---|
| GitHub | #373 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 22b7c9d9) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (19), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `allDaySlot`, `dayMinWidth`, `dayNarrowWidth`, `expandRows`, `multiMonthMaxColumns`, `nowIndicator`, `nowIndicatorSnap`, `scrollTime`, `scrollTimeReset`, `singleMonthMinWidth`, `slotDuration`, `slotEventOverlap`, `slotHeaderAlign`, `slotHeaderInterval`, `slotHeaderSticky`, `slotMaxTime`, `slotMinHeight`, `slotMinTime`, `snapDuration`.

**Value types from 57 (2026-10-16):** `slotDuration`, `snapDuration`, `slotHeaderInterval`, `scrollTime`, `slotMinTime` and `slotMaxTime` take `DurationValue` (setters `Duration`, `Period`, `String`).

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, 22b7c9d9)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Note from the work (2026-10-16):** `slotHeaderAlign` and `slotHeaderSticky` are read only by the timeline views of the Scheduler, which their Javadoc says. `slotEventOverlap` is a plain `Boolean` (`slotEntryOverlap`), because the library takes no callback for it.

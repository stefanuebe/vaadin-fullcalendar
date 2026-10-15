# 29: Fields: texts, hints and formats

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `22-value-types-other-callbacks`, `24-value-types-auto-false-now` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (35), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `allDayText`, `closeHint`, `dayText`, `eventsHint`, `listText`, `monthText`, `moreLinkHint`, `moreLinkText`, `navLinkHint`, `nextHint`, `nextText`, `nextYearText`, `noEventsText`, `prevHint`, `prevText`, `prevYearText`, `timedText`, `todayHint`, `todayText`, `viewChangeHint`, `viewHint`, `weekTextLong`, `weekTextShort`, `yearText`, `dayCellFormat`, `dayHeaderFormat`, `eventTimeFormat`, `listDayAltFormat`, `listDayFormat`, `monthStartFormat`, `popoverFormat`, `singleMonthTitleFormat`, `slotHeaderFormat`, `titleFormat`, `weekNumberFormat`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `22-value-types-other-callbacks`, `24-value-types-auto-false-now`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.

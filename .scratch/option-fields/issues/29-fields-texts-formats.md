# 29: Fields: texts, hints and formats

| | |
|---|---|
| GitHub | #376 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `22-value-types-other-callbacks`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (35), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `allDayText`, `closeHint`, `dayText`, `eventsHint`, `listText`, `monthText`, `moreLinkHint`, `moreLinkText`, `navLinkHint`, `nextHint`, `nextText`, `nextYearText`, `noEventsText`, `prevHint`, `prevText`, `prevYearText`, `timedText`, `todayHint`, `todayText`, `viewChangeHint`, `viewHint`, `weekTextLong`, `weekTextShort`, `yearText`, `dayCellFormat`, `dayHeaderFormat`, `eventTimeFormat`, `listDayAltFormat`, `listDayFormat`, `monthStartFormat`, `popoverFormat`, `singleMonthTitleFormat`, `slotHeaderFormat`, `titleFormat`, `weekNumberFormat`.

**Value types from 57 (2026-10-16):** Texts with a callback form (`moreLinkText`, `moreLinkHint`, `navLinkHint`, `viewHint`, `todayHint`, `prevHint`, `nextHint` and the like) take `TextValue` (`TextContent` or `JsCallback`, no HTML), not `ContentValue`. A text option without a callback form stays `String`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `22-value-types-other-callbacks`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Note from 22 (2026-10-15):** an option that takes a text or a callback returning a text (`moreLinkText`, and the hints with a callback form such as `navLinkHint`, `nextHint`, `prevHint`, `todayHint`) uses `ContentValue` (`TextContent` or `JsCallback`), with one setter per form. Changed by the review of 21–24 (2026-10-15): these options take the text value type that 57 adds, not `ContentValue`, because the library reads them as a string or a function returning one, never as `{html}`.

**Note from 24 (2026-10-15):** the format options take `DateFormatValue` (`DateFormat` or `JsCallback`), except `listDayFormat` and `listDayAltFormat`, which take `ListDayFormatValue` (also `ListDayFormatValue.NONE`). `listText` takes `ListTextValue` (`TextContent` or `ListTextValue.NONE`).

**Note from the work (2026-10-16):** `slotHeaderFormat` is left out, because the library also takes a list of formats, one per header tier, and no value type has that form. It follows in 65.

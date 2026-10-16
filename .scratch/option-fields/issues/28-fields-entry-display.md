# 28: Fields: entry display and layout

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `23-value-types-constraints-business-hours-day-max`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (30), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `aspectRatio`, `backgroundEventColor`, `borderless`, `borderlessBottom`, `borderlessTop`, `borderlessX`, `businessHours`, `class`, `colorScheme`, `columnEventTitleSticky`, `contentHeight`, `dayHeaderAlign`, `dayMaxEvents`, `dayMaxEventRows`, `displayEventEnd`, `displayEventTime`, `eventColor`, `eventContrastColor`, `eventDisplay`, `eventMaxStack`, `eventMinHeight`, `eventOrder`, `eventOrderStrict`, `eventShortHeight`, `eventSlicing`, `footerScrollbarSticky`, `progressiveEventRendering`, `rerenderDelay`, `rowEventTitleSticky`, `tableHeaderSticky`. The library option `class` is the field `className`.

**Value types from 57 (2026-10-16):** `eventOrder` (field `entryOrder`) takes `EntryOrderValue` (`EntryOrder` or one `JsCallback`); setters `String...` and `EntryOrder.Key...`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `23-value-types-constraints-business-hours-day-max`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.

# 28: Fields: entry display and layout

| | |
|---|---|
| GitHub | #375 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 93113657) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `23-value-types-constraints-business-hours-day-max`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (30), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `aspectRatio`, `backgroundEventColor`, `borderless`, `borderlessBottom`, `borderlessTop`, `borderlessX`, `businessHours`, `class`, `colorScheme`, `columnEventTitleSticky`, `contentHeight`, `dayHeaderAlign`, `dayMaxEvents`, `dayMaxEventRows`, `displayEventEnd`, `displayEventTime`, `eventColor`, `eventContrastColor`, `eventDisplay`, `eventMaxStack`, `eventMinHeight`, `eventOrder`, `eventOrderStrict`, `eventShortHeight`, `eventSlicing`, `footerScrollbarSticky`, `progressiveEventRendering`, `rerenderDelay`, `rowEventTitleSticky`, `tableHeaderSticky`. The library option `class` is the field `className`.

**Value types from 57 (2026-10-16):** `eventOrder` (field `entryOrder`) takes `OrderValue` (`Order` or one `JsCallback`; renamed from `EntryOrder*` in 39); setters `String...` and `Order.Key...`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `23-value-types-constraints-business-hours-day-max`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, 93113657)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Note from the work (2026-10-16):** `eventDisplay` is left out, because `DisplayMode.NONE` writes `null` instead of `"none"`. It follows in 66. `dayHeaderAlign` already existed. `className` (library `class`) is a `ClassNameValue`, because the calendar-level option takes a callback too.

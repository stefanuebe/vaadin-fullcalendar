# 35: Render hooks: list view, more link, no entries, popover

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `19-more-link-click`, `21-value-types-render-hooks`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (25), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `columnMoreLinkClass`, `columnMoreLinkInnerClass`, `listDaysClass`, `listDayBodyClass`, `listDayClass`, `listDayHeaderClass`, `listDayHeaderContent`, `listDayHeaderDidMount`, `listDayHeaderInnerClass`, `listDayHeaderWillUnmount`, `moreLinkClass`, `moreLinkContent`, `moreLinkDidMount`, `moreLinkInnerClass`, `moreLinkWillUnmount`, `noEventsClass`, `noEventsContent`, `noEventsDidMount`, `noEventsInnerClass`, `noEventsWillUnmount`, `popoverClass`, `popoverCloseClass`, `popoverCloseContent`, `rowMoreLinkClass`, `rowMoreLinkInnerClass`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `19-more-link-click`, `21-value-types-render-hooks`, `60-setters-without-form-overloads`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.

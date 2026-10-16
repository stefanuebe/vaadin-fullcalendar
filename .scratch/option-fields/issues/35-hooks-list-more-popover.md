# 35: Render hooks: list view, more link, no entries, popover

| | |
|---|---|
| GitHub | #392 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `19-more-link-click`, `21-value-types-render-hooks`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (25), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `columnMoreLinkClass`, `columnMoreLinkInnerClass`, `listDaysClass`, `listDayBodyClass`, `listDayClass`, `listDayHeaderClass`, `listDayHeaderContent`, `listDayHeaderDidMount`, `listDayHeaderInnerClass`, `listDayHeaderWillUnmount`, `moreLinkClass`, `moreLinkContent`, `moreLinkDidMount`, `moreLinkInnerClass`, `moreLinkWillUnmount`, `noEventsClass`, `noEventsContent`, `noEventsDidMount`, `noEventsInnerClass`, `noEventsWillUnmount`, `popoverClass`, `popoverCloseClass`, `popoverCloseContent`, `rowMoreLinkClass`, `rowMoreLinkInnerClass`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `19-more-link-click`, `21-value-types-render-hooks`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Notes from the implementation (2026-10-16):**

- All 25 options are per view, in the new `ViewOptions` section *Render hooks: list view, more link, no entries, popover*. The `noEvents*` options are `noEntries*` fields with `@JsonName`.
- Types from the `.d.ts`: 12 class options are `ClassNameValue`, `listDaysClass`, `popoverClass` and `popoverCloseClass` are `typeof refineClassName` and so plain `String`; 4 content options are `ContentValue`; 6 mount hooks are `JsCallback`. No uncovered form.
- `moreLinkClass` applies to every more link; the library joins it with `columnMoreLinkClass` or `rowMoreLinkClass` (`MoreLinkTrigger.render`). The popover close button takes its accessible label from `closeHint`.
- The generic tests cover the batch: `TypedOptionsSyncTest` sets every field through every setter, `OptionFieldsStaticTest` checks names and scope. Mutation: a removed `@JsonName` and a removed converter fail them. Not caught: two valid library names swapped between fields; the option-by-option review (67) covers that.
- Gate green: full build, E2E 464 passed.

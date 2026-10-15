# 34: Render hooks: entries, background entries, list items

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `18-sync-hook-entry-did-mount`, `21-value-types-render-hooks` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (20), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `backgroundEventClass`, `backgroundEventContent`, `backgroundEventDidMount`, `backgroundEventInnerClass`, `backgroundEventTitleClass`, `backgroundEventWillUnmount`, `eventAfterClass`, `eventBeforeClass`, `eventClass`, `eventContent`, `eventInnerClass`, `eventTimeClass`, `eventTitleClass`, `eventWillUnmount`, `listItemEventAfterClass`, `listItemEventBeforeClass`, `listItemEventClass`, `listItemEventInnerClass`, `listItemEventTimeClass`, `listItemEventTitleClass`. `eventDidMount` exists since 18.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `18-sync-hook-entry-did-mount`, `21-value-types-render-hooks`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.

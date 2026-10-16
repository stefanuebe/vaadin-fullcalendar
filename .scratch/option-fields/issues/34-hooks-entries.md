# 34: Render hooks: entries, background entries, list items

| | |
|---|---|
| GitHub | #367 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 65ad5385) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `18-sync-hook-entry-did-mount`, `21-value-types-render-hooks`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (20), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `backgroundEventClass`, `backgroundEventContent`, `backgroundEventDidMount`, `backgroundEventInnerClass`, `backgroundEventTitleClass`, `backgroundEventWillUnmount`, `eventAfterClass`, `eventBeforeClass`, `eventClass`, `eventContent`, `eventInnerClass`, `eventTimeClass`, `eventTitleClass`, `eventWillUnmount`, `listItemEventAfterClass`, `listItemEventBeforeClass`, `listItemEventClass`, `listItemEventInnerClass`, `listItemEventTimeClass`, `listItemEventTitleClass`. `eventDidMount` exists since 18.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `18-sync-hook-entry-did-mount`, `21-value-types-render-hooks`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, 65ad5385)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

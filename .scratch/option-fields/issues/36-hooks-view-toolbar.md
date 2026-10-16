# 36: Render hooks: view, toolbar, buttons, single month, nav links

| | |
|---|---|
| GitHub | #370 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (14), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `buttonClass`, `buttonGroupClass`, `navLinkClass`, `singleMonthClass`, `singleMonthDidMount`, `singleMonthHeaderClass`, `singleMonthHeaderInnerClass`, `singleMonthWillUnmount`, `toolbarClass`, `toolbarSectionClass`, `toolbarTitleClass`, `viewClass`, `viewDidMount`, `viewWillUnmount`.

**`viewDidMount` moved to 63 (review, 2026-10-16):** the client registers its own `viewDidMount` handler for `ViewSkeletonRenderedEvent`, so the option needs the sync hook like `entryDidMount` (18). This batch adds the other 13 options.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

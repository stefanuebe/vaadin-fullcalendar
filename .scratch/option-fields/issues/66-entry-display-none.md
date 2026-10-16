# 66: `eventDisplay` and the client value of `DisplayMode.NONE`

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, needs-triage |
| Status | needs-triage (needs a decision by the maintainer) |
| Blocked by | `28-fields-entry-display` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `entryDisplay` (`@JsonName("eventDisplay")`, global per `option-names.txt`). The library takes `auto`, `block`, `list-item`, `background`, `inverse-background` and `none`. The existing enum `DisplayMode` has all six, but `NONE` writes `null` instead of `"none"`, so as an option value it would remove the option instead of hiding the entries.

`Entry.setDisplayMode(DisplayMode.NONE)` uses the same enum. It probably sends `display: null`, which the library reads as unset, so the entry would still show. Not verified yet, the ticket checks it first with a Playwright test.

Found by batch 28 (2026-10-16), which left the option out.

**Open decision:** change `NONE` to write `"none"` (fixes the entry too, if the check confirms the bug), or a separate value for the option.

**Blocked by:** `28-fields-entry-display`

**Status:** needs-triage

- [ ] Playwright: an entry with `DisplayMode.NONE` is hidden (shows whether the entry has the bug).
- [ ] The field `entryDisplay` sends every library value, `none` included.
- [ ] The static checks, the pinned lists and the converter round trip include it.

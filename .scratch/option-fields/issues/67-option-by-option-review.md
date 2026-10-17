# 67: Compare every option with its field

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `42-coverage-check` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A review after all options, the scheduler's included, have their fields. Several agents compare EVERY `Option` and `SchedulerOption` constant in detail with its successor field:

- **Javadoc:** the main focus. The old Javadoc is the language frame, the facts come from the library docs (maintainer, 2026-10-16). Every factual statement of the new Javadoc is checked against the library docs, and a statement about the add-on against the add-on (`full-calendar.ts`, `FullCalendar`). Where the docs and the library code (`.d.ts`, chunks) disagree, the code wins, because it is what runs. The Javadoc of the field then says so, naming what the library docs say and what the used library version does (maintainer, 2026-10-16: "das muss dann definitiv am Javadoc dokumentiert werden"), and the case is recorded (wave 2 found two: the default header formats of `dayHeaderFormat`, and the argument of a format callback, which is an info object and not a date). Parts of the old Javadoc were written for an older library version and were wrong for the used one (wave 2: `moreLinkHint` sets `title`, not `aria-label`; `now` takes effect again after a detach and attach).
- **Values:** does the field take every value and form the library takes for the option, and nothing it rejects? Does the client receive the value as the library reads it?
- **Mapping:** where the library form cannot be mapped 1:1, is the reason concrete and written down (ticket and Javadoc)?

Options the add-on handles through Vaadin itself (for example the nav link click handlers, `moreLinkClick`, `viewDidMount`, `height`) are left out, as far as they are covered by their own tickets.

Asked for by the maintainer (2026-10-16), after the review of wave 1: a human cannot review the field batches option by option, so this review does it once for all options together.

**Blocked by:** `42-coverage-check`

**Status:** ready-for-agent

- [x] Every option constant is checked on all three points, the result is recorded per option (in this ticket or a file beside it).
- [ ] Every finding is fixed, or recorded with its reason as a deliberate gap.
- [ ] Every field whose library docs disagree with the library code says so in its Javadoc, the two cases of wave 2 included.
- [ ] The list of options that cannot be mapped 1:1 goes to the maintainer.

## Review run (2026-10-17)

The review ran on 2026-10-17. The decisions below were taken the same day; the fixes are under "Fixes".

**How it ran.** Six Sonnet agents, read-only, each about 58 options (inputs `part1–6.md`, brief `brief.md`), wrote one report each (`report1–6.md`). Three Opus agents then checked the six reports independently against the library code 7.1.0 and the docs (`verify-A/B/C.md`). Everything is in `../67-review/`. Excluded as in the brief: the options with their own tickets (`navLinkDayClick`, `navLinkWeekClick`, `moreLinkClick`, `viewDidMount`, `height`, `class`) and the eleven duration fields, whose Javadoc ticket 70 rewrites.

**Result.** 340 options, about 75 findings. None of the three verifiers found a finding wrong. Open, because only a browser test settles them: the four `*WillUnmount` fields (does a changed callback reach elements already rendered), `entryWillUnmount` with `dayMaxEvents` (subset rendering since 7.1), `refetchResourcesOnNavigate` (does a raw resources function still work), `unselectCancel` with `""` (DOM `closest("")` throws by the spec).

The kinds of findings (details and sources in the reports and the verify files):

1. Javadoc wrong for the used library, about a dozen fields. Among them: "Without a value, no classes are added" on the row, block and column entry class fields (the theme sets classes, ours are joined with them, `28b4e459.js:33-37,65-77`; the verifiers found the block and column fields, which report 1 missed); `footerScrollbarSticky` `TRUE`; `expandRows`; `dayMaxEntries`/`dayMaxEntryRows` in the time grid all-day section; `slotHeaderRowClass` (timeline only); `slotMinHeight`/`slotMinWidth` (minimums); `entryInteractive` with a url; `Direction.LTR` "(default)"; the `Order` example for `resourceOrder` (resource compare objects have no `extendedProps`, `0a9e97be.js:858-862`; for entries the example works).
2. Library docs and code differ and the Javadoc does not say so, about 15 fields: `buttons` click arguments, `direction`, `dayNarrowWidth`, `entryStartEditable`/`entryDurationEditable` (no default, they follow `editable`), `entryInteractive`, `entryDragMinDistance`, `listDayFormat` `false`, `singleMonth*Class` `date`, `dayHeaderDividerClass` and `resourceLane*` `options`, `resourceGroupField` with a group column, and the format callbacks: only `slotHeaderFormat` has the note, missing on `titleFormat`, `weekNumberFormat`, `dayCellFormat`, `dayHeaderFormat` (also its default formats), `entryTimeFormat`, `monthStartFormat`, `popoverFormat`, `singleMonthTitleFormat`, `listDayFormat`, `listDayAltFormat`.
3. True facts of the old Javadoc that got lost: the note "FullCalendar documents this option only partly or not at all" on all 14 constants that had it; the terms background, block, column and row entry and "applies in addition to the general hook"; the pointer from `editable` to the start and duration options; the group id and business hours forms in the `entryConstraint` field Javadoc; three facts of `buttons`; `el` in the scheduler mount hooks; the list default "7pm" of `entryTimeFormat`; `firstDay` with ISO week numbers.
4. Library defaults missing, about 30 fields (reports 2 and 3 skipped this axis, reading ticket 17 as having decided it; it did not, so the list is incomplete).
5. Values: `ListDayFormatValue.NONE` sends `false`, which the library turns into its default format, so the text stays (`642eba18.js:947-949`, `33aacced.js:37-38`); `hiddenDays` with all seven days makes the library throw (`54e239b5.js:260-275`); a comma inside an `Order` name is not split, so `Order.of("title,start")` sorts by nothing (`642eba18.js:162-170`); `DateRange` rejects equal bounds, which the library reads as one day (`54e239b5.js:247-252`, as in 7.x).
6. Add-on bug, also in 7.x: `entryDidMount` given as an arrow function with a block body throws once the add-on merges its snippet into it, because the merged code reads `arguments[0]`, which an arrow function does not have (`FullCalendar.java:1141-1186`, `full-calendar.ts:94`). The default entry id snippet is on by default. Not yet reproduced in a browser.
7. Contradiction to this ticket: the wave-2 example "a newline in `allDayText` is no line break" is contradicted by the code (`4b6eee98.js:319` wraps the text in `white-space: pre`, `CHANGELOG.md:380`). The browser check (2026-10-17, `../67-review/browser-checks.md`) settled it: a newline is a line break, so the wave-2 example was wrong and is removed above.

**Decisions for the maintainer** (with the recommendations of the three verifiers, A/B/C):

- D1: name the library default in every field Javadoc again (one sentence; where view, theme or locale decide, say that)? A, B, C: yes. Today 14 of 355 fields name one; the old Javadoc had 109 Default rows.
- D2: bring back the note "the library docs do not describe this option" on the 14 options, and fix the `@see` links whose page does not name the option (`nowIndicatorSnap` has no page)? A, C: yes.
- D3: `Order.Property` rejects a comma, with a message pointing to `Order.of("a", "b")`? A, B, C: yes.
- D4: `DateRange` with equal bounds: keep rejecting (A, C: 7.x does the same) or allow and document "one day" (B)?
- D5: `ListDayFormatValue.NONE`: keep it with the docs-versus-code note and an `FC-UPDATE:` marker after a browser check (A, C), or remove it (B)?
- D6: `hiddenDays` with all seven days: reject in the setter and document the `weekends` combination (A, B), or document only (C)?
- D7: fix the `entryDidMount` merge by wrapping the user callback instead of splicing into its body (C: `function(){<snippet>; return (<user>).apply(this, arguments);}`), with a Playwright test, and port it back to `v7_master`? A, B, C: fix it.
- D8: `allDayText`: browser check, then correct either this ticket's wave-2 example or restore the old sentence (A, B, C).
- D9: make `el` part of every DidMount/WillUnmount info list as a convention (B)?

**Decided (2026-10-17):** the standing rules answer D1–D9, the maintainer agreed. D1, D2, D3: yes. D4: keep rejecting, as 7.x does. D5: check in the browser; if `false` shows the default format, remove `NONE` and say in the Javadoc that the library docs name `false` but the used version ignores it. D6: the library throws (`54e239b5.js:265-275`), so the setter rejects seven hidden days. D7: ticket 77. D8: check in the browser, then correct the wrong side. D9: no convention; each info list names what the library passes, `el` included where it does.

## Fixes (2026-10-17, branch `option-fields-67`)

**State:** fixes done and reviewed; the full gate has not run; nothing of the fixes is committed yet (only the browser checks `dc1a919b`, the writers' brief `bef56830` and the six writers' Javadoc commits with their merges up to `a53ae0f6`). The working tree holds the code changes and all review fixes.

- **Browser checks** (`../67-review/browser-checks.md`): `allDayText` newline is a line break (the wave-2 example above was wrong and is removed); `listDayFormat`/`listDayAltFormat` ignore `false`; seven hidden days throw; a changed WillUnmount callback runs for elements rendered before the change; `entryDidMount` does not run for entries behind "+N more" with a number limit; `unselectCancel ""` acts like no value; `refetchResourcesOnNavigate` has no effect on the add-on's resource list.
- **Code:** `ListDayFormatValue` and its converter removed, `listDayFormat`/`listDayAltFormat` take `DateFormatValue` (D5); `Order.Property` rejects a comma (D3); `setHiddenDays` rejects all seven days (D6); tests in `ConverterTest`, `TypedOptionsSyncTest`, `ValueTypeSwitchTest` and the api pin files adjusted. The targeted tests passed (244).
- **Javadoc:** six Sonnet writers in worktrees (brief `../67-review/fix-brief.md`), one pass making the class and callback default sentences uniform, then five review rounds of three agents (Standards and Spec on Sonnet, claims against the library code on Opus). Rounds 4 and 5 found no major. Rejected review findings, with reasons: `virtualization` is read by the library (`8f1316dc.js:1390`); the D2 note is outdated for `columnEntryTitleClass`, `rowEntryTitleClass`, `moreLinkInnerClass`, `slotHeaderRowClass`, which the current library docs describe; the `entryDidMount` braces sentence waits for ticket 77; the sentence "the add-on also adds stable class names" was removed everywhere (legacy class names are an internal compatibility layer).
- **Minors left as they are** (maintainer, 2026-10-17): number defaults written with and without `{@code}`; default and getter sentence sometimes in one paragraph; the duration setters' "the duration, days count as 24 hours, or null"; a few phrasing differences ("Show or hide…" against "Whether…"); the `buttons` `hint` docs note does not say what the used version does.

**Next step:** run the full gate (`demo/.vaadin/vaadin-dev shutdown` if running, `mvn clean install` with `JAVA_HOME=~/.vaadin/jdk/jbr-25.0.4.1`, delete `e2e-test-app` `prod.bundle`, then `mvn clean verify -Pit` in `e2e-test-app/`), commit the working tree on `option-fields-67` (explicit files, check the branch first), tick the acceptance criteria above, then hand the maintainer the list of options that cannot be mapped 1:1 (fourth criterion) and merge locally like 70–76. Then ticket 77.

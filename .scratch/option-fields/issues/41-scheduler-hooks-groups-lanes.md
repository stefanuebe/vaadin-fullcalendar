# 41: Scheduler render hooks: groups, lanes, rows, timeline

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | in progress (2026-10-16), branch `option-fields-41` |
| Blocked by | `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (22), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `resourceGroupHeaderClass`, `resourceGroupHeaderContent`, `resourceGroupHeaderDidMount`, `resourceGroupHeaderInnerClass`, `resourceGroupHeaderWillUnmount`, `resourceGroupLaneClass`, `resourceGroupLaneContent`, `resourceGroupLaneDidMount`, `resourceGroupLaneInnerClass`, `resourceGroupLaneWillUnmount`, `resourceHeaderRowClass`, `resourceIndentClass`, `resourceLaneBottomClass`, `resourceLaneBottomContent`, `resourceLaneClass`, `resourceLaneDidMount`, `resourceLaneTopClass`, `resourceLaneTopContent`, `resourceLaneWillUnmount`, `resourceRowClass`, `timelineBottomClass`, `timelineTopClass`.

**Blocked by:** `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `60-setters-without-form-overloads`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.

**Progress (2026-10-16):** a Sonnet implementer agent writes the 22 fields on branch `option-fields-41` (uncommitted; it touches `SchedulerViewOptions`/`SchedulerCalendarOptions`, the two scheduler `*-api.txt` lists and, if needed, the scheduler test registrations). Pattern: ticket 40. Next steps: check its report (Javadoc info properties only from the `.d.ts`, lines ≤120, wrapped `@see` as in 40), targeted scheduler tests, a mutation check, the three-agent review (one extra round after the first without a major), gate, issue, commit, merge into local master, then 42.

**Implementer report (2026-10-16):** all 22 fields in `SchedulerViewOptions` (all per view), uncommitted in the working tree with the two `*-api.txt` lists; 208 scheduler tests green. 7 `ClassNameValue`, 4 `ContentValue`, 6 `JsCallback`, 5 plain `String` class fields (`resourceHeaderRowClass`, `resourceIndentClass`, `resourceRowClass`, `timelineBottomClass`, `timelineTopClass`). `fc-docs/_docs-v7` has no pages for these options, so the `.d.ts` is the source of the `info` properties; check the `@see` targets and the Javadoc claims in the review. Next: verify (lines ≤120, info properties, `@see` links), mutation check, review.

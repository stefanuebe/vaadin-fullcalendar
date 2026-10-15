# 39: Scheduler fields

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (19), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `datesAboveResources`, `eventMinWidth`, `eventPrintLayout`, `eventResourceEditable`, `filterResourcesWithEvents`, `schedulerLicenseKey`, `printMaxRows`, `refetchResourcesOnNavigate`, `resourcesInitiallyExpanded`, `resourcesSet`, `resourceAdd`, `resourceChange`, `resourceColumnsWidth`, `resourceDayHeaderAlign`, `resourceGroupField`, `resourceOrder`, `resourceRemove`, `slotMinWidth`, `virtualization`. `resourceGroupField` and `resourcesInitiallyExpanded` exist since 37.

**Blocked by:** `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.

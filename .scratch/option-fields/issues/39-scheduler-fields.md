# 39: Scheduler fields

| | |
|---|---|
| GitHub | #395 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16), commit `bde2c1cf` |
| Blocked by | `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (19), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `datesAboveResources`, `eventMinWidth`, `eventPrintLayout`, `eventResourceEditable`, `filterResourcesWithEvents`, `schedulerLicenseKey`, `printMaxRows`, `refetchResourcesOnNavigate`, `resourcesInitiallyExpanded`, `resourcesSet`, `resourceAdd`, `resourceChange`, `resourceColumnsWidth`, `resourceDayHeaderAlign`, `resourceGroupField`, `resourceOrder`, `resourceRemove`, `slotMinWidth`, `virtualization`. `resourceGroupField` and `resourcesInitiallyExpanded` exist since 37.

**Value types from 57 (2026-10-16):** `resourceColumnsWidth` takes `WidthValue` (pixels or percentage, the only forms the library reads; setter `int` for pixels).

**Blocked by:** `12-pinned-api-and-converter-roundtrip`, `21-value-types-render-hooks`, `37-scheduler-options-object`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.

**Notes from the implementation (2026-10-16):**

- 17 new fields: `refetchResourcesOnNavigate` in `SchedulerCalendarOptions` (global), the rest in `SchedulerViewOptions`. Java names with "entry": `entryMinWidth`, `entryPrintLayout` (the existing `EntryPrintLayout` enum), `entryResourceEditable`, `filterResourcesWithEntries`, each with `@JsonName`.
- `resourceOrder`: the library refines it with `parseFieldSpecs` like `eventOrder` (strings, arrays mixing names and functions, one function). Maintainer decision (2026-10-16): rename `EntryOrder`/`EntryOrderValue`/`EntryOrderValueConverter` to `Order`/`OrderValue`/`OrderValueConverter` and share them, instead of a copy in the scheduler. The Javadoc of the types speaks of items (entries or resources). README table, tickets 28, 54 and 57 carry the new names.
- `resourceColumnsWidth` takes `WidthValue` (setter `int` for pixels), `resourceDayHeaderAlign` takes `HeaderAlignValue` (Lombok setter). Its Javadoc names only `level`, the property the `.d.ts` types; the library also passes `inPopover` and `isNarrow` at runtime.
- The four resource listeners also run for changes the server sends, because the client applies them with `addResource`, `setProp`/`setExtendedProp` and `remove`; their Javadoc says so.
- Tests: `HeaderAlignValue`, `WidthValue`, `OrderValue` in `SchedulerConverterTest.VALUE_TYPES`; `SchedulerTypedOptionsSyncTest` gets `JsCallback` and `Order.Key` samples, the `PER_FORM_PARAMETERS` rows, and `everyFormOfAValueTypeField_reachesTheClient`, which sends every form of each value-type field and checks the `setOptions` payload. Mutation check: a `setResourceOrder(String...)` that ignores its argument fails it.
- Review: three rounds (round 1 one major: callback info properties not in the `.d.ts`; fixed).

# 42: Every supported option has a field

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `13-timezone-local-server`, `14-timezone-constructor-json-and-raw`, `17-drop-addon-defaults`, `19-more-link-click`, `20-height-from-hassize`, `25-fields-dates`, `26-fields-time-grid`, `27-fields-interaction`, `28-fields-entry-display`, `29-fields-texts-formats`, `30-fields-toolbar-sources`, `31-hooks-days`, `32-hooks-slots`, `33-hooks-entry-layouts`, `34-hooks-entries`, `35-hooks-list-more-popover`, `36-hooks-view-toolbar`, `38-scheduler-resource-columns`, `39-scheduler-fields`, `40-scheduler-hooks-cells-columns`, `41-scheduler-hooks-groups-lanes` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A test compares every non-deprecated `Option` and `SchedulerOption` key with the fields (core and scheduler) and fails on a key without a field, apart from a short documented exception list (for example `height`, which `HasSize` covers). Deprecated constants are add-on aliases of the same library option and get no field; the migration guide maps them. This is the gate before callers move.

**Blocked by:** `13-timezone-local-server`, `14-timezone-constructor-json-and-raw`, `17-drop-addon-defaults`, `19-more-link-click`, `20-height-from-hassize`, `25-fields-dates`, `26-fields-time-grid`, `27-fields-interaction`, `28-fields-entry-display`, `29-fields-texts-formats`, `30-fields-toolbar-sources`, `31-hooks-days`, `32-hooks-slots`, `33-hooks-entry-layouts`, `34-hooks-entries`, `35-hooks-list-more-popover`, `36-hooks-view-toolbar`, `38-scheduler-resource-columns`, `39-scheduler-fields`, `40-scheduler-hooks-cells-columns`, `41-scheduler-hooks-groups-lanes`

**Status:** ready-for-agent

- [ ] The test passes; every exception is listed with its reason.
- [ ] The option-name resources and the fields agree.
- [ ] Every form the library takes for an option (its `.d.ts` type and refiner, and its docs page) has a setter on its field, or the batch ticket gives a concrete technical reason it is left out. The `<dt>Type</dt>` blocks of the old Javadoc (there until 51) are checked too, as a second source.

# 10: Option-name resource for the scheduler module

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | None (can start immediately) |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** As 09, in `addon-scheduler`, generated from `SchedulerOption`. `resourceColumns` is global by the add-on's choice.

**Blocked by:** None (can start immediately)

**Status:** done (2026-10-15)

- [x] The resource lists every non-deprecated `SchedulerOption` key once, with its flag.
- [x] The global flags are checked against the scheduler source of FullCalendar 7.1.

**Where each group was found (2026-10-15, library 7.1.0 in `e2e-test-app/node_modules`):**

- The resource is `option-names.txt` in the scheduler module's test resources, same package path as the core one. The scheduler tests have no test-jar dependency on the core module, so the two files do not clash.
- Global: `refetchResourcesOnNavigate` (library source, `reduceResourceSource` in `fullcalendar-scheduler/chunks/0a9e97be.js`; plugin reducers get `{ ...calendarContext, ...newState }`, and `calendarContext.options` is `optionsData.calendarOptions`, `fullcalendar/chunks/65e4f9c4.js`) and `resourceColumns` (add-on's choice).
- Per view, although they look calendar-wide: `schedulerLicenseKey` (`buildLicenseWarning` in `fullcalendar-scheduler/chunks/dc4db303.js` is a `viewContainerAppends` hook and gets the calendar data, whose `options` come from `currentViewData`, `fullcalendar/chunks/65e4f9c4.js`), and the resource listeners (`emitter.setOptions(currentViewData.options)` in `65e4f9c4.js`; the add-on does not register them).
- `eventResourceEditable`: `computeResourceEditable` (`fullcalendar-scheduler/chunks/0a9e97be.js`) reads it from the context it gets. For a drag, `applyMutationToEventStore` passes `receivingContext`, the context of the view the entry is dropped on (`fullcalendar/chunks/5342e739.js`).
- The scheduler data classes do not exist yet. 37 adds the `FC-UPDATE` comment to them that points to this resource, as 09 did for the core classes.

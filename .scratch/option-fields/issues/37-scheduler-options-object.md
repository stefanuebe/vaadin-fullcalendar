# 37: Scheduler options object and scheduler view options

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `07-view-options`, `10-option-names-scheduler`, `12-pinned-api-and-converter-roundtrip` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `SchedulerCalendarOptions extends CalendarOptions`, `SchedulerViewOptions extends ViewOptions`; the calendar creates its data objects through protected factory methods the scheduler overrides (no `Supplier` fields). `FullCalendarSchedulerOptions extends FullCalendarOptions`, `FullCalendarScheduler.options()` returns it covariantly, its `view(…)` returns `FullCalendarSchedulerViewOptions`. First fields: `resourceGroupField`, `resourcesInitiallyExpanded`. The static checks and the pinned API of 11/12 run in `addon-scheduler` against its resource. Spec: *Types* (Scheduler), user stories 43–46.

**Blocked by:** `07-view-options`, `10-option-names-scheduler`, `12-pinned-api-and-converter-roundtrip`

**Status:** ready-for-agent

- [ ] Core and scheduler options set through one scheduler `options()` object both arrive.
- [ ] The scheduler's `view(…)` sets a core and a scheduler field without a cast.
- [ ] A core calendar's view object has no scheduler accessors (pinned API).
- [ ] `SchedulerSerializationTest` covers scheduler options and view values.
- [ ] The static checks of 11 in `addon-scheduler` check only the fields the scheduler classes declare. The core test covers the inherited ones, and the core resource is not on the scheduler's test classpath (see the note in 11).
- [ ] The scheduler module pins the public methods of its options classes as `OptionsApiTest` in the core does (`getDeclaredMethods`, so the `@Delegate` overrides are listed), and runs the value-type round trip of the core `ConverterTest` over the fields the scheduler data classes declare. The core test classes are not on the scheduler's test classpath (see the note in 12).
- [ ] The scheduler data classes carry an `FC-UPDATE` comment that points to the scheduler's option-name resource (from 10).

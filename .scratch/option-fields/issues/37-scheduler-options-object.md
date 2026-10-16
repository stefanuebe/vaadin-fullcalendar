# 37: Scheduler options object and scheduler view options

| | |
|---|---|
| GitHub | #393 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, a1bda347; follow-up 104fc86a) |
| Blocked by | `07-view-options`, `10-option-names-scheduler`, `12-pinned-api-and-converter-roundtrip` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `SchedulerCalendarOptions extends CalendarOptions`, `SchedulerViewOptions extends ViewOptions`; the calendar creates its data objects through protected factory methods the scheduler overrides (no `Supplier` fields). `FullCalendarSchedulerOptions extends FullCalendarOptions`, `FullCalendarScheduler.options()` returns it covariantly, its `view(…)` returns `FullCalendarSchedulerViewOptions`. First fields: `resourceGroupField`, `resourcesInitiallyExpanded`. The static checks and the pinned API of 11/12 run in `addon-scheduler` against its resource. Spec: *Types* (Scheduler), user stories 43–46.

**Blocked by:** `07-view-options`, `10-option-names-scheduler`, `12-pinned-api-and-converter-roundtrip`

**Status:** done (2026-10-16, a1bda347; follow-up 104fc86a)

- [x] Core and scheduler options set through one scheduler `options()` object both arrive.
- [x] The scheduler's `view(…)` sets a core and a scheduler field without a cast.
- [x] A core calendar's view object has no scheduler accessors (pinned API).
- [x] `SchedulerSerializationTest` covers scheduler options and view values.
- [x] The static checks of 11 in `addon-scheduler` check only the fields the scheduler classes declare. The core test covers the inherited ones, and the core resource is not on the scheduler's test classpath (see the note in 11).
- [x] The scheduler module pins the public methods of its options classes as `OptionsApiTest` in the core does (`getDeclaredMethods`), and runs the value-type round trip of the core `ConverterTest` over the fields the scheduler data classes declare. The core test classes are not on the scheduler's test classpath (see the note in 12).
- [x] The scheduler module also runs the field checks of the core `TypedOptionsSyncTest` (every field set through each of its setters, its JSON at the client) and of `ConverterTest` (converter of each field) over the fields the scheduler data classes declare, so 38–41 get the same guards as the core batches.
- [x] The scheduler data classes carry an `FC-UPDATE` comment that points to the scheduler's option-name resource (from 10).

## Notes from the implementation

- `FullCalendar` creates its data objects and its options object through `createCalendarOptions()`, `createViewOptions()` and `createOptions()`. They run while the core fields are initialized, before the scheduler's constructor, so the overrides only call `new`. `FullCalendarScheduler.options()` casts `super.options()`, because the core constructor already calls `options()`.
- The core accessors `calendarOptions()` and `viewOptions()` of the options objects are package-private, so the scheduler's options objects delegate to the same instances, cast to the scheduler types. Per-view instances are created through `createViewOptions()` too.
- The scheduler's `@Delegate` excludes the core data classes (`excludes = CalendarOptions.class` / `ViewOptions.class`), so the core accessors are inherited and not generated again (maintainer, 2026-10-16: avoid the duplicates). The pinned lists of the scheduler show only the scheduler accessors and the covariant `view(…)` with its bridge method, so a new core field changes only the core lists.
- Scheduler tests: `SchedulerOptionFieldsStaticTest` (the name clash check includes the core fields, which are on the classpath), `SchedulerOptionsApiTest` with `FullCalendarSchedulerOptions-api.txt` and `FullCalendarSchedulerViewOptions-api.txt`, `SchedulerConverterTest`, `SchedulerTypedOptionsSyncTest` (also ports the client-handler guard; it reads the core `full-calendar.ts`). `VALUE_TYPES`, `ONE_FORM_TYPES` and `PER_FORM_PARAMETERS` start empty. `SchedulerCalendarOptions` has no field yet, so the global branches of these tests run over nothing until 39 adds `refetchResourcesOnNavigate`; the value-type and handler guards bite once a field needs them (`assertNotNull`).
- Maintainer review, 2026-10-16: the factory methods stay `protected`. Third-party subclasses do not get typed options of their own; they use the raw options. Opening the data classes for that (public data classes, protected constructors on the options objects) was rejected as out of scope for a FullCalendar wrapper.


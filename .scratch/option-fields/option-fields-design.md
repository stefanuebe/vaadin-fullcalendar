# Options as typed fields instead of the option API (design, prototype next)

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, needs prototype |
| Status | prototype done and design-reviewed, review R1–R3 decided (2026-10-15). Maintainer is thinking a few points through (2026-10-15); spec being written with `/to-spec` (2026-10-15). Prototype branch is kept. Not on GitHub. |
| Blocked by | – |

---

## Why

`setOption(Option, Object)` takes any value. `setOption(Option.TIMEZONE, new Person())` compiles. The accepted types are only written in the Javadoc. The option API was introduced because writing a setter and a getter for every option was too much work by hand. That reason is gone.

Neither 7.3 nor 8.0 is released, so no user has migrated to the option API yet. "It breaks API" is no argument against this: 8.0 breaks a lot anyway.

## Considered and rejected

- **Nested option groups** such as `options().entry()…` (maintainer's question, 2026-10-15): the library's options are one flat object of about 295 keys; it nests only `headerToolbar`, `buttonText`, `views`, `businessHours` and the format objects, which are value objects here already. The grouping exists only in the library docs, and it is not stable (about 40 categories in v6, a different coarse structure in v7), so an API following it would break on each major update. Grouping by prefix (`event` 30, `day` 30, `slot` 19, then a long tail) would be invented by the add-on. A name equal to the library's makes an option easy to find from the library docs; groups would hide it, make view-option builders longer and add flattening and copy logic. Decided: flat, names 1:1 as in the library, nested only where the library nests. Fields are ordered in sections by topic in the source; every field has Javadoc with an `@see` link to its library docs page (required, maintainer).
- **A base class `CalendarBase` with all fields**, from which `FullCalendar` and the view options derive: `FullCalendar` must extend `Component`, so the base class would too, and the view options would become components. The global options would also have to be split off again. Composition with `@Delegate` avoids both (maintainer, 2026-10-15).

- **Runtime type check in `setOption`** (first version of `option-type-check.md`): fails loudly, but only at runtime.
- **Generic keys `Option<T>`** with `<T> setOption(Option<T>, T)` and an overload `setOption(Option<?>, JsCallback)` that checks an `@JsCallback` annotation on the key. Works (checked with javac). An `Option<Object>` makes the two overloads ambiguous, so every option would need one canonical Java type. Rejected in favour of fields, because fields give real setters and getters.
- **Hand-written setters and getters per option:** about 900 methods. Lombok removes most of them.

## The design

As validated by the prototype (2026-10-15). Options become fields, following the pattern of `Entry`: Lombok accessors, `@JsonName` and `@JsonConverter` on the fields, `BeanProperties` builds the JSON.

```java
@Getter @Setter @SuperBuilder(toBuilder = true) @NoArgsConstructor
public class ViewOptions {            // every option that can apply per view
    @JsonConverter(DayOfWeekConverter.class)
    private DayOfWeek firstDay;
    @JsonName("dayMaxEventRows")
    private Integer dayMaxEntryRows;
    ...
}

@Getter @Setter
public class CalendarOptions {        // global only, no superclass
    @JsonName("locale")
    private Locale calendarLocale;
    ...
}

public class FullCalendar extends Component {
    @Delegate(excludes = NotDelegated.class) private CalendarOptions options() { scheduleSync(); return options; }
    @Delegate(excludes = NotDelegated.class) private ViewOptions viewDefaults() { scheduleSync(); return viewDefaults; }

    public void setViewOptions(CalendarView view, ViewOptions options) { ... }   // stores a copy
    public ViewOptions getViewOptions(CalendarView view) { ... }                 // a copy, or empty options
    public void setRawOption(String option, Object value) { ... }               // always wins
}
```

- Each field exists once. The calendar holds one `CalendarOptions` (global) and one `ViewOptions` (the values for all views); each view type gets its own `ViewOptions` copy. Composition, not inheritance: `CalendarOptions` does not extend `ViewOptions`, so the scheduler has no diamond (see the prototype results).
- `@Delegate` (Lombok, `lombok.experimental`) on the two no-arg methods generates the accessors in `FullCalendar`, so the API is `calendar.setFirstDay(…)`, and every call schedules the diff. `excludes` keeps `toBuilder()` off the calendar.
- View-specific options with value semantics: `calendar.setViewOptions(DAY_GRID_MONTH, ViewOptions.builder().dayMaxEntryRows(3).build())`. Global options cannot be set per view, because they live only in `CalendarOptions`.
- `Option`, `SchedulerOption` and the public `setOption` methods go away. `setRawOption(String, Object)` passes what the add-on has no field for and always wins over a field.
- Scheduler: `SchedulerCalendarOptions extends CalendarOptions` and `SchedulerViewOptions extends ViewOptions`; the scheduler delegates to them, which brings the core and the scheduler accessors.
- The diff runs in `beforeClientResponse`, scheduled by every accessor call and by the constructor. It sends the changes as calls and keeps the full state in the element property `initialOptions`, from which a client element creates its calendar.

## Points the prototype answers

1. **Sync to the client:** a diff in `beforeClientResponse` compares the fields with the last sent state and sends only the changes (preferred, keeps Lombok setters), or hand-written setters that mark a change.
2. **Removing:** `null` means "not set". The diff sends a field set to `null` as a removal. Wrapper types (`Boolean`, `Integer`) instead of primitives.
3. **Add-on defaults:** field initial values (for example `editable = true`) instead of the `addonDefaultOptions` map. Fallback order on removal stays: add-on default, initial JSON options, library default.
4. **Callbacks:** render hooks take a value or a `JsCallback`. A second setter per hook, or a second field. About 110 options.
5. **View diff:** each `ViewOptions` instance is diffed and merged into the `views` option, as `setViewSpecificOptions` does today.
6. **Javadoc on fields:** Lombok copies it to the accessors. The `<dt>Type</dt>` blocks drop out, because the type is in the signature.

Prototype scope (maintainer, 2026-10-15): a few options of different types, so each kind of value is tried once. Throwaway (committed later on its own branch on request, see the results).

| Kind | Option | Also shows |
|---|---|---|
| String | `allDayText` | |
| number | `dayMaxEntryRows` (`Integer`) | a view-specific value (first `forView(…)`, later `setViewOptions(…)`) |
| boolean | `editable` (`Boolean`) | the add-on default `true` and removing a value |
| specific object | `firstDay` (`DayOfWeek`, with converter) | a `@JsonConverter` on a field |
| specific object | `businessHours` (`BusinessHours` or an array of them) | a complex value |
| value or `JsCallback` | `dayCellClass` (`String` or `JsCallback`) | the callback setter |
| global only | `locale` (`Locale`) | `CalendarOptions` vs `ViewOptions` |

## Cost beyond the code

- The RN 8.0 note "Why are the typed setters and getters deprecated?" (wiki `8.0`, `e4a6df9`, unpushed) argues that typed methods for all options would be too big. The field design brings typed setters and getters back, so that note (and the whole option-API story of RN/MG 7.3 and 8.0) must be rewritten if the design is implemented. An open proposal from before the prototype (drop its last sentence on the line count) is moot then.

- Release notes and migration guides of 7.3 and 8.0, which present the option API as the one way to configure a calendar (7.3 deprecates the typed setters).
- The wiki page `Calendar-configuration` and every wiki example with `setOption(Option.X, …)`.
- Library updates: a renamed option changes only `@JsonName`. Deprecations of Java names stay as much work as today.

## Prototype results (2026-10-15)

Throwaway code on the local branch `prototype/option-fields`, commits `1d243971`, `be39b364` and `22fafda0` (not pushed): `addon/.../prototype/` (`ViewOptions`, `CalendarOptions`, `OptionFieldsPrototypeCalendar`), the view `OptionFieldsPrototypeView` (route `/option-fields-prototype`) in the demo, package `ui.view.test`, and a guard in `FullCalendar.postConstruct` that skips `setOption(EDITABLE, true)` for the prototype calendar. Clicked through with Playwright in the e2e-test-app.

Works:

- `@Delegate` on a no-arg method `options()` generates all accessors on the calendar, including those inherited from `ViewOptions`. Every delegated call goes through `options()`, which schedules the diff. Lombok setters stay plain.
- The diff in `beforeClientResponse` sends only changed keys. Verified on the client for every type in the scope table: String, Integer, Boolean, `DayOfWeek` (converter), `BusinessHours[]` (converter), `JsCallback`, `Locale` (converter) and the merged `views`.
- Removing: `null` in a field sends a removal. The client then falls back to the initial JSON options over the client defaults of `createInitOptions` (`_optionsWithoutServer`), else the library default.
- Value or callback: two fields with the same `@JsonName` (`dayCellClass`, `dayCellClassCallback`). A set callback wins; removing it brings the string value back. Pure Lombok, no hand-written code. **Superseded by review R1:** one value-typed field (`ClassNameValue`) with hand-written overloads, see below.
- `BusinessHours`: one hand-written varargs setter replaces Lombok's (`setBusinessHours(a, b)`, `setBusinessHours()` removes).
- Field Javadoc is copied to the getter and setter of the options classes (checked with delombok).

Problems found:

1. **Javadoc of the calendar methods (resolved for the IDE).** delombok writes no Javadoc on the methods that `@Delegate` generates on the calendar. IntelliJ shows the field Javadoc for them anyway (maintainer screenshot, 2026-10-15). The project publishes no Javadoc jar, so the IDE is what counts.
2. **A stored `ViewOptions` reference did not sync (resolved).** With `forView(…)` returning the live object, a setter call on a stored reference in a later request sent nothing. Lombok setters have no hook, so neither a change notification nor an exception is possible without hand-written setters. Replaced `forView` with value semantics (maintainer, 2026-10-15): `setViewOptions(view, ViewOptions)` stores a copy, `getViewOptions(view)` returns a copy, or empty options when none are set (no Optional, maintainer), null removes. `ViewOptions` has `@SuperBuilder(toBuilder = true)`: `setViewOptions(MONTH, ViewOptions.builder().dayMaxEntryRows(2).build())`. One object can be set for several views. Checked in the browser: changing the passed object afterwards has no effect; a change via `getViewOptions(…).toBuilder()` and setting it again is sent. `@Value` does not work, because `CalendarOptions` inherits the fields and needs setters. `@Delegate(excludes = …)` keeps `toBuilder()` off the calendar. The copy is shallow; an array value such as `businessHours` is shared.
3. **Name clash with Vaadin (resolved).** A field `locale` generates `getLocale()`, which overrides the protected `Component#getLocale()` and makes it public. Vaadin uses that method for `getTranslation(key)`, so the calendar's translations would follow the calendar locale, or get `null` while it is not set. Renamed to `calendarLocale` with `@JsonName("locale")` (maintainer, 2026-10-15). A reflection check over all non-private methods of `FullCalendar`, its superclasses and interfaces finds the clash with `locale` and none with `calendarLocale`. It must include protected methods: a check over public methods only missed it. The real implementation gets this as a unit test over all fields.
4. **Entry vs. event names (resolved).** The option is `dayMaxEventRows` in the library. Each field with "entry" in its name needs `@JsonName`. The first run sent `dayMaxEntryRows`, which the library ignored with only a console warning. A check compares the JSON name of every field with the option names of the library (maintainer, 2026-10-15). In the prototype the names come from the distinct keys of the `Option` enum (295 from the run; the scheduler fields would need the keys of `SchedulerOption` too). The check passes and, without the `@JsonName`, reports `dayMaxEntryRows`. The local `fc-docs` are no source: the v7 docs have no file per option. Since `Option` goes away, the real implementation needs the names elsewhere, for example a resource file generated once from `Option` and kept current on each library update. Both options classes carry an `FC-UPDATE:` note.
5. **Removal semantics (decided, 2026-10-15): `null` means the library default.** Today, removing `editable` restores the add-on default `true`. With fields, `setEditable(null)` means "not set": the getter returns `null` and the library default `false` applies. A value the add-on wants is only the field's initial value. The maintainer agreed on condition that the server sees it the same way. Server side: the only option value the add-on reads internally is the time zone (`getTimezoneForOffsets`). Client side: `createInitOptions` in `full-calendar.ts` has its own add-on defaults (`timeZone`: `local` with `withAutoBrowserTimezone()`, else `UTC`; `headerToolbar: false`, `weekNumbers: true`, `tableHeaderSticky: true`, `footerScrollbarSticky: true`), and a removed option falls back to them. So these defaults move to field initial values of `CalendarOptions`, and the client sends none of its own; the locale default of the constructor too. Then `null` means the library default on both sides and the getters show the real state. Initial JSON options of the constructor are left out for now (maintainer). The time zone: `null` means `local`, the library default; the topic is handled separately in `option-fields-timezone.md`. Not built in the prototype (none of these options is in its scope).
6. **Two sources of truth (resolved, 2026-10-15): `setRawOption` always wins.** With `setOption(String, …)` next to the fields, `setOption("editable", false)` changed the client but not the field, and the next diff did not see it. Decided (maintainer): the public `setOption` methods go away together with `Option` and `SchedulerOption`. Their replacement has a new name, so old code fails to compile instead of silently changing behaviour: `setRawOption(String, Object)`, `getRawOption(String)`, keys only as strings. A raw option is kept in its own map and laid over the field values in the sync, so it always wins over a field with the same name and goes through the same diff. Null removes it; then the field applies again, including changes made to the field meanwhile. The value is passed on without conversion (`firstDay` takes `1`, not `DayOfWeek.MONDAY`), and the field getter still returns the field value. Documented as an advanced/backup method: it always wins, the caller is responsible. Stays public for now (maintainer). A raw `views` replaces all view options. Needed for options the add-on has no field for (as in earlier versions: a missing option waits for a release or uses this method), custom plugins and subclasses. The scheduler and the sync send through the internal funnel. Checked in the browser.
7. Boolean wrapper fields get `getEditable()`, not `isEditable()` (decided, 2026-10-15: keep it). JavaBeans uses `is` only for the primitive `boolean`; `BeanProperties` and Vaadin's `Binder` find `get…`.

Second round (2026-10-15, maintainer: check the scheduler and detach/attach):

8. **Scheduler: the inheritance `CalendarOptions extends ViewOptions` breaks (resolved by composition).** A `SchedulerCalendarOptions` would have to extend both `CalendarOptions` (global fields) and `SchedulerViewOptions` (scheduler per-view fields). Changed to composition: `CalendarOptions` holds only the global fields; the calendar holds a `CalendarOptions` and a `ViewOptions` for all views and delegates to both. The scheduler replaces them with `SchedulerCalendarOptions extends CalendarOptions` and `SchedulerViewOptions extends ViewOptions` (with `@SuperBuilder`). No field exists twice. Checked in the browser with `slotMinWidth` and `resourceColumnsWidth` (per view) and `resourceOrder` (global): core and scheduler fields on the scheduler, `SchedulerViewOptions.builder()` with core and scheduler fields for one view, `toBuilder()` on a copy. In the real code the sync lives in `FullCalendar`, with protected factory methods for the two options objects that the scheduler overrides; in the prototype it is the helper `OptionFieldsSync`, because the two prototype calendars cannot share a superclass.
9. **Initial values were not sent until an option was touched (fixed).** The scheduler got `editable = true` only with the first click: nothing had scheduled the diff. The constructor now schedules it, so the initial values reach the client on the first attach.
10. **Options that work only at creation had no effect (fixed).** The first diff arrived as a call after the client had created its calendar; `initialDate` was set but the calendar showed today. The sync now also keeps the full state in the element property `initialOptions`, from which the client creates its calendar. Then `initialDate` took effect. Applies to `initialDate`, `initialView`, `now` and any other creation-only option.
11. **Detach and attach (works).** Flow created a new client element on the reattach. After a non-initial attach the sync sends every value again and removes keys removed while detached; the property covers a new element, the calls cover a kept one. Checked: a field changed and one removed while detached, plus view options, arrive after the attach. This would replace the funnel's own bookkeeping (`options`, `initialOptions`, `optionsRemovedWhileDetached`, the resend in `onAttach`), which the prototype still runs alongside.

## Design review of the prototype (2026-10-15)

Three parallel agents: API and type design (Opus), sync and lifecycle (Opus), ticket claims against the code (Sonnet; its corrections are applied above). Read-only, nothing built.

Decisions needed:

- **R1. Options that take one of several types.** Two fields with one JSON name (value + callback) only work for about 110 render hooks, cost two fields each, and the value getter can show a value that does not apply. Other unions cannot be expressed: `businessHours: true` vs `BusinessHours[]`, `dayMaxEvents` boolean or number, `height` number or string, `entryOverlap`/`selectOverlap` boolean or callback, `selectConstraint` string or object, `headerToolbar: false` vs a toolbar object. Either one field per union with a small value type (`DayMaxEvents.of(3)`, `ClassValue.of(String | JsCallback)`), or two fields and accepting getters that can mislead. Must be fixed before 350 fields are written.
- **R2. Constructor JSON options conflict with field initial values.** The client merges `{...initialOptions, ...jsonOptions}`, so JSON `editable:false` wins on the client while the getter says `true`; in the prototype the per-key calls of the first sync then override the JSON. Proposal: at construction, parse known JSON keys into the fields and pass unknown keys to `setRawOption`; drop the client's JSON fallback. Reopens "initial options left out for now".
- **R3. Name clashes beyond `locale`.** `height` (`setHeight(String)` of the calendar / `HasSize`), `class` (Java keyword; `className` would hit `HasStyle#setClassName`), `timeZone` next to the existing `setTimezone` (differs only in case), `resourceAreaColumns` (overloads in `Scheduler`), `validRange` (overload next to `setValidRange(LocalDate, LocalDate)`). The clash test must match names regardless of parameter types and case, against all non-private members including `Scheduler`. A fixed rename rule is needed (e.g. `calendarHeight`, `calendarClass`).

Maintainer decisions on the review (2026-10-15):

- R1: one field per option with a value type, which the getter returns, e.g. `BusinessHoursValue` as a sealed interface with records (`Default` for `true`, `Hours` with a `List<BusinessHours>`); records are immutable, so no shared arrays. Value or callback: one sealed interface per kind of value (e.g. `ClassNameValue permits ClassName, JsCallback`, `OverlapValue permits Overlap, JsCallback`), which `JsCallback` implements, so `setDayCellClass(JsCallback.of(…))` works directly and a `switch` over the getter is exhaustive. A handful of kinds, not one per option. Rejected: a generic `ValueOrCallback<T>`: `JsCallback` cannot implement it for every `T`; the only variant that compiles (`JsCallback implements ValueOrCallback<Object>`, parameter `ValueOrCallback<? super String>`) also accepts `ValueOrCallback.of(new Person())` (checked with javac). `JsCallback` becomes `final` (or `non-sealed`) to be listed in `permits`. Own types for the other unions (`businessHours`, `dayMaxEvents`, `selectConstraint`, `headerToolbar`). Each value type has a converter. Plus hand-written overloaded setters for convenience and migration, e.g. `setDayCellClass(String)`, `setDayCellClass(JsCallback)`, `setBusinessHours(BusinessHours...)`. There is no setter taking the value type, to keep the visible API small (maintainer, 2026-10-15): the public overloads set the field directly, and the public `JsCallback` overload is needed. The getter (Lombok, public) returns the value type, and the view-options builder takes it (`.dayCellClass(ClassName.of("x"))` or `.dayCellClass(JsCallback.of(…))`). `setDayCellClass(null)` is ambiguous between the overloads; accepted as a usual Java issue, no remove methods for now. Accepted cost (maintainer, 2026-10-15): for the about 140 options with more than one type, Lombok gives only the field and the getter; the overloads (about 280) are hand-written, each with its own Javadoc (a `{@link}` to the getter avoids a second description). The about 210 options with one type stay pure Lombok (maintainer: hand-written code is fine). Lombok generates no setter once any method with that name exists, so the setter taking the value type is hand-written too; the getter stays Lombok. `@Delegate` passes all overloads on to the calendar.
- R2: the constructor JSON options become an expert feature like `setRawOption`: they bypass the fields and win over them. Parsing them into the fields is not trouble-free: only 10 of the 21 converters implement `toServerModel`, and JSON callbacks have no field form. So the constructor JSON is stored as raw options; the getters show the field values.
- R3: not a real issue, solved by naming. A clashing option gets the prefix `calendar` (as `calendarLocale`; e.g. `calendarClass`). The clash test stays. `height` gets no field: the size comes from Vaadin's `HasSize` (the add-on sets 100% internally); changing the library's own `height` option goes through `setRawOption`.

Directions to apply in the implementation (no decision needed unless the maintainer objects):

- **R4. Order of client calls.** Options now arrive in `beforeClientResponse`, after immediate calls: `setValidRange(r); gotoDate(d)` navigates under the old range. Also `changeView`, `scrollToTime`, `select`, event sources, and on reattach `restoreStateFromServer` and the scheduler's resource re-add. Fix: a `flushOptions()` that runs the diff at once, called first by every method that calls the client.
- **R5. Options the add-on handles specially.** `eventDidMount` (merged with native listeners and the auto entry id), `resourceColumns` (binding in the setter), `height`. A protected hook in the diff where the calendar contributes computed values; hand-written setters where the setter must act.
- **R6. Builder ignores field initial values.** All fields of `ViewOptions` start as null; add-on defaults go only onto the calendar's own instance in its constructor (as the prototype does for `editable`). Otherwise a copy from `getViewOptions` can pin a default per view.
- **R7. View-options type safety.** `setViewOptions(CalendarView, ViewOptions)` stays callable on the scheduler; a plain `ViewOptions` there makes the overridden getter throw `ClassCastException`, and `SchedulerViewOptions` on a core calendar sends scheduler keys. Copy into a fresh instance from the calendar's factory via `BeanProperties`, rejecting unknown fields; then one base-typed setter suffices.
- **R8. First sync and resends.** Use `ExecutionContext.isClientSideInitialized()`: while false, write only the property; when true, send one `setOptions(changes)` call, no property rewrite. Drop `resendAllOnNextSync`: Flow keeps the property for a recreated element, and `lastSent` stays valid for a kept one. Swap the merge order in `initCalendar` so the property wins. Avoids sending every value twice and re-evaluating callbacks.

Minors for the implementation:

- Collections as immutable `List.copyOf`, no arrays (the shallow copy shared `BusinessHours[]`; the builder bypassed the empty-array rule).
- Every public method of an options class becomes calendar API through `@Delegate`: keep them to plain accessors, test the list of delegated methods.
- Getters schedule a sync and touch the state tree, which needs the session lock: schedule only on setters (separate delegates) or only when the lock is held.
- Convert `setRawOption` values at once, so a bad value fails at the caller, not in the response.
- Serialization: protected factory methods instead of `Supplier`/`Consumer` fields; `SerializationTest` with view options and a `JsCallback`; `@EqualsAndHashCode(callSuper = true)` on subclasses.
- Library updates: a retyped option changes a generated signature; a deprecated alias is a hand-written getter/setter pair forwarding to the new field.
- Changing one view's options resends the whole `views` object (acceptable).
- Not an issue: `syncScheduled` cannot get stuck; Flow keeps `beforeClientResponse` entries over a detach.

## Options behind `options()` (maintainer, 2026-10-15)

The option accessors move off `FullCalendar` into an options object: `calendar.options().setEditable(true)`. Reason: `calendar.` in the IDE lists only calendar methods, and it is clear which methods are options.

- The calendar creates the object once in its constructor and passes itself in. The object does what the prototype calendar does today: a private accessor that schedules the sync, with `@Delegate` onto the data classes (global fields and the fields for all views). Every setter of the object therefore schedules the sync itself, so a kept reference (`var o = calendar.options();` and a setter call in a later request) still reaches the client. No diff on every response, no hand-written setters beyond the overloads from R1.
- `options()` returns the live object. The data class passed to `setViewOptions` stays a plain value class, copied on set and get.
- Side effect: no name clash with `Component` any more, so R3's `calendar…` prefix is dropped (`locale` instead of `calendarLocale`).
- `height` becomes a field again (maintainer, 2026-10-15). Its Javadoc says explicitly what it does: it sets the library option `height`, which overrides the size set through `HasSize` on the calendar.

## Prototype state (2026-10-15, for a fresh start)

- Branch `prototype/option-fields` (local, never push, not for master): `1d243971` (fields, diff, setRawOption, calendarLocale), `be39b364` (composition, scheduler, initialOptions property, detach/attach), `22fafda0` (`dayCellClass` as `ClassNameValue` with String/JsCallback overloads, `JsCallback` final). The final `dayCellClass` form was clicked in the browser on 2026-10-15 (string, callback, removal, per view via builder: all reach the client). **Keep the branch** until the implementation is done (maintainer, 2026-10-15); it is the reference for the spec.
- Uncommitted on that branch: a blank line in `OptionFieldsSchedulerPrototypeView` (not Claude's, left alone); untracked in the repo root: the maintainer's screenshot `Bildschirmfoto_20261015_130244.png` and `github-support-reply.md` (draft reply to GitHub Support, see memory `gh-bot-account`). Whether to delete the two files is unanswered; don't delete without asking.
- Demo views: `/option-fields-prototype`, `/option-fields-scheduler-prototype`. Start: `mvn -o install -pl addon,addon-scheduler -DskipTests`, then `cd demo && mvn -o spring-boot:run -Pproduction`; reachable at http://172.17.0.2:8080. Stopped at the end of the session.
- Throwaway checks in the scratchpad (not in the repo): clash check over all non-private members incl. protected (`ClashCheck.java`), JSON name check against `Option` keys (`NameCheck.java`). The real implementation gets them as unit tests (see R3 and point 4).

## Per-view options as `options().view(…)` (maintainer, 2026-10-15)

Replaces the value semantics (`setViewOptions`/`getViewOptions` with copies, builder, `toBuilder()`) decided under problem 2: `calendar.options().view(DAY_GRID_MONTH).setDayMaxEntryRows(3)`, plus `view(…).clear()`. The view object holds the calendar and the view key and schedules the sync like `options()`, so the kept-reference problem that killed `forView(…)` is gone. Details in `option-fields-spec.md`.

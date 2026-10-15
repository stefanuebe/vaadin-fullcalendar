# Spec: calendar options as typed fields

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged; publish when the bot account is back) |
| Labels | enhancement, ready-for-agent |
| Status | reviewed 2026-10-15 (5 full rounds + 1 targeted round); then changed to `options().view(…)` + `clear()` (maintainer) and reviewed in a targeted round, no major left; broken down into 56 tickets in `issues/` (2026-10-15) |
| Blocked by | – |
| Sources | `option-fields-design.md` (design, prototype, review R1–R8), `option-fields-timezone.md`, `option-fields-timezone-client.md`, `option-fields-defaults.md`. Prototype: local branch `prototype/option-fields` (kept until this is implemented) |

---

## Problem Statement

A Vaadin developer configures a calendar with `setOption(Option, Object)`. The method takes any value: `setOption(Option.TIMEZONE, new Person())` compiles, and the accepted types are written only in the Javadoc. A wrong value shows up at runtime at best, often only as a console warning in the browser. The getter `getOption(Option)` returns `Optional<Object>`, so reading a value back needs a cast.

In the IDE, `calendar.` lists several hundred methods: calendar methods, Vaadin component methods and option methods mixed together. A developer cannot see which of them are options.

The add-on also overrides some library defaults (time zone `UTC`, `editable`, week numbers, sticky headers). A developer who reads the FullCalendar docs gets a different calendar than the docs describe, and removing an option restores the add-on's value, not the library's.

The option API was introduced because a typed setter and getter for every option was too much work by hand. With Lombok that reason is gone. Neither 7.3 nor 8.0 is released, so no user has migrated to the option API yet.

## Solution

Every FullCalendar option the add-on supports becomes a typed field with a setter and a getter. The fields live in an options object that the calendar returns from `options()`:

```java
calendar.options().setEditable(true);
calendar.options().setFirstDay(DayOfWeek.MONDAY);
calendar.options().setDayCellClass("weekend");
calendar.options().setDayCellClass(JsCallback.of("info => info.isToday ? 'today' : ''"));
```

Options for one view type work the same way, on the view's options object:

```java
calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayMaxEntryRows(3);
```

`null` means "not set": the library default applies, on the client and in the getter. The add-on no longer overrides library defaults, apart from the few it needs for its integration.

For an option the add-on has no field for, `setRawOption(String, Object)` passes a value by its library name. It is documented as an expert feature and always wins over a field.

`Option`, `SchedulerOption` and the `setOption`/`getOption` methods go away. Old code fails to compile instead of silently changing behaviour.

## User Stories

1. As a Vaadin developer, I want to set a calendar option with a typed setter, so that the compiler rejects a value of the wrong type.
2. As a Vaadin developer, I want a typed getter for every option, so that I can read a value back without casting.
3. As a Vaadin developer, I want all options behind `calendar.options()`, so that code completion on `calendar.` shows only calendar methods.
4. As a Vaadin developer, I want code completion on `calendar.options().` to list every option, so that I can discover what is configurable.
5. As a Vaadin developer, I want each option to carry the name it has in the FullCalendar docs, so that I can find it from the library docs and the other way round.
6. As a Vaadin developer, I want options whose library name contains "event" to use "entry" instead, so that the names match the add-on's domain language (Entry).
7. As a Vaadin developer, I want the Javadoc of each option to link to its page in the library docs, so that I can read the full behaviour there.
8. As a Vaadin developer, I want to keep a reference to `calendar.options()` and change it in a later request, so that I do not need to call `options()` before each change.
9. As a Vaadin developer, I want a changed option to reach the browser in the same response, so that the calendar updates without extra calls.
10. As a Vaadin developer, I want only the changed options to be sent, so that a change does not resend the whole configuration.
11. As a Vaadin developer, I want to set an option to `null`, so that the library default applies again.
12. As a Vaadin developer, I want the getter to return `null` for an option I have not set and the add-on gives no initial value, so that the getter shows what really applies.
13. As a Vaadin developer, I want to set options before the calendar is attached, so that the calendar is created with them.
14. As a Vaadin developer, I want options that only work at creation (`initialDate`, `initialView`, `now`) to take effect, so that I can set them like any other option.
15. As a Vaadin developer, I want options changed while the calendar is detached to apply after it is attached again, so that detaching does not lose configuration.
16. As a Vaadin developer, I want the calendar to keep its options when Flow creates a new client element (for example with `@PreserveOnRefresh`), so that a reload shows the same calendar.
17. As a Vaadin developer, I want to set options for one view type through `options().view(view)`, so that month view and week view can differ.
18. As a Vaadin developer, I want the view's options object to work like `options()`, including a kept reference, so that I learn one way for both.
19. As a Vaadin developer, I want the getter on `options().view(view)` to return the value set for that view (`null` if none, even when a calendar-wide value exists), so that I can tell a view override from the calendar value.
20. As a Vaadin developer, I want a view value to override the calendar-wide value only while that view is shown, so that the calendar-wide value stays the default.
21. As a Vaadin developer, I want to set a view value to `null`, so that the view falls back to the calendar-wide value for that option.
22. As a Vaadin developer, I want `options().view(view).clear()`, so that a view falls back to the calendar-wide values for all options at once.
23. As a Vaadin developer, I want global-only options to be missing from the view's options object, so that I cannot set an option per view that the library or the add-on applies only globally.
24. As a Vaadin developer, I want an option that takes either a value or a callback (render hooks such as `dayCellClass`) to have one setter for each, so that both forms are typed.
25. As a Vaadin developer, I want the getter of such an option to return a sealed type, so that a `switch` over it covers every case.
26. As a Vaadin developer, I want options with several value forms (library names `businessHours`, `dayMaxEvents`, `selectConstraint`, `headerToolbar` and similar) to have typed overloads, so that each form is expressible without raw JSON.
27. As a Vaadin developer, I want a list value (for example business hours) to be immutable once set, so that a later change to my list does not bypass the calendar.
28. As a Vaadin developer, I want to set an option the add-on has no field for with `setRawOption`, so that I am not blocked until the next release.
29. As a Vaadin developer, I want a raw option to win over a field with the same name (except `timeZone`, which only the field sets), so that I can override the add-on's conversion when I need to.
30. As a Vaadin developer, I want removing a raw option to bring the field value back, so that a temporary override is reversible.
31. As a Vaadin developer, I want a bad raw value to fail when I set it, so that the stack trace points to my code and not to a later response.
32. As a Vaadin developer, I want JSON options passed to the constructor to keep working (as raw options, apart from `views` and `timeZone`), so that existing JSON configuration still applies.
33. As a Vaadin developer, I want the calendar to send pending options before it calls the client directly (for example `options().setValidRange(…)` followed by `gotoDate`), so that the call runs under the new options.
34. As a Vaadin developer, I want the time zone to default to the browser's zone, as in the library, so that times show in the user's local time without configuration.
35. As a Vaadin developer, I want to set `Timezone.LOCAL`, so that the calendar follows the browser's zone explicitly.
36. As a Vaadin developer, I want `options().getTimeZone()` to return the concrete zone the browser reported (`LOCAL` before the report), so that I know which zone the server converts with.
37. As a Vaadin developer, I want `options().setTimeZone(null)` to be rejected, so that the time zone is never undefined on the server.
38. As a Vaadin developer, I want the offset helpers to use UTC until the browser has reported its zone, so that code in the view constructor gets a defined result.
39. As a Vaadin developer, I want `editable`, `weekNumbers` and the sticky options to start with the library defaults, so that the calendar behaves as the FullCalendar docs describe.
40. As a Vaadin developer, I want the add-on to keep the FullCalendar header toolbar off, so that navigation stays with Vaadin components and the server knows the current date range.
41. As a Vaadin developer, I want `setHeight`/`setHeightFull` (`HasSize`) to size the calendar as today, so that sizing works like in every other component.
42. As a Vaadin developer, I want the calendar's `getLocale()` to keep its Vaadin meaning, so that translations and sizing work as in every other component.
43. As a Vaadin developer using the scheduler, I want the scheduler options on the same `options()` object as the core options, so that I configure both the same way.
44. As a Vaadin developer using the scheduler, I want `options().view(view)` to offer the core and the scheduler per-view options without a cast, so that one object configures one resource view.
45. As a Vaadin developer, I want the per-view options of a calendar to be reachable only through that calendar, so that no detached option object can be passed between calendars by mistake.
46. As a Vaadin developer using a core calendar, I want scheduler-only options to be missing from its view options object, so that I cannot send keys the core library ignores.
47. As a Vaadin developer, I want my calendar to stay serializable with options, view options and callbacks set, so that session replication keeps working.
48. As a Vaadin developer upgrading to 8.0, I want the migration guide to map each `Option` constant and each removed method to its replacement, so that I can migrate mechanically.
49. As a Vaadin developer upgrading to 8.0, I want the migration guide to list every dropped add-on default with the line that restores it, so that my calendar looks as before.
50. As a Vaadin developer, I want the wiki examples to use the new API, so that I can copy them.
51. As the add-on maintainer, I want a test that fails when a field's JSON name is not a library option name, so that a typo or a missing `@JsonName` does not ship.
52. As the add-on maintainer, I want a test that fails when a field name is a Java keyword or an options method overrides an `Object` method, so that a library name never breaks the generated accessors.
53. As the add-on maintainer, I want a test over the methods the options object exposes, so that no builder or helper method leaks into the public API.
54. As the add-on maintainer, I want a library update to change only `@JsonName` for a renamed option, so that updates stay cheap.
55. As the add-on maintainer, I want `FC-UPDATE:` notes on the options classes, so that a library update checks them.
56. As a subclass author, I want a protected hook to contribute computed option values to the sync, so that options the add-on handles specially keep working.
57. As a Vaadin developer, I want `entryDidMount` to keep working together with native entry listeners and the automatic entry id, so that setting it does not break either.
58. As a Vaadin developer using the scheduler, I want component resource columns to keep their binding when set through the options object, so that they render as today.
59. As a Vaadin developer, I want a calendar that follows the browser zone to follow it again after a reattach or reload, so that `LOCAL` is not frozen to the first zone it saw.

## Implementation Decisions

### Types

- **`ViewOptions`** (data class, not public API): every option that can apply per view. Lombok `@Getter`, `@Setter`. All fields start as `null` (R6); the calendar holds one instance for all views (behind `options()`) and one per view type that has been accessed. No builder, no copies. Fields are wrapper types (`Boolean`, `Integer`), not primitives. Boolean getters are `getWeekends()` (JavaBeans uses `is` only for the primitive).
- **`CalendarOptions` data**: the global-only options. No superclass; composition, not inheritance (the scheduler would otherwise need a diamond).
- **The options object** returned by `calendar.options()`: class `FullCalendarOptions`, created once in the calendar's constructor, holds a reference to the calendar. It exposes the accessors of both data classes through Lombok `@Delegate` on private no-arg accessors that schedule the sync. Every call on it, setter or getter, schedules the sync, including calls on a kept reference; a getter call costs one empty diff. It is live, not a copy. Options need the session lock like any other component access; the Javadoc of `options()` says so. Rejected: separate delegates for setters and getters, because `@Delegate` filters only by hand-written interfaces listing every method.
- **Scheduler**: `SchedulerViewOptions extends ViewOptions` and `SchedulerCalendarOptions extends CalendarOptions`. `FullCalendarSchedulerOptions extends FullCalendarOptions`; `FullCalendarScheduler` overrides `options()` covariantly. The calendar creates its data objects through protected factory methods that the scheduler overrides (no `Supplier` fields, for serialization); the scheduler's options object delegates to the same instances, cast to the scheduler types. Its `@Delegate` regenerates the core accessors as overrides; the pinned-method test expects that. `FullCalendarSchedulerOptions` also overrides `view(…)` to return `FullCalendarSchedulerViewOptions`.
- **The view's options object** returned by `options().view(CalendarView)`: class `FullCalendarViewOptions` (scheduler: `FullCalendarSchedulerViewOptions extends FullCalendarViewOptions`). It holds the calendar and the view's client-side value (a `String`; `CalendarView` is not `Serializable`), is `Serializable` and has no public constructor, and exposes only the per-view accessors, through `@Delegate` on a private accessor that gets or creates that view's data instance and schedules the sync; so it behaves like `options()`, a kept reference included. It may be created on each call; it holds no state of its own. `clear()` removes that view's data instance and schedules the sync; on a custom view it removes only the options set through `view(…)`, never the view's own definition. An empty view instance (created by a getter) adds no `views` entry. A core calendar's view object has no scheduler accessors, so scheduler keys cannot reach a core calendar (replaces the rejection check of R7).
- **Data classes** (`CalendarOptions`, `ViewOptions` and their scheduler subclasses) are not public API; users see only the options objects. The scheduler module reaches them through the shared package on the classpath; the add-on has no `module-info`, and a JPMS module setup would break this split package.
- **Which options are global.** The library documents no list, and its refiner sets do not tell it either: any option can appear under `views`, and the view refiners include the calendar-only and the listener ones. Two groups are global:
  - *Read only from the calendar options by FullCalendar 7.1:* the inputs of `buildDateEnv` (`timeZone`, `locale`, `weekNumberCalculation`, `firstDay`, `weekTextLong`, `weekTextShort`), of the toolbar (`headerToolbar`, `footerToolbar`, `direction`, the button texts and hints), `views`, `plugins`, `initialEvents`, `events`, `eventSources`, `className`, `viewClass`, `viewDidMount`, `viewWillUnmount` (`CALENDAR_ONLY_OPTION_REFINERS`), `initialView`, `initialDate`, `rerenderDelay`, and what the library reads through the calendar context (`buildViewUiProps`: `eventDisplay`, `eventConstraint` and the like; fetching entries: `eventDataTransform`, `lazyFetching`, the request parameters, `googleCalendarApiKey`). A per-view value would have no effect. The option-name resource lists them all, with the functions to look up.
  - *Global by the add-on's choice, although FullCalendar would accept them per view:* the calendar listeners (`eventClick`, `datesSet` and the others; the add-on registers them itself to send server events), and the options the add-on handles itself: `eventDidMount` and `moreLinkClick` (R5 merge and server click handling) and `resourceColumns`.
  The split is recorded in the option-name resource (see *Library updates*), one flag per name with its group as the reason, re-derived from the library source on each update; a check compares the two data classes with it.
- Field shape, from the prototype (trimmed). The prototype delegates on the calendar itself; `options()`, `options().view(…)`, R4, R5 and R8 are not prototyped (the prototype still has `setViewOptions` with copies):

```java
@Getter @Setter
class ViewOptions implements Serializable {
    @JsonName("dayMaxEventRows")              // "event" in the library, "entry" in Java (as in the prototype)
    private Integer dayMaxEntryRows;

    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellClass;      // sealed: ClassName | JsCallback

    public void setDayCellClass(String classNames) { ... }   // hand-written overloads,
    public void setDayCellClass(JsCallback callback) { ... } // no setter taking ClassNameValue
}
```

### Names

- Flat, 1:1 with the library option names, nested only where the library nests (`headerToolbar`, `buttonText`, `views`, `businessHours`, format objects). No option groups.
- Where the library says "event", the Java name says "entry" and `@JsonName` carries the library name.
- No `calendar…` prefix: the options object is not a component and the calendar no longer delegates to it, so a field `locale` (`getLocale()`) or `className` clashes with nothing. `calendar.getLocale()` keeps its Vaadin meaning. The library option is `className` (also accepted as `class`); the field is `className`.
- Name checks: Java keywords and `Object` methods (no other clash is possible), and no field name contains "event" in any case (`@JsonName` may).
- Fields are ordered in sections by topic in the source. Every field has Javadoc with an `@see` link to its library docs page (required); Lombok copies it to the accessors of the data classes; the methods `@Delegate` generates on `FullCalendarOptions` carry none in delombok, but IntelliJ shows the field Javadoc for them (checked in the prototype, `option-fields-design.md` problem 1). The type blocks (`<dt>Type</dt>`) of today's `Option` Javadoc are not carried over, the type is in the signature.

### Options with more than one value form (R1)

- One field per option. Its type is a value type the getter returns.
- Value or callback: one sealed interface per kind of value, which `JsCallback` implements (for example `ClassNameValue permits ClassName, JsCallback`, `OverlapValue permits Overlap, JsCallback`). A handful of kinds, not one per option. `JsCallback` becomes `final`.
- Other unions get their own types (library names; `businessHours`: `true` vs a list of hours; `dayMaxEvents`: boolean or number; `selectConstraint`: string or object; `headerToolbar`: `false` vs a toolbar).
- Every value type has a converter.
- Setters are hand-written overloads for each form (`setDayCellClass(String)`, `setDayCellClass(JsCallback)`, `setBusinessHours(BusinessHours...)`). There is no public setter taking the value type. `setDayCellClass(null)` is ambiguous between overloads; accepted.
- Size: about 140 options with more than one form, about 280 hand-written overloads with their own Javadoc (a `{@link}` to the getter avoids repeating the description). About 210 options with one form stay pure Lombok.
- Collections are immutable (`List.copyOf`), no arrays.

### Sync

- The sync runs in `beforeClientResponse`, scheduled by every setter and by the constructor. It builds the JSON of the global data, the all-views data, the per-view data (merged into `views`) and the raw options, and compares it with the last sent state.
- While the client is not initialized, it writes the full current state to the element property `initialOptions`, from which the client creates its calendar, and sets the last sent state to it. Afterwards it sends the changes in one call and does not rewrite the property (R8). The calendar keeps an `initialStateSent` flag: set as soon as the initial state is written to the property, cleared on a detach that is not followed by an attach in the same round trip, so a client element Flow keeps still receives calls. The check is a `beforeClientResponse` registered on the UI node at detach, which tests `isAttached()` of the calendar: a task registered on the detached calendar itself would be postponed until it is attached again (`StateTree.beforeClientResponse`, Flow 25.3). An attach to another UI than the last one also clears the flag: with `@PreserveOnRefresh` the component moves to a new UI, and the old UI may never answer again, so its detach check never runs. Flow keeps the client element when a node is detached and reattached within one round trip: `StateNode.collectChanges` sends no detach then, and `isClientSideInitialized()` stays true. A detach also discards the calendar's queued JavaScript calls for good (`unregister` → `discardPendingJavaScriptInvocations`), so the reattach after a detach always resyncs: the sync sends every value that differs from the empty state, even when the client element is kept. `flushOptions()` runs outside `beforeClientResponse` and reads only this flag and whether the calendar is attached; a sync that sees it set sends calls, because the client element created from the property is the one that receives them. While detached it sends nothing, because the call would wait for the attach and could then reach a new client element that the property already created with the options. Every attach schedules a sync, so a new client element starts from the current state even when nothing changed while detached. `restoreStateFromServer` restores view and date only; it no longer applies options (its `setOptions` call goes away, the sync carries them).
- The server builds the complete `views` object: custom views (`customCalendarViews`, including the anonymous views parsed from the constructor JSON) first, then the per-view options laid over them per view and per key. The client no longer merges `customViews` into later `views` updates.
- The client keeps no fallback of its own: `_optionsWithoutServer` (built from the constructor JSON and the client defaults) and the separate `initialJsonOptions` property go away; plugins and client-side handlers stay in `createInitOptions`. The line `timeZone: autoBrowserTimezone ? 'local' : 'UTC'`, the `autoBrowserTimezone` property, the `customViews` property and its merge in `createInitOptions` go away with them; `setCustomCalendarViews` only fills `customCalendarViews` on the server; its "only once" guard becomes a boolean flag (not a non-empty map check, because the anonymous views from the constructor JSON go into the same map). A removed option gets the library default, a removed raw option the field value. An option the add-on handles with its own event handler (for example `viewDidMount`) gets that handler back, so the server keeps receiving the event. `headerToolbar: false` lives only on the server, as an initial value.
- A key that is no longer present is sent as a removal.
- `flushOptions()` runs the sync at once. Every method that calls the client directly (`gotoDate`, `changeView`, `next`, `previous`, `today`, `scrollToTime`, `render`, entry sources, the reattach restore, the scheduler's resource re-add) calls it first (R4).
- A protected hook lets the calendar contribute computed values to the sync, for options the add-on handles specially (R5). It is called in every sync after the fields and before the raw options and may add or replace keys. Users: `entryDidMount` (`@JsonName("eventDidMount")`, merged with native entry listeners and the auto entry id), `resourceColumns` (component columns keep their binding; the setter stays hand-written on the scheduler's options object), `height` from `HasSize`, and `moreLinkClick` (value type: a `MoreLinkClickAction` or a `JsCallback`; the client's special handling, today `_moreLinkClickWithoutServer`, is checked against the dropped fallback).
- This replaces the existing option bookkeeping in the calendar (`options`, `initialOptions`, `optionsRemovedWhileDetached`, the resend in `onAttach`, `addonDefaultOptions`).
- Changing one view's options resends the whole `views` object; accepted. The client keeps the evaluated value of every view option whose JSON did not change, so the code of a callback in an unchanged view runs once.

### View options

- `options().view(view)` replaces the value semantics of the prototype (`setViewOptions`/`getViewOptions` with copies, builder, `toBuilder()`), maintainer 2026-10-15. Reason: one way for global and per-view options, kept references work, no copy logic.
- A view value overrides the calendar-wide value only in that view; the getter returns only the view's own value. `null` removes one value, `clear()` all values of that view.

### Raw options

- `setRawOption(String, Object)`, `getRawOption(String)` (returns `Optional`, as in the prototype). Kept in their own map and laid over the field values, so a raw option always wins and goes through the same diff. `null` removes it; the field applies again, including changes made meanwhile.
- The value is converted to JSON at once, so a bad value fails at the caller. It is passed on without the field's converter (`firstDay` takes `1`, not `DayOfWeek.MONDAY`). The field getter keeps returning the field value.
- A raw `views` replaces the whole generated `views` object.
- JSON options passed to the constructor are stored as raw options (R2), with two exceptions: `views` is parsed into custom views as today, and `timeZone` goes into the field (`"local"` becomes `LOCAL` with the flag set). This is new: master special-cases only `local` and leaves a concrete id as raw JSON, so the server converted with UTC. `setRawOption("timeZone", …)` is rejected with a pointer to the field, because the server converts entry times with the field. The getters show the field values.
- Public, documented as an expert feature: it bypasses the type safety and the caller is responsible.

### Time zone

- The field is `timeZone` of type `Timezone` (`options().setTimeZone(…)`). `Timezone` gets a constant `LOCAL` (client value `local`). The field's initial value is `LOCAL`, the library default; the hand-written setter rejects `null`.
- `handleTimeZoneChange` on the client drops its own `refetchEvents()` and keeps the `timezone-changed` dispatch: on a zone change FullCalendar already refetches every source that needs a range (`reduceEventSourcesNewTimeZone`), the add-on's function source included.
- `LOCAL` is defined as `new Timezone(ZONE_ID_UTC, "local")`: the offset helpers convert with UTC, and `equals` keeps it apart from `UTC`.
- `LOCAL` is resolved: setting it sets a follow-browser flag; no client request is needed, because the client reports its zone on every connect; when the browser reports (`@ClientCallable` `setBrowserTimezone`), the field gets the concrete zone. While the flag is set, the sync puts `local` for `timeZone` into the JSON before it compares with the last sent state (otherwise a report would show up as a change), so a report changes nothing on the client: no zone switch, no refetch (FullCalendar treats `'local'` to a named zone as a change and refetches, `option-fields-timezone-client.md`). A concrete zone set explicitly is sent as it is. Setting a concrete zone clears the flag. While the flag is set, every report (reattach, reload in another zone) replaces the zone. If the browser zone is already known when `LOCAL` is set, it applies at once. The client already reports its zone on every connect; the server now accepts the report by default, because the flag starts set. The flag and the `local` rule of the sync live in `FullCalendarOptions`/the calendar, not in the data class; `setTimeZone` on `FullCalendarOptions` is hand-written (rejects `null`, sets or clears the flag). `withAutoBrowserTimezone()` becomes a synonym for `options().setTimeZone(Timezone.LOCAL)`.
- Until the browser reports, the offset helpers convert with UTC. The gap covers only server code that runs before the first round trip; the Javadoc of `LOCAL` says so. No extra code.

### Add-on defaults

- Dropped, the library default applies: `timeZone` (was `UTC`), `editable` (was `true`), `weekNumbers` (was `true`), `tableHeaderSticky` and `footerScrollbarSticky` (were `true`).
- Kept: `headerToolbar: false` (navigation belongs to Vaadin components; FullCalendar's toolbar navigates on the client without the server), the `locale` fallback `CalendarLocale.getDefaultLocale()`, `setHeightFull()` (Vaadin sizing), `resources: []` in the scheduler client (technically needed). Kept defaults are initial values on the server: field initializers in the global data class, which is never copied (R6: `ViewOptions` fields always start `null`). The client line for `headerToolbar` goes away.
- The client's `createInitOptions` sets none of the dropped options.

### Height

- `HasSize` stays as on master: `setHeight`/`setHeightFull` write the library option `height` internally (through the R5 hook once the bookkeeping is gone). A raw `height` (constructor JSON or `setRawOption`) wins over the hook value, and the constructor skips `setHeightFull()` when its JSON has `height`, as on master. No `height` field in this spec, see *Out of Scope*.

### Removed API

- `Option`, `SchedulerOption` and every method built on them: `setOption`, `getOption` (also with `forceClientSideValue`), `getOptionOrDefault`, `removeOption`, `setViewSpecificOption(s)` (replaced by `options().view(…)`), each with its `Option` and its `String` key variants and the `valueForServerSide` overloads (`setOption(Option, Object, Object)`, `setOption(String, Object, JsonItemPropertyConverter...)`, `removeOption(String)`, `getOption(String[, boolean])`, `getOptionOrDefault(String, T)`, `setViewSpecificOption(String, String, Object, …)`), and the scheduler's `setOption`, `removeOption` and `getOptionOrDefault` for `SchedulerOption`. The scheduler's `setResourceColumns` moves to its options object. `withAutoUiLocale()` stays and sets the `locale` field. `allowDatesRenderEventOnOptionChange(boolean)` stays; it controls the client's dates-rendered event on option changes, not an option.
- The typed calendar methods that are deprecated on master in favour of the option API: `setTimezone`/`getTimezone`, `setMaxEntriesPerDay…`, `setMoreLinkClickAction`, `setValidRange…`/`clearValidRange`. Their options become fields.
- The guard for `OptionFieldsPrototype*` in `postConstruct` (prototype only, never on master).

### Migration of callers

- Every caller of the removed API moves to the new one in the same piece of work: add-on internals (constructor, `withAutoBrowserTimezone`, scheduler), the Test views in `e2e-test-app`, the demo views in `demo/` (verified with `mvn -pl demo verify`). The prototype classes and views are not migrated; they stay on the prototype branch.

### Documentation

Part of the same piece of work, on the local wiki branch `8.0`, written as the final state:

- The page `Calendar-configuration` and every wiki example with `setOption(Option.X, …)`.
- Release notes 8.0: the option fields, `options()`, `options().view(…)` (replaces `setViewSpecificOption`), `setRawOption`, the dropped defaults; the note "Why are the typed setters and getters deprecated?" (`e4a6df9`) is rewritten.
- Migration guide 7.x to 8.0: a table mapping each `Option` and `SchedulerOption` constant to its field, a table mapping each removed method (see *Removed API*) to its replacement, every dropped default with its restore line, `LOCAL` as the new time zone default.
- Wiki pages are reviewed per section with three agents, language by Opus, as for all user docs.

### Library updates

- A renamed option changes only `@JsonName`. A retyped option changes a signature. A deprecated alias is a hand-written getter/setter pair forwarding to the new field.
- The list of library option names for the name check is a resource file, generated once per module (core from `Option` in `addon`, scheduler from `SchedulerOption` in `addon-scheduler`) before the enums are deleted, corrected where a key differs from the library (`class` becomes `className`), completed with the global flag from the library source and the add-on overrides, and kept current on each library update. Both options classes carry an `FC-UPDATE:` note.

## Testing Decisions

A good test here sets options through the public API and checks what reaches the client, never internal fields. "Test our wiring, not FullCalendar": whether the library renders a week number is its business; whether `weekNumbers` arrives is ours.

- **Main seam, browserless (addon and addon-scheduler):** set options through `calendar.options()`, `options().view(…)` and `setRawOption`, let the response run, and assert the element property `initialOptions` and the calls sent to the client. Covers: only changes sent; `null` sent as removal; initial values including `LOCAL` and the absence of the dropped defaults; a kept `options()` reference in a later request; a view value sent only under `views`, a kept `view(…)` reference in a later request, the view getter returning only the view's own value, `null` and `clear()` removing view values, `clear()` while detached reaching the client after the reattach, a view getter alone sending nothing, `clear()` on a custom view keeping its definition, the scheduler's `view(…)` setting a core and a scheduler field; raw wins and its removal brings the field back; constructor JSON as raw options, its `views` as custom views and its `timeZone` in the field (`"local"` as `LOCAL`); `setRawOption("timeZone", …)` throws; custom views survive a later view value; a raw `views` replaces the generated `views`;  a bad raw value throws at the call; detach and attach (changes while detached, a reattach with no change still writes the current state); `flushOptions()` before a direct client call, also in the request after the first attach;  immutable list values; `options().setTimeZone(null)` throws; `withAutoBrowserTimezone()` has the same effect as `setTimeZone(LOCAL)`; `LOCAL` follows a second browser report, a concrete zone does not; `LOCAL` set after attach applies a known browser zone at once; offset helpers use UTC and the getter returns `LOCAL` before the report; a report sends nothing to the client while following the browser; the R5 hook output for `entryDidMount` (including the auto entry id), `resourceColumns`, `moreLinkClick` and `height` (`setHeight` and the constructor's `setHeightFull()` reach the client; `setHeightFull()` is skipped when the constructor JSON has `height`, as on master); an unset option without initial value has a `null` getter; core and scheduler options set through one scheduler `options()` object both arrive; a minimal subclass adds and replaces a key through the R5 hook; `headerToolbar` is `false` and `locale` is `CalendarLocale.getDefaultLocale()` in the initial state; `calendar.getLocale()` still returns the UI locale; `withAutoUiLocale()` sets the `locale` field; the scheduler's resource re-add calls `flushOptions()` first; a sealed getter is switched over exhaustively in a test (compile-time check). Prior art: `TypedOptionValuesTest`, `AutoBrowserSettingsTest`.
- **Static checks, unit tests without a calendar instance:**
  - every field's JSON name is in the library option-name resource (one resource per module: core in `addon`, scheduler in `addon-scheduler`; the scheduler checks run there), and global and per-view fields match its flag;
  - no field name contains "event", case-insensitive (domain language; `@JsonName` values may);
  - no field name is a Java keyword and no options method overrides an `Object` method;
  - every field has an `@see` tag in its Javadoc (source scan);
  - the lists of methods `FullCalendarOptions` and `FullCalendarViewOptions` (and their scheduler subclasses) expose are pinned, and neither has a public constructor;
  - every value type round-trips through its converter.
- **Serialization:** extend `SerializationTest` and `SchedulerSerializationTest` with view values, a kept `view(…)` reference and a `JsCallback`.
- **Playwright (e2e-test-app owns its test views):** one option per value form reaches FullCalendar (String, converter, callback, `views`, raw); `initialDate`, `initialView` and `now` take effect; a calendar with no options set has no week numbers, no draggable entries, no FullCalendar toolbar, non-sticky headers at full height and the browser's time zone; component resource columns render after being set through the scheduler's options object; the scheduler starts with an empty resource list; `entryDidMount` and a native entry listener both run; detach and attach with a new client element keeps the options; `LOCAL` resolves to the browser zone; a browser report while following the browser causes no refetch, and an explicit zone change fetches the entries exactly once. Prior art: `option-reset.spec.js`, `detach-reattach.spec.js`, `auto-browser-timezone.spec.js`. The new cases come with their own Test views in `e2e-test-app`. Read the *Testing standards* in `STYLEGUIDE.md` first.
- **Migration of existing tests:** every test using `setOption(Option…)` moves to the new API. Tests of the removed bookkeeping are deleted with it.

## Out of Scope

- Nested option groups (`options().entry()…`): rejected, see `option-fields-design.md`.
- A `height` field on the options object and the precedence between it and `HasSize` (maintainer, 2026-10-15: not important for now). The library option stays reachable with `setRawOption("height", …)`.
- What 7.3 does about the option API: own ticket `option-fields-7.3.md`.
- Parsing constructor JSON into fields: it stays raw (R2).
- Remove methods per option and resolving the `setX(null)` overload ambiguity.
- A published Javadoc jar.
- Backporting to `v7_master`: decided per feature, not part of this spec.
- Publishing the spec and tickets on GitHub while the bot account is suspended.

## Further Notes

- **Documentation.** The wiki branch `8.0` (unpushed) presents the option API as the way to configure a calendar; see *Documentation* for what is rewritten. The topic pages wait for this.
- **7.3** is not released and may still change, as long as nothing breaks against 7.2 (maintainer, 2026-10-15). It must not point its users to the option API that 8.0 removes; what it gets instead is `option-fields-7.3.md`.
- **Prototype.** The local branch `prototype/option-fields` shows the field model working for one option of each kind of value, the scheduler, detach/attach and creation-only options; `options()`, `options().view(…)`, R4, R5 and R8 are not in it. It is not merged; it stays until this spec is implemented.
- **Breaking changes in 8.0** for users who set nothing: times shown in the browser zone instead of UTC, entries not draggable, no week numbers, sticky headers `'auto'` (with the full height effectively off). Each goes into the migration guide with its restore line.

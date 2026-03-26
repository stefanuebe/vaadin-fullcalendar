We know that migrations suck. Not only developers fear the appearance of a new major version but also every
product or project manager of an application, that uses 3rd party software, knows, that it can be a stressful and time consuming horror to
integrate a new major version.

Unfortunately this applies also for this addon. There will be breaking changes here and  there, that requires you to get back
into your code and review everything. Nevertheless, we hope, that this migration guide helps you as good as possible
to get a detailed insight of what has changed and what needs to be done to get you back on track.

If we missed something or anything is unclear, please ping us on GitHub. We hope, that your upgrade goes through
as smoothly as possible.

## Index
* [7.1 > 7.2](#migrating-from-71--72)
* [6.3 > 7.1](#migrating-from-63--71)
* [6.4 > 7.1](#migrating-from-64--71)
* [6.3 > 6.4](#migrating-from-63--64)
* [4.1 > 6.0](#migrating-from-41--60)
* [4.0 > 4.1](#migrating-from-40--41)
* [3.x > 4.0](#migrating-from-3x--40)

## Migrating from 7.1 > 7.2

**Prerequisite:** Vaadin 25.1 (up from 25.0)

### Behavioral change: auto-revert on drop/resize

Starting with 7.2, entries automatically revert to their original position on the client when `applyChangesOnEntry()` is not called in a drop or resize listener. This was previously not the case — the client would keep the new position even without explicit apply, leading to an inconsistent state between client and server.

**If your code already calls `applyChangesOnEntry()` in every drop/resize listener:** No change needed. Behavior is identical.

**If your code intentionally does NOT call `applyChangesOnEntry()` and relies on the client keeping the new position:** Set `calendar.setAutoRevertUnappliedEntryChanges(false)` to restore the previous behavior.

No APIs have been removed in this release.

## Migrating from 6.3 > 7.1

This guide is for users jumping directly from v6.3 (Vaadin 14) to v7.1 (Vaadin 25). It combines the feature
changes introduced in v6.4 with the technical platform changes required for v7.

**Prerequisite:** Vaadin 25

### Feature changes (6.3 → 6.4, carried into 7.1)

The following changes were introduced in v6.4 and are therefore also part of any direct 6.3→7.1 migration.
For full detail and code examples, see the [standalone 6.3 → 6.4 guide](Migration-guide-6.3-to-6.4.md).

**Breaking change — `Entry.overlap` type:**

The `overlap` field changed from a primitive `boolean` (default `true`) to a boxed `Boolean` (default `null`).
The getter name also changed: `isOverlap()` is deprecated, use `getOverlap()`.

```java
// Old (6.3)
boolean overlap = entry.isOverlap();

// New (7.1)
Boolean overlap = entry.getOverlap();
// Or for safe primitive conversion:
boolean value = Boolean.TRUE.equals(entry.getOverlap());
```

`null` now means "inherit the calendar-level setting" rather than always sending `true` to the client.

**Deprecated option setters — use `setOption` instead:**

About 30 individual setter/getter methods are deprecated in favour of the unified `setOption(Option, value)` API.
Examples:

| Deprecated method | Replacement |
|---|---|
| `setFirstDay(DayOfWeek)` | `setOption(Option.FIRST_DAY, DayOfWeek.MONDAY)` |
| `setWeekends(boolean)` | `setOption(Option.WEEKENDS, ...)` |
| `setBusinessHours(BusinessHours...)` | `setOption(Option.BUSINESS_HOURS, ...)` |
| `setSlotMinTime(LocalTime)` | `setOption(Option.SLOT_MIN_TIME, LocalTime.of(9, 0))` |
| `setSlotMaxTime(LocalTime)` | `setOption(Option.SLOT_MAX_TIME, LocalTime.of(17, 0))` |

See the [6.3 → 6.4 guide](Migration-guide-6.3-to-6.4.md) for the complete list.

**New: `JsCallback` for function-valued options:**

Calendar options that accept JavaScript functions are now set via `JsCallback.of(...)`:

```java
calendar.setOption(Option.ENTRY_OVERLAP,
    JsCallback.of("function(stillEvent, movingEvent) { return stillEvent.display === 'background'; }"));
```

Deprecated callback setters (`setEntryDidMountCallback`, `setEntryContentCallback`, etc.) are replaced by
`setOption(Option.ENTRY_DID_MOUNT, JsCallback.of(...))` and equivalent calls.

**New features available from 6.4:**

- RRule-based recurring entries (`entry.setRRule(...)`)
- External event sources (Google Calendar, iCal, JSON feed via `ClientSideEventSource`)
- `ComponentResourceAreaColumn` for the scheduler extension
- 11 new server-side events (e.g. `EntryDragStartEvent`, `ExternalEntryDroppedEvent`)
- New `Entry` fields: `url`, `interactive`

### Technical changes (6.4 → 7.1)

These changes are required when moving from Vaadin 24.10 to Vaadin 25.

#### JSON framework: elemental.json → Jackson 3

v7 replaces elemental.json with Jackson 3 (`com.fasterxml.jackson`). This only affects you if you use these
types directly — for example, in custom converters implementing `JsonItemPropertyConverter`.

| Old (elemental.json) | New (Jackson 3) |
|---|---|
| `JsonObject` | `ObjectNode` |
| `JsonArray` | `ArrayNode` |
| `JsonValue` | `JsonNode` |
| `json.hasKey(key)` | `json.has(key)` / `json.hasNonNull(key)` |
| `json.put(key, value)` | `json.set(key, value)` (for `JsonNode` children) |
| `json.keys()` | `json.propertyNames()` |
| `array.set(array.length(), value)` | `array.add(value)` — Elemental appends via `set(length(), ...)`. Jackson's `add()` appends directly. |
| `JsonUtils.toJsonValue()` | `JsonUtils.toJsonNode()` |
| `JsonUtils.ofJsonValue()` | `JsonUtils.ofJsonNode()` |

**Note:** `CustomCalendarView.getViewSettings()` returns `ObjectNode` (Jackson) instead of `JsonObject` (elemental).
Update any custom view implementations accordingly.

#### BusinessHours API

The `BusinessHours` constructors were removed. Use the fluent static factory API instead:

```java
// Old (6.3) — constructor-based
new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY, DayOfWeek.FRIDAY);

// New (7.1) — fluent API
BusinessHours.builder()
    .start(LocalTime.of(9, 0))
    .end(LocalTime.of(17, 0))
    .dayOfWeeks(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
    .build();
// Or for a standard business week:
BusinessHours.businessWeek().start(LocalTime.of(9, 0)).end(LocalTime.of(17, 0));
```

#### Theme variant rename

The `FullCalendarVariant.LUMO` constant has been renamed to `FullCalendarVariant.VAADIN`. Update any
references in your code.

#### CSS design tokens

If you override the addon's CSS theme variables, update from Lumo tokens to Vaadin unified tokens:

| Old (Lumo) | New (Vaadin unified) |
|---|---|
| `--lumo-base-color` | `--vaadin-background-color` |
| `--lumo-body-text-color` | `--vaadin-text-color` |
| `--lumo-contrast-10pct` | `--vaadin-border-color-secondary` |
| `--lumo-contrast-20pct` | `--vaadin-border-color` |
| `--lumo-space-xs` | `--vaadin-padding-xs` |
| `--lumo-border-radius-s` | `--vaadin-radius-s` |

This only applies if you have custom CSS that references these addon theme variables directly.

#### Aura theme support

v7.1 supports the Aura theme (Vaadin 25). The addon's CSS automatically includes Aura fallbacks — no
action required.

## Migrating from 6.4 > 7.1

This guide is for users already on v6.4 (Vaadin 24.10) upgrading to v7.1 (Vaadin 25). There are no new
addon features to adopt — this migration is purely technical.

**Prerequisite:** Vaadin 25

### JSON framework: elemental.json → Jackson 3

v7 replaces elemental.json with Jackson 3 (`com.fasterxml.jackson`). This only affects you if you use these
types directly — for example, in custom converters implementing `JsonItemPropertyConverter`.

| Old (elemental.json) | New (Jackson 3) |
|---|---|
| `JsonObject` | `ObjectNode` |
| `JsonArray` | `ArrayNode` |
| `JsonValue` | `JsonNode` |
| `json.hasKey(key)` | `json.has(key)` / `json.hasNonNull(key)` |
| `json.put(key, value)` | `json.set(key, value)` (for `JsonNode` children) |
| `json.keys()` | `json.propertyNames()` |
| `array.set(array.length(), value)` | `array.add(value)` — Elemental appends via `set(length(), ...)`. Jackson's `add()` appends directly. |
| `JsonUtils.toJsonValue()` | `JsonUtils.toJsonNode()` |
| `JsonUtils.ofJsonValue()` | `JsonUtils.ofJsonNode()` |

**Note:** `CustomCalendarView.getViewSettings()` returns `ObjectNode` (Jackson) instead of `JsonObject` (elemental).
Update any custom view implementations accordingly.

### CSS design tokens

The addon's built-in CSS uses these Lumo tokens internally. If you have **custom CSS** that references Lumo tokens alongside the addon, be aware that Vaadin 25 introduces unified `--vaadin-*` tokens that work across both Lumo and Aura themes:

| Old (Lumo) | New (Vaadin unified) |
|---|---|
| `--lumo-base-color` | `--vaadin-background-color` |
| `--lumo-body-text-color` | `--vaadin-text-color` |
| `--lumo-contrast-10pct` | `--vaadin-border-color-secondary` |
| `--lumo-contrast-20pct` | `--vaadin-border-color` |
| `--lumo-space-xs` | `--vaadin-padding-xs` |
| `--lumo-border-radius-s` | `--vaadin-radius-s` |

This only applies if you have custom CSS that references these addon theme variables directly.

### BusinessHours API

The `BusinessHours` constructors have been replaced with a fluent builder API:

```java
// Old (6.4)
new BusinessHours(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)

// New (7.1)
BusinessHours.businessWeek().start(LocalTime.of(9, 0)).end(LocalTime.of(17, 0))
```

### Theme Variant Rename

`FullCalendarVariant.LUMO` has been renamed to `FullCalendarVariant.VAADIN`. Update any references.

### Aura theme support

v7.1 supports the Aura theme (Vaadin 25). The addon's CSS automatically includes Aura fallbacks — no
action required.

## Migrating from 6.3 > 6.4

Version 6.4 upgrades the platform from Vaadin 14 to Vaadin 24.10 and backports v7 features. For the full migration guide, see [Migration Guide 6.3 → 6.4](Migration-guide-6.3-to-6.4.md).

**Key changes:**
- **Vaadin 24.10 minimum** (Vaadin 14 no longer supported)
- `overlap` field type changed from `boolean` to `Boolean` (nullable); `isOverlap()` deprecated, use `getOverlap()`
- ~30 convenience methods deprecated in favour of `setOption(Option, value)`
- New features: RRule, JsCallback, event sources, ComponentResourceAreaColumn, 11 new events

## Migrating from 4.1 > 6.0
Depending on your Vaadin version you may need to update also other things, related to Vaadin core.
Steps to be taken there, will not be covered here.

### Removed polymer
Since Polymer has been removed from Vaadin 24, we decided to convert the JS elements to plain, HTML element based
web components. Thus, this version should work with Vaadin 23 and 24. 

For most cases this should not affect your application. However, if you subclasses the javascript classes, you may need
to adapt some of the changes. Any Polymer related feature needs to be converted to plain javascript. If you used
properties, you should still be able to set those via the Flow Java API, but you simply have to declare them as normal
JS class fields. 

Also please note, that the content of the component is now part of the light dom. This will most likely affect
any custom stylings you may have defined. See the next part for details.

### Styling
Since the component is now part of the light dom, you have the advantages and disadvantages of it. 

Any custom styles you may have defined via overriding the JS class or via using the Java method `addCustomStyles`
can now be simply defined via plain css style files. So simply take your css snippets and move them to your global
css styles (however they are structured).

Please be aware, that due to being part of the light dom, any other stylings may bleed into the FC now or vice versa.
But our experience showed, that the majority of our user base preferred the light dom variant, thus we decided to
go this way.

Also since we moved to a new major of the FC library, it might be, that css selectors have changed in their 
provided styles and thus your custom styles might not work out of the box.

### Folder structure and tags
We changed the folder structure and tag names a bit to better represent the Vaadin nature of this addon and to
prevent potential naming conflicts. Therefore the the files will now be placed inside a folder named
`vaadin-full-calendar`. The file names themselves have not changed.

If you subclasses one of the FC JavaScript classes, simply update the folder structure to be something like
`@vaadin/flow-frontend/vaadin-full-calendar/full-calendar`. You may need to add the file suffix ".ts".

Additionally we prefixed the element tag names with a `vaadin-`, so that the FC itself now has the tag 
`vaadin-full-calendar`. This is relevant, if you used the old tag names for styling or dom querying purposes.
In that case update the tag names.

### TypeScript
We migrated the old JS files to TypeScript. But this process is not yet done and thus there are many things still in
progress (mainly the definition of custom types and some code refinement). 

Nevertheless, for the normal use case, this has no effect. If you subclassed the JS classes, feel free to convert
it to TypeScript, but be aware, we do not yet have any fancy TypeScript stuff. But it will come somewhere in future ;)

Things to have in mind when migrating: We use protected and private modifiers of typescript now. Any underscores 
have been removed and the methods / fields have become private or protected. For instance the `_initCalendar()` method
is now named `initCalendar()`. This is one of the most important ones - if you have subclassed the FC, you most likely
have overridden this method and have to update your code. We recommend to use the `override` modifier, so that the 
compiler marks any issues regarding such cases.

#### Issues with webpack
There is a known issue with webpack, that will most likely lead to issues, when starting the application.

Check the "Known Issues" page for details (> Startup issue when using webpack (Vaadin 14-23)).

### No more EagerInMemoryEntryProvider
To make maintenance of the client side a bit easier and less error prone, we decided to remove the 
`EagerInMemoryEntryProvider` and thus have the client side always be in "lazy loading" mode. This means, that there is 
no official way anymore to move all entries to the client side at once.

Of course you still can override this behavior by using simple JS, but we do not recommend to do
that as all infrastructure is built around the fetching mechanisms.

The `LazyInMemoryEntryProvider` has been renamed to `InMemoryEntryProvider`. Rename all imports and class occurrences.
Also the static methods inside EntryProvider have been renamed from `lazyInMemoryFromItems` to `inMemoryFrom`.

Replace any usage of the eager variant with the (now) default one. You also need to call `refreshAll`/`refreshItem` 
after you changed the data in the provider. This behavior aligns to the behavior to the normal Vaadin data provider.

The `updateMethod` of the eager variant has no replacement in the default one, as items are not pushed to the client,
but fetched. When the client shall be updated, call the refresh methods instead.

There are also other methods, that where some variant of `Eager`. Simply replace them with the now default one and
check, if refreshs are necessary in your use cases.

#### Prefetch mode
There is now a prefetch mode, that allows fetching adjacent periods. This shall prevent flickering, when switching
to the previous/next period. See the samples page for details.

### Custom views
Up to now custom views had to be created as a Java class but could only be registered via initial options. To
make life a bit easier here, the interface `CustomCalendarView` has been added plus methods to register those. 

It is recommended to convert your views and use this new interface. While the old way should still work
and lead to `AnonymousCustomCalendarView` instances, a warning will be printed onto the error console.

Check the `CustomCalendarView` docs for additional information. The relevant method is `getViewSettings()`.

Please note, that due to limitations of the FC library, views can only be set at initialization time.

### Entry is "static" again, JsonItem is gone
This part describes a rare use case and should not be affecting most of the FC users.

With 4.x we tried to make the `Entry` structure more dynamic to allow easier definition of new properties and automate
the update mechanism a bit. Unfortunately the approach we took was not practicable in all use cases and we even had
some issues with proxying and mocking mechanisms.

Due to that we decided to go back to the POJO approach as we had it prior to 4.x. This means, if you had extended
the `Entry` class to add properties using the `Key` mechanism, we are sorry, but you will have to convert your
subclass. 

#### Json annotations
Nevertheless, some ideas have survived this rollback. Especially automating the conversion and update process is still
something we do not want to miss. To make life a bit easier, there are now annotations to mark class fields for
automated conversion and allow them to be updated. Check the `Entry` source code and annotations in the `json` 
subpackage, if you want or need to use those annotations.

### Deprecated methods have been removed
#### Calendar Entry CRUD
Replace Calendar Entry CRUD calls with `getEntryProvider().asInMemory()`, e.g. 
```java
// before
calendar.addEntry(entry);

// after
calendar.getEntryProvider().asInMemory().addEntry(entry);
```

We know, this might be cumbersome for use cases, where you only use the in memory provider, but with having a cleaner
api in mind this is a step we need to take. Sorry for the inconvenience.

#### Entry Resource API
Resource methods in `Entry` are now more aligned to other Vaadin item api, so replace for instance `assignResources`
with `addResources`.

#### Other changes
* RenderingMode has been renamed to DisplayMode to align with the FC library and is a top class now. 
  Replace it and related methods respectively.
* Entry's method `copy(Class<T>)` has been renamed to `copyAsType(Class<T>)`.
* Entry's method `getResources` now may return `null`. Use `getOrCreateResources`. We wanted to have the names here being aligned to other namings in Entry.
* `CalendarLocale` is now an enum. This should not affect you in most use cases.
* The option "week numbers within days" is no longer available, week numbers are now always display inside days by the FC library. Simply remove any calls to that setting.
* Since we used the javax @NotNull annotation a lot for better dx, but Vaadin 24 will no longer support that due to Spring Boot 3, we decided to add our own annotation to provide additional information without potential naming conflicts. 


## Migrating from 4.0 > 4.1
### Entry Provider and old CRUD operations
4.1 most important change is the introduction of EntryProviders. Please see the [examples](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/FullCalendar-Examples#entry-providers) for details regarding the different new variants.

By default the FullCalendar uses an `EagerInMemoryEntryProvider`, which behaves the same way as the FullCalendar did before. This means, this should not be a breaking change for your application. Yet we recommend to change to either the `LazyInMemoryEntryProvider` or a callback variant to use the advantages of the new entry providers.

Please note, that the Entry CRUD API on the FullCalendar has been marked as deprecated. It is recommended to replace it with the respective API of the eager in memory provider, if you want to stay with that implementation. See the [examples page](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/FullCalendar-Examples#in-memory-eager-loading) for an example on how that could look like.

## Migrating from 3.x > 4.0
### Introduction
#### Timezones
The most breaking change when migrating from version 3.x to 4.0 will be that the server side got
rid of any timezone inclusion regarding date times. You still may set a timezone for the calendar
or set/get offsetted local date times, but the regular api is now always UTC based and we try to keep
anything as clear as possible regarding whether a local date time represents a UTC time or a time with offset.

Part of this change is
* Getter and Setter of Entry start and end have changed in name and meaning
* Timezones are not applied anymore to the server side times
* Calendar and event time related api is also now UTC based (e.g. finding entries in a timespan)

#### JsonItem
With this version also the foundation of the `Entry` type has changed to a more dynamic, automated way of handling and converting properties, when communicating with the client. This is done in a new class `JsonItem` which `Entry` now extends.

In theory, this should not break your code, unless you have extended the Entry class. In that case please have a look into the examples page or the implementation of Entry to get an idea what has changed. We hope, that we have covered now all basic properties of the client side FullCalendar items and that subclasses are not necessary anymore.

If you still need your subclass, you may have to overhaul the conversion part. For this case it is not really possible to foresee any implementation details and give advices on those, unfortunately.

### Migration steps / manual in detail
#### Timezone
Before heading into all UTC and timezone related changes, the first thing to mention is, that the Timezone class now
has some simple methods to apply or remove the offset of the timezone it represents from a local date time to create
another ((un)offsetted) local date time to help doing things manual. Just to keep in mind, when one of the following
things might seem to be a too breaking change.

#### Entry start / end and timezone.
If are using the `Instant` based start / end api only, theoretically you do not need to change your code.
Please notify that `getStartUTC / getEndUTC` have been deprecated. Replace them at some point with `getStartAsInstant / getEndAsInstant`.

If are using the `LocalDateTime` based versions, you will most likely need to change your code as the `LocalDateTime` based `getStart / getEnd` and
`setStart / setEnd` methods are now always referring to UTC and never to the timezones. If you want to set or get the date time including
the calendar timezone's offset, please use the newly introduced `getStartWithOffset / getEndWithOffset` and `setStartWithOffset / setEndWithOffset`.
They take care of adding/subtracting the timezone's offset - either from the assigned calendar or as parameter.

Since some methods seem to do the same as before, it might feel a bit unnecessary to have the method names changed, but we wanted to assure, that it is always clear, what "kind" of date time the respective method is working with. Either with the default (UTC) or some explicit offset.

```java
// Reading and writing the UTC based backend

Timezone calendarTimezone = ...;
LocalDateTime utcBasedStartDate = ...

// old
entry.setStart(utcBasedStartDate, calendarTimezone);

// new
entry.setStart(utcBasedStartDate);
```

```java
// Editing an existing entry inside an edit form

DatePicker datePicker = ...

// old
datePicker.setValue(entry.getStart());

// ...value changed by date picker

entry.setStart(datePicker.getValue());


// new
datePicker.setValue(entry.getStartWithOffset());

// ...value changed by date picker

entry.setStartWithOffset(datePicker.getValue());
```

The offset variants with the timezone parameter are intended to be used, when working on a new entry, that
is not yet added to a calendar. Here the entry cannot access the calendar's timezone. In this case you should use these methods. In all
other cases we recommend to let the entry handle the timezone conversion internally by using the offset variants without timezone parameter.

```java
// Creating a new entry and let the user fill it inside an edit form
Timezone timezone = ...;
DatePicker datePicker = ...

// old
datePicker.setValue(entry.getStart(timezone));

// ...value changed by date picker

entry.setStart(datePicker.getValue(), timezone);


// new
datePicker.setValue(entry.getStartWithOffset(timezone));

// ...value changed by date picker

entry.setStartWithOffset(datePicker.getValue(), timezone);
```

Summarized we recommend: when working with your backend (persisting, etc), use the UTC variants. When working with some kind
of edit form, where the user can modify his/her entry based on the calendar's timezone, use the offset variants. For new entries, that have not yet been added to the calendar, use the offset variants with the calendar's timezone parameter.

#### Entry related events date time
##### Events and timezones
As with the entries also event times are now always UTC based. We tried to align the api the the entry's one in naming and behavior, so that the "default" date time is always UTC based, while the offset variant api provides the data with the calendar timezone's offset applied.

```java
// old
calendar.addTimeslotSelectedEvent(event -> {
    Entry entry = new Entry()
    entry.setStart(event.getStartDateTimeUTC());
    entry.setEnd(event.getEndDateTimeUTC());

    // if you need the offset variant, use for instance event.getStartDateTime();
});

// new
calendar.addTimeslotSelectedEvent(event -> {
    Entry entry = new Entry()
    entry.setStart(event.getStart());
    entry.setEnd(event.getEnd());

    // if you need the offset variant, use for instance event.getStartWithOffset();
});
```

##### Renamed getters
Getters in `TimeslotsSelectedEvent` have changed to be more aligned to the entry's and other events names to simplify the code to read a bit (e. g. from `getStartDateTime` to `getStart`). We added respective methods for getting the Instant and the offsetted variant.

##### Removed getters in all-day related events
See chapter [**All-day behavior**](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/FullCalendar-MigrationGuides#all-day-behavior) for details.

#### Recurrence
`setRecurring(boolean)` is gone. There is no replacement for this method, since recurrence is now detected automatically
based on if you have set any relevant recurrence property in the entry. See isRecurring() for details, which properties are relevant. This behavior has been taken over from the client side library.

```java
// old
entry.setRecurring(recurring);

// new
// entry.setRecurring(recurring); // not needed anymore, is calculated automatically
```

`setRecurringStartDate / setRecurringEndDate` has lost the timezone parameter. Remove the timezone parameter. See chapter **All-day behavior** for details.

```java
// old
entry.setRecurringStartDate(start.toLocalDate(), timezone);

// new
entry.setRecurringStartDate(start.toLocalDate());
```

#### All-day behavior
The displayment / interpretion of all-day entries and changing timezones has changed. In version 3.x an all-day entry was not bound to the day alone, but
also influenced by the timezone. This could lead to effects, that a holiday, which is on a specific day (e. g. New Year),
spans two days, when switching the timezone. While it might somehow appear to be technically correct, it simply is not from the perspective of a calendar.
A holiday - or in general an all-day event - is bound to the day, not the time. If you need a day spanning event, that
is bound to the timezone and "moves", when changing the timezone, please create a time based event going over the whole
day. With this change we align the server side behavior to the client side library and other big calendar providers (for instance Google Calendar).

This change also affects code at some points. You may have compilation errors for missing `Instant` or `LocalDateTime`
based methods, e. g. at `MoreLinkClickedEvent`. Those events provide a `LocalDate` based getter, which you
should use instead.

Other events still may provide a date time based getter, where the returned value "behaves" now differently for all-day events
as described above, e. g. all subclasses of `DateTimeEvent`, for instance the `TimeslotClickedEvent`. In 3.x when clicking
the 1st of March, the returned date may have been pointing to the 28th of February due to applied timezone
conversion. Now the returned timestamp will always be the correct day at 00:00 UTC time. Simply ignore the
time part in this case or use the `LocalDate` getter.

#### Accessing custom properties in eventDidMount or eventContent
Not a required but a recommended change. If you have customized the appearance of your entries using one of the
callbacks `setEntryDidMountCallback(...)` or `setEntryContentCallback(...)`
and you access custom properties of an entry (for instance `description`), you should change the access to the newly
introduced `getCustomProperty()` method. This method takes the custom property key and allows to define a fallback
default value as second parameter.

> **Note (7.1):** The `setEntryContentCallback` and `setEntryDidMountCallback` methods shown below are
> deprecated as of 7.1. Use `setOption(Option.ENTRY_CONTENT, JsCallback.of(...))` instead.
> The `getCustomProperty()` access pattern inside the callback remains the same.

```java
// set the custom property beforehand
Entry someEntry = ...;
someEntry.setCustomProperty(EntryCustomProperties.DESCRIPTION, "some description");

calendar.setEntryContentCallback("" +
    "function(info) {" +

    // old
    "if(info.event.extendedProps && info.eventExtendedProps.customProperty && info.eventExtendedProps.customProperty.description) "+
    "   console.log(info.event.extendedProps.customProperty.description);" +
    "else " +
    "   console.log('no description set');" +

    // new
    "   console.log(info.event.getCustomProperty('" +EntryCustomProperties.DESCRIPTION+ "', 'no description set'));" + // get custom property with fallback

    "   /* ... do something with the event content ...*/" +
    "   return info.el; " +
    "}"
);
```

You can use that method also in javascript subclasses of the FullCalendar. Please note, that this method is a custom javascript, which is added
by us as soon as the client side `eventDidMount` or `eventContent` option has been set. Beforehand the method is not present
and using it will lead to javascript errors.


#### Deprecation
Some methods have been deprecated due to typos or ambigious meaning. Please check any compiler warnings and apidocs
regarding deprecated methods. They might be removed in any upcoming minor or major release without additional
warning.

### Removed features
If you used a feature in your calendar, that an entry could have a different timezone for start and end:
this is not supported anymore out of the box. We know, that this is a step back regarding functionality,
but at this point we thing having a straight way of storing the times internally is a bigger and more
common advantage, since this spares a lot of network overhead (entries do not need to be resend on
timezone change anymore). Implementing this would have taken too much time now without us knowing, if it is needed at all.
Instead we wanted to bring you the new version as fast as possible.

If you need this feature again, please contact us and we will check how to bring it back in one or another way.
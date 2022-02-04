We know, that migration suck. Not only developers fear the appearance of a new major version but also every
product or project manager using 3rd party software knows, that it can be a stressful, time taking horror to
integrate a new major version.

Unfortunately this applies also for this addon. There will be breaking changes, that need you guys to get back
into your code and review everything. Nevertheless, we hope, that this migration guide helps you as good as possible
to get a detailed insight of what has changed and what needs to be done to get you back on track.

If we missed something or anything is unclear, please ping us on GitHub. We hope, that your upgrade goes through
as smoothly as possible.

#Migrating from 3.x > 4.0
The most breaking change when migrating from version 3.x to 4.0 will be that the server side got
rid of any timezone inclusion regarding date times. You still may set a timezone for the calendar
or set/get offsetted local date times, but the regular api is now always utc based and we try to keep
anything as clear as possible regarding whether a local date time represents a utc time or a time with offset.

Part of this change is
* Getter and Setter of Entry start and end have changed in name and meaning
* Timezones are not applied anymore to the server side times
* Calendar time related api is also now utc based (e.g. finding entries in a timespan)

## Removed features
If you used a feature in your calendar, that an entry could have a different timezone for start and end:
this is not supported anymore out of the box. We know, that this is a step back regarding functionality,
but at this point we thing having a straight way of storing the times internally is a bigger and more
common advantage, since this spares a lot of network overhead (entries do not need to be resend on 
timezone change anymore). Implementing this would have taken too much time now without us knowing, if it is needed at all.
Instead we wanted to bring you the new version as fast as possible. 
If you need this feature again, please contact us and we will check how to bring it back in one or another way.

## Migration steps / manual in detail
### Timezone
Before heading into all utc and timezone related changes, the first thing to mention is, that the Timezone class now
has some simple methods to apply or remove the offset of the timezone it represents from a local date time to create
another ((un)offsetted) local date time to help doing things manual. Just to keep in mind, when one of the following 
things might seem to be a to breaking change.

### Entry start / end and timezone.
If you have used the `Instant` based start / end api only, you theoretically do not need to change your code. 
Please notify that `getStartUTC / getEndUTC` have been deprecated. Replace them at some point with `getStartAsInstant / getEndAsInstant`.

If you have used the `LocalDateTime` based versions, you will most likely need to change your code as the LocalDateTime based `getStart / getEnd` and
`setStart / setEnd` methods are now always referring to UTC and never to the timezones. If you want to set or get the date time including
the calendar timezone's offset, please use the respective `getStartWithOffset / getEndWithOffset` and `setStartWithOffset / setEndWithOffset`.
They take care of adding/subtracting the timezone's offset.

It might feel a bit unnecessary to have the method names changed, but this way we wanted to assure, that it is always clear,
what "kind" of date time the respective method is working with. Either with utc or some explicit offset.

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

```java
// Reading and writing the utc based backend
Timezone calendarTimezone = ...;
LocalDateTime utcBasedStartDate = ...

// old
entry.setStart(utcBasedStartDate, calendarTimezone);

// new
entry.setStart(utcBasedStartDate);
```

There is also a offset variant with a timezone parameter. This variant is intended to be used, when working on a new entry, that
is not yet added to a calendar. Here the entry cannot access the calendar's timezone. In this case you should use these methods. In all
other cases we recommend to let the entry handle the timezone internally.

```java
Timezone timezone = ...;
Entry entry = new Entry();

// old
entry.setStart(start, timezone);

// new
entry.setStartWithOffset(start, timezone);
```

Summarized we recommend: when working with your backend (persisting, etc), use the utc variants. When working with some kind
of edit form, where the user can modify his/her entry based on the calendar's timezone, use the offset variants.

### Entry related events date time
#### Events and timezones
As with the entries also event times are now always date time based. The api has changed in the same way, that the "default"
date time is always utc based, while the offset variant api provides the data with the calendar timezone's offset applied.

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
        entry.setStart(event.getStartDateTime());
        entry.setEnd(event.getEndDateTime());

        // if you need the offset variant, use for instance event.getStartDateTimeWithOffset();
        });
```

#### Renamed getters
Getters in `TimeslotsSelectedEvent` have changed to be more aligned to the entry's and other events names to simplify the code to read a bit (e.g.
 a name has changed from `getStartDateTime` to `getStart`). We added respective methods for getting the Instant and the offsetted variant.

#### Removed getters in all day related events
See chapter **All day behavior** for details.

### Recurrence
setRecurring(boolean) is gone. There is no replacement for this method. Recurrence is now detected automatically
based on if you have set any relevant recurrence property in the entry. See isRecurring() for details, which properties are relevant.

```java
// old
entry.setRecurring(recurring);

// new
// entry.setRecurring(recurring); // not needed anymore, is calculated automatically
```

setRecurringStartDate / EndDate has lost timezone. This was simply a wrong, as dates are not bound to timezones. Remove
the timezone parameter.

```java
// old
entry.setRecurringStartDate(start.toLocalDate(), timezone);

// new
entry.setRecurringStartDate(start.toLocalDate());
```

### All day behavior
Behavior of all day entries and changing timezones. In version 3.x an all day entry was not bound to the day alone, but
also influenced by the timezone. This could lead to effects, that a holiday, which is on a specific day (e. g. new year),
spans two days, when switching the timezone. While it might somehow appear to be technically correct, it is not.
A holiday, or in general an all day event, is still only on that single day. If you need a day spanning event, that
is bound to the timezone and "moves", when changing the timezone, please create a time based event going over the whole
day.

This change also affects code at some points. You may have compilation errors for missing `Instant` or `LocalDateTime` 
based methods, e. g. at `MoreLinkClickedEvent`. Those events provide a LocalDate based getter, which you
should use instead.

Other events still may provide a date time based getter, where the returned value behaves now differently for all day events 
as described above, e. g. all subclasses of `DateTimeEvent`, for instance the `TimeslotClickedEvent`. In 3.x when clicking
the 1st of March, the returned date may have been pointing to the 28th of February due to applied timezone
conversion. The returned timestamp will always be the correct date at 00:00 UTC time. Simply ignore the
time part in this case or use the LocalDate getter.

### Deprecation
Some methods have been deprecated due to typos or ambigious meaning. Please check any compiler warnings and apidocs
regarding deprecated methods. They might be removed in any upcoming minor or major release without additional
warning.

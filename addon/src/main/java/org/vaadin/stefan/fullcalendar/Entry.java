/*
 * Copyright 2020, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import elemental.json.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a simple calendar item. Each entry is at least defined by an id, a title and some kind of timespan.
 * If not stated differently, time (e.g. LocalDateTime) are always referring to UTC, e.g. getStart() returns a
 * LocalDateTime representing the start of the entry at UTC time (so it would be equal to an Instant representing
 * the same "time string" or a zoned date time with zone UTC). The client side takes care of displaying
 * entries in the calendar's current timezone
 * <br><br>
 * If you handle entries on the client side please be aware, that the FC library names them as "event". We name
 * them Entry here to prevent naming conflicts with UI Events (like "click" events, etc).
 * <br><br>
 * To create a recurring entry, simply set any of the "recurring" properties. With any of them set the entry
 * is automatically recurring.
 * <p></p>
 * You will find, that the entry implements a concept of being "known to the client". This concept is mainly
 * used by the eager loading in memory provider and is an artifact of earlier versions. In theory it should
 * not be important for any type of lazy loading provider, but the flag of "known to the client" will be
 * set at important points anyway (e. g.see {@link FullCalendar#fetchFromServer(JsonObject)}.
 * <p></p>
 * Timezones are currently not supported by the native client side library and therefore this instance
 * does not provide official offset api for recurrence times: https://github.com/fullcalendar/fullcalendar/issues/5273
 */
public class Entry extends JsonItem<String> {

    private static final Set<JsonItem.Key> KEYS = JsonItem.Key.readAndRegisterKeysAsUnmodifiable(EntryKey.class);

    /**
     * Returns the set of known keys of this instance. By default all defined keys of {@link EntryKey}.
     *
     * @see EntryKey
     * @return keys
     */
    public Set<JsonItem.Key> getKeys() {
        return KEYS;
    }

    /**
     * The referenced calendar instance. Can be null.
     */
    private FullCalendar calendar;

    /**
     * Creates a new editable instance with a generated id.
     */
    public Entry() {
        this(null);
    }

    /**
     * Creates a new entry with the given id. Null will lead to a generated id.
     * <br><br>
     * Please be aware, that the ID needs to be unique in the calendar instance. Otherwise it can lead to
     * unpredictable results.
     *
     * @param id id
     */
    public Entry(String id) {
        super(id == null ? UUID.randomUUID().toString() : id);
    }

    /**
     * Creates an entry based on the given json object. The id is taken from the json object. Providing none will
     * lead to an exception.
     * @param object json object
     * @return entry based on the object
     */
    public static Entry fromJson(JsonObject object) {
        Entry entry = new Entry(object.getString(Entry.EntryKey.ID.getName()));
        entry.updateFromJson(object, false);
        return entry;

    }

    /**
     * Returns the calendar instance of this entry. Is empty when not yet added to a calendar.
     *
     * @return calendar instance
     */
    public Optional<FullCalendar> getCalendar() {
        return Optional.ofNullable(calendar);
    }

    public void setCalendar(FullCalendar calendar) {
        if (this.calendar != null && calendar != null && this.calendar != calendar) {
            throw new UnsupportedOperationException("This entry is already attached to a calendar instance. Please remove it first from the old one.");
        }
        this.calendar = calendar;
    }

    @Override
    protected void toJson(JsonObject jsonObject, boolean changedValuesOnly) {
        if (changedValuesOnly && (isRecurring() || isMarkedAsChangedProperty(EntryKey.RECURRING_DAYS_OF_WEEKS))) {
            // Current issues with built in properties (therefore the special handlings of recurring and resources)
            // - https://github.com/fullcalendar/fullcalendar/issues/4393
            // - https://github.com/fullcalendar/fullcalendar/issues/5166
            // - https://github.com/fullcalendar/fullcalendar/issues/5262
            // Therefore this if will lead to a lot of "reset event", due to the fact, that resource editable
            // etc. might be set often.

            super.toJson(jsonObject, false); // override the "changed" and write all values
            writeHardResetToJson(jsonObject);
        } else {
            super.toJson(jsonObject, changedValuesOnly);
        }
    }


    /**
     * Returns the start of the entry as local date time. Represents the UTC date time this entry starts, which
     * means the time is the same as when calling {@link #getStartAsInstant()}.
     *
     * @return start as local date time or null
     */
    public LocalDateTime getStart() {
        return get(EntryKey.START);
    }

    /**
     * Returns the entry's start as an {@link Instant}. The contained time is the same as when calling
     * {@link #getStart()}.
     *
     * @return start as Instant or null
     */
    public Instant getStartAsInstant() {
        return getOrNull(EntryKey.START, (LocalDateTime start) -> start.toInstant(ZoneOffset.UTC));
    }

    /**
     * Returns the entry's start date.
     * @return start date or null
     */
    public LocalDate getStartAsLocalDate() {
        return getOrNull(EntryKey.START, LocalDateTime::toLocalDate);
    }

    /**
     * Returns the entry's start as an {@link Instant}. The contained time is the same as when calling
     * {@link #getStart()}.
     *
     * @return start as Instant
     * @deprecated use {@link #getStartAsInstant()}
     */
    @Deprecated
    public Instant getStartUTC() {
        return getStartAsInstant();
    }

    /**
     * Returns the start time as a zoned date time using this entry's start time zone. By default this is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p/>
     * Calling {@link ZonedDateTime#toLocalDateTime()} returns the time including the offset as LocalDateTime.
     * @return start at current timezone or null
     */
    public ZonedDateTime getStartWithTimezone() {
        return getStartTimezone().applyTimezone(getStart());
    }

    /**
     * Returns the start time as a local date time after applying the timezone's offset to
     * the utc based start date ({@link #getStart()}). By default the timezone is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p/>
     * To get a {@link OffsetDateTime} please use {@link #getStartWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return start with offset or null
     */
    public LocalDateTime getStartWithOffset() {
        return getStartWithOffset(getStartTimezone());
    }

    /**
     * Returns the start time as a local date time after applying the timezone's offset to
     * the utc based start date ({@link #getStart()}).
     * <p/>
     * This method is intended to be used for new entrys that have not yet been added to the
     * calender and thus have no reference to its timezone.
     * <p/>
     * To get a {@link OffsetDateTime} please use {@link #getStartWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return start with offset or null
     */
    public LocalDateTime getStartWithOffset(Timezone timezone) {
        return timezone.applyTimezoneOffset(getStart());
    }

    /**
     * Sets the entry's start. The given date time will be interpreted as the UTC start time of this entry.
     *
     * @param start utc start
     */
    public void setStart(LocalDateTime start) {
        set(EntryKey.START, start);
    }

    /**
     * Sets the entry's start. The given instant will be interpreted as the UTC start time of this entry.
     *
     * @param start utc start
     */
    public void setStart(Instant start) {
        setStart(start != null ? LocalDateTime.ofInstant(start, Timezone.ZONE_ID_UTC) : null);
    }

    /**
     * Sets the given local date as start using the start of the day as time (utc based).
     *
     * @param start start at 00:00 utc time
     */
    public void setStart(LocalDate start) {
        setStart(start != null ? start.atStartOfDay() : null);
    }

    /**
     * Sets the entry's start based on the zoned date time instance. The given date time will be converted to UTC.
     * <p/>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting start time will be ...T00:00.
     * <p/>
     * @param startWithTimezone start with time zone
     */
    public void setStartWithTimezone(ZonedDateTime startWithTimezone) {
        setStart(startWithTimezone != null ? startWithTimezone.withZoneSameInstant(Timezone.ZONE_ID_UTC).toLocalDateTime() : null);
    }

    /**
     * Sets the entry's start. The given date time will be interpreted as having the offset of the
     * start time zone applied. The time will be converted to UTC.
     * <p/>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting start time will be ...T00:00.
     * <p/>
     * This method is intended to be used in cases where the start time is edited in relation to
     * the current time zone (like a calendar entry editor).
     * <p/>
     *
     * @param startWithTimezone start with time zone
     */
    public void setStartWithOffset(LocalDateTime startWithTimezone) {
        setStartWithOffset(startWithTimezone, getStartTimezone());
    }

    /**
     * Sets the entry's start. The given date time will be interpreted as having the offset of the
     * given time zone applied. The time will be converted to UTC.
     * <p/>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting start time will be ...T00:00.
     * <p/>
     * This method is intended to be used in cases where entry is not yet added to the calender and thus
     * cannot use its timezone to interpret the offset.
     * <p/>
     *
     * @param startWithTimezone start with time zone
     * @param timezone timezone
     */
    public void setStartWithOffset(LocalDateTime startWithTimezone, Timezone timezone) {
        setStart(timezone.removeTimezoneOffset(startWithTimezone));
    }

    /**
     * Clears the current start time. Convenience method to prevent unnecessary casting when using
     * setStart(null).
     */
    public void clearStart() {
        setStart((LocalDateTime) null);
    }

    /**
     * Returns the end of the entry as local date time. Represents the UTC date time this entry ends, which
     * means the time is the same as when calling {@link #getStartAsInstant()}.
     *
     * @return end as local date time or null
     */
    public LocalDateTime getEnd() {
        return get(EntryKey.END);
    }

    /**
     * Returns the entry's end as an {@link Instant}. The contained time is the same as when calling
     * {@link #getEnd()}.
     *
     * @return end as Instant or null
     */
    public Instant getEndAsInstant() {
        return getOrNull(EntryKey.END, (LocalDateTime end) -> end.toInstant(ZoneOffset.UTC));
    }

    /**
     * Returns the entry's end date.
     * @return end date or null
     */
    public LocalDate getEndAsLocalDate() {
        return getOrNull(EntryKey.END, LocalDateTime::toLocalDate);
    }

    /**
     * Returns the entry's end as an {@link Instant}. The contained time is the same as when calling
     * {@link #getEnd()}.
     *
     * @return end as Instant
     * @deprecated use {@link #getEndAsInstant()}
     */
    @Deprecated
    public Instant getEndUTC() {
        return getEndAsInstant();
    }

    /**
     * Returns the end time as a zoned date time using this entry's end time zone. By default this is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p/>
     * Calling {@link ZonedDateTime#toLocalDateTime()} returns the time including the offset as LocalDateTime.
     * @return end at current timezone or null
     */
    public ZonedDateTime getEndWithTimezone() {
        return getEndTimezone().applyTimezone(getEnd());
    }

    /**
     * Returns the end time as a local date time after applying the timezone's offset to
     * the utc based end date ({@link #getEnd()}). By default the timezone is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p/>
     * To get a {@link OffsetDateTime} please use {@link #getEndWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return end with offset or null
     */
    public LocalDateTime getEndWithOffset() {
        return getEndTimezone().applyTimezoneOffset(getEnd());
    }

    /**
     * Returns the end time as a local date time after applying the timezone's offset to
     * the utc based end date ({@link #getEnd()}).
     * <p/>
     * This method is intended to be used for new entrys that have not yet been added to the
     * calender and thus have no reference to its timezone.
     * <p/>
     * To get a {@link OffsetDateTime} please use {@link #getEndWithTimezone()} and call
     * {@link ZonedDateTime#toOffsetDateTime()}
     * @return end with offset or null
     */
    public LocalDateTime getEndWithOffset(Timezone timezone) {
        return timezone.applyTimezoneOffset(getEnd());
    }

    /**
     * Sets the entry's end. The given date time will be interpreted as the UTC end time of this entry.
     *
     * @param end utc end
     */
    public void setEnd(LocalDateTime end) {
        set(EntryKey.END, end);
    }

    /**
     * Sets the entry's end. The given instant will be interpreted as the UTC end time of this entry.
     *
     * @param end utc end
     */
    public void setEnd(Instant end) {
        setEnd(end != null ? LocalDateTime.ofInstant(end, Timezone.ZONE_ID_UTC) : null);
    }

    /**
     * Sets the given local date as end using the end of the day as time (utc based).
     *
     * @param end end at 00:00 utc time
     */
    public void setEnd(LocalDate end) {
        setEnd(end != null ? end.atStartOfDay() : null);
    }

    /**
     * Sets the entry's end based on the zoned date time instance. The given date time will be converted to UTC.
     * <p/>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting end time will be ...T00:00.
     * <p/>
     * @param endWithTimezone end with time zone
     */
    public void setEndWithTimezone(ZonedDateTime endWithTimezone) {
        setEnd(endWithTimezone != null ? endWithTimezone.withZoneSameInstant(Timezone.ZONE_ID_UTC).toLocalDateTime() : null);
    }

    /**
     * Sets the entry's end. The given date time will be interpreted as having the offset of the
     * end time zone applied. The time will be converted to UTC.
     * <p/>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting end time will be ...T00:00.
     * <p/>
     * This method is intended to be used in cases where the end time is edited in relation to
     * the current time zone (like a calendar entry editor).
     * <p/>
     *
     * @param endWithTimezone end with time zone
     */
    public void setEndWithOffset(LocalDateTime endWithTimezone) {
        setEnd(getEndTimezone().removeTimezoneOffset(endWithTimezone));
    }

    /**
     * Sets the entry's end. The given date time will be interpreted as having the offset of the
     * given time zone applied. The time will be converted to UTC.
     * <p/>
     * For instance, when passing an instance with ...T01:00 and the timezone is Europe/Berlin in winter,
     * the resulting end time will be ...T00:00.
     * <p/>
     * This method is intended to be used in cases where entry is not yet added to the calender and thus
     * cannot use its timezone to interpret the offset.
     * <p/>
     *
     * @param endWithTimezone end with time zone
     * @param timezone timezone
     */
    public void setEndWithOffset(LocalDateTime endWithTimezone, Timezone timezone) {
        setEnd(timezone.removeTimezoneOffset(endWithTimezone));
    }

    /**
     * Clears the current end time. Convenience method to prevent unnecessary casting when using
     * setEnd(null).
     */
    public void clearEnd() {
        setEnd((LocalDateTime) null);
    }

    /**
     * Moves the entry by the given delta. Negative deltas will result in moving the entry to the past.
     * @param delta delta to be applied
     */
    public void moveStartEnd(@NotNull Delta delta) {
        moveStart(delta);
        moveEnd(delta);
    }

    /**
     * Moves the entry's start by the given delta without modifying the end.
     * Negative deltas will result in moving the start to the past.
     * @param delta delta to be applied to the entry' start
     */
    public void moveStart(Delta delta) {
        setStart(delta.applyOn(getStart()));
    }

    /**
     * Moves the entry's end by the given delta without modifying the start.
     * Negative deltas will result in moving the end to the past.
     * @param delta delta to be applied to the entry' end
     */
    public void moveEnd(Delta delta) {
        setEnd(delta.applyOn(getEnd()));
    }

    /**
     * Returns the timezone which is used on the client side. It is used to convert the internal utc timestamp
     * to the client side timezone. By default UTC.
     *
     * @return timezone
     */
    public Timezone getStartTimezone() {
        return calendar != null ? calendar.getTimezone() : Timezone.UTC;
    }

    /**
     * Returns the timezone which is used on the client side. It is used to convert the internal utc timestamp
     * to the client side timezone. By default UTC.
     *
     * @return timezone
     */
    public Timezone getEndTimezone() {
        return calendar != null ? calendar.getTimezone() : Timezone.UTC;
    }

    /**
     * Assign an additional className to this entry. Already assigned classNames will be kept.
     *
     * @param className class name to assign
     * @throws NullPointerException when null is passed
     */
    public void assignClassName(String className) {
        assignClassNames(Objects.requireNonNull(className));
    }

    /**
     * Assign additional classNames to this entry. Already assigned classNames will be kept.
     *
     * @param classNames class names to assign
     * @throws NullPointerException when null is passed
     */
    public void assignClassNames(@NotNull String... classNames) {
        assignClassNames(Arrays.asList(classNames));
    }

    /**
     * Assign additional classNames to this entry. Already assigned classNames will be kept.
     *
     * @param classNames class names to assign
     * @throws NullPointerException when null is passed
     */
    public void assignClassNames(@NotNull Collection<String> classNames) {
        Objects.requireNonNull(classNames);
        getOrCreateClassNames().addAll(classNames);
        markAsChangedProperty(EntryKey.CLASS_NAMES);
    }

    /**
     * Unassigns the given className from this entry.
     *
     * @param className class name to unassign
     * @throws NullPointerException when null is passed
     */
    public void unassignClassName(String className) {
        unassignClassNames(Objects.requireNonNull(className));
    }

    /**
     * Unassigns the given classNames from this entry.
     *
     * @param classNames class names to unassign
     * @throws NullPointerException when null is passed
     */
    public void unassignClassNames(@NotNull String... classNames) {
        unassignClassNames(Arrays.asList(classNames));
    }

    /**
     * Unassigns the given classNames from this entry.
     *
     * @param classNamesToRemove class names to unassign
     * @throws NullPointerException when null is passed
     */
    public void unassignClassNames(@NotNull Collection<String> classNamesToRemove) {
        Set<String> classNames = getClassNames();
        if (classNames != null) {
            classNames.removeAll(classNamesToRemove);
            markAsChangedProperty(EntryKey.CLASS_NAMES);
        }

    }

    /**
     * Unassigns all classNames from this entry.
     */
    public void unassignAllClassNames() {
        Set<String> classNames = getClassNames();
        if (classNames != null) {
            classNames.clear();
            setClassNames(null);
        }
    }

    /**
     * Returns the amount of assigned classNames.
     *
     * @return int size of classNames
     */
    public int getClassNamesSize() {
        Set<String> classNames = getClassNames();
        return classNames != null ? classNames.size() : 0;
    }

    /**
     * Returns, if the entry has any class name assigned.
     *
     * @return Boolean hasClassNames
     */
    public boolean hasClassNames() {
        Set<String> classNames = getClassNames();
        return classNames != null && !classNames.isEmpty();
    }

    @Override
    protected JsonItem.Key getIdKey() {
        return EntryKey.ID;
    }

    protected Key getCustomPropertiesKey() {
        return EntryKey.CUSTOM_PROPERTIES;
    }

    /**
     * Returns the title of this entry.
     * @return title
     */
    public String getTitle() {
        return get(EntryKey.TITLE);
    }

    /**
     * Sets the title of this entry. When the rendering of the entry is not customized, the title will be shown
     * as "name / title / description" of this entry on the client side.
     * @param title title
     */
    public void setTitle(String title) {
        set(EntryKey.TITLE, title);
    }

    /**
     * Returns the group id of this entry. A non null, non empty string means, that this entry is recurring.
     * @see #isRecurring()
     * @return group id
     */
    public String getGroupId() {
        return get(EntryKey.GROUP_ID);
    }

    /**
     * Sets a group id. When passing a non null / non empty string, this entry will become a recurring entry.
     * @see #isRecurring()
     * @param groupId group id
     */
    public void setGroupId(String groupId) {
        set(EntryKey.GROUP_ID, groupId);
    }



//    /**
//     * Sets the start of this entry as an UTC based instant.
//     * @param start start
//     * @deprecated use {@link #setStartUTC(Instant)}
//     */
//    @Deprecated
//    public void setStart(Instant start) {
//        setStartUTC(start);
//    }

//    /**
//     * Sets the start of this entry as an UTC based instant.
//     * @param start start
//     */
//    public void setStartUTC(Instant start) {
//        set(EntryKey.START, start);
//    }

//    /**
//     * Returns the end of the entry based on UTC.
//     *
//     * @return start
//     */
//    public Instant getEndUTC() {
//        return get(EntryKey.END);
//    }

//    /**
//     * Sets the end of this entry as an UTC based instant.
//     * @param end end
//     * @deprecated use {@link #setEndUTC(Instant)} instead
//     */
//    @Deprecated
//    public void setEnd(Instant end) {
//        set(EntryKey.END, end);
//    }

//    /**
//     * Sets the end of this entry as an UTC based instant.
//     * @param end end
//     */
//    public void setEndUTC(Instant end) {
//        set(EntryKey.END, end);
//    }

    /**
     * Indicates, if this entry is an all day entry. Default ist false.
     *
     * @return is all day
     */
    public boolean isAllDay() {
        return getBoolean(EntryKey.ALL_DAY);
    }

    /**
     * Sets, if this entry is an all day entry. In this case the client side will ignore any time information
     * on start / end and use date information only.
     * @param allDay all day
     */
    public void setAllDay(boolean allDay) {
        set(EntryKey.ALL_DAY, allDay);
    }

    /**
     * Indicates, if this entry may overlap with other entries. Default ist true.
     *
     * @return is overlap allowed
     */
    public boolean isOverlapAllowed() {
        return getBoolean(EntryKey.OVERLAP, true);
    }

    /**
     * Sets, if this entry may overlap with other entries.
     * @param overlap overlapping is allowed
     */
    public void setOverlapAllowed(boolean overlap) {
        set(EntryKey.OVERLAP, overlap);
    }

    /**
     * Returns a set of the class names of this instance. Might be null.
     * @see #getOrCreateClassNames()
     *
     * @return Set
     */
    public Set<String> getClassNames() {
        return get(EntryKey.CLASS_NAMES);
    }

    /**
     * Returns the set of class names or creates a new, empty one, if none exists yet.
     * @see #getClassNames()
     * @return class names set
     */
    public Set<String> getOrCreateClassNames() {
        Set<String> classNames = getClassNames();
        if (classNames == null) {
            classNames = new LinkedHashSet<>();
            setClassNames(classNames);
        }

        return classNames;
    }

    public void setClassNames(Set<String> classNames) {
        set(EntryKey.CLASS_NAMES, classNames);
    }

    public boolean isEditable() {
        return getBoolean(EntryKey.EDITABLE, true);
    }

    public void setEditable(boolean editable) {
        set(EntryKey.EDITABLE, editable);
    }

    public boolean isStartEditable() {
        return getBoolean(EntryKey.START_EDITABLE, true);
    }

    public void setStartEditable(boolean editable) {
        set(EntryKey.START_EDITABLE, editable);
    }

    public boolean isDurationEditable() {
        return getBoolean(EntryKey.DURATION_EDITABLE, true);
    }

    public void setDurationEditable(boolean editable) {
        set(EntryKey.DURATION_EDITABLE, editable);
    }

    /**
     * Returns the color of the entry. This is interpreted as background and border color on the client side.
     * @return color
     */
    public String getColor() {
        return get(EntryKey.COLOR);
    }

    /**
     * Sets the entry Constraint.
     * Null or empty string resets the color to the FC's default.
     *
     * @param constraint constraint
     */
    public void setConstraint(String constraint) {
        set(EntryKey.CONSTRAINT, StringUtils.trimToNull(constraint));
    }
    
    /**
     * Returns the entry Constraint.
     * @return constraint
     */
    public String getConstraint() {
        return get(EntryKey.CONSTRAINT);
    }

    /**
     * Sets the color for this entry. This is interpreted as background and border color on the client side.
     * Null or empty string resets the color to the FC's default.
     *
     * @param color color
     */
    public void setColor(String color) {
        set(EntryKey.COLOR, StringUtils.trimToNull(color));
    }

    /**
     * Returns the background color of the entry.
     * @return background color
     */
    public String getBackgroundColor() {
        return get(EntryKey.BACKGROUND_COLOR);
    }

    /**
     * Sets the background color for this entry. Null or empty string resets the color to the FC's default.
     *
     * @param backgroundColor background color
     */
    public void setBackgroundColor(String backgroundColor) {
        set(EntryKey.BACKGROUND_COLOR, StringUtils.trimToNull(backgroundColor));
    }

    /**
     * Returns the text color of the entry.
     * @return text color
     */
    public String getTextColor() {
        return get(EntryKey.TEXT_COLOR);
    }

    /**
     * Sets the text color for this entry. Null or empty string resets the color to the FC's default.
     *
     * @param textColor text color
     */
    public void setTextColor(String textColor) {
        set(EntryKey.TEXT_COLOR, StringUtils.trimToNull(textColor));
    }

    /**
     * Returns the border color of the entry.
     * @return border color
     */
    public String getBorderColor() {
        return get(EntryKey.BORDER_COLOR);
    }

    /**
     * Sets the border color for this entry. Null or empty string resets the color to the FC's default.
     *
     * @param borderColor border color
     */
    public void setBorderColor(String borderColor) {
        set(EntryKey.BORDER_COLOR, StringUtils.trimToNull(borderColor));
    }

    /**
     * Returns the description of this entry. Since the description is a <b>custom property</b>, it will
     * not automatically be shown on the entry.
     * @return description
     */
    public String getDescription() {
        return getCustomProperty(EntryCustomProperties.DESCRIPTION);
    }

    /**
     * Sets the description of this entry. Since the description is a <b>custom property</b>, it will
     * not automatically be shown on the entry.
     * @param description description
     */
    public void setDescription(String description) {
        setCustomProperty(EntryCustomProperties.DESCRIPTION, description);
    }

    /**
     * Returns the rendering mode ("display"). If not set, it will return AUTO (default), which display
     * the entry as a "normal" one.
     * @return rendering mode
     */
    public RenderingMode getRenderingMode() {
        return get(EntryKey.RENDERING_MODE, RenderingMode.AUTO);
    }

    /**
     * Sets the rendering mode ("display") for this entry. Passing null will reset it to the default.
     * @param renderingMode rengeringMode
     */
    public void setRenderingMode(RenderingMode renderingMode) {
        setOrDefault(EntryKey.RENDERING_MODE, renderingMode, RenderingMode.AUTO);
    }

    /**
     * Indicates, if this entry is recurring. This is indicated by having any "recurring" property set (e.g.
     * {@link #getRecurringDaysOfWeek()}).
     * @return is a recurring event
     */
    public boolean isRecurring() {
        Set<DayOfWeek> daysOfWeek = getRecurringDaysOfWeek();
        return (daysOfWeek != null && !daysOfWeek.isEmpty())
                || getRecurringEndDate() != null
                || getRecurringStartDate() != null
                || getRecurringStartTime() != null
                || getRecurringEndTime() != null;
    }

    /**
     * Returns a set of recurring days of week. Might be null.
     * @see #isRecurring()
     * @return days of week
     */
    public Set<DayOfWeek> getRecurringDaysOfWeek() {
        return get(EntryKey.RECURRING_DAYS_OF_WEEKS);
    }

    /**
     * Returns a set of recurring days of week. Might be null.
     * @see #isRecurring()
     * @deprecated method name has a typo, will be removed in future. use {@link #getRecurringDaysOfWeek()} instead
     * @return days of week
     */
    @Deprecated
    public Set<DayOfWeek> getRecurringDaysOfWeeks() {
        return getRecurringDaysOfWeek();
    }


    /**
     * Sets days of week on which this entry shall recur. Setting a non empty set automatically marks this entry
     * as recurring. Pass null or an empty set may remove the recurring.
     * @see #isRecurring()
     * @param daysOfWeek day of week
     * @deprecated method name has a typo, will be removed in future. use {@link #setRecurringDaysOfWeek} instead
     */
    @Deprecated
    public void setRecurringDaysOfWeeks(Set<DayOfWeek> daysOfWeek) {
        setRecurringDaysOfWeek(daysOfWeek);
    }

    /**
     * Sets days of week on which this entry shall recur. Setting a non empty set automatically marks this entry
     * as recurring. Pass null or an empty set may remove the recurring.
     *
     * @param daysOfWeek day of week
     * @see #isRecurring()
     */
    public void setRecurringDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        set(EntryKey.RECURRING_DAYS_OF_WEEKS, daysOfWeek);
    }

    /**
     * Sets days of week on which this entry shall recur. Setting a non empty set automatically marks this entry
     * as recurring. Pass null or an empty set may remove the recurring.
     *
     * @param daysOfWeek day of week
     * @see #isRecurring()
     */
    public void setRecurringDaysOfWeek(DayOfWeek... daysOfWeek) {
        setRecurringDaysOfWeek(daysOfWeek.length == 0 ? null : new HashSet<>(Arrays.asList(daysOfWeek)));
    }


//    /**
//     * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
//     * is recurring).
//     *
//     * @see #isRecurring()
//     * @return start date of recurrence
//     */
//    public Instant getRecurringStartDateUTC() {
//        return get(EntryKey.RECURRING_START_DATE);
//    }
//
//
//    /**
//     * Sets the start date for a recurring entry. Passing a date automatically marks this entry
//     * as recurring. Passing null may remove the recurrence or let the recurring entry extend infinitely to the future.
//     * @see #isRecurring()
//     * @param start start date
//     */
//    public void setRecurringStartDateUTC(Instant start) {
//        set(EntryKey.RECURRING_START_DATE, start);
//    }

//    /**
//     * Sets the start date for a recurring entry. Passing a date automatically marks this entry
//     * as recurring. Passing null may remove the recurrence or let the recurring entry extend infinitely to the future.
//     * @see #isRecurring()
//     * @param start start date
//     * @deprecated use {@link #setRecurringStartDateUTC(Instant)} instead
//     *
//     */
//    @Deprecated
//    public void setRecurringStartDate(Instant start) {
//        setRecurringStartDateUTC(start);
//    }

    /**
     * Sets the given local date as recurring start. Does not change the start time.
     *
     * @param recurringStart start
     */
    public void setRecurringStartDate(LocalDate recurringStart) {
        set(EntryKey.RECURRING_START_DATE, recurringStart);
    }

    /**
     * Sets the given local date as recurring start.
     *
     * @param recurringStart start
     * @deprecated use {@link #setRecurringStartDate(LocalDate)}
     */
    @Deprecated
    public void setRecurringStart(LocalDate recurringStart) {
        setRecurringStartDate(recurringStart);
    }

    /**
     * Returns the recurring start of the entry as local date time based on utc.
     *
     * @return start as local date time
     */
    public LocalDate getRecurringStartDate() {
        return get(EntryKey.RECURRING_START_DATE);
    }

    /**
     * Returns the recurring end of the entry as local date time based on utc.
     *
     * @return start as local date time
     */
    public LocalDate getRecurringEndDate() {
        return get(EntryKey.RECURRING_END_DATE);
    }

    /**
     * Returns the recurring end. This method is a shortcut for combining {@link #getRecurringEndDate()}
     * and {@link #getRecurringEndTime()}. Will return null, when no recurrence date is defined. When only a
     * end date is defined, the returned date time will be at the end of that day.
     * @see #isRecurring()
     * @return end date time of recurrence
     */
    public LocalDateTime getRecurringEnd() {
        LocalDate endDate = getRecurringEndDate();
        if (endDate == null) {
            return null;
        }

        LocalTime endTime = getRecurringEndTime();
        return endTime != null ? endDate.atTime(endTime) : endDate.atStartOfDay();
    }

    /**
     * Sets the given local date as recurring end.
     *
     * @param recurringEnd end
     */
    public void setRecurringEndDate(LocalDate recurringEnd) {
        set(EntryKey.RECURRING_END_DATE, recurringEnd);
    }


    /**
     * Sets the given local date as recurring end.
     *
     * @param recurringEnd end
     * @deprecated use {@link #setRecurringEndDate(LocalDate)}
     */
    @Deprecated
    public void setRecurringEnd(LocalDate recurringEnd) {
        setRecurringEndDate(recurringEnd);
    }

    /**
     * The start time of recurrence per day. When not defined, recurrence will extend to the end of day for
     * a recurring entry.
     *
     * @return start time of recurrence
     * @see #isRecurring()
     */
    public LocalTime getRecurringStartTime() {
        return get(EntryKey.RECURRING_START_TIME);
    }

    /**
     * Returns the recurring start. This method is a shortcut for combining {@link #getRecurringStartDate()}
     * and {@link #getRecurringStartTime()}. Will return null, when no recurrence date is defined. When only a
     * start date is defined, the returned date time will be at the start of that day.
     * @see #isRecurring()
     * @return start date time of recurrence
     */
    public LocalDateTime getRecurringStart() {
        LocalDate startDate = getRecurringStartDate();
        if (startDate == null) {
            return null;
        }

        LocalTime startTime = getRecurringStartTime();
        return startTime != null ? startDate.atTime(startTime) : startDate.atStartOfDay();
    }

    /**
     * Sets the start time for a recurring entry. Passing a non null value automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend to the
     * end of day.
     * @see #isRecurring()
     * @param start start time
     */
    public void setRecurringStartTime(LocalTime start) {
        set(EntryKey.RECURRING_START_TIME, start);
    }

    /**
     * The end time of recurrence per day. When not defined, recurrence will extend to the start of day for
     * a recurring entry.
     *
     * @see #isRecurring()
     * @return end time of recurrence
     */
    public LocalTime getRecurringEndTime() {
        return get(EntryKey.RECURRING_END_TIME);
    }

    /**
     * Sets the end time for a recurring entry. Passing a non null value automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend to the
     * start of day.
     * @see #isRecurring()
     * @param end start time
     */
    public void setRecurringEndTime(LocalTime end) {
        set(EntryKey.RECURRING_END_TIME, end);
    }

    /**
     * Sets the recurring start as local date time. Shortcut for calling {@link #setRecurringStartDate(LocalDate)}
     * and {@link #setRecurringStartTime(LocalTime)}.
     * <p></p>
     * <b>Please note</b>, that despite being a LocalDateTime the date and time must not necessarily be related
     * to each other due to the nature of a recurring entry. It is just a shortcut method.
     *
     * @param recurringStart recurring start
     */
    public void setRecurringStart(LocalDateTime recurringStart) {
        setRecurringStartDate(recurringStart != null ? recurringStart.toLocalDate() : null);
        setRecurringStartTime(recurringStart != null ? recurringStart.toLocalTime() : null);
    }

    /**
     * Sets the recurring end as local date time. Shortcut for calling {@link #setRecurringEndDate(LocalDate)}
     * and {@link #setRecurringEndTime(LocalTime)}.
     * <p></p>
     * <b>Please note</b>, that despite being a LocalDateTime the date and time must not necessarily be related
     * to each other due to the nature of a recurring entry. It is just a shortcut method.
     *
     * @param recurringEnd recurring end
     */
    public void setRecurringEnd(LocalDateTime recurringEnd) {
        setRecurringEndDate(recurringEnd != null ? recurringEnd.toLocalDate() : null);
        setRecurringEndTime(recurringEnd != null ? recurringEnd.toLocalTime() : null);
    }

    /**
     * Clears the current recurring start date and time.
     */
    public void clearRecurringStart() {
        setRecurringStartDate(null);
        setRecurringStartTime(null);
    }

    /**
     * Clears the current recurring end date and time.
     */
    public void clearRecurringEnd() {
        setRecurringEndDate(null);
        setRecurringEndTime(null);
    }

    /**
     * Sets custom properties.
     * <p/>
     * You can access custom properties on the client side when customizing the event rendering via the property
     * <code>event.getCustomProperty('key')</code>, for instance inside the entry content callback.
     *
     * @see FullCalendarBuilder#withEntryContent(String)
     * @param customProperties custom properties
     */
    public void setCustomProperties(Map<String, Object> customProperties) {
        set(getCustomPropertiesKey(), customProperties);
    }

    /**
     * Sets custom property for this entry. An existing property will be overwritten.
     * <p/>
     * You can access custom properties on the client side when customizing the event rendering via the property
     * <code>event.getCustomProperty('key')</code>, for instance inside the entry content callback.
     *
     *  @see FullCalendarBuilder#withEntryContent(String)
     *
     * @param key   the name of the property to set
     * @param value value to set
     */
    public void setCustomProperty(@NotNull String key, Object value) {
        Objects.requireNonNull(key);
        getOrCreateCustomProperties().put(key, value);
        markAsChangedProperty(getCustomPropertiesKey());
    }

    /**
     * Returns a custom property (or null if not defined).
     * <p/>
     * You can access custom properties on the client side when customizing the event rendering via the property
     * <code>event.getCustomProperty('key')</code>, for instance inside the entry content callback.
     *
     * @see FullCalendarBuilder#withEntryContent(String)
     *
     * @param key name of the custom property
     * @param <T> return type
     * @return custom property value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomProperty(@NotNull String key) {
        return (T) getCustomPropertiesOrEmpty().get(key);
    }

    /**
     * Remove the custom property based on the name.
     *
     * @param key the name of the property to remove
     */
    public void removeCustomProperty(@NotNull String key) {
        Map<String, Object> customProperties = getCustomProperties();
        if (customProperties != null) {
            // FIXME this will currently not remove the custom property from the client side!
            customProperties.remove(Objects.requireNonNull(key));
            markAsChangedProperty(getCustomPropertiesKey());
        }
    }

    /**
     * Remove specific custom property where the name and value match.
     *
     * @param key   the name of the property to remove
     * @param value the object to remove
     */
    public void removeCustomProperty(@NotNull String key, @NotNull Object value) {
        Map<String, Object> customProperties = getCustomProperties();
        if (customProperties != null) {
            // FIXME this will currently not remove the custom property from the client side!
            customProperties.remove(Objects.requireNonNull(key), Objects.requireNonNull(value));
            markAsChangedProperty(getCustomPropertiesKey());
        }
    }

    /**
     * Returns the map of the custom properties of this instance. This map is editable and any changes
     * will be sent to the client using {@link FullCalendar#updateEntry(Entry)}.
     * <p></p>
     * Might be null.
     * <p/>
     * You can access custom properties on the client side when customizing the event rendering via the property
     * <code>event.getCustomProperty('key')</code>, for instance inside the entry content callback.
     *
     * @see FullCalendarBuilder#withEntryContent(String)
     *
     * @return Map
     * @see #getCustomPropertiesOrEmpty()
     * @see #getOrCreateCustomProperties()
     */
    public Map<String, Object> getCustomProperties() {
        return get(getCustomPropertiesKey());
    }

//    /**
//     * Returns the key to be used to assign custom properties. Throws an {@link UnsupportedOperationException} by
//     * default. Only necessary to be overridden, when custom properties shall be usable.
//     *
//     * @return custom properties key.
//     */
//    protected Key getCustomPropertiesKey() {
//        throw new UnsupportedOperationException("Override getCustomPropertiesKey to use custom properties.");
//    }

    /**
     * Returns the custom properties map or an empty one, if none has yet been created. The map is not writable.
     * <p></p>
     * Be aware, that any non standard property you
     * set via "set(..., ...)" is not automatically put into this map, but this is done by the client later.
     *
     * @return map
     * @see #getCustomProperties()
     * @see #getOrCreateCustomProperties()
     */
    public Map<String, Object> getCustomPropertiesOrEmpty() {
        Map<String, Object> map = get(getCustomPropertiesKey());
        return map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap();
    }

    /**
     * Returns the map of the custom properties of this instance. This map is editable and any changes
     * will be sent to the client using {@link FullCalendar#updateEntry(Entry)}.
     * <p/>
     * Creates and registers a new map, if none is there yet.
     * <p></p>
     * Be aware, that any non standard property you
     * set via "set(..., ...)" is not automatically put into this map, but this is done by the client later.
     *
     * @return Map
     * @see #getCustomPropertiesOrEmpty()
     * @see #getCustomProperties()
     */
    public Map<String, Object> getOrCreateCustomProperties() {
        Map<String, Object> map = get(getCustomPropertiesKey());
        if (map == null) {
            map = new HashMap<>();
            setCustomProperties(map);
        }
        return map;
    }


    @Getter
    @RequiredArgsConstructor
    public static class ClientDateTimeConverter<T extends JsonItem> implements JsonItemPropertyConverter<LocalDateTime, T> {

        @Override
        public JsonValue toJsonValue(LocalDateTime serverValue, T currentInstance) {
            return JsonUtils.toJsonValue(JsonUtils.formatClientSideDateTimeString(serverValue));
        }

        @Override
        public LocalDateTime ofJsonValue(JsonValue clientValue, T currentInstance) {
            if (clientValue instanceof JsonNull) {
                return null;
            }

            if (clientValue instanceof JsonString) {
                return JsonUtils.parseClientSideDateTime(clientValue.asString());
            }

            throw new IllegalArgumentException(clientValue + " must either be of type JsonNull or JsonString, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ClientDateConverter<T extends JsonItem> implements JsonItemPropertyConverter<LocalDate, T> {

        @Override
        public JsonValue toJsonValue(LocalDate serverValue, T currentInstance) {
            return JsonUtils.toJsonValue(JsonUtils.formatClientSideDateString(serverValue));
        }

        @Override
        public LocalDate ofJsonValue(JsonValue clientValue, T currentInstance) {
            if (clientValue instanceof JsonNull) {
                return null;
            }

            if (clientValue instanceof JsonString) {
                return JsonUtils.parseClientSideDate(clientValue.asString());
            }

            throw new IllegalArgumentException(clientValue + " must either be of type JsonNull or JsonString, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ClientTimeConverter<T extends JsonItem> implements JsonItemPropertyConverter<LocalTime, T> {
        @Override
        public JsonValue toJsonValue(LocalTime serverValue, T currentInstance) {
            return JsonUtils.toJsonValue(JsonUtils.formatClientSideTimeString(serverValue));
        }

        @Override
        public LocalTime ofJsonValue(JsonValue clientValue, T currentInstance) {
            if (clientValue instanceof JsonNull) {
                return null;
            }

            if (clientValue instanceof JsonString) {
                return JsonUtils.parseClientSideTime(clientValue.asString());
            }

            throw new IllegalArgumentException(clientValue + " must either be of type JsonNull or JsonString, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
        }
    }

    private static class RecurringTimeConverter<T extends Entry> extends ClientTimeConverter<T> {
        @Override
        public JsonValue toJsonValue(LocalTime serverValue, T currentInstance) {
            // recurring time must not be sent, when all day
            return currentInstance.isAllDay() ? null : super.toJsonValue(serverValue, currentInstance);
        }
    }

    private static class DayOfWeekItemConverter implements JsonItemPropertyConverter<Set<DayOfWeek>, Entry> {
        @Override
        public JsonValue toJsonValue(Set<DayOfWeek> serverValue, Entry currentInstance) {
            if (serverValue == null) {
                return Json.createNull();
            }

            return JsonUtils.toJsonValue(serverValue
                    .stream()
                    .map(dayOfWeek -> dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue())
                    .collect(Collectors.toList()));
        }

        @Override
        public Set<DayOfWeek> ofJsonValue(JsonValue clientValue, Entry currentInstance) {
            Set<Number> daysOfWeek = JsonUtils.ofJsonValue(clientValue, HashSet.class);
            return daysOfWeek != null ? daysOfWeek.stream().map(n -> {
                int dayOfWeek = n.intValue();
                if (dayOfWeek == 0) {
                    dayOfWeek = 7;
                }

                return DayOfWeek.of(dayOfWeek);
            }).collect(Collectors.toSet()) : null;
        }
    }

    /**
     * Predefined set of known Entry keys.
     */
    public static class EntryKey {
        /**
         * The entry's id.
         */
        public static final JsonItem.Key ID = JsonItem.Key.builder().name("id").allowedType(String.class).build();

        /**
         * Events that share a groupId will be dragged and resized together automatically.
         */
        public static final JsonItem.Key GROUP_ID = JsonItem.Key.builder().name("groupId").allowedType(String.class).build();

        /**
         * The entry's title. This title will be shown on the client side.
         */
        public static final JsonItem.Key TITLE = JsonItem.Key.builder().name("title").allowedType(String.class).build();

        /**
         * The entry's start as UTC.
         */
        public static final JsonItem.Key START = JsonItem.Key.builder()
                .name("start")
                .allowedType(Instant.class)
                .updateFromClientAllowed(true)
                .converter(new ClientDateTimeConverter<>())
                .build();

        /**
         * The entry's end as UTC.
         */
        public static final JsonItem.Key END = JsonItem.Key.builder()
                .name("end")
                .allowedType(Instant.class)
                .updateFromClientAllowed(true)
                .converter(new ClientDateTimeConverter<>())
                .build();

        /**
         * Indicates if this entry is all day or not. There might be time values set for start or end even if this
         * value is false. Changes on this field do <b>not</b> modifiy the date values directly. Any
         * changes on date values are done by the FC by event, but not this class.
         */
        public static final JsonItem.Key ALL_DAY = JsonItem.Key.builder()
                .name("allDay")
                .allowedType(Boolean.class)
                .updateFromClientAllowed(true)
                .build();

        /**
         * Determines which HTML classNames will be attached to the rendered event.
         */
        public static final JsonItem.Key CLASS_NAMES = JsonItem.Key.builder()
                .name("classNames")
                .allowedType(Set.class) // Set<String>
                .jsonArrayToCollectionConversionType(HashSet.class) // for copyFrom()
                .build();

        /**
         * Indicates if this entry is editable by the users. This value
         * is passed to the client side and interpreted there, but can also be used for server side checks.
         * <br><br>
         * This value has no impact on the resource API of this class.
         */
        public static final JsonItem.Key EDITABLE = JsonItem.Key.builder()
                .name("editable")
                .allowedType(Boolean.class)
                .defaultValue(true)
                .build();

        /**
         * Indicates if this entry's start is editable by the users. This value
         * is passed to the client side and interpreted there, but can also be used for server side checks.
         * <br><br>
         * This value has no impact on the resource API of this class.
         */
        public static final JsonItem.Key START_EDITABLE = JsonItem.Key.builder()
                .name("startEditable")
                .allowedType(Boolean.class)
                .build();

        /**
         * Indicates if this entry's end is editable by the users. This value
         * is passed to the client side and interpreted there, but can also be used for server side checks.
         * <br><br>
         * This value has no impact on the resource API of this class.
         */
        public static final JsonItem.Key DURATION_EDITABLE = JsonItem.Key.builder()
                .name("durationEditable")
                .allowedType(Boolean.class)
                .build();

        /**
         * The color of this entry. Any valid css color is allowed (e.g. #f00, #ff0000, rgb(255,0,0), or red).
         */
        public static final JsonItem.Key COLOR = JsonItem.Key.builder()
                .name("color")
                .allowedType(String.class)
                .build();
        
        /**
         * The entry constraint.
         */
        public static final JsonItem.Key CONSTRAINT = JsonItem.Key.builder()
                .name("constraint")
                .allowedType(String.class)
                .build();

        /**
         * The background color of this entry. Any valid css color is allowed (e.g. #f00, #ff0000, rgb(255,0,0), or red).
         */
        public static final JsonItem.Key BACKGROUND_COLOR = JsonItem.Key.builder()
                .name("backgroundColor")
                .allowedType(String.class)
                .build();

        /**
         * The border color of this entry. Any valid css color is allowed (e.g. #f00, #ff0000, rgb(255,0,0), or red).
         */
        public static final JsonItem.Key BORDER_COLOR = JsonItem.Key.builder()
                .name("borderColor")
                .allowedType(String.class)
                .build();

        /**
         * The text color of this entry. Any valid css color is allowed (e.g. #f00, #ff0000, rgb(255,0,0), or red).
         */
        public static final JsonItem.Key TEXT_COLOR = JsonItem.Key.builder()
                .name("textColor")
                .allowedType(String.class)
                .build();

        /**
         * The custom property list. Contains any non standard property. Please see also the fullcalendar
         * documentation regarding extended properties. Be aware, that any non standard property you
         * set via "set(..., ...)" is not automatically put into this map, but this is done by the client later.
         */
        public static final JsonItem.Key CUSTOM_PROPERTIES = JsonItem.Key.builder()
                .name("customProperties")
                .allowedType(Map.class) // Map<String, Object>
                .build();

        /**
         * The rendering mode of this entry. Never null
         */
        public static final JsonItem.Key RENDERING_MODE = JsonItem.Key.builder()
                .name("display")
                .allowedType(RenderingMode.class)
                .build();

        /**
         * Returns the days of weeks on which this event should recur. Null or empty when
         * no recurring is defined.
         */
        public static final JsonItem.Key RECURRING_DAYS_OF_WEEKS = JsonItem.Key.builder()
                .name("daysOfWeek")
                .converter(new DayOfWeekItemConverter())
                .build();

        /**
         * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
         * is recurring).
         */
        public static final JsonItem.Key RECURRING_START_DATE = JsonItem.Key.builder()
                .name("startRecur")
                .allowedType(Instant.class)
                .converter(new ClientDateConverter<>())
                .build();

        /**
         * The start time of recurrence. When not defined, the event will appear as an all day event.
         */
        public static final JsonItem.Key RECURRING_START_TIME = JsonItem.Key.builder()
                .name("startTime")
                .allowedType(LocalTime.class)
                .converter(new RecurringTimeConverter<>())
                .build();

        /**
         * The end date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
         * is recurring).
         */
        public static final JsonItem.Key RECURRING_END_DATE = JsonItem.Key.builder()
                .name("endRecur")
                .allowedType(Instant.class)
                .converter(new ClientDateConverter<>())
                .build();

        /**
         * The start time of recurrence. Passing null on a recurring entry will make it appear as an all day event.
         */
        public static final JsonItem.Key RECURRING_END_TIME = JsonItem.Key.builder()
                .name("endTime")
                .allowedType(LocalTime.class)
                .converter(new RecurringTimeConverter<>())
                .build();

        /**
         * Indicates, if this single entry may overlap with other entries. If false, prevents this entry from being
         * dragged/resized over other entries. Also prevents other entries from being dragged/resized over this entry.
         */
        public static final JsonItem.Key OVERLAP = JsonItem.Key.builder()
                .name("overlap")
                .allowedType(Boolean.class)
                .build();
    }

    /**
     * Defines known custom properties, for instance since they are widely used.
     */
    public static final class EntryCustomProperties {
        /**
         * Key for an entry's description.
         */
        public static final String DESCRIPTION = "description";
    }

    /**
     * Constants for rendering of an entry.
     */
    public enum RenderingMode implements ClientSideValue {
        /**
         * Does not render the entry.
         */
        NONE(null),

        /**
         * Renders as a solid rectangle in day grid
         */
        BLOCK("block"),

        /**
         * Renders with a dot when in day grid
         */
        LIST_ITEM("list-item"),

        /**
         * Renders as 'block' if all-day or multi-day, otherwise will display as 'list-item'
         */
        AUTO("auto"),

        /**
         * Renders as background entry (marks the area of the entry interval).
         */
        BACKGROUND("background"),

        /**
         * Renders as inversed background entry (marks everything except the entry interval).
         */
        INVERSE_BACKGROUND("inverse-background");

        private final String clientSideName;

        RenderingMode(String clientSideName) {
            this.clientSideName = clientSideName;
        }

        @Override
        public String getClientSideValue() {
            return clientSideName;
        }

    }


}

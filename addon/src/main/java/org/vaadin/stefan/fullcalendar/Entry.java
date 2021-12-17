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

import com.vaadin.flow.function.SerializableFunction;
import elemental.json.JsonNull;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.*;

/**
 * Represents an event in the full calendar. It is named Entry here to prevent name conflicts with
 * event handling mechanisms (e.g. a component event fired by clicking something).
 * <br><br>
 * To create a recurring entry, simply set any of the "recurring" properties. With any of them set the entry
 * is automatically recurring.
 * <br><br>
 * <i><b>Note: </b>Creation of an entry might be exported to a builder later.</i>
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
     * The calendar instance to be used internally. There is NO automatic removal or add when the calendar changes.
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PROTECTED)
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
        setEditable(true);
    }

    /**
     * Returns the calendar instance of this entry. Is empty when not yet added to a calendar.
     *
     * @return calendar instance
     */
    public Optional<FullCalendar> getCalendar() {
        return Optional.ofNullable(calendar);
    }

    @Override
    protected void writeJsonOnUpdate(JsonObject jsonObject) {
        if (isRecurring() || isMarkedAsChangedProperty(EntryKey.RECURRING_DAYS_OF_WEEKS)) {
            // Current issues with built in properties (therefore the special handlings of recurring and resources)
            // - https://github.com/fullcalendar/fullcalendar/issues/4393
            // - https://github.com/fullcalendar/fullcalendar/issues/5166
            // - https://github.com/fullcalendar/fullcalendar/issues/5262
            // Therefore this if will lead to a lot of "reset event", due to the fact, that resource editable
            // etc. might be set often.

            super.writeJsonOnAdd(jsonObject);
            writeHardResetToJson(jsonObject);
        } else {
            super.writeJsonOnUpdate(jsonObject);
        }
    }

    /**
     * Returns the start of the entry as local date time based on the timezone returned by {@link #getStartTimezoneServer()}.
     *
     * @return start as local date time
     */
    public LocalDateTime getStart() {
        return getStart(getStartTimezoneServer());
    }

    /**
     * Sets the given local date time as start. It is converted to an instant by using the
     * calendar's server start timezone.
     *
     * @param start start
     */
    public void setStart(LocalDateTime start) {
        setStart(start, getStartTimezoneServer());
    }

    /**
     * Returns the start of the entry as local date time based on the given timezone
     *
     * @param timezone timezone
     * @return start as local date time
     * @throws NullPointerException when null is passed
     */
    public LocalDateTime getStart(@NotNull Timezone timezone) {
        Objects.requireNonNull(timezone, "timezone");
        Instant start = getStartUTC();
        return start != null ? LocalDateTime.ofInstant(start, timezone.getZoneId().getRules().getOffset(start)) : null;
    }

    /**
     * Returns the start of the entry as local date time based on the timezone returned by {@link #getEndTimezoneServer()}.
     *
     * @return start as local date time
     */
    public LocalDateTime getEnd() {
        return getEnd(getEndTimezoneServer());
    }

    /**
     * Sets the given local date time as end. It is converted to an instant by using the
     * calendar's server end timezone.
     *
     * @param end end
     */
    public void setEnd(LocalDateTime end) {
        setEnd(end, getEndTimezoneServer());
    }

    /**
     * Returns the start of the entry as local date time based on the given timezone
     *
     * @param timezone timezone
     * @return start as local date time
     * @throws NullPointerException when null is passed
     */
    public LocalDateTime getEnd(@NotNull Timezone timezone) {
        Objects.requireNonNull(timezone, "timezone");
        Instant end = getEndUTC();
        return end != null ? LocalDateTime.ofInstant(end, timezone.getZoneId().getRules().getOffset(end)) : null;
    }

    /**
     * Sets the given local date time as start. It is converted to an Instant by using the given timezone.
     * <br><br>
     * Null values are not allowed here. Use {@link #setStart(Instant)} instead to reset the date.
     *
     * @param start    start
     * @param timezone timezone
     * @throws NullPointerException when null is passed
     */
    public void setStart(@NotNull LocalDateTime start, @NotNull Timezone timezone) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(timezone, "timezone");
        setStart(timezone.convertToUTC(start));
    }

    /**
     * Sets the given local date time as end. It is converted to an Instant by using the given timezone.
     * <br><br>
     * Null values are not allowed here. Use {@link #setEnd(Instant)} instead to reset the date.
     *
     * @param end      end
     * @param timezone timezone
     * @throws NullPointerException when null is passed
     */
    public void setEnd(@NotNull LocalDateTime end, @NotNull Timezone timezone) {
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(timezone, "timezone");
        setEnd(timezone.convertToUTC(end));
    }

//    /**
//     * Returns the timezone which is used on the client side. It is used to convert the internal utc timestamp
//     * to the client side timezone. By default UTC.
//     *
//     * @return timezone
//     * @deprecated use {@link #getStartTimezoneClient()}() or {@link #getEndTimezoneClient()} instead depending
//     * on your use case
//     */
//    @Deprecated
//    public Timezone getStartTimezone() {
//        return getStartTimezoneClient();
//    }

    /**
     * Returns the timezone which is used on the client side. It is used to convert the internal utc timestamp
     * to the client side timezone. By default UTC.
     *
     * @return timezone
     */
    public Timezone getStartTimezoneClient() {
        return calendar != null ? calendar.getTimezoneClient() : Timezone.UTC;
    }


//    /**
//     * Returns the timezone which is used on the client side. It is used to convert the internal utc timestamp
//     * to the client side timezone. By default UTC.
//     *
//     * @return timezone
//     * @deprecated use {@link #getEndTimezoneClient()} or {@link #getEndTimezoneServer()} instead depending on your
//     * use case
//     */
//    @Deprecated
//    public Timezone getEndTimezone() {
//        return getEndTimezoneClient();
//    }

    /**
     * Returns the timezone which is used on the client side. It is used to convert the internal utc timestamp
     * to the client side timezone. By default UTC.
     *
     * @return timezone
     */
    public Timezone getEndTimezoneClient() {
        return calendar != null ? calendar.getTimezoneClient() : Timezone.UTC;
    }

    /**
     * Returns the server's timezone used for automatic conversion between Instant and LocalDateTime for the entry start
     * using {@link #setStart(LocalDateTime)} or {@link #getStart()}.
     * <p></p>
     * By default the server timezone.
     *
     * @return timezone
     */
    public Timezone getStartTimezoneServer() {
        return Timezone.getSystem();
    }

    /**
     * Returns the server's timezone used for automatic conversion between Instant and LocalDateTime for the entry start
     * using {@link #setEnd(LocalDateTime)} or {@link #getEnd()}.
     * <p></p>
     * By default the server timezone.
     *
     * @return timezone
     */
    public Timezone getEndTimezoneServer() {
        return Timezone.getSystem();
    }

    /**
     * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     * <br><br>
     * The given timezone is used to convert the instant to a local date instance.
     *
     * @param timezone timezone
     * @return start date of recurrence
     * @throws NullPointerException when null is passed
     */
    public LocalDate getRecurringStartDate(@NotNull Timezone timezone) {
        Objects.requireNonNull(timezone, "timezone");

        Instant recurringStartDate = getRecurringStartDateUTC();

        return recurringStartDate != null ? LocalDateTime.ofInstant(recurringStartDate, timezone.getZoneId().getRules().getOffset(recurringStartDate)).toLocalDate() : null;
    }

    /**
     * The start date of recurrence. Passing null on a recurring entry will extend the recurrence infinitely to the past.
     * It is converted to an Instant by using the given timezone.
     * <br><br>
     * Null is not allowed here, use {@link #setRecurringStartDate(Instant)} to reset the value.
     *
     * @param recurringStartDate start date or recurrence
     * @param timezone           timezone
     * @throws NullPointerException when null is passed
     */
    public void setRecurringStartDate(@NotNull LocalDate recurringStartDate, @NotNull Timezone timezone) {
        Objects.requireNonNull(recurringStartDate, "recurringStartDate");
        Objects.requireNonNull(timezone, "timezone");
        setRecurringStartDate(timezone.convertToUTC(recurringStartDate));
    }


    /**
     * The end date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     * <br><br>
     * The given timezone is used to convert the instant to a local date instance.
     *
     * @param timezone timezone
     * @return end date of recurrence
     * @throws NullPointerException when null is passed
     */
    public LocalDate getRecurringEndDate(@NotNull Timezone timezone) {
        Objects.requireNonNull(timezone, "timezone");
        Instant recurringEndDate = getRecurringEndDateUTC();
        return recurringEndDate != null ? LocalDateTime.ofInstant(recurringEndDate, timezone.getZoneId().getRules().getOffset(recurringEndDate)).toLocalDate() : null;
    }

    /**
     * The end date of recurrence. Passing null on a recurring entry will extend the recurrence infinitely to the past.
     * It is converted to an Instant by using the given timezone.
     * <br><br>
     * Null is not allowed here, use {@link #setRecurringEndDate(Instant)} to reset the value.
     *
     * @param recurringEndDate end date or recurrence
     * @param timezone         timezone
     * @throws NullPointerException when null is passed
     */
    public void setRecurringEndDate(@NotNull LocalDate recurringEndDate, @NotNull Timezone timezone) {
        Objects.requireNonNull(recurringEndDate, "recurringEndDate");
        Objects.requireNonNull(timezone, "timezone");

        setRecurringEndDate(timezone.convertToUTC(recurringEndDate));
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
        if (classNames != null)
            classNames.removeAll(classNamesToRemove);
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

    @Override
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

    /**
     * Returns the start of the entry based on UTC.
     *
     * @return start
     */
    public Instant getStartUTC() {
        return get(EntryKey.START);
    }

    /**
     * Sets the start of this entry as an UTC based instant.
     * @param start start
     * @deprecated use {@link #setStartUTC(Instant)}
     */
    @Deprecated
    public void setStart(Instant start) {
        setStartUTC(start);
    }

    /**
     * Sets the start of this entry as an UTC based instant.
     * @param start start
     */
    public void setStartUTC(Instant start) {
        set(EntryKey.START, start);
    }

    /**
     * Returns the end of the entry based on UTC.
     *
     * @return start
     */
    public Instant getEndUTC() {
        return get(EntryKey.END);
    }

    /**
     * Sets the end of this entry as an UTC based instant.
     * @param end end
     * @deprecated use {@link #setEndUTC(Instant)} instead
     */
    @Deprecated
    public void setEnd(Instant end) {
        set(EntryKey.END, end);
    }

    /**
     * Sets the end of this entry as an UTC based instant.
     * @param end end
     */
    public void setEndUTC(Instant end) {
        set(EntryKey.END, end);
    }

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
     * Returns the rendering mode ("display"). If not set, it will return NONE (default), which display
     * the entry as a "normal" one.
     * @return rendering mode
     */
    public RenderingMode getRenderingMode() {
        return get(EntryKey.RENDERING_MODE, RenderingMode.NONE);
    }

    /**
     * Sets the rendering mode ("display") for this entry. Passing null will reset it to the default.
     * @param renderingMode rengeringMode
     */
    public void setRenderingMode(RenderingMode renderingMode) {
        set(EntryKey.RENDERING_MODE, renderingMode);
    }

    /**
     * Indicates, if this entry is recurring. This is indicated by having any "recurring" property set (e.g.
     * {@link #getRecurringDaysOfWeek()}).
     * @return is a recurring event
     */
    public boolean isRecurring() {
        Set<DayOfWeek> daysOfWeek = getRecurringDaysOfWeek();
        return (daysOfWeek != null && !daysOfWeek.isEmpty())
                || getRecurringEndDateUTC() != null
                || getRecurringStartDateUTC() != null
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
     * @see #isRecurring()
     * @param daysOfWeek day of week
     */
    public void setRecurringDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        set(EntryKey.RECURRING_DAYS_OF_WEEKS, daysOfWeek);
    }

    /**
     * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     *
     * @see #isRecurring()
     * @return start date of recurrence
     */
    public Instant getRecurringStartDateUTC() {
        return get(EntryKey.RECURRING_START_DATE);
    }

    /**
     * Sets the start date for a recurring entry. Passing a date automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend infinitely to the future.
     * @see #isRecurring()
     * @param start start date
     */
    public void setRecurringStartDate(Instant start) {
        set(EntryKey.RECURRING_START_DATE, start);
    }

    /**
     * The end date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     *
     * @see #isRecurring()
     * @return end date of recurrence
     */
    public Instant getRecurringEndDateUTC() {
        return get(EntryKey.RECURRING_END_DATE);
    }

    /**
     * Sets the start date for a recurring entry. Passing a date automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend infinitely to the past.
     * @see #isRecurring()
     * @param end end date
     */
    public void setRecurringEndDate(Instant end) {
        set(EntryKey.RECURRING_END_DATE, end);
    }


    /**
     * Returns the recurring start of the entry as local date time based on the timezone returned by {@link #getEndTimezoneServer()}.
     *
     * @return start as local date time
     */
    public LocalDate getRecurringStartDate() {
        return getRecurringStartDate(getStartTimezoneServer());
    }

    /**
     * Returns the recurring end of the entry as local date time based on the timezone returned by {@link #getEndTimezoneServer()}.
     *
     * @return start as local date time
     */
    public LocalDate getRecurringEndDate() {
        return getRecurringEndDate(getEndTimezoneServer());
    }

    /**
     * Sets the given local date as recurring start. It is converted to an instant by using the
     * calendar's server start timezone.
     *
     * @param recurringStart start
     */
    public void setRecurringStart(LocalDate recurringStart) {
        setRecurringStartDate(recurringStart, getStartTimezoneServer());
    }

    /**
     * Sets the given local date as recurring end. It is converted to an instant by using the
     * calendar's server end timezone.
     *
     * @param recurringEnd end
     */
    public void setRecurringEnd(LocalDate recurringEnd) {
        setRecurringEndDate(recurringEnd, getEndTimezoneServer());
    }

    /**
     * The start time of recurrence per day. When not defined, recurrence will extend to the end of day for
     * a recurring entry.
     *
     * @see #isRecurring()
     * @return start time of recurrence
     */
    public LocalTime getRecurringStartTime() {
        return get(EntryKey.RECURRING_START_TIME);
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

    @Getter
    @RequiredArgsConstructor
    public static class DateUTCConverter<T extends JsonItem> implements JsonItem.JsonPropertyConverter<Instant, T> {
        private final SerializableFunction<T, Timezone> timezoneSupplier;

        @Override
        public JsonValue toJsonValue(Instant serverValue, T currentInstance) {
            return JsonUtils.toJsonValue(serverValue == null ? null : timezoneSupplier.apply(currentInstance).formatWithZoneId(serverValue));
        }

        @Override
        public Instant ofJsonValue(JsonValue clientValue, T currentInstance) {
            if (clientValue instanceof JsonNull) {
                return null;
            }

            if (clientValue instanceof JsonString) {
                return JsonUtils.parseDateTimeString(clientValue.asString(), timezoneSupplier.apply(currentInstance));
            }

            throw new IllegalArgumentException(clientValue + " must either be of type JsonNull or JsonString");
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
                .converter(new DateUTCConverter<>(Entry::getStartTimezoneClient))
                .build();

        /**
         * The entry's end as UTC.
         */
        public static final JsonItem.Key END = JsonItem.Key.builder()
                .name("end")
                .allowedType(Instant.class)
                .updateFromClientAllowed(true)
                .converter(new DateUTCConverter<>(Entry::getEndTimezoneClient))
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
                .defaultValue(true)
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
                .defaultValue(true)
                .build();

        /**
         * The color of this entry. Any valid css color is allowed (e.g. #f00, #ff0000, rgb(255,0,0), or red).
         */
        public static final JsonItem.Key COLOR = JsonItem.Key.builder()
                .name("color")
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
         * The extended property list. Contains any non standard property. Please see also the fullcalendar
         * documentation regarding extended properties. Be aware, that any non standard property you
         * set via "set(..., ...)" is not automatically put into this map, but this is done by the client later.
         */
        public static final JsonItem.Key CUSTOM_PROPERTIES = JsonItem.Key.builder()
                .name("extendedProps")
                .allowedType(Map.class) // Map<String, Object>
                .build();

        /**
         * The rendering mode of this entry. Never null
         */
        public static final JsonItem.Key RENDERING_MODE = JsonItem.Key.builder()
                .name("display")
                .allowedType(RenderingMode.class)
                .converter((RenderingMode serverValue, Entry currentInstance) -> JsonUtils.toJsonValue(serverValue == null ? RenderingMode.NONE : serverValue))
                .build();

        /**
         * Returns the days of weeks on which this event should recur. Null or empty when
         * no recurring is defined.
         */
        public static final JsonItem.Key RECURRING_DAYS_OF_WEEKS = JsonItem.Key.builder()
                .name("daysOfWeek")
                .allowedType(Set.class) // Set<DayOfWeek>
//                .converter((Set<DayOfWeek> recurringDaysOfWeeks, Entry currentInstance) ->
//                        JsonUtils.toJsonValue(
//                                recurringDaysOfWeeks == null || recurringDaysOfWeeks.isEmpty()
//                                        ? null
//                                        : recurringDaysOfWeeks.stream().map(dayOfWeek -> dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue())))
                .collectableItemConverter(dayOfWeek -> JsonUtils.toJsonValue(dayOfWeek == DayOfWeek.SUNDAY ? 0 : ((DayOfWeek) dayOfWeek).getValue()))
                .build();

        /**
         * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
         * is recurring).
         */
        public static final JsonItem.Key RECURRING_START_DATE = JsonItem.Key.builder()
                .name("startRecur")
                .allowedType(Instant.class)
                .converter(new DateUTCConverter<>(Entry::getStartTimezoneClient))
                .build();

        /**
         * The start time of recurrence. When not defined, the event will appear as an all day event.
         */
        public static final JsonItem.Key RECURRING_START_TIME = JsonItem.Key.builder()
                .name("startTime")
                .allowedType(LocalTime.class)
                .build();

        /**
         * The end date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
         * is recurring).
         */
        public static final JsonItem.Key RECURRING_END_DATE = JsonItem.Key.builder()
                .name("endRecur")
                .allowedType(Instant.class)
                .converter(new DateUTCConverter<>(Entry::getEndTimezoneClient))
                .build();

        /**
         * The start time of recurrence. Passing null on a recurring entry will make it appear as an all day event.
         */
        public static final JsonItem.Key RECURRING_END_TIME = JsonItem.Key.builder()
                .name("endTime")
                .allowedType(LocalTime.class)
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
         * Renders as normal entry.
         */
        NONE(null),

        /**
         * Renders as a solid rectangle in daygrid
         */
        BLOCK("block"),

        /**
         * Renders with a dot when in daygrid
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

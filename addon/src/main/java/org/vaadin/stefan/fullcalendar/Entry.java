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

import com.vaadin.flow.data.binder.BeanPropertySet;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.vaadin.stefan.fullcalendar.converters.ClientDateTimeConverter;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonIgnore;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.time.*;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Entry {

    public static final RenderingMode DEFAULT_RENDERING_MODE = RenderingMode.AUTO;
    private static final PropertySet<Entry> PROPERTIES = BeanPropertySet.get(Entry.class);

    private final String id;
    private String groupId;
    private String title;

    @JsonConverter(ClientDateTimeConverter.class)
    private LocalDateTime start;

    @JsonConverter(ClientDateTimeConverter.class)
    private LocalDateTime end;

    private boolean allDay;
    private boolean editable = true;
    private boolean startEditable = true;
    private boolean durationEditable = true;
    private String color;
    private String constraint;
    private String backgroundColor;
    private String borderColor;
    private String textColor;
    private boolean overlap = true;

    @NonNull
    private RenderingMode renderingMode = DEFAULT_RENDERING_MODE;
    private LocalDate recurringStartDate;
    private LocalDate recurringEndDate;
    private RecurringTime recurringStartTime; // see #139
    private RecurringTime recurringEndTime; // see #139

    private Set<DayOfWeek> recurringDaysOfWeek;
    private Set<String> classNames;
    private Map<String, Object> customProperties;

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
        this.id = id == null ? UUID.randomUUID().toString() : id;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonObject toJson() {
        // The toJson is implemented in a dynamic fashion to not need to extend it every time a
        // new property comes out.

        JsonObject json = Json.createObject();

        PROPERTIES.getProperties().forEach(def -> {
            String name = def.getName();
            Field field = FieldUtils.getField(Entry.class, name);
            try {
                if (field.getAnnotation(JsonIgnore.class) == null) {
                    ValueProvider<Entry, ?> getter = def.getGetter();
                    Object value = getter.apply(this);

                    JsonValue jsonValue;

                    JsonConverter converterAnnotation = field.getAnnotation(JsonConverter.class);
                    JsonItemPropertyConverter converter = null;
                    if (converterAnnotation != null) {
                        converter = converterAnnotation.value().getConstructor().newInstance();
                    }

                    if (converter != null && converter.supports(value)) {
                        jsonValue = converter.toJsonValue(value, this);
                    } else {
                        jsonValue = JsonUtils.toJsonValue(value);
                    }

                    String jsonName = name;
                    JsonName nameAnnotation = field.getAnnotation(JsonName.class);
                    if (nameAnnotation != null) {
                        jsonName = nameAnnotation.value();
                    }

                    json.put(jsonName, jsonValue);
                }
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });

        return json;
    }

    /**
     * Converts this instance to a json object, that only contains the id. This still represents
     * this item but without any data.
     *
     * @return json object representing this instance
     */
    public JsonObject toJsonWithIdOnly() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", getId());
        return jsonObject;
    }

    /**
     * Returns the entry's start as an {@link Instant}. The contained time is the same as when calling
     * {@link #getStart()}.
     *
     * @return start as Instant or null
     */
    public Instant getStartAsInstant() {
        return convertNullable(getStart(), (LocalDateTime start) -> start.toInstant(ZoneOffset.UTC));
    }

    /**
     * Returns the entry's start date.
     * @return start date or null
     */
    public LocalDate getStartAsLocalDate() {
        return convertNullable(getStart(), LocalDateTime::toLocalDate);
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
        this.start = start;
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
     * Returns the entry's end as an {@link Instant}. The contained time is the same as when calling
     * {@link #getEnd()}.
     *
     * @return end as Instant or null
     */
    public Instant getEndAsInstant() {
        return convertNullable(getEnd(), (LocalDateTime end) -> end.toInstant(ZoneOffset.UTC));
    }

    /**
     * Returns the entry's end date.
     * @return end date or null
     */
    public LocalDate getEndAsLocalDate() {
        return convertNullable(getEnd(), LocalDateTime::toLocalDate);
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
        this.end = end;
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
     * Returns the set of class names or creates a new, empty one, if none exists yet. The returned set is
     * the same as used internally, therefore any changes to it will be reflected to the client side on the
     * next refresh.
     *
     * @return class names set
     * @see #getClassNames()
     */
    public Set<String> getOrCreateClassNames() {
        Set<String> classNames = getClassNames();
        if (classNames == null) {
            classNames = new LinkedHashSet<>();
            setClassNames(classNames);
        }

        return classNames;
    }

    /**
     * Assign an additional className to this entry. Already assigned classNames will be kept.
     *
     * @param className class name to assign
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #addClassNames(String...)}
     */
    @Deprecated
    public void assignClassName(String className) {
        assignClassNames(Objects.requireNonNull(className));
    }

    /**
     * Assign additional classNames to this entry. Already assigned classNames will be kept.
     *
     * @param classNames class names to assign
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #addClassNames(String...)}
     */
    @Deprecated
    public void assignClassNames(@NotNull String... classNames) {
        assignClassNames(Arrays.asList(classNames));
    }

    /**
     * Assign additional classNames to this entry. Already assigned classNames will be kept.
     *
     * @param classNames class names to assign
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #addClassNames(Collection)}
     */
    @Deprecated
    public void assignClassNames(@NotNull Collection<String> classNames) {
        Objects.requireNonNull(classNames);
        getOrCreateClassNames().addAll(classNames);
    }

    /**
     * Adds css class names to this entry. Duplicates will automatically be filtered out.
     *
     * @param classNames class names to add
     * @throws NullPointerException when null is passed
     */
    public void addClassNames(@NotNull String... classNames) {
        assignClassNames(Arrays.asList(classNames));
    }

    /**
     * Adds css class names to this entry. Duplicates will automatically be filtered out.
     *
     * @param classNames class names to add
     * @throws NullPointerException when null is passed
     */
    public void addClassNames(@NotNull Collection<String> classNames) {
        Objects.requireNonNull(classNames);
        getOrCreateClassNames().addAll(classNames);
    }

    /**
     * Unassigns the given className from this entry.
     *
     * @param className class name to unassign
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #removeClassNames(String...)}
     */
    @Deprecated
    public void unassignClassName(String className) {
        unassignClassNames(Objects.requireNonNull(className));
    }

    /**
     * Unassigns the given classNames from this entry.
     *
     * @param classNames class names to unassign
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #removeClassNames(String...)}
     */
    @Deprecated
    public void unassignClassNames(@NotNull String... classNames) {
        unassignClassNames(Arrays.asList(classNames));
    }

    /**
     * Unassigns the given classNames from this entry.
     *
     * @param classNamesToRemove class names to unassign
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #removeClassNames(Collection)}
     */
    @Deprecated
    public void unassignClassNames(@NotNull Collection<String> classNamesToRemove) {
        removeClassNames(classNamesToRemove);
    }

    /**
     * Unassigns all classNames from this entry.
     *
     * @deprecated use {@link #removeClassNames()}
     */
    @Deprecated
    public void unassignAllClassNames() {
        removeClassNames();
    }

    /**
     * Removes the given classNames from this entry.
     *
     * @param classNames class names to remove
     * @throws NullPointerException when null is passed
     */
    public void removeClassNames(@NotNull String... classNames) {
        removeClassNames(Arrays.asList(classNames));
    }

    /**
     * Removes the given classNames from this entry.
     *
     * @param classNamesToRemove class names to remove
     * @throws NullPointerException when null is passed
     */
    public void removeClassNames(@NotNull Collection<String> classNamesToRemove) {
        Set<String> classNames = getClassNames();
        if (classNames != null) {
            classNames.removeAll(classNamesToRemove);
        }
    }

    /**
     * Removes the class names from this entry. Copies of the internal class name set will be unaffected;
     */
    public void removeClassNames() {
        setClassNames(null);
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

    /**
     * Same as {@link #isOverlap()}.
     *
     * @return is overlap allowed
     */
    public boolean isOverlapAllowed() {
        return isOverlap();
    }

    /**
     * Same as {@link #setOverlap(boolean)}
     *
     * @param overlap overlapping is allowed
     */
    public void setOverlapAllowed(boolean overlap) {
        setOverlap(true);
    }

    /**
     * Sets the entry Constraint.
     * Null or empty string resets the color to the FC's default.
     *
     * @param constraint constraint
     */
    public void setConstraint(String constraint) {
        this.constraint = StringUtils.trimToNull(constraint);
    }

    /**
     * Sets the color for this entry. This is interpreted as background and border color on the client side.
     * Null or empty string resets the color to the FC's default.
     *
     * @param color color
     */
    public void setColor(String color) {
        this.color = StringUtils.trimToNull(color);
    }

    /**
     * Sets the background color for this entry. Null or empty string resets the color to the FC's default.
     *
     * @param backgroundColor background color
     */
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = StringUtils.trimToNull(backgroundColor);
    }

    /**
     * Sets the text color for this entry. Null or empty string resets the color to the FC's default.
     *
     * @param textColor text color
     */
    public void setTextColor(String textColor) {
        this.textColor = StringUtils.trimToNull(textColor);
    }

    /**
     * Sets the border color for this entry. Null or empty string resets the color to the FC's default.
     *
     * @param borderColor border color
     */
    public void setBorderColor(String borderColor) {
        this.borderColor = StringUtils.trimToNull(borderColor);
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
     * Sets the rendering mode ("display") for this entry. Passing null will reset it to the default.
     * @param renderingMode rengeringMode
     */
    public void setRenderingMode(RenderingMode renderingMode) {
        this.renderingMode = renderingMode != null ? renderingMode : DEFAULT_RENDERING_MODE;
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
                || getRecurringStartTimeAsLocalTime() != null
                || getRecurringEndTimeAsLocalTime() != null;
    }

    /**
     * Sets days of week on which this entry shall recur. Setting a non empty set automatically marks this entry
     * as recurring. Pass null or an empty set may remove the recurring.
     *
     * @param daysOfWeek day of week
     * @see #isRecurring()
     */
    public void setRecurringDaysOfWeek(DayOfWeek... daysOfWeek) {
        setRecurringDaysOfWeek(daysOfWeek.length == 0 ? null : new HashSet<>(List.of(daysOfWeek)));
    }

    public void setRecurringDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        this.recurringDaysOfWeek = daysOfWeek;
    }

    /**
     * Returns the recurring start time as a recurring time instance. <br>
     * Since the FC allows recurring times to be
     * above the normal 24h span of a day, this format is used to represent start and end "times".
     *
     * @return recurring start time
     */
    public RecurringTime getRecurringStartTime() {
        return recurringStartTime;
    }

    /**
     * Returns the recurring start time as a local time.<br>
     * Since the FC allows recurring times to be above the normal 24h span of a day, using a LocalTime can lead to
     * issues, as it does not support times of 24h or above.
     *
     * @return LocalTime instance
     * @throws DateTimeException if this instance represents a time of 24 hours or above.
     */
    public LocalTime getRecurringStartTimeAsLocalTime() {
        return recurringStartTime != null ? recurringStartTime.toLocalTime() : null;
    }

    /**
     * Sets the start time for a recurring entry. Passing a non null value automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend to the
     * end of day.
     *
     * @param start start time
     * @see #isRecurring()
     */
    public void setRecurringStartTime(RecurringTime start) {
        this.recurringStartTime = start;
    }

    /**
     * Sets the start time for a recurring entry. Passing a non null value automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend to the
     * end of day.
     *
     * @param start start time
     * @see #isRecurring()
     */
    public void setRecurringStartTime(LocalTime start) {
        setRecurringStartTime(start != null ? new RecurringTime(start) : null);
    }

    /**
     * Returns the recurring end time as a recurring time instance. <br>
     * Since the FC allows recurring times to be
     * above the normal 24h span of a day, this format is used to represent end and end "times".
     *
     * @return recurring end time
     */
    public RecurringTime getRecurringEndTime() {
        return recurringEndTime;
    }

    /**
     * Returns the recurring end time as a local time.<br>
     * Since the FC allows recurring times to be above the normal 24h span of a day, using a LocalTime can lead to
     * issues, as it does not support times of 24h or above.
     *
     * @return LocalTime instance
     * @throws DateTimeException if this instance represents a time of 24 hours or above.
     */
    public LocalTime getRecurringEndTimeAsLocalTime() {
        return recurringEndTime != null ? recurringEndTime.toLocalTime() : null;
    }

    /**
     * Sets the end time for a recurring entry. Passing a non null value automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend to the
     * end of day.
     *
     * @param end end time
     * @see #isRecurring()
     */
    public void setRecurringEndTime(RecurringTime end) {
        this.recurringEndTime = end;
    }

    /**
     * Sets the end time for a recurring entry. Passing a non null value automatically marks this entry
     * as recurring. Passing null may remove the recurrence or let the recurring entry extend to the
     * end of day.
     *
     * @param end end time
     * @see #isRecurring()
     */
    public void setRecurringEndTime(LocalTime end) {
        setRecurringEndTime(end != null ? new RecurringTime(end) : null);
    }

    /**
     * Returns the recurring start. This method is a shortcut for combining {@link #getRecurringStartDate()}
     * and {@link #getRecurringStartTimeAsLocalTime()}. <br>
     * Will return null, when no recurrence date is defined. When only
     * a start date is defined, the returned date time will be at the start of that day.
     * <p></p>
     * Be careful, as LocalTime does not support times of 24h or
     * above and thus an exception will be thrown, if the start time is above that limit.
     *
     * @return start date time of recurrence
     * @throws DateTimeException if the start time represents a time of 24 hours or above.
     * @see #isRecurring()
     */
    public LocalDateTime getRecurringStart() {
        LocalDate startDate = getRecurringStartDate();
        if (startDate == null) {
            return null;
        }

        LocalTime startTime = getRecurringStartTimeAsLocalTime();
        return startTime != null ? startDate.atTime(startTime) : startDate.atStartOfDay();
    }

    /**
     * Returns the recurring end. This method is a shortcut for combining {@link #getRecurringEndDate()}
     * and {@link #getRecurringEndTimeAsLocalTime()}. <br>Will return null, when no recurrence date is defined. When only a
     * end date is defined, the returned date time will be at the end of that day.
     * <p></p>
     * Be careful, as LocalTime does not support times of 24h or
     * above and thus an exception will be thrown, if the end time is above that limit.
     *
     * @return end date time of recurrence
     * @throws DateTimeException if the end time represents a time of 24 hours or above.
     * @see #isRecurring()
     */
    public LocalDateTime getRecurringEnd() {
        LocalDate endDate = getRecurringEndDate();
        if (endDate == null) {
            return null;
        }

        LocalTime endTime = getRecurringEndTimeAsLocalTime();
        return endTime != null ? endDate.atTime(endTime) : endDate.atStartOfDay();
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
        setRecurringStartTime((RecurringTime) null);
    }

    /**
     * Clears the current recurring end date and time.
     */
    public void clearRecurringEnd() {
        setRecurringEndDate(null);
        setRecurringEndTime((RecurringTime) null);
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
        this.customProperties = customProperties;
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
        }
    }

    /**
     * Returns the map of the custom properties of this instance. This map is editable and any changes
     * will be sent to the client when entries are refreshed.
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
        return customProperties;
    }

    /**
     * Returns the custom properties map or an empty one, if none has yet been created. The map is not writable.
     *
     * @return map
     * @see #getCustomProperties()
     * @see #getOrCreateCustomProperties()
     */
    public Map<String, Object> getCustomPropertiesOrEmpty() {
        return customProperties != null ? Collections.unmodifiableMap(customProperties) : Collections.emptyMap();
    }

    /**
     * Returns the map of the custom properties of this instance. This map is editable and any changes
     * will be sent to the client when the entry provider is refreshed.
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
        if (customProperties == null) {
            customProperties = new HashMap<>();
        }
        return customProperties;
    }

    protected <T, R> R convertNullable(T value, SerializableFunction<T, R> converter) {
        return value != null ? converter.apply(value) : null;
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

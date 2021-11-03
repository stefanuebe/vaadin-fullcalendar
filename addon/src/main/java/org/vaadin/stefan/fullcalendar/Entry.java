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

import elemental.json.Json;
import elemental.json.JsonObject;
import lombok.*;

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
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"calendar", "description"})
public class Entry {
    /**
     * The entry's id.
     */
    private final String id;

    /**
     * The entry's title. This title will be shown on the client side.
     */
    private String title;

    /**
     * The entry's start as UTC.
     */
    private Instant start;

    /**
     * The entry's end as UTC.
     */
    private Instant end;

    /**
     * Indicates if this entry is all day or not. There might be time values set for start or end even if this
     * value is false. Changes on this field do <b>not</b> modifiy the date values directly. Any
     * changes on date values are done by the FC by event, but not this class.
     */
    private boolean allDay;

    /**
     * Determines which HTML classNames will be attached to the rendered event.
     */
    private Set<String> classNames;
    
    /**
     * Indicates if this entry is editable by the users. This value
     * is passed to the client side and interpreted there, but can also be used for server side checks.
     * <br><br>
     * This value has no impact on the resource API of this class.
     */
    private boolean editable;

    /**
     * Indicates if this entry's start is editable by the users. This value
     * is passed to the client side and interpreted there, but can also be used for server side checks.
     * <br><br>
     * This value has no impact on the resource API of this class.
     */
    private boolean startEditable;

    /**
     * Indicates if this entry's end is editable by the users. This value
     * is passed to the client side and interpreted there, but can also be used for server side checks.
     * <br><br>
     * This value has no impact on the resource API of this class.
     */
    private boolean durationEditable;

    /**
     * The color of this entry.
     */
    private String color;

    /**
     * The description of this entry.
     * <br><br>
     * Please be aware, that the description is a non-standard field on the client side and thus will not be
     * displayed in the entry's space. You can use it for custom entry rendering
     * (see {@link FullCalendar#setEntryContentCallback(String)}.
     */
    private String description;

    /**
     * The custom property list
     */
    private Map<String, Object> customProperties;

    /**
     * The rendering mode of this entry. Never null
     */
    @NonNull
    private RenderingMode renderingMode = RenderingMode.NONE;

    /**
     * Simple flag that indicates, if this entry is a recurring one or not. Recurring information
     * might be stored in the entry independently to this flag (server side only information).
     * Does <b>not</b> remove any recurring information, when set to false.
     */
    private boolean recurring;

    /**
     * Returns the days of weeks on which this event should recur. Null or empty when
     * no recurring is defined.
     */
    private Set<DayOfWeek> recurringDaysOfWeeks;

    /**
     * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     */
    private Instant recurringStartDate;

    /**
     * The start time of recurrence. When not defined, the event will appear as an all day event.
     */
    private LocalTime recurringStartTime;

    /**
     * The end date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     */
    private Instant recurringEndDate;

    /**
     * The start time of recurrence. Passing null on a recurring entry will make it appear as an all day event.
     */
    private LocalTime recurringEndTime;

    /**
     * Events that share a groupId will be dragged and resized together automatically.
     */
    private String groupId;

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
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.editable = true;
    }

    /**
     * Converts the content of this instance to json to be sent to the client.
     *
     * @return json
     */
    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", JsonUtils.toJsonValue(getId()));
        jsonObject.put("title", JsonUtils.toJsonValue(getTitle()));
        Optional.ofNullable(getGroupId()).ifPresent(g -> jsonObject.put("groupId", JsonUtils.toJsonValue(g)));

        boolean fullDayEvent = isAllDay();
        jsonObject.put("allDay", JsonUtils.toJsonValue(fullDayEvent));

        jsonObject.put("start", JsonUtils.toJsonValue(getStartUTC() == null ? null : getStartTimezone().formatWithZoneId(getStartUTC())));
        jsonObject.put("end", JsonUtils.toJsonValue(getEndUTC() == null ? null : getEndTimezone().formatWithZoneId(getEndUTC())));
        jsonObject.put("editable", isEditable());
        Optional.ofNullable(getColor()).ifPresent(s -> jsonObject.put("color", s));
        jsonObject.put("display", JsonUtils.toJsonValue(getRenderingMode()));

        jsonObject.put("daysOfWeek", JsonUtils.toJsonValue(recurringDaysOfWeeks == null || recurringDaysOfWeeks.isEmpty() ? null : recurringDaysOfWeeks.stream().map(dayOfWeek -> dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue())));
        jsonObject.put("startTime", JsonUtils.toJsonValue(recurringStartTime));
        jsonObject.put("endTime", JsonUtils.toJsonValue(recurringEndTime));
        jsonObject.put("startRecur", JsonUtils.toJsonValue(recurringStartDate == null ? null : getStartTimezone().formatWithZoneId(recurringStartDate)));
        jsonObject.put("endRecur", JsonUtils.toJsonValue(recurringEndDate == null ? null : getEndTimezone().formatWithZoneId(recurringEndDate)));

        jsonObject.put("description", JsonUtils.toJsonValue(getDescription()));

        jsonObject.put("classNames", JsonUtils.toJsonValue(getClassNames()));

        JsonObject extObject = Json.createObject();

        Map<String, Object> customProperties = getCustomProperties();
        if (!customProperties.isEmpty()) {
            for (Map.Entry<String, Object> extProp : customProperties.entrySet()) {
                extObject.put(extProp.getKey(), JsonUtils.toJsonValue(extProp.getValue()));
            }
        }

        jsonObject.put("customProperties", extObject);

        return jsonObject;
    }

    /**
     * Updates this instance with the content of the given object. Properties, that are not part of the object or are
     * of an invalid type will be unmodified. Same for the id. Properties in the object, that do not match with this
     * instance will be ignored.
     *
     * @param object json object / change set
     * @throws NullPointerException when null is passed
     */
    protected void update(@NotNull JsonObject object) {
        String id = object.getString("id");
        if (!this.id.equals(id)) {
            throw new IllegalArgumentException("IDs are not matching.");
        }

        JsonUtils.updateString(object, "title", this::setTitle);
        JsonUtils.updateString(object, "groupId", this::setGroupId);
        JsonUtils.updateBoolean(object, "editable", this::setEditable);
        JsonUtils.updateBoolean(object, "allDay", this::setAllDay);
        JsonUtils.updateDateTime(object, "start", this::setStart, getStartTimezone());
        JsonUtils.updateDateTime(object, "end", this::setEnd, getEndTimezone());
        JsonUtils.updateString(object, "color", this::setColor);
        JsonUtils.updateHashMap(object, "extendedProps", this::setCustomProperties);
        JsonUtils.updateSetString(object, "classNames", this::setClassNames);
    }

    /**
     * Returns the calendar instance of this entry. Is empty when not yet added to a calendar.
     *
     * @return calendar instance
     */
    public Optional<FullCalendar> getCalendar() {
        return Optional.ofNullable(calendar);
    }

    /**
     * Returns the start of the entry based on UTC.
     *
     * @return start
     */
    public Instant getStartUTC() {
        return start;
    }

    /**
     * Returns the start of the entry as local date time based on the timezone returned by {@link #getStartTimezone()} (by
     * default the calendars timezone or UTC).
     *
     * @return start as local date time
     */
    public LocalDateTime getStart() {
        return getStart(getStartTimezone());
    }

    /**
     * Sets the given instant time as start.
     *
     * @param start start
     */
    public void setStart(Instant start) {
        this.start = start;
    }

    /**
     * Sets the given local date time as start. It is converted to an instant by using the
     * calendars timezone. If no calendar has been set yet, <b>UTC</b> is taken.
     *
     * @param start start
     */
    public void setStart(LocalDateTime start) {
        setStart(start, getStartServerTimezone());
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
        return start != null ? LocalDateTime.ofInstant(start, timezone.getZoneId().getRules().getOffset(start)) : null;
    }

    /**
     * Returns the start of the entry based on UTC.
     *
     * @return start
     */
    public Instant getEndUTC() {
        return end;
    }

    /**
     * Returns the start of the entry as local date time based on the timezone returned by {@link #getStartTimezone()} (by
     * default the calendars timezone or UTC).
     *
     * @return start as local date time
     */
    public LocalDateTime getEnd() {
        return getEnd(getEndTimezone());
    }

    /**
     * Sets the given instant time as end.
     *
     * @param end end
     */
    public void setEnd(Instant end) {
        this.end = end;
    }

    /**
     * Sets the given local date time as end. It is converted to an instant by using the
     * calendars timezone. If no calendar has been set yet, UTC is taken.
     *
     * @param end end
     */
    public void setEnd(LocalDateTime end) {
        setEnd(end, getEndServerTimezone());
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
        this.start = timezone.convertToUTC(start);
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
        this.end = timezone.convertToUTC(end);
    }

    /**
     * Sets the color for this entry. Null resets the color to the FC's default.
     *
     * @param color color
     */
    public void setColor(String color) {
        this.color = color == null || color.trim().isEmpty() ? null : color;
    }

    /**
     * Sets custom property for this entry. An existing property will be overwritten.
     *
     * @param key   the name of the property to set
     * @param value value to set
     *
     */
    public void setCustomProperty(@NotNull String key, Object value) {
        if (customProperties == null) {
            customProperties = new HashMap<>();
        }
        customProperties.put(Objects.requireNonNull(key), value);
    }

    /**
     * Remove the custom property based on the name.
     *
     * @param key the name of the property to remove
     */
    public void removeCustomProperty(@NotNull String key) {
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
        if (customProperties != null) {
            // FIXME this will currently not remove the custom property from the client side!
            customProperties.remove(Objects.requireNonNull(key), Objects.requireNonNull(value));
        }
    }

    /**
     * Returns the timezone which is currently set to the calendar used for automatic conversion between Instant and LocalDateTime for the entry start.
     *
     * @return timezone
     */
    public Timezone getStartTimezone() {
        return calendar != null ? calendar.getTimezone() : Timezone.UTC;
    }

    /**
     * Returns the timezone which is currently set to the calendar used for automatic conversion between Instant and LocalDateTime for the entry end.
     *
     * @return timezone
     */
    public Timezone getEndTimezone() {
    	return calendar != null ? calendar.getTimezone() : Timezone.UTC;
    }
    
    /**
     * Returns the server's timezone used for automatic conversion between Instant and LocalDateTime for the entry start.
     *
     * @return timezone
     */
    public Timezone getStartServerTimezone() {
        return new Timezone( ZoneId.systemDefault() );
    }

    /**
     * Returns the server's timezone used for automatic conversion between Instant and LocalDateTime for the entry end.
     *
     * @return timezone
     */
    public Timezone getEndServerTimezone() {
    	return new Timezone( ZoneId.systemDefault() );
    }

    /**
     * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     *
     * @return start date of recurrence
     */
    public Instant getRecurringStartDateUTC() {
        return recurringStartDate;
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
     *
     * @return end date of recurrence
     */
    public Instant getRecurringEndDateUTC() {
        return recurringEndDate;
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
     * Returns an unmodifiable map of the custom properties of this instance.
     * @return Map
     */
    public Map<String, Object> getCustomProperties() {
        return customProperties != null ? Collections.unmodifiableMap(customProperties) : Collections.emptyMap();
    }
    
    /**
     * Assign an additional className to this entry. Already assigned classNames will be kept.
     *
     * @param String className
     * @throws NullPointerException when null is passed
     */
    public void assignClassName(String className) {
    	assignClassNames(Objects.requireNonNull(className));
    }
    
    /**
     * Assign additional classNames to this entry. Already assigned classNames will be kept.
     *
     * @param String className
     * @throws NullPointerException when null is passed
     */
    public void assignClassNames(@NotNull String... classNames) {
    	assignClassNames(Arrays.asList(classNames));
    }
    
    /**
     * Assign additional classNames to this entry. Already assigned classNames will be kept.
     *
     * @param String classNames
     * @throws NullPointerException when null is passed
     */
    public void assignClassNames(@NotNull Collection<String> classNames) {
        Objects.requireNonNull(classNames);
        if (this.classNames == null) {
            this.classNames = new LinkedHashSet<>(classNames);
        } else {
            this.classNames.addAll(classNames);
        }
    }
    
    /**
     * Unassigns the given className from this entry.
     *
     * @param String className
     * @throws NullPointerException when null is passed
     */
    public void unassignClassName(String className) {
    	unassignClassNames(Objects.requireNonNull(className));
    }
    
    /**
     * Unassigns the given classNames from this entry.
     *
     * @param String classNames
     * @throws NullPointerException when null is passed
     */
    public void unassignClassNames(@NotNull String... classNames) {
    	unassignClassNames(Arrays.asList(classNames));
    }

    /**
     * Unassigns the given classNames from this entry.
     *
     * @param String classNames
     * @throws NullPointerException when null is passed
     */
    public void unassignClassNames(@NotNull Collection<String> classNames) {
        if (this.classNames != null)
            this.classNames.removeAll(classNames);
    }
    
    /**
     * Unassigns all classNames from this entry.
     */
    public void unassignAllClassNames() {
        if (this.classNames != null) {
            this.classNames.clear();
            this.classNames = null;
        }
    }
    
    /**
     * Returns an unmodifiable set of the class names of this instance.
     * @return Set
     */
    public Set<String> getClassNames() {
    	return classNames != null ? Collections.unmodifiableSet(classNames) : Collections.emptySet();
    }
    
    /**
     * Returns the amount of assigned classNames.
     *
     * @return int size of classNames
     */
    public int getClassNamesSize() {
        return classNames != null ? classNames.size() : 0;
    }
    
    /**
     * Returns, if the entry has any class name assigned.
     *
     * @return Boolean hasClassNames
     */
    public boolean hasClassNames() {
    	return classNames != null && !classNames.isEmpty();
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

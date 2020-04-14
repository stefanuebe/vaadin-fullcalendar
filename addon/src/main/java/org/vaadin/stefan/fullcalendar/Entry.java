/*
 * Copyright 2018, Stefan Uebe
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

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a event / item in the full calendar. It is named Entry here to prevent name conflicts with
 * event handling mechanisms (e.g. a component event fired by clicking something).
 * <br><br>
 * To create a recurring entry, simply set any of the "recurring" properties. With any of them set the entry
 * is automatically recurring.
 * <br><br>
 * <i><b>Note: </b>Creation of an entry might be exported to a builder later.</i>
 */
public class Entry {
    private final String id;
    private boolean editable;
    private String title;
    private Instant start;
    private Instant end;
    private boolean allDay;
    private String color;
    private String description;
    private RenderingMode renderingMode = RenderingMode.NORMAL;

    private boolean recurring;
    private Set<DayOfWeek> recurringDaysOfWeeks;
    private Instant recurringStartDate;
    private Instant recurringEndDate;
    private LocalTime recurringStartTime;
    private LocalTime recurringEndTime;


    // TODO
    // groupId
    // className / classNames


    private FullCalendar calendar;

    public Entry(String id, String title, Instant start, Instant end, boolean allDay, boolean editable, String color, String description) {
        this(id);

        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.editable = editable;
        this.description = description;

        setColor(color);
    }

    public Entry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        this(id);

        this.title = title;
        this.start = start.toInstant(ZoneOffset.UTC);
        this.end = end.toInstant(ZoneOffset.UTC);
        this.allDay = allDay;
        this.editable = editable;
        this.description = description;

        setColor(color);
    }

    public Entry(String id, String title, LocalDateTime start, LocalDateTime end, Timezone timezone, boolean allDay, boolean editable, String color, String description) {
        this(id);

        this.title = title;
        this.start = timezone.convertToUTC(start);
        this.end = timezone.convertToUTC(end);
        this.allDay = allDay;
        this.editable = editable;
        this.description = description;

        setColor(color);
    }

    /**
     * Empty instance.
     */
    public Entry() {
        this(null);
        this.editable = true;
    }

    protected Entry(String id) {
        this.id = id != null ? id : UUID.randomUUID().toString();
    }

    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", JsonUtils.toJsonValue(getId()));
        jsonObject.put("title", JsonUtils.toJsonValue(getTitle()));

        boolean fullDayEvent = isAllDay();
        jsonObject.put("allDay", JsonUtils.toJsonValue(fullDayEvent));

        jsonObject.put("start", JsonUtils.toJsonValue(getStartUTC() == null ? null : getStartTimezone().formatWithZoneId(getStartUTC())));
        jsonObject.put("end", JsonUtils.toJsonValue(getEndUTC() == null ? null : getEndTimezone().formatWithZoneId(getEndUTC())));
        jsonObject.put("editable", isEditable());
        Optional.ofNullable(getColor()).ifPresent(s -> jsonObject.put("color", s));
        jsonObject.put("rendering", JsonUtils.toJsonValue(getRenderingMode()));

        jsonObject.put("daysOfWeek", JsonUtils.toJsonValue(recurringDaysOfWeeks == null || recurringDaysOfWeeks.isEmpty() ? null : recurringDaysOfWeeks.stream().map(dayOfWeek -> dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue())));
        jsonObject.put("startTime", JsonUtils.toJsonValue(recurringStartTime));
        jsonObject.put("endTime", JsonUtils.toJsonValue(recurringEndTime));
        jsonObject.put("startRecur", JsonUtils.toJsonValue(recurringStartDate == null ? null : getStartTimezone().formatWithZoneId(recurringStartDate)));
        jsonObject.put("endRecur", JsonUtils.toJsonValue(recurringEndDate == null ? null : getEndTimezone().formatWithZoneId(recurringEndDate)));

        return jsonObject;
    }

    /**
     * Updates this instance with the content of the given object. Properties, that are not part of the object or are
     * of an invalid type will be unmodified. Same for the id. Properties in the object, that do not match with this
     * instance will be ignored.
     *
     * @param object json object / change set
     */
    protected void update(JsonObject object) {
        String id = object.getString("id");
        if (!this.id.equals(id)) {
            throw new IllegalArgumentException("IDs are not matching.");
        }

        JsonUtils.updateString(object, "title", this::setTitle);
        JsonUtils.updateBoolean(object, "editable", this::setEditable);
        JsonUtils.updateBoolean(object, "allDay", this::setAllDay);
        JsonUtils.updateDateTime(object, "start", this::setStart, getStartTimezone());
        JsonUtils.updateDateTime(object, "end", this::setEnd, getEndTimezone());
        JsonUtils.updateString(object, "color", this::setColor);
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
     * Sets the calendar instance to be used internally. There is NO automatic removal or add when the calendar changes.
     *
     * @param calendar calendar instance
     */
    protected void setCalendar(FullCalendar calendar) {
        this.calendar = calendar;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
     * Sets the entry's start as UTC.
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
        setStart(start, getStartTimezone());
    }

    /**
     * Returns the start of the entry as local date time based on the given timezone
     *
     * @param timezone timezone
     * @return start as local date time
     */
    public LocalDateTime getStart(Timezone timezone) {
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
     * Sets the entry's end.
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
        setEnd(end, getEndTimezone());
    }

    /**
     * Returns the start of the entry as local date time based on the given timezone
     *
     * @param timezone timezone
     * @return start as local date time
     */
    public LocalDateTime getEnd(Timezone timezone) {
        return end != null ? LocalDateTime.ofInstant(end, timezone.getZoneId().getRules().getOffset(end)) : null;
    }

    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Marks this entry as an all day entry or not. This does <b>not</b> modifiy the date values directly. Any
     * changes on date values are done by the FC by event, but not this class.
     *
     * @param allDay all day entry
     */
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Sets the given local date time as start. It is converted to an Instant by using the given timezone.
     *
     * @param start    start
     * @param timezone timezone
     */
    public void setStart(LocalDateTime start, Timezone timezone) {
        this.start = timezone.convertToUTC(start);
    }

    /**
     * Sets the given local date time as end. It is converted to an Instant by using the given timezone.
     *
     * @param end      end
     * @param timezone timezone
     */
    public void setEnd(LocalDateTime end, Timezone timezone) {
        this.end = timezone.convertToUTC(end);
    }

    /**
     * Returns the color for this entry.
     *
     * @return color
     */
    public String getColor() {
        return color;
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
     * Returns the rendering mode of this entry. Never null.
     *
     * @return rendering mode
     */
    public RenderingMode getRenderingMode() {
        return renderingMode;
    }

    /**
     * Sets the rendering of this entry. Default is {@link RenderingMode#NORMAL}
     *
     * @param renderingMode rendering
     * @throws NullPointerException when passing null
     */
    public void setRenderingMode(@NotNull RenderingMode renderingMode) {
        Objects.requireNonNull(renderingMode);
        this.renderingMode = renderingMode;
    }

    /**
     * Gets the description of an event.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of an event.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the timezone used for automatic conversion between Instant and LocalDateTime for the entry start.
     *
     * @return timezone
     */
    public Timezone getStartTimezone() {
        return calendar != null ? calendar.getTimezone() : Timezone.UTC;
    }

    /**
     * Returns the timezone used for automatic conversion between Instant and LocalDateTime for the entry end.
     *
     * @return timezone
     */
    public Timezone getEndTimezone() {
        return calendar != null ? calendar.getTimezone() : Timezone.UTC;
    }

    /**
     * Simple flag that indicates, if this entry is a recurring one or not. Recurring information
     * might be stored in the event independently to this flag (server side only information).
     *
     * @return is recurring
     */
    public boolean isRecurring() {
        return recurring;
    }

    /**
     * Simple flag that indicates, if this entry is a recurring one or not. This is a server side only information.
     * Does <b>not</b> remove any recurring information, when set to false.
     *
     * @param recurring is recurring
     */
    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    /**
     * Returns the days of weeks on which this event should recur. Null or empty when
     * no recurring is defined.
     *
     * @return days of week of recurrence
     */
    public Set<DayOfWeek> getRecurringDaysOfWeeks() {
        return recurringDaysOfWeeks;
    }

    /**
     * Sets the days of weeks on which this event should recur.
     *
     * @param recurringDaysOfWeeks days of week for recurrence
     */
    public void setRecurringDaysOfWeeks(Set<DayOfWeek> recurringDaysOfWeeks) {
        this.recurringDaysOfWeeks = recurringDaysOfWeeks;
    }

    /**
     * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     *
     * @return start date of recurrence
     */
    public Instant getRecurringStartDate() {
        return recurringStartDate;
    }

    /**
     * The start date of recurrence. Passing null on a recurring entry will extend the recurrence infinitely to the past.
     *
     * @param recurringStartDate start date or recurrence
     */
    public void setRecurringStartDate(Instant recurringStartDate) {
        this.recurringStartDate = recurringStartDate;
    }

    /**
     * The start date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     * <br><br>
     * The given timezone is used to convert the instant to a local date instance.
     *
     * @param timezone timezone
     * @return start date of recurrence
     */
    public LocalDate getRecurringStartDate(Timezone timezone) {
        return recurringStartDate != null ? LocalDateTime.ofInstant(recurringStartDate, timezone.getZoneId().getRules().getOffset(recurringStartDate)).toLocalDate() : null;
    }

    /**
     * The start date of recurrence. Passing null on a recurring entry will extend the recurrence infinitely to the past.
     * It is converted to an Instant by using the given timezone.
     *
     * @param recurringStartDate start date or recurrence
     * @param timezone           timezone
     */
    public void setRecurringStartDate(LocalDate recurringStartDate, Timezone timezone) {
        setRecurringStartDate(timezone.convertToUTC(recurringStartDate));
    }


    /**
     * The end date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     *
     * @return end date of recurrence
     */
    public Instant getRecurringEndDate() {
        return recurringEndDate;
    }

    /**
     * The end date of recurrence. Passing null on a recurring entry will extend the recurrence infinitely to the past.
     *
     * @param recurringEndDate end date or recurrence
     */
    public void setRecurringEndDate(Instant recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
    }

    /**
     * The end date of recurrence. When not defined, recurrence will extend infinitely to the past (when the entry
     * is recurring).
     * <br><br>
     * The given timezone is used to convert the instant to a local date instance.
     *
     * @param timezone timezone
     * @return end date of recurrence
     */
    public LocalDate getRecurringEndDate(Timezone timezone) {
        return recurringEndDate != null ? LocalDateTime.ofInstant(recurringEndDate, timezone.getZoneId().getRules().getOffset(recurringEndDate)).toLocalDate() : null;
    }

    /**
     * The end date of recurrence. Passing null on a recurring entry will extend the recurrence infinitely to the past.
     * It is converted to an Instant by using the given timezone.
     *
     * @param recurringEndDate end date or recurrence
     * @param timezone         timezone
     */
    public void setRecurringEndDate(LocalDate recurringEndDate, Timezone timezone) {
        setRecurringEndDate(timezone.convertToUTC(recurringEndDate));
    }

    /**
     * The start time of recurrence. When not defined, the event will appear as an all day event.
     *
     * @return start time of recurrence
     */
    public LocalTime getRecurringStartTime() {
        return recurringStartTime;
    }

    /**
     * The start time of recurrence. Passing null on a recurring entry will make it appear as an all day event.
     *
     * @param recurringStartTime start time or recurrence
     */
    public void setRecurringStartTime(LocalTime recurringStartTime) {
        this.recurringStartTime = recurringStartTime;
    }

    /**
     * The end time of recurrence. When not defined, the event will appear with default duration.
     *
     * @return end time of recurrence
     */
    public LocalTime getRecurringEndTime() {
        return recurringEndTime;
    }

    /**
     * The end time of recurrence. Passing null on a recurring entry will make it appear with default duration.
     *
     * @param recurringEndTime end time or recurrence
     */
    public void setRecurringEndTime(LocalTime recurringEndTime) {
        this.recurringEndTime = recurringEndTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entry event = (Entry) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "title='" + title + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", allDay=" + allDay +
                ", color='" + color + '\'' +
                ", description='" + description + '\'' +
                ", editable=" + editable +
                ", id='" + id + '\'' +
                ", calendar=" + calendar +
                ", rendering=" + renderingMode +
                ", startTimezone=" + getStartTimezone() +
                ", endTimezone=" + getEndTimezone() +
                '}';
    }

    /**
     * Constants for rendering of an event.
     */
    public enum RenderingMode implements ClientSideValue {
        /**
         * Renders as normal entry.
         */
        NORMAL(null),

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

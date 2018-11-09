package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * Definition of business hours for a calendar instance.
 */
public class BusinessHours {
    /**
     * Represents all days of week.
     */
    public static final Set<DayOfWeek> ALL_DAYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DayOfWeek.values())));

    private final Set<DayOfWeek> dayOfWeeks;
    private final LocalTime start;
    private final LocalTime end;

    /**
     * Creates a new instance. Defines start and end time of the business hours. Passing null means start / end of day.
     *
     * @param start start time
     * @param end   end time
     */
    public BusinessHours(LocalTime start, LocalTime end) {
        this(ALL_DAYS, start, end);
    }

    /**
     * Creates a new instance. Defines start of the business hours. End time is automatically end of day.
     * Passing null means start of day.
     * @param start start time
     */
    public BusinessHours(LocalTime start) {
        this(ALL_DAYS, start, null);
    }

    /**
     * Creates a new instance. Defines the days of business. Time will be all day automatically.
     * <p/>
     * Passing null for days is the same as passing an empty set (means no business days at all).
     *
     * @param dayOfWeeks days of business
     */
    public BusinessHours(Set<DayOfWeek> dayOfWeeks) {
        this(dayOfWeeks, null, null);
    }

    /**
     * Creates a new instance. Defines the days of business plus start time for each of these days.
     * End time is automatically end of day.
     * <p/>
     * Passing null for days is the same as passing an empty set (means no business days at all).
     * Passing null for start means start of day.
     *
     * @param dayOfWeeks days of business
     * @param start      start time
     */
    public BusinessHours(Set<DayOfWeek> dayOfWeeks, LocalTime start) {
        this(dayOfWeeks, start, null);
    }

    /**
     * Creates a new instance. Defines the days of business plus start and end time for each of these days.
     * Passing null for days is the same as passing an empty set (means no business days at all). Passing null for times
     * means start / end of day.
     *
     * @param dayOfWeeks days of business
     * @param start      start time
     * @param end        end time
     */
    public BusinessHours(Set<DayOfWeek> dayOfWeeks, LocalTime start, LocalTime end) {
        this.dayOfWeeks = dayOfWeeks == null ? Collections.emptySet() : dayOfWeeks;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the end time or empty if none was set.
     *
     * @return end time or empty
     */
    public Optional<LocalTime> getEnd() {
        return Optional.ofNullable(end);
    }

    /**
     * Returns the start time or empty if none was set.
     *
     * @return start time or empty
     */

    public Optional<LocalTime> getStart() {
        return Optional.ofNullable(start);
    }

    /**
     * Returns the days of week for business. Empty if there are no business days.
     *
     * @return days of week for business
     */
    public Set<DayOfWeek> getDayOfWeeks() {
        return Collections.unmodifiableSet(dayOfWeeks);
    }

    /**
     * Converts the given object into a json object.
     * @return json object
     */
    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();

        jsonObject.put("dow", JsonUtils.toJsonValue(dayOfWeeks.stream().map(DayOfWeek::getValue)));
        jsonObject.put("start", JsonUtils.toJsonValue(start != null ? start : "00:00"));
        jsonObject.put("end", JsonUtils.toJsonValue(end != null ? end : "1.00:00"));

        return jsonObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BusinessHours that = (BusinessHours) o;
        return Objects.equals(dayOfWeeks, that.dayOfWeeks) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeeks, start, end);
    }

    @Override
    public String toString() {
        return "BusinessHours{" +
                "dayOfWeeks=" + dayOfWeeks +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}

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
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * Definition of business hours for a calendar instance.
 */
@EqualsAndHashCode
@ToString
public class BusinessHours {
    /**
     * Represents all days of week.
     */
    public static final DayOfWeek[] ALL_DAYS = DayOfWeek.values();

    /**
     * Represents default business days (mo-fr).
     */
    public static final DayOfWeek[] DEFAULT_BUSINESS_WEEK = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};

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
        this(start, end, ALL_DAYS);
    }

    /**
     * Creates a new instance. Defines start of the business hours. End time is automatically end of day.
     * Passing null means start of day.
     * @param start start time
     */
    public BusinessHours(LocalTime start) {
        this(start, null, ALL_DAYS);
    }

    /**
     * Creates a new instance. Defines the days of business. Time will be all day automatically.
     * <br><br>
     * Passing null for days is the same as passing an empty set (means no business days at all).
     *
     * @param dayOfWeeks days of business
     */
    public BusinessHours(DayOfWeek... dayOfWeeks) {
        this(null, null, dayOfWeeks);
    }

    /**
     * Creates a new instance. Defines the days of business plus start time for each of these days.
     * End time is automatically end of day.
     * <br><br>
     * Passing null for days is the same as passing an empty set (means no business days at all).
     * Passing null for start means start of day.
     *
     * @param dayOfWeeks days of business
     * @param start      start time
     */
    public BusinessHours(LocalTime start, DayOfWeek... dayOfWeeks) {
        this(start, null, dayOfWeeks);
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
    public BusinessHours(LocalTime start, LocalTime end, DayOfWeek... dayOfWeeks) {
        Set<DayOfWeek> set;
        if (dayOfWeeks == null || dayOfWeeks.length == 0) {
            set = Collections.emptySet();
        } else {
            set = new LinkedHashSet<>(Arrays.asList(dayOfWeeks));
            if (set.stream().anyMatch(Objects::isNull)) {
                throw new NullPointerException("Day of weeks must not contain null");
            }
        }

        this.dayOfWeeks = set;
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

        jsonObject.put("daysOfWeek", JsonUtils.toJsonValue(dayOfWeeks.stream().map(dayOfWeek -> convertToClientSideDow(dayOfWeek))));

        jsonObject.put("startTime", JsonUtils.toJsonValue(start != null ? start : "00:00"));
        jsonObject.put("endTime", JsonUtils.toJsonValue(end != null ? end : "24:00"));

        return jsonObject;
    }

    /**
     * Converts the given day of the week to the correct client side number (handles sundays differently).
     * @param dayOfWeek day of week
     * @return client side representation
     * @throws NullPointerException when null is passed
     */
    public static int convertToClientSideDow(@NotNull DayOfWeek dayOfWeek) {
        return Objects.requireNonNull(dayOfWeek) == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
    }

}

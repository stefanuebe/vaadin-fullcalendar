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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Definition of business hours for a calendar instance.
 * <p>
 *     Use one of the static methods to define the days of week. You can define the range per day using the {@link #start}
 *     and {@link #end} methods or leave them, to let the business hours range from the start of the day to its end.
 * </p>
 */
@EqualsAndHashCode
@ToString
public class BusinessHours {
    /**
     * Represents all days of week.
     */
    public static final Set<DayOfWeek> ALL_DAYS = Set.of(DayOfWeek.values());

    /**
     * Represents default business days (mo-fr).
     */
    public static final Set<DayOfWeek> DEFAULT_BUSINESS_WEEK = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private final Set<DayOfWeek> dayOfWeeks;
    private final LocalTime start;
    private final LocalTime end;

    /**
     * Creates a new instance for the given days of week.
     * <br><br>
     *
     *
     * @param dayOfWeeks days of business
     * @throws IllegalArgumentException if the array is empty
     */
    public static BusinessHours of(DayOfWeek[] dayOfWeeks) {
        return of(Set.of(dayOfWeeks));
    }

    /**
     * Creates a new instance for the given days of week.
     * <br><br>
     *
     * @param dayOfWeek days of business
     * @param additionalDaysOfWeek additional days of business
     */
    public static BusinessHours of(DayOfWeek dayOfWeek, DayOfWeek... additionalDaysOfWeek) {
        return of(Stream.concat(Stream.of(dayOfWeek), Stream.of(additionalDaysOfWeek)).collect(Collectors.toSet()));
    }

    /**
     * Creates a new instance for the given days of week.
     * <br><br>
     *
     *
     * @param dayOfWeeks days of business
     * @throws IllegalArgumentException if the set is empty
     */
    public static BusinessHours of(Set<DayOfWeek> dayOfWeeks) {
        return new BusinessHours(LocalTime.MIN, LocalTime.MAX, dayOfWeeks);
    }

    /**
     * Creates a new instance for all days of week.
     * <br><br>
     */
    public static BusinessHours allDays() {
        return of(ALL_DAYS);
    }


    /**
     * Creates a new instance for all days of a normal business week (Mo-Fr).
     * <br><br>
     *
     */
    public static BusinessHours businessWeek() {
        return of(DEFAULT_BUSINESS_WEEK);
    }


    /**
     * Creates a new instance. Defines the days of business plus start and end time for each of these days.
     * Passing null for times
     * means start / end of day.
     *
     * @param start      start time
     * @param end        end time
     * @param dayOfWeeks days of business
     */
    private BusinessHours(LocalTime start, LocalTime end, Set<DayOfWeek> dayOfWeeks) {
        if (dayOfWeeks == null || dayOfWeeks.isEmpty()) {
            throw new IllegalArgumentException("Days of week must not be null nor empty");
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException("Times must not be null");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must not be before start");
        }

        this.dayOfWeeks = dayOfWeeks;
        this.start = start;
        this.end = end;

    }

    /**
     * Creates a copy of this instance with the given time as its new starting time.
     * @param start new start time
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start
     */
    public BusinessHours start(LocalTime start) {
        return new BusinessHours(start, end, dayOfWeeks);
    }

    /**
     * Creates a copy of thist instance with the given time as its new ending time.
     * @param end new end time
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start
     */
    public BusinessHours end(LocalTime end) {
        return new BusinessHours(start, end, dayOfWeeks);
    }

    /**
     * Creates a copy of thist instance with the given int as its new starting hour. Must be a value between 0 and 24,
     * where 24 is converted to {@link java.time.LocalDate#MAX}.
     * @param startingHour new starting hour
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start or the given hour exceeds 24
     */
    public BusinessHours start(int startingHour) {
        return start(startingHour == 24 ? LocalTime.MAX : LocalTime.of(startingHour, 0));
    }

    /**
     * Creates a copy of thist instance with the given time as its new ending hour. Must be a value between 0 and 24,
     * where 24 is converted to {@link java.time.LocalDate#MAX}.
     * @param endingHour new ending hour
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start or the given hour exceeds 24
     */
    public BusinessHours end(int endingHour) {
        return end(endingHour == 24 ? LocalTime.MAX : LocalTime.of(endingHour, 0));
    }

    /**
     * Returns the end time. If none has been set, the returned time will be {@link LocalTime#MAX}.
     *
     * @return end time or empty
     */
    public LocalTime getEnd() {
        return end;
    }

    /**
     * Returns the start time. If none has been set, the returned time will be {@link LocalTime#MIN}.
     *
     * @return start time or empty
     */

    public LocalTime getStart() {
        return start;
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
    protected ObjectNode toJson() {
        ObjectNode jsonObject = JsonFactory.createObject();

        jsonObject.set("daysOfWeek", JsonUtils.toJsonNode(dayOfWeeks.stream().map(BusinessHours::convertToClientSideDow)));

        jsonObject.set("startTime", JsonUtils.toJsonNode(start));
        jsonObject.set("endTime", JsonUtils.toJsonNode(end == LocalTime.MAX ? "24:00" : end));

        return jsonObject;
    }

    /**
     * Converts the given day of the week to the correct client side number (handles sundays differently).
     * @param dayOfWeek day of week
     * @return client side representation
     * @throws NullPointerException when null is passed
     */
    public static int convertToClientSideDow(DayOfWeek dayOfWeek) {
        return Objects.requireNonNull(dayOfWeek) == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
    }

}

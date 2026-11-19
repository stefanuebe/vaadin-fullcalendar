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
package org.vaadin.stefan.fullcalendar.option;

import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Definition of business hours for a calendar instance, also a recurring window of a {@link ConstraintValue}.
 * <p>
 *     Use one of the static methods to define the days of week. You can define the range per day using the {@link #start}
 *     and {@link #end} methods or leave them, to let the business hours range from the start of the day to its end.
 *     The end may run past midnight, as an {@link ExtendedLocalTime} such as 26:00.
 *     {@link #recurringStartDate} and {@link #recurringEndDate} limit the dates on which they recur.
 * </p>
 */
@EqualsAndHashCode
@ToString
public non-sealed class BusinessHours implements ConstraintValue.Window {
    /**
     * Represents all days of week.
     */
    public static final Set<DayOfWeek> ALL_DAYS = Set.of(DayOfWeek.values());

    /**
     * Represents default business days (mo-fr).
     */
    public static final Set<DayOfWeek> DEFAULT_BUSINESS_WEEK = Set.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private static final ExtendedLocalTime START_OF_DAY = ExtendedLocalTime.of(0);
    private static final ExtendedLocalTime END_OF_DAY = ExtendedLocalTime.of(24);

    private final Set<DayOfWeek> dayOfWeeks;
    private final ExtendedLocalTime start;
    private final ExtendedLocalTime end;
    private final LocalDate recurringStartDate;
    private final LocalDate recurringEndDate;

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
        return new BusinessHours(START_OF_DAY, END_OF_DAY, dayOfWeeks, null, null);
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
     *
     * @param start      start time
     * @param end        end time
     * @param dayOfWeeks days of business
     * @param recurringStartDate first date on which they recur, or null for no limit
     * @param recurringEndDate date after the last one on which they recur, or null for no limit
     */
    private BusinessHours(ExtendedLocalTime start, ExtendedLocalTime end, Set<DayOfWeek> dayOfWeeks,
                          LocalDate recurringStartDate,
                          LocalDate recurringEndDate) {
        if (dayOfWeeks == null || dayOfWeeks.isEmpty()) {
            throw new IllegalArgumentException("Days of week must not be null nor empty");
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException("Times must not be null");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must not be before start");
        }

        if (recurringStartDate != null && recurringEndDate != null && !recurringStartDate.isBefore(recurringEndDate)) {
            throw new IllegalArgumentException("The recurring start date must be before the recurring end date");
        }

        this.dayOfWeeks = Collections.unmodifiableSet(EnumSet.copyOf(dayOfWeeks));
        this.start = start;
        this.end = end;
        this.recurringStartDate = recurringStartDate;
        this.recurringEndDate = recurringEndDate;

    }

    /**
     * Creates a copy of this instance with the given time as its new starting time. Seconds are dropped.
     * @param start new start time
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start
     * @throws NullPointerException when null is passed
     */
    public BusinessHours start(LocalTime start) {
        return start(ExtendedLocalTime.of(start));
    }

    /**
     * Creates a copy of this instance with the given time as its new starting time. For a start after 24:00, set the
     * end first, because the end of the day is the default end.
     * @param start new start time
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start or null is passed
     */
    public BusinessHours start(ExtendedLocalTime start) {
        return new BusinessHours(start, end, dayOfWeeks, recurringStartDate, recurringEndDate);
    }

    /**
     * Creates a copy of this instance with the given time as its new ending time. {@link LocalTime#MAX} means the end
     * of the day, 24:00. Seconds are dropped.
     * For an end after midnight use {@link #end(ExtendedLocalTime)}.
     * @param end new end time
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start
     * @throws NullPointerException when null is passed
     */
    public BusinessHours end(LocalTime end) {
        return end(end.equals(LocalTime.MAX) ? END_OF_DAY : ExtendedLocalTime.of(end));
    }

    /**
     * Creates a copy of this instance with the given time as its new ending time. It may run past midnight, such as
     * 26:00 for 2am of the next day.
     * @param end new end time
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start or null is passed
     */
    public BusinessHours end(ExtendedLocalTime end) {
        return new BusinessHours(start, end, dayOfWeeks, recurringStartDate, recurringEndDate);
    }

    /**
     * Creates a copy of this instance that recurs from the given date on, the library's {@code startRecur}.
     * @param recurringStartDate first date on which they recur, or null for no limit
     * @return new instance
     * @throws IllegalArgumentException if the date is not before the recurring end date
     */
    public BusinessHours recurringStartDate(LocalDate recurringStartDate) {
        return new BusinessHours(start, end, dayOfWeeks, recurringStartDate, recurringEndDate);
    }

    /**
     * Creates a copy of this instance that recurs until the day before the given date, the library's
     * {@code endRecur}. The date is exclusive, as the end of entries.
     * @param recurringEndDate date after the last one on which they recur, or null for no limit
     * @return new instance
     * @throws IllegalArgumentException if the date is not after the recurring start date
     */
    public BusinessHours recurringEndDate(LocalDate recurringEndDate) {
        return new BusinessHours(start, end, dayOfWeeks, recurringStartDate, recurringEndDate);
    }

    /**
     * Returns the first date on which they recur.
     *
     * @return the date, or null for no limit
     */
    public LocalDate getRecurringStartDate() {
        return recurringStartDate;
    }

    /**
     * Returns the date after the last one on which they recur.
     *
     * @return the date, or null for no limit
     */
    public LocalDate getRecurringEndDate() {
        return recurringEndDate;
    }

    /**
     * Creates a copy of this instance with the given full hour as its new starting time.
     * @param startingHour new starting hour, not negative
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start or the hour is negative
     */
    public BusinessHours start(int startingHour) {
        return start(ExtendedLocalTime.of(startingHour));
    }

    /**
     * Creates a copy of this instance with the given full hour as its new ending time. 24 is the end of the day, a
     * larger hour runs past midnight, such as 26 for 2am of the next day.
     * @param endingHour new ending hour, not negative
     * @return new instance
     * @throws IllegalArgumentException if the end is before the start or the hour is negative
     */
    public BusinessHours end(int endingHour) {
        return end(ExtendedLocalTime.of(endingHour));
    }

    /**
     * Returns the end time. If none has been set, it is 24:00, the end of the day. It may run past midnight.
     *
     * @return end time
     */
    public ExtendedLocalTime getEnd() {
        return end;
    }

    /**
     * Returns the start time. If none has been set, it is 00:00.
     *
     * @return start time
     */
    public ExtendedLocalTime getStart() {
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
    public ObjectNode toJson() {
        ObjectNode jsonObject = JsonFactory.createObject();

        jsonObject.set("daysOfWeek", JsonUtils.toJsonNode(dayOfWeeks.stream().map(BusinessHours::convertToClientSideDow)));

        jsonObject.put("startTime", start.toFormattedString());
        jsonObject.put("endTime", end.toFormattedString());
        if (recurringStartDate != null) {
            jsonObject.put("startRecur", JsonUtils.formatClientSideDateString(recurringStartDate));
        }
        if (recurringEndDate != null) {
            jsonObject.put("endRecur", JsonUtils.formatClientSideDateString(recurringEndDate));
        }

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

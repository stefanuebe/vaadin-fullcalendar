/*
 * Copyright 2026, Stefan Uebe
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

import org.vaadin.stefan.fullcalendar.Entry;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * The value of an option that limits where entries can be dragged, resized or selected: the entries of a group, the
 * business hours of the calendar, or windows of time, recurring ones and fixed spans. A {@code switch} over it needs no
 * {@code default} branch.
 * <p>
 * A sealed type rather than one Java type, because the library takes a string, which is a group id or the keyword
 * {@code "businessHours"}, as well as objects.
 */
public sealed interface ConstraintValue extends Serializable {

    /** Within the business hours of the calendar, the library's {@code "businessHours"}. */
    ConstraintValue BUSINESS_HOURS = new CalendarBusinessHours();

    /**
     * Creates the value for the entries of a group.
     *
     * @param groupId the group id
     * @return the value
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when {@code "businessHours"} is passed, use {@link #BUSINESS_HOURS}
     */
    static ConstraintValue ofGroupId(String groupId) {
        return new GroupId(groupId);
    }

    /**
     * Creates the value for recurring windows of time. An entry or selection must lie fully inside one of them.
     *
     * @param windows the windows, at least one, for instance {@code BusinessHours.businessWeek().start(10).end(18)}
     * @return the value
     * @throws NullPointerException when null is passed or contained
     * @throws IllegalArgumentException when no window is passed
     */
    static ConstraintValue ofWindows(BusinessHours... windows) {
        return new Windows(List.of(windows));
    }

    /**
     * Creates the value for windows of time, recurring ones and fixed spans mixed. An entry or selection must lie fully
     * inside one of them.
     *
     * @param windows the windows, at least one, for instance {@code Window.span(start, end)}
     * @return the value
     * @throws NullPointerException when null is passed or contained
     * @throws IllegalArgumentException when no window is passed
     */
    static ConstraintValue ofWindows(Window... windows) {
        return new Windows(List.of(windows));
    }

    private static <T extends Comparable<? super T>> void checkBounds(T start, T end) {
        if (start == null && end == null) {
            throw new IllegalArgumentException("A span needs a start or an end");
        }
        if (start != null && end != null && start.compareTo(end) >= 0) {
            throw new IllegalArgumentException("The start must be before the end, was " + start + " and " + end);
        }
    }

    /**
     * Within the entries of a group: a dragged entry must lie fully inside at least one of them.
     *
     * @param groupId the group id
     */
    record GroupId(String groupId) implements ConstraintValue {

        public GroupId {
            Objects.requireNonNull(groupId);
            if (groupId.equals("businessHours")) {
                throw new IllegalArgumentException("The library reads this group id as the business hours. "
                        + "Use ConstraintValue.BUSINESS_HOURS.");
            }
        }
    }

    /** Within the business hours of the calendar. Use {@link #BUSINESS_HOURS}. */
    record CalendarBusinessHours() implements ConstraintValue {
    }

    /**
     * Within windows of time. The list is copied, so a later change to the passed list does not change the value.
     *
     * @param windows the windows, at least one
     */
    record Windows(List<Window> windows) implements ConstraintValue {

        public Windows {
            windows = List.copyOf(Objects.requireNonNull(windows));
            if (windows.isEmpty()) {
                throw new IllegalArgumentException("At least one window is needed");
            }
        }
    }

    /**
     * A window of time of a constraint: recurring {@link BusinessHours}, or a fixed span of dates or of dates and
     * times. The library reads each window like an entry. A {@code switch} over it needs no {@code default} branch.
     */
    sealed interface Window extends Serializable permits BusinessHours, DateSpan, DateTimeSpan {

        /**
         * Creates a fixed span of whole days.
         *
         * @param start the first day, or null for no limit before
         * @param end   the day after the span, or null for no limit after
         * @return the span
         * @throws IllegalArgumentException when both are null or the start is not before the end
         */
        static Window span(LocalDate start, LocalDate end) {
            return new DateSpan(start, end);
        }

        /**
         * Creates a fixed span with times. The times are read as UTC, like the start and end of entries (see
         * {@link Entry#setStart(LocalDateTime)}).
         *
         * @param start the start, or null for no limit before
         * @param end   the end, exclusive, or null for no limit after
         * @return the span
         * @throws IllegalArgumentException when both are null or the start is not before the end
         */
        static Window span(LocalDateTime start, LocalDateTime end) {
            return new DateTimeSpan(start, end);
        }
    }

    /**
     * A fixed span of whole days. One bound may be null for an open span.
     *
     * @param start the first day, or null for no limit before
     * @param end   the day after the span, or null for no limit after
     */
    record DateSpan(LocalDate start, LocalDate end) implements Window {

        public DateSpan {
            checkBounds(start, end);
        }
    }

    /**
     * A fixed span with times, read as UTC like the start and end of entries. One bound may be null for an open span.
     *
     * @param start the start, or null for no limit before
     * @param end   the end, exclusive, or null for no limit after
     */
    record DateTimeSpan(LocalDateTime start, LocalDateTime end) implements Window {

        public DateTimeSpan {
            checkBounds(start, end);
        }
    }
}

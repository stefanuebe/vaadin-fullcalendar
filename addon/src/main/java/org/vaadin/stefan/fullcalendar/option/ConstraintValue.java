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
import org.vaadin.stefan.fullcalendar.FullCalendarOptions;
import org.vaadin.stefan.fullcalendar.RRule;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * The value of an option that limits where entries can be dragged, resized or selected: the entries of a group, the
 * business hours of the calendar, windows of time (recurring business hours, windows defined by an {@link RRule}
 * and fixed spans), or resources. A {@code switch} over it needs no {@code default} branch.
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
     * @see Windows#withResources(String...)
     */
    static Windows ofWindows(BusinessHours... windows) {
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
     * @see Windows#withResources(String...)
     */
    static Windows ofWindows(Window... windows) {
        return new Windows(List.of(windows));
    }

    /**
     * Creates the value for resources: every resource of an entry or selection must be one of them, at any time. For
     * resources within windows of time, use {@link Windows#withResources(String...)}.
     * <p>
     * The ids take effect with the scheduler only. There, an entry or selection without a resource, for example in a
     * view without resources, is refused.
     *
     * @param resourceIds the ids of the resources, at least one
     * @return the value
     * @throws NullPointerException when null is passed or contained
     * @throws IllegalArgumentException when no id is passed
     */
    static ConstraintValue ofResources(String... resourceIds) {
        return new Resources(List.of(resourceIds));
    }

    private static List<String> checkResourceIds(List<String> resourceIds) {
        if (resourceIds.isEmpty()) {
            throw new IllegalArgumentException("At least one resource id is needed");
        }

        return resourceIds;
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
     * Within windows of time, optionally limited to resources. The lists are copied, so a later change to a passed
     * list does not change the value.
     *
     * @param windows     the windows, at least one
     * @param resourceIds the ids of the resources every window is limited to, empty for no limit
     */
    record Windows(List<Window> windows, List<String> resourceIds) implements ConstraintValue {

        public Windows {
            windows = List.copyOf(Objects.requireNonNull(windows));
            resourceIds = List.copyOf(Objects.requireNonNull(resourceIds));
            if (windows.isEmpty()) {
                throw new IllegalArgumentException("At least one window is needed");
            }
        }

        /**
         * Creates the value for windows of time, not limited to resources.
         *
         * @param windows the windows, at least one
         */
        public Windows(List<Window> windows) {
            this(windows, List.of());
        }

        /**
         * Returns a copy whose windows are limited to the given resources: an entry or selection must lie in one of
         * the windows, and every resource of it must be one of the given ones. The ids replace those set before.
         * <p>
         * The ids apply to every window. The library refuses a resource for the whole constraint as soon as one
         * window names other resources, so ids per window would only act as the ids all windows share.
         * <p>
         * The ids take effect with the scheduler only. There, an entry or selection without a resource, for example
         * in a view without resources, is refused.
         *
         * @param resourceIds the ids of the resources, at least one
         * @return the new value
         * @throws NullPointerException when null is passed or contained
         * @throws IllegalArgumentException when no id is passed
         */
        public Windows withResources(String... resourceIds) {
            return new Windows(windows, checkResourceIds(List.of(resourceIds)));
        }
    }

    /**
     * Within resources, at any time. The list is copied, so a later change to the passed list does not change the
     * value.
     *
     * @param resourceIds the ids of the resources, at least one
     */
    record Resources(List<String> resourceIds) implements ConstraintValue {

        public Resources {
            resourceIds = checkResourceIds(List.copyOf(Objects.requireNonNull(resourceIds)));
        }
    }

    /**
     * A window of time of a constraint: recurring {@link BusinessHours}, an {@link RRule} with an optional duration,
     * or a fixed span of dates or of dates and times. The library reads each window like an entry. A {@code switch}
     * over it needs no {@code default} branch.
     */
    sealed interface Window extends Serializable permits BusinessHours, RRuleWindow, DateSpan, DateTimeSpan {

        /**
         * Creates a window from an RRule, written like the recurrence of an entry, including its excluded dates and
         * rules. The rule needs its own {@code dtstart}, because no entry start is there to take it from. Without
         * one, the rrule library starts the rule at the current time. For a raw rule, put {@code DTSTART} into its
         * string. A rule without a time anywhere (start, end or excluded dates) gives whole-day windows, also when the
         * start defaults to the current time. A time in any of them gives timed windows.
         * Each window lasts the default entry duration of the calendar, see
         * {@link FullCalendarOptions#setDefaultTimedEntryDuration(Duration)} and
         * {@link FullCalendarOptions#setDefaultAllDayEntryDuration(Duration)}. For a timed window this is one hour
         * by default.
         * The rule is copied, so a later change to the passed rule does not change the window.
         *
         * @param rrule the rule
         * @return the window
         * @throws NullPointerException when null is passed
         */
        static Window ofRRule(RRule rrule) {
            return new RRuleWindow(rrule, null);
        }

        /**
         * Creates a window from an RRule and the duration of each occurrence, see {@link #ofRRule(RRule)}.
         * A null duration gives the default entry duration of the calendar.
         *
         * @param rrule    the rule
         * @param duration the duration of each occurrence, or null to leave it to the library
         * @return the window
         * @throws NullPointerException when the rule is null
         */
        static Window ofRRule(RRule rrule, Duration duration) {
            return new RRuleWindow(rrule, duration);
        }

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
     * A window defined by an {@link RRule}, with the duration of each occurrence. The rule is copied, so a later
     * change to the passed rule does not change the window.
     *
     * @param rrule    the rule with its excluded dates and rules
     * @param duration the duration of each occurrence, or null
     */
    record RRuleWindow(RRule rrule, Duration duration) implements Window {

        public RRuleWindow {
            rrule = Objects.requireNonNull(rrule).copy();
        }

        /**
         * Returns a copy of the rule, so a change to it does not change the window.
         *
         * @return a copy of the rule
         */
        @Override
        public RRule rrule() {
            return rrule.copy();
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

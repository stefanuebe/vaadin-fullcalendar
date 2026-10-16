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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The value of an option that limits where entries can be dragged, resized or selected: the entries of a group, the
 * business hours of the calendar, or windows of time. A {@code switch} over it needs no {@code default} branch.
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
     * Creates the value for windows of time. An entry or selection must lie fully inside one of them.
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
    record Windows(List<BusinessHours> windows) implements ConstraintValue {

        public Windows {
            windows = List.copyOf(Objects.requireNonNull(windows));
            if (windows.isEmpty()) {
                throw new IllegalArgumentException("At least one window is needed");
            }
        }
    }
}

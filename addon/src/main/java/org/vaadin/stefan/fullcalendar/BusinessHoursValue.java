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
package org.vaadin.stefan.fullcalendar;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The value of the business hours option: none, the library's default business hours (Monday to Friday, 9am to 5pm)
 * or own {@link BusinessHours}. A {@code switch} over it needs no {@code default} branch.
 * <p>
 * A sealed type rather than a list of {@link BusinessHours}, because the library takes a boolean as well as objects.
 */
public sealed interface BusinessHoursValue extends Serializable {

    /**
     * No business hours, the library's {@code false}. This is the library's default, so it is only needed to turn
     * calendar-wide business hours off for a single view.
     */
    BusinessHoursValue NONE = new None();

    /** The library's default business hours, Monday to Friday, 9am to 5pm. */
    BusinessHoursValue DEFAULT = new Default();

    /**
     * Creates the value for own business hours.
     *
     * @param hours the business hours, at least one
     * @return the value
     * @throws NullPointerException when null is passed or contained
     * @throws IllegalArgumentException when no hours are passed
     */
    static BusinessHoursValue of(BusinessHours... hours) {
        return new Hours(List.of(hours));
    }

    /** No business hours, the library's {@code false}. Use {@link #NONE}. */
    record None() implements BusinessHoursValue {
    }

    /** The library's default business hours, the library's {@code true}. Use {@link #DEFAULT}. */
    record Default() implements BusinessHoursValue {
    }

    /**
     * Own business hours. The list is copied, so a later change to the passed list does not change the value.
     *
     * @param hours the business hours, at least one
     */
    record Hours(List<BusinessHours> hours) implements BusinessHoursValue {

        public Hours {
            hours = List.copyOf(Objects.requireNonNull(hours));
            if (hours.isEmpty()) {
                throw new IllegalArgumentException("At least one business hours entry is needed");
            }
        }
    }
}

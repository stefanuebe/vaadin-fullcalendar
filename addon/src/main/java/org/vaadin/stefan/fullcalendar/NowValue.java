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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The value of the option that sets the current date of the calendar: a date, a date and time, or a {@link JsCallback}
 * that returns a date. A {@code switch} over it needs no {@code default} branch.
 * <p>
 * A sealed type rather than one Java type, because the library takes a date, a date and time, or a function.
 * <p>
 * The library also takes a date string, a {@code Date} or milliseconds. The typed forms replace them: the converter
 * writes the date strings the library reads.
 */
public sealed interface NowValue extends Serializable permits NowValue.Day, NowValue.DateTime, JsCallback {

    /**
     * Creates the value for a date.
     *
     * @param date the date
     * @return the value
     * @throws NullPointerException when null is passed
     */
    static NowValue ofDay(LocalDate date) {
        return new Day(date);
    }

    /**
     * Creates the value for a date and time. It is read as UTC, like the start and end of entries (see
     * {@link Entry#setStart(LocalDateTime)}), and the calendar shows it in its time zone. Convert a wall time of the
     * calendar's zone to UTC first.
     *
     * @param dateTime the date and time
     * @return the value
     * @throws NullPointerException when null is passed
     */
    static NowValue ofDateTime(LocalDateTime dateTime) {
        return new DateTime(dateTime);
    }

    /**
     * A date without time.
     *
     * @param date the date
     */
    record Day(LocalDate date) implements NowValue {

        public Day {
            Objects.requireNonNull(date);
        }
    }

    /**
     * A date and time, read as UTC like the start and end of entries.
     *
     * @param dateTime the date and time
     */
    record DateTime(LocalDateTime dateTime) implements NowValue {

        public DateTime {
            Objects.requireNonNull(dateTime);
        }
    }
}

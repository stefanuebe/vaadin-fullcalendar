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

import org.vaadin.stefan.fullcalendar.FullCalendarViewOptions;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A range of dates, for example the value of {@link FullCalendarViewOptions#getValidRange() validRange}. Either bound
 * may be {@code null} for an open-ended range. {@link #withOpenEnd(LocalDate)} and
 * {@link #withOpenStart(LocalDate)} create such a range without passing {@code null}. The end is exclusive, as in FullCalendar.
 * <pre>{@code
 * // nothing before March 2025
 * calendar.options().setValidRange(DateRange.withOpenEnd(LocalDate.of(2025, 3, 1)));
 * }</pre>
 *
 * @param start first date of the range, or {@code null} for an open start
 * @param end   date after the range, or {@code null} for an open end
 */
public record DateRange(LocalDate start, LocalDate end) implements DateRangeValue {

    /**
     * Creates a new range.
     *
     * @throws IllegalArgumentException if both bounds are set and {@code start} is not before {@code end}
     */
    public DateRange {
        if (start != null && end != null && !start.isBefore(end)) {
            throw new IllegalArgumentException("Start must be before end");
        }
    }

    /**
     * Creates a range from the given date on, with no end.
     *
     * @param start first date of the range
     * @return the range
     * @throws NullPointerException when null is passed
     */
    public static DateRange withOpenEnd(LocalDate start) {
        return new DateRange(Objects.requireNonNull(start), null);
    }

    /**
     * Creates a range of all dates before the given date, with no start.
     *
     * @param end date after the range, exclusive
     * @return the range
     * @throws NullPointerException when null is passed
     */
    public static DateRange withOpenStart(LocalDate end) {
        return new DateRange(null, Objects.requireNonNull(end));
    }
}

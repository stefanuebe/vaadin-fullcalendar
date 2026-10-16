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
import java.time.Duration;
import java.time.Period;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The value of an option that takes a duration, such as {@code slotDuration} or {@code dateIncrement}: a time-based
 * duration, or a calendar-based one in years, months and days, optionally with a time on top. A {@code switch} over it
 * needs no {@code default} branch.
 * <p>
 * A sealed type rather than one Java type, because {@link Duration} has no months or years, and the library counts
 * days apart from hours: {@code {days: 1}} is one calendar day, {@code "24:00:00"} is 24 hours.
 * <p>
 * The library also takes a number of milliseconds and the strings {@link #ofTimeString(String)} reads. They are the
 * same durations as the two forms, written another way, so they have no form of their own.
 */
public sealed interface DurationValue extends Serializable {

    /**
     * Creates a time-based duration, written as {@code "hh:mm:ss"}. Anything below a millisecond is dropped on the
     * way to the client. Days count as 24 hours each; for calendar days, use {@link #ofCalendar(Period)}.
     *
     * @param time the duration
     * @return the value
     * @throws NullPointerException when null is passed
     */
    static DurationValue ofTime(Duration time) {
        return new Time(time);
    }

    /**
     * Creates a calendar-based duration, such as one month for {@code dateIncrement}.
     *
     * @param period the years, months and days, not zero
     * @return the value
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when the period is zero, use {@link #ofTime(Duration)}
     */
    static DurationValue ofCalendar(Period period) {
        return new Calendar(period, Duration.ZERO);
    }

    /**
     * Creates a calendar-based duration with a time on top, such as one day and 12 hours.
     *
     * @param period the years, months and days, not zero
     * @param time   the time on top of the period
     * @return the value
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when the period is zero, use {@link #ofTime(Duration)}
     */
    static DurationValue ofCalendar(Period period, Duration time) {
        return new Calendar(period, time);
    }

    /**
     * Reads a duration in the library's string format {@code [-][d.]hh:mm[:ss[.sss]]}, such as {@code "08:30"} or
     * {@code "1.12:00:00"}. Days in front give a calendar-based duration, otherwise it is time-based. A leading minus
     * applies to the whole duration. Like the library, it reads a match at the start of the string and ignores the
     * rest.
     *
     * @param text the duration string
     * @return the value
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when the string does not start with a duration in that format, which the
     *                                  library would ignore
     */
    static DurationValue ofTimeString(String text) {
        Matcher m = Time.CLIENT_FORMAT.matcher(Objects.requireNonNull(text));
        if (!m.lookingAt()) {
            throw new IllegalArgumentException("Not a duration string of the form [-][d.]hh:mm[:ss[.sss]]: " + text);
        }
        int sign = m.group(1).isEmpty() ? 1 : -1;
        Duration time = Duration.ofHours(Long.parseLong(m.group(3)))
                .plusMinutes(Long.parseLong(m.group(4)))
                .plusSeconds(m.group(5) != null ? Long.parseLong(m.group(5)) : 0)
                .plusMillis(m.group(6) != null ? Long.parseLong(m.group(6)) : 0)
                .multipliedBy(sign);
        int days = m.group(2) != null ? Integer.parseInt(m.group(2)) * sign : 0;
        return days == 0 ? ofTime(time) : ofCalendar(Period.ofDays(days), time);
    }

    /**
     * A time-based duration.
     *
     * @param time the duration
     */
    record Time(Duration time) implements DurationValue {

        // FC-UPDATE: check this pattern against the client's duration parsing, especially on a major update. If the
        // client format changes, duration strings are read wrongly or rejected, also those read back from the client,
        // such as on an external drop.
        /** The library's duration string format, matched from the start like the library does. */
        private static final Pattern CLIENT_FORMAT =
                Pattern.compile("(-?)(?:(\\d+)\\.)?(\\d+):(\\d\\d)(?::(\\d\\d)(?:\\.(\\d\\d\\d))?)?");

        public Time {
            Objects.requireNonNull(time);
        }
    }

    /**
     * A calendar-based duration, with a time on top.
     *
     * @param period the years, months and days, not zero
     * @param time   the time on top of the period, {@link Duration#ZERO} for none
     */
    record Calendar(Period period, Duration time) implements DurationValue {

        public Calendar {
            Objects.requireNonNull(period);
            Objects.requireNonNull(time);
            if (period.isZero()) {
                throw new IllegalArgumentException("Use DurationValue.ofTime for a duration without years, months "
                        + "and days");
            }
        }
    }
}

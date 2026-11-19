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
 * A duration as the library reads it, such as the value of {@code slotDuration} or {@code dateIncrement}: years,
 * months, weeks, days, hours, minutes, seconds and milliseconds, each kept on its own and sent only when not zero.
 * <p>
 * Neither {@link Duration} nor {@link Period} covers what the library reads. A {@code Duration} has no months or
 * years, and the library counts days apart from hours: one day is one calendar day, 24 hours are 24 hours. A
 * {@code Period} has no weeks: {@code Period.ofWeeks(1)} is seven days. The library aligns a duration given in weeks to
 * the start of the week, for example the range of a view with a {@code duration} of one week, and a duration of seven
 * days not.
 * <p>
 * For example:
 * <pre>{@code
 * calendar.options().view(customView).setDuration(CalendarDuration.ofWeeks(2));
 * calendar.options().setSlotDuration(CalendarDuration.ofHours(1).plusMinutes(30));
 * }</pre>
 *
 * @param years        the years
 * @param months       the months
 * @param weeks        the weeks
 * @param days         the days
 * @param hours        the hours
 * @param minutes      the minutes
 * @param seconds      the seconds
 * @param milliseconds the milliseconds
 */
public record CalendarDuration(int years, int months, int weeks, int days, int hours, int minutes, int seconds,
                               int milliseconds) implements Serializable {

    /** A duration of zero. */
    public static final CalendarDuration ZERO = new CalendarDuration(0, 0, 0, 0, 0, 0, 0, 0);

    // FC-UPDATE: check this pattern against the client's duration parsing, especially on a major update. If the
    // client format changes, duration strings are read wrongly or rejected, also those read back from the client,
    // such as on an external drop.
    /** The library's duration string format, matched from the start like the library does. */
    private static final Pattern CLIENT_FORMAT =
            Pattern.compile("(-?)(?:(\\d+)\\.)?(\\d+):(\\d\\d)(?::(\\d\\d)(?:\\.(\\d\\d\\d))?)?");

    /**
     * Creates a duration in years.
     *
     * @param years the years
     * @return the duration
     */
    public static CalendarDuration ofYears(int years) {
        return ZERO.plusYears(years);
    }

    /**
     * Creates a duration in months.
     *
     * @param months the months
     * @return the duration
     */
    public static CalendarDuration ofMonths(int months) {
        return ZERO.plusMonths(months);
    }

    /**
     * Creates a duration in weeks. The library aligns it to the start of the week where it aligns a range, for
     * example for {@code duration} and {@code dateIncrement}.
     *
     * @param weeks the weeks
     * @return the duration
     */
    public static CalendarDuration ofWeeks(int weeks) {
        return ZERO.plusWeeks(weeks);
    }

    /**
     * Creates a duration in calendar days.
     *
     * @param days the days
     * @return the duration
     */
    public static CalendarDuration ofDays(int days) {
        return ZERO.plusDays(days);
    }

    /**
     * Creates a duration in hours.
     *
     * @param hours the hours
     * @return the duration
     */
    public static CalendarDuration ofHours(int hours) {
        return ZERO.plusHours(hours);
    }

    /**
     * Creates a duration in minutes.
     *
     * @param minutes the minutes
     * @return the duration
     */
    public static CalendarDuration ofMinutes(int minutes) {
        return ZERO.plusMinutes(minutes);
    }

    /**
     * Creates a duration in seconds.
     *
     * @param seconds the seconds
     * @return the duration
     */
    public static CalendarDuration ofSeconds(int seconds) {
        return ZERO.plusSeconds(seconds);
    }

    /**
     * Creates a duration in milliseconds.
     *
     * @param milliseconds the milliseconds
     * @return the duration
     */
    public static CalendarDuration ofMillis(int milliseconds) {
        return ZERO.plusMillis(milliseconds);
    }

    /**
     * Creates a duration of the hours, minutes, seconds and milliseconds of the given duration. Days count as 24
     * hours each. For calendar days, use {@link #ofDays(int)}. Anything below a millisecond is dropped.
     *
     * @param duration the duration
     * @return the duration
     * @throws NullPointerException when null is passed
     * @throws ArithmeticException when the hours do not fit into an {@code int}
     */
    public static CalendarDuration of(Duration duration) {
        long millis = duration.toMillis();

        return new CalendarDuration(0, 0, 0, 0, Math.toIntExact(millis / 3_600_000), (int) (millis / 60_000 % 60),
                (int) (millis / 1000 % 60), (int) (millis % 1000));
    }

    /**
     * Creates a duration of the years, months and days of the given period. A {@link Period} has no weeks, so
     * {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the alignment to the start of the week. For
     * weeks, use {@link #ofWeeks(int)}.
     *
     * @param period the period
     * @return the duration
     * @throws NullPointerException when null is passed
     */
    public static CalendarDuration of(Period period) {
        return new CalendarDuration(period.getYears(), period.getMonths(), 0, period.getDays(), 0, 0, 0, 0);
    }

    /**
     * Reads a duration in the library's string format {@code [-][d.]hh:mm[:ss[.sss]]}, such as {@code "08:30"} or
     * {@code "1.12:00:00"}. Days in front are calendar days. A leading minus applies to the whole duration. Like the
     * library, it reads a match at the start of the string and ignores the rest.
     *
     * @param text the duration string
     * @return the duration
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when the string does not start with a duration in that format, which the
     *                                  library would ignore
     */
    public static CalendarDuration ofTimeString(String text) {
        Matcher matcher = CLIENT_FORMAT.matcher(Objects.requireNonNull(text));
        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException("Not a duration string of the form [-][d.]hh:mm[:ss[.sss]]: " + text);
        }

        int sign = matcher.group(1).isEmpty() ? 1 : -1;

        return new CalendarDuration(0, 0, 0, matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) * sign : 0,
                Integer.parseInt(matcher.group(3)) * sign, Integer.parseInt(matcher.group(4)) * sign,
                matcher.group(5) != null ? Integer.parseInt(matcher.group(5)) * sign : 0,
                matcher.group(6) != null ? Integer.parseInt(matcher.group(6)) * sign : 0);
    }

    /**
     * Returns a copy with the given years added.
     *
     * @param years the years to add
     * @return the new duration
     */
    public CalendarDuration plusYears(int years) {
        return new CalendarDuration(Math.addExact(this.years, years), months, weeks, days, hours, minutes, seconds,
                milliseconds);
    }

    /**
     * Returns a copy with the given months added.
     *
     * @param months the months to add
     * @return the new duration
     */
    public CalendarDuration plusMonths(int months) {
        return new CalendarDuration(years, Math.addExact(this.months, months), weeks, days, hours, minutes, seconds,
                milliseconds);
    }

    /**
     * Returns a copy with the given weeks added.
     *
     * @param weeks the weeks to add
     * @return the new duration
     */
    public CalendarDuration plusWeeks(int weeks) {
        return new CalendarDuration(years, months, Math.addExact(this.weeks, weeks), days, hours, minutes, seconds,
                milliseconds);
    }

    /**
     * Returns a copy with the given calendar days added.
     *
     * @param days the days to add
     * @return the new duration
     */
    public CalendarDuration plusDays(int days) {
        return new CalendarDuration(years, months, weeks, Math.addExact(this.days, days), hours, minutes, seconds,
                milliseconds);
    }

    /**
     * Returns a copy with the given hours added.
     *
     * @param hours the hours to add
     * @return the new duration
     */
    public CalendarDuration plusHours(int hours) {
        return new CalendarDuration(years, months, weeks, days, Math.addExact(this.hours, hours), minutes, seconds,
                milliseconds);
    }

    /**
     * Returns a copy with the given minutes added.
     *
     * @param minutes the minutes to add
     * @return the new duration
     */
    public CalendarDuration plusMinutes(int minutes) {
        return new CalendarDuration(years, months, weeks, days, hours, Math.addExact(this.minutes, minutes), seconds,
                milliseconds);
    }

    /**
     * Returns a copy with the given seconds added.
     *
     * @param seconds the seconds to add
     * @return the new duration
     */
    public CalendarDuration plusSeconds(int seconds) {
        return new CalendarDuration(years, months, weeks, days, hours, minutes, Math.addExact(this.seconds, seconds),
                milliseconds);
    }

    /**
     * Returns a copy with the given milliseconds added.
     *
     * @param milliseconds the milliseconds to add
     * @return the new duration
     */
    public CalendarDuration plusMillis(int milliseconds) {
        return new CalendarDuration(years, months, weeks, days, hours, minutes, seconds,
                Math.addExact(this.milliseconds, milliseconds));
    }
}

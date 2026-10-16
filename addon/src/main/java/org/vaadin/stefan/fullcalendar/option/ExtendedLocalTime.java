package org.vaadin.stefan.fullcalendar.option;

import lombok.Getter;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A time of day without a time zone, like {@link LocalTime}, that may run past midnight: 26:00 is 2am of the next
 * day. The library reads such times as the time since the start of the day, so a recurring entry or a business hours
 * window can end after midnight. Hours and minutes only.
 */
@Getter
public final class ExtendedLocalTime implements Serializable {
    private final int hour;
    private final int minute;

    /**
     * New instance. Both parameters must not be negative.
     * Passing 60 minutes or more will automatically increase the given hours value.
     *
     * @param hour   hours
     * @param minute minutes
     */
    private ExtendedLocalTime(int hour, int minute) {
        if (hour < 0) {
            throw new IllegalArgumentException("Hours must not be negative");
        }
        if (minute < 0) {
            throw new IllegalArgumentException("Minutes must not be negative");
        }

        this.hour = hour + (minute / 60);
        this.minute = minute % 60;
    }

    /**
     * Creates a new instance based on the given integers. Both parameters must not be negative.
     * Passing 60 minutes or more will automatically increase the given hours value.
     * @param hours hours
     * @param minutes minutes (values 60+ will be merged into hours)
     * @return new instance
     * @throws IllegalArgumentException when negative values are passed
     */
    public static ExtendedLocalTime of(int hours, int minutes) {
        return new ExtendedLocalTime(hours, minutes);
    }

    /**
     * Creates a new instance based on the given integer. The parameter must not be negative.
     * @param hours hours
     * @return new instance
     * @throws IllegalArgumentException when negative values are passed
     */
    public static ExtendedLocalTime of(int hours) {
        return new ExtendedLocalTime(hours, 0);
    }

    /**
     * Creates an instance based on the given local time. Seconds and nanos are dropped.
     * @param time local time
     * @return new instance
     */
    public static ExtendedLocalTime of(LocalTime time) {
        return of(time.getHour(), time.getMinute());
    }

    /**
     * Creates a new instance of the given string. The string must follow the same rules as the integer based
     * factory methods. Expects hours and minutes in "HH:mm" format. Minutes can be left out (e.g. "1" for 1 hour),
     * leading zeros are optional. A seconds part is ignored. Passing 60 minutes or more will automatically increase the given hours value.
     * @param string hours:minutes string
     * @return new instance
     * @throws NumberFormatException when the string cannot be parsed to integer(s)
     * @throws IllegalArgumentException when a blank string or negative values are passed
     */
    public static ExtendedLocalTime of(String string) {
        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException("String must not be blank");
        }

        String[] split = string.split(":");

        // add additional exception handling, if necessary
        int hours = Integer.parseInt(split[0].trim());
        if (split.length == 1) {
            return of(hours);
        }

        return of(hours, Integer.parseInt(split[1].trim()));
    }

    /**
     * Converts this time to a local time. Be careful, as LocalTime does not support times of 24h or
     * above and thus such an instance will lead to an exception.
     *
     * @return LocalTime instance
     * @throws DateTimeException if this instance represents a time of 24 hours or above.
     */
    public LocalTime toLocalTime() {
        return LocalTime.of(hour, minute);
    }

    /**
     * Converts this instance to a {@link Duration} instance.
     *
     * @return duration instance
     */
    public Duration toDuration() {
        return Duration.ofHours(hour).plusMinutes(minute);
    }

    /**
     * Returns this instance as a formatted string. The pattern is always "HH:mm".
     *
     * @return formatted string
     */
    public String toFormattedString() {
        return (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;
    }

    /**
     * Returns a new instance with the given hours added on top. Negative values are allowed, but may lead
     * to an exception, if the resulting amount of hours is negative.
     *
     * @param hours hours
     * @return new instance with changed hours
     */
    public ExtendedLocalTime plusHours(int hours) {
        int totalMinutes = this.toTotalMinutes();
        totalMinutes += hours * 60;
        int[] times = fromTotalMinutes(totalMinutes);

        return new ExtendedLocalTime(times[0], times[1]);
    }

    /**
     * Returns a new instance with the given minutes added on top. Negative values are allowed, but may lead
     * to an exception, if the resulting amount of minutes is negative. If the resulting amount of minutes
     * is 60 or greater, it will also increase the hours.
     *
     * @param minutes minutes
     * @return new instance with changed minutes
     */
    public ExtendedLocalTime plusMinutes(int minutes) {
        int totalMinutes = this.toTotalMinutes();
        totalMinutes += minutes;
        int[] times = fromTotalMinutes(totalMinutes);

        return new ExtendedLocalTime(times[0], times[1]);
    }

    /**
     * Returns whether this time can be converted to a {@link LocalTime}, so it is before 24:00.
     *
     * @return true if it is before 24:00
     */
    public boolean isValidLocalTime() {
        return hour < 24;
    }

    /**
     * Returns whether this time equals the given local time.
     *
     * @param localTime local time to compare with
     * @return true if both are the same time of day
     */
    public boolean equals(LocalTime localTime) {
        return equals(ExtendedLocalTime.of(localTime));
    }

    /**
     * Returns whether this time is after the given one.
     *
     * @param other time to compare with
     * @return true if this time is later
     */
    public boolean isAfter(ExtendedLocalTime other) {
        return toTotalMinutes() > other.toTotalMinutes();
    }

    /**
     * Returns whether this time is before the given one.
     *
     * @param other time to compare with
     * @return true if this time is earlier
     */
    public boolean isBefore(ExtendedLocalTime other) {
        return toTotalMinutes() < other.toTotalMinutes();
    }

    /**
     * Returns whether this time is after the given local time.
     *
     * @param other local time to compare with
     * @return true if this time is later
     */
    public boolean isAfter(LocalTime other) {
        return isAfter(ExtendedLocalTime.of(other));
    }

    /**
     * Returns whether this time is before the given local time.
     *
     * @param other local time to compare with
     * @return true if this time is earlier
     */
    public boolean isBefore(LocalTime other) {
        return isBefore(ExtendedLocalTime.of(other));
    }

    private int toTotalMinutes() {
        return hour * 60 + minute;
    }

    private int[] fromTotalMinutes(int totalMinutes) {
        return new int[] {totalMinutes / 60, totalMinutes % 60};
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedLocalTime that = (ExtendedLocalTime) o;
        return hour == that.hour && minute == that.minute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, minute);
    }
}


package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonObject;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Delta {

    private final int years;
    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;

    public Delta(int years, int months, int days, int hours, int minutes, int seconds) {
        assertLessThan("months", months, 12);
        assertLessThan("days", days, 31);
        assertLessThan("hours", hours, 24);
        assertLessThan("minutes", minutes, 60);
        assertLessThan("seconds", seconds, 60);

        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    static void assertLessThan(String name, int current, int lessThanThis) {
        if(current >= lessThanThis) {
            throw new IllegalArgumentException("Value '" + name + "' must be less than '" + lessThanThis + "' but was '" + current + "'!");
        }
    }

    public static Delta fromJson(JsonObject jsonObject) {
        int years = toInt(jsonObject, "years");
        int months = toInt(jsonObject, "months");
        int days = toInt(jsonObject, "days");
        int hours = toInt(jsonObject, "hours");
        int minutes = toInt(jsonObject, "minutes");
        int seconds = toInt(jsonObject, "seconds");
        return new Delta(years, months, days, hours, minutes, seconds);
    }

    private static int toInt(JsonObject delta, String key) {
        return (int) delta.getNumber(key);
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    /**
     * Applies this delta instance on the given local date time by adding all day and time related delta values.
     * @param dateTime date time to modify
     * @return modified date time instance
     */
    public LocalDateTime applyOn(LocalDateTime dateTime) {
        return dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    /**
     * Applies this delta instance on the given local date by adding all day related delta values. Time values are ignored.
     * @param date date time to modify
     * @return modified date instance
     */
    public LocalDate applyOn(LocalDate date) {
        return date.plusYears(years).plusMonths(months).plusDays(days);
    }

    /**
     * Applies this delta instance on the given instant by adding all day and time related delta values. Internally
     * uses a local date time, that the instant will be converted to (utc based).
     * @param instant instanceto modify
     * @return updated instance
     */
    public Instant applyOn(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        return applyOn(localDateTime).toInstant(ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return "Delta{" +
                "years=" + years +
                ", months=" + months +
                ", days=" + days +
                ", hours=" + hours +
                ", minutes=" + minutes +
                ", seconds=" + seconds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Delta delta = (Delta) o;
        return years == delta.years &&
                months == delta.months &&
                days == delta.days &&
                hours == delta.hours &&
                minutes == delta.minutes &&
                seconds == delta.seconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(years, months, days, hours, minutes, seconds);
    }
}

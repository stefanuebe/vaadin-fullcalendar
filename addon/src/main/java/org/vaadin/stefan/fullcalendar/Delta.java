/*
 * Copyright 2020, Stefan Uebe
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

import elemental.json.JsonObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a delta between two times. A delta can contain negative values if the first date is later then the second one.
 */
@Getter
@ToString
@EqualsAndHashCode
public class Delta {

    /**
     * The delta's years part.
     */
    private final int years;
    /**
     * The delta's months part.
     */
    private final int months;
    /**
     * The delta's days part.
     */
    private final int days;
    /**
     * The delta's hours part.
     */
    private final int hours;
    /**
     * The delta's minutes part.
     */
    private final int minutes;
    /**
     * The delta's seconds part.
     */
    private final int seconds;

    /**
     * Creates a new instance.
     * @param years years delta
     * @param months months delta
     * @param days days delta
     * @param hours hours delta
     * @param minutes minutes delta
     * @param seconds seconds delta
     */
    @Builder
    public Delta(int years, int months, int days, int hours, int minutes, int seconds) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    static void assertLessThan(String name, int current, int lessThanThis) {
        if (current >= lessThanThis) {
            throw new IllegalArgumentException("Value'" + name + "' must be less than or equal to '" + lessThanThis + "' (as absolute) but was '" + current + "'!");
        }
    }

    /**
     * Parses the given json object.
     * @param jsonObject json object
     * @return delta
     */
    public static Delta fromJson(JsonObject jsonObject) {
        int years = toInt(jsonObject, "years");
        int months = toInt(jsonObject, "months");
        int days = toInt(jsonObject, "days");

        // new 4.x way
        if (jsonObject.hasKey("milliseconds")) {
            long remainingMS = (long) jsonObject.getNumber("milliseconds");
            int hours = (int) TimeUnit.MILLISECONDS.toHours(remainingMS);
            remainingMS -= TimeUnit.HOURS.toMillis(hours);
            int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(remainingMS);
            remainingMS -= TimeUnit.MINUTES.toMillis(minutes);
            int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(remainingMS);

            return new Delta(years, months, days, hours, minutes, seconds);
        }

        // old 3.9 way
        int hours = toInt(jsonObject, "hours");
        int minutes = toInt(jsonObject, "minutes");
        int seconds = toInt(jsonObject, "seconds");
        return new Delta(years, months, days, hours, minutes, seconds);
    }

    public static Delta fromLocalDates(LocalDateTime deltaFrom, LocalDateTime deltaTo) {
        return new Delta(
                deltaTo.getYear() - deltaFrom.getYear(),
                deltaTo.getMonthValue() - deltaFrom.getMonthValue(),
                deltaTo.getDayOfMonth() - deltaFrom.getDayOfMonth(),
                deltaTo.getHour() - deltaFrom.getHour(),
                deltaTo.getMinute() - deltaFrom.getMinute(),
                deltaTo.getSecond()- deltaFrom.getSecond()
                );
    }

    private static int toInt(JsonObject delta, String key) {
        return (int) delta.getNumber(key);
    }

    /**
     * Applies this delta instance on the given local date time by adding all day and time related delta values.
     *
     * @param dateTime date time to modify
     * @return modified date time instance
     * @throws NullPointerException when null is passed
     */
    public LocalDateTime applyOn(@NotNull LocalDateTime dateTime) {
        return dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    /**
     * Applies this delta instance on the given local date by adding all day related delta values. Time values are ignored.
     *
     * @param date date time to modify
     * @return modified date instance
     * @throws NullPointerException when null is passed
     */
    public LocalDate applyOn(@NotNull LocalDate date) {
        return date.plusYears(years).plusMonths(months).plusDays(days);
    }

    /**
     * Applies this delta instance on the given instant by adding all day and time related delta values.
     *
     * @param instant instance to modify
     * @return updated instance
     * @throws NullPointerException when null is passed
     */
    public Instant applyOn(@NotNull Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        return applyOn(localDateTime).toInstant(ZoneOffset.UTC);
    }

}

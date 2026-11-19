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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import tools.jackson.databind.node.ObjectNode;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Represents a delta between two times, as days and time. A delta can contain negative values if the first date is
 * later than the second one.
 * <p>
 * A delta has no years or months part. In practice FullCalendar reports the date part of a drop or resize delta in
 * days, for example {@code days: 31} for an entry dragged by one month. A span of months also has no fixed length in
 * days.
 */
@Getter
@ToString
@EqualsAndHashCode
public class Delta {

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
     * @param days days delta
     * @param hours hours delta
     * @param minutes minutes delta
     * @param seconds seconds delta
     */
    @Builder
    public Delta(int days, int hours, int minutes, int seconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     * Parses the given json object. A {@code years} or {@code months} value in it is ignored, because in practice
     * FullCalendar sends both as zero.
     * @param jsonObject json object
     * @return delta
     */
    public static Delta fromJson(ObjectNode jsonObject) {
        int days = jsonObject.get("days").asInt();

        // new 4.x way
        if (jsonObject.hasNonNull("milliseconds")) {
            long remainingMS = (long) jsonObject.get("milliseconds").asLong();
            int hours = (int) TimeUnit.MILLISECONDS.toHours(remainingMS);
            remainingMS -= TimeUnit.HOURS.toMillis(hours);
            int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(remainingMS);
            remainingMS -= TimeUnit.MINUTES.toMillis(minutes);
            int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(remainingMS);

            return new Delta(days, hours, minutes, seconds);
        }

        // old 3.9 way
        int hours = jsonObject.get("hours").asInt();
        int minutes = jsonObject.get("minutes").asInt();
        int seconds = jsonObject.get("seconds").asInt();
        return new Delta(days, hours, minutes, seconds);
    }

    /**
     * Creates the delta that moves {@code deltaFrom} to {@code deltaTo}, so that {@code applyOn(deltaFrom)} returns
     * {@code deltaTo}. The span is truncated to whole seconds, so this holds exactly only when both date times have the
     * same fraction of a second.
     *
     * @param deltaFrom date time the delta starts at
     * @param deltaTo date time the delta ends at
     * @return delta between both date times
     * @throws NullPointerException when null is passed
     * @throws ArithmeticException when the span has more days than an int holds
     */
    public static Delta fromLocalDates(LocalDateTime deltaFrom, LocalDateTime deltaTo) {
        Duration span = Duration.ofSeconds(ChronoUnit.SECONDS.between(deltaFrom, deltaTo));
        return new Delta(Math.toIntExact(span.toDays()), span.toHoursPart(), span.toMinutesPart(),
                span.toSecondsPart());
    }

    /**
     * Applies this delta instance on the given local date time by adding all day and time related delta values.
     *
     * @param dateTime date time to modify
     * @return modified date time instance
     * @throws NullPointerException when null is passed
     */
    public LocalDateTime applyOn(LocalDateTime dateTime) {
        return dateTime.plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    /**
     * Inverse of {@link #applyOn(LocalDateTime)}: returns the given date-time with all components of
     * this delta subtracted. Useful for reconstructing the "before" value from a received delta +
     * "after" value (e.g. in external drop/resize events, where only the new position is known on
     * the server side).
     *
     * @param dateTime date time to modify
     * @return date time with this delta subtracted
     * @throws NullPointerException when null is passed
     * @since 7.2.0
     */
    public LocalDateTime subtractFrom(LocalDateTime dateTime) {
        return dateTime.minusDays(days).minusHours(hours).minusMinutes(minutes).minusSeconds(seconds);
    }

    /**
     * Inverse of {@link #applyOn(LocalDate)}: returns the given date with all day-related components
     * of this delta subtracted. Time components are ignored.
     *
     * @param date date to modify
     * @return date with this delta subtracted
     * @throws NullPointerException when null is passed
     * @since 7.2.0
     */
    public LocalDate subtractFrom(LocalDate date) {
        return date.minusDays(days);
    }

    /**
     * Inverse of {@link #applyOn(Instant)}: returns the given instant with all components of this
     * delta subtracted.
     *
     * @param instant instant to modify
     * @return instant with this delta subtracted
     * @throws NullPointerException when null is passed
     * @since 7.2.0
     */
    public Instant subtractFrom(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        return subtractFrom(localDateTime).toInstant(ZoneOffset.UTC);
    }

    /**
     * Applies this delta instance on the given local date by adding all day related delta values. Time values are ignored.
     *
     * @param date date time to modify
     * @return modified date instance
     * @throws NullPointerException when null is passed
     */
    public LocalDate applyOn(LocalDate date) {
        return date.plusDays(days);
    }

    /**
     * Applies this delta instance on the given instant by adding all day and time related delta values.
     *
     * @param instant instance to modify
     * @return updated instance
     * @throws NullPointerException when null is passed
     */
    public Instant applyOn(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        return applyOn(localDateTime).toInstant(ZoneOffset.UTC);
    }

}

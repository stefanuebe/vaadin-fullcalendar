package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Delta {

    private final int years;
    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;

    public Delta(int years, int months, int days, int hours, int minutes, int seconds) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
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

    public LocalDateTime applyOn(LocalDateTime dateTime) {
        return dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    public LocalDate applyOn(LocalDate date) {
        return date.plusYears(years).plusMonths(months).plusDays(days);
    }

    /**
     * Applies this delta on the given date time object and converts it to a local date <b>after that</b>. This means, that
     * the applied time changes are cut off, but a day switch might has happen anyways.
     *
     * @param dateTime local date time
     * @return local date
     */
    public LocalDate applyOnAndConvert(LocalDateTime dateTime) {
        return dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds).toLocalDate();
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
}

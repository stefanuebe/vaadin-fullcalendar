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

import lombok.Getter;
import lombok.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a recurrence rule (RRULE) for a calendar entry, compatible with the {@code @fullcalendar/rrule}
 * plugin. Used to define rich recurrence patterns beyond the simple built-in recurrence fields
 * ({@code recurringDaysOfWeek}, {@code recurringStartDate}, etc.).
 * <p>
 * Two modes of use:
 * <ol>
 *   <li><b>Structured form</b> (recommended): Set {@link #freq} and optional fields like
 *       {@link #byweekday}, {@link #until}, {@link #count}, etc. Serializes to a JSON object.</li>
 *   <li><b>Raw RRULE string</b>: Set a raw iCalendar RRULE string via {@link #ofRaw(String)}.
 *       Serializes as a string. Useful for importing recurrence rules from external sources.</li>
 * </ol>
 * <p>
 * The two modes are mutually exclusive. If a raw RRULE string is set, it takes precedence.
 * <p>
 * <b>Important:</b> RRule-based recurrence and FC's built-in recurrence ({@code daysOfWeek} etc.) are
 * <em>mutually exclusive</em> on a per-entry basis. Do not set both on the same entry.
 * <p>
 * Requires the {@code @fullcalendar/rrule} npm package and plugin to be loaded.
 *
 * @see <a href="https://fullcalendar.io/docs/rrule-plugin">FC rrule plugin documentation</a>
 */
@Getter
public class RRule {

    /**
     * The recurrence frequency. Maps to FC's {@code freq} property.
     */
    public enum Frequency implements ClientSideValue {
        YEARLY("yearly"),
        MONTHLY("monthly"),
        WEEKLY("weekly"),
        DAILY("daily");

        private final String clientSideValue;

        Frequency(String clientSideValue) {
            this.clientSideValue = clientSideValue;
        }

        @Override
        public String getClientSideValue() {
            return clientSideValue;
        }
    }

    /**
     * Recurrence frequency (required for structured form).
     * If null, FullCalendar may not render the recurrence correctly.
     */
    private Frequency freq;

    /**
     * The start date/time of the recurrence. ISO 8601 datetime string.
     * If not set, FC uses the event's {@code start} date.
     */
    private String dtstart;

    /**
     * The end date of the recurrence (exclusive with {@link #count}).
     * ISO 8601 date or datetime string.
     * Setting both {@code until} and {@code count} produces undefined behavior on the client side.
     */
    private String until;

    /**
     * The number of occurrences (exclusive with {@link #until}).
     * Setting both {@code until} and {@code count} produces undefined behavior on the client side.
     */
    private Integer count;

    /**
     * The interval between occurrences. Default is 1.
     */
    private Integer interval;

    /**
     * Days of the week for the recurrence. Values like {@code "mo"}, {@code "tu"}, {@code "we"},
     * {@code "th"}, {@code "fr"}, {@code "sa"}, {@code "su"}. May include offsets like {@code "-1fr"}
     * for the last Friday of the month.
     */
    private List<String> byweekday;

    /**
     * Day numbers within the year (1–366). Negative values count from the end.
     */
    private List<Integer> byyearday;

    /**
     * Month numbers (1–12).
     */
    private List<Integer> bymonth;

    /**
     * Day-of-month numbers (1–31). Negative values count from the end.
     */
    private List<Integer> bymonthday;

    /**
     * Hour numbers (0–23).
     */
    private List<Integer> byhour;

    /**
     * Minute numbers (0–59).
     */
    private List<Integer> byminute;

    /**
     * The week start day. Values like {@code "mo"}, {@code "su"}, etc.
     */
    private String wkst;

    /**
     * Raw iCalendar RRULE string. When set, all structured fields are ignored and this string
     * is sent directly to FC. Used for importing recurrence rules from external sources.
     * Example: {@code "FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=20231231T235959Z"}
     */
    private String rawRRule;

    /**
     * Dates to exclude from this recurrence. Transferred to the entry's {@code exdate} property
     * when the RRule is set on an {@link Entry} via {@link Entry#setRRule(RRule)}.
     * Not serialized as part of the {@code rrule} JSON object itself.
     */
    private List<LocalDate> excludedDates;

    private RRule() {
    }

    /**
     * Creates a new RRule with the given frequency.
     *
     * @param freq recurrence frequency
     * @return new RRule instance
     */
    public static RRule of(@NonNull Frequency freq) {
        RRule rrule = new RRule();
        rrule.freq = freq;
        return rrule;
    }

    /**
     * Creates a new weekly RRule.
     *
     * @return new RRule instance with WEEKLY frequency
     */
    public static RRule weekly() {
        return of(Frequency.WEEKLY);
    }

    /**
     * Creates a new daily RRule.
     *
     * @return new RRule instance with DAILY frequency
     */
    public static RRule daily() {
        return of(Frequency.DAILY);
    }

    /**
     * Creates a new monthly RRule.
     *
     * @return new RRule instance with MONTHLY frequency
     */
    public static RRule monthly() {
        return of(Frequency.MONTHLY);
    }

    /**
     * Creates a new yearly RRule.
     *
     * @return new RRule instance with YEARLY frequency
     */
    public static RRule yearly() {
        return of(Frequency.YEARLY);
    }

    /**
     * Creates a new RRule from a raw iCalendar RRULE string. The string will be sent directly to FC
     * without any parsing. Useful for importing recurrence rules from external sources.
     * <p>
     * Example: {@code RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=20231231T235959Z")}
     * If the source string includes the {@code RRULE:} prefix, strip it before passing to this method.
     *
     * @param rawRRule raw RRULE string (without the "RRULE:" prefix)
     * @return new RRule instance
     */
    public static RRule ofRaw(@NonNull String rawRRule) {
        RRule rrule = new RRule();
        rrule.rawRRule = rawRRule;
        return rrule;
    }

    /**
     * Sets the start date/time of the recurrence as an ISO 8601 string.
     *
     * @param dtstart ISO 8601 datetime string
     * @return this instance
     */
    public RRule dtstart(String dtstart) {
        this.dtstart = dtstart;
        return this;
    }

    /**
     * Sets the start date/time of the recurrence.
     *
     * @param dtstart start datetime
     * @return this instance
     */
    public RRule dtstart(LocalDateTime dtstart) {
        this.dtstart = dtstart != null ? dtstart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        return this;
    }

    /**
     * Sets the start date of the recurrence (all-day events).
     *
     * @param dtstart start date
     * @return this instance
     */
    public RRule dtstart(LocalDate dtstart) {
        this.dtstart = dtstart != null ? dtstart.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        return this;
    }

    /**
     * Sets the end date of the recurrence as an ISO 8601 string. Exclusive with {@link #count(int)}.
     *
     * @param until ISO 8601 date or datetime string
     * @return this instance
     */
    public RRule until(String until) {
        this.until = until;
        return this;
    }

    /**
     * Sets the end date of the recurrence. Exclusive with {@link #count(int)}.
     *
     * @param until end date
     * @return this instance
     */
    public RRule until(LocalDate until) {
        this.until = until != null ? until.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        return this;
    }

    /**
     * Sets the end datetime of the recurrence. Exclusive with {@link #count(int)}.
     *
     * @param until end datetime
     * @return this instance
     */
    public RRule until(LocalDateTime until) {
        this.until = until != null ? until.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        return this;
    }

    /**
     * Sets the number of occurrences. Exclusive with {@link #until(String)}.
     *
     * @param count number of occurrences
     * @return this instance
     */
    public RRule count(int count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the interval between occurrences. Default is 1.
     *
     * @param interval interval
     * @return this instance
     */
    public RRule interval(int interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Sets the days of the week for the recurrence using string abbreviations like {@code "mo"}, {@code "tu"},
     * {@code "we"}, {@code "th"}, {@code "fr"}, {@code "sa"}, {@code "su"}. Also supports positional
     * prefixes like {@code "-1fr"} for the last Friday of the month, or {@code "2mo"} for the
     * second Monday — syntax not available via the {@link DayOfWeek} overload.
     *
     * @param days day abbreviations
     * @return this instance
     * @see #byWeekday(DayOfWeek...)
     */
    public RRule byWeekday(String... days) {
        this.byweekday = Arrays.asList(days);
        return this;
    }

    /**
     * Sets the days of the week for the recurrence using {@link DayOfWeek} enum values.
     * For positional syntax (e.g. last Friday = {@code "-1fr"}), use {@link #byWeekday(String...)} instead.
     *
     * @param days days of the week
     * @return this instance
     */
    public RRule byWeekday(DayOfWeek... days) {
        this.byweekday = Stream.of(days).map(RRule::toRRuleDay).collect(Collectors.toList());
        return this;
    }

    /**
     * Sets day numbers within the year (1–366). Negative values count from the end of the year.
     *
     * @param days year day numbers
     * @return this instance
     */
    public RRule byYearday(Integer... days) {
        this.byyearday = Arrays.asList(days);
        return this;
    }

    /**
     * Sets month numbers (1–12).
     *
     * @param months month numbers
     * @return this instance
     * @see #byMonth(Month...)
     */
    public RRule byMonth(Integer... months) {
        this.bymonth = Arrays.asList(months);
        return this;
    }

    /**
     * Sets the months for the recurrence using {@link Month} enum values.
     *
     * @param months months of the year
     * @return this instance
     */
    public RRule byMonth(Month... months) {
        this.bymonth = Stream.of(months).map(Month::getValue).collect(Collectors.toList());
        return this;
    }

    /**
     * Sets day-of-month numbers (1–31). Negative values count from the end of the month.
     *
     * @param days day-of-month numbers
     * @return this instance
     */
    public RRule byMonthday(Integer... days) {
        this.bymonthday = Arrays.asList(days);
        return this;
    }

    /**
     * Sets hour numbers for fine-grained time-based recurrence (0–23).
     *
     * @param hours hour numbers
     * @return this instance
     */
    public RRule byHour(Integer... hours) {
        this.byhour = Arrays.asList(hours);
        return this;
    }

    /**
     * Sets minute numbers for fine-grained time-based recurrence (0–59).
     *
     * @param minutes minute numbers
     * @return this instance
     */
    public RRule byMinute(Integer... minutes) {
        this.byminute = Arrays.asList(minutes);
        return this;
    }

    /**
     * Sets the week start day using a string abbreviation like {@code "mo"}, {@code "su"}, etc.
     *
     * @param wkst week start day abbreviation
     * @return this instance
     * @see #weekStart(DayOfWeek)
     */
    public RRule weekStart(String wkst) {
        this.wkst = wkst;
        return this;
    }

    /**
     * Sets the week start day using a {@link DayOfWeek} enum value.
     *
     * @param wkst week start day
     * @return this instance
     */
    public RRule weekStart(DayOfWeek wkst) {
        this.wkst = toRRuleDay(wkst);
        return this;
    }

    /**
     * Sets the dates to exclude from this recurrence. When this RRule is set on an {@link Entry}
     * via {@link Entry#setRRule(RRule)}, these dates are transferred to the entry's {@code exdate}
     * property and serialized as a JSON array for FullCalendar's RRule plugin.
     *
     * @param dates dates to exclude
     * @return this instance
     */
    public RRule excludeDates(LocalDate... dates) {
        this.excludedDates = Arrays.asList(dates);
        return this;
    }

    /**
     * Sets the dates to exclude from this recurrence. When this RRule is set on an {@link Entry}
     * via {@link Entry#setRRule(RRule)}, these dates are transferred to the entry's {@code exdate}
     * property and serialized as a JSON array for FullCalendar's RRule plugin.
     *
     * @param dates dates to exclude
     * @return this instance
     */
    public RRule excludeDates(List<LocalDate> dates) {
        this.excludedDates = dates;
        return this;
    }

    /**
     * Converts a {@link DayOfWeek} to the 2-letter lowercase string expected by the FC rrule plugin
     * (e.g. {@code MONDAY} → {@code "mo"}).
     */
    private static String toRRuleDay(DayOfWeek day) {
        return day.name().substring(0, 2).toLowerCase();
    }

    /**
     * Serializes this RRule to a JsonNode for sending to the FC client.
     * <ul>
     *   <li>If a raw RRULE string was set (via {@link #ofRaw(String)}), returns a StringNode.</li>
     *   <li>Otherwise, returns an ObjectNode with the structured properties.</li>
     * </ul>
     *
     * @return JsonNode representing this RRule
     */
    public JsonNode toJson() {
        if (rawRRule != null) {
            return JsonUtils.toJsonNode(rawRRule);
        }

        ObjectNode node = JsonFactory.createObject();

        if (freq != null) {
            node.put("freq", freq.getClientSideValue());
        }
        if (dtstart != null) {
            node.put("dtstart", dtstart);
        }
        if (until != null) {
            node.put("until", until);
        }
        if (count != null) {
            node.put("count", count);
        }
        if (interval != null) {
            node.put("interval", interval);
        }
        if (byweekday != null && !byweekday.isEmpty()) {
            node.set("byweekday", JsonUtils.toJsonNode(byweekday));
        }
        if (byyearday != null && !byyearday.isEmpty()) {
            node.set("byyearday", JsonUtils.toJsonNode(byyearday));
        }
        if (bymonth != null && !bymonth.isEmpty()) {
            node.set("bymonth", JsonUtils.toJsonNode(bymonth));
        }
        if (bymonthday != null && !bymonthday.isEmpty()) {
            node.set("bymonthday", JsonUtils.toJsonNode(bymonthday));
        }
        if (byhour != null && !byhour.isEmpty()) {
            node.set("byhour", JsonUtils.toJsonNode(byhour));
        }
        if (byminute != null && !byminute.isEmpty()) {
            node.set("byminute", JsonUtils.toJsonNode(byminute));
        }
        if (wkst != null) {
            node.put("wkst", wkst);
        }

        return node;
    }
}

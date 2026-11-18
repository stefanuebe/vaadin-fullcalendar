package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The library compares an excluded date with the start of each occurrence. For a timed rule, the calendar sends each
 * excluded date with the time of the rule's start, in the time zone of the calendar (#461).
 */
class RRuleExcludedDatesTest {

    private static final LocalDate EXCLUDED = LocalDate.of(2025, 1, 8);

    private static JsonNode fetchExdate(FullCalendar calendar, Entry entry) {
        calendar.getEntryProvider().asInMemory().setItems(entry);
        return calendar.fetchEntriesFromServer(JsonFactory.createObject()).get(0).get("exdate");
    }

    private static Entry entry(LocalDateTime utcStart, RRule rule) {
        Entry entry = new Entry();
        entry.setStart(utcStart);
        entry.setRRule(rule);
        return entry;
    }

    private static FullCalendar berlinCalendar() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setTimeZone(new Timezone(ZoneId.of("Europe/Berlin")));
        return calendar;
    }

    @Test
    void timedRuleWithoutDtstartGetsTheEntryStartTimeInCalendarZone() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0), RRule.daily().excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T15:00:00\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void timedRuleWithDtstartGetsItsTime() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.daily().dtstart(LocalDateTime.of(2025, 1, 6, 9, 30)).excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T09:30:00\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void dtstartInBasicFormGivesTheTimeInExtendedForm() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.daily().dtstart("20250106T093000Z").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T09:30:00Z\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void dtstartWithoutSecondsGetsZeroSeconds() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.daily().dtstart("2025-01-06 09:30").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T09:30:00\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void dtstartWithFractionAndZoneKeepsBoth() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.daily().dtstart("2025-01-06T09:30:00.000Z").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T09:30:00.000Z\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void dtstartWithOffsetKeepsIt() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.daily().dtstart("2025-01-06T09:30:00+01:00").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T09:30:00+01:00\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void ruleAsTextGetsTheTimeOfItsDtstart() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 1, 8, 0),
                RRule.monthly().byWeekday("2we").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T09:00:00\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void rawRuleGetsTheTimeOfItsDtstartWithZone() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.ofRaw("DTSTART:20250106T140000Z\nRRULE:FREQ=DAILY").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T14:00:00Z\"]", fetchExdate(new FullCalendar(), entry).toString());
    }

    @Test
    void rawRuleWithTimezoneGetsTheTimeOfItsDtstart() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.ofRaw("DTSTART;TZID=Europe/Berlin:20250106T150000\nRRULE:FREQ=DAILY").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08T15:00:00\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void timedEntryWithDateOnlyDtstartKeepsPlainDates() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.daily().dtstart(LocalDate.of(2025, 1, 6)).excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void ruleAsTextStartingOnADateKeepsPlainDatesDespiteATimedUntil() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0),
                RRule.ofRaw("FREQ=DAILY;DTSTART=20250106;UNTIL=20250110T100000").excludeDates(EXCLUDED));

        assertEquals("[\"2025-01-08\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void allDayRuleKeepsPlainDates() {
        Entry entry = entry(LocalDate.of(2025, 1, 6).atStartOfDay(), RRule.daily().excludeDates(EXCLUDED));
        entry.setAllDay(true);

        assertEquals("[\"2025-01-08\"]", fetchExdate(berlinCalendar(), entry).toString());
    }

    @Test
    void entryKeepsPlainDates() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 6, 14, 0), RRule.daily().excludeDates(EXCLUDED));
        fetchExdate(berlinCalendar(), entry);

        assertEquals("[\"2025-01-08\"]", entry.toJson().get("exdate").toString());
    }
}

package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A rule without dtstart starts at the start of its entry. The library reads that start in the time zone of the
 * calendar, so the calendar converts the UTC start of the entry to its zone (#451). This also holds for a rule with a
 * positional weekday, which goes to the client as text (#452).
 */
class RRuleEntryStartTest {

    private static final Timezone BERLIN = new Timezone(ZoneId.of("Europe/Berlin"));

    private static FullCalendar berlinCalendar() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setTimeZone(BERLIN);
        return calendar;
    }

    private static Entry entry(LocalDateTime utcStart, RRule rule) {
        Entry entry = new Entry();
        entry.setStart(utcStart);
        entry.setRRule(rule);
        return entry;
    }

    private static Entry allDayEntry(LocalDate start, RRule rule) {
        Entry entry = entry(start.atStartOfDay(), rule);
        entry.setAllDay(true);
        return entry;
    }

    private static JsonNode fetchRRule(FullCalendar calendar, Entry entry) {
        calendar.getEntryProvider().asInMemory().setItems(entry);

        return calendar.fetchEntriesFromServer(JsonFactory.createObject()).get(0).get("rrule");
    }

    @Test
    void timedEntryStartIsConvertedToCalendarZone() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0), RRule.daily().count(1));

        assertEquals("2025-01-07T15:00:00", fetchRRule(berlinCalendar(), entry).get("dtstart").asString());
    }

    @Test
    void timedEntryStartUsesTheOffsetOfItsDate() {
        // summer time in Berlin is two hours ahead of UTC
        Entry entry = entry(LocalDateTime.of(2025, 7, 8, 14, 0), RRule.daily().count(1));

        assertEquals("2025-07-08T16:00:00", fetchRRule(berlinCalendar(), entry).get("dtstart").asString());
    }

    @Test
    void allDayEntryStartStaysTheDate() {
        Entry entry = allDayEntry(LocalDate.of(2025, 3, 3), RRule.weekly());

        assertEquals("2025-03-03", fetchRRule(berlinCalendar(), entry).get("dtstart").asString());
    }

    @Test
    void positionalRuleGetsEntryStartAsDtstart() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0), RRule.monthly().byWeekday("2we"));

        assertEquals("FREQ=MONTHLY;DTSTART=20250107T150000;BYDAY=2WE", fetchRRule(berlinCalendar(), entry).asString());
    }

    @Test
    void positionalRuleOfAllDayEntryGetsTheDate() {
        Entry entry = allDayEntry(LocalDate.of(2025, 1, 1), RRule.monthly().byWeekday("2we"));

        assertEquals("FREQ=MONTHLY;DTSTART=20250101;BYDAY=2WE", fetchRRule(berlinCalendar(), entry).asString());
    }

    @Test
    void explicitDtstartIsSentAsItIs() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0),
                RRule.daily().dtstart(LocalDateTime.of(2025, 2, 1, 9, 0)));

        assertEquals("2025-02-01T09:00:00", fetchRRule(berlinCalendar(), entry).get("dtstart").asString());
    }

    @Test
    void explicitDtstartOfPositionalRuleIsSentAsItIs() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0),
                RRule.monthly().byWeekday("2we").dtstart(LocalDate.of(2025, 2, 1)));

        assertEquals("FREQ=MONTHLY;DTSTART=20250201;BYDAY=2WE", fetchRRule(berlinCalendar(), entry).asString());
    }

    @Test
    void rawRuleIsSentAsItIs() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0), RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO"));

        assertEquals("FREQ=WEEKLY;BYDAY=MO", fetchRRule(berlinCalendar(), entry).asString());
    }

    @Test
    void utcCalendarKeepsTheUtcStart() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0), RRule.daily());

        assertEquals("2025-01-07T14:00:00", fetchRRule(new FullCalendar(), entry).get("dtstart").asString());
    }

    @Test
    void changedTimezoneMovesTheStart() {
        FullCalendar calendar = berlinCalendar();
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0), RRule.daily());
        fetchRRule(calendar, entry);

        calendar.options().setTimeZone(new Timezone(ZoneId.of("America/New_York")));

        assertEquals("2025-01-07T09:00:00", fetchRRule(calendar, entry).get("dtstart").asString());
    }

    @Test
    void localCalendarUsesTheReportedBrowserZone() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setTimeZone(Timezone.LOCAL);
        calendar.setBrowserTimezone("Europe/Berlin");
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0), RRule.daily());

        assertEquals("2025-01-07T15:00:00", fetchRRule(calendar, entry).get("dtstart").asString());
    }

    @Test
    void entryRuleKeepsNoDtstart() {
        Entry entry = entry(LocalDateTime.of(2025, 1, 7, 14, 0), RRule.daily());
        fetchRRule(berlinCalendar(), entry);

        assertNull(entry.getRRule().getDtstart());
    }
}

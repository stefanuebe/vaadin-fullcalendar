package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Range filtering of recurring entries: the recurring start and end date, combined with their times, bound
 * the recurrence.
 */
class EntryQueryTest {

    private static boolean matches(Entry entry, LocalDateTime start, LocalDateTime end) {
        return new EntryQuery(start, end, EntryQuery.AllDay.BOTH).applyFilter(Stream.of(entry)).findAny().isPresent();
    }

    private static Entry recurring() {
        Entry entry = new Entry();
        entry.setRecurringDaysOfWeek(DayOfWeek.MONDAY);
        return entry;
    }

    @Test
    void recurringWithoutDates_alwaysMatches() {
        Entry entry = recurring();
        assertTrue(matches(entry, LocalDateTime.of(1990, 1, 1, 0, 0), LocalDateTime.of(1990, 1, 2, 0, 0)));
    }

    @Test
    void recurringEndDateWithoutTime_includesThatDay() {
        // the client treats the end date as exclusive, the filter includes it (deliberate over-inclusion)
        Entry entry = recurring();
        entry.setRecurringEndDate(LocalDate.of(2025, 3, 31));

        assertTrue(matches(entry, LocalDateTime.of(2025, 3, 31, 23, 59, 58), null));
        assertFalse(matches(entry, LocalDateTime.of(2025, 3, 31, 23, 59, 59), null));
    }

    @Test
    void recurringEndDateWithTime_endsAtThatTime() {
        Entry entry = recurring();
        entry.setRecurringEndDate(LocalDate.of(2025, 3, 31));
        entry.setRecurringEndTime(LocalTime.of(10, 30));

        assertTrue(matches(entry, LocalDateTime.of(2025, 3, 31, 10, 29), null));
        assertFalse(matches(entry, LocalDateTime.of(2025, 3, 31, 10, 30), null));
    }

    @Test
    void recurringStartDateWithoutTime_startsAtStartOfThatDay() {
        Entry entry = recurring();
        entry.setRecurringStartDate(LocalDate.of(2025, 3, 1));

        assertTrue(matches(entry, null, LocalDateTime.of(2025, 3, 1, 0, 1)));
        assertFalse(matches(entry, null, LocalDateTime.of(2025, 3, 1, 0, 0)));
    }

    @Test
    void recurringStartDateWithTime_startsAtThatTime() {
        Entry entry = recurring();
        entry.setRecurringStartDate(LocalDate.of(2025, 3, 1));
        entry.setRecurringStartTime(LocalTime.of(9, 30));

        assertTrue(matches(entry, null, LocalDateTime.of(2025, 3, 1, 9, 31)));
        assertFalse(matches(entry, null, LocalDateTime.of(2025, 3, 1, 9, 30)));
    }

    @Test
    void recurringEndTimeBeyond24h_shiftsToNextDay() {
        Entry entry = recurring();
        entry.setRecurringEndDate(LocalDate.of(2025, 3, 31));
        entry.setRecurringEndTime(RecurringTime.of(26, 0));

        assertTrue(matches(entry, LocalDateTime.of(2025, 4, 1, 1, 59), null));
        assertFalse(matches(entry, LocalDateTime.of(2025, 4, 1, 2, 0), null));
    }

    @Test
    void recurringStartAndEnd_matchesOverlappingRangeOnly() {
        Entry entry = recurring();
        entry.setRecurringStartDate(LocalDate.of(2025, 3, 1));
        entry.setRecurringEndDate(LocalDate.of(2025, 3, 31));

        assertTrue(matches(entry, LocalDateTime.of(2025, 3, 10, 0, 0), LocalDateTime.of(2025, 3, 17, 0, 0)));
        assertFalse(matches(entry, LocalDateTime.of(2025, 2, 1, 0, 0), LocalDateTime.of(2025, 3, 1, 0, 0)));
        assertFalse(matches(entry, LocalDateTime.of(2025, 4, 1, 0, 0), LocalDateTime.of(2025, 4, 8, 0, 0)));
    }

    @Test
    void recurringStartTimeBeyond24h_shiftsToNextDay() {
        Entry entry = recurring();
        entry.setRecurringStartDate(LocalDate.of(2025, 3, 1));
        entry.setRecurringStartTime(RecurringTime.of(25, 0));

        assertTrue(matches(entry, null, LocalDateTime.of(2025, 3, 2, 1, 1)));
        assertFalse(matches(entry, null, LocalDateTime.of(2025, 3, 2, 1, 0)));
    }
}

package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.DateRange;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** The factory methods of {@link DateRange} for a range with one open end. */
public class DateRangeTest {

    private static final LocalDate MARCH_1 = LocalDate.of(2025, 3, 1);

    @Test
    void withOpenEnd_startsAtTheDate_andHasNoEnd() {
        DateRange range = DateRange.withOpenEnd(MARCH_1);

        assertEquals(MARCH_1, range.start());
        assertNull(range.end());
    }

    @Test
    void withOpenStart_endsAtTheDate_andHasNoStart() {
        DateRange range = DateRange.withOpenStart(MARCH_1);

        assertNull(range.start());
        assertEquals(MARCH_1, range.end());
    }

    @Test
    void withOpenEnd_null_throws() {
        assertThrows(NullPointerException.class, () -> DateRange.withOpenEnd(null));
    }

    @Test
    void withOpenStart_null_throws() {
        assertThrows(NullPointerException.class, () -> DateRange.withOpenStart(null));
    }
}

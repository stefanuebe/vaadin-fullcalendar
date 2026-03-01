package org.vaadin.stefan.fullcalendar.dataprovider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CalendarQuery}.
 */
class CalendarQueryTest {

    @Test
    @DisplayName("no-arg constructor creates query with null start and end")
    void noArgConstructor() {
        CalendarQuery query = new CalendarQuery();

        assertNull(query.getStart());
        assertNull(query.getEnd());
    }

    @Test
    @DisplayName("constructor with start and end stores values")
    void withStartAndEnd() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 31, 23, 59);

        CalendarQuery query = new CalendarQuery(start, end);

        assertEquals(start, query.getStart());
        assertEquals(end, query.getEnd());
    }

    @Test
    @DisplayName("start and end can be individually null")
    void partiallyNull() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 1, 0, 0);

        CalendarQuery queryStartOnly = new CalendarQuery(start, null);
        assertEquals(start, queryStartOnly.getStart());
        assertNull(queryStartOnly.getEnd());

        CalendarQuery queryEndOnly = new CalendarQuery(null, start);
        assertNull(queryEndOnly.getStart());
        assertEquals(start, queryEndOnly.getEnd());
    }
}

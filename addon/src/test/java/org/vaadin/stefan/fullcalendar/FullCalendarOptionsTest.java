package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;

import java.util.stream.Stream;

public class FullCalendarOptionsTest {
    @Test
    void testNonEmptyOptionKeys() {
        Assertions.assertFalse(Stream.of(Option.values())
                .map(Option::getOptionKey)
                .anyMatch(s -> s == null || s.trim().isEmpty()));
    }


}

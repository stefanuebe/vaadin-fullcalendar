package org.vaadin.stefan.fullcalendar.dataprovider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CalendarItemsChangeEvent}.
 */
class CalendarItemsChangeEventTest {

    @Test
    @DisplayName("getSource returns the provider")
    void getSource() {
        InMemoryCalendarItemProvider<String> provider =
                new InMemoryCalendarItemProvider<>(s -> s);

        CalendarItemsChangeEvent<String> event = new CalendarItemsChangeEvent<>(provider);

        assertSame(provider, event.getSource());
    }

    @Test
    @DisplayName("constructor rejects null source")
    void rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new CalendarItemsChangeEvent<>(null));
    }
}

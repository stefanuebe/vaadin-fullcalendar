package org.vaadin.stefan.fullcalendar.dataprovider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CalendarItemRefreshEvent}.
 */
class CalendarItemRefreshEventTest {

    @Test
    @DisplayName("getSource returns the provider")
    void getSource() {
        InMemoryCalendarItemProvider<String> provider =
                new InMemoryCalendarItemProvider<>(s -> s);

        CalendarItemRefreshEvent<String> event = new CalendarItemRefreshEvent<>(provider, "item-1");

        assertSame(provider, event.getSource());
    }

    @Test
    @DisplayName("getItemToRefresh returns the item")
    void getItemToRefresh() {
        InMemoryCalendarItemProvider<String> provider =
                new InMemoryCalendarItemProvider<>(s -> s);

        CalendarItemRefreshEvent<String> event = new CalendarItemRefreshEvent<>(provider, "item-1");

        assertEquals("item-1", event.getItemToRefresh());
    }

    @Test
    @DisplayName("constructor rejects null source")
    void rejectsNullSource() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalendarItemRefreshEvent<>(null, "item"));
    }
}

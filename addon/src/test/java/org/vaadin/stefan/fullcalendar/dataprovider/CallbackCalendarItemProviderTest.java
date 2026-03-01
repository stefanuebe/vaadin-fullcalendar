package org.vaadin.stefan.fullcalendar.dataprovider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CallbackCalendarItemProvider}.
 */
class CallbackCalendarItemProviderTest {

    @Test
    @DisplayName("fetch delegates to callback")
    void fetchDelegates() {
        var provider = new CallbackCalendarItemProvider<>(
                query -> Stream.of("a", "b"),
                id -> null);

        List<String> result = provider.fetch(new CalendarQuery()).collect(Collectors.toList());

        assertEquals(2, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    @Test
    @DisplayName("fetch passes query to callback")
    void fetchPassesQuery() {
        AtomicReference<CalendarQuery> received = new AtomicReference<>();

        var provider = new CallbackCalendarItemProvider<>(
                query -> {
                    received.set(query);
                    return Stream.empty();
                },
                id -> null);

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
        CalendarQuery query = new CalendarQuery(start, end);

        provider.fetch(query);

        assertSame(query, received.get());
        assertEquals(start, received.get().getStart());
        assertEquals(end, received.get().getEnd());
    }

    @Test
    @DisplayName("fetchById delegates to callback and wraps in Optional")
    void fetchByIdDelegates() {
        var provider = new CallbackCalendarItemProvider<>(
                query -> Stream.empty(),
                id -> id.equals("found") ? "found-item" : null);

        assertTrue(provider.fetchById("found").isPresent());
        assertEquals("found-item", provider.fetchById("found").get());
        assertTrue(provider.fetchById("missing").isEmpty());
    }

    @Test
    @DisplayName("refreshAll fires change event")
    void refreshAllFiresEvent() {
        AtomicBoolean fired = new AtomicBoolean(false);
        var provider = new CallbackCalendarItemProvider<>(
                q -> Stream.empty(), id -> null);
        provider.addItemsChangeListener(e -> fired.set(true));

        provider.refreshAll();

        assertTrue(fired.get());
    }

    @Test
    @DisplayName("refreshItem fires refresh event")
    void refreshItemFiresEvent() {
        AtomicReference<String> refreshed = new AtomicReference<>();
        CallbackCalendarItemProvider<String> provider = new CallbackCalendarItemProvider<>(
                q -> Stream.empty(), id -> null);
        provider.addItemRefreshListener(e -> refreshed.set(e.getItemToRefresh()));

        provider.refreshItem("my-item");

        assertEquals("my-item", refreshed.get());
    }

    @Test
    @DisplayName("constructor rejects null callbacks")
    void rejectsNull() {
        assertThrows(NullPointerException.class,
                () -> new CallbackCalendarItemProvider<>(null, id -> null));
        assertThrows(NullPointerException.class,
                () -> new CallbackCalendarItemProvider<>(q -> Stream.empty(), null));
    }
}

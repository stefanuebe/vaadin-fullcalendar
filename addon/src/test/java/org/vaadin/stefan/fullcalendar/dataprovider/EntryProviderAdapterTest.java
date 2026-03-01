package org.vaadin.stefan.fullcalendar.dataprovider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.Entry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying Phase 2 adapter wiring: EntryProvider hierarchy extends
 * CalendarItemProvider hierarchy with zero breaking changes.
 */
class EntryProviderAdapterTest {

    @Nested
    @DisplayName("Type assignability")
    class TypeAssignability {

        @Test
        @DisplayName("EntryProvider is assignable to CalendarItemProvider")
        void entryProviderIsCalendarItemProvider() {
            assertTrue(CalendarItemProvider.class.isAssignableFrom(EntryProvider.class));
        }

        @Test
        @DisplayName("InMemoryEntryProvider is assignable to CalendarItemProvider")
        void inMemoryEntryProviderIsCalendarItemProvider() {
            InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
            assertInstanceOf(CalendarItemProvider.class, provider);
        }

        @Test
        @DisplayName("CallbackEntryProvider is assignable to CalendarItemProvider")
        void callbackEntryProviderIsCalendarItemProvider() {
            CallbackEntryProvider<Entry> provider = EntryProvider.fromCallbacks(
                    q -> Stream.empty(), id -> null);
            assertInstanceOf(CalendarItemProvider.class, provider);
        }

        @Test
        @DisplayName("EntryQuery is assignable to CalendarQuery")
        void entryQueryIsCalendarQuery() {
            assertTrue(CalendarQuery.class.isAssignableFrom(EntryQuery.class));
        }

        @Test
        @DisplayName("EntriesChangeEvent is assignable to CalendarItemsChangeEvent")
        void entriesChangeEventIsCalendarItemsChangeEvent() {
            assertTrue(CalendarItemsChangeEvent.class.isAssignableFrom(EntriesChangeEvent.class));
        }

        @Test
        @DisplayName("EntryRefreshEvent is assignable to CalendarItemRefreshEvent")
        void entryRefreshEventIsCalendarItemRefreshEvent() {
            assertTrue(CalendarItemRefreshEvent.class.isAssignableFrom(EntryRefreshEvent.class));
        }
    }

    @Nested
    @DisplayName("Bridge fetch")
    class BridgeFetch {

        @Test
        @DisplayName("fetch(CalendarQuery) wraps to EntryQuery when not already an EntryQuery")
        void fetchCalendarQueryWraps() {
            Entry entry = new Entry("1");
            entry.setStart(LocalDateTime.of(2024, 1, 15, 10, 0));
            entry.setEnd(LocalDateTime.of(2024, 1, 15, 11, 0));

            InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
            provider.addEntry(entry);

            // Call via CalendarItemProvider interface
            CalendarItemProvider<Entry> cipRef = provider;
            CalendarQuery query = new CalendarQuery(
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 2, 1, 0, 0));

            List<Entry> results = cipRef.fetch(query).toList();
            assertEquals(1, results.size());
            assertEquals("1", results.getFirst().getId());
        }

        @Test
        @DisplayName("fetch(CalendarQuery) pattern-matches EntryQuery and delegates directly")
        void fetchEntryQueryPassthrough() {
            AtomicReference<EntryQuery> capturedQuery = new AtomicReference<>();

            CallbackEntryProvider<Entry> provider = EntryProvider.fromCallbacks(
                    q -> {
                        capturedQuery.set(q);
                        return Stream.empty();
                    },
                    id -> null);

            EntryQuery entryQuery = new EntryQuery(
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 2, 1, 0, 0),
                    EntryQuery.AllDay.ALL_DAY_ONLY);

            // Call via CalendarItemProvider.fetch(CalendarQuery), passing an EntryQuery
            CalendarItemProvider<Entry> cipRef = provider;
            cipRef.fetch((CalendarQuery) entryQuery).toList();

            // The original EntryQuery should be passed through, not wrapped
            assertSame(entryQuery, capturedQuery.get());
            assertEquals(EntryQuery.AllDay.ALL_DAY_ONLY, capturedQuery.get().getAllDay());
        }
    }

    @Nested
    @DisplayName("Event bridge")
    class EventBridge {

        @Test
        @DisplayName("refreshAll fires both EntriesChangeEvent and CalendarItemsChangeEvent listeners")
        void refreshAllFiresBothListeners() {
            InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

            AtomicInteger entriesChangeCount = new AtomicInteger();
            AtomicInteger itemsChangeCount = new AtomicInteger();

            provider.addEntriesChangeListener(e -> entriesChangeCount.incrementAndGet());
            provider.addItemsChangeListener(e -> itemsChangeCount.incrementAndGet());

            provider.refreshAll();

            assertEquals(1, entriesChangeCount.get(), "EntriesChangeEvent listener should fire");
            assertEquals(1, itemsChangeCount.get(), "CalendarItemsChangeEvent listener should fire");
        }

        @Test
        @DisplayName("refreshItem fires both EntryRefreshEvent and CalendarItemRefreshEvent listeners")
        void refreshItemFiresBothListeners() {
            InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
            Entry entry = new Entry("1");

            AtomicReference<Entry> entryRefreshItem = new AtomicReference<>();
            AtomicReference<Object> itemRefreshItem = new AtomicReference<>();

            provider.addEntryRefreshListener(e -> entryRefreshItem.set(e.getItemToRefresh()));
            provider.addItemRefreshListener(e -> itemRefreshItem.set(e.getItemToRefresh()));

            provider.refreshItem(entry);

            assertSame(entry, entryRefreshItem.get(), "EntryRefreshEvent should carry the item");
            assertSame(entry, itemRefreshItem.get(), "CalendarItemRefreshEvent should carry the item");
        }

        @Test
        @DisplayName("CalendarItemsChangeEvent listener receives event with correct source type")
        void itemsChangeEventSourceType() {
            InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

            AtomicReference<CalendarItemsChangeEvent<?>> capturedEvent = new AtomicReference<>();
            provider.addItemsChangeListener(capturedEvent::set);

            provider.refreshAll();

            assertNotNull(capturedEvent.get());
            assertInstanceOf(EntriesChangeEvent.class, capturedEvent.get());
            assertInstanceOf(EntryProvider.class, capturedEvent.get().getSource());
        }
    }

    @Nested
    @DisplayName("EntryQuery construction")
    class EntryQueryConstruction {

        @Test
        @DisplayName("no-arg constructor creates query with null start/end and BOTH allDay")
        void noArgConstructor() {
            EntryQuery query = new EntryQuery();
            assertNull(query.getStart());
            assertNull(query.getEnd());
            assertEquals(EntryQuery.AllDay.BOTH, query.getAllDay());
        }

        @Test
        @DisplayName("LocalDateTime constructor sets start/end, defaults allDay to BOTH")
        void localDateTimeConstructor() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 2, 1, 0, 0);

            EntryQuery query = new EntryQuery(start, end);
            assertEquals(start, query.getStart());
            assertEquals(end, query.getEnd());
            assertEquals(EntryQuery.AllDay.BOTH, query.getAllDay());
        }

        @Test
        @DisplayName("full constructor sets all fields")
        void fullConstructor() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 2, 1, 0, 0);

            EntryQuery query = new EntryQuery(start, end, EntryQuery.AllDay.TIMED_ONLY);
            assertEquals(start, query.getStart());
            assertEquals(end, query.getEnd());
            assertEquals(EntryQuery.AllDay.TIMED_ONLY, query.getAllDay());
        }

        @Test
        @DisplayName("builder creates query with correct values")
        void builder() {
            LocalDateTime start = LocalDateTime.of(2024, 3, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 4, 1, 0, 0);

            EntryQuery query = EntryQuery.builder()
                    .start(start)
                    .end(end)
                    .allDay(EntryQuery.AllDay.ALL_DAY_ONLY)
                    .build();

            assertEquals(start, query.getStart());
            assertEquals(end, query.getEnd());
            assertEquals(EntryQuery.AllDay.ALL_DAY_ONLY, query.getAllDay());
        }

        @Test
        @DisplayName("EntryQuery getStart/getEnd return values via CalendarQuery reference")
        void calendarQueryGetters() {
            LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 1, 0, 0);

            CalendarQuery query = new EntryQuery(start, end);
            assertEquals(start, query.getStart());
            assertEquals(end, query.getEnd());
        }
    }

    @Nested
    @DisplayName("Backward compatibility")
    class BackwardCompat {

        @Test
        @DisplayName("InMemoryEntryProvider addEntry/fetch/fetchById unchanged")
        void inMemoryEntryProviderBasicOps() {
            InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

            Entry entry = new Entry("test-1");
            entry.setTitle("Test Entry");

            provider.addEntry(entry);

            assertEquals(1, provider.fetchAll().count());

            Optional<Entry> fetched = provider.fetchById("test-1");
            assertTrue(fetched.isPresent());
            assertEquals("Test Entry", fetched.get().getTitle());
        }

        @Test
        @DisplayName("CallbackEntryProvider delegates unchanged")
        void callbackEntryProviderDelegates() {
            List<Entry> backingList = new ArrayList<>();
            backingList.add(new Entry("cb-1"));
            backingList.add(new Entry("cb-2"));

            CallbackEntryProvider<Entry> provider = EntryProvider.fromCallbacks(
                    q -> backingList.stream(),
                    id -> backingList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null));

            assertEquals(2, provider.fetchAll().count());
            assertTrue(provider.fetchById("cb-1").isPresent());
            assertTrue(provider.fetchById("cb-2").isPresent());
            assertFalse(provider.fetchById("cb-3").isPresent());
        }

        @Test
        @DisplayName("EntryProvider.isInMemory works correctly")
        void isInMemory() {
            EntryProvider<Entry> inMemory = EntryProvider.emptyInMemory();
            assertTrue(inMemory.isInMemory());

            EntryProvider<Entry> callback = EntryProvider.fromCallbacks(q -> Stream.empty(), id -> null);
            assertFalse(callback.isInMemory());
        }

        @Test
        @DisplayName("EntryProvider.asInMemory works correctly")
        void asInMemory() {
            EntryProvider<Entry> provider = EntryProvider.emptyInMemory();
            InMemoryEntryProvider<Entry> inMemory = provider.asInMemory();
            assertNotNull(inMemory);
        }

        @Test
        @DisplayName("EntryProvider.asInMemory throws for callback provider")
        void asInMemoryThrowsForCallback() {
            EntryProvider<Entry> provider = EntryProvider.fromCallbacks(q -> Stream.empty(), id -> null);
            assertThrows(ClassCastException.class, provider::asInMemory);
        }

        @Test
        @DisplayName("EntryProvider fetchAll delegates to fetch(EntryQuery)")
        void fetchAllDelegates() {
            InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
            provider.addEntry(new Entry("a"));
            provider.addEntry(new Entry("b"));

            assertEquals(2, provider.fetchAll().count());
        }
    }
}

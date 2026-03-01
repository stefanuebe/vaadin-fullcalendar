package org.vaadin.stefan.fullcalendar.dataprovider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link InMemoryCalendarItemProvider}.
 */
class InMemoryCalendarItemProviderTest {

    // Simple record-like test item
    static class TestItem {
        final String id;
        final String name;

        TestItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    InMemoryCalendarItemProvider<TestItem> provider;

    @BeforeEach
    void setUp() {
        provider = new InMemoryCalendarItemProvider<>(item -> item.id);
    }

    @Nested
    @DisplayName("CRUD operations")
    class CrudOperations {

        @Test
        @DisplayName("addItem adds item retrievable by ID")
        void addItem() {
            provider.addItem(new TestItem("1", "First"));

            assertEquals(1, provider.getItems().size());
            assertTrue(provider.getItemById("1").isPresent());
            assertEquals("First", provider.getItemById("1").get().name);
        }

        @Test
        @DisplayName("addItem ignores duplicate IDs")
        void addDuplicate() {
            provider.addItem(new TestItem("1", "First"));
            provider.addItem(new TestItem("1", "Duplicate"));

            assertEquals(1, provider.getItems().size());
            assertEquals("First", provider.getItemById("1").get().name);
        }

        @Test
        @DisplayName("addItems adds multiple items")
        void addItemsIterable() {
            provider.addItems(List.of(
                    new TestItem("1", "First"),
                    new TestItem("2", "Second")));

            assertEquals(2, provider.getItems().size());
        }

        @Test
        @DisplayName("addItems varargs adds multiple items")
        void addItemsVarargs() {
            provider.addItems(
                    new TestItem("1", "First"),
                    new TestItem("2", "Second"));

            assertEquals(2, provider.getItems().size());
        }

        @Test
        @DisplayName("removeItem removes by ID")
        void removeItem() {
            TestItem item = new TestItem("1", "First");
            provider.addItem(item);
            provider.removeItem(item);

            assertEquals(0, provider.getItems().size());
            assertTrue(provider.getItemById("1").isEmpty());
        }

        @Test
        @DisplayName("removeItem is noop for missing items")
        void removeNonExistent() {
            provider.addItem(new TestItem("1", "First"));
            provider.removeItem(new TestItem("2", "Missing"));

            assertEquals(1, provider.getItems().size());
        }

        @Test
        @DisplayName("removeItems removes multiple items")
        void removeItems() {
            TestItem a = new TestItem("1", "A");
            TestItem b = new TestItem("2", "B");
            provider.addItems(a, b);
            provider.removeItems(List.of(a, b));

            assertEquals(0, provider.getItems().size());
        }

        @Test
        @DisplayName("removeAllItems clears all items")
        void removeAllItems() {
            provider.addItems(
                    new TestItem("1", "First"),
                    new TestItem("2", "Second"));
            provider.removeAllItems();

            assertEquals(0, provider.getItems().size());
        }
    }

    @Nested
    @DisplayName("Fetch operations")
    class FetchOperations {

        @Test
        @DisplayName("fetch returns all items regardless of query")
        void fetchReturnsAll() {
            provider.addItems(
                    new TestItem("1", "First"),
                    new TestItem("2", "Second"));

            // Even with a time-range query, in-memory returns all (no time filtering in base impl)
            long count = provider.fetch(new CalendarQuery()).count();
            assertEquals(2, count);
        }

        @Test
        @DisplayName("fetchById returns correct item")
        void fetchById() {
            provider.addItem(new TestItem("42", "TheOne"));

            assertTrue(provider.fetchById("42").isPresent());
            assertEquals("TheOne", provider.fetchById("42").get().name);
        }

        @Test
        @DisplayName("fetchById returns empty for missing ID")
        void fetchByIdMissing() {
            assertTrue(provider.fetchById("nonexistent").isEmpty());
        }

        @Test
        @DisplayName("getItems returns items in insertion order")
        void insertionOrder() {
            provider.addItem(new TestItem("c", "Third"));
            provider.addItem(new TestItem("a", "First"));
            provider.addItem(new TestItem("b", "Second"));

            List<TestItem> items = provider.getItems();
            assertEquals("c", items.get(0).id);
            assertEquals("a", items.get(1).id);
            assertEquals("b", items.get(2).id);
        }
    }

    @Nested
    @DisplayName("Events")
    class Events {

        @Test
        @DisplayName("refreshAll fires CalendarItemsChangeEvent")
        void refreshAllFiresEvent() {
            AtomicBoolean fired = new AtomicBoolean(false);
            provider.addItemsChangeListener(event -> fired.set(true));

            provider.refreshAll();

            assertTrue(fired.get());
        }

        @Test
        @DisplayName("refreshItem fires CalendarItemRefreshEvent")
        void refreshItemFiresEvent() {
            TestItem item = new TestItem("1", "Test");
            AtomicReference<TestItem> refreshed = new AtomicReference<>();
            provider.addItemRefreshListener(event -> refreshed.set(event.getItemToRefresh()));

            provider.refreshItem(item);

            assertSame(item, refreshed.get());
        }

        @Test
        @DisplayName("listener registration can be removed")
        void listenerRemoval() {
            AtomicBoolean fired = new AtomicBoolean(false);
            var registration = provider.addItemsChangeListener(event -> fired.set(true));
            registration.remove();

            provider.refreshAll();

            assertFalse(fired.get());
        }
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("constructor with iterable pre-populates items")
        void constructorWithItems() {
            var populated = new InMemoryCalendarItemProvider<>(
                    (TestItem item) -> item.id,
                    List.of(new TestItem("1", "A"), new TestItem("2", "B")));

            assertEquals(2, populated.getItems().size());
        }

        @Test
        @DisplayName("constructor rejects null idExtractor")
        void rejectsNullExtractor() {
            assertThrows(NullPointerException.class,
                    () -> new InMemoryCalendarItemProvider<>(null));
        }
    }
}

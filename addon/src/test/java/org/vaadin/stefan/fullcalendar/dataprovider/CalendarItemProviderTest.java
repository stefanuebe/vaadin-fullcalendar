package org.vaadin.stefan.fullcalendar.dataprovider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CalendarItemProvider} interface (factory methods, defaults).
 */
class CalendarItemProviderTest {

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("emptyInMemory creates empty provider")
        void emptyInMemory() {
            CalendarItemProvider<String> provider = CalendarItemProvider.emptyInMemory(s -> s);

            assertInstanceOf(InMemoryCalendarItemProvider.class, provider);
            assertEquals(0, provider.fetchAll().count());
        }

        @Test
        @DisplayName("inMemoryFrom(varargs) creates pre-populated provider")
        void inMemoryFromVarargs() {
            CalendarItemProvider<String> provider =
                    CalendarItemProvider.inMemoryFrom(s -> s, "a", "b", "c");

            assertInstanceOf(InMemoryCalendarItemProvider.class, provider);
            assertEquals(3, provider.fetchAll().count());
        }

        @Test
        @DisplayName("inMemoryFrom(Iterable) creates pre-populated provider")
        void inMemoryFromIterable() {
            CalendarItemProvider<String> provider =
                    CalendarItemProvider.inMemoryFrom(s -> s, List.of("x", "y"));

            assertInstanceOf(InMemoryCalendarItemProvider.class, provider);
            assertEquals(2, provider.fetchAll().count());
        }

        @Test
        @DisplayName("fromCallbacks creates callback provider")
        void fromCallbacks() {
            CalendarItemProvider<String> provider = CalendarItemProvider.fromCallbacks(
                    query -> Stream.of("item-1"),
                    id -> id.equals("item-1") ? "item-1" : null);

            assertInstanceOf(CallbackCalendarItemProvider.class, provider);
            assertEquals(1, provider.fetchAll().count());
            assertTrue(provider.fetchById("item-1").isPresent());
            assertTrue(provider.fetchById("missing").isEmpty());
        }
    }

    @Nested
    @DisplayName("Default methods")
    class DefaultMethods {

        @Test
        @DisplayName("fetchAll delegates to fetch with empty query")
        void fetchAll() {
            InMemoryCalendarItemProvider<String> provider =
                    CalendarItemProvider.inMemoryFrom(s -> s, "a", "b");

            List<String> all = provider.fetchAll().collect(Collectors.toList());

            assertEquals(2, all.size());
            assertTrue(all.contains("a"));
            assertTrue(all.contains("b"));
        }

    }
}

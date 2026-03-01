package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A provider for calendar items of arbitrary type. Similar in concept to {@link EntryProvider}
 * but without requiring items to extend {@link org.vaadin.stefan.fullcalendar.Entry}.
 * <p>
 * This is the base interface for the Calendar Item Provider (CIP) feature. It supports
 * both in-memory and callback-based data provision.
 *
 * @param <T> the calendar item type
 * @author Stefan Uebe
 */
public interface CalendarItemProvider<T> extends Serializable {

    /**
     * Streams items based on the given query. The query might have null start/end to fetch all items.
     *
     * @param query the query with optional time range
     * @return stream of matching items
     */
    Stream<T> fetch(CalendarQuery query);

    /**
     * Returns a single item by its ID, or empty if not found.
     *
     * @param id the item ID
     * @return optional item or empty
     */
    Optional<T> fetchById(String id);

    /**
     * Streams all items without any time-range filter.
     *
     * @return stream of all items
     */
    default Stream<T> fetchAll() {
        return fetch(new CalendarQuery());
    }

    /**
     * Refreshes all data. Fires a {@link CalendarItemsChangeEvent} to notify listeners.
     */
    void refreshAll();

    /**
     * Refreshes a single item. Fires a {@link CalendarItemRefreshEvent} to notify listeners.
     *
     * @param item the item to refresh
     */
    void refreshItem(T item);

    /**
     * Adds a listener that is notified when items change (e.g. due to a refresh).
     *
     * @param listener the listener
     * @return registration to remove the listener
     */
    Registration addItemsChangeListener(CalendarItemsChangeEvent.Listener<T> listener);

    /**
     * Adds a listener that is notified when a single item should be refreshed.
     *
     * @param listener the listener
     * @return registration to remove the listener
     */
    Registration addItemRefreshListener(CalendarItemRefreshEvent.Listener<T> listener);

    /**
     * Creates a callback-based provider from the given fetch functions.
     *
     * @param fetch     callback to fetch items based on the given query
     * @param fetchById callback to fetch a single item by ID (may return null)
     * @param <T>       the item type
     * @return a new callback provider
     */
    static <T> CallbackCalendarItemProvider<T> fromCallbacks(
            SerializableFunction<CalendarQuery, Stream<T>> fetch,
            SerializableFunction<String, T> fetchById) {
        return new CallbackCalendarItemProvider<>(fetch, fetchById);
    }

    /**
     * Creates an empty in-memory provider.
     *
     * @param idExtractor function to extract the ID from an item
     * @param <T>         the item type
     * @return a new empty in-memory provider
     */
    static <T> InMemoryCalendarItemProvider<T> emptyInMemory(SerializableFunction<T, String> idExtractor) {
        return new InMemoryCalendarItemProvider<>(idExtractor);
    }

    /**
     * Creates an in-memory provider pre-populated with the given items.
     *
     * @param idExtractor function to extract the ID from an item
     * @param items       initial items
     * @param <T>         the item type
     * @return a new in-memory provider with the given items
     */
    @SafeVarargs
    static <T> InMemoryCalendarItemProvider<T> inMemoryFrom(SerializableFunction<T, String> idExtractor, T... items) {
        return new InMemoryCalendarItemProvider<>(idExtractor, Arrays.asList(items));
    }

    /**
     * Creates an in-memory provider pre-populated with the given items.
     * The given iterable is not used as the backing collection; it will never be modified by this provider.
     *
     * @param idExtractor function to extract the ID from an item
     * @param items       initial items
     * @param <T>         the item type
     * @return a new in-memory provider with the given items
     */
    static <T> InMemoryCalendarItemProvider<T> inMemoryFrom(SerializableFunction<T, String> idExtractor, Iterable<T> items) {
        return new InMemoryCalendarItemProvider<>(idExtractor, items);
    }
}

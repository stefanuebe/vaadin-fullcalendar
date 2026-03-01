package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An in-memory implementation of {@link CalendarItemProvider} backed by a {@link LinkedHashMap}.
 * <p>
 * Uses a caller-provided {@code idExtractor} function to derive item IDs, since arbitrary POJOs
 * don't have a known {@code getId()} method.
 *
 * @param <T> the calendar item type
 * @author Stefan Uebe
 */
public class InMemoryCalendarItemProvider<T> extends AbstractCalendarItemProvider<T> {

    private final Map<String, T> itemsMap = new LinkedHashMap<>();
    private final SerializableFunction<T, String> idExtractor;

    /**
     * Creates an empty provider.
     *
     * @param idExtractor function to extract the ID from an item
     */
    public InMemoryCalendarItemProvider(SerializableFunction<T, String> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor, "idExtractor");
    }

    /**
     * Creates a provider pre-populated with the given items.
     *
     * @param idExtractor function to extract the ID from an item
     * @param items       initial items (the iterable itself is not retained)
     */
    public InMemoryCalendarItemProvider(SerializableFunction<T, String> idExtractor, Iterable<T> items) {
        this(idExtractor);
        addItems(items);
    }

    @Override
    public Stream<T> fetch(CalendarQuery query) {
        return itemsMap.values().stream();
    }

    @Override
    public Optional<T> fetchById(String id) {
        return Optional.ofNullable(itemsMap.get(id));
    }

    /**
     * Adds an item. Noop if an item with the same ID is already registered.
     *
     * @param item the item to add
     * @throws NullPointerException if item is null
     */
    public void addItem(T item) {
        Objects.requireNonNull(item, "item");
        String id = idExtractor.apply(item);
        if (!itemsMap.containsKey(id)) {
            itemsMap.put(id, item);
        }
    }

    /**
     * Adds multiple items. Noop for items whose IDs are already registered.
     *
     * @param items the items to add
     * @throws NullPointerException if items is null
     */
    public void addItems(Iterable<T> items) {
        Objects.requireNonNull(items, "items");
        items.forEach(this::addItem);
    }

    /**
     * Adds multiple items. Noop for items whose IDs are already registered.
     *
     * @param items the items to add
     */
    @SafeVarargs
    public final void addItems(T... items) {
        addItems(Arrays.asList(items));
    }

    /**
     * Removes an item by its ID. Noop if not registered.
     *
     * @param item the item to remove
     * @throws NullPointerException if item is null
     */
    public void removeItem(T item) {
        Objects.requireNonNull(item, "item");
        String id = idExtractor.apply(item);
        itemsMap.remove(id);
    }

    /**
     * Removes multiple items. Noop for items not registered.
     *
     * @param items the items to remove
     * @throws NullPointerException if items is null
     */
    public void removeItems(Iterable<T> items) {
        Objects.requireNonNull(items, "items");
        items.forEach(this::removeItem);
    }

    /**
     * Removes all items.
     */
    public void removeAllItems() {
        itemsMap.clear();
    }

    /**
     * Returns all items as a list.
     *
     * @return list of all items (in insertion order)
     */
    public List<T> getItems() {
        return fetchAll().collect(Collectors.toList());
    }

    /**
     * Returns a single item by its ID.
     *
     * @param id the item ID
     * @return optional containing the item, or empty
     */
    public Optional<T> getItemById(String id) {
        return fetchById(id);
    }
}

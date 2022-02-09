package org.vaadin.stefan.fullcalendar.dataprovider;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.Timezone;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A subtype of {@link EntryProvider} representing a provider, that keeps its entries in memory. It provides
 * additional api to modify the contained entry set (add / remove). Implementations may either use lazy loading
 * mechanism or push their items manually. Please also see the two reference implementations for direct use.
 *
 * @see LazyInMemoryEntryProvider
 * @see EagerInMemoryEntryProvider
 * @author Stefan Uebe
 */
public interface InMemoryEntryProvider<T extends Entry> extends EntryProvider<T> {

    /**
     * Creates a lazy loading instance. The given entries are used as initial items. Leave empty, if there
     * are no initial entries.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    @SafeVarargs
    static <T extends Entry> LazyInMemoryEntryProvider<T> lazyInstance(T... entries) {
        return new LazyInMemoryEntryProvider<>(Arrays.asList(entries));
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items, but the given iterable
     * is not used as the backing collection or similar. It will never be modified by this provider.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    static <T extends Entry> LazyInMemoryEntryProvider<T> lazyInstance(Iterable<T> entries) {
        return new LazyInMemoryEntryProvider<>(entries);
    }

    /**
     * Creates an eager loading instance. The given entries are used as initial items. Leave empty, if there
     * are no initial entries.
     * @param entries initial entries
     * @param <T> type
     * @return eager loading in memory provider
     */
    @SafeVarargs
    static <T extends Entry> EagerInMemoryEntryProvider<T> eagerInstance(T... entries) {
        return new EagerInMemoryEntryProvider<>(Arrays.asList(entries));
    }

    /**
     * Creates an eager loading instance. The given entries are used as initial items, but the given iterable
     * is not used as the backing collection or similar. It will never be modified by this provider.
     * @param entries initial entries
     * @param <T> type
     * @return eager loading in memory provider
     */
    static <T extends Entry> EagerInMemoryEntryProvider<T> eagerInstance(Iterable<T> entries) {
        return new EagerInMemoryEntryProvider<>(entries);
    }

    /**
     * Adds a list of entries to the calendar. Noop for already registered entries.
     *
     * @param iterableEntries list of entries
     * @throws NullPointerException when null is passed
     */
    void addEntries(@NotNull Iterable<T> iterableEntries);

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param iterableEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    void removeEntries(@NotNull Iterable<T> iterableEntries);

    /**
     * Returns all entries of this instance.
     * @return all entries
     */
    default List<T> getEntries() {
        return fetchAll().collect(Collectors.toList());
    }

    /**
     * Returns all entries, that lay inside or cross the given timespan.
     * @param filterStart start
     * @param filterEnd end
     * @return matching entries
     */
    default List<T> getEntries(LocalDateTime filterStart, LocalDateTime filterEnd) {
        return fetch(new EntryQuery(filterStart, filterEnd)).collect(Collectors.toList());
    }

    /**
     * Returns all entries, that lay inside or cross the given timespan.
     * @param filterStart start
     * @param filterEnd end
     * @return matching entries
     */
    default List<T> getEntries(Instant filterStart, Instant filterEnd) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(filterStart), Timezone.UTC.convertToLocalDateTime(filterEnd));
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date as a new list.
     * @param dateTime point of time to check
     * @return crossing entries
     */
    default List<T> getEntries(@NotNull Instant dateTime) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(dateTime));
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date as a new list.
     * @param date date to check
     * @return crossing entries
     */
    default List<T> getEntries(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        return getEntries(date.atStartOfDay());
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date as a new list.
     * @param dateTime point of time to check
     * @return crossing entries
     */
    default List<T> getEntries(@NotNull LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime);
        return getEntries(dateTime, dateTime.plusDays(1));
    }

    /**
     * Adds an entry to this calendar. Noop if the entry id is already registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    default void addEntry(@NotNull T entry) {
        Objects.requireNonNull(entry);
        addEntries(Collections.singletonList(entry));
    }
    /**
     * Adds an array of entries to the calendar. Noop for the entry id is already registered.
     *
     * @param arrayOfEntries array of entries
     * @throws NullPointerException when null is passed
     */
    @SuppressWarnings("unchecked")
    default void addEntries(@NotNull T... arrayOfEntries) {
        addEntries(Arrays.asList(arrayOfEntries));
    }



    /**
     * Removes the given entry. Noop if the id is not registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    default void removeEntry(@NotNull T entry) {
        Objects.requireNonNull(entry);
        removeEntries(Collections.singletonList(entry));
    }

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param arrayOfEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    default void removeEntries(@NotNull T... arrayOfEntries) {
        removeEntries(Arrays.asList(arrayOfEntries));
    }

    /**
     * Remove all entries.
     */
    default void removeAllEntries() {
        removeEntries(fetchAll().collect(Collectors.toList())); // prevent concurrent mod exception
    }

}

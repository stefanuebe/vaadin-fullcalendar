package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Timezone;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Basic abstract implementation of an in memory entry provider utilizing a hashmap.
 *
 * @author Stefan Uebe
 */
public class InMemoryEntryProvider<T extends Entry> extends AbstractEntryProvider<T> implements EntryProvider<T> {

    /**
     * Maps the entry ids to their respective entry instance. Any change to this map reflects directly
     * to this instance.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, T> entriesMap = new HashMap<>();

    public InMemoryEntryProvider() {
    }

    public InMemoryEntryProvider(Iterable<T> entries) {
        addEntries(entries);
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items. Leave empty, if there
     * are no initial entries.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    @SafeVarargs
    public static <T extends Entry> InMemoryEntryProvider<T> from(T... entries) {
        return new InMemoryEntryProvider<>(Arrays.asList(entries));
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items, but the given iterable
     * is not used as the backing collection or similar. It will never be modified by this provider.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    public static <T extends Entry> InMemoryEntryProvider<T> from(Iterable<T> entries) {
        return new InMemoryEntryProvider<>(entries);
    }

    /**
     * Connects this instance with the calendar. Not intended to be called manually, the FC will take care of this.
     * NOOP when called for the same calendar instance multiple times.
     *
     * @param calendar calendar to "connect" to.
     */
    @Override
    public void setCalendar(FullCalendar calendar) {
        FullCalendar oldCalendar = getCalendar();
        super.setCalendar(calendar);

        if (oldCalendar != calendar) {
            entriesMap.values().forEach(e -> e.setCalendar(calendar));
        }
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return query.applyFilter(entriesMap.values().stream());
    }

    @Override
    public Optional<T> fetchById(@NonNull String id) {
        return Optional.ofNullable(entriesMap.get(id));
    }

    /**
     * Adds a list of entries to the calendar. Noop for already registered entries.
     *
     * @param iterableEntries list of entries
     * @throws NullPointerException when null is passed
     */
    public void addEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);

        iterableEntries.forEach(entry -> {
            String id = entry.getId();

            if (!entriesMap.containsKey(id)) {
                entriesMap.put(id, entry);
                entry.setCalendar(getCalendar());
                onEntryAdd(entry);
            }
        });
    }

    protected void onEntryAdd(T entry) {

    }


    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param iterableEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    public void removeEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);

        iterableEntries.forEach(entry -> {
            String id = entry.getId();
            if (entriesMap.remove(id) != null) {
                entry.setCalendar(null);

                onEntryRemove(entry);
            }
        });
    }

    protected void onEntryRemove(T entry) {

    }

    /**
     * Updates the given entries on the client side. Ignores non-registered entries.
     *
     * @param iterableEntries entries to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);
        Map<String, T> entriesMap = getEntriesMap();
        StreamSupport.stream(iterableEntries.spliterator(), true)
                .filter(entry -> entriesMap.containsKey(entry.getId()) && entry.isKnownToTheClient())
                .forEach(this::onEntryUpdate);
    }

    public void onEntryUpdate(T entry) {

    }


    /**
     * Returns a single entry identified by the given id or an empty optional.
     * @param id id
     * @return optional entry or empty
     */
    public Optional<T> getEntryById(@NotNull String id) {
        return fetchById(id);
    }

    /**
     * Returns all entries of this instance.
     * @return all entries
     */
    public List<T> getEntries() {
        return fetchAll().collect(Collectors.toList());
    }

    /**
     * Returns all entries, that lay inside or cross the given timespan.
     * @param filterStart start
     * @param filterEnd end
     * @return matching entries
     */
    public List<T> getEntries(LocalDateTime filterStart, LocalDateTime filterEnd) {
        return fetch(new EntryQuery(filterStart, filterEnd)).collect(Collectors.toList());
    }

    /**
     * Returns all entries, that lay inside or cross the given timespan.
     * @param filterStart start
     * @param filterEnd end
     * @return matching entries
     */
    public List<T> getEntries(Instant filterStart, Instant filterEnd) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(filterStart), Timezone.UTC.convertToLocalDateTime(filterEnd));
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date as a new list.
     * @param dateTime point of time to check
     * @return crossing entries
     */
    public List<T> getEntries(@NotNull Instant dateTime) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(dateTime));
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date as a new list.
     * @param date date to check
     * @return crossing entries
     */
    public List<T> getEntries(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        return getEntries(date.atStartOfDay());
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date as a new list.
     * @param dateTime point of time to check
     * @return crossing entries
     */
    public List<T> getEntries(@NotNull LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime);
        return getEntries(dateTime, dateTime.plusDays(1));
    }

    /**
     * Adds an entry to this calendar. Noop if the entry id is already registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    public void addEntry(T entry) {
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
    public void addEntries(@NotNull T... arrayOfEntries) {
        addEntries(Arrays.asList(arrayOfEntries));
    }

    /**
     * Removes the given entry. Noop if the id is not registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    public void removeEntry(T entry) {
        Objects.requireNonNull(entry);
        removeEntries(Collections.singletonList(entry));
    }

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param arrayOfEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    public void removeEntries(@NotNull T... arrayOfEntries) {
        removeEntries(Arrays.asList(arrayOfEntries));
    }

    /**
     * Remove all entries.
     */
    public void removeAllEntries() {
        removeEntries(fetchAll().collect(Collectors.toList())); // prevent concurrent mod exception
    }
}

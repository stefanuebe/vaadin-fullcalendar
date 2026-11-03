package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.option.Timezone;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry provider that works on a collection in the server memory, like the {@code ListDataProvider} of
 * Vaadin. The provider uses the given collection itself, not a copy. After changing the collection, call
 * {@link #refreshAll()} so that the calendar shows the change. Before the calendar is shown for the first
 * time, no refresh is needed.
 * <p></p>
 * The provider looks up entries by their id in a map built from the collection. The constructor,
 * {@link #setItems(Collection)} and {@link #refreshAll()} mark this map as outdated, and the next
 * {@link #fetch(EntryQuery)} or {@link #fetchById(String)} builds it again. Two entries with the same id in
 * the collection make that call throw an {@link IllegalStateException}. {@link #refreshItem(Entry)} does not
 * rebuild the map, so an entry added to the collection after the last rebuild is not refreshed until
 * {@link #refreshAll()}. After adding, removing or replacing entries, call {@link #refreshAll()}.
 *
 * @author Stefan Uebe
 */
public class InMemoryEntryProvider<T extends Entry> extends AbstractEntryProvider<T> implements EntryProvider<T> {

    private Collection<T> items;

    /**
     * Maps the entry ids to the entries of {@link #items}. {@code null} when outdated, see {@link #getEntriesById()}.
     */
    private Map<String, T> entriesById;

    /**
     * Creates an instance with an empty, modifiable collection. Add entries with {@code getItems().add(entry)}
     * or replace the collection with {@link #setItems(Collection)}.
     */
    public InMemoryEntryProvider() {
        this(new ArrayList<>());
    }

    /**
     * Creates an instance that uses the given collection. The collection is not copied, see the class
     * documentation.
     *
     * @param items entries
     * @throws NullPointerException when null is passed
     */
    public InMemoryEntryProvider(@NonNull Collection<T> items) {
        this.items = items;
    }

    /**
     * Creates an instance with a new, modifiable collection holding the given entries.
     * @param entries initial entries
     * @param <T> type
     * @return in memory provider
     */
    @SafeVarargs
    public static <T extends Entry> InMemoryEntryProvider<T> from(@NonNull T... entries) {
        return new InMemoryEntryProvider<>(new ArrayList<>(Arrays.asList(entries)));
    }

    /**
     * Creates an instance that uses the given collection, see {@link #InMemoryEntryProvider(Collection)}.
     * @param entries entries
     * @param <T> type
     * @return in memory provider
     */
    public static <T extends Entry> InMemoryEntryProvider<T> from(@NonNull Collection<T> entries) {
        return new InMemoryEntryProvider<>(entries);
    }

    /**
     * Returns the collection this provider works on. It is the collection that was passed to the constructor
     * or to {@link #setItems(Collection)}, not a copy. Call {@link #refreshAll()} after changing it.
     *
     * @return the entries
     */
    public Collection<T> getItems() {
        return items;
    }

    /**
     * Replaces the collection this provider works on and refreshes the calendar. The collection is not copied,
     * see the class documentation.
     *
     * @param items entries
     * @throws NullPointerException when null is passed
     */
    public void setItems(@NonNull Collection<T> items) {
        this.items = items;
        refreshAll();
    }

    /**
     * Replaces the entries of this provider with a new, modifiable collection holding the given entries and
     * refreshes the calendar.
     *
     * @param items entries
     * @throws NullPointerException when null is passed
     */
    @SafeVarargs
    public final void setItems(@NonNull T... items) {
        setItems(new ArrayList<>(Arrays.asList(items)));
    }

    /**
     * Marks the id map as outdated and tells the calendar to fetch its entries again.
     */
    @Override
    public void refreshAll() {
        entriesById = null;
        super.refreshAll();
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return query.applyFilter(getEntriesById().values().stream());
    }

    @Override
    public Optional<T> fetchById(@NonNull String id) {
        return Optional.ofNullable(getEntriesById().get(id));
    }

    /**
     * Returns the id map, built from {@link #items} when it is outdated.
     */
    private Map<String, T> getEntriesById() {
        if (entriesById == null) {
            Map<String, T> map = new LinkedHashMap<>();
            for (T entry : items) {
                if (map.putIfAbsent(entry.getId(), entry) != null) {
                    throw new IllegalStateException("The entries of this provider contain the id '"
                            + entry.getId() + "' more than once. Entry ids must be unique.");
                }
            }
            entriesById = map;
        }

        return entriesById;
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
     * Returns all entries of this instance whose timespan crosses the given date as a new list.
     * @param dateTime point of time to check
     * @return crossing entries
     */
    public List<T> getEntries(Instant dateTime) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(dateTime));
    }

    /**
     * Returns all entries of this instance whose timespan crosses the given date as a new list.
     * @param date date to check
     * @return crossing entries
     */
    public List<T> getEntries(LocalDate date) {
        Objects.requireNonNull(date);
        return getEntries(date.atStartOfDay());
    }

    /**
     * Returns all entries of this instance whose timespan crosses the given date as a new list.
     * @param dateTime point of time to check
     * @return crossing entries
     */
    public List<T> getEntries(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime);
        return getEntries(dateTime, dateTime.plusDays(1));
    }

}

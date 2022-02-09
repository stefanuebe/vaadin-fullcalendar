package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An {@link EntryProvider} provides an API to fetch a list of entries based on filter parameters. It orientates
 * in its functionality on the Vaadin {@link com.vaadin.flow.data.provider.DataProvider}, but is based
 * on time spans instead of row counts.
 * @author Stefan Uebe
 */
public interface EntryProvider<T extends Entry> {

    /**
     * Creates a new instance that will fetch its content from the given callbacks. Passing null will lead to an exception.
     * @param fetchItems callback to fetch items based on the given query
     * @param fetchSingleItem callback to fetch a single item based on the given id
     * @param <T> type
     * @return callback entry provider
     */
    static <T extends Entry> CallbackEntryProvider<T> fromCallbacks(SerializableFunction<EntryQuery, Stream<T>> fetchItems, SerializableFunction<String, T> fetchSingleItem) {
        return new CallbackEntryProvider<>(fetchItems, fetchSingleItem);
    }

    /**
     * Creates a lazy loading instance with no initial entries.
     * @param <T> type
     * @return lazy loading in memory provider
     */
    static <T extends Entry> LazyInMemoryEntryProvider<T> lazyInMemory() {
        return InMemoryEntryProvider.lazyInstance();
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items. Leave empty, if there
     * are no initial entries.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    @SafeVarargs
    static <T extends Entry> LazyInMemoryEntryProvider<T> lazyInMemoryFromItems(T... entries) {
        return InMemoryEntryProvider.lazyInstance(entries);
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items, but the given iterable
     * is not used as the backing collection or similar. It will never be modified by this provider.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    static <T extends Entry> LazyInMemoryEntryProvider<T> lazyInMemoryFromItems(Iterable<T> entries) {
        return InMemoryEntryProvider.lazyInstance(entries);
    }

    /**
     * Creates an eager loading instance with no initial entries.
     * @param <T> type
     * @return eager loading in memory provider
     */
    static <T extends Entry> EagerInMemoryEntryProvider<T> eagerInMemory() {
        return InMemoryEntryProvider.eagerInstance();
    }

    /**
     * Creates an eager loading instance. The given entries are used as initial items. Leave empty, if there
     * are no initial entries.
     * @param entries initial entries
     * @param <T> type
     * @return eager loading in memory provider
     */
    @SafeVarargs
    static <T extends Entry> EagerInMemoryEntryProvider<T> eagerInMemoryFromItems(T... entries) {
        return InMemoryEntryProvider.eagerInstance(entries);
    }

    /**
     * Creates an eager loading instance. The given entries are used as initial items, but the given iterable
     * is not used as the backing collection or similar. It will never be modified by this provider.
     * @param entries initial entries
     * @param <T> type
     * @return eager loading in memory provider
     */
    static <T extends Entry> EagerInMemoryEntryProvider<T> eagerInMemoryFromItems(Iterable<T> entries) {
        return InMemoryEntryProvider.eagerInstance(entries);
    }

    /**
     * Streams all entries represented by this entry provider. Be careful as the produces amount
     * of entries may lead to memory or performance issues.
     * @return stream containing all entries
     */
    default Stream<T> fetchAll() {
        return fetch(new EntryQuery());
    }

    /**
     * Shortcut method for calling fetch with an EntryQuery. The two given dates will be used as
     * filter start and end (can be null).
     * @param start start
     * @param end end
     * @return stream containing matching entries
     */
    default Stream<T> fetch(LocalDateTime start, LocalDateTime end) {
        return fetch(new EntryQuery(start, end));
    }

    /**
     * Shortcut method for calling fetch with an EntryQuery. The two given dates will be used as
     * filter start and end (can be null).
     * @param start start
     * @param end end
     * @return stream containing matching entries
     */
    default Stream<T> fetch(Instant start, Instant end) {
        return fetch(new EntryQuery(start, end));
    }

    /**
     * Streams entries based on the given query. The query might be empty to fetch all available items.
     * @param query query
     * @return stream containing entries matching the query filter
     */
    Stream<T> fetch(@NonNull EntryQuery query);

    /**
     * Returns a single entry represented by the given id or an empty optional, if there is no entry
     * with this id.
     * @param id id
     * @return optional entry or empty
     */
    Optional<T> fetchById(@NonNull String id);

    /**
     * Refreshes a single item.
     * <p></p>
     * <i>Please note, that this functionality is currently not directly supported by the client side. Therefore,
     * in most cases calling this method will currently refetch all items of the currently shown interval.</i>
     */
    void refreshItem(T item);

    /**
     * Refreshes all data of this instance on the client. Depending on the implementation this can be
     * based on the current shown timespan or all data.
     */
    void refreshAll();

    /**
     * Registers the used calendar. This method will be called once automatically, when setting
     * this entry provider to the calendar. The given parameter might be stored internally, but that
     * is optionally. It is more a support method, if the calendar is necessary for internal usage.
     * @param calendar calendar
     */
    void setCalendar(FullCalendar calendar);

    /**
     * Indicates, if this instance is an in memory instance or not. By default checks, if this instance
     * implements {@link InMemoryEntryProvider}.
     * @return is in memory
     */
    default boolean isInMemory() {
        return this instanceof InMemoryEntryProvider;
    }

    /**
     * Adds a listener, that will be notified, when the entries are about to change (e.g. due to a refresh).
     * @param listener listener
     * @return registration to remove the listener
     */
    Registration addEntriesChangeListener(EntriesChangeEvent.EntriesChangeListener<T> listener);

    /**
     * Adds a listener, that will be notified, when a single entry is about to change (e.g. due to a refresh).
     * @param listener listener
     * @return registration to remove the listener
     */
    Registration addEntryRefreshListener(EntryRefreshEvent.EntryRefreshListener<T> listener);
}

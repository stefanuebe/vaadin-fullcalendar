package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.ResourceEntry;

import java.util.stream.Stream;

/**
 * A ResourceEntryProvider extends the normal {@link EntryProvider} API by a {@link #fetchResourceEntries(ResourceEntryQuery)} method,
 * which allows to fetch concrete entries for specific resources.
 * @param <T> entry type
 */
public interface ResourceEntryProvider<T extends ResourceEntry> extends EntryProvider<T> {

    /**
     * Creates a new instance that will fetch its content from the given callbacks. Passing null will lead to an exception.
     * @param fetchItems callback to fetch items based on the given query
     * @param fetchSingleItem callback to fetch a single item based on the given id
     * @param <T> type
     * @return callback entry provider
     */
    static <T extends ResourceEntry> CallbackResourceEntryProvider<T> fromCallbacks(SerializableFunction<ResourceEntryQuery, Stream<T>> fetchItems, SerializableFunction<String, T> fetchSingleItem) {
        return new CallbackResourceEntryProvider<>(fetchItems, fetchSingleItem);
    }

    /**
     * Creates a lazy loading instance with no initial entries.
     * @param <T> type
     * @return lazy loading in memory provider
     */
    static <T extends ResourceEntry> InMemoryResourceEntryProvider<T> emptyInMemory() {
        return InMemoryResourceEntryProvider.fromResourceEntries();
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items. Leave empty, if there
     * are no initial entries.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    @SafeVarargs
    static <T extends ResourceEntry> InMemoryResourceEntryProvider<T> inMemoryFrom(T... entries) {
        return InMemoryResourceEntryProvider.fromResourceEntries(entries);
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items, but the given iterable
     * is not used as the backing collection or similar. It will never be modified by this provider.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    static <T extends ResourceEntry> InMemoryResourceEntryProvider<T> inMemoryFrom(Iterable<T> entries) {
        return InMemoryResourceEntryProvider.fromResourceEntries(entries);
    }

    /**
     * Fetches the entries based on the given {@link ResourceEntryQuery}.
     * <p>
     *     Please note, that it is explicitly unspecified, how {@code null} for the {@code resource} field is handled.
     *     It is up to the implementation, if it returns all entries or only entries without an assigned resource.
     *     <br/>
     *     However, the default implemention of this method witll return all entries, when {@code resource == null}.
     * </p>
     * @param query resource entry query
     * @return matching resource entries
     */
    default Stream<T> fetchResourceEntries(@NonNull ResourceEntryQuery query) {
        Stream<T> stream = fetch(query);

        Resource resource = query.getResource();
        if (resource == null) {
            return stream;
        }

        return stream
                .filter(ResourceEntry::hasResources)
                .filter(e -> e.getResources().contains(resource));
    }


}
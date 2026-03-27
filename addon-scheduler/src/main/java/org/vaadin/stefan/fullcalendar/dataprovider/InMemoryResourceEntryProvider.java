package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.ResourceEntry;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * In memory implementation of the {@link ResourceEntryProvider} interface.
 * <p>
 *     Queries with {@code resource == null} will return ALL entries.
 * </p>
 * @param <T> resource entry type
 */
public class InMemoryResourceEntryProvider<T extends ResourceEntry>
        extends InMemoryEntryProvider<T>
        implements ResourceEntryProvider<T> {

    public InMemoryResourceEntryProvider(Iterable<T> entries) {
        super(entries);
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items. Leave empty, if there
     * are no initial entries.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    @SafeVarargs
    public static <T extends ResourceEntry> InMemoryResourceEntryProvider<T> fromResourceEntries(T... entries) {
        return fromResourceEntries(Arrays.asList(entries));
    }

    /**
     * Creates a lazy loading instance. The given entries are used as initial items, but the given iterable
     * is not used as the backing collection or similar. It will never be modified by this provider.
     * @param entries initial entries
     * @param <T> type
     * @return lazy loading in memory provider
     */
    public static <T extends ResourceEntry> InMemoryResourceEntryProvider<T> fromResourceEntries(Iterable<T> entries) {
        return new InMemoryResourceEntryProvider<>(entries);
    }

    /**
     * Fetches the entries based on the given {@link ResourceEntryQuery}.
     * <p>
     *     Queries with {@code resource == null} will return ALL entries.
     * </p>
     * @param query resource entry query
     * @return matching resource entries
     */
    @Override
    public Stream<T> fetchResourceEntries(@NonNull ResourceEntryQuery query) {
        // overridden for a more specific behavior description in the javadocs.
        return ResourceEntryProvider.super.fetchResourceEntries(query);
    }
}
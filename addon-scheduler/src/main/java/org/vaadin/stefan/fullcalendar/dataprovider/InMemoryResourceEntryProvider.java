package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.ResourceEntry;

import java.util.stream.Stream;

/**
 * In memory implementation of the {@link ResourceEntryProvider} interface.
 * <p>
 *     Queries with {@code resource == null} will return ALL entries.
 * </p>
 * @param <T> resource entry type
 * @param <R> resource type
 */
public class InMemoryResourceEntryProvider<T extends ResourceEntry>
        extends InMemoryEntryProvider<T>
        implements ResourceEntryProvider<T> {

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
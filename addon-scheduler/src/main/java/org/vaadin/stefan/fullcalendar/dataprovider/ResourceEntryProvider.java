package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.ResourceEntry;

import java.util.stream.Stream;

/**
 * A ResourceEntryProvider extends the normal {@link EntryProvider} API by a {@link #fetchResourceEntries(ResourceEntryQuery)} method,
 * which allows to fetch concrete entries for specific resources.
 * @param <T> entry type
 */
public interface ResourceEntryProvider<T extends ResourceEntry> extends EntryProvider<T> {

    /**
     * Fetches the entries based on the given {@link ResourceEntryQuery}.
     * <p>
     *     Please note, that it is explicitly unspecified, how {@code null} for the {@code resource} field is handled.
     *     It is up to the implementation, if it returns all entries or only entries without an assigned resource.
     * </p>
     * @param query resource entry query
     * @return matching resource entries
     */
    Stream<T> fetchResourceEntries(@NonNull ResourceEntryQuery query);
}
package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.ResourceEntry;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An {@link ResourceEntryProvider} using a predefined callback to fetch items when necessary.
 *
 * @author Stefan Uebe
 */
public class CallbackResourceEntryProvider<T extends ResourceEntry> extends AbstractEntryProvider<T> implements ResourceEntryProvider<T> {

    private final SerializableFunction<ResourceEntryQuery, Stream<T>> fetchItems;
    private final SerializableFunction<String, T> fetchSingleItem;

    public CallbackResourceEntryProvider(SerializableFunction<ResourceEntryQuery, Stream<T>> fetchItems, SerializableFunction<String, T> fetchSingleItem) {
        this.fetchItems = Objects.requireNonNull(fetchItems);
        this.fetchSingleItem = Objects.requireNonNull(fetchSingleItem);
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return fetchResourceEntries(new ResourceEntryQuery(null, query.getStart(), query.getEnd(), query.getAllDay()));
    }

    @Override
    public Optional<T> fetchById(@NonNull String id) {
        return Optional.ofNullable(fetchSingleItem.apply(id));
    }

    /**
     * Fetches the entries based on the given {@link ResourceEntryQuery}.
     * <p>
     *     It is up to the {@link #fetchItems} callback to decide, how entries with {@code resource == null} are handled.
     * </p>
     *
     * @param query resource entry query
     * @return matching resource entries
     */
    @Override
    public Stream<T> fetchResourceEntries(@NonNull ResourceEntryQuery query) {
        return fetchItems.apply(query);
    }
}

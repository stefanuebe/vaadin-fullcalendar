package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An {@link EntryProvider} using a predefined callback to fetch items when necessary.
 *
 * @author Stefan Uebe
 */
public class CallbackEntryProvider<T extends Entry> extends AbstractEntryProvider<T> {
    private final SerializableFunction<EntryQuery, Stream<T>> fetchItems;
    private final SerializableFunction<String, T> fetchSingleItem;

    public CallbackEntryProvider(@NotNull SerializableFunction<EntryQuery, Stream<T>> fetchItems, @NotNull SerializableFunction<String, T> fetchSingleItem) {
        this.fetchItems = Objects.requireNonNull(fetchItems);
        this.fetchSingleItem = Objects.requireNonNull(fetchSingleItem);
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return fetchItems.apply(query);
    }

    @Override
    public Optional<T> fetchById(@NonNull String id) {
        return Optional.ofNullable(fetchSingleItem.apply(id));
    }
}

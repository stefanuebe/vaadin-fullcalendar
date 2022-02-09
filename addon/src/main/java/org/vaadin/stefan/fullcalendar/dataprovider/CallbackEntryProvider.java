package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
public class CallbackEntryProvider<T extends Entry> extends AbstractEntryProvider<T>{
    private final SerializableFunction<EntryQuery, Stream<T>> fetchItems;

    public CallbackEntryProvider(@NotNull SerializableFunction<EntryQuery, Stream<T>> fetchItems) {
        this.fetchItems = Objects.requireNonNull(fetchItems);
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return fetchItems.apply(query);
    }

}

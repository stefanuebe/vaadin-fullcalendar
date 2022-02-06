package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.shared.Registration;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
public interface EntryProvider<T extends Entry> {

    default Stream<T> fetchAll() {
        return fetch(new EntryQuery());
    }

    Stream<T> fetch(@NonNull EntryQuery query);

    Optional<T> fetchById(@NonNull String id);

    /**
     * Refreshes the given item. This method should be used to inform all
     * {@link DataProviderListener DataProviderListeners} that an item has been
     * updated or replaced with a new instance.
     * <p>
     * For this to work properly, the item must either implement
     * {@link Object#equals(Object)} and {@link Object#hashCode()} to consider
     * both the old and the new item instances to be equal, or alternatively
     * {@link #getId(Object)} should be implemented to return an appropriate
     * identifier.
     *
     * @see #getId(Object)
     *
     * @param item
     *            the item to refresh
     */
    void refreshItem(T item);

    /**
     * Refreshes all data based on currently available data in the underlying
     * provider.
     */
    void refreshAll();

    /**
     * Creates a new data provider of the given collection. The collection is NOT used as backend reference,
     * as it is for instance in the {@link com.vaadin.flow.data.provider.ListDataProvider}
     *
     * @param <T>
     *            the data item type
     * @param items
     *            the collection of data, not <code>null</code>
     * @return a new list data provider
     */
    static <T extends Entry> InMemoryEntryProvider<T> ofCollection(Collection<T> items) {
        return new InMemoryEntryProvider<T>(items);
    }

    Registration addEntriesChangeListener(EntriesChangeEvent.EntriesChangeListener<T> listener);

    Registration addEntryRefreshListener(EntryRefreshEvent.EntryRefreshListener<T> listener);
}

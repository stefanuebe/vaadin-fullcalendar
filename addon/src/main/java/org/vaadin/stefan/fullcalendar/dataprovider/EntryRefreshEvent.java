package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.Getter;
import org.vaadin.stefan.fullcalendar.Entry;

import java.io.Serializable;
import java.util.EventObject;

/**
 * This event is fired, when a single item shall be refreshed.
 * @author Stefan Uebe
 */
@Getter
public class EntryRefreshEvent<T extends Entry> extends EventObject {
    private final T itemToRefresh;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public EntryRefreshEvent(EntryProvider<T> source, T itemToRefresh) {
        super(source);
        this.itemToRefresh = itemToRefresh;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntryProvider<T> getSource() {
        return (EntryProvider<T>) super.getSource();
    }

    @FunctionalInterface
    public interface EntryRefreshListener<T extends Entry> extends Serializable {
        void onDataRefresh(EntryRefreshEvent<T> event);
    }
}

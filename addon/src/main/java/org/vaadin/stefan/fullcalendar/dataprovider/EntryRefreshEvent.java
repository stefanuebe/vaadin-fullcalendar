package org.vaadin.stefan.fullcalendar.dataprovider;

import org.vaadin.stefan.fullcalendar.Entry;

import java.io.Serializable;

/**
 * This event is fired, when a single item shall be refreshed.
 * @author Stefan Uebe
 */
public class EntryRefreshEvent<T extends Entry> extends CalendarItemRefreshEvent<T> {

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public EntryRefreshEvent(EntryProvider<T> source, T itemToRefresh) {
        super(source, itemToRefresh);
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

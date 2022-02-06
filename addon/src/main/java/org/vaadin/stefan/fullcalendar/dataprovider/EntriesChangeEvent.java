package org.vaadin.stefan.fullcalendar.dataprovider;

import org.vaadin.stefan.fullcalendar.Entry;

import java.io.Serializable;
import java.util.EventObject;

/**
 * @author Stefan Uebe
 */
public class EntriesChangeEvent<T extends Entry> extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public EntriesChangeEvent(EntryProvider<T> source) {
        super(source);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntryProvider<T> getSource() {
        return (EntryProvider<T>) super.getSource();
    }

    @FunctionalInterface
    public static interface EntriesChangeListener<T extends Entry> extends Serializable {
        void onDataChange(EntriesChangeEvent<T> event);
    }

}

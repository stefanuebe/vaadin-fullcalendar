package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.Getter;
import org.vaadin.stefan.fullcalendar.Entry;

import java.io.Serializable;
import java.util.EventObject;

/**
 * @author Stefan Uebe
 */
public class FullCalendarDataChangeEvent<T extends Entry> extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public FullCalendarDataChangeEvent(FullCalendarDataProvider<T> source) {
        super(source);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FullCalendarDataProvider<T> getSource() {
        return (FullCalendarDataProvider<T>) super.getSource();
    }

    @FunctionalInterface
    public static interface FullCalendarDataChangeListener<T extends Entry> extends Serializable {
        void onDataChange(FullCalendarDataChangeEvent<T> event);
    }

}

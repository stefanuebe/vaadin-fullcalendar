package org.vaadin.stefan.fullcalendar.dataprovider;

import java.io.Serializable;
import java.util.EventObject;

/**
 * This event is fired when the items represented by a {@link CalendarItemProvider} are about to change
 * and a reaction (e.g. re-fetch) is necessary.
 *
 * @param <T> the calendar item type
 * @author Stefan Uebe
 */
public class CalendarItemsChangeEvent<T> extends EventObject {

    /**
     * Creates a new event.
     *
     * @param source the provider that changed
     * @throws IllegalArgumentException if source is null
     */
    public CalendarItemsChangeEvent(CalendarItemProvider<T> source) {
        super(source);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CalendarItemProvider<T> getSource() {
        return (CalendarItemProvider<T>) super.getSource();
    }

    /**
     * Listener for {@link CalendarItemsChangeEvent}.
     *
     * @param <T> the calendar item type
     */
    @FunctionalInterface
    public interface Listener<T> extends Serializable {
        void onDataChange(CalendarItemsChangeEvent<T> event);
    }
}

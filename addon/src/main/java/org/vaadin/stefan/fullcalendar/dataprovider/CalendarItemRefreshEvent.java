package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.Getter;

import java.io.Serializable;
import java.util.EventObject;

/**
 * This event is fired when a single item shall be refreshed.
 *
 * @param <T> the calendar item type
 * @author Stefan Uebe
 */
@Getter
public class CalendarItemRefreshEvent<T> extends EventObject {

    private final T itemToRefresh;

    /**
     * Creates a new event.
     *
     * @param source the provider that owns the item
     * @param itemToRefresh the item to refresh
     * @throws IllegalArgumentException if source is null
     */
    public CalendarItemRefreshEvent(CalendarItemProvider<T> source, T itemToRefresh) {
        super(source);
        this.itemToRefresh = itemToRefresh;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CalendarItemProvider<T> getSource() {
        return (CalendarItemProvider<T>) super.getSource();
    }

    /**
     * Listener for {@link CalendarItemRefreshEvent}.
     *
     * @param <T> the calendar item type
     */
    @FunctionalInterface
    public interface Listener<T> extends Serializable {
        void onDataRefresh(CalendarItemRefreshEvent<T> event);
    }
}

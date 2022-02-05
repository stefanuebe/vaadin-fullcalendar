package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.Getter;
import org.vaadin.stefan.fullcalendar.Entry;

import java.io.Serializable;

/**
 * @author Stefan Uebe
 */
@Getter
public class FullCalendarDataRefreshEvent<T extends Entry> extends FullCalendarDataChangeEvent<T> {
    private final T itemToRefresh;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public FullCalendarDataRefreshEvent(FullCalendarDataProvider<T> source, T itemToRefresh) {
        super(source);
        this.itemToRefresh = itemToRefresh;
    }


    @FunctionalInterface
    public interface FullCalendarDataRefreshListener<T extends Entry> extends Serializable {
        void onDataRefresh(FullCalendarDataChangeEvent<T> event);
    }
}

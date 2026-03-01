package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.EntriesChangeEvent.EntriesChangeListener;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryRefreshEvent.EntryRefreshListener;

/**
 * Abstract base implementation of the {@link EntryProvider} interface.
 * @author Stefan Uebe
 */
public abstract class AbstractEntryProvider<T extends Entry> extends AbstractCalendarItemProvider<T> implements EntryProvider<T> {

    @Getter
    private FullCalendar calendar;

    @Override
    public void refreshAll() {
        fireEvent(new EntriesChangeEvent<>(this));
    }

    @Override
    public void refreshItem(T item) {
        fireEvent(new EntryRefreshEvent<>(this, item));
    }

    @Override
    public Registration addEntriesChangeListener(EntriesChangeListener<T> listener) {
        return addListener(EntriesChangeEvent.class, listener::onDataChange);
    }

    @Override
    public Registration addEntryRefreshListener(EntryRefreshListener<T> listener) {
        return addListener(EntryRefreshEvent.class, listener::onDataRefresh);
    }

    /**
     * Sets the calendar. Throws an exception, when there is already a calendar set and it is not the
     * same instance as the given one.
     *
     * @param calendar calendar to set
     * @throws UnsupportedOperationException when setting another calendar
     */
    @Override
    public void setCalendar(FullCalendar calendar) {
        if (this.calendar != null && calendar != null && this.calendar != calendar) {
            throw new UnsupportedOperationException("Calendar must be set only once. Please create a new instance instead.");
        }

        this.calendar = calendar;
    }
}

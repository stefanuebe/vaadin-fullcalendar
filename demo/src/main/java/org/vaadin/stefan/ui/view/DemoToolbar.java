package org.vaadin.stefan.ui.view;

import org.vaadin.stefan.fullcalendar.CalendarView;

import java.time.LocalDate;

/**
 * Interface for demo toolbars that can be wired to calendar events.
 * Implemented by {@link CalendarViewToolbar} and {@link CalendarItemProviderToolbar}.
 * {@link AbstractDemoView} uses this interface to wire the toolbar to calendar listener callbacks.
 */
public interface DemoToolbar {

    /**
     * Updates the displayed date interval label.
     *
     * @param intervalStart the start of the currently visible interval
     */
    void updateInterval(LocalDate intervalStart);

    /**
     * Updates the selected view label/state.
     *
     * @param view the currently active calendar view
     */
    void updateSelectedView(CalendarView view);
}

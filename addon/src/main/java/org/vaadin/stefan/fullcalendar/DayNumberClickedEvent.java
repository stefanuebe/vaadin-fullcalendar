package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Occurs when number links are active and a user clicked on a day's number.
 */
@DomEvent("navLinkDayClick")
public class DayNumberClickedEvent extends DateTimeEvent {
    /**
     * New instance. Awaits the date (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param date       date instance as iso string
     * @param allDay     all day event
     */
    public DayNumberClickedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.date") String date, @EventData("event.detail.allDay") boolean allDay) {
        super(source, fromClient, date, allDay);
    }
}

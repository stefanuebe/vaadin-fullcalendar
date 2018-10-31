package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This event occurs when an empty timeslot in the calendar has clicked. Empty means the empty space itself, not that there
 * is not yet another entry located at this timeslot.
 * <p/>
 * Client side event: dayClick.
 */
@DomEvent("dayClick")
public class TimeslotClickedEvent extends DateTimeEvent {

    /**
     * New instance. Awaits the clicked date (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param date clicked time slot as iso string
     */
    public TimeslotClickedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.date") String date, @EventData("event.detail.allDay") boolean allDay) {
        super(source, fromClient, date, allDay);
    }
}

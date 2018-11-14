package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.time.Instant;

/**
 * Client side event: eventLimitClick.
 */
@DomEvent("eventLimitClick")
public class LimitedEntriesClickedEvent extends ComponentEvent<FullCalendar> {
    private final Instant clickedDate;

    /**
     * New instance. Awaits the clicked date as iso string (e.g. "2018-10-23").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param date       clicked time slot as iso string
     */
    public LimitedEntriesClickedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.date") String date) {
        super(source, fromClient);

        clickedDate = Instant.parse(date);
    }

    /**
     * Returns the clicked date.
     *
     * @return date
     */
    public Instant getClickedDate() {
        return clickedDate;
    }
}

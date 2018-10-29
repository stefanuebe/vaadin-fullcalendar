package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This event occurs when an empty slot in the calendar has clicked. Empty means the empty space itself, not that there
 * is not yet another entry located at this timeslot.
 */
@DomEvent("dayClick")
public class DayClickEvent extends ComponentEvent<FullCalendar> {

    private final boolean allDay;
    private LocalDateTime clickedDateTime;

    /**
     * New instance. Awaits the clicked date (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param date clicked time slot as iso string
     */
    public DayClickEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.date") String date, @EventData("event.detail.allDay") boolean allDay) {
        super(source, fromClient);

        this.allDay = allDay;
        if (allDay) {
            clickedDateTime = LocalDate.parse(date).atStartOfDay();
        } else {
            clickedDateTime = LocalDateTime.parse(date);
        }
    }

    /**
     * Returns the clicked date time. For day slots the time will be at start of the day.
     * @return date time
     */
    public LocalDateTime getClickedDateTime() {
        return clickedDateTime;
    }

    /**
     * Returns, if the click has been for a day slot. False means, it has been a click on a time slot inside a day.
     * @return all day click
     */
    public boolean isAllDay() {
        return allDay;
    }
}

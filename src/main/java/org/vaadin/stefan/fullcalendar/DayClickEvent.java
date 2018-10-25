package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * This event occurs when an empty slot in the calendar has clicked. Empty means the empty space itself, not that there
 * is not yet another entry located at this timeslot.
 */
@DomEvent("dayClick")
public class DayClickEvent extends ComponentEvent<FullCalendar> {

    private LocalDateTime clickedDateTime;
    private LocalDate clickedDate;

    /**
     * New instance. Awaits the clicked date (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param date clicked time slot as iso string
     */
    public DayClickEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.date") String date) {
        super(source, fromClient);

        try {
            clickedDateTime = LocalDateTime.parse(date);
        } catch (DateTimeParseException e) {
            clickedDate = LocalDate.parse(date);
        }
    }

    public Optional<LocalDate> getClickedDate() {
        return Optional.ofNullable(clickedDate);
    }

    public Optional<LocalDateTime> getClickedDateTime() {
        return Optional.ofNullable(clickedDateTime);
    }

    public boolean isTimeSlotEvent() {
        return clickedDateTime != null;
    }
}

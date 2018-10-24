package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@DomEvent("dayClick")
public class DayClickEvent extends ComponentEvent<FullCalendar> {

    private LocalDateTime clickedDateTime;
    private LocalDate clickedDate;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
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

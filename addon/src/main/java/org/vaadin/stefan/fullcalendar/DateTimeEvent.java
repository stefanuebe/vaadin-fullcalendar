package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.EventData;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An abstract class for date events, that are not directly entry related.
 */
public abstract class DateTimeEvent extends ComponentEvent<FullCalendar> {

    private final boolean allDay;
    private LocalDateTime dateTime;

    /**
     * New instance. Awaits the date (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param date       date instance as iso string
     * @param allDay     all day event
     */
    public DateTimeEvent(FullCalendar source, boolean fromClient, String date, boolean allDay) {
        super(source, fromClient);

        this.allDay = allDay;
        if (allDay) {
            dateTime = LocalDate.parse(date).atStartOfDay();
        } else {
            dateTime = LocalDateTime.parse(date);
        }
    }

    /**
     * Returns the date time. For day slots the time will be at start of the day.
     *
     * @return date time
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Returns, if the event has occurred for a day slot. False means, it has been for a time slot inside a day.
     *
     * @return all day event
     */
    public boolean isAllDay() {
        return allDay;
    }
}

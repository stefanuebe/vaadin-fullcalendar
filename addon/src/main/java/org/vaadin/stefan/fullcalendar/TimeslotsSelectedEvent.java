package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Occurs when the user selects one or multiple timeslots on the calendar. The selected timeslots may contain
 * entries.
 * <p/>
 * Client side event: select
 *
 */
@DomEvent("select")
public class TimeslotsSelectedEvent extends ComponentEvent<FullCalendar> {

    private final boolean allDay;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    /**
     * New instance. Awaits the selected dates (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param start start time slot as iso string
     * @param end end time slot as iso string
     */
    public TimeslotsSelectedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.start") String start, @EventData("event.detail.end") String end, @EventData("event.detail.allDay") boolean allDay) {
        super(source, fromClient);

        this.allDay = allDay;
        if (allDay) {
            startDateTime = LocalDate.parse(start).atStartOfDay();
            endDateTime = LocalDate.parse(end).atStartOfDay();
        } else {
            startDateTime = LocalDateTime.parse(start);
            endDateTime = LocalDateTime.parse(end);
        }
    }

    /**
     * Returns, if the selection has been for day slots. False means, it has been a selection on time slots inside a day.
     * @return all day click
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Returns the selected start date time. For day slots the time will be at start of the day.
     * @return date time
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Returns the selected end date time. For day slots the time will be at start of the day.
     * @return date time
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}

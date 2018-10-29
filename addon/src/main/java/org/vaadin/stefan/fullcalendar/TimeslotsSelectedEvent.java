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

    public boolean isAllDay() {
        return allDay;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}

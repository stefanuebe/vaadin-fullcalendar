package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

import java.time.LocalDateTime;

@DomEvent("eventDrop")
public class EntryDropEvent extends EntryDeltaEvent {

    private final boolean allDay;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public EntryDropEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id, @EventData("event.detail.delta") JsonObject jsonDelta, @EventData("event.detail.allDay") boolean allDay) {
        super(source, fromClient, id, jsonDelta);
        this.allDay = allDay;

        Entry entry = getEntry();
        Delta delta = getDelta();

        LocalDateTime start = entry.getStart();
        LocalDateTime end = entry.getEnd();

        boolean allDayBefore = entry.isAllDay();
        entry.setAllDay(allDay);
        entry.setStart(allDay ? delta.applyOnAndConvert(start).atStartOfDay() : delta.applyOn(start));
        entry.setEnd(allDay ? delta.applyOnAndConvert(end).atStartOfDay() : delta.applyOn(end));

        if (allDayBefore && !allDay) {
            // this handles the server side default timespan of an event that has been
            // an all day event before dragging and is now a timed event. client side will handle
            // that duration automatically.
            entry.setEnd(entry.getEnd().plusHours(FullCalendar.DEFAULT_TIMED_EVENT_DURATION));
        }

    }

    public boolean isAllDay() {
        return allDay;
    }
}

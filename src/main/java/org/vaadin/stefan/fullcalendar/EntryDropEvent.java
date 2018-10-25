package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

/**
 * Occurs when an entry time has changed by drag and drop.
 */
@DomEvent("eventDrop")
public class EntryDropEvent extends EntryTimeChangedEvent {

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryDropEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.data") JsonObject jsonEntry, @EventData("event.detail.delta") JsonObject jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);

//        Entry entry = getEntry();
//        Delta delta = getDelta();
//
//        LocalDateTime start = entry.getStart();
//        LocalDateTime end = entry.getEnd();
//
//        boolean allDayBefore = entry.isAllDay();
//        entry.setAllDay(allDay);
//
//        if (allDayBefore && !allDay) {
//            // this handles the server side default timespan of an event that has been
//            // an all day event before dragging and is now a timed event. client side will handle
//            // that duration automatically.
//            entry.setStart(delta.applyOn(start));
//            entry.setEnd(delta.applyOn(start).plusHours(FullCalendar.DEFAULT_TIMED_EVENT_DURATION));
//        } else if(!allDayBefore && allDay){
//            entry.setStart(delta.applyOnAndConvert(start).atStartOfDay());
//            entry.setEnd(delta.applyOnAndConvert(end).atStartOfDay());
//        } else {
//            entry.setStart(allDay ? delta.applyOnAndConvert(start).atStartOfDay() : delta.applyOn(start));
//            entry.setEnd(allDay ? delta.applyOnAndConvert(end).atStartOfDay() : delta.applyOn(end));
//        }
//
//        source.updateEntry(entry);

    }
}

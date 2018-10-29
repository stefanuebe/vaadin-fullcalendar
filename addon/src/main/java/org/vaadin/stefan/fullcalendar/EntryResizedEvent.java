package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

/**
 * Occurs when an entry time has changed by resizing.
 * <p/>
 * Client side name: eventResize
 */
@DomEvent("eventResize")
public class EntryResizedEvent extends EntryTimeChangedEvent {

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryResizedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.data") JsonObject jsonEntry, @EventData("event.detail.delta") JsonObject jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);

//        Entry entry = getEntry();
//        entry.setEnd(getDelta().applyOn(entry.getEnd()));

    }
}

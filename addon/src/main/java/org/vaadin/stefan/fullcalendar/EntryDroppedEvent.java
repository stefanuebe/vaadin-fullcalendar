package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

/**
 * Occurs when an entry time has changed by drag and drop.
 * <p/>
 * Client side name: eventDrop
 */
@DomEvent("eventDrop")
public class EntryDroppedEvent extends EntryTimeChangedEvent {

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryDroppedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.data") JsonObject jsonEntry, @EventData("event.detail.delta") JsonObject jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);
    }
}

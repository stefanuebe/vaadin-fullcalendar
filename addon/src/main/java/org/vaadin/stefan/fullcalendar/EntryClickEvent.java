package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

/**
 * Occurs when an entry has been clicked on the client side.
 */
@DomEvent("eventClick")
public class EntryClickEvent extends EntryEvent {

    /**
     * New instance. Awaits the id of the clicked entry.
     * @param source source component
     * @param fromClient from client
     * @param entryId affected entry id
     */
    public EntryClickEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String entryId) {
        super(source, fromClient, entryId);
    }

}

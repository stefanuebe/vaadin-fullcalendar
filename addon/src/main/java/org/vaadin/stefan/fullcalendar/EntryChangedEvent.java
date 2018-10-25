package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonObject;

/**
 * An event, that occur, when a entry has been changed on the client side. Returns the updated event.
 */
public class EntryChangedEvent extends EntryEvent {

    /**
     * New instance. Awaits the changed data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     */
    public EntryChangedEvent(FullCalendar source, boolean fromClient, JsonObject jsonEntry) {
        super(source, fromClient, jsonEntry.getString("id"));
        getEntry().update(jsonEntry);
    }
}

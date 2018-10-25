package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

/**
 * This specialized version of the entry changed event gives additional information about the changed time as
 * a delta instance. Please note that the entry data will be updated automatically. Also the delta gives only
 * information about the start time. The end time might have changed independently given by the client.
 */
public class EntryTimeChangedEvent extends EntryChangedEvent {
    private final Delta delta;

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryTimeChangedEvent(FullCalendar source, boolean fromClient, JsonObject jsonEntry, JsonObject jsonDelta) {
        super(source, fromClient, jsonEntry);
        this.delta = Delta.fromJson(jsonDelta);
    }

    /**
     * Returns the delta information. Please note, that the entry itself already is up-to-date, so there is no need
     * to apply the delta on it.
     * @return delta
     */
    public Delta getDelta() {
        return delta;
    }
}

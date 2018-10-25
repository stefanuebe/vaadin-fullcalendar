package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import elemental.json.JsonObject;

/**
 * Simple event that occurred for a specific calendar item.
 */
public class EntryEvent extends ComponentEvent<FullCalendar> {
    private final Entry entry;

    /**
     * New instance. Awaits the entry id.
     * @param source source component
     * @param fromClient from client
     * @param entryId affected entry id
     */
    public EntryEvent(FullCalendar source, boolean fromClient, String entryId) {
        super(source, fromClient);
        this.entry = source.getEntryById(entryId).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Returns the entry, for which the event occurred.
     * @return
     */
    public Entry getEntry() {
        return entry;
    }
}

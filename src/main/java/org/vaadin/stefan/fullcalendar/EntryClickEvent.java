package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("eventClick")
public class EntryClickEvent extends ComponentEvent<FullCalendar> {

    private final Entry entry;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public EntryClickEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id) {
        super(source, fromClient);
        this.entry = source.getEntryById(id).orElseThrow(IllegalArgumentException::new);
    }

    public Entry getEntry() {
        return entry;
    }
}

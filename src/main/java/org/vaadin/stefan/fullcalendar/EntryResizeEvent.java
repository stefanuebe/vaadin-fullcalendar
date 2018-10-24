package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

@DomEvent("eventResize")
public class EntryResizeEvent extends EntryDeltaEvent {

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public EntryResizeEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id, @EventData("event.detail.delta") JsonObject delta) {
        super(source, fromClient, id, delta);

        Entry entry = getEntry();
        entry.setEnd(getDelta().applyOn(entry.getEnd()));

    }
}

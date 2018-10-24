package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

@DomEvent("eventResize")
public class EntryResizeEvent extends ComponentEvent<FullCalendar> {

    private final Entry entry;
    private final Delta delta;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public EntryResizeEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id, @EventData("event.detail.delta") JsonObject delta) {
        super(source, fromClient);
        this.entry = source.getEntryById(id).orElseThrow(IllegalArgumentException::new);

        this.delta = Delta.fromJson(delta);
        entry.setEnd(this.delta.applyOn(entry.getEnd().orElseGet(entry::getStart)));
    }


    /**
     * Returns the modified event. The end date of this event has already been updated by the delta.
     * @return event
     */
    public Entry getEntry() {
        return entry;
    }

    public Delta getDelta() {
        return delta;
    }
}

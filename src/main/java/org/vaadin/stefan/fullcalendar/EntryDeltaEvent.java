package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

public class EntryDeltaEvent extends EntryEvent {
    private final Delta delta;

    public EntryDeltaEvent(FullCalendar source, boolean fromClient, String id, @EventData("event.detail.delta") JsonObject delta) {
        super(source, fromClient, id);
        this.delta = Delta.fromJson(delta);
    }

    public Delta getDelta() {
        return delta;
    }
}

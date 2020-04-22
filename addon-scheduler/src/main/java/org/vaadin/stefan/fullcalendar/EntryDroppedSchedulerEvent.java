package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import java.util.Optional;

@DomEvent("eventDrop")
public class EntryDroppedSchedulerEvent extends EntryTimeChangedEvent {
    private Resource oldResource;
    private Resource newResource;

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param jsonEntry  json object with changed data
     * @param jsonDelta  json object with delta information
     */
    public EntryDroppedSchedulerEvent(FullCalendarScheduler source, boolean fromClient,
                                      @EventData("event.detail.data") JsonObject jsonEntry,
                                      @EventData("event.detail.delta") JsonObject jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);

        Optional.<JsonValue>ofNullable(jsonEntry.get("oldResource"))
                .map(JsonValue::asString)
                .ifPresent(id -> this.oldResource = source.getResourceById(id).orElseThrow(IllegalArgumentException::new));

        Optional.<JsonValue>ofNullable(jsonEntry.get("newResource"))
                .map(JsonValue::asString)
                .ifPresent(id -> this.newResource = source.getResourceById(id).orElseThrow(IllegalArgumentException::new));

    }

    public Optional<Resource> getOldResource() {
        return Optional.ofNullable(oldResource);
    }

    public Optional<Resource> getNewResource() {
        return Optional.ofNullable(newResource);
    }
}

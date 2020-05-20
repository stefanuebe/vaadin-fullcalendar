package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;

import java.util.Optional;

@DomEvent("eventDrop")
public class EntryDroppedSchedulerEvent extends EntryTimeChangedEvent {

    private final Resource oldResource;
    private final Resource newResource;

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

        if(jsonEntry.hasKey("oldResource")) {
            this.oldResource = source.getResourceById(jsonEntry.getString("oldResource")).orElseThrow(IllegalArgumentException::new);
        } else {
            this.oldResource = null;
        }

        if(jsonEntry.hasKey("newResource")) {
            this.newResource = source.getResourceById(jsonEntry.getString("newResource")).orElseThrow(IllegalArgumentException::new);
        } else {
            this.newResource = null;
        }
    }

    /**
     * If there has been a change in the resource assignments, this method returns the previous assigned resource.
     * @return previous resource or empty
     */
    public Optional<Resource> getOldResource() {
        return Optional.ofNullable(oldResource);
    }

    /**
     * If there has been a change in the resource assignments, this method returns the newly assigned resource.
     * @return newly resource or empty
     */
    public Optional<Resource> getNewResource() {
        return Optional.ofNullable(newResource);
    }
}

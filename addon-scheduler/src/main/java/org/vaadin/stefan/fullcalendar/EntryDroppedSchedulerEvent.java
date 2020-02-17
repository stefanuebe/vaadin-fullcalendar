package org.vaadin.stefan.fullcalendar;

import java.util.Optional;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import elemental.json.JsonObject;

@DomEvent("eventDrop")
public class EntryDroppedSchedulerEvent extends EntryTimeChangedEvent {
	
	private Resource oldResource;
	private Resource newResource;
	
	/**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryDroppedSchedulerEvent(FullCalendarScheduler source, boolean fromClient, @EventData("event.detail.data") JsonObject jsonEntry, 
    		@EventData("event.detail.delta") JsonObject jsonDelta,
    		@EventData("event.detail.oldResource") String oldResourceId,
    		@EventData("event.detail.newResource") String newResourceId) {
        super(source, fromClient, jsonEntry, jsonDelta);
        if (oldResourceId != null) {
            this.oldResource = source.getResourceById(oldResourceId).orElseThrow(IllegalArgumentException::new);
        }
        if (newResourceId != null) {
            this.newResource = source.getResourceById(newResourceId).orElseThrow(IllegalArgumentException::new);
        }
    }
    
    public Optional<Resource> getOldResource() {
        return Optional.ofNullable(oldResource);
    }

    public Optional<Resource> getNewResource() {
        return Optional.ofNullable(newResource);
    }
}

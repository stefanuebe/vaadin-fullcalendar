package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import lombok.ToString;

import java.util.Collections;
import java.util.Optional;

@DomEvent("eventDrop")
@ToString(callSuper = true)
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
     * Applies the changes on the entry including updating a resource change.
     * @return this event's entry instance
     */
    @Override
    public Entry applyChangesOnEntry() {
        ResourceEntry entry = (ResourceEntry) super.applyChangesOnEntry();
        JsonObject object = getJsonObject();

        updateResourcesFromEventResourceDelta(entry, object);

        return entry;
    }

    public static void updateResourcesFromEventResourceDelta(ResourceEntry entry, JsonObject object) {
        entry.getCalendar().map(c -> (Scheduler) c).ifPresent(calendar -> {
            Optional.<JsonValue>ofNullable(object.get("oldResource"))
                    .filter(o -> o instanceof JsonString)
                    .map(JsonValue::asString)
                    .flatMap(calendar::getResourceById)
                    .map(Collections::singleton)
                    .ifPresent(entry::unassignResources);

            Optional.<JsonValue>ofNullable(object.get("newResource"))
                    .filter(o -> o instanceof JsonString)
                    .map(JsonValue::asString)
                    .flatMap(calendar::getResourceById)
                    .map(Collections::singleton)
                    .ifPresent(entry::assignResources);
        });
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

package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an entry that can be connected with a resource. Needed for timeline views.
 */
public class ResourceEntry extends Entry {

    private Set<Resource> resources;


    public ResourceEntry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        super(id, title, start, end, allDay, editable, color, description);
    }

    public ResourceEntry() {
    }

    public ResourceEntry(String id) {
        super(id);
    }

    protected void setCalendar(FullCalendar calendar) {
        if (!(calendar instanceof Scheduler)) {
            throw new IllegalArgumentException("ResourceEntries must be added to a FullCalendar that implements Scheduler");
        }
        super.setCalendar(calendar);
    }

    /**
     * Returns the first set resource. Is empty if no resource has been registered.
     *
     * @return
     */
    public Optional<Resource> getResource() {
        return Optional.ofNullable(resources != null && !resources.isEmpty() ? resources.iterator().next() : null);
    }

    /**
     * Sets a resource for this entry. Previously set resources will be removed. Setting null is the same
     * as calling {@link #removeAllResources()}
     *
     * @param resource resource
     */
    public void setResource(Resource resource) {
        if (resource == null) {
            removeAllResources();
        } else {
            if (this.resources == null) {
                this.resources = new HashSet<>(1);
            } else {
                this.resources.clear();
            }
            this.resources.add(resource);
        }
    }

    /**
     * Add multiple resources to this entry.
     *
     * @param resources resources
     */
    public void addResources(Collection<Resource> resources) {
        if (this.resources == null) {
            this.resources = new HashSet<>(resources.size());
        }
        this.resources.addAll(resources);
    }

    /**
     * Removes the given resources from this entry.
     *
     * @param resources resources
     */
    public void removeResources(Collection<Resource> resources) {
        if (this.resources != null) {
            this.resources.removeAll(resources);
        }
    }

    /**
     * Removes all resources from this entry.
     */
    public void removeAllResources() {
        if (this.resources != null) {
            this.resources.clear();
            this.resources = null;
        }
    }

    @Override
    protected JsonObject toJson() {
        JsonObject jsonObject = super.toJson();

        if (resources != null) {
            JsonArray array = Json.createArray();
            int i = 0;
            for (Resource r : resources) {
                array.set(i++, r.getId());
            }

            jsonObject.put("resourceIds", array);
        }

        return jsonObject;
    }

    @Override
    protected void update(JsonObject object) {
        super.update(object);

        Optional<Resource> optional = getResource();
        if (optional.isPresent()) {
            Resource resource = optional.get();
            String resourceId = object.getString("resourceId");
            if (!resource.getId().equals(resourceId)) {
                if (resourceId == null) {
                    setResource(null);
                } else {
                    getCalendar().ifPresent(c -> {
                        ((Scheduler) c).getResourceById(resourceId).ifPresent(newResource -> setResource(newResource));
                    });
                }
            }
        } else {
            setResource(null);
        }
    }
}

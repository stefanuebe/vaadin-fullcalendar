/*
 * Copyright 2018, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import elemental.json.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents an entry that can be connected with a resource. Needed for timeline views.
 */
public class ResourceEntry extends Entry {

    private Set<Resource> resources;
    private boolean resourceEditableOnClientSide;

    public ResourceEntry(String id, String title, Instant start, Instant end, boolean allDay, boolean editable, String color, String description) {
        super(id, title, start, end, allDay, editable, color, description);
    }

    public ResourceEntry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        super(id, title, start, end, allDay, editable, color, description);
    }

    public ResourceEntry(String id, String title, LocalDateTime start, LocalDateTime end, Timezone timezone, boolean allDay, boolean editable, String color, String description) {
        super(id, title, start, end, timezone, allDay, editable, color, description);
    }

    public ResourceEntry() {
    }

    public ResourceEntry(String id) {
        super(id);
    }

    protected void setCalendar(FullCalendar calendar) {
        if (calendar != null && !(calendar instanceof Scheduler)) {
            throw new IllegalArgumentException("ResourceEntries must be added to a FullCalendar that implements Scheduler");
        }
        super.setCalendar(calendar);
    }

    /**
     * Returns the first set resource. Is empty if no resource has been registered.
     *
     * @return resource
     */
    public Optional<Resource> getResource() {
        return Optional.ofNullable(resources != null && !resources.isEmpty() ? resources.iterator().next() : null);
    }

    /**
     * Sets a resource for this entry. Previously set resources will be removed. Setting null is the same
     * as calling {@link #removeAllResources()}.
     * <br><br>
     * Does not check, if the resources have been added somewhere else before
     * (for instance as children to other resources). May lead to corrupted data on the client side, when there
     * are hierarchical loops.
     *
     * @param resource resource
     */
    public void setResource(Resource resource) {
        if (resource == null) {
            removeAllResources();
        } else {
            if (this.resources == null) {
                this.resources = new LinkedHashSet<>(1);
            } else {
                this.resources.clear();
            }
            this.resources.add(resource);
        }
    }

    /**
     * Returns a copy of the entry's resources. Only contains the top level resources (means, child resources
     * have to be collected manually by using {@link Resource#getChildren()}.
     *
     * @return entry's resources (top level)
     */
    public Set<Resource> getResources() {
        return resources != null ? Collections.unmodifiableSet(resources) : Collections.emptySet();
    }

    /**
     * Returns the amount of assigned resources.
     * @return resources
     */
    public int getResourcesSize() {
        return resources != null ? resources.size() : 0;
    }

    /**
     * Returns, if the entry has any ressources assigned.
     * @return has resources
     */
    public boolean hasResources() {
        return resources != null && !resources.isEmpty();
    }

    /**
     * Add multiple resources to this entry. Does not check, if the resources have been added somewhere else before
     * (for instance as children to other resources). May lead to corrupted data on the client side, when there
     * are hierarchical loops.
     *
     * @param resources resources
     */
    public void addResources(Collection<Resource> resources) {
        if (this.resources == null) {
            this.resources = new LinkedHashSet<>(resources);
        } else {
            this.resources.addAll(resources);
        }
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

            if (getColor() == null && !resources.isEmpty()) {
                jsonObject.put("_hardReset", true);  // currently needed to make sure, that the color is
                                                                // set correctly. Might change in future, if not performant
            }
        }


        jsonObject.put("resourceEditable", resourceEditableOnClientSide);

        return jsonObject;
    }

    @Override
    protected void update(JsonObject object) {
        super.update(object);

        getCalendar().map(c -> (Scheduler) c).ifPresent(calendar -> {

            Optional.<JsonValue>ofNullable(object.get("oldResource"))
                    .filter(o -> o instanceof JsonString)
                    .map(JsonValue::asString)
                    .flatMap(calendar::getResourceById)
                    .map(Collections::singleton)
                    .ifPresent(this::removeResources);

            Optional.<JsonValue>ofNullable(object.get("newResource"))
                    .filter(o -> o instanceof JsonString)
                    .map(JsonValue::asString)
                    .flatMap(calendar::getResourceById)
                    .map(Collections::singleton)
                    .ifPresent(this::addResources);

        });
    }

    /**
     * Defines, if the user can move entries between resources (by using drag and drop). This value
     * is passed to the client side and interpreted there, but can also be used for server side checks.
     * <br><br>
     * This value has no impact on the resource API of this class.
     * @return resource is editable on client side
     */
    public boolean isResourceEditableOnClientSide() {
        return resourceEditableOnClientSide;
    }

    /**
     * Defines, if the user can move entries between resources (by using drag and drop). This value
     * is passed to the client side and interpreted there, but can also be used for server side checks.
     * <br><br>
     * This value has no impact on the resource API of this class.
     *
     * @param resourceEditableOnClientSide resource editable on client side
     */
    public void setResourceEditableOnClientSide(boolean resourceEditableOnClientSide) {
        this.resourceEditableOnClientSide = resourceEditableOnClientSide;
    }
}

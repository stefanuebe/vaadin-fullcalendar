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
     * @return
     */
    public Optional<Resource> getResource() {
        return Optional.ofNullable(resources != null && !resources.isEmpty() ? resources.iterator().next() : null);
    }

    public Set<Resource> getResources() {
        return resources != null ? Collections.unmodifiableSet(resources) : Collections.emptySet();
    }

    /**
     * Sets a resource for this entry. Previously set resources will be removed. Setting null is the same
     * as calling {@link #removeAllResources()}.
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

        getCalendar().map(c -> (Scheduler) c).ifPresent(calendar -> {
            JsonValue jsonValue = object.get("resourceIds");

            if (jsonValue instanceof JsonNull) {
                jsonValue = object.get("resourceId");
                if (jsonValue instanceof JsonNull) {
                    setResource(null);
                } else {
                    setResource(calendar.getResourceById(jsonValue.asString()).orElse(null));
                }
            } else {
                JsonArray resourceIds = (JsonArray) jsonValue;
                if (resourceIds != null) {
                    removeAllResources();

                    int length = resourceIds.length();
                    HashSet<Resource> set = new HashSet<>(length);

                    for (int i = 0; i < length; i++) {
                        String resourceId = resourceIds.getString(i);
                        calendar.getResourceById(resourceId).ifPresent(set::add);
                }

                    addResources(set);
            }
            }

        });
        }
}

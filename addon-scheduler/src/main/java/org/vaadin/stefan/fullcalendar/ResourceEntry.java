/*
 * Copyright 2020, Stefan Uebe
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

import elemental.json.JsonObject;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Represents an entry that can be connected with a resource. Needed for timeline views.
 */
public class ResourceEntry extends Entry {

    private static final Set<Key> KEYS = Key.readAndRegisterKeysAsUnmodifiable(ResourceEntryKey.class);

    /**
     * Creates a new entry with a generated id.
     */
    public ResourceEntry() {
        super();
        setResourceEditable(true);
    }

    /**
     * Creates a new entry with the given id. Null will lead to a generated id.
     * <br><br>
     * Please be aware, that the ID needs to be unique in the calendar instance. Otherwise it can lead to
     * unpredictable results.
     *
     * @param id id
     */
    public ResourceEntry(String id) {
        super(id);
        setResourceEditable(true);
    }

    @Override
    public Set<Key> getKeys() {
        return KEYS;
    }

    /**
     * Sets the calendar for this instance. The given calendar must be implementing Scheduler.
     *
     * @param calendar calendar instance
     * @throws IllegalArgumentException instance is not implementing {@link Scheduler}
     */
    @Override
    protected void setCalendar(FullCalendar calendar) {
        if (calendar != null && !(calendar instanceof Scheduler)) {
            throw new IllegalArgumentException("ResourceEntries must be added to a FullCalendar that implements Scheduler");
        }
        super.setCalendar(calendar);
    }

    /**
     * Indicates, if the user can move entries between resources (by using drag and drop). This value
     * is passed to the client side and interpreted there, but can also be used for server side checks.
     * <br><br>
     * This value has no impact on the resource API of this class.
     *
     */
    public boolean isResourceEditable() {
        return getBoolean(ResourceEntryKey.RESOURCE_EDITABLE, true);
    }

    /**
     * Defines, if the user can move entries between resources (by using drag and drop). This value
     * is passed to the client side and interpreted there, but can also be used for server side checks.
     * <br><br>
     * This value has no impact on the resource API of this class.
     */
    public void setResourceEditable(boolean resourceEditable) {
        set(ResourceEntryKey.RESOURCE_EDITABLE, resourceEditable);
    }

    /**
     * Returns an assigned resource. Is empty if no resource has been assigned yet. This method is mainly intended
     * to be used for entries where it is sure, that it only has one resource. For entries with multiple
     * resources it might be, that on the next call, the resource change (currently this class uses
     * a {@link LinkedHashSet}, but that might change in future).
     *
     * @return resource
     */
    public Optional<Resource> getResource() {
        return getResourcesOrEmpty().stream().findFirst();
    }

    /**
     * Returns an unmodifiable set of resources or an empty one, if no resources have been defined yet.
     *
     * @return unmodifiable set of resources
     */
    public Set<Resource> getResourcesOrEmpty() {
        Set<Resource> resources = getResources();
        return resources != null ? Collections.unmodifiableSet(resources) : Collections.emptySet();
    }

    /**
     * Returns a copy of the entry's assigned resources. Any changes to this set are reflected to the
     * backend and will be applied to the client on the next entry's update.
     * <p></p>
     * In earlier versions this set might have been unmodifiable. This is not the case anymore.
     *
     * @return entry's resources
     */
    public Set<Resource> getResources() {
        return getOrInit(ResourceEntryKey.RESOURCES, entry -> new LinkedHashSet<>());
    }

    /**
     * Returns the amount of assigned resources.
     *
     * @return resources
     */
    public int getResourcesSize() {
        return getResourcesOrEmpty().size();
    }

    /**
     * Returns, if the entry has any resources assigned.
     *
     * @return has resources
     */
    public boolean hasResources() {
        return !getResourcesOrEmpty().isEmpty();
    }

    /**
     * Assign an additional resource to this entry. Already assigned resources will be kept.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    public void assignResource(@NotNull Resource resource) {
        assignResources(Objects.requireNonNull(resource));
    }

    /**
     * Assign additional resources to this entry. Already assigned resources will be kept.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void assignResources(@NotNull Collection<Resource> resources) {
        Objects.requireNonNull(resources);
        getResources().addAll(resources);
    }

    /**
     * Assigns additional resources to this entry. Already assigned resources will be kept.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void assignResources(@NotNull Resource... resources) {
        assignResources(Arrays.asList(resources));
    }

    /**
     * Unassigns the given resource from this entry.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    public void unassignResource(@NotNull Resource resource) {
        unassignResources(Objects.requireNonNull(resource));
    }

    /**
     * Unassigns the given resources from this entry.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void unassignResources(@NotNull Resource... resources) {
        unassignResources(Arrays.asList(resources));
    }

    /**
     * Unassigns the given resources from this entry.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void unassignResources(@NotNull Collection<Resource> resources) {
        if (hasResources()) {
            getResources().removeAll(resources);
        }
    }

    /**
     * Unassigns all resources from this entry.
     */
    public void unassignAllResources() {
        remove(ResourceEntryKey.RESOURCES);
    }

    @Override
    protected void writeJsonOnUpdate(JsonObject jsonObject) {
        // Current issues with built in properties (therefore the special handlings of recurring and resources)
        // - https://github.com/fullcalendar/fullcalendar/issues/4393
        // - https://github.com/fullcalendar/fullcalendar/issues/5166
        // - https://github.com/fullcalendar/fullcalendar/issues/5262
        // Therefore this if will lead to a lot of "reset event", due to the fact, that resource editable
        // etc. might be set often.
        if (getColor() == null && hasResources() || isMarkedAsChangedProperty(ResourceEntryKey.RESOURCES) || isMarkedAsChangedProperty(ResourceEntryKey.RESOURCE_EDITABLE)) {
            // set correctly. Might change in future, if not performant
            super.writeJsonOnAdd(jsonObject);
            writeHardResetToJson(jsonObject);
        } else {
            super.writeJsonOnUpdate(jsonObject);
        }
    }

    /**
     * Applies resource change information from an {@link EntryDroppedSchedulerEvent}. This method
     * exists for backward compatibility (normaly update did this). Might be moved to the event in future.
     *
     * @param eventData event data
     * @deprecated try to not use this method (except for inside the {@link EntryDroppedSchedulerEvent}).
     */
    @Deprecated
    public void updateResourcesFromEvent(JsonObject eventData) {
        EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(this, eventData);
    }

    public static class ResourceEntryKey extends EntryKey {
        /**
         * Defines, if the user can move entries between resources (by using drag and drop). This value
         * is passed to the client side and interpreted there, but can also be used for server side checks.
         * <br><br>
         * This value has no impact on the resource API of this class.
         */
        public static final Key RESOURCE_EDITABLE = Key.builder()
                .name("resourceEditable")
                .defaultValue(true)
                .updateFromClientAllowed(false)
                .allowedType(Boolean.class)
                .build();

        public static final Key RESOURCES = Key.builder()
                .name("resourceIds")
                .allowedType(Set.class)
                .updateFromClientAllowed(true)
                .collectableItemConverter(item -> JsonUtils.toJsonValue(((Resource) item).getId()))
                .build();
    }
}

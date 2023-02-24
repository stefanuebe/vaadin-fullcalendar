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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converter.ResourceConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import org.vaadin.stefan.fullcalendar.json.JsonUpdateAllowed;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Stream;

/**
 * Represents an entry that can be connected with a resource. Needed for timeline views.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResourceEntry extends Entry {

    @SuppressWarnings("rawtypes")
    private static final Set PROPERTIES = BeanProperties.read(ResourceEntry.class);

    private boolean resourceEditable = true;

    @JsonUpdateAllowed
    @JsonName("resourceIds")
    @JsonConverter(ResourceConverter.class)
    private Set<Resource> resources;

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
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Stream<BeanProperties<Entry>> streamProperties() {
        return Stream.concat(super.streamProperties(), PROPERTIES.stream());
    }

    /**
     * Sets the calendar for this instance. The given calendar must be implementing Scheduler.
     *
     * @param calendar calendar instance
     * @throws IllegalArgumentException instance is not implementing {@link Scheduler}
     */
    @Override
    public void setCalendar(FullCalendar calendar) {
        if (calendar != null && !(calendar instanceof Scheduler)) {
            throw new IllegalArgumentException("ResourceEntries must be added to a FullCalendar that implements Scheduler");
        }
        super.setCalendar(calendar);
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
        Set<Resource> resources = getOrCreateResources();
        return resources != null ? Collections.unmodifiableSet(resources) : Collections.emptySet();
    }

    /**
     * Returns the entry's assigned resources. Any changes to this set are reflected to the
     * backend and will be applied to the client on the next entry's update.
     * <p></p>
     * In earlier versions this set might have been unmodifiable. This is not the case anymore.
     *
     * @return entry's resources
     */
    public Set<Resource> getOrCreateResources() {
        if (resources == null) {
            resources = new LinkedHashSet<>();
        }

        return resources;
    }

    @Nullable
    public Set<Resource> getResources() {
        return this.resources;
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
     * Assigns additional resources to this entry. Already assigned resources will be kept.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    @Deprecated
    public void assignResources(@NotNull Resource... resources) {
        addResources(resources);
    }

    /**
     * Assign an additional resource to this entry. Already assigned resources will be kept.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    @Deprecated
    public void assignResource(@NotNull Resource resource) {
        addResources(Objects.requireNonNull(resource));
    }

    /**
     * Assigns additional resources to this entry. Already assigned resources will be kept.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void addResources(@NotNull Resource... resources) {
        addResources(Arrays.asList(resources));
    }

    /**
     * Assign additional resources to this entry. Already assigned resources will be kept.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void addResources(@NotNull Collection<Resource> resources) {
        Objects.requireNonNull(resources);
        getOrCreateResources().addAll(resources);
    }

    /**
     * Unassigns the given resource from this entry.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    @Deprecated
    public void unassignResource(@NotNull Resource resource) {
        removeResources(Objects.requireNonNull(resource));
    }

    /**
     * Unassigns the given resources from this entry.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    @Deprecated
    public void unassignResources(@NotNull Resource... resources) {
        removeResources(resources);
    }

    /**
     * Unassigns the given resources from this entry.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    @Deprecated
    public void unassignResources(@NotNull Collection<Resource> resources) {
        removeResources(resources);
    }

    /**
     * Unassigns the given resources from this entry.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void removeResources(@NotNull Resource... resources) {
        removeResources(Arrays.asList(resources));
    }

    /**
     * Unassigns the given resources from this entry.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    public void removeResources(@NotNull Collection<Resource> resources) {
        if (hasResources()) {
            getOrCreateResources().removeAll(resources);
        }
    }

    /**
     * Unassigns all resources from this entry.
     */
    @Deprecated
    public void unassignAllResources() {
        removeAllResources();
    }

    /**
     * Unassigns all resources from this entry.
     */
    public void removeAllResources() {
        setResources(null);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        return json;
    }

    //    @Override
//    protected void toJson(JsonObject jsonObject) {

        // Current issues with built in properties (therefore the special handlings of recurring and resources)
        // - https://github.com/fullcalendar/fullcalendar/issues/4393
        // - https://github.com/fullcalendar/fullcalendar/issues/5166
        // - https://github.com/fullcalendar/fullcalendar/issues/5262
        // Therefore this if will lead to a lot of "reset event", due to the fact, that resource editable
        // etc. might be set often.
//        if (changedValuesOnly && (getColor() == null && hasResources() || isMarkedAsChangedProperty(ResourceEntryKey.RESOURCES) || isMarkedAsChangedProperty(ResourceEntryKey.RESOURCE_EDITABLE))) {
//            // set correctly. Might change in future, if not performant
//            super.toJson(jsonObject, false);
////            writeHardResetToJson(jsonObject);
//        } else {
//            super.toJson(jsonObject, changedValuesOnly);
//        }
//
//    }


}

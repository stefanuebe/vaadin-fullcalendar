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

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@NpmPackage(value = "@fullcalendar/resource-timeline", version = "^4.3.0")
@NpmPackage(value = "@fullcalendar/resource-timeline", version = "^4.3.0")
@NpmPackage(value = "@fullcalendar/resource-timegrid", version = "^4.3.0")
@Tag("full-calendar-scheduler")
@JsModule("./full-calendar-scheduler.js")
public class FullCalendarScheduler extends FullCalendar implements Scheduler {

    private Map<String, Resource> resources = new HashMap<>();

    public FullCalendarScheduler() {
        super();
    }

    public FullCalendarScheduler(int entryLimit) {
        super(entryLimit);
    }

    @Override
    public void setSchedulerLicenseKey(String schedulerLicenseKey) {
        setOption("schedulerLicenseKey", schedulerLicenseKey);
    }

    @Override
    public void addResource(Resource resource) {
        Objects.requireNonNull(resource);
        addResources(Collections.singletonList(resource));
    }

    @Override
    public void addResources(Resource... resources) {
        addResources(Arrays.asList(resources));
    }

    @Override
    public void addResources(Iterable<Resource> iterableResource) {
        Objects.requireNonNull(iterableResource);

        JsonArray array = Json.createArray();
        iterableResource.forEach(resource -> {
            String id = resource.getId();
            boolean containsKey = resources.containsKey(id);
            if (!containsKey) {
                resources.put(id, resource);
                array.set(array.length(), resource.toJson());
            }

        });

        getElement().callJsFunction("addResources", array);
    }

    @Override
    public void removeResource(Resource resource) {
        Objects.requireNonNull(resource);
        removeResources(Collections.singletonList(resource));
    }

    @Override
    public void removeResources(Resource... resources) {
        removeResources(Arrays.asList(resources));
    }

    @Override
    public void removeResources(Iterable<Resource> iterableResources) {
        Objects.requireNonNull(iterableResources);
        JsonArray array = Json.createArray();
        iterableResources.forEach(resource -> {
            String id = resource.getId();
            if (resources.containsKey(id)) {
                resources.remove(id);
                array.set(array.length(), resource.toJson());
            }
        });

        List<Resource> resources = StreamSupport.stream(iterableResources.spliterator(), false).collect(Collectors.toList());
        getEntries().stream().filter(e -> e instanceof ResourceEntry).forEach(e -> {
            ((ResourceEntry) e).removeResources(resources);
        });

        getElement().callJsFunction("removeResources", array);

    }

    @Override
    public Optional<Resource> getResourceById(@NotNull String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(resources.get(id));
    }

    @Override
    public Set<Resource> getResources() {
        return new HashSet<>(resources.values());
    }

    @Override
    public void removeAllResources() {
        removeResources(new HashSet<>(resources.values()));
    }

    /**
     * Set a grouping option for entries based on their assigned resource(s) and date.
     *
     * @param groupEntriesBy group entries by option
     */
    @Override
    public void setGroupEntriesBy(GroupEntriesBy groupEntriesBy) {
        switch (groupEntriesBy) {
            default:
            case NONE:
                setOption("groupByResource", false);
                setOption("groupByDateAndResource", false);
                break;
            case RESOURCE_DATE:
                setOption("groupByDateAndResource", false);
                setOption("groupByResource", true);
                break;
            case DATE_RESOURCE:
                setOption("groupByResource", false);
                setOption("groupByDateAndResource", true);
                break;
        }
    }

    /**
     * Registers a listener to be informed when the user selected a range of timeslots.
     * <br><br>
     * You should deactivate timeslot clicked listeners since both events will get fired when the user only selects
     * one timeslot / day.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    @Override
    public Registration addTimeslotsSelectedListener(@NotNull ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Registers a listener to be informed when a timeslot click event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotClickedListener(@NotNull ComponentEventListener<? extends TimeslotClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedSchedulerEvent.class, (ComponentEventListener) listener);
    }
}

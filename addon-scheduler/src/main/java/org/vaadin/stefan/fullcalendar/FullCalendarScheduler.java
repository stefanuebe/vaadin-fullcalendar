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

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

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
@NpmPackage(value = "@fullcalendar/resource", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/resource-timeline", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/resource-timegrid", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@Tag("full-calendar-scheduler")
@JsModule("./full-calendar/full-calendar-scheduler.ts")
public class FullCalendarScheduler extends FullCalendar implements Scheduler {

    /**
     * The scheduler base version used in this addon. Some additionl libraries might have a different version number due to
     * a different release cycle or known issues.
     */
    public static final String FC_SCHEDULER_CLIENT_VERSION = "6.0.0";
    private final Map<String, Resource> resources = new HashMap<>();

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefault()}).
     */
    public FullCalendarScheduler() {
        super();
    }

    /**
     * Creates a new instance.
     * <br><br>
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views. This value has to be set here and cannot be modified afterwards due to
     * technical reasons of FC. If set afterwards the entry limit would overwrite settings
     * and would show the limit also for basic views where it makes no sense (might change in future).
     * Passing a negative number or 0 disabled the entry limit (same as passing no number at all).
     * <br><br>
     * Sets the locale to {@link CalendarLocale#getDefault()}
     *
     *
     * @param entryLimit max entries to shown per day
     * @deprecated use the {@link FullCalendarBuilder#withEntryLimit(int)} instead
     */
    @Deprecated
    public FullCalendarScheduler(int entryLimit) {
        super(entryLimit);
    }

    /**
     * Creates a new instance with custom initial options. This allows a full override of the default
     * initial options, that the calendar would normally receive. Theoretically you can set all options,
     * as long as they are not based on a client side variable (as for instance "plugins" or "locales").
     * Complex objects are possible, too, for instance for view-specific settings.
     *  Please refer to the official FC documentation regarding potential options.
     * <br><br>
     * Client side event handlers, that are technically also a part of the options are still applied to
     * the options object. However you may set your own event handlers with the correct name. In that case
     * they will be taken into account instead of the default ones.
     * <br><br>
     * Plugins (key "plugins") will always be set on the client side (and thus override any key passed with this
     * object), since they are needed for a functional calendar. This may change in future. Same for locales
     * (key "locales").
     * <br><br>
     * Please be aware, that incorrect options or event handler overriding can lead to unpredictable errors,
     * which will NOT be supported in any case.
     * <br><br>
     * Also, options set this way are not cached in the server side state. Calling any of the
     * {@code getOption(...)} methods will result in {@code null} (or the respective native default).
     *
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     *
     * @param initialOptions initial options
     * @throws NullPointerException when null is passed
     */
    public FullCalendarScheduler(@NotNull JsonObject initialOptions) {
        super(initialOptions);
    }

    @Override
    public void setSchedulerLicenseKey(String schedulerLicenseKey) {
        setOption("schedulerLicenseKey", schedulerLicenseKey);
    }

    @Override
    public void setResourceAreaHeaderContent(String resourceAreaHeaderContent) {
        setOption("resourceAreaHeaderContent", resourceAreaHeaderContent);
    }
    
    @Override
    public void setResourceAreaWidth(String resourceAreaWidth) {
        setOption("resourceAreaWidth", resourceAreaWidth);
    }
    
    @Override
    public void setSlotMinWidth(String slotMinWidth) {
        setOption("slotMinWidth", slotMinWidth);
    }
    
    @Override
    public void setResourcesInitiallyExpanded(boolean resourcesInitiallyExpanded) {
        setOption("resourcesInitiallyExpanded", resourcesInitiallyExpanded);
    }
    
    @Override
    public void setFilterResourcesWithEvents(boolean filterResourcesWithEvents) {
        setOption("filterResourcesWithEvents", filterResourcesWithEvents);
    }

    @Override
    public void setResourceOrder(String resourceOrder) {
        setOption("resourceOrder", resourceOrder);
    }
    
    @Override
    public void setEntryResourceEditable(boolean eventResourceEditable) {
    	setOption("eventResourceEditable", eventResourceEditable);
    }

    @Deprecated
    @Override
    public void addResources(@NotNull Iterable<Resource> iterableResource) {
        Objects.requireNonNull(iterableResource);

        JsonArray array = Json.createArray();
        iterableResource.forEach(resource -> {
            String id = resource.getId();
            if (!resources.containsKey(id)) {
                resources.put(id, resource);
                array.set(array.length(), resource.toJson()); // this automatically sends sub resources to the client side
            }

            // now also register child resources
            registerResourcesInternally(resource.getChildren());
        });

        getElement().callJsFunction("addResources", array, true);
    }

    @Deprecated
    @Override
    public void addResources(@NotNull Iterable<Resource> iterableResource, boolean scrollToLast) {
        Objects.requireNonNull(iterableResource);

        JsonArray array = Json.createArray();
        iterableResource.forEach(resource -> {
            String id = resource.getId();
            if (!resources.containsKey(id)) {
                resources.put(id, resource);
                array.set(array.length(), resource.toJson()); // this automatically sends sub resources to the client side
            }

            // now also register child resources
            registerResourcesInternally(resource.getChildren());
        });
        getElement().callJsFunction("addResources", array, scrollToLast);
    }

    @Deprecated
    /**
     * Adds resources to the internal resources map. Does not update the client side. This method is mainly intended
     * to be used for child resources of registered resources, as the toJson method takes care for recursive child registration
     * on the client side, thus no separate call of toJson for children is needed.
     * @param resources resources
     */
    private void registerResourcesInternally(Collection<Resource> resources) {
        for (Resource resource : resources) {
            this.resources.put(resource.getId(), resource);
            registerResourcesInternally(resource.getChildren());
        }
    }

    @Deprecated
    @Override
    public void removeResources(@NotNull Iterable<Resource> iterableResources) {
        Objects.requireNonNull(iterableResources);

        removeFromEntries(iterableResources);

        // create registry of removed items to send to client
        JsonArray array = Json.createArray();
        iterableResources.forEach(resource -> {
            String id = resource.getId();
            if (this.resources.containsKey(id)) {
                this.resources.remove(id);
                array.set(array.length(), resource.toJson());
            }
        });
        
        getElement().callJsFunction("removeResources", array);

    }

    @Deprecated
    /**
     * Removes the given resources from the known entries of this calendar.
     * @param iterableResources resources
     */
    private void removeFromEntries(Iterable<Resource> iterableResources) {
        List<Resource> resources = StreamSupport.stream(iterableResources.spliterator(), false).collect(Collectors.toList());
        // TODO integrate in memory resource provider
//        getEntries().stream().filter(e -> e instanceof ResourceEntry).forEach(e -> ((ResourceEntry) e).unassignResources(resources));
    }

    @Deprecated
    @Override
    public Optional<Resource> getResourceById(@NotNull String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(resources.get(id));
    }

    @Deprecated
    @Override
    public Set<Resource> getResources() {
        return new LinkedHashSet<>(resources.values());
    }

    @Deprecated
    @Override
    public void removeAllResources() {
        removeFromEntries(resources.values());
    	resources.clear();
        getElement().callJsFunction("removeAllResources");
    }

    @Override
    public void setResourceLabelClassNamesCallback(String s) {
        getElement().callJsFunction("setResourceLabelClassNamesCallback", s);
    }
    
    @Override
    public void setResourceLabelContentCallback(String s) {
        getElement().callJsFunction("setResourceLabelContentCallback", s);
    }
    
    @Override
    public void setResourceLabelDidMountCallback(String s) {
        getElement().callJsFunction("setResourceLabelDidMountCallback", s);
    }
    
    @Override
    public void setResourceLablelWillUnmountCallback(String s) {
        getElement().callJsFunction("setResourceLablelWillUnmountCallback", s);
    }
    
    @Override
    public void setResourceLaneClassNamesCallback(String s) {
        getElement().callJsFunction("setResourceLaneClassNamesCallback", s);
    }
    
    @Override
    public void setResourceLaneContentCallback(String s) {
        getElement().callJsFunction("setResourceLaneContentCallback", s);
    }
    
    @Override
    public void setResourceLaneDidMountCallback(String s) {
        getElement().callJsFunction("setResourceLaneDidMountCallback", s);
    }
    
    @Override
    public void setResourceLaneWillUnmountCallback(String s) {
        getElement().callJsFunction("setResourceLaneWillUnmountCallback", s);
    }

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

    @Override
    public Registration addTimeslotsSelectedListener(@NotNull ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
        return addTimeslotsSelectedSchedulerListener((ComponentEventListener) listener);
    }

    @Override
    public Registration addTimeslotsSelectedSchedulerListener(@NotNull ComponentEventListener<? extends TimeslotsSelectedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    @Override
    public Registration addTimeslotClickedListener(@NotNull ComponentEventListener<? extends TimeslotClickedEvent> listener) {
        return addTimeslotClickedSchedulerListener((ComponentEventListener) listener);
    }

    @Override
    public Registration addTimeslotClickedSchedulerListener(@NotNull ComponentEventListener<? extends TimeslotClickedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    @Override
    public Registration addEntryDroppedSchedulerListener(@NotNull ComponentEventListener<? extends EntryDroppedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDroppedSchedulerEvent.class, (ComponentEventListener) listener);
    }
}

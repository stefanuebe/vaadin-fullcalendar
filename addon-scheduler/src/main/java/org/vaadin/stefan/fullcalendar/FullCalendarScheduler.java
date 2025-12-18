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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
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
@NpmPackage(value = "@fullcalendar/resource-daygrid", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/scrollgrid", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@JsModule("./vaadin-full-calendar/full-calendar-scheduler.ts")
@CssImport("./vaadin-full-calendar/full-calendar-scheduler-styles.css")

@Tag("vaadin-full-calendar-scheduler")
public class FullCalendarScheduler extends FullCalendar implements Scheduler {

    /**
     * The scheduler base version used in this addon. Some additionl libraries might have a different version number due to
     * a different release cycle or known issues.
     */
    public static final String FC_SCHEDULER_CLIENT_VERSION = "6.1.9";
    private final Map<String, Resource> resources = new HashMap<>();

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefaultLocale()}).
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
    @Deprecated(forRemoval = true)
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
    public FullCalendarScheduler(ObjectNode initialOptions) {
        super(initialOptions);
    }

    @Override
    public void setSchedulerLicenseKey(String schedulerLicenseKey) {
        setOption(SchedulerOption.LICENSE_KEY, schedulerLicenseKey);
    }

    @Override
    public void setResourceAreaHeaderContent(String resourceAreaHeaderContent) {
        setOption(SchedulerOption.RESOURCE_AREA_HEADER_CONTENT, resourceAreaHeaderContent);
    }
    
    @Override
    public void setResourceAreaWidth(String resourceAreaWidth) {
        setOption(SchedulerOption.RESOURCE_AREA_WIDTH, resourceAreaWidth);
    }
    
    @Override
    public void setSlotMinWidth(String slotMinWidth) {
        setOption(SchedulerOption.SLOT_MIN_WIDTH, slotMinWidth);
    }
    
    @Override
    public void setResourcesInitiallyExpanded(boolean resourcesInitiallyExpanded) {
        setOption(SchedulerOption.RESOURCES_INITIALLY_EXPANDED, resourcesInitiallyExpanded);
    }
    
    @Override
    public void setFilterResourcesWithEvents(boolean filterResourcesWithEvents) {
        setOption(SchedulerOption.FILTER_RESOURCES_WITH_ENTRIES, filterResourcesWithEvents);
    }

    @Override
    public void setResourceOrder(String resourceOrder) {
        setOption(SchedulerOption.RESOURCE_ORDER, resourceOrder);
    }
    
    @Override
    public void setEntryResourceEditable(boolean eventResourceEditable) {
    	setOption(SchedulerOption.ENTRY_RESOURCES_EDITABLE, eventResourceEditable);
    }

    @Override
    public void addResources(Iterable<Resource> iterableResource) {
        addResources(iterableResource, true);
    }

    @Override
    public void addResources(Iterable<Resource> iterableResource, boolean scrollToLast) {
        Objects.requireNonNull(iterableResource);

        ArrayNode array = JsonFactory.createArray();
        iterableResource.forEach(resource -> {
            String id = resource.getId();
            if (!resources.containsKey(id)) {
                resources.put(id, resource);
                array.add(resource.toJson()); // this automatically sends sub resources to the client side
        }

            // now also register child resources
            registerResourcesInternally(resource.getChildren());
        });
        getElement().callJsFunction("addResources", array, scrollToLast);
    }

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

    @Override
    public void removeResources(Iterable<Resource> iterableResources) {
        Objects.requireNonNull(iterableResources);

        removeFromEntries(iterableResources);

        // create registry of removed items to send to client
        ArrayNode array = JsonFactory.createArray();
        iterableResources.forEach(resource -> {
            String id = resource.getId();
            if (this.resources.containsKey(id)) {
                this.resources.remove(id);
                array.add(resource.toJson());
            }
        });

        getElement().callJsFunction("removeResources", array);

    }

    /**
     * Removes the given resources from the known entries of this calendar.
     * @param iterableResources resources
     */
    private void removeFromEntries(Iterable<Resource> iterableResources) {
        List<Resource> resources = StreamSupport.stream(iterableResources.spliterator(), false).collect(Collectors.toList());
        // TODO integrate in memory resource provider
        EntryProvider<Entry> entryProvider = getEntryProvider();
        if (entryProvider.isInMemory()) {
            entryProvider.asInMemory()
                    .getEntries()
                    .stream()
                    .filter(e -> e instanceof ResourceEntry)
                    .forEach(e -> ((ResourceEntry) e).removeResources(resources));
        }
    }

    @Override
    public Optional<Resource> getResourceById(String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(resources.get(id));
    }

    @Override
    public Set<Resource> getResources() {
        return new LinkedHashSet<>(resources.values());
    }

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
            case NONE -> {
                setOption(SchedulerOption.GROUP_BY_RESOURCE, false);
                setOption(SchedulerOption.GROUP_BY_DATE_AND_RESOURCE, false);
            }
            case RESOURCE_DATE -> {
                setOption(SchedulerOption.GROUP_BY_DATE_AND_RESOURCE, false);
                setOption(SchedulerOption.GROUP_BY_RESOURCE, true);
            }
            case DATE_RESOURCE -> {
                setOption(SchedulerOption.GROUP_BY_RESOURCE, false);
                setOption(SchedulerOption.GROUP_BY_DATE_AND_RESOURCE, true);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotsSelectedListener(ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
        return addTimeslotsSelectedSchedulerListener((ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotsSelectedSchedulerListener(ComponentEventListener<? extends TimeslotsSelectedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotClickedListener(ComponentEventListener<? extends TimeslotClickedEvent> listener) {
        return addTimeslotClickedSchedulerListener((ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotClickedSchedulerListener(ComponentEventListener<? extends TimeslotClickedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addEntryDroppedSchedulerListener(ComponentEventListener<? extends EntryDroppedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDroppedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(SchedulerOption option, Serializable value) {
        setOption(option, value, null);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(SchedulerOption)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <pre>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(SchedulerOption option, Serializable value, Object valueForServerSide) {
        setOption(option.getOptionKey(), value, valueForServerSide);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(SchedulerOption, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(SchedulerOption option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(SchedulerOption, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(SchedulerOption option, boolean forceClientSideValue) {
        return getOption(option.getOptionKey(), forceClientSideValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CalendarView> Optional<T> lookupViewByClientSideValue(String clientSideValue) {
        Optional<T> optional = super.lookupViewByClientSideValue(clientSideValue);
        if (optional.isEmpty()) {
            optional = (Optional<T>) SchedulerView.ofClientSideValue(clientSideValue);
        }
        return optional;
    }

    /**
     * Enumeration of possible scheduler options, that can be applied to the calendar.
     * Contains only options, that affect the client side library, but not internal options.
     * Also this list may not contain all options, but the most common used ones.
     * Any missing option can be set manually using one of the {@link FullCalendar#setOption} methods
     * using a string key.
     * <br><br>
     * Please refer to the FullCalendar client library documentation for possible options:
     * <a href="https://fullcalendar.io/docs">https://fullcalendar.io/docs</a>
     */
    public enum SchedulerOption {
        ENTRY_RESOURCES_EDITABLE("eventResourceEditable"),
        FILTER_RESOURCES_WITH_ENTRIES("filterResourcesWithEvents"),
        GROUP_BY_DATE_AND_RESOURCE("groupByDateAndResource"),
        GROUP_BY_RESOURCE("groupByResource"),
        LICENSE_KEY("schedulerLicenseKey"),
        REFETCH_RESOURCES_ON_NAVIGATE("refetchResourcesOnNavigate"),
        RESOURCE_AREA_HEADER_CONTENT("resourceAreaHeaderContent"),
        RESOURCE_AREA_WIDTH("resourceAreaWidth"),
        RESOURCES_INITIALLY_EXPANDED("resourcesInitiallyExpanded"),
        RESOURCE_ORDER("resourceOrder"),
        SLOT_MIN_WIDTH("slotMinWidth"),
        ;

        private final String optionKey;

        SchedulerOption(String optionKey) {
            this.optionKey = optionKey;
        }

        String getOptionKey() {
            return optionKey;
        }
    }
}

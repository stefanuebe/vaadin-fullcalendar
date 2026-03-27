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
import com.vaadin.flow.shared.Registration;

import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents functionality for the FullCalendarScheduler.
 */
public interface Scheduler {

    /**
     * The non-commercial Creative Commons license key for development and evaluation.
     * While developing, use this key to hide the license warning. This is the same value
     * as {@link #NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY} and represents FullCalendar's
     * recommended approach for non-commercial development and testing. It is <b>not</b> a special
     * "development-only" key that suppresses warnings without a valid license — it IS a valid
     * non-commercial license key.
     * <br><br>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String DEVELOPER_LICENSE_KEY = "CC-Attribution-NonCommercial-NoDerivatives";

    /**
     * Constant for the non-commercial creative commons license.
     * <br><br>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY = "CC-Attribution-NonCommercial-NoDerivatives";
    
    /**
     * 
     * Constant for the GPL v3 open source license.
     * <br><br>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String GPL_V3_LICENSE_KEY = "GPL-My-Project-Is-Open-Source";

    /**
     * Sets the license key to be used for the scheduler. For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     * @param schedulerLicenseKey license key
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#LICENSE_KEY} instead.
     */
    @Deprecated
    void setSchedulerLicenseKey(String schedulerLicenseKey);

    /**
     * Sets the content for the resource area header cell — the top-left cell in timeline views
     * that appears above the resource list. Accepts a plain text string or HTML string.
     * For dynamic content (JavaScript function), use the raw {@code setOption("resourceAreaHeaderContent", fn)} method.
     *
     * @param resourceAreaHeaderContent plain text or HTML content for the resource area header
     * @see <a href="https://fullcalendar.io/docs/resource-area-header-render-hooks">FullCalendar resourceAreaHeaderContent</a>
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_AREA_HEADER_CONTENT} instead.
     */
    @Deprecated
    void setResourceAreaHeaderContent(String resourceAreaHeaderContent);

    /**
     * Determines the width of the area that contains the list of resources.
     * Can be specified as a number of pixels, or a CSS string value, like "25%".
     * @param resourceAreaWidth
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_AREA_WIDTH} instead.
     */
    @Deprecated
    void setResourceAreaWidth(String resourceAreaWidth);
    
    /**
     * Determines how wide each of the time-axis slots will be. Specified as a number of pixels.
     * When not specified, a reasonable value will be automatically computed.
     * @param slotMinWidth
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#SLOT_MIN_WIDTH} instead.
     */
    @Deprecated
    void setSlotMinWidth(String slotMinWidth);
    
    /**
     * Whether child resources should be expanded when the view loads.
     * By default, all child resources are visible, but if you’d like child resources to be collapsed,
     * meaning they are not initially visible, change this setting to false.
     *
     * Only supported in Timeline view.
     *
     * @param resourcesInitiallyExpanded
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCES_INITIALLY_EXPANDED} instead.
     */
    @Deprecated
    void setResourcesInitiallyExpanded(boolean resourcesInitiallyExpanded);
    
    /**
     * When this setting is activated, only resources that have associated entries will be displayed.
     * When activated, please be aware that in order for resources to render, entry data will need to finish being fetched.
     * @param filterResourcesWithEvents
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#FILTER_RESOURCES_WITH_ENTRIES} instead.
     */
    @Deprecated
    void setFilterResourcesWithEvents(boolean filterResourcesWithEvents);
    
    /**
     * Determines the ordering of the resource list.
     * If prefixed with a minus sign like '-propertyName', the ordering will be descending.
     * If no resourceOrder is given (the default), resources will be ordered by their id, then by title.
     * @param resourceOrder
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_ORDER} instead.
     */
    @Deprecated
    void setResourceOrder(String resourceOrder);
    
    /**
     * Determines whether the user can drag entries between resources.
     * The default value is inherited from the master editable flag, which is false by default.
     * @param eventResourceEditable
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#ENTRY_RESOURCES_EDITABLE} instead.
     */
    @Deprecated
    void setEntryResourceEditable(boolean eventResourceEditable);

    /**
     * Adds a resource to this calendar. Does nothing if the resource ID is already registered.
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    default void addResource(Resource resource, boolean scrollToLast) {
        Objects.requireNonNull(resource);
        addResources(Collections.singletonList(resource), scrollToLast);
    }
    default void addResource(Resource resource) {
        Objects.requireNonNull(resource);
        addResources(Collections.singletonList(resource));
    }

    /**
     * Adds resources to this calendar. Does nothing for already registered resources.
     *
     * @param resources resources to add
     * @throws NullPointerException when null is passed
     */
    default void addResources(boolean scrollToLast, Resource... resources) {
        addResources(Arrays.asList(resources), scrollToLast);
    }
    default void addResources(Resource... resources) {
        addResources(Arrays.asList(resources));
    }

    /**
     * Adds resources to this calendar. Does nothing for already registered resources.
     *
     * @param resources resources to add
     * @throws NullPointerException when null is passed
     */
    void addResources(Iterable<Resource> resources, boolean scrollToLast);
    void addResources(Iterable<Resource> resources);

    /**
     * Removes the given resource. Also removes it from its related entries.
     * Does not send an extra update for the entries.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    default void removeResource(Resource resource) {
        Objects.requireNonNull(resource);
        removeResources(Collections.singletonList(resource));
    }

    /**
     * Removes the given resources. Also removes them from their related entries.
     * Does not send an extra update for the entries.
     * <br><br>
     * Does nothing for unregistered resources.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    default void removeResources(Resource... resources) {
        removeResources(Arrays.asList(resources));
    }
 /**
     * Removes the given resources.  Also removes them from their related entries.
     * Does not send an extra update for the entries.
     * <br><br>
     * Does nothing for unregistered resources.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    void removeResources(Iterable<Resource> resources);

    /**
     * Returns the resource with the given id. Is empty when the id is not registered.
     *
     * @param id id
     * @return resource or empty
     * @throws NullPointerException when null is passed
     */
    Optional<Resource> getResourceById(String id);

    /**
     * Returns all resources registered in this instance, including child resources. Changes in a resource instance are reflected in the
     * calendar instance on server side, but not client side. Resources can currently not be updated on the client side.
     * <br><br>
     * Changes in the list are not reflected to the calendar's list instance. Also please note, that the content
     * of the list is <b>unsorted</b> and may vary with each call. The return of a list is due to presenting
     * a convenient way of using the returned values without the need to encapsulate them yourselves.
     *
     * @return resources resources
     */
    Set<Resource> getResources();

    /**
     * Returns all top level resources registered in this instance (having no parent). This list is calculated on each
     * call to reflect the latest state of possible changes in the resources' structure. If you need it multiple times
     * in a row and can be sure, that there haven't been changes, you should cache the list for that short time.
     * <br><br>
     * Changes in a resource instance are reflected in the
     * calendar instance on server side, but not client side. Resources can currently not be updated on the client side.
     * <br><br>
     * Changes in the list are not reflected to the calendar's list instance. Also please note, that the content
     * of the list is <b>unsorted</b> and may vary with each call. The return of a list is due to presenting
     * a convenient way of using the returned values without the need to encapsulate them yourselves.
     *
     * @return resources resources
     */
    default Set<Resource> getTopLevelResources() {
        return getResources().stream().filter(r -> !r.getParent().isPresent()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Removes all registered resources from this instance. Also removes them from their related entries.
     * Does not send an extra update for the entries.
     */
    void removeAllResources();

    /**
     * Sets a JavaScript callback for resource label class names.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LABEL_CLASS_NAMES} instead.
     */
    @Deprecated
    void setResourceLabelClassNamesCallback(String s);

    /**
     * Sets a JavaScript callback for resource label content.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LABEL_CONTENT} instead.
     */
    @Deprecated
    void setResourceLabelContentCallback(String s);

    /**
     * Sets a JavaScript callback for resource label didMount.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LABEL_DID_MOUNT} instead.
     */
    @Deprecated
    void setResourceLabelDidMountCallback(String s);

    /**
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LABEL_WILL_UNMOUNT} instead (also fixes the typo).
     */
    @Deprecated
    void setResourceLablelWillUnmountCallback(String s);

    /**
     * Sets a JavaScript callback for resource lane class names.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LANE_CLASS_NAMES} instead.
     */
    @Deprecated
    void setResourceLaneClassNamesCallback(String s);

    /**
     * Sets a JavaScript callback for resource lane content.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LANE_CONTENT} instead.
     */
    @Deprecated
    void setResourceLaneContentCallback(String s);

    /**
     * Sets a JavaScript callback for resource lane didMount.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LANE_DID_MOUNT} instead.
     */
    @Deprecated
    void setResourceLaneDidMountCallback(String s);

    /**
     * Sets a JavaScript callback for resource lane willUnmount.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link FullCalendarScheduler#setOption(FullCalendarScheduler.SchedulerOption, Object)}
     *             with {@link FullCalendarScheduler.SchedulerOption#RESOURCE_LANE_WILL_UNMOUNT} instead.
     */
    @Deprecated
    void setResourceLaneWillUnmountCallback(String s);

    /**
     * Returns an optional option value for the given scheduler option.
     *
     * @param option scheduler option
     * @param <T> type of value
     * @return optional value or empty
     * @see FullCalendarScheduler#getOption(FullCalendarScheduler.SchedulerOption)
     */
    <T> Optional<T> getOption(FullCalendarScheduler.SchedulerOption option);
    
    /**
     * Set a grouping option for entries based on their assigned resource(s) and date.
     *
     * @param groupEntriesBy group entries by option
     */
    void setGroupEntriesBy(GroupEntriesBy groupEntriesBy);

    /**
     * Configures the resource area as a multi-column data grid. Each column maps to a resource property.
     * When set, FC renders a header row with column titles above the resource list.
     * <p>
     * Example:
     * <pre>{@code
     * scheduler.setResourceAreaColumns(List.of(
     *     new ResourceAreaColumn("title", "Resource").withWidth("200px"),
     *     new ResourceAreaColumn("department", "Department").withWidth("150px").withGroup(true)
     * ));
     * }</pre>
     *
     * @param columns list of column definitions; must not be null
     * @see ResourceAreaColumn
     * @see <a href="https://fullcalendar.io/docs/resourceAreaColumns">FullCalendar resourceAreaColumns</a>
     */
    void setResourceAreaColumns(List<ResourceAreaColumn> columns);

    /**
     * Convenience overload for {@link #setResourceAreaColumns(List)}.
     *
     * @param columns column definitions
     */
    default void setResourceAreaColumns(ResourceAreaColumn... columns) {
        setResourceAreaColumns(Arrays.asList(columns));
    }


    /**
     * Propagates a server-side resource change to the client. Call this after modifying
     * resource properties to keep the display in sync.
     * <p>
     * Note: This is called automatically when using {@link Resource#setTitle(String)} or
     * {@link Resource#setColor(String)} on a resource that has been added to this scheduler.
     *
     * @param resource the resource to update on the client side; must not be null
     */
    void updateResource(Resource resource);

    /**
     * Registers a listener to be informed when an entry dropped event occurred, along with scheduler
     * specific data.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    Registration addEntryDroppedSchedulerListener(ComponentEventListener<? extends EntryDroppedSchedulerEvent> listener);

    /**
     * Registers a listener to be informed when a timeslot has been clicked, including scheduler specific data.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    Registration addTimeslotClickedSchedulerListener(ComponentEventListener<? extends TimeslotClickedSchedulerEvent> listener);

    /**
     * Registers a listener to be informed when a timespan has been selected, including scheduler specific data.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    Registration addTimeslotsSelectedSchedulerListener(ComponentEventListener<? extends TimeslotsSelectedSchedulerEvent> listener);
}

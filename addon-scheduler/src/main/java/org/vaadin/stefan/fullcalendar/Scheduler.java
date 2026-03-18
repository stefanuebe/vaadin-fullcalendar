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
     */
    void setSchedulerLicenseKey(String schedulerLicenseKey);

    /**
     * Sets the content for the resource area header cell — the top-left cell in timeline views
     * that appears above the resource list. Accepts a plain text string or HTML string.
     * For dynamic content (JavaScript function), use the raw {@code setOption("resourceAreaHeaderContent", fn)} method.
     *
     * @param resourceAreaHeaderContent plain text or HTML content for the resource area header
     * @see #setResourceAreaHeaderClassNamesCallback(String)
     * @see #setResourceAreaHeaderDidMountCallback(String)
     * @see <a href="https://fullcalendar.io/docs/resourceAreaHeaderContent">FullCalendar resourceAreaHeaderContent</a>
     */
    void setResourceAreaHeaderContent(String resourceAreaHeaderContent);

    /**
     * Determines the width of the area that contains the list of resources.
     * Can be specified as a number of pixels, or a CSS string value, like "25%".
     * @param resourceAreaWidth
     */
    void setResourceAreaWidth(String resourceAreaWidth);
    
    /**
     * Determines how wide each of the time-axis slots will be. Specified as a number of pixels.
     * When not specified, a reasonable value will be automatically computed.
     * @param slotMinWidth
     */
    void setSlotMinWidth(String slotMinWidth);
    
    /**
     * Whether child resources should be expanded when the view loads.
     * By default, all child resources are visible, but if you’d like child resources to be collapsed, 
     * meaning they are not initially visible, change this setting to false.
     * 
     * Only supported in Timeline view.
     * 
     * @param resourcesInitiallyExpanded
     */
    void setResourcesInitiallyExpanded(boolean resourcesInitiallyExpanded);
    
    /**
     * When this setting is activated, only resources that have associated events will be displayed.
     * When activated, please be aware that in order for resources to render, event data will need to finish being fetched.
     * @param filterResourcesWithEvents
     */
    void setFilterResourcesWithEvents(boolean filterResourcesWithEvents);
    
    /**
     * Determines the ordering of the resource list.
     * If prefixed with a minus sign like '-propertyName', the ordering will be descending.
     * If no resourceOrder is given (the default), resources will be ordered by their id, then by title.
     * @param resourceOrder
     */
    void setResourceOrder(String resourceOrder);
    
    /**
     * Determines whether the user can drag events between resources.
     * The default value is inherited from the master editable flag, which is false by default.
     * @param eventResourceEditable
     */
    void setEntryResourceEditable(boolean eventResourceEditable);

    /**
     * Adds an resource to this calendar. Noop if the resource id is already registered.
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
     * Adds resources to this calendar. Noop already registered resources.
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
     * Adds resources to this calendar. Noop already registered resources.
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
     * Noop on not registered resources.
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
     * Noop on not registered resources.
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
     * Returns all resources registered in this instance, including child resources. Changes in an resource instance is reflected in the
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
     * Changes in an resource instance is reflected in the
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
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     * <p></p>
     * A resource "label" is anywhere the name of a resource is displayed. 
     * They exist in the header of vertical resource view and the side section of resource timeline view.
     *
     * @param s JS function to be attached
     */

    void setResourceLabelClassNamesCallback(String s);

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     * 
     * A resource "label" is anywhere the name of a resource is displayed. 
     * They exist in the header of vertical resource view and the side section of resource timeline view.
     *
     * @param s JS function to be attached
     */
    void setResourceLabelContentCallback(String s);

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     * 
     * A resource "label" is anywhere the name of a resource is displayed. 
     * They exist in the header of vertical resource view and the side section of resource timeline view.
     *
     * @param s JS function to be attached
     */
    void setResourceLabelDidMountCallback(String s);

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     *
     * A resource "label" is anywhere the name of a resource is displayed.
     * They exist in the header of vertical resource view and the side section of resource timeline view.
     *
     * @param s JS function to be attached
     */
    void setResourceLabelWillUnmountCallback(String s);

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     *
     * A resource "label" is anywhere the name of a resource is displayed.
     * They exist in the header of vertical resource view and the side section of resource timeline view.
     *
     * @param s JS function to be attached
     * @deprecated Use {@link #setResourceLabelWillUnmountCallback(String)} instead.
     */
    @Deprecated(forRemoval = true)
    default void setResourceLablelWillUnmountCallback(String s) {
        setResourceLabelWillUnmountCallback(s);
    }

    /**
     * Whether to re-fetch resources when the user navigates to a new date range.
     *
     * @param refetch whether to refetch resources on navigate
     * @see <a href="https://fullcalendar.io/docs/refetchResourcesOnNavigate">refetchResourcesOnNavigate</a>
     */
    void setRefetchResourcesOnNavigate(boolean refetch);

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
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     * 
     * A resource "lane" is an element in resource-timeline view. 
     * It runs horizontally across the timeline slots for each resource.
     *
     * @param s JS function to be attached
     */
    void setResourceLaneClassNamesCallback(String s);

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     * 
     * A resource "lane" is an element in resource-timeline view. 
     * It runs horizontally across the timeline slots for each resource.
     *
     * @param s JS function to be attached
     */
    void setResourceLaneContentCallback(String s);

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     * 
     * A resource "lane" is an element in resource-timeline view. 
     * It runs horizontally across the timeline slots for each resource.
     *
     * @param s JS function to be attached
     */
    void setResourceLaneDidMountCallback(String s);

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as callback. It must be a valid JavaScript function.
     * 
     * A resource "lane" is an element in resource-timeline view. 
     * It runs horizontally across the timeline slots for each resource.
     *
     * @param s JS function to be attached
     */
    void setResourceLaneWillUnmountCallback(String s);

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
     * Groups resources visually in the resource area by the given field name.
     * Resources with the same value for that field will be grouped under a shared group header row.
     * <p>
     * <b>Note:</b> {@code setResourceGroupField} is required to enable resource grouping. Setting
     * {@link ResourceAreaColumn#withGroup(boolean) group=true} on a column only designates which column
     * shows the group label in a multi-column area — it does not enable grouping on its own.
     * <p>
     * For best results, sort your resources by the group field before adding them.
     * To also configure the grouping column in a multi-column resource area, set
     * {@link ResourceAreaColumn#withGroup(boolean) group=true} on the corresponding column.
     *
     * @param fieldName the resource property name to group by (e.g., a key added via
     *                  {@link Resource#addExtendedProps(String, Object)})
     * @see #setResourceOrder(String)
     * @see #setResourceAreaColumns(List)
     * @see <a href="https://fullcalendar.io/docs/resourceGroupField">FullCalendar resourceGroupField</a>
     */
    void setResourceGroupField(String fieldName);

    /**
     * Sets a JavaScript function that returns CSS class names for resource group header rows.
     * The function receives a {@code groupInfo} object:
     * <pre>{@code
     * {
     *   groupValue: any,       // the value of the group field (e.g., "Sales")
     *   resources: Resource[], // resources in this group
     *   view: View
     * }
     * }</pre>
     * Must return a string array.
     * <p>
     * Example: {@code "function(info) { return ['group-' + info.groupValue]; }"}
     *
     * @param jsFunction JavaScript function string
     * @see #setResourceGroupField(String)
     * @see <a href="https://fullcalendar.io/docs/resourceGroupClassNames">FullCalendar resourceGroupClassNames</a>
     */
    void setResourceGroupClassNamesCallback(String jsFunction);

    /**
     * Sets a JavaScript function that customizes the content of resource group header rows.
     * The function receives a {@code groupInfo} object and may return a string, DOM element, or
     * content object ({@code { html: '...', text: '...' }}).
     * <p>
     * Example: {@code "function(info) { return { html: '<b>' + info.groupValue + '</b>' }; }"}
     *
     * @param jsFunction JavaScript function string
     * @see #setResourceGroupField(String)
     * @see <a href="https://fullcalendar.io/docs/resourceGroupContent">FullCalendar resourceGroupContent</a>
     */
    void setResourceGroupContentCallback(String jsFunction);

    /**
     * Sets a JavaScript function called after a resource group header row is added to the DOM.
     * The function receives a {@code groupInfo} object.
     *
     * @param jsFunction JavaScript function string
     * @see #setResourceGroupField(String)
     * @see <a href="https://fullcalendar.io/docs/resourceGroupDidMount">FullCalendar resourceGroupDidMount</a>
     */
    void setResourceGroupDidMountCallback(String jsFunction);

    /**
     * Sets a JavaScript function called before a resource group header row is removed from the DOM.
     * The function receives a {@code groupInfo} object.
     *
     * @param jsFunction JavaScript function string
     * @see #setResourceGroupField(String)
     * @see <a href="https://fullcalendar.io/docs/resourceGroupWillUnmount">FullCalendar resourceGroupWillUnmount</a>
     */
    void setResourceGroupWillUnmountCallback(String jsFunction);

    /**
     * Sets a JavaScript function that returns CSS class names for the resource area header cell
     * (the top-left cell in timeline views).
     * The function receives a {@code headerInfo} object with a {@code view} property.
     * <p>
     * Example: {@code "function(info) { return ['custom-header']; }"}
     *
     * @param jsFunction JavaScript function string
     * @see #setResourceAreaHeaderContent(String)
     * @see <a href="https://fullcalendar.io/docs/resourceAreaHeaderClassNames">FullCalendar resourceAreaHeaderClassNames</a>
     */
    void setResourceAreaHeaderClassNamesCallback(String jsFunction);

    /**
     * Sets a JavaScript function called after the resource area header cell is added to the DOM.
     * The function receives a {@code headerInfo} object with a {@code view} property.
     *
     * @param jsFunction JavaScript function string
     * @see #setResourceAreaHeaderContent(String)
     * @see <a href="https://fullcalendar.io/docs/resourceAreaHeaderDidMount">FullCalendar resourceAreaHeaderDidMount</a>
     */
    void setResourceAreaHeaderDidMountCallback(String jsFunction);

    /**
     * Sets a JavaScript function called before the resource area header cell is removed from the DOM.
     * The function receives a {@code headerInfo} object with a {@code view} property.
     *
     * @param jsFunction JavaScript function string
     * @see #setResourceAreaHeaderContent(String)
     * @see <a href="https://fullcalendar.io/docs/resourceAreaHeaderWillUnmount">FullCalendar resourceAreaHeaderWillUnmount</a>
     */
    void setResourceAreaHeaderWillUnmountCallback(String jsFunction);

    /**
     * In vertical resource views ({@code resourceTimeGridDay}, {@code resourceDayGridDay}, etc.),
     * determines whether date column headers appear above resource column headers.
     * <p>
     * {@code true}: dates above resources (dates as outer grouping)<br>
     * {@code false} (default): resources above dates (resources as outer grouping)
     *
     * @param datesAboveResources {@code true} to show dates above resources
     * @see <a href="https://fullcalendar.io/docs/datesAboveResources">FullCalendar datesAboveResources</a>
     */
    void setDatesAboveResources(boolean datesAboveResources);

    /**
     * Sets the minimum pixel width for events in timeline views. Ensures that very short events
     * (spanning less than one pixel in the current zoom level) remain visible and clickable.
     * The default is 3 pixels.
     *
     * @param pixels minimum width in pixels
     * @see <a href="https://fullcalendar.io/docs/eventMinWidth">FullCalendar eventMinWidth</a>
     */
    void setEventMinWidth(int pixels);

    /**
     * Sets a JavaScript function called after a resource is added to FullCalendar's internal store.
     * Fired after {@link #addResource(Resource)}.
     * <p>
     * Since resources are server-managed, the server already knows about the addition; this callback
     * is useful for client-side reactions (e.g., updating a DOM counter).
     * <p>
     * The function receives a {@code resourceInfo} object with a {@code resource} property.
     * Note that the {@code resource} in the callback argument is a FullCalendar client-side resource
     * object, not a Java {@link Resource} instance.
     *
     * @param jsFunction JavaScript function string
     * @see <a href="https://fullcalendar.io/docs/resourceAdd">FullCalendar resourceAdd</a>
     */
    void setResourceAddCallback(String jsFunction);

    /**
     * Sets a JavaScript function called after a resource's properties are modified in
     * FullCalendar's internal store.
     * <p>
     * The function receives a {@code resourceInfo} object with {@code resource} and {@code revert} properties.
     * Note that the {@code resource} in the callback argument is a FullCalendar client-side resource object,
     * not a Java {@link Resource} instance.
     * <p>
     * On the server side, this fires after {@link #updateResource(Resource)} is called, or after
     * {@link Resource#setTitle(String)} / {@link Resource#setColor(String)} (which auto-push the change).
     *
     * @param jsFunction JavaScript function string
     * @see <a href="https://fullcalendar.io/docs/resourceChange">FullCalendar resourceChange</a>
     */
    void setResourceChangeCallback(String jsFunction);

    /**
     * Sets a JavaScript function called after a resource is removed from FullCalendar's internal store.
     * Fired after {@link #removeResource(Resource)}.
     * <p>
     * The function receives a {@code resourceInfo} object with a {@code resource} property.
     * Note that the {@code resource} in the callback argument is a FullCalendar client-side resource object,
     * not a Java {@link Resource} instance.
     *
     * @param jsFunction JavaScript function string
     * @see <a href="https://fullcalendar.io/docs/resourceRemove">FullCalendar resourceRemove</a>
     */
    void setResourceRemoveCallback(String jsFunction);

    /**
     * Sets a JavaScript function called after all resources have been (re-)initialized or modified
     * in FullCalendar's internal store.
     * <p>
     * The function receives a {@code resourceInfo} object with a {@code resources} array.
     * Note that the {@code resources} in the callback argument are FullCalendar client-side resource objects,
     * not Java {@link Resource} instances.
     *
     * @param jsFunction JavaScript function string
     * @see <a href="https://fullcalendar.io/docs/resourcesSet">FullCalendar resourcesSet</a>
     */
    void setResourcesSetCallback(String jsFunction);

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

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

import org.vaadin.stefan.fullcalendar.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents functionality for the FullCalendarScheduler.
 */
public interface Scheduler {

    /**
     * While developing, in order to hide the license warning, use this following key.
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
    default void addResource(@NotNull Resource resource, boolean scrollToLast) {
        Objects.requireNonNull(resource);
        addResources(Collections.singletonList(resource), scrollToLast);
    }
    default void addResource(@NotNull Resource resource) {
        Objects.requireNonNull(resource);
        addResources(Collections.singletonList(resource));
    }

    /**
     * Adds resources to this calendar. Noop already registered resources.
     *
     * @param resources resources to add
     * @throws NullPointerException when null is passed
     */
    default void addResources(boolean scrollToLast, @NotNull Resource... resources) {
        addResources(Arrays.asList(resources), scrollToLast);
    }
    default void addResources(@NotNull Resource... resources) {
        addResources(Arrays.asList(resources));
    }

    /**
     * Adds resources to this calendar. Noop already registered resources.
     *
     * @param resources resources to add
     * @throws NullPointerException when null is passed
     */
    void addResources(@NotNull Iterable<Resource> resources, boolean scrollToLast);
    void addResources(@NotNull Iterable<Resource> resources);

    /**
     * Removes the given resource. Also removes it from its related entries.
     * Does not send an extra update for the entries.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    default void removeResource(@NotNull Resource resource) {
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
    default void removeResources(@NotNull Resource... resources) {
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
    void removeResources(@NotNull Iterable<Resource> resources);

    /**
     * Returns the resource with the given id. Is empty when the id is not registered.
     *
     * @param id id
     * @return resource or empty
     * @throws NullPointerException when null is passed
     */
    Optional<Resource> getResourceById(@NotNull String id);

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
    void setResourceLablelWillUnmountCallback(String s);
    
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
     * Registers a listener to be informed when an entry dropped event occurred, along with scheduler
     * specific data.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    Registration addEntryDroppedSchedulerListener(@NotNull ComponentEventListener<? extends EntryDroppedSchedulerEvent> listener);

    /**
     * Registers a listener to be informed when a timeslot has been clicked, including scheduler specific data.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    Registration addTimeslotClickedSchedulerListener(@NotNull ComponentEventListener<? extends TimeslotClickedSchedulerEvent> listener);

    /**
     * Registers a listener to be informed when a timespan has been selected, including scheduler specific data.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    Registration addTimeslotsSelectedSchedulerListener(@NotNull ComponentEventListener<? extends TimeslotsSelectedSchedulerEvent> listener);
}

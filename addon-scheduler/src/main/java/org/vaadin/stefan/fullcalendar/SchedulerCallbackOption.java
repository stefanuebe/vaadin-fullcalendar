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

/**
 * Scheduler-specific FC options that accept a JavaScript function value.
 * Use with {@link FullCalendar#setCallbackOption(String, String)} or the
 * string-key overload on any {@link FullCalendarScheduler} instance.
 *
 * @see CallbackOption for core (non-scheduler) callback options
 */
public enum SchedulerCallbackOption implements ClientSideValue {

    // ---- Render hooks: Resource Label ----
    /**
     * Add CSS classes to the resource name label cell. Called when a resource label is rendered.
     * Arguments: {@code {resource, el, view}}.
     * Returns: string array of CSS class names.
     */
    RESOURCE_LABEL_CLASS_NAMES("resourceLabelClassNames"),

    /**
     * Customize the content inside a resource name label cell.
     * Arguments: {@code {resource, el, view}}.
     * Returns: content object or HTML string.
     */
    RESOURCE_LABEL_CONTENT("resourceLabelContent"),

    /**
     * Called after a resource label element is added to the DOM.
     * Arguments: {@code {resource, el, view}}.
     */
    RESOURCE_LABEL_DID_MOUNT("resourceLabelDidMount"),

    /**
     * Called before a resource label element is removed from the DOM.
     * Arguments: {@code {resource, el, view}}.
     */
    RESOURCE_LABEL_WILL_UNMOUNT("resourceLabelWillUnmount"),

    // ---- Render hooks: Resource Lane ----
    /**
     * Add CSS classes to a resource lane (row in timeline or vertical-resource view).
     * Arguments: {@code {resource, el, view}}.
     * Returns: string array of CSS class names.
     */
    RESOURCE_LANE_CLASS_NAMES("resourceLaneClassNames"),

    /**
     * Customize the content inside a resource lane.
     * Arguments: {@code {resource, el, view}}.
     * Returns: content object or HTML string.
     */
    RESOURCE_LANE_CONTENT("resourceLaneContent"),

    /**
     * Called after a resource lane element is added to the DOM.
     * Arguments: {@code {resource, el, view}}.
     */
    RESOURCE_LANE_DID_MOUNT("resourceLaneDidMount"),

    /**
     * Called before a resource lane element is removed from the DOM.
     * Arguments: {@code {resource, el, view}}.
     */
    RESOURCE_LANE_WILL_UNMOUNT("resourceLaneWillUnmount"),

    // ---- Render hooks: Resource Group ----
    /**
     * Add CSS classes to a resource group header row (when grouping by resource field).
     * Arguments: {@code {groupValue, el, view}}.
     * Returns: string array of CSS class names.
     */
    RESOURCE_GROUP_CLASS_NAMES("resourceGroupClassNames"),

    /**
     * Customize the content inside a resource group header row.
     * Arguments: {@code {groupValue, el, view}}.
     * Returns: content object or HTML string.
     */
    RESOURCE_GROUP_CONTENT("resourceGroupContent"),

    /**
     * Called after a resource group header element is added to the DOM.
     * Arguments: {@code {groupValue, el, view}}.
     */
    RESOURCE_GROUP_DID_MOUNT("resourceGroupDidMount"),

    /**
     * Called before a resource group header element is removed from the DOM.
     * Arguments: {@code {groupValue, el, view}}.
     */
    RESOURCE_GROUP_WILL_UNMOUNT("resourceGroupWillUnmount"),

    // ---- Render hooks: Resource Group Lane ----
    /**
     * Add CSS classes to a resource group lane row (when grouping by resource field).
     * Arguments: {@code {groupValue, el, view}}.
     * Returns: string array of CSS class names.
     */
    RESOURCE_GROUP_LANE_CLASS_NAMES("resourceGroupLaneClassNames"),

    /**
     * Customize the content inside a resource group lane row.
     * Arguments: {@code {groupValue, el, view}}.
     * Returns: content object or HTML string.
     */
    RESOURCE_GROUP_LANE_CONTENT("resourceGroupLaneContent"),

    /**
     * Called after a resource group lane element is added to the DOM.
     * Arguments: {@code {groupValue, el, view}}.
     */
    RESOURCE_GROUP_LANE_DID_MOUNT("resourceGroupLaneDidMount"),

    /**
     * Called before a resource group lane element is removed from the DOM.
     * Arguments: {@code {groupValue, el, view}}.
     */
    RESOURCE_GROUP_LANE_WILL_UNMOUNT("resourceGroupLaneWillUnmount"),

    // ---- Render hooks: Resource Area Header ----
    /**
     * Add CSS classes to the resource area header cell (top-left corner in timeline views).
     * Arguments: {@code {el, view}}.
     * Returns: string array of CSS class names.
     */
    RESOURCE_AREA_HEADER_CLASS_NAMES("resourceAreaHeaderClassNames"),

    /**
     * Called after the resource area header element is added to the DOM.
     * Arguments: {@code {el, view}}.
     */
    RESOURCE_AREA_HEADER_DID_MOUNT("resourceAreaHeaderDidMount"),

    /**
     * Called before the resource area header element is removed from the DOM.
     * Arguments: {@code {el, view}}.
     */
    RESOURCE_AREA_HEADER_WILL_UNMOUNT("resourceAreaHeaderWillUnmount"),

    // ---- Resource lifecycle callbacks ----
    /**
     * Called when a resource is added to the scheduler. Use for custom logic on resource creation.
     * Arguments: {@code {resource}}.
     */
    RESOURCE_ADD("resourceAdd"),

    /**
     * Called when a resource is changed (updated). Use for custom logic on resource modification.
     * Arguments: {@code {resource, oldResource}}.
     */
    RESOURCE_CHANGE("resourceChange"),

    /**
     * Called when a resource is removed from the scheduler. Use for cleanup on resource deletion.
     * Arguments: {@code {resource}}.
     */
    RESOURCE_REMOVE("resourceRemove"),

    /**
     * Called when the full resource list is (re)set via a data provider refresh.
     * Arguments: {@code {resources}}} (array of all resources).
     */
    RESOURCES_SET("resourcesSet");

    private final String clientSideValue;

    SchedulerCallbackOption(String clientSideValue) {
        this.clientSideValue = clientSideValue;
    }

    @Override
    public String getClientSideValue() {
        return clientSideValue;
    }
}

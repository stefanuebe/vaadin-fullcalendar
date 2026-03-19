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
    RESOURCE_LABEL_CLASS_NAMES("resourceLabelClassNames"),
    RESOURCE_LABEL_CONTENT("resourceLabelContent"),
    RESOURCE_LABEL_DID_MOUNT("resourceLabelDidMount"),
    RESOURCE_LABEL_WILL_UNMOUNT("resourceLabelWillUnmount"),

    // ---- Render hooks: Resource Lane ----
    RESOURCE_LANE_CLASS_NAMES("resourceLaneClassNames"),
    RESOURCE_LANE_CONTENT("resourceLaneContent"),
    RESOURCE_LANE_DID_MOUNT("resourceLaneDidMount"),
    RESOURCE_LANE_WILL_UNMOUNT("resourceLaneWillUnmount"),

    // ---- Render hooks: Resource Group ----
    RESOURCE_GROUP_CLASS_NAMES("resourceGroupClassNames"),
    RESOURCE_GROUP_CONTENT("resourceGroupContent"),
    RESOURCE_GROUP_DID_MOUNT("resourceGroupDidMount"),
    RESOURCE_GROUP_WILL_UNMOUNT("resourceGroupWillUnmount"),

    // ---- Render hooks: Resource Group Lane ----
    RESOURCE_GROUP_LANE_CLASS_NAMES("resourceGroupLaneClassNames"),
    RESOURCE_GROUP_LANE_CONTENT("resourceGroupLaneContent"),
    RESOURCE_GROUP_LANE_DID_MOUNT("resourceGroupLaneDidMount"),
    RESOURCE_GROUP_LANE_WILL_UNMOUNT("resourceGroupLaneWillUnmount"),

    // ---- Render hooks: Resource Area Header ----
    RESOURCE_AREA_HEADER_CLASS_NAMES("resourceAreaHeaderClassNames"),
    RESOURCE_AREA_HEADER_DID_MOUNT("resourceAreaHeaderDidMount"),
    RESOURCE_AREA_HEADER_WILL_UNMOUNT("resourceAreaHeaderWillUnmount"),

    // ---- Resource lifecycle callbacks ----
    RESOURCE_ADD("resourceAdd"),
    RESOURCE_CHANGE("resourceChange"),
    RESOURCE_REMOVE("resourceRemove"),
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

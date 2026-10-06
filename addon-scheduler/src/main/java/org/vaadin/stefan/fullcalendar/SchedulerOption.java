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

import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import tools.jackson.databind.JsonNode;

import java.util.*;

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
    /**
     * In the vertical resource views ({@code resourceTimeGrid} and {@code resourceDayGrid}), puts the date headings
     * above the resource headings instead of the resource headings above the date headings.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/datesAboveResources">datesAboveResources</a>
     */
    DATES_ABOVE_RESOURCES("datesAboveResources"),

    /**
     * Minimum width of an entry in the timeline view, in pixels.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>{@code 30}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventMinWidth">eventMinWidth</a>
     */
    ENTRY_MIN_WIDTH("eventMinWidth"),

    /**
     * Former name of {@link #ENTRY_RESOURCE_EDITABLE}: allows the user to drag entries between resources.
     *
     * @deprecated use {@link #ENTRY_RESOURCE_EDITABLE}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    ENTRY_RESOURCES_EDITABLE("eventResourceEditable"),

    /**
     * Allows the user to drag entries between resources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>inherited from {@link Option#EDITABLE}, which the add-on sets to {@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventResourceEditable">eventResourceEditable</a>
     */
    ENTRY_RESOURCE_EDITABLE("eventResourceEditable"),

    /**
     * Shows only resources that have entries assigned. The entries must be fetched before the resources can
     * render.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/filterResourcesWithEvents">filterResourcesWithEvents</a>
     */
    FILTER_RESOURCES_WITH_ENTRIES("filterResourcesWithEvents"),

    /**
     * The FullCalendar Scheduler license key, needed to use the premium (scheduler) features.
     * <dl>
     *   <dt>Type</dt> <dd>{@code String}: a commercial key in the form {@code XXXXXXXXXX-XXX-XXXXXXXXXX}, or one of
     *                     the free keys {@link Scheduler#AGPL_V3_LICENSE_KEY} and
     *                     {@link Scheduler#NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.LICENSE_KEY, Scheduler.AGPL_V3_LICENSE_KEY);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/schedulerLicenseKey">schedulerLicenseKey</a>
     */
    LICENSE_KEY("schedulerLicenseKey"),

    /**
     * Refetches and rerenders the resources when the user changes the date or the view. The resources
     * function or JSON feed then receives {@code start}, {@code end} and {@code timezone}.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/refetchResourcesOnNavigate">refetchResourcesOnNavigate</a>
     */
    REFETCH_RESOURCES_ON_NAVIGATE("refetchResourcesOnNavigate"),

    /**
     * Whether child resources are expanded when the view loads. Set to {@code false} to start with them
     * collapsed. Only supported in the timeline view.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesInitiallyExpanded">resourcesInitiallyExpanded</a>
     */
    RESOURCES_INITIALLY_EXPANDED("resourcesInitiallyExpanded"),

    /**
     * Called after the resource data is initialized or changed in any way.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(resources)}. {@code resources} is an array of all resources, the
     *                         same as {@code Calendar.getResources} returns.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCES_SET, JsCallback.of(
     *         "resources => console.log(resources.length + ' resources')"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesSet">resourcesSet</a>
     */
    RESOURCES_SET("resourcesSet"),

    /**
     * Called after a resource has been added to the calendar, that is after {@code Calendar.addResource} was
     * called on the client.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(addInfo)}. {@code addInfo} has {@code resource} (the added resource),
     *                         {@code revert} (a function that reverses the action).</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_ADD, JsCallback.of(
     *         "addInfo => console.log('added', addInfo.resource.title)"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceAdd">resourceAdd</a>
     */
    RESOURCE_ADD("resourceAdd"),

    /**
     * Former name of {@link #RESOURCE_COLUMNS}: column definitions of the resource area.
     *
     * @deprecated use {@link #RESOURCE_COLUMNS}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_COLUMNS("resourceColumns"),

    /**
     * Former name of {@link #RESOURCE_COLUMN_HEADER_CLASS}: CSS classes for the resource column header.
     *
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_CLASS_NAMES("resourceColumnHeaderClass"),

    /**
     * Former name of {@link #RESOURCE_COLUMN_HEADER_CONTENT}: custom content for the resource column header.
     *
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_CONTENT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_CONTENT("resourceColumnHeaderContent"),

    /**
     * Former name of {@link #RESOURCE_COLUMN_HEADER_DID_MOUNT}: called after the resource column header is added
     * to the DOM.
     *
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_DID_MOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_DID_MOUNT("resourceColumnHeaderDidMount"),

    /**
     * Former name of {@link #RESOURCE_COLUMN_HEADER_WILL_UNMOUNT}: called before the resource column header is
     * removed from the DOM.
     *
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_WILL_UNMOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_WILL_UNMOUNT("resourceColumnHeaderWillUnmount"),

    /**
     * Former name of {@link #RESOURCE_COLUMNS_WIDTH}: width of the resource area.
     *
     * @deprecated use {@link #RESOURCE_COLUMNS_WIDTH}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_WIDTH("resourceColumnsWidth"),

    /**
     * Adds CSS classes to the cells of the resource area in the timeline view. Each cell shows one column field
     * (the resource title or a column of {@link #RESOURCE_COLUMNS}). The hook also runs for the cells of group
     * rows, which have no {@code resource}.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} (absent in group rows),
     *                         {@code field}, {@code fieldValue}.</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_CELL_CLASS, JsCallback.of(
     *         "info => info.field === 'title' ? 'resource-title' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellClass</a>
     */
    RESOURCE_CELL_CLASS("resourceCellClass"),

    /**
     * Customizes the content of the cells of the resource area in the timeline view. Branch on
     * {@code info.field === 'title'} for the title cell only.
     * <dl>
     *   <dt>Type</dt>     <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} (absent in group rows),
     *                         {@code field}, {@code fieldValue}.</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_CELL_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return info.field === 'title' ? info.fieldValue.toUpperCase() : info.fieldValue;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellContent</a>
     */
    RESOURCE_CELL_CONTENT("resourceCellContent"),

    /**
     * Called after a cell of the resource area is added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} (absent in group rows),
     *                         {@code field}, {@code fieldValue}, {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_CELL_DID_MOUNT, JsCallback.of(
     *         "info => info.el.title = String(info.fieldValue)"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellDidMount</a>
     */
    RESOURCE_CELL_DID_MOUNT("resourceCellDidMount"),

    /**
     * Called before a cell of the resource area is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} (absent in group rows),
     *                         {@code field}, {@code fieldValue}, {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_CELL_WILL_UNMOUNT, JsCallback.of(
     *         "info => console.log('removing cell', info.field)"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellWillUnmount</a>
     */
    RESOURCE_CELL_WILL_UNMOUNT("resourceCellWillUnmount"),

    /**
     * Called after a resource has been modified, that is after {@code Resource.setProp} or
     * {@code Resource.setExtendedProp} was called on the client.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(changeInfo)}. {@code changeInfo} has {@code resource} (with the
     *                         changed data), {@code oldResource} (with the data before the change),
     *                         {@code revert} (a function that reverses the action).</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_CHANGE, JsCallback.of("""
     *         function(changeInfo) {
     *             console.log(changeInfo.oldResource.title, '->', changeInfo.resource.title);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceChange">resourceChange</a>
     */
    RESOURCE_CHANGE("resourceChange"),

    /**
     * Turns the resource area from a plain list of resource titles into a grid of data. Each column shows a
     * property of the resource and can be grouped, sized and customized.
     * Prefer {@link Scheduler#setResourceColumns(List)}. Passing a list of {@link ResourceColumn}
     * to {@link FullCalendarScheduler#setOption(SchedulerOption, Object)} is forwarded to it, so component
     * columns are bound. Raw JSON is sent as it is and cannot carry component columns.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link List} of {@link ResourceColumn}</dd>
     *   <dt>Default</dt> <dd>none, the resource area is a plain list of resource titles</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setResourceColumns(
     *         new ResourceColumn("title", "Resource").withWidth("200px"),
     *         new ResourceColumn("department", "Department").withGroup(true));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumns">resourceColumns</a>
     */
    RESOURCE_COLUMNS("resourceColumns"),

    /**
     * Width of the area that contains the list of resources.
     * <dl>
     *   <dt>Type</dt>    <dd>number of pixels, or CSS width string (e.g. {@code "200px"}, {@code "25%"})</dd>
     *   <dt>Default</dt> <dd>{@code "30%"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumnsWidth">resourceColumnsWidth</a>
     */
    RESOURCE_COLUMNS_WIDTH("resourceColumnsWidth"),

    /**
     * Adds CSS classes to the resource column header, shown above the resource data in the timeline view.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function()}. FullCalendar documents no argument for this hook. Only the DidMount
     *                         and WillUnmount hooks of the resource column header get an {@code info} with {@code el}.</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_COLUMN_HEADER_CLASS, "staff-header");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderClass</a>
     */
    RESOURCE_COLUMN_HEADER_CLASS("resourceColumnHeaderClass"),

    /**
     * Custom content for the resource column header, shown above the resource data in the timeline view
     * (the text "Resources" by default).
     * <dl>
     *   <dt>Type</dt>     <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function()}. FullCalendar documents no argument for this hook. Only the DidMount
     *                         and WillUnmount hooks of the resource column header get an {@code info} with {@code el}.</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_COLUMN_HEADER_CONTENT, JsCallback.of("() => 'Staff'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderContent</a>
     */
    RESOURCE_COLUMN_HEADER_CONTENT("resourceColumnHeaderContent"),

    /**
     * Called after the resource column header is added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_COLUMN_HEADER_DID_MOUNT, JsCallback.of(
     *         "info => info.el.title = 'Resources'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderDidMount</a>
     */
    RESOURCE_COLUMN_HEADER_DID_MOUNT("resourceColumnHeaderDidMount"),

    /**
     * Called before the resource column header is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_COLUMN_HEADER_WILL_UNMOUNT, JsCallback.of(
     *         "info => info.el.removeAttribute('title')"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderWillUnmount</a>
     */
    RESOURCE_COLUMN_HEADER_WILL_UNMOUNT("resourceColumnHeaderWillUnmount"),

    /**
     * Adds CSS classes to the column header cells that show the resource names in the vertical resource views
     * (resource DayGrid and resource TimeGrid).
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code date} (if the column
     *                         is scoped to a date), {@code text}, {@code isOther}, {@code isToday}, {@code isPast},
     *                         {@code isFuture}, {@code isDisabled}, {@code isNarrow}, {@code isMajor}, {@code level}.</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_DAY_HEADER_CLASS, JsCallback.of(
     *         "info => info.isToday ? 'today-resource' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderClass</a>
     */
    RESOURCE_DAY_HEADER_CLASS("resourceDayHeaderClass"),

    /**
     * Customizes the content of the column header cells that show the resource names in the vertical resource
     * views (resource DayGrid and resource TimeGrid).
     * <dl>
     *   <dt>Type</dt>     <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code date} (if the column
     *                         is scoped to a date), {@code text}, {@code isOther}, {@code isToday}, {@code isPast},
     *                         {@code isFuture}, {@code isDisabled}, {@code isNarrow}, {@code isMajor}, {@code level}.</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_DAY_HEADER_CONTENT, JsCallback.of(
     *         "info => info.resource.title + (info.isNarrow ? '' : ' (' + info.resource.id + ')')"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderContent</a>
     */
    RESOURCE_DAY_HEADER_CONTENT("resourceDayHeaderContent"),

    /**
     * Called after a resource day header cell is added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code date} (if the column
     *                         is scoped to a date), {@code text}, {@code isOther}, {@code isToday}, {@code isPast},
     *                         {@code isFuture}, {@code isDisabled}, {@code isNarrow}, {@code isMajor}, {@code level},
     *                         {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_DAY_HEADER_DID_MOUNT, JsCallback.of(
     *         "info => info.el.dataset.resourceId = info.resource.id"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderDidMount</a>
     */
    RESOURCE_DAY_HEADER_DID_MOUNT("resourceDayHeaderDidMount"),

    /**
     * Called before a resource day header cell is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code date} (if the column
     *                         is scoped to a date), {@code text}, {@code isOther}, {@code isToday}, {@code isPast},
     *                         {@code isFuture}, {@code isDisabled}, {@code isNarrow}, {@code isMajor}, {@code level},
     *                         {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_DAY_HEADER_WILL_UNMOUNT, JsCallback.of(
     *         "info => delete info.el.dataset.resourceId"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderWillUnmount</a>
     */
    RESOURCE_DAY_HEADER_WILL_UNMOUNT("resourceDayHeaderWillUnmount"),

    /**
     * Former name of {@link #RESOURCE_GROUP_HEADER_CLASS}: CSS classes for resource group headers.
     *
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_CLASS_NAMES("resourceGroupHeaderClass"),

    /**
     * Former name of {@link #RESOURCE_GROUP_HEADER_CONTENT}: customizes the content of a resource group header.
     *
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_CONTENT}, which sets the same FullCalendar option.
     * The group value is now {@code info.fieldValue}.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_CONTENT("resourceGroupHeaderContent"),

    /**
     * Former name of {@link #RESOURCE_GROUP_HEADER_DID_MOUNT}: called after a resource group header is added to
     * the DOM.
     *
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_DID_MOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_DID_MOUNT("resourceGroupHeaderDidMount"),

    /**
     * Visually groups resources by a field that each resource has. Each group gets a divider at the top. The text
     * of the divider is set by {@link #RESOURCE_GROUP_HEADER_CONTENT}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code String} (field name of the resource)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceGroupField">resourceGroupField</a>
     */
    RESOURCE_GROUP_FIELD("resourceGroupField"),

    /**
     * Adds CSS classes to a resource group header, the cell that shows the group name at the top of each row
     * group in the timeline view.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field).</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_HEADER_CLASS, JsCallback.of(
     *         "info => 'group-' + info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderClass</a>
     */
    RESOURCE_GROUP_HEADER_CLASS("resourceGroupHeaderClass"),

    /**
     * Customizes the content of a resource group header. This sets the text of the group divider created by
     * {@link #RESOURCE_GROUP_FIELD}.
     * <dl>
     *   <dt>Type</dt>     <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field).</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_HEADER_CONTENT, JsCallback.of(
     *         "info => 'Department: ' + info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderContent</a>
     */
    RESOURCE_GROUP_HEADER_CONTENT("resourceGroupHeaderContent"),

    /**
     * Called after a resource group header is added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field), {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_HEADER_DID_MOUNT, JsCallback.of(
     *         "info => info.el.dataset.group = info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderDidMount</a>
     */
    RESOURCE_GROUP_HEADER_DID_MOUNT("resourceGroupHeaderDidMount"),

    /**
     * Called before a resource group header is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field), {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_HEADER_WILL_UNMOUNT, JsCallback.of(
     *         "info => delete info.el.dataset.group"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderWillUnmount</a>
     */
    RESOURCE_GROUP_HEADER_WILL_UNMOUNT("resourceGroupHeaderWillUnmount"),

    /**
     * Adds CSS classes to a resource group lane, the horizontal area of a group along the time slots in the
     * timeline view.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field).</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_LANE_CLASS, JsCallback.of(
     *         "info => 'group-lane-' + info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneClass</a>
     */
    RESOURCE_GROUP_LANE_CLASS("resourceGroupLaneClass"),

    /**
     * Former name of {@link #RESOURCE_GROUP_LANE_CLASS}: CSS classes for resource group lanes.
     *
     * @deprecated use {@link #RESOURCE_GROUP_LANE_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_LANE_CLASS_NAMES("resourceGroupLaneClass"),

    /**
     * Customizes the content of a resource group lane.
     * <dl>
     *   <dt>Type</dt>     <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field).</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_LANE_CONTENT, JsCallback.of(
     *         "info => info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneContent</a>
     */
    RESOURCE_GROUP_LANE_CONTENT("resourceGroupLaneContent"),

    /**
     * Called after a resource group lane is added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field), {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_LANE_DID_MOUNT, JsCallback.of(
     *         "info => info.el.dataset.group = info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneDidMount</a>
     */
    RESOURCE_GROUP_LANE_DID_MOUNT("resourceGroupLaneDidMount"),

    /**
     * Called before a resource group lane is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *                         field), {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_GROUP_LANE_WILL_UNMOUNT, JsCallback.of(
     *         "info => delete info.el.dataset.group"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneWillUnmount</a>
     */
    RESOURCE_GROUP_LANE_WILL_UNMOUNT("resourceGroupLaneWillUnmount"),

    /**
     * Former name of {@link #RESOURCE_GROUP_HEADER_WILL_UNMOUNT}: called before a resource group header is removed
     * from the DOM.
     *
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_WILL_UNMOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_WILL_UNMOUNT("resourceGroupHeaderWillUnmount"),

    /**
     * Adds CSS classes to the element below the entries of a resource lane.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}.</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_BOTTOM_CLASS, JsCallback.of(
     *         "info => 'lane-bottom-' + info.resource.id"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneBottomClass</a>
     */
    RESOURCE_LANE_BOTTOM_CLASS("resourceLaneBottomClass"),

    /**
     * Content inserted at the bottom of a resource lane, below its entries.
     * <dl>
     *   <dt>Type</dt>     <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}.</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_BOTTOM_CONTENT, JsCallback.of(
     *         "info => 'End of ' + info.resource.title"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneBottomContent</a>
     */
    RESOURCE_LANE_BOTTOM_CONTENT("resourceLaneBottomContent"),

    /**
     * Adds CSS classes to a resource lane. A resource lane runs horizontally across the timeline slots of a
     * resource in the timeline view.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}.</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_CLASS, JsCallback.of(
     *         "info => info.resource.extendedProps.isUrgent ? 'urgent-lane' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneClass</a>
     */
    RESOURCE_LANE_CLASS("resourceLaneClass"),

    /**
     * Former name of {@link #RESOURCE_LANE_CLASS}: CSS classes for resource lanes.
     *
     * @deprecated use {@link #RESOURCE_LANE_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_LANE_CLASS_NAMES("resourceLaneClass"),

    /**
     * Called after a resource lane element is added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_DID_MOUNT, JsCallback.of(
     *         "info => info.el.dataset.resourceId = info.resource.id"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneDidMount</a>
     */
    RESOURCE_LANE_DID_MOUNT("resourceLaneDidMount"),

    /**
     * Adds CSS classes to the element above the entries of a resource lane.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}.</dd>
     *   <dt>Returns</dt>  <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_TOP_CLASS, JsCallback.of(
     *         "info => 'lane-top-' + info.resource.id"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneTopClass</a>
     */
    RESOURCE_LANE_TOP_CLASS("resourceLaneTopClass"),

    /**
     * Content inserted at the top of a resource lane, above its entries.
     * <dl>
     *   <dt>Type</dt>     <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}.</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_TOP_CONTENT, JsCallback.of(
     *         "info => info.resource.title"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneTopContent</a>
     */
    RESOURCE_LANE_TOP_CONTENT("resourceLaneTopContent"),

    /**
     * Called before a resource lane element is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_WILL_UNMOUNT, JsCallback.of(
     *         "info => delete info.el.dataset.resourceId"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneWillUnmount</a>
     */
    RESOURCE_LANE_WILL_UNMOUNT("resourceLaneWillUnmount"),

    /**
     * Order of the resource list. A property name of the resource sorts ascending by that property, a leading
     * minus sign ({@code "-name"}) sorts descending. Several criteria are separated by commas.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code String}, e.g. {@code "title"}, {@code "-type1,type2"}</dd>
     *   <dt>Default</dt> <dd>{@code "id,title"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceOrder">resourceOrder</a>
     */
    RESOURCE_ORDER("resourceOrder"),

    /**
     * Called after a resource has been removed from the calendar, that is after {@code Resource.remove} was
     * called on the client.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(removeInfo)}. {@code removeInfo} has {@code resource} (the removed
     *                         resource), {@code revert} (a function that reverses the action).</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_REMOVE, JsCallback.of(
     *         "removeInfo => console.log('removed', removeInfo.resource.id)"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceRemove">resourceRemove</a>
     */
    RESOURCE_REMOVE("resourceRemove"),

    /**
     * Width of each time slot (time axis slot) in the timeline view, in pixels.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>not set, FullCalendar computes a reasonable value</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinWidth">slotMinWidth</a>
     */
    SLOT_MIN_WIDTH("slotMinWidth"),

    ;


    private final String optionKey;

    private static final Map<SchedulerOption, List<JsonItemPropertyConverter<?, ?>>> CONVERTER_CACHE;

    static {
        Map<SchedulerOption, List<JsonItemPropertyConverter<?, ?>>> map = new EnumMap<>(SchedulerOption.class);
        for (SchedulerOption opt : values()) {
            try {
                JsonConverter[] annotations = SchedulerOption.class.getField(opt.name())
                        .getAnnotationsByType(JsonConverter.class);
                if (annotations.length > 0) {
                    List<JsonItemPropertyConverter<?, ?>> list = new ArrayList<>();
                    for (JsonConverter ann : annotations) {
                        list.add(ann.value().getConstructor().newInstance());
                    }
                    map.put(opt, Collections.unmodifiableList(list));
                }
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        CONVERTER_CACHE = Collections.unmodifiableMap(map);
    }

    SchedulerOption(String optionKey) {
        this.optionKey = optionKey;
    }

    String getOptionKey() {
        return optionKey;
    }

    /**
     * Returns the converters registered for this option via {@link JsonConverter} annotations, in order.
     */
    public List<JsonItemPropertyConverter<?, ?>> getConverters() {
        return CONVERTER_CACHE.getOrDefault(this, List.of());
    }

    /**
     * If this option has one or more {@link JsonConverter} annotations and the given value is
     * supported by one of them, returns the converted {@link JsonNode}. Otherwise returns empty.
     */
    @SuppressWarnings("unchecked")
    public Optional<JsonNode> convertValue(Object value) {
        for (JsonItemPropertyConverter<?, ?> c : getConverters()) {
            if (c.supports(value)) {
                return Optional.of(((JsonItemPropertyConverter<Object, Object>) c).toClientModel(value, null));
            }
        }
        return Optional.empty();
    }
}

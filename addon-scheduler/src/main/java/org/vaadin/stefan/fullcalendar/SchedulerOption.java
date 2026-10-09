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

import org.vaadin.stefan.fullcalendar.converter.ResourceColumnsConverter;
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
     * How time grid views lay out entries when printing.
     * <p>
     * Browsers, especially Firefox, have difficulties printing absolutely positioned elements across pages, which
     * affects time grid views most. Requires FullCalendar's adaptive premium plugin, which
     * {@link FullCalendarScheduler} loads.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link EntryPrintLayout}</dd>
     *   <dt>Default</dt> <dd>{@link EntryPrintLayout#AUTO} (FullCalendar stacks the entries in Firefox and keeps the
     *                        grid otherwise)</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.ENTRY_PRINT_LAYOUT, EntryPrintLayout.STACK);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventPrintLayout">eventPrintLayout</a>
     */
    ENTRY_PRINT_LAYOUT("eventPrintLayout"),

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
     * Maximum number of rows rendered when printing.
     * <p>
     * Printed output does not use virtual rendering, so a calendar with very many resources could overwhelm the
     * browser. If there are more rows, the extra rows are left out and the client logs a warning to the browser
     * console.
     * <p>
     * The option only takes effect with FullCalendar's adaptive plugin (loaded by {@link FullCalendarScheduler}) and
     * currently only applies to timeline views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number}</dd>
     *   <dt>Default</dt> <dd>{@code 1000}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.PRINT_MAX_ROWS, 200);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/printMaxRows">printMaxRows</a>
     */
    PRINT_MAX_ROWS("printMaxRows"),

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
     * collapsed.
     * Only supported in the timeline view.
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
    @JsonConverter(ResourceColumnsConverter.class)
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
     * (the resource title or a column of {@link #RESOURCE_COLUMNS}).
     * <p>
     * The hook also runs for the cells of group rows, which have no {@code resource}.
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
     * <p>
     * Set or removed on an attached calendar, it applies only to elements rendered afterwards, for example after
     * navigating, not to the elements already shown.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellDidMount</a>
     */
    RESOURCE_CELL_DID_MOUNT("resourceCellDidMount"),

    /**
     * CSS classes for the inner wrapper of a resource cell in the timeline view.
     * <p>
     * A resource cell is a cell in the resource area on the left side of the view, one per column field (such as the
     * resource title or a custom column).
     * <p>
     * The hook also applies to resource group cells.
     * <p>
     * The inner wrapper holds the content and is useful for adjusting padding.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} (absent for group cells),
     *       {@code field}, {@code fieldValue}, {@code view}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_CELL_INNER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.field === 'title' ? 'resource-title-cell' : '';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellInnerClass</a>
     */
    RESOURCE_CELL_INNER_CLASS("resourceCellInnerClass"),

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
     * <p>
     * Prefer {@link Scheduler#setResourceColumns(List)}. Passing a list of {@link ResourceColumn}
     * to {@link FullCalendarScheduler#setOption(SchedulerOption, Object)} binds component columns the same way.
     * <p>
     * Raw JSON is sent as it is, cannot carry component columns and unbinds the component columns set before.
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
    @JsonConverter(ResourceColumnsConverter.class)
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
     * CSS classes for the divider line between the resource area and the timeline area in the timeline view.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated). A callback is not supported for this option.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_COLUMN_DIVIDER_CLASS, "thick-divider");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-divider-render-hooks">resourceColumnDividerClass</a>
     */
    RESOURCE_COLUMN_DIVIDER_CLASS("resourceColumnDividerClass"),

    /**
     * Adds CSS classes to the resource column header, shown above the resource data in the timeline view.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view}.</dd>
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
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view}.</dd>
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
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view} and {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_COLUMN_HEADER_DID_MOUNT, JsCallback.of(
     *         "info => info.el.title = 'Resources'"));
     * }</pre>
     * <p>
     * Set or removed on an attached calendar, it applies only to elements rendered afterwards, for example after
     * navigating, not to the elements already shown.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderDidMount</a>
     */
    RESOURCE_COLUMN_HEADER_DID_MOUNT("resourceColumnHeaderDidMount"),

    /**
     * CSS classes for the inner wrapper of the resource column header in the timeline view. The resource column
     * header is the header above the resource data and shows the text "Resources" by default.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_COLUMN_HEADER_INNER_CLASS, "resource-header-padding");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderInnerClass</a>
     */
    RESOURCE_COLUMN_HEADER_INNER_CLASS("resourceColumnHeaderInnerClass"),

    /**
     * Called before the resource column header is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view} and {@code el}.</dd>
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
     * CSS classes for the handle the user drags to resize a resource column in the timeline view. The handle sits at
     * the end edge of every column header except the last one and is not rendered when printing.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_COLUMN_RESIZER_CLASS, "wide-resizer");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnResizerClass</a>
     */
    RESOURCE_COLUMN_RESIZER_CLASS("resourceColumnResizerClass"),

    /**
     * Horizontal alignment of the resource name in the column header cells of the vertical resource views
     * ({@code resourceTimeGrid} and {@code resourceDayGrid}). In a left-to-right locale, start means left and end
     * means right.
     * <dl>
     *   <dt>Type</dt> <dd>{@link HeaderAlign} | {@link JsCallback} returning {@code 'start'}, {@code 'center'} or
     *       {@code 'end'}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code level}.</dd>
     *   <dt>Default</dt> <dd>decided by the theme</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_DAY_HEADER_ALIGN, HeaderAlign.CENTER);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderAlign</a>
     */
    RESOURCE_DAY_HEADER_ALIGN("resourceDayHeaderAlign"),

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
     * <p>
     * Set or removed on an attached calendar, it applies only to elements rendered afterwards, for example after
     * navigating, not to the elements already shown.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderDidMount</a>
     */
    RESOURCE_DAY_HEADER_DID_MOUNT("resourceDayHeaderDidMount"),

    /**
     * CSS classes for the inner wrapper of a resource day header in the vertical resource views
     * ({@code resourceTimeGrid} and {@code resourceDayGrid}).
     * <p>
     * A resource day header is the cell in the column header that shows a resource's name.
     * <p>
     * The inner wrapper is useful for adjusting padding.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code date} (if the column
     *       is scoped to a date), {@code text}, {@code isOther}, {@code isToday}, {@code isPast}, {@code isFuture},
     *       {@code isDisabled}, {@code isNarrow}, {@code isMajor}, {@code level}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_DAY_HEADER_INNER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.isNarrow ? 'compact-resource-header' : '';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderInnerClass</a>
     */
    RESOURCE_DAY_HEADER_INNER_CLASS("resourceDayHeaderInnerClass"),

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
     * CSS classes for the expand/collapse toggle in front of expandable resource rows in the hierarchical timeline
     * view.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code isExpanded}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_EXPANDER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.isExpanded ? 'expander-open' : 'expander-closed';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceExpanderClass</a>
     */
    RESOURCE_EXPANDER_CLASS("resourceExpanderClass"),

    /**
     * Content of the expand/collapse toggle in front of expandable resource rows in the hierarchical timeline view.
     * The content is inserted inside the toggle, typically as an icon, and is hidden from assistive technology.
     * <dl>
     *   <dt>Type</dt> <dd>text | {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code isExpanded}.</dd>
     *   <dt>Returns</dt> <dd>a string, or an object like {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_EXPANDER_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return info.isExpanded ? '-' : '+';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceExpanderContent</a>
     */
    RESOURCE_EXPANDER_CONTENT("resourceExpanderContent"),

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
     * <p>
     * Set or removed on an attached calendar, it applies only to elements rendered afterwards, for example after
     * navigating, not to the elements already shown.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderDidMount</a>
     */
    RESOURCE_GROUP_HEADER_DID_MOUNT("resourceGroupHeaderDidMount"),

    /**
     * CSS classes for the inner wrapper of a resource group header cell in the timeline view. A group's header is
     * where its name is displayed, at the top of each row group.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *       field), {@code view}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_GROUP_HEADER_INNER_CLASS, "group-header-padding");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderInnerClass</a>
     */
    RESOURCE_GROUP_HEADER_INNER_CLASS("resourceGroupHeaderInnerClass"),

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
     * <p>
     * Set or removed on an attached calendar, it applies only to elements rendered afterwards, for example after
     * navigating, not to the elements already shown.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneDidMount</a>
     */
    RESOURCE_GROUP_LANE_DID_MOUNT("resourceGroupLaneDidMount"),

    /**
     * CSS classes for the inner wrapper of a resource group lane cell in the timeline view. A group's lane is the
     * horizontal area running along the time slots.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code fieldValue} (the value of the group
     *       field), {@code view}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_GROUP_LANE_INNER_CLASS, "group-lane-inner");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneInnerClass</a>
     */
    RESOURCE_GROUP_LANE_INNER_CLASS("resourceGroupLaneInnerClass"),

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
     * CSS classes for the header rows of the resource area in the timeline view.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated). A callback is not supported for this option.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_HEADER_ROW_CLASS, "resource-header-row");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-header-row-render-hooks">resourceHeaderRowClass</a>
     */
    RESOURCE_HEADER_ROW_CLASS("resourceHeaderRowClass"),

    /**
     * CSS classes for the indentation space in front of each resource row in the hierarchical timeline view.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated). A callback is not supported for this option.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_INDENT_CLASS, "resource-indent");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceIndentClass</a>
     */
    RESOURCE_INDENT_CLASS("resourceIndentClass"),

    /**
     * Adds CSS classes to the element below the entries of a resource lane.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} and {@code options}
     *                         (holds only {@code eventOverlap}).</dd>
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
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} and {@code options}
     *                         (holds only {@code eventOverlap}).</dd>
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
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} and {@code options}
     *                         (holds only {@code eventOverlap}).</dd>
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
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code options}
     *                         (holds only {@code eventOverlap}) and {@code el}.</dd>
     * </dl>
     * <pre>{@code
     * scheduler.setOption(SchedulerOption.RESOURCE_LANE_DID_MOUNT, JsCallback.of(
     *         "info => info.el.dataset.resourceId = info.resource.id"));
     * }</pre>
     * <p>
     * Set or removed on an attached calendar, it applies only to elements rendered afterwards, for example after
     * navigating, not to the elements already shown.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneDidMount</a>
     */
    RESOURCE_LANE_DID_MOUNT("resourceLaneDidMount"),

    /**
     * Adds CSS classes to the element above the entries of a resource lane.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} and {@code options}
     *                         (holds only {@code eventOverlap}).</dd>
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
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource} and {@code options}
     *                         (holds only {@code eventOverlap}).</dd>
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
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code resource}, {@code options}
     *                         (holds only {@code eventOverlap}) and {@code el}.</dd>
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
     * CSS classes for the body rows of the resource area in the timeline view. Does not apply to the resource group
     * header rows within the body.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated). A callback is not supported for this option.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.RESOURCE_ROW_CLASS, "resource-row");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-row-render-hooks">resourceRowClass</a>
     */
    RESOURCE_ROW_CLASS("resourceRowClass"),

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

    /**
     * CSS classes for the element below the entries in the timeline view without resources.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated). A callback is not supported for this option.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.TIMELINE_BOTTOM_CLASS, "timeline-bottom");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/timeline-render-hooks">timelineBottomClass</a>
     */
    TIMELINE_BOTTOM_CLASS("timelineBottomClass"),

    /**
     * CSS classes for the element above the entries in the timeline view without resources.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated). A callback is not supported for this option.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.TIMELINE_TOP_CLASS, "timeline-top");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/timeline-render-hooks">timelineTopClass</a>
     */
    TIMELINE_TOP_CLASS("timelineTopClass"),

    /**
     * Renders only the rows and columns currently visible in the scroll viewport. This improves performance when a
     * calendar has a large number of resources. Rows and columns that leave the viewport are removed from the DOM and
     * newly visible ones are rendered while the user scrolls.
     * <p>
     * Currently only applies to timeline views that display resources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(SchedulerOption.VIRTUALIZATION, true);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/virtualization">virtualization</a>
     */
    VIRTUALIZATION("virtualization"),

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

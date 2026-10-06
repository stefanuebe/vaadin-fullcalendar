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
     * In vertical resource view, display dates above resources instead of resources above dates.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/datesAboveResources">datesAboveResources</a>
     */
    DATES_ABOVE_RESOURCES("datesAboveResources"),

    /**
     * Allow dragging entries between resources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>inherits from {@link Option#EDITABLE}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventResourceEditable">eventResourceEditable</a>
     */
    ENTRY_RESOURCE_EDITABLE("eventResourceEditable"),

    /**
     * @deprecated use {@link #ENTRY_RESOURCE_EDITABLE}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    ENTRY_RESOURCES_EDITABLE("eventResourceEditable"),

    /**
     * Minimum pixel width of entries in timeline view.
     * <dl>
     *   <dt>Type</dt> <dd>{@code number} (pixels)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventMinWidth">eventMinWidth</a>
     */
    ENTRY_MIN_WIDTH("eventMinWidth"),

    /**
     * Only show resources that have entries assigned.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/filterResourcesWithEvents">filterResourcesWithEvents</a>
     */
    FILTER_RESOURCES_WITH_ENTRIES("filterResourcesWithEvents"),

    /**
     * FullCalendar Scheduler license key required for scheduler views to work.
     * <dl>
     *   <dt>Type</dt> <dd>{@code string}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/schedulerLicenseKey">schedulerLicenseKey</a>
     */
    LICENSE_KEY("schedulerLicenseKey"),

    /**
     * Re-fetch resources from the data provider when navigating to a different period.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/refetchResourcesOnNavigate">refetchResourcesOnNavigate</a>
     */
    REFETCH_RESOURCES_ON_NAVIGATE("refetchResourcesOnNavigate"),

    /**
     * Column definitions for the resource area (left side in timeline views).
     * Prefer {@link Scheduler#setResourceColumns(java.util.List)}. Passing a list of {@link ResourceColumn}
     * to {@link FullCalendarScheduler#setOption(SchedulerOption, Object)} is forwarded to it, so component
     * columns are bound. Raw JSON is sent as it is and cannot carry component columns.
     * <dl>
     *   <dt>Type</dt>    <dd>array of column configuration objects</dd>
     *   <dt>Default</dt> <dd>single column with resource name</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumns">resourceColumns</a>
     */
    RESOURCE_COLUMNS("resourceColumns"),

    /**
     * @deprecated use {@link #RESOURCE_COLUMNS}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_COLUMNS("resourceColumns"),

    /**
     * Custom content for the resource column header cell (top-left corner in timeline views).
     * <dl>
     *   <dt>Type</dt> <dd>{@code string} | HTML string | content object | {@link JsCallback}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderContent</a>
     */
    RESOURCE_COLUMN_HEADER_CONTENT("resourceColumnHeaderContent"),

    /**
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_CONTENT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_CONTENT("resourceColumnHeaderContent"),

    /**
     * Width of the resource area (left column in timeline views).
     * <dl>
     *   <dt>Type</dt>    <dd>CSS width string (e.g., {@code "200px"}, {@code "20%"})</dd>
     *   <dt>Default</dt> <dd>auto-calculated by FullCalendar</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumnsWidth">resourceColumnsWidth</a>
     */
    RESOURCE_COLUMNS_WIDTH("resourceColumnsWidth"),

    /**
     * @deprecated use {@link #RESOURCE_COLUMNS_WIDTH}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_WIDTH("resourceColumnsWidth"),

    /**
     * Field name in resource data used to group resources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string} (field name)</dd>
     *   <dt>Default</dt> <dd>none (no grouping)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceGroupField">resourceGroupField</a>
     */
    RESOURCE_GROUP_FIELD("resourceGroupField"),

    /**
     * Whether resource groups start in an expanded state.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesInitiallyExpanded">resourcesInitiallyExpanded</a>
     */
    RESOURCES_INITIALLY_EXPANDED("resourcesInitiallyExpanded"),

    /**
     * Default sort order for resources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string} | array of sort keys | {@code -1} for reverse order</dd>
     *   <dt>Default</dt> <dd>alphabetical by name</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceOrder">resourceOrder</a>
     */
    RESOURCE_ORDER("resourceOrder"),

    /**
     * Minimum pixel width of each time slot column in timeline view.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>auto-calculated</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinWidth">slotMinWidth</a>
     */
    SLOT_MIN_WIDTH("slotMinWidth"),


    // ---- Render hooks: Resource Cell (resource area cells in timeline views) ----
    /**
     * Add CSS classes to the resource cells of the resource area. A cell of a group row has no {@code resource}.
     * Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, field, fieldValue, el}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellClass</a>
     */
    RESOURCE_CELL_CLASS("resourceCellClass"),

    /**
     * Customize the content inside a resource cell.
     * Branch on {@code info.field === 'title'} for the title cell only. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, field, fieldValue, el}</dd>
     *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellContent</a>
     */
    RESOURCE_CELL_CONTENT("resourceCellContent"),

    /**
     * Called after a resource cell is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, field, fieldValue, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellDidMount</a>
     */
    RESOURCE_CELL_DID_MOUNT("resourceCellDidMount"),

    /**
     * Called before a resource cell is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, field, fieldValue, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellWillUnmount</a>
     */
    RESOURCE_CELL_WILL_UNMOUNT("resourceCellWillUnmount"),

    // ---- Render hooks: Resource Day Header (resource names in resource daygrid and timegrid views) ----
    /**
     * Add CSS classes to the column header cells that show resource names in vertical resource views.
     * Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, date, text, isToday, el, ...}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderClass</a>
     */
    RESOURCE_DAY_HEADER_CLASS("resourceDayHeaderClass"),

    /**
     * Customize the content inside a resource day header. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, date, text, isToday, el, ...}</dd>
     *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderContent</a>
     */
    RESOURCE_DAY_HEADER_CONTENT("resourceDayHeaderContent"),

    /**
     * Called after a resource day header is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, date, text, isToday, el, ...}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderDidMount</a>
     */
    RESOURCE_DAY_HEADER_DID_MOUNT("resourceDayHeaderDidMount"),

    /**
     * Called before a resource day header is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, date, text, isToday, el, ...}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderWillUnmount</a>
     */
    RESOURCE_DAY_HEADER_WILL_UNMOUNT("resourceDayHeaderWillUnmount"),

    // ---- Render hooks: Resource Lane ----
    /**
     * Add CSS classes to a resource lane. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, el}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneClass</a>
     */
    RESOURCE_LANE_CLASS("resourceLaneClass"),

    /**
     * @deprecated use {@link #RESOURCE_LANE_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_LANE_CLASS_NAMES("resourceLaneClass"),

    /**
     * Add CSS classes to the element above the entries of a resource lane. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, el}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneTopClass</a>
     */
    RESOURCE_LANE_TOP_CLASS("resourceLaneTopClass"),

    /**
     * Content inserted at the top of a resource lane, above its entries. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, el}</dd>
     *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneTopContent</a>
     */
    RESOURCE_LANE_TOP_CONTENT("resourceLaneTopContent"),

    /**
     * Add CSS classes to the element below the entries of a resource lane. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, el}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneBottomClass</a>
     */
    RESOURCE_LANE_BOTTOM_CLASS("resourceLaneBottomClass"),

    /**
     * Content inserted at the bottom of a resource lane, below its entries. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, el}</dd>
     *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneBottomContent</a>
     */
    RESOURCE_LANE_BOTTOM_CONTENT("resourceLaneBottomContent"),

    /**
     * Called after a resource lane element is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneDidMount</a>
     */
    RESOURCE_LANE_DID_MOUNT("resourceLaneDidMount"),

    /**
     * Called before a resource lane element is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code resource, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneWillUnmount</a>
     */
    RESOURCE_LANE_WILL_UNMOUNT("resourceLaneWillUnmount"),

    // ---- Render hooks: Resource Group Header ----
    /**
     * Add CSS classes to a resource group header, the cell that shows the group name at the top of each row group.
     * Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderClass</a>
     */
    RESOURCE_GROUP_HEADER_CLASS("resourceGroupHeaderClass"),

    /**
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_CLASS_NAMES("resourceGroupHeaderClass"),

    /**
     * Customize the content inside a resource group header. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderContent</a>
     */
    RESOURCE_GROUP_HEADER_CONTENT("resourceGroupHeaderContent"),

    /**
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_CONTENT}, which sets the same FullCalendar option.
     * The group value is now {@code info.fieldValue}.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_CONTENT("resourceGroupHeaderContent"),

    /**
     * Called after a resource group header is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderDidMount</a>
     */
    RESOURCE_GROUP_HEADER_DID_MOUNT("resourceGroupHeaderDidMount"),

    /**
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_DID_MOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_DID_MOUNT("resourceGroupHeaderDidMount"),

    /**
     * Called before a resource group header is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderWillUnmount</a>
     */
    RESOURCE_GROUP_HEADER_WILL_UNMOUNT("resourceGroupHeaderWillUnmount"),

    /**
     * @deprecated use {@link #RESOURCE_GROUP_HEADER_WILL_UNMOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_WILL_UNMOUNT("resourceGroupHeaderWillUnmount"),

    // ---- Render hooks: Resource Group Lane ----
    /**
     * Add CSS classes to a resource group lane. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneClass</a>
     */
    RESOURCE_GROUP_LANE_CLASS("resourceGroupLaneClass"),

    /**
     * @deprecated use {@link #RESOURCE_GROUP_LANE_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_GROUP_LANE_CLASS_NAMES("resourceGroupLaneClass"),

    /**
     * Customize the content inside a resource group lane. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneContent</a>
     */
    RESOURCE_GROUP_LANE_CONTENT("resourceGroupLaneContent"),

    /**
     * Called after a resource group lane is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneDidMount</a>
     */
    RESOURCE_GROUP_LANE_DID_MOUNT("resourceGroupLaneDidMount"),

    /**
     * Called before a resource group lane is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code fieldValue, el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneWillUnmount</a>
     */
    RESOURCE_GROUP_LANE_WILL_UNMOUNT("resourceGroupLaneWillUnmount"),

    // ---- Render hooks: Resource Column Header ----
    /**
     * Add CSS classes to the resource column header cell. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code el}</dd>
     *   <dt>Returns</dt>   <dd>a class name string. FullCalendar 7 drops arrays.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderClass</a>
     */
    RESOURCE_COLUMN_HEADER_CLASS("resourceColumnHeaderClass"),

    /**
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_CLASS}, which sets the same FullCalendar option.
     * Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_CLASS_NAMES("resourceColumnHeaderClass"),

    /**
     * Called after the resource column header is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderDidMount</a>
     */
    RESOURCE_COLUMN_HEADER_DID_MOUNT("resourceColumnHeaderDidMount"),

    /**
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_DID_MOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_DID_MOUNT("resourceColumnHeaderDidMount"),

    /**
     * Called before the resource column header is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code el}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderWillUnmount</a>
     */
    RESOURCE_COLUMN_HEADER_WILL_UNMOUNT("resourceColumnHeaderWillUnmount"),

    /**
     * @deprecated use {@link #RESOURCE_COLUMN_HEADER_WILL_UNMOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    RESOURCE_AREA_HEADER_WILL_UNMOUNT("resourceColumnHeaderWillUnmount"),

    // ---- Resource lifecycle callbacks ----
    /**
     * Called when a resource is added to the calendar. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {resource}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceAdd">resourceAdd</a>
     */
    RESOURCE_ADD("resourceAdd"),

    /**
     * Called when a resource is modified. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {oldResource, resource, revert}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceChange">resourceChange</a>
     */
    RESOURCE_CHANGE("resourceChange"),

    /**
     * Called when a resource is removed from the calendar. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {resource}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceRemove">resourceRemove</a>
     */
    RESOURCE_REMOVE("resourceRemove"),

    /**
     * Called after all resources are set (bulk). Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {resources}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesSet">resourcesSet</a>
     */
    RESOURCES_SET("resourcesSet"),

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

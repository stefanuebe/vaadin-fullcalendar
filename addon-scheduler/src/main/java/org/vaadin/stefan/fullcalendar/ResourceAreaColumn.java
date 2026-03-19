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

import lombok.NonNull;
import tools.jackson.databind.node.ObjectNode;

/**
 * Represents a column definition for the resource area sidebar in timeline and vertical resource views.
 * When {@link Scheduler#setResourceAreaColumns(java.util.List) resourceAreaColumns} is configured,
 * the resource area becomes a multi-column data grid with a header row.
 * <p>
 * Each column maps to a resource property name (from the resource's built-in fields or
 * {@link Resource#addExtendedProps(String, Object) extendedProps}).
 * <p>
 * Use the fluent {@code withXxx()} methods to configure optional properties:
 * <pre>{@code
 * ResourceAreaColumn deptCol = new ResourceAreaColumn("department", "Department")
 *     .withWidth("150px")
 *     .withGroup(true);
 * ResourceAreaColumn capCol = new ResourceAreaColumn("capacity", "Capacity")
 *     .withWidth("80px");
 * scheduler.setResourceAreaColumns(List.of(deptCol, capCol));
 * }</pre>
 *
 * @see Scheduler#setResourceAreaColumns(java.util.List)
 * @see <a href="https://fullcalendar.io/docs/resourceAreaColumns">FullCalendar resourceAreaColumns</a>
 */
public class ResourceAreaColumn {

    private final String field;
    private String headerContent;
    private boolean group;
    private String width;
    private String headerClassNames;
    private String headerDidMount;
    private String headerWillUnmount;
    private String cellContent;
    private String cellClassNames;
    private String cellDidMount;
    private String cellWillUnmount;

    /**
     * Creates a new column definition for the given resource field name.
     * The field name must match a property in the resource's JSON representation
     * (e.g., {@code "title"}, {@code "eventColor"}, or any key added via
     * {@link Resource#addExtendedProps(String, Object)}).
     *
     * @param field the resource property name to display in this column; must not be null
     */
    public ResourceAreaColumn(@NonNull String field) {
        this.field = field;
    }

    /**
     * Creates a new column definition with a field name and header text.
     * <p>
     * {@code headerContent} sets static text/HTML for the column header. The callback methods
     * ({@link #withHeaderClassNames(String)}, {@link #withHeaderDidMount(String)},
     * {@link #withHeaderWillUnmount(String)}) add dynamic behavior and are independent of
     * {@code headerContent}.
     *
     * @param field         the resource property name; must not be null
     * @param headerContent plain text or HTML string to display as the column header
     */
    public ResourceAreaColumn(@NonNull String field, String headerContent) {
        this.field = field;
        this.headerContent = headerContent;
    }

    /**
     * Sets the column width. Accepts any CSS width value.
     *
     * @param width CSS width string, e.g., {@code "150px"}, {@code "20%"}
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withWidth(String width) {
        this.width = width;
        return this;
    }

    /**
     * When {@code true}, designates this column as the one to display the group label in a multi-column
     * resource area. Setting this to {@code true} alone does NOT enable grouping — you must also call
     * {@link Scheduler#setResourceGroupField(String)} to enable resource grouping.
     * <p>
     * In a multi-column layout, only one column should have {@code group=true}, and it will show the
     * group header (e.g., "Sales", "Engineering") for grouped resources.
     *
     * @param group whether this column displays the group label
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withGroup(boolean group) {
        this.group = group;
        return this;
    }

    /**
     * Sets a JavaScript function string for dynamic CSS class names on the column header.
     * The function receives a {@code headerInfo} object and must return a string array.
     * <p>
     * Example: {@code "function(info) { return ['my-header']; }"}
     *
     * @param jsFunction JavaScript function string; must return a string array
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderClassNames(String jsFunction) {
        this.headerClassNames = jsFunction;
        return this;
    }

    /**
     * Sets a JavaScript function string called after the column header is added to the DOM.
     * The function receives a {@code headerInfo} object. The function's return value is ignored
     * by FullCalendar.
     * <p>
     * Example: {@code "function(info) { /* init tooltip *\/ }"}
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderDidMount(String jsFunction) {
        this.headerDidMount = jsFunction;
        return this;
    }

    /**
     * Sets a JavaScript function string called before the column header is removed from the DOM.
     * Use this to clean up any resources created in {@link #withHeaderDidMount(String)}.
     * The function's return value is ignored by FullCalendar.
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderWillUnmount(String jsFunction) {
        this.headerWillUnmount = jsFunction;
        return this;
    }

    /**
     * Sets a JavaScript function string or static string for the cell content of this column.
     * When a function is provided, it receives a {@code cellInfo} object and must return a string,
     * DOM node, or virtual DOM node (VNode) to render as the cell content.
     * <p>
     * Use this to customize how each resource's value is displayed in this column, beyond
     * simply showing the raw field value.
     * <p>
     * Example: {@code "function(info) { return info.fieldValue + ' hrs'; }"}
     *
     * @param cellContentOrJsFunction static string or JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellContent(String cellContentOrJsFunction) {
        this.cellContent = cellContentOrJsFunction;
        return this;
    }

    /**
     * Sets a JavaScript function string for dynamic CSS class names on each cell of this column.
     * The function receives a {@code cellInfo} object and must return a string array.
     * <p>
     * Example: {@code "function(info) { return info.resource.extendedProps.urgent ? ['urgent-cell'] : []; }"}
     *
     * @param jsFunction JavaScript function string; must return a string array
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellClassNames(String jsFunction) {
        this.cellClassNames = jsFunction;
        return this;
    }

    /**
     * Sets a JavaScript function string called after each cell of this column is added to the DOM.
     * The function receives a {@code cellInfo} object. The return value is ignored by FullCalendar.
     * <p>
     * Example: {@code "function(info) { /* attach tooltip *\/ }"}
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellDidMount(String jsFunction) {
        this.cellDidMount = jsFunction;
        return this;
    }

    /**
     * Sets a JavaScript function string called before each cell of this column is removed from the DOM.
     * Use this to clean up any resources created in {@link #withCellDidMount(String)}.
     * The return value is ignored by FullCalendar.
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellWillUnmount(String jsFunction) {
        this.cellWillUnmount = jsFunction;
        return this;
    }

    /**
     * Returns the resource property field name for this column.
     *
     * @return field name
     */
    public String getField() {
        return field;
    }

    /**
     * Returns the header content string, or {@code null} if not set.
     *
     * @return header content or null
     */
    public String getHeaderContent() {
        return headerContent;
    }

    /**
     * Returns whether resources are grouped by this column's field value.
     *
     * @return true if grouping is enabled for this column
     */
    public boolean isGroup() {
        return group;
    }

    /**
     * Returns the column width string, or {@code null} if not set.
     *
     * @return CSS width string or null
     */
    public String getWidth() {
        return width;
    }

    /**
     * Returns the header class names JavaScript function string, or {@code null} if not set.
     *
     * @return JS function string or null
     */
    public String getHeaderClassNames() {
        return headerClassNames;
    }

    /**
     * Returns the header did-mount JavaScript function string, or {@code null} if not set.
     *
     * @return JS function string or null
     */
    public String getHeaderDidMount() {
        return headerDidMount;
    }

    /**
     * Returns the header will-unmount JavaScript function string, or {@code null} if not set.
     *
     * @return JS function string or null
     */
    public String getHeaderWillUnmount() {
        return headerWillUnmount;
    }

    /**
     * Returns the cell content string or JavaScript function string, or {@code null} if not set.
     *
     * @return cell content or JS function string or null
     */
    public String getCellContent() {
        return cellContent;
    }

    /**
     * Returns the cell class names JavaScript function string, or {@code null} if not set.
     *
     * @return JS function string or null
     */
    public String getCellClassNames() {
        return cellClassNames;
    }

    /**
     * Returns the cell did-mount JavaScript function string, or {@code null} if not set.
     *
     * @return JS function string or null
     */
    public String getCellDidMount() {
        return cellDidMount;
    }

    /**
     * Returns the cell will-unmount JavaScript function string, or {@code null} if not set.
     *
     * @return JS function string or null
     */
    public String getCellWillUnmount() {
        return cellWillUnmount;
    }

    /**
     * Serializes this column definition to a JSON object for the FullCalendar
     * {@code resourceAreaColumns} option.
     *
     * @return JSON object node
     */
    public ObjectNode toJson() {
        ObjectNode json = JsonFactory.createObject();
        json.put("field", field);
        if (headerContent != null) json.put("headerContent", headerContent);
        if (width != null) json.put("width", width);
        if (group) json.put("group", true);
        if (headerClassNames != null) json.put("headerClassNames", headerClassNames);
        if (headerDidMount != null) json.put("headerDidMount", headerDidMount);
        if (headerWillUnmount != null) json.put("headerWillUnmount", headerWillUnmount);
        if (cellContent != null) json.put("cellContent", cellContent);
        if (cellClassNames != null) json.put("cellClassNames", cellClassNames);
        if (cellDidMount != null) json.put("cellDidMount", cellDidMount);
        if (cellWillUnmount != null) json.put("cellWillUnmount", cellWillUnmount);
        return json;
    }
}

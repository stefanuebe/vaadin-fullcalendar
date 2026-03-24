/*
 * Copyright 2026, Stefan Uebe
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
import elemental.json.JsonObject;

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
 *     .withWidth("80px")
 *     .withCellDidMount(JsCallback.of("function(info) { info.el.title = 'Tooltip'; }"));
 * scheduler.setResourceAreaColumns(List.of(deptCol, capCol));
 * }</pre>
 *
 * @see Scheduler#setResourceAreaColumns(java.util.List)
 * @see <a href="https://fullcalendar.io/docs/resourceAreaColumns">FullCalendar resourceAreaColumns</a>
 */
public class ResourceAreaColumn {

    private final String field;
    private Object headerContent; // String or JsCallback
    private boolean group;
    private String width;
    private Object headerClassNames; // String or JsCallback
    private JsCallback headerDidMount;
    private JsCallback headerWillUnmount;
    private Object cellContent; // String or JsCallback
    private Object cellClassNames; // String or JsCallback
    private JsCallback cellDidMount;
    private JsCallback cellWillUnmount;

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
     * {@code headerContent} sets static text/HTML for the column header.
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
     * When {@code true}, designates this column as the one to display the group label.
     *
     * @param group whether this column displays the group label
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withGroup(boolean group) {
        this.group = group;
        return this;
    }

    /**
     * Sets a static CSS class name string for the column header.
     *
     * @param classNames static class names string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderClassNames(String classNames) {
        this.headerClassNames = classNames;
        return this;
    }

    /**
     * Sets a JavaScript callback for dynamic CSS class names on the column header.
     *
     * @param callback JsCallback returning a string array
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderClassNames(JsCallback callback) {
        this.headerClassNames = callback;
        return this;
    }

    /**
     * Sets a JavaScript function string called after the column header is added to the DOM.
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderDidMount(String jsFunction) {
        this.headerDidMount = JsCallback.of(jsFunction);
        return this;
    }

    /**
     * Sets a JsCallback called after the column header is added to the DOM.
     *
     * @param callback JsCallback
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderDidMount(JsCallback callback) {
        this.headerDidMount = callback;
        return this;
    }

    /**
     * Sets a JavaScript function string called before the column header is removed from the DOM.
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderWillUnmount(String jsFunction) {
        this.headerWillUnmount = JsCallback.of(jsFunction);
        return this;
    }

    /**
     * Sets a JsCallback called before the column header is removed from the DOM.
     *
     * @param callback JsCallback
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withHeaderWillUnmount(JsCallback callback) {
        this.headerWillUnmount = callback;
        return this;
    }

    /**
     * Sets a static string for the cell content of this column.
     *
     * @param staticContent static string or HTML
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellContent(String staticContent) {
        this.cellContent = staticContent;
        return this;
    }

    /**
     * Sets a JsCallback for the cell content of this column.
     *
     * @param callback JsCallback
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellContent(JsCallback callback) {
        this.cellContent = callback;
        return this;
    }

    /**
     * Sets a static CSS class name string for each cell of this column.
     *
     * @param classNames static class names
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellClassNames(String classNames) {
        this.cellClassNames = classNames;
        return this;
    }

    /**
     * Sets a JsCallback for dynamic CSS class names on each cell.
     *
     * @param callback JsCallback returning a string array
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellClassNames(JsCallback callback) {
        this.cellClassNames = callback;
        return this;
    }

    /**
     * Sets a JavaScript function string called after each cell is added to the DOM.
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellDidMount(String jsFunction) {
        this.cellDidMount = JsCallback.of(jsFunction);
        return this;
    }

    /**
     * Sets a JsCallback called after each cell is added to the DOM.
     *
     * @param callback JsCallback
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellDidMount(JsCallback callback) {
        this.cellDidMount = callback;
        return this;
    }

    /**
     * Sets a JavaScript function string called before each cell is removed from the DOM.
     *
     * @param jsFunction JavaScript function string
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellWillUnmount(String jsFunction) {
        this.cellWillUnmount = JsCallback.of(jsFunction);
        return this;
    }

    /**
     * Sets a JsCallback called before each cell is removed from the DOM.
     *
     * @param callback JsCallback
     * @return this instance for fluent chaining
     */
    public ResourceAreaColumn withCellWillUnmount(JsCallback callback) {
        this.cellWillUnmount = callback;
        return this;
    }

    public String getField() {
        return field;
    }

    /**
     * Returns the header content (String or JsCallback), or {@code null} if not set.
     */
    public Object getHeaderContent() {
        return headerContent;
    }

    public boolean isGroup() {
        return group;
    }

    public String getWidth() {
        return width;
    }

    /**
     * Returns the header class names (String or JsCallback), or {@code null} if not set.
     */
    public Object getHeaderClassNames() {
        return headerClassNames;
    }

    public JsCallback getHeaderDidMount() {
        return headerDidMount;
    }

    public JsCallback getHeaderWillUnmount() {
        return headerWillUnmount;
    }

    /**
     * Returns the cell content (String or JsCallback), or {@code null} if not set.
     */
    public Object getCellContent() {
        return cellContent;
    }

    /**
     * Returns the cell class names (String or JsCallback), or {@code null} if not set.
     */
    public Object getCellClassNames() {
        return cellClassNames;
    }

    public JsCallback getCellDidMount() {
        return cellDidMount;
    }

    public JsCallback getCellWillUnmount() {
        return cellWillUnmount;
    }

    /**
     * Serializes this column definition to a JSON object for the FullCalendar
     * {@code resourceAreaColumns} option.
     *
     * @return JSON object node
     */
    public JsonObject toJson() {
        JsonObject json = JsonFactory.createObject();
        json.put("field", field);
        if (headerContent != null) serializeMixed(json, "headerContent", headerContent);
        if (width != null) json.put("width", width);
        if (group) json.put("group", true);
        if (headerClassNames != null) serializeMixed(json, "headerClassNames", headerClassNames);
        if (headerDidMount != null) json.put("headerDidMount", headerDidMount.toMarkerJson());
        if (headerWillUnmount != null) json.put("headerWillUnmount", headerWillUnmount.toMarkerJson());
        if (cellContent != null) serializeMixed(json, "cellContent", cellContent);
        if (cellClassNames != null) serializeMixed(json, "cellClassNames", cellClassNames);
        if (cellDidMount != null) json.put("cellDidMount", cellDidMount.toMarkerJson());
        if (cellWillUnmount != null) json.put("cellWillUnmount", cellWillUnmount.toMarkerJson());
        return json;
    }

    private static void serializeMixed(JsonObject json, String key, Object value) {
        if (value instanceof JsCallback) {
            json.put(key, ((JsCallback) value).toMarkerJson());
        } else {
            json.put(key, (String) value);
        }
    }
}

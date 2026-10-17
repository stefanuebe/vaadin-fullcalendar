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
package org.vaadin.stefan.fullcalendar.option;

import lombok.Getter;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.FullCalendarSchedulerOptions;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.converters.ContentValueConverter;
import tools.jackson.databind.node.ObjectNode;

/**
 * A column of the resource area in timeline views. When columns are set with
 * {@link FullCalendarSchedulerOptions#setResourceColumns(java.util.List) resourceColumns}, the resource area becomes a
 * grid of data with a header row.
 * <p>
 * Each column shows a resource property, a built-in field or a key of the
 * {@link Resource#setExtendedProp(String, Object) extended properties}. Besides the settings of
 * {@link AbstractResourceColumn}, it takes the content of its cells and their hooks.
 * <p>
 * For example:
 * <pre>{@code
 * ResourceColumn department = new ResourceColumn("department", "Department")
 *     .withWidth(150)
 *     .withGroup(true);
 * ResourceColumn capacity = new ResourceColumn("capacity", "Capacity")
 *     .withWidth(WidthValue.ofPercent(20))
 *     .withCellDidMount(JsCallback.of("info => info.el.title = 'Seats'"));
 * scheduler.options().setResourceColumns(List.of(department, capacity));
 * }</pre>
 *
 * @see FullCalendarSchedulerOptions#setResourceColumns(java.util.List)
 * @see <a href="https://fullcalendar.io/docs/resourceColumns">resourceColumns</a>
 */
@Getter
public class ResourceColumn extends AbstractResourceColumn<ResourceColumn> {

    private static final ContentValueConverter CONTENT = new ContentValueConverter();

    /** The content of each cell, or null for the value of the field. */
    private ContentValue cellContent;
    /** The callback that runs after each cell has been added to the DOM, or null for none. */
    private JsCallback cellDidMount;
    /** The callback that runs before each cell is removed from the DOM, or null for none. */
    private JsCallback cellWillUnmount;

    /**
     * Creates a column for the given resource property.
     *
     * @param field the resource property, such as {@code "title"} or a key added with
     *              {@link Resource#setExtendedProp(String, Object)}
     */
    public ResourceColumn(@NonNull String field) {
        super(field);
    }

    /**
     * Creates a column for the given resource property with a header text.
     *
     * @param field      the resource property
     * @param headerText the text of the column header, shown as text, or null for none
     */
    public ResourceColumn(@NonNull String field, String headerText) {
        super(field, headerText);
    }

    /**
     * Sets the content of each cell: a text, HTML or a callback, see {@link ContentValue}. Without it, a cell shows
     * the value of the field.
     *
     * @param content the content, or null for the value of the field
     * @return this column
     */
    public ResourceColumn withCellContent(ContentValue content) {
        this.cellContent = content;
        return this;
    }

    /**
     * Sets a text as the content of each cell. It is shown as text. For HTML, use
     * {@link #withCellContent(ContentValue)} with {@link ContentValue#ofHtml(String)}.
     *
     * @param text the text, or null for the value of the field
     * @return this column
     */
    public ResourceColumn withCellContent(String text) {
        return withCellContent(text == null ? null : TextContent.of(text));
    }

    /**
     * Sets a callback that runs after each cell has been added to the DOM.
     *
     * @param callback the callback, or null for none
     * @return this column
     */
    public ResourceColumn withCellDidMount(JsCallback callback) {
        this.cellDidMount = callback;
        return this;
    }

    /**
     * Sets a JavaScript function that runs after each cell has been added to the DOM.
     *
     * @param jsFunction the function, such as {@code "info => info.el.title = info.resource.title"}, or null for none
     * @return this column
     */
    public ResourceColumn withCellDidMount(String jsFunction) {
        return withCellDidMount(JsCallback.of(jsFunction));
    }

    /**
     * Sets a callback that runs before each cell is removed from the DOM.
     *
     * @param callback the callback, or null for none
     * @return this column
     */
    public ResourceColumn withCellWillUnmount(JsCallback callback) {
        this.cellWillUnmount = callback;
        return this;
    }

    /**
     * Sets a JavaScript function that runs before each cell is removed from the DOM.
     *
     * @param jsFunction the function, or null for none
     * @return this column
     */
    public ResourceColumn withCellWillUnmount(String jsFunction) {
        return withCellWillUnmount(JsCallback.of(jsFunction));
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode json = super.toJson();
        if (cellContent != null) {
            json.set("cellContent", CONTENT.toClientModel(cellContent, null));
        }
        putCallback(json, "cellDidMount", cellDidMount);
        putCallback(json, "cellWillUnmount", cellWillUnmount);

        return json;
    }
}

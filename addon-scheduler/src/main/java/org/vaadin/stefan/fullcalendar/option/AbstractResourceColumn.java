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
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ContentValueConverter;
import org.vaadin.stefan.fullcalendar.converters.WidthValueConverter;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;

/**
 * The settings every column of the resource area has, the library's {@code ColSpec} apart from the cell content and
 * its hooks: the field, the header, the width, the group and the CSS classes. {@link ResourceColumn} adds the cell
 * content, {@link ComponentResourceColumn} renders a component in each cell instead.
 * <p>
 * The {@code with…} methods change the column and return it as its own type {@code S}, so a chain keeps the subtype.
 * <p>
 * Two settings of {@code ColSpec} are left out. {@code headerDefault} takes a React node, which cannot come from the
 * server. {@code isMain} is overwritten by the library, which makes the first column that does not show the group
 * label carry the indent of hierarchical resources.
 *
 * @param <S> the type of the column, returned by the {@code with…} methods
 * @see FullCalendarSchedulerOptions#setResourceColumns(java.util.List)
 * @see <a href="https://fullcalendar.io/docs/resourceColumns">resourceColumns</a>
 */
@Getter
public abstract class AbstractResourceColumn<S extends AbstractResourceColumn<S>> implements Serializable {

    private static final ContentValueConverter CONTENT = new ContentValueConverter();
    private static final ClassNameValueConverter CLASS_NAME = new ClassNameValueConverter();
    private static final WidthValueConverter WIDTH = new WidthValueConverter();

    /** The resource property the column shows, such as {@code "title"} or the key of an extended property. */
    private final String field;
    /** The content of the column header, or null for none. */
    private ContentValue headerContent;
    /** Whether the column shows the group label, when the resources are grouped. */
    private boolean group;
    /** The width of the column, or null for the library default. */
    private WidthValue width;
    /** The CSS classes of the column header, or null for none. */
    private ClassNameValue headerClass;
    /** The CSS classes of the inner element of the column header, or null for none. */
    private ClassNameValue headerInnerClass;
    /** The CSS classes of the resizer of the column header, or null for none. */
    private ClassNameValue headerResizerClass;
    /** The callback that runs after the column header has been added to the DOM, or null for none. */
    private JsCallback headerDidMount;
    /** The callback that runs before the column header is removed from the DOM, or null for none. */
    private JsCallback headerWillUnmount;
    /** The CSS classes of each cell of the column, or null for none. */
    private ClassNameValue cellClass;
    /** The CSS classes of the inner element of each cell, or null for none. */
    private ClassNameValue cellInnerClass;

    /**
     * Creates a column for the given resource property.
     *
     * @param field the resource property, such as {@code "title"} or a key added with
     *              {@link Resource#setExtendedProp(String, Object)}
     */
    protected AbstractResourceColumn(@NonNull String field) {
        this.field = field;
    }

    /**
     * Creates a column for the given resource property with a header text.
     *
     * @param field      the resource property
     * @param headerText the text of the column header, shown as text, or null for none
     */
    protected AbstractResourceColumn(@NonNull String field, String headerText) {
        this(field);
        this.headerContent = headerText == null ? null : TextContent.of(headerText);
    }

    private static void putClassName(ObjectNode json, String key, ClassNameValue value) {
        if (value != null) {
            json.set(key, CLASS_NAME.toClientModel(value, null));
        }
    }

    /**
     * Writes the callback under the key, if it is not null.
     *
     * @param json     the column object
     * @param key      the key
     * @param callback the callback, or null
     */
    protected static void putCallback(ObjectNode json, String key, JsCallback callback) {
        if (callback != null) {
            json.set(key, callback.toMarkerJson());
        }
    }

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    /**
     * Sets the content of the column header: a text, HTML or a callback, see {@link ContentValue}.
     *
     * @param content the content, or null for none
     * @return this column
     */
    public S withHeaderContent(ContentValue content) {
        this.headerContent = content;
        return self();
    }

    /**
     * Sets a text as the content of the column header. It is shown as text. For HTML, use
     * {@link #withHeaderContent(ContentValue)} with {@link ContentValue#ofHtml(String)}.
     *
     * @param text the text, or null for none
     * @return this column
     */
    public S withHeaderContent(String text) {
        return withHeaderContent(text == null ? null : TextContent.of(text));
    }

    /**
     * Sets the width of the column: pixels or a percentage of the resource area, see {@link WidthValue}.
     *
     * @param width the width, or null for the library default
     * @return this column
     */
    public S withWidth(WidthValue width) {
        this.width = width;
        return self();
    }

    /**
     * Sets the width of the column in pixels.
     *
     * @param pixels the width in pixels, not negative
     * @return this column
     * @throws IllegalArgumentException when the width is negative
     */
    public S withWidth(int pixels) {
        return withWidth(WidthValue.ofPixels(pixels));
    }

    /**
     * Sets whether the column shows the group label, when the resources are grouped.
     *
     * @param group whether the column shows the group label
     * @return this column
     */
    public S withGroup(boolean group) {
        this.group = group;
        return self();
    }

    /**
     * Sets the CSS classes of the column header: class names or a callback, see {@link ClassNameValue}.
     *
     * @param className the classes, or null for none
     * @return this column
     */
    public S withHeaderClass(ClassNameValue className) {
        this.headerClass = className;
        return self();
    }

    /**
     * Sets fixed CSS classes for the column header.
     *
     * @param classNames the class names, separated by spaces, or null for none
     * @return this column
     */
    public S withHeaderClass(String classNames) {
        return withHeaderClass(classNames == null ? null : ClassName.of(classNames));
    }

    /**
     * Sets the CSS classes of the inner element of the column header: class names or a callback, see
     * {@link ClassNameValue}.
     *
     * @param className the classes, or null for none
     * @return this column
     */
    public S withHeaderInnerClass(ClassNameValue className) {
        this.headerInnerClass = className;
        return self();
    }

    /**
     * Sets fixed CSS classes for the inner element of the column header.
     *
     * @param classNames the class names, separated by spaces, or null for none
     * @return this column
     */
    public S withHeaderInnerClass(String classNames) {
        return withHeaderInnerClass(classNames == null ? null : ClassName.of(classNames));
    }

    /**
     * Sets the CSS classes of the resizer of the column header: class names or a callback, see
     * {@link ClassNameValue}.
     *
     * @param className the classes, or null for none
     * @return this column
     */
    public S withHeaderResizerClass(ClassNameValue className) {
        this.headerResizerClass = className;
        return self();
    }

    /**
     * Sets fixed CSS classes for the resizer of the column header.
     *
     * @param classNames the class names, separated by spaces, or null for none
     * @return this column
     */
    public S withHeaderResizerClass(String classNames) {
        return withHeaderResizerClass(classNames == null ? null : ClassName.of(classNames));
    }

    /**
     * Sets a callback that runs after the column header has been added to the DOM.
     *
     * @param callback the callback, or null for none
     * @return this column
     */
    public S withHeaderDidMount(JsCallback callback) {
        this.headerDidMount = callback;
        return self();
    }

    /**
     * Sets a JavaScript function that runs after the column header has been added to the DOM.
     *
     * @param jsFunction the function, such as {@code "info => info.el.title = 'Name'"}, or null for none
     * @return this column
     */
    public S withHeaderDidMount(String jsFunction) {
        return withHeaderDidMount(JsCallback.of(jsFunction));
    }

    /**
     * Sets a callback that runs before the column header is removed from the DOM.
     *
     * @param callback the callback, or null for none
     * @return this column
     */
    public S withHeaderWillUnmount(JsCallback callback) {
        this.headerWillUnmount = callback;
        return self();
    }

    /**
     * Sets a JavaScript function that runs before the column header is removed from the DOM.
     *
     * @param jsFunction the function, or null for none
     * @return this column
     */
    public S withHeaderWillUnmount(String jsFunction) {
        return withHeaderWillUnmount(JsCallback.of(jsFunction));
    }

    /**
     * Sets the CSS classes of each cell of the column: class names or a callback, see {@link ClassNameValue}.
     *
     * @param className the classes, or null for none
     * @return this column
     */
    public S withCellClass(ClassNameValue className) {
        this.cellClass = className;
        return self();
    }

    /**
     * Sets fixed CSS classes for each cell of the column.
     *
     * @param classNames the class names, separated by spaces, or null for none
     * @return this column
     */
    public S withCellClass(String classNames) {
        return withCellClass(classNames == null ? null : ClassName.of(classNames));
    }

    /**
     * Sets the CSS classes of the inner element of each cell: class names or a callback, see
     * {@link ClassNameValue}.
     *
     * @param className the classes, or null for none
     * @return this column
     */
    public S withCellInnerClass(ClassNameValue className) {
        this.cellInnerClass = className;
        return self();
    }

    /**
     * Sets fixed CSS classes for the inner element of each cell.
     *
     * @param classNames the class names, separated by spaces, or null for none
     * @return this column
     */
    public S withCellInnerClass(String classNames) {
        return withCellInnerClass(classNames == null ? null : ClassName.of(classNames));
    }

    /**
     * Writes the column as the library reads it, an object of the {@code resourceColumns} option.
     *
     * @return the column object
     */
    public ObjectNode toJson() {
        ObjectNode json = JsonFactory.createObject();
        json.put("field", field);
        if (headerContent != null) {
            json.set("headerContent", CONTENT.toClientModel(headerContent, null));
        }
        if (width != null) {
            json.set("width", WIDTH.toClientModel(width, null));
        }
        if (group) {
            json.put("group", true);
        }
        putClassName(json, "headerClass", headerClass);
        putClassName(json, "headerInnerClass", headerInnerClass);
        putClassName(json, "headerResizerClass", headerResizerClass);
        putCallback(json, "headerDidMount", headerDidMount);
        putCallback(json, "headerWillUnmount", headerWillUnmount);
        putClassName(json, "cellClass", cellClass);
        putClassName(json, "cellInnerClass", cellInnerClass);

        return json;
    }
}

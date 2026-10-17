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

import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.converters.WidthValueConverter;

/**
 * Former name of {@link ResourceColumn}. Its {@code with…} methods that existed before return
 * {@code ResourceAreaColumn}, so existing code keeps compiling.
 *
 * @deprecated use {@link ResourceColumn}. The library calls them resource columns
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class ResourceAreaColumn extends ResourceColumn {

    private static final WidthValueConverter WIDTH = new WidthValueConverter();

    public ResourceAreaColumn(@NonNull String field) {
        super(field);
    }

    public ResourceAreaColumn(@NonNull String field, String headerContent) {
        super(field, headerContent);
    }

    @Override
    public ResourceAreaColumn withGroup(boolean group) {
        super.withGroup(group);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderClass(ClassNameValue className) {
        super.withHeaderClass(className);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderClass(String classNames) {
        super.withHeaderClass(classNames);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderDidMount(JsCallback callback) {
        super.withHeaderDidMount(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderDidMount(String jsFunction) {
        super.withHeaderDidMount(jsFunction);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderWillUnmount(JsCallback callback) {
        super.withHeaderWillUnmount(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderWillUnmount(String jsFunction) {
        super.withHeaderWillUnmount(jsFunction);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellClass(ClassNameValue className) {
        super.withCellClass(className);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellClass(String classNames) {
        super.withCellClass(classNames);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellContent(ContentValue content) {
        super.withCellContent(content);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellContent(String text) {
        super.withCellContent(text);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellDidMount(JsCallback callback) {
        super.withCellDidMount(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellDidMount(String jsFunction) {
        super.withCellDidMount(jsFunction);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellWillUnmount(JsCallback callback) {
        super.withCellWillUnmount(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellWillUnmount(String jsFunction) {
        super.withCellWillUnmount(jsFunction);
        return this;
    }

    /**
     * Former way to set the width as a string. It takes a whole number of pixels or a percentage, such as
     * {@code "150px"} or {@code "20%"}. Any other string leaves the width unset.
     *
     * @param width the width, such as {@code "150px"} or {@code "20%"}, or null for the library default
     * @return this column
     * @deprecated use {@link #withWidth(int)} or {@link #withWidth(WidthValue)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withWidth(String width) {
        WidthValue value;
        try {
            value = width == null ? null : WIDTH.toServerModel(JsonUtils.toJsonNode(width), null);
        } catch (IllegalArgumentException e) {
            value = null;
        }
        withWidth(value);

        return this;
    }

    /**
     * Former name of {@link #withHeaderClass(String)}. Delegates to it.
     *
     * @param classNames the class names, separated by spaces, or null for none
     * @return this column
     * @deprecated use {@link #withHeaderClass(String)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withHeaderClassNames(String classNames) {
        return withHeaderClass(classNames);
    }

    /**
     * Former name of {@link #withHeaderClass(ClassNameValue)}, which also takes a {@link JsCallback}. Delegates to
     * it.
     *
     * @param className the classes or callback, or null for none
     * @return this column
     * @deprecated use {@link #withHeaderClass(ClassNameValue)}. A callback returns a class name string
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withHeaderClassNames(ClassNameValue className) {
        return withHeaderClass(className);
    }

    /**
     * Former name of {@link #withCellClass(String)}. Delegates to it.
     *
     * @param classNames the class names, separated by spaces, or null for none
     * @return this column
     * @deprecated use {@link #withCellClass(String)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withCellClassNames(String classNames) {
        return withCellClass(classNames);
    }

    /**
     * Former name of {@link #withCellClass(ClassNameValue)}, which also takes a {@link JsCallback}. Delegates to
     * it.
     *
     * @param className the classes or callback, or null for none
     * @return this column
     * @deprecated use {@link #withCellClass(ClassNameValue)}. A callback returns a class name string
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withCellClassNames(ClassNameValue className) {
        return withCellClass(className);
    }

    /**
     * Former name of {@link #getHeaderClass()}. Delegates to it.
     *
     * @return the classes of the column header, or null
     * @deprecated use {@link #getHeaderClass()}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ClassNameValue getHeaderClassNames() {
        return getHeaderClass();
    }

    /**
     * Former name of {@link #getCellClass()}. Delegates to it.
     *
     * @return the classes of each cell, or null
     * @deprecated use {@link #getCellClass()}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ClassNameValue getCellClassNames() {
        return getCellClass();
    }
}

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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.converters.WidthValueConverter;

/**
 * Former name of {@link ComponentResourceColumn}. Its {@code with…} methods that existed before return
 * {@code ComponentResourceAreaColumn}, so existing code keeps compiling.
 *
 * @param <T> the component type returned by the factory callback
 * @deprecated use {@link ComponentResourceColumn}. The library calls them resource columns
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class ComponentResourceAreaColumn<T extends Component> extends ComponentResourceColumn<T> {

    private static final WidthValueConverter WIDTH = new WidthValueConverter();

    public ComponentResourceAreaColumn(@NonNull String field, String headerContent,
                                       @NonNull SerializableFunction<Resource, T> componentFactory) {
        super(field, headerContent, componentFactory);
    }

    public ComponentResourceAreaColumn(@NonNull String field,
                                       @NonNull SerializableFunction<Resource, T> componentFactory) {
        super(field, componentFactory);
    }

    @Override
    public ComponentResourceAreaColumn<T> withGroup(boolean group) {
        super.withGroup(group);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderClass(ClassNameValue className) {
        super.withHeaderClass(className);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderClass(String classNames) {
        super.withHeaderClass(classNames);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderDidMount(JsCallback callback) {
        super.withHeaderDidMount(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderDidMount(String jsFunction) {
        super.withHeaderDidMount(jsFunction);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderWillUnmount(JsCallback callback) {
        super.withHeaderWillUnmount(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderWillUnmount(String jsFunction) {
        super.withHeaderWillUnmount(jsFunction);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withCellClass(ClassNameValue className) {
        super.withCellClass(className);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withCellClass(String classNames) {
        super.withCellClass(classNames);
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
    public ComponentResourceAreaColumn<T> withWidth(String width) {
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
    public ComponentResourceAreaColumn<T> withHeaderClassNames(String classNames) {
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
    public ComponentResourceAreaColumn<T> withHeaderClassNames(ClassNameValue className) {
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
    public ComponentResourceAreaColumn<T> withCellClassNames(String classNames) {
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
    public ComponentResourceAreaColumn<T> withCellClassNames(ClassNameValue className) {
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

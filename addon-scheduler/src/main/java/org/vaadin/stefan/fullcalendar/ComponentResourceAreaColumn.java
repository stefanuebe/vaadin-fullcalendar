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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import lombok.NonNull;

/**
 * Former name of {@link ComponentResourceColumn}. Its fluent methods return {@code ComponentResourceAreaColumn},
 * so existing code keeps compiling. It is a {@link ResourceColumn}, but not a {@link ResourceAreaColumn}.
 *
 * @param <T> the component type returned by the factory callback
 * @deprecated use {@link ComponentResourceColumn}. FullCalendar 7 calls them resource columns
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class ComponentResourceAreaColumn<T extends Component> extends ComponentResourceColumn<T> {

    public ComponentResourceAreaColumn(@NonNull String field, String headerContent,
                                       @NonNull SerializableFunction<Resource, T> componentFactory) {
        super(field, headerContent, componentFactory);
    }

    public ComponentResourceAreaColumn(@NonNull String field,
                                       @NonNull SerializableFunction<Resource, T> componentFactory) {
        super(field, componentFactory);
    }

    @Override
    public ComponentResourceAreaColumn<T> withWidth(String width) {
        super.withWidth(width);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withGroup(boolean group) {
        super.withGroup(group);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderClass(String className) {
        super.withHeaderClass(className);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderClass(JsCallback callback) {
        super.withHeaderClass(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderDidMount(String jsFunction) {
        super.withHeaderDidMount(jsFunction);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderDidMount(JsCallback callback) {
        super.withHeaderDidMount(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderWillUnmount(String jsFunction) {
        super.withHeaderWillUnmount(jsFunction);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderWillUnmount(JsCallback callback) {
        super.withHeaderWillUnmount(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withCellClass(String className) {
        super.withCellClass(className);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withCellClass(JsCallback callback) {
        super.withCellClass(callback);
        return this;
    }

    /**
     * Former name of {@link #withHeaderClass(String)}: sets a static CSS class name string for the column
     * header. Separate several classes with spaces. Delegates to it.
     *
     * @param classNames static class name string
     * @return this instance for fluent chaining
     * @deprecated use {@link #withHeaderClass(String)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ComponentResourceAreaColumn<T> withHeaderClassNames(String classNames) {
        return withHeaderClass(classNames);
    }

    /**
     * Former name of {@link #withHeaderClass(JsCallback)}: sets a JavaScript callback for dynamic CSS
     * class names on the column header. Delegates to it.
     *
     * @param callback JsCallback returning a class name string
     * @return this instance for fluent chaining
     * @deprecated use {@link #withHeaderClass(JsCallback)}. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ComponentResourceAreaColumn<T> withHeaderClassNames(JsCallback callback) {
        return withHeaderClass(callback);
    }

    /**
     * Former name of {@link #withCellClass(String)}: sets a static CSS class name string for each cell of
     * this column. Separate several classes with spaces. Delegates to it.
     *
     * @param classNames static class name string
     * @return this instance for fluent chaining
     * @deprecated use {@link #withCellClass(String)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ComponentResourceAreaColumn<T> withCellClassNames(String classNames) {
        return withCellClass(classNames);
    }

    /**
     * Former name of {@link #withCellClass(JsCallback)}: sets a JsCallback for dynamic CSS class names on
     * each cell. Delegates to it.
     *
     * @param callback JsCallback returning a class name string
     * @return this instance for fluent chaining
     * @deprecated use {@link #withCellClass(JsCallback)}. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ComponentResourceAreaColumn<T> withCellClassNames(JsCallback callback) {
        return withCellClass(callback);
    }

    /**
     * Former name of {@link #getHeaderClass()}: returns the header class (String or JsCallback), or {@code
     * null} if not set. Delegates to it.
     *
     * @return header class or null
     * @deprecated use {@link #getHeaderClass()}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public Object getHeaderClassNames() {
        return getHeaderClass();
    }

    /**
     * Former name of {@link #getCellClass()}: returns the cell class (String or JsCallback), or {@code
     * null} if not set. Delegates to it.
     *
     * @return cell class or null
     * @deprecated use {@link #getCellClass()}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public Object getCellClassNames() {
        return getCellClass();
    }
}

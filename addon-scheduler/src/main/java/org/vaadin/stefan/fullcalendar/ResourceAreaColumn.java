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

/**
 * Former name of {@link ResourceColumn}. Its fluent methods return {@code ResourceAreaColumn}, so existing code
 * keeps compiling.
 *
 * @deprecated use {@link ResourceColumn}. FullCalendar 7 calls them resource columns
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class ResourceAreaColumn extends ResourceColumn {

    public ResourceAreaColumn(@NonNull String field) {
        super(field);
    }

    public ResourceAreaColumn(@NonNull String field, String headerContent) {
        super(field, headerContent);
    }

    @Override
    public ResourceAreaColumn withWidth(String width) {
        super.withWidth(width);
        return this;
    }

    @Override
    public ResourceAreaColumn withGroup(boolean group) {
        super.withGroup(group);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderClass(String className) {
        super.withHeaderClass(className);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderClass(JsCallback callback) {
        super.withHeaderClass(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderDidMount(String jsFunction) {
        super.withHeaderDidMount(jsFunction);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderDidMount(JsCallback callback) {
        super.withHeaderDidMount(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderWillUnmount(String jsFunction) {
        super.withHeaderWillUnmount(jsFunction);
        return this;
    }

    @Override
    public ResourceAreaColumn withHeaderWillUnmount(JsCallback callback) {
        super.withHeaderWillUnmount(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellContent(String staticContent) {
        super.withCellContent(staticContent);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellContent(JsCallback callback) {
        super.withCellContent(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellClass(String className) {
        super.withCellClass(className);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellClass(JsCallback callback) {
        super.withCellClass(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellDidMount(String jsFunction) {
        super.withCellDidMount(jsFunction);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellDidMount(JsCallback callback) {
        super.withCellDidMount(callback);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellWillUnmount(String jsFunction) {
        super.withCellWillUnmount(jsFunction);
        return this;
    }

    @Override
    public ResourceAreaColumn withCellWillUnmount(JsCallback callback) {
        super.withCellWillUnmount(callback);
        return this;
    }

    /**
     * @deprecated use {@link #withHeaderClass(String)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withHeaderClassNames(String classNames) {
        return withHeaderClass(classNames);
    }

    /**
     * @deprecated use {@link #withHeaderClass(JsCallback)}. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withHeaderClassNames(JsCallback callback) {
        return withHeaderClass(callback);
    }

    /**
     * @deprecated use {@link #withCellClass(String)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withCellClassNames(String classNames) {
        return withCellClass(classNames);
    }

    /**
     * @deprecated use {@link #withCellClass(JsCallback)}. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public ResourceAreaColumn withCellClassNames(JsCallback callback) {
        return withCellClass(callback);
    }

    /**
     * @deprecated use {@link #getHeaderClass()}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public Object getHeaderClassNames() {
        return getHeaderClass();
    }

    /**
     * @deprecated use {@link #getCellClass()}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public Object getCellClassNames() {
        return getCellClass();
    }
}

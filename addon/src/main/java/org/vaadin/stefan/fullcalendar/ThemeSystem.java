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

/**
 * Defines the theme system used by FullCalendar for styling. Controls which CSS framework is
 * integrated with the calendar rendering.
 * <p>
 * The default is {@link #STANDARD}, which uses FullCalendar's own built-in CSS.
 *
 * @see <a href="https://fullcalendar.io/docs/themeSystem">FC themeSystem documentation</a>
 */
public enum ThemeSystem implements ClientSideValue {

    /**
     * Default FullCalendar styling. No external CSS framework required.
     */
    STANDARD("standard"),

    /**
     * Bootstrap 5 integration. Requires Bootstrap 5 CSS to be loaded separately in the application.
     */
    BOOTSTRAP5("bootstrap5"),

    /**
     * Bootstrap 4 integration. Requires Bootstrap 4 CSS and the {@code @fullcalendar/bootstrap} plugin.
     *
     * @deprecated Bootstrap 4 support is deprecated in FC v6; prefer {@link #BOOTSTRAP5}.
     *             Using BOOTSTRAP with FC v6 may result in unstyled or incorrectly styled components.
     */
    @Deprecated
    BOOTSTRAP("bootstrap");

    private final String clientSideValue;

    ThemeSystem(String clientSideValue) {
        this.clientSideValue = clientSideValue;
    }

    @Override
    public String getClientSideValue() {
        return clientSideValue;
    }
}

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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The palettes of the FullCalendar stock themes, as shipped with the FullCalendar version of the add-on
 * ({@link FullCalendar#FC_CLIENT_VERSION}).
 * <p>
 * A palette is a stylesheet that sets the color variables of a stock theme for the whole page. The add-on loads the
 * default palette of the theme a calendar uses. To use another one, import it in your application, e.g.
 * {@code @CssImport("fullcalendar/themes/monarch/palettes/green.css")}. See
 * <a href="https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Themes#colors-and-palettes">Colors and
 * palettes</a> in the wiki.
 *
 * @see FullCalendarTheme
 * @see <a href="https://fullcalendar.io/docs/color-palettes">Color palettes</a>
 */
public final class FullCalendarPalette {

    // the palettes of FullCalendar.FC_CLIENT_VERSION, the default palette first
    private static final Map<String, List<String>> PALETTES = Map.of(
            FullCalendarTheme.MONARCH, List.of("purple", "blue", "green", "red", "yellow"),
            FullCalendarTheme.BREEZY, List.of("indigo", "amber", "emerald", "rose"),
            FullCalendarTheme.FORMA, List.of("blue", "green", "purple", "red"),
            FullCalendarTheme.PULSE, List.of("red", "blue", "green", "purple"));

    private FullCalendarPalette() {
    }

    /**
     * Returns the names of the palettes of the given stock theme, the default palette first. Returns an empty list for
     * {@link FullCalendarTheme#VAADIN}, for {@link FullCalendarTheme#CLASSIC}, whose only palette has no name, and for
     * any other name, e.g. a custom theme.
     *
     * @param fullCalendarThemeName the name of the theme, e.g. {@link FullCalendarTheme#MONARCH}
     * @return the palette names, unmodifiable
     * @throws NullPointerException if the name is null
     */
    public static List<String> availablePalettesFor(String fullCalendarThemeName) {
        Objects.requireNonNull(fullCalendarThemeName, "fullCalendarThemeName");

        return PALETTES.getOrDefault(fullCalendarThemeName, List.of());
    }
}

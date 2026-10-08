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

/**
 * Names of the FullCalendar themes that come with the add-on, for {@link FullCalendar#setTheme(String)}.
 * <p>
 * FullCalendar has its own built-in theme mechanism to style its grid, its entries and its toolbar.
 * <p>
 * The default, {@link #VAADIN}, takes its look from the Vaadin
 * application theme (Lumo / Aura), so that the calendar matches the other Vaadin components.
 * Additional to the Vaadin theme, the add-on ships the five FullCalendar stock themes: classic, monarch, breezy, forma and pulse.
 * <p>
 * A stock theme takes its colors from a palette, a stylesheet that sets CSS color variables for the whole page, named
 * {@code --fc-<theme>-*}, e.g. {@code --fc-monarch-background}. Classic has one palette, the other stock themes have
 * several. The add-on loads the default palette of the theme.
 * <p>
 *     <b>Note:</b> Because a palette applies to the whole page, all
 * calendars with the same theme share its colors.
 * <p>
 * To change colors, override the color variables or load another palette in a stylesheet of your application. See
 * <a href="https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Themes#colors-and-palettes">Colors and
 * palettes</a> in the wiki for details and an example.
 * <p>
 * You can register your own custom theme using javascript:
 * <pre>{@code
 * // your frontend js file, loaded with the @JsModule annotation in Java
 * import {FullCalendar} from 'Frontend/generated/jar-resources/vaadin-full-calendar/full-calendar';
 * FullCalendar.registerTheme('corporate', () => import('./corporate-theme'));
 *
 * // in Java
 * calendar.setTheme("corporate");
 * }</pre>
 * <p>
 * The browser loads a theme only when a calendar on the page uses it, so themes that no calendar uses cost nothing.
 *
 * @see <a href="https://fullcalendar.io/docs/stock-themes">Stock themes</a>
 * @see <a href="https://fullcalendar.io/docs/color-palettes">Color palettes</a>
 */
public final class FullCalendarTheme {

    /** The add-on's own FullCalendar theme, which takes its look from the Vaadin application theme. The default. */
    public static final String VAADIN = "vaadin";
    /** FullCalendar's classic theme, the look of FullCalendar 6. */
    public static final String CLASSIC = "classic";
    /** FullCalendar's monarch theme, inspired by Material Design 3. Default palette: purple. */
    public static final String MONARCH = "monarch";
    /** FullCalendar's breezy theme, inspired by Tailwind Plus. Default palette: indigo. */
    public static final String BREEZY = "breezy";
    /** FullCalendar's forma theme, inspired by Fluent UI. Default palette: blue. */
    public static final String FORMA = "forma";
    /** FullCalendar's pulse theme, a minimal Apple-like design. Default palette: red. */
    public static final String PULSE = "pulse";

    private FullCalendarTheme() {
    }
}

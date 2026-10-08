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
 * Names of the positions and items of FullCalendar's built-in toolbar, for the map passed to
 * {@link Option#HEADER_TOOLBAR} and {@link Option#FOOTER_TOOLBAR}.
 * <p>
 * The constants only affect FullCalendar's built-in header and footer toolbar, not a Vaadin component. The add-on hides
 * the header toolbar by default, and FullCalendar shows no footer toolbar by default. Setting
 * {@link Option#HEADER_TOOLBAR} or {@link Option#FOOTER_TOOLBAR} shows the respective toolbar.
 * <p>
 * The map goes from a position to a string of items. Items separated by a comma are shown adjacent, items
 * separated by a space with a small gap. A view button is the view's name, see
 * {@link CalendarView#getClientSideValue()}.
 * <pre>{@code
 * calendar.setOption(Option.HEADER_TOOLBAR, Map.of(
 *         NativeToolbarParts.START,
 *                 NativeToolbarParts.PREV + "," + NativeToolbarParts.NEXT + " " + NativeToolbarParts.TODAY,
 *         NativeToolbarParts.CENTER, NativeToolbarParts.TITLE,
 *         NativeToolbarParts.END, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue() + ","
 *                 + CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue()));
 * }</pre>
 *
 * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
 */
public final class NativeToolbarParts {

    /** Position at the start of the toolbar, left in a left-to-right layout. */
    public static final String START = "start";
    /** Position in the center of the toolbar. */
    public static final String CENTER = "center";
    /** Position at the end of the toolbar, right in a left-to-right layout. */
    public static final String END = "end";

    /** Text with the current month, week or day. */
    public static final String TITLE = "title";
    /** Button that moves the calendar back one month, week or day. */
    public static final String PREV = "prev";
    /** Button that moves the calendar forward one month, week or day. */
    public static final String NEXT = "next";
    /** Button that moves the calendar back one year. */
    public static final String PREV_YEAR = "prevYear";
    /** Button that moves the calendar forward one year. */
    public static final String NEXT_YEAR = "nextYear";
    /** Button that moves the calendar to the current month, week or day. */
    public static final String TODAY = "today";

    private NativeToolbarParts() {
    }
}

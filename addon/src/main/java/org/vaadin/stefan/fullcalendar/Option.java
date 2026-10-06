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

import org.apache.commons.text.CaseUtils;
import org.vaadin.stefan.fullcalendar.converters.BusinessHoursConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekArrayConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekConverter;
import org.vaadin.stefan.fullcalendar.converters.DurationConverter;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.converters.LocalDateConverter;
import org.vaadin.stefan.fullcalendar.converters.LocaleConverter;
import org.vaadin.stefan.fullcalendar.converters.ToolbarConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.model.Footer;
import org.vaadin.stefan.fullcalendar.model.Header;
import tools.jackson.databind.JsonNode;

import java.time.*;
import java.util.*;

/**
 * Enumeration of options that can be applied to the calendar via
 * {@link FullCalendar#setOption(Option, Object)}. Contains only options that affect
 * the client-side library, not internal options. Missing options can be set manually
 * via one of the {@link FullCalendar#setOption} overloads using a raw string key.
 *
 * <p><b>Naming convention:</b> Java constant names use the {@code ENTRY_} prefix
 * where the underlying FullCalendar JS option uses {@code event}
 * (e.g., {@code ENTRY_CONTRAST_COLOR} → {@code eventContrastColor}).
 * Constants that provide an explicit string key via their constructor override this rule.
 * Deprecated constants are aliases of a renamed constant and set the same FullCalendar option.
 * Note: the {@code ENTRY} → {@code EVENT} substitution is applied to all occurrences of
 * {@code ENTRY} in the constant name, not only at the prefix. For example,
 * {@code DISPLAY_ENTRY_TIME} maps to {@code displayEventTime}.
 *
 * <p><b>Format objects</b> (used by {@link #DAY_HEADER_FORMAT}, {@link #SLOT_HEADER_FORMAT},
 * {@link #ENTRY_TIME_FORMAT}, {@link #LIST_DAY_FORMAT}, {@link #LIST_DAY_ALT_FORMAT},
 * {@link #WEEK_NUMBER_FORMAT}, {@link #POPOVER_FORMAT}, and similar):
 * pass a {@code Map<String, Object>} with FC formatter properties
 * (e.g., {@code Map.of("hour", "numeric", "minute", "2-digit", "meridiem", "short")}).
 *
 * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
 */
public enum Option {

    /**
     * CSS classes for the cell in the header area of time grid views that labels the all-day section.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text}, {@code isNarrow}, {@code view}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ALL_DAY_HEADER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.isNarrow ? 'narrow-all-day' : '';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderClass</a>
     */
    ALL_DAY_HEADER_CLASS("allDayHeaderClass"),


    /**
     * Custom content for the cell in the header area of time grid views that labels the all-day section. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text}, {@code isNarrow}, {@code view}.</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ALL_DAY_HEADER_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return info.isNarrow ? 'All' : info.text;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderContent</a>
     */
    ALL_DAY_HEADER_CONTENT("allDayHeaderContent"),

    /**
     * Called after the cell in the header area of time grid views that labels the all-day section is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text}, {@code isNarrow}, {@code view},
     *                         and {@code el} (the all-day header cell, a {@code <div role="rowheader">}, only in
     *                         allDayHeaderDidMount and allDayHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ALL_DAY_HEADER_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.title = 'All-day entries';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderDidMount</a>
     */
    ALL_DAY_HEADER_DID_MOUNT("allDayHeaderDidMount"),

    /**
     * Called before the cell in the header area of time grid views that labels the all-day section is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text}, {@code isNarrow}, {@code view},
     *                         and {@code el} (the all-day header cell, a {@code <div role="rowheader">}, only in
     *                         allDayHeaderDidMount and allDayHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ALL_DAY_HEADER_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing all-day header');
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderWillUnmount</a>
     */
    ALL_DAY_HEADER_WILL_UNMOUNT("allDayHeaderWillUnmount"),

    /**
     * How the duration of an entry changes when it is dragged between the timed and the all-day section. With
     * {@code true}, the duration stays roughly the same (hourly durations are rounded down to whole days). With
     * {@code false}, the duration is reset to the default all-day or timed entry duration of the target section.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/allDayMaintainDuration">allDayMaintainDuration</a>
     */
    ALL_DAY_MAINTAIN_DURATION,

    /**
     * Show or hide the all-day row at the top of the calendar. When hidden, all-day entries are not displayed
     * in time grid views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/allDaySlot">allDaySlot</a>
     */
    ALL_DAY_SLOT,

    /**
     * Width-to-height ratio of the calendar. The calendar fills the available width, its height follows from
     * this ratio (larger numbers make smaller heights). More precisely, it is the ratio of the calendar's content area.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (e.g., {@code 1.35})</dd>
     *   <dt>Default</dt> <dd>{@code 1.35}. Only used while neither {@link #HEIGHT} nor {@link #CONTENT_HEIGHT} is set,
     *                        and the add-on sets {@link #HEIGHT} to {@code "100%"}.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/aspectRatio">aspectRatio</a>
     */
    ASPECT_RATIO,

    /**
     * Emphasize business hours on the calendar. With {@code true} the default business hours are used
     * (Monday to Friday, 9am to 5pm).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean} | {@link BusinessHours} | {@code BusinessHours[]}</dd>
     *   <dt>Default</dt> <dd>{@code false} (no emphasis)</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.BUSINESS_HOURS, BusinessHours.businessWeek().start(10).end(18));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/businessHours">businessHours</a>
     */
    @JsonConverter(BusinessHoursConverter.class)
    BUSINESS_HOURS,

    /**
     * Accessible hint ({@code aria-label}) for the close button of the entry popover.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code String}</dd>
     *   <dt>Default</dt> <dd>{@code "Close"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">closeHint</a>
     */
    CLOSE_HINT,

    /**
     * Height of the view area of the calendar (without header and footer). If the contents do not fit,
     * scrollbars appear. With {@code "auto"} the view takes its natural height and uses no scrollbars.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} (pixels) | {@code "auto"}</dd>
     *   <dt>Default</dt> <dd>unset. The add-on sets {@link #HEIGHT} to {@code "100%"}, so the view area fills the rest of the calendar.</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/contentHeight">contentHeight</a>
     */
    CONTENT_HEIGHT,

    /**
     * Determines the first visible day of a custom view, e.g. to make it start at the start of the week or month.
     * Ignored if the view's range is defined by {@code visibleRange}.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code String} like {@code "week"} or {@code "month"}</dd>
     *   <dt>Default</dt> <dd>generated from the view's duration</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dateAlignment">dateAlignment</a>
     */
    DATE_ALIGNMENT,

    /**
     * How far the calendar navigates when prev/next is executed (toolbar buttons or the prev/next methods).
     * Unnecessary for standard views. For a custom view with a {@code duration}, that duration is the default.
     * <dl>
     *   <dt>Type</dt> <dd>duration as FullCalendar accepts it: object with keys like {@code days} or {@code months},
     *                     {@code String} like {@code "hh:mm"} | milliseconds ({@code number})</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DATE_INCREMENT, Map.of("days", 7));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dateIncrement">dateIncrement</a>
     */
    DATE_INCREMENT,

    /**
     * CSS classes for day cells in day grid views (including the all-day section of time grid views) and the
     * body of the "+N more" popover.
     * Time grid day columns use {@link #DAY_LANE_CLASS}.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text},
     *                         {@code textParts}, {@code dayNumberText}, {@code weekdayText}, {@code monthText},
     *                         {@code isPast}, {@code isFuture}, {@code isToday}, {@code isOther}, {@code isMajor},
     *                         {@code isNarrow}, {@code inPopover}, {@code hasNavLink}, {@code options} (the calendar
     *                         options) and {@code resource} (only in vertical resource views of the scheduler).</dd>
     *   <dt>Returns</dt>  <dd>the class name {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_CELL_CLASS, JsCallback.of("function(info) { return info.isToday ? 'today-cell' : ''; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellClass</a>
     */
    DAY_CELL_CLASS("dayCellClass"),

    /**
     * Former name of {@link #DAY_CELL_CLASS}: CSS classes for day cells.
     *
     * @deprecated use {@link #DAY_CELL_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     * It no longer applies to time grid day columns, see {@link #DAY_LANE_CLASS}.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_CELL_CLASS_NAMES("dayCellClass"),

    /**
     * Former name of {@link #DAY_CELL_TOP_CONTENT}: custom content for the day number area of day cells.
     *
     * @deprecated use {@link #DAY_CELL_TOP_CONTENT}, which sets the same FullCalendar option.
     * It no longer applies to time grid day columns, which have no content hook in FullCalendar 7.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_CELL_CONTENT("dayCellTopContent"),

    /**
     * Called right after a day cell has been added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text},
     *                         {@code textParts}, {@code dayNumberText}, {@code weekdayText}, {@code monthText},
     *                         {@code isPast}, {@code isFuture}, {@code isToday}, {@code isOther}, {@code isMajor},
     *                         {@code isNarrow}, {@code inPopover}, {@code hasNavLink}, {@code options} (the calendar
     *                         options), {@code resource} (only in vertical resource views of the scheduler) and
     *                         {@code el} (the cell's DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_CELL_DID_MOUNT, JsCallback.of("function(info) { info.el.dataset.day = info.dayNumberText; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellDidMount</a>
     */
    DAY_CELL_DID_MOUNT("dayCellDidMount"),

    /**
     * Custom content for the top area of day cells (where the day number appears). The generated content is
     * inserted inside the inner-most wrapper of that area, it does not replace the cell.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text},
     *                         {@code textParts}, {@code dayNumberText}, {@code weekdayText}, {@code monthText},
     *                         {@code isPast}, {@code isFuture}, {@code isToday}, {@code isOther}, {@code isMajor},
     *                         {@code isNarrow}, {@code inPopover}, {@code hasNavLink}, {@code options} (the calendar
     *                         options) and {@code resource} (only in vertical resource views of the scheduler).</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_CELL_TOP_CONTENT, JsCallback.of("function(info) { return info.dayNumberText + (info.isToday ? ' (today)' : ''); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopContent</a>
     */
    DAY_CELL_TOP_CONTENT("dayCellTopContent"),

    /**
     * Called right before a day cell is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text},
     *                         {@code textParts}, {@code dayNumberText}, {@code weekdayText}, {@code monthText},
     *                         {@code isPast}, {@code isFuture}, {@code isToday}, {@code isOther}, {@code isMajor},
     *                         {@code isNarrow}, {@code inPopover}, {@code hasNavLink}, {@code options} (the calendar
     *                         options), {@code resource} (only in vertical resource views of the scheduler) and
     *                         {@code el} (the cell's DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_CELL_WILL_UNMOUNT, JsCallback.of("function(info) { delete info.el.dataset.day; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellWillUnmount</a>
     */
    DAY_CELL_WILL_UNMOUNT("dayCellWillUnmount"),

    /**
     * Show or hide the day headers (column header cells). Applies to the month, time grid and day grid views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dayHeaders">dayHeaders</a>
     */
    DAY_HEADERS,

    /**
     * CSS classes for day header cells (above the day cells in day grid and time grid views) and the header of
     * the "+N more" popover.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isOther}, {@code hasNavLink},
     *                         {@code inPopover}, {@code view}, {@code level} and {@code resource} (only in vertical
     *                         resource views of the scheduler).</dd>
     *   <dt>Returns</dt>  <dd>the class name {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_HEADER_CLASS, JsCallback.of("function(info) { return info.isToday ? 'today-header' : ''; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderClass</a>
     */
    DAY_HEADER_CLASS("dayHeaderClass"),

    /**
     * Former name of {@link #DAY_HEADER_CLASS}: CSS classes for day header cells.
     *
     * @deprecated use {@link #DAY_HEADER_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_HEADER_CLASS_NAMES("dayHeaderClass"),

    /**
     * Custom content for day header cells. The generated content is inserted inside the inner-most wrapper of
     * the header cell, it does not replace the cell.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isOther}, {@code hasNavLink},
     *                         {@code inPopover}, {@code view}, {@code level} and {@code resource} (only in vertical
     *                         resource views of the scheduler).</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_HEADER_CONTENT, JsCallback.of("function(info) { return info.text.toUpperCase(); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderContent</a>
     */
    DAY_HEADER_CONTENT("dayHeaderContent"),

    /**
     * Called right after a day header cell has been added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isOther}, {@code hasNavLink},
     *                         {@code inPopover}, {@code view}, {@code level}, {@code resource} (only in vertical
     *                         resource views of the scheduler) and {@code el} (the header cell's DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_HEADER_DID_MOUNT, JsCallback.of("function(info) { info.el.title = info.date.toDateString(); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderDidMount</a>
     */
    DAY_HEADER_DID_MOUNT("dayHeaderDidMount"),

    /**
     * Format of the text in the day headers (column headings).
     * <dl>
     *   <dt>Type</dt>    <dd>format object, e.g. a {@code Map<String, Object>} with {@code weekday}, {@code month}, {@code day}
     *                        and {@code omitCommas}</dd>
     *   <dt>Default</dt> <dd>depends on the view: {@code weekday: 'short'} in month view, weekday plus numeric month and day
     *                        in week views, {@code weekday: 'long'} in day views</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_HEADER_FORMAT, Map.of("weekday", "long"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dayHeaderFormat">dayHeaderFormat</a>
     */
    DAY_HEADER_FORMAT,

    /**
     * Called right before a day header cell is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isOther}, {@code hasNavLink},
     *                         {@code inPopover}, {@code view}, {@code level}, {@code resource} (only in vertical
     *                         resource views of the scheduler) and {@code el} (the header cell's DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_HEADER_WILL_UNMOUNT, JsCallback.of("function(info) { info.el.removeAttribute('title'); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderWillUnmount</a>
     */
    DAY_HEADER_WILL_UNMOUNT("dayHeaderWillUnmount"),

    /**
     * CSS classes for day lanes, the columns of time slots of a day in time grid views.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isOther}, {@code isNarrow},
     *                         {@code isMajor}, {@code isStack} (simplified print layout, Firefox only) and
     *                         {@code resource} (only in vertical resource views of the scheduler).</dd>
     *   <dt>Returns</dt>  <dd>the class name {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_LANE_CLASS, JsCallback.of("function(info) { return info.isToday ? 'today-lane' : ''; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneClass</a>
     */
    DAY_LANE_CLASS("dayLaneClass"),

    /**
     * Called right after a day lane (column of time slots in time grid views) has been added to the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isOther}, {@code isNarrow},
     *                         {@code isMajor}, {@code isStack} (simplified print layout, Firefox only),
     *                         {@code resource} (only in vertical resource views of the scheduler) and
     *                         {@code el} (the lane's DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_LANE_DID_MOUNT, JsCallback.of("function(info) { info.el.dataset.date = info.date.toISOString(); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneDidMount</a>
     */
    DAY_LANE_DID_MOUNT("dayLaneDidMount"),

    /**
     * Called right before a day lane (column of time slots in time grid views) is removed from the DOM.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isOther}, {@code isNarrow},
     *                         {@code isMajor}, {@code isStack} (simplified print layout, Firefox only),
     *                         {@code resource} (only in vertical resource views of the scheduler) and
     *                         {@code el} (the lane's DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DAY_LANE_WILL_UNMOUNT, JsCallback.of("function(info) { delete info.el.dataset.date; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneWillUnmount</a>
     */
    DAY_LANE_WILL_UNMOUNT("dayLaneWillUnmount"),

    /**
     * Maximum number of entries in a day cell of day grid views, not counting the "+N more" link. The rest is
     * shown in a popover. For time grid and timeline views use {@link #ENTRY_MAX_STACK}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code false} | {@code integer} | {@code true} (false = no limit, integer = fixed count, true = limit to cell height)</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see FullCalendar#setMaxEntriesPerDay(int)
     * @see FullCalendar#setMaxEntriesPerDayFitToCell()
     * @see FullCalendar#setMaxEntriesPerDayUnlimited()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">dayMaxEvents</a>
     */
    DAY_MAX_ENTRIES("dayMaxEvents"),

    /**
     * Maximum number of stacked entry rows within a day in daygrid views, including the "+N more" link. The remaining
     * entries are shown in a popover. For timegrid and timeline views, FullCalendar uses {@code eventMaxStack}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean} | {@code integer}. {@code true} limits the rows to the height of the day
     *                     cell, an integer to that number of rows, {@code false} shows all entries.</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
     */
    DAY_MAX_ENTRY_ROWS,

    /**
     * Former name of {@link #DAY_MAX_ENTRY_ROWS}: maximum number of stacked entry rows within a day.
     *
     * @deprecated use {@link #DAY_MAX_ENTRY_ROWS}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_MAX_EVENT_ROWS,

    /**
     * Minimum pixel width of each day cell. When the calendar gets too narrow for it, horizontal scrollbars
     * appear. Applies to vertical resource views, day grid views and time grid views. It needs FullCalendar's
     * premium {@code scrollgrid} plugin, which only {@code FullCalendarScheduler} loads.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>unset</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dayMinWidth">dayMinWidth</a>
     */
    DAY_MIN_WIDTH,

    /**
     * Former name of {@link #POPOVER_FORMAT}: date format of the title of the "+N more" popover.
     *
     * @deprecated use {@link #POPOVER_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_POPOVER_FORMAT("popoverFormat"),

    /**
     * Default value of the all-day flag for entries that do not specify one. If not set, FullCalendar guesses the flag from the entry data.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>not set</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/defaultAllDay">defaultAllDay</a>
     */
    DEFAULT_ALL_DAY,

    /**
     * Text direction of the calendar, useful for right-to-left languages such as Arabic and Hebrew.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code "ltr"} (left-to-right) | {@code "rtl"} (right-to-left)</dd>
     *   <dt>Default</dt> <dd>{@code "ltr"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/direction">direction</a>
     */
    DIRECTION,

    /**
     * Whether the end time of an entry is displayed. Entries without an end or with the entry time display turned off never show it.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false} in month and daygrid week views, {@code true} in timegrid views and daygrid day view</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/displayEventEnd">displayEventEnd</a>
     */
    DISPLAY_ENTRY_END,

    /**
     * Show or hide the time text on entries. Applies only to timed entries, all-day entries never show time text.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>not set, computed from the current view: time text is shown in day grid and time grid views,
     *                        in timeline views it depends on the duration</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/displayEventTime">displayEventTime</a>
     */
    DISPLAY_ENTRY_TIME,

    /**
     * Former name of {@link #DISPLAY_ENTRY_END}: whether the end time of an entry is displayed.
     *
     * @deprecated use {@link #DISPLAY_ENTRY_END}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DISPLAY_EVENT_END,

    /**
     * Time an entry takes to move back to its original position after an unsuccessful drag.
     * <dl>
     *   <dt>Type</dt> <dd>{@code integer} (milliseconds)</dd>
     *   <dt>Default</dt> <dd>{@code 500}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dragRevertDuration">dragRevertDuration</a>
     */
    DRAG_REVERT_DURATION,

    /**
     * Automatically scroll the scroll containers while dragging entries or selecting dates, once the mouse
     * gets close to the edge.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dragScroll">dragScroll</a>
     */
    DRAG_SCROLL,

    /**
     * Determines whether external elements or entries from other calendars can be dropped onto the calendar.
     * Has to be enabled to receive drops from external elements.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/droppable">droppable</a>
     */
    DROPPABLE,

    /**
     * Filters which external elements can be dropped onto the calendar (requires {@link #DROPPABLE}).
     * <dl>
     *   <dt>Type</dt>     <dd>CSS selector {@code String} | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>{@code "*"}</dd>
     *   <dt>Callback</dt> <dd>{@code function(draggable)}. {@code draggable} is the dragged item.</dd>
     *   <dt>Returns</dt>  <dd>{@code true} if the item can be dropped onto the calendar</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.DROP_ACCEPT, ".cool-entry");
     * // or by function
     * calendar.setOption(Option.DROP_ACCEPT, JsCallback.of("function(draggable) { return draggable.classList.contains('acceptable'); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
     */
    DROP_ACCEPT,

    /**
     * Master switch for dragging and resizing entries. Enables or disables both at the same time, use
     * {@link #ENTRY_START_EDITABLE} and {@link #ENTRY_DURATION_EDITABLE} for one of them only. Background entries can
     * not be dragged or resized.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}, set by the add-on (FullCalendar's own default is {@code false})</dd>
     * </dl>
     * Individual entries can override via {@link Entry#setEditable(boolean)}.
     *
     * @see <a href="https://fullcalendar.io/docs/editable">editable</a>
     */
    EDITABLE,

    /**
     * Exact programmatic control over where an entry can be dropped. Called after {@link #ENTRY_OVERLAP} and
     * {@link #ENTRY_CONSTRAINT} have allowed the position, for every new potential drop position while the user
     * is dragging. Must return a boolean synchronously.
     * {@code draggedEvent} has {@code getCustomProperty()} available.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(dropInfo, draggedEvent)}. {@code dropInfo} has {@code start},
     *                         {@code end}, {@code startStr}, {@code endStr}, {@code allDay} and {@code resource}
     *                         (only in resource views of the scheduler). {@code draggedEvent} is the dragged entry.</dd>
     *   <dt>Returns</dt>  <dd>{@code true} if the drop is allowed, otherwise {@code false}</dd>
     * </dl>
     * <pre>{@code
     * // entries must not be dropped on a Sunday
     * calendar.setOption(Option.ENTRY_ALLOW, JsCallback.of("function(dropInfo, draggedEvent) { return dropInfo.start.getDay() !== 0; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventAllow">eventAllow</a>
     */
    ENTRY_ALLOW,

    /**
     * CSS classes for entries, set on the outermost element of each entry.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code event}, {@code timeText}, {@code isStart},
     *                         {@code isEnd}, {@code isMirror}, {@code isPast}, {@code isFuture}, {@code isToday},
     *                         {@code color}, {@code contrastColor}, {@code isInteractive}, {@code isNarrow},
     *                         {@code isShort}, {@code level}, {@code timeClass}, {@code titleClass},
     *                         {@code options} (the calendar options) and {@code view}.</dd>
     *   <dt>Returns</dt>  <dd>the class name {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_CLASS, JsCallback.of("function(info) { return info.isPast ? 'past-entry' : ''; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventClass</a>
     */
    ENTRY_CLASS("eventClass"),

    /**
     * Former name of {@link #ENTRY_CLASS}: CSS classes for entries.
     *
     * @deprecated use {@link #ENTRY_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    ENTRY_CLASS_NAMES("eventClass"),

    /**
     * Default color for all foreground entries (not background entries).
     * FullCalendar sets the color on each entry's element as the CSS variable {@code --fc-event-color}. The theme's
     * styles read it, so where the color shows depends on the theme. Own styles can read the variable as well.
     * <dl>
     *   <dt>Type</dt> <dd>CSS color string, e.g. {@code "#378006"}, {@code "rgb(255,0,0)"} or {@code "red"}</dd>
     * </dl>
     * Can be overridden per-entry via {@link Entry#setColor(String)}. Own styles can read the variable, e.g.:
     * <pre>{@code
     * .fc-event.important { border-left: 4px solid var(--fc-event-color); }
     * }</pre>
     * The class {@code fc-event} marks foreground entries, background entries carry {@code fc-bg-event}.
     *
     * @see <a href="https://fullcalendar.io/docs/eventColor">eventColor</a>
     * @see <a href="https://fullcalendar.io/docs/custom-themes">Custom themes: event colors</a>
     */
    ENTRY_COLOR,

    /**
     * Limits entry dragging and resizing to certain windows of time.
     * <dl>
     *   <dt>Type</dt> <dd>group id {@code String} (dragged entries must be fully contained by at least one entry
     *                     of that group) | {@code "businessHours"} | {@link BusinessHours}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_CONSTRAINT, "businessHours");
     * // or a custom time window
     * calendar.setOption(Option.ENTRY_CONSTRAINT, BusinessHours.businessWeek().start(10).end(18));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">eventConstraint</a>
     */
    @JsonConverter(BusinessHoursConverter.class)
    ENTRY_CONSTRAINT,

    /**
     * Custom content for entries. The generated content is inserted inside the inner-most wrapper of the entry.
     * If given as callback, it is called every time the entry data changes.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code event}, {@code timeText}, {@code isStart},
     *                         {@code isEnd}, {@code isMirror}, {@code isPast}, {@code isFuture}, {@code isToday},
     *                         {@code color}, {@code contrastColor}, {@code isInteractive}, {@code isNarrow},
     *                         {@code isShort}, {@code level}, {@code timeClass}, {@code titleClass},
     *                         {@code options} (the calendar options) and {@code view}.</dd>
     *   <dt>Returns</dt>  <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_CONTENT, JsCallback.of("function(info) { return info.timeText + ' ' + info.event.title; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventContent</a>
     */
    ENTRY_CONTENT("eventContent"),

    /**
     * Default contrast color for all entries, used for text and other elements drawn on the entry color.
     * FullCalendar sets it on each entry's element as the CSS variable {@code --fc-event-contrast-color}, which the
     * theme's styles and own styles can read, like {@code --fc-event-color} of {@link #ENTRY_COLOR}.
     * <dl>
     *   <dt>Type</dt> <dd>CSS color string</dd>
     * </dl>
     * Can be overridden per-entry via {@link Entry#setContrastColor(String)}. Own styles can read the variable, e.g.:
     * <pre>{@code
     * .fc-event.important { outline: 2px dashed var(--fc-event-contrast-color); }
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventContrastColor">eventContrastColor</a>
     * @see <a href="https://fullcalendar.io/docs/custom-themes">Custom themes: event colors</a>
     */
    ENTRY_CONTRAST_COLOR,

    /**
     * Transforms the raw data of each received entry into a standard entry object before FullCalendar parses it. Called once per
     * received entry, but not for entries added via the client side API. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(eventData)}. The argument is the received entry data, given directly, there is no {@code info} object.</dd>
     *   <dt>Returns</dt> <dd>a parsable entry (event) object, or {@code false} to discard the entry</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_DATA_TRANSFORM, JsCallback.of("""
     *         function(eventData) {
     *             if (eventData.cancelled) return false;
     *             return { title: eventData.name, start: eventData.begin, end: eventData.finish };
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventDataTransform">eventDataTransform</a>
     */
    ENTRY_DATA_TRANSFORM("eventDataTransform"),

    /**
     * Called right after an entry's DOM element has been added to the DOM. Not called again when the entry data
     * changes.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code event}, {@code timeText}, {@code isStart},
     *                         {@code isEnd}, {@code isMirror}, {@code isPast}, {@code isFuture}, {@code isToday},
     *                         {@code color}, {@code contrastColor}, {@code isInteractive}, {@code isNarrow},
     *                         {@code isShort}, {@code level}, {@code timeClass}, {@code titleClass},
     *                         {@code options} (the calendar options), {@code view} and {@code el} (the entry's
     *                         DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.of("function(info) { info.el.title = info.event.title; }"));
     * }</pre>
     * <p>
     * When using this option, any native event listeners registered via
     * {@link FullCalendar#addEntryNativeEventListener(String, String)} are automatically
     * merged into the callback.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventDidMount</a>
     */
    ENTRY_DID_MOUNT("eventDidMount"),

    /**
     * Default display mode for entries.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code "auto"} | {@code "block"} | {@code "list-item"} | {@code "background"} |
     *                        {@code "inverse-background"} | {@code "none"} | {@link DisplayMode}</dd>
     *   <dt>Default</dt> <dd>{@code "auto"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventDisplay">eventDisplay</a>
     */
    ENTRY_DISPLAY,

    /**
     * Number of pixels the mouse or touch must move before an entry drag starts.
     * <dl>
     *   <dt>Type</dt> <dd>{@code integer}</dd>
     *   <dt>Default</dt> <dd>{@code 5}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventDragMinDistance">eventDragMinDistance</a>
     */
    ENTRY_DRAG_MIN_DISTANCE,

    /**
     * Allow resizing (duration editing) of entries.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true} — effective only when {@link #EDITABLE} is also {@code true}</dd>
     * </dl>
     * Can be overridden per-entry.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDurationEditable">eventDurationEditable</a>
     */
    ENTRY_DURATION_EDITABLE,

    /**
     * Makes all entries focusable / tabbable for keyboard accessibility. By default, only entries with a
     * {@code url} are.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventInteractive">eventInteractive</a>
     */
    ENTRY_INTERACTIVE,

    /**
     * Time the user must hold down on a touch device before an entry becomes draggable. Has no effect on non-touch devices.
     * <dl>
     *   <dt>Type</dt> <dd>{@code integer} (milliseconds)</dd>
     *   <dt>Default</dt> <dd>value of {@link #LONG_PRESS_DELAY} ({@code 1000})</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventLongPressDelay">eventLongPressDelay</a>
     */
    ENTRY_LONG_PRESS_DELAY,

    /**
     * Maximum number of entries that stack next to each other. In time grid view they stack left-to-right,
     * in timeline view top-to-bottom. Hidden entries are represented by a more-link, which by default opens a popover
     * with them. For day grid view use {@link #DAY_MAX_ENTRIES} instead.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} | {@code null} (no maximum, all entries are shown)</dd>
     *   <dt>Default</dt> <dd>{@code null}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventMaxStack">eventMaxStack</a>
     */
    ENTRY_MAX_STACK,

    /**
     * Minimum height in pixels an entry is allowed to have in time grid views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>{@code 15}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventMinHeight">eventMinHeight</a>
     */
    ENTRY_MIN_HEIGHT,

    /**
     * Order of entries within the same day. In most views it is the vertical order, in time grid views the
     * horizontal order. The default puts earlier entries first, then longer ones, then all-day entries, then orders by
     * title. Unless {@link #ENTRY_ORDER_STRICT} is set, FullCalendar may deviate from the order to be more compact.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} (name of an entry property such as {@code "title"}, prefixed with {@code -}
     *                         for descending order, or several names separated by commas) | array of property names and
     *                         functions | {@link JsCallback} with a compare function</dd>
     *   <dt>Default</dt>  <dd>{@code "start,-duration,allDay,title"}</dd>
     *   <dt>Callback</dt> <dd>{@code function(a, b)}. {@code a} and {@code b} are the two entries to compare.</dd>
     *   <dt>Returns</dt>  <dd>{@code -1} or {@code 1}, like the compare function of JavaScript's {@code sort}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_ORDER, "title,-start");
     * calendar.setOption(Option.ENTRY_ORDER, JsCallback.of("function(a, b) { return a.title.localeCompare(b.title); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrder">eventOrder</a>
     */
    ENTRY_ORDER,

    /**
     * Make FullCalendar follow {@link #ENTRY_ORDER} strictly. By default it is not strict, and compactness
     * is prioritized over following the order exactly.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrderStrict">eventOrderStrict</a>
     */
    ENTRY_ORDER_STRICT,

    /**
     * Determines whether entries being dragged or resized may overlap other entries. Background entries are
     * treated like normal entries.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code boolean} ({@code false} prevents any overlap) | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>{@code true}</dd>
     *   <dt>Callback</dt> <dd>{@code function(stillEvent, movingEvent)}. Called for every pair of intersecting
     *                         entries on drag or resize. {@code stillEvent} is the entry underneath,
     *                         {@code movingEvent} the entry being dragged or resized (it still has its original
     *                         start and end).</dd>
     *   <dt>Returns</dt>  <dd>{@code true} if the overlap is allowed, otherwise {@code false}</dd>
     * </dl>
     * <pre>{@code
     * // overlap only if both entries are all-day
     * calendar.setOption(Option.ENTRY_OVERLAP, JsCallback.of("function(stillEvent, movingEvent) { return stillEvent.allDay && movingEvent.allDay; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
     */
    ENTRY_OVERLAP,

    /**
     * Allow the user to resize entries from their start edge.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventResizableFromStart">eventResizableFromStart</a>
     */
    ENTRY_RESIZABLE_FROM_START,

    /**
     * Height threshold in pixels in time grid views, below which an entry gets the "short" style.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>{@code 30}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventShortHeight">eventShortHeight</a>
     */
    ENTRY_SHORT_HEIGHT,

    /**
     * Default name of the query parameter that is sent to each JSON feed entry source and describes the
     * exclusive end of the fetched interval (value is an ISO 8601 date string).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code String}</dd>
     *   <dt>Default</dt> <dd>{@code "end"}</dd>
     * </dl>
     * Per-source override: {@link JsonFeedEventSource#withEndParam(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/endParam">endParam</a>
     */
    ENTRY_SOURCE_END_PARAM("endParam"),

    /**
     * Global Google Calendar API key used by all {@link GoogleCalendarEventSource} instances that do not specify their own key.
     * <dl>
     *   <dt>Type</dt> <dd>{@code String}</dd>
     * </dl>
     * Per-source override: {@link GoogleCalendarEventSource#withApiKey(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/google-calendar">googleCalendarApiKey</a>
     */
    ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY("googleCalendarApiKey"),

    /**
     * Default name of the query parameter that is sent to each JSON feed entry source and describes the start
     * of the fetched interval (value is an ISO 8601 date string).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code String}</dd>
     *   <dt>Default</dt> <dd>{@code "start"}</dd>
     * </dl>
     * Per-source override: {@link JsonFeedEventSource#withStartParam(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/startParam">startParam</a>
     */
    ENTRY_SOURCE_START_PARAM("startParam"),

    /**
     * Called when fetching from an entry source succeeds. It can transform the response, for example to unwrap a wrapper object
     * into the entry array. Accepts a {@link JsCallback}. To transform each single entry, use {@link #ENTRY_DATA_TRANSFORM} instead.
     * <dl>
     *   <dt>Type</dt> <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(rawEvents, response)}. The arguments are given directly, there is no
     *                         {@code info} object. {@code rawEvents} is the raw response content, {@code response} a
     *                         {@code Response} object if the source was a JSON feed.</dd>
     *   <dt>Returns</dt> <dd>a new array of parsable entry (event) objects, which is used instead of the received response</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_SOURCE_SUCCESS, JsCallback.of("""
     *         function(rawEvents, response) {
     *             return rawEvents.eventArray;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventSourceSuccess">eventSourceSuccess</a>
     */
    ENTRY_SOURCE_SUCCESS("eventSourceSuccess"),

    /**
     * Default name of the query parameter that is sent to each JSON feed entry source and describes the time
     * zone of the start and end values as well as the desired time zone of the returned entries
     * (value like {@code "America/Chicago"} or {@code "UTC"}, unspecified for {@code local}).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code String}</dd>
     *   <dt>Default</dt> <dd>{@code "timeZone"}</dd>
     * </dl>
     * Per-source override: {@link JsonFeedEventSource#withTimeZoneParam(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/timeZoneParam">timeZoneParam</a>
     */
    ENTRY_SOURCE_TIME_ZONE_PARAM("timeZoneParam"),

    /**
     * Allow dragging entries to change their start time.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true} — effective only when {@link #EDITABLE} is also {@code true}</dd>
     * </dl>
     * Can be overridden per-entry.
     *
     * @see <a href="https://fullcalendar.io/docs/eventStartEditable">eventStartEditable</a>
     */
    ENTRY_START_EDITABLE,

    /**
     * Former name of {@link #ENTRY_CONTRAST_COLOR}: the default text color for all entries
     * (FullCalendar option {@code eventTextColor} in earlier versions). It now sets {@code eventContrastColor}.
     *
     * @deprecated use {@link #ENTRY_CONTRAST_COLOR}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    ENTRY_TEXT_COLOR("eventContrastColor"),

    /**
     * Format of the time text shown on entries. Shown only for timed entries.
     * <dl>
     *   <dt>Type</dt>    <dd>format object, e.g. a {@code Map<String, Object>} with {@code hour}, {@code minute},
     *                        {@code meridiem}, {@code omitZeroMinute}</dd>
     *   <dt>Default</dt> <dd>depends on the view: like {@code 7:00} in time grid views, like {@code 7p} in day grid views,
     *                        like {@code 7pm} in list views</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_TIME_FORMAT, Map.of("hour", "2-digit", "minute", "2-digit", "hour12", false));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventTimeFormat">eventTimeFormat</a>
     */
    ENTRY_TIME_FORMAT,

    /**
     * Called right before an entry's DOM element is removed from the DOM.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code event}, {@code timeText}, {@code isStart},
     *                         {@code isEnd}, {@code isMirror}, {@code isPast}, {@code isFuture}, {@code isToday},
     *                         {@code color}, {@code contrastColor}, {@code isInteractive}, {@code isNarrow},
     *                         {@code isShort}, {@code level}, {@code timeClass}, {@code titleClass},
     *                         {@code options} (the calendar options), {@code view} and {@code el} (the entry's
     *                         DOM element).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_WILL_UNMOUNT, JsCallback.of("function(info) { console.log('removing ' + info.event.title); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventWillUnmount</a>
     */
    ENTRY_WILL_UNMOUNT("eventWillUnmount"),

    /**
     * Expand the rows of a view to fill its height, if they do not take up the entire height by themselves.
     * Applies to time grid and timeline views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/expandRows">expandRows</a>
     */
    EXPAND_ROWS,

    /**
     * Former name of {@link #ENTRY_SOURCE_END_PARAM}: name of the query parameter that describes the end
     * of the interval fetched by JSON feed entry sources.
     *
     * @deprecated use {@link #ENTRY_SOURCE_END_PARAM}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_END_PARAM("endParam"),

    /**
     * Former name of {@link #ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY}: global Google Calendar API key used by
     * {@link GoogleCalendarEventSource} instances without their own key.
     *
     * @deprecated use {@link #ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY("googleCalendarApiKey"),

    /**
     * Former name of {@link #ENTRY_SOURCE_START_PARAM}: name of the query parameter that describes the start
     * of the interval fetched by JSON feed entry sources.
     *
     * @deprecated use {@link #ENTRY_SOURCE_START_PARAM}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_START_PARAM("startParam"),

    /**
     * Former name of {@link #ENTRY_SOURCE_TIME_ZONE_PARAM}: name of the query parameter that describes the
     * time zone for JSON feed entry sources.
     *
     * @deprecated use {@link #ENTRY_SOURCE_TIME_ZONE_PARAM}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM("timeZoneParam"),

    /**
     * First day of the week (0 = Sunday, 1 = Monday, ..., 6 = Saturday).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} | {@link DayOfWeek}</dd>
     *   <dt>Default</dt> <dd>depends on the {@link #LOCALE}, {@code 0} (Sunday) otherwise</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/firstDay">firstDay</a>
     */
    @JsonConverter(DayOfWeekConverter.class)
    FIRST_DAY,

    /**
     * Number of weeks shown in month view. With {@code true} the calendar is always 6 weeks tall, with
     * {@code false} it has 4, 5 or 6 weeks, depending on the month.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/fixedWeekCount">fixedWeekCount</a>
     */
    FIXED_WEEK_COUNT,


    /**
     * Whether the view's horizontal scrollbar is fixed to the bottom of the viewport while the page is scrolled
     * vertically, if the calendar is in view but the scrollbar is below the fold. Relevant for views with a horizontal
     * scrollbar, e.g. timeline views.
     * With {@code "auto"}, the scrollbar is sticky when the calendar height is {@code auto}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean} | {@code "auto"}</dd>
     *   <dt>Default</dt> <dd>{@code true}, set by the add-on (FullCalendar's own default is {@code "auto"})</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/footerScrollbarSticky">footerScrollbarSticky</a>
     */
    FOOTER_SCROLLBAR_STICKY,

    /**
     * Footer toolbar: the buttons and title shown at the bottom of the calendar. Values are strings of comma or space separated items,
     * comma separated items are shown adjacent, space separated items with a small gap. Items are {@code title},
     * {@code prev}, {@code next}, {@code prevYear}, {@code nextYear}, {@code today} or a view name like
     * {@code dayGridMonth}.
     * <dl>
     *   <dt>Type</dt>    <dd>object with {@code left}, {@code center}, and {@code right} properties (FullCalendar also
     *                        accepts {@code start} and {@code end}) | {@link Footer} |
     *                        {@code Map<String, String>}</dd>
     *   <dt>Default</dt> <dd>{@code false} (no toolbar)</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.FOOTER_TOOLBAR, Map.of("left", "prev,next today", "center", "title", "right", "dayGridMonth,timeGridWeek"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/footerToolbar">footerToolbar</a>
     */
    @JsonConverter(ToolbarConverter.class)
    FOOTER_TOOLBAR,

    /**
     * Whether an end is assigned to entries that have none. The end is calculated from the default timed or all-day entry duration.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/forceEventDuration">forceEventDuration</a>
     */
    FORCE_ENTRY_DURATION,

    /**
     * Former name of {@link #FORCE_ENTRY_DURATION}: whether an end is assigned to entries that have none.
     *
     * @deprecated use {@link #FORCE_ENTRY_DURATION}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    FORCE_EVENT_DURATION,

    /**
     * Header toolbar: the buttons and title shown at the top of the calendar. Values are strings of comma or space separated items,
     * comma separated items are shown adjacent, space separated items with a small gap. Items are {@code title},
     * {@code prev}, {@code next}, {@code prevYear}, {@code nextYear}, {@code today} or a view name like
     * {@code dayGridMonth}.
     * <dl>
     *   <dt>Type</dt>    <dd>object with {@code left}, {@code center}, and {@code right} properties (FullCalendar also
     *                        accepts {@code start} and {@code end}) | {@link Header} |
     *                        {@code Map<String, String>}</dd>
     *   <dt>Default</dt> <dd>{@code false} (no toolbar)</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.HEADER_TOOLBAR, Map.of("left", "prev,next today", "center", "title", "right", "dayGridMonth,timeGridWeek"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    @JsonConverter(ToolbarConverter.class)
    HEADER_TOOLBAR,

    /**
     * Height of the entire calendar, including header and footer. If the contents do not fit, scrollbars appear.
     * With {@code "auto"} the view takes its natural height and uses no scrollbars, with {@code "100%"} the calendar matches
     * the height of its parent element.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} (pixels) | {@code "auto"} | any CSS value like {@code "100%"}</dd>
     *   <dt>Default</dt> <dd>{@code "100%"}, set by the add-on's constructor ({@code setHeightFull()}). Without a
     *                        height, FullCalendar calculates it from {@link #ASPECT_RATIO}.</dd>
     * </dl>
     *
     * @see FullCalendar#setHeight(String)
     * @see <a href="https://fullcalendar.io/docs/height">height</a>
     */
    HEIGHT,

    /**
     * Days of the week to exclude from the display (0 = Sunday, 6 = Saturday).
     * <dl>
     *   <dt>Type</dt>    <dd>array of {@code integer} | {@code DayOfWeek[]} | {@code Collection<DayOfWeek>}</dd>
     *   <dt>Default</dt> <dd>empty (no days hidden, unless weekends are turned off)</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.HIDDEN_DAYS, new DayOfWeek[] {DayOfWeek.TUESDAY, DayOfWeek.THURSDAY});
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/hiddenDays">hiddenDays</a>
     */
    @JsonConverter(DayOfWeekArrayConverter.class)
    HIDDEN_DAYS,

    /**
     * The date the calendar shows on first attach. Pass a {@link LocalDate}.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code LocalDate}</dd>
     *   <dt>Default</dt> <dd>the current date</dd>
     * </dl>
     *
     * @see FullCalendar#gotoDate(LocalDate)
     * @see <a href="https://fullcalendar.io/docs/initialDate">initialDate</a>
     */
    @JsonConverter(LocalDateConverter.class)
    INITIAL_DATE,

    /**
     * The view the calendar renders on first attach. Pass a {@link CalendarView}'s
     * {@link CalendarView#getClientSideValue() client-side value} (for example
     * {@code CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue()}).
     * <p>
     * Set this before attach to skip the {@code changeView()}-after-attach workaround
     * that was necessary in earlier FC versions.
     * <dl>
     *   <dt>Type</dt>    <dd>String (FC view key, e.g. {@code "timeGridWeek"})</dd>
     *   <dt>Default</dt> <dd>{@code "dayGridMonth"}</dd>
     * </dl>
     *
     * @see FullCalendar#changeView(CalendarView)
     * @see <a href="https://fullcalendar.io/docs/initialView">initialView</a>
     * @since 7.2.0
     */
    INITIAL_VIEW("initialView"),

    /**
     * CSS classes for the week number shown inline in day grid cells.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.INLINE_WEEK_NUMBER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.num % 2 == 0 ? 'even-week' : 'odd-week';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberClass</a>
     */
    INLINE_WEEK_NUMBER_CLASS("inlineWeekNumberClass"),

    /**
     * Custom content for the week number shown inline in day grid cells. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}.</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.INLINE_WEEK_NUMBER_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return 'W' + info.num;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberContent</a>
     */
    INLINE_WEEK_NUMBER_CONTENT("inlineWeekNumberContent"),

    /**
     * Called after the week number shown inline in day grid cells is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}, and {@code el} (the element, only in
     *                         inlineWeekNumberDidMount and inlineWeekNumberWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.INLINE_WEEK_NUMBER_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.title = 'Week ' + info.num;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberDidMount</a>
     */
    INLINE_WEEK_NUMBER_DID_MOUNT("inlineWeekNumberDidMount"),

    /**
     * Called before the week number shown inline in day grid cells is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}, and {@code el} (the element, only in
     *                         inlineWeekNumberDidMount and inlineWeekNumberWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.INLINE_WEEK_NUMBER_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing week number', info.num);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberWillUnmount</a>
     */
    INLINE_WEEK_NUMBER_WILL_UNMOUNT("inlineWeekNumberWillUnmount"),


    /**
     * When entries are fetched. With {@code true}, the calendar fetches only when it needs to and reuses entries it
     * already has, e.g. when the user switches from month to week view within the same month. With {@code false}, it
     * fetches on every view switch and every date change.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/lazyFetching">lazyFetching</a>
     */
    LAZY_FETCHING,

    /**
     * Format of the text on the right side of the day headings in list view.
     * <dl>
     *   <dt>Type</dt> <dd>format object, e.g. a {@code Map<String, Object>} | {@code false} to hide the text</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.LIST_DAY_ALT_FORMAT, Map.of("year", "numeric", "month", "short", "day", "numeric"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/listDayAltFormat">listDayAltFormat</a>
     */
    LIST_DAY_ALT_FORMAT,

    /**
     * Format of the text on the left side of the day headings in list view.
     * <dl>
     *   <dt>Type</dt> <dd>format object, e.g. a {@code Map<String, Object>} with {@code year}, {@code month}, {@code day},
     *                     {@code weekday} | {@code false} to hide the text</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.LIST_DAY_FORMAT, Map.of("weekday", "long", "month", "long", "day", "numeric"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/listDayFormat">listDayFormat</a>
     */
    LIST_DAY_FORMAT,

    /**
     * CSS classes for list view day headings (the row that shows the date above a group of entries).
     * <dl>
     *   <dt>Type</dt>     <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code isPast},
     *                         {@code isFuture} and {@code isToday}.</dd>
     *   <dt>Returns</dt>  <dd>the class name {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.LIST_DAY_HEADER_CLASS, JsCallback.of("function(info) { return info.isToday ? 'today-heading' : ''; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderClass</a>
     */
    LIST_DAY_HEADER_CLASS("listDayHeaderClass"),

    /**
     * Custom content for the texts of a list view day heading. Called once per text, {@code level} 0
     * for {@link #LIST_DAY_FORMAT} and 1 for {@link #LIST_DAY_ALT_FORMAT}. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code isPast}, {@code isFuture},
     *                         {@code isToday}, {@code text}, {@code textParts}, {@code dayNumberText},
     *                         {@code weekdayText}, {@code hasNavLink}, {@code level}. {@code text} is the formatted
     *                         date text, {@code level} is 0 for the primary format and 1 for the alternative
     *                         format.</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.LIST_DAY_HEADER_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return info.level == 0 ? { html: '<b>' + info.text + '</b>' } : info.text;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderContent</a>
     */
    LIST_DAY_HEADER_CONTENT("listDayHeaderContent"),

    /**
     * Called after a list view day heading is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code isPast}, {@code isFuture},
     *                         {@code isToday}, and {@code el} (the element, only in listDayHeaderDidMount and
     *                         listDayHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.LIST_DAY_HEADER_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             if (info.isToday) info.el.classList.add('today-heading');
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderDidMount</a>
     */
    LIST_DAY_HEADER_DID_MOUNT("listDayHeaderDidMount"),

    /**
     * Called before a list view day heading is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code isPast}, {@code isFuture},
     *                         {@code isToday}, and {@code el} (the element, only in listDayHeaderDidMount and
     *                         listDayHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.LIST_DAY_HEADER_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing heading', info.date);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderWillUnmount</a>
     */
    LIST_DAY_HEADER_WILL_UNMOUNT("listDayHeaderWillUnmount"),


    /**
     * Former name of {@link #LIST_DAY_ALT_FORMAT}: the format of the text on the right side of the day
     * headings in list view (FullCalendar option {@code listDaySideFormat} in earlier versions).
     *
     * @deprecated use {@link #LIST_DAY_ALT_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    LIST_DAY_SIDE_FORMAT("listDayAltFormat"),

    /**
     * Called when fetching entries starts or stops. Accepts a {@link JsCallback}. Often used to show or hide a loading indicator.
     * With the scheduler, it is also called when resources are fetched.
     * <dl>
     *   <dt>Type</dt> <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(isLoading)}. The argument is given directly, there is no {@code info}
     *                         object. {@code isLoading} is {@code true} when fetching begins and {@code false} when it
     *                         is done.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.LOADING, JsCallback.of("""
     *         function(isLoading) {
     *             document.body.classList.toggle('loading', isLoading);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/loading">loading</a>
     */
    LOADING("loading"),

    /**
     * Locale/language code for displaying calendar text.
     * <dl>
     *   <dt>Type</dt>    <dd>language code {@code string} (e.g., {@code "en"}, {@code "de"}, {@code "fr"}) | {@link Locale}</dd>
     *   <dt>Default</dt> <dd>{@link CalendarLocale#getDefaultLocale()}, set by the constructor</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">locale</a>
     */
    @JsonConverter(LocaleConverter.class)
    LOCALE,

    /**
     * Time a touch user must hold down before an entry can be dragged or a date can be selected. Has no effect on
     * non-touch devices. See {@link #ENTRY_LONG_PRESS_DELAY} for entry dragging only.
     * <dl>
     *   <dt>Type</dt> <dd>{@code integer} (milliseconds)</dd>
     *   <dt>Default</dt> <dd>{@code 1000}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/longPressDelay">longPressDelay</a>
     */
    LONG_PRESS_DELAY,

    /**
     * Former name of {@link #DAY_MAX_ENTRIES}: the maximum number of entries in a day cell before a
     * "+N more" link is shown.
     *
     * @deprecated use {@link #DAY_MAX_ENTRIES}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MAX_ENTRIES_PER_DAY("dayMaxEvents"),


    /**
     * Format of the first cell of each month, when a day grid view spans several months.
     * <dl>
     *   <dt>Type</dt>    <dd>format object, e.g. a {@code Map<String, Object>} with {@code month}, {@code day}</dd>
     *   <dt>Default</dt> <dd>{@code month: 'long', day: 'numeric'} (like "January 1")</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.MONTH_START_FORMAT, Map.of("month", "short", "day", "numeric"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/monthStartFormat">monthStartFormat</a>
     */
    MONTH_START_FORMAT,

    /**
     * CSS classes for the "+N more" link.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code numericText},
     *                         {@code longText}, {@code isNarrow}. {@code num} is the number of hidden entries,
     *                         {@code text} the localized default text, {@code numericText} its numeric part (for
     *                         example {@code "+5"}) and {@code longText} the full text (for example
     *                         {@code "+5 events"}).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.MORE_LINK_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.num > 5 ? 'busy-day' : '';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkClass</a>
     */
    MORE_LINK_CLASS("moreLinkClass"),

    /**
     * Former name of {@link #MORE_LINK_CLASS}: CSS classes for the "+N more" link.
     * @deprecated use {@link #MORE_LINK_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MORE_LINK_CLASS_NAMES("moreLinkClass"),

    /**
     * Determines what happens when the user clicks a "+N more" link (created by the max entries options).
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} ({@code "popover"} | {@code "week"} | {@code "day"} | view name) | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>{@code "popover"}</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code allSegs} (all entry
     *                         segments of the day), {@code hiddenSegs} (segments not displayed before) and
     *                         {@code jsEvent} (the native click event).</dd>
     *   <dt>Returns</dt>  <dd>optionally a string like {@code "day"}, which is processed as the new value of the option</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.MORE_LINK_CLICK, JsCallback.of("function(info) { return 'day'; }"));
     * }</pre>
     *
     * @see FullCalendar#setMoreLinkClickAction(MoreLinkClickAction)
     * @see <a href="https://fullcalendar.io/docs/moreLinkClick">moreLinkClick</a>
     */
    MORE_LINK_CLICK,

    /**
     * Custom content for the "+N more" link. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code numericText},
     *                         {@code longText}, {@code isNarrow}. {@code num} is the number of hidden entries,
     *                         {@code text} the localized default text, {@code numericText} its numeric part (for
     *                         example {@code "+5"}) and {@code longText} the full text (for example
     *                         {@code "+5 events"}).</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.MORE_LINK_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return info.isNarrow ? info.numericText : info.longText;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkContent</a>
     */
    MORE_LINK_CONTENT("moreLinkContent"),

    /**
     * Called right after a "+N more" link is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code numericText},
     *                         {@code longText}, {@code isNarrow}, and {@code el} (the element, only in moreLinkDidMount
     *                         and moreLinkWillUnmount). {@code num} is the number of hidden entries, {@code text} the
     *                         localized default text, {@code numericText} its numeric part (for example {@code "+5"})
     *                         and {@code longText} the full text (for example {@code "+5 events"}).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.MORE_LINK_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.title = info.longText;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkDidMount</a>
     */
    MORE_LINK_DID_MOUNT("moreLinkDidMount"),

    /**
     * Accessible hint ({@code aria-label}) for the "+N more" link. Describes what happens after the link
     * opens the entry popover.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} (use {@code $0} as placeholder for the number of hidden entries, e.g.
     *                         {@code "Click to see $0 more entries"}) | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>locale-dependent</dd>
     *   <dt>Callback</dt> <dd>{@code function(count)}. {@code count} is the number of hidden entries.</dd>
     *   <dt>Returns</dt>  <dd>the hint {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.MORE_LINK_HINT, "Click to see $0 more entries");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">moreLinkHint</a>
     */
    MORE_LINK_HINT,

    /**
     * Called right before a "+N more" link is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code numericText},
     *                         {@code longText}, {@code isNarrow}, and {@code el} (the element, only in moreLinkDidMount
     *                         and moreLinkWillUnmount). {@code num} is the number of hidden entries, {@code text} the
     *                         localized default text, {@code numericText} its numeric part (for example {@code "+5"})
     *                         and {@code longText} the full text (for example {@code "+5 events"}).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.MORE_LINK_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing more link', info.num);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkWillUnmount</a>
     */
    MORE_LINK_WILL_UNMOUNT("moreLinkWillUnmount"),


    /**
     * Maximum number of month columns the multi-month grid tries to render. Fewer columns are shown if each
     * month would become smaller than {@link #SINGLE_MONTH_MIN_WIDTH}. Use {@code 1} for a single column.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer}</dd>
     *   <dt>Default</dt> <dd>{@code 3}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/multiMonthMaxColumns">multiMonthMaxColumns</a>
     */
    MULTI_MONTH_MAX_COLUMNS,


    /**
     * Former name of {@link #SINGLE_MONTH_MIN_WIDTH}: the minimum pixel width of each month in the
     * multi-month view before months wrap to the next row.
     *
     * @deprecated use {@link #SINGLE_MONTH_MIN_WIDTH}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MULTI_MONTH_MIN_WIDTH("singleMonthMinWidth"),

    /**
     * Former name of {@link #SINGLE_MONTH_TITLE_FORMAT}: the format of each month's title in the
     * multi-month view.
     *
     * @deprecated use {@link #SINGLE_MONTH_TITLE_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MULTI_MONTH_TITLE_FORMAT("singleMonthTitleFormat"),

    /**
     * Former name of {@link #VIEW_HINT}: accessible label for the view-switcher buttons in the native FC toolbar.
     *
     * @deprecated use {@link #VIEW_HINT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    NATIVE_TOOLBAR_VIEW_HINT("viewHint"),

    /**
     * Make day headings and week numbers clickable. A click opens a view for that day or week. The target
     * views are derived from the views in the header toolbar, or set explicitly by {@link Option#NAV_LINK_DAY_CLICK}
     * and {@link Option#NAV_LINK_WEEK_CLICK}.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/navLinks">navLinks</a>
     */
    NAV_LINKS,

    /**
     * Determines what happens upon a click on a day heading nav link (requires {@link #NAV_LINKS}). By default, the
     * user is taken to the first day view in the header toolbar. A view name {@code String} navigates to that view
     * instead.
     * A custom function replaces the default, the user is not navigated automatically then. The {@code dateClick} handler is not fired for such a click.
     * <dl>
     *   <dt>Type</dt> <dd>view name {@code String} | {@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(date, jsEvent)}. The arguments are given directly, there is no
     *                         {@code info} object. {@code date} is a Date object, {@code jsEvent} the browser's click
     *                         event.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NAV_LINK_DAY_CLICK, "timeGridDay");
     * calendar.setOption(Option.NAV_LINK_DAY_CLICK, JsCallback.of("function(date, jsEvent) { console.log('day', date.toISOString()); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkDayClick">navLinkDayClick</a>
     */
    NAV_LINK_DAY_CLICK("navLinkDayClick"),

    /**
     * Accessible hint ({@code aria-label}) for clickable day/week numbers (nav links). Describes what happens
     * after the nav link is clicked.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} (use {@code $0} as placeholder for the localized date text, e.g.
     *                         {@code "Go to $0"} results in "Go to January 1, 2022") | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>locale-dependent</dd>
     *   <dt>Callback</dt> <dd>{@code function(dateText, date)}. {@code dateText} is the localized date text,
     *                         {@code date} the JavaScript {@code Date} of the link.</dd>
     *   <dt>Returns</dt>  <dd>the hint {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NAV_LINK_HINT, "Go to $0");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">navLinkHint</a>
     */
    NAV_LINK_HINT,

    /**
     * Determines what happens upon a click on a week number nav link (requires {@link #NAV_LINKS}). By default, the
     * user is taken to the first week view in the header toolbar. A view name {@code String} navigates to that view
     * instead.
     * A custom function replaces the default, the user is not navigated automatically then.
     * <dl>
     *   <dt>Type</dt> <dd>view name {@code String} | {@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(weekStart, jsEvent)}. The arguments are given directly, there is no
     *                         {@code info} object. {@code weekStart} is a Date object, {@code jsEvent} the browser's
     *                         click event.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NAV_LINK_WEEK_CLICK, "timeGridWeek");
     * calendar.setOption(Option.NAV_LINK_WEEK_CLICK, JsCallback.of("function(weekStart, jsEvent) { console.log('week', weekStart.toISOString()); }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkWeekClick">navLinkWeekClick</a>
     */
    NAV_LINK_WEEK_CLICK("navLinkWeekClick"),

    /**
     * How far a timed entry must run into the next day before it is rendered on that day. Affects only
     * timed entries shown on whole days: in day grid, multi-month and list views, and in timeline views with day-sized
     * slots. With a threshold of 9am, an entry from 8pm to 2am appears on one day only.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "00:00:00"}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NEXT_DAY_THRESHOLD, Duration.ofHours(9));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/nextDayThreshold">nextDayThreshold</a>
     */
    @JsonConverter(DurationConverter.class)
    NEXT_DAY_THRESHOLD,

    /**
     * Accessible label ({@code aria-label}) of the "next" button in the native FC toolbar.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>locale-dependent</dd>
     *   <dt>Callback</dt> <dd>{@code function(unitText, unitId)}. {@code unitText} is the localized unit text,
     *                         {@code unitId} a canonical string like {@code 'day'}, {@code 'week'} or {@code 'month'}.</dd>
     *   <dt>Returns</dt>  <dd>the label {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NEXT_HINT, "Next $0");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">nextHint</a>
     */
    NEXT_HINT,

    /**
     * Show a marker for the current time. It repositions itself while the calendar is shown. Applies to time
     * grid and timeline views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/nowIndicator">nowIndicator</a>
     */
    NOW_INDICATOR,

    /**
     * CSS classes for the arrow of the now indicator in the time axis (time grid and timeline views).
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_HEADER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.date.getHours() < 12 ? 'now-am' : 'now-pm';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderClass</a>
     */
    NOW_INDICATOR_HEADER_CLASS("nowIndicatorHeaderClass"),

    /**
     * Custom content for the arrow of the now indicator in the time axis (time grid and timeline views). Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}.</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_HEADER_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return { html: '<span>now</span>' };
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderContent</a>
     */
    NOW_INDICATOR_HEADER_CONTENT("nowIndicatorHeaderContent"),


    /**
     * Called after the now indicator arrow is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, and {@code el} (the element, only
     *                         in nowIndicatorHeaderDidMount and nowIndicatorHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_HEADER_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.classList.add('pulse');
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderDidMount</a>
     */
    NOW_INDICATOR_HEADER_DID_MOUNT("nowIndicatorHeaderDidMount"),

    /**
     * Called before the now indicator arrow is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, and {@code el} (the element, only
     *                         in nowIndicatorHeaderDidMount and nowIndicatorHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_HEADER_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing now indicator', info.date);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderWillUnmount</a>
     */
    NOW_INDICATOR_HEADER_WILL_UNMOUNT("nowIndicatorHeaderWillUnmount"),

    /**
     * CSS classes for the line of the now indicator over the day columns (time grid and timeline views).
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_LINE_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.date.getHours() < 12 ? 'now-am' : 'now-pm';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineClass</a>
     */
    NOW_INDICATOR_LINE_CLASS("nowIndicatorLineClass"),

    /**
     * Custom content for the line of the now indicator over the day columns (time grid and timeline views). Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}.</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_LINE_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return info.date.toLocaleTimeString();
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineContent</a>
     */
    NOW_INDICATOR_LINE_CONTENT("nowIndicatorLineContent"),

    /**
     * Called after the now indicator line is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, and {@code el} (the element, only
     *                         in nowIndicatorLineDidMount and nowIndicatorLineWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_LINE_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.classList.add('pulse');
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineDidMount</a>
     */
    NOW_INDICATOR_LINE_DID_MOUNT("nowIndicatorLineDidMount"),

    /**
     * Called before the now indicator line is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, and {@code el} (the element, only
     *                         in nowIndicatorLineDidMount and nowIndicatorLineWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NOW_INDICATOR_LINE_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing now indicator', info.date);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineWillUnmount</a>
     */
    NOW_INDICATOR_LINE_WILL_UNMOUNT("nowIndicatorLineWillUnmount"),

    /**
     * Whether the now indicator aligns with the start of its slot. Relevant for timeline views.
     * With {@code "auto"}, it snaps when the slot unit is a year, month, week, day or hour. For smaller units, its
     * position reflects the exact current time. With {@code false}, its position always reflects the exact current
     * time.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean} | {@code "auto"}</dd>
     *   <dt>Default</dt> <dd>{@code "auto"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorSnap">nowIndicatorSnap</a>
     */
    NOW_INDICATOR_SNAP,

    /**
     * CSS classes for the "No events to display" message of list views.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text} (the message text, from the
     *                         {@code noEventsText} option), {@code view} (the current view object).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NO_ENTRIES_CLASS, JsCallback.of("""
     *         function(info) {
     *             return 'empty-' + info.view.type;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsClass</a>
     */
    NO_ENTRIES_CLASS("noEventsClass"),

    /**
     * Former name of {@link #NO_ENTRIES_CLASS}: CSS classes for the "No events to display" message of list views.
     * @deprecated use {@link #NO_ENTRIES_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    NO_ENTRIES_CLASS_NAMES("noEventsClass"),

    /**
     * Custom content for the "No events to display" message of list views. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text} (the message text, from the
     *                         {@code noEventsText} option), {@code view} (the current view object).</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NO_ENTRIES_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return { html: '<i>' + info.text + '</i>' };
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsContent</a>
     */
    NO_ENTRIES_CONTENT("noEventsContent"),

    /**
     * Called after the "No events to display" message of list views is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text} (the message text, from the
     *                         {@code noEventsText} option), {@code view} (the current view object), and {@code el} (the
     *                         element, only in noEventsDidMount and noEventsWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NO_ENTRIES_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.setAttribute('role', 'status');
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsDidMount</a>
     */
    NO_ENTRIES_DID_MOUNT("noEventsDidMount"),

    /**
     * Called before the "No events to display" message of list views is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code text} (the message text, from the
     *                         {@code noEventsText} option), {@code view} (the current view object), and {@code el} (the
     *                         element, only in noEventsDidMount and noEventsWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.NO_ENTRIES_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing no entries message');
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsWillUnmount</a>
     */
    NO_ENTRIES_WILL_UNMOUNT("noEventsWillUnmount"),

    /**
     * Date format of the title of the "+N more" popover.
     * <dl>
     *   <dt>Type</dt> <dd>format object, e.g. a {@code Map} with {@code month}, {@code day}, {@code year}, and other properties</dd>
     *   <dt>Default</dt> <dd>{@code {month: 'long', day: 'numeric', year: 'numeric'}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.POPOVER_FORMAT, Map.of("month", "short", "day", "numeric"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/popoverFormat">popoverFormat</a>
     */
    POPOVER_FORMAT,

    /**
     * Accessible label ({@code aria-label}) of the "prev" button in the native FC toolbar.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>locale-dependent</dd>
     *   <dt>Callback</dt> <dd>{@code function(unitText, unitId)}. {@code unitText} is the localized unit text,
     *                         {@code unitId} a canonical string like {@code 'day'}, {@code 'week'} or {@code 'month'}.</dd>
     *   <dt>Returns</dt>  <dd>the label {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.PREV_HINT, "Previous $0");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">prevHint</a>
     */
    PREV_HINT,

    /**
     * When entries of multiple asynchronous entry sources are rendered. With {@code true}, each source is rendered as
     * soon as it is received (more renders). With {@code false}, rendering waits until all sources are received (fewer
     * renders).
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/progressiveEventRendering">progressiveEventRendering</a>
     */
    PROGRESSIVE_ENTRY_RENDERING,

    /**
     * Former name of {@link #PROGRESSIVE_ENTRY_RENDERING}: when entries of multiple asynchronous entry sources are rendered.
     *
     * @deprecated use {@link #PROGRESSIVE_ENTRY_RENDERING}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    PROGRESSIVE_EVENT_RENDERING,

    /**
     * Delay (in milliseconds) the calendar waits before rerendering after any operation that might cause a
     * rerender (changing the view, adding an entry, ...). The calendar then rerenders what it needs all at once,
     * which can reduce the number of rerenders.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code int} | {@code null} (turned off)</dd>
     *   <dt>Default</dt> <dd>{@code null}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/rerenderDelay">rerenderDelay</a>
     */
    RERENDER_DELAY,

    /**
     * Initial vertical scroll position of timegrid views, as time of day. The user can still scroll back to earlier
     * times; use {@link #SLOT_MIN_TIME} to prevent that.
     * <dl>
     *   <dt>Type</dt> <dd>{@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "06:00:00"}</dd>
     * </dl>
     * The position is applied again whenever the date range changes, unless {@link #SCROLL_TIME_RESET} is {@code false}.
     *
     * @see <a href="https://fullcalendar.io/docs/scrollTime">scrollTime</a>
     */
    @JsonConverter(DurationConverter.class)
    SCROLL_TIME,

    /**
     * Whether the view scrolls to {@link #SCROLL_TIME} every time the date range changes, via the API or by the user navigating.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/scrollTimeReset">scrollTimeReset</a>
     */
    SCROLL_TIME_RESET,

    /**
     * Allow users to select time ranges by clicking and dragging.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     * Fires {@code TimeslotsSelectedEvent} server-side.
     *
     * @see <a href="https://fullcalendar.io/docs/selectable">selectable</a>
     */
    SELECTABLE,

    /**
     * Exact programmatic control over where the user can select. Called for every new potential selection while
     * the user is dragging and must return a boolean synchronously.
     * <dl>
     *   <dt>Type</dt>     <dd>{@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(selectInfo)}. {@code selectInfo} has {@code start}, {@code end}
     *                         (exclusive), {@code startStr}, {@code endStr}, {@code allDay} and {@code resource}
     *                         (only in resource views of the scheduler).</dd>
     *   <dt>Returns</dt>  <dd>{@code true} if the selection is allowed, otherwise {@code false}</dd>
     * </dl>
     * <pre>{@code
     * // no selections starting on a Sunday
     * calendar.setOption(Option.SELECT_ALLOW, JsCallback.of("function(selectInfo) { return selectInfo.start.getDay() !== 0; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/selectAllow">selectAllow</a>
     */
    SELECT_ALLOW,

    /**
     * Limits where the user can make selections to certain windows of time. Accepts the same values as
     * {@code eventConstraint} does for entries. Only applies when {@link #SELECTABLE} is {@code true}.
     * <dl>
     *   <dt>Type</dt> <dd>group id {@code String} | {@code "businessHours"} | object like {@link BusinessHours}</dd>
     * </dl>
     * To pass a {@link BusinessHours} object, serialize it to JSON via
     * {@code setOption(SELECT_CONSTRAINT, businessHours.toJson())}.
     * <pre>{@code
     * calendar.setOption(Option.SELECT_CONSTRAINT, "businessHours");
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/selectConstraint">selectConstraint</a>
     */
    SELECT_CONSTRAINT,

    /**
     * Long press delay (in milliseconds) on touch devices before a date becomes selectable. Only applies on
     * touch devices.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} (milliseconds)</dd>
     *   <dt>Default</dt> <dd>value of {@link #LONG_PRESS_DELAY} ({@code 1000})</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectLongPressDelay">selectLongPressDelay</a>
     */
    SELECT_LONG_PRESS_DELAY,

    /**
     * Minimum distance in pixels the mouse must travel after a mouse down before a selection starts. {@code 0} puts no
     * restriction on the distance. A non-zero value helps to tell a selection from a date click. Applies to mouse
     * interaction only.
     * <dl>
     *   <dt>Type</dt> <dd>{@code integer}</dd>
     *   <dt>Default</dt> <dd>{@code 0}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectMinDistance">selectMinDistance</a>
     */
    SELECT_MIN_DISTANCE,

    /**
     * Whether a placeholder entry is drawn while the user drags a selection, instead of the standard highlighting of each cell. Applies to timegrid views only.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectMirror">selectMirror</a>
     */
    SELECT_MIRROR,

    /**
     * Whether the user may select periods of time that are occupied by entries. With a function, it is called once for
     * every entry the selection intersects. Only applies when {@link #SELECTABLE} is {@code true}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean} | {@link JsCallback} returning a boolean</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     *   <dt>Callback</dt> <dd>{@code function(event)}. The intersected entry (FullCalendar event object) is passed
     *                         directly as the argument, not wrapped in an info object.</dd>
     *   <dt>Returns</dt> <dd>{@code true} to allow the selection, {@code false} to prevent it</dd>
     * </dl>
     * <pre>{@code
     * // allow selections over background entries only
     * calendar.setOption(Option.SELECT_OVERLAP,
     *         JsCallback.of("function(event) { return event.display === 'background'; }"));
     * }</pre>
     *
     * An all-day entry of a day counts as intersecting in timegrid views, although it does not visually overlap the time slots.
     *
     * @see <a href="https://fullcalendar.io/docs/selectOverlap">selectOverlap</a>
     */
    SELECT_OVERLAP,

    /**
     * Whether dates of the previous or next month are rendered in month view. Disabled days do not render entries.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/showNonCurrentDates">showNonCurrentDates</a>
     */
    SHOW_NON_CURRENT_DATES,

    /**
     * Minimum pixel width of each mini-month in the multi-month grid, padding included. If the available
     * width would make a month smaller, the months wrap to the next row.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>{@code 350}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/singleMonthMinWidth">singleMonthMinWidth</a>
     */
    SINGLE_MONTH_MIN_WIDTH,

    /**
     * Format of the text above each month in multi-month views.
     * <dl>
     *   <dt>Type</dt>    <dd>format object, e.g. a {@code Map<String, Object>} with {@code month}, {@code year}</dd>
     *   <dt>Default</dt> <dd>{@code month: 'long'} if the calendar spans a single year, {@code month: 'long', year: 'numeric'}
     *                        if it spans several years</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SINGLE_MONTH_TITLE_FORMAT, Map.of("month", "short", "year", "numeric"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/singleMonthTitleFormat">singleMonthTitleFormat</a>
     */
    SINGLE_MONTH_TITLE_FORMAT,

    /**
     * Duration of each time slot in timegrid and timeline views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "00:30:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotDuration">slotDuration</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_DURATION,

    /**
     * Whether timed entries in timegrid views visually overlap. With {@code true}, at most half of each entry is
     * obscured; with {@code false}, entries never overlap.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotEventOverlap">slotEventOverlap</a>
     */
    SLOT_ENTRY_OVERLAP,

    /**
     * CSS classes for time slot headers, where time grid and timeline views show the date or time text of a slot.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isMajor}, {@code isMinor}, {@code isNarrow},
     *                         {@code hasNavLink}, {@code isFirst}, and {@code level} (only in timeline views when
     *                         {@link #SLOT_HEADER_FORMAT} is an array: the tier being rendered, 0 is the
     *                         bottom-most).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_HEADER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.isMajor ? 'major-slot' : '';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderClass</a>
     */
    SLOT_HEADER_CLASS("slotHeaderClass"),

    /**
     * Custom content for a time slot header. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isMajor}, {@code isMinor}, {@code isNarrow},
     *                         {@code hasNavLink}, {@code isFirst}, and {@code level} (only in timeline views when
     *                         {@link #SLOT_HEADER_FORMAT} is an array).</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_HEADER_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return info.isMajor ? { html: '<b>' + info.text + '</b>' } : info.text;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderContent</a>
     */
    SLOT_HEADER_CONTENT("slotHeaderContent"),

    /**
     * Called after a time slot header is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isMajor}, {@code isMinor}, {@code isNarrow},
     *                         {@code hasNavLink}, {@code isFirst}, {@code level} (only in timeline views when
     *                         {@link #SLOT_HEADER_FORMAT} is an array), and {@code el} (the element, only in
     *                         slotHeaderDidMount and slotHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_HEADER_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.title = info.date.toISOString();
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderDidMount</a>
     */
    SLOT_HEADER_DID_MOUNT("slotHeaderDidMount"),

    /**
     * Text format of the time slot headers. In timeline views, a list of formats creates multiple tiers of header rows.
     * <dl>
     *   <dt>Type</dt> <dd>format object, e.g. a {@code Map} with {@code hour}, {@code minute}, {@code meridiem}, and other properties</dd>
     *   <dt>Default</dt> <dd>{@code {hour: 'numeric', minute: '2-digit', omitZeroMinute: true, meridiem: 'short'}},
     *                        which shows times like {@code 5pm} and {@code 5:30pm} in English</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_HEADER_FORMAT,
     *         Map.of("hour", "2-digit", "minute", "2-digit", "hour12", false));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slotHeaderFormat">slotHeaderFormat</a>
     */
    SLOT_HEADER_FORMAT,

    /**
     * Interval at which time slots are labeled with a header text, e.g. {@code "01:00"} shows headers on the hour marks
     * even if {@link #SLOT_DURATION} is 15 or 30 minutes.
     * <dl>
     *   <dt>Type</dt> <dd>{@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>computed automatically from {@link #SLOT_DURATION}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotHeaderInterval">slotHeaderInterval</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_HEADER_INTERVAL,

    /**
     * Called before a time slot header is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isMajor}, {@code isMinor}, {@code isNarrow},
     *                         {@code hasNavLink}, {@code isFirst}, {@code level} (only in timeline views when
     *                         {@link #SLOT_HEADER_FORMAT} is an array), and {@code el} (the element, only in
     *                         slotHeaderDidMount and slotHeaderWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_HEADER_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing slot header', info.text);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderWillUnmount</a>
     */
    SLOT_HEADER_WILL_UNMOUNT("slotHeaderWillUnmount"),

    /**
     * Former name of {@link #SLOT_HEADER_CLASS}: CSS classes for time slot headers, where time grid and timeline views show the date or time text of a slot.
     * @deprecated use {@link #SLOT_HEADER_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_CLASS_NAMES("slotHeaderClass"),

    /**
     * Former name of {@link #SLOT_HEADER_CONTENT}: Custom content for a time slot header.
     * @deprecated use {@link #SLOT_HEADER_CONTENT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_CONTENT("slotHeaderContent"),

    /**
     * Former name of {@link #SLOT_HEADER_DID_MOUNT}: Called after a time slot header is added to the DOM. Accepts a {@link JsCallback}.
     * @deprecated use {@link #SLOT_HEADER_DID_MOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_DID_MOUNT("slotHeaderDidMount"),

    /**
     * Former name of {@link #SLOT_HEADER_FORMAT}: text format of the time slot headers.
     *
     * @deprecated use {@link #SLOT_HEADER_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_FORMAT("slotHeaderFormat"),

    /**
     * Former name of {@link #SLOT_HEADER_INTERVAL}: interval at which time slots are labeled.
     *
     * @deprecated use {@link #SLOT_HEADER_INTERVAL}, which sets the same FullCalendar option
     */
    @JsonConverter(DurationConverter.class)
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_INTERVAL("slotHeaderInterval"),

    /**
     * Former name of {@link #SLOT_HEADER_WILL_UNMOUNT}: Called before a time slot header is removed from the DOM. Accepts a {@link JsCallback}.
     * @deprecated use {@link #SLOT_HEADER_WILL_UNMOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_WILL_UNMOUNT("slotHeaderWillUnmount"),

    /**
     * CSS classes for the lane of a time slot. In time grid views this is the horizontal space passing under all days,
     * in timeline views the vertical space passing through the resources.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isMajor}, {@code isMinor}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_LANE_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.isMajor ? 'major-lane' : 'minor-lane';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneClass</a>
     */
    SLOT_LANE_CLASS("slotLaneClass"),

    /**
     * Former name of {@link #SLOT_LANE_CLASS}: CSS classes for the lane of a time slot. In time grid views this is the
     * horizontal space passing under all days, in timeline views the vertical space passing through the resources.
     * @deprecated use {@link #SLOT_LANE_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LANE_CLASS_NAMES("slotLaneClass"),

    /**
     * Called after a time slot lane is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isMajor}, {@code isMinor}, and {@code el} (the
     *                         element, only in slotLaneDidMount and slotLaneWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_LANE_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             if (info.isPast) info.el.style.opacity = 0.6;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneDidMount</a>
     */
    SLOT_LANE_DID_MOUNT("slotLaneDidMount"),

    /**
     * Called before a time slot lane is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code date}, {@code text}, {@code isPast},
     *                         {@code isFuture}, {@code isToday}, {@code isMajor}, {@code isMinor}, and {@code el} (the
     *                         element, only in slotLaneDidMount and slotLaneWillUnmount).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.SLOT_LANE_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing lane', info.date);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneWillUnmount</a>
     */
    SLOT_LANE_WILL_UNMOUNT("slotLaneWillUnmount"),

    /**
     * Last time slot displayed for each day, as an exclusive end time. {@code "24:00:00"} ends at midnight. The slot
     * limit also applies when the view is scrolled back all the way.
     * <dl>
     *   <dt>Type</dt> <dd>{@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "24:00:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotMaxTime">slotMaxTime</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_MAX_TIME,

    /**
     * First time slot displayed for each day. {@code "00:00:00"} starts at midnight. The slot limit also applies when the view is scrolled back all the way.
     * <dl>
     *   <dt>Type</dt> <dd>{@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "00:00:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinTime">slotMinTime</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_MIN_TIME,

    /**
     * Time interval to which a dragged entry snaps on the time axis. Also sets the granularity of selections.
     * <dl>
     *   <dt>Type</dt> <dd>{@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>same as {@link #SLOT_DURATION}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/snapDuration">snapDuration</a>
     */
    @JsonConverter(DurationConverter.class)
    SNAP_DURATION,

    /**
     * Former name of {@link #FOOTER_SCROLLBAR_STICKY}: whether the view's horizontal scrollbar is fixed to the bottom of the viewport.
     *
     * @deprecated use {@link #FOOTER_SCROLLBAR_STICKY}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    STICKY_FOOTER_SCROLLBAR("footerScrollbarSticky"),

    /**
     * Former name of {@link #TABLE_HEADER_STICKY}: whether the date headers are fixed to the top of the viewport.
     *
     * @deprecated use {@link #TABLE_HEADER_STICKY}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    STICKY_HEADER_DATES("tableHeaderSticky"),

    /**
     * Whether the date headers at the top of the calendar are fixed to the top of the viewport while the page is
     * scrolled vertically. List view day headings are always sticky.
     * With {@code "auto"}, the headers are sticky when the calendar height is {@code auto}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean} | {@code "auto"}</dd>
     *   <dt>Default</dt> <dd>{@code true}, set by the add-on (FullCalendar's own default is {@code "auto"})</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/tableHeaderSticky">tableHeaderSticky</a>
     */
    TABLE_HEADER_STICKY,

    /**
     * Time zone used for displaying and interpreting dates on the calendar. It affects the displayed times of entries,
     * their position on the calendar and the dates the client sends to the server.
     * <dl>
     *   <dt>Type</dt> <dd>{@code String} (e.g., {@code "local"}, {@code "UTC"}, {@code "America/New_York"}) | {@link Timezone}</dd>
     *   <dt>Default</dt> <dd>{@code "UTC"}, set by the add-on (FullCalendar's own default is {@code "local"}).
     *                        {@link FullCalendar#getTimezone()} returns {@code Timezone.UTC} as long as the option is
     *                        not set.</dd>
     * </dl>
     *
     * @see FullCalendar#setTimezone(Timezone)
     * @see FullCalendar#getTimezone()
     * @see <a href="https://fullcalendar.io/docs/timeZone">timeZone</a>
     */
    TIMEZONE("timeZone"),

    /**
     * Accessible label ({@code aria-label}) of the "today" button in the native FC toolbar.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback}</dd>
     *   <dt>Default</dt>  <dd>locale-dependent</dd>
     *   <dt>Callback</dt> <dd>{@code function(unitText, unitId)}. {@code unitText} is the localized unit text,
     *                         {@code unitId} a canonical string like {@code 'day'}, {@code 'week'} or {@code 'month'}.</dd>
     *   <dt>Returns</dt>  <dd>the label {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.TODAY_HINT, JsCallback.of("function(unitText, unitId) { return 'Jump to this ' + unitText; }"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">todayHint</a>
     */
    TODAY_HINT,

    /**
     * Whether clicking elsewhere on the page clears the current selection. Only applies when {@link #SELECTABLE} is {@code true}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/unselectAuto">unselectAuto</a>
     */
    UNSELECT_AUTO,

    /**
     * CSS selector for elements that, when clicked, do not clear the current selection (see {@link #UNSELECT_AUTO}).
     * Useful for a "create entry" form that opens after a selection.
     * <dl>
     *   <dt>Type</dt> <dd>CSS selector {@code String} (e.g., {@code ".dialog, .menu"})</dd>
     *   <dt>Default</dt> <dd>{@code ""}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/unselectCancel">unselectCancel</a>
     */
    UNSELECT_CANCEL,

    /**
     * Limits the dates the user can navigate to and where entries can go. Dates outside the range are grayed out,
     * entries cannot be dragged or resized into them, and the prev/next buttons are disabled when they would leave the
     * range.
     * <dl>
     *   <dt>Type</dt> <dd>object with {@code start} and/or {@code end} date strings (one may be omitted for an
     *                     open-ended range) | {@link JsCallback} returning such an object</dd>
     *   <dt>Callback</dt> <dd>{@code function(todayDate)}. {@code todayDate} is the start of the day of "now" as a
     *                         {@code Date}. The function is called several times per view render, so keep it cheap. No
     *                         info object is involved.</dd>
     *   <dt>Returns</dt> <dd>object with {@code start} and/or {@code end}</dd>
     * </dl>
     * <pre>{@code
     * // open-ended range: nothing before today
     * calendar.setOption(Option.VALID_RANGE,
     *         JsCallback.of("function(todayDate) { return { start: todayDate }; }"));
     * }</pre>
     *
     * @see FullCalendar#setValidRange(LocalDate, LocalDate)
     * @see FullCalendar#setValidRangeStart(LocalDate)
     * @see FullCalendar#setValidRangeEnd(LocalDate)
     * @see <a href="https://fullcalendar.io/docs/validRange">validRange</a>
     */
    VALID_RANGE,

    /**
     * CSS classes for the root element of the view. Called whenever the view changes.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view} (the view object).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.VIEW_CLASS, JsCallback.of("""
     *         function(info) {
     *             return 'view-' + info.view.type;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewClass</a>
     */
    VIEW_CLASS("viewClass"),

    /**
     * Former name of {@link #VIEW_CLASS}: CSS classes for the root element of the view.
     * @deprecated use {@link #VIEW_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    VIEW_CLASS_NAMES("viewClass"),

    /**
     * Called right after the root element of the view is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view} (the view object) and {@code el} (the root element of the view).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.VIEW_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('view mounted', info.view.title);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewDidMount</a>
     */
    VIEW_DID_MOUNT("viewDidMount"),

    /**
     * Accessible label for view-switcher buttons in the native FC toolbar, used for view buttons that have no
     * own entry in the {@code buttonHints}.
     * <dl>
     *   <dt>Type</dt>     <dd>{@code String} (use {@code $0} as placeholder for the localized button text, e.g.
     *                         {@code "$0 view"} results in "week view") | {@link JsCallback}</dd>
     *   <dt>Callback</dt> <dd>{@code function(buttonText, buttonName)}. {@code buttonText} is the localized button
     *                         text, {@code buttonName} the button name from the header or footer toolbar
     *                         (e.g. {@code "listWeek"}).</dd>
     *   <dt>Returns</dt>  <dd>the label {@code String}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.VIEW_HINT, JsCallback.of("""
     *         function(buttonText, buttonName) {
     *             return buttonText + (buttonName.startsWith('list') ? ' list view' : ' view');
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">viewHint</a>
     */
    VIEW_HINT("viewHint"),

    /**
     * Called right before the root element of the view is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code view} (the view object) and {@code el} (the root element of the view).</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.VIEW_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('view unmounting', info.view.type);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewWillUnmount</a>
     */
    VIEW_WILL_UNMOUNT("viewWillUnmount"),

    /**
     * Whether Saturday and Sunday columns are included in the calendar views.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekends">weekends</a>
     */
    WEEKENDS,

    /**
     * Whether week numbers are displayed: next to each row of days in month and daygrid views, and in the top-left corner of timegrid views.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}, set by the add-on (FullCalendar's own default is {@code false})</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumbers">weekNumbers</a>
     */
    WEEK_NUMBERS,

    /**
     * Method used to calculate the week numbers shown by {@link #WEEK_NUMBERS}. {@code "ISO"} also changes the default
     * of {@code firstDay} to {@code 1} (Monday).
     * <dl>
     *   <dt>Type</dt> <dd>{@code "local"} | {@code "ISO"} (ISO 8601) | {@link WeekNumberCalculation} | {@link JsCallback} returning an integer</dd>
     *   <dt>Default</dt> <dd>{@code "local"} (calculation of the calendar locale)</dd>
     *   <dt>Callback</dt> <dd>{@code function(date)}. {@code date} is a {@code Date}, passed directly, not wrapped in an info object.</dd>
     *   <dt>Returns</dt> <dd>the week number as integer</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.WEEK_NUMBER_CALCULATION, WeekNumberCalculation.ISO);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumberCalculation">weekNumberCalculation</a>
     */
    WEEK_NUMBER_CALCULATION,

    /**
     * Text format of the week numbers. The text depends on {@link #WEEK_TEXT_SHORT}: with {@code "W"}, {@code narrow}
     * gives {@code "W6"}, {@code short} gives {@code "W 6"}, {@code numeric} gives {@code "6"}.
     * <dl>
     *   <dt>Type</dt> <dd>format object, e.g. a {@code Map} with a {@code week} property</dd>
     *   <dt>Default</dt> <dd>{@code {week: 'narrow'}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.WEEK_NUMBER_FORMAT, Map.of("week", "numeric"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumberFormat">weekNumberFormat</a>
     */
    WEEK_NUMBER_FORMAT,

    /**
     * CSS classes for the week number in the header above the time axis of time grid views.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code String} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}, {@code options}. {@code options} is
     *                         the calendar options object.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.WEEK_NUMBER_HEADER_CLASS, JsCallback.of("""
     *         function(info) {
     *             return info.isNarrow ? 'narrow-week-number' : '';
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderClass</a>
     */
    WEEK_NUMBER_HEADER_CLASS("weekNumberHeaderClass"),

    /**
     * Custom content for the week number in the header above the time axis of time grid views. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Type</dt> <dd>text, or a {@link JsCallback} returning content</dd>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}, {@code options}. {@code options} is
     *                         the calendar options object.</dd>
     *   <dt>Returns</dt> <dd>unescaped text {@code String}, {@code {html: '...'}} or {@code {domNodes: [...]}}</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.WEEK_NUMBER_HEADER_CONTENT, JsCallback.of("""
     *         function(info) {
     *             return { html: '<i>' + info.text + '</i>' };
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderContent</a>
     */
    WEEK_NUMBER_HEADER_CONTENT("weekNumberHeaderContent"),

    /**
     * Called after the week number in the header above the time axis of time grid views is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}, {@code options}, and {@code el} (the
     *                         element, only in weekNumberHeaderDidMount and weekNumberHeaderWillUnmount).
     *                         {@code options} is the calendar options object.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.WEEK_NUMBER_HEADER_DID_MOUNT, JsCallback.of("""
     *         function(info) {
     *             info.el.title = 'Week ' + info.num;
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderDidMount</a>
     */
    WEEK_NUMBER_HEADER_DID_MOUNT("weekNumberHeaderDidMount"),

    /**
     * Called before the week number in the header above the time axis of time grid views is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Callback</dt> <dd>{@code function(info)}. {@code info} has {@code num}, {@code text}, {@code textParts},
     *                         {@code date}, {@code isNarrow}, {@code hasNavLink}, {@code options}, and {@code el} (the
     *                         element, only in weekNumberHeaderDidMount and weekNumberHeaderWillUnmount).
     *                         {@code options} is the calendar options object.</dd>
     * </dl>
     * <pre>{@code
     * calendar.setOption(Option.WEEK_NUMBER_HEADER_WILL_UNMOUNT, JsCallback.of("""
     *         function(info) {
     *             console.log('removing week number', info.num);
     *         }"""));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderWillUnmount</a>
     */
    WEEK_NUMBER_HEADER_WILL_UNMOUNT("weekNumberHeaderWillUnmount"),

    /**
     * Former name of {@link #WEEK_TEXT_SHORT}: short heading text for week numbers.
     *
     * @deprecated use {@link #WEEK_TEXT_SHORT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    WEEK_TEXT("weekTextShort"),

    /**
     * Like {@link #WEEK_TEXT_SHORT}, but only used when the {@code week} setting of a date format is {@code "long"}.
     * <dl>
     *   <dt>Type</dt> <dd>{@code String}</dd>
     *   <dt>Default</dt> <dd>{@code "Week"}, changes with the locale</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextLong">weekTextLong</a>
     */
    WEEK_TEXT_LONG,

    /**
     * Short heading text for week numbers (e.g., "W" in "W1"). Shown above the week number column in daygrid views and
     * next to the week number in the top-left cell of timegrid views. Also used for weeks in date formatting.
     * <dl>
     *   <dt>Type</dt> <dd>{@code String}</dd>
     *   <dt>Default</dt> <dd>{@code "W"}, changes with the locale</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextShort">weekTextShort</a>
     */
    WEEK_TEXT_SHORT,

    ;

    private final String optionKey;

    private static final Map<Option, List<JsonItemPropertyConverter<?, ?>>> CONVERTER_CACHE;

    static {
        Map<Option, List<JsonItemPropertyConverter<?, ?>>> map = new EnumMap<>(Option.class);
        for (Option opt : values()) {
            try {
                JsonConverter[] annotations = Option.class.getField(opt.name())
                        .getAnnotationsByType(JsonConverter.class);
                if (annotations.length > 0) {
                    List<JsonItemPropertyConverter<?, ?>> list = new ArrayList<>();
                    for (JsonConverter ann : annotations) {
                        list.add(ann.value().getConstructor().newInstance());
                    }
                    map.put(opt, Collections.unmodifiableList(list));
                }
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        CONVERTER_CACHE = Collections.unmodifiableMap(map);
    }

    Option() {
        this.optionKey = CaseUtils.toCamelCase(name().replace("ENTRY", "EVENT"), false, '_');
    }

    Option(String optionKey) {
        this.optionKey = optionKey;
    }

    String getOptionKey() {
        return optionKey;
    }

    /**
     * Returns the converters registered for this option via {@link JsonConverter} annotations, in order.
     */
    public List<JsonItemPropertyConverter<?, ?>> getConverters() {
        return CONVERTER_CACHE.getOrDefault(this, List.of());
    }

    /**
     * If this option has one or more {@link JsonConverter} annotations and the given value is
     * supported by one of them, returns the converted {@link JsonNode}. Otherwise returns empty.
     */
    @SuppressWarnings("unchecked")
    public Optional<JsonNode> convertValue(Object value) {
        for (JsonItemPropertyConverter<?, ?> c : getConverters()) {
            if (c.supports(value)) {
                return Optional.of(((JsonItemPropertyConverter<Object, Object>) c).toClientModel(value, null));
            }
        }
        return Optional.empty();
    }
}

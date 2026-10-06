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
     * Show or hide the all-day row at the top of timegrid views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/allDaySlot">allDaySlot</a>
     */
    ALL_DAY_SLOT,


    /**
     * Width-to-height ratio of the calendar container.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (e.g., {@code 1.35})</dd>
     *   <dt>Default</dt> <dd>{@code 1.35}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/aspectRatio">aspectRatio</a>
     */
    ASPECT_RATIO,

    /**
     * Highlight business hours on the calendar ({@code true} uses default 9am–5pm Mon–Fri).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean} | {@link BusinessHours} | {@code BusinessHours[]}</dd>
     *   <dt>Default</dt> <dd>{@code false} (disabled)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/businessHours">businessHours</a>
     */
    @JsonConverter(BusinessHoursConverter.class)
    BUSINESS_HOURS,

    /**
     * Show or hide column header cells at the top of the view.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dayHeaders">dayHeaders</a>
     */
    DAY_HEADERS,

    /**
     * Height of the calendar's event area.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels) | {@code string} (e.g., {@code "500px"}, {@code "100%"}) | {@code "auto"}</dd>
     *   <dt>Default</dt> <dd>{@code "auto"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/contentHeight">contentHeight</a>
     */
    CONTENT_HEIGHT,

    /**
     * Format of the day column headers.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code month}, {@code day}, {@code weekday}, and {@code meridiem} properties</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dayHeaderFormat">dayHeaderFormat</a>
     */
    DAY_HEADER_FORMAT,

    /**
     * Minimum pixel width of each day column; enables horizontal scrolling on narrow views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>none (columns grow to fit available space)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dayMinWidth">dayMinWidth</a>
     */
    DAY_MIN_WIDTH,

    /**
     * Show or hide the time label on entry elements.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true} for timed entries in agenda views, {@code false} for all-day entries</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/displayEventTime">displayEventTime</a>
     */
    DISPLAY_ENTRY_TIME,

    /**
     * Text direction of the calendar.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code "ltr"} (left-to-right) | {@code "rtl"} (right-to-left)</dd>
     *   <dt>Default</dt> <dd>{@code "ltr"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/direction">direction</a>
     */
    DIRECTION,

    /**
     * Auto-scroll the view when dragging entries near the viewport edges.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dragScroll">dragScroll</a>
     */
    DRAG_SCROLL,

    /**
     * Master switch for entry dragging and resizing.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     * Individual entries can override via {@link Entry#setEditable(boolean)}.
     *
     * @see <a href="https://fullcalendar.io/docs/editable">editable</a>
     */
    EDITABLE,

    /**
     * Default color for all entries. The theme decides which parts of an entry it colors
     * (background, border, dot).
     * <dl>
     *   <dt>Type</dt> <dd>CSS color string</dd>
     * </dl>
     * Can be overridden per-entry via {@link Entry#setColor(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/eventColor">eventColor</a>
     */
    ENTRY_COLOR,

    /**
     * Allow resizing (duration editing) of entries.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true} — effective only when {@link #EDITABLE} is also {@code true}; when {@code EDITABLE} is {@code false} (the default), this setting has no effect</dd>
     * </dl>
     * Can be overridden per-entry.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDurationEditable">eventDurationEditable</a>
     */
    ENTRY_DURATION_EDITABLE,

    /**
     * Maximum number of overlapping entries rendered in a time slot before showing a "+N more" link.
     * In timeGrid view entries stack left-to-right; in timeline view they stack top-to-bottom.
     * Does not apply to dayGrid view (use {@link #DAY_MAX_ENTRIES} instead).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} | {@code null} (no limit)</dd>
     *   <dt>Default</dt> <dd>{@code null} (all entries shown)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventMaxStack">eventMaxStack</a>
     */
    ENTRY_MAX_STACK,

    /**
     * Minimum pixel height of entries in timegrid views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>{@code 15}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventMinHeight">eventMinHeight</a>
     */
    ENTRY_MIN_HEIGHT,

    /**
     * Sort order for entries within a time slot.
     * <dl>
     *   <dt>Static value</dt> <dd>{@code string} (e.g., {@code "title"}) | array of sort keys | {@code -1} for reverse order</dd>
     *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(a, b) { return a.title.localeCompare(b.title); }")}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrder">eventOrder</a>
     */
    ENTRY_ORDER,

    /**
     * Prevent entries from being reordered when they have the same start time.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrderStrict">eventOrderStrict</a>
     */
    ENTRY_ORDER_STRICT,

    /**
     * Default display mode for entries.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code "auto"} | {@code "block"} | {@code "list-item"} | {@code "background"} | {@code "inverse-background"} | {@code "none"} | {@link DisplayMode}</dd>
     *   <dt>Default</dt> <dd>{@code "auto"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventDisplay">eventDisplay</a>
     */
    ENTRY_DISPLAY,

    /**
     * Allow resizing entries from their start edge.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventResizableFromStart">eventResizableFromStart</a>
     */
    ENTRY_RESIZABLE_FROM_START,

    /**
     * Pixel height threshold below which the time label is hidden on entries.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>{@code 30}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventShortHeight">eventShortHeight</a>
     */
    ENTRY_SHORT_HEIGHT,

    /**
     * Allow dragging entries to change their start time.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true} — effective only when {@link #EDITABLE} is also {@code true}; when {@code EDITABLE} is {@code false} (the default), this setting has no effect</dd>
     * </dl>
     * Can be overridden per-entry.
     *
     * @see <a href="https://fullcalendar.io/docs/eventStartEditable">eventStartEditable</a>
     */
    ENTRY_START_EDITABLE,

    /**
     * Default contrast color for all entries, used for text and other elements drawn on the entry color.
     * <dl>
     *   <dt>Type</dt> <dd>CSS color string</dd>
     * </dl>
     * Can be overridden per-entry via {@link Entry#setContrastColor(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/eventContrastColor">eventContrastColor</a>
     */
    ENTRY_CONTRAST_COLOR,

    /**
     * @deprecated use {@link #ENTRY_CONTRAST_COLOR}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    ENTRY_TEXT_COLOR("eventContrastColor"),

    /**
     * Format of the time shown on entry elements.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code hour}, {@code minute}, {@code meridiem}, and other properties</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventTimeFormat">eventTimeFormat</a>
     */
    ENTRY_TIME_FORMAT,

    /**
     * Stretch row heights to fill the view vertically.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/expandRows">expandRows</a>
     */
    EXPAND_ROWS,

    /**
     * First day of the week (0 = Sunday, 6 = Saturday).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} | {@link java.time.DayOfWeek}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/firstDay">firstDay</a>
     */
    @JsonConverter(DayOfWeekConverter.class)
    FIRST_DAY,

    /**
     * Always display 6 weeks in month view.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/fixedWeekCount">fixedWeekCount</a>
     */
    FIXED_WEEK_COUNT,

    /**
     * Configuration for the footer toolbar buttons.
     * <dl>
     *   <dt>Type</dt> <dd>object with {@code left}, {@code center}, and {@code right} properties | {@link org.vaadin.stefan.fullcalendar.model.Footer} | {@code Map<String, String>}</dd>
     * </dl>
     * Pass a {@code Map<String, String>} with keys {@code "left"}, {@code "center"}, {@code "right"}
     * and FC button-name strings as values, e.g.:
     * {@code Map.of("left", "prev,next,today", "center", "title", "right", "dayGridMonth,timeGridWeek")}.
     *
     * @see <a href="https://fullcalendar.io/docs/footerToolbar">footerToolbar</a>
     */
    @JsonConverter(ToolbarConverter.class)
    FOOTER_TOOLBAR,

    /**
     * Configuration for the header toolbar buttons.
     * <dl>
     *   <dt>Type</dt> <dd>object with {@code left}, {@code center}, and {@code right} properties | {@link org.vaadin.stefan.fullcalendar.model.Header} | {@code Map<String, String>}</dd>
     * </dl>
     * Pass a {@code Map<String, String>} with keys {@code "left"}, {@code "center"}, {@code "right"}
     * and FC button-name strings as values, e.g.:
     * {@code Map.of("left", "prev,next,today", "center", "title", "right", "dayGridMonth,timeGridWeek")}.
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    @JsonConverter(ToolbarConverter.class)
    HEADER_TOOLBAR,

    /**
     * Total height of the calendar.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (pixels) | {@code string} (e.g., {@code "500px"}, {@code "100%"}) | {@code "auto"}</dd>
     *   <dt>Default</dt> <dd>none (fills parent container)</dd>
     * </dl>
     *
     * @see FullCalendar#setHeight(String)
     * @see <a href="https://fullcalendar.io/docs/height">height</a>
     */
    HEIGHT,

    /**
     * Days of the week to hide (0 = Sunday, 6 = Saturday).
     * <dl>
     *   <dt>Type</dt>    <dd>array of {@code integer} | {@code DayOfWeek[]} | {@code Collection<DayOfWeek>}</dd>
     *   <dt>Default</dt> <dd>none (all days shown)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/hiddenDays">hiddenDays</a>
     */
    @JsonConverter(DayOfWeekArrayConverter.class)
    HIDDEN_DAYS,

    /**
     * Format of the date column (left side) in list view.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code year}, {@code month}, {@code day}, and other properties</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/listDayFormat">listDayFormat</a>
     */
    LIST_DAY_FORMAT,

    /**
     * Format of the secondary date text in the list view day headings.
     * <dl>
     *   <dt>Type</dt> <dd>format object | {@code false} to hide the text</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/listDayAltFormat">listDayAltFormat</a>
     */
    LIST_DAY_ALT_FORMAT,

    /**
     * @deprecated use {@link #LIST_DAY_ALT_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    LIST_DAY_SIDE_FORMAT("listDayAltFormat"),

    /**
     * Locale/language code for displaying calendar text.
     * <dl>
     *   <dt>Type</dt>    <dd>language code {@code string} (e.g., {@code "en"}, {@code "de"}, {@code "fr"}) | {@link java.util.Locale}</dd>
     *   <dt>Default</dt> <dd>browser language</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">locale</a>
     */
    @JsonConverter(LocaleConverter.class)
    LOCALE,

    /**
     * Maximum number of entries visible in a day cell before showing a '+N more' link.
     * <dl>
     *   <dt>Type</dt> <dd>{@code false} | {@code integer} | {@code true} (false = no limit, integer = fixed count, true = limit to cell height)</dd>
     * </dl>
     *
     * @see FullCalendar#setMaxEntriesPerDay(int)
     * @see FullCalendar#setMaxEntriesPerDayFitToCell()
     * @see FullCalendar#setMaxEntriesPerDayUnlimited()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">dayMaxEvents</a>
     */
    DAY_MAX_ENTRIES("dayMaxEvents"),

    /**
     * @deprecated use {@link #DAY_MAX_ENTRIES}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MAX_ENTRIES_PER_DAY("dayMaxEvents"),

    /**
     * The view the calendar renders on first attach. Pass a {@link CalendarView}'s
     * {@link CalendarView#getClientSideValue() client-side value} (for example
     * {@code CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue()}).
     * <p>
     * Set this before attach to skip the {@code changeView()}-after-attach workaround
     * that was necessary in earlier FC versions.
     * <dl>
     *   <dt>Type</dt> <dd>String (FC view key, e.g. {@code "timeGridWeek"})</dd>
     * </dl>
     *
     * @see FullCalendar#changeView(CalendarView)
     * @see <a href="https://fullcalendar.io/docs/initialView">initialView</a>
     * @since 7.2.0
     */
    INITIAL_VIEW("initialView"),

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
     * Format of the month label in multi-month grid views.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code month}, {@code year}, and other properties</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/monthStartFormat">monthStartFormat</a>
     */
    MONTH_START_FORMAT,

    /**
     * Maximum number of columns in multi-month view.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer}</dd>
     *   <dt>Default</dt> <dd>{@code 3}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/multiMonthMaxColumns">multiMonthMaxColumns</a>
     */
    MULTI_MONTH_MAX_COLUMNS,

    /**
     * Minimum pixel width of each month in multi-month view, padding included, before months wrap to the next row.
     * <dl>
     *   <dt>Type</dt> <dd>{@code number} (pixels)</dd>
     *   <dt>Default</dt> <dd>auto-calculated</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/singleMonthMinWidth">singleMonthMinWidth</a>
     */
    SINGLE_MONTH_MIN_WIDTH,

    /**
     * @deprecated use {@link #SINGLE_MONTH_MIN_WIDTH}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MULTI_MONTH_MIN_WIDTH("singleMonthMinWidth"),

    /**
     * Format of each month's title in multi-month view.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code month}, {@code year}, and other properties</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/singleMonthTitleFormat">singleMonthTitleFormat</a>
     */
    SINGLE_MONTH_TITLE_FORMAT,

    /**
     * @deprecated use {@link #SINGLE_MONTH_TITLE_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MULTI_MONTH_TITLE_FORMAT("singleMonthTitleFormat"),

    /**
     * Make day/week numbers clickable to navigate to that period.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     * Can be overridden by {@link Option#NAV_LINK_DAY_CLICK} and {@link Option#NAV_LINK_WEEK_CLICK}.
     *
     * @see <a href="https://fullcalendar.io/docs/navLinks">navLinks</a>
     */
    NAV_LINKS,

    /**
     * Time threshold at which a multi-day entry transitions to display on the next day.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "00:00:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/nextDayThreshold">nextDayThreshold</a>
     */
    @JsonConverter(DurationConverter.class)
    NEXT_DAY_THRESHOLD,

    /**
     * Show a visual indicator for the current time.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     * Only works with timegrid views.
     *
     * @see <a href="https://fullcalendar.io/docs/nowIndicator">nowIndicator</a>
     */
    NOW_INDICATOR,

    /**
     * Initial scroll position in timegrid views (time of day from top of viewport).
     * <dl>
     *   <dt>Type</dt>    <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "06:00:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/scrollTime">scrollTime</a>
     */
    @JsonConverter(DurationConverter.class)
    SCROLL_TIME,

    /**
     * Reset the scroll position when navigating to a different view.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
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
     * Restricts where the user can make time-range selections.
     * <dl>
     *   <dt>Type</dt> <dd>group id {@code string} | {@code "businessHours"}</dd>
     * </dl>
     * To pass a {@link BusinessHours} object, serialize it to JSON via
     * {@code setOption(SELECT_CONSTRAINT, businessHours.toJson())}.
     *
     * @see <a href="https://fullcalendar.io/docs/selectConstraint">selectConstraint</a>
     */
    SELECT_CONSTRAINT,

    /**
     * Minimum drag distance in pixels before a selection is initiated.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number}</dd>
     *   <dt>Default</dt> <dd>{@code 0}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectMinDistance">selectMinDistance</a>
     */
    SELECT_MIN_DISTANCE,

    /**
     * Show a placeholder entry while selecting a time range.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectMirror">selectMirror</a>
     */
    SELECT_MIRROR,

    /**
     * Controls whether a time selection can overlap an existing entry.
     * <dl>
     *   <dt>Static value</dt> <dd>{@code boolean} ({@code true} allows overlap, {@code false} prevents it)</dd>
     *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(event) { return event.extendedProps.allowOverlap !== false; }")}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectOverlap">selectOverlap</a>
     */
    SELECT_OVERLAP,

    /**
     * Show dates from adjacent months in month view.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/showNonCurrentDates">showNonCurrentDates</a>
     */
    SHOW_NON_CURRENT_DATES,

    /**
     * Duration of each time slot in timegrid and timeline views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "00:30:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotDuration">slotDuration</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_DURATION,

    /**
     * Allow entries in the same timegrid slot to overlap visually.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotEventOverlap">slotEventOverlap</a>
     */
    SLOT_ENTRY_OVERLAP,

    /**
     * Format of the time slot headers.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code hour}, {@code minute}, {@code meridiem}, and other properties</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotHeaderFormat">slotHeaderFormat</a>
     */
    SLOT_HEADER_FORMAT,

    /**
     * @deprecated use {@link #SLOT_HEADER_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_FORMAT("slotHeaderFormat"),

    /**
     * Interval between visible time slot headers.
     * <dl>
     *   <dt>Type</dt> <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>auto-computed based on {@link #SLOT_DURATION}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotHeaderInterval">slotHeaderInterval</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_HEADER_INTERVAL,

    /**
     * @deprecated use {@link #SLOT_HEADER_INTERVAL}, which sets the same FullCalendar option
     */
    @JsonConverter(DurationConverter.class)
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_INTERVAL("slotHeaderInterval"),

    /**
     * End of the visible time range in timegrid views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "24:00:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotMaxTime">slotMaxTime</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_MAX_TIME,

    /**
     * Start of the visible time range in timegrid views.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>{@code "00:00:00"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinTime">slotMinTime</a>
     */
    @JsonConverter(DurationConverter.class)
    SLOT_MIN_TIME,

    /**
     * Snap interval when dragging entries.
     * <dl>
     *   <dt>Type</dt>    <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
     *   <dt>Default</dt> <dd>same as {@link #SLOT_DURATION}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/snapDuration">snapDuration</a>
     */
    @JsonConverter(DurationConverter.class)
    SNAP_DURATION,

    /**
     * Fix the view's horizontal scrollbar to the bottom of the viewport while scrolling.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean} | {@code "auto"} (sticky when the height is {@code auto})</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/footerScrollbarSticky">footerScrollbarSticky</a>
     */
    FOOTER_SCROLLBAR_STICKY,

    /**
     * @deprecated use {@link #FOOTER_SCROLLBAR_STICKY}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    STICKY_FOOTER_SCROLLBAR("footerScrollbarSticky"),

    /**
     * Fix the date headers to the top of the viewport while scrolling. List view day headings are always sticky.
     * <dl>
     *   <dt>Type</dt> <dd>{@code boolean} | {@code "auto"} (sticky when the height is {@code auto})</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/tableHeaderSticky">tableHeaderSticky</a>
     */
    TABLE_HEADER_STICKY,

    /**
     * @deprecated use {@link #TABLE_HEADER_STICKY}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    STICKY_HEADER_DATES("tableHeaderSticky"),

    /**
     * Time zone used for displaying and interpreting dates on the calendar.
     * <dl>
     *   <dt>Type</dt> <dd>{@code string} (e.g., {@code "local"}, {@code "UTC"}, {@code "America/New_York"}) | {@link Timezone}</dd>
     * </dl>
     *
     * @see FullCalendar#setTimezone(Timezone)
     * @see FullCalendar#getTimezone()
     * @see <a href="https://fullcalendar.io/docs/timeZone">timeZone</a>
     */
    TIMEZONE("timeZone"),

    /**
     * Deselect time range selections when clicking outside the selection.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/unselectAuto">unselectAuto</a>
     */
    UNSELECT_AUTO,

    /**
     * CSS selector for elements that, when clicked, won't deselect the current selection.
     * <dl>
     *   <dt>Type</dt> <dd>CSS selector {@code string} (e.g., {@code ".dialog, .menu"})</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/unselectCancel">unselectCancel</a>
     */
    UNSELECT_CANCEL,

    /**
     * Restrict the navigable date range.
     * <dl>
     *   <dt>Static value</dt> <dd>object with {@code start} and {@code end} date strings</dd>
     *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(nowDate) { return { start: '2024-01-01', end: '2024-12-31' }; }")}</dd>
     * </dl>
     *
     * @see FullCalendar#setValidRange(LocalDate, LocalDate)
     * @see FullCalendar#setValidRangeStart(LocalDate)
     * @see FullCalendar#setValidRangeEnd(LocalDate)
     * @see <a href="https://fullcalendar.io/docs/validRange">validRange</a>
     */
    VALID_RANGE,

    /**
     * Show or hide weekends.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekends">weekends</a>
     */
    WEEKENDS,

    /**
     * Show week number cells/columns.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumbers">weekNumbers</a>
     */
    WEEK_NUMBERS,

    /**
     * Algorithm for calculating week numbers.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code "locale"} | {@code "ISO"} (ISO 8601) | {@link WeekNumberCalculation}</dd>
     *   <dt>Default</dt> <dd>{@code "locale"}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumberCalculation">weekNumberCalculation</a>
     */
    WEEK_NUMBER_CALCULATION,

    /**
     * Format of the week number cell.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code week} property</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumberFormat">weekNumberFormat</a>
     */
    WEEK_NUMBER_FORMAT,

    /**
     * Short text prepended to week numbers (e.g., "W" in "W1", "W2").
     * <dl>
     *   <dt>Type</dt> <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextShort">weekTextShort</a>
     */
    WEEK_TEXT_SHORT,

    /**
     * @deprecated use {@link #WEEK_TEXT_SHORT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    WEEK_TEXT("weekTextShort"),

    /**
     * Long form of the week text for wider views (e.g., "Week" in "Week 1").
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextLong">weekTextLong</a>
     */
    WEEK_TEXT_LONG,



    /**
     * Keep duration when dragging a timed entry to/from the all-day slot.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/allDayMaintainDuration">allDayMaintainDuration</a>
     */
    ALL_DAY_MAINTAIN_DURATION,

    /**
     * Default all-day status for entries without an explicit time.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/defaultAllDay">defaultAllDay</a>
     */
    DEFAULT_ALL_DAY,

    /**
     * Maximum number of entry rows in month view cells.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code integer} | {@code true} (auto-calculate based on cell height)</dd>
     *   <dt>Default</dt> <dd>auto-calculated</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
     */
    DAY_MAX_ENTRY_ROWS,

    /**
     * @deprecated use {@link #DAY_MAX_ENTRY_ROWS}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_MAX_EVENT_ROWS,

    /**
     * Format of the "+N more" popover title.
     * <dl>
     *   <dt>Type</dt> <dd>format object with {@code month}, {@code day}, {@code year}, and other properties</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/popoverFormat">popoverFormat</a>
     */
    POPOVER_FORMAT,

    /**
     * @deprecated use {@link #POPOVER_FORMAT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_POPOVER_FORMAT("popoverFormat"),

    /**
     * Show end time on entry elements.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true} for timed entries in agenda views, {@code false} for all-day entries</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/displayEventEnd">displayEventEnd</a>
     */
    DISPLAY_ENTRY_END,

    /**
     * @deprecated use {@link #DISPLAY_ENTRY_END}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DISPLAY_EVENT_END,

    /**
     * Duration of the animation when a dropped entry reverts to its original position (rejected drop).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} (milliseconds)</dd>
     *   <dt>Default</dt> <dd>{@code 500}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dragRevertDuration">dragRevertDuration</a>
     */
    DRAG_REVERT_DURATION,

    /**
     * Minimum drag distance in pixels before dragging an entry begins.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number}</dd>
     *   <dt>Default</dt> <dd>{@code 5}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventDragMinDistance">eventDragMinDistance</a>
     */
    ENTRY_DRAG_MIN_DISTANCE,

    /**
     * Long press delay (in milliseconds) for initiating drag on touch devices.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number}</dd>
     *   <dt>Default</dt> <dd>inherits from {@link #LONG_PRESS_DELAY}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventLongPressDelay">eventLongPressDelay</a>
     */
    ENTRY_LONG_PRESS_DELAY,

    /**
     * Force display of end time on entries even when duration is not set.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/forceEventDuration">forceEventDuration</a>
     */
    FORCE_ENTRY_DURATION,

    /**
     * @deprecated use {@link #FORCE_ENTRY_DURATION}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    FORCE_EVENT_DURATION,


    /**
     * Only fetch entries for the currently visible date range.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/lazyFetching">lazyFetching</a>
     */
    LAZY_FETCHING,

    /**
     * Long press delay in milliseconds for touch interactions (drag/select).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number}</dd>
     *   <dt>Default</dt> <dd>{@code 1000}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/longPressDelay">longPressDelay</a>
     */
    LONG_PRESS_DELAY,

    /**
     * Snap dragged entries to the now indicator position.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorSnap">nowIndicatorSnap</a>
     */
    NOW_INDICATOR_SNAP,

    /**
     * Render entries in batches for performance.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/progressiveEventRendering">progressiveEventRendering</a>
     */
    PROGRESSIVE_ENTRY_RENDERING,

    /**
     * @deprecated use {@link #PROGRESSIVE_ENTRY_RENDERING}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    PROGRESSIVE_EVENT_RENDERING,

    /**
     * Delay (in milliseconds) before re-rendering entries.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number} | {@code null} (immediate)</dd>
     *   <dt>Default</dt> <dd>{@code null}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/rerenderDelay">rerenderDelay</a>
     */
    RERENDER_DELAY,

    /**
     * Long press delay (in milliseconds) before a selection begins on touch devices.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code number}</dd>
     *   <dt>Default</dt> <dd>inherits from {@link #LONG_PRESS_DELAY}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectLongPressDelay">selectLongPressDelay</a>
     */
    SELECT_LONG_PRESS_DELAY,




    /**
     * Controls whether a time-range selection is allowed. Accepts a {@link JsCallback}.
     * Called on every mouse move during selection drag; must return boolean synchronously.
     * <dl>
     *   <dt>Function</dt>  <dd>{@code JsCallback.of("function(selectInfo) { return selectInfo.start.getDay() !== 0; }")}</dd>
     *   <dt>Arguments</dt> <dd>{@code {selectInfo}}</dd>
     *   <dt>Returns</dt>   <dd>boolean</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/selectAllow">selectAllow</a>
     */
    SELECT_ALLOW,

    /**
     * Controls whether an entry drag-and-drop is allowed. Accepts a {@link JsCallback}.
     * {@code draggedEvent} has {@code getCustomProperty()} available.
     * <dl>
     *   <dt>Function</dt>  <dd>{@code JsCallback.of("function(dropInfo, draggedEvent) { return dropInfo.start.getDay() !== 0; }")}</dd>
     *   <dt>Arguments</dt> <dd>{@code {dropInfo, draggedEvent}}</dd>
     *   <dt>Returns</dt>   <dd>boolean</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventAllow">eventAllow</a>
     */
    ENTRY_ALLOW,

    /**
     * Controls whether entries may overlap during dragging.
     * <dl>
     *   <dt>Static value</dt> <dd>{@code boolean} ({@code false} prevents any overlap)</dd>
     *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(stillEvent, movingEvent) { return true; }")}</dd>
     *   <dt>Default</dt>      <dd>{@code true}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
     */
    ENTRY_OVERLAP,

    /**
     * Allow dropping external DOM elements onto the calendar.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/droppable">droppable</a>
     */
    DROPPABLE,

    /**
     * Filter which external DOM elements can be dropped onto the calendar.
     * <dl>
     *   <dt>Static value</dt> <dd>CSS selector {@code string}</dd>
     *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(draggable) { return draggable.classList.contains('acceptable'); }")}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
     */
    DROP_ACCEPT,



    /**
     * Default query parameter name for the range start sent to JSON feed event sources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>{@code "start"}</dd>
     * </dl>
     * Per-source override: {@link JsonFeedEventSource#withStartParam(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/startParam">startParam</a>
     */
    ENTRY_SOURCE_START_PARAM("startParam"),

    /**
     * @deprecated use {@link #ENTRY_SOURCE_START_PARAM}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_START_PARAM("startParam"),

    /**
     * Default query parameter name for the range end sent to JSON feed event sources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>{@code "end"}</dd>
     * </dl>
     * Per-source override: {@link JsonFeedEventSource#withEndParam(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/endParam">endParam</a>
     */
    ENTRY_SOURCE_END_PARAM("endParam"),

    /**
     * @deprecated use {@link #ENTRY_SOURCE_END_PARAM}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_END_PARAM("endParam"),

    /**
     * Default query parameter name for the timezone sent to JSON feed event sources.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>{@code "timeZone"}</dd>
     * </dl>
     * Per-source override: {@link JsonFeedEventSource#withTimeZoneParam(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/timeZoneParam">timeZoneParam</a>
     */
    ENTRY_SOURCE_TIME_ZONE_PARAM("timeZoneParam"),

    /**
     * @deprecated use {@link #ENTRY_SOURCE_TIME_ZONE_PARAM}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM("timeZoneParam"),

    /**
     * Global Google Calendar API key used by all {@link GoogleCalendarEventSource} instances that do not specify their own key.
     * <dl>
     *   <dt>Type</dt> <dd>{@code string}</dd>
     * </dl>
     * Per-source override: {@link GoogleCalendarEventSource#withApiKey(String)}.
     *
     * @see <a href="https://fullcalendar.io/docs/google-calendar">googleCalendarApiKey</a>
     */
    ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY("googleCalendarApiKey"),

    /**
     * @deprecated use {@link #ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY("googleCalendarApiKey"),



    /**
     * Make entries focusable for keyboard accessibility.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code boolean}</dd>
     *   <dt>Default</dt> <dd>{@code false} (only entries with a {@code url} are focusable by default)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventInteractive">eventInteractive</a>
     */
    ENTRY_INTERACTIVE,



    /**
     * Restrict where entries can be dragged or resized.
     * <dl>
     *   <dt>Type</dt> <dd>group id {@code string} | {@code "businessHours"} | {@link BusinessHours}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">eventConstraint</a>
     */
    @JsonConverter(BusinessHoursConverter.class)
    ENTRY_CONSTRAINT,

    /**
     * How much the calendar advances or retreats when navigating with prev/next buttons.
     * <dl>
     *   <dt>Type</dt>    <dd>duration {@code string} (e.g., {@code "P1M"} for one month)</dd>
     *   <dt>Default</dt> <dd>view-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dateIncrement">dateIncrement</a>
     */
    DATE_INCREMENT,

    /**
     * Alignment of the date range when navigating.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code "week"} | {@code "day"} | other alignment options</dd>
     *   <dt>Default</dt> <dd>view-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/dateAlignment">dateAlignment</a>
     */
    DATE_ALIGNMENT,

    /**
     * Accessible label ({@code aria-label}) of the "today" button in the native FC toolbar.
     * <dl>
     *   <dt>Type</dt> <dd>{@code string} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback} {@code function(unitText, unit)}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">todayHint</a>
     */
    TODAY_HINT,

    /**
     * Accessible label ({@code aria-label}) of the "prev" button in the native FC toolbar.
     * <dl>
     *   <dt>Type</dt> <dd>{@code string} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback} {@code function(unitText, unit)}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">prevHint</a>
     */
    PREV_HINT,

    /**
     * Accessible label ({@code aria-label}) of the "next" button in the native FC toolbar.
     * <dl>
     *   <dt>Type</dt> <dd>{@code string} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback} {@code function(unitText, unit)}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">nextHint</a>
     */
    NEXT_HINT,

    /**
     * Accessible label for the view-switcher buttons in the native FC toolbar.
     * <dl>
     *   <dt>Type</dt> <dd>{@code string} (use {@code $0} as placeholder for the view name, e.g., {@code "Switch to $0 view"})</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">viewHint</a>
     */
    VIEW_HINT("viewHint"),

    /**
     * @deprecated use {@link #VIEW_HINT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    NATIVE_TOOLBAR_VIEW_HINT("viewHint"),

    /**
     * Accessible hint ({@code aria-label}) for clickable day/week numbers.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">navLinkHint</a>
     */
    NAV_LINK_HINT,

    /**
     * Accessible hint ({@code aria-label}) for the "+N more" link.
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">moreLinkHint</a>
     */
    MORE_LINK_HINT,

    /**
     * Action for the "+N more" link click.
     * <dl>
     *   <dt>Static value</dt> <dd>{@code string} ({@code "popover"} | {@code "day"} | {@code "week"} | view name)</dd>
     *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(info) { console.log('Clicked more link'); return 'day'; }")}</dd>
     *   <dt>Default</dt>      <dd>{@code "popover"}</dd>
     * </dl>
     *
     * @see FullCalendar#setMoreLinkClickAction(MoreLinkClickAction)
     * @see <a href="https://fullcalendar.io/docs/moreLinkClick">moreLinkClick</a>
     */
    MORE_LINK_CLICK,

    /**
     * Accessible hint ({@code aria-label}) for close buttons (e.g., in popovers).
     * <dl>
     *   <dt>Type</dt>    <dd>{@code string}</dd>
     *   <dt>Default</dt> <dd>locale-dependent</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">closeHint</a>
     */
    CLOSE_HINT,


    // ---- Render hooks: Entry ----
    /**
     * CSS classes for entry elements.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {event, timeText, isStart, isEnd, isMirror, isPast, isFuture, isToday, isSelected, isDragging, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventClass</a>
     */
    ENTRY_CLASS("eventClass"),

    /**
     * @deprecated use {@link #ENTRY_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    ENTRY_CLASS_NAMES("eventClass"),

    /**
     * Custom content for an entry element. Accepts a {@link JsCallback}.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {event, timeText, isStart, isEnd, isMirror, isPast, isFuture, isToday, isSelected, isDragging, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventContent</a>
     */
    ENTRY_CONTENT("eventContent"),

    /**
     * Called after an entry element is added to the DOM. Accepts a {@link JsCallback}.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {event, timeText, isStart, isEnd, isMirror, isPast, isFuture, isToday, isSelected, isDragging, view, el}}</dd>
     * </dl>
     * <p>
     * When using this option, any native event listeners registered via
     * {@link FullCalendar#addEntryNativeEventListener(String, String)} are automatically
     * merged into the callback.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventDidMount</a>
     */
    ENTRY_DID_MOUNT("eventDidMount"),

    /**
     * Called before an entry element is removed from the DOM. Accepts a {@link JsCallback}.
     * {@code info.event} has {@code getCustomProperty()} available. Background entries use their own
     * {@code backgroundEvent*} hooks.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {event, timeText, isStart, isEnd, isMirror, isPast, isFuture, isToday, isSelected, isDragging, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventWillUnmount</a>
     */
    ENTRY_WILL_UNMOUNT("eventWillUnmount"),

    // ---- Render hooks: Day Cell ----
    /**
     * CSS classes for day cells in day grid views and the "+N more" popover body.
     * Time grid day columns use {@link #DAY_LANE_CLASS}.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, dayNumberText, isToday, isPast, isFuture, isOther, isDisabled, inPopover, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellClass</a>
     */
    DAY_CELL_CLASS("dayCellClass"),

    /**
     * @deprecated use {@link #DAY_CELL_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     * It no longer applies to time grid day columns, see {@link #DAY_LANE_CLASS}.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_CELL_CLASS_NAMES("dayCellClass"),

    /**
     * Custom content for the day number area at the top of day cells. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, dayNumberText, isToday, isPast, isFuture, isOther, isDisabled, inPopover, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopContent</a>
     */
    DAY_CELL_TOP_CONTENT("dayCellTopContent"),

    /**
     * @deprecated use {@link #DAY_CELL_TOP_CONTENT}, which sets the same FullCalendar option.
     * It no longer applies to time grid day columns, which have no content hook in FullCalendar 7.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_CELL_CONTENT("dayCellTopContent"),

    /**
     * Called after a day cell is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, dayNumberText, isToday, isPast, isFuture, isOther, isDisabled, inPopover, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellDidMount</a>
     */
    DAY_CELL_DID_MOUNT("dayCellDidMount"),

    /**
     * Called before a day cell is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, dayNumberText, isToday, isPast, isFuture, isOther, isDisabled, inPopover, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellWillUnmount</a>
     */
    DAY_CELL_WILL_UNMOUNT("dayCellWillUnmount"),

    // ---- Render hooks: Day Lane ----
    /**
     * CSS classes for time grid day columns.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, isToday, isPast, isFuture, isDisabled, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneClass</a>
     */
    DAY_LANE_CLASS("dayLaneClass"),

    /**
     * Called after a time grid day column is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, isToday, isPast, isFuture, isDisabled, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneDidMount</a>
     */
    DAY_LANE_DID_MOUNT("dayLaneDidMount"),

    /**
     * Called before a time grid day column is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, isToday, isPast, isFuture, isDisabled, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneWillUnmount</a>
     */
    DAY_LANE_WILL_UNMOUNT("dayLaneWillUnmount"),

    // ---- Render hooks: Day Header ----
    /**
     * CSS classes for day header cells and the "+N more" popover header.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, isToday, isPast, isFuture, isDisabled, inPopover, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderClass</a>
     */
    DAY_HEADER_CLASS("dayHeaderClass"),

    /**
     * @deprecated use {@link #DAY_HEADER_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    DAY_HEADER_CLASS_NAMES("dayHeaderClass"),

    /**
     * Custom content for a day header cell. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, isToday, isPast, isFuture, isDisabled, inPopover, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderContent</a>
     */
    DAY_HEADER_CONTENT("dayHeaderContent"),

    /**
     * Called after a day header cell is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, isToday, isPast, isFuture, isDisabled, inPopover, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderDidMount</a>
     */
    DAY_HEADER_DID_MOUNT("dayHeaderDidMount"),

    /**
     * Called before a day header cell is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, isToday, isPast, isFuture, isDisabled, inPopover, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderWillUnmount</a>
     */
    DAY_HEADER_WILL_UNMOUNT("dayHeaderWillUnmount"),

    // ---- Render hooks: List Day Header ----
    /**
     * CSS classes for list view day headings.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, isToday, isPast, isFuture, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderClass</a>
     */
    LIST_DAY_HEADER_CLASS("listDayHeaderClass"),

    /**
     * Custom content for the texts of a list view day heading. Called once per text, {@code level} 0
     * for {@link #LIST_DAY_FORMAT} and 1 for {@link #LIST_DAY_ALT_FORMAT}. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, text, level, isToday, isPast, isFuture, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderContent</a>
     */
    LIST_DAY_HEADER_CONTENT("listDayHeaderContent"),

    /**
     * Called after a list view day heading is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, isToday, isPast, isFuture, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderDidMount</a>
     */
    LIST_DAY_HEADER_DID_MOUNT("listDayHeaderDidMount"),

    /**
     * Called before a list view day heading is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, dow, isToday, isPast, isFuture, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderWillUnmount</a>
     */
    LIST_DAY_HEADER_WILL_UNMOUNT("listDayHeaderWillUnmount"),

    // ---- Render hooks: Slot Header ----
    /**
     * CSS classes for time slot header cells.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, time, text, isMajor, isMinor, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderClass</a>
     */
    SLOT_HEADER_CLASS("slotHeaderClass"),

    /**
     * @deprecated use {@link #SLOT_HEADER_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_CLASS_NAMES("slotHeaderClass"),

    /**
     * Custom content for a time slot header cell. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, time, text, isMajor, isMinor, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderContent</a>
     */
    SLOT_HEADER_CONTENT("slotHeaderContent"),

    /**
     * @deprecated use {@link #SLOT_HEADER_CONTENT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_CONTENT("slotHeaderContent"),

    /**
     * Called after a time slot header cell is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, time, text, isMajor, isMinor, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderDidMount</a>
     */
    SLOT_HEADER_DID_MOUNT("slotHeaderDidMount"),

    /**
     * @deprecated use {@link #SLOT_HEADER_DID_MOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_DID_MOUNT("slotHeaderDidMount"),

    /**
     * Called before a time slot header cell is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, time, text, isMajor, isMinor, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderWillUnmount</a>
     */
    SLOT_HEADER_WILL_UNMOUNT("slotHeaderWillUnmount"),

    /**
     * @deprecated use {@link #SLOT_HEADER_WILL_UNMOUNT}, which sets the same FullCalendar option
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LABEL_WILL_UNMOUNT("slotHeaderWillUnmount"),

    // ---- Render hooks: Slot Lane ----
    /**
     * CSS classes for time slot lane cells.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, time, isMajor, isMinor, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneClass</a>
     */
    SLOT_LANE_CLASS("slotLaneClass"),

    /**
     * @deprecated use {@link #SLOT_LANE_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    SLOT_LANE_CLASS_NAMES("slotLaneClass"),

    /**
     * Called after a time slot lane cell is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, time, isMajor, isMinor, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneDidMount</a>
     */
    SLOT_LANE_DID_MOUNT("slotLaneDidMount"),

    /**
     * Called before a time slot lane cell is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, time, isMajor, isMinor, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneWillUnmount</a>
     */
    SLOT_LANE_WILL_UNMOUNT("slotLaneWillUnmount"),

    // ---- Render hooks: View ----
    /**
     * CSS classes for the view root element.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewClass</a>
     */
    VIEW_CLASS("viewClass"),

    /**
     * @deprecated use {@link #VIEW_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    VIEW_CLASS_NAMES("viewClass"),

    /**
     * Called after the view root element is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewDidMount</a>
     */
    VIEW_DID_MOUNT("viewDidMount"),

    /**
     * Called before the view root element is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewWillUnmount</a>
     */
    VIEW_WILL_UNMOUNT("viewWillUnmount"),

    // ---- Render hooks: Now Indicator Header ----
    /**
     * CSS classes for the now indicator arrow in the time axis.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderClass</a>
     */
    NOW_INDICATOR_HEADER_CLASS("nowIndicatorHeaderClass"),

    /**
     * Custom content for the now indicator arrow. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderContent</a>
     */
    NOW_INDICATOR_HEADER_CONTENT("nowIndicatorHeaderContent"),

    /**
     * Called after the now indicator arrow is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderDidMount</a>
     */
    NOW_INDICATOR_HEADER_DID_MOUNT("nowIndicatorHeaderDidMount"),

    /**
     * Called before the now indicator arrow is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderWillUnmount</a>
     */
    NOW_INDICATOR_HEADER_WILL_UNMOUNT("nowIndicatorHeaderWillUnmount"),

    // ---- Render hooks: Now Indicator Line ----
    /**
     * CSS classes for the now indicator line across the day.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {date, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineClass</a>
     */
    NOW_INDICATOR_LINE_CLASS("nowIndicatorLineClass"),

    /**
     * Custom content for the now indicator line. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineContent</a>
     */
    NOW_INDICATOR_LINE_CONTENT("nowIndicatorLineContent"),

    /**
     * Called after the now indicator line is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineDidMount</a>
     */
    NOW_INDICATOR_LINE_DID_MOUNT("nowIndicatorLineDidMount"),

    /**
     * Called before the now indicator line is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineWillUnmount</a>
     */
    NOW_INDICATOR_LINE_WILL_UNMOUNT("nowIndicatorLineWillUnmount"),

    // ---- Render hooks: Inline Week Number ----
    /**
     * CSS classes for week numbers inside day grid cells.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberClass</a>
     */
    INLINE_WEEK_NUMBER_CLASS("inlineWeekNumberClass"),

    /**
     * Custom content for a day grid week number. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberContent</a>
     */
    INLINE_WEEK_NUMBER_CONTENT("inlineWeekNumberContent"),

    /**
     * Called after a day grid week number is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberDidMount</a>
     */
    INLINE_WEEK_NUMBER_DID_MOUNT("inlineWeekNumberDidMount"),

    /**
     * Called before a day grid week number is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberWillUnmount</a>
     */
    INLINE_WEEK_NUMBER_WILL_UNMOUNT("inlineWeekNumberWillUnmount"),

    // ---- Render hooks: Week Number Header ----
    /**
     * CSS classes for the week number above the time axis in time grid views.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderClass</a>
     */
    WEEK_NUMBER_HEADER_CLASS("weekNumberHeaderClass"),

    /**
     * Custom content for the time grid week number. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderContent</a>
     */
    WEEK_NUMBER_HEADER_CONTENT("weekNumberHeaderContent"),

    /**
     * Called after the time grid week number is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderDidMount</a>
     */
    WEEK_NUMBER_HEADER_DID_MOUNT("weekNumberHeaderDidMount"),

    /**
     * Called before the time grid week number is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, date, text, isNarrow, hasNavLink, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderWillUnmount</a>
     */
    WEEK_NUMBER_HEADER_WILL_UNMOUNT("weekNumberHeaderWillUnmount"),

    // ---- Render hooks: More Link ----
    /**
     * CSS classes for the "+N more" link.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {num, text, numericText, longText, isNarrow, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkClass</a>
     */
    MORE_LINK_CLASS("moreLinkClass"),

    /**
     * @deprecated use {@link #MORE_LINK_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    MORE_LINK_CLASS_NAMES("moreLinkClass"),

    /**
     * Custom content for a more link. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, text, numericText, longText, isNarrow, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkContent</a>
     */
    MORE_LINK_CONTENT("moreLinkContent"),

    /**
     * Called after a more link is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, text, numericText, longText, isNarrow, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkDidMount</a>
     */
    MORE_LINK_DID_MOUNT("moreLinkDidMount"),

    /**
     * Called before a more link is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {num, text, numericText, longText, isNarrow, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkWillUnmount</a>
     */
    MORE_LINK_WILL_UNMOUNT("moreLinkWillUnmount"),

    // ---- Render hooks: No Entries ----
    /**
     * CSS classes for the "No events" message in list view.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {text, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsClass</a>
     */
    NO_ENTRIES_CLASS("noEventsClass"),

    /**
     * @deprecated use {@link #NO_ENTRIES_CLASS}, which sets the same FullCalendar option. Return a class name string. FullCalendar 7 drops arrays.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    NO_ENTRIES_CLASS_NAMES("noEventsClass"),

    /**
     * Custom content for the no-entries message. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {text, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsContent</a>
     */
    NO_ENTRIES_CONTENT("noEventsContent"),

    /**
     * Called after the no-entries message is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {text, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsDidMount</a>
     */
    NO_ENTRIES_DID_MOUNT("noEventsDidMount"),

    /**
     * Called before the no-entries message is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {text, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsWillUnmount</a>
     */
    NO_ENTRIES_WILL_UNMOUNT("noEventsWillUnmount"),

    // ---- Render hooks: All-Day Header ----
    /**
     * CSS classes for the all-day section header cell in time grid views.
     * <dl>
     *   <dt>Type</dt> <dd>class name {@code string} (space separated) | {@link JsCallback} returning one</dd>
     *   <dt>Arguments</dt> <dd>{@code {text, isNarrow, view}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderClass</a>
     */
    ALL_DAY_HEADER_CLASS("allDayHeaderClass"),

    /**
     * Custom content for the all-day header cell. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {text, isNarrow, view}}</dd>
     *   <dt>Returns</dt> <dd>content object or HTML string</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderContent</a>
     */
    ALL_DAY_HEADER_CONTENT("allDayHeaderContent"),

    /**
     * Called after the all-day header cell is added to the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {text, isNarrow, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderDidMount</a>
     */
    ALL_DAY_HEADER_DID_MOUNT("allDayHeaderDidMount"),

    /**
     * Called before the all-day header cell is removed from the DOM. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {text, isNarrow, view, el}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderWillUnmount</a>
     */
    ALL_DAY_HEADER_WILL_UNMOUNT("allDayHeaderWillUnmount"),

    // ---- Data transform / loading callbacks ----
    /**
     * Called when async entry fetching starts or stops. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {isLoading}} (boolean)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/loading">loading</a>
     */
    LOADING("loading"),

    /**
     * Transform raw entry data before FullCalendar parses it. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {eventData}}</dd>
     *   <dt>Returns</dt>   <dd>transformed event data object</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventDataTransform">eventDataTransform</a>
     */
    ENTRY_DATA_TRANSFORM("eventDataTransform"),

    /**
     * Called after an entry source fetches successfully. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {rawEvents, response}}</dd>
     *   <dt>Returns</dt>   <dd>array of event objects (or undefined to keep original)</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/eventSourceSuccess">eventSourceSuccess</a>
     */
    ENTRY_SOURCE_SUCCESS("eventSourceSuccess"),

    // ---- Navigation callbacks ----
    /**
     * Custom handler for clickable day nav links. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {date, jsEvent}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkDayClick">navLinkDayClick</a>
     */
    NAV_LINK_DAY_CLICK("navLinkDayClick"),

    /**
     * Custom handler for clickable week nav links. Accepts a {@link JsCallback}.
     * <dl>
     *   <dt>Arguments</dt> <dd>{@code {weekStart, jsEvent}}</dd>
     * </dl>
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkWeekClick">navLinkWeekClick</a>
     */
    NAV_LINK_WEEK_CLICK("navLinkWeekClick"),

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

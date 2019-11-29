/*
 * Copyright 2018, Stefan Uebe
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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@NpmPackage(value = "@fullcalendar/core", version = "^4.3.1")
@NpmPackage(value = "@fullcalendar/interaction", version = "^4.3.0")
@NpmPackage(value = "@fullcalendar/daygrid", version = "^4.3.0")
@NpmPackage(value = "@fullcalendar/timegrid", version = "^4.3.0")
@NpmPackage(value = "@fullcalendar/list", version = "^4.3.0")
@NpmPackage(value = "moment", version = "^2.24.0")
@NpmPackage(value = "moment-timezone", version = "^0.5.26")
@NpmPackage(value = "@fullcalendar/moment", version = "^4.3.0")
@NpmPackage(value = "@fullcalendar/moment-timezone", version = "^4.3.0")
@Tag("full-calendar")
@JsModule("./full-calendar.js")
public class FullCalendar extends Component implements HasStyle, HasSize {

    /**
     * This is the default duration of an timeslot event in hours. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_TIMED_EVENT_DURATION = 1;

    /**
     * This is the default duration of an daily event in days. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_DAY_EVENT_DURATION = 1;

    private Map<String, Entry> entries = new HashMap<>();
    private Map<String, Serializable> options = new HashMap<>();
    private Map<String, Object> serverSideOptions = new HashMap<>();

    // used to keep the amount of timeslot selected listeners. when 0, then selectable option is auto removed
    private int timeslotsSelectedListenerCount;

    private Timezone browserTimezone;

    /**
     * Creates a new FullCalendar.
     */
    public FullCalendar() {
        this(-1);
    }

    /**
     * Creates a new FullCalendar.
     * <br><br>
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views. This value has to be set here and cannot be modified afterwards due to
     * technical reasons of FC. If set afterwards the entry limit would overwrite settings
     * and would show the limit also for basic views where it makes no sense (might change in future).
     * Passing a negative number or 0 disabled the entry limit (same as passing no number at all).
     *
     * @param entryLimit max entries to shown per day
     */
    public FullCalendar(int entryLimit) {
        setLocale(CalendarLocale.getDefault());
        if (entryLimit > 0) {
            getElement().setProperty("eventLimit", entryLimit);
        } else {
            getElement().setProperty("eventLimit", false);
        }
    }

    public void allowDatesRenderEventOnOptionChange(boolean allow) {
        getElement().setProperty("noDatesRenderEventOnOptionSetting", !allow);
    }

    /**
     * Moves to the next interval (e. g. next month if current view is monthly based).
     */
    public void next() {
        getElement().callJsFunction("next");
    }

    /**
     * Moves to the previous interval (e. g. previous month if current view is monthly based).
     */
    public void previous() {
        getElement().callJsFunction("previous");
    }

    /**
     * Moves to the current interval (e. g. current month if current view is monthly based).
     */
    public void today() {
        getElement().callJsFunction("today");
    }

    /**
     * Returns the entry with the given id. Is empty when the id is not registered.
     *
     * @param id id
     * @return entry or empty
     * @throws NullPointerException when null is passed
     */
    public Optional<Entry> getEntryById(@NotNull String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(entries.get(id));
    }

    /**
     * Returns all entries registered in this instance. Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     * <br><br>
     * Changes in the list are not reflected to the calendar's list instance. Also please note, that the content
     * of the list is <b>unsorted</b> and may vary with each call. The return of a list is due to presenting
     * a convenient way of using the returned values without the need to encapsulate them yourselves.
     *
     * @return entries entries
     */
    public List<Entry> getEntries() {
        return new ArrayList<>(entries.values());
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given time span. You may
     * pass null for the parameters to have the timespan search only on one side. Passing null for both
     * parameters return all entries.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     * <br><br>
     * Please be aware that the filter and entry times are exclusive due to the nature of the FC entries
     * to range from e.g. 07:00-08:00 or "day 1, 0:00" to "day 2, 0:00" where the end is a marker but somehow
     * exclusive to the date.
     * That means, that search for 06:00-07:00 or 08:00-09:00 will NOT include the given time example.
     * Searching for anything between these two timespans (like 06:00-07:01, 07:30-10:00, 07:59-09:00, etc.) will
     * include it.
     *
     * @param filterStart start point of filter timespan or null to have no limit
     * @param filterEnd   end point of filter timespan or null to have no limit
     * @return entries
     */
    public List<Entry> getEntries(Instant filterStart, Instant filterEnd) {
        if (filterStart == null && filterEnd == null) {
            return getEntries();
        }

        Stream<Entry> stream = getEntries().stream();
        if (filterStart != null) {
            stream = stream.filter(e -> e.getEndUTC().isAfter(filterStart));
        }

        if (filterEnd != null) {
            stream = stream.filter(e -> e.getStartUTC().isBefore(filterEnd));
        }

        return stream.collect(Collectors.toList());
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given time span. You may
     * pass null for the parameters to have the timespan search only on one side. Passing null for both
     * parameters return all entries. The times are converted
     * to UTC before searching. The conversion is done with the calendars timezone.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     * <br><br>
     * Please be aware that the filter and entry times are exclusive due to the nature of the FC entries
     * to range from e.g. 07:00-08:00 or "day 1, 0:00" to "day 2, 0:00" where the end is a marker but somehow
     * exclusive to the date.
     * That means, that search for 06:00-07:00 or 08:00-09:00 will NOT include the given time example.
     * Searching for anything between these two timespans (like 06:00-07:01, 07:30-10:00, 07:59-09:00, etc.) will
     * include it.
     *
     * @param filterStart start point of filter timespan or null to have no limit
     * @param filterEnd   end point of filter timespan or null to have no limit
     * @return entries
     */
    public List<Entry> getEntries(LocalDateTime filterStart, LocalDateTime filterEnd) {
        Timezone timezone = getTimezone();
        return getEntries(timezone.convertToUTC(filterStart), timezone.convertToUTC(filterEnd));
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     *
     * @param date end point of filter timespan
     * @return entries
     * @throws NullPointerException when null is passed
     */
    public List<Entry> getEntries(@NotNull Instant date) {
        Objects.requireNonNull(date);
        return getEntries(date, date.plus(1, ChronoUnit.DAYS));
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date. The date is converted
     * to UTC before searching. The conversion is done with the calendars timezone.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     *
     * @param date end point of filter timespan
     * @return entries
     * @throws NullPointerException when null is passed
     */
    public List<Entry> getEntries(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        return getEntries(getTimezone().convertToUTC(date));
    }

    /**
     * Adds an entry to this calendar. Noop if the entry id is already registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    public void addEntry(@NotNull Entry entry) {
        Objects.requireNonNull(entry);
        addEntries(Collections.singletonList(entry));
    }

    /**
     * Adds an array of entries to the calendar. Noop for the entry id is already registered.
     *
     * @param arrayOfEntries array of entries
     * @throws NullPointerException when null is passed
     */
    public void addEntries(@NotNull Entry... arrayOfEntries) {
        addEntries(Arrays.asList(arrayOfEntries));
    }

    /**
     * Adds a list of entries to the calendar. Noop for the entry id is already registered.
     *
     * @param iterableEntries list of entries
     * @throws NullPointerException when null is passed
     */
    public void addEntries(@NotNull Iterable<Entry> iterableEntries) {
        Objects.requireNonNull(iterableEntries);

        JsonArray array = Json.createArray();
        iterableEntries.forEach(entry -> {
            String id = entry.getId();

            if (!entries.containsKey(id)) {
                entry.setCalendar(this);
                entries.put(id, entry);
                array.set(array.length(), entry.toJson());
            }
        });

        if (array.length() > 0) {
            getElement().callJsFunction("addEvents", array);
        }
    }


    /**
     * Updates the given entry on the client side. Will check if the id is already registered, otherwise a noop.
     *
     * @param entry entry to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntry(@NotNull Entry entry) {
        Objects.requireNonNull(entry);
        updateEntries(Collections.singletonList(entry));
    }

    /**
     * Updates the given entries on the client side. Ignores non-registered entries.
     *
     * @param arrayOfEntries entries to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntries(@NotNull Entry... arrayOfEntries) {
        updateEntries(Arrays.asList(arrayOfEntries));
    }


    /**
     * Updates the given entries on the client side. Ignores non-registered entries.
     *
     * @param iterableEntries entries to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntries(@NotNull Iterable<Entry> iterableEntries) {
        Objects.requireNonNull(entries);

        JsonArray array = Json.createArray();
        iterableEntries.forEach(entry -> {
            String id = entry.getId();
            if (entries.containsKey(id)) {
                array.set(array.length(), entry.toJson());
            }
        });

        if (array.length() > 0) {
            getElement().callJsFunction("updateEvents", array);
        }
    }

    /**
     * Removes the given entry. Noop if the id is not registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    public void removeEntry(@NotNull Entry entry) {
        Objects.requireNonNull(entry);
        removeEntries(Collections.singletonList(entry));
    }

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param arrayOfEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    public void removeEntries(@NotNull Entry... arrayOfEntries) {
        removeEntries(Arrays.asList(arrayOfEntries));
    }

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param iterableEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    public void removeEntries(@NotNull Iterable<Entry> iterableEntries) {
        Objects.requireNonNull(entries);

        JsonArray array = Json.createArray();
        iterableEntries.forEach(entry -> {
            String id = entry.getId();

            if (entries.containsKey(id)) {
                entry.setCalendar(null);
                entries.remove(id);
                array.set(array.length(), entry.toJson());
            }
        });

        if (array.length() > 0) {
            getElement().callJsFunction("removeEvents", array);
        }
    }

    /**
     * Remove all entries.
     */
    public void removeAllEntries() {
        entries.values().forEach(e -> e.setCalendar(null));
        entries.clear();
        getElement().callJsFunction("removeAllEvents");
    }

    /**
     * Change the view of the calendar (e. g. from monthly to weekly)
     *
     * @param view view to set
     * @throws NullPointerException when null is passed
     */
    public void changeView(@NotNull CalendarView view) {
        Objects.requireNonNull(view);
        getElement().callJsFunction("changeView", view.getClientSideValue());
    }

    /**
     * Switch to the intervall containing the given date (e. g. to month "October" if the "15th October ..." is passed).
     *
     * @param date date to goto
     * @throws NullPointerException when null is passed
     */
    public void gotoDate(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        getElement().callJsFunction("gotoDate", date.toString());
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull Option option, Serializable value) {
        setOption(option, value, null);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(Option)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Example:
     * <pre>
     * // sends a client parseable version to client and stores original in server side
     * calendar.setOption(Option.LOCALE, locale.toLanguageTag().toLowerCase(), locale);
     *
     * // returns the original locale (as optional)
     * Optional&lt;Locale&gt; optionalLocale = calendar.getOption(Option.LOCALE)
     * </pre>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull Option option, Serializable value, Object valueForServerSide) {
        setOption(option.getOptionKey(), value, valueForServerSide);
    }

    /**
     * Sets a custom option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, JsonValue value) {
        setOption(option, (JsonValue) value, null);
    }

    /**
     * Sets a custom option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(Option)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <p>
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, JsonValue value, Object valueForServerSide) {
        setOption(option, (Serializable) value, valueForServerSide);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, Serializable value) {
        setOption(option, value, null);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(Option)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <p>
     * <br><br>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull String option, Serializable value, Object valueForServerSide) {
        Objects.requireNonNull(option);

        if (value == null) {
            options.remove(option);
            serverSideOptions.remove(option);
        } else {
            options.put(option, value);

            if (valueForServerSide == null || valueForServerSide.equals(value)) {
                serverSideOptions.remove(option);
            } else {
                serverSideOptions.put(option, valueForServerSide);
            }
        }
        getElement().callJsFunction("setOption", option, value);
    }

    /**
     * Sets the first day of a week to be shown by the calendar. Per default sunday.
     * <br><br>
     * <b>Note:</b> FC works internally with 0 for sunday. This method converts SUNDAY to
     * this number before passing it to the client.
     *
     * @param firstDay first day to be shown
     * @throws NullPointerException when null is passed
     */
    public void setFirstDay(@NotNull DayOfWeek firstDay) {
        Objects.requireNonNull(firstDay);
        int value = firstDay == DayOfWeek.SUNDAY ? 0 : firstDay.getValue();
        setOption(Option.FIRST_DAY, value, firstDay);
    }

    /**
     * Sets the calendar's height to a fixed amount of pixels.
     *
     * @param heightInPixels height in pixels (e.g. 300)
     */
    public void setHeight(int heightInPixels) {
        setOption(Option.HEIGHT, heightInPixels);
    }

    /**
     * Sets the calendar's height to be calculated from parents height. Please be aware, that a block parent with
     * relative height (e. g. 100%) might not work properly. In this case use flex layout or set a fixed height for
     * the parent or the calendar.
     */
    public void setHeightByParent() {
        setOption(Option.HEIGHT, "parent");
    }

    /**
     * Sets the calendar's height to be calculated automatically. In current implementation this means by the calendars
     * width-height-ratio.
     */
    public void setHeightAuto() {
        setOption(Option.HEIGHT, "auto");
    }

    /**
     * Set if timeslots might be selected by the user. Please see also documentation of {@link #addTimeslotsSelectedListener(ComponentEventListener)}.
     *
     * @param selectable activate selectable
     */
    public void setTimeslotsSelectable(boolean selectable) {
        setOption(Option.SELECTABLE, selectable);
    }

    /**
     * Should the calendar show week numbers (when available for the current view)?
     *
     * @param weekNumbersVisible week numbers visible
     */
    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        setOption(Option.WEEK_NUMBERS, weekNumbersVisible);
    }

    /**
     * Returns the current set locale.
     *
     * @return locale
     */
    public Locale getLocale() {
        Optional<Object> option = getOption(Option.LOCALE);

        if (!option.isPresent()) {
            return CalendarLocale.getDefault();
        }

        Object value = option.get();
        return value instanceof Locale ? (Locale) value : Locale.forLanguageTag((String) value);
    }

    /**
     * Sets the locale to be used. If invoked for the first time it will load additional language scripts.
     *
     * @param locale locale
     * @throws NullPointerException when null is passed
     */
    public void setLocale(@NotNull Locale locale) {
        Objects.requireNonNull(locale);
        setOption(Option.LOCALE, locale.toLanguageTag().toLowerCase(), locale);
    }

    /**
     * If true is passed then the calendar will show a indicator for the current time, depending on the view.
     *
     * @param shown show indicator for now
     */
    public void setNowIndicatorShown(boolean shown) {
        setOption(Option.NOW_INDICATOR, shown);
    }

    /**
     * When true is passed the day / week numbers (or texts) will become clickable by the user and fire an event
     * for the clicked day / week.
     *
     * @param clickable clickable
     */
    public void setNumberClickable(boolean clickable) {
        setOption(Option.NAV_LINKS, clickable);
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the eventRender callback. It must be a valid JavaScript function.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is NO content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * Example
     * <pre>
     * calendar.setEntryRenderCallback("" +
     * "function(event, element) {" +
     * "   console.log(event.title + 'X');" +
     * "   element.css('color', 'red');" +
     * "   return element; " +
     * "}");
     *
     * </pre>
     *
     * @param s js function to be attached to eventRender callback
     */
    public void setEntryRenderCallback(String s) {
        getElement().callJsFunction("setEventRenderCallback", s);
    }

    /**
     * Sets the business hours for this calendar instance. You may pass multiple instances for different configurations.
     * Please be aware, that instances with crossing days or times are handled by the client side and may lead
     * to unexpected results.
     *
     * @param hours hours to set
     * @throws NullPointerException when null is passed
     */
    public void setBusinessHours(@NotNull BusinessHours... hours) {
        Objects.requireNonNull(hours);

        setOption(Option.BUSINESS_HOURS, JsonUtils.toJsonValue(Arrays.stream(hours).map(BusinessHours::toJson)), hours);
    }

    /**
     * Removes the business hours for this calendar instance.
     *
     * @throws NullPointerException when null is passed
     */
    public void removeBusinessHours() {
        setOption(Option.BUSINESS_HOURS, null);
    }
    
    /**
     * Sets the snap duration for this calendar instance.<p>
     * The default is '00:30'
     *
     * @param duration duration to set in format hh:mm
     * @throws NullPointerException when null is passed
     */
    public void setSnapDuration(@NotNull String duration) {
        Objects.requireNonNull(duration);
        setOption(Option.SNAP_DURATION, duration);
    }
    
    /**
     * Sets the min time for this calendar instance. This is the first time slot that will be displayed for each day.<p>
     * The default is '00:00:00'
     *
     * @param minTime minTime to set
     * @throws NullPointerException when null is passed
     */
    public void setMinTime(@NotNull LocalTime minTime) {
        Objects.requireNonNull(minTime);
        setOption(Option.MIN_TIME, JsonUtils.toJsonValue(minTime != null ? minTime : "00:00:00"));
    }
    
    /**
     * Sets the max time for this calendar instance. This is the last time slot that will be displayed for each day<p>
     * The default is '24:00:00'
     *
     * @param maxTime maxTime to set
     * @throws NullPointerException when null is passed
     */
    public void setMaxTime(@NotNull LocalTime maxTime) {
        Objects.requireNonNull(maxTime);  
        setOption(Option.MAX_TIME, JsonUtils.toJsonValue(maxTime != null ? maxTime : "24:00:00"));
    }

    /**
     * Returns the timezone set for this browser. By default UTC. If obtainable, you can read the timezone from
     * the browser.
     *
     * @return time zone
     */
    public Timezone getTimezone() {
        return (Timezone) getOption("timeZone").orElse(Timezone.UTC);
    }

    public void setTimezone(Timezone timezone) {
        Objects.requireNonNull(timezone);

        Timezone oldTimezone = getTimezone();
        if (!timezone.equals(oldTimezone)) {
            setOption("timeZone", timezone.getClientSideValue(), timezone);
            updateEntries(getEntries());
        }
    }

    /**
     * This method returns the timezone sent by the browser. It is <b>not</b> automatically set as the FC's timezone.
     * Is empty if there was no timezone obtainable or the instance has not been attached to the client side, yet.
     *
     * @return optional client side timezone
     */
    public Optional<Timezone> getBrowserTimezone() {
        return Optional.ofNullable(browserTimezone);
    }

    @ClientCallable
    protected void setBrowserTimezone(String timezoneId) {
        if (timezoneId != null) {
            this.browserTimezone = new Timezone(ZoneId.of(timezoneId));
            getEventBus().fireEvent(new BrowserTimezoneObtainedEvent(this, false, browserTimezone));
        }
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull Option option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull Option option, boolean forceClientSideValue) {
        return getOption(option.getOptionKey(), forceClientSideValue);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull String option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(Option, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull String option, boolean forceClientSideValue) {
        Objects.requireNonNull(option);
        return Optional.ofNullable((T) (!forceClientSideValue && serverSideOptions.containsKey(option)
                ? serverSideOptions.get(option) : options.get(option)));
    }


    /**
     * Force the client side instance to re-render it's content.
     */
    public void render() {
        getElement().callJsFunction("render");
    }

    /**
     * Registers a listener to be informed when a timeslot click event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotClickedListener(@NotNull ComponentEventListener<? extends TimeslotClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedEvent.class, (ComponentEventListener<TimeslotClickedEvent>) listener);
    }

    /**
     * Registers a listener to be informed when an entry click event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryClickedListener(@NotNull ComponentEventListener<EntryClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry resized event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryResizedListener(@NotNull ComponentEventListener<EntryResizedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry dropped event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryDroppedListener(@NotNull ComponentEventListener<EntryDroppedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDroppedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when a dates rendered event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addDatesRenderedListener(@NotNull ComponentEventListener<DatesRenderedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DatesRenderedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when the user selected a range of timeslots.
     * <br><br>
     * You should deactivate timeslot clicked listeners since both events will get fired when the user only selects
     * one timeslot / day.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotsSelectedListener(@NotNull ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedEvent.class, (ComponentEventListener<TimeslotsSelectedEvent>) listener);
    }

    /**
     * Registers a listener to be informed when the user clicked on the limited entries link (e.g. "+6 more").
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addLimitedEntriesClickedListener(@NotNull ComponentEventListener<LimitedEntriesClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(LimitedEntriesClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed, when a user clicks a day's number.
     * <br><br>
     * {@link #setNumberClickable(boolean)} needs to be called with true before.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addDayNumberClickedListener(@NotNull ComponentEventListener<DayNumberClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DayNumberClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed, when a user clicks a week's number.
     * <br><br>
     * {@link #setNumberClickable(boolean)} needs to be called with true before.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addWeekNumberClickedListener(@NotNull ComponentEventListener<WeekNumberClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(WeekNumberClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed, when the browser's timezone has been obtained by the server.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addBrowserTimezoneObtainedListener(@NotNull ComponentEventListener<BrowserTimezoneObtainedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(BrowserTimezoneObtainedEvent.class, listener);
    }

    enum Option {
        FIRST_DAY("firstDay"),
        HEIGHT("height"),
        LOCALE("locale"),
        SELECTABLE("selectable"),
        WEEK_NUMBERS("weekNumbers"),
        NOW_INDICATOR("nowIndicator"),
        NAV_LINKS("navLinks"),
        BUSINESS_HOURS("businessHours"),
        TIMEZONE("timeZone"),
        SNAP_DURATION("snapDuration"),
        MIN_TIME("minTime"),
        MAX_TIME("maxTime")
        ;

        private final String optionKey;

        Option(String optionKey) {
            this.optionKey = optionKey;
        }

        String getOptionKey() {
            return optionKey;
        }
    }


}

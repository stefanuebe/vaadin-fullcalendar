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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.vaadin.stefan.fullcalendar.CustomCalendarView.AnonymousCustomCalendarView;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.model.Footer;
import org.vaadin.stefan.fullcalendar.model.Header;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@NpmPackage(value = "@fullcalendar/core", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/interaction", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/daygrid", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/timegrid", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/list", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/multimonth", version = FullCalendar.FC_CLIENT_VERSION)
// TODO still necessary?
@NpmPackage(value = "moment", version = "2.30.1")
@NpmPackage(value = "moment-timezone", version = "0.6.0")
@NpmPackage(value = "@fullcalendar/moment", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/moment-timezone", version = FullCalendar.FC_CLIENT_VERSION)

@JsModule("./vaadin-full-calendar/full-calendar.ts")
@CssImport("./vaadin-full-calendar/full-calendar-styles.css")
@Tag("vaadin-full-calendar")
public class FullCalendar extends Component implements HasStyle, HasSize, HasTheme {

    /**
     * The library base version used in this addon. Some additional libraries might have a different version number due to
     * a different release cycle or known issues.
     */
    public static final String FC_CLIENT_VERSION = "6.1.19";

    /**
     * This is the default duration of an timeslot event in hours. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_TIMED_EVENT_DURATION = 1;

    /**
     * This is the default duration of an daily event in days. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_DAY_EVENT_DURATION = 1;

    private static final String JSON_INITIAL_OPTIONS = "initialJsonOptions";
    private static final String INITIAL_OPTIONS = "initialOptions";

    /**
     * Caches the last fetched entries for entry based events.
     */
    private final Map<String, Entry> lastFetchedEntries = new HashMap<>();
    private final Map<String, Object> options = new HashMap<>();

    /**
     * A map for options, that have been set before the attachment. They are mapped here instead of the options
     * map to allow a correct init of the client on reattachment plus have something to return in getOption.
     * The main reason are options, that have to be set before attachment, but must not "bleed" into the option
     * map, like eventContent
     */
    private final Map<String, Object> initialOptions = new HashMap<>();
    private final Map<String, Object> serverSideOptions = new HashMap<>();

    private EntryProvider<? extends Entry> entryProvider;
    private final List<Registration> entryProviderDataListeners = new LinkedList<>();

    private final Map<String, CustomCalendarView> customCalendarViews = new LinkedHashMap<>();

    // used to keep the amount of timeslot selected listeners. when 0, then selectable option is auto removed
    private int timeslotsSelectedListenerCount;

    private Timezone browserTimezone;

    private String currentViewName;
    private LocalDate currentIntervalStart;
    private CalendarView currentView;

    private boolean refreshAllEntriesRequested;

    private Map<String, String> customNativeEventsMap = new LinkedHashMap<>();
    private String eventDidMountCallback;

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefaultLocale()}).
     * <p></p>
     * Uses {@link InMemoryEntryProvider} by default.
     */
    public FullCalendar() {
        setMaxEntriesPerDayUnlimited();
        postConstruct();
    }

    /**
     * Creates a new instance.
     * <br><br>
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views. This value has to be set here and cannot be modified afterwards due to
     * technical reasons of FC. If set afterwards the entry limit would overwrite settings
     * and would show the limit also for basic views where it makes no sense (might change in future).
     * Passing a negative number disabled the entry limit (same as passing no number at all).
     * <br><br>
     * Sets the locale to {@link CalendarLocale#getDefaultLocale()} ()}
     * <p></p>
     * Uses {@link InMemoryEntryProvider} by default.
     *
     * @param entryLimit The max number of stacked event levels within a given day. This excludes the +more link if present. The rest will show up in a popover.
     * @deprecated use the {@link FullCalendarBuilder#withEntryLimit(int)} instead.
     */
    public FullCalendar(int entryLimit) {
        if (entryLimit >= 0) {
            setMaxEntriesPerDay(entryLimit);
        } else {
            setMaxEntriesPerDayUnlimited();
        }

        setLocale(CalendarLocale.getDefaultLocale());

        postConstruct();
    }

    /**
     * Creates a new instance with custom initial options. This allows a full override of the default
     * initial options, that the calendar would normally receive. Theoretically you can set all options,
     * as long as they are not based on a client side variable (as for instance "plugins" or "locales").
     * Complex objects are possible, too, for instance for view-specific settings.
     * Please refer to the official FC documentation regarding potential options.
     * <br><br>
     * Client side event handlers, that are technically also a part of the options are still applied to
     * the options object. However you may set your own event handlers with the correct name. In that case
     * they will be taken into account instead of the default ones.
     * <br><br>
     * Plugins (key "plugins") will always be set on the client side (and thus override any key passed with this
     * object), since they are needed for a functional calendar. This may change in future. Same for locales
     * (key "locales").
     * <br><br>
     * Please be aware, that incorrect options or event handler overriding can lead to unpredictable errors,
     * which will NOT be supported in any case.
     * <br><br>
     * Also, options set this way are not cached in the server side state. Calling any of the
     * {@code getOption(...)} methods will result in {@code null} (or the respective native default).
     * <p></p>
     * Any "set some option" calls will override the given initial options, when they use the same key.
     * <p></p>
     * Uses {@link InMemoryEntryProvider} by default.
     *
     * @param initialOptions initial options
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     */
    public FullCalendar(ObjectNode initialOptions) {
        if (initialOptions.hasNonNull("views")) {
            ObjectNode views = (ObjectNode) initialOptions.get("views");

            // register custom views mentioned in the initial options
            for (String viewName : views.propertyNames()) {
                // only register anonmyous views, if there is no real registered variant
                ObjectNode viewSettings = (ObjectNode) views.get(viewName);
                AnonymousCustomCalendarView anonymousView = new AnonymousCustomCalendarView(viewName, viewSettings);
                this.customCalendarViews.put(anonymousView.getClientSideValue(), anonymousView);
            }
        }

        this.getElement().setPropertyJson(JSON_INITIAL_OPTIONS, Objects.requireNonNull(initialOptions));

        if (!initialOptions.hasNonNull(Option.LOCALE.getOptionKey())) {
            // fallback to prevent strange locale effects on the client side
            setLocale(CalendarLocale.getDefaultLocale());
        }


        postConstruct();
    }

    /**
     * Called after the constructor has been initialized.
     */
    private void postConstruct() {
        setEntryProvider(EntryProvider.emptyInMemory());

        setPrefetchEnabled(true);

        // just to prevent, that those are null
        currentView = CalendarViewImpl.DAY_GRID_MONTH;
        currentViewName = currentView.getName();

        addDatesRenderedListener(event -> {
            currentIntervalStart = event.getIntervalStart();
        });

        addViewSkeletonRenderedListener(event -> {
            currentViewName = event.getViewName();
            currentView = event.getCalendarView().orElse(null);
        });

        // currently disabled, since a ResizeObserver is registered on the client side
        setOption("handleWindowResize", false);

        setHeightFull(); // default from previous versions

        /* to allow class based styling for custom subclasses (e.g. for applying the lumo theme)*/
        addClassName("vaadin-full-calendar");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if(!attachEvent.isInitialAttach()) {
            getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(this, executionContext -> {
                    // We do not need to set the initial options again as that is handled by Flow automatically.
                    // All other options, set by setOption have to be reset, as they are transported to the client
                    // via function at the moment and thus not stored in the server side state.

                    // options. dont use the initialOptions map here, the component takes care of initialOptions itself
                    // since that is also cached as a property
                    ObjectNode optionsJson = JsonFactory.createObject();
                    if (!options.isEmpty()) {
                        options.forEach((key, value) -> optionsJson.set(key, JsonUtils.toJsonNode(value)));
                    }

                    getElement().callJsFunction("restoreStateFromServer",
                            optionsJson,
                            JsonUtils.toJsonNode(currentViewName),
                            JsonUtils.toJsonNode(currentIntervalStart));

                });
            });
        }

        updateEntryDidMountCallbackOnAttach();
    }

    /**
     * Sets a property to allow or disallow (re-)rendering of dates, when an option changes. When allowed,
     * each option will fire a dates rendering event, which can lead to multiple rendering events, even if only
     * one is needed.
     *
     * @param allow allow
     */
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
     * Sets the entry provider for this instance. The previous entry provider will be removed and the
     * client side will be updated.
     * <p></p>
     * By default a new full calendar is initialized with an {@link InMemoryEntryProvider}.
     *
     * @param entryProvider entry provider
     */
    public void setEntryProvider(EntryProvider<? extends Entry> entryProvider) {
        Objects.requireNonNull(entryProvider);

        if (this.entryProvider != entryProvider) {
            if (this.entryProvider != null) {
                this.entryProvider.setCalendar(null);
            }

            this.entryProvider = entryProvider;
            entryProvider.setCalendar(this);

            entryProviderDataListeners.forEach(Registration::remove);
            entryProviderDataListeners.clear();

            entryProviderDataListeners.add(entryProvider.addEntryRefreshListener(event -> requestRefresh(event.getItemToRefresh())));
            entryProviderDataListeners.add(entryProvider.addEntriesChangeListener(event -> requestRefreshAllEntries()));
        }
    }

    /**
     * Returns the entry provider of this calendar. Never null.
     * @param <T> entry provider class or subclass
     * @return entry provider
     */
    @SuppressWarnings("unchecked")
    public <R extends Entry, T extends EntryProvider<R>> T getEntryProvider() {
        return (T) entryProvider;
    }

    /**
     * This method requests an entry refresh from the client side. Every call of this method will register
     * a client side call, since it might be called for different items. Calls are handled in the order
     * they are requested. This method will not interfere or "communicate" with {@link #requestRefreshAllEntries()}.
     *
     * @param item item to refresh
     */
    protected void requestRefresh(Entry item) {
        getElement().getNode().runWhenAttached(ui -> {
            ui.beforeClientResponse(this, pExecutionContext -> {
                getEntryProvider()
                        .fetchById(item.getId())
                        .ifPresent(refreshedEntry -> {
                            lastFetchedEntries.put(refreshedEntry.getId(), refreshedEntry);
                            getElement().callJsFunction("refreshSingleEvent", refreshedEntry.getId());
                        });


                // refreshAllRequested = false; // why was this here?
            });
        });
    }

    /**
     * This method is intended to be triggered by the entry provider "refreshAll" methods.
     * Informs the client side, that a "refresh all" has been requested. Subsequent calls to this method during the
     * same request cycle will still just result in one fetch from the client side (in fact, only one call to the
     * client will be executed). This behavior might change in future, if it appears to be problematic regarding
     * other client side calls.
     * <p></p>
     * When parallel to this call {@link #requestRefresh(Entry)} is called, the calls will be handled in the order
     * they are called, whereas only the first call of this method is relevant.
     */
    protected void requestRefreshAllEntries() {
        if (!refreshAllEntriesRequested) {
            refreshAllEntriesRequested = true;
            getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(this, pExecutionContext -> {
                    getElement().callJsFunction("refreshAllEvents");
                    refreshAllEntriesRequested = false;
                });
            });
        }
    }

    /**
     * Indicates, if the entry provider is a in memory provider.
     *
     * @return is eager loading
     */
    public boolean isInMemoryEntryProvider() {
        return entryProvider instanceof InMemoryEntryProvider;
    }

    @ClientCallable
    protected ArrayNode fetchEntriesFromServer(ObjectNode query) {
        Objects.requireNonNull(query);
        Objects.requireNonNull(entryProvider);

        lastFetchedEntries.clear();

        LocalDateTime start = query.hasNonNull("start") ? JsonUtils.parseClientSideDateTime(query.get("start").asString()) : null;
        LocalDateTime end = query.hasNonNull("end") ? JsonUtils.parseClientSideDateTime(query.get("end").asString()) : null;

        ArrayNode array = JsonFactory.createArray();
        entryProvider.fetch(new EntryQuery(start, end, EntryQuery.AllDay.BOTH))
                .peek(entry -> {
                    entry.setCalendar(this);
                    entry.setKnownToTheClient(true); // mark entry as "has been sent to client"
                    lastFetchedEntries.put(entry.getId(), entry);
                })
                .map(Entry::toJson)
                .forEach(array::add);

        return array;
    }

    /**
     * Returns an entry with the given id from the last fetched set of entries. Returns an empty instance,
     * when there was no fetch yet or the id is unknown.
     * <p></p>
     * Uses {@link InMemoryEntryProvider#getEntryById(String)} when the eager in memory provider is used.
     * <p></p>
     * This method is an internal method, intended to be used by entry based events only. Do not use it for
     * any other purpose as the implementation or scope may change in future.
     *
     * @param id id
     * @return cached entry from last fetch or empty
     */
    public Optional<Entry> getCachedEntryFromFetch(String id) {
        return Optional.ofNullable(lastFetchedEntries.get(id));
    }

    protected InMemoryEntryProvider<Entry> assureInMemoryProvider() {
        if (!(entryProvider instanceof InMemoryEntryProvider)) {
            throw new UnsupportedOperationException("Needs an InMemoryEntryProvider to work.");
        }

        return (InMemoryEntryProvider<Entry>) entryProvider;
    }

    /**
     * Change the view of the calendar (e. g. from monthly to weekly)
     *
     * @param view view to set
     * @throws NullPointerException when null is passed
     */
    public void changeView(CalendarView view) {
        Objects.requireNonNull(view);

        lookupViewByClientSideValue(view.getClientSideValue()).orElseThrow(() -> new IllegalArgumentException("Unknown view: " + view.getClientSideValue() + ". If you want to use a custom view, please register it first by using addCustomView()."));

        currentView = view;
        currentViewName = view.getClientSideValue();

        getElement().callJsFunction("changeView", currentViewName);
    }

    /**
     * The name of the current view.
     * @return view name
     */
    public String getCurrentViewName() {
        return currentViewName;
    }
    /**
     * The current view of this isntance. Empty, if the current view could not be matched with one of the predefined
     * views (e.g. in case of a custom view).
     * @return calendar view
     */
    public Optional<CalendarView> getCurrentView() {
        return Optional.ofNullable(currentView);
    }

    /**
     * Switch to the interval containing the given date (e. g. to month "October" if the "15th October ..." is passed).
     *
     * @param date date to goto
     * @throws NullPointerException when null is passed
     */
    public void gotoDate(LocalDate date) {
        Objects.requireNonNull(date);
        getElement().callJsFunction("gotoDate", date.toString());
    }
    
    /**
     * Programatically scroll the current view to the given time in the format `hh:mm:ss.sss`, `hh:mm:sss` or `hh:mm`. For example, '05:00' signifies 5 hours.
     * 
     * @param duration duration
     * @throws NullPointerException when null is passed
     */
    public void scrollToTime(String duration) {
        Objects.requireNonNull(duration);	// No format check, it is already done in the calendar code
        getElement().callJsFunction("scrollToTime", duration);
    }
    
    /**
     * Programatically scroll the current view to the given time in the format `hh:mm:ss.sss`, `hh:mm:sss` or `hh:mm`. For example, '05:00' signifies 5 hours.
     * 
     * @param duration duration
     * @throws NullPointerException when null is passed
     */
    public void scrollToTime(LocalTime duration) {
        Objects.requireNonNull(duration);
        getElement().callJsFunction("scrollToTime", duration.format(DateTimeFormatter.ISO_LOCAL_TIME));
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
    public void setOption(Option option, Object value) {
        setOption(option, value, null);
    }

    /**
     * Sets an option for this instance. Passing a null value removes the option. The third parameter
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
    public void setOption(Option option, Object value, Object valueForServerSide) {
        setOption(option.getOptionKey(), value, valueForServerSide);
    }

    /**
     * Sets an option for this instance. Passing a null value removes the option.
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
    public void setOption(String option, Object value) {
        setOption(option, value, null);
    }

    /**
     * Sets an option for this instance. Passing a null value removes the option. The third parameter
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
    public void setOption(String option, Object value, Object valueForServerSide) {
        callOptionUpdate(option, value, valueForServerSide, "setOption");
    }

    private void callOptionUpdate(String option, Object value, Object valueForServerSide, String method, Serializable... additionalParameters) {
        Objects.requireNonNull(option);

        boolean attached = isAttached();

        if (value == null) {
            initialOptions.remove(option);
            options.remove(option);
            serverSideOptions.remove(option);
        } else {
            if (attached) {
                options.put(option, value);
            } else {
                initialOptions.put(option, value);
            }

            if (valueForServerSide == null || valueForServerSide.equals(value)) {
                serverSideOptions.remove(option);
            } else {
                serverSideOptions.put(option, valueForServerSide);
            }
        }

        if (attached) {
            Object[] parameters = Stream.concat(Stream.of(option, value), Stream.of(additionalParameters)).toArray(Object[]::new);
            getElement().callJsFunction(method, parameters);
        } else {
            ObjectNode initialOptions = (ObjectNode) getElement().getPropertyRaw("initialOptions");
            if (initialOptions == null) {
                initialOptions = JsonFactory.createObject();
                getElement().setPropertyJson("initialOptions", initialOptions);
            }

            if (value == null) {
                initialOptions.remove(option);
            } else {
                initialOptions.set(option, JsonUtils.toJsonNode(value));
            }
        }
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
    public void setFirstDay(DayOfWeek firstDay) {
        Objects.requireNonNull(firstDay);
        int value = firstDay == DayOfWeek.SUNDAY ? 0 : firstDay.getValue();
        setOption(Option.FIRST_DAY, value, firstDay);
    }

    /**
     *
     * Sets the calendar's height to a fixed amount of pixels.
     *
     * @param heightInPixels height in pixels (e.g. 300)
     * @deprecated Use {@link #setHeight(String)} or {@link #setHeight(float, Unit)} instead
     */
    @Deprecated(forRemoval = true)
    public void setHeight(int heightInPixels) {
        setHeight(heightInPixels, Unit.PIXELS);
    }

    /**
     * Sets the calendar's height to be calculated from parents height. Please be aware, that a block parent with
     * relative height (e. g. 100%) might not work properly. In this case use flex layout or set a fixed height for
     * the parent or the calendar.
     * @deprecated Use {@link #setHeight(String)} or {@link #setHeight(float, Unit)} instead
     */
    @Deprecated(forRemoval = true)
    public void setHeightByParent() {
        setHeight("100%");
    }

    /**
     * Sets the calendar's height to be calculated automatically. In current implementation this means by the calendars
     * width-height-ratio.
     * @deprecated Use {@link #setHeight(String)} or {@link #setHeight(float, Unit)} instead
     */
    @Deprecated(forRemoval = true)
    public void setHeightAuto() {
        setHeight("auto");
    }

    @Override
    public void setHeight(String height) {
        // we use the calendar option as it would otherwise override the plain style set by Vaadin
        setOption(Option.HEIGHT, height);
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
     * Determines the styling for week numbers in Month and DayGrid views.
     *
     * @param weekNumbersWithinDays by default to false
     * @deprecated this functionality is no longer supported, thus you can remove the call
     */
    @Deprecated(forRemoval = true)
    public void setWeekNumbersWithinDays(boolean weekNumbersWithinDays) {
        // NOOP
    }

    /**
     * Returns the current set locale.
     *
     * @return locale
     */
    public Locale getLocale() {
        Optional<Object> option = getOption(Option.LOCALE);

        if (!option.isPresent()) {
            return CalendarLocale.getDefaultLocale();
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
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale);
        setOption(Option.LOCALE, toClientSideLocale(locale), locale);
    }

    protected String toClientSideLocale(Locale locale) {
        return locale.toLanguageTag().toLowerCase();
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
     * and attached to the calendar as the eventClassNames callback. It must be a valid JavaScript function.
     * <br><br>
     * A ClassName Input for adding classNames to the outermost event element. If supplied as a callback function, it is called every time the associated event data changes.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * <b>Example</b>
     * <pre>
     * calendar.setEventClassNamesCallback("" +
     * "function(arg) { " +
     * "  if (arg.event.getCustomProperty('isUrgent', false)) {" +
     * "    return [ 'urgent' ];" +
     * "  } else { " +
     * "    return [ 'normal' ];" +
     * "  }" +
     * "}");
     * </pre>
     *
     * @param s function to be attached
     */
    public void setEntryClassNamesCallback(String s) {
        getElement().callJsFunction("setEventClassNamesCallback", s);
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the eventDidMount callback. It must be a valid JavaScript function.
     * <br><br>
     * Called right after the element has been added to the DOM. If the event data changes, this is <b>NOT</b> called again.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * Also this method must be called before the calendar is added to the client side. Changes afterwards are
     * ignored, until the component is re-attached.
     * <br><br>
     * If you also setup native event listeners, then these will automatically be attached to the end of your custom
     * callback function (must end with an closing bracked "}").
     *
     * @param s function to be attached
     * @see #addEntryNativeEventListener(String, String)
     *
     */
    public void setEntryDidMountCallback(String s) {
        eventDidMountCallback = s;
    }

    /**
     * Adds a native, client side / java script event listener, that will be added for all entries, when they
     * are mounted. The first parameter is the java script event name (for instance "click" or "mouseover"), the
     * second parameter is the javascript callback (e.g. "e => console.warn(e)" or "e => alert('Hello')").
     * <br><br>
     * This method does NOT check, if you pass valid event names or callbacks. It will also NOT sanitize the given
     * content, but pass it to the client as it is. <b>Be careful to not provide harmful code to the user!</b>
     * Also be aware, that some events may fire very often (e.g. "mousemove") and thus can lead to performance
     * issues.
     * <br><br>
     * Event listeners will be added utilizing the entryDidMount callback ({@link #setEntryDidMountCallback(String)}).
     * You still can setup a custom callback yourself. The event listener registration will automatically be
     * attached to the end of your custom function.
     * <br><br>
     * Inside the callback you may access the parameter of the entryDidMount callback by using the default name
     * "info" or the parameter name you used, if you created a custom callback. With that you can access the
     * entry itself (using "info.event") or the created html element (using "info.el"). For additional details
     * on which details are available in the callback, see the <a href="https://fullcalendar.io/docs/event-render-hooks">official FC docs</a>.
     * @see #setEntryDidMountCallback(String)
     * @param eventName javascript event name
     * @param eventCallback javascript event callback to be hooked to the event
     */
    public void addEntryNativeEventListener(String eventName, String eventCallback) {
        customNativeEventsMap.put(eventName, eventCallback);
    }

    private void updateEntryDidMountCallbackOnAttach() {
        StringBuilder events = null;
        if (!customNativeEventsMap.isEmpty()) {
            events = new StringBuilder();

            for (Map.Entry<String, String> entry : customNativeEventsMap.entrySet()) {
                events.append("arguments[0].el.addEventListener('")
                        .append(entry.getKey())
                        .append("', ")
                        .append(entry.getValue())
                        .append(")\n");
            }
        }

        String s = null;
        if (StringUtils.isNotBlank(eventDidMountCallback)) {

            if (events != null) {
                int index = eventDidMountCallback.lastIndexOf("}");
                s = eventDidMountCallback.substring(0, index);
                s += events;
                s += eventDidMountCallback.substring(index);
            } else {
                s = eventDidMountCallback;
            }

        } else if (events != null) {
            // there is no custom event did mount, so we just setup the native event callbacks
            s = "function(info) {\n" + events + "}";
        }

        if (s != null) {
            getElement().callJsFunction("setEventDidMountCallback", s);
        }
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the eventWillUnmount callback. It must be a valid JavaScript function.
     * <br><br>
     * Called right before the element will be removed from the DOM.
     * <br><br>
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     *
     * @param s function to be attached
     */
    public void setEntryWillUnmountCallback(String s) {
        getElement().callJsFunction("setEventWillUnmountCallback", s);
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the "eventContent" callback. It must be a valid JavaScript function.
     * <br><br>
     * Called right after the element has been added to the DOM.
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">https://fullcalendar.io/docs/event-render-hooks</a>
     * @see <a href="https://fullcalendar.io/docs/content-injection">https://fullcalendar.io/docs/content-injection</a>
     *
     * @param s function to be attached
     */
    public void setEntryContentCallback(String s) {
        setOption("eventContent", s);
    }

    /**
     * Sets the business hours for this calendar instance. You may pass multiple instances for different configurations.
     * Please be aware, that instances with crossing days or times are handled by the client side and may lead
     * to unexpected results.
     *
     * @param hours hours to set
     * @throws NullPointerException when null is passed
     */
    public void setBusinessHours(BusinessHours... hours) {
        Objects.requireNonNull(hours);

        setOption(Option.BUSINESS_HOURS, JsonUtils.toJsonNode(Arrays.stream(hours).map(BusinessHours::toJson)), hours);
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
    public void setSnapDuration(String duration) {
        Objects.requireNonNull(duration);
        setOption(Option.SNAP_DURATION, duration);
    }

    /**
     * Sets the min time for this calendar instance. This is the first time slot that will be displayed for each day.<p>
     * The default is '00:00:00'
     *
     * @param slotMinTime slotMinTime to set
     * @throws NullPointerException when null is passed
     */
    public void setSlotMinTime(LocalTime slotMinTime) {
        Objects.requireNonNull(slotMinTime);
        setOption(Option.SLOT_MIN_TIME, JsonUtils.toJsonNode(slotMinTime != null ? slotMinTime : "00:00:00"));
    }

    /**
     * Returns the fixedWeekCount. By default true.
     *
     * @return fixedWeekCount
     */
    public boolean getFixedWeekCount() {
        return (boolean) getOption(Option.FIXED_WEEK_COUNT).orElse(true);
    }

    /**
     * Determines the number of weeks displayed in a month view.
     * If true, the calendar will always be 6 weeks tall.
     * If false, the calendar will have either 4, 5, or 6 weeks, depending on the month.
     *
     * @param fixedWeekCount
     */
    public void setFixedWeekCount(boolean fixedWeekCount) {
        setOption(Option.FIXED_WEEK_COUNT, fixedWeekCount);
    }

    /**
     * Sets the max time for this calendar instance. This is the last time slot that will be displayed for each day<p>
     * The default is '24:00:00'
     *
     * @param slotMaxTime slotMaxTime to set
     * @throws NullPointerException when null is passed
     */
    public void setSlotMaxTime(LocalTime slotMaxTime) {
        Objects.requireNonNull(slotMaxTime);
        setOption(Option.SLOT_MAX_TIME, JsonUtils.toJsonNode(slotMaxTime != null ? slotMaxTime : "24:00:00"));
    }

    /**
     * Returns the current timezone of this calendar. Entries will be displayed related to this timezone.
     * Does not affect the server side times of entries, only their client side displayment.
     *
     * @return time zone
     */
    public Timezone getTimezone() {
        return (Timezone) getOption(Option.TIMEZONE).orElse(Timezone.UTC);
    }

    /**
     * Sets the timezone the calendar shall show. Does not affect the entries directly but only their client side displayment.
     *
     * @param timezone
     */
    public void setTimezone(Timezone timezone) {
        Objects.requireNonNull(timezone);

        Timezone oldTimezone = getTimezone();
        if (!timezone.equals(oldTimezone)) {
            setOption(Option.TIMEZONE, timezone.getClientSideValue(), timezone);
        }
    }

    /**
     * Returns the editable flag. By default true.
     *
     * @return editable editable
     */
    public boolean getEntryDurationEditable() {
        return (boolean) getOption(Option.ENTRY_DURATION_EDITABLE).orElse(true);
    }

    /**
     * Allow events’ durations to be editable through resizing.
     * <p>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setDurationEditable(Boolean)}
     *
     * @param editable editable
     */
    public void setEntryDurationEditable(boolean editable) {
        setOption(Option.ENTRY_DURATION_EDITABLE, editable);
    }

    /**
     * Returns the editable flag. By default false.
     *
     * @return editable editable
     */
    public boolean getEntryResizableFromStart() {
        return (boolean) getOption(Option.ENTRY_RESIZABLE_FROM_START).orElse(false);
    }

    /**
     * Whether the user can resize an event from its starting edge.
     *
     * @param editable editable
     */
    public void setEntryResizableFromStart(boolean editable) {
        setOption(Option.ENTRY_RESIZABLE_FROM_START, editable);
    }

    /**
     * Returns the editable flag. By default true.
     *
     * @return editable editable
     */
    public boolean getEntryStartEditable() {
        return (boolean) getOption(Option.ENTRY_START_EDITABLE).orElse(true);
    }

    /**
     * Allow events’ start times to be editable through dragging.
     * <p>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setStartEditable(Boolean)}
     *
     * @param editable editable
     */
    public void setEntryStartEditable(boolean editable) {
        setOption(Option.ENTRY_START_EDITABLE, editable);
    }

    /**
     * Returns the editable flag. By default false.
     *
     * @return editable editable
     */
    public boolean getEditable() {
        return (boolean) getOption(Option.EDITABLE).orElse(false);
    }

    /**
     * Determines whether the events on the calendar can be modified.
     * <p>
     * This determines if the events can be dragged and resized.
     * Enables/disables both at the same time.
     * If you don’t want both, use the more specific {@link #setEntryStartEditable(boolean)} and {@link #setEntryDurationEditable(boolean)} instead.
     * <br><br>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setEditable(boolean)}.
     * However, Background Events can not be dragged or resized.
     *
     * @param editable editable
     */
    public void setEditable(boolean editable) {
        setOption(Option.EDITABLE, editable);
    }

    /**
     * This method will limit the maximal entries shown per day to the given number (not including
     * the "+ x more entries" link). Must be a number > 0.
     *
     * @see #setMaxEntriesPerDayFitToCell()
     * @see #setMaxEntriesPerDayUnlimited()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">https://fullcalendar.io/docs/dayMaxEvents</a>
     *
     * @param maxEntriesPerDay maximal entries per day
     */
    public void setMaxEntriesPerDay(int maxEntriesPerDay) {
        setOption(Option.MAX_ENTRIES_PER_DAY, maxEntriesPerDay);
    }

    /**
     * When calling this method, the entries shown per day will be limited to fit the cell height.
     *
     * @see #setMaxEntriesPerDay(int)
     * @see #setMaxEntriesPerDayUnlimited()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">https://fullcalendar.io/docs/dayMaxEvents</a>
     */
    public void setMaxEntriesPerDayFitToCell() {
        setOption(Option.MAX_ENTRIES_PER_DAY, true);
    }

    /**
     * When calling this method, the entries shown per day will be unlimited and take all the space needed.
     *
     * @see #setMaxEntriesPerDay(int)
     * @see #setMaxEntriesPerDayFitToCell()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">https://fullcalendar.io/docs/dayMaxEvents</a>
     */
    public void setMaxEntriesPerDayUnlimited() {
        setOption(Option.MAX_ENTRIES_PER_DAY, false);
    }

    /**
     * Returns the weekends display status. By default true.
     *
     * @return weekends
     */
    public boolean getWeekends() {
        return (boolean) getOption(Option.WEEKENDS).orElse(true);
    }

    /**
     * Whether to include Saturday/Sunday columns in any of the calendar views.
     *
     * @param weekends
     */
    public void setWeekends(boolean weekends) {
        setOption(Option.WEEKENDS, weekends);
    }


    /**
     * display the header.
     *
     * @param header
     */
    public void setHeaderToolbar(Header header) {
        setOption(Option.HEADER_TOOLBAR, header.toJson());
    }

    /**
     * display the footer.
     *
     * @param footer
     */
    public void setFooterToolbar(Footer footer) {
        setOption(Option.FOOTER_TOOLBAR, footer.toJson());
    }

    /**
     * Returns the columnHeader.
     *
     * @return columnHeader
     */
    public boolean getColumnHeader() {
        return (boolean) getOption(Option.COLUMN_HEADER).orElse(true);
    }

    /**
     * Whether the day headers should appear. For the Month, TimeGrid, and DayGrid views.
     *
     * @param columnHeader
     */
    public void setColumnHeader(boolean columnHeader) {
        setOption(Option.COLUMN_HEADER, columnHeader);
    }

    /**
     * This method returns the timezone sent by the browser. It is <b>not</b> automatically set as the FC's timezone,
     * except for when the FC builder has been used with the auto timezone parameter.
     * <p></p>
     * Is empty if there was no timezone obtainable or the instance has not been attached to the client side, yet.
     *
     * @return optional client side timezone
     */
    public Optional<Timezone> getBrowserTimezone() {
        return Optional.ofNullable(browserTimezone);
    }

    /**
     * Sets the browser time zone. This method is intended to be used by the client only.
     *
     * @param timezoneId timezone id
     */
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
     * via {@link #setOption(Option, Object, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(Option option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(Option, Object, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(Option option, boolean forceClientSideValue) {
        return getOption(option.getOptionKey(), forceClientSideValue);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(Option, Object, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(String option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(Option, Object, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     * <br><br>
     * Returns {@code null} for initial options. Please use #getRawOption(String)
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(String option, boolean forceClientSideValue) {
        Objects.requireNonNull(option);
        if (!forceClientSideValue && serverSideOptions.containsKey(option)) {
            return Optional.ofNullable((T) serverSideOptions.get(option));
        }

        Object value = options.get(option);
        if(value == null) {
            value = initialOptions.get(option);
        }
        return Optional.ofNullable((T) value);
    }

    /**
     * Force the client side instance to re-render it's content.
     */
    public void render() {
        getElement().callJsFunction("renderCalendar");
    }

    /**
     * Registers a listener to be informed when a timeslot click event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotClickedListener(ComponentEventListener<? extends TimeslotClickedEvent> listener) {
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
    public Registration addEntryClickedListener(ComponentEventListener<EntryClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryClickedEvent.class, listener);
    }
    
    /**
     * Registers a listener to be informed when the user mouses over an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryMouseEnterListener(ComponentEventListener<EntryMouseEnterEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryMouseEnterEvent.class, listener);
    }
    
    /**
     * Registers a listener to be informed when the user mouses out of an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryMouseLeaveListener(ComponentEventListener<EntryMouseLeaveEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryMouseLeaveEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry resized event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryResizedListener(ComponentEventListener<EntryResizedEvent> listener) {
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
    public Registration addEntryDroppedListener(ComponentEventListener<EntryDroppedEvent> listener) {
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
    public Registration addDatesRenderedListener(ComponentEventListener<DatesRenderedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DatesRenderedEvent.class, listener);
    }


    /**
     * Registers a listener to be informed when a view skeleton rendered event occurred. This happens, when
     * the view has been rendered (intially or after a view change).
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addViewSkeletonRenderedListener(ComponentEventListener<ViewSkeletonRenderedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(ViewSkeletonRenderedEvent.class, listener);
    }

    /**
     * Same as {@link #addViewSkeletonRenderedListener(ComponentEventListener)} but with a more intuitive naming.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addViewChangedListener(ComponentEventListener<ViewSkeletonRenderedEvent> listener) {
        return addViewSkeletonRenderedListener(listener);
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
    public Registration addTimeslotsSelectedListener(ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedEvent.class, (ComponentEventListener<TimeslotsSelectedEvent>) listener);
    }

    /**
     * Registers a listener to be informed when the user clicked on the "more" link (e.g. "+6 more").
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addMoreLinkClickedListener(ComponentEventListener<MoreLinkClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(MoreLinkClickedEvent.class, listener);
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
    public Registration addDayNumberClickedListener(ComponentEventListener<DayNumberClickedEvent> listener) {
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
    public Registration addWeekNumberClickedListener(ComponentEventListener<WeekNumberClickedEvent> listener) {
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
    public Registration addBrowserTimezoneObtainedListener(ComponentEventListener<BrowserTimezoneObtainedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(BrowserTimezoneObtainedEvent.class, listener);
    }

    /**
     * Sets an action, that shall happen, when a user clicks the "+x more" link in the calendar (which occurs when the max
     * entries per day are exceeded). Default value is {@code POPUP}. Passing {@code null} will reset the default.
     *
     * @param moreLinkClickAction action to set
     * @see MoreLinkClickAction
     */
    public void setMoreLinkClickAction(MoreLinkClickAction moreLinkClickAction) {
        getElement().setProperty("moreLinkClickAction", (moreLinkClickAction != null ? moreLinkClickAction : MoreLinkClickAction.POPUP).getClientSideValue());
    }

    /**
     * Activates or deactivates the automatic calendar scrolling, when dragging an entry to the borders.
     *
     * @see <a href="https://fullcalendar.io/docs/dragScroll">https://fullcalendar.io/docs/dragScroll</a>
     * @param dragScrollActive activate drag scroll
     */
    public void setDragScrollActive(boolean dragScrollActive) {
        setOption(Option.DRAG_SCROLL, dragScrollActive);
    }

    /**
     * Enables prefetching of entries of adjacent time ranges (enabled by default).
     * <p></p>
     * Prefetching means, that entries of adjacent periods are also fetched. For instance, when the current view is
     * month based and prefetching is enabled, the client will not only fetch the entries of the shown month, but also
     * the one before and after. This prevents flickering / jumping calendar cells, when switching to the previous
     * or next time period.
     * <p></p>
     * The additional fetched entries are not cached on the client side. When switching to an adjacent period,
     * the client will fetch the entries for that period again (inclusive its own adjacent periods). Therefore,
     * if network performance is more important than visual appearence, you should disable prefetching.
     *
     * @param prefetchEnabled enable prefetch
     */
    public void setPrefetchEnabled(boolean prefetchEnabled) {
        getElement().setProperty("prefetchEnabled", prefetchEnabled);
    }

    /**
     * Indicates, if prefetching of entries of adjacent time ranges is enabled (true by default).
     * <p></p>
     * Prefetching means, that entries of adjacent periods are also fetched. For instance, when the current view is
     * month based and prefetching is enabled, the client will not only fetch the entries of the shown month, but also
     * the one before and after. This prevents flickering / jumping calendar cells, when switching to the previous
     * or next time period.
     * <p></p>
     * The additional fetched entries are not cached on the client side. When switching to an adjacent period,
     * the client will fetch the entries for that period again (inclusive its own adjacent periods). Therefore,
     * if network performance is more important than visual appearence, you should disable prefetching.
     *
     * @return prefetch is enabled
     */
    public boolean isPrefetchEnabled() {
        return getElement().getProperty("prefetchEnabled", false);
    }

    /**
     * Tries to find the calendar view based on the given clientSideValue. Empty, when the view name is not known on
     * the Java side (can be the case with unregistered custom views).
     * @param clientSideValue view's client side value to lookup
     * @return calendar view
     * @deprecated use {@link #lookupViewByClientSideValue(String)} instead
     */
    @Deprecated(forRemoval = true)
    public <T extends CalendarView> Optional<T> lookupViewName(String clientSideValue) {
        return lookupViewByClientSideValue(clientSideValue);
    }

    /**
     * Tries to find the calendar view based on the given name. Empty, when the view name is not known on the Java
     * side (can be the case with unregistered custom views).
     *
     * @param clientSideValue view name to lookup
     * @return calendar view
     */
    @SuppressWarnings("unchecked")
    public <T extends CalendarView> Optional<T> lookupViewByClientSideValue(String clientSideValue) {
        Optional<T> optional = (Optional<T>) CalendarViewImpl.ofClientSideValue(clientSideValue);
        if (optional.isPresent()) {
            return optional;
        }
        return Optional.ofNullable((T) customCalendarViews.get(clientSideValue));
    }

    public void setEntryDisplay(DisplayMode displayMode) {
        this.setOption(Option.ENTRY_DISPLAY, displayMode != null ? displayMode : DisplayMode.AUTO);
    }

    /**
     * Sets a valid range, that is open into the future.
     * @param start start of range
     */
    public void setValidRangeStart(LocalDate start) {
        setValidRange(start, null);
    }

    /**
     * Sets a valid range, that is open into the past.
     * @param end end of range
     */
    public void setValidRangeEnd(LocalDate end) {
        setValidRange(null, end);
    }

    /**
     * Creates a valid range between the given dates. If one of the dates is null, the range will be open to
     * that direction. If both are null, the valid range will be cleared.
     * @param start start
     * @param end end
     */
    public void setValidRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && !(start.isBefore(end))) {
            throw new IllegalArgumentException("Start must be before end");
        }

        ObjectNode jsonObject = JsonFactory.createObject();
        if (start != null) {
            jsonObject.put("start", JsonUtils.formatClientSideDateString(start));
        }
        if (end != null) {
            jsonObject.put("end", JsonUtils.formatClientSideDateString(end));
        }
        setOption(Option.VALID_RANGE, jsonObject);
    }

    /**
     * Clears the valid range.
     */
    public void clearValidRange() {
        setValidRange(null, null);
    }

    /**
     * Clears the current selection. This is only necessary, if after a selection no further click is required
     * by the user (e.g. through a dialog button). Any click by the user will clear the selection automatically.
     */
    public void clearSelection() {
        getElement().executeJs("this.calendar.unselect()");
    }

    /**
     * Returns an unmodifiable copy of the custom calendar views. Any changes to this instance's custom views will
     * be reflected in the returned map.
     * <br><br>
     * This map contains any custom calendar view, that has been registered via the builder's method
     * {@link FullCalendarBuilder#withCustomCalendarViews(CustomCalendarView...)} plus anonymous instances for any
     * view, that has been registered via the initial options. Views, that had been registered in both ways will
     * return the original type, not an anonymous one.
     * @return custom calendar views map
     */
    public Map<String, CustomCalendarView> getCustomCalendarViews() {
        return Collections.unmodifiableMap(customCalendarViews);
    }

    /**
     * Registers the given custom calendar views. Any custom views, that have been registered via the
     * initial options will be overridden by the given ones.
     * This method may be called only once and only before the component
     * is attached, otherwise an exception will be thrown. Same goes for calendar instances, that have already
     * registered custom views via the FC builder.
     * @param customCalendarViews custom calendar views
     */
    public void setCustomCalendarViews(CustomCalendarView... customCalendarViews) {
        if (isAttached()) {
            throw new UnsupportedOperationException("Views can only be set before the component is attached");
        }

        if (getElement().hasProperty("customViews")) {
            throw new UnsupportedOperationException("Custom views can only be set once");
        }

        ObjectNode json = JsonFactory.createObject();
        for (CustomCalendarView customCalendarView : customCalendarViews) {
            this.customCalendarViews.put(customCalendarView.getClientSideValue(), customCalendarView);
            json.set(customCalendarView.getClientSideValue(), customCalendarView.getViewSettings());
        }

        this.getElement().setPropertyJson("customViews", json);
    }

    /**
     * Adds theme variants to the calendar.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(FullCalendarVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants).map(FullCalendarVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the calendar.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(FullCalendarVariant... variants) {
        getThemeNames()
                .removeAll(Stream.of(variants).map(FullCalendarVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    public boolean hasThemeVariant(FullCalendarVariant variant) {
        return hasThemeName(variant.getVariantName());
    }

    /**
     * Enumeration of possible options, that can be applied to the calendar. Contains only options, that affect
     * the client side library, but not internal options. Also this list may not contain all options, but the most
     * common used ones. Any missing option can be set manually using one of the {@link FullCalendar#setOption} methods
     * using a string key.
     * <br><br>
     * Please refer to the FullCalendar client library documentation for possible options:
     * https://fullcalendar.io/docs
     */
    public enum Option {

        /**
         * @see <a href="https://fullcalendar.io/docs/allDaySlot">allDaySlot</a>
         */
        ALL_DAY_SLOT,


        /**
         * @see <a href="https://fullcalendar.io/docs/aspectRatio">aspectRatio</a>
         */
        ASPECT_RATIO,

        /**
         * @see <a href="https://fullcalendar.io/docs/businessHours">businessHours</a>
         */
        BUSINESS_HOURS,

        /**
         * @see <a href="https://fullcalendar.io/docs/dayHeaders">dayHeaders</a>
         */
        DAY_HEADERS,

        /**
         * @see <a href="https://fullcalendar.io/docs/dayHeaders">dayHeaders</a>
         * @deprecated use {@link #DAY_HEADERS} instead
         */
        @Deprecated(forRemoval = true)
        COLUMN_HEADER("dayHeaders"),

        /**
         * @see <a href="https://fullcalendar.io/docs/contentHeight">contentHeight</a>
         */
        CONTENT_HEIGHT,

        /**
         * @see <a href="https://fullcalendar.io/docs/dayHeaderFormat">dayHeaderFormat</a>
         */
        DAY_HEADER_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/dayMinWidth">dayMinWidth</a>
         */
        DAY_MIN_WIDTH,

        /**
         * @see <a href="https://fullcalendar.io/docs/displayEventTime">displayEventTime</a>
         */
        DISPLAY_ENTRY_TIME,

        /**
         * @see <a href="https://fullcalendar.io/docs/direction">direction</a>
         */
        DIRECTION,

        /**
         * @see <a href="https://fullcalendar.io/docs/dragScroll">dragScroll</a>
         */
        DRAG_SCROLL,

        /**
         * @see <a href="https://fullcalendar.io/docs/editable">editable</a>
         */
        EDITABLE,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventBackgroundColor">eventBackgroundColor</a>
         */
        ENTRY_BACKGROUND_COLOR,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventBorderColor">eventBorderColor</a>
         */
        ENTRY_BORDER_COLOR,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventColor">eventColor</a>
         */
        ENTRY_COLOR,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventDurationEditable">eventDurationEditable</a>
         */
        ENTRY_DURATION_EDITABLE,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventMaxStack">eventMaxStack</a>
         */
        ENTRY_MAX_STACK,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventMinHeight">eventMinHeight</a>
         */
        ENTRY_MIN_HEIGHT,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventOrder">eventOrder</a>
         */
        ENTRY_ORDER,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventOrderStrict">eventOrderStrict</a>
         */
        ENTRY_ORDER_STRICT,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventDisplay">eventDisplay</a>
         */
        ENTRY_DISPLAY,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventResizableFromStart">eventResizableFromStart</a>
         */
        ENTRY_RESIZABLE_FROM_START,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventShortHeight">eventShortHeight</a>
         */
        ENTRY_SHORT_HEIGHT,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventStartEditable">eventStartEditable</a>
         */
        ENTRY_START_EDITABLE,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventTextColor">eventTextColor</a>
         */
        ENTRY_TEXT_COLOR,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventTimeFormat">eventTimeFormat</a>
         */
        ENTRY_TIME_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/expandRows">expandRows</a>
         */
        EXPAND_ROWS,

        /**
         * @see <a href="https://fullcalendar.io/docs/firstDay">firstDay</a>
         */
        FIRST_DAY,

        /**
         * @see <a href="https://fullcalendar.io/docs/fixedWeekCount">fixedWeekCount</a>
         */
        FIXED_WEEK_COUNT,

        /**
         * @see <a href="https://fullcalendar.io/docs/footerToolbar">footerToolbar</a>
         */
        FOOTER_TOOLBAR,

        /**
         * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
         */
        HEADER_TOOLBAR,

        /**
         * @see <a href="https://fullcalendar.io/docs/height">height</a>
         */
        HEIGHT,

        /**
         * @see <a href="https://fullcalendar.io/docs/hiddenDays">hiddenDays</a>
         */
        HIDDEN_DAYS,

        /**
         * @see <a href="https://fullcalendar.io/docs/listDayFormat">listDayFormat</a>
         */
        LIST_DAY_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/listDaySideFormat">listDaySideFormat</a>
         */
        LIST_DAY_SIDE_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/locale">locale</a>
         */
        LOCALE,

        /**
         * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">dayMaxEvents</a>
         */
        MAX_ENTRIES_PER_DAY("dayMaxEvents"),

        /**
         * @see <a href="https://fullcalendar.io/docs/monthStartFormat">monthStartFormat</a>
         */
        MONTH_START_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/multiMonthMaxColumns">multiMonthMaxColumns</a>
         */
        MULTI_MONTH_MAX_COLUMNS,

        /**
         * @see <a href="https://fullcalendar.io/docs/multiMonthMinWidth">multiMonthMinWidth</a>
         */
        MULTI_MONTH_MIN_WIDTH,

        /**
         * @see <a href="https://fullcalendar.io/docs/multiMonthTitleFormat">multiMonthTitleFormat</a>
         */
        MULTI_MONTH_TITLE_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/navLinks">navLinks</a>
         */
        NAV_LINKS,

        /**
         * @see <a href="https://fullcalendar.io/docs/nextDayThreshold">nextDayThreshold</a>
         */
        NEXT_DAY_THRESHOLD,

        /**
         * @see <a href="https://fullcalendar.io/docs/nowIndicator">nowIndicator</a>
         */
        NOW_INDICATOR,

        /**
         * @see <a href="https://fullcalendar.io/docs/scrollTime">scrollTime</a>
         */
        SCROLL_TIME,

        /**
         * @see <a href="https://fullcalendar.io/docs/scrollTimeReset">scrollTimeReset</a>
         */
        SCROLL_TIME_RESET,

        /**
         * @see <a href="https://fullcalendar.io/docs/selectable">selectable</a>
         */
        SELECTABLE,

        /**
         * @see <a href="https://fullcalendar.io/docs/selectConstraint">selectConstraint</a>
         */
        SELECT_CONSTRAINT,

        /**
         * @see <a href="https://fullcalendar.io/docs/selectMinDistance">selectMinDistance</a>
         */
        SELECT_MIN_DISTANCE,

        /**
         * @see <a href="https://fullcalendar.io/docs/selectMirror">selectMirror</a>
         */
        SELECT_MIRROR,

        /**
         * @see <a href="https://fullcalendar.io/docs/selectOverlap">selectOverlap</a>
         */
        SELECT_OVERLAP, // function not yet supported

        /**
         * @see <a href="https://fullcalendar.io/docs/showNonCurrentDates">showNonCurrentDates</a>
         */
        SHOW_NON_CURRENT_DATES,

        /**
         * @see <a href="https://fullcalendar.io/docs/slotDuration">slotDuration</a>
         */
        SLOT_DURATION,

        /**
         * @see <a href="https://fullcalendar.io/docs/slotEventOverlap">slotEventOverlap</a>
         */
        SLOT_ENTRY_OVERLAP,

        /**
         * @see <a href="https://fullcalendar.io/docs/slotLabelFormat">slotLabelFormat</a>
         */
        SLOT_LABEL_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/slotLabelInterval">slotLabelInterval</a>
         */
        SLOT_LABEL_INTERVAL,

        /**
         * @see <a href="https://fullcalendar.io/docs/slotMaxTime">slotMaxTime</a>
         */
        SLOT_MAX_TIME,

        /**
         * @see <a href="https://fullcalendar.io/docs/slotMinTime">slotMinTime</a>
         */
        SLOT_MIN_TIME,

        /**
         * @see <a href="https://fullcalendar.io/docs/snapDuration">snapDuration</a>
         */
        SNAP_DURATION,

        /**
         * @see <a href="https://fullcalendar.io/docs/stickyFooterScrollbar">stickyFooterScrollbar</a>
         */
        STICKY_FOOTER_SCROLLBAR,

        /**
         * @see <a href="https://fullcalendar.io/docs/stickyHeaderDates">stickyHeaderDates</a>
         */
        STICKY_HEADER_DATES,

        /**
         * @see <a href="https://fullcalendar.io/docs/timeZone">timeZone</a>
         */
        TIMEZONE("timeZone"),

        /**
         * @see <a href="https://fullcalendar.io/docs/unselectAuto">unselectAuto</a>
         */
        UNSELECT_AUTO,

        /**
         * @see <a href="https://fullcalendar.io/docs/unselectCancel">unselectCancel</a>
         */
        UNSELECT_CANCEL,

        /**
         * @see <a href="https://fullcalendar.io/docs/validRange">validRange</a>
         */
        VALID_RANGE, // function not yet supported, but should not be necessary

        /**
         * @see <a href="https://fullcalendar.io/docs/weekends">weekends</a>
         */
        WEEKENDS,

        /**
         * @see <a href="https://fullcalendar.io/docs/weekNumbers">weekNumbers</a>
         */
        WEEK_NUMBERS,

        /**
         * @see <a href="https://fullcalendar.io/docs/weekNumberCalculation">weekNumberCalculation</a>
         */
        WEEK_NUMBER_CALCULATION,

        /**
         * @see <a href="https://fullcalendar.io/docs/weekNumberFormat">weekNumberFormat</a>
         */
        WEEK_NUMBER_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/weekText">weekText</a>
         */
        WEEK_TEXT,

        /**
         * @see <a href="https://fullcalendar.io/docs/weekTextLong">weekTextLong</a>
         */
        WEEK_TEXT_LONG;

        private final String optionKey;

        Option() {
            this.optionKey = CaseUtils.toCamelCase(name().replace("ENTRY", "EVENT"), false, '_');
        }

        Option(String optionKey) {
            this.optionKey = optionKey;
        }

        String getOptionKey() {
            return optionKey;
        }
    }

    /**
     * Possible actions, that shall happen on the client side.
     */
    public enum MoreLinkClickAction implements ClientSideValue {
        /**
         * Shows a popup on the client side. This popup is purely client side rendered. Entries shown in that
         * popup will fire the same click event as "normal" entries do.
         */
        POPUP("popover"),
        /**
         * Goes to a "day" view based on the current one.
         */
        DAY("day"),

        /**
         * Goes to a "week" view based on the current one.
         */
        WEEK("week"),

        /**
         * Nothing will happen automatically. You should use this action if you want to handle
         * the action manually (e.g. showing your own dialog / popup for the given events).
         */
        NOTHING("function");

        private final String clientSideValue;

        MoreLinkClickAction(String clientSideValue) {
            this.clientSideValue = clientSideValue;
        }

        @Override
        public String getClientSideValue() {
            return this.clientSideValue;
        }
    }

}

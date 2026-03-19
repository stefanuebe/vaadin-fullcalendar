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
@NpmPackage(value = "@fullcalendar/rrule", version = FullCalendar.FC_CLIENT_VERSION)
// TODO still necessary?
@NpmPackage(value = "moment", version = "2.30.1")
@NpmPackage(value = "moment-timezone", version = "0.6.0")
@NpmPackage(value = "@fullcalendar/moment", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/moment-timezone", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/google-calendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/icalendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "ical.js", version = "2.0.1")

@JsModule("./vaadin-full-calendar/full-calendar.ts")
@CssImport("./vaadin-full-calendar/full-calendar-styles.css")
@Tag("vaadin-full-calendar")
public class FullCalendar extends Component implements HasStyle, HasSize, HasTheme {

    /**
     * The library base version used in this addon. Some additional libraries might have a different version number due to
     * a different release cycle or known issues.
     */
    public static final String FC_CLIENT_VERSION = "6.1.20";

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
     * Maximum number of entries to cache. This prevents unbounded memory growth in long-running applications.
     */
    private static final int MAX_CACHED_ENTRIES = 10000;

    /**
     * Caches the last fetched entries for entry based events.
     * Uses a bounded LinkedHashMap to prevent unbounded memory growth.
     */
    private final Map<String, Entry> lastFetchedEntries = createBoundedEntryCache();
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
    private LocalDate currentIntervalEnd;
    private CalendarView currentView;

    private volatile boolean refreshAllEntriesRequested;
    private final Object refreshLock = new Object();

    private Map<String, String> customNativeEventsMap = new LinkedHashMap<>();
    private String eventDidMountCallback;

    /**
     * Server-side registry of client-managed event sources, keyed by source id.
     */
    private final Map<String, ClientSideEventSource<?>> eventSourceRegistry = new LinkedHashMap<>();

    // ---- View-Specific Options ----
    private final Map<String, ObjectNode> viewSpecificOptionsMap = new LinkedHashMap<>();

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
            currentIntervalEnd = event.getIntervalEnd();
        });

        addViewSkeletonRenderedListener(event -> {
            currentViewName = event.getViewName();
            currentView = event.getCalendarView().orElse(null);
        });

        setHeightFull(); // default from previous versions

        /* to allow class based styling for custom subclasses (e.g. for applying the lumo theme)*/
        addClassName("vaadin-full-calendar");
        addThemeVariants(FullCalendarVariant.VAADIN);
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

                    if (!eventSourceRegistry.isEmpty()) {
                        ArrayNode sourcesArray = JsonFactory.createArray();
                        eventSourceRegistry.values().stream().map(ClientSideEventSource::toJson).forEach(sourcesArray::add);
                        getElement().callJsFunction("restoreEventSources", sourcesArray);
                    }

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
            // Remove old listeners first to ensure cleanup even if setCalendar throws
            entryProviderDataListeners.forEach(Registration::remove);
            entryProviderDataListeners.clear();

            EntryProvider<? extends Entry> oldProvider = this.entryProvider;
            try {
                if (oldProvider != null) {
                    oldProvider.setCalendar(null);
                }

                this.entryProvider = entryProvider;
                entryProvider.setCalendar(this);

                entryProviderDataListeners.add(entryProvider.addEntryRefreshListener(event -> requestRefresh(event.getItemToRefresh())));
                entryProviderDataListeners.add(entryProvider.addEntriesChangeListener(event -> requestRefreshAllEntries()));
            } catch (RuntimeException e) {
                // Restore old provider on failure to maintain consistent state
                this.entryProvider = oldProvider;
                if (oldProvider != null) {
                    try {
                        oldProvider.setCalendar(this);
                        entryProviderDataListeners.add(oldProvider.addEntryRefreshListener(event -> requestRefresh(event.getItemToRefresh())));
                        entryProviderDataListeners.add(oldProvider.addEntriesChangeListener(event -> requestRefreshAllEntries()));
                    } catch (RuntimeException restoreException) {
                        // Log and suppress restore exception, throw original
                        e.addSuppressed(restoreException);
                    }
                }
                throw e;
            }
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
        synchronized (refreshLock) {
            if (!refreshAllEntriesRequested) {
                refreshAllEntriesRequested = true;
                getElement().getNode().runWhenAttached(ui -> {
                    ui.beforeClientResponse(this, pExecutionContext -> {
                        getElement().callJsFunction("refreshAllEvents");
                        synchronized (refreshLock) {
                            refreshAllEntriesRequested = false;
                        }
                    });
                });
            }
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
     * Returns the start of the currently displayed date interval (e.g., the first day of the
     * displayed month in month view). Updated after each render via {@code DatesRenderedEvent}.
     * <p>
     * Note: The value lags by one server round-trip — it reflects the state after the last
     * {@code datesSet} event was processed. Calling this immediately after a navigation method
     * (e.g., {@link #next()}) before the client fires the next {@code datesSet} event will
     * return the previous value.
     *
     * @return the current interval start, or empty if the calendar has not rendered yet
     * @see #getCurrentIntervalEnd()
     */
    public Optional<LocalDate> getCurrentIntervalStart() {
        return Optional.ofNullable(currentIntervalStart);
    }

    /**
     * Returns the end of the currently displayed date interval (exclusive). Updated after each
     * render via {@code DatesRenderedEvent}.
     * <p>
     * Note: The value lags by one server round-trip (see {@link #getCurrentIntervalStart()}).
     *
     * @return the current interval end (exclusive), or empty if the calendar has not rendered yet
     * @see #getCurrentIntervalStart()
     */
    public Optional<LocalDate> getCurrentIntervalEnd() {
        return Optional.ofNullable(currentIntervalEnd);
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
     * @deprecated Use {@link #setSelectable(boolean)} instead.
     */
    @Deprecated(forRemoval = false)
    public void setTimeslotsSelectable(boolean selectable) {
        setSelectable(selectable);
    }

    /**
     * Set if timeslots might be selected by the user. Please see also documentation of {@link #addTimeslotsSelectedListener(ComponentEventListener)}.
     *
     * @param selectable activate selectable
     */
    public void setSelectable(boolean selectable) {
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
     * @deprecated Use {@link #setNowIndicator(boolean)} instead.
     */
    @Deprecated(forRemoval = false)
    public void setNowIndicatorShown(boolean shown) {
        setNowIndicator(shown);
    }

    /**
     * If true is passed then the calendar will show a indicator for the current time, depending on the view.
     *
     * @param shown show indicator for now
     */
    public void setNowIndicator(boolean shown) {
        setOption(Option.NOW_INDICATOR, shown);
    }

    /**
     * When true is passed the day / week numbers (or texts) will become clickable by the user and fire an event
     * for the clicked day / week.
     *
     * @param clickable clickable
     * @deprecated Use {@link #setNavLinks(boolean)} instead.
     */
    @Deprecated(forRemoval = false)
    public void setNumberClickable(boolean clickable) {
        setNavLinks(clickable);
    }

    /**
     * When true is passed the day / week numbers (or texts) will become clickable by the user and fire an event
     * for the clicked day / week.
     *
     * @param navLinks navLinks
     */
    public void setNavLinks(boolean navLinks) {
        setOption(Option.NAV_LINKS, navLinks);
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

    // -------------------------------------------------------------------------
    // Drag/resize/select/drop JS-only callback setters
    // -------------------------------------------------------------------------

    /**
     * Sets a JavaScript function as the {@code selectAllow} callback. The function runs synchronously
     * on every mouse move during a drag-to-select operation and must return {@code true} to allow
     * or {@code false} to deny the selection.
     * <br><br>
     * This must be a client-side only callback because synchronous drag feedback requires zero latency.
     * <br><br>
     * <b>Note: </b> No security mechanism is applied to the string. Validate it before passing to the client.
     * <br><br>
     * Example:
     * <pre>
     * calendar.setSelectAllowCallback(
     *     "function(selectInfo) { return selectInfo.start >= new Date('2023-01-01'); }");
     * </pre>
     *
     * @param s JavaScript function string
     * @see <a href="https://fullcalendar.io/docs/selectAllow">selectAllow</a>
     */
    public void setSelectAllowCallback(String s) {
        getElement().callJsFunction("setSelectAllowCallback", s);
    }

    /**
     * Sets a JavaScript function as the {@code eventAllow} callback. The function runs synchronously
     * during a drag operation and must return {@code true} to allow or {@code false} to deny a drop
     * at the given location.
     * <br><br>
     * This must be a client-side only callback because synchronous drag feedback requires zero latency.
     * <br><br>
     * <b>Note: </b> No security mechanism is applied to the string. Validate it before passing to the client.
     * <br><br>
     * Example:
     * <pre>
     * calendar.setEventAllowCallback(
     *     "function(dropInfo, draggedEvent) { return dropInfo.resource.id !== 'locked-room'; }");
     * </pre>
     *
     * @param s JavaScript function string
     * @see <a href="https://fullcalendar.io/docs/eventAllow">eventAllow</a>
     */
    public void setEventAllowCallback(String s) {
        getElement().callJsFunction("setEventAllowCallback", s);
    }

    /**
     * Sets a JavaScript function as the global {@code eventOverlap} callback. The function provides
     * per-combination control over whether a dragged event may overlap with a stationary event.
     * <br><br>
     * This must be a client-side only callback because it runs synchronously during drag feedback.
     * The per-entry {@link Entry#setOverlap(Boolean)} takes precedence over this global function.
     * <br><br>
     * <b>Note: </b> No security mechanism is applied to the string. Validate it before passing to the client.
     * <br><br>
     * Example:
     * <pre>
     * calendar.setEventOverlapCallback(
     *     "function(stillEvent, movingEvent) { return stillEvent.display === 'background'; }");
     * </pre>
     *
     * @param s JavaScript function string
     * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
     */
    public void setEventOverlapCallback(String s) {
        getElement().callJsFunction("setEventOverlapCallback", s);
    }

    /**
     * Sets whether events may overlap each other when being dragged or resized.
     * {@code true} (the default) allows overlap; {@code false} prevents it.
     * <p>
     * For finer per-combination control use {@link #setEventOverlapCallback(String)}.
     * The per-entry {@link Entry#setOverlapAllowed(Boolean)} takes precedence.
     *
     * @param overlap whether events may overlap
     * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
     */
    public void setEventOverlap(boolean overlap) {
        setOption(Option.EVENT_OVERLAP, overlap);
    }

    /**
     * Sets whether the calendar accepts external HTML elements being dragged onto it.
     * When {@code true}, the {@code drop} and {@code eventReceive} events become active.
     *
     * @param droppable enable external drag-drop
     * @see <a href="https://fullcalendar.io/docs/droppable">droppable</a>
     */
    public void setDroppable(boolean droppable) {
        setOption(Option.DROPPABLE, droppable);
    }

    /**
     * Sets a CSS selector or JavaScript function to filter which external elements may be dropped onto
     * the calendar. Only elements matching the selector (or for which the function returns {@code true})
     * will be accepted.
     * <br><br>
     * <b>Note: </b> No security mechanism is applied to the string. Validate it before passing to the client.
     *
     * @param cssOrFunction CSS selector string or JS function string
     * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
     */
    public void setDropAccept(String cssOrFunction) {
        setOption(Option.DROP_ACCEPT, cssOrFunction);
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
     * @param columnHeader whether to show day headers
     * @deprecated Use {@link #setDayHeaders(boolean)} instead.
     */
    @Deprecated(forRemoval = false)
    public void setColumnHeader(boolean columnHeader) {
        setDayHeaders(columnHeader);
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

    // -------------------------------------------------------------------------
    // Interaction callback listeners
    // -------------------------------------------------------------------------

    /**
     * Registers a listener for when the user begins dragging an entry. Fires regardless of whether the
     * drag results in a position change. Use {@link #addEntryDragStopListener} to clean up UI feedback.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryDragStartListener(ComponentEventListener<EntryDragStartEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDragStartEvent.class, listener);
    }

    /**
     * Registers a listener for when the user stops dragging an entry, regardless of whether the position
     * changed. Use this to clean up UI feedback shown in response to {@link #addEntryDragStartListener}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryDragStopListener(ComponentEventListener<EntryDragStopEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDragStopEvent.class, listener);
    }

    /**
     * Registers a listener for when the user begins resizing an entry. Fires regardless of whether the
     * resize results in a duration change. Use {@link #addEntryResizeStopListener} to clean up UI feedback.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryResizeStartListener(ComponentEventListener<EntryResizeStartEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizeStartEvent.class, listener);
    }

    /**
     * Registers a listener for when the user stops resizing an entry, regardless of whether the duration
     * changed. Use this to clean up UI feedback shown in response to {@link #addEntryResizeStartListener}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryResizeStopListener(ComponentEventListener<EntryResizeStopEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizeStopEvent.class, listener);
    }

    /**
     * Registers a listener for when the current timeslot selection is cleared. Fires on user click outside
     * the selection, Escape key, a new selection, or a programmatic call to {@code calendar.unselect()}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotsUnselectListener(ComponentEventListener<TimeslotsUnselectEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsUnselectEvent.class, listener);
    }

    /**
     * Registers a listener for when any external HTML element is dropped onto the calendar.
     * Requires {@link #setDroppable(boolean) setDroppable(true)}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addDropListener(ComponentEventListener<DropEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DropEvent.class, listener);
    }

    /**
     * Registers a listener for when an external element with a {@code data-event} attribute has been dropped
     * and FullCalendar has created a new entry from it. The entry is not added to the provider automatically.
     * Requires {@link #setDroppable(boolean) setDroppable(true)}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryReceiveListener(ComponentEventListener<EntryReceiveEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryReceiveEvent.class, listener);
    }

    /**
     * Registers a listener for when an entry is dragged away from this calendar to another instance.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     * @see EntryLeaveEvent
     */
    public Registration addEntryLeaveListener(ComponentEventListener<EntryLeaveEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryLeaveEvent.class, listener);
    }

    // -------------------------------------------------------------------------
    // Event source management
    // -------------------------------------------------------------------------

    /**
     * Adds a client-managed event source to this calendar. The browser will fetch events from this source directly,
     * bypassing the server-side {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
     * <br><br>
     * A server-side registry entry is kept so the source can be restored on reattachment.
     *
     * @param source event source to add; must not be null
     * @throws NullPointerException if source is null
     */
    public void addEventSource(ClientSideEventSource<?> source) {
        Objects.requireNonNull(source, "source must not be null");
        eventSourceRegistry.put(source.getId(), source);
        getElement().callJsFunction("addEventSource", source.toJson());
    }

    /**
     * Removes the client-managed event source with the given id from this calendar.
     * Does nothing if no source with that id has been added.
     *
     * @param id id of the source to remove; must not be null
     * @throws NullPointerException if id is null
     */
    public void removeEventSource(String id) {
        Objects.requireNonNull(id, "id must not be null");
        eventSourceRegistry.remove(id);
        getElement().callJsFunction("removeEventSource", id);
    }

    /**
     * Replaces all current client-managed event sources with the given collection.
     * Previously added sources are removed. If the collection is empty, all client-managed
     * sources are cleared.
     *
     * @param sources new set of event sources; must not be null
     * @throws NullPointerException if sources is null
     */
    public void setEventSources(java.util.Collection<? extends ClientSideEventSource<?>> sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        eventSourceRegistry.clear();
        sources.forEach(s -> eventSourceRegistry.put(s.getId(), s));
        ArrayNode array = JsonFactory.createArray();
        sources.stream().map(ClientSideEventSource::toJson).forEach(array::add);
        getElement().callJsFunction("setEventSources", array);
    }

    /**
     * Returns an unmodifiable view of all registered client-managed event sources.
     *
     * @return collection of registered event sources
     */
    public java.util.Collection<ClientSideEventSource<?>> getEventSources() {
        return java.util.Collections.unmodifiableCollection(eventSourceRegistry.values());
    }

    /**
     * Returns the client-managed event source with the given ID, or empty if no such source is registered.
     *
     * @param id event source id; must not be null
     * @return the event source, or empty
     * @throws NullPointerException if id is null
     */
    public Optional<ClientSideEventSource<?>> getEventSourceById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(eventSourceRegistry.get(id));
    }

    /**
     * Forces all event sources (including the server-side entry provider) to re-fetch their data immediately.
     */
    public void refetchEvents() {
        getElement().callJsFunction("refetchEvents");
    }

    /**
     * Forces a single event source to re-fetch its data. Only the source with the given id is refreshed;
     * all other sources remain untouched. Use {@link #refetchEvents()} to refresh all sources at once.
     *
     * @param sourceId the id of the event source to refetch
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/EventSource-refetch">EventSource::refetch</a>
     */
    public void refetchEventSource(String sourceId) {
        Objects.requireNonNull(sourceId);
        getElement().executeJs("var s = this.calendar.getEventSourceById($0); if (s) s.refetch();", sourceId);
    }

    /**
     * Sets the name of the query parameter sent to JSON feed sources for the start of the range.
     * Default is {@code "start"}.
     *
     * @param startParam parameter name
     * @see <a href="https://fullcalendar.io/docs/startParam">startParam</a>
     */
    public void setStartParam(String startParam) {
        setOption(Option.START_PARAM, startParam);
    }

    /**
     * Sets the name of the query parameter sent to JSON feed sources for the end of the range.
     * Default is {@code "end"}.
     *
     * @param endParam parameter name
     * @see <a href="https://fullcalendar.io/docs/endParam">endParam</a>
     */
    public void setEndParam(String endParam) {
        setOption(Option.END_PARAM, endParam);
    }

    /**
     * Sets the name of the query parameter sent to JSON feed sources for the time zone.
     * Default is {@code "timeZone"}.
     *
     * @param timeZoneParam parameter name
     * @see <a href="https://fullcalendar.io/docs/timeZoneParam">timeZoneParam</a>
     */
    public void setTimeZoneParam(String timeZoneParam) {
        setOption(Option.TIME_ZONE_PARAM, timeZoneParam);
    }

    /**
     * Sets the Google Calendar API key used by all {@link GoogleCalendarEventSource} instances that do not
     * specify their own key.
     *
     * @param apiKey Google Calendar API key
     * @see <a href="https://fullcalendar.io/docs/googleCalendarApiKey">googleCalendarApiKey</a>
     */
    public void setGoogleCalendarApiKey(String apiKey) {
        setOption(Option.GOOGLE_CALENDAR_API_KEY, apiKey);
    }

    /**
     * Sets a JS function called while event sources are loading (async fetch). Use this to show/hide a loading
     * Sets a JavaScript callback that fires when event sources begin and complete loading.
     * <br><br>
     * <b>Note: </b> No content parsing, escaping, quoting or other security mechanism is applied on this string.
     * Validate it before passing to the client.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/loading">loading</a>
     */
    public void setLoadingCallback(String jsFunction) {
        getElement().callJsFunction("setLoadingCallback", jsFunction);
    }

    /**
     * Sets a JS function that transforms each raw event record from any event source before FullCalendar
     * parses it. This is a client-side-only callback.
     * <br><br>
     * <b>Note: </b> No content parsing, escaping, quoting or other security mechanism is applied on this string.
     * Validate it before passing to the client.
     *
     * @param jsFunction JS function string: {@code "function(event) { event.title = event.name; return event; }"}
     * @see <a href="https://fullcalendar.io/docs/eventDataTransform">eventDataTransform</a>
     */
    public void setEventDataTransformCallback(String jsFunction) {
        getElement().callJsFunction("setEventDataTransformCallback", jsFunction);
    }

    /**
     * Sets a JS function that transforms the raw HTTP response from a JSON feed source before FullCalendar
     * parses it. This is a client-side-only callback.
     * <br><br>
     * <b>Note: </b> No content parsing, escaping, quoting or other security mechanism is applied on this string.
     * Validate it before passing to the client.
     *
     * @param jsFunction JS function string: {@code "function(content, xhr) { return content.data; }"}
     * @see <a href="https://fullcalendar.io/docs/eventSourceSuccess">eventSourceSuccess</a>
     */
    public void setEventSourceSuccessCallback(String jsFunction) {
        getElement().callJsFunction("setEventSourceSuccessCallback", jsFunction);
    }

    /**
     * Registers a listener for when a client-managed event source fails to load.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEventSourceFailureListener(ComponentEventListener<EventSourceFailureEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EventSourceFailureEvent.class, listener);
    }

    /**
     * Registers a listener for when an entry from a client-managed event source is dragged to a new time slot.
     * Fires instead of {@link EntryDroppedEvent} when the dropped entry's id is not in the server-side cache.
     * <br><br>
     * Requires that drag/drop is enabled on the source via {@link ClientSideEventSource#withEditable(boolean) withEditable(true)}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addExternalEntryDroppedListener(ComponentEventListener<ExternalEntryDroppedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(ExternalEntryDroppedEvent.class, listener);
    }

    /**
     * Registers a listener for when an entry from a client-managed event source is resized.
     * Fires instead of {@link EntryResizedEvent} when the resized entry's id is not in the server-side cache.
     * <br><br>
     * Requires that resize is enabled on the source via {@link ClientSideEventSource#withEditable(boolean) withEditable(true)}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addExternalEntryResizedListener(ComponentEventListener<ExternalEntryResizedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(ExternalEntryResizedEvent.class, listener);
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
     * @deprecated Use {@link #setDragScroll(boolean)} instead.
     */
    @Deprecated(forRemoval = false)
    public void setDragScrollActive(boolean dragScrollActive) {
        setDragScroll(dragScrollActive);
    }

    /**
     * Activates or deactivates the automatic calendar scrolling, when dragging an entry to the borders.
     *
     * @see <a href="https://fullcalendar.io/docs/dragScroll">https://fullcalendar.io/docs/dragScroll</a>
     * @param dragScroll activate drag scroll
     */
    public void setDragScroll(boolean dragScroll) {
        setOption(Option.DRAG_SCROLL, dragScroll);
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

    /**
     * Sets the default display mode for all events on this calendar.
     * Corresponds to the FC {@code eventDisplay} option.
     * <br><br>
     * Default is {@link DisplayMode#AUTO}.
     *
     * @param displayMode the display mode; must not be null
     * @throws NullPointerException if null is passed
     * @see <a href="https://fullcalendar.io/docs/eventDisplay">eventDisplay</a>
     */
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

    /**
     * Checks whether the given theme variant is currently applied to this calendar.
     *
     * @param variant the variant to check
     * @return {@code true} if the variant is active
     */
    public boolean hasThemeVariant(FullCalendarVariant variant) {
        return hasThemeName(variant.getVariantName());
    }

    // -------------------------------------------------------------------------
    // Typed setters
    // -------------------------------------------------------------------------

    /**
     * Whether the time text is displayed in the event block.
     *
     * @param display display event time
     * @see <a href="https://fullcalendar.io/docs/displayEventTime">displayEventTime</a>
     */
    public void setDisplayEntryTime(boolean display) {
        setOption(Option.DISPLAY_ENTRY_TIME, display);
    }

    /**
     * Hides the given days of week from the calendar. Sunday in Java is {@link DayOfWeek#SUNDAY} (value 7),
     * but FullCalendar uses 0 for Sunday; this method converts accordingly.
     *
     * @param days days to hide
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/hiddenDays">hiddenDays</a>
     */
    public void setHiddenDays(DayOfWeek... days) {
        Objects.requireNonNull(days);
        Integer[] values = new Integer[days.length];
        for (int i = 0; i < days.length; i++) {
            values[i] = days[i] == DayOfWeek.SUNDAY ? 0 : days[i].getValue();
        }
        setOption(Option.HIDDEN_DAYS, values);
    }

    /**
     * Sets the aspect ratio (width-to-height) of the calendar.
     *
     * @param ratio aspect ratio
     * @see <a href="https://fullcalendar.io/docs/aspectRatio">aspectRatio</a>
     */
    public void setAspectRatio(double ratio) {
        setOption(Option.ASPECT_RATIO, ratio);
    }

    /**
     * Sets a fixed content height in pixels.
     *
     * @param heightInPixels height in pixels
     * @see <a href="https://fullcalendar.io/docs/contentHeight">contentHeight</a>
     */
    public void setContentHeight(int heightInPixels) {
        setOption(Option.CONTENT_HEIGHT, heightInPixels);
    }

    /**
     * Sets the content height as a CSS string value (e.g. {@code "auto"} or {@code "100%"}).
     *
     * @param height height string
     * @see <a href="https://fullcalendar.io/docs/contentHeight">contentHeight</a>
     */
    public void setContentHeight(String height) {
        setOption(Option.CONTENT_HEIGHT, height);
    }

    /**
     * Whether time-grid rows will expand to fill the available height.
     *
     * @param expand expand rows
     * @see <a href="https://fullcalendar.io/docs/expandRows">expandRows</a>
     */
    public void setExpandRows(boolean expand) {
        setOption(Option.EXPAND_ROWS, expand);
    }

    /**
     * Whether the date headers should stick to the top of the scroll container while scrolling.
     *
     * @param sticky sticky header dates
     * @see <a href="https://fullcalendar.io/docs/stickyHeaderDates">stickyHeaderDates</a>
     */
    public void setStickyHeaderDates(boolean sticky) {
        setOption(Option.STICKY_HEADER_DATES, sticky);
    }

    /**
     * Whether a scrollbar at the bottom of the calendar should stick to the bottom of the screen.
     *
     * @param sticky sticky footer scrollbar
     * @see <a href="https://fullcalendar.io/docs/stickyFooterScrollbar">stickyFooterScrollbar</a>
     */
    public void setStickyFooterScrollbar(boolean sticky) {
        setOption(Option.STICKY_FOOTER_SCROLLBAR, sticky);
    }

    /**
     * Whether to display the all-day slot in the time-grid views.
     *
     * @param allDaySlot show all-day slot
     * @see <a href="https://fullcalendar.io/docs/allDaySlot">allDaySlot</a>
     */
    public void setAllDaySlot(boolean allDaySlot) {
        setOption(Option.ALL_DAY_SLOT, allDaySlot);
    }

    /**
     * Whether events in the time-grid can overlap each other within the same slot.
     *
     * @param overlap slot event overlap
     * @see <a href="https://fullcalendar.io/docs/slotEventOverlap">slotEventOverlap</a>
     */
    public void setSlotEntryOverlap(boolean overlap) {
        setOption(Option.SLOT_ENTRY_OVERLAP, overlap);
    }

    /**
     * Sets the minimum height of an event in pixels when it is very short in duration.
     *
     * @param pixels minimum event height
     * @see <a href="https://fullcalendar.io/docs/eventMinHeight">eventMinHeight</a>
     */
    public void setEntryMinHeight(int pixels) {
        setOption(Option.ENTRY_MIN_HEIGHT, pixels);
    }

    /**
     * Sets the pixel height threshold for when events are considered "short".
     *
     * @param pixels short event height threshold
     * @see <a href="https://fullcalendar.io/docs/eventShortHeight">eventShortHeight</a>
     */
    public void setEntryShortHeight(int pixels) {
        setOption(Option.ENTRY_SHORT_HEIGHT, pixels);
    }

    /**
     * Sets the maximum number of events that will stack on top of each other in time-grid views.
     *
     * @param maxStack maximum event stack
     * @see <a href="https://fullcalendar.io/docs/eventMaxStack">eventMaxStack</a>
     */
    public void setEntryMaxStack(int maxStack) {
        setOption(Option.ENTRY_MAX_STACK, maxStack);
    }

    /**
     * Whether dates in the current month, but outside of the current month range should be shown.
     *
     * @param show show non-current dates
     * @see <a href="https://fullcalendar.io/docs/showNonCurrentDates">showNonCurrentDates</a>
     */
    public void setShowNonCurrentDates(boolean show) {
        setOption(Option.SHOW_NON_CURRENT_DATES, show);
    }

    /**
     * Sets the duration of each time slot in the time-grid (e.g. {@code "00:30:00"}).
     *
     * @param duration slot duration string
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/slotDuration">slotDuration</a>
     */
    public void setSlotDuration(String duration) {
        Objects.requireNonNull(duration);
        setOption(Option.SLOT_DURATION, duration);
    }

    /**
     * Sets the frequency at which the time-axis is labeled (e.g. {@code "01:00:00"}).
     *
     * @param interval slot label interval string
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/slotLabelInterval">slotLabelInterval</a>
     */
    public void setSlotLabelInterval(String interval) {
        Objects.requireNonNull(interval);
        setOption(Option.SLOT_LABEL_INTERVAL, interval);
    }

    /**
     * Whether to display the day headers in the time-grid and day-grid views.
     *
     * @param show show day headers
     * @see <a href="https://fullcalendar.io/docs/dayHeaders">dayHeaders</a>
     */
    public void setDayHeaders(boolean show) {
        setOption(Option.DAY_HEADERS, show);
    }

    /**
     * Sets the initial scroll position of the time-grid (e.g. {@code "06:00:00"}).
     *
     * @param time scroll time string
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/scrollTime">scrollTime</a>
     */
    public void setScrollTime(String time) {
        Objects.requireNonNull(time);
        setOption(Option.SCROLL_TIME, time);
    }

    /**
     * Sets the initial scroll position of the time-grid from a {@link LocalTime}.
     *
     * @param time scroll time
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/scrollTime">scrollTime</a>
     */
    public void setScrollTime(LocalTime time) {
        Objects.requireNonNull(time);
        setOption(Option.SCROLL_TIME, time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    /**
     * Whether the scroll position is reset to {@code scrollTime} when navigating to a new date.
     *
     * @param reset reset scroll time on navigation
     * @see <a href="https://fullcalendar.io/docs/scrollTimeReset">scrollTimeReset</a>
     */
    public void setScrollTimeReset(boolean reset) {
        setOption(Option.SCROLL_TIME_RESET, reset);
    }

    /**
     * Sets the text direction of the calendar. Pass {@code null} to clear the option and revert to
     * the browser/locale default.
     *
     * @param direction text direction, or {@code null} to clear
     * @see <a href="https://fullcalendar.io/docs/direction">direction</a>
     */
    public void setDirection(Direction direction) {
        setOption(Option.DIRECTION, direction != null ? direction.getClientSideValue() : null, direction);
    }

    /**
     * Whether a "mirror" event will be shown while the user is dragging a selection.
     *
     * @param mirror show select mirror
     * @see <a href="https://fullcalendar.io/docs/selectMirror">selectMirror</a>
     */
    public void setSelectMirror(boolean mirror) {
        setOption(Option.SELECT_MIRROR, mirror);
    }

    /**
     * Sets the minimum distance in pixels the user must drag before a selection is made.
     *
     * @param pixels minimum drag distance
     * @see <a href="https://fullcalendar.io/docs/selectMinDistance">selectMinDistance</a>
     */
    public void setSelectMinDistance(int pixels) {
        setOption(Option.SELECT_MIN_DISTANCE, pixels);
    }

    /**
     * Whether selections are allowed to overlap events.
     *
     * @param overlap allow selection overlap
     * @see <a href="https://fullcalendar.io/docs/selectOverlap">selectOverlap</a>
     */
    public void setSelectOverlap(boolean overlap) {
        setOption(Option.SELECT_OVERLAP, overlap);
    }

    /**
     * Limits user selections to a constraint. Restricts when selections can be made.
     *
     * @param constraint constraint string — either an event group id or {@code "businessHours"}
     *                   that restricts when selections can be made
     * @see <a href="https://fullcalendar.io/docs/selectConstraint">selectConstraint</a>
     */
    public void setSelectConstraint(String constraint) {
        setOption(Option.SELECT_CONSTRAINT, constraint);
    }

    /**
     * Limits user selections to business hours only.
     * Equivalent to {@code setSelectConstraint("businessHours")}.
     *
     * @see <a href="https://fullcalendar.io/docs/selectConstraint">selectConstraint</a>
     */
    public void setSelectConstraintToBusinessHours() {
        setOption(Option.SELECT_CONSTRAINT, "businessHours");
    }

    /**
     * Whether clicking elsewhere on the page will cause the current selection to be cleared.
     *
     * @param auto unselect on outside click
     * @see <a href="https://fullcalendar.io/docs/unselectAuto">unselectAuto</a>
     */
    public void setUnselectAuto(boolean auto) {
        setOption(Option.UNSELECT_AUTO, auto);
    }

    /**
     * A CSS selector for elements that, when clicked, will not cause the current selection to be cleared.
     *
     * @param cssSelector CSS selector
     * @see <a href="https://fullcalendar.io/docs/unselectCancel">unselectCancel</a>
     */
    public void setUnselectCancel(String cssSelector) {
        setOption(Option.UNSELECT_CANCEL, cssSelector);
    }

    /**
     * Sets the ordering of events within a day. Multiple fields can be passed; they are joined as a
     * comma-separated string sent to the client.
     *
     * @param fields event order fields (e.g. {@code "start"}, {@code "-duration"}, {@code "title"})
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/eventOrder">eventOrder</a>
     */
    public void setEntryOrder(String... fields) {
        Objects.requireNonNull(fields);
        setOption(Option.ENTRY_ORDER, String.join(",", fields));
    }

    /**
     * Whether {@code eventOrder} should be strictly enforced.
     *
     * @param strict strict event order
     * @see <a href="https://fullcalendar.io/docs/eventOrderStrict">eventOrderStrict</a>
     */
    public void setEntryOrderStrict(boolean strict) {
        setOption(Option.ENTRY_ORDER_STRICT, strict);
    }

    /**
     * Sets the time threshold at which an event spanning into the next day is considered to belong to the next day
     * (e.g. {@code "09:00:00"}).
     *
     * @param threshold threshold string
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/nextDayThreshold">nextDayThreshold</a>
     */
    public void setNextDayThreshold(String threshold) {
        Objects.requireNonNull(threshold);
        setOption(Option.NEXT_DAY_THRESHOLD, threshold);
    }

    /**
     * Sets the time threshold at which an event spanning into the next day is considered to belong to the next day.
     *
     * @param threshold threshold as LocalTime
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/nextDayThreshold">nextDayThreshold</a>
     */
    public void setNextDayThreshold(LocalTime threshold) {
        Objects.requireNonNull(threshold);
        setOption(Option.NEXT_DAY_THRESHOLD, threshold.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    /**
     * Sets the minimum width (in pixels) of each day column in the day-grid views.
     *
     * @param pixels minimum day width
     * @see <a href="https://fullcalendar.io/docs/dayMinWidth">dayMinWidth</a>
     */
    public void setDayMinWidth(int pixels) {
        setOption(Option.DAY_MIN_WIDTH, pixels);
    }

    /**
     * Sets the time format for event blocks (e.g. {@code "HH:mm"}).
     *
     * @param format time format string
     * @see <a href="https://fullcalendar.io/docs/eventTimeFormat">eventTimeFormat</a>
     */
    public void setEntryTimeFormat(String format) {
        setOption(Option.ENTRY_TIME_FORMAT, format);
    }

    /**
     * Sets the format for day headers.
     *
     * @param format day header format string
     * @see <a href="https://fullcalendar.io/docs/dayHeaderFormat">dayHeaderFormat</a>
     */
    public void setDayHeaderFormat(String format) {
        setOption(Option.DAY_HEADER_FORMAT, format);
    }

    /**
     * Sets the format for the day heading in list view.
     *
     * @param format list day format string
     * @see <a href="https://fullcalendar.io/docs/listDayFormat">listDayFormat</a>
     */
    public void setListDayFormat(String format) {
        setOption(Option.LIST_DAY_FORMAT, format);
    }

    /**
     * Sets the format for the side text next to the day heading in list view.
     *
     * @param format list day side format string
     * @see <a href="https://fullcalendar.io/docs/listDaySideFormat">listDaySideFormat</a>
     */
    public void setListDaySideFormat(String format) {
        setOption(Option.LIST_DAY_SIDE_FORMAT, format);
    }

    /**
     * Sets the week number calculation algorithm.
     *
     * @param calc week number calculation
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/weekNumberCalculation">weekNumberCalculation</a>
     */
    public void setWeekNumberCalculation(WeekNumberCalculation calc) {
        Objects.requireNonNull(calc);
        setOption(Option.WEEK_NUMBER_CALCULATION, calc.getClientSideValue(), calc);
    }

    /**
     * Sets the format for week numbers.
     *
     * @param format week number format string
     * @see <a href="https://fullcalendar.io/docs/weekNumberFormat">weekNumberFormat</a>
     */
    public void setWeekNumberFormat(String format) {
        setOption(Option.WEEK_NUMBER_FORMAT, format);
    }

    /**
     * Sets the text that is displayed before week numbers.
     *
     * @param text week text
     * @see <a href="https://fullcalendar.io/docs/weekText">weekText</a>
     */
    public void setWeekText(String text) {
        setOption(Option.WEEK_TEXT, text);
    }

    /**
     * Sets the long version of text that is displayed before week numbers.
     *
     * @param text long week text
     * @see <a href="https://fullcalendar.io/docs/weekTextLong">weekTextLong</a>
     */
    public void setWeekTextLong(String text) {
        setOption(Option.WEEK_TEXT_LONG, text);
    }

    /**
     * Sets the maximum number of columns in the multi-month view.
     *
     * @param columns maximum number of columns
     * @see <a href="https://fullcalendar.io/docs/multiMonthMaxColumns">multiMonthMaxColumns</a>
     */
    public void setMultiMonthMaxColumns(int columns) {
        setOption(Option.MULTI_MONTH_MAX_COLUMNS, columns);
    }

    /**
     * Sets the minimum width (in pixels) for each month in the multi-month view.
     *
     * @param pixels minimum month width
     * @see <a href="https://fullcalendar.io/docs/multiMonthMinWidth">multiMonthMinWidth</a>
     */
    public void setMultiMonthMinWidth(int pixels) {
        setOption(Option.MULTI_MONTH_MIN_WIDTH, pixels);
    }

    /**
     * Sets the title format for each month in the multi-month view.
     *
     * @param format multi-month title format string
     * @see <a href="https://fullcalendar.io/docs/multiMonthTitleFormat">multiMonthTitleFormat</a>
     */
    public void setMultiMonthTitleFormat(String format) {
        setOption(Option.MULTI_MONTH_TITLE_FORMAT, format);
    }

    /**
     * Sets the format for the "month start" date label in the multi-month view.
     *
     * @param format month start format string
     * @see <a href="https://fullcalendar.io/docs/monthStartFormat">monthStartFormat</a>
     */
    public void setMonthStartFormat(String format) {
        setOption(Option.MONTH_START_FORMAT, format);
    }

    /**
     * Sets the default duration for timed events (e.g. {@code "01:00:00"}).
     *
     * @param duration duration string
     * @see <a href="https://fullcalendar.io/docs/defaultTimedEventDuration">defaultTimedEventDuration</a>
     */
    public void setDefaultTimedEventDuration(String duration) {
        setOption("defaultTimedEventDuration", duration);
    }

    /**
     * Sets the default duration for all-day events (e.g. {@code "P1D"}).
     *
     * @param duration duration string
     * @see <a href="https://fullcalendar.io/docs/defaultAllDayEventDuration">defaultAllDayEventDuration</a>
     */
    public void setDefaultAllDayEventDuration(String duration) {
        setOption("defaultAllDayEventDuration", duration);
    }

    // -------------------------------------------------------------------------
    // Typed setters — Display options and render hooks
    // -------------------------------------------------------------------------

    /**
     * Whether to display an event's end time. When {@code false}, only the start time is shown.
     *
     * @param displayEventEnd display end time
     * @see <a href="https://fullcalendar.io/docs/displayEventEnd">displayEventEnd</a>
     */
    public void setDisplayEventEnd(boolean displayEventEnd) {
        setOption(Option.DISPLAY_EVENT_END, displayEventEnd);
    }

    /**
     * Whether the time text is displayed in the event block. Alias for {@link #setDisplayEntryTime(boolean)}.
     *
     * @param display display event time
     * @see <a href="https://fullcalendar.io/docs/displayEventTime">displayEventTime</a>
     */
    public void setDisplayEventTime(boolean display) {
        setDisplayEntryTime(display);
    }

    /**
     * Whether to render events progressively from top to bottom. Useful for reducing initial render cost.
     *
     * @param progressive enable progressive rendering
     * @see <a href="https://fullcalendar.io/docs/progressiveEventRendering">progressiveEventRendering</a>
     */
    public void setProgressiveEventRendering(boolean progressive) {
        setOption(Option.PROGRESSIVE_EVENT_RENDERING, progressive);
    }

    /**
     * The number of milliseconds FC will wait before re-rendering the calendar after a resize or option change.
     * Pass {@code -1} to disable the delay entirely.
     *
     * @param delayMs delay in milliseconds, or {@code -1} to disable
     * @see <a href="https://fullcalendar.io/docs/rerenderDelay">rerenderDelay</a>
     */
    public void setRerenderDelay(int delayMs) {
        setOption(Option.RERENDER_DELAY, delayMs);
    }

    /**
     * Overrides the date used as "now" in the calendar. Affects today highlighting and the now-indicator.
     * Useful for testing or fixed-date scenarios. Pass {@code null} to reset to the actual current date.
     *
     * @param now the fixed "now" date, or {@code null} to reset
     * @see <a href="https://fullcalendar.io/docs/now">now</a>
     */
    public void setNow(LocalDate now) {
        setOption(Option.NOW, now != null ? JsonUtils.formatClientSideDateString(now) : null);
    }

    /**
     * Overrides the datetime used as "now" in the calendar. Affects today highlighting and the now-indicator.
     * Useful for testing or fixed-time scenarios. Pass {@code null} to reset to the actual current time.
     *
     * @param now the fixed "now" datetime, or {@code null} to reset
     * @see <a href="https://fullcalendar.io/docs/now">now</a>
     */
    public void setNow(LocalDateTime now) {
        setOption(Option.NOW, now != null ? now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
    }

    /**
     * Whether the now-indicator snaps to slot boundaries ({@code true}) or renders at the exact current time
     * ({@code false}). Default is {@code false} (exact time).
     *
     * @param snap snap to slot boundaries
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorSnap">nowIndicatorSnap</a>
     */
    public void setNowIndicatorSnap(boolean snap) {
        setOption(Option.NOW_INDICATOR_SNAP, snap);
    }

    /**
     * Sets the initial date displayed when the calendar first renders.
     * <p>
     * <strong>Only effective before the calendar is attached to the UI.</strong>
     * Use {@link #gotoDate(LocalDate)} to navigate after the calendar is attached.
     *
     * @param date initial display date
     * @see <a href="https://fullcalendar.io/docs/initialDate">initialDate</a>
     */
    public void setInitialDate(LocalDate date) {
        setOption(Option.INITIAL_DATE, date != null ? JsonUtils.formatClientSideDateString(date) : null);
    }

    /**
     * Sets the initial view shown when the calendar first renders.
     * <p>
     * <strong>Only effective before the calendar is attached to the UI.</strong>
     * Use {@link #changeView(CalendarView)} to switch views after the calendar is attached.
     *
     * @param view initial calendar view
     * @see <a href="https://fullcalendar.io/docs/initialView">initialView</a>
     */
    public void setInitialView(CalendarView view) {
        setOption(Option.INITIAL_VIEW, view != null ? view.getClientSideValue() : null);
    }

    /**
     * Sets the theme system for calendar styling. Default is {@link ThemeSystem#STANDARD}.
     *
     * @param themeSystem theme system to use
     * @see <a href="https://fullcalendar.io/docs/themeSystem">themeSystem</a>
     */
    public void setThemeSystem(ThemeSystem themeSystem) {
        setOption(Option.THEME_SYSTEM, themeSystem != null ? themeSystem.getClientSideValue() : null);
    }

    /**
     * Sets the separator text used between start and end dates in the default date range format.
     * Default is {@code " \u2013 "} (en dash with spaces).
     *
     * @param separator separator string
     * @see <a href="https://fullcalendar.io/docs/defaultRangeSeparator">defaultRangeSeparator</a>
     */
    public void setDefaultRangeSeparator(String separator) {
        setOption(Option.DEFAULT_RANGE_SEPARATOR, separator);
    }

    /**
     * Sets the separator text used between start and end dates in the toolbar title area.
     * Default is {@code " \u2013 "} (en dash with spaces).
     *
     * @param separator separator string
     * @see <a href="https://fullcalendar.io/docs/titleRangeSeparator">titleRangeSeparator</a>
     */
    public void setTitleRangeSeparator(String separator) {
        setOption(Option.TITLE_RANGE_SEPARATOR, separator);
    }

    /**
     * Customizes the button labels shown in the toolbar. The map keys are FC button/view names such as
     * {@code "today"}, {@code "month"}, {@code "week"}, {@code "day"}, {@code "list"}, {@code "prev"},
     * {@code "next"}, {@code "prevYear"}, {@code "nextYear"}. Pass {@code null} to reset to default labels.
     *
     * @param buttonText map of button key to display label, or {@code null} to reset
     * @see <a href="https://fullcalendar.io/docs/buttonText">buttonText</a>
     */
    public void setButtonText(Map<String, String> buttonText) {
        setOption(Option.BUTTON_TEXT, buttonText);
    }

    /**
     * Sets the date format string used for the day popover title (the popup shown when clicking "+N more").
     * Accepts a FC date format object (JSON string) or a predefined format identifier.
     *
     * @param format date format string
     * @see <a href="https://fullcalendar.io/docs/dayPopoverFormat">dayPopoverFormat</a>
     */
    public void setDayPopoverFormat(String format) {
        setOption(Option.DAY_POPOVER_FORMAT, format);
    }

    /**
     * Limits the maximum number of events shown per day row in month or dayGrid view. Events beyond
     * the limit are hidden behind a "+N more" link. Pass {@code -1} to show all events.
     *
     * @param rows maximum number of event rows
     * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
     * @see #setDayMaxEventRowsFitToCell()
     */
    public void setDayMaxEventRows(int rows) {
        setOption(Option.DAY_MAX_EVENT_ROWS, rows);
    }

    /**
     * Enables automatic fitting of event rows to the available cell height. FC will show as many rows as
     * the cell allows and hide the rest behind a "+N more" link.
     *
     * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
     * @see #setDayMaxEventRows(int)
     */
    public void setDayMaxEventRowsFitToCell() {
        setOption(Option.DAY_MAX_EVENT_ROWS, true);
    }

    /**
     * Sets the delay in milliseconds between the user holding down a touch and a long-press action being
     * triggered for all interactions. Default is {@code 1000} (1 second).
     *
     * @param delayMs delay in milliseconds
     * @see <a href="https://fullcalendar.io/docs/longPressDelay">longPressDelay</a>
     */
    public void setLongPressDelay(int delayMs) {
        setOption(Option.LONG_PRESS_DELAY, delayMs);
    }

    /**
     * Sets the long-press delay in milliseconds specifically for event dragging. Overrides
     * {@link #setLongPressDelay(int)} for event drag interactions on touch devices.
     *
     * @param delayMs delay in milliseconds
     * @see <a href="https://fullcalendar.io/docs/eventLongPressDelay">eventLongPressDelay</a>
     */
    public void setEventLongPressDelay(int delayMs) {
        setOption(Option.EVENT_LONG_PRESS_DELAY, delayMs);
    }

    /**
     * Sets the long-press delay in milliseconds specifically for time-slot selection. Overrides
     * {@link #setLongPressDelay(int)} for selection interactions on touch devices.
     *
     * @param delayMs delay in milliseconds
     * @see <a href="https://fullcalendar.io/docs/selectLongPressDelay">selectLongPressDelay</a>
     */
    public void setSelectLongPressDelay(int delayMs) {
        setOption(Option.SELECT_LONG_PRESS_DELAY, delayMs);
    }

    // ---- Accessibility, Touch, and Print Options ----

    /**
     * Controls whether calendar events are keyboard-focusable and activatable via Enter/Space, even when
     * they have no {@code url}. Defaults to {@code false}.
     *
     * <p>Enable this option in any application that registers
     * {@link #addEntryClickedListener(com.vaadin.flow.component.ComponentEventListener)} and needs to
     * support keyboard-only users (WCAG 2.1 AA, Success Criterion 2.1.1). Once enabled, existing
     * {@code EntryClickedEvent} handlers fire automatically for keyboard activations — no extra
     * event handling is needed.
     *
     * <p>Per-entry override: {@link Entry#setInteractive(Boolean)}.
     *
     * @param interactive {@code true} to make all events keyboard-reachable
     * @see <a href="https://fullcalendar.io/docs/eventInteractive">eventInteractive</a>
     */
    public void setEventInteractive(boolean interactive) {
        setOption(Option.EVENT_INTERACTIVE, interactive);
    }

    /**
     * Sets accessible labels ({@code aria-label} / {@code title}) for the toolbar navigation buttons.
     * The map key is the button name (e.g., {@code "today"}, {@code "prev"}, {@code "next"}) and the
     * value is the hint text for that button.
     *
     * @param hints map of button name → accessible hint text, or {@code null} to reset
     * @see <a href="https://fullcalendar.io/docs/buttonHints">buttonHints</a>
     */
    public void setButtonHints(Map<String, String> hints) {
        setOption(Option.BUTTON_HINTS, hints);
    }

    /**
     * Sets the accessible label for the view selector buttons. The placeholder {@code $0} is replaced
     * at render time with the view name (e.g., "Month", "Week").
     *
     * <p>Example: {@code "Switch to $0 view"}
     *
     * @param hint hint string, optionally containing {@code $0} as a view-name placeholder
     * @see <a href="https://fullcalendar.io/docs/viewHint">viewHint</a>
     */
    public void setViewHint(String hint) {
        setOption(Option.VIEW_HINT, hint);
    }

    /**
     * Sets the accessible label for day/week number navigation links. The placeholder {@code $0} is
     * replaced at render time with the full date text of the link (e.g., "Monday, March 15, 2024"),
     * which is essential for meaningful screen-reader announcements.
     *
     * <p>Requires {@link #setNavLinks(boolean) setNavLinks(true)}.
     * Recommended value: {@code "Go to $0"}.
     *
     * @param hint hint string, optionally containing {@code $0} as a date placeholder
     * @see <a href="https://fullcalendar.io/docs/navLinkHint">navLinkHint</a>
     */
    public void setNavLinkHint(String hint) {
        setOption(Option.NAV_LINK_HINT, hint);
    }

    /**
     * Sets a JavaScript callback that fires when a day navigation link is clicked (instead of
     * the default view change). Requires {@link #setNavLinks(boolean) setNavLinks(true)}.
     * <br><br>
     * The callback receives a single argument: the {@code Date} object of the clicked day.
     * <br><br>
     * <b>Note:</b> No escaping is applied — validate before passing to the client.
     *
     * @param jsFunction JS function string, e.g. {@code "function(date) { alert(date); }"}
     * @see <a href="https://fullcalendar.io/docs/navLinkDayClick">navLinkDayClick</a>
     */
    public void setNavLinkDayClickCallback(String jsFunction) {
        setOption("navLinkDayClick", jsFunction);
    }

    /**
     * Sets a JavaScript callback that fires when a week navigation link is clicked (instead of
     * the default view change). Requires {@link #setNavLinks(boolean) setNavLinks(true)}.
     * <br><br>
     * The callback receives a single argument: the {@code Date} object of the start of the clicked week.
     * <br><br>
     * <b>Note:</b> No escaping is applied — validate before passing to the client.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/navLinkWeekClick">navLinkWeekClick</a>
     */
    public void setNavLinkWeekClickCallback(String jsFunction) {
        setOption("navLinkWeekClick", jsFunction);
    }

    /**
     * Sets the accessible label for the "+N more" overflow link shown when events exceed the visible row
     * limit. The placeholder {@code $0} is replaced at render time with the hidden-event count.
     *
     * <p>Recommended value: {@code "$0 more events. Click to expand"}.
     *
     * @param hint hint string, optionally containing {@code $0} as the hidden-event-count placeholder
     * @see <a href="https://fullcalendar.io/docs/moreLinkHint">moreLinkHint</a>
     */
    public void setMoreLinkHint(String hint) {
        setOption(Option.MORE_LINK_HINT, hint);
    }

    /**
     * Sets the accessible label for close/dismiss buttons (e.g., the popover close button).
     *
     * @param hint hint text
     * @see <a href="https://fullcalendar.io/docs/closeHint">closeHint</a>
     */
    public void setCloseHint(String hint) {
        setOption(Option.CLOSE_HINT, hint);
    }

    /**
     * Sets the accessible label used to describe a time value (e.g., in event time displays).
     *
     * @param hint hint text
     * @see <a href="https://fullcalendar.io/docs/timeHint">timeHint</a>
     */
    public void setTimeHint(String hint) {
        setOption(Option.TIME_HINT, hint);
    }

    /**
     * Sets the accessible label used to announce an event to screen readers (e.g., as a prefix
     * before the event title).
     *
     * @param hint hint text
     * @see <a href="https://fullcalendar.io/docs/eventHint">eventHint</a>
     */
    public void setEventHint(String hint) {
        setOption(Option.EVENT_HINT, hint);
    }

    // ---- Advanced and Niche Options ----



    /**
     * Sets a JavaScript function string as the {@code validRange} option. The function receives
     * the current "now" date and must return an object with {@code start} and/or {@code end}
     * properties, restricting which dates are navigable.
     * <p>
     * Example:
     * <pre>{@code
     * calendar.setValidRangeCallback(
     *     "function(nowDate) { return { " +
     *     "  start: new Date(nowDate.getFullYear(), nowDate.getMonth(), 1), " +
     *     "  end:   new Date(nowDate.getFullYear(), nowDate.getMonth() + 1, 1) " +
     *     "}; }"
     * );
     * }</pre>
     * <p>
     * This overrides any static range set by {@link #setValidRange(java.time.LocalDate, java.time.LocalDate)}.
     *
     * @param jsFunction JavaScript function string; pass {@code null} to clear
     * @see <a href="https://fullcalendar.io/docs/validRange">FC validRange documentation</a>
     */
    public void setValidRangeCallback(String jsFunction) {
        if (jsFunction == null) {
            // Clear by passing null via the standard option path so it is persisted correctly
            setOption(Option.VALID_RANGE, (Object) null);
        } else {
            getElement().callJsFunction("setValidRangeCallback", jsFunction);
        }
    }

    /**
     * Sets a JavaScript function string as the {@code selectOverlap} option. The function
     * receives the existing "still" event and must return {@code true} if the selection may
     * overlap it. This provides per-event control over selection constraints.
     * <p>
     * Example — allow selection only over background events:
     * <pre>{@code
     * calendar.setSelectOverlapCallback(
     *     "function(stillEvent) { return stillEvent.display === 'background'; }"
     * );
     * }</pre>
     * <p>
     * This overrides any boolean value set by {@link #setSelectOverlap(boolean)}.
     *
     * @param jsFunction JavaScript function string; pass {@code null} to clear
     * @see <a href="https://fullcalendar.io/docs/selectOverlap">FC selectOverlap documentation</a>
     */
    public void setSelectOverlapCallback(String jsFunction) {
        if (jsFunction == null) {
            setOption(Option.SELECT_OVERLAP, (Object) null);
        } else {
            getElement().callJsFunction("setSelectOverlapCallback", jsFunction);
        }
    }

    /**
     * Constrains event dragging and resizing to a specific event group id. Only time ranges
     * covered by events with that group id can be used as drop targets.
     *
     * @param groupId event group id to constrain to, or {@code null} to clear
     * @see #setEventConstraint(BusinessHours)
     * @see #setEventConstraintToBusinessHours()
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">FC eventConstraint documentation</a>
     */
    public void setEventConstraint(String groupId) {
        setOption(Option.EVENT_CONSTRAINT, groupId);
    }

    /**
     * Constrains event dragging and resizing to the specified business hours. Events can only
     * be moved to or resized within business hours slots.
     *
     * @param hours business hours definition; must not be null
     * @see #setEventConstraint(String)
     * @see #setEventConstraintToBusinessHours()
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">FC eventConstraint documentation</a>
     */
    public void setEventConstraint(BusinessHours hours) {
        Objects.requireNonNull(hours);
        setOption(Option.EVENT_CONSTRAINT, hours.toJson());
    }

    /**
     * Constrains event dragging and resizing to the calendar's configured business hours.
     * Equivalent to {@code setEventConstraint("businessHours")}.
     *
     * @see #setEventConstraint(String)
     * @see #setEventConstraint(BusinessHours)
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">FC eventConstraint documentation</a>
     */
    public void setEventConstraintToBusinessHours() {
        setOption(Option.EVENT_CONSTRAINT, "businessHours");
    }

    /**
     * Sets how much time is advanced when the user clicks prev/next in the toolbar.
     * Only needed for custom views — built-in views handle their own increment automatically.
     * <p>
     * Example: {@code "P1W"} (one week), {@code "P2W"} (bi-weekly), {@code "P3D"} (3 days).
     *
     * @param duration ISO 8601 duration string; pass {@code null} to reset to FC default
     * @see #setDateAlignment(String)
     * @see <a href="https://fullcalendar.io/docs/dateIncrement">FC dateIncrement documentation</a>
     */
    public void setDateIncrement(String duration) {
        setOption(Option.DATE_INCREMENT, duration);
    }

    /**
     * Sets the date boundary that the calendar snaps to when navigating prev/next.
     * Only needed for custom views.
     * <p>
     * Example values: {@code "week"} (align to week start), {@code "month"}, {@code "day"}.
     *
     * @param alignment alignment boundary string; pass {@code null} to reset to FC default
     * @see #setDateIncrement(String)
     * @see <a href="https://fullcalendar.io/docs/dateAlignment">FC dateAlignment documentation</a>
     */
    public void setDateAlignment(String alignment) {
        setOption(Option.DATE_ALIGNMENT, alignment);
    }

    /**
     * Sets the CSP nonce that FullCalendar will add to all dynamically generated {@code <style>}
     * elements. Required when the application enforces a Content Security Policy with
     * {@code style-src 'nonce-<value>'} and {@code 'unsafe-inline'} is not permitted.
     * <p>
     * The nonce must match the nonce used in the CSP header for this request. If the application
     * uses Vaadin's own nonce mechanism, use the same value here.
     * <p>
     * This option must be set <em>before</em> the calendar is attached (before FC initialises
     * and generates its first {@code <style>} tags). Setting it after attachment has no effect.
     *
     * @param nonce CSP nonce value; pass {@code null} to clear
     * @see <a href="https://fullcalendar.io/docs/contentSecurityPolicy">FC contentSecurityPolicy documentation</a>
     */
    public void setContentSecurityPolicyNonce(String nonce) {
        setOption(Option.CONTENT_SECURITY_POLICY, nonce != null ? Map.of("nonce", nonce) : null);
    }

    /**
     * Sets a view-specific option override. The option applies only when the calendar is
     * showing the specified view type. Multiple calls for the same view type accumulate;
     * each key is set independently.
     * <p>
     * Example — limit event rows only in month view:
     * <pre>{@code
     * calendar.setViewSpecificOption("dayGridMonth", Option.DAY_MAX_EVENT_ROWS, 3);
     * }</pre>
     *
     * @param viewType  FullCalendar view type name (e.g., {@code "dayGrid"}, {@code "timeGrid"},
     *                  {@code "dayGridMonth"}, {@code "listWeek"})
     * @param optionKey option key string
     * @param value     option value; pass {@code null} to remove this key from the view override
     * @throws NullPointerException if viewType or optionKey is null
     * @see #setViewSpecificOption(String, Option, Object)
     * @see #setViewSpecificOption(CalendarView, Option, Object)
     * @see #setViewSpecificOptions(String, Map)
     * @see <a href="https://fullcalendar.io/docs/view-specific-options">FC view-specific options</a>
     */
    public void setViewSpecificOption(String viewType, String optionKey, Object value) {
        Objects.requireNonNull(viewType, "viewType must not be null");
        Objects.requireNonNull(optionKey, "optionKey must not be null");
        ObjectNode viewNode = viewSpecificOptionsMap.computeIfAbsent(viewType, k -> JsonFactory.createObject());
        if (value == null) {
            viewNode.remove(optionKey);
            if (viewNode.isEmpty()) {
                viewSpecificOptionsMap.remove(viewType);
            }
        } else {
            viewNode.set(optionKey, JsonUtils.toJsonNode(value));
        }
        syncViewSpecificOptions();
    }

    /**
     * Sets a view-specific option using the typed {@link Option} enum.
     *
     * @param viewType FullCalendar view type name
     * @param option   option enum constant
     * @param value    option value; pass {@code null} to remove
     * @see #setViewSpecificOption(String, String, Object)
     */
    public void setViewSpecificOption(String viewType, Option option, Object value) {
        Objects.requireNonNull(option, "option must not be null");
        setViewSpecificOption(viewType, option.getOptionKey(), value);
    }

    /**
     * Sets a view-specific option using the typed {@link CalendarView} enum to identify the view.
     *
     * @param view   calendar view
     * @param option option enum constant
     * @param value  option value; pass {@code null} to remove
     * @see #setViewSpecificOption(String, Option, Object)
     */
    public void setViewSpecificOption(CalendarView view, Option option, Object value) {
        Objects.requireNonNull(view, "view must not be null");
        setViewSpecificOption(view.getClientSideValue(), option, value);
    }

    /**
     * Sets multiple view-specific options at once for the given view type. Merges with any
     * existing overrides for that view type; entries in the provided map override existing keys.
     *
     * @param viewType FullCalendar view type name
     * @param options  map of option key → value; null values remove the corresponding key
     * @throws NullPointerException if viewType or options is null
     * @see #setViewSpecificOption(String, String, Object)
     */
    public void setViewSpecificOptions(String viewType, Map<String, Object> options) {
        Objects.requireNonNull(viewType, "viewType must not be null");
        Objects.requireNonNull(options, "options must not be null");
        options.forEach((key, value) -> setViewSpecificOption(viewType, key, value));
    }

    private void syncViewSpecificOptions() {
        if (viewSpecificOptionsMap.isEmpty()) {
            setOption("views", null);
        } else {
            ObjectNode viewsNode = JsonFactory.createObject();
            viewSpecificOptionsMap.forEach(viewsNode::set);
            setOption("views", viewsNode);
        }
    }

    /**
     * Sets the DOM element parent for the drag mirror (ghost element shown during event drag).
     * The value is a JavaScript expression that evaluates to a DOM element.
     * <p>
     * This is necessary when the calendar is inside a CSS-transformed container (e.g., a container
     * with {@code transform: scale()} or {@code transform: translate()}), which would cause the
     * mirror to appear at the wrong position. Setting this to a non-transformed parent (e.g.,
     * {@code "document.body"}) fixes the position.
     * <p>
     * Example:
     * <pre>{@code
     * calendar.setFixedMirrorParent("document.body");
     * calendar.setFixedMirrorParent("document.querySelector('.my-layout')");
     * }</pre>
     *
     * @param jsExpression JS expression that evaluates to a DOM element, or {@code null} to reset
     * @see <a href="https://fullcalendar.io/docs/fixedMirrorParent">FC fixedMirrorParent documentation</a>
     */
    public void setFixedMirrorParent(String jsExpression) {
        if (jsExpression == null) {
            getElement().callJsFunction("setFixedMirrorParent", (Object) null);
        } else {
            getElement().callJsFunction("setFixedMirrorParent", jsExpression);
        }
    }

    /**
     * Sets the CSS selectors of elements that should auto-scroll when the drag mirror
     * approaches their edge. Accepts one or more CSS selector strings; multiple selectors
     * are joined with a comma.
     * <p>
     * Example:
     * <pre>{@code
     * calendar.setDragScrollEls(".my-scroll-container", "body");
     * }</pre>
     *
     * @param cssSelectors CSS selector strings; pass {@code null} or empty to reset
     * @see <a href="https://fullcalendar.io/docs/dragScrollEls">FC dragScrollEls documentation</a>
     */
    public void setDragScrollEls(String... cssSelectors) {
        if (cssSelectors == null || cssSelectors.length == 0) {
            setOption(Option.DRAG_SCROLL_ELS, (Object) null);
        } else {
            setOption(Option.DRAG_SCROLL_ELS, String.join(",", cssSelectors));
        }
    }

    /**
     * Sets the duration in milliseconds of the revert animation when an event drag is cancelled.
     * Default is {@code 500}.
     *
     * @param durationMs duration in milliseconds
     * @see <a href="https://fullcalendar.io/docs/dragRevertDuration">dragRevertDuration</a>
     */
    public void setDragRevertDuration(int durationMs) {
        setOption(Option.DRAG_REVERT_DURATION, durationMs);
    }

    /**
     * Whether to maintain an event's duration when the event is moved between timed and all-day sections.
     * When {@code false}, the event snaps to the default all-day or timed duration. Default is {@code false}.
     *
     * @param maintain maintain duration across timed/all-day boundary
     * @see <a href="https://fullcalendar.io/docs/allDayMaintainDuration">allDayMaintainDuration</a>
     */
    public void setAllDayMaintainDuration(boolean maintain) {
        setOption(Option.ALL_DAY_MAINTAIN_DURATION, maintain);
    }

    /**
     * Sets the minimum number of pixels the user must drag the cursor before FC starts a drag action.
     * Helps avoid accidental drags. Default is {@code 5}.
     *
     * @param pixels minimum drag distance in pixels
     * @see <a href="https://fullcalendar.io/docs/eventDragMinDistance">eventDragMinDistance</a>
     */
    public void setEventDragMinDistance(int pixels) {
        setOption(Option.EVENT_DRAG_MIN_DISTANCE, pixels);
    }

    /**
     * Whether FC will lazily fetch events only for the currently visible range, or eagerly pre-fetch
     * a wider range to support faster navigation. Default is {@code true} (lazy fetching).
     *
     * @param lazy {@code true} to fetch only the visible range (default); {@code false} for wider pre-fetch
     * @see <a href="https://fullcalendar.io/docs/lazyFetching">lazyFetching</a>
     * @see #setPrefetchEnabled(boolean)
     */
    public void setLazyFetching(boolean lazy) {
        setOption(Option.LAZY_FETCHING, lazy);
    }

    /**
     * Whether events without an explicit end date or time should be forced to render with a
     * default duration. When {@code false} (default), events without end are treated as instantaneous.
     *
     * @param force force default duration for events without end
     * @see <a href="https://fullcalendar.io/docs/forceEventDuration">forceEventDuration</a>
     */
    public void setForceEventDuration(boolean force) {
        setOption(Option.FORCE_EVENT_DURATION, force);
    }

    /**
     * Sets the default {@code allDay} value applied to events that do not have an explicit time component.
     * Default is {@code false} (treat as timed events).
     *
     * @param defaultAllDay default all-day flag for events without a time
     * @see <a href="https://fullcalendar.io/docs/defaultAllDay">defaultAllDay</a>
     */
    public void setDefaultAllDay(boolean defaultAllDay) {
        setOption(Option.DEFAULT_ALL_DAY, defaultAllDay);
    }

    // ---- Render hook callbacks (2.2 – 2.11) ----
    // All accept raw JS function strings evaluated via new Function() on the client side.
    // Never pass user-controlled content to these methods.

    /**
     * Sets a JavaScript callback for adding CSS class names to day-cell {@code <td>} elements.
     * The callback receives an info object with {@code date}, {@code dayNumberText}, {@code isToday},
     * {@code isPast}, {@code isFuture}, {@code isOther}, and {@code view} properties, and must return
     * an array of class name strings.
     * <p>
     * Example: {@code "function(info) { return info.isToday ? ['my-today'] : []; }"}
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayCellClassNames">dayCellClassNames</a>
     */
    public void setDayCellClassNamesCallback(String jsFunction) {
        setOption("dayCellClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of day-cell {@code <td>} elements.
     * The callback receives the same info object as {@link #setDayCellClassNamesCallback(String)}
     * and should return a content object (e.g. {@code { html: '...' }}).
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayCellContent">dayCellContent</a>
     */
    public void setDayCellContentCallback(String jsFunction) {
        setOption("dayCellContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after a day-cell element is added to the DOM.
     * The callback receives {@code { el, date, dayNumberText, isToday, isPast, isFuture, isOther, view }}.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayCellDidMount">dayCellDidMount</a>
     */
    public void setDayCellDidMountCallback(String jsFunction) {
        setOption("dayCellDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before a day-cell element is removed from the DOM.
     * The callback receives the same info as {@link #setDayCellDidMountCallback(String)}.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayCellWillUnmount">dayCellWillUnmount</a>
     */
    public void setDayCellWillUnmountCallback(String jsFunction) {
        setOption("dayCellWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to day column header {@code <th>} cells.
     * The callback receives {@code { date, text, isToday, isPast, isFuture, view }} and must return
     * an array of class name strings.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayHeaderClassNames">dayHeaderClassNames</a>
     */
    public void setDayHeaderClassNamesCallback(String jsFunction) {
        setOption("dayHeaderClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of day column header cells.
     * The callback receives {@code { date, text, isToday, isPast, isFuture, view }} and should return
     * a content object.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayHeaderContent">dayHeaderContent</a>
     */
    public void setDayHeaderContentCallback(String jsFunction) {
        setOption("dayHeaderContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after a day column header element is added to the DOM.
     * The callback receives {@code { el, date, text, isToday, isPast, isFuture, view }}.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayHeaderDidMount">dayHeaderDidMount</a>
     */
    public void setDayHeaderDidMountCallback(String jsFunction) {
        setOption("dayHeaderDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before a day column header element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/dayHeaderWillUnmount">dayHeaderWillUnmount</a>
     */
    public void setDayHeaderWillUnmountCallback(String jsFunction) {
        setOption("dayHeaderWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to slot label cells in timegrid/timeline views.
     * The callback receives {@code { date, text, view }} and must return an array of class name strings.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLabelClassNames">slotLabelClassNames</a>
     */
    public void setSlotLabelClassNamesCallback(String jsFunction) {
        setOption("slotLabelClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of time slot label cells.
     * The callback receives {@code { date, text, view }} and should return a content object.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLabelContent">slotLabelContent</a>
     */
    public void setSlotLabelContentCallback(String jsFunction) {
        setOption("slotLabelContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after a slot label element is added to the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLabelDidMount">slotLabelDidMount</a>
     */
    public void setSlotLabelDidMountCallback(String jsFunction) {
        setOption("slotLabelDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before a slot label element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLabelWillUnmount">slotLabelWillUnmount</a>
     */
    public void setSlotLabelWillUnmountCallback(String jsFunction) {
        setOption("slotLabelWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to slot lane cells in timegrid/timeline views.
     * The callback receives {@code { date, time, view }} and must return an array of class name strings.
     * The {@code time} property is a duration object relative to start of day.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLaneClassNames">slotLaneClassNames</a>
     */
    public void setSlotLaneClassNamesCallback(String jsFunction) {
        setOption("slotLaneClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of slot lane cells.
     * The callback receives {@code { date, time, view }} and should return a content object.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLaneContent">slotLaneContent</a>
     */
    public void setSlotLaneContentCallback(String jsFunction) {
        setOption("slotLaneContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after a slot lane element is added to the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLaneDidMount">slotLaneDidMount</a>
     */
    public void setSlotLaneDidMountCallback(String jsFunction) {
        setOption("slotLaneDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before a slot lane element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/slotLaneWillUnmount">slotLaneWillUnmount</a>
     */
    public void setSlotLaneWillUnmountCallback(String jsFunction) {
        setOption("slotLaneWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to the view root element.
     * The callback receives {@code { view, el }} and must return an array of class name strings.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/viewClassNames">viewClassNames</a>
     */
    public void setViewClassNamesCallback(String jsFunction) {
        setOption("viewClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after the view root element is added to the DOM.
     * This is the client-side equivalent of the server-side {@code ViewSkeletonRenderedEvent}; both
     * can be used simultaneously.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/viewDidMount">viewDidMount</a>
     */
    public void setViewDidMountCallback(String jsFunction) {
        setOption("viewDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before the view root element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/viewWillUnmount">viewWillUnmount</a>
     */
    public void setViewWillUnmountCallback(String jsFunction) {
        setOption("viewWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to the now-indicator element.
     * The callback receives {@code { date, isAxis, view }} and must return an array of class name strings.
     * {@code isAxis} is {@code true} for the time-label part and {@code false} for the line.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorClassNames">nowIndicatorClassNames</a>
     */
    public void setNowIndicatorClassNamesCallback(String jsFunction) {
        setOption("nowIndicatorClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of the now-indicator.
     * The callback receives {@code { date, isAxis, view }} and should return a content object.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorContent">nowIndicatorContent</a>
     */
    public void setNowIndicatorContentCallback(String jsFunction) {
        setOption("nowIndicatorContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after the now-indicator element is added to the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorDidMount">nowIndicatorDidMount</a>
     */
    public void setNowIndicatorDidMountCallback(String jsFunction) {
        setOption("nowIndicatorDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before the now-indicator element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorWillUnmount">nowIndicatorWillUnmount</a>
     */
    public void setNowIndicatorWillUnmountCallback(String jsFunction) {
        setOption("nowIndicatorWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to week-number cells.
     * The callback receives {@code { date, num, text, view }} and must return an array of class name strings.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/weekNumberClassNames">weekNumberClassNames</a>
     */
    public void setWeekNumberClassNamesCallback(String jsFunction) {
        setOption("weekNumberClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of week-number cells.
     * The callback receives {@code { date, num, text, view }} and should return a content object.
     * Example: {@code "function(info) { return { html: '<span title=\"Week ' + info.num + '\">' + info.text + '</span>' }; }"}
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/weekNumberContent">weekNumberContent</a>
     */
    public void setWeekNumberContentCallback(String jsFunction) {
        setOption("weekNumberContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after a week-number element is added to the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/weekNumberDidMount">weekNumberDidMount</a>
     */
    public void setWeekNumberDidMountCallback(String jsFunction) {
        setOption("weekNumberDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before a week-number element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/weekNumberWillUnmount">weekNumberWillUnmount</a>
     */
    public void setWeekNumberWillUnmountCallback(String jsFunction) {
        setOption("weekNumberWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to the "+N more" link element.
     * The callback receives {@code { num, text, shortText, view }} and must return an array of class name strings.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/moreLinkClassNames">moreLinkClassNames</a>
     */
    public void setMoreLinkClassNamesCallback(String jsFunction) {
        setOption("moreLinkClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of the "+N more" link.
     * The callback receives {@code { num, text, shortText, view }} and should return a content object.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/moreLinkContent">moreLinkContent</a>
     */
    public void setMoreLinkContentCallback(String jsFunction) {
        setOption("moreLinkContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after the "+N more" link element is added to the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/moreLinkDidMount">moreLinkDidMount</a>
     */
    public void setMoreLinkDidMountCallback(String jsFunction) {
        setOption("moreLinkDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before the "+N more" link element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/moreLinkWillUnmount">moreLinkWillUnmount</a>
     */
    public void setMoreLinkWillUnmountCallback(String jsFunction) {
        setOption("moreLinkWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to the "no events" message element in list view.
     * The callback receives {@code { view }} and must return an array of class name strings.
     * The message text is controlled separately by the {@code noEventsText} option.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/noEventsClassNames">noEventsClassNames</a>
     */
    public void setNoEventsClassNamesCallback(String jsFunction) {
        setOption("noEventsClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of the "no events" message element in list view.
     * The callback receives {@code { view }} and should return a content object.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/noEventsContent">noEventsContent</a>
     */
    public void setNoEventsContentCallback(String jsFunction) {
        setOption("noEventsContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after the "no events" message element is added to the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/noEventsDidMount">noEventsDidMount</a>
     */
    public void setNoEventsDidMountCallback(String jsFunction) {
        setOption("noEventsDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before the "no events" message element is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/noEventsWillUnmount">noEventsWillUnmount</a>
     */
    public void setNoEventsWillUnmountCallback(String jsFunction) {
        setOption("noEventsWillUnmount", jsFunction);
    }

    /**
     * Sets a JavaScript callback for adding CSS class names to the all-day section header cell in timegrid views.
     * The callback receives {@code { text, view }} and must return an array of class name strings.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/allDayClassNames">allDayClassNames</a>
     */
    public void setAllDayClassNamesCallback(String jsFunction) {
        setOption("allDayClassNames", jsFunction);
    }

    /**
     * Sets a JavaScript callback for customizing the content of the all-day section header cell in timegrid views.
     * The callback receives {@code { text, view }} and should return a content object.
     * Example: {@code "function(info) { return { text: 'Todo el día' }; }"}
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/allDayContent">allDayContent</a>
     */
    public void setAllDayContentCallback(String jsFunction) {
        setOption("allDayContent", jsFunction);
    }

    /**
     * Sets a JavaScript callback called after the all-day section header cell is added to the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/allDayDidMount">allDayDidMount</a>
     */
    public void setAllDayDidMountCallback(String jsFunction) {
        setOption("allDayDidMount", jsFunction);
    }

    /**
     * Sets a JavaScript callback called just before the all-day section header cell is removed from the DOM.
     *
     * @param jsFunction JS function string
     * @see <a href="https://fullcalendar.io/docs/allDayWillUnmount">allDayWillUnmount</a>
     */
    public void setAllDayWillUnmountCallback(String jsFunction) {
        setOption("allDayWillUnmount", jsFunction);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the current slotMinTime. Default is {@code "00:00:00"}.
     *
     * @return slot min time string
     */
    public String getSlotMinTime() {
        return (String) getOption(Option.SLOT_MIN_TIME).orElse("00:00:00");
    }

    /**
     * Returns the current slotMaxTime. Default is {@code "24:00:00"}.
     *
     * @return slot max time string
     */
    public String getSlotMaxTime() {
        return (String) getOption(Option.SLOT_MAX_TIME).orElse("24:00:00");
    }

    /**
     * Returns the current snapDuration if set, otherwise empty.
     *
     * @return optional snap duration string
     */
    public Optional<String> getSnapDuration() {
        return getOption(Option.SNAP_DURATION);
    }

    /**
     * Returns the first day of the week. Defaults to {@link DayOfWeek#MONDAY} when not set.
     *
     * @return first day of week
     */
    public DayOfWeek getFirstDay() {
        return (DayOfWeek) getOption(Option.FIRST_DAY).orElse(DayOfWeek.MONDAY);
    }

    /**
     * Returns whether the all-day slot is shown. Default is {@code true}.
     *
     * @return all day slot
     */
    public boolean isAllDaySlot() {
        return (Boolean) getOption(Option.ALL_DAY_SLOT).orElse(true);
    }

    /**
     * Returns whether time-grid rows expand to fill available height. Default is {@code false}.
     *
     * @return expand rows
     */
    public boolean isExpandRows() {
        return (Boolean) getOption(Option.EXPAND_ROWS).orElse(false);
    }

    /**
     * Returns whether sticky header dates are enabled. Default is {@code false}.
     *
     * @return sticky header dates
     */
    public boolean isStickyHeaderDates() {
        return (Boolean) getOption(Option.STICKY_HEADER_DATES).orElse(false);
    }

    /**
     * Returns whether the now indicator is shown. Default is {@code false}.
     *
     * @return now indicator
     */
    public boolean isNowIndicator() {
        return (Boolean) getOption(Option.NOW_INDICATOR).orElse(false);
    }

    /**
     * Returns whether timeslots are selectable. Default is {@code false}.
     *
     * @return selectable
     */
    public boolean isSelectable() {
        return (Boolean) getOption(Option.SELECTABLE).orElse(false);
    }

    /**
     * Returns whether nav links (day/week number clicking) are enabled. Default is {@code false}.
     *
     * @return nav links
     */
    public boolean isNavLinks() {
        return (Boolean) getOption(Option.NAV_LINKS).orElse(false);
    }

    /**
     * Returns whether day headers are shown. Default is {@code true}.
     *
     * @return day headers
     */
    public boolean isDayHeaders() {
        return (Boolean) getOption(Option.DAY_HEADERS).orElse(true);
    }

    /**
     * Returns whether slot event overlap is enabled. Default is {@code true}.
     *
     * @return slot entry overlap
     */
    public boolean isSlotEntryOverlap() {
        return (Boolean) getOption(Option.SLOT_ENTRY_OVERLAP).orElse(true);
    }

    /**
     * Returns the minimum day width in pixels. Default is {@code 0}.
     *
     * @return day min width
     */
    public int getDayMinWidth() {
        return (Integer) getOption(Option.DAY_MIN_WIDTH).orElse(0);
    }

    /**
     * Returns the maximum number of columns in the multi-month view. Default is {@code 3}.
     *
     * @return multi month max columns
     */
    public int getMultiMonthMaxColumns() {
        return (Integer) getOption(Option.MULTI_MONTH_MAX_COLUMNS).orElse(3);
    }

    /**
     * Returns the maximum event stack count if set, otherwise empty.
     *
     * @return optional entry max stack
     */
    public Optional<Integer> getEntryMaxStack() {
        return getOption(Option.ENTRY_MAX_STACK);
    }

    /**
     * Returns the content height as an integer if set, otherwise empty.
     *
     * @return optional content height
     */
    public Optional<Integer> getContentHeight() {
        return getOption(Option.CONTENT_HEIGHT);
    }

    /**
     * Returns the aspect ratio if set, otherwise empty.
     *
     * @return optional aspect ratio
     */
    public Optional<Double> getAspectRatio() {
        return getOption(Option.ASPECT_RATIO);
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
        WEEK_TEXT_LONG,



        /**
         * @see <a href="https://fullcalendar.io/docs/allDayMaintainDuration">allDayMaintainDuration</a>
         */
        ALL_DAY_MAINTAIN_DURATION,

        /**
         * @see <a href="https://fullcalendar.io/docs/buttonText">buttonText</a>
         */
        BUTTON_TEXT,

        /**
         * @see <a href="https://fullcalendar.io/docs/defaultAllDay">defaultAllDay</a>
         */
        DEFAULT_ALL_DAY,

        /**
         * @see <a href="https://fullcalendar.io/docs/defaultRangeSeparator">defaultRangeSeparator</a>
         */
        DEFAULT_RANGE_SEPARATOR,

        /**
         * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
         */
        DAY_MAX_EVENT_ROWS,

        /**
         * @see <a href="https://fullcalendar.io/docs/dayPopoverFormat">dayPopoverFormat</a>
         */
        DAY_POPOVER_FORMAT,

        /**
         * @see <a href="https://fullcalendar.io/docs/displayEventEnd">displayEventEnd</a>
         */
        DISPLAY_EVENT_END,

        /**
         * @see <a href="https://fullcalendar.io/docs/dragRevertDuration">dragRevertDuration</a>
         */
        DRAG_REVERT_DURATION,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventDragMinDistance">eventDragMinDistance</a>
         */
        EVENT_DRAG_MIN_DISTANCE,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventLongPressDelay">eventLongPressDelay</a>
         */
        EVENT_LONG_PRESS_DELAY,

        /**
         * @see <a href="https://fullcalendar.io/docs/forceEventDuration">forceEventDuration</a>
         */
        FORCE_EVENT_DURATION,


        /**
         * @see <a href="https://fullcalendar.io/docs/initialDate">initialDate</a>
         */
        INITIAL_DATE,

        /**
         * @see <a href="https://fullcalendar.io/docs/initialView">initialView</a>
         */
        INITIAL_VIEW,

        /**
         * @see <a href="https://fullcalendar.io/docs/lazyFetching">lazyFetching</a>
         */
        LAZY_FETCHING,

        /**
         * @see <a href="https://fullcalendar.io/docs/longPressDelay">longPressDelay</a>
         */
        LONG_PRESS_DELAY,

        /**
         * @see <a href="https://fullcalendar.io/docs/now">now</a>
         */
        NOW,

        /**
         * @see <a href="https://fullcalendar.io/docs/nowIndicatorSnap">nowIndicatorSnap</a>
         */
        NOW_INDICATOR_SNAP,

        /**
         * @see <a href="https://fullcalendar.io/docs/progressiveEventRendering">progressiveEventRendering</a>
         */
        PROGRESSIVE_EVENT_RENDERING,

        /**
         * @see <a href="https://fullcalendar.io/docs/rerenderDelay">rerenderDelay</a>
         */
        RERENDER_DELAY,

        /**
         * @see <a href="https://fullcalendar.io/docs/selectLongPressDelay">selectLongPressDelay</a>
         */
        SELECT_LONG_PRESS_DELAY,

        /**
         * @see <a href="https://fullcalendar.io/docs/themeSystem">themeSystem</a>
         */
        THEME_SYSTEM,

        /**
         * @see <a href="https://fullcalendar.io/docs/titleRangeSeparator">titleRangeSeparator</a>
         */
        TITLE_RANGE_SEPARATOR,




        /**
         * @see <a href="https://fullcalendar.io/docs/selectAllow">selectAllow</a>
         */
        SELECT_ALLOW,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventAllow">eventAllow</a>
         */
        EVENT_ALLOW,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
         */
        EVENT_OVERLAP,

        /**
         * @see <a href="https://fullcalendar.io/docs/droppable">droppable</a>
         */
        DROPPABLE,

        /**
         * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
         */
        DROP_ACCEPT,



        /**
         * @see <a href="https://fullcalendar.io/docs/startParam">startParam</a>
         */
        START_PARAM,

        /**
         * @see <a href="https://fullcalendar.io/docs/endParam">endParam</a>
         */
        END_PARAM,

        /**
         * @see <a href="https://fullcalendar.io/docs/timeZoneParam">timeZoneParam</a>
         */
        TIME_ZONE_PARAM,

        /**
         * @see <a href="https://fullcalendar.io/docs/googleCalendarApiKey">googleCalendarApiKey</a>
         */
        GOOGLE_CALENDAR_API_KEY,



        /**
         * @see <a href="https://fullcalendar.io/docs/eventInteractive">eventInteractive</a>
         */
        EVENT_INTERACTIVE,



        /**
         * @see <a href="https://fullcalendar.io/docs/eventConstraint">eventConstraint</a>
         */
        EVENT_CONSTRAINT,

        /**
         * @see <a href="https://fullcalendar.io/docs/dateIncrement">dateIncrement</a>
         */
        DATE_INCREMENT,

        /**
         * @see <a href="https://fullcalendar.io/docs/dateAlignment">dateAlignment</a>
         */
        DATE_ALIGNMENT,

        /**
         * @see <a href="https://fullcalendar.io/docs/contentSecurityPolicy">contentSecurityPolicy</a>
         */
        CONTENT_SECURITY_POLICY,

        /**
         * @see <a href="https://fullcalendar.io/docs/dragScrollEls">dragScrollEls</a>
         */
        DRAG_SCROLL_ELS,

        /**
         * @see <a href="https://fullcalendar.io/docs/buttonHints">buttonHints</a>
         */
        BUTTON_HINTS,

        /**
         * @see <a href="https://fullcalendar.io/docs/viewHint">viewHint</a>
         */
        VIEW_HINT,

        /**
         * @see <a href="https://fullcalendar.io/docs/navLinkHint">navLinkHint</a>
         */
        NAV_LINK_HINT,

        /**
         * @see <a href="https://fullcalendar.io/docs/moreLinkHint">moreLinkHint</a>
         */
        MORE_LINK_HINT,

        /**
         * @see <a href="https://fullcalendar.io/docs/closeHint">closeHint</a>
         */
        CLOSE_HINT,

        /**
         * @see <a href="https://fullcalendar.io/docs/timeHint">timeHint</a>
         */
        TIME_HINT,

        /**
         * @see <a href="https://fullcalendar.io/docs/eventHint">eventHint</a>
         */
        EVENT_HINT;

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

    /**
     * Creates a bounded LinkedHashMap for caching entries.
     * This is a separate method to avoid type name conflicts with java.util.Map.Entry.
     */
    private static Map<String, Entry> createBoundedEntryCache() {
        return new LinkedHashMap<String, org.vaadin.stefan.fullcalendar.Entry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, org.vaadin.stefan.fullcalendar.Entry> eldest) {
                return size() > MAX_CACHED_ENTRIES;
            }
        };
    }

}

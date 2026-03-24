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
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.vaadin.stefan.fullcalendar.CustomCalendarView.AnonymousCustomCalendarView;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.model.Footer;
import org.vaadin.stefan.fullcalendar.model.Header;

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

    private static Map<String, Entry> createBoundedEntryCache() {
        return new BoundedEntryCache();
    }

    private static final class BoundedEntryCache extends LinkedHashMap<String, Entry> {
        BoundedEntryCache() {
            super(16, 0.75f, false);
        }

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > MAX_CACHED_ENTRIES;
        }
    }

    private final Map<String, Serializable> options = new HashMap<>();

    /**
     * A map for options, that have been set before the attachment. They are mapped here instead of the options
     * map to allow a correct init of the client on reattachment plus have something to return in getOption.
     * The main reason are options, that have to be set before attachment, but must not "bleed" into the option
     * map, like eventContent
     */
    private final Map<String, Serializable> initialOptions = new HashMap<>();
    private final Map<String, Object> serverSideOptions = new HashMap<>();

    private EntryProvider<? extends Entry> entryProvider;
    private final List<Registration> entryProviderDataListeners = new LinkedList<>();

    private final Map<String, CustomCalendarView> customCalendarViews = new LinkedHashMap<>();

    /**
     * Registry of client-side event sources added via {@link #addEventSource(ClientSideEventSource)}.
     * Keyed by source id. Used for reattach restoration.
     */
    private final Map<String, ClientSideEventSource<?>> clientSideEventSourceRegistry = new LinkedHashMap<>();

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
    private volatile JsCallback userEntryDidMountCallback;

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefault()}).
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
     * Sets the locale to {@link CalendarLocale#getDefault()}
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
    public FullCalendar(@NotNull JsonObject initialOptions) {
        if (initialOptions.hasKey("views")) {
            JsonObject views = initialOptions.getObject("views");

            // register custom views mentioned in the initial options
            for (String viewName : views.keys()) {
                // only register anonmyous views, if there is no real registered variant
                JsonObject viewSettings = views.getObject(viewName);
                AnonymousCustomCalendarView anonymousView = new AnonymousCustomCalendarView(viewName, viewSettings);
                this.customCalendarViews.put(anonymousView.getClientSideValue(), anonymousView);
            }
        }

        this.getElement().setPropertyJson(JSON_INITIAL_OPTIONS, Objects.requireNonNull(initialOptions));

        if (!initialOptions.hasKey(Option.LOCALE.getOptionKey())) {
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
                    JsonObject optionsJson = Json.createObject();
                    if (!options.isEmpty()) {
                        options.forEach((key, value) -> optionsJson.put(key, JsonUtils.toJsonValue(value)));
                    }

                    getElement().callJsFunction("restoreStateFromServer",
                            optionsJson,
                            JsonUtils.toJsonValue(currentViewName),
                            JsonUtils.toJsonValue(currentIntervalStart));

                    // restore client-side event sources after reattach
                    if (!clientSideEventSourceRegistry.isEmpty()) {
                        JsonArray sourcesArray = Json.createArray();
                        int i = 0;
                        for (ClientSideEventSource<?> source : clientSideEventSourceRegistry.values()) {
                            sourcesArray.set(i++, source.toJson());
                        }
                        getElement().callJsFunction("restoreEventSources", sourcesArray);
                    }

                });
            });
        }

        applyEntryDidMountMerge();
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
    public void setEntryProvider(@NotNull EntryProvider<? extends Entry> entryProvider) {
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
    protected void requestRefresh(@NotNull Entry item) {
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
    protected JsonArray fetchEntriesFromServer(@NotNull JsonObject query) {
        Objects.requireNonNull(query);
        Objects.requireNonNull(entryProvider);

        lastFetchedEntries.clear();

        LocalDateTime start = query.hasKey("start") ? JsonUtils.parseClientSideDateTime(query.getString("start")) : null;
        LocalDateTime end = query.hasKey("end") ? JsonUtils.parseClientSideDateTime(query.getString("end")) : null;

        JsonArray array = Json.createArray();
        entryProvider.fetch(new EntryQuery(start, end, EntryQuery.AllDay.BOTH))
                .peek(entry -> {
                    entry.setCalendar(this);
                    entry.setKnownToTheClient(true); // mark entry as "has been sent to client"
                    lastFetchedEntries.put(entry.getId(), entry);
                })
                .map(Entry::toJson)
                .forEach(jsonObject -> array.set(array.length(), jsonObject));

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
    public void changeView(@NotNull CalendarView view) {
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
     * Returns the start of the currently displayed date interval (e.g., the first day of the
     * displayed month in month view). Updated after each render via {@code DatesRenderedEvent}.
     * <p>
     * Note: The value lags by one server round-trip — it reflects the state after the last
     * {@code datesSet} event was processed.
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
     * Switch to the interval containing the given date (e. g. to month "October" if the "15th October ..." is passed).
     *
     * @param date date to goto
     * @throws NullPointerException when null is passed
     */
    public void gotoDate(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        getElement().callJsFunction("gotoDate", date.toString());
    }
    
    /**
     * Programatically scroll the current view to the given time in the format `hh:mm:ss.sss`, `hh:mm:sss` or `hh:mm`. For example, '05:00' signifies 5 hours.
     * 
     * @param duration duration
     * @throws NullPointerException when null is passed
     */
    public void scrollToTime(@NotNull String duration) {
        Objects.requireNonNull(duration);	// No format check, it is already done in the calendar code
        getElement().callJsFunction("scrollToTime", duration);
    }
    
    /**
     * Programatically scroll the current view to the given time in the format `hh:mm:ss.sss`, `hh:mm:sss` or `hh:mm`. For example, '05:00' signifies 5 hours.
     * 
     * @param duration duration
     * @throws NullPointerException when null is passed
     */
    public void scrollToTime(@NotNull LocalTime duration) {
        Objects.requireNonNull(duration);
        getElement().callJsFunction("scrollToTime", duration.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option.
     * <br><br>
     * Accepts any value including {@link JsCallback} instances for JavaScript callback options.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(@NotNull Option option, Object value) {
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
    public void setOption(@NotNull Option option, Object value, Object valueForServerSide) {
        callOptionUpdate(option.getOptionKey(), value, valueForServerSide, "setOption");
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
        setOption(option, value, null);
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
        callOptionUpdate(option, value, valueForServerSide, "setOption");
    }

    private void callOptionUpdate(@NotNull String option, Object value, Object valueForServerSide, String method, Serializable... additionalParameters) {
        Objects.requireNonNull(option);

        // 1. ENTRY_DID_MOUNT intercept: detect this key and route to merge logic
        if (Option.ENTRY_DID_MOUNT.getOptionKey().equals(option)) {
            if (value instanceof JsCallback) {
                userEntryDidMountCallback = (JsCallback) value;
            } else {
                userEntryDidMountCallback = null;
            }
            applyEntryDidMountMerge();
            return;
        }

        // 2. Convert JsCallback to marker JSON before the attached/not-attached split.
        //    Preserve original JsCallback for getOption() round-trip via serverSideOptions.
        if (value instanceof JsCallback) {
            JsCallback cb = (JsCallback) value;
            if (valueForServerSide == null) {
                valueForServerSide = cb;
            }
            value = cb.toMarkerJson();
        }

        // 3. Auto-convert ClientSideValue implementations to their client-side string,
        //    keeping the original typed object for server-side getOption() retrieval.
        if (value instanceof ClientSideValue) {
            ClientSideValue csv = (ClientSideValue) value;
            if (valueForServerSide == null) {
                valueForServerSide = value;
            }
            value = csv.getClientSideValue();
        }

        boolean attached = isAttached();

        if (value == null) {
            initialOptions.remove(option);
            options.remove(option);
            serverSideOptions.remove(option);
        } else {
            Serializable serializableValue = value instanceof Serializable ? (Serializable) value : value.toString();
            if (attached) {
                options.put(option, serializableValue);
            } else {
                initialOptions.put(option, serializableValue);
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
            JsonObject initialOptions = (JsonObject) getElement().getPropertyRaw("initialOptions");
            if (initialOptions == null) {
                initialOptions = Json.createObject();
                getElement().setPropertyJson("initialOptions", initialOptions);
            }

            if (value == null) {
                initialOptions.remove(option);
            } else {
                initialOptions.put(option, JsonUtils.toJsonValue(value));
            }
        }
    }

    private void applyEntryDidMountMerge() {
        String merged = buildEntryDidMountMerged();
        if (merged != null) {
            getElement().callJsFunction("setOption", "eventDidMount", JsCallback.of(merged).toMarkerJson());
        } else {
            getElement().callJsFunction("setOption", "eventDidMount", (Serializable) null);
        }
    }

    /**
     * Builds the merged eventDidMount function string from the user callback and native event listeners.
     * Package-private for testing.
     */
    String buildEntryDidMountMerged() {
        String userCallback = userEntryDidMountCallback != null ? userEntryDidMountCallback.getJsFunction() : null;

        if (customNativeEventsMap.isEmpty()) {
            return userCallback;
        }

        StringBuilder nativeEvents = new StringBuilder();
        for (Map.Entry<String, String> entry : customNativeEventsMap.entrySet()) {
            nativeEvents.append("info.el.addEventListener('")
                    .append(entry.getKey())
                    .append("', ")
                    .append(entry.getValue())
                    .append(");\n");
        }

        if (userCallback != null) {
            // Inject native event listeners before the closing brace of the user callback
            int closingBrace = userCallback.lastIndexOf("}");
            if (closingBrace >= 0) {
                return userCallback.substring(0, closingBrace) + nativeEvents + userCallback.substring(closingBrace);
            }
            // Fallback: append after user callback
            return userCallback + "\nfunction(info) {\n" + nativeEvents + "}";
        } else {
            return "function(info) {\n" + nativeEvents + "}";
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
    public void setFirstDay(@NotNull DayOfWeek firstDay) {
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
    @Deprecated
    public void setHeight(int heightInPixels) {
        setHeight(heightInPixels, Unit.PIXELS);
    }

    /**
     * Sets the calendar's height to be calculated from parents height. Please be aware, that a block parent with
     * relative height (e. g. 100%) might not work properly. In this case use flex layout or set a fixed height for
     * the parent or the calendar.
     * @deprecated Use {@link #setHeight(String)} or {@link #setHeight(float, Unit)} instead
     */
    @Deprecated
    public void setHeightByParent() {
        setHeight("100%");
    }

    /**
     * Sets the calendar's height to be calculated automatically. In current implementation this means by the calendars
     * width-height-ratio.
     * @deprecated Use {@link #setHeight(String)} or {@link #setHeight(float, Unit)} instead
     */
    @Deprecated
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
    @Deprecated
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
    public void setLocale(@NotNull Locale locale) {
        Objects.requireNonNull(locale);
        setOption(Option.LOCALE, toClientSideLocale(locale), locale);
    }

    protected String toClientSideLocale(@NotNull Locale locale) {
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_CLASS_NAMES} and {@link JsCallback} instead.
     */
    @Deprecated(since = "6.4.0")
    public void setEntryClassNamesCallback(String s) {
        setOption(Option.ENTRY_CLASS_NAMES, JsCallback.of(s));
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
     * If you also setup native event listeners, then these will automatically be attached to the end of your custom
     * callback function (must end with a closing bracket "}").
     *
     * @param s function to be attached
     * @see #addEntryNativeEventListener(String, String)
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_DID_MOUNT} and {@link JsCallback} instead.
     */
    @Deprecated(since = "6.4.0")
    public void setEntryDidMountCallback(String s) {
        setOption(Option.ENTRY_DID_MOUNT, JsCallback.of(s));
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
     * Native event listeners are merged into the {@code eventDidMount} callback and attached to each
     * entry element automatically on render. You may also provide a custom {@code eventDidMount}
     * function via {@link #setOption(Option, Object)} with {@link Option#ENTRY_DID_MOUNT} and {@link JsCallback}
     * — native event listener registration will automatically be merged into that callback.
     * <br><br>
     * Inside the callback you may access the entry itself (using {@code info.event}) or the html element
     * (using {@code info.el}).
     * @see #setEntryDidMountCallback(String)
     * @param eventName javascript event name
     * @param eventCallback javascript event callback to be hooked to the event
     */
    public void addEntryNativeEventListener(String eventName, String eventCallback) {
        customNativeEventsMap.put(eventName, eventCallback);
        applyEntryDidMountMerge();
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_WILL_UNMOUNT} and {@link JsCallback} instead.
     */
    @Deprecated(since = "6.4.0")
    public void setEntryWillUnmountCallback(String s) {
        setOption(Option.ENTRY_WILL_UNMOUNT, JsCallback.of(s));
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_CONTENT} and {@link JsCallback} instead.
     */
    @Deprecated(since = "6.4.0")
    public void setEntryContentCallback(String s) {
        setOption(Option.ENTRY_CONTENT, JsCallback.of(s));
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
     * @param slotMinTime slotMinTime to set
     * @throws NullPointerException when null is passed
     */
    public void setSlotMinTime(@NotNull LocalTime slotMinTime) {
        Objects.requireNonNull(slotMinTime);
        setOption(Option.SLOT_MIN_TIME, JsonUtils.toJsonValue(slotMinTime != null ? slotMinTime : "00:00:00"));
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
    public void setSlotMaxTime(@NotNull LocalTime slotMaxTime) {
        Objects.requireNonNull(slotMaxTime);
        setOption(Option.SLOT_MAX_TIME, JsonUtils.toJsonValue(slotMaxTime != null ? slotMaxTime : "24:00:00"));
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
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setDurationEditable(boolean)}
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
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setStartEditable(boolean)}
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
     * <br><br>
     * Returns {@code null} for initial options. Please use #getRawOption(String)
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(@NotNull String option, boolean forceClientSideValue) {
        Objects.requireNonNull(option);
        if (!forceClientSideValue && serverSideOptions.containsKey(option)) {
            return Optional.ofNullable((T) serverSideOptions.get(option));
        }

        Serializable value = options.get(option);
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
     * Registers a listener to be informed when the user mouses over an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryMouseEnterListener(@NotNull ComponentEventListener<EntryMouseEnterEvent> listener) {
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
    public Registration addEntryMouseLeaveListener(@NotNull ComponentEventListener<EntryMouseLeaveEvent> listener) {
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
     * Registers a listener to be informed when a view skeleton rendered event occurred. This happens, when
     * the view has been rendered (intially or after a view change).
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addViewSkeletonRenderedListener(@NotNull ComponentEventListener<ViewSkeletonRenderedEvent> listener) {
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
    public Registration addViewChangedListener(@NotNull ComponentEventListener<ViewSkeletonRenderedEvent> listener) {
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
    public Registration addTimeslotsSelectedListener(@NotNull ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
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
    public Registration addMoreLinkClickedListener(@NotNull ComponentEventListener<MoreLinkClickedEvent> listener) {
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

    /**
     * Registers a listener for when the user begins dragging an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addEntryDragStartListener(ComponentEventListener<EntryDragStartEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDragStartEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Registers a listener for when the user stops dragging an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addEntryDragStopListener(ComponentEventListener<EntryDragStopEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDragStopEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Registers a listener for when the user begins resizing an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addEntryResizeStartListener(ComponentEventListener<EntryResizeStartEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizeStartEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Registers a listener for when the user stops resizing an entry.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addEntryResizeStopListener(ComponentEventListener<EntryResizeStopEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizeStopEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Registers a listener for when an external element carrying event data has been dropped onto the calendar.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addEntryReceiveListener(ComponentEventListener<EntryReceiveEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryReceiveEvent.class, listener);
    }

    /**
     * Registers a listener for when a timeslot selection is cleared.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addTimeslotsUnselectListener(ComponentEventListener<TimeslotsUnselectEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsUnselectEvent.class, listener);
    }

    /**
     * Registers a listener for when an entry from a client-managed event source is dropped.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addExternalEntryDroppedListener(ComponentEventListener<ExternalEntryDroppedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(ExternalEntryDroppedEvent.class, listener);
    }

    /**
     * Registers a listener for when an entry from a client-managed event source is resized.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addExternalEntryResizedListener(ComponentEventListener<ExternalEntryResizedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(ExternalEntryResizedEvent.class, listener);
    }

    /**
     * Registers a listener for when an external DOM element is dropped onto the calendar.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addDropListener(ComponentEventListener<DropEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DropEvent.class, listener);
    }

    /**
     * Registers a listener for when a client-managed event source fails to load.
     *
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addEventSourceFailureListener(ComponentEventListener<EventSourceFailureEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EventSourceFailureEvent.class, listener);
    }

    /**
     * Adds a client-side event source to this calendar. Client-side sources (JSON feed, Google Calendar, iCal)
     * fetch events directly in the browser — they do NOT go through the server-side
     * {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
     * <br><br>
     * If the calendar is already attached, the source is registered immediately via a JS call.
     * If not yet attached, the source is queued and will be applied on first attach (as part of the
     * initial options) and on every subsequent reattach.
     * <br><br>
     * Adding a source with an id that is already registered replaces the existing source.
     *
     * @param source the event source to add; must not be null
     * @throws NullPointerException if source is null
     */
    public void addEventSource(ClientSideEventSource<?> source) {
        Objects.requireNonNull(source, "source must not be null");
        clientSideEventSourceRegistry.put(source.getId(), source);
        if (getElement().getNode().isAttached()) {
            getElement().callJsFunction("addEventSource", source.toJson());
        }
    }

    /**
     * Removes the client-side event source with the given id from this calendar.
     * If the calendar is attached, the source is removed on the client immediately.
     * If the source id is not registered, this method does nothing.
     *
     * @param id the id of the source to remove; must not be null
     * @throws NullPointerException if id is null
     */
    public void removeEventSource(String id) {
        Objects.requireNonNull(id, "id must not be null");
        clientSideEventSourceRegistry.remove(id);
        if (getElement().getNode().isAttached()) {
            getElement().callJsFunction("removeEventSource", id);
        }
    }

    /**
     * Removes the given client-side event source from this calendar, using its {@link ClientSideEventSource#getId()}.
     * If the calendar is attached, the source is removed on the client immediately.
     * If the source is not registered, this method does nothing.
     *
     * @param source the source to remove; must not be null
     * @throws NullPointerException if source is null
     */
    public void removeEventSource(ClientSideEventSource<?> source) {
        Objects.requireNonNull(source, "source must not be null");
        removeEventSource(source.getId());
    }

    /**
     * Returns an unmodifiable view of all registered client-side event sources, keyed by their id.
     *
     * @return unmodifiable map of source id to source
     */
    public Map<String, ClientSideEventSource<?>> getEventSources() {
        return Collections.unmodifiableMap(clientSideEventSourceRegistry);
    }

    /**
     * Enables or disables dropping external DOM elements onto the calendar.
     *
     * @param droppable true to enable
     * @see Option#DROPPABLE
     */
    public void setDroppable(boolean droppable) {
        setOption(Option.DROPPABLE, droppable);
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
     * Indicates, if prefetching of entries of adjacent time ranges is enabled (enabled by default).
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
    @Deprecated
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

        JsonObject jsonObject = Json.createObject();
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

        JsonObject json = Json.createObject();
        for (CustomCalendarView customCalendarView : customCalendarViews) {
            this.customCalendarViews.put(customCalendarView.getClientSideValue(), customCalendarView);
            json.put(customCalendarView.getClientSideValue(), customCalendarView.getViewSettings());
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
        @Deprecated
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

        // ---- Interaction callbacks ----

        /**
         * Controls whether a time-range selection is allowed. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/selectAllow">selectAllow</a>
         */
        SELECT_ALLOW,

        /**
         * Controls whether an entry drag-and-drop is allowed. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/eventAllow">eventAllow</a>
         */
        ENTRY_ALLOW,

        /**
         * Controls whether entries may overlap during dragging.
         * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
         */
        ENTRY_OVERLAP,

        /**
         * Allow dropping external DOM elements onto the calendar.
         * @see <a href="https://fullcalendar.io/docs/droppable">droppable</a>
         */
        DROPPABLE,

        /**
         * Filter which external DOM elements can be dropped onto the calendar.
         * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
         */
        DROP_ACCEPT,

        /**
         * Make day/week numbers clickable (navLinks). Alias for {@link #NAV_LINKS}.
         */
        NAV_LINK_HINT,

        /**
         * Accessible hint for the "+N more" link.
         * @see <a href="https://fullcalendar.io/docs/hints">moreLinkHint</a>
         */
        MORE_LINK_HINT,

        /**
         * Action for the "+N more" link click. Accepts a {@link JsCallback}.
         * @see FullCalendar#setMoreLinkClickAction(MoreLinkClickAction)
         * @see <a href="https://fullcalendar.io/docs/moreLinkClick">moreLinkClick</a>
         */
        MORE_LINK_CLICK,

        /**
         * Accessible hint for close buttons.
         * @see <a href="https://fullcalendar.io/docs/hints">closeHint</a>
         */
        CLOSE_HINT,

        /**
         * Accessible hint for time display.
         * @see <a href="https://fullcalendar.io/docs/hints">timeHint</a>
         */
        TIME_HINT,

        /**
         * Accessible hint for entry elements.
         * @see <a href="https://fullcalendar.io/docs/hints">eventHint</a>
         */
        ENTRY_HINT,

        // ---- Render hooks: Entry ----

        /**
         * Add CSS classes to entry wrapper elements. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventClassNames</a>
         */
        ENTRY_CLASS_NAMES("eventClassNames"),

        /**
         * Customize the inner content of entry elements. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventContent</a>
         */
        ENTRY_CONTENT("eventContent"),

        /**
         * Called after an entry element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventDidMount</a>
         */
        ENTRY_DID_MOUNT("eventDidMount"),

        /**
         * Called before an entry element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventWillUnmount</a>
         */
        ENTRY_WILL_UNMOUNT("eventWillUnmount"),

        // ---- Render hooks: Day Cell ----

        /**
         * Add CSS classes to day cell elements. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellClassNames</a>
         */
        DAY_CELL_CLASS_NAMES("dayCellClassNames"),

        /**
         * Customize the content inside day cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellContent</a>
         */
        DAY_CELL_CONTENT("dayCellContent"),

        /**
         * Called after a day cell element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellDidMount</a>
         */
        DAY_CELL_DID_MOUNT("dayCellDidMount"),

        /**
         * Called before a day cell element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellWillUnmount</a>
         */
        DAY_CELL_WILL_UNMOUNT("dayCellWillUnmount"),

        // ---- Render hooks: Day Header ----

        /**
         * Add CSS classes to day header elements. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderClassNames</a>
         */
        DAY_HEADER_CLASS_NAMES("dayHeaderClassNames"),

        /**
         * Customize the content inside day header cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderContent</a>
         */
        DAY_HEADER_CONTENT("dayHeaderContent"),

        /**
         * Called after a day header element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderDidMount</a>
         */
        DAY_HEADER_DID_MOUNT("dayHeaderDidMount"),

        /**
         * Called before a day header element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderWillUnmount</a>
         */
        DAY_HEADER_WILL_UNMOUNT("dayHeaderWillUnmount"),

        // ---- Render hooks: Slot Label ----

        /**
         * Add CSS classes to time slot label cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelClassNames</a>
         */
        SLOT_LABEL_CLASS_NAMES("slotLabelClassNames"),

        /**
         * Customize the content inside time slot label cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelContent</a>
         */
        SLOT_LABEL_CONTENT("slotLabelContent"),

        /**
         * Called after a slot label element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelDidMount</a>
         */
        SLOT_LABEL_DID_MOUNT("slotLabelDidMount"),

        /**
         * Called before a slot label element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelWillUnmount</a>
         */
        SLOT_LABEL_WILL_UNMOUNT("slotLabelWillUnmount"),

        // ---- Render hooks: Slot Lane ----

        /**
         * Add CSS classes to time slot lane cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneClassNames</a>
         */
        SLOT_LANE_CLASS_NAMES("slotLaneClassNames"),

        /**
         * Customize the content inside time slot lane cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneContent</a>
         */
        SLOT_LANE_CONTENT("slotLaneContent"),

        /**
         * Called after a slot lane element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneDidMount</a>
         */
        SLOT_LANE_DID_MOUNT("slotLaneDidMount"),

        /**
         * Called before a slot lane element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneWillUnmount</a>
         */
        SLOT_LANE_WILL_UNMOUNT("slotLaneWillUnmount"),

        // ---- Render hooks: View ----

        /**
         * Add CSS classes to the view root element. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewClassNames</a>
         */
        VIEW_CLASS_NAMES("viewClassNames"),

        /**
         * Called after the view root element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewDidMount</a>
         */
        VIEW_DID_MOUNT("viewDidMount"),

        /**
         * Called before the view root element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewWillUnmount</a>
         */
        VIEW_WILL_UNMOUNT("viewWillUnmount"),

        // ---- Render hooks: Now Indicator ----

        /**
         * Add CSS classes to now indicator elements. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorClassNames</a>
         */
        NOW_INDICATOR_CLASS_NAMES("nowIndicatorClassNames"),

        /**
         * Customize the content inside now indicator elements. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorContent</a>
         */
        NOW_INDICATOR_CONTENT("nowIndicatorContent"),

        /**
         * Called after a now indicator element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorDidMount</a>
         */
        NOW_INDICATOR_DID_MOUNT("nowIndicatorDidMount"),

        /**
         * Called before a now indicator element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorWillUnmount</a>
         */
        NOW_INDICATOR_WILL_UNMOUNT("nowIndicatorWillUnmount"),

        // ---- Render hooks: Week Number ----

        /**
         * Add CSS classes to week number cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberClassNames</a>
         */
        WEEK_NUMBER_CLASS_NAMES("weekNumberClassNames"),

        /**
         * Customize the content inside week number cells. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberContent</a>
         */
        WEEK_NUMBER_CONTENT("weekNumberContent"),

        /**
         * Called after a week number element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberDidMount</a>
         */
        WEEK_NUMBER_DID_MOUNT("weekNumberDidMount"),

        /**
         * Called before a week number element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberWillUnmount</a>
         */
        WEEK_NUMBER_WILL_UNMOUNT("weekNumberWillUnmount"),

        // ---- Render hooks: More Link ----

        /**
         * Add CSS classes to the "+N more" link element. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkClassNames</a>
         */
        MORE_LINK_CLASS_NAMES("moreLinkClassNames"),

        /**
         * Customize the content of the "+N more" link. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkContent</a>
         */
        MORE_LINK_CONTENT("moreLinkContent"),

        /**
         * Called after a more link element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkDidMount</a>
         */
        MORE_LINK_DID_MOUNT("moreLinkDidMount"),

        /**
         * Called before a more link element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkWillUnmount</a>
         */
        MORE_LINK_WILL_UNMOUNT("moreLinkWillUnmount"),

        // ---- Render hooks: No Entries ----

        /**
         * Add CSS classes to the "No events" message in list view. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsClassNames</a>
         */
        NO_ENTRIES_CLASS_NAMES("noEventsClassNames"),

        /**
         * Customize the "No events" message in list view. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsContent</a>
         */
        NO_ENTRIES_CONTENT("noEventsContent"),

        /**
         * Called after a no-entries element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsDidMount</a>
         */
        NO_ENTRIES_DID_MOUNT("noEventsDidMount"),

        /**
         * Called before a no-entries element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsWillUnmount</a>
         */
        NO_ENTRIES_WILL_UNMOUNT("noEventsWillUnmount"),

        // ---- Render hooks: All Day ----

        /**
         * Add CSS classes to the all-day section header cell. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayClassNames</a>
         */
        ALL_DAY_CLASS_NAMES("allDayClassNames"),

        /**
         * Customize the content inside the all-day header cell. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayContent</a>
         */
        ALL_DAY_CONTENT("allDayContent"),

        /**
         * Called after the all-day header element is added to the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayDidMount</a>
         */
        ALL_DAY_DID_MOUNT("allDayDidMount"),

        /**
         * Called before the all-day header element is removed from the DOM. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayWillUnmount</a>
         */
        ALL_DAY_WILL_UNMOUNT("allDayWillUnmount"),

        // ---- Data transform / loading callbacks ----

        /**
         * Called when async entry fetching starts or stops. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/loading">loading</a>
         */
        LOADING("loading"),

        /**
         * Transform raw entry data before FullCalendar parses it. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/eventDataTransform">eventDataTransform</a>
         */
        ENTRY_DATA_TRANSFORM("eventDataTransform"),

        /**
         * Called after an entry source fetches successfully. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/eventSourceSuccess">eventSourceSuccess</a>
         */
        ENTRY_SOURCE_SUCCESS("eventSourceSuccess"),

        // ---- Navigation callbacks ----

        /**
         * Custom handler for clickable day nav links. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/navLinkDayClick">navLinkDayClick</a>
         */
        NAV_LINK_DAY_CLICK("navLinkDayClick"),

        /**
         * Custom handler for clickable week nav links. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/navLinkWeekClick">navLinkWeekClick</a>
         */
        NAV_LINK_WEEK_CLICK("navLinkWeekClick"),

        // ---- External event sources ----

        /**
         * Default query parameter name for the range start sent to JSON feed event sources.
         * @see <a href="https://fullcalendar.io/docs/startParam">startParam</a>
         */
        EXTERNAL_EVENT_SOURCE_START_PARAM("startParam"),

        /**
         * Default query parameter name for the range end sent to JSON feed event sources.
         * @see <a href="https://fullcalendar.io/docs/endParam">endParam</a>
         */
        EXTERNAL_EVENT_SOURCE_END_PARAM("endParam"),

        /**
         * Default query parameter name for the timezone sent to JSON feed event sources.
         * @see <a href="https://fullcalendar.io/docs/timeZoneParam">timeZoneParam</a>
         */
        EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM("timeZoneParam"),

        /**
         * Global Google Calendar API key used by all Google Calendar event sources.
         * @see <a href="https://fullcalendar.io/docs/google-calendar">googleCalendarApiKey</a>
         */
        EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY("googleCalendarApiKey"),

        /**
         * Controls how a "fixed mirror" parent element is specified during drag. Accepts a {@link JsCallback}.
         * @see <a href="https://fullcalendar.io/docs/fixedMirrorParent">fixedMirrorParent</a>
         */
        FIXED_MIRROR_PARENT;

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

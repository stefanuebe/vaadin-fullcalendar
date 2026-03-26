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
import org.vaadin.stefan.fullcalendar.converters.BusinessHoursConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekArrayConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekConverter;
import org.vaadin.stefan.fullcalendar.converters.DurationConverter;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.converters.LocaleConverter;
import org.vaadin.stefan.fullcalendar.converters.StringArrayConverter;
import org.vaadin.stefan.fullcalendar.converters.ToolbarConverter;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.model.Footer;
import org.vaadin.stefan.fullcalendar.model.Header;
import tools.jackson.databind.JsonNode;
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

    private final Map<String, String> customNativeEventsMap = new LinkedHashMap<>();
    private volatile JsCallback userEntryDidMountCallback;

    /**
     * Server-side registry of client-managed event sources, keyed by source id.
     */
    private final Map<String, ClientSideEventSource<?>> clientSideEventSourceRegistry = new LinkedHashMap<>();

    /**
     * Server-side registry of draggable components, keyed by draggable UUID.
     */
    private final Map<String, Draggable> draggableRegistry = new LinkedHashMap<>();
    private final Map<String, Registration> draggableCleanups = new LinkedHashMap<>();

    /**
     * Whether to automatically revert client-side entry changes when
     * {@link EntryDataEvent#applyChangesOnEntry()} is not called. Default is {@code true}.
     */
    private boolean autoRevertUnappliedEntryChanges = true;

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

                    if (!clientSideEventSourceRegistry.isEmpty()) {
                        ArrayNode sourcesArray = JsonFactory.createArray();
                        clientSideEventSourceRegistry.values().stream().map(ClientSideEventSource::toJson).forEach(sourcesArray::add);
                        getElement().callJsFunction("restoreEventSources", sourcesArray);
                    }

                });
            });
        }

        // Initialize draggables on both initial attach and reattach
        if (!draggableRegistry.isEmpty()) {
            getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(this, executionContext -> {
                    for (Draggable d : draggableRegistry.values()) {
                        callInitDraggable(d);
                    }
                });
            });
        }

        applyEntryDidMountMerge(false);
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

        lookupViewName(view.getClientSideValue()).orElseThrow(() -> new IllegalArgumentException("Unknown view: " + view.getClientSideValue() + ". If you want to use a custom view, please register it first by using addCustomView()."));

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
        setOption(option.getOptionKey(), value, null, option.getConverters());
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
        setOption(option.getOptionKey(), value, valueForServerSide, option.getConverters());
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
    public void setOption(String option, Object value, @SuppressWarnings("rawtypes") JsonItemPropertyConverter... converters) {
        setOption(option, value, null, converters == null ? List.of() : List.of(converters));
    }

    /**
     * Sets an option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(Option)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Optionally, one or more {@link JsonItemPropertyConverter converters} can be passed to automatically
     * convert the value to a client-side representation. The first converter whose
     * {@link JsonItemPropertyConverter#supports(Object)} returns {@code true} for the value will be used.
     * The original value is stored as the server-side value.
     * <br><br>
     * Please be aware that this method does not check the passed value. Explicit setter
     * methods should be prefered (e.g. {@link #setLocale(Locale)}).
     * <p>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @param converters         optional converters to apply to the value
     * @throws NullPointerException when null is passed
     */
    public void setOption(String option, Object value, Object valueForServerSide, @SuppressWarnings("rawtypes") JsonItemPropertyConverter... converters) {
        setOption(option, value, valueForServerSide, List.of(converters));
    }

    protected void setOption(String option, Object value, Object valueForServerSide,
                             List<JsonItemPropertyConverter<?, ?>> converters) {
        if (value != null && !converters.isEmpty()) {
            for (JsonItemPropertyConverter<?, ?> c : converters) {
                if (c.supports(value)) {
                    @SuppressWarnings("unchecked")
                    JsonNode converted = ((JsonItemPropertyConverter<Object, Object>) c).toClientModel(value, null);
                    callOptionUpdate(option, converted,
                            valueForServerSide != null ? valueForServerSide : value,
                            "setOption");
                    return;
                }
            }
        }
        callOptionUpdate(option, value, valueForServerSide, "setOption");
    }

    private void callOptionUpdate(String option, Object value, Object valueForServerSide, String method, Serializable... additionalParameters) {
        Objects.requireNonNull(option);

        // 1. ENTRY_DID_MOUNT intercept: detect this key and route to merge logic
        if (Option.ENTRY_DID_MOUNT.getOptionKey().equals(option)) {
            boolean removed;
            if (value instanceof JsCallback cb) {
                userEntryDidMountCallback = cb;
                removed = false;
            } else {
                removed = userEntryDidMountCallback != null; // was set before, now being cleared
                userEntryDidMountCallback = null;
            }
            applyEntryDidMountMerge(removed);
            return;
        }

        // 2. Convert JsCallback to marker JSON before the attached/not-attached split.
        //    Preserve original JsCallback for getOption() round-trip via serverSideOptions.
        if (value instanceof JsCallback cb) {
            if (valueForServerSide == null) {
                valueForServerSide = cb;
            }
            value = cb.toMarkerJson();
        }

        // Auto-convert ClientSideValue implementations to their client-side string,
        // keeping the original typed object for server-side getOption() retrieval.
        if (value instanceof ClientSideValue csv) {
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#FIRST_DAY} instead.
     *             Accepts a {@link DayOfWeek} directly via the option converter.
     */
    @Deprecated
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#SELECTABLE} instead.
     */
    @Deprecated
    public void setTimeslotsSelectable(boolean selectable) {
        setOption(Option.SELECTABLE, selectable);
    }


    /**
     * Should the calendar show week numbers (when available for the current view)?
     *
     * @param weekNumbersVisible week numbers visible
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#WEEK_NUMBERS} instead.
     */
    @Deprecated
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
     * @deprecated Use {@link #getOption(Option)} with {@link Option#LOCALE} instead.
     *             Accepts a {@link Locale} directly.
     */
    @Deprecated
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#LOCALE} instead.
     *             Accepts a {@link Locale} directly.
     */
    @Deprecated
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale);
        setOption(Option.LOCALE, locale);
    }

    protected String toClientSideLocale(Locale locale) {
        return locale.toLanguageTag().toLowerCase();
    }

    /**
     * If true is passed then the calendar will show a indicator for the current time, depending on the view.
     *
     * @param shown show indicator for now
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#NOW_INDICATOR} instead.
     */
    @Deprecated
    public void setNowIndicatorShown(boolean shown) {
        setOption(Option.NOW_INDICATOR, shown);
    }


    /**
     * When true is passed the day / week numbers (or texts) will become clickable by the user and fire an event
     * for the clicked day / week.
     *
     * @param clickable clickable
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#NAV_LINKS} instead.
     */
    @Deprecated
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
     * calendar.setOption(Option.ENTRY_CLASS_NAMES, JsCallback.of("" +
     * "function(arg) { " +
     * "  if (arg.event.getCustomProperty('isUrgent', false)) {" +
     * "    return [ 'urgent' ];" +
     * "  } else { " +
     * "    return [ 'normal' ];" +
     * "  }" +
     * "}"));
     * </pre>
     *
     * @param s function to be attached
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_CLASS_NAMES}
     *             and {@link JsCallback} instead.
     */
    @Deprecated
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
     * If you also setup native event listeners via {@link #addEntryNativeEventListener(String, String)},
     * then these will automatically be merged into the callback (the function must end with a closing brace {@code }}).
     *
     * @param s function to be attached
     * @see #addEntryNativeEventListener(String, String)
     *
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_DID_MOUNT}
     *             and {@link JsCallback} instead.
     *             Native event listeners registered via {@link #addEntryNativeEventListener(String, String)}
     *             are automatically merged regardless of which method is used.
     */
    @Deprecated
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
     * — the native event registrations will be appended to the end of it automatically
     * (the function must end with a closing brace {@code }}). Adding a native event listener after the
     * calendar is attached takes effect immediately.
     * <br><br>
     * Inside the native event callback you may access the entry DOM element via the event's
     * {@code currentTarget} or {@code target} property.  For the full set of available parameters
     * in the surrounding {@code eventDidMount} hook, see the
     * <a href="https://fullcalendar.io/docs/event-render-hooks">official FC docs</a>.
     * @param eventName javascript event name
     * @param eventCallback javascript event callback to be hooked to the event
     * @return registration to remove the native event listener
     */
    public Registration addEntryNativeEventListener(String eventName, String eventCallback) {
        customNativeEventsMap.put(eventName, eventCallback);
        applyEntryDidMountMerge(false);

        return Registration.once(() -> {
            customNativeEventsMap.remove(eventName);
            applyEntryDidMountMerge(false);
        });
    }

    /**
     * Applies the merged eventDidMount callback to the client.
     * @param clearIfNull if true and the merged result is null, actively sends null to clear the client-side callback.
     *                    If false and merged is null, does nothing (preserves any callback set directly in TypeScript subclasses).
     */
    private void applyEntryDidMountMerge(boolean clearIfNull) {
        if (!isAttached()) {
            return;
        }
        String merged = buildEntryDidMountMerged();
        if (merged != null) {
            getElement().callJsFunction("setOption", "eventDidMount", JsCallback.of(merged).toMarkerJson());
        } else if (clearIfNull) {
            getElement().callJsFunction("setOption", "eventDidMount", (JsonNode) null);
        }
    }

    /**
     * Builds the merged eventDidMount function string from the user callback and native event listeners.
     * Returns {@code null} when both are empty (meaning "clear the callback").
     * <p>
     * Package-private for testing.
     */
    String buildEntryDidMountMerged() {
        String userCallback = userEntryDidMountCallback != null ? userEntryDidMountCallback.getJsFunction() : null;

        StringBuilder events = null;
        if (!customNativeEventsMap.isEmpty()) {
            events = new StringBuilder();
            for (Map.Entry<String, String> entry : customNativeEventsMap.entrySet()) {
                events.append("arguments[0].el.addEventListener('")
                        .append(entry.getKey().replace("\\", "\\\\").replace("'", "\\'"))
                        .append("', ")
                        .append(entry.getValue())
                        .append(")\n");
            }
        }

        String merged = null;
        if (StringUtils.isNotBlank(userCallback)) {
            if (events != null) {
                int index = userCallback.lastIndexOf("}");
                merged = userCallback.substring(0, index) + events + userCallback.substring(index);
            } else {
                merged = userCallback;
            }
        } else if (events != null) {
            merged = "function(info) {\n" + events + "}";
        }

        return merged;
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_WILL_UNMOUNT}
     *             and {@link JsCallback} instead.
     */
    @Deprecated
    public void setEntryWillUnmountCallback(String s) {
        setOption(Option.ENTRY_WILL_UNMOUNT, JsCallback.of(s));
    }

    /**
     * The given string will be interpreted as JS function on the client side
     * and attached to the calendar as the "eventContent" callback. It must be a valid JavaScript function.
     * <br><br>
     * Called when an entry is being rendered, allowing you to return custom HTML content or a DOM node to replace the default entry content.
     * <b>Note: </b> Please be aware, that there is <b>NO</b> content parsing, escaping, quoting or
     * other security mechanism applied on this string, so check it yourself before passing it to the client.
     * <br><br>
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">https://fullcalendar.io/docs/event-render-hooks</a>
     * @see <a href="https://fullcalendar.io/docs/content-injection">https://fullcalendar.io/docs/content-injection</a>
     *
     * @param s function to be attached
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_CONTENT}
     *             and {@link JsCallback} instead.
     */
    @Deprecated
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#BUSINESS_HOURS} instead.
     *             Accepts {@link BusinessHours}, {@code BusinessHours[]}, or {@code boolean}. Pass {@code null} to remove.
     */
    @Deprecated
    public void setBusinessHours(BusinessHours... hours) {
        Objects.requireNonNull(hours);

        setOption(Option.BUSINESS_HOURS, JsonUtils.toJsonNode(Arrays.stream(hours).map(BusinessHours::toJson)), hours);
    }

    /**
     * Removes the business hours for this calendar instance.
     *
     * @throws NullPointerException when null is passed
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#BUSINESS_HOURS} instead.
     *             Accepts {@link BusinessHours}, {@code BusinessHours[]}, or {@code boolean}. Pass {@code null} to remove.
     */
    @Deprecated
    public void removeBusinessHours() {
        setOption(Option.BUSINESS_HOURS, null);
    }

    /**
     * Sets the snap duration for this calendar instance (the time interval entries snap to when dragging).
     * The default is {@code "00:30:00"}.
     *
     * @param duration duration (e.g. {@code "00:15:00"} for 15-minute snapping)
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/snapDuration">snapDuration</a>
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#SNAP_DURATION} instead.
     *             Accepts a {@code String} (e.g. {@code "00:15:00"}) or a {@link java.time.Duration}.
     */
    @Deprecated
    public void setSnapDuration(String duration) {
        Objects.requireNonNull(duration);
        setOption(Option.SNAP_DURATION, duration);
    }

    /**
     * Sets the min time for this calendar instance. This is the first time slot that will be displayed for each day.
     * The default is {@code "00:00:00"}.
     *
     * @param slotMinTime slotMinTime to set
     * @throws NullPointerException when null is passed
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#SLOT_MIN_TIME} instead.
     *             Accepts a {@link java.time.LocalTime}, a {@link java.time.Duration}, or a duration string.
     */
    @Deprecated
    public void setSlotMinTime(LocalTime slotMinTime) {
        Objects.requireNonNull(slotMinTime);
        setOption(Option.SLOT_MIN_TIME, slotMinTime);
    }

    /**
     * Returns the fixedWeekCount. By default true.
     *
     * @return fixedWeekCount
     * @deprecated Use {@link #getOption(Option)} with {@link Option#FIXED_WEEK_COUNT} instead.
     */
    @Deprecated
    public boolean getFixedWeekCount() {
        return (boolean) getOption(Option.FIXED_WEEK_COUNT).orElse(true);
    }

    /**
     * Determines the number of weeks displayed in a month view.
     * If true, the calendar will always be 6 weeks tall.
     * If false, the calendar will have either 4, 5, or 6 weeks, depending on the month.
     *
     * @param fixedWeekCount
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#FIXED_WEEK_COUNT} instead.
     */
    @Deprecated
    public void setFixedWeekCount(boolean fixedWeekCount) {
        setOption(Option.FIXED_WEEK_COUNT, fixedWeekCount);
    }

    /**
     * Sets the max time for this calendar instance. This is the last time slot that will be displayed for each day.
     * The default is {@code "24:00:00"}.
     *
     * @param slotMaxTime slotMaxTime to set
     * @throws NullPointerException when null is passed
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#SLOT_MAX_TIME} instead.
     *             Accepts a {@link java.time.LocalTime}, a {@link java.time.Duration}, or a duration string.
     */
    @Deprecated
    public void setSlotMaxTime(LocalTime slotMaxTime) {
        Objects.requireNonNull(slotMaxTime);
        setOption(Option.SLOT_MAX_TIME, slotMaxTime);
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
            setOption(Option.TIMEZONE, timezone);
        }
    }

    /**
     * Returns the editable flag. By default true.
     *
     * @return editable editable
     * @deprecated Use {@link #getOption(Option)} with {@link Option#ENTRY_DURATION_EDITABLE} instead.
     */
    @Deprecated
    public boolean getEntryDurationEditable() {
        return (boolean) getOption(Option.ENTRY_DURATION_EDITABLE).orElse(true);
    }

    /**
     * Allow events’ durations to be editable through resizing.
     * <p>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setDurationEditable(Boolean)}
     *
     * @param editable editable
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_DURATION_EDITABLE} instead.
     */
    @Deprecated
    public void setEntryDurationEditable(boolean editable) {
        setOption(Option.ENTRY_DURATION_EDITABLE, editable);
    }

    /**
     * Returns the editable flag. By default false.
     *
     * @return editable editable
     * @deprecated Use {@link #getOption(Option)} with {@link Option#ENTRY_RESIZABLE_FROM_START} instead.
     */
    @Deprecated
    public boolean getEntryResizableFromStart() {
        return (boolean) getOption(Option.ENTRY_RESIZABLE_FROM_START).orElse(false);
    }

    /**
     * Whether the user can resize an event from its starting edge.
     *
     * @param editable editable
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_RESIZABLE_FROM_START} instead.
     */
    @Deprecated
    public void setEntryResizableFromStart(boolean editable) {
        setOption(Option.ENTRY_RESIZABLE_FROM_START, editable);
    }

    /**
     * Returns the editable flag. By default true.
     *
     * @return editable editable
     * @deprecated Use {@link #getOption(Option)} with {@link Option#ENTRY_START_EDITABLE} instead.
     */
    @Deprecated
    public boolean getEntryStartEditable() {
        return (boolean) getOption(Option.ENTRY_START_EDITABLE).orElse(true);
    }

    /**
     * Allow events’ start times to be editable through dragging.
     * <p>
     * This option can be overridden with {@link org.vaadin.stefan.fullcalendar.Entry#setStartEditable(Boolean)}
     *
     * @param editable editable
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_START_EDITABLE} instead.
     */
    @Deprecated
    public void setEntryStartEditable(boolean editable) {
        setOption(Option.ENTRY_START_EDITABLE, editable);
    }

    /**
     * Returns the editable flag. By default false.
     *
     * @return editable editable
     * @deprecated Use {@link #getOption(Option)} with {@link Option#EDITABLE} instead.
     */
    @Deprecated
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#EDITABLE} instead.
     */
    @Deprecated
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
     * @deprecated Use {@link #getOption(Option)} with {@link Option#WEEKENDS} instead.
     */
    @Deprecated
    public boolean getWeekends() {
        return (boolean) getOption(Option.WEEKENDS).orElse(true);
    }

    /**
     * Whether to include Saturday/Sunday columns in any of the calendar views.
     *
     * @param weekends
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#WEEKENDS} instead.
     */
    @Deprecated
    public void setWeekends(boolean weekends) {
        setOption(Option.WEEKENDS, weekends);
    }


    /**
     * display the header.
     *
     * @param header
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#HEADER_TOOLBAR} instead.
     *             Accepts a {@link Header} directly.
     */
    @Deprecated
    public void setHeaderToolbar(Header header) {
        setOption(Option.HEADER_TOOLBAR, header.toJson());
    }

    /**
     * display the footer.
     *
     * @param footer
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#FOOTER_TOOLBAR} instead.
     *             Accepts a {@link Footer} directly.
     */
    @Deprecated
    public void setFooterToolbar(Footer footer) {
        setOption(Option.FOOTER_TOOLBAR, footer.toJson());
    }

    /**
     * Returns the columnHeader.
     *
     * @return columnHeader
     * @deprecated Use {@link #getOption(Option)} with {@link Option#DAY_HEADERS} instead.
     */
    @Deprecated
    public boolean getColumnHeader() {
        return (boolean) getOption(Option.DAY_HEADERS).orElse(true);
    }

    /**
     * Whether the day headers should appear. For the Month, TimeGrid, and DayGrid views.
     *
     * @param columnHeader whether to show day headers
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#DAY_HEADERS} instead.
     */
    @Deprecated
    public void setColumnHeader(boolean columnHeader) {
        setOption(Option.DAY_HEADERS, columnHeader);
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
        return addAutoRevertAwareListener(EntryResizedEvent.class, listener);
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
        return addAutoRevertAwareListener(EntryDroppedEvent.class, listener);
    }

    /**
     * Returns whether the calendar automatically reverts client-side entry changes
     * when {@link EntryDataEvent#applyChangesOnEntry()} is not called in a drop/resize listener.
     * Default is {@code true}.
     *
     * @return true if auto-revert is enabled
     */
    public boolean isAutoRevertUnappliedEntryChanges() {
        return autoRevertUnappliedEntryChanges;
    }

    /**
     * Sets whether the calendar should automatically revert client-side entry changes
     * when {@link EntryDataEvent#applyChangesOnEntry()} is not called during event listener
     * processing of drop or resize events.
     * <p>
     * When {@code true} (the default), if a developer's listener does not call
     * {@code event.applyChangesOnEntry()}, the client-side entry is reverted to its
     * original position/size, keeping server and client in sync.
     * <p>
     * When {@code false}, the old behavior is used: the client keeps the new position
     * regardless of whether changes were applied on the server.
     *
     * @param autoRevert true to enable auto-revert
     */
    public void setAutoRevertUnappliedEntryChanges(boolean autoRevert) {
        // Future: validate against signal binding
        // if (!autoRevert && isSignalBindingActive()) {
        //     throw new BindingActiveException("Auto-revert cannot be disabled while signal binding is active");
        // }
        this.autoRevertUnappliedEntryChanges = autoRevert;
    }

    /**
     * Wraps a listener for {@link EntryDataEvent} subclasses with auto-revert support.
     * When {@link #isAutoRevertUnappliedEntryChanges()} is true, schedules a
     * {@code beforeClientResponse} callback after each event dispatch to check if
     * {@link EntryDataEvent#applyChangesOnEntry()} was called. If not, the client-side
     * entry is reverted to its original position.
     *
     * @param eventType the event class
     * @param listener the user's listener
     * @param <T> event type extending EntryDataEvent
     * @return registration to remove the listener
     */
    protected <T extends EntryDataEvent> Registration addAutoRevertAwareListener(
            Class<T> eventType, ComponentEventListener<T> listener) {

        return addListener(eventType, event -> {
            listener.onComponentEvent(event);

            if (autoRevertUnappliedEntryChanges && !event.isRevertCheckScheduled()) {
                event.markRevertCheckScheduled();
                String entryId = event.getEntry().getId();
                getElement().getNode().runWhenAttached(ui ->
                    ui.beforeClientResponse(this, ctx -> {
                        if (event.isChangesApplied()) {
                            getElement().callJsFunction("clearPendingRevert", entryId);
                        } else {
                            getElement().callJsFunction("revertEntry", entryId);
                        }
                    })
                );
            }
        });
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
     * Requires {@link Option#DROPPABLE} to be set to {@code true}.
     * <p>
     *     This listener is considered low-level. If you external html element uses the Draggable feature
     *     of the FullCalendar and also provides valid entry data, we strongly recommend to use
     *     the {@link #addEntryReceiveListener(ComponentEventListener)} instead.
     * </p>
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
     * Registers a {@link Draggable} on this calendar. The wrapped component becomes draggable
     * onto any calendar that has {@link Option#DROPPABLE} set to {@code true}.
     * <p>
     * When the component is dropped onto <em>this</em> calendar, the resulting {@link DropEvent}
     * provides typed access to the component and entry via {@link DropEvent#getDraggedComponent()}
     * and {@link DropEvent#getDraggedEntry()}.
     * <p>
     * <strong>Important:</strong> The draggable registry is per-calendar. If the component is dropped
     * onto a <em>different</em> calendar, that calendar's {@link DropEvent} will not resolve the
     * draggable — {@code getDraggedComponent()} and {@code getDraggedEntry()} will return empty.
     * The raw {@link DropEvent#getEntryJson()} is still available in all cases.
     *
     * @param draggable the draggable to register
     * @return registration to remove the draggable
     * @throws NullPointerException when null is passed
     */
    public Registration addDraggable(Draggable draggable) {
        Objects.requireNonNull(draggable);
        com.vaadin.flow.dom.Element el = draggable.getComponent().getElement();

        // Double-registration guard: clean up previous if exists
        String existingId = el.getAttribute("data-draggable-id");
        if (existingId != null && draggableCleanups.containsKey(existingId)) {
            draggableCleanups.get(existingId).remove();
        }

        String id = java.util.UUID.randomUUID().toString();
        el.setAttribute("data-draggable-id", id);

        if (draggable.getEntryData().isPresent()) {
            el.setAttribute("data-event", draggable.getEntryData().get().toJson().toString());
        }

        draggableRegistry.put(id, draggable);

        if (isAttached()) {
            getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(this, ctx -> callInitDraggable(draggable));
            });
        }

        Registration cleanup = () -> {
            draggableRegistry.remove(id);
            draggableCleanups.remove(id);
            el.removeAttribute("data-draggable-id");
            el.removeAttribute("data-event");
            if (isAttached()) {
                getElement().callJsFunction("destroyDraggable", el);
            }
        };
        draggableCleanups.put(id, cleanup);

        return cleanup;
    }

    /**
     * Resolves a draggable by its client-side ID. Package-private — used by {@link DropEvent}.
     */
    Optional<Draggable> resolveDraggable(String draggableId) {
        if (draggableId == null || draggableId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(draggableRegistry.get(draggableId));
    }

    /**
     * Sends the initDraggable JS call with optional itemSelector and eventDataCallback.
     */
    private void callInitDraggable(Draggable draggable) {
        com.vaadin.flow.dom.Element el = draggable.getComponent().getElement();
        String itemSelector = draggable.getItemSelector();
        JsCallback eventDataCallback = draggable.getEventDataCallback();

        if (itemSelector != null || eventDataCallback != null) {
            getElement().callJsFunction("initDraggable", el,
                    itemSelector != null ? itemSelector : "",
                    eventDataCallback != null ? eventDataCallback.toMarkerJson() : null);
        } else {
            getElement().callJsFunction("initDraggable", el);
        }
    }

    /**
     * Registers a listener for when an external element with a {@code data-event} attribute has been dropped
     * and FullCalendar has created a new entry from it. The entry is not added to the provider automatically.
     * Requires {@link Option#DROPPABLE} to be set to {@code true}.
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
     * Adds a client-side event source to this calendar. The browser will fetch events from this source directly,
     * bypassing the server-side {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
     * <br><br>
     * A server-side registry entry is kept so the source can be restored on reattachment.
     * <br><br>
     * Returns a {@link Registration} that removes this source when invoked.
     *
     * @param source event source to add; must not be null
     * @return a registration that removes the source
     * @throws NullPointerException if source is null
     */
    public Registration addClientSideEventSource(ClientSideEventSource<?> source) {
        Objects.requireNonNull(source, "source must not be null");
        clientSideEventSourceRegistry.put(source.getId(), source);
        getElement().callJsFunction("addEventSource", source.toJson());
        return () -> removeClientSideEventSource(source.getId());
    }

    /**
     * Removes the client-side event source with the given id from this calendar.
     * Does nothing if no source with that id has been added.
     *
     * @param id id of the source to remove; must not be null
     * @throws NullPointerException if id is null
     */
    public void removeClientSideEventSource(String id) {
        Objects.requireNonNull(id, "id must not be null");
        clientSideEventSourceRegistry.remove(id);
        getElement().callJsFunction("removeEventSource", id);
    }

    /**
     * Replaces all current client-side event sources with the given collection.
     * Previously added sources are removed. If the collection is empty, all client-side
     * sources are cleared.
     * <br><br>
     * Returns a {@link Registration} that clears all client-side event sources when invoked.
     *
     * @param sources new set of event sources; must not be null
     * @return a registration that clears all client-side sources
     * @throws NullPointerException if sources is null
     */
    public Registration setClientSideEventSources(java.util.Collection<? extends ClientSideEventSource<?>> sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        clientSideEventSourceRegistry.clear();
        sources.forEach(s -> clientSideEventSourceRegistry.put(s.getId(), s));
        ArrayNode array = JsonFactory.createArray();
        sources.stream().map(ClientSideEventSource::toJson).forEach(array::add);
        getElement().callJsFunction("setEventSources", array);
        return () -> setClientSideEventSources(java.util.Collections.emptyList());
    }

    /**
     * Returns an unmodifiable view of all registered client-side event sources.
     *
     * @return collection of registered event sources
     */
    public java.util.Collection<ClientSideEventSource<?>> getClientSideEventSources() {
        return java.util.Collections.unmodifiableCollection(clientSideEventSourceRegistry.values());
    }

    /**
     * Returns the client-side event source with the given ID, or empty if no such source is registered.
     *
     * @param id event source id; must not be null
     * @return the event source, or empty
     * @throws NullPointerException if id is null
     */
    public Optional<ClientSideEventSource<?>> getClientSideEventSourceById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(clientSideEventSourceRegistry.get(id));
    }

    /**
     * Forces all event sources to re-fetch their data immediately. This includes both the server-side
     * {@link EntryProvider} and any client-side event sources added via {@link #addClientSideEventSource}.
     * <br><br>
     * To refresh only a single client-side source, use {@link #refetchClientSideEventSource(String)}.
     */
    public void refetchEvents() {
        getElement().callJsFunction("refetchEvents");
    }

    /**
     * Forces a single <em>client-side</em> event source to re-fetch its data. Only the source with the given id is
     * refreshed; all other sources remain untouched.
     * <br><br>
     * <strong>Important:</strong> This method only works for client-side event sources added via
     * {@link #addClientSideEventSource} (e.g. {@link JsonFeedEventSource}, {@link GoogleCalendarEventSource},
     * {@link ICalendarEventSource}). It cannot be used to refresh the server-side {@link EntryProvider} — use
     * {@link #refetchEvents()} or the entry provider's own {@code refresh} methods for that.
     *
     * @param sourceId the id of the client-side event source to refetch; must not be null
     * @throws NullPointerException when null is passed
     * @see #refetchEvents()
     * @see <a href="https://fullcalendar.io/docs/EventSource-refetch">EventSource::refetch</a>
     */
    public void refetchClientSideEventSource(String sourceId) {
        Objects.requireNonNull(sourceId);
        getElement().executeJs("var s = this.calendar.getEventSourceById($0); if (s) s.refetch();", sourceId);
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
     * <p>For a custom JS function callback, use {@link #setOption(Option, Object)} with
     * {@link Option#MORE_LINK_CLICK} and {@link JsCallback} instead.
     *
     * @param moreLinkClickAction action to set
     * @see MoreLinkClickAction
     * @see Option#MORE_LINK_CLICK
     */
    public void setMoreLinkClickAction(MoreLinkClickAction moreLinkClickAction) {
        getElement().setProperty("moreLinkClickAction", (moreLinkClickAction != null ? moreLinkClickAction : MoreLinkClickAction.POPUP).getClientSideValue());
    }

    /**
     * Activates or deactivates the automatic calendar scrolling, when dragging an entry to the borders.
     *
     * @see <a href="https://fullcalendar.io/docs/dragScroll">https://fullcalendar.io/docs/dragScroll</a>
     * @param dragScrollActive activate drag scroll
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#DRAG_SCROLL} instead.
     */
    @Deprecated
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
    /**
     * Tries to find the calendar view based on the given client-side value. Empty, when the view name is not known
     * on the Java side (can be the case with unregistered custom views).
     *
     * @param clientSideValue view's client-side value to lookup
     * @return calendar view
     */
    @SuppressWarnings("unchecked")
    public <T extends CalendarView> Optional<T> lookupViewName(String clientSideValue) {
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
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_DISPLAY} instead.
     *             Accepts a {@link DisplayMode} directly.
     */
    @Deprecated
    public void setEntryDisplay(DisplayMode displayMode) {
        this.setOption(Option.ENTRY_DISPLAY, displayMode != null ? displayMode : DisplayMode.AUTO);
    }

    /**
     * Restricts the calendar so the user cannot navigate before {@code start}. Dates before this date are grayed
     * out and the previous-navigation buttons stop at this boundary. The end of the valid range remains open.
     * <br><br>
     * Use {@link #setValidRange(LocalDate, LocalDate)} to set both boundaries at once, or
     * {@link #clearValidRange()} to remove the restriction.
     *
     * @param start earliest date the user can navigate to; must not be null
     */
    public void setValidRangeStart(LocalDate start) {
        setValidRange(start, null);
    }

    /**
     * Restricts the calendar so the user cannot navigate past {@code end}. Dates after this date are grayed
     * out and the next-navigation buttons stop at this boundary. The start of the valid range remains open.
     * <br><br>
     * Use {@link #setValidRange(LocalDate, LocalDate)} to set both boundaries at once, or
     * {@link #clearValidRange()} to remove the restriction.
     *
     * @param end latest date the user can navigate to; must not be null
     */
    public void setValidRangeEnd(LocalDate end) {
        setValidRange(null, end);
    }

    /**
     * Restricts navigation to the given date range. Dates outside the range are grayed out and navigation
     * buttons stop at the boundaries. Pass {@code null} for either boundary to leave it open-ended.
     * Pass {@code null} for both to remove all restrictions (same as {@link #clearValidRange()}).
     * <br><br>
     * A static valid range set here is overridden if {@link Option#VALID_RANGE} is also configured with a
     * {@link JsCallback}.
     *
     * @param start earliest navigable date, or {@code null} for open start
     * @param end   latest navigable date, or {@code null} for open end
     * @throws IllegalArgumentException if both are non-null and {@code start} is not before {@code end}
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
     * Removes any navigation restriction previously set by {@link #setValidRange}, {@link #setValidRangeStart},
     * or {@link #setValidRangeEnd}. Also removes a dynamic valid range callback ({@link Option#VALID_RANGE})
     * if one had been set before. The user can navigate freely again.
     */
    public void clearValidRange() {
        setValidRange(null, null);
        setOption(Option.VALID_RANGE, null);
    }

    /**
     * Clears the highlighted time-range selection created when the user drags across time slots while
     * {@code selectable} is active (see {@link Option#SELECTABLE}). The highlight remains visible until
     * either the user clicks somewhere on the calendar, or this method is called programmatically.
     * <br><br>
     * Call this after handling a {@link TimeslotsSelectedEvent} — for example, after opening a dialog
     * or creating a new entry — so the highlight does not linger. If the user will interact with a dialog
     * that contains a clickable button, that click will clear the selection naturally and this method is
     * not required.
     * <br><br>
     * Maps to FC's {@code calendar.unselect()}.
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






























    /**
     * Sets the week number calculation algorithm.
     *
     * @param calc week number calculation
     * @throws NullPointerException when null is passed
     * @see <a href="https://fullcalendar.io/docs/weekNumberCalculation">weekNumberCalculation</a>
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#WEEK_NUMBER_CALCULATION} instead.
     *             Accepts a {@link WeekNumberCalculation} directly.
     */
    @Deprecated
    public void setWeekNumberCalculation(WeekNumberCalculation calc) {
        Objects.requireNonNull(calc);
        setOption(Option.WEEK_NUMBER_CALCULATION, calc);
    }









    // -------------------------------------------------------------------------
    // Typed setters — Display options and render hooks
    // -------------------------------------------------------------------------


















    // ---- Accessibility, Touch, and Print Options ----









    // ---- Advanced and Niche Options ----




    /**
     * Constrains entry dragging and resizing to the specified business hours. Entries can only
     * be moved to or resized within business hours slots.
     *
     * @param hours business hours definition; must not be null
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">FC eventConstraint documentation</a>
     * @deprecated Use {@link #setOption(Option, Object)} with {@link Option#ENTRY_CONSTRAINT} instead.
     *             Accepts a {@link BusinessHours} directly via converter.
     */
    @Deprecated
    public void setEntryConstraint(BusinessHours hours) {
        Objects.requireNonNull(hours);
        setOption(Option.ENTRY_CONSTRAINT, hours.toJson());
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
    public void setViewSpecificOption(String viewType, String optionKey, Object value,
                                      @SuppressWarnings("rawtypes") JsonItemPropertyConverter... converters) {
        Objects.requireNonNull(viewType, "viewType must not be null");
        Objects.requireNonNull(optionKey, "optionKey must not be null");
        ObjectNode viewNode = viewSpecificOptionsMap.computeIfAbsent(viewType, k -> JsonFactory.createObject());
        if (value == null) {
            viewNode.remove(optionKey);
            if (viewNode.isEmpty()) {
                viewSpecificOptionsMap.remove(viewType);
            }
        } else {
            Object clientValue = value;
            for (JsonItemPropertyConverter<?, ?> c : converters) {
                if (c.supports(value)) {
                    @SuppressWarnings("unchecked")
                    JsonNode converted = ((JsonItemPropertyConverter<Object, Object>) c).toClientModel(value, null);
                    clientValue = converted;
                    break;
                }
            }
            viewNode.set(optionKey, JsonUtils.toJsonNode(clientValue));
        }
        syncViewSpecificOptions();
    }

    /**
     * Sets a view-specific option using the typed {@link Option} enum.
     * Converters registered on the option via {@link org.vaadin.stefan.fullcalendar.json.JsonConverter}
     * are applied automatically.
     *
     * @param viewType FullCalendar view type name
     * @param option   option enum constant
     * @param value    option value; pass {@code null} to remove
     * @see #setViewSpecificOption(String, String, Object, JsonItemPropertyConverter...)
     */
    public void setViewSpecificOption(String viewType, Option option, Object value) {
        Objects.requireNonNull(option, "option must not be null");
        setViewSpecificOption(viewType, option.getOptionKey(), value,
                option.getConverters().toArray(JsonItemPropertyConverter[]::new));
    }

    /**
     * Sets a view-specific option using the typed {@link CalendarView} enum to identify the view.
     * Converters registered on the option via {@link org.vaadin.stefan.fullcalendar.json.JsonConverter}
     * are applied automatically.
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


    /**
     * Enumeration of options that can be applied to the calendar via
     * {@link FullCalendar#setOption(Option, Object)}. Contains only options that affect
     * the client-side library, not internal options. Missing options can be set manually
     * via one of the {@link FullCalendar#setOption} overloads using a raw string key.
     *
     * <p><b>Naming convention:</b> Java constant names use the {@code ENTRY_} prefix
     * where the underlying FullCalendar JS option uses {@code event}
     * (e.g., {@code ENTRY_BACKGROUND_COLOR} → {@code eventBackgroundColor}).
     * Constants that provide an explicit string key via their constructor override this rule.
     * Exceptions to the {@code ENTRY_} convention exist for historical reasons and are noted
     * in their individual Javadoc ({@link #DISPLAY_EVENT_END}, {@link #FORCE_EVENT_DURATION},
     * {@link #PROGRESSIVE_EVENT_RENDERING}).
     * Note: the {@code ENTRY} → {@code EVENT} substitution is applied to all occurrences of
     * {@code ENTRY} in the constant name, not only at the prefix. For example,
     * {@code DISPLAY_ENTRY_TIME} maps to {@code displayEventTime}.
     *
     * <p><b>Format objects</b> (used by {@link #DAY_HEADER_FORMAT}, {@link #SLOT_LABEL_FORMAT},
     * {@link #ENTRY_TIME_FORMAT}, {@link #LIST_DAY_FORMAT}, {@link #LIST_DAY_SIDE_FORMAT},
     * {@link #WEEK_NUMBER_FORMAT}, {@link #DAY_POPOVER_FORMAT}, and similar):
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
         * Deprecated alias for {@link #DAY_HEADERS}.
         *
         * @see <a href="https://fullcalendar.io/docs/dayHeaders">dayHeaders</a>
         * @deprecated use {@link #DAY_HEADERS} instead
         */
        @Deprecated
        COLUMN_HEADER("dayHeaders"),

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
         * Default background color for all entries.
         * <dl>
         *   <dt>Type</dt> <dd>CSS color string</dd>
         * </dl>
         * Can be overridden per-entry via {@link Entry#setBackgroundColor(String)}.
         *
         * @see <a href="https://fullcalendar.io/docs/eventBackgroundColor">eventBackgroundColor</a>
         */
        ENTRY_BACKGROUND_COLOR,

        /**
         * Default border color for all entries.
         * <dl>
         *   <dt>Type</dt> <dd>CSS color string</dd>
         * </dl>
         * Can be overridden per-entry via {@link Entry#setBorderColor(String)}.
         *
         * @see <a href="https://fullcalendar.io/docs/eventBorderColor">eventBorderColor</a>
         */
        ENTRY_BORDER_COLOR,

        /**
         * Default combined background and border color for all entries.
         * <dl>
         *   <dt>Type</dt> <dd>CSS color string</dd>
         * </dl>
         * Equivalent to setting both {@link #ENTRY_BACKGROUND_COLOR} and {@link #ENTRY_BORDER_COLOR}.
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
         * Does not apply to dayGrid view (use {@link #MAX_ENTRIES_PER_DAY} instead).
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
         * Default text color for all entries.
         * <dl>
         *   <dt>Type</dt> <dd>CSS color string</dd>
         * </dl>
         * Can be overridden per-entry via {@link Entry#setTextColor(String)}.
         *
         * @see <a href="https://fullcalendar.io/docs/eventTextColor">eventTextColor</a>
         */
        ENTRY_TEXT_COLOR,

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
         * Parent DOM element for the "mirror" element shown while dragging an entry.
         * Accepts a {@link JsCallback} with a function returning a DOM element.
         * <dl>
         *   <dt>Type</dt>    <dd>{@link JsCallback} returning a DOM element</dd>
         *   <dt>Default</dt> <dd>none (mirror uses the calendar's scroll container)</dd>
         * </dl>
         * <p>Example:
         * <pre>{@code calendar.setOption(Option.FIXED_MIRROR_PARENT,
         *     JsCallback.of("function() { return document.body; }"));}</pre>
         *
         * @see <a href="https://fullcalendar.io/docs/fixedMirrorParent">fixedMirrorParent</a>
         */
        FIXED_MIRROR_PARENT,

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
         * Format of the right-side date column in list view (typically time info for each entry).
         * <dl>
         *   <dt>Type</dt> <dd>format object</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/listDaySideFormat">listDaySideFormat</a>
         */
        LIST_DAY_SIDE_FORMAT,

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
        MAX_ENTRIES_PER_DAY("dayMaxEvents"),

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
         * Minimum pixel width of each month cell in multi-month view before wrapping to next row.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
         *   <dt>Default</dt> <dd>auto-calculated</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/multiMonthMinWidth">multiMonthMinWidth</a>
         */
        MULTI_MONTH_MIN_WIDTH,

        /**
         * Format of each month's title in multi-month view.
         * <dl>
         *   <dt>Type</dt> <dd>format object with {@code month}, {@code year}, and other properties</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/multiMonthTitleFormat">multiMonthTitleFormat</a>
         */
        MULTI_MONTH_TITLE_FORMAT,

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
         * Format of the time slot labels.
         * <dl>
         *   <dt>Type</dt> <dd>format object with {@code hour}, {@code minute}, {@code meridiem}, and other properties</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slotLabelFormat">slotLabelFormat</a>
         */
        SLOT_LABEL_FORMAT,

        /**
         * Interval between visible time slot labels.
         * <dl>
         *   <dt>Type</dt>    <dd>{@link java.time.Duration} | {@link java.time.LocalTime} | duration string (e.g. {@code "HH:MM:SS"})</dd>
         *   <dt>Default</dt> <dd>auto-computed based on {@link #SLOT_DURATION}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slotLabelInterval">slotLabelInterval</a>
         */
        @JsonConverter(DurationConverter.class)
        SLOT_LABEL_INTERVAL,

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
         * Keep the footer scrollbar visible when scrolling.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code true}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/stickyFooterScrollbar">stickyFooterScrollbar</a>
         */
        STICKY_FOOTER_SCROLLBAR,

        /**
         * Keep header dates visible when scrolling vertically in timegrid views.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>auto-detected based on view height</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/stickyHeaderDates">stickyHeaderDates</a>
         */
        STICKY_HEADER_DATES,

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
         * Text prepended to week numbers (e.g., "W" in "W1", "W2").
         * <dl>
         *   <dt>Type</dt>    <dd>{@code string}</dd>
         *   <dt>Default</dt> <dd>locale-dependent</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/weekText">weekText</a>
         */
        WEEK_TEXT,

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
         * Customizes the labels on the native FC toolbar buttons.
         * <dl>
         *   <dt>Type</dt> <dd>map of button/view name to display label (e.g., {@code Map.of("today", "Heute", "month", "Monat")})</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/buttonText">buttonText</a>
         */
        NATIVE_TOOLBAR_BUTTON_TEXT("buttonText"),

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
         * Separator text between two adjacent dates in the toolbar title (e.g., "Jan 1 – Jan 7").
         * <dl>
         *   <dt>Type</dt>    <dd>{@code string}</dd>
         *   <dt>Default</dt> <dd>{@code " \u2013 "} (en dash with spaces)</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/defaultRangeSeparator">defaultRangeSeparator</a>
         */
        NATIVE_TOOLBAR_DEFAULT_RANGE_SEPARATOR("defaultRangeSeparator"),

        /**
         * Maximum number of entry rows in month view cells.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code integer} | {@code true} (auto-calculate based on cell height)</dd>
         *   <dt>Default</dt> <dd>auto-calculated</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
         */
        DAY_MAX_EVENT_ROWS,

        /**
         * Format of the "+N more" popover title in month view.
         * <dl>
         *   <dt>Type</dt> <dd>format object with {@code month}, {@code day}, {@code year}, and other properties</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/dayPopoverFormat">dayPopoverFormat</a>
         */
        DAY_POPOVER_FORMAT,

        /**
         * Show end time on entry elements.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code true} for timed entries in agenda views, {@code false} for all-day entries</dd>
         * </dl>
         * <p>Note: this constant uses the {@code EVENT_} naming convention instead of the usual
         * {@code ENTRY_} prefix, retained for historical reasons.
         *
         * @see <a href="https://fullcalendar.io/docs/displayEventEnd">displayEventEnd</a>
         */
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
         * <p>Note: this constant uses the {@code EVENT_} naming convention instead of the usual
         * {@code ENTRY_} prefix, retained for historical reasons.
         *
         * @see <a href="https://fullcalendar.io/docs/forceEventDuration">forceEventDuration</a>
         */
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
         * <p>Note: this constant uses the {@code EVENT_} naming convention instead of the usual
         * {@code ENTRY_} prefix, retained for historical reasons.
         *
         * @see <a href="https://fullcalendar.io/docs/progressiveEventRendering">progressiveEventRendering</a>
         */
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
         * Separator between start and end dates in the toolbar title area.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code string}</dd>
         *   <dt>Default</dt> <dd>{@code " \u2013 "} (en dash with spaces)</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/titleRangeSeparator">titleRangeSeparator</a>
         */
        NATIVE_TOOLBAR_TITLE_RANGE_SEPARATOR("titleRangeSeparator"),




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
         * CSP (Content Security Policy) nonce for dynamically generated {@code <style>} elements.
         * FullCalendar injects inline styles; if your page uses a Content Security Policy that
         * requires a nonce on inline styles, pass the nonce value here so FC can set it on its
         * generated style tags.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code Map.of("nonce", nonceValue)}</dd>
         *   <dt>Default</dt> <dd>none (no nonce set)</dd>
         * </dl>
         * <p><b>Important:</b> this option must be set <em>before</em> the calendar is attached
         * to the DOM (before FC initialises and generates its first style tags). Setting it
         * after attachment has no effect.
         * <p>Example:
         * <pre>{@code calendar.setOption(Option.CONTENT_SECURITY_POLICY, Map.of("nonce", myNonce));}</pre>
         *
         * @see <a href="https://fullcalendar.io/docs/content-security-policy">contentSecurityPolicy</a>
         */
        CONTENT_SECURITY_POLICY,

        /**
         * CSS selector for custom elements to scroll when dragging entries near their edges.
         * <dl>
         *   <dt>Type</dt>    <dd>CSS selector {@code string} | {@code String[]} | {@code Collection<String>} (joined with comma)</dd>
         *   <dt>Default</dt> <dd>calendar viewport</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/dragScrollEls">dragScrollEls</a>
         */
        @JsonConverter(StringArrayConverter.class)
        DRAG_SCROLL_ELS,

        /**
         * Accessible labels ({@code aria-label}) for the native FC toolbar buttons.
         * <dl>
         *   <dt>Type</dt> <dd>map of button name to hint {@code string} (e.g., {@code Map.of("today", "Go to today")})</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/hints">buttonHints</a>
         */
        NATIVE_TOOLBAR_BUTTON_HINTS("buttonHints"),

        /**
         * Accessible label for the view-switcher buttons in the native FC toolbar.
         * <dl>
         *   <dt>Type</dt> <dd>{@code string} (use {@code $0} as placeholder for the view name, e.g., {@code "Switch to $0 view"})</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/hints">viewHint</a>
         */
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

        /**
         * Accessible hint ({@code aria-label}) for time display.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code string}</dd>
         *   <dt>Default</dt> <dd>locale-dependent</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/hints">timeHint</a>
         */
        TIME_HINT,

        /**
         * Accessible hint ({@code aria-label}) for entry elements.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code string}</dd>
         *   <dt>Default</dt> <dd>locale-dependent</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/hints">eventHint</a>
         */
        ENTRY_HINT,


        // ---- Render hooks: Entry ----
        /**
         * Add CSS classes to entry wrapper elements. Called when an entry is rendered.
         * {@code info.event} has {@code getCustomProperty()} method available.
         * <dl>
         *   <dt>Function</dt>  <dd>{@code JsCallback.of("function(arg) { return ['css-class']; }")}</dd>
         *   <dt>Arguments</dt> <dd>{@code {event, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventClassNames</a>
         */
        ENTRY_CLASS_NAMES("eventClassNames"),

        /**
         * Customize the inner content of entry elements. Called when an entry is rendered.
         * {@code info.event} has {@code getCustomProperty()} available.
         * <dl>
         *   <dt>Function</dt>  <dd>{@code JsCallback.of("function(arg) { return { html: '<b>' + arg.event.title + '</b>' }; }")}</dd>
         *   <dt>Arguments</dt> <dd>{@code {event, timeText, isStart, isEnd, isMirror, isPast, isFuture, isToday, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventContent</a>
         */
        ENTRY_CONTENT("eventContent"),

        /**
         * Called after an entry element is added to the DOM. Use for post-render setup (e.g., tooltips).
         * {@code info.event} has {@code getCustomProperty()} available.
         * <dl>
         *   <dt>Function</dt>  <dd>{@code JsCallback.of("function(arg) { arg.el.title = arg.event.title; }")}</dd>
         *   <dt>Arguments</dt> <dd>{@code {event, el, view}}</dd>
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
         * Called before an entry element is removed from the DOM. Use for cleanup.
         * {@code info.event} has {@code getCustomProperty()} available.
         * <dl>
         *   <dt>Function</dt>  <dd>{@code JsCallback.of("function(arg) { ... }")}</dd>
         *   <dt>Arguments</dt> <dd>{@code {event, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventWillUnmount</a>
         */
        ENTRY_WILL_UNMOUNT("eventWillUnmount"),

        // ---- Render hooks: Day Cell ----
        /**
         * Add CSS classes to day cell {@code <td>} elements. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellClassNames</a>
         */
        DAY_CELL_CLASS_NAMES("dayCellClassNames"),

        /**
         * Customize the content inside day cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellContent</a>
         */
        DAY_CELL_CONTENT("dayCellContent"),

        /**
         * Called after a day cell element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellDidMount</a>
         */
        DAY_CELL_DID_MOUNT("dayCellDidMount"),

        /**
         * Called before a day cell element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellWillUnmount</a>
         */
        DAY_CELL_WILL_UNMOUNT("dayCellWillUnmount"),

        // ---- Render hooks: Day Header ----
        /**
         * Add CSS classes to day header {@code <th>} elements. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, isToday, isPast, isFuture, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderClassNames</a>
         */
        DAY_HEADER_CLASS_NAMES("dayHeaderClassNames"),

        /**
         * Customize the content inside day header cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, isToday, isPast, isFuture, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderContent</a>
         */
        DAY_HEADER_CONTENT("dayHeaderContent"),

        /**
         * Called after a day header element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, isToday, isPast, isFuture, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderDidMount</a>
         */
        DAY_HEADER_DID_MOUNT("dayHeaderDidMount"),

        /**
         * Called before a day header element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, isToday, isPast, isFuture, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderWillUnmount</a>
         */
        DAY_HEADER_WILL_UNMOUNT("dayHeaderWillUnmount"),

        // ---- Render hooks: Slot Label ----
        /**
         * Add CSS classes to time slot label cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelClassNames</a>
         */
        SLOT_LABEL_CLASS_NAMES("slotLabelClassNames"),

        /**
         * Customize the content inside time slot label cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelContent</a>
         */
        SLOT_LABEL_CONTENT("slotLabelContent"),

        /**
         * Called after a slot label element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelDidMount</a>
         */
        SLOT_LABEL_DID_MOUNT("slotLabelDidMount"),

        /**
         * Called before a slot label element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, text, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLabelWillUnmount</a>
         */
        SLOT_LABEL_WILL_UNMOUNT("slotLabelWillUnmount"),

        // ---- Render hooks: Slot Lane ----
        /**
         * Add CSS classes to time slot lane cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, time, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneClassNames</a>
         */
        SLOT_LANE_CLASS_NAMES("slotLaneClassNames"),

        /**
         * Customize the content inside time slot lane cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, time, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneContent</a>
         */
        SLOT_LANE_CONTENT("slotLaneContent"),

        /**
         * Called after a slot lane element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, time, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneDidMount</a>
         */
        SLOT_LANE_DID_MOUNT("slotLaneDidMount"),

        /**
         * Called before a slot lane element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, time, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slot-render-hooks">slotLaneWillUnmount</a>
         */
        SLOT_LANE_WILL_UNMOUNT("slotLaneWillUnmount"),

        // ---- Render hooks: View ----
        /**
         * Add CSS classes to the view root element. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewClassNames</a>
         */
        VIEW_CLASS_NAMES("viewClassNames"),

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

        // ---- Render hooks: Now Indicator ----
        /**
         * Add CSS classes to now indicator elements. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, isAxis, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorClassNames</a>
         */
        NOW_INDICATOR_CLASS_NAMES("nowIndicatorClassNames"),

        /**
         * Customize the content inside now indicator elements. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, isAxis, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorContent</a>
         */
        NOW_INDICATOR_CONTENT("nowIndicatorContent"),

        /**
         * Called after a now indicator element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, isAxis, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorDidMount</a>
         */
        NOW_INDICATOR_DID_MOUNT("nowIndicatorDidMount"),

        /**
         * Called before a now indicator element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, isAxis, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/now-indicator-render-hooks">nowIndicatorWillUnmount</a>
         */
        NOW_INDICATOR_WILL_UNMOUNT("nowIndicatorWillUnmount"),

        // ---- Render hooks: Week Number ----
        /**
         * Add CSS classes to week number cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, num, text, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberClassNames</a>
         */
        WEEK_NUMBER_CLASS_NAMES("weekNumberClassNames"),

        /**
         * Customize the content inside week number cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, num, text, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberContent</a>
         */
        WEEK_NUMBER_CONTENT("weekNumberContent"),

        /**
         * Called after a week number element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, num, text, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberDidMount</a>
         */
        WEEK_NUMBER_DID_MOUNT("weekNumberDidMount"),

        /**
         * Called before a week number element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {date, num, text, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/week-number-render-hooks">weekNumberWillUnmount</a>
         */
        WEEK_NUMBER_WILL_UNMOUNT("weekNumberWillUnmount"),

        // ---- Render hooks: More Link ----
        /**
         * Add CSS classes to the "+N more" link element. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {num, text, shortText, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkClassNames</a>
         */
        MORE_LINK_CLASS_NAMES("moreLinkClassNames"),

        /**
         * Customize the content of the "+N more" link. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {num, text, shortText, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkContent</a>
         */
        MORE_LINK_CONTENT("moreLinkContent"),

        /**
         * Called after a more link element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {num, text, shortText, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkDidMount</a>
         */
        MORE_LINK_DID_MOUNT("moreLinkDidMount"),

        /**
         * Called before a more link element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {num, text, shortText, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkWillUnmount</a>
         */
        MORE_LINK_WILL_UNMOUNT("moreLinkWillUnmount"),

        // ---- Render hooks: No Entries ----
        /**
         * Add CSS classes to the "No events" message in list view. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsClassNames</a>
         */
        NO_ENTRIES_CLASS_NAMES("noEventsClassNames"),

        /**
         * Customize the "No events" message in list view. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsContent</a>
         */
        NO_ENTRIES_CONTENT("noEventsContent"),

        /**
         * Called after a no-entries element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsDidMount</a>
         */
        NO_ENTRIES_DID_MOUNT("noEventsDidMount"),

        /**
         * Called before a no-entries element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsWillUnmount</a>
         */
        NO_ENTRIES_WILL_UNMOUNT("noEventsWillUnmount"),

        // ---- Render hooks: All Day ----
        /**
         * Add CSS classes to the all-day section header cell. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {text, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayClassNames</a>
         */
        ALL_DAY_CLASS_NAMES("allDayClassNames"),

        /**
         * Customize the content inside the all-day header cell. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {text, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayContent</a>
         */
        ALL_DAY_CONTENT("allDayContent"),

        /**
         * Called after the all-day header element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {text, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayDidMount</a>
         */
        ALL_DAY_DID_MOUNT("allDayDidMount"),

        /**
         * Called before the all-day header element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {text, view, el}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/all-day-render-hooks">allDayWillUnmount</a>
         */
        ALL_DAY_WILL_UNMOUNT("allDayWillUnmount"),

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

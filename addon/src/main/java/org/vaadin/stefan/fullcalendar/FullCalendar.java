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

import org.vaadin.stefan.fullcalendar.option.CalendarLocale;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.CustomCalendarView.AnonymousCustomCalendarView;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@NpmPackage(value = "fullcalendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "temporal-polyfill", version = "1.0.5") // peer dependency of fullcalendar 7.1.0 (^1.0.1)
@NpmPackage(value = "@fullcalendar/rrule", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/google-calendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/icalendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "ical.js", version = "2.0.1")

@JsModule("./vaadin-full-calendar/full-calendar.ts")
@CssImport("./vaadin-full-calendar/full-calendar-styles.css")
@CssImport("./vaadin-full-calendar/vaadin-theme.css")
// imported by full-calendar.ts, named here so that a change to them triggers a frontend bundle rebuild
@JsModule("./vaadin-full-calendar/legacy-class-names.ts")
@JsModule("./vaadin-full-calendar/vaadin-theme.ts")
@Tag("vaadin-full-calendar")
public class FullCalendar extends Component implements HasStyle, HasSize {

    /**
     * The FullCalendar version used in this addon, for the core package and its plugins. Third-party libraries such as
     * ical.js and temporal-polyfill have their own version numbers.
     */
    public static final String FC_CLIENT_VERSION = "7.1.0";

    /**
     * This is the default duration of a timed entry in hours. Will be dynamically settable in a later version.
     */
    public static final int DEFAULT_TIMED_EVENT_DURATION = 1;

    /**
     * This is the default duration of a daily entry in days. Will be dynamically settable in a later version.
     */
    public static final int DEFAULT_DAY_EVENT_DURATION = 1;

    private static final String INITIAL_OPTIONS = "initialOptions";
    private static final String THEME_PROPERTY = "fcTheme";
    /**
     * The options the client has its own handler for and the typed fields of which run after it, see
     * {@link #contributeOptions(Map)}. The client registers the handlers in {@code createEventHandlers}.
     */
    static final Set<String> CALLBACKS_AFTER_CLIENT_HANDLER = Set.of(
            "viewDidMount", "navLinkDayClick", "navLinkWeekClick");
    /** The key in a JsCallback marker that makes the client run the callback after its own handler. */
    static final String AFTER_CLIENT_HANDLER_KEY = "__afterClientHandler";

    /**
     * Caches the entries of the last client fetch for entry based events. Cleared and repopulated
     * on every {@link #fetchEntriesFromServer(ObjectNode)}, so it only ever holds one viewport worth.
     */
    private final Map<String, Entry> lastFetchedEntries = new HashMap<>();

    /**
     * The raw options in their client form, by library name. {@link #syncOptions()} sends them to the client.
     */
    final Map<String, JsonNode> rawOptions = new HashMap<>();

    /** The typed options, see {@link #options()}. Their values lie under the raw options. */
    final CalendarOptions calendarOptions = createCalendarOptions();
    final ViewOptions viewOptions = createViewOptions();
    /**
     * The options of single view types, by client-side view name, see {@link FullCalendarOptions#view(CalendarView)}.
     */
    final Map<String, ViewOptions> perViewOptions = new HashMap<>();
    /** The raw options of single view types, by client-side view name, see {@link FullCalendarViewOptions}. */
    final Map<String, Map<String, JsonNode>> perViewRawOptions = new HashMap<>();
    private final FullCalendarOptions fullCalendarOptions = createOptions();

    /** The options the client got last, see {@link #syncOptions()}. */
    private final Map<String, JsonNode> lastSentOptions = new HashMap<>();
    private boolean optionSyncScheduled;

    /**
     * True once the client got the option state through the element property {@code initialOptions}. From then on
     * the client gets the changes as calls.
     */
    private boolean initialStateSent;

    /**
     * The ObjectNode passed to {@link #FullCalendar(ObjectNode)}, kept so the addon-default
     * logic in {@link #postConstruct()} can tell whether the caller already provided a
     * particular option. {@code null} for the no-arg / int-limit constructors.
     */
    private ObjectNode constructorInitialOptions;

    /** The height of {@link #setHeight(String)}, which {@link #contributeOptions(Map)} puts in as the option. */
    private String height;

    private EntryProvider<? extends Entry> entryProvider;
    private final List<Registration> entryProviderDataListeners = new LinkedList<>();

    private final Map<String, CustomCalendarView> customCalendarViews = new LinkedHashMap<>();
    private boolean customViewsSetExplicitly;

    // used to keep the amount of timeslot selected listeners. when 0, then selectable option is auto removed
    private int timeslotsSelectedListenerCount;

    private Timezone browserTimezone;
    /** Set while the time zone is {@link Timezone#LOCAL}, see {@link #applyTimeZone(Timezone)}. */
    private boolean followBrowserTimezone;

    private String currentViewName;
    private LocalDate currentIntervalStart;
    private LocalDate currentIntervalEnd;
    private CalendarView currentView;

    // only ever accessed under the Vaadin session lock (all component mutation is), so no extra guarding needed
    private boolean refreshAllEntriesRequested;

    private final Map<String, String> customNativeEventsMap = new LinkedHashMap<>();
    private boolean autoProvideEntryIdOnClient = true;

    /**
     * Server-side registry of remote entry sources, keyed by source id.
     */
    private final Map<String, RemoteEntrySource<?>> remoteEntrySourceRegistry = new LinkedHashMap<>();

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

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefaultLocale()}).
     * <p></p>
     * Uses {@link InMemoryEntryProvider} by default.
     */
    public FullCalendar() {
        postConstruct();
    }

    /**
     * Creates a new instance with custom initial options. This allows a full override of the default
     * initial options, that the calendar would normally receive. Theoretically you can set all options,
     * as long as they are not based on a client side variable (as for instance "plugins" or "locales").
     * Complex objects are possible, too, for instance for view-specific settings.
     * Please refer to the official FullCalendar documentation regarding potential options.
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
     * Also, options set this way are raw options, they have no typed field.
     * {@link FullCalendarOptions#getRawOption(String)} returns them, but the field getters of {@link #options()}
     * do not.
     * <p></p>
     * Like every raw option, an initial option wins over the field with the same key. Replace or remove it with
     * {@link FullCalendarOptions#setRawOption(String, Object)}.
     * <p></p>
     * The time zone ({@code timeZone}) is not kept as an option of its own but goes into
     * {@link FullCalendarOptions#getTimeZone()}, because the server converts the entry times with it. {@code "local"}
     * becomes {@link Timezone#LOCAL}. Without the key, the time zone is {@link Timezone#UTC}.
     * <p></p>
     * Uses {@link InMemoryEntryProvider} by default.
     *
     * @param initialOptions initial options
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when the time zone is not a valid time zone id
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     */
    public FullCalendar(ObjectNode initialOptions) {
        // the time zone goes into its field, because the server converts entry times with it. It cannot be a raw option.
        String timeZoneKey = "timeZone";
        Timezone timeZone = null;
        if (initialOptions.has(timeZoneKey)) {
            if (initialOptions.hasNonNull(timeZoneKey)) {
                String id = initialOptions.get(timeZoneKey).asString();
                timeZone = isLocalTimezone(id) ? Timezone.LOCAL : parseTimezone(id);
            }
            initialOptions = initialOptions.deepCopy();
            initialOptions.remove(timeZoneKey);
        }
        // the calendar always has a locale, a null leaves the field's initial value
        if (initialOptions.path("locale").isNull()) {
            initialOptions = initialOptions.deepCopy();
            initialOptions.remove("locale");
        }

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

        this.constructorInitialOptions = initialOptions;
        initialOptions.properties().forEach(property -> {
            // views become custom views
            if (!property.getKey().equals("views")) {
                rawOptions.put(property.getKey(), property.getValue().deepCopy());
            }
        });

        if (timeZone != null) {
            options().setTimeZone(timeZone);
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
        currentViewName = currentView.getClientSideValue();

        addDatesRenderedListener(event -> {
            currentIntervalStart = event.getIntervalStart();
            currentIntervalEnd = event.getIntervalEnd();
        });

        addViewSkeletonRenderedListener(event -> {
            currentViewName = event.getViewName();
            currentView = event.getCalendarView().orElse(null);
        });

        if (constructorInitialOptions == null
                || !constructorInitialOptions.hasNonNull("height")) {
            setHeightFull(); // default from previous versions
        }

        /* to allow class based styling for custom subclasses */
        addClassName("vaadin-full-calendar");
        setTheme(FullCalendarTheme.VAADIN);

        optionsChanged(); // the option state reaches the client on the first attach, even if no option is set
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // An initial attach always gets a new client element. Flow also reports the move of a view kept with
        // @PreserveOnRefresh to a new UI and a client resync as initial attach, and the detach check in onDetach
        // misses both: the old UI sends no further response, and on a resync the calendar is attached again at once.
        if (attachEvent.isInitialAttach()) {
            initialStateSent = false;
        }
        if (initialStateSent) {
            // Flow kept the client element (detach and attach in one round trip), but discarded the calls queued for
            // it. So every option goes out again, and every key that is no longer present goes out as a removal.
            lastSentOptions.replaceAll((key, value) -> JsonFactory.createNull());
        }
        // a sync scheduled in another UI is dropped by Flow when the component moves, so schedule it again
        optionSyncScheduled = false;
        optionsChanged();

        if(!attachEvent.isInitialAttach()) {
            getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(this, executionContext -> {
                    flushOptions();
                    getElement().callJsFunction("restoreStateFromServer",
                            JsonUtils.toJsonNode(currentViewName),
                            JsonUtils.toJsonNode(currentIntervalStart));

                    if (!remoteEntrySourceRegistry.isEmpty()) {
                        ArrayNode sourcesArray = JsonFactory.createArray();
                        remoteEntrySourceRegistry.values().stream().map(RemoteEntrySource::toJson).forEach(sourcesArray::add);
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
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        // If the calendar is still detached when the response goes out, the browser removes the client element. An
        // attach then creates a new one from the property. Registered on the UI, because a task of the detached
        // calendar waits for the attach.
        UI ui = detachEvent.getUI();
        ui.beforeClientResponse(ui, context -> {
            if (!isAttached()) {
                initialStateSent = false;
            }
        });
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
        flushOptions();
        getElement().callJsFunction("next");
    }

    /**
     * Moves to the previous interval (e. g. previous month if current view is monthly based).
     */
    public void previous() {
        flushOptions();
        getElement().callJsFunction("previous");
    }

    /**
     * Moves to the current interval (e. g. current month if current view is monthly based).
     */
    public void today() {
        flushOptions();
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
            entryProviderDataListeners.forEach(Registration::remove);
            entryProviderDataListeners.clear();

            if (this.entryProvider != null) {
                this.entryProvider.setCalendar(null);
            }

            this.entryProvider = entryProvider;
            entryProvider.setCalendar(this);

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

        lookupViewName(view.getClientSideValue()).orElseThrow(() -> new IllegalArgumentException("Unknown view: "
                + view.getClientSideValue()
                + ". If you want to use a custom view, please register it first by using setCustomCalendarViews()."));

        currentView = view;
        currentViewName = view.getClientSideValue();

        flushOptions();
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
     * The current view of this instance. Empty, if the current view could not be matched with a built-in view or a
     * registered custom view.
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
        flushOptions();
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
        flushOptions();
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
        flushOptions();
        getElement().callJsFunction("scrollToTime", duration.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * Returns the options of this calendar as typed fields. The returned object is live: its setters change the
     * calendar, also through a reference kept for a later request.
     * <p>
     * Like any access to a component, the options need the lock of the Vaadin session. Outside a request, for example
     * in a background thread, change them inside {@code UI.access(…)}.
     *
     * @return the options of this calendar
     */
    public FullCalendarOptions options() {
        return fullCalendarOptions;
    }

    // The factories run while the fields of this class are initialized, before a subclass constructor. An override
    // must not use fields of the subclass.

    /**
     * Creates the data object of the calendar-wide options. The scheduler overrides it to add its options.
     *
     * @return a new, empty data object
     */
    protected CalendarOptions createCalendarOptions() {
        return new CalendarOptions();
    }

    /**
     * Creates a data object of the options that can apply per view, for the calendar-wide values and for each view
     * type. The scheduler overrides it to add its options.
     *
     * @return a new, empty data object
     */
    protected ViewOptions createViewOptions() {
        return new ViewOptions();
    }

    /**
     * Creates the object {@link #options()} returns. The scheduler overrides it to return its own options object.
     *
     * @return the options object of this calendar
     */
    protected FullCalendarOptions createOptions() {
        return new FullCalendarOptions(this);
    }

    /**
     * Schedules the option sync for the next response. A subclass calls it when a value it puts in with
     * {@link #contributeOptions(Map)} changes.
     * <p>
     * Until the client got the initial state, the element property {@code initialOptions} is also updated at once.
     * {@link FullCalendarOptions} calls this before the accessor runs, so the property misses that one change until
     * the sync. The browser gets the property only with a response, and the sync before it writes the current state.
     */
    protected void optionsChanged() {
        if (!initialStateSent) {
            // ponytail: rewrites the whole property on each change before the first sync, so a raw option change is
            // in it also without a response. This is quadratic in the options set before attach, which is fine for
            // the few hundred options there are.
            writeInitialOptions(currentOptions());
        }
        if (!optionSyncScheduled) {
            optionSyncScheduled = true;
            getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(this, context -> syncOptions()));
        }
    }

    /**
     * Brings the client in line with the option state. Until the client got the initial state, it is written to the
     * element property {@code initialOptions}, from which the client creates its calendar. Afterwards the changed
     * options are sent in one call, a removed option as null.
     */
    private void syncOptions() {
        optionSyncScheduled = false;
        Map<String, JsonNode> current = currentOptions();

        ObjectNode changes = JsonFactory.createObject();
        current.forEach((key, value) -> {
            if (!value.equals(lastSentOptions.get(key))) {
                changes.set(key, value);
            }
        });
        lastSentOptions.keySet().stream().filter(key -> !current.containsKey(key)).forEach(changes::putNull);

        if (!initialStateSent) {
            writeInitialOptions(current);
            initialStateSent = true;
        } else if (!changes.isEmpty()) {
            getElement().callJsFunction("setOptions", changes);
        }

        lastSentOptions.clear();
        lastSentOptions.putAll(current);
    }

    /**
     * Sends pending option changes at once, so a direct client call that follows runs under them. Before the client
     * got the initial state, nothing is sent, because the sync before the response writes the property.
     * <p>
     * While detached nothing is sent either. The call would wait for the attach and could then reach a new client
     * element, which the property already created with the options. The attach resends the options anyway.
     * <p>
     * A subclass calls it before it calls the client directly, as the scheduler does before it sends resources.
     */
    protected void flushOptions() {
        if (initialStateSent && isAttached()) {
            syncOptions();
        }
    }

    /** The option state as the client gets it: the typed fields, with the raw options over them. */
    private Map<String, JsonNode> currentOptions() {
        Map<String, JsonNode> current = FullCalendarOptions.toJson(viewOptions);
        current.putAll(FullCalendarOptions.toJson(calendarOptions));
        if (followBrowserTimezone) {
            // the field holds the zone the browser reported, which the browser shows anyway as "local". Sending the
            // concrete zone would make FullCalendar refetch the entries.
            current.put("timeZone", JsonUtils.toJsonNode(Timezone.LOCAL));
        }
        viewsJson().ifPresent(views -> current.put("views", views));
        contributeOptions(current);
        current.putAll(rawOptions);
        return current;
    }

    /**
     * Adds or replaces option values the calendar computes, in their client form by library name. The sync calls it
     * each time after it has put in the typed fields and the {@code views} object, and lays the raw options over the
     * result, so a raw option still wins. A change that affects the computed values has to call {@link #optionsChanged()}.
     * <p>
     * This calendar puts in {@code height} from {@link #setHeight(String)} and {@code eventDidMount}: the callback of
     * {@link FullCalendarOptions#getEntryDidMount()} merged with the native entry listeners and the automatic entry id, see
     * {@link #addEntryNativeEventListener(String, String)} and {@link #setAutoProvideEntryIdOnClient(boolean)}. It
     * also marks the field callbacks of the options the client has its own handler for, such as {@code viewDidMount},
     * so the client runs them after its handler. A raw option is not marked and replaces the client's handler. An
     * override calls this method to keep both.
     *
     * @param options the option values the client gets, to add to or replace in
     */
    protected void contributeOptions(Map<String, JsonNode> options) {
        if (height != null) {
            options.put("height", JsonUtils.toJsonNode(height));
        }
        String merged = buildEntryDidMountMerged();
        if (merged != null) {
            options.put("eventDidMount", JsCallback.of(merged).toMarkerJson());
        }
        CALLBACKS_AFTER_CLIENT_HANDLER.forEach(key -> {
            if (options.get(key) instanceof ObjectNode marker && JsCallback.isMarker(marker)) {
                options.put(key, marker.deepCopy().put(AFTER_CLIENT_HANDLER_KEY, true));
            }
        });
    }

    /**
     * The library's {@code views} object: the definitions of the custom views first, then the values of
     * {@link FullCalendarOptions#view(CalendarView)} over them per view and key, then the raw options of the views.
     */
    private Optional<ObjectNode> viewsJson() {
        ObjectNode views = JsonFactory.createObject();
        customCalendarViews.forEach((name, view) -> {
            if (view.getViewSettings() != null) {
                views.set(name, view.getViewSettings().deepCopy());
            }
        });
        FullCalendarOptions.toViewsJson(perViewOptions).ifPresent(values -> values.properties()
                .forEach(entry -> mergeView(views, entry.getKey(), (ObjectNode) entry.getValue())));
        perViewRawOptions.forEach((name, values) -> {
            if (!values.isEmpty()) {
                mergeView(views, name, JsonFactory.createObject().setAll(values));
            }
        });

        return views.isEmpty() ? Optional.empty() : Optional.of(views);
    }

    private static void mergeView(ObjectNode views, String name, ObjectNode values) {
        ObjectNode merged = views.get(name) instanceof ObjectNode existing ? existing : JsonFactory.createObject();
        merged.setAll(values.deepCopy());
        views.set(name, merged);
    }

    private void writeInitialOptions(Map<String, JsonNode> options) {
        getElement().setPropertyJson(INITIAL_OPTIONS, JsonFactory.createObject().setAll(options));
    }

    /**
     * Sets the height of the calendar. Null removes the height, as {@link #setSizeUndefined()} does. A raw {@code height}
     * option wins over it, see {@link FullCalendarOptions#setRawOption(String, Object)}.
     *
     * @param height the height, for example {@code "100%"} or {@code "500px"}, or null
     */
    @Override
    public void setHeight(String height) {
        // the library option, not the style Vaadin sets, because the library sizes the calendar by its option
        this.height = height;
        optionsChanged();
    }

    protected String toClientSideLocale(Locale locale) {
        return locale.toLanguageTag().toLowerCase();
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
     * entry's DOM element automatically on render. You may also provide a custom {@code eventDidMount}
     * function via {@link FullCalendarOptions#setEntryDidMount(JsCallback)}. The calendar registers the native
     * listeners after your function has run, whatever form your function has. The library also mounts the drag
     * preview of an entry, so its element gets the listeners too. Adding a native event listener after the calendar
     * is attached takes effect immediately.
     * <br><br>
     * Inside the native event callback you may access the entry DOM element via the event's
     * {@code currentTarget} or {@code target} property.  For the full set of available parameters
     * in the surrounding {@code eventDidMount} hook, see the
     * <a href="https://fullcalendar.io/docs/event-render-hooks">official FullCalendar docs</a>.
     * @param eventName javascript event name
     * @param eventCallback javascript event callback to be hooked to the event
     * @return registration to remove the native event listener
     */
    public Registration addEntryNativeEventListener(String eventName, String eventCallback) {
        customNativeEventsMap.put(eventName, eventCallback);
        optionsChanged();

        return Registration.once(() -> {
            customNativeEventsMap.remove(eventName);
            optionsChanged();
        });
    }

    /**
     * JS snippet the merged eventDidMount callback runs first, as its own function, when
     * {@link #isAutoProvideEntryIdOnClient()} is {@code true}. Assigns {@code id="entry-<entryId>"}
     * to the start segment of each rendered entry, with a {@code -<resourceId>} suffix
     * when the entry is assigned to more than one resource (Scheduler resource views).
     * Drag-preview elements (mirrors) and non-start segments of multi-day entries are
     * skipped so the id remains unique.
     */
    static final String DEFAULT_ENTRY_ID_ASSIGNMENT_SNIPPET =
            "if (arguments[0].el && arguments[0].el.classList && arguments[0].el.classList.contains('fc-event-mirror')) return;\n"
                    + "if (arguments[0].event.id && arguments[0].isStart) {\n"
                    // FC's Scheduler plugin patches EventApi.prototype.getResources even for
                    // plain (non-scheduler) calendars when the plugin is on the classpath.
                    // Its body is `this._def.resourceIds.map(...)` which throws for entries
                    // that were never set up with resource support (_def.resourceIds is
                    // undefined). Swallow the error here; we only need the resource id for
                    // multi-resource uniqueness, which by definition means the entry HAS
                    // resources, so a throw means "no resources, plain entry-<id>".
                    + "  var _resources = [];\n"
                    + "  try { var _resFn = arguments[0].event.getResources;"
                    + " if (_resFn) _resources = _resFn.call(arguments[0].event) || []; } catch (_e) { _resources = []; }\n"
                    + "  var _resEl = arguments[0].el.closest ? arguments[0].el.closest('[data-resource-id]') : null;\n"
                    + "  var _resId = _resEl ? _resEl.getAttribute('data-resource-id') : null;\n"
                    + "  arguments[0].el.id = (_resources.length > 1 && _resId)\n"
                    + "    ? 'entry-' + arguments[0].event.id + '-' + _resId\n"
                    + "    : 'entry-' + arguments[0].event.id;\n"
                    + "}\n";

    /**
     * Builds the merged eventDidMount function string from three layers:
     * the default id-assignment snippet (when {@link #isAutoProvideEntryIdOnClient()} is true),
     * the user callback set via {@link FullCalendarOptions#setEntryDidMount(JsCallback)}, and native entry event
     * listeners registered via {@link #addEntryNativeEventListener(String, String)}.
     * <p>
     * The merged function calls the default snippet first, the user callback next and registers the native listeners
     * last. This lets the user callback override the default id by reassigning {@code info.el.id}. The snippet and
     * the user callback each run as their own function with {@code this} and all arguments, so the user callback may
     * have any function form, and a {@code return} in one layer does not skip the others.
     * <p>
     * The user callback alone is returned as it is. Returns {@code null} only when all three layers are empty.
     * <p>
     * Package-private for testing.
     */
    String buildEntryDidMountMerged() {
        JsCallback entryDidMount = calendarOptions.getEntryDidMount();
        String userCallback = entryDidMount != null ? entryDidMount.getJsFunction() : null;
        boolean hasUser = StringUtils.isNotBlank(userCallback);

        if (!autoProvideEntryIdOnClient && customNativeEventsMap.isEmpty()) {
            return hasUser ? userCallback : null;
        }

        StringBuilder merged = new StringBuilder("function(info) {\n");
        if (autoProvideEntryIdOnClient) {
            merged.append("(function() {\n")
                    .append(DEFAULT_ENTRY_ID_ASSIGNMENT_SNIPPET)
                    .append("}).apply(this, arguments);\n");
        }
        if (hasUser) {
            // A trailing semicolon would end the statement inside the parentheses, and the line break keeps a trailing
            // line comment from swallowing the closing parenthesis.
            merged.append("(")
                    .append(userCallback.replaceFirst(";+$", ""))
                    .append("\n).apply(this, arguments);\n");
        }
        customNativeEventsMap.forEach((eventName, eventCallback) -> merged
                .append("arguments[0].el.addEventListener('")
                .append(eventName.replace("\\", "\\\\").replace("'", "\\'"))
                .append("', ")
                .append(eventCallback)
                .append(");\n"));
        merged.append("}");

        return merged.toString();
    }

    /**
     * Returns the time zone the offset helpers of entries and events convert with, the value of the time zone field.
     * {@link Timezone#LOCAL} converts with UTC.
     */
    Timezone getTimezoneForOffsets() {
        return calendarOptions.getTimeZone();
    }

    /**
     * Sets the time zone field. {@link Timezone#LOCAL} sets the browser's zone if it is known, and lets each later
     * report of the browser replace it. Any other zone ends that.
     */
    void applyTimeZone(Timezone timeZone) {
        Objects.requireNonNull(timeZone);
        followBrowserTimezone = Timezone.LOCAL.equals(timeZone);
        calendarOptions.setTimeZone(followBrowserTimezone && browserTimezone != null ? browserTimezone : timeZone);
        optionsChanged();
    }

    /**
     * FullCalendar's {@code "local"} is the time zone of the browser, {@link Timezone#LOCAL}.
     */
    private static boolean isLocalTimezone(String id) {
        return "local".equalsIgnoreCase(id);
    }

    /**
     * Turns a time zone id into a {@link Timezone}.
     */
    private static Timezone parseTimezone(String id) {
        try {
            return new Timezone(ZoneId.of(id));
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Unknown time zone id: " + id, e);
        }
    }

    /**
     * This method returns the timezone sent by the browser. The calendar takes it as its time zone while that is
     * {@link Timezone#LOCAL}, see {@link FullCalendarOptions#setTimeZone(Timezone)}.
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
            if (followBrowserTimezone) {
                // the client gets "local" either way, so there is nothing to sync
                calendarOptions.setTimeZone(browserTimezone);
            }
            getEventBus().fireEvent(new BrowserTimezoneObtainedEvent(this, false, browserTimezone));
        }
    }

    /**
     * Force the client side instance to re-render its content.
     */
    public void render() {
        flushOptions();
        getElement().callJsFunction("renderCalendar");
    }

    /**
     * Registers a listener to be informed when a timeslot click event occurred.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotClickedListener(ComponentEventListener<TimeslotClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedEvent.class, listener);
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
     * @since 7.2.0
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
     * @since 7.2.0
     */
    public void setAutoRevertUnappliedEntryChanges(boolean autoRevert) {
        this.autoRevertUnappliedEntryChanges = autoRevert;
    }

    /**
     * Returns whether the calendar automatically exposes the server-side entry id as a DOM
     * {@code id} attribute on the DOM element of each client-rendered entry. Default is {@code true}.
     *
     * @return true if the client-side id is provided automatically
     * @see #setAutoProvideEntryIdOnClient(boolean)
     * @since 7.2.0
     */
    public boolean isAutoProvideEntryIdOnClient() {
        return autoProvideEntryIdOnClient;
    }

    /**
     * Controls whether the server-side entry id is automatically propagated to the client as a
     * DOM {@code id} attribute on the DOM element of each rendered entry, so server-side components
     * (e.g. {@code Popover}) can anchor to a specific entry via {@code document.getElementById}.
     * The method only toggles client-side id <b>publication</b>; it does not generate ids
     * server-side — the id on the {@link Entry} itself is unaffected.
     * <p>
     * When enabled (the default), an internal {@code eventDidMount} hook sets
     * {@code id="entry-<entryId>"} on the start segment of each rendered entry.
     * For entries assigned to more than one resource in a Scheduler resource view,
     * the id is suffixed with the resource id ({@code id="entry-<entryId>-<resourceId>"})
     * to preserve HTML id-uniqueness.
     * <p>
     * <b>Limitations:</b>
     * <ul>
     *     <li>For multi-day entries in day/week/month views, only the <b>start segment</b>
     *     receives the id; continuation segments remain id-free.</li>
     *     <li>Drag-preview ("mirror") elements are not tagged.</li>
     *     <li>For anchoring to the segment the user actually clicked, prefer the element
     *     delivered in the click event over {@code getElementById}.</li>
     * </ul>
     * If you set a custom {@code eventDidMount} callback via
     * {@link FullCalendarOptions#setEntryDidMount(JsCallback)}, your callback
     * runs after the default snippet, so you can reassign {@code info.el.id} to override it.
     *
     * @param autoProvide true to enable automatic client-side id publication (default),
     *                    false to disable
     * @since 7.2.0
     */
    public void setAutoProvideEntryIdOnClient(boolean autoProvide) {
        if (this.autoProvideEntryIdOnClient == autoProvide) {
            return;
        }
        this.autoProvideEntryIdOnClient = autoProvide;
        optionsChanged();
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
     * @since 7.2.0
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
    public Registration addTimeslotsSelectedListener(ComponentEventListener<TimeslotsSelectedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedEvent.class, listener);
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
     * Requires {@link FullCalendarViewOptions#getNavLinks() navLinks} to be {@code true}.
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
     * Requires {@link FullCalendarViewOptions#getNavLinks() navLinks} to be {@code true}.
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
     * Requires {@link FullCalendarViewOptions#getDroppable() droppable} to be set to {@code true}.
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
     * onto any calendar that has {@link FullCalendarViewOptions#getDroppable() droppable} set to {@code true}.
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
     * Requires {@link FullCalendarViewOptions#getDroppable() droppable} to be set to {@code true}.
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

    /**
     * Adds a remote entry source to this calendar. The browser will fetch entries from this source directly,
     * bypassing the server-side {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
     * <br><br>
     * A server-side registry entry is kept so the source can be restored on reattachment.
     * <br><br>
     * Returns a {@link Registration} that removes this source when invoked.
     *
     * @param source entry source to add; must not be null
     * @return a registration that removes the source
     * @throws NullPointerException if source is null
     */
    public Registration addRemoteEntrySource(RemoteEntrySource<?> source) {
        Objects.requireNonNull(source, "source must not be null");
        remoteEntrySourceRegistry.put(source.getId(), source);
        flushOptions();
        getElement().callJsFunction("addEventSource", source.toJson());
        return () -> removeRemoteEntrySource(source.getId());
    }

    /**
     * Removes the remote entry source with the given id from this calendar.
     * Does nothing if no source with that id has been added.
     *
     * @param id id of the source to remove; must not be null
     * @throws NullPointerException if id is null
     */
    public void removeRemoteEntrySource(String id) {
        Objects.requireNonNull(id, "id must not be null");
        remoteEntrySourceRegistry.remove(id);
        flushOptions();
        getElement().callJsFunction("removeEventSource", id);
    }

    /**
     * Replaces all current remote entry sources with the given collection.
     * Previously added sources are removed. If the collection is empty, all client-side
     * sources are cleared.
     * <br><br>
     * Returns a {@link Registration} that clears all remote entry sources when invoked.
     *
     * @param sources new set of entry sources; must not be null
     * @return a registration that clears all remote entry sources
     * @throws NullPointerException if sources is null
     */
    public Registration setRemoteEntrySources(java.util.Collection<? extends RemoteEntrySource<?>> sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        remoteEntrySourceRegistry.clear();
        sources.forEach(s -> remoteEntrySourceRegistry.put(s.getId(), s));
        ArrayNode array = JsonFactory.createArray();
        sources.stream().map(RemoteEntrySource::toJson).forEach(array::add);
        flushOptions();
        getElement().callJsFunction("setEventSources", array);
        return () -> setRemoteEntrySources(java.util.Collections.emptyList());
    }

    /**
     * Returns an unmodifiable view of all registered remote entry sources.
     *
     * @return collection of registered entry sources
     */
    public java.util.Collection<RemoteEntrySource<?>> getRemoteEntrySources() {
        return java.util.Collections.unmodifiableCollection(remoteEntrySourceRegistry.values());
    }

    /**
     * Returns the remote entry source with the given ID, or empty if no such source is registered.
     *
     * @param id entry source id; must not be null
     * @return the entry source, or empty
     * @throws NullPointerException if id is null
     */
    public Optional<RemoteEntrySource<?>> getRemoteEntrySourceById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(remoteEntrySourceRegistry.get(id));
    }

    /**
     * Forces all entry sources to re-fetch their data immediately. This includes both the server-side
     * {@link EntryProvider} and any remote entry sources added via {@link #addRemoteEntrySource}.
     * <br><br>
     * To refresh only a single remote entry source, use {@link #refetchRemoteEntrySource(String)}.
     */
    public void refetchEvents() {
        flushOptions();
        getElement().callJsFunction("refetchEvents");
    }

    /**
     * Forces a single <em>remote</em> entry source to re-fetch its data. Only the source with the given id is
     * refreshed; all other sources remain untouched.
     * <br><br>
     * <strong>Important:</strong> This method only works for remote entry sources added via
     * {@link #addRemoteEntrySource} (e.g. {@link JsonFeedEntrySource}, {@link GoogleCalendarEntrySource},
     * {@link ICalendarEntrySource}). It cannot be used to refresh the server-side {@link EntryProvider} — use
     * {@link #refetchEvents()} or the entry provider's own {@code refresh} methods for that.
     *
     * @param sourceId the id of the remote entry source to refetch; must not be null
     * @throws NullPointerException when null is passed
     * @see #refetchEvents()
     * @see <a href="https://fullcalendar.io/docs/EventSource-refetch">EventSource::refetch</a>
     */
    public void refetchRemoteEntrySource(String sourceId) {
        Objects.requireNonNull(sourceId);
        flushOptions();
        getElement().executeJs("var s = this.calendar.getEventSourceById($0); if (s) s.refetch();", sourceId);
    }

    /**
     * Registers a listener for when a remote entry source fails to load.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addRemoteEntrySourceFailureListener(ComponentEventListener<RemoteEntrySourceFailureEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(RemoteEntrySourceFailureEvent.class, listener);
    }

    /**
     * Registers a listener for when an entry from an entry source ({@link RemoteEntrySource}) is dragged to a new
     * time slot. Fires instead of {@link EntryDroppedEvent} when the dropped entry's id is not in the server-side cache.
     * <br><br>
     * Requires that drag/drop is enabled on the source via {@link RemoteEntrySource#withEditable(boolean) withEditable(true)}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addRemoteEntryDroppedListener(ComponentEventListener<RemoteEntryDroppedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(RemoteEntryDroppedEvent.class, listener);
    }

    /**
     * Registers a listener for when an entry from an entry source ({@link RemoteEntrySource}) is resized.
     * Fires instead of {@link EntryResizedEvent} when the resized entry's id is not in the server-side cache.
     * <br><br>
     * Requires that resize is enabled on the source via {@link RemoteEntrySource#withEditable(boolean) withEditable(true)}.
     *
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addRemoteEntryResizedListener(ComponentEventListener<RemoteEntryResizedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(RemoteEntryResizedEvent.class, listener);
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
     * Clears the highlighted time-range selection created when the user drags across time slots while
     * {@code selectable} is active (see {@link FullCalendarViewOptions#getSelectable() selectable}). The highlight
     * remains visible until either the user clicks somewhere on the calendar, or this method is called
     * programmatically.
     * <br><br>
     * Call this after handling a {@link TimeslotsSelectedEvent} — for example, after opening a dialog
     * or creating a new entry — so the highlight does not linger. If the user will interact with a dialog
     * that contains a clickable button, that click will clear the selection naturally and this method is
     * not required.
     * <br><br>
     * Maps to FullCalendar's {@code calendar.unselect()}.
     */
    public void clearSelection() {
        getElement().executeJs("this.calendar.unselect()");
    }

    /**
     * Returns an unmodifiable copy of the custom calendar views. Any changes to this instance's custom views will
     * be reflected in the returned map.
     * <br><br>
     * This map contains any custom calendar view, that has been registered via
     * {@link #setCustomCalendarViews(CustomCalendarView...)} plus anonymous instances for any
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
     * registered custom views via the FullCalendar builder.
     * @param customCalendarViews custom calendar views
     */
    public void setCustomCalendarViews(CustomCalendarView... customCalendarViews) {
        if (isAttached()) {
            throw new UnsupportedOperationException("Views can only be set before the component is attached");
        }

        // a flag, because the anonymous views of the constructor JSON are in the same map
        if (customViewsSetExplicitly) {
            throw new UnsupportedOperationException("Custom views can only be set once");
        }
        customViewsSetExplicitly = true;

        for (CustomCalendarView customCalendarView : customCalendarViews) {
            this.customCalendarViews.put(customCalendarView.getClientSideValue(), customCalendarView);
        }
        optionsChanged();
    }

    /**
     * Sets the FullCalendar theme of this calendar. A FullCalendar theme is neither a Vaadin application theme nor a
     * theme variant, see {@link FullCalendarTheme}.
     * <p>
     * Use a constant of {@link FullCalendarTheme} or the name of a custom theme registered in the browser with
     * {@code FullCalendar.registerTheme(name, loader)}. The theme can be changed at any time, also while the calendar
     * is shown.
     * <p>
     * A name that is not registered in the browser logs an error to the browser console. The calendar keeps the
     * theme it had before, or shows without a theme if it had none yet.
     * <p>
     * Default is {@link FullCalendarTheme#VAADIN}.
     *
     * @param theme theme name
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when the name is blank
     */
    public void setTheme(String theme) {
        Objects.requireNonNull(theme, "theme");
        if (theme.isBlank()) {
            throw new IllegalArgumentException("The theme name must not be blank");
        }

        getElement().setProperty(THEME_PROPERTY, theme);
    }

    /**
     * Returns the name of the FullCalendar theme set for this calendar.
     *
     * @return theme name
     * @see #setTheme(String)
     */
    public String getTheme() {
        return getElement().getProperty(THEME_PROPERTY);
    }
}

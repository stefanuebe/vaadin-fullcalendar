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
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.CustomCalendarView.AnonymousCustomCalendarView;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
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
@NpmPackage(value = "fullcalendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "temporal-polyfill", version = "1.0.5") // peer dependency of fullcalendar 7.1.0 (^1.0.1)
@NpmPackage(value = "@fullcalendar/rrule", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/google-calendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/icalendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "ical.js", version = "2.0.1")

@JsModule("./vaadin-full-calendar/full-calendar.ts")
@CssImport("./vaadin-full-calendar/full-calendar-styles.css")
@Tag("vaadin-full-calendar")
public class FullCalendar extends Component implements HasStyle, HasSize, HasTheme, LocaleChangeObserver {

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

    private static final String JSON_INITIAL_OPTIONS = "initialJsonOptions";
    private static final String INITIAL_OPTIONS = "initialOptions";

    /**
     * Caches the entries of the last client fetch for entry based events. Cleared and repopulated
     * on every {@link #fetchEntriesFromServer(ObjectNode)}, so it only ever holds one viewport worth.
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

    /**
     * The ObjectNode passed to {@link #FullCalendar(ObjectNode)}, kept so the addon-default
     * logic in {@link #postConstruct()} can tell whether the caller already provided a
     * particular option. {@code null} for the no-arg / int-limit constructors.
     */
    private ObjectNode constructorInitialOptions;
    private final Map<String, Object> serverSideOptions = new HashMap<>();

    private EntryProvider<? extends Entry> entryProvider;
    private final List<Registration> entryProviderDataListeners = new LinkedList<>();

    private final Map<String, CustomCalendarView> customCalendarViews = new LinkedHashMap<>();

    // used to keep the amount of timeslot selected listeners. when 0, then selectable option is auto removed
    private int timeslotsSelectedListenerCount;

    private Timezone browserTimezone;
    private boolean autoBrowserTimezone;
    private boolean autoUiLocale;

    private String currentViewName;
    private LocalDate currentIntervalStart;
    private LocalDate currentIntervalEnd;
    private CalendarView currentView;

    // only ever accessed under the Vaadin session lock (all component mutation is), so no extra guarding needed
    private boolean refreshAllEntriesRequested;

    private final Map<String, String> customNativeEventsMap = new LinkedHashMap<>();
    private JsCallback userEntryDidMountCallback;
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

    private final Map<String, ObjectNode> viewSpecificOptionsMap = new LinkedHashMap<>();

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefaultLocale()}).
     * <p></p>
     * Uses {@link InMemoryEntryProvider} by default.
     */
    public FullCalendar() {
        setOption(Option.LOCALE, CalendarLocale.getDefaultLocale());
        setOption(Option.DAY_MAX_ENTRIES, false);
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
        this.constructorInitialOptions = initialOptions;

        if (!initialOptions.hasNonNull(Option.LOCALE.getOptionKey())) {
            // fallback to prevent strange locale effects on the client side
            setOption(Option.LOCALE, CalendarLocale.getDefaultLocale());
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

        // Addon default: editable=true. FC's native default is false; since #212 the per-entry
        // editable flag is no longer force-pushed, so without this the out-of-the-box UX would
        // flip to "nothing draggable". Don't override an explicit value in the initialOptions ctor.
        if (constructorInitialOptions == null
                || !constructorInitialOptions.hasNonNull(Option.EDITABLE.getOptionKey())) {
            setOption(Option.EDITABLE, true);
        }
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
                + ". If you want to use a custom view, please register it first by using addCustomView()."));

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
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link Option} constants for type safety (e.g. {@code setOption(Option.LOCALE, myLocale)}).
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
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link Option} constants for type safety (e.g. {@code setOption(Option.LOCALE, myLocale)}).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #setOption(Option, Object)} with the typed value. The option's converter creates the
     * client-side value from it, so the client and {@link #getOption(Option)} cannot get out of step.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setOption(Option option, Object value, Object valueForServerSide) {
        setOption(option.getOptionKey(), value, valueForServerSide, option.getConverters());
    }

    /**
     * Sets an option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link Option} constants for type safety (e.g. {@code setOption(Option.LOCALE, myLocale)}).
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
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link Option} constants for type safety (e.g. {@code setOption(Option.LOCALE, myLocale)}).
     * <p>
     * For a full overview of possible options have a look at the FullCalendar documentation
     * (<a href='https://fullcalendar.io/docs'>https://fullcalendar.io/docs</a>).
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @param converters         optional converters to apply to the value
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #setOption(String, Object, JsonItemPropertyConverter...)}. A converter creates the
     * client-side value and the passed value is stored on the server side, so the two cannot get out of step.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
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

        // 0. A time zone given as id is kept as Timezone, so getOption() and the offset helpers can rely on it
        if (Option.TIMEZONE.getOptionKey().equals(option)) {
            if (valueForServerSide instanceof String id) {
                valueForServerSide = parseTimezone(id);
            } else if (valueForServerSide == null && value instanceof String id) {
                value = parseTimezone(id);
            }
        }

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

    @Override
    public void setHeight(String height) {
        // we use the calendar option as it would otherwise override the plain style set by Vaadin
        setOption(Option.HEIGHT, height);
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
     * JS snippet injected at the start of the merged eventDidMount callback when
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
     * the user callback set via {@link Option#ENTRY_DID_MOUNT}, and native entry event listeners
     * registered via {@link #addEntryNativeEventListener(String, String)}.
     * <p>
     * Ordering inside the merged function: default snippet first, user callback body next,
     * native listeners last. This lets the user callback override the default id by
     * reassigning {@code info.el.id}.
     * <p>
     * Returns {@code null} only when all three layers are empty.
     * <p>
     * Package-private for testing.
     */
    String buildEntryDidMountMerged() {
        String userCallback = userEntryDidMountCallback != null ? userEntryDidMountCallback.getJsFunction() : null;
        String defaultSnippet = autoProvideEntryIdOnClient ? DEFAULT_ENTRY_ID_ASSIGNMENT_SNIPPET : null;

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

        boolean hasUser = StringUtils.isNotBlank(userCallback);
        boolean hasDefault = defaultSnippet != null;
        boolean hasEvents = events != null;

        if (!hasUser && !hasDefault && !hasEvents) {
            return null;
        }

        if (hasUser) {
            String merged = userCallback;
            int openBrace = merged.indexOf('{');
            int closeBrace = merged.lastIndexOf('}');
            // Expression-body callbacks like "info => info.el.title = 'x'" have no braces —
            // there is no body to splice into. Return the user callback untouched; the default
            // snippet and native listeners are dropped for this call. This keeps the output
            // syntactically valid instead of corrupting it by prepending snippets outside
            // the expression.
            if (openBrace < 0 || closeBrace < openBrace) {
                return merged;
            }
            if (hasDefault) {
                merged = merged.substring(0, openBrace + 1) + "\n" + defaultSnippet + merged.substring(openBrace + 1);
            }
            if (hasEvents) {
                int finalClose = merged.lastIndexOf('}');
                merged = merged.substring(0, finalClose) + events + merged.substring(finalClose);
            }
            return merged;
        }

        StringBuilder sb = new StringBuilder("function(info) {\n");
        if (hasDefault) {
            sb.append(defaultSnippet);
        }
        if (hasEvents) {
            sb.append(events);
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns the current timezone of this calendar. Entries will be displayed related to this timezone.
     * Does not affect the server side times of entries, only their client side displayment.
     *
     * @return time zone
     * @deprecated use {@link #getOption(Option)} with {@link Option#TIMEZONE}. {@code getOption} is empty while the
     * option is not set, where this method returns UTC, which is not necessarily the time zone the client uses.
     * {@link #getOptionOrDefault(Option, Object)} with {@code Timezone.UTC} returns what this method returns
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public Timezone getTimezone() {
        return getTimezoneForOffsets();
    }

    /**
     * Time zone the offset helpers of entries and events convert with: the time zone option, or UTC, the add-on's
     * client default, while it is not set.
     */
    Timezone getTimezoneForOffsets() {
        return getOptionOrDefault(Option.TIMEZONE, Timezone.UTC);
    }

    /**
     * Turns a time zone id into a {@link Timezone}. FullCalendar's {@code "local"} is rejected, because the server
     * needs the real zone to compute entry offsets.
     */
    private static Timezone parseTimezone(String id) {
        if ("local".equalsIgnoreCase(id)) {
            throw new IllegalArgumentException("The time zone \"local\" is not supported, because the server needs the "
                    + "real zone. Use withAutoBrowserTimezone() to follow the browser's time zone.");
        }
        try {
            return new Timezone(ZoneId.of(id));
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Unknown time zone id: " + id, e);
        }
    }

    /**
     * Sets the timezone the calendar shall show. Does not affect the entries directly but only their client side displayment.
     *
     * @param timezone time zone to show the entries in
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#TIMEZONE}, which takes a {@link Timezone}
     * or a time zone id
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setTimezone(Timezone timezone) {
        Objects.requireNonNull(timezone);
        setOption(Option.TIMEZONE, timezone);
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
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#DAY_MAX_ENTRIES} and the number
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setMaxEntriesPerDay(int maxEntriesPerDay) {
        setOption(Option.DAY_MAX_ENTRIES, maxEntriesPerDay);
    }

    /**
     * When calling this method, the entries shown per day will be limited to fit the cell height.
     *
     * @see #setMaxEntriesPerDay(int)
     * @see #setMaxEntriesPerDayUnlimited()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">https://fullcalendar.io/docs/dayMaxEvents</a>
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#DAY_MAX_ENTRIES} and {@code true}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setMaxEntriesPerDayFitToCell() {
        setOption(Option.DAY_MAX_ENTRIES, true);
    }

    /**
     * When calling this method, the entries shown per day will be unlimited and take all the space needed.
     *
     * @see #setMaxEntriesPerDay(int)
     * @see #setMaxEntriesPerDayFitToCell()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">https://fullcalendar.io/docs/dayMaxEvents</a>
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#DAY_MAX_ENTRIES} and {@code false}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setMaxEntriesPerDayUnlimited() {
        setOption(Option.DAY_MAX_ENTRIES, false);
    }


    /**
     * Lets the calendar follow the time zone of the browser. The client reports it after attach, and the calendar
     * sets it as {@link Option#TIMEZONE}. If the browser time zone is already known, it is applied at once. A later
     * {@code setOption(Option.TIMEZONE, …)} applies until the browser reports its time zone again, for instance
     * after the calendar is attached anew.
     *
     * @return this instance
     */
    public FullCalendar withAutoBrowserTimezone() {
        autoBrowserTimezone = true;
        getBrowserTimezone().ifPresent(timezone -> setOption(Option.TIMEZONE, timezone));
        return this;
    }

    /**
     * Lets the calendar follow the locale of the UI. The calendar sets the UI locale as {@link Option#LOCALE} on
     * attach and whenever the UI locale changes ({@link UI#setLocale(Locale)}). Vaadin
     * derives the initial UI locale from the browser, unless the application sets it. A later
     * {@code setOption(Option.LOCALE, …)} applies until the next locale change.
     *
     * @return this instance
     */
    public FullCalendar withAutoUiLocale() {
        autoUiLocale = true;
        getUI().ifPresent(ui -> setOption(Option.LOCALE, ui.getLocale()));
        return this;
    }

    /**
     * Sets the UI locale as {@link Option#LOCALE}, if {@link #withAutoUiLocale()} is enabled. Called by Vaadin on
     * attach and when the UI locale changes. Does nothing otherwise.
     *
     * @param event locale change event
     */
    @Override
    public void localeChange(LocaleChangeEvent event) {
        if (autoUiLocale) {
            setOption(Option.LOCALE, event.getLocale());
        }
    }

    /**
     * This method returns the timezone sent by the browser. It is <b>not</b> automatically set as the FC's timezone,
     * unless {@link #withAutoBrowserTimezone()} is enabled.
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
            if (autoBrowserTimezone) {
                setOption(Option.TIMEZONE, browserTimezone);
            }
            getEventBus().fireEvent(new BrowserTimezoneObtainedEvent(this, false, browserTimezone));
        }
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(Option, Object, Object)}, that will be returned instead.
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
     * Returns the value of the given option like {@link #getOption(Option)}, or the given default when the option
     * is not set. Named like {@link Map#getOrDefault(Object, Object)}, because a {@code getOption} overload with a
     * {@code Boolean} default would clash with {@link #getOption(Option, boolean)}.
     * <pre>{@code
     * Timezone timezone = calendar.getOptionOrDefault(Option.TIMEZONE, Timezone.UTC);
     * }</pre>
     *
     * @param option       option
     * @param defaultValue value to return when the option is not set
     * @param <T>          type of value
     * @return the option's value or the default
     * @throws NullPointerException when null is passed as option
     * @throws ClassCastException at the call site when the option's value is not of the default's type
     */
    public <T> T getOptionOrDefault(Option option, T defaultValue) {
        return this.<T>getOption(option).orElse(defaultValue);
    }

    /**
     * Returns the value of the given option like {@link #getOption(String)}, or the given default when the option
     * is not set.
     *
     * @param option       option
     * @param defaultValue value to return when the option is not set
     * @param <T>          type of value
     * @return the option's value or the default
     * @throws NullPointerException when null is passed as option
     * @see #getOptionOrDefault(Option, Object)
     */
    public <T> T getOptionOrDefault(String option, T defaultValue) {
        return this.<T>getOption(option).orElse(defaultValue);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(Option, Object, Object)}, that will be returned instead.
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
     * Force the client side instance to re-render its content.
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
     * {@link #setOption(Option, Object)} with {@link Option#ENTRY_DID_MOUNT}, your callback
     * runs after the default snippet for brace-bodied functions (so you can reassign
     * {@code info.el.id} to override). For expression-body arrow callbacks the merge is
     * bypassed entirely and only your callback runs.
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
        applyEntryDidMountMerge(true);
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
     * Requires {@link Option#NAV_LINKS} to be {@code true}.
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
     * Requires {@link Option#NAV_LINKS} to be {@code true}.
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
     * Sets an action, that shall happen, when a user clicks the "+x more" link in the calendar (which occurs when the max
     * entries per day are exceeded). Default value is {@code POPUP}. Passing {@code null} will reset the default.
     * <p>For a custom JS function callback, use {@link #setOption(Option, Object)} with
     * {@link Option#MORE_LINK_CLICK} and {@link JsCallback} instead.
     *
     * @param moreLinkClickAction action to set
     * @see MoreLinkClickAction
     * @see Option#MORE_LINK_CLICK
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#MORE_LINK_CLICK}, which takes the
     * {@link MoreLinkClickAction}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setMoreLinkClickAction(MoreLinkClickAction moreLinkClickAction) {
        setOption(Option.MORE_LINK_CLICK, moreLinkClickAction);
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
     * Restricts the calendar so the user cannot navigate before {@code start}. Dates before this date are grayed
     * out and the previous-navigation buttons stop at this boundary. The end of the valid range remains open.
     *
     * @param start earliest date the user can navigate to; must not be null
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#VALID_RANGE} and
     * {@code new DateRange(start, null)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setValidRangeStart(LocalDate start) {
        setOption(Option.VALID_RANGE, new DateRange(start, null));
    }

    /**
     * Restricts the calendar so the user cannot navigate past {@code end}. Dates after this date are grayed
     * out and the next-navigation buttons stop at this boundary. The start of the valid range remains open.
     *
     * @param end latest date the user can navigate to; must not be null
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#VALID_RANGE} and
     * {@code new DateRange(null, end)}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setValidRangeEnd(LocalDate end) {
        setOption(Option.VALID_RANGE, new DateRange(null, end));
    }

    /**
     * Restricts navigation to the given date range. Dates outside the range are grayed out and navigation
     * buttons stop at the boundaries. Pass {@code null} for either boundary to leave it open-ended.
     * Pass {@code null} for both to remove all restrictions (same as {@link #clearValidRange()}).
     *
     * @param start earliest navigable date, or {@code null} for open start
     * @param end   latest navigable date, or {@code null} for open end
     * @throws IllegalArgumentException if both are non-null and {@code start} is not before {@code end}
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#VALID_RANGE} and a {@link DateRange}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setValidRange(LocalDate start, LocalDate end) {
        setOption(Option.VALID_RANGE, start == null && end == null ? null : new DateRange(start, end));
    }

    /**
     * Removes any navigation restriction previously set by {@link #setValidRange}, {@link #setValidRangeStart},
     * or {@link #setValidRangeEnd}. Also removes a dynamic valid range callback ({@link Option#VALID_RANGE})
     * if one had been set before. The user can navigate freely again.
     *
     * @deprecated use {@link #setOption(Option, Object)} with {@link Option#VALID_RANGE} and {@code null}
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void clearValidRange() {
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
     * Sets a view-specific option override. The option applies only when the calendar is
     * showing the specified view type. Multiple calls for the same view type accumulate;
     * each key is set independently.
     * <p>
     * Example — limit event rows only in month view:
     * <pre>{@code
     * calendar.setViewSpecificOption("dayGridMonth", Option.DAY_MAX_ENTRY_ROWS, 3);
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

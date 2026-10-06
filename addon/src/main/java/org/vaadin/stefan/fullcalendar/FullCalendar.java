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
import org.vaadin.stefan.fullcalendar.converters.LocalDateConverter;
import org.vaadin.stefan.fullcalendar.converters.LocaleConverter;
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
@NpmPackage(value = "fullcalendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "temporal-polyfill", version = "1.0.5") // peer dependency of fullcalendar 7.1.0 (^1.0.1)
@NpmPackage(value = "@fullcalendar/rrule", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/google-calendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/icalendar", version = FullCalendar.FC_CLIENT_VERSION)
@NpmPackage(value = "ical.js", version = "2.0.1")

@JsModule("./vaadin-full-calendar/full-calendar.ts")
@CssImport("./vaadin-full-calendar/full-calendar-styles.css")
@Tag("vaadin-full-calendar")
public class FullCalendar extends Component implements HasStyle, HasSize, HasTheme {

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
        setOption(Option.LOCALE, CalendarLocale.getDefaultLocale());
        setMaxEntriesPerDayUnlimited();
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
     * Example:
     * <pre>
     * // sends a client parseable version to client and stores original in server side
     * calendar.setOption(Option.LOCALE, locale.toLanguageTag().toLowerCase(), locale);
     *
     * // returns the original locale (as optional)
     * Optional&lt;Locale&gt; optionalLocale = calendar.getOption(Option.LOCALE)
     * </pre>
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link Option} constants for type safety (e.g. {@code setOption(Option.LOCALE, myLocale)}).
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
                    + "  try { var _resFn = arguments[0].event.getResources; if (_resFn) _resources = _resFn.call(arguments[0].event) || []; } catch (_e) { _resources = []; }\n"
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
        setOption(Option.DAY_MAX_ENTRIES, maxEntriesPerDay);
    }

    /**
     * When calling this method, the entries shown per day will be limited to fit the cell height.
     *
     * @see #setMaxEntriesPerDay(int)
     * @see #setMaxEntriesPerDayUnlimited()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">https://fullcalendar.io/docs/dayMaxEvents</a>
     */
    public void setMaxEntriesPerDayFitToCell() {
        setOption(Option.DAY_MAX_ENTRIES, true);
    }

    /**
     * When calling this method, the entries shown per day will be unlimited and take all the space needed.
     *
     * @see #setMaxEntriesPerDay(int)
     * @see #setMaxEntriesPerDayFitToCell()
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">https://fullcalendar.io/docs/dayMaxEvents</a>
     */
    public void setMaxEntriesPerDayUnlimited() {
        setOption(Option.DAY_MAX_ENTRIES, false);
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
     * {@code id} attribute on the client-rendered entry element. Default is {@code true}.
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
     * DOM {@code id} attribute on the rendered entry element, so server-side components
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






































    // -------------------------------------------------------------------------
    // Typed setters — Display options and render hooks
    // -------------------------------------------------------------------------


















    // ---- Accessibility, Touch, and Print Options ----









    // ---- Advanced and Niche Options ----







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

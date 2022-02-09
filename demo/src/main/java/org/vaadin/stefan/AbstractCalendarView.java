package org.vaadin.stefan;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import elemental.json.Json;
import elemental.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import org.vaadin.stefan.CalendarViewToolbar.CalendarViewToolbarBuilder;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.util.Set;

/**
 * A basic class for simple calendar views, e.g. for demo or testing purposes.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCalendarView extends VerticalLayout {
    private final EntryService entryService;
    private final EntryProvider<Entry> entryProvider;

    private final CalendarViewToolbar toolbar;
    private final FullCalendar calendar;

    public AbstractCalendarView() {
        entryService = initEntryService(EntryService.createInstance());

        calendar = createFullCalendar();
        CalendarViewToolbarBuilder toolbarBuilder = CalendarViewToolbar.builder()
                .calendar(calendar)
                .viewChangeable(true)
                .dateChangeable(true)
                .onSamplesCreated(this::onSamplesCreated)
                .onSamplesRemoved(this::onSamplesRemoved);

        CalendarViewToolbarBuilder modifiedToolbarBuilder = initToolbarBuilder(toolbarBuilder);
        toolbar = createToolbar(modifiedToolbarBuilder);

        entryProvider = createEntryProvider(entryService);
        if (entryProvider != null) {
            calendar.setEntryProvider(entryProvider);
        }

        calendar.setHeightByParent();
        calendar.addDatesRenderedListener(event -> {
            if (toolbar != null) {
                toolbar.updateInterval(event.getIntervalStart());
            }
        });
        calendar.addDayNumberClickedListener(event -> Notification.show("Clicked day number " + event.getDate()));
        calendar.addEntryClickedListener(this::onEntryClick);
        calendar.addEntryDroppedListener(this::onEntryDropped);
        calendar.addEntryResizedListener(this::onEntryResized);

        Component descriptionElement = createDescriptionElement();
        if (descriptionElement != null) {
            add(descriptionElement);
            setHorizontalComponentAlignment(Alignment.STRETCH, descriptionElement);
        }

        if (toolbar != null) {
            add(toolbar);
            setHorizontalComponentAlignment(Alignment.CENTER, toolbar);
        }

        add(calendar);

        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);

        setSizeFull();
    }

    /**
     * Creates the plain full calendar instance with all initial options. If {@link #createEntryProvider(EntryService)}
     * returns a custom entry provider, it will be set at a later point automatically.
     * <p></p>
     * Also the height is set afterwards.
     * <p></p>
     * Initializes the calendar by default with a custom time format, an entry limit of 3, no scheduler and timezone UTC.
     * You can override {@link #createFullCalendarBuilder(JsonObject)} to customize that or create your own builder.
     * @return calendar instance
     */
    private FullCalendar createFullCalendar() {
        JsonObject initialOptions = Json.createObject();
        JsonObject eventTimeFormat = Json.createObject();
        //{ hour: 'numeric', minute: '2-digit', timeZoneName: 'short' }
        eventTimeFormat.put("hour", "2-digit");
        eventTimeFormat.put("minute", "2-digit");
        eventTimeFormat.put("timeZoneName", "short");
        eventTimeFormat.put("meridiem",false);
        eventTimeFormat.put("hour12",false);
        initialOptions.put("eventTimeFormat", eventTimeFormat);

        return createFullCalendarBuilder(initialOptions).build();
    }

    /**
     * Creates the builder used by {@link #createFullCalendar()}. By default creates a builder with the initial
     * options and an entry limit of 3.
     * @param initialOptions initial options
     * @return FC builder
     */
    protected FullCalendarBuilder createFullCalendarBuilder(JsonObject initialOptions) {
        return FullCalendarBuilder.create()
                .withInitialOptions(initialOptions)
                .withEntryLimit(3);
    }

    /**
     * Allows modifying the used entry server. This method is called before all others. You may also return
     * a new instance to be used instead, but not null.
     * @param entryService entry service
     */
    protected EntryService initEntryService(EntryService entryService) {
        return entryService;
    }

    protected Component createDescriptionElement() {
        String description = createDescription();
        return description == null ? null : new Span(description);
    }

    protected String createDescription() {
        return null;
    }

    /**
     * Inits the toolbar. Calendar and the "onSample" callbacks are already set. Change view and date
     * parameters are also enabled by default. Either update the given variable or create a new one, if
     * necessary. Return null for no toolbar at all.
     * @param toolbarBuilder toolbar builder
     * @return modified or new instance
     */
    protected CalendarViewToolbarBuilder initToolbarBuilder(CalendarViewToolbarBuilder toolbarBuilder) {
        return toolbarBuilder;
    }

    /**
     * Creates the toolbar. The parameter might be null depending on a custom implementation of
     * {@link #initToolbarBuilder(CalendarViewToolbarBuilder)}. Return null if no toolbar shall
     * be available.
     * @param toolbarBuilder builder or null
     * @return toolbar or null
     */
    protected CalendarViewToolbar createToolbar(CalendarViewToolbarBuilder toolbarBuilder) {
        return toolbarBuilder != null ? toolbarBuilder.build() : null;
    }

    protected void onEntryResized(EntryResizedEvent event) {

    }

    protected void onEntryDropped(EntryDroppedEvent event) {
    }

    protected void onEntryClick(EntryClickedEvent event) {
    }


    protected EntryProvider<Entry> createEntryProvider(EntryService service) {
        return null;
    }

    protected void onSamplesCreated(Set<Entry> entries) {
        throw new UnsupportedOperationException("Implement me");
    }

    protected void onSamplesRemoved(Set<Entry> entries) {
        throw new UnsupportedOperationException("Implement me");
    }

}

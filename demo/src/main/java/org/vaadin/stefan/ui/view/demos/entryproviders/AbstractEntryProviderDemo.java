package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.notification.Notification;
import elemental.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.AbstractCalendarView;
import org.vaadin.stefan.CalendarViewToolbar;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;

import java.util.*;

/**
 * An abstract demo class for the different entry provider variants. Does not provide much functionality
 * beside the loading and displayment of entries plus some simple click, dropped and resized listeners.
 * <p></p>
 * Also delegates CRUD operations of the toolbar and the calendar to the entry service and refreshes
 * the entry provider based on the modifications.
 * <p></p>
 * The entry service is always instantiated with random data.
 * @author Stefan Uebe
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractEntryProviderDemo extends AbstractCalendarView {

    private EntryService entryService;
    private EntryProvider<Entry> entryProvider;

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        entryService = EntryService.createRandomInstance();
        entryProvider = createEntryProvider(entryService);

        return FullCalendarBuilder.create()
                .withEntryProvider(entryProvider)
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .build();
    }

    /**
     * Creates the demo's specific entry provider and initializes it with the necessary data.
     * @param entryService entry service
     * @return entry provider
     */
    protected abstract EntryProvider<Entry> createEntryProvider(EntryService entryService);

    @Override
    protected void onEntryClick(EntryClickedEvent event) {
        super.onEntryClick(event);
        Notification.show("Entry clicked " + event.getEntry().getId());
    }

    @Override
    protected void onEntryDropped(EntryDroppedEvent event) {
        super.onEntryDropped(event);
        Notification.show("Dropped entry " + event.getEntry().getId());
    }

    @Override
    protected void onEntryResized(EntryResizedEvent event) {
        super.onEntryResized(event);
        Notification.show("Resized entry " + event.getEntry().getId());
    }
}

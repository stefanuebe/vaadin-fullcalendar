package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.notification.Notification;
import elemental.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;

import java.util.Collections;

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

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        return super.createToolbar(toolbarBuilder.allowAddingRandomItemsInitially(false));
    }

    /**
     * Creates the demo's specific entry provider and initializes it with the necessary data.
     * @param entryService entry service
     * @return entry provider
     */
    protected abstract EntryProvider<Entry> createEntryProvider(EntryService<Entry> entryService);

    @Override
    protected void onTimeslotsSelected(TimeslotsSelectedEvent event) {
        Entry entry = createNewEntry();

        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());

        entry.setColor("green");
        entry.setCalendar(event.getSource());
        DemoDialog dialog = new DemoDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.open();
    }

    protected abstract Entry createNewEntry();

    @Override
    protected void onEntryClick(EntryClickedEvent event) {
        DemoDialog dialog = new DemoDialog(event.getEntry(), false);
        dialog.setSaveConsumer(this::onEntryChanged);
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
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

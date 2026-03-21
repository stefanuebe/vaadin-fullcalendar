package org.vaadin.stefan.ui.view.playground;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * Shared base class for the three entry provider test views.
 * Creates calendar + toolbar + dialog handlers.
 */
public abstract class AbstractEntryProviderView extends VerticalLayout {

    private final FullCalendar calendar;
    private final EntryProvider<Entry> entryProvider;
    private final EntryService entryService;

    public AbstractEntryProviderView() {
        setSizeFull();

        entryService = EntryService.createRandomInstance();
        entryProvider = createEntryProvider(entryService);

        calendar = FullCalendarBuilder.create()
                .withEntryProvider(entryProvider)
                .withEntryLimit(3)
                .build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setOption(FullCalendar.Option.WEEK_NUMBERS, true);
        calendar.setOption(FullCalendar.Option.SELECTABLE, true);

        // --- Toolbar ---
        PlaygroundToolbar toolbar = new PlaygroundToolbar(
                calendar,
                this::onEntriesCreated,
                this::onEntriesRemoved,
                false,  // no "Add random entries"
                true    // show "Add 1k entries"
        );
        add(toolbar);
        setHorizontalComponentAlignment(Alignment.CENTER, toolbar);

        // --- Event listeners ---
        calendar.addEntryClickedListener(this::onEntryClick);
        calendar.addTimeslotsSelectedListener(this::onTimeslotsSelected);
        calendar.addEntryDroppedListener(this::onEntryDropped);
        calendar.addEntryResizedListener(this::onEntryResized);

        add(calendar);
        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);
    }

    protected abstract EntryProvider<Entry> createEntryProvider(EntryService entryService);

    protected EntryService getEntryService() {
        return entryService;
    }

    protected EntryProvider<Entry> getEntryProvider() {
        return entryProvider;
    }

    protected FullCalendar getCalendar() {
        return calendar;
    }

    // --- Event handlers ---

    private void onEntryClick(EntryClickedEvent event) {
        PlaygroundDialog dialog = new PlaygroundDialog(event.getEntry(), false);
        dialog.setSaveConsumer(this::onEntryChanged);
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
    }

    private void onTimeslotsSelected(TimeslotsSelectedEvent event) {
        Entry entry = new Entry();
        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());
        entry.setCalendar(event.getSource());

        PlaygroundDialog dialog = new PlaygroundDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.open();
    }

    private void onEntryDropped(EntryDroppedEvent event) {
        event.applyChangesOnEntry();
        onEntryChanged(event.getEntry());
        Notification.show("Dropped entry " + event.getEntry().getId());
    }

    private void onEntryResized(EntryResizedEvent event) {
        event.applyChangesOnEntry();
        onEntryChanged(event.getEntry());
        Notification.show("Resized entry " + event.getEntry().getId());
    }

    // --- CRUD callbacks (abstract for provider-specific logic) ---

    protected abstract void onEntriesCreated(Collection<Entry> entries);

    protected abstract void onEntriesRemoved(Collection<Entry> entries);

    protected abstract void onEntryChanged(Entry entry);
}

package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This demo shows the usage of signal binding for reactive entry management.
 * Instead of using an EntryProvider with explicit refresh calls, entries are
 * managed via a {@link ListSignal}. The calendar updates automatically when
 * entries are added, removed, or modified through the signal.
 * <p>
 * This feature requires Vaadin 25.1 (Signals API, experimental).
 *
 * @author Stefan Uebe
 */
@Route(value = "signals", layout = MainLayout.class)
@MenuItem(label = "Signals")
public class SignalsDemo extends AbstractCalendarView {

    private ListSignal<Entry> entriesSignal;

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        entriesSignal = new ListSignal<>();

        // Pre-populate the signal with some sample entries
        EntryService<Entry> entryService = EntryService.createRandomInstance();
        entryService.streamEntries().forEach(entriesSignal::insertLast);

        return FullCalendarBuilder.create()
                .withSignalBinding(entriesSignal)
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .build();
    }

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        return super.createToolbar(toolbarBuilder.allowAddingRandomItemsInitially(false));
    }

    @Override
    protected void onTimeslotsSelected(TimeslotsSelectedEvent event) {
        Entry entry = new Entry();
        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());
        entry.setColor("green");

        DemoDialog dialog = new DemoDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.open();
    }

    @Override
    protected void onEntryClick(EntryClickedEvent event) {
        DemoDialog dialog = new DemoDialog(event.getEntry(), false);
        dialog.setSaveConsumer(this::onEntryChanged);
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
    }

    @Override
    protected void onEntryDropped(EntryDroppedEvent event) {
        // applyChangesOnEntry() automatically routes through signal.modify()
        // when signal binding is active — no manual refresh needed
        event.applyChangesOnEntry();
        Notification.show("Dropped entry " + event.getEntry().getTitle());
    }

    @Override
    protected void onEntryResized(EntryResizedEvent event) {
        // Same as drop — routes through signal.modify() automatically
        event.applyChangesOnEntry();
        Notification.show("Resized entry " + event.getEntry().getTitle());
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        // Simply add to the signal — calendar updates automatically
        entries.forEach(entriesSignal::insertLast);
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        // Find the ValueSignal for each entry and remove it
        // ListSignal stores ValueSignal wrappers, not raw Entry objects — we match by entry ID
        List<ValueSignal<Entry>> signals = entriesSignal.peek();
        for (Entry entry : entries) {
            signals.stream()
                    .filter(vs -> { Entry e = vs.peek(); return e != null && entry.getId().equals(e.getId()); })
                    .findFirst()
                    .ifPresent(entriesSignal::remove);
        }
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        // Find the ValueSignal for this entry and modify it
        // The modify() call triggers the per-entry effect → calendar updates automatically
        entriesSignal.peek().stream()
                .filter(vs -> { Entry e = vs.peek(); return e != null && entry.getId().equals(e.getId()); })
                .findFirst()
                .ifPresent(vs -> vs.modify(e -> {
                    e.setTitle(entry.getTitle());
                    e.setStart(entry.getStart());
                    e.setEnd(entry.getEnd());
                    e.setAllDay(entry.isAllDay());
                    e.setColor(entry.getColor());
                    e.setDescription(entry.getDescription());
                }));
    }

    @Override
    protected String createDescription() {
        return "This demo shows signal binding for reactive entry management (requires Vaadin 25.1+, experimental). " +
                "Entries are managed via a ListSignal — the calendar updates automatically " +
                "when entries are added, removed, or modified through the signal. " +
                "No manual refreshItem() or refreshAll() calls are needed. " +
                "Try clicking a timeslot to create an entry, clicking an entry to edit/delete it, " +
                "or dragging/resizing entries.";
    }
}

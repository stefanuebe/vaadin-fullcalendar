package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This demo shows signal binding for a scheduler with resources.
 * Both entries and resources are managed via {@link ListSignal} —
 * the scheduler updates automatically when entries or resources are
 * added, removed, or modified through the signal.
 * <p>
 * This feature requires Vaadin 25.1 (Signals API, experimental).
 *
 * @author Stefan Uebe
 */
@Route(value = "signal-scheduler", layout = MainLayout.class)
@MenuItem(label = "Signal Scheduler")
public class SignalSchedulerDemo extends AbstractSchedulerView {

    private ListSignal<Entry> entriesSignal;
    private ListSignal<Resource> resourcesSignal;

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        entriesSignal = new ListSignal<>();
        resourcesSignal = new ListSignal<>();

        // Pre-populate resources
        Resource roomA = new Resource(null, "Room A", "#3788d8");
        Resource roomB = new Resource(null, "Room B", "#e53935");
        Resource roomC = new Resource(null, "Room C", "#43a047");
        resourcesSignal.insertLast(roomA);
        resourcesSignal.insertLast(roomB);
        resourcesSignal.insertLast(roomC);

        // Pre-populate entries with resource assignments
        EntryService<ResourceEntry> entryService = EntryService.createRandomResourceInstance();
        Resource[] rooms = {roomA, roomB, roomC};
        int i = 0;
        for (ResourceEntry entry : (Iterable<ResourceEntry>) entryService.streamEntries()::iterator) {
            entry.addResources(rooms[i % rooms.length]);
            entriesSignal.insertLast(entry);
            i++;
        }

        FullCalendarScheduler scheduler = (FullCalendarScheduler) FullCalendarBuilder.create()
                .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .build();

        scheduler.setOption("initialView", SchedulerView.RESOURCE_TIMELINE_WEEK.getClientSideValue());
        scheduler.setOption(FullCalendarScheduler.SchedulerOption.ENTRY_RESOURCES_EDITABLE, true);

        // Bind both signals — scheduler updates automatically
        scheduler.bindResources(resourcesSignal);
        scheduler.bindEntries(entriesSignal);

        return scheduler;
    }

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        return super.createToolbar(toolbarBuilder.allowAddingRandomItemsInitially(false));
    }

    @Override
    protected void onTimeslotsSelectedScheduler(TimeslotsSelectedSchedulerEvent event) {
        ResourceEntry entry = new ResourceEntry();
        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());
        entry.setColor("green");
        event.getResource().ifPresent(r -> entry.addResources(r));

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
    protected void onEntryDroppedScheduler(EntryDroppedSchedulerEvent event) {
        event.applyChangesOnEntry();
        Notification.show("Dropped entry " + event.getEntry().getTitle());
    }

    @Override
    protected void onEntryResized(EntryResizedEvent event) {
        event.applyChangesOnEntry();
        Notification.show("Resized entry " + event.getEntry().getTitle());
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        entries.forEach(entriesSignal::insertLast);
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        List<ValueSignal<Entry>> signals = entriesSignal.peek();
        for (Entry entry : entries) {
            signals.stream()
                    .filter(vs -> entry.getId().equals(vs.peek().getId()))
                    .findFirst()
                    .ifPresent(entriesSignal::remove);
        }
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        entriesSignal.peek().stream()
                .filter(vs -> entry.getId().equals(vs.peek().getId()))
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
        return "This demo shows signal binding for a scheduler with resources. " +
                "Both entries and resources are managed via ListSignal — the scheduler " +
                "updates automatically when entries or resources are added, removed, or modified. " +
                "Try clicking a timeslot to create an entry, dragging entries between resources, " +
                "or resizing entries.";
    }
}

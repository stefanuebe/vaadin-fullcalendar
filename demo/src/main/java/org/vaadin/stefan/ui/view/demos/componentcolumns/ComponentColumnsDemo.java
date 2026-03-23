package org.vaadin.stefan.ui.view.demos.componentcolumns;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Demo view for component resource area columns (UC-024).
 * <p>
 * Shows a scheduler with tasks assigned to resources. Each resource row has two
 * DatePicker columns (Start, End) that reflect the assigned entry's time range.
 * Changing a DatePicker updates the entry; dragging/resizing an entry updates the DatePickers.
 * Cross-resource dragging is disabled.
 */
@Route(value = "component-columns", layout = MainLayout.class)
@MenuItem(label = "Component Columns")
public class ComponentColumnsDemo extends VerticalLayout {

    private final FullCalendarScheduler scheduler;
    private final ComponentResourceAreaColumn<DatePicker> startColumn;
    private final ComponentResourceAreaColumn<DatePicker> endColumn;
    private final InMemoryEntryProvider<Entry> entryProvider;

    public ComponentColumnsDemo() {
        setSizeFull();
        setPadding(true);

        add(new Span("Each resource has a task with Start/End date pickers. " +
                "Edit a date picker to move the entry, or drag/resize the entry to update the pickers."));

        // Build scheduler
        scheduler = (FullCalendarScheduler) FullCalendarBuilder.create()
                .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
                .withAutoBrowserTimezone()
                .build();

        scheduler.addThemeVariants(FullCalendarVariant.VAADIN);
        scheduler.setOption("initialView", SchedulerView.RESOURCE_TIMELINE_MONTH.getClientSideValue());
        scheduler.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        scheduler.setOption(FullCalendar.Option.ENTRY_DURATION_EDITABLE, true);
        scheduler.setOption(FullCalendar.Option.EDITABLE, true);
        // Disable cross-resource dragging
        scheduler.setOption(FullCalendarScheduler.SchedulerOption.ENTRY_RESOURCES_EDITABLE, false);
        // Wider resource area to fit both date picker columns
        scheduler.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_AREA_WIDTH, "500px");
        // Taller rows for date pickers
        scheduler.setOption("resourceAreaHeaderContent", "Project Plan");

        entryProvider = scheduler.getEntryProvider().asInMemory();

        // Component columns: Start and End date pickers
        startColumn = new ComponentResourceAreaColumn<>("start", "Start",
                resource -> {
                    DatePicker picker = createCompactDatePicker();
                    picker.addValueChangeListener(e -> {
                        if (e.isFromClient()) {
                            onStartDateChanged(resource, e.getValue());
                        }
                    });
                    return picker;
                });

        endColumn = new ComponentResourceAreaColumn<>("end", "End",
                resource -> {
                    DatePicker picker = createCompactDatePicker();
                    picker.addValueChangeListener(e -> {
                        if (e.isFromClient()) {
                            onEndDateChanged(resource, e.getValue());
                        }
                    });
                    return picker;
                });

        scheduler.setResourceAreaColumns(List.of(
                new ResourceAreaColumn("title", "Task").withWidth("150px"),
                startColumn.withWidth("170px"),
                endColumn.withWidth("170px")
        ));

        // Resources with entries
        createResourceWithEntry("Design Phase", "#3788d8",
                LocalDate.of(2025, 3, 3), LocalDate.of(2025, 3, 14));
        createResourceWithEntry("Development", "#e53935",
                LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 25));
        createResourceWithEntry("Testing", "#4caf50",
                LocalDate.of(2025, 3, 17), LocalDate.of(2025, 3, 28));
        createResourceWithEntry("Deployment", "#ff9800",
                LocalDate.of(2025, 3, 24), LocalDate.of(2025, 3, 31));

        // Listen for entry drag and resize to update pickers
        scheduler.addEntryDroppedListener(this::onEntryDropped);
        scheduler.addEntryResizedListener(this::onEntryResized);

        scheduler.setSizeFull();
        add(scheduler);
        setFlexGrow(1, scheduler);
    }

    private void createResourceWithEntry(String name, String color,
                                         LocalDate start, LocalDate end) {
        Resource resource = new Resource(null, name, color);
        scheduler.addResource(resource);

        ResourceEntry entry = new ResourceEntry();
        entry.setTitle(name);
        entry.setStart(start.atStartOfDay());
        entry.setEnd(end.atStartOfDay());
        entry.setAllDay(true);
        entry.setColor(color);
        entry.addResources(resource);
        entryProvider.addEntry(entry);

        // Set initial DatePicker values
        startColumn.getComponent(resource).ifPresent(p -> p.setValue(start));
        endColumn.getComponent(resource).ifPresent(p -> p.setValue(end));
    }

    // ---- DatePicker → Entry sync ----

    private void onStartDateChanged(Resource resource, LocalDate newStart) {
        if (newStart == null) return;
        findEntryForResource(resource).ifPresent(entry -> {
            entry.setStart(newStart.atStartOfDay());
            entryProvider.refreshItem(entry);
            Notification.show(resource.getTitle() + " start → " + newStart, 2000,
                    Notification.Position.BOTTOM_START);
        });
    }

    private void onEndDateChanged(Resource resource, LocalDate newEnd) {
        if (newEnd == null) return;
        findEntryForResource(resource).ifPresent(entry -> {
            entry.setEnd(newEnd.atStartOfDay());
            entryProvider.refreshItem(entry);
            Notification.show(resource.getTitle() + " end → " + newEnd, 2000,
                    Notification.Position.BOTTOM_START);
        });
    }

    // ---- Entry drag/resize → DatePicker sync ----

    private void onEntryDropped(EntryDroppedEvent event) {
        event.applyChangesOnEntry();
        syncPickersFromEntry(event.getEntry());
    }

    private void onEntryResized(EntryResizedEvent event) {
        event.applyChangesOnEntry();
        syncPickersFromEntry(event.getEntry());
    }

    private void syncPickersFromEntry(Entry entry) {
        if (!(entry instanceof ResourceEntry re)) return;

        for (Resource resource : re.getResources()) {
            LocalDateTime start = entry.getStart();
            LocalDateTime end = entry.getEnd();

            startColumn.getComponent(resource).ifPresent(p ->
                    p.setValue(start != null ? start.toLocalDate() : null));
            endColumn.getComponent(resource).ifPresent(p ->
                    p.setValue(end != null ? end.toLocalDate() : null));
        }
    }

    // ---- Helper ----

    private DatePicker createCompactDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setWidth("130px");
        picker.getStyle().set("--vaadin-input-field-height", "32px");
        picker.getStyle().set("font-size", "var(--lumo-font-size-s)");
        return picker;
    }

    private java.util.Optional<ResourceEntry> findEntryForResource(Resource resource) {
        return entryProvider.getEntries().stream()
                .filter(e -> e instanceof ResourceEntry)
                .map(e -> (ResourceEntry) e)
                .filter(e -> e.getResources().contains(resource))
                .findFirst();
    }
}

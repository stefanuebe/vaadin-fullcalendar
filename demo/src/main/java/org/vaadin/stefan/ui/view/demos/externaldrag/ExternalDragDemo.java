package org.vaadin.stefan.ui.view.demos.externaldrag;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demonstrates the {@link Draggable} API for dragging external Vaadin components
 * onto the calendar.
 */
@Route(value = "external-drag", layout = MainLayout.class)
@MenuItem(label = "External Drag")
public class ExternalDragDemo extends AbstractCalendarView {

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        FullCalendar calendar = FullCalendarBuilder.create()
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .build();

        calendar.setOption(FullCalendar.Option.DROPPABLE, true);

        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        calendar.setEntryProvider(provider);

        // --- Draggable items ---
        HorizontalLayout dragItems = new HorizontalLayout();
        dragItems.setSpacing(true);
        dragItems.getStyle().set("margin-bottom", "8px");

        // All Day Event — green, all-day
        Entry allDayEntry = new Entry();
        allDayEntry.setTitle("All Day Event");
        allDayEntry.setAllDay(true);
        allDayEntry.setColor("#4CAF50");
        Div allDayItem = createDragItem("All Day Event", "#4CAF50");

        // Lunch Break — orange, timed at 12:00 for 30 min
        Entry lunchEntry = new Entry();
        lunchEntry.setTitle("Lunch Break");
        lunchEntry.setColor("#FF9800");
        lunchEntry.setCustomProperty("duration", "00:30");
        lunchEntry.setStart(LocalDateTime.of(2025, 1, 1, 12, 0)); // time template
        Div lunchItem = createDragItem("Lunch Break", "#FF9800");

        // Task — blue, no entry data
        Div taskItem = createDragItem("Task", "#2196F3");

        dragItems.add(allDayItem, lunchItem, taskItem);

        // Register draggables on the calendar
        calendar.addDraggable(new Draggable(allDayItem, allDayEntry));
        calendar.addDraggable(new Draggable(lunchItem, lunchEntry));
        calendar.addDraggable(new Draggable(taskItem));  // no entry data — title will be empty on drop

        // --- Drop listener ---
        calendar.addDropListener(e -> {
            String componentInfo = e.getDraggedComponent()
                    .map(c -> c.getElement().getText())
                    .orElse("unknown");
            String entryInfo = e.getDraggedEntry()
                    .map(Entry::getTitle)
                    .orElse("no entry data");

            Notification.show(
                    "Dropped '" + componentInfo + "' on " + e.getDate()
                            + " (entry: " + entryInfo + ")",
                    3000, Notification.Position.BOTTOM_START);
        });

        // Insert drag items before the calendar
        addComponentAsFirst(dragItems);

        return calendar;
    }

    private Div createDragItem(String label, String color) {
        Div item = new Div(new Span(label));
        item.getStyle()
                .set("padding", "8px 16px")
                .set("background", color)
                .set("color", "white")
                .set("cursor", "grab")
                .set("border-radius", "4px")
                .set("user-select", "none");
        return item;
    }

    @Override
    protected String createDescription() {
        return "Drag the colored items from above onto the calendar. " +
                "'All Day Event' creates an all-day entry (green). " +
                "'Lunch Break' creates a timed entry at 12:00 (orange). " +
                "'Task' has no entry data — the drop still fires but without entry info.";
    }
}

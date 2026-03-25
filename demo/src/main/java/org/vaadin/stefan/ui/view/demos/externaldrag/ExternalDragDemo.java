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

        Div meetingItem = createDragItem("Meeting", "#4CAF50", createEntry("Team Meeting", "01:00"));
        Div lunchItem = createDragItem("Lunch Break", "#FF9800", createEntry("Lunch", "00:30"));
        Div taskItem = createDragItem("Task", "#2196F3", null);  // no entry data

        dragItems.add(meetingItem, lunchItem, taskItem);

        // Register draggables on the calendar
        calendar.addDraggable(new Draggable(meetingItem, createEntry("Team Meeting", "01:00")));
        calendar.addDraggable(new Draggable(lunchItem, createEntry("Lunch", "00:30")));
        calendar.addDraggable(new Draggable(taskItem));  // no entry data

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

    private Div createDragItem(String label, String color, Entry entry) {
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

    private Entry createEntry(String title, String duration) {
        Entry entry = new Entry();
        entry.setTitle(title);
        entry.setCustomProperty("duration", duration);
        return entry;
    }

    @Override
    protected String createDescription() {
        return "Drag the colored items from above onto the calendar. " +
                "The 'Meeting' and 'Lunch Break' items carry Entry data; " +
                "'Task' has no entry data. The drop notification shows the resolved component and entry.";
    }
}

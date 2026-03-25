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
import elemental.json.JsonObject;

import java.time.LocalDateTime;

/**
 * Demonstrates the {@link Draggable} API for dragging external Vaadin components
 * onto the calendar.
 */
@Route(value = "external-drag", layout = MainLayout.class)
@MenuItem(label = "External Drag")
public class ExternalDragDemo extends AbstractCalendarView {

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        FullCalendar calendar = FullCalendarBuilder.create()
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .build();

        calendar.setOption(FullCalendar.Option.DROPPABLE, true);

        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        calendar.setEntryProvider(provider);

        // --- Drop listener ---
        calendar.addDropListener(e -> {
            String componentInfo = e.getDraggable()
                    .map(Draggable::getComponent)
                    .map(c -> c.getElement().getText())
                    .orElse("unknown");
            String entryInfo = e.getDraggable()
                    .flatMap(Draggable::getEntryData)
                    .map(Entry::getTitle)
                    .orElse("no entry data");

            Notification.show(
                    "Dropped '" + componentInfo + "' on " + e.getDate()
                            + " (entry: " + entryInfo + ")",
                    3000, Notification.Position.BOTTOM_START);
        });

        // --- Entry receive listener: persist the client-created entry ---
        calendar.addEntryReceiveListener(e -> {
            provider.addEntry(e.getEntry());
            provider.refreshAll();
        });




        return calendar;
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {

        // --- Container draggable with itemSelector + eventData callback ---
        Div taskList = new Div();
        taskList.getStyle()
                .set("border", "1px solid #ccc")
                .setBorderRadius("var(--vaadin-radius-m)")
                .set("padding", "8px")
                .set("margin-bottom", "8px");
        taskList.add(new Span("Task list (drag individual items):"));

        for (String task : new String[]{"Write report", "Fix bug #42", "Review PR", "Deploy v2.0"}) {
            Div item = new Div(new Span(task));
            item.addClassName("task-item");
            item.getStyle()
                    .set("padding", "4px 12px")
                    .set("margin", "4px 0")
                    .set("background", "var(--vaadin-background-container-strong)")
                    .set("cursor", "grab")
                    .setBorderRadius("var(--vaadin-radius-m)");
            taskList.add(item);
        }

        // Register as container draggable: children with .task-item are draggable,
        // eventData callback creates entry from the dragged element's text
        calendar.addDraggable(new Draggable(taskList)
                .withItemSelector(".task-item")
                .withEventDataCallback(JsCallback.of(
                        "function(el) { console.info(el); return { title: el.innerText, duration: '01:00' }; }")));
        // Insert drag items and task list before the calendar


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

        // Lunch Break — orange, timed for 30 min (drop time determined by target slot)
        Entry lunchEntry = new Entry();
        lunchEntry.setTitle("Lunch Break");
        lunchEntry.setColor("#FF9800");
        lunchEntry.setAllDay(false);
        lunchEntry.setRecurringDuration("00:30");
        Div lunchItem = createDragItem("Lunch Break", "#FF9800");

        // Task — blue, no entry data
        Div taskItem = createDragItem("Task", "#2196F3");

        dragItems.add(allDayItem, lunchItem, taskItem);

        // Register draggables on the calendar
        calendar.addDraggable(new Draggable(allDayItem, allDayEntry));
        calendar.addDraggable(new Draggable(lunchItem, lunchEntry));
        calendar.addDraggable(new Draggable(taskItem));  // no entry data — title will be empty on drop

        int i = indexOf(getToolbar());

        addComponentAtIndex(i, taskList);
        addComponentAtIndex(i, dragItems);
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
        return "Drag the colored items onto the calendar. " +
                "'All Day Event' creates an all-day entry (green). " +
                "'Lunch Break' creates a timed entry at 12:00 (orange). " +
                "'Task' has no entry data. " +
                "The task list below demonstrates container dragging with itemSelector — " +
                "each item generates entry data dynamically via a JS callback.";
    }
}

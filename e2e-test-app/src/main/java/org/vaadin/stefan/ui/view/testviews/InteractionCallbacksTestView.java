package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test view for interaction callbacks.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Drag start/stop events update a counter badge visible to Playwright</li>
 *   <li>Resize start/stop events update a counter badge</li>
 *   <li>Unselect event fires when selection is cleared</li>
 *   <li>selectAllow callback prevents selections before a cutoff date</li>
 *   <li>eventAllow callback prevents drops onto the "locked" day</li>
 *   <li>External drop: droppable=true + drop/eventReceive events</li>
 * </ul>
 * <p>
 * Route: /test/interaction-callbacks
 */
@Route(value = "interaction-callbacks", layout = TestLayout.class)
@MenuItem(label = "Interaction Callbacks")
public class InteractionCallbacksTestView extends VerticalLayout {

    public InteractionCallbacksTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Interaction Callbacks"));
        add(new Paragraph(
                "Drag an event to increment drag-start/stop counters. " +
                "Resize an event to increment resize-start/stop counters. " +
                "Select a date range, then click elsewhere to fire unselect."));

        // Counters displayed as badges for Playwright
        Span dragStartBadge = new Span("0");
        dragStartBadge.setId("drag-start-count");
        Span dragStopBadge = new Span("0");
        dragStopBadge.setId("drag-stop-count");
        Span resizeStartBadge = new Span("0");
        resizeStartBadge.setId("resize-start-count");
        Span resizeStopBadge = new Span("0");
        resizeStopBadge.setId("resize-stop-count");
        Span unselectBadge = new Span("0");
        unselectBadge.setId("unselect-count");
        Span dropBadge = new Span("0");
        dropBadge.setId("drop-count");
        Span receiveEntryTitle = new Span("");
        receiveEntryTitle.setId("receive-entry-title");

        Div counters = new Div(
                label("dragStart: "), dragStartBadge,
                label(" | dragStop: "), dragStopBadge,
                label(" | resizeStart: "), resizeStartBadge,
                label(" | resizeStop: "), resizeStopBadge,
                label(" | unselect: "), unselectBadge,
                label(" | drop: "), dropBadge,
                label(" | receiveTitle: "), receiveEntryTitle
        );
        add(counters);

        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);

        // Fix the date for reproducible tests
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue());
        calendar.setEditable(true);
        calendar.setOption(FullCalendar.Option.SELECTABLE, true);
        calendar.setOption(FullCalendar.Option.DROPPABLE, true);

        // Add some timed entries so drag/resize can be tested
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        Entry draggable = new Entry();
        draggable.setTitle("Drag Me");
        draggable.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        draggable.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
        provider.addEntry(draggable);

        Entry resizable = new Entry();
        resizable.setTitle("Resize Me");
        resizable.setStart(LocalDateTime.of(2025, 3, 4, 14, 0));
        resizable.setEnd(LocalDateTime.of(2025, 3, 4, 15, 0));
        provider.addEntry(resizable);

        calendar.setEntryProvider(provider);

        // --- Event listeners ---
        calendar.addEntryDragStartListener(e -> {
            int count = Integer.parseInt(dragStartBadge.getText()) + 1;
            dragStartBadge.setText(String.valueOf(count));
        });
        calendar.addEntryDragStopListener(e -> {
            int count = Integer.parseInt(dragStopBadge.getText()) + 1;
            dragStopBadge.setText(String.valueOf(count));
        });
        calendar.addEntryResizeStartListener(e -> {
            int count = Integer.parseInt(resizeStartBadge.getText()) + 1;
            resizeStartBadge.setText(String.valueOf(count));
        });
        calendar.addEntryResizeStopListener(e -> {
            int count = Integer.parseInt(resizeStopBadge.getText()) + 1;
            resizeStopBadge.setText(String.valueOf(count));
        });
        calendar.addTimeslotsUnselectListener(e -> {
            int count = Integer.parseInt(unselectBadge.getText()) + 1;
            unselectBadge.setText(String.valueOf(count));
        });
        calendar.addDropListener(e -> {
            int count = Integer.parseInt(dropBadge.getText()) + 1;
            dropBadge.setText(String.valueOf(count));
        });
        calendar.addEntryReceiveListener(e -> {
            receiveEntryTitle.setText(e.getEntry().getTitle() != null ? e.getEntry().getTitle() : "(no title)");
        });

        // selectAllow: deny selections before 2025-03-03
        calendar.setSelectAllowCallback(
                "function(selectInfo) { return selectInfo.start >= new Date('2025-03-03'); }");

        // eventAllow: prevent drops onto Monday 2025-03-03 (used for the Playwright deny test)
        calendar.setEventAllowCallback(
                "function(dropInfo, draggedEvent) { " +
                "  var d = dropInfo.start; " +
                "  return !(d.getFullYear() === 2025 && d.getMonth() === 2 && d.getDate() === 3); " +
                "}");

        add(calendar);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}

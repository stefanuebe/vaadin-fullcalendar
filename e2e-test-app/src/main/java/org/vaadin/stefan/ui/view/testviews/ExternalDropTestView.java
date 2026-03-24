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
import java.util.Locale;

/**
 * Test view for external drop (DropEvent).
 * <p>
 * Creates a draggable HTML element outside the calendar, initialized with FC's Draggable API.
 * When dropped onto the calendar, DropEvent fires.
 * <p>
 * Route: /test/external-drop
 */
@Route(value = "external-drop", layout = TestLayout.class)
@MenuItem(label = "External Drop")
public class ExternalDropTestView extends VerticalLayout {

    public ExternalDropTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("External Drop"));
        add(new Paragraph("Drag the external element onto the calendar."));

        // --- Badges ---
        Span dropCount = badge("drop-count", "0");
        Span dropDate = badge("drop-date", "");
        Span dropAllDay = badge("drop-allday", "");
        Span dropData = badge("drop-data", "");

        Div badges = new Div(
                label("dropCount: "), dropCount,
                label(" | dropDate: "), dropDate,
                label(" | dropAllDay: "), dropAllDay,
                label(" | dropData: "), dropData
        );
        badges.getStyle().set("font-size", "12px");
        add(badges);

        // --- External draggable element ---
        Div draggableEl = new Div();
        draggableEl.setId("external-draggable");
        draggableEl.setText("Drag me to calendar");
        draggableEl.getStyle()
                .set("padding", "8px 16px")
                .set("background", "#4CAF50")
                .set("color", "white")
                .set("cursor", "grab")
                .set("display", "inline-block")
                .set("margin-bottom", "8px");
        // data-event attribute tells FC what entry to create
        draggableEl.getElement().setAttribute("data-event",
                "{\"title\":\"External Task\",\"duration\":\"01:00\"}");
        add(draggableEl);

        // --- Calendar ---
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.LUMO);
        calendar.setLocale(Locale.ENGLISH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(FullCalendar.Option.DROPPABLE, true);

        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        calendar.setEntryProvider(provider);

        // --- Listeners ---
        calendar.addDropListener(e -> {
            int count = Integer.parseInt(dropCount.getText()) + 1;
            dropCount.setText(String.valueOf(count));
            dropDate.setText(e.getDate() != null ? e.getDate().toString() : "null");
            dropAllDay.setText(String.valueOf(e.isAllDay()));
            dropData.setText(e.getDraggedElData() != null ? e.getDraggedElData() : "null");
        });

        add(calendar);
        setFlexGrow(1, calendar);

        // Initialize FC Draggable on the external element after attach
        // This requires the FC interaction plugin's Draggable class
        calendar.getElement().executeJs(
                "const el = document.getElementById('external-draggable');" +
                "if (el && window.FullCalendar && window.FullCalendar.Draggable) {" +
                "  new window.FullCalendar.Draggable(el, { eventData: function(el) {" +
                "    return JSON.parse(el.getAttribute('data-event'));" +
                "  }});" +
                "} else if (el && typeof FullCalendarInteraction !== 'undefined') {" +
                "  new FullCalendarInteraction.Draggable(el);" +
                "}"
        );
    }

    private static Span badge(String id, String text) {
        Span s = new Span(text);
        s.setId(id);
        return s;
    }

    private static Span label(String text) {
        return new Span(text);
    }
}

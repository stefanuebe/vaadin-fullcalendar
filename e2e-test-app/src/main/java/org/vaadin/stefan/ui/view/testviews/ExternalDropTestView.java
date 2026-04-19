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
import java.util.Locale;

/**
 * Test view for external drop using the new {@link Draggable} API.
 * <p>
 * Creates a draggable Vaadin component registered via {@link FullCalendar#addDraggable(Draggable)}.
 * When dropped onto the calendar, the {@link DropEvent} provides typed access to the component and entry.
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
        Span dropComponent = badge("drop-component", "");
        Span dropEntry = badge("drop-entry", "");

        Div badges = new Div(
                label("dropCount: "), dropCount,
                label(" | dropDate: "), dropDate,
                label(" | dropAllDay: "), dropAllDay,
                label(" | dropData: "), dropData,
                label(" | dropComponent: "), dropComponent,
                label(" | dropEntry: "), dropEntry
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
        add(draggableEl);

        // --- Entry data for the draggable ---
        Entry entryData = new Entry();
        entryData.setTitle("External Task");
        entryData.setStart(LocalDateTime.of(2025, 3, 12, 10, 0));
        entryData.setEnd(LocalDateTime.of(2025, 3, 12, 11, 0));

        // --- Calendar ---
        FullCalendar calendar = new FullCalendar();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setLocale(Locale.ENGLISH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        calendar.setOption(FullCalendar.Option.DROPPABLE, true);

        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        calendar.setEntryProvider(provider);

        // --- Register draggable via new API ---
        calendar.addDraggable(new Draggable(draggableEl, entryData));

        // --- Listeners ---
        calendar.addDropListener(e -> {
            int count = Integer.parseInt(dropCount.getText()) + 1;
            dropCount.setText(String.valueOf(count));
            dropDate.setText(e.getDate() != null ? e.getDate().toString() : "null");
            dropAllDay.setText(String.valueOf(e.isAllDay()));
            dropComponent.setText(e.getDraggable().map(Draggable::getComponent).map(c -> c.getId().orElse("no-id")).orElse("none"));
            dropEntry.setText(e.getDraggable().flatMap(Draggable::getEntryData).map(Entry::getTitle).orElse("none"));
        });

        add(calendar);
        setFlexGrow(1, calendar);
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

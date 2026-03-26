package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Test view for drop/resize with signal binding.
 * Verifies that applyChangesOnEntry() routes through signal.modify()
 * and that auto-revert works correctly with signal binding.
 * <p>
 * Route: /test/signal-drop-resize
 */
@Route(value = "signal-drop-resize", layout = TestLayout.class)
@MenuItem(label = "Signal Drop/Resize")
public class SignalDropResizeTestView extends VerticalLayout {

    public SignalDropResizeTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Signal Drop/Resize"));

        Span applyToggle = new Span("false");
        applyToggle.setId("apply-changes");
        Span dropStatus = new Span("");
        dropStatus.setId("drop-status");
        Span serverStart = new Span("");
        serverStart.setId("server-start");

        Div status = new Div(
                label("apply: "), applyToggle,
                label(" | status: "), dropStatus,
                label(" | serverStart: "), serverStart
        );
        status.getStyle().set("font-size", "12px");
        add(status);

        Button toggleBtn = new Button("Toggle Apply", e -> {
            boolean current = "true".equals(applyToggle.getText());
            applyToggle.setText(String.valueOf(!current));
        });
        toggleBtn.setId("toggle-apply-btn");
        add(toggleBtn);

        ListSignal<Entry> entries = new ListSignal<>();

        FullCalendar calendar = FullCalendarBuilder.create()
                .withSignalBinding(entries)
                .build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setLocale(Locale.UK);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.setOption("initialView", CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue());
        calendar.setOption(FullCalendar.Option.EDITABLE, true);

        // Add a draggable entry via the signal
        Entry draggable = new Entry("drag-signal");
        draggable.setTitle("Drag Me");
        draggable.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        draggable.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
        entries.insertLast(draggable);

        serverStart.setText(draggable.getStart().toString());

        // Drop listener — conditionally applies changes
        calendar.addEntryDroppedListener(event -> {
            boolean shouldApply = "true".equals(applyToggle.getText());
            if (shouldApply) {
                event.applyChangesOnEntry();
                dropStatus.setText("applied");
            } else {
                dropStatus.setText("rejected");
            }
            serverStart.setText(event.getEntry().getStart() != null
                    ? event.getEntry().getStart().toString() : "null");
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}

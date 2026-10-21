package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Test view for auto-revert without drop or resize listeners (#440).
 * <p>
 * The calendar starts without drop and resize listeners. Buttons add and remove listeners that apply the change, and
 * switch auto-revert off. Each button updates a counter, so a test can wait for the server to have handled it.
 * <p>
 * Route: /test/auto-revert-without-listener
 */
@Route(value = "auto-revert-without-listener", layout = TestLayout.class)
@MenuItem(label = "Auto Revert Without Listener")
public class AutoRevertWithoutListenerTestView extends VerticalLayout {

    private final Deque<Registration> dropListeners = new ArrayDeque<>();
    private final Deque<Registration> resizeListeners = new ArrayDeque<>();

    public AutoRevertWithoutListenerTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Auto Revert Without Listener (#440)"));
        add(new Paragraph("Drag or resize 'Drag Me'. Without a listener for the gesture, the browser reverts it."));

        Span dropListenerCount = new Span("0");
        dropListenerCount.setId("drop-listeners");
        Span resizeListenerCount = new Span("0");
        resizeListenerCount.setId("resize-listeners");
        Span autoRevert = new Span("true");
        autoRevert.setId("auto-revert");
        Span applied = new Span("0");
        applied.setId("applied-count");

        add(new Div(
                new Span("drop listeners: "), dropListenerCount,
                new Span(" | resize listeners: "), resizeListenerCount,
                new Span(" | auto-revert: "), autoRevert,
                new Span(" | applied: "), applied));

        FullCalendar calendar = new FullCalendar();
        calendar.options().setLocale(Locale.UK);
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 3));
        calendar.options().setInitialView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.options().setEditable(true);

        Entry entry = new Entry();
        entry.setTitle("Drag Me");
        entry.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
        calendar.getEntryProvider().asInMemory().addEntry(entry);

        Runnable countApplied = () -> applied.setText(String.valueOf(Integer.parseInt(applied.getText()) + 1));

        Button addDrop = new Button("Add drop listener", e -> {
            dropListeners.push(calendar.addEntryDroppedListener(event -> {
                event.applyChangesOnEntry();
                countApplied.run();
            }));
            dropListenerCount.setText(String.valueOf(dropListeners.size()));
        });
        addDrop.setId("add-drop-listener");

        Button removeDrop = new Button("Remove drop listener", e -> {
            if (!dropListeners.isEmpty()) {
                dropListeners.pop().remove();
            }
            dropListenerCount.setText(String.valueOf(dropListeners.size()));
        });
        removeDrop.setId("remove-drop-listener");

        Button addResize = new Button("Add resize listener", e -> {
            resizeListeners.push(calendar.addEntryResizedListener(event -> {
                event.applyChangesOnEntry();
                countApplied.run();
            }));
            resizeListenerCount.setText(String.valueOf(resizeListeners.size()));
        });
        addResize.setId("add-resize-listener");

        Button disableAutoRevert = new Button("Disable auto-revert", e -> {
            calendar.setAutoRevertUnappliedEntryChanges(false);
            autoRevert.setText("false");
        });
        disableAutoRevert.setId("disable-auto-revert");

        add(new Div(addDrop, removeDrop, addResize, disableAutoRevert));

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

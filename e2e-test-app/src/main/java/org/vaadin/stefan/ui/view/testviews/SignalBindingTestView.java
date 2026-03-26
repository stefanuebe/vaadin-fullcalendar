package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Test view for signal binding (UC-025).
 * <p>
 * Verifies:
 * <ul>
 *   <li>Entries added to ListSignal appear on the calendar</li>
 *   <li>Entries removed from ListSignal disappear from the calendar</li>
 *   <li>Entry modifications via ValueSignal.modify() update the calendar</li>
 *   <li>bindEntries(null) clears all entries</li>
 * </ul>
 * <p>
 * Route: /test/signal-binding
 */
@Route(value = "signal-binding", layout = TestLayout.class)
@MenuItem(label = "Signal Binding")
public class SignalBindingTestView extends VerticalLayout {

    private final ListSignal<Entry> entriesSignal = new ListSignal<>();
    private final FullCalendar calendar;
    private int entryCounter = 0;

    // Status badges for Playwright
    private final Span entryCount = new Span("0");
    private final Span lastAction = new Span("none");

    public SignalBindingTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Signal Binding (UC-025)"));
        add(new Paragraph("Test signal-based entry binding. Use buttons to add/remove/modify entries via ListSignal."));

        entryCount.setId("entry-count");
        lastAction.setId("last-action");

        Div status = new Div(
                label("entries: "), entryCount,
                label(" | action: "), lastAction
        );
        status.getStyle().set("font-size", "12px");
        add(status);

        calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setLocale(Locale.UK);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_WEEK.getClientSideValue());

        // Bind entries via signal
        calendar.bindEntries(entriesSignal);

        // Buttons
        Button addBtn = new Button("Add Entry", e -> {
            entryCounter++;
            Entry entry = new Entry("signal-" + entryCounter);
            entry.setTitle("Signal Entry " + entryCounter);
            entry.setStart(LocalDateTime.of(2025, 3, 3, 9, 0).plusDays(entryCounter - 1));
            entry.setEnd(entry.getStart().plusHours(2));
            entry.setColor("#3788d8");
            entriesSignal.insertLast(entry);
            updateCount("added");
        });
        addBtn.setId("add-entry-btn");

        Button removeFirstBtn = new Button("Remove First", e -> {
            List<ValueSignal<Entry>> items = entriesSignal.peek();
            if (!items.isEmpty()) {
                entriesSignal.remove(items.get(0));
                updateCount("removed");
            }
        });
        removeFirstBtn.setId("remove-first-btn");

        Button modifyFirstBtn = new Button("Modify First Title", e -> {
            List<ValueSignal<Entry>> items = entriesSignal.peek();
            if (!items.isEmpty()) {
                items.get(0).modify(entry -> entry.setTitle("Modified-" + System.currentTimeMillis()));
                updateCount("modified");
            }
        });
        modifyFirstBtn.setId("modify-first-btn");

        Button unbindBtn = new Button("Unbind", e -> {
            calendar.bindEntries(null);
            updateCount("unbound");
        });
        unbindBtn.setId("unbind-btn");

        Button rebindBtn = new Button("Rebind", e -> {
            calendar.bindEntries(entriesSignal);
            updateCount("rebound");
        });
        rebindBtn.setId("rebind-btn");

        Button detachBtn = new Button("Detach Calendar", e -> {
            remove(calendar);
            lastAction.setText("detached");
        });
        detachBtn.setId("detach-btn");

        Button reattachBtn = new Button("Re-Attach Calendar", e -> {
            add(calendar);
            setFlexGrow(1, calendar);
            lastAction.setText("reattached");
        });
        reattachBtn.setId("reattach-btn");

        add(addBtn, removeFirstBtn, modifyFirstBtn, unbindBtn, rebindBtn, detachBtn, reattachBtn);
        add(calendar);
        setFlexGrow(1, calendar);
    }

    private void updateCount(String action) {
        entryCount.setText(String.valueOf(entriesSignal.peek().size()));
        lastAction.setText(action);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}

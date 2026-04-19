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
 * Test view for auto-revert behavior (#225).
 * <p>
 * Verifies:
 * <ul>
 *   <li>When autoRevert=true and applyChangesOnEntry() is NOT called, the entry reverts to its original position</li>
 *   <li>When autoRevert=true and applyChangesOnEntry() IS called, the entry stays at the new position</li>
 *   <li>When autoRevert=false, the entry always stays at the new position (old behavior)</li>
 * </ul>
 * <p>
 * The view uses a badge to indicate whether the last drop was applied or rejected,
 * and exposes the entry's server-side start time for Playwright to verify.
 * <p>
 * Route: /test/auto-revert
 */
@Route(value = "auto-revert", layout = TestLayout.class)
@MenuItem(label = "Auto Revert")
public class AutoRevertTestView extends VerticalLayout {

    public AutoRevertTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Auto Revert (#225)"));
        add(new Paragraph(
                "Drag 'Drag Me' to a different day. " +
                "The 'apply-changes' toggle controls whether applyChangesOnEntry() is called. " +
                "Check the entry position and server-side start to verify revert behavior."));

        // Toggle: whether to apply changes on drop (default: false = reject = should revert)
        Span applyToggle = new Span("false");
        applyToggle.setId("apply-changes");

        // Status badges
        Span dropStatus = new Span("");
        dropStatus.setId("drop-status");
        Span serverStart = new Span("");
        serverStart.setId("server-start");
        Span dropCount = new Span("0");
        dropCount.setId("drop-count");

        Div controls = new Div(
                label("apply: "), applyToggle,
                label(" | status: "), dropStatus,
                label(" | serverStart: "), serverStart,
                label(" | drops: "), dropCount
        );
        controls.getStyle().set("font-size", "12px");
        add(controls);

        // Button to toggle apply mode
        com.vaadin.flow.component.button.Button toggleBtn = new com.vaadin.flow.component.button.Button(
                "Toggle Apply", e -> {
            boolean current = "true".equals(applyToggle.getText());
            applyToggle.setText(String.valueOf(!current));
        });
        toggleBtn.setId("toggle-apply-btn");
        add(toggleBtn);

        FullCalendar calendar = new FullCalendar();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);

        // Fix date for reproducible tests
        calendar.setLocale(Locale.UK);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.setOption("initialView", CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue());
        calendar.setOption(FullCalendar.Option.EDITABLE, true);

        // autoRevert is true by default — no need to set explicitly

        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        Entry draggable = new Entry();
        draggable.setTitle("Drag Me");
        draggable.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        draggable.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
        provider.addEntry(draggable);

        calendar.setEntryProvider(provider);
        provider.refreshAll();

        // Update server-side start display
        serverStart.setText(draggable.getStart().toString());

        // Drop listener — conditionally applies changes based on toggle
        calendar.addEntryDroppedListener(event -> {
            int count = Integer.parseInt(dropCount.getText()) + 1;
            dropCount.setText(String.valueOf(count));

            boolean shouldApply = "true".equals(applyToggle.getText());
            if (shouldApply) {
                event.applyChangesOnEntry();
                provider.refreshItem(event.getEntry());
                dropStatus.setText("applied");
            } else {
                dropStatus.setText("rejected");
            }

            // Always update the server-side start display (reflects actual entry state)
            var start = event.getEntry().getStart();
            serverStart.setText(start != null ? start.toString() : "null");
        });

        // Resize listener — same pattern
        calendar.addEntryResizedListener(event -> {
            boolean shouldApply = "true".equals(applyToggle.getText());
            if (shouldApply) {
                event.applyChangesOnEntry();
                provider.refreshItem(event.getEntry());
                dropStatus.setText("resize-applied");
            } else {
                dropStatus.setText("resize-rejected");
            }
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}

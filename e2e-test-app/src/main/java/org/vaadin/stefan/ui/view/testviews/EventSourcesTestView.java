package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for event source improvements.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Calendar renders with a JSON feed event source configured</li>
 *   <li>Event source failure fires when the configured URL does not exist</li>
 *   <li>External entry drop counter badge is present for Playwright</li>
 * </ul>
 * <p>
 * Route: /test/event-sources
 */
@Route(value = "event-sources", layout = TestLayout.class)
@MenuItem(label = "Event Sources")
public class EventSourcesTestView extends VerticalLayout {

    public EventSourcesTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Event Sources"));
        add(new Paragraph(
                "A JSON feed source pointing to a non-existent URL is added. " +
                "When the feed fails to load the failure message is shown below."));

        // Badge for external entry drop events (opt-in via editable source)
        Span externalDropCount = new Span("0");
        externalDropCount.setId("external-drop-count");

        // Span for event source failure messages
        Span failureMessage = new Span("");
        failureMessage.setId("event-source-failure-message");

        Div counters = new Div(
                label("externalDrop: "), externalDropCount,
                label(" | failureMsg: "), failureMessage
        );
        add(counters);

        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.LUMO);
        calendar.getElement().setAttribute("data-testid", "calendar");

        // Fix the date for reproducible tests
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue());

        // Add a JSON feed source pointing to a non-existent URL to trigger the failure event
        JsonFeedEventSource failingSource = new JsonFeedEventSource("/test/api/event-sources/events")
                .withId("failing-feed");
        calendar.addEventSource(failingSource);

        // Listen for event source failures
        calendar.addEventSourceFailureListener(e -> {
            failureMessage.setText(e.getMessage() != null ? e.getMessage() : "unknown error");
        });

        // Listen for external entry drops (won't fire unless source is editable, but wiring is tested)
        calendar.addExternalEntryDroppedListener(e -> {
            int count = Integer.parseInt(externalDropCount.getText()) + 1;
            externalDropCount.setText(String.valueOf(count));
        });

        add(calendar);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}

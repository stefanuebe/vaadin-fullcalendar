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
import java.util.List;
import java.util.TreeSet;

/**
 * Test view for event source improvements.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Calendar renders with a JSON feed event source configured</li>
 *   <li>Event source failure fires when the configured URL does not exist, and the source's own
 *   {@code withFailure} callback runs as well (it counts its calls in {@code window.userFailureCalls})</li>
 *   <li>External entry drop counter badge is present for Playwright</li>
 *   <li>A source added with the names of 8.0 ({@link JsonFeedEntrySource}) fails as well, and the
 *   {@link RemoteEntrySourceFailureEvent} listener receives the failures of both sources</li>
 *   <li>The per-source {@code withAllow} callback controls drops (#303): entries of the source whose callback
 *   returns {@code false} snap back and send no {@link RemoteEntryDroppedEvent}, entries of the source whose
 *   callback returns {@code true} are dropped. The blocking callback records its calls in
 *   {@code window.blockedAllowCalls} and whether the dragged entry has {@code getCustomProperty} in
 *   {@code window.blockedAllowHasCustomApi}</li>
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

        // Span for the source ids the RemoteEntrySourceFailureEvent listener received
        Span remoteFailureSources = new Span("");
        remoteFailureSources.setId("remote-failure-sources");

        // Badge for drops of entries from the editable feeds below
        Span remoteDropCount = new Span("0");
        remoteDropCount.setId("remote-drop-count");

        Div counters = new Div(
                label("externalDrop: "), externalDropCount,
                label(" | remoteDrop: "), remoteDropCount,
                label(" | failureMsg: "), failureMessage,
                label(" | remoteFailureSources: "), remoteFailureSources
        );
        add(counters);

        FullCalendar calendar = new FullCalendar();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.getElement().setAttribute("data-testid", "calendar");

        // Fix the date for reproducible tests
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue());
        // A blocked drop snaps back without animation, so the second drag of a test does not start while the entry still moves back
        calendar.setOption("dragRevertDuration", 0);

        // Add a JSON feed source pointing to a non-existent URL to trigger the failure event
        JsonFeedEventSource failingSource = new JsonFeedEventSource("/test/api/event-sources/events")
                .withId("failing-feed")
                .withFailure("function(error) { window.userFailureCalls = (window.userFailureCalls || 0) + 1; }");
        calendar.addClientSideEventSource(failingSource);

        // A second failing source, added with the names of 8.0
        calendar.addRemoteEntrySource(new JsonFeedEntrySource("/test/api/event-sources/remote-events")
                .withId("failing-remote-feed"));

        calendar.addRemoteEntrySourceFailureListener(e -> {
            TreeSet<String> ids = new TreeSet<>(List.of(remoteFailureSources.getText().split(",")));
            ids.remove("");
            ids.add(e.getSourceId());
            remoteFailureSources.setText(String.join(",", ids));
        });

        // Two editable feeds whose allow callbacks permit and block every drop, dated into the week of initialDate
        calendar.addRemoteEntrySource(new JsonFeedEntrySource("/test-feeds/allowed-drag.json")
                .withId("allowed-drag-feed")
                .withEditable(true)
                .withAllow("function() { return true; }"));
        calendar.addRemoteEntrySource(new JsonFeedEntrySource("/test-feeds/blocked-drag.json")
                .withId("blocked-drag-feed")
                .withEditable(true)
                .withAllow("function(dropInfo, draggedEvent) {"
                        + " window.blockedAllowCalls = (window.blockedAllowCalls || 0) + 1;"
                        + " window.blockedAllowHasCustomApi = typeof draggedEvent.getCustomProperty === 'function';"
                        + " return false; }"));

        calendar.addRemoteEntryDroppedListener(e ->
                remoteDropCount.setText(String.valueOf(Integer.parseInt(remoteDropCount.getText()) + 1)));

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

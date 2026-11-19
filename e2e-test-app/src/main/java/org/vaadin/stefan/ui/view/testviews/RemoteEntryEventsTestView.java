package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * The entry events of an entry from a remote entry source (#456). A click, the mouse, a drag and a resize of the
 * remote entry fire the RemoteEntry...Events, while the same actions on an entry of the entry provider still fire the
 * normal entry events. Each event is appended to the log as {@code type:entryId:sourceId}.
 */
@Route(value = "remote-entry-events", layout = TestLayout.class)
@MenuItem(label = "Remote Entry Events")
public class RemoteEntryEventsTestView extends VerticalLayout {

    private final Span log = new Span();

    public RemoteEntryEventsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Remote Entry Events"));
        log.setId("event-log");
        add(new Div(new Span("log: "), log));

        FullCalendar calendar = new FullCalendar();
        calendar.options().setLocale(Locale.ENGLISH);
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 10));
        calendar.options().setInitialView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.options().setScrollTime(Duration.ofHours(9));
        calendar.options().setTimeZone(Timezone.UTC);
        calendar.options().setEditable(true);

        Entry serverEntry = new Entry("server-1");
        serverEntry.setTitle("Server entry");
        serverEntry.setStart(LocalDateTime.of(2025, 3, 13, 10, 0));
        serverEntry.setEnd(LocalDateTime.of(2025, 3, 13, 11, 0));
        calendar.setEntryProvider(new InMemoryEntryProvider<>(List.of(serverEntry)));

        calendar.addRemoteEntrySource(new JsonFeedEntrySource("/test-data/remote-entries.json").withId("feed"));

        calendar.addRemoteEntryClickedListener(e -> logRemote("click", e));
        calendar.addRemoteEntryMouseEnterListener(e -> logRemote("mouseenter", e));
        calendar.addRemoteEntryMouseLeaveListener(e -> logRemote("mouseleave", e));
        calendar.addRemoteEntryDragStartListener(e -> logRemote("dragstart", e));
        calendar.addRemoteEntryDragStopListener(e -> logRemote("dragstop", e));
        calendar.addRemoteEntryResizeStartListener(e -> logRemote("resizestart", e));
        calendar.addRemoteEntryResizeStopListener(e -> logRemote("resizestop", e));
        calendar.addRemoteEntryLeaveListener(e -> logRemote("leave", e));

        calendar.addEntryClickedListener(e -> append("serverclick:" + e.getEntry().getId()));

        add(calendar);
        setFlexGrow(1, calendar);
    }

    private void logRemote(String type, RemoteEntryEvent event) {
        append(type + ":" + event.getEntry().getId() + ":" + event.getSourceId());
    }

    private void append(String text) {
        log.setText(log.getText().isEmpty() ? text : log.getText() + " " + text);
    }
}

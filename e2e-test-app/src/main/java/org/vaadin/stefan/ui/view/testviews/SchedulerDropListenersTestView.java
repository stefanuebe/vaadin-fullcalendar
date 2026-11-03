package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Test view for drop listeners on the scheduler (#306).
 * <p>
 * A resource entry in resourceTimelineDay. A listener added with {@code addEntryDroppedListener} and one added with
 * {@code addEntryDroppedSchedulerListener} are registered. Only the scheduler listener applies the changes. The
 * counters show how often each listener has been called for the drops, the type shows the event class the plain
 * listener received, and the server start shows the entry state on the server.
 * <p>
 * A second entry is not resource-editable (#449). The new resource shows what the last drop reported, and the server
 * resources show the resources of the dropped entry on the server after the changes were applied.
 * <p>
 * Route: /test/scheduler-drop-listeners
 */
@Route(value = "scheduler-drop-listeners", layout = TestLayout.class)
@MenuItem(label = "Scheduler Drop Listeners")
public class SchedulerDropListenersTestView extends VerticalLayout {

    private int plainCount;
    private int schedulerCount;

    public SchedulerDropListenersTestView() {
        setSizeFull();
        add(new H2("Scheduler Drop Listeners (#306)"));

        Span plain = new Span("0");
        plain.setId("plain-count");
        Span scheduler = new Span("0");
        scheduler.setId("scheduler-count");
        Span plainType = new Span("");
        plainType.setId("plain-type");
        Span serverStart = new Span("");
        serverStart.setId("server-start");
        Span newResource = new Span("");
        newResource.setId("new-resource");
        Span serverResources = new Span("");
        serverResources.setId("server-resources");
        add(new Div(new Span("plain: "), plain, new Span(" | scheduler: "), scheduler,
                new Span(" | plain event type: "), plainType, new Span(" | serverStart: "), serverStart));
        add(new Div(new Span("new resource: "), newResource, new Span(" | server resources: "), serverResources));

        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.options().setSchedulerLicenseKey(Scheduler.DEVELOPER_LICENSE_KEY);
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 3));
        calendar.options().setInitialView(SchedulerView.RESOURCE_TIMELINE_DAY);
        calendar.options().setEditable(true);

        Resource roomA = new Resource("a", "Room A", null);
        calendar.addResources(roomA, new Resource("b", "Room B", null));

        ResourceEntry entry = new ResourceEntry();
        entry.setTitle("Drag Me");
        entry.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        entry.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
        entry.addResources(roomA);
        serverStart.setText(entry.getStart().toString());

        ResourceEntry locked = new ResourceEntry();
        locked.setTitle("Locked");
        locked.setStart(LocalDateTime.of(2025, 3, 3, 12, 0));
        locked.setEnd(LocalDateTime.of(2025, 3, 3, 13, 0));
        locked.addResources(roomA);
        locked.setResourceEditable(false);

        InMemoryEntryProvider<ResourceEntry> provider = InMemoryEntryProvider.from(entry, locked);
        calendar.setEntryProvider(provider);

        // does not apply the changes
        calendar.addEntryDroppedListener(event -> {
            plain.setText(String.valueOf(++plainCount));
            plainType.setText(event.getClass().getSimpleName());
        });

        // applies the changes, so the entry has to stay at the new place
        calendar.addEntryDroppedSchedulerListener(event -> {
            scheduler.setText(String.valueOf(++schedulerCount));
            event.applyChangesOnEntry();
            provider.refreshItem((ResourceEntry) event.getEntry());
            serverStart.setText(event.getEntry().getStart().toString());
            newResource.setText(event.getNewResource().map(Resource::getId).orElse("none"));
            serverResources.setText(((ResourceEntry) event.getEntry()).getResourcesOrEmpty().stream()
                    .map(Resource::getId).sorted().collect(Collectors.joining(",")));
        });

        addAndExpand(calendar);
    }
}

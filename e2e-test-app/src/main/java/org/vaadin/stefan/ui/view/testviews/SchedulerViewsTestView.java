package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler.SchedulerOption;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Test view for the scheduler options under their FullCalendar 7 names (UC-015, UC-025).
 * <p>
 * Starts in resourceTimelineDay. The tests switch to resourceTimeGridDay and resourceDayGridDay on the client.
 * Resource "Room A" carries entry style overrides (eventColor, eventContrastColor, eventClass). The
 * "restyle-room-b" button gives "Room B" a class and colors afterwards, through {@code updateResource}.
 * <p>
 * Route: /test/scheduler-views
 */
@Route(value = "scheduler-views", layout = TestLayout.class)
@MenuItem(label = "Scheduler Views")
public class SchedulerViewsTestView extends VerticalLayout {

    public SchedulerViewsTestView() {
        setSizeFull();
        add(new H2("Scheduler Views"));

        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.setOption(SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.setOption(FullCalendar.Option.INITIAL_DATE, LocalDate.of(2025, 3, 3));
        calendar.setOption(FullCalendar.Option.INITIAL_VIEW, SchedulerView.RESOURCE_TIMELINE_DAY.getClientSideValue());

        // timeline hooks
        calendar.setOption(SchedulerOption.RESOURCE_COLUMN_HEADER_CONTENT, "Rooms");
        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS_WIDTH, "200px");
        calendar.setOption(SchedulerOption.RESOURCE_CELL_CLASS,
                JsCallback.of("function(info) { return info.field === 'title' ? 'hook-resource-cell' : ''; }"));
        calendar.setOption(SchedulerOption.RESOURCE_LANE_CLASS, "hook-lane");
        calendar.setOption(SchedulerOption.RESOURCE_LANE_TOP_CONTENT,
                JsCallback.of("function(info) { return 'Top ' + info.resource.title; }"));

        // vertical resource view hooks
        calendar.setOption(SchedulerOption.RESOURCE_DAY_HEADER_CLASS, "hook-day-header");
        calendar.setOption(SchedulerOption.RESOURCE_DAY_HEADER_CONTENT,
                JsCallback.of("function(info) { return 'Res ' + info.resource.title; }"));

        Resource roomA = new Resource("a", "Room A", "#00ff00");
        // not white: the classic theme's default contrast color is white, so white would prove nothing
        roomA.setEntryContrastColor("#ff0000");
        roomA.setEntryClassNames(Set.of("room-a-entry"));
        Resource roomB = new Resource("b", "Room B", null);
        calendar.addResources(roomA, roomB);

        ResourceEntry meeting = new ResourceEntry();
        meeting.setTitle("Room A Meeting");
        meeting.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        meeting.setEnd(LocalDateTime.of(2025, 3, 3, 11, 0));
        meeting.addResources(roomA);

        ResourceEntry review = new ResourceEntry();
        review.setTitle("Room B Review");
        review.setStart(LocalDateTime.of(2025, 3, 3, 13, 0));
        review.setEnd(LocalDateTime.of(2025, 3, 3, 14, 0));
        review.addResources(roomB);

        InMemoryEntryProvider<ResourceEntry> provider = new InMemoryEntryProvider<>();
        provider.addEntries(meeting, review);
        calendar.setEntryProvider(provider);

        Button restyle = new Button("Restyle Room B", e -> {
            // updateResource and setColor both push the resource. Pushes of one request are batched into a
            // single updateResource call, which the spec checks by counting the calls.
            roomB.setEntryClassNames(Set.of("room-b-restyled"));
            roomB.setEntryContrastColor("#ff0000");
            calendar.updateResource(roomB);
            roomB.setColor("#0000ff");
        });
        restyle.setId("restyle-room-b");

        add(restyle);
        addAndExpand(calendar);
    }
}

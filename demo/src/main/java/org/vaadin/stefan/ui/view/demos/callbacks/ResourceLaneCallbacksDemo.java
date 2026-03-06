package org.vaadin.stefan.ui.view.demos.callbacks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.ResourceEntry;
import org.vaadin.stefan.fullcalendar.Scheduler;
import org.vaadin.stefan.fullcalendar.SchedulerView;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.LocalDate;

@Route(value = "callback-resource-lane", layout = MainLayout.class)
@MenuItem(label = "Resource Lane Callbacks")
public class ResourceLaneCallbacksDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create()
                .withScheduler(Scheduler.DEVELOPER_LICENSE_KEY)
                .build();

        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_WEEK);

        // resourceLaneContent: render a subtle watermark behind events in each resource row.
        // The watermark shows the resource name at low opacity using its own color.
        Scheduler scheduler = (Scheduler) calendar;
        scheduler.setResourceLaneContentCallback(
                "function(arg) {" +
                "  var color = arg.resource.extendedProps && arg.resource.extendedProps.color" +
                "              ? arg.resource.extendedProps.color : '#aaa';" +
                "  return {" +
                "    html: '<div style=\"" +
                "              position:absolute;" +
                "              right:8px;" +
                "              top:50%;" +
                "              transform:translateY(-50%);" +
                "              opacity:0.12;" +
                "              font-size:22px;" +
                "              font-weight:bold;" +
                "              color:' + color + ';'" +
                "          '>' + arg.resource.title + '</div>'" +
                "  };" +
                "}"
        );

        // Define resources
        Resource roomA = new Resource("room-a", "Conference Room A", "#3788d8");
        Resource roomB = new Resource("room-b", "Conference Room B", "#e74c3c");
        Resource roomC = new Resource("room-c", "Meeting Pod C",     "#2ecc71");
        Resource roomD = new Resource("room-d", "Board Room D",      "#9b59b6");

        scheduler.addResources(roomA, roomB, roomC, roomD);

        // Add entries so the lanes are not empty
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);

        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

        Object[][] entries = {
                {"Team Standup",    roomA, 0, 9,  9,  30, "#3788d8"},
                {"Sprint Planning", roomB, 0, 10, 11, 30, "#e74c3c"},
                {"Code Review",     roomA, 2, 14, 15, 0,  "#3788d8"},
                {"Design Review",   roomC, 3, 11, 12, 0,  "#2ecc71"},
                {"Board Meeting",   roomD, 4, 9,  10, 0,  "#9b59b6"},
        };

        for (Object[] row : entries) {
            ResourceEntry entry = new ResourceEntry();
            entry.setTitle((String) row[0]);
            entry.addResources((Resource) row[1]);
            int day = (int) row[2];
            entry.setStart(monday.plusDays(day).atTime((int) row[3], (int) row[4]));
            entry.setEnd(monday.plusDays(day).atTime((int) row[5], (int) row[6]));
            entry.setColor((String) row[7]);
            provider.addEntry(entry);
        }

        return calendar;
    }

    @Override
    protected Component createToolbar() {
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .build();
    }
    // DEMO-END

    @Override
    protected String createDescription() {
        return "Demonstrates the resourceLaneContent callback for Scheduler views. "
                + "A subtle watermark with the resource name is rendered behind events in each resource row. "
                + "Requires the FullCalendar Scheduler license.";
    }
}

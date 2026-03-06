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

@Route(value = "callback-resource-label", layout = MainLayout.class)
@MenuItem(label = "Resource Label Callbacks")
public class ResourceLabelCallbacksDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create()
                .withScheduler(Scheduler.DEVELOPER_LICENSE_KEY)
                .build();

        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_WEEK);

        // resourceLabelContent: render custom HTML for each resource label in the timeline.
        // Each label shows a colored dot, bold name, and the resource ID below it.
        Scheduler scheduler = (Scheduler) calendar;
        scheduler.setResourceLabelContentCallback(
                "function(arg) {" +
                "  var color = arg.resource.extendedProps && arg.resource.extendedProps.color" +
                "              ? arg.resource.extendedProps.color : '#555';" +
                "  return {" +
                "    html: '<div style=\"padding:4px\">'+" +
                "          '<span style=\"display:inline-block;width:10px;height:10px;" +
                "                border-radius:50%;background:' + color + ';margin-right:6px\"></span>'+" +
                "          '<strong>' + arg.resource.title + '</strong>'+" +
                "          '<br><small style=\"color:#888\">id: ' + arg.resource.id + '</small>'+" +
                "          '</div>'" +
                "  };" +
                "}"
        );

        // Define resources
        Resource roomA = new Resource("room-a", "Conference Room A", "#3788d8");
        Resource roomB = new Resource("room-b", "Conference Room B", "#e74c3c");
        Resource roomC = new Resource("room-c", "Meeting Pod C",     "#2ecc71");
        Resource roomD = new Resource("room-d", "Board Room D",      "#9b59b6");

        scheduler.addResources(roomA, roomB, roomC, roomD);

        // Add entries assigned to resources
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);

        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();

        Object[][] entries = {
                {"Team Standup",    roomA, 0, 9,  0,  9,  30, "#3788d8"},
                {"Sprint Planning", roomB, 0, 10, 0,  11, 30, "#e74c3c"},
                {"Code Review",     roomA, 2, 14, 0,  15, 0,  "#3788d8"},
                {"Design Review",   roomC, 3, 11, 0,  12, 0,  "#2ecc71"},
                {"Board Meeting",   roomD, 4, 9,  0,  10, 0,  "#9b59b6"},
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
        return "Demonstrates the resourceLabelContent callback for Scheduler views. "
                + "Each resource label is rendered with a custom colored dot, bold name, and resource ID. "
                + "Requires the FullCalendar Scheduler license.";
    }
}

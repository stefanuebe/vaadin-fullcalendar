package org.vaadin.stefan.ui.view.demos.calendaritemprovider;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Demonstrates CIP with the Scheduler extension. Shows POJO with resource IDs,
 * timeline view with multiple resources, and drag-drop between resources.
 */
@Route(value = "cip-scheduler-demo", layout = MainLayout.class)
@MenuItem(label = "CIP Scheduler Demo")
public class CalendarItemProviderSchedulerDemo extends VerticalLayout {

    private final List<Meeting> meetings = new ArrayList<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CalendarItemProviderSchedulerDemo() {
        setSizeFull();

        // Sample data with resource assignments
        LocalDate today = LocalDate.now();
        var m1 = new Meeting("1", "Team Standup", today.atTime(9, 0), today.atTime(9, 30), "#3788d8");
        m1.setResourceIds(Set.of("room-a"));
        meetings.add(m1);

        var m2 = new Meeting("2", "Sprint Planning", today.atTime(10, 0), today.atTime(11, 30), "#e74c3c");
        m2.setResourceIds(Set.of("room-b"));
        meetings.add(m2);

        var m3 = new Meeting("3", "Design Review", today.atTime(14, 0), today.atTime(15, 0), "#2ecc71");
        m3.setResourceIds(Set.of("room-a"));
        meetings.add(m3);

        var m4 = new Meeting("4", "1:1 Meeting", today.plusDays(1).atTime(11, 0), today.plusDays(1).atTime(11, 30), "#9b59b6");
        m4.setResourceIds(Set.of("room-c"));
        meetings.add(m4);

        // Mapper with resource IDs and bidirectional start/end/resourceIds
        var mapper = CalendarItemPropertyMapper.of(Meeting.class)
                .id(Meeting::getId)
                .title(Meeting::getSubject)
                .start(Meeting::getBegin, Meeting::setBegin)
                .end(Meeting::getFinish, Meeting::setFinish)
                .color(Meeting::getColor)
                .resourceIds(Meeting::getResourceIds, Meeting::setResourceIds);

        // Callback provider
        var provider = CalendarItemProvider.fromCallbacks(
                query -> meetings.stream(),
                id -> meetings.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null)
        );

        // Build scheduler calendar
        FullCalendar<Meeting> calendar = FullCalendarBuilder.<Meeting>create(Meeting.class)
                .withScheduler()
                .withCalendarItemProvider(provider, mapper)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();

        // Set timeline view
        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_WEEK);

        // Add resources
        Scheduler scheduler = (Scheduler) calendar;
        scheduler.addResource(new Resource("room-a", "Conference Room A", null));
        scheduler.addResource(new Resource("room-b", "Conference Room B", null));
        scheduler.addResource(new Resource("room-c", "Meeting Pod C", null));

        // Drag-drop between resources
        scheduler.addCalendarItemDroppedSchedulerListener(event -> {
            event.applyChangesOnItem();
            Meeting meeting = (Meeting) event.getItem();
            StringBuilder msg = new StringBuilder("Moved: " + meeting.getSubject());
            event.getOldResource().ifPresent(r -> msg.append(" from ").append(r.getTitle()));
            event.getNewResource().ifPresent(r -> msg.append(" to ").append(r.getTitle()));
            Notification.show(msg.toString());
        });

        // Click listener
        calendar.addCalendarItemClickedListener(event -> {
            Meeting meeting = event.getItem();
            Notification.show("Clicked: " + meeting.getSubject()
                    + " (resources: " + meeting.getResourceIds() + ")");
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

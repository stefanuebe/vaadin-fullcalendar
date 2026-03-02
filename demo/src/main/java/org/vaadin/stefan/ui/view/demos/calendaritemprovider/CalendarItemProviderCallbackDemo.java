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
import java.util.Optional;

/**
 * Demonstrates CIP with a callback provider simulating backend database access.
 * Shows date-range filtering and drag-and-drop with Strategy A (mapper setters).
 */
@Route(value = "cip-callback-demo", layout = MainLayout.class)
@MenuItem(label = "CIP Callback Demo")
public class CalendarItemProviderCallbackDemo extends VerticalLayout {

    private final List<Meeting> meetings = new ArrayList<>();

    public CalendarItemProviderCallbackDemo() {
        setSizeFull();

        // Simulate a backend data store
        LocalDate today = LocalDate.now();
        meetings.add(new Meeting("1", "Architecture Review", today.atTime(9, 0), today.atTime(10, 30), "#3788d8"));
        meetings.add(new Meeting("2", "Product Demo", today.atTime(14, 0), today.atTime(15, 0), "#e74c3c"));
        meetings.add(new Meeting("3", "1:1 with Manager", today.plusDays(1).atTime(11, 0), today.plusDays(1).atTime(11, 30), "#2ecc71"));
        meetings.add(new Meeting("4", "Design Workshop", today.plusDays(2).atTime(9, 0), today.plusDays(2).atTime(12, 0), "#9b59b6"));
        meetings.add(new Meeting("5", "Release Planning", today.minusDays(1).atTime(15, 0), today.minusDays(1).atTime(16, 0), "#f39c12"));

        // Mapper with setters — enables drag & drop (Strategy A)
        var mapper = CalendarItemPropertyMapper.of(Meeting.class)
                .id(Meeting::getId)
                .title(Meeting::getSubject)
                .start(Meeting::getBegin, Meeting::setBegin)
                .end(Meeting::getFinish, Meeting::setFinish)
                .allDay(Meeting::isAllDay, Meeting::setAllDay)
                .color(Meeting::getColor);

        // Callback provider with date-range filtering
        var provider = CalendarItemProvider.fromCallbacks(
                query -> {
                    // Simulate backend query with date-range filter
                    return meetings.stream().filter(m -> {
                        if (query.getStart() != null && m.getFinish() != null && m.getFinish().isBefore(query.getStart())) {
                            return false;
                        }
                        if (query.getEnd() != null && m.getBegin() != null && m.getBegin().isAfter(query.getEnd())) {
                            return false;
                        }
                        return true;
                    });
                },
                id -> meetings.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null)
        );

        // Build calendar
        FullCalendar<Meeting> calendar = FullCalendarBuilder.<Meeting>create(Meeting.class)
                .withCalendarItemProvider(provider, mapper)
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();

        // Drag & drop — Strategy A (setters on mapper auto-apply changes)
        calendar.addCalendarItemDroppedListener(event -> {
            event.applyChangesOnItem();
            Meeting meeting = event.getItem();
            Notification.show("Moved: " + meeting.getSubject()
                    + " to " + meeting.getBegin().toLocalDate());
        });

        // Resize
        calendar.addCalendarItemResizedListener(event -> {
            event.applyChangesOnItem();
            Meeting meeting = event.getItem();
            Notification.show("Resized: " + meeting.getSubject()
                    + " now ends at " + meeting.getFinish().toLocalTime());
        });

        // Click
        calendar.addCalendarItemClickedListener(event -> {
            Meeting meeting = event.getItem();
            Notification.show("Clicked: " + meeting.getSubject());
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

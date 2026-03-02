package org.vaadin.stefan.ui.view.demos.calendaritemprovider;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryCalendarItemProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demonstrates basic CIP usage with a simple POJO and in-memory provider.
 * Shows click listener, property mapping, and basic calendar setup.
 */
@Route(value = "cip-basic-demo", layout = MainLayout.class)
@MenuItem(label = "CIP Basic Demo")
public class CalendarItemProviderBasicDemo extends VerticalLayout {

    public CalendarItemProviderBasicDemo() {
        setSizeFull();

        // Define the property mapping
        var mapper = CalendarItemPropertyMapper.of(Meeting.class)
                .id(Meeting::getId)
                .title(Meeting::getSubject)
                .start(Meeting::getBegin)
                .end(Meeting::getFinish)
                .allDay(Meeting::isAllDay)
                .color(Meeting::getColor);

        // Create sample data
        LocalDate today = LocalDate.now();
        var provider = CalendarItemProvider.inMemoryFrom(
                Meeting::getId,
                new Meeting("1", "Team Standup", today.atTime(9, 0), today.atTime(9, 30), "#3788d8"),
                new Meeting("2", "Sprint Planning", today.atTime(10, 0), today.atTime(11, 30), "#e74c3c"),
                new Meeting("3", "Lunch Break", today.atTime(12, 0), today.atTime(13, 0), "#2ecc71"),
                new Meeting("4", "Code Review", today.plusDays(1).atTime(14, 0), today.plusDays(1).atTime(15, 0), "#9b59b6"),
                new Meeting("5", "Retrospective", today.plusDays(2).atTime(16, 0), today.plusDays(2).atTime(17, 0), "#f39c12")
        );

        // Build the calendar
        FullCalendar<Meeting> calendar = FullCalendarBuilder.<Meeting>create(Meeting.class)
                .withCalendarItemProvider(provider, mapper)
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();

        // Click listener — shows item details
        calendar.addCalendarItemClickedListener(event -> {
            Meeting meeting = event.getItem();
            Notification.show("Clicked: " + meeting.getSubject()
                    + " (" + meeting.getBegin().toLocalTime() + " - " + meeting.getFinish().toLocalTime() + ")");
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

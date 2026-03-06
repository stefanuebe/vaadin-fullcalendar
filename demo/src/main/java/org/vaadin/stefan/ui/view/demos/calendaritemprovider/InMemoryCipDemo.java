package org.vaadin.stefan.ui.view.demos.calendaritemprovider;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryCalendarItemProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.LocalDate;

@Route(value = "cip-in-memory", layout = MainLayout.class)
@MenuItem(label = "In Memory CIP")
public class InMemoryCipDemo extends AbstractDemoView {

    // DEMO-START
    private InMemoryCalendarItemProvider<Meeting> provider;

    @Override
    protected FullCalendar<?> createCalendar() {
        LocalDate today = LocalDate.now();

        // Define property mapping with setters to enable drag/drop (Strategy A)
        var mapper = CalendarItemPropertyMapper.of(Meeting.class)
                .id(Meeting::getId)
                .title(Meeting::getSubject)
                .start(Meeting::getBegin, Meeting::setBegin)
                .end(Meeting::getFinish, Meeting::setFinish)
                .allDay(Meeting::isAllDay, Meeting::setAllDay)
                .color(Meeting::getColor);

        // Create in-memory provider with sample meetings
        provider = (InMemoryCalendarItemProvider<Meeting>) CalendarItemProvider.inMemoryFrom(
                Meeting::getId,
                new Meeting("1", "Team Standup",
                        today.atTime(9, 0), today.atTime(9, 30), "#3788d8"),
                new Meeting("2", "Sprint Planning",
                        today.atTime(10, 0), today.atTime(11, 30), "#e74c3c"),
                new Meeting("3", "Lunch Break",
                        today.atTime(12, 0), today.atTime(13, 0), "#2ecc71"),
                new Meeting("4", "Code Review",
                        today.plusDays(1).atTime(14, 0), today.plusDays(1).atTime(15, 0), "#9b59b6"),
                new Meeting("5", "Architecture Discussion",
                        today.plusDays(2).atTime(11, 0), today.plusDays(2).atTime(12, 0), "#f39c12"),
                new Meeting("6", "Retrospective",
                        today.plusDays(4).atTime(16, 0), today.plusDays(4).atTime(17, 0), "#3788d8")
        );

        // Build the calendar
        FullCalendar<Meeting> calendar = FullCalendarBuilder.<Meeting>create(Meeting.class)
                .withCalendarItemProvider(provider, mapper)
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();

        // Enable drag/drop and resize globally
        calendar.setEditable(true);

        // Strategy A: applyChangesOnItem() writes the new values into the Meeting via the setters
        calendar.addCalendarItemDroppedListener(event -> {
            event.applyChangesOnItem();
            Meeting meeting = event.getItem();
            Notification.show("Moved: " + meeting.getSubject()
                    + " to " + meeting.getBegin().toLocalDate());
        });

        calendar.addCalendarItemResizedListener(event -> {
            event.applyChangesOnItem();
            Meeting meeting = event.getItem();
            Notification.show("Resized: " + meeting.getSubject()
                    + " — now ends at " + meeting.getFinish().toLocalTime());
        });

        // Click listener showing meeting details
        calendar.addCalendarItemClickedListener(event -> {
            Meeting meeting = event.getItem();
            String time = meeting.isAllDay()
                    ? "all day"
                    : meeting.getBegin().toLocalTime() + " – " + meeting.getFinish().toLocalTime();
            Notification.show(meeting.getSubject() + " (" + time + ")");
        });

        return calendar;
    }

    @Override
    protected Component createToolbar() {
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .settingsAvailable(true)
                .build();
    }
    // DEMO-END

    @Override
    protected String createDescription() {
        return "Uses InMemoryCalendarItemProvider with a custom Meeting POJO. "
                + "Drag/drop and resize are handled automatically via setter-based property mapping (Strategy A).";
    }
}

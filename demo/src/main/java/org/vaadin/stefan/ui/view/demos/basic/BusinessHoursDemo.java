package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Route(value = "business-hours", layout = MainLayout.class)
@MenuItem(label = "Business Hours")
public class BusinessHoursDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate friday = today.with(DayOfWeek.FRIDAY);
        LocalDate saturday = today.with(DayOfWeek.SATURDAY);

        FullCalendar<Entry> calendar = FullCalendarBuilder.<Entry>create()
                .withInitialEntries(List.of(
                        createEntry("Team Standup",
                                monday.atTime(9, 0), monday.atTime(9, 30), "#3788d8"),
                        createEntry("Lunch Meeting",
                                monday.plusDays(1).atTime(12, 0), monday.plusDays(1).atTime(13, 0), "#2ecc71"),
                        createEntry("Friday Wrap-up",
                                friday.atTime(11, 0), friday.atTime(12, 0), "#e74c3c"),
                        createEntry("Saturday Workshop",
                                saturday.atTime(10, 0), saturday.atTime(13, 0), "#9b59b6")
                ))
                .build();

        calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);

        calendar.setBusinessHours(
                BusinessHours.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY).start(9).end(17),
                BusinessHours.of(DayOfWeek.FRIDAY).start(9).end(13),
                BusinessHours.of(DayOfWeek.SATURDAY).start(10).end(14)
        );

        return calendar;
    }

    private Entry createEntry(String title, LocalDateTime start, LocalDateTime end, String color) {
        Entry entry = new Entry(UUID.randomUUID().toString());
        entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(end);
        entry.setColor(color);
        return entry;
    }
    // DEMO-END

    @Override
    protected Component createToolbar() {
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .settingsAvailable(true)
                .build();
    }

    @Override
    protected String createDescription() {
        return "Business hours highlight the working hours of your team or organization. "
                + "Non-business hours are shown with a gray overlay. This example shows different "
                + "hours for weekdays, Fridays (half day), and Saturdays.";
    }
}

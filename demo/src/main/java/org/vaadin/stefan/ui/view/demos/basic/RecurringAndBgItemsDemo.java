package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Route(value = "recurring-bg-items", layout = MainLayout.class)
@MenuItem(label = "Recurring & BG Items")
public class RecurringAndBgItemsDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.<Entry>create()
                .withCalendarItemLimit(5)
                .build();

        calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setTimeslotsSelectable(true);

        LocalDate today = LocalDate.now();
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate yearEnd = yearStart.plusYears(1);

        // Weekly Monday standup (recurring, blue)
        Entry standup = new Entry();
        standup.setTitle("Monday Standup");
        standup.setColor("#3788d8");
        standup.setRecurringDaysOfWeek(DayOfWeek.MONDAY);
        standup.setRecurringStartDate(yearStart);
        standup.setRecurringEndDate(yearEnd);
        standup.setRecurringStartTime(RecurringTime.of(9, 0));
        standup.setRecurringEndTime(RecurringTime.of(9, 30));

        // Bi-weekly Friday review (recurring, green)
        // Note: FullCalendar JS does not natively support bi-weekly recurrence via daysOfWeek;
        // we model it as a weekly Friday recurring entry and label it accordingly.
        Entry fridayReview = new Entry();
        fridayReview.setTitle("Bi-Weekly Friday Review");
        fridayReview.setColor("#2ecc71");
        fridayReview.setRecurringDaysOfWeek(DayOfWeek.FRIDAY);
        fridayReview.setRecurringStartDate(yearStart);
        fridayReview.setRecurringEndDate(yearEnd);
        fridayReview.setRecurringStartTime(RecurringTime.of(14, 0));
        fridayReview.setRecurringEndTime(RecurringTime.of(15, 0));

        // Daily lunch block Mon-Fri as background display mode (gray)
        Entry lunchBlock = new Entry();
        lunchBlock.setTitle("Lunch");
        lunchBlock.setColor("#aaaaaa");
        lunchBlock.setDisplayMode(DisplayMode.BACKGROUND);
        lunchBlock.setRecurringDaysOfWeek(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        lunchBlock.setRecurringStartDate(yearStart);
        lunchBlock.setRecurringEndDate(yearEnd);
        lunchBlock.setRecurringStartTime(RecurringTime.of(12, 0));
        lunchBlock.setRecurringEndTime(RecurringTime.of(13, 0));

        // Working-hours highlight: Mon-Fri all-day background (light green)
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate friday = today.with(DayOfWeek.FRIDAY);
        Entry workingHours = new Entry();
        workingHours.setTitle("Working Days");
        workingHours.setColor("#c8f7c5");
        workingHours.setDisplayMode(DisplayMode.BACKGROUND);
        workingHours.setAllDay(true);
        workingHours.setStart(monday.atStartOfDay());
        workingHours.setEnd(friday.plusDays(1).atStartOfDay());

        // Background entry for Wednesday (highlights unavailability)
        LocalDate wednesday = today.with(DayOfWeek.WEDNESDAY);
        Entry offBlock = new Entry();
        offBlock.setTitle("Out of Office");
        offBlock.setColor("#f5b7b1");
        offBlock.setDisplayMode(DisplayMode.BACKGROUND);
        offBlock.setAllDay(true);
        offBlock.setStart(wednesday.atStartOfDay());
        offBlock.setEnd(wednesday.plusDays(1).atStartOfDay());

        // Regular timed entries for contrast
        Entry teamLunch = new Entry();
        teamLunch.setTitle("Team Lunch");
        teamLunch.setColor("#9b59b6");
        teamLunch.setStart(monday.plusDays(1).atTime(12, 30));
        teamLunch.setEnd(monday.plusDays(1).atTime(13, 30));

        Entry planning = new Entry();
        planning.setTitle("Sprint Planning");
        planning.setColor("#e67e22");
        planning.setStart(monday.atTime(10, 0));
        planning.setEnd(monday.atTime(11, 30));

        Entry retro = new Entry();
        retro.setTitle("Retrospective");
        retro.setColor("#1abc9c");
        retro.setStart(friday.atTime(15, 30));
        retro.setEnd(friday.atTime(16, 30));

        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) calendar.getCalendarItemProvider();
        provider.addEntries(standup, fridayReview, lunchBlock, workingHours,
                offBlock, teamLunch, planning, retro);

        return calendar;
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
        return "Recurring events repeat on specified days of the week or follow custom recurrence rules. "
                + "Background items create colored overlays behind regular events — useful for highlighting "
                + "time blocks, working hours, or availability. This demo shows weekly recurring meetings, "
                + "a daily background lunch block, and a background overlay marking a day as out-of-office.";
    }
}

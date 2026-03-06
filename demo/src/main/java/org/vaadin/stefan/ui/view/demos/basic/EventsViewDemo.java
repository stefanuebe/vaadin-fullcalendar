package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarItemProviderToolbar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Demonstrates all available calendar event listeners with visible feedback.
 * Every interaction fires a server-side event whose details are logged below the calendar.
 */
@Route(value = "events-view", layout = MainLayout.class)
@MenuItem(label = "Events View")
public class EventsViewDemo extends AbstractDemoView {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_LOG_ENTRIES = 50;

    // Initialized here so it is available when createCalendar() runs during super()
    private final VerticalLayout eventLog = new VerticalLayout();

    public EventsViewDemo() {
        super();

        // Style the event log panel
        eventLog.getStyle()
                .set("overflow-y", "auto")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)");
        eventLog.setPadding(true);
        eventLog.setSpacing(false);
        eventLog.setWidth("300px");
        eventLog.setMinWidth("300px");

        // Wrap calendar + event log side by side
        FullCalendar<?> cal = getCalendar();
        int calIndex = indexOf(cal);
        remove(cal);

        HorizontalLayout calendarRow = new HorizontalLayout(cal, eventLog);
        calendarRow.setSizeFull();
        calendarRow.setFlexGrow(1, cal);
        calendarRow.setFlexGrow(0, eventLog);
        calendarRow.setAlignItems(FlexComponent.Alignment.STRETCH);

        addComponentAtIndex(calIndex, calendarRow);
        setFlexGrow(1, calendarRow);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendarRow);
    }

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        LocalDate today = LocalDate.now();

        // Build a small set of entries: timed, all-day, multi-day, and enough on one day to
        // trigger the "+more" link (requires calendarItemLimit(3) on the builder).
        Entry standup = new Entry();
        standup.setTitle("Team Standup");
        standup.setStart(today.atTime(9, 0));
        standup.setEnd(today.atTime(9, 30));
        standup.setColor("#3788d8");

        Entry planning = new Entry();
        planning.setTitle("Sprint Planning");
        planning.setStart(today.atTime(10, 0));
        planning.setEnd(today.atTime(11, 30));
        planning.setColor("#e74c3c");

        Entry lunch = new Entry();
        lunch.setTitle("Lunch Break");
        lunch.setStart(today.atTime(12, 0));
        lunch.setEnd(today.atTime(13, 0));
        lunch.setColor("#2ecc71");

        Entry review = new Entry();
        review.setTitle("Code Review");
        review.setStart(today.atTime(14, 0));
        review.setEnd(today.atTime(15, 0));
        review.setColor("#9b59b6");

        // Fourth entry on the same day — will cause the "+more" link with calendarItemLimit(3)
        Entry retrospective = new Entry();
        retrospective.setTitle("Retrospective");
        retrospective.setStart(today.atTime(16, 0));
        retrospective.setEnd(today.atTime(17, 0));
        retrospective.setColor("#f39c12");

        // All-day entry
        Entry teamDay = new Entry();
        teamDay.setTitle("Team Day");
        teamDay.setStart(today.plusDays(1).atStartOfDay());
        teamDay.setAllDay(true);
        teamDay.setColor("#1abc9c");

        // Multi-day entry
        Entry conference = new Entry();
        conference.setTitle("Conference");
        conference.setStart(today.plusDays(3).atStartOfDay());
        conference.setEnd(today.plusDays(5).atStartOfDay());
        conference.setAllDay(true);
        conference.setColor("#e67e22");

        FullCalendar<Entry> calendar = FullCalendarBuilder.<Entry>create()
                .withInitialEntries(
                        List.of(standup, planning, lunch, review, retrospective, teamDay, conference))
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setTimeslotsSelectable(true);
        calendar.setNumberClickable(true);
        calendar.setWeekNumbersVisible(true);

        // ---- Register all event listeners ----

        calendar.addCalendarItemClickedListener(event -> {
            Entry entry = event.getItem();
            log("Item Clicked: \"" + entry.getTitle() + "\"");
        });

        calendar.addCalendarItemDroppedListener(event -> {
            Entry entry = event.applyChangesOnItem();
            String newStart = entry.getStart() != null
                    ? entry.getStart().format(DT_FMT) : "?";
            log("Item Dropped: \"" + entry.getTitle() + "\" → " + newStart);
        });

        calendar.addCalendarItemResizedListener(event -> {
            Entry entry = event.applyChangesOnItem();
            String newEnd = entry.getEnd() != null
                    ? entry.getEnd().format(DT_FMT) : "?";
            log("Item Resized: \"" + entry.getTitle() + "\" until " + newEnd);
        });

        calendar.addTimeslotClickedListener(event -> {
            String slot = event.isAllDay()
                    ? event.getDate().toString()
                    : event.getDateTime().format(DT_FMT);
            log("Timeslot Clicked: " + slot);
        });

        calendar.addTimeslotsSelectedListener(event -> {
            String start = event.isAllDay()
                    ? event.getStartDate().toString()
                    : event.getStart().format(DT_FMT);
            String end = event.isAllDay()
                    ? event.getEndDate().toString()
                    : event.getEnd().format(DT_FMT);
            log("Timeslots Selected: " + start + " → " + end);
        });

        calendar.addDatesRenderedListener(event ->
                log("Dates Rendered: interval " + event.getIntervalStart()));

        calendar.addMoreLinkClickedListener(event ->
                log("More Link Clicked: " + event.getClickedDate()));

        calendar.addDayNumberClickedListener(event ->
                log("Day Number Clicked: " + event.getDate()));

        calendar.addWeekNumberClickedListener(event ->
                log("Week Number Clicked: " + event.getDate()));

        calendar.addViewChangedListener(event ->
                log("View Changed: " + event.getViewName()));

        calendar.addViewSkeletonRenderedListener(event ->
                log("View Skeleton Rendered: " + event.getViewName()));

        calendar.addBrowserTimezoneObtainedListener(event ->
                log("Browser Timezone: " + event.getTimezone().getClientSideValue()));

        return calendar;
    }

    @Override
    protected com.vaadin.flow.component.Component createToolbar() {
        return CalendarItemProviderToolbar.builder()
                .calendar(getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .settingsAvailable(true)
                .build();
    }

    @Override
    protected String createDescription() {
        return "This view demonstrates all available calendar event listeners. Every interaction with the "
                + "calendar is logged below — click entries, drag them, resize them, select timeslots, "
                + "or navigate between dates to see the events in action.";
    }

    private void log(String message) {
        String ts = LocalDateTime.now().format(TIME_FMT);
        Span line = new Span(ts + " — " + message);
        line.getStyle().set("font-size", "var(--lumo-font-size-s)");
        eventLog.addComponentAsFirst(line);
        if (eventLog.getComponentCount() > MAX_LOG_ENTRIES) {
            eventLog.remove(eventLog.getComponentAt(eventLog.getComponentCount() - 1));
        }
    }
    // DEMO-END
}

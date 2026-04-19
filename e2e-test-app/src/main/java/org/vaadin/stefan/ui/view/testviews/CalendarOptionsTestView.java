package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Test view for verifying visual effects of calendar options.
 * <p>
 * Verifies Pattern 2: "When I set an option on the calendar, does it have the expected effect?"
 * <p>
 * Contains two calendars:
 * <ul>
 *   <li>Calendar 1 (dayGridMonth): locale=DE, firstDay=Monday, weekends=false, weekNumbers=true</li>
 *   <li>Calendar 2 (timeGridDay): slotMinTime, slotMaxTime, slotDuration, businessHours</li>
 * </ul>
 * Route: /test/calendar-options
 */
@Route(value = "calendar-options", layout = TestLayout.class)
@MenuItem(label = "Calendar Options")
public class CalendarOptionsTestView extends VerticalLayout {

    public CalendarOptionsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Calendar Options"));
        add(new Paragraph("Tests visual effects of calendar options: locale, firstDay, weekends, weekNumbers, slotDuration, businessHours."));

        // ========================
        // Calendar 1: dayGridMonth
        // ========================
        add(new H3("DayGrid — Locale DE, No Weekends"));

        FullCalendar cal1 = new FullCalendar();
        cal1.addThemeVariants(FullCalendarVariant.VAADIN);
        cal1.getElement().setAttribute("id", "cal-daygrid");
        cal1.setLocale(Locale.GERMAN);
        cal1.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        cal1.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());
        cal1.setOption(FullCalendar.Option.FIRST_DAY, DayOfWeek.MONDAY);
        cal1.setOption(FullCalendar.Option.WEEKENDS, false);
        cal1.setOption(FullCalendar.Option.WEEK_NUMBERS, true);

        // Add a sample entry so the calendar isn't empty
        InMemoryEntryProvider<Entry> provider1 = new InMemoryEntryProvider<>();
        Entry sample1 = new Entry();
        sample1.setTitle("Test Event");
        sample1.setStart(LocalDate.of(2025, 3, 5).atStartOfDay());
        sample1.setAllDay(true);
        provider1.addEntry(sample1);
        cal1.setEntryProvider(provider1);

        // Set fixed height so both calendars fit
        cal1.setHeight("400px");
        add(cal1);

        // ========================
        // Calendar 2: timeGridDay
        // ========================
        add(new H3("TimeGrid — Slot Options & Business Hours"));

        FullCalendar cal2 = new FullCalendar();
        cal2.addThemeVariants(FullCalendarVariant.VAADIN);
        cal2.getElement().setAttribute("id", "cal-timegrid");
        cal2.setLocale(Locale.ENGLISH);
        cal2.setOption("initialDate", LocalDate.of(2025, 3, 5).toString());
        cal2.setOption("initialView", CalendarViewImpl.TIME_GRID_DAY.getClientSideValue());
        cal2.setOption(FullCalendar.Option.SLOT_MIN_TIME, "08:00:00");
        cal2.setOption(FullCalendar.Option.SLOT_MAX_TIME, "18:00:00");
        cal2.setOption(FullCalendar.Option.SLOT_DURATION, "00:15:00");
        cal2.setOption(FullCalendar.Option.BUSINESS_HOURS, true);

        // Add a timed entry
        InMemoryEntryProvider<Entry> provider2 = new InMemoryEntryProvider<>();
        Entry sample2 = new Entry();
        sample2.setTitle("Morning Meeting");
        sample2.setStart(LocalDateTime.of(2025, 3, 5, 9, 0));
        sample2.setEnd(LocalDateTime.of(2025, 3, 5, 10, 0));
        provider2.addEntry(sample2);
        cal2.setEntryProvider(provider2);

        cal2.setHeight("400px");
        add(cal2);

        // ========================
        // Calendar 3: dayGridMonth with hiddenDays and nowIndicator
        // ========================
        add(new H3("DayGrid — Hidden Days, Now Indicator, Scroll Time"));

        FullCalendar cal3 = new FullCalendar();
        cal3.addThemeVariants(FullCalendarVariant.VAADIN);
        cal3.getElement().setAttribute("id", "cal-extra");
        cal3.setLocale(Locale.ENGLISH);
        // Use today's date (no hiddenDays!) so nowIndicator is always visible
        cal3.setOption("initialDate", java.time.LocalDate.now().toString());
        cal3.setOption("initialView", CalendarViewImpl.TIME_GRID_DAY.getClientSideValue());
        cal3.setOption(FullCalendar.Option.NOW_INDICATOR, true);
        cal3.setOption(FullCalendar.Option.SCROLL_TIME, "14:00:00");

        InMemoryEntryProvider<Entry> provider3 = new InMemoryEntryProvider<>();
        cal3.setEntryProvider(provider3);
        cal3.setHeight("400px");
        add(cal3);
    }
}

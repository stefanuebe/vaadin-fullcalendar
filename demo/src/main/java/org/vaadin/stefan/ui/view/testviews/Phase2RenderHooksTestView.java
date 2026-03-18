package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for Phase 2 render hook callbacks.
 * <p>
 * Applies dayCellClassNames, dayCellContent, dayHeaderClassNames, dayHeaderContent,
 * weekNumberClassNames, and weekNumberContent callbacks so Playwright can verify
 * they take effect on the client side.
 * <p>
 * Route: /test/phase2-render-hooks
 */
@Route(value = "phase2-render-hooks", layout = TestLayout.class)
@MenuItem(label = "Phase 2: Render Hooks")
public class Phase2RenderHooksTestView extends VerticalLayout {

    public Phase2RenderHooksTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Phase 2 — Render Hook Callbacks"));
        add(new Paragraph(
                "Each day cell gets class 'phase2-cell', each header gets class 'phase2-header', " +
                "and week numbers get class 'phase2-weeknum'. " +
                "The all-day row gets class 'phase2-allday'."));

        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setWeekNumbersVisible(true);

        // Fix the displayed date for reproducible tests
        calendar.setInitialDate(LocalDate.of(2025, 3, 1));
        calendar.setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        // Add a timed entry so the time-grid hooks can also be tested via the timegrid view
        Entry timedEntry = new Entry();
        timedEntry.setTitle("Test Event");
        timedEntry.setStart(LocalDate.of(2025, 3, 10).atTime(9, 0));
        timedEntry.setEnd(LocalDate.of(2025, 3, 10).atTime(10, 0));

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(timedEntry));

        // --- dayCellClassNames: every cell gets 'phase2-cell' ---
        calendar.setDayCellClassNamesCallback(
                "function(info) { return ['phase2-cell']; }");

        // --- dayCellContent: wrap day number in a span with data-testid ---
        calendar.setDayCellContentCallback(
                "function(info) { " +
                "  return { html: '<span data-testid=\"phase2-day-content\" class=\"phase2-day-num\">' " +
                "    + info.dayNumberText + '</span>'; }; }");

        // --- dayHeaderClassNames: every column header gets 'phase2-header' ---
        calendar.setDayHeaderClassNamesCallback(
                "function(info) { return ['phase2-header']; }");

        // --- dayHeaderContent: wrap header text in a span ---
        calendar.setDayHeaderContentCallback(
                "function(info) { " +
                "  return { html: '<span class=\"phase2-header-text\">' + info.text + '</span>'; }; }");

        // --- weekNumberClassNames: every week number cell gets 'phase2-weeknum' ---
        calendar.setWeekNumberClassNamesCallback(
                "function(info) { return ['phase2-weeknum']; }");

        // --- weekNumberContent: prefix with 'W' ---
        calendar.setWeekNumberContentCallback(
                "function(info) { return { html: '<span class=\"phase2-weeknum-text\">W' + info.num + '</span>'; }; }");

        // --- allDayClassNames: all-day row header gets 'phase2-allday' (timegrid only) ---
        calendar.setAllDayClassNamesCallback(
                "function(info) { return ['phase2-allday']; }");

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

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
 * Test view for render hook callbacks.
 * <p>
 * Applies dayCellClassNames, dayCellContent, dayHeaderClassNames, dayHeaderContent,
 * weekNumberClassNames, and weekNumberContent callbacks so Playwright can verify
 * they take effect on the client side.
 * <p>
 * Route: /test/render-hooks
 */
@Route(value = "render-hooks", layout = TestLayout.class)
@MenuItem(label = "Render Hooks")
public class RenderHooksTestView extends VerticalLayout {

    public RenderHooksTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Render Hook Callbacks"));
        add(new Paragraph(
                "Each day cell gets class 'hook-day-cell', each header gets class 'hook-header', " +
                "and week numbers get class 'hook-weeknum'. " +
                "The all-day row gets class 'hook-allday'."));

        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setWeekNumbersVisible(true);

        // Fix the displayed date for reproducible tests
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.setOption("initialView", CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());

        // Add a timed entry so the time-grid hooks can also be tested via the timegrid view
        Entry timedEntry = new Entry();
        timedEntry.setTitle("Test Event");
        timedEntry.setStart(LocalDate.of(2025, 3, 10).atTime(9, 0));
        timedEntry.setEnd(LocalDate.of(2025, 3, 10).atTime(10, 0));

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(timedEntry));

        // --- dayCellClassNames: every cell gets 'hook-day-cell' ---
        calendar.setCallbackOption(CallbackOption.DAY_CELL_CLASS_NAMES,
                "function(info) { return ['hook-day-cell']; }");

        // --- dayCellContent: wrap day number in a span with data-testid ---
        calendar.setCallbackOption(CallbackOption.DAY_CELL_CONTENT,
                "function(info) { " +
                "  return { html: '<span data-testid=\"hook-day-content\" class=\"hook-day-num\">' " +
                "    + info.dayNumberText + '</span>'; }; }");

        // --- dayHeaderClassNames: every column header gets 'hook-header' ---
        calendar.setCallbackOption(CallbackOption.DAY_HEADER_CLASS_NAMES,
                "function(info) { return ['hook-header']; }");

        // --- dayHeaderContent: wrap header text in a span ---
        calendar.setCallbackOption(CallbackOption.DAY_HEADER_CONTENT,
                "function(info) { " +
                "  return { html: '<span class=\"hook-header-text\">' + info.text + '</span>'; }; }");

        // --- weekNumberClassNames: every week number cell gets 'hook-weeknum' ---
        calendar.setCallbackOption(CallbackOption.WEEK_NUMBER_CLASS_NAMES,
                "function(info) { return ['hook-weeknum']; }");

        // --- weekNumberContent: prefix with 'W' ---
        calendar.setCallbackOption(CallbackOption.WEEK_NUMBER_CONTENT,
                "function(info) { return { html: '<span class=\"hook-weeknum-text\">W' + info.num + '</span>'; }; }");

        // --- allDayClassNames: all-day row header gets 'hook-allday' (timegrid only) ---
        calendar.setCallbackOption(CallbackOption.ALL_DAY_CLASS_NAMES,
                "function(info) { return ['hook-allday']; }");

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

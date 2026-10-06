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
 * Applies the day cell, day header, inline week number and all-day header render hooks
 * (class, content, did-mount) so Playwright can verify they take effect on the client side.
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

        FullCalendar calendar = new FullCalendar();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setOption(Option.WEEK_NUMBERS, true);

        // Fix the displayed date for reproducible tests
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 1));
        calendar.setOption(Option.INITIAL_VIEW, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());

        // Add a timed entry so the time-grid hooks can also be tested via the timegrid view
        Entry timedEntry = new Entry();
        timedEntry.setTitle("Test Event");
        timedEntry.setStart(LocalDate.of(2025, 3, 10).atTime(9, 0));
        timedEntry.setEnd(LocalDate.of(2025, 3, 10).atTime(10, 0));

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(timedEntry));

        // --- dayCellClass: every cell gets 'hook-day-cell' ---
        calendar.setOption(Option.DAY_CELL_CLASS,
                JsCallback.of("function(info) { return 'hook-day-cell'; }"));

        // --- dayCellTopContent: wrap day number in a span with data-testid ---
        calendar.setOption(Option.DAY_CELL_TOP_CONTENT,
                JsCallback.of("function(info) { " +
                "  return { html: '<span data-testid=\"hook-day-content\" class=\"hook-day-num\">' " +
                "    + info.dayNumberText + '</span>' }; }"));

        // --- dayCellDidMount: marks every mounted cell with a data attribute ---
        calendar.setOption(Option.DAY_CELL_DID_MOUNT,
                JsCallback.of("function(info) { info.el.setAttribute('data-hook-mounted', 'true'); }"));

        // --- dayHeaderClass: a plain class name string, no callback ---
        calendar.setOption(Option.DAY_HEADER_CLASS, "hook-header");

        // --- dayHeaderContent: wrap header text in a span ---
        calendar.setOption(Option.DAY_HEADER_CONTENT,
                JsCallback.of("function(info) { " +
                "  return { html: '<span class=\"hook-header-text\">' + info.text + '</span>' }; }"));

        // --- inlineWeekNumberClass: every day grid week number gets 'hook-weeknum' ---
        calendar.setOption(Option.INLINE_WEEK_NUMBER_CLASS,
                JsCallback.of("function(info) { return 'hook-weeknum'; }"));

        // --- inlineWeekNumberContent: prefix with 'W' ---
        calendar.setOption(Option.INLINE_WEEK_NUMBER_CONTENT,
                JsCallback.of("function(info) { return { html: '<span class=\"hook-weeknum-text\">W' + info.num + '</span>' }; }"));

        // --- allDayHeaderClass: all-day row header gets 'hook-allday' (timegrid only) ---
        calendar.setOption(Option.ALL_DAY_HEADER_CLASS, "hook-allday");

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

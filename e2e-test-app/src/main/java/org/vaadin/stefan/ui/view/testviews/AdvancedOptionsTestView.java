package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for advanced and niche options.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Calendar renders in dayGridMonth view</li>
 *   <li>View-specific option (dayMaxEventRows=2 for dayGrid only) truncates events in month view</li>
 *   <li>Header and footer toolbar set as maps, including a view button</li>
 * </ul>
 * <p>
 * Route: /test/advanced-options
 */
@Route(value = "advanced-options", layout = TestLayout.class)
@MenuItem(label = "Advanced Options")
public class AdvancedOptionsTestView extends VerticalLayout {

    public AdvancedOptionsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Advanced Options"));
        add(new Paragraph(
                "Tests view-specific options (dayMaxEventRows per view), " +
                "dateIncrement, and getCurrentIntervalStart/End."));

        // --- Calendar ---
        FullCalendar calendar = new FullCalendar();
        calendar.getElement().setAttribute("data-testid", "calendar");

        // Enable native FC toolbars (default is false in the web component), with a view button
        calendar.options().setHeaderToolbar(ToolbarValue.ofStartEnd(
                "prev,next today", "title", "dayGridMonth,timeGridWeek"));
        calendar.options().setFooterToolbar(ToolbarValue.ofStartEnd("", "prevYear,nextYear", ""));
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 1));
        calendar.options().setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        // dateAlignment --------------------------------------------------------
        // Aligning to "month" is the default for dayGridMonth; this just exercises the setter.
        calendar.options().setDateAlignment("month");

        // View-specific option ------------------------------------------------
        // Limit displayed event rows to 2 only in the month view (not in other views).
        // With 5 events on 2025-03-05 this guarantees a "+N more" link in month view.
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayMaxEntryRows(2);

        // eventConstraint -------------------------------------------------------
        // Constrain drag-and-drop to business hours (does not affect rendering, exercises setter).
        calendar.options().setEntryConstraint(ConstraintValue.BUSINESS_HOURS);

        // --- Entry provider --------------------------------------------------------------
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // 5 all-day events on 2025-03-05: with dayMaxEventRows=2 these produce a "+N more" link
        for (int i = 1; i <= 5; i++) {
            Entry e = new Entry();
            e.setTitle("Event " + i);
            e.setStart(LocalDate.of(2025, 3, 5));
            e.setAllDay(true);
            provider.addEntry(e);
        }

        calendar.setEntryProvider(provider);
        add(calendar);
    }
}

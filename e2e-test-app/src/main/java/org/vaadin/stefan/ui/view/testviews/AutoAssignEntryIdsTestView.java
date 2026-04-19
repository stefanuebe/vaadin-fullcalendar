package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Regression test view for issue #202: {@code FullCalendar} assigns
 * {@code id="entry-<entryId>"} to the start segment of each rendered entry when
 * {@code setAutoAssignEntryIds(true)} (the default).
 * <p>
 * Renders two entries — a single-day entry ("simple") and a 3-day entry ("multi") — in
 * month view. A toggle button flips {@code setAutoAssignEntryIds}. Playwright verifies
 * that id assignment follows the flag and that multi-day entries produce exactly one
 * DOM element with the id (the start segment).
 * <p>
 * Route: /test/auto-assign-entry-ids
 */
@Route(value = "auto-assign-entry-ids", layout = TestLayout.class)
@MenuItem(label = "Auto-Assign Entry IDs")
public class AutoAssignEntryIdsTestView extends VerticalLayout {

    public AutoAssignEntryIdsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Auto-Assign Entry IDs — Regression #202"));
        add(new Paragraph(
                "Default on: rendered entries have id=entry-<entryId>. Toggle to turn off."));

        FullCalendar calendar = new FullCalendar();
        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());

        Entry simple = new Entry("simple");
        simple.setTitle("Simple");
        simple.setStart(LocalDate.of(2025, 3, 5).atTime(10, 0));
        simple.setEnd(LocalDate.of(2025, 3, 5).atTime(11, 0));

        Entry multi = new Entry("multi");
        multi.setTitle("Multi-day");
        multi.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
        multi.setEnd(LocalDate.of(2025, 3, 13).atStartOfDay());
        multi.setAllDay(true);

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(List.of(simple, multi)));

        Button toggleBtn = new Button("Toggle auto-assign", e -> calendar.setAutoAssignEntryIds(!calendar.isAutoAssignEntryIds()));
        toggleBtn.getElement().setAttribute("data-testid", "btn-toggle");

        add(toggleBtn, calendar);
    }
}

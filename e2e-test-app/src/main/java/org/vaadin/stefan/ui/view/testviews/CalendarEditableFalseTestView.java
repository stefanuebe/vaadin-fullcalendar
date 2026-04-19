package org.vaadin.stefan.ui.view.testviews;

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
 * Regression test view for issue #212, in the direction the original bug hit: a calendar-level
 * {@code setOption(EDITABLE, false)} must actually prevent entries from being draggable, even
 * when those entries were never explicitly configured (no {@code setEditable(...)} call). Before
 * #212, every entry's implicit {@code editable: true} default was serialized and overrode the
 * calendar-level option.
 * <p>
 * The view renders two entries, neither of which calls {@code setEditable}. Playwright verifies
 * that FC does <b>not</b> add the {@code fc-event-draggable} class to their rendered elements.
 * <p>
 * Route: /test/calendar-editable-false
 */
@Route(value = "calendar-editable-false", layout = TestLayout.class)
@MenuItem(label = "Calendar editable=false")
public class CalendarEditableFalseTestView extends VerticalLayout {

    public CalendarEditableFalseTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Calendar editable=false — Regression #212"));
        add(new Paragraph(
                "With calendar-level EDITABLE=false and default entries (no per-entry setEditable call), "
                        + "no entry must carry the fc-event-draggable class."));

        FullCalendar calendar = new FullCalendar();
        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.setOption(FullCalendar.Option.INITIAL_VIEW, CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue());
        calendar.setOption(FullCalendar.Option.EDITABLE, false);

        Entry e1 = new Entry("e1");
        e1.setTitle("Default A");
        e1.setStart(LocalDate.of(2025, 3, 4).atTime(10, 0));
        e1.setEnd(LocalDate.of(2025, 3, 4).atTime(11, 0));

        Entry e2 = new Entry("e2");
        e2.setTitle("Default B");
        e2.setStart(LocalDate.of(2025, 3, 5).atTime(14, 0));
        e2.setEnd(LocalDate.of(2025, 3, 5).atTime(15, 0));

        calendar.setEntryProvider(EntryProvider.inMemoryFrom(List.of(e1, e2)));

        add(calendar);
    }
}

package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Test view for the end the client computes for an all-day entry without end. The calendar runs in
 * {@code America/New_York}, where 2025-11-02 has 25 hours. Clicking the entry sends its data, and
 * {@code #clicked-end} shows the end the server received.
 * <p>
 * Route: /test/all-day-dst
 */
@Route(value = "all-day-dst", layout = TestLayout.class)
@MenuItem(label = "All-day DST")
public class AllDayDstTestView extends VerticalLayout {

    public AllDayDstTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("All-day entry without end on the 25-hour DST day"));

        Span clickedEnd = new Span("");
        clickedEnd.setId("clicked-end");

        ZoneId newYork = ZoneId.of("America/New_York");
        FullCalendar calendar = new FullCalendar();
        calendar.setOption(Option.TIMEZONE, new Timezone(newYork));
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 11, 2));
        calendar.setOption(Option.INITIAL_VIEW, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue());

        // the start is stored in UTC, so midnight in New York is given as such
        Entry entry = new Entry();
        entry.setTitle("DST day");
        entry.setStart(LocalDate.of(2025, 11, 2).atStartOfDay(newYork).toInstant());
        entry.setAllDay(true);
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        provider.addEntry(entry);
        calendar.setEntryProvider(provider);

        calendar.addEntryClickedListener(e -> clickedEnd.setText(String.valueOf(e.getChangesAsEntry().getEndAsLocalDate())));

        add(clickedEnd, calendar);
    }
}

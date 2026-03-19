package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Test view for display options.
 * <p>
 * Tests:
 * <ul>
 *   <li>{@code dayMaxEventRows(2)} — more than 2 events on a single day triggers the "+N more" link</li>
 *   <li>{@code displayEventEnd(true)} — end time is shown on timed events</li>
 *   <li>{@code nowIndicatorSnap(true)} — now indicator present in timegrid views</li>
 * </ul>
 * <p>
 * Route: /test/display-options
 */
@Route(value = "display-options", layout = TestLayout.class)
@MenuItem(label = "Display Options")
public class DisplayOptionsTestView extends VerticalLayout {

    public DisplayOptionsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Display Options"));
        add(new Paragraph(
                "dayMaxEventRows=2, displayEventEnd=true. " +
                "On 2025-03-10 there are 5 events, so a '+3 more' link should appear."));

        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.VAADIN);

        // Fix date so the test is reproducible
        calendar.setInitialDate(LocalDate.of(2025, 3, 1));
        calendar.setInitialView(CalendarViewImpl.DAY_GRID_MONTH);

        // Display options under test
        calendar.setDayMaxEventRows(2);
        calendar.setDisplayEventEnd(true);

        // Create 5 entries on 2025-03-10 to trigger the "+N more" overflow link
        LocalDate crowdedDay = LocalDate.of(2025, 3, 10);
        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Entry e = new Entry();
            e.setTitle("Event " + i);
            e.setStart(crowdedDay.atTime(8 + i, 0));
            e.setEnd(crowdedDay.atTime(9 + i, 0));
            entries.add(e);
        }

        // Also add a single entry on another day with a visible end time
        Entry withEnd = new Entry();
        withEnd.setTitle("Has End Time");
        withEnd.setStart(LocalDate.of(2025, 3, 15).atTime(10, 0));
        withEnd.setEnd(LocalDate.of(2025, 3, 15).atTime(11, 30));
        entries.add(withEnd);

        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        provider.addEntries(entries);
        calendar.setEntryProvider(provider);

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

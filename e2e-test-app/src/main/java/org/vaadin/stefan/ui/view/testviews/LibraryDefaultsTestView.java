package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test view for a calendar with no options set, so the add-on's initial values and the library defaults apply.
 * <p>
 * The calendar shows March 2025 with one timed entry on 2025-03-05 at 10:00. Only the initial date is set, and it
 * puts the entry in view.
 * <p>
 * Route: /test/library-defaults
 */
@Route(value = "library-defaults", layout = TestLayout.class)
@MenuItem(label = "Library Defaults")
public class LibraryDefaultsTestView extends VerticalLayout {

    public LibraryDefaultsTestView() {
        setSizeFull();
        add(new H2("Library Defaults"));

        Entry entry = new Entry("timed");
        entry.setTitle("Timed entry");
        entry.setStart(LocalDateTime.of(2025, 3, 5, 10, 0));
        entry.setEnd(LocalDateTime.of(2025, 3, 5, 11, 0));

        FullCalendar calendar = new FullCalendar();
        calendar.setId("cal-library-defaults");
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 5));
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(entry));

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

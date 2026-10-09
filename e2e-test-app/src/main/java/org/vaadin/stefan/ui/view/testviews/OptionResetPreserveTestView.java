package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Option;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test view for removing an option of an attached calendar that the browser creates again. The view keeps its
 * components on a refresh, the browser then creates a new calendar element.
 * <p>
 * The calendar shows the week of 2025-03-03 with a timed entry. Before attach, the server sets the entry color red.
 * The button "Remove entry color" removes it.
 * <p>
 * Route: /test/option-reset-preserve
 */
@PreserveOnRefresh
@Route(value = "option-reset-preserve", layout = TestLayout.class)
@MenuItem(label = "Option Reset Preserve")
public class OptionResetPreserveTestView extends VerticalLayout {

    public OptionResetPreserveTestView() {
        setSizeFull();
        add(new H2("Option Reset Preserve"));

        Entry timed = new Entry("timed");
        timed.setTitle("Timed entry");
        timed.setStart(LocalDateTime.of(2025, 3, 5, 10, 0));
        timed.setEnd(LocalDateTime.of(2025, 3, 5, 11, 0));

        FullCalendar calendar = new FullCalendar();
        calendar.setId("cal-option-reset-preserve");
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 3));
        calendar.setOption(Option.ENTRY_COLOR, "red");
        calendar.getEntryProvider().asInMemory().addEntry(timed);

        add(new Button("Remove entry color", e -> calendar.setOption(Option.ENTRY_COLOR, null)), calendar);
        setFlexGrow(1, calendar);
    }
}

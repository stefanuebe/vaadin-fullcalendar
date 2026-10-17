package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for options set right before a direct client call. The calendar shows March 2025 as a month.
 * <p>
 * The button "Step a year and go next" sets the date increment to one year and then moves to the next interval, in
 * one request. Then it disables itself, so a test can wait for the response.
 * <p>
 * Route: /test/flush-options
 */
@Route(value = "flush-options", layout = TestLayout.class)
@MenuItem(label = "Flush Options")
public class FlushOptionsTestView extends VerticalLayout {

    public FlushOptionsTestView() {
        setSizeFull();
        add(new H2("Flush Options"));

        FullCalendar calendar = new FullCalendar();
        calendar.setId("cal-flush-options");
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 15));

        add(new Button("Step a year and go next", e -> {
            calendar.options().setRawOption("dateIncrement", JsonFactory.createObject().put("years", 1));
            calendar.next();
            e.getSource().setEnabled(false);
        }), calendar);
        setFlexGrow(1, calendar);
    }
}

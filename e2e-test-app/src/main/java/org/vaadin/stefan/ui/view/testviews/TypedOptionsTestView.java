package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for the typed options of {@code calendar.options()}. Before attach the server sets the initial date
 * 2025-03-15, so the month view shows March 2025.
 * <p>
 * The button "Day cell class as text" sets the class {@code typed-day} for day cells, the button "Day cell class from
 * callback" sets a callback that returns {@code callback-day}.
 * <p>
 * Route: /test/typed-options
 */
@Route(value = "typed-options", layout = TestLayout.class)
@MenuItem(label = "Typed Options")
public class TypedOptionsTestView extends VerticalLayout {

    public TypedOptionsTestView() {
        setSizeFull();
        add(new H2("Typed Options"));

        FullCalendar calendar = new FullCalendar();
        calendar.setId("cal-typed-options");
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 15));

        add(new HorizontalLayout(
                new Button("Day cell class as text", e -> calendar.options().setDayCellClass("typed-day")),
                new Button("Day cell class from callback", e -> calendar.options().setDayCellClass(
                        JsCallback.of("function() { return 'callback-day'; }")))), calendar);
        setFlexGrow(1, calendar);
    }
}

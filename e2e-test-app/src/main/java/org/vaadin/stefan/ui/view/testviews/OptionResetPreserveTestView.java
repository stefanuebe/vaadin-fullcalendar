package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test view for changing an option of an attached calendar that the browser creates again. The view keeps its
 * components on a refresh, the browser then creates a new calendar element.
 * <p>
 * The calendar shows the week of 2025-03-03 with a timed entry. Before attach, the server sets the entry color red.
 * The button "Set entry color" sets it blue, the button "Remove entry color" removes it. The server also sets an
 * eventDataTransform callback, whose code counts how often it is evaluated in {@code window.preserveEvaluations}.
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
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 3));
        calendar.options().setEntryColor("red");
        calendar.options().setRawOption("eventDataTransform", JsCallback.of("""
                (() => {
                    window.preserveEvaluations = (window.preserveEvaluations || 0) + 1;
                    return function(data) { return data; };
                })()"""));
        calendar.getEntryProvider().asInMemory().addEntry(timed);

        add(new Button("Set entry color", e -> calendar.options().setEntryColor("blue")),
                new Button("Remove entry color", e -> calendar.options().setEntryColor(null)), calendar);
        setFlexGrow(1, calendar);
    }
}

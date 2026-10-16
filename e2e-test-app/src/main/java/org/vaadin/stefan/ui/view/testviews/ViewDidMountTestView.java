package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for a {@code viewDidMount} callback beside the calendar's own handler, which sends the
 * {@code ViewSkeletonRenderedEvent}.
 * <p>
 * The calendar "cal-field" starts with a field callback that counts in the span "field-count". Its server listener
 * counts in "field-server-count". Buttons set another field callback, which counts in "other-count", clear the field,
 * set a raw option, which counts in "raw-count", and switch the view.
 * <p>
 * The calendar "cal-raw" starts with a raw option, which counts in "initial-raw-count". Its server listener counts in
 * "raw-server-count".
 * <p>
 * The button "Ping" counts its clicks in "ping-count" through the server. Once that count shows, the server has
 * handled every event the client sent before the click.
 * <p>
 * Route: /test/view-did-mount
 */
@Route(value = "view-did-mount", layout = TestLayout.class)
@MenuItem(label = "View Did Mount")
public class ViewDidMountTestView extends VerticalLayout {

    private int fieldServerCount;
    private int rawServerCount;
    private int pingCount;

    public ViewDidMountTestView() {
        setSizeFull();
        add(new H2("View Did Mount"));

        Span fieldCount = counter("field-count");
        Span otherCount = counter("other-count");
        Span rawCount = counter("raw-count");
        Span initialRawCount = counter("initial-raw-count");
        Span fieldServerCountSpan = counter("field-server-count");
        Span rawServerCountSpan = counter("raw-server-count");
        Span pingCountSpan = counter("ping-count");

        FullCalendar fieldCalendar = calendar("cal-field");
        fieldCalendar.options().setViewDidMount(countIn("field-count"));
        fieldCalendar.addViewSkeletonRenderedListener(
                e -> fieldServerCountSpan.setText(String.valueOf(++fieldServerCount)));

        FullCalendar rawCalendar = calendar("cal-raw");
        rawCalendar.options().setRawOption("viewDidMount", countIn("initial-raw-count"));
        rawCalendar.addViewSkeletonRenderedListener(
                e -> rawServerCountSpan.setText(String.valueOf(++rawServerCount)));

        HorizontalLayout buttons = new HorizontalLayout(
                new Button("Set other callback", e -> fieldCalendar.options().setViewDidMount(countIn("other-count"))),
                new Button("Clear callback", e -> fieldCalendar.options().setViewDidMount(null)),
                new Button("Set raw callback",
                        e -> fieldCalendar.options().setRawOption("viewDidMount", countIn("raw-count"))),
                new Button("Show week", e -> fieldCalendar.changeView(CalendarViewImpl.TIME_GRID_WEEK)),
                new Button("Show month", e -> fieldCalendar.changeView(CalendarViewImpl.DAY_GRID_MONTH)),
                new Button("Ping", e -> pingCountSpan.setText(String.valueOf(++pingCount))));

        add(buttons,
                new HorizontalLayout(new Span("Field:"), fieldCount, new Span("Other:"), otherCount,
                        new Span("Raw:"), rawCount, new Span("Server:"), fieldServerCountSpan),
                new HorizontalLayout(new Span("Initial raw:"), initialRawCount,
                        new Span("Server:"), rawServerCountSpan, new Span("Pings:"), pingCountSpan),
                fieldCalendar, rawCalendar);
    }

    private static Span counter(String id) {
        Span span = new Span("0");
        span.setId(id);
        return span;
    }

    private static FullCalendar calendar(String id) {
        FullCalendar calendar = new FullCalendar();
        calendar.setId(id);
        calendar.setHeight("300px");
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 10));
        return calendar;
    }

    /** A callback that counts its calls in the element with the given id. */
    private static JsCallback countIn(String id) {
        return JsCallback.of("function(info) { var el = document.getElementById('" + id + "');"
                + " el.textContent = String(Number(el.textContent) + 1); }");
    }
}

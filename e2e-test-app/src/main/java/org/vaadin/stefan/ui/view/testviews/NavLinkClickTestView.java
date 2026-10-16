package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for {@code navLinkDayClick} and {@code navLinkWeekClick} callbacks beside the calendar's own handlers,
 * which send the {@code DayNumberClickedEvent} and the {@code WeekNumberClickedEvent}.
 * <p>
 * The calendar shows March 2025 with nav links and week numbers. It starts with field callbacks that count in the
 * spans "day-count" and "week-count". Its server listeners count in "day-server-count" and "week-server-count".
 * Buttons clear the day callback and set a raw day callback, which counts in "raw-day-count".
 * <p>
 * The button "Ping" counts its clicks in "ping-count" through the server. Once that count shows, the server has
 * handled every event the client sent before the click.
 * <p>
 * Route: /test/nav-link-click
 */
@Route(value = "nav-link-click", layout = TestLayout.class)
@MenuItem(label = "Nav Link Click")
public class NavLinkClickTestView extends VerticalLayout {

    private int dayServerCount;
    private int weekServerCount;
    private int pingCount;

    public NavLinkClickTestView() {
        setSizeFull();
        add(new H2("Nav Link Click"));

        Span dayServerCountSpan = counter("day-server-count");
        Span weekServerCountSpan = counter("week-server-count");
        Span pingCountSpan = counter("ping-count");

        FullCalendar calendar = new FullCalendar();
        calendar.setId("cal-nav-link");
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 10));
        calendar.options().setNavLinks(true);
        calendar.options().setWeekNumbers(true);
        calendar.options().setNavLinkDayClick(countIn("day-count"));
        calendar.options().setNavLinkWeekClick(countIn("week-count"));
        calendar.addDayNumberClickedListener(e -> dayServerCountSpan.setText(String.valueOf(++dayServerCount)));
        calendar.addWeekNumberClickedListener(e -> weekServerCountSpan.setText(String.valueOf(++weekServerCount)));

        HorizontalLayout buttons = new HorizontalLayout(
                new Button("Clear day callback", e -> calendar.options().setNavLinkDayClick(null)),
                new Button("Set raw day callback",
                        e -> calendar.options().setRawOption("navLinkDayClick", countIn("raw-day-count"))),
                new Button("Ping", e -> pingCountSpan.setText(String.valueOf(++pingCount))));

        add(buttons,
                new HorizontalLayout(new Span("Day:"), counter("day-count"), new Span("Week:"), counter("week-count"),
                        new Span("Raw day:"), counter("raw-day-count")),
                new HorizontalLayout(new Span("Day server:"), dayServerCountSpan,
                        new Span("Week server:"), weekServerCountSpan, new Span("Pings:"), pingCountSpan),
                calendar);
        setFlexGrow(1, calendar);
    }

    private static Span counter(String id) {
        Span span = new Span("0");
        span.setId(id);
        return span;
    }

    /** A callback that counts its calls in the element with the given id. */
    private static JsCallback countIn(String id) {
        return JsCallback.of("function() { var el = document.getElementById('" + id + "');"
                + " el.textContent = String(Number(el.textContent) + 1); }");
    }
}

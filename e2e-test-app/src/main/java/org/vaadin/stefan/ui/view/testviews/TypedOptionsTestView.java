package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.CustomCalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.HeaderAlign;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonFactory;
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
 * The second calendar "cal-custom-view" shows the custom view "threeDays", a day grid of three days from 2025-03-15.
 * Its view options set the class {@code three-day} for day cells.
 * <p>
 * The third calendar "cal-header-hooks" shows the week of 2025-03-15 in the time grid. Its buttons set the all-day
 * header content as text ("Text all-day") or from a callback ("Callback all-day"), and the day header alignment as
 * {@code center} or from a callback that returns {@code end}.
 * <p>
 * The fourth calendar "cal-initial-view-now" starts in the time grid week through the typed {@code initialView}, shows
 * the week of 2025-03-15 and takes 2025-03-12 as the current date ({@code now}).
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

        CustomCalendarView threeDays = new CustomCalendarView.AnonymousCustomCalendarView("threeDays",
                JsonFactory.createObject().put("type", "dayGrid")
                        .set("duration", JsonFactory.createObject().put("days", 3)));
        FullCalendar customViewCalendar = new FullCalendar();
        customViewCalendar.setId("cal-custom-view");
        customViewCalendar.setCustomCalendarViews(threeDays);
        customViewCalendar.options().setRawOption("initialView", "threeDays");
        customViewCalendar.options().setInitialDate(LocalDate.of(2025, 3, 15));
        customViewCalendar.options().view(threeDays).setDayCellClass("three-day");
        customViewCalendar.setHeight("300px");
        add(customViewCalendar);

        FullCalendar headerHooksCalendar = new FullCalendar();
        headerHooksCalendar.setId("cal-header-hooks");
        headerHooksCalendar.options().setRawOption("initialView", "timeGridWeek");
        headerHooksCalendar.options().setInitialDate(LocalDate.of(2025, 3, 15));
        headerHooksCalendar.setHeight("300px");
        add(new HorizontalLayout(
                new Button("All-day header as text",
                        e -> headerHooksCalendar.options().setAllDayHeaderContent("Text all-day")),
                new Button("All-day header from callback", e -> headerHooksCalendar.options().setAllDayHeaderContent(
                        JsCallback.of("function() { return 'Callback all-day'; }"))),
                new Button("Header align center",
                        e -> headerHooksCalendar.options().setDayHeaderAlign(HeaderAlign.CENTER)),
                new Button("Header align from callback", e -> headerHooksCalendar.options().setDayHeaderAlign(
                        JsCallback.of("function() { return 'end'; }")))), headerHooksCalendar);

        FullCalendar initialViewCalendar = new FullCalendar();
        initialViewCalendar.setId("cal-initial-view-now");
        initialViewCalendar.options().setInitialView(CalendarViewImpl.TIME_GRID_WEEK);
        initialViewCalendar.options().setInitialDate(LocalDate.of(2025, 3, 15));
        initialViewCalendar.options().setNow(LocalDate.of(2025, 3, 12));
        initialViewCalendar.setHeight("300px");
        add(initialViewCalendar);
    }
}

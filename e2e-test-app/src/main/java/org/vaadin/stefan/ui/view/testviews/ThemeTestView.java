package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;

/**
 * Test view for FC theme selection (UC-027).
 * <p>
 * Two calendars with different themes on the same page. The buttons switch the theme of the first calendar to
 * each stock theme, to a custom theme registered in the browser ({@code test-custom}) and to a name nobody
 * registered ({@code unknown-theme}). The application stylesheet overrides one color of forma's default palette.
 * A third calendar uses {@code test-gated}, a custom theme that loads only once the test releases it. Two more use
 * {@code test-failing} (two calendars waiting on one load), whose load fails when the test says so, and
 * {@code test-swapped} / {@code test-swapped-failing}, whose loader the test replaces while it loads. A scheduler in
 * a resource view checks that a theme change keeps the scheduler's plugins, and a button sets an option on the first
 * calendar after attach, to check that a theme change keeps it. Two buttons detach and reattach the first calendar.
 * <p>
 * Route: /test/theme
 */
@Route(value = "theme", layout = TestLayout.class)
@MenuItem(label = "Theme")
@JsModule("./theme-test/register-custom-theme.ts")
@CssImport("./theme-test/palette-override.css")
public class ThemeTestView extends VerticalLayout {

    public ThemeTestView() {
        setSizeFull();

        add(new H2("FC themes"));

        FullCalendar first = createCalendar("cal-first", FullCalendarTheme.MONARCH);
        FullCalendar second = createCalendar("cal-second", FullCalendarTheme.PULSE);

        HorizontalLayout buttons = new HorizontalLayout();
        for (String theme : new String[]{FullCalendarTheme.VAADIN, FullCalendarTheme.CLASSIC,
                FullCalendarTheme.MONARCH, FullCalendarTheme.BREEZY, FullCalendarTheme.FORMA, FullCalendarTheme.PULSE,
                "test-custom", "unknown-theme"}) {
            Button button = new Button(theme, e -> first.setTheme(theme));
            button.setId("theme-" + theme);
            buttons.add(button);
        }

        Button weekNumbersOff = new Button("week numbers off", e -> first.setOption(Option.WEEK_NUMBERS, false));
        weekNumbersOff.setId("first-week-numbers-off");
        buttons.add(weekNumbersOff);

        Button detachFirst = new Button("detach first", e -> remove(first));
        detachFirst.setId("first-detach");
        Button attachFirst = new Button("attach first", e -> add(first));
        attachFirst.setId("first-attach");
        buttons.add(detachFirst, attachFirst);

        FullCalendar gated = createCalendar("cal-gated", "test-gated");
        Button gatedUnknown = new Button("gated unknown", e -> gated.setTheme("unknown-theme"));
        gatedUnknown.setId("gated-theme-unknown");
        Button gatedMonarch = new Button("gated monarch", e -> gated.setTheme(FullCalendarTheme.MONARCH));
        gatedMonarch.setId("gated-theme-monarch");
        buttons.add(gatedUnknown, gatedMonarch);

        FullCalendar failing = createCalendar("cal-failing", "test-failing");
        FullCalendar failingToo = createCalendar("cal-failing-too", "test-failing");
        Button failingMonarch = new Button("failing monarch", e -> failing.setTheme(FullCalendarTheme.MONARCH));
        failingMonarch.setId("failing-monarch");
        Button failingAgain = new Button("failing again", e -> failing.setTheme("test-failing"));
        failingAgain.setId("failing-again");
        buttons.add(failingMonarch, failingAgain);
        FullCalendar swapped = createCalendar("cal-swapped", "test-swapped");
        FullCalendar swappedFailing = createCalendar("cal-swapped-failing", "test-swapped-failing");

        FullCalendarScheduler scheduler = new FullCalendarScheduler();
        scheduler.setId("cal-scheduler");
        scheduler.setOption(SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
        scheduler.setTheme(FullCalendarTheme.MONARCH);
        scheduler.changeView(SchedulerView.RESOURCE_TIMELINE_DAY);
        scheduler.addResource(new Resource("room-a", "Room A", null));
        Button schedulerForma = new Button("scheduler forma", e -> scheduler.setTheme(FullCalendarTheme.FORMA));
        schedulerForma.setId("scheduler-theme-forma");
        buttons.add(schedulerForma);

        add(buttons, first, second, gated, failing, failingToo, swapped, swappedFailing, scheduler);
        setFlexGrow(1, first, second, gated, failing, failingToo, swapped, swappedFailing, scheduler);
    }

    private FullCalendar createCalendar(String id, String theme) {
        FullCalendar calendar = new FullCalendar();
        calendar.setId(id);
        calendar.setTheme(theme);
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 10));

        Entry entry = new Entry();
        entry.setTitle("Themed entry");
        entry.setStart(LocalDate.of(2025, 3, 10).atStartOfDay());
        entry.setAllDay(true);
        calendar.setEntryProvider(EntryProvider.inMemoryFrom(entry));

        return calendar;
    }
}

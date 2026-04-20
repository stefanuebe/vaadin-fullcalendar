package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;
import org.vaadin.stefan.fullcalendar.Scheduler;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Regression test view: plain {@link FullCalendar} with entries on the same page as a
 * {@link FullCalendarScheduler}. The scheduler's presence loads FC's Resource plugin,
 * which patches {@code EventApi.prototype.getResources} onto every entry, including
 * the plain calendar's. If the default {@code eventDidMount} snippet (#202) calls
 * {@code event.getResources()} unguarded, the plain entry's {@code _def.resourceIds}
 * is undefined and {@code .map(...)} throws — which previously left entries unrendered
 * on first attach (only appearing after a reload).
 *
 * <p>Route: /test/entry-id-with-scheduler-on-page
 */
@Route(value = "entry-id-with-scheduler-on-page", layout = TestLayout.class)
@MenuItem(label = "Entry ID + Scheduler on Page")
public class EntryIdWithSchedulerOnPageTestView extends VerticalLayout {

    public EntryIdWithSchedulerOnPageTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Entry ID + Scheduler on Page — Regression"));
        add(new Paragraph(
                "Plain FullCalendar with entries alongside a Scheduler. The scheduler forces "
                        + "the Resource plugin to load, which patches EventApi.getResources onto "
                        + "the plain calendar's entries — the default eventDidMount snippet must "
                        + "survive a throw from that patched method."));

        // The plain calendar that must render entries on first attach.
        FullCalendar plain = new FullCalendar();
        plain.getElement().setAttribute("data-testid", "plain-calendar");
        plain.changeView(CalendarViewImpl.DAY_GRID_MONTH);
        plain.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());

        Entry e1 = new Entry("e1");
        e1.setTitle("Entry 1");
        e1.setStart(LocalDate.of(2025, 3, 5).atTime(10, 0));
        e1.setEnd(LocalDate.of(2025, 3, 5).atTime(11, 0));

        Entry e2 = new Entry("e2");
        e2.setTitle("Entry 2");
        e2.setStart(LocalDate.of(2025, 3, 10).atTime(14, 0));
        e2.setEnd(LocalDate.of(2025, 3, 10).atTime(15, 0));

        plain.setEntryProvider(EntryProvider.inMemoryFrom(List.of(e1, e2)));

        // The scheduler instance only exists to force the Resource plugin to load.
        // No entries / resources — just the empty calendar.
        FullCalendarScheduler scheduler = new FullCalendarScheduler();
        scheduler.setOption(FullCalendarScheduler.SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
        scheduler.getElement().setAttribute("data-testid", "scheduler-loader");
        scheduler.getStyle().set("display", "none");

        add(scheduler, plain);
    }
}

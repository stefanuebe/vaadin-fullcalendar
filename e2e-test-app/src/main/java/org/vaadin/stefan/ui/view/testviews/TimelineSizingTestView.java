package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.Scheduler;
import org.vaadin.stefan.fullcalendar.SchedulerView;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Regression test view for issue #231: initial render of a scheduler with many resources
 * must produce a timeline whose inner scrollgrid chunk tables fill the available container
 * width. Pre-fix, the tables got stuck at {@code style="width: 0px; min-width: 930px"},
 * rendering at ~930 px inside a ~1535 px container.
 * <p>
 * Reproducer: 11 resources in {@link SchedulerView#RESOURCE_TIMELINE_MONTH}. No entries,
 * since the bug is about resource row count / Preact settling time, not entries.
 * <p>
 * Route: /test/timeline-sizing
 */
@Route(value = "timeline-sizing", layout = TestLayout.class)
@MenuItem(label = "Timeline Sizing")
public class TimelineSizingTestView extends VerticalLayout {

    public TimelineSizingTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Timeline Sizing — Regression #231"));
        add(new Paragraph(
                "After load, the inner scrollgrid chunk tables must fill the container "
                        + "width, not snap to min-width (~930 px)."));

        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.setOption(FullCalendarScheduler.SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
        calendar.setSizeFull();
        calendar.getElement().setAttribute("data-testid", "calendar");

        calendar.setOption("initialDate", LocalDate.of(2025, 3, 1).toString());
        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_MONTH);

        // 100 resources — enough to reliably flip the Preact-settling race on the fix-less
        // build, even on fast CI hardware. The reporter's machine reproduced with 11; on
        // faster containers we need more work to exceed the double-rAF settling window.
        List<Resource> resources = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            resources.add(new Resource(UUID.randomUUID().toString(), "Resource " + i, null));
        }
        calendar.addResources(resources);

        add(calendar);
        setFlexGrow(1, calendar);
    }
}

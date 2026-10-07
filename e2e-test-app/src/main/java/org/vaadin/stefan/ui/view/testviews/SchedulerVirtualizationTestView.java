package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Test view for the scheduler options {@link SchedulerOption#VIRTUALIZATION} and
 * {@link SchedulerOption#RESOURCE_ROW_CLASS}. 300 resources are shown in resourceTimelineDay.
 * Virtualization is on initially, a button switches it off.
 * <p>
 * Route: /test/scheduler-virtualization
 */
@Route(value = "scheduler-virtualization", layout = TestLayout.class)
@MenuItem(label = "Scheduler Virtualization")
public class SchedulerVirtualizationTestView extends VerticalLayout {

    public static final int RESOURCE_COUNT = 300;
    public static final int PRINT_MAX_ROWS = 20;

    public SchedulerVirtualizationTestView() {
        setSizeFull();
        setPadding(true);
        add(new H2("Scheduler virtualization"));

        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.setOption(SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
        calendar.setOption(Option.INITIAL_DATE, LocalDate.of(2025, 3, 3));
        calendar.setOption(Option.INITIAL_VIEW, SchedulerView.RESOURCE_TIMELINE_DAY.getClientSideValue());
        calendar.setOption(Option.HEIGHT, "500px");
        calendar.setOption(SchedulerOption.VIRTUALIZATION, true);
        calendar.setOption(SchedulerOption.RESOURCE_ROW_CLASS, "hook-resource-row");
        calendar.setOption(SchedulerOption.PRINT_MAX_ROWS, PRINT_MAX_ROWS);

        List<Resource> resources = new ArrayList<>();
        for (int i = 0; i < RESOURCE_COUNT; i++) {
            resources.add(new Resource("r" + i, "Resource " + i, null));
        }
        calendar.addResources(resources);

        Button off = new Button("Virtualization off", e -> calendar.setOption(SchedulerOption.VIRTUALIZATION, false));
        off.setId("virtualization-off");

        add(off, calendar);
        setFlexGrow(1, calendar);
    }
}

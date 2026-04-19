package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test view for verifying that scheduler resources survive detach/reattach.
 * <p>
 * A button removes the calendar from the layout and re-adds it.
 * After reattach, resources and entries must still be visible.
 * <p>
 * Route: /test/scheduler-reattach
 */
@Route(value = "scheduler-reattach", layout = TestLayout.class)
@MenuItem(label = "Scheduler Reattach")
public class SchedulerReattachTestView extends VerticalLayout {

    public SchedulerReattachTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Scheduler Reattach Test"));
        add(new Paragraph("Click the button to detach and reattach the calendar. Resources must survive."));

        // Build the scheduler calendar
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.setOption(FullCalendarScheduler.SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);

        calendar.getElement().setAttribute("data-testid", "calendar");

        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_WEEK);

        calendar.setResourceAreaColumns(
                new ResourceAreaColumn("title", "Resource Name")
        );

        // Resources
        Resource r1 = new Resource("r1", "Alice", "blue");
        Resource r2 = new Resource("r2", "Bob", null);
        calendar.addResources(List.of(r1, r2));

        // Entries
        InMemoryEntryProvider<ResourceEntry> provider = new InMemoryEntryProvider<>();

        ResourceEntry aliceTask = new ResourceEntry();
        aliceTask.setTitle("Alice Task");
        aliceTask.setStart(LocalDateTime.of(2025, 3, 3, 9, 0));
        aliceTask.setEnd(LocalDateTime.of(2025, 3, 3, 10, 0));
        aliceTask.addResources(r1);

        ResourceEntry bobTask = new ResourceEntry();
        bobTask.setTitle("Bob Task");
        bobTask.setStart(LocalDateTime.of(2025, 3, 4, 14, 0));
        bobTask.setEnd(LocalDateTime.of(2025, 3, 4, 15, 0));
        bobTask.addResources(r2);

        provider.addEntries(aliceTask, bobTask);
        calendar.setEntryProvider(provider);

        // Detach/reattach button
        Button reattachButton = new Button("Detach & Reattach", e -> {
            remove(calendar);
            // Re-add in the same server round-trip
            add(calendar);
        });
        reattachButton.setId("reattach-button");

        add(reattachButton, calendar);
    }
}

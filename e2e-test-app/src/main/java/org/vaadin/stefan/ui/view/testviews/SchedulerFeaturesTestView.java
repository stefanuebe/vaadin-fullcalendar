package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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
 * Test view for scheduler resource features.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Calendar renders in resource timeline view</li>
 *   <li>Resource area columns (title + department) are rendered</li>
 *   <li>Resources are listed in the resource area</li>
 *   <li>Resource grouping by department renders group headers</li>
 *   <li>ResourceEntry events are visible in the timeline</li>
 * </ul>
 * <p>
 * Route: /test/scheduler-features
 */
@Route(value = "scheduler-features", layout = TestLayout.class)
@MenuItem(label = "Scheduler Features")
public class SchedulerFeaturesTestView extends VerticalLayout {

    public SchedulerFeaturesTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Scheduler Resource Features"));
        add(new Paragraph(
                "A FullCalendarScheduler is shown in resourceTimelineWeek view with three resources " +
                "grouped by department, two resource area columns, and resource group class name callback."));

        // Stable DOM anchor for Playwright
        Span groupLabelArea = new Span("Resource Groups:");
        groupLabelArea.setId("group-label-area");
        add(groupLabelArea);

        // Build the scheduler calendar
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.setOption(FullCalendarScheduler.SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);

        calendar.getElement().setAttribute("data-testid", "calendar");

        // Fix date and view for reproducibility
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.setOption("initialView", SchedulerView.RESOURCE_TIMELINE_WEEK.getClientSideValue());

        // --- Scheduler features ---

        // 1. Two resource area columns: built-in "title" field + a "dept" column
        // Note: use "dept" (not "department") for the column field so individual resource rows
        // don't show the department text — only group headers should show "Engineering"/"Design"
        // to avoid Playwright strict mode violations (multiple elements matching the same text)
        calendar.setResourceAreaColumns(
                new ResourceAreaColumn("title", "Resource Name"),
                new ResourceAreaColumn("dept", "Dept")
        );

        // 2. Group resources by the "department" extendedProp
        calendar.setOption(FullCalendarScheduler.SchedulerOption.RESOURCE_GROUP_FIELD, "department");

        // 3. Add CSS class to group header rows via JS callback
        // Use raw string key because SchedulerOption.RESOURCE_GROUP_CLASS_NAMES maps to a
        // non-existent FC option. The correct FC v6 option is "resourceGroupLabelClassNames".
        calendar.setOption("resourceGroupLabelClassNames",
                JsCallback.of("function(arg) { return ['custom-group']; }"));

        // --- Resources ---
        Resource r1 = new Resource("r1", "Alice", "blue");
        r1.addExtendedProps("department", "Engineering");

        Resource r2 = new Resource("r2", "Bob", null);
        r2.addExtendedProps("department", "Engineering");

        Resource r3 = new Resource("r3", "Carol", "green");
        r3.addExtendedProps("department", "Design");

        calendar.addResources(List.of(r1, r2, r3));

        // --- Entries (ResourceEntry carries resource assignment) ---
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

        add(calendar);
    }
}

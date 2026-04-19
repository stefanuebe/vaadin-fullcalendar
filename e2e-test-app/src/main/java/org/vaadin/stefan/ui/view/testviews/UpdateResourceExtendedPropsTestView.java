package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
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

/**
 * Regression test view for issue #230: {@code Scheduler.updateResource} must propagate
 * extended property changes to the client.
 * <p>
 * Renders a scheduler with one resource carrying an extended prop {@code department=Engineering}.
 * A button triggers {@code setExtendedProps("department", "Marketing")} followed by
 * {@code updateResource(resource)}. The Playwright spec reads the prop from the FC client via
 * {@code calendar.getResourceById('r1').extendedProps.department} before and after the click.
 * <p>
 * Route: /test/update-resource-extended-props
 */
@Route(value = "update-resource-extended-props", layout = TestLayout.class)
@MenuItem(label = "Update Resource Extended Props")
public class UpdateResourceExtendedPropsTestView extends VerticalLayout {

    public UpdateResourceExtendedPropsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Update Resource Extended Props — Regression #230"));
        add(new Paragraph(
                "Click \"Change department\" to update the extended prop and sync to the client."));

        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.setOption(FullCalendarScheduler.SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
        calendar.getElement().setAttribute("data-testid", "calendar");

        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_WEEK);

        Resource room = new Resource("r1", "Room 1", null);
        room.addExtendedProps("department", "Engineering");
        calendar.addResource(room);

        Button updateBtn = new Button("Change department", e -> {
            room.addExtendedProps("department", "Marketing");
            calendar.updateResource(room);
        });
        updateBtn.getElement().setAttribute("data-testid", "btn-change-dept");

        add(updateBtn, calendar);
    }
}

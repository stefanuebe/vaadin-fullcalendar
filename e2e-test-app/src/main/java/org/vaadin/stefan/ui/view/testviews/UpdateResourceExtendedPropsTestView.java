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
import java.util.Map;

/**
 * Regression test view for issue #230: {@code Scheduler.updateResource} must propagate
 * extended property changes to the client.
 * <p>
 * Renders a scheduler with one resource carrying the extended props {@code department=Engineering} and
 * {@code floor=3}. A button triggers {@code addExtendedProps("department", "Marketing")} followed by
 * {@code updateResource(resource)}, a second removes {@code department}, a third replaces all extended props with
 * {@code building=B} through the map and calls {@code updateResource(resource)}. The Playwright spec reads the
 * props from the FC client via {@code calendar.getResourceById('r1').extendedProps} before and after the click.
 * <p>
 * A second resource {@code r2} uses only the extended prop methods of 8.0: {@code setExtendedProp},
 * {@code removeExtendedProp} and {@code setExtendedProps}. Its extended prop {@code location} is a record, which is
 * sent as a JSON object.
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
        room.addExtendedProps("floor", 3);
        calendar.addResource(room);

        Button updateBtn = new Button("Change department", e -> {
            room.addExtendedProps("department", "Marketing");
            calendar.updateResource(room);
        });
        updateBtn.getElement().setAttribute("data-testid", "btn-change-dept");

        Button removeBtn = new Button("Remove department", e -> room.removeExtendedProps("department"));
        removeBtn.getElement().setAttribute("data-testid", "btn-remove-dept");

        Button replaceBtn = new Button("Replace extended props", e -> {
            room.getExtendedProps().clear();
            room.getExtendedProps().put("building", "B");
            calendar.updateResource(room);
        });
        replaceBtn.getElement().setAttribute("data-testid", "btn-replace-props");

        Resource room2 = new Resource("r2", "Room 2", null);
        room2.setExtendedProp("department", "Engineering");
        room2.setExtendedProp("location", new Location("A", 2));
        calendar.addResource(room2);

        Button setBtn2 = new Button("Change department of room 2", e -> room2.setExtendedProp("department", "Marketing"));
        setBtn2.getElement().setAttribute("data-testid", "btn-change-dept-r2");

        Button removeBtn2 = new Button("Remove department of room 2", e -> room2.removeExtendedProp("department"));
        removeBtn2.getElement().setAttribute("data-testid", "btn-remove-dept-r2");

        Button replaceBtn2 = new Button("Replace extended props of room 2",
                e -> room2.setExtendedProps(Map.of("building", "B")));
        replaceBtn2.getElement().setAttribute("data-testid", "btn-replace-props-r2");

        add(updateBtn, removeBtn, replaceBtn, setBtn2, removeBtn2, replaceBtn2, calendar);
    }

    record Location(String building, int floor) {
    }
}

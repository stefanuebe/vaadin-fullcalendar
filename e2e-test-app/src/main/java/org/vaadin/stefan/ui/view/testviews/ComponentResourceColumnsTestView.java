package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test view for component resource area columns (UC-024).
 * <p>
 * Verifies:
 * <ul>
 *   <li>DatePicker and TextField render in resource cells</li>
 *   <li>Components are interactive</li>
 *   <li>Components survive view changes</li>
 *   <li>Add/remove resource creates/destroys components</li>
 *   <li>Detach/reattach preserves component state</li>
 * </ul>
 * <p>
 * Route: /test/component-resource-columns
 */
@Route(value = "component-resource-columns", layout = TestLayout.class)
@MenuItem(label = "Component Resource Columns")
public class ComponentResourceColumnsTestView extends VerticalLayout {

    private final FullCalendarScheduler calendar;
    private final ComponentResourceAreaColumn<DatePicker> dateColumn;
    private final ComponentResourceAreaColumn<TextField> notesColumn;
    private final Span stateSpan;

    public ComponentResourceColumnsTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Component Resource Columns"));
        add(new Paragraph("A scheduler with DatePicker and TextField component columns."));

        stateSpan = new Span("");
        stateSpan.setId("component-state");
        add(stateSpan);

        // Build scheduler
        calendar = (FullCalendarScheduler) FullCalendarBuilder.create()
                .withScheduler(Scheduler.DEVELOPER_LICENSE_KEY)
                .build();
        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.setOption("initialView", SchedulerView.RESOURCE_TIMELINE_WEEK.getClientSideValue());

        // Component columns
        dateColumn = new ComponentResourceAreaColumn<>("deadline", "Deadline",
                resource -> {
                    DatePicker picker = new DatePicker();
                    picker.setWidth("130px");
                    DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
                    i18n.setDateFormat("yyyy-MM-dd");
                    picker.setI18n(i18n);
                    picker.getElement().setAttribute("data-testid",
                            "datepicker-" + resource.getId());
                    return picker;
                });

        notesColumn = new ComponentResourceAreaColumn<>("notes", "Notes",
                resource -> {
                    TextField field = new TextField();
                    field.setWidth("120px");
                    field.setPlaceholder("Notes...");
                    field.getElement().setAttribute("data-testid",
                            "textfield-" + resource.getId());
                    return field;
                });

        calendar.setResourceAreaColumns(List.of(
                new ResourceAreaColumn("title", "Name").withWidth("150px"),
                dateColumn.withWidth("160px"),
                notesColumn.withWidth("150px")
        ));

        // Resources
        Resource resA = new Resource("res-a", "Alice", "#3788d8");
        Resource resB = new Resource("res-b", "Bob", "#e53935");
        Resource resParent = new Resource("res-parent", "Building", null);
        Resource resChild = new Resource("res-child", "Floor 1", null);
        resParent.addChild(resChild);

        calendar.addResources(resA, resB, resParent);

        // Entry for visual context
        ResourceEntry entry = new ResourceEntry();
        entry.setTitle("Meeting");
        entry.setStart(LocalDateTime.of(2025, 3, 3, 10, 0));
        entry.setEnd(LocalDateTime.of(2025, 3, 3, 12, 0));
        entry.addResources(resA);
        calendar.getEntryProvider().asInMemory().addEntry(entry);

        calendar.setSizeFull();
        add(calendar);
        setFlexGrow(1, calendar);

        // Action buttons
        Button addResourceBtn = new Button("Add Resource", e -> {
            Resource newRes = new Resource("res-new", "Charlie", "#4caf50");
            calendar.addResource(newRes);
        });
        addResourceBtn.setId("btn-add-resource");

        Button removeResourceBtn = new Button("Remove Bob", e -> {
            calendar.getResourceById("res-b").ifPresent(calendar::removeResource);
        });
        removeResourceBtn.setId("btn-remove-resource");

        Button detachBtn = new Button("Detach Calendar", e -> {
            remove(calendar);
        });
        detachBtn.setId("btn-detach");

        Button reattachBtn = new Button("Reattach Calendar", e -> {
            add(calendar);
            setFlexGrow(1, calendar);
        });
        reattachBtn.setId("btn-reattach");

        Button readStateBtn = new Button("Read State", e -> {
            StringBuilder sb = new StringBuilder();
            dateColumn.getComponents().forEach((id, picker) -> {
                sb.append("date[").append(id).append("]=").append(picker.getValue()).append(";");
            });
            notesColumn.getComponents().forEach((id, field) -> {
                sb.append("notes[").append(id).append("]=").append(field.getValue()).append(";");
            });
            stateSpan.setText(sb.toString());
        });
        readStateBtn.setId("btn-read-state");

        add(new HorizontalLayout(addResourceBtn, removeResourceBtn, detachBtn, reattachBtn, readStateBtn));
    }
}

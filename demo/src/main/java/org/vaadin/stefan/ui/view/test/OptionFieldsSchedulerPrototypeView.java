package org.vaadin.stefan.ui.view.test;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Option;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.Scheduler;
import org.vaadin.stefan.fullcalendar.SchedulerOption;
import org.vaadin.stefan.fullcalendar.SchedulerView;
import org.vaadin.stefan.fullcalendar.prototype.OptionFieldsPrototypeScheduler;
import org.vaadin.stefan.fullcalendar.prototype.SchedulerViewOptions;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.time.LocalDate;
import java.util.List;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. The scheduler with options as fields.
 * <p>
 * Route: /option-fields-scheduler-prototype
 */
@Route(value = "option-fields-scheduler-prototype", layout = MainLayout.class)
public class OptionFieldsSchedulerPrototypeView extends VerticalLayout {

    private final Pre sent = new Pre();

    public OptionFieldsSchedulerPrototypeView() {
        setSizeFull();
        add(new H2("PROTOTYPE: scheduler options as fields"));

        OptionFieldsPrototypeScheduler calendar = new OptionFieldsPrototypeScheduler();
        calendar.getElement().setAttribute("data-testid", "calendar");
        calendar.setOption(SchedulerOption.LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
        calendar.setOption(Option.INITIAL_VIEW, SchedulerView.RESOURCE_TIMELINE_DAY.getClientSideValue());
        calendar.addResources(List.of(new Resource("a", "Room A", null), new Resource("b", "Room B", null)));
        calendar.setSyncLog(sent::setText);
        // PROTOTYPE check: does an option that only works at creation take effect when the sync sends it?
        calendar.setRawOption("initialDate", "2025-06-01");
        calendar.setHeight("400px");

        sent.getElement().setAttribute("data-testid", "sent");

        FlexLayout buttons = new FlexLayout(
                new Button("core field: allDayText = 'Whole day'", e -> calendar.setAllDayText("Whole day")),
                new Button("scheduler view field: slotMinWidth = 120", e -> calendar.setSlotMinWidth(120)),
                new Button("scheduler global field: resourceOrder = '-title'", e -> calendar.setResourceOrder("-title")),
                new Button("timeline week: core + scheduler view options", e -> calendar.setViewOptions(
                        SchedulerView.RESOURCE_TIMELINE_WEEK, SchedulerViewOptions.builder()
                                .allDayText("Week")
                                .resourceColumnsWidth("30%")
                                .build())),
                new Button("timeline week: toBuilder, slotMinWidth = 40", e -> calendar.setViewOptions(
                        SchedulerView.RESOURCE_TIMELINE_WEEK, calendar.getViewOptions(SchedulerView.RESOURCE_TIMELINE_WEEK)
                                .toBuilder().slotMinWidth(40).build())));
        buttons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        buttons.getStyle().set("gap", "4px");
        add(buttons, sent, calendar);
    }
}

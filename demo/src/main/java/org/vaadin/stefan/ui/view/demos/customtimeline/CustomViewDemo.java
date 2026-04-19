package org.vaadin.stefan.ui.view.demos.customtimeline;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;

import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

@Route(value = "custom-view", layout = MainLayout.class)
@MenuItem(label = "Custom View")
public class CustomViewDemo extends AbstractSchedulerView {

    private FixedDaysCalendarView calendarView;

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        calendarView = new FixedDaysCalendarView(28);

        // test for duplicat registration (anonymous and named)
//        ObjectNode initialOptions = Json.createObject();
//        ObjectNode views = Json.createObject();
//        views.put(calendarView.getClientSideValue(), new FixedDaysCalendarView(5).getViewSettings());
//        initialOptions.put("views", views);

        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.setOption(FullCalendarScheduler.SchedulerOption.LICENSE_KEY, Scheduler.GPL_V3_LICENSE_KEY);
        calendar.setCustomCalendarViews(calendarView);
        calendar.setLocale(UI.getCurrent().getLocale());

        List<Entry> entries = EntryService.createRandomInstance().getEntries();
        calendar.getEntryProvider().asInMemory().addEntries(entries);

        return calendar;
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {
        calendar.changeView(calendarView);
    }

    @Override
    protected String createDescription() {
        return "This demo shows how to create a custom view. The view is based on the timeline view, but only shows a" +
                " fixed number of days. The view is configured to show 28 days.";
    }

    @Override
    protected String createTitle() {
        return "Custom View Demo";
    }

    @Override
    protected boolean isToolbarViewChangeable() {
        return false;
    }


}
package org.vaadin.stefan.ui.view.demos.customtimeline;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import elemental.json.JsonObject;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.model.Header;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterItem;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPart;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;

@Route(value = "custom-view", layout = MainLayout.class)
@MenuItem(label = "Custom View")
public class CustomViewDemo extends AbstractSchedulerView {

    private FixedDaysCalendarView calendarView;

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        calendarView = new FixedDaysCalendarView(28);
        FullCalendar calendar = FullCalendarBuilder.create()
                .withScheduler("GPL-My-Project-Is-Open-Source")
                .withInitialOptions(calendarView.getInitialOptions()).build();
        calendar.setLocale(CalendarLocale.getDefaultLocale());
        calendar.setHeight("100%");
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
}
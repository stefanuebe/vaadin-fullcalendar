package org.vaadin.stefan.ui.view.demos.calendaritemprovider;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;

@Route(value = "cip-backend", layout = MainLayout.class)
@MenuItem(label = "Backend CIP")
public class BackendCipDemo extends AbstractDemoView {

    @Override
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create().build();
        return calendar;
    }

    @Override
    protected String createDescription() {
        return "This demo is under construction.";
    }

    @Override
    protected boolean isCodeDisplayEnabled() {
        return false;
    }
}

package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import elemental.json.JsonObject;
import org.vaadin.stefan.AbstractCalendarView;
import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.time.DayOfWeek;

@Route(value = "basic-demo", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Basic FullCalendar Demo")
@MenuItem(label = "Basic Demo")
public class BasicDemo extends AbstractCalendarView {

    @Override
    protected EntryService initEntryService(EntryService entryService) {
        entryService.fillDatabaseWithSimpleData();
        return entryService;
    }

    @Override
    protected FullCalendar createFullCalendar() {
        FullCalendar calendar = super.createFullCalendar();
        calendar.setBusinessHours(new BusinessHours(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        return calendar;
    }

    @Override
    protected JsonObject createInitialOptions(JsonObject initialOptions) {
        initialOptions.put("locale", "de");
        return initialOptions;
    }

}

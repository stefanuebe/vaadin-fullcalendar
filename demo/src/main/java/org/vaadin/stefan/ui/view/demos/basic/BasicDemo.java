package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import elemental.json.JsonObject;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.time.DayOfWeek;

// not sure if we still need the basic demo now with the cleaned up full demo
//@Route(value = "basic-demo", layout = MainLayout.class)
//@MenuItem(label = "Basic Demo")
//public class BasicDemo extends AbstractCalendarView {
//
//    @Override
//    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
//        EntryService simpleInstance = EntryService.createSimpleInstance();
//
//        FullCalendar calendar = FullCalendarBuilder.create()
//                .withInitialOptions(defaultInitialOptions)
//                .withInitialEntries(simpleInstance.getEntries())
//                .withEntryLimit(3)
//                .build();
//
//        calendar.setBusinessHours(new BusinessHours(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
//
//        return calendar;
//    }
//
//    @Override
//    protected String createDescription() {
//        return "A reduced version of the playground demo, showing the basic interaction events with the calendar and allow basic modification of entries.";
//    }
//}

package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.router.RouteAlias;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.vaadin.stefan.AbstractCalendarView;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;
import org.vaadin.stefan.util.EntryManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

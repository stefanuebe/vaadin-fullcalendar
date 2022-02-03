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
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.util.EntryManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Route(value = "basic-demo", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Basic FullCalendar Demo")
@MenuItem(label = "Basic Demo")
public class BasicDemo extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private FullCalendar calendar;
    private FormLayout toolbar;
    private int count = 1;

    public BasicDemo() {
    	initView();

    	createToolbar();
		add(toolbar);

		createCalendar();
		add(calendar);

		createBasicEntries(calendar);
    }

    private void initView() {
		setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createToolbar() {
    	Button buttonPrevious = new Button("Previous", VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
        Button buttonToday = new Button("Today", VaadinIcon.HOME.create(), e -> calendar.today());
        Button buttonNext = new Button("Next", VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        buttonNext.setIconAfterText(true);

        // simulate the date picker light that we can use in polymer
        DatePicker gotoDate = new DatePicker();
        gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
        gotoDate.getElement().getStyle().set("visibility", "hidden");
        gotoDate.getElement().getStyle().set("position", "fixed");
        gotoDate.setWeekNumbersVisible(true);
        Button buttonDatePicker = new Button("", VaadinIcon.CALENDAR.create());
        buttonDatePicker.getElement().appendChild(gotoDate.getElement());
        buttonDatePicker.addClickListener(event -> gotoDate.open());

        List<CalendarView> calendarViews = new ArrayList<>(Arrays.asList(CalendarViewImpl.values()));
        calendarViews.addAll(Arrays.asList(SchedulerView.values()));
        calendarViews.sort(Comparator.comparing(CalendarView::getName));

        ComboBox<CalendarView> comboBoxView = new ComboBox<>("", calendarViews);
        comboBoxView.setValue(CalendarViewImpl.DAY_GRID_MONTH);
        comboBoxView.setWidthFull();
        comboBoxView.addValueChangeListener(e -> {
            CalendarView value = e.getValue();
            calendar.changeView(value == null ? CalendarViewImpl.DAY_GRID_MONTH : value);
        });

        Button attachButton = new Button(VaadinIcon.EYE_SLASH.create(), event -> {
            if (calendar.getParent().isPresent()) {
                remove(calendar);
                event.getSource().setIcon(VaadinIcon.EYE.create());
            } else {
                add(calendar);
                event.getSource().setIcon(VaadinIcon.EYE_SLASH.create());
            }
        });

        ComboBox<Timezone> timezoneComboBox = new ComboBox<>("");
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
        timezoneComboBox.setItems(Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));
        timezoneComboBox.setValue(Timezone.UTC);
        timezoneComboBox.addValueChangeListener(event -> {
            Timezone value = event.getValue();
            calendar.setTimezone(value != null ? value : Timezone.UTC);
        });
        timezoneComboBox.setWidthFull();

        Button add = new Button("Add single entry", event -> {
            Entry entry = new Entry();
            entry.setStart(LocalDate.now().atTime(10, 0));
            entry.setEnd(LocalDate.now().atTime(11, 0));
            entry.setTitle("Test " + count++);
            calendar.addEntry(entry);
        });

        toolbar = new FormLayout(buttonToday, buttonPrevious, buttonNext, buttonDatePicker, comboBoxView, attachButton, timezoneComboBox, add);
        toolbar.getElement().getStyle().set("margin-top", "0px");
        toolbar.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("25em", (int) toolbar.getChildren().count()));
    }


    private void createCalendar() {
        JsonObject initialOptions = Json.createObject();
        initialOptions.put("locale", "de");

        JsonObject eventTimeFormat = Json.createObject();
        //{ hour: 'numeric', minute: '2-digit', timeZoneName: 'short' }
        eventTimeFormat.put("hour", "numeric");
        eventTimeFormat.put("minute", "2-digit");
        eventTimeFormat.put("timeZoneName", "short");
        initialOptions.put("eventTimeFormat", eventTimeFormat);

        calendar = FullCalendarBuilder.create()
                .withScheduler("GPL-My-Project-Is-Open-Source")
                .withInitialOptions(initialOptions)
                .build();

        calendar.setSizeFull();
        calendar.setHeightByParent();
        calendar.setNowIndicatorShown(true);

        ((FullCalendarScheduler) calendar).setSlotMinWidth("150");
        ((FullCalendarScheduler) calendar).setResourceAreaWidth("15%");

        calendar.setBusinessHours(new BusinessHours(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
		calendar.setFirstDay(DayOfWeek.MONDAY);
    }

    private void createBasicEntries(FullCalendar calendar) {
        LocalDate now = LocalDate.now();
        EntryManager.createTimedEntry(calendar, "Grocery Store", now.withDayOfMonth(7).atTime(17, 30), 45, "green");
        EntryManager.createTimedEntry(calendar, "Dentist", now.withDayOfMonth(20).atTime(11, 30), 60, "green");
        EntryManager.createTimedEntry(calendar, "Cinema", now.withDayOfMonth(10).atTime(20, 30), 140, "green");
        EntryManager.createDayEntry(calendar, "Short trip", now.withDayOfMonth(17), 2, "green");
        EntryManager.createDayEntry(calendar, "John's Birthday", now.withDayOfMonth(23), 1, "green");
        EntryManager.createDayEntry(calendar, "This special holiday", now.withDayOfMonth(4), 1, "green");
    }
}

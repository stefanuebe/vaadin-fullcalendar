package org.vaadin.stefan;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Route(value = "demo2", layout = MainView.class)
public class Demo2 extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
    public Demo2() {
        initBasicDemo();
    }

    private void initBasicDemo() {
    	getStyle().set("flex-grow", "1");

        FullCalendar calendar = FullCalendarBuilder.create().withScheduler("GPL-My-Project-Is-Open-Source").build();

        setFlexGrow(1, calendar);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        createBasicEntries(calendar);

        add(new H1("Basic calendar"), createBasicToolbar(calendar), calendar);
    }

    private void createBasicEntries(FullCalendar calendar) {
        LocalDate now = LocalDate.now();
        Demo.createTimedEntry(calendar, "Grocery Store", now.withDayOfMonth(7).atTime(17, 30), 45, "purple");
        Demo.createTimedEntry(calendar, "Dentist", now.withDayOfMonth(20).atTime(11, 30), 60, "purple");
        Demo.createTimedEntry(calendar, "Cinema", now.withDayOfMonth(10).atTime(20, 30), 140, "dodgerblue");
        Demo.createDayEntry(calendar, "Short trip", now.withDayOfMonth(17), 2, "dodgerblue");
        Demo.createDayEntry(calendar, "John's Birthday", now.withDayOfMonth(23), 1, "gray");
        Demo.createDayEntry(calendar, "This special holiday", now.withDayOfMonth(4), 1, "gray");
    }

    private HorizontalLayout createBasicToolbar(FullCalendar calendar) {
        Button buttonToday = new Button("Today", VaadinIcon.HOME.create(), e -> calendar.today());
        Button buttonPrevious = new Button("Previous", VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
        Button buttonNext = new Button("Next", VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        buttonNext.setIconAfterText(true);

        // simulate the date picker light that we can use in polymer
        DatePicker gotoDate = new DatePicker();
        gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
        gotoDate.getElement().getStyle().set("visibility", "hidden");
        gotoDate.getElement().getStyle().set("position", "fixed");
        gotoDate.setWidth("0px");
        gotoDate.setHeight("0px");
        gotoDate.setWeekNumbersVisible(true);
        Button buttonDatePicker = new Button(VaadinIcon.CALENDAR.create());
        buttonDatePicker.getElement().appendChild(gotoDate.getElement());
        buttonDatePicker.addClickListener(event -> gotoDate.open());

        List<CalendarView> calendarViews = new ArrayList<>(Arrays.asList(CalendarViewImpl.values()));
        calendarViews.addAll(Arrays.asList(SchedulerView.values()));
        calendarViews.sort(Comparator.comparing(CalendarView::getName));

        ComboBox<CalendarView> comboBoxView = new ComboBox<>("", calendarViews);
        comboBoxView.setValue(CalendarViewImpl.DAY_GRID_MONTH);
        comboBoxView.setWidth("300px");
        comboBoxView.addValueChangeListener(e -> {
            CalendarView value = e.getValue();
            calendar.changeView(value == null ? CalendarViewImpl.DAY_GRID_MONTH : value);
        });

        return new HorizontalLayout(buttonToday, buttonPrevious, buttonNext, buttonDatePicker, gotoDate, comboBoxView);
    }


    // plain / basic settings, only click listener, resizable / draggable

    // different entry types

    // resources

    // with selectable

    // limited entries

    // custom entry rendering

    // with business hours

    // locale / timezone


}

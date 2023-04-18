package org.vaadin.stefan.ui.view.demos.backgroundevent;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.DisplayMode;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.io.Serial;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Route(value = "demobackgroundevent", layout = MainLayout.class)
@MenuItem(label = "Background Events")
public class DemoCalendarWithBackgroundEvent extends VerticalLayout {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private FullCalendar calendar;

    public DemoCalendarWithBackgroundEvent() {
    	initView();

        createCalendarInstance();
        addBackgroundEvent(DisplayMode.BACKGROUND, 4, "This special holiday");
        addBackgroundEvent(DisplayMode.BACKGROUND, 6, "");

        add(calendar);
        setFlexGrow(1, calendar);
    }
    
    private void initView() {
    	setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }


    private void createCalendarInstance() {
        calendar = FullCalendarBuilder.create().withScheduler().build();
        calendar.setHeightByParent();
        ((FullCalendarScheduler) calendar).setSchedulerLicenseKey("GPL-My-Project-Is-Open-Source");
        ((FullCalendarScheduler) calendar).setSlotMinWidth("150");
        calendar.setLocale(Locale.ENGLISH);

        calendar.changeView(SchedulerView.TIMELINE_MONTH);
    }

    private void addBackgroundEvent(DisplayMode displayMode, int offset, String title) {
        LocalDate now = LocalDate.now();
        
        ResourceEntry entry = new ResourceEntry();
        
        entry.setTitle(title);
        entry.setStart(now.withDayOfMonth(offset));
        entry.setEnd(entry.getStart().plus(1, ChronoUnit.DAYS));
        entry.setAllDay(true);
        entry.setColor("red");
        entry.setDisplayMode(displayMode);
        entry.setResourceEditable(true);
        
        calendar.getEntryProvider().asInMemory().addEntry(entry);
    }
}
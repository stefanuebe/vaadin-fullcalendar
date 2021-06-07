package org.vaadin.stefan;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import org.vaadin.stefan.fullcalendar.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Route(value = "demobackgroundevent", layout = MainView.class)
public class DemoCalendarWithBackgroundEvent extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    
    private FullCalendar calendar;

    public DemoCalendarWithBackgroundEvent() {
    	getStyle().set("flex-grow", "1");

        createCalendarInstance();
        addBackgroundEvent();

        add(new H3("Calendar with background event"), calendar);
        
        setFlexGrow(1, calendar);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createCalendarInstance() {
        calendar = FullCalendarBuilder.create().withScheduler().build();
        ((FullCalendarScheduler) calendar).setSchedulerLicenseKey("GPL-My-Project-Is-Open-Source");
        ((FullCalendarScheduler) calendar).setSlotWidth("150");
        
        calendar.setHeight(500);
        calendar.changeView(SchedulerView.TIMELINE_MONTH);
    }

    private void addBackgroundEvent() {
        LocalDate now = LocalDate.now();
        
        ResourceEntry entry = new ResourceEntry();
        
        entry.setTitle("This special holiday");
        entry.setStart(now.withDayOfMonth(4).atStartOfDay(), calendar.getTimezone());
        entry.setEnd(entry.getStartUTC().plus(1, ChronoUnit.DAYS));
        entry.setAllDay(true);
        entry.setColor("red");
        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        entry.setResourceEditable(true);
        
        calendar.addEntry(entry);
    }

}
package org.vaadin.stefan;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.Entry.RenderingMode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Route(value = "demobackgroundevent", layout = MainView.class)
@PageTitle("FC with Background Events")
public class DemoCalendarWithBackgroundEvent extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    
    private FullCalendar calendar;

    public DemoCalendarWithBackgroundEvent() {
    	getStyle().set("flex-grow", "1");

        createCalendarInstance();
        addBackgroundEvent(Entry.RenderingMode.BACKGROUND, 4, "This special holiday");
        addBackgroundEvent(Entry.RenderingMode.BACKGROUND, 6, "");

        add(calendar);
        
        setFlexGrow(1, calendar);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createCalendarInstance() {
        calendar = FullCalendarBuilder.create().withScheduler().build();
        ((FullCalendarScheduler) calendar).setSchedulerLicenseKey("GPL-My-Project-Is-Open-Source");
        ((FullCalendarScheduler) calendar).setSlotMinWidth("150");
        calendar.setLocale(Locale.ENGLISH);
        
        calendar.setHeight(500);
        calendar.changeView(SchedulerView.TIMELINE_MONTH);
    }

    private void addBackgroundEvent(RenderingMode renderingMode, int offset, String title) {
        LocalDate now = LocalDate.now();
        
        ResourceEntry entry = new ResourceEntry();
        
        entry.setTitle(title);
        entry.setStart(now.withDayOfMonth(offset).atStartOfDay(), calendar.getTimezone());
        entry.setEnd(entry.getStartUTC().plus(1, ChronoUnit.DAYS));
        entry.setAllDay(true);
        entry.setColor("red");
        entry.setRenderingMode(renderingMode);
        entry.setResourceEditable(true);
        
        calendar.addEntry(entry);
    }
}
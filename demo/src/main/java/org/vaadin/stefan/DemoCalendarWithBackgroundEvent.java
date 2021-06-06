package org.vaadin.stefan;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.stefan.fullcalendar.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DemoCalendarWithBackgroundEvent extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    public DemoCalendarWithBackgroundEvent() {
        initBasicDemo();
    }

    private void initBasicDemo() {
        FullCalendar calendar = FullCalendarBuilder.create().withScheduler().build();
        ((FullCalendarScheduler) calendar).setSchedulerLicenseKey("GPL-My-Project-Is-Open-Source");
        ((FullCalendarScheduler) calendar).setSlotWidth("150");
        addBackgroundEvent(calendar);
        calendar.setHeight(500);
        calendar.changeView(SchedulerView.TIMELINE_MONTH);
        add(new H1("Calendar with background event"), calendar);
    }

    private void addBackgroundEvent(FullCalendar calendar) {
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

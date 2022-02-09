package org.vaadin.stefan.ui.view.demos.backgroundevent;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.Entry.RenderingMode;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Route(value = "demobackgroundevent", layout = MainLayout.class)
@PageTitle("FC with Background Events")
@MenuItem(label = "Background Events")
public class DemoCalendarWithBackgroundEvent extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    
    private FullCalendar calendar;

    public DemoCalendarWithBackgroundEvent() {
    	initView();

        createCalendarInstance();
        addBackgroundEvent(Entry.RenderingMode.BACKGROUND, 4, "This special holiday");
        addBackgroundEvent(Entry.RenderingMode.BACKGROUND, 6, "");

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

    private void addBackgroundEvent(RenderingMode renderingMode, int offset, String title) {
        LocalDate now = LocalDate.now();
        
        ResourceEntry entry = new ResourceEntry();
        
        entry.setTitle(title);
        entry.setStart(now.withDayOfMonth(offset));
        entry.setEnd(entry.getStart().plus(1, ChronoUnit.DAYS));
        entry.setAllDay(true);
        entry.setColor("red");
        entry.setRenderingMode(renderingMode);
        entry.setResourceEditable(true);
        
        calendar.addEntry(entry);
    }
}
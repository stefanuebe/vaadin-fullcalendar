package org.vaadin.stefan.ui.view.demos.customdaygrid;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.vaadin.stefan.fullcalendar.CalendarLocale;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.model.Header;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterItem;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPart;
import org.vaadin.stefan.ui.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

@Route(value = "demodaygridsixweeks", layout = MainLayout.class)
@PageTitle("FC with Six Weeks Grid")
@MenuItem(label = "Six Weeks Grid")
public class DemoDayGridWeekWithSixWeeks extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    
    private FullCalendar calendar;

    public DemoDayGridWeekWithSixWeeks() {
    	initView();
    	
    	createCalendarInstance();
    	
    	addAndExpand(calendar);
    	
    	addEntries();
    }
    
    private void initView() {
    	setSizeFull();
    }

    private void createCalendarInstance() {
        CustomDayGridWeekCalendarView calendarView = new CustomDayGridWeekCalendarView(6);
        
        calendar = FullCalendarBuilder.create().withInitialOptions(calendarView.getInitialOptions()).build();
        calendar.setSizeFull();
        calendar.setLocale(CalendarLocale.getDefault());

        Header testHeader = new Header();
        
        HeaderFooterPart headerLeft = testHeader.getStart();
        headerLeft.addItem(HeaderFooterItem.TITLE);
        
        HeaderFooterPart headerRight = testHeader.getEnd();
        headerRight.addItem(HeaderFooterItem.BUTTON_PREVIOUS);
        headerRight.addItem(HeaderFooterItem.BUTTON_TODAY);
        headerRight.addItem(HeaderFooterItem.BUTTON_NEXT);
        
        calendar.setHeaderToolbar(testHeader);

        calendar.changeView(calendarView);
    }
    
    private void addEntries() {
    	LocalDate now = LocalDate.now();
        
        Entry entry_1 = new Entry(UUID.randomUUID().toString());
        
        entry_1.setColor("lightgreen");
        entry_1.setTitle("Entry 1 - testgroup");
        entry_1.setStart(now.atStartOfDay());
        entry_1.setEnd(now.plusDays(1).atTime(LocalTime.MAX));
        entry_1.setGroupId("testgroup");
        
        Entry entry_2 = new Entry(UUID.randomUUID().toString());
        
        entry_2.setColor("lightgreen");
        entry_2.setTitle("Entry 2");
        entry_2.setStart(now.minusDays(3).atStartOfDay());
        entry_2.setEnd(now.minusDays(2).atTime(LocalTime.MAX));
        entry_2.setGroupId("testgroup");
        
        Entry entry_3 = new Entry(UUID.randomUUID().toString());
        
        entry_3.setColor("lightred");
        entry_3.setTitle("Entry 3 - ungrouped");
        entry_3.setStart(now.plusDays(2).atStartOfDay());
        entry_3.setEnd(now.plusDays(5).atTime(LocalTime.MAX));
        
        calendar.addEntries(entry_1, entry_2, entry_3);
    }
}
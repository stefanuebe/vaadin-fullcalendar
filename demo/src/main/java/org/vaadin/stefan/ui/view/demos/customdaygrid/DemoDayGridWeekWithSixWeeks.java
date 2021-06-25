package org.vaadin.stefan.ui.view.demos.customdaygrid;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.vaadin.stefan.fullcalendar.CalendarLocale;
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
}
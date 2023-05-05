package org.vaadin.stefan.ui.view.demos.customtimeline;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.model.Header;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterItem;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPart;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

@Route(value = "demotimelinecustomdays", layout = MainLayout.class)
@MenuItem(label = "28 Days Timeline")
public class DemoTimelineWith28Days extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    
    private FullCalendarScheduler calendar;
    private final CustomDaysTimelineCalendarView calendarView;

    public DemoTimelineWith28Days() {
        calendarView = new CustomDaysTimelineCalendarView(28);
    	initView();
    	
    	createCalendarInstance();

        add(new Button("Change", ev -> calendar.changeView(calendarView)));
    	add(calendar);
        setFlexGrow(1, calendar);
    }
    
    private void initView() {
    	setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createCalendarInstance() {
        calendar = FullCalendarBuilder.create().withScheduler("GPL-My-Project-Is-Open-Source").withInitialOptions(calendarView.getInitialOptions()).build();
        calendar.setLocale(CalendarLocale.getDefaultLocale());
        calendar.setHeight("100%");

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
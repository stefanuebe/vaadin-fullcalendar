package org.vaadin.stefan;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;
import org.vaadin.stefan.fullcalendar.model.Header;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterItem;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPart;

@Route(value = "demotimelinecustomdays", layout = MainView.class)
public class DemoTimelineWith28Days extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    
    private FullCalendar calendar;

    public DemoTimelineWith28Days() {
    	getStyle().set("flex-grow", "1");
    	
    	createCalendarInstance();
    	
    	add(new H3("Timeline calendar with 28 days"), calendar);
    	
    	setFlexGrow(1, calendar);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createCalendarInstance() {
        CustomDaysTimelineCalendarView calendarView = new CustomDaysTimelineCalendarView(28);
        
        calendar = FullCalendarBuilder.create().withScheduler().withInitialOptions(calendarView.getInitialOptions()).build();
        ((FullCalendarScheduler) calendar).setSchedulerLicenseKey("GPL-My-Project-Is-Open-Source");
        
        Header testHeader = new Header();
        
        HeaderFooterPart headerLeft = testHeader.getLeft();
        headerLeft.addItem(HeaderFooterItem.TITLE);
        
        HeaderFooterPart headerRight = testHeader.getRight();
        headerRight.addItem(HeaderFooterItem.BUTTON_PREVIOUS);
        headerRight.addItem(HeaderFooterItem.BUTTON_TODAY);
        headerRight.addItem(HeaderFooterItem.BUTTON_NEXT);
        
        calendar.setHeader(testHeader);
        
        calendar.setHeight(500);
        calendar.changeView(calendarView);
    }

    static class CustomDaysTimelineCalendarView implements CalendarView {

        private final int numberOfDays;

        public CustomDaysTimelineCalendarView(int numberOfDays) {
            this.numberOfDays = numberOfDays;
        }

        @Override
        public String getClientSideValue() {
            return "customTimeline";
        }

        /**
         * views: {
         * customTimeline: {
         * type: 'timeline',
         * duration: { days: 31 }
         * }
         * }
         *
         * @return
         */
        public JreJsonObject getInitialOptions() {
            JsonFactory factory = new JreJsonFactory();
            JreJsonObject initialOptions = new JreJsonObject(factory);
            
            JreJsonObject durationHolder = new JreJsonObject(factory);
            durationHolder.set("days", factory.create(numberOfDays));
            
            JreJsonObject customViewHolder = new JreJsonObject(factory);
            customViewHolder.set("type", factory.create("timeline"));
            customViewHolder.set("duration", durationHolder);
            
            JreJsonObject viewsHolder = new JreJsonObject(factory);
            viewsHolder.set(getName(), customViewHolder);
            
            initialOptions.set("views", viewsHolder);
            
            return initialOptions;
        }

        @Override
        public String getName() {
            return "customTimeline";
        }
    }

}
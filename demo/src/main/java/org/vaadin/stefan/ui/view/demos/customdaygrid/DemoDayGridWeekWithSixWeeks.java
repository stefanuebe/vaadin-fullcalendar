package org.vaadin.stefan;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;

import java.util.Locale;

import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.model.Header;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterItem;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPart;

@Route(value = "demodaygridsixweeks", layout = MainView.class)
@PageTitle("FC with Six Weeks Grid")
public class DemoDayGridWeekWithSixWeeks extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    
    private FullCalendar calendar;

    public DemoDayGridWeekWithSixWeeks() {
    	getStyle().set("flex-grow", "1");
    	
    	createCalendarInstance();
    	
    	add(calendar);
        
    	setFlexGrow(1, calendar);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createCalendarInstance() {
        CustomDayGridWeekCalendarView calendarView = new CustomDayGridWeekCalendarView(6);
        
        calendar = FullCalendarBuilder.create().withInitialOptions(calendarView.getInitialOptions()).build();
        calendar.setLocale(Locale.ENGLISH);

        Header testHeader = new Header();
        
        HeaderFooterPart headerLeft = testHeader.getStart();
        headerLeft.addItem(HeaderFooterItem.TITLE);
        
        HeaderFooterPart headerRight = testHeader.getEnd();
        headerRight.addItem(HeaderFooterItem.BUTTON_PREVIOUS);
        headerRight.addItem(HeaderFooterItem.BUTTON_TODAY);
        headerRight.addItem(HeaderFooterItem.BUTTON_NEXT);
        
        calendar.setHeaderToolbar(testHeader);

        calendar.setHeight(500);
        calendar.changeView(calendarView);
    }

    static class CustomDayGridWeekCalendarView implements CalendarView {

        private final int numberOfWeeks;

        public CustomDayGridWeekCalendarView(int numberOfWeeks) {
            this.numberOfWeeks = numberOfWeeks;
        }

        @Override
        public String getClientSideValue() {
            return "customDayGridWeek";
        }

        /**
         * views: {
         * 'customDayGridWeek': {
         * type: 'dayGridWeek',
         * duration: { weeks: 6 }
         * }
         * },
         *
         * @return
         */
        public JreJsonObject getInitialOptions() {
            JsonFactory factory = new JreJsonFactory();
            JreJsonObject initialOptions = new JreJsonObject(factory);
            
            JreJsonObject durationHolder = new JreJsonObject(factory);
            durationHolder.set("weeks", factory.create(numberOfWeeks));
            
            JreJsonObject customViewHolder = new JreJsonObject(factory);
            customViewHolder.set("type", factory.create("dayGridWeek"));
            customViewHolder.set("duration", durationHolder);
            
            JreJsonObject viewsHolder = new JreJsonObject(factory);
            viewsHolder.set(getName(), customViewHolder);
            
            initialOptions.set("views", viewsHolder);
            
            return initialOptions;
        }

        @Override
        public String getName() {
            return "customDayGridWeek";
        }
    }

}
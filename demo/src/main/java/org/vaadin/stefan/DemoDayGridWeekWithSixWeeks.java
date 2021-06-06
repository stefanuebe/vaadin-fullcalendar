package org.vaadin.stefan;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;

public class DemoDayGridWeekWithSixWeeks extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    public DemoDayGridWeekWithSixWeeks() {
        initBasicDemo();
    }

    private void initBasicDemo() {
        CustomDayGridWeekCalendarView calendarView = new CustomDayGridWeekCalendarView(6);
        FullCalendar calendar = FullCalendarBuilder.create().withInitialOptions(calendarView.getInitialOptions()).build();
        calendar.setHeight(500);
        calendar.changeView(calendarView);
        add(new H1("DayGridWeek calendar with six weeks"), calendar);
        setSizeFull();
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

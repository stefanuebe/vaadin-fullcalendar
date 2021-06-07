package org.vaadin.stefan;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;
import org.vaadin.stefan.fullcalendar.CalendarView;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

public class DemoTimelineWith28Days extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    public DemoTimelineWith28Days() {
        initBasicDemo();
    }

    private void initBasicDemo() {
        CustomDaysTimelineCalendarView calendarView = new CustomDaysTimelineCalendarView(28);
        FullCalendar calendar = FullCalendarBuilder.create().withScheduler().withInitialOptions(calendarView.getInitialOptions()).build();
        ((FullCalendarScheduler) calendar).setSchedulerLicenseKey("GPL-My-Project-Is-Open-Source");
        calendar.setHeight(500);
        calendar.changeView(calendarView);
        add(new H1("Timeline calendar with 28 days"), calendar);
        setSizeFull();
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

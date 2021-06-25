package org.vaadin.stefan.ui.view.demos.customdaygrid;

import org.vaadin.stefan.fullcalendar.CalendarView;

import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;

public class CustomDayGridWeekCalendarView implements CalendarView {
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

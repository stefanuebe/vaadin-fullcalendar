package org.vaadin.stefan.ui.view.demos.customtimeline;

import org.vaadin.stefan.fullcalendar.CalendarView;

import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;

public class FixedDaysCalendarView implements CalendarView {
	private final int numberOfDays;

    public FixedDaysCalendarView(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    @Override
    public String getClientSideValue() {
        return "customView";
    }

    /**
     * views: {
     * customView: {
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
        customViewHolder.set("type", factory.create("resourceTimeline"));
        customViewHolder.set("duration", durationHolder);

        JreJsonObject viewsHolder = new JreJsonObject(factory);
        viewsHolder.set(getName(), customViewHolder);

        initialOptions.set("views", viewsHolder);

        return initialOptions;
    }

    @Override
    public String getName() {
        return "customView";
    }
}

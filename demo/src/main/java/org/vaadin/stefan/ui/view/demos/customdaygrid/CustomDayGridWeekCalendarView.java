package org.vaadin.stefan.ui.view.demos.customdaygrid;

import elemental.json.Json;
import elemental.json.JsonObject;
import org.vaadin.stefan.fullcalendar.CalendarView;

import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;

/**
 * Custom implementation of a calendar view. The customization happens via the initial options
 * of the calendar, which will be transported to the client side.
 */
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
    public void extendInitialOptions(JsonObject initialOptions) {
        JsonObject durationHolder = Json.createObject();
        durationHolder.put("weeks", Json.create(numberOfWeeks));

        JsonObject customViewHolder = Json.createObject();
        customViewHolder.put("type", Json.create("dayGridWeek"));
        customViewHolder.put("duration", durationHolder);

        JsonObject viewsHolder = Json.createObject();
        viewsHolder.put("customDayGridWeek", customViewHolder);

        initialOptions.put("views", viewsHolder);
    }

    @Override
    public String getName() {
        return "customDayGridWeek";
    }
}

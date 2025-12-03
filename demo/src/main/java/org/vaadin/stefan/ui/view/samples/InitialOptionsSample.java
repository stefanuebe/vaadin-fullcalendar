package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;

/**
 * @author Stefan Uebe
 */
public class InitialOptionsSample extends AbstractSample {
    @Override
    protected void buildSample(FullCalendar calendar) {
        JsonObject initialOptions = Json.createObject();
        initialOptions.put("height", "100%");
        initialOptions.put("timeZone", "UTC");
        initialOptions.put("header", false);
        initialOptions.put("weekNumbers", true);
        initialOptions.put("eventLimit", false); // pass an int value to limit the entries per day
        initialOptions.put("navLinks", true);
        initialOptions.put("selectable", true);
        calendar = FullCalendarBuilder.create().withScheduler().withInitialOptions(initialOptions).build();
    }
}

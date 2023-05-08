package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

/**
 * @author Stefan Uebe
 */
public class BasicInteractionSample extends AbstractSample{
    @Override
    protected void buildSample(FullCalendar calendar) {
        calendar.addTimeslotsSelectedListener((event) -> {
            // react on the selected timeslot, for instance create a new instance and let the user edit it
            Entry entry = new Entry();

            entry.setStart(event.getStart()); // also event times are always utc based
            entry.setEnd(event.getEnd());
            entry.setAllDay(event.isAllDay());

            entry.setColor("dodgerblue");

            // ... show and editor
        });

        calendar.addEntryClickedListener((event) -> {
            // react on the clicked entry, for instance let the user edit it
            Entry entry = event.getEntry();

            // ... show and editor
        });
    }


}

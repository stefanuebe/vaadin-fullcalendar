package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;

/**
 * @author Stefan Uebe
 */
public class BaseSample extends AbstractSample {
    @Override
    protected void buildSample(FullCalendar calendar) {
        // Create a initial sample entry
        Entry entry = new Entry();
        entry.setTitle("Some event");
        entry.setColor("#ff3333");

        // the given times will be interpreted as utc based - useful when the times are fetched from your database
        entry.setStart(LocalDate.now().withDayOfMonth(3).atTime(10, 0));
        entry.setEnd(entry.getStart().plusHours(2));

        // FC uses a data provider concept similar to the Vaadin default's one, with some differences
        // By default the FC uses a in-memory data provider, which is sufficient for most basic use cases.
        calendar.getEntryProvider().asInMemory().addEntries(entry);
    }
}

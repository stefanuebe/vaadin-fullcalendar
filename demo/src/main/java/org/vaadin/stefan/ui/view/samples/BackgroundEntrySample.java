package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.DisplayMode;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

/**
 * @author Stefan Uebe
 */
public class BackgroundEntrySample extends AbstractSample {

    @Override
    protected void buildSample(FullCalendar calendar) {
        Entry entry = new Entry();
        // ... setup entry details

        entry.setDisplayMode(DisplayMode.BACKGROUND);

        // add the entry to the calendar, e.g. for an in-memory provider:
        calendar.getEntryProvider().asInMemory().addEntry(entry);
    }
}

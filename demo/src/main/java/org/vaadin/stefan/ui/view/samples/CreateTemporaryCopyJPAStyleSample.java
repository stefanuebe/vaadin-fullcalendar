package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

/**
 * @author Stefan Uebe
 */
public class CreateTemporaryCopyJPAStyleSample extends AbstractSample{

    private Entry entry;

    @Override
    protected void buildSample(FullCalendar calendar) {
        createCopy();
    }

    private Entry createCopy() {
        Entry tmpEntry = entry.copy(); // create a temporary copy

        // ... modify the temporary copy

        // return a new copy at the end without changing the initial entry
        return tmpEntry.copy();
    }
}

package org.vaadin.stefan.ui.view.samples;

import com.vaadin.flow.data.binder.Binder;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

/**
 * @author Stefan Uebe
 */
public class CreateTemporaryCopySample extends AbstractSample {

    private Entry entry;

    @Override
    protected void buildSample(FullCalendar calendar) {
        Entry tmpEntry = entry.copy(); // create a temporary copy
        // you may also call copyAsType to allow the copy to be of a different type

        Binder<Entry> binder = new Binder<>();

        // ... init binder

        binder.setBean(tmpEntry); // you can of course also use the read/writeBean api

        // modify the bound fields

        if (binder.validate().isOk()) {
            entry.copyFrom(tmpEntry); // this will overwrite the entry with the values of the tmpEntry
            // ... update the backend as needed, e.g. by calling refreshItem on the entry provider
        }
    }
}

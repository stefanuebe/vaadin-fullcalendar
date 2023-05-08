package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;

/**
 * @author Stefan Uebe
 */
public class HandleDataChangeInEventSample extends AbstractSample {

    private LocalDate someRequiredMinimalDate;

    @Override
    protected void buildSample(FullCalendar calendar) {
        // directly apply the changes
        calendar.addEntryDroppedListener(event -> {
            event.applyChangesOnEntry(); // includes now the allDay attribute if sent by client
        });

        // create a copy to do some business logic checks
        calendar.addEntryDroppedListener(event -> {
            Entry copy = event.createCopyBasedOnChanges();

            if(copy.getStartAsLocalDate().isBefore(someRequiredMinimalDate) /* do some background checks on the changed data */){
                event.applyChangesOnEntry();
                event.getSource().getEntryProvider().refreshItem(event.getEntry()); // refresh the entry to update the UI
            }
        });
    }
}

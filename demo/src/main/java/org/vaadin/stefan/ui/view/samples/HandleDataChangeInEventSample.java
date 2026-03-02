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
    protected void buildSample(FullCalendar<Entry> calendar) {
        // directly apply the changes
        calendar.addCalendarItemDroppedListener(event -> {
            event.applyChangesOnItem(); // includes now the allDay attribute if sent by client
        });

        // apply changes and refresh conditionally after some business logic checks
        calendar.addCalendarItemDroppedListener(event -> {
            // Use getChanges() to inspect the incoming new values before applying them
            boolean startOk = event.getChanges().getChangedStart()
                    .map(s -> !s.toLocalDate().isBefore(someRequiredMinimalDate))
                    .orElse(true);

            if (startOk /* do some background checks on the changed data */) {
                event.applyChangesOnItem();
                event.getSource().getEntryProvider().refreshItem(event.getItem()); // refresh the entry to update the UI
            }
        });
    }
}

package org.vaadin.stefan.ui.view;

import com.vaadin.flow.component.ComponentEventListener;
import org.vaadin.stefan.fullcalendar.*;

/**
 * @author Stefan Uebe
 */
public abstract class AbstractSchedulerView extends AbstractCalendarView {
    public AbstractSchedulerView() {
        FullCalendarScheduler calendar = (FullCalendarScheduler) getCalendar();

        calendar.addEntryDroppedSchedulerListener(this::onEntryDroppedScheduler);
        calendar.addTimeslotsSelectedSchedulerListener(this::onTimeslotsSelectedScheduler);
        calendar.addTimeslotClickedSchedulerListener(this::onTimeslotClickedScheduler);
    }

    /**
     * Called by the calendar's entry drop listener (i. e. an entry has been dragged around / moved by the user).
     * Applies the changes to the entry and calls {@link #onEntryChanged(Entry)} by default.<br>
     * <br>
     * This is the scheduler variant, which also includes resource information.
     *
     * @param event event
     * @see FullCalendarScheduler#addEntryDroppedSchedulerListener(ComponentEventListener)
     */
    protected void onEntryDroppedScheduler(EntryDroppedSchedulerEvent event) {
        event.applyChangesOnEntry();
        onEntryChanged(event.getEntry());
    }

    /**
     * Called by the calendar's timeslot selected listener. Noop by default.<br>
     * <br>
     * This is the scheduler variant, which also includes resource information.
     *
     * @param event event
     * @see FullCalendarScheduler#addTimeslotsSelectedSchedulerListener(ComponentEventListener)
     */
    protected void onTimeslotsSelectedScheduler(TimeslotsSelectedSchedulerEvent event) {

    }

    /**
     * Called by the calendar's timeslot clicked listener. Noop by default.<br>
     * <br>
     * This is the scheduler variant, which also includes resource information.
     *
     * @param event event
     * @see FullCalendarScheduler#addTimeslotClickedSchedulerListener(ComponentEventListener)
     */
    protected void onTimeslotClickedScheduler(TimeslotClickedSchedulerEvent event) {

    }

}

package org.vaadin.stefan.ui.view;

import com.vaadin.flow.component.ComponentEventListener;
import org.vaadin.stefan.fullcalendar.*;

/**
 * @author Stefan Uebe
 */
public abstract class AbstractSchedulerView extends AbstractCalendarView {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AbstractSchedulerView() {
        FullCalendarScheduler calendar = (FullCalendarScheduler) getCalendar();

        calendar.addCalendarItemDroppedSchedulerListener(event -> onEntryDroppedScheduler((CalendarItemDroppedSchedulerEvent<Entry>) event));
        calendar.addTimeslotsSelectedSchedulerListener(event -> onTimeslotsSelectedScheduler((TimeslotsSelectedSchedulerEvent) event));
        calendar.addTimeslotClickedSchedulerListener(event -> onTimeslotClickedScheduler((TimeslotClickedSchedulerEvent) event));
    }

    /**
     * Called by the calendar's entry drop listener (i.e. an entry has been dragged around / moved by the user).
     * Applies the changes to the entry and calls {@link #onEntryChanged(Entry)} by default.<br>
     * <br>
     * This is the scheduler variant, which also includes resource information.
     *
     * @param event event
     * @see FullCalendarScheduler#addCalendarItemDroppedSchedulerListener(ComponentEventListener)
     */
    protected void onEntryDroppedScheduler(CalendarItemDroppedSchedulerEvent<Entry> event) {
        event.applyChangesOnItem();
        onEntryChanged(event.getItem());
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

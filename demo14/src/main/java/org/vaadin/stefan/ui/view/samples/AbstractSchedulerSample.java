package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

/**
 * @author Stefan Uebe
 */
public abstract class AbstractSchedulerSample extends AbstractSample {
    @Override
    protected FullCalendarScheduler createCalendar() {
        return new FullCalendarScheduler();
    }
}
